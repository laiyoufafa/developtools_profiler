/*
 * Copyright (c) 2022 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <linux/version.h>
#include "vmlinux.h"
#include "bpf_helpers.h"
#include "bpf_tracing.h"
#include "bpf_core_read.h"
#include "hiebpf_types.h"
#include "bpf_log_writer.h"

#ifndef VM_FAULT_ERROR
#define VM_FAULT_ERROR (VM_FAULT_OOM | VM_FAULT_SIGBUS |	\
			VM_FAULT_SIGSEGV | VM_FAULT_HWPOISON |	\
			VM_FAULT_HWPOISON_LARGE | VM_FAULT_FALLBACK)
#endif

// global configuration data
// const volatile int tracer_pid = -1;
// const volatile int bpf_log_level = BPF_LOG_DEBUG;
// const volatile int max_stack_depth = MAX_STACK_LIMIT;
// const volatile int target_pids[MAX_TARGET_PIDS];

/*********************************************************************************
 * use BPF map to store global configuration data instead if global variables
 * are not available
 ********************/
struct {
    /*
     * config_var_map[0] = tracer_pid, tracer_pid != -1 means not tracing the tracer itself
     * config_var_map[1] = bpf_log_level
     * config_var_map[2] = max_stack_limit
     * config_var_map[3] = max_stack_depth,
     * config_var_map[4] = unwind_stack, none-zero means unwinding stack, other wise not unwinding
    */
    __uint(type, BPF_MAP_TYPE_ARRAY);
    __uint(key_size, sizeof(u32));
    __uint(value_size, sizeof(u32));
    __uint(max_entries, NR_CONFIG_VARIABLES);
} config_var_map SEC(".maps");

struct {
    /*
     * target_pid_map[0] != 0 means tracing all processes
     */
    __uint(type, BPF_MAP_TYPE_ARRAY);
    __uint(key_size, sizeof(u32));
    __uint(value_size, sizeof(u32));
    __uint(max_entries, MAX_TARGET_PIDS + 1);
} target_pid_map SEC(".maps");
/**********************
*************************************************************************************/


/******************************** BPF maps BEGIN*************************************/
/*start event map*/
struct {
    /* Since execution of syscalls of the same process never cross over,
     * we can simply use pid as the identifier of the start of a syscall
     */
	__uint(type, BPF_MAP_TYPE_HASH);
	__uint(key_size, sizeof(u64));
	__uint(value_size, sizeof(struct start_event_t));
    __uint(max_entries, MAX_START_EVENTS_NUM);
} start_event_map SEC(".maps");

/*pftrace stats map*/
struct {
    __uint(type, BPF_MAP_TYPE_ARRAY);
    __uint(key_size, sizeof(u32));
    __uint(value_size, sizeof(struct pf_stat_t));
    __uint(max_entries, PF_MAX_EVENT_TYPE);
} pftrace_stats_map SEC(".maps");

/*bpf ringbuffers*/
struct {
    __uint(type, BPF_MAP_TYPE_RINGBUF);
    __uint(max_entries, BPF_RINGBUF_SIZE);
} bpf_ringbuf_map SEC(".maps");

struct {
    __uint(type, BPF_MAP_TYPE_ARRAY_OF_MAPS);
    __uint(key_size, sizeof(u32));
    __uint(value_size, sizeof(u32));
    __uint(max_entries, NUM_STACK_TRACE_MAPS);
} ustack_maps_array SEC(".maps");
/********************************* BPF maps END *************************************/


/******************************** inline funcs BEGIN ********************************/
static __always_inline
int unwind_stack()
{
    u32 index = UNWIND_FLAG_INDEX;
    const u32 *unwind_ptr = bpf_map_lookup_elem(&config_var_map, &index);
    u32 unwind = 0;
    int err = bpf_probe_read_kernel(&unwind, sizeof(u32), unwind_ptr);
    if (err) {
        BPFLOGW(BPF_TRUE, "failed to read unwind configuration");
    }
    return unwind;
}

static __always_inline
u32 get_max_stack_depth()
{
    u32 index = MAX_STACK_DEPTH_INDEX;
    const u32 *max_stack_depth_ptr = bpf_map_lookup_elem(&config_var_map, &index);
    u32 max_stack_depth = 0;
    int err = bpf_probe_read_kernel(&max_stack_depth, sizeof(u32), max_stack_depth_ptr);
    if (err) {
        BPFLOGW(BPF_TRUE, "failed to read max stack depth");
    }
    return max_stack_depth;
}

static __always_inline
int emit_fstrace_event(void* ctx, int64_t retval)
{
    // get exit timestamp as soon as possible
    u64 ctime = bpf_ktime_get_ns();
    u64 pid_tgid = bpf_get_current_pid_tgid();

    const u64 event_size = sizeof(struct fstrace_cmplt_event_t);
    struct fstrace_cmplt_event_t *cmplt_event = bpf_ringbuf_reserve(&bpf_ringbuf_map, event_size, 0);
    if (cmplt_event == NULL) {
        BPFLOGD(BPF_TRUE, "failed to reserve space for fstrace event from BPF ringbuffer");
        return -1;
    }
    __builtin_memset(cmplt_event, 0, event_size);

    const struct fstrace_start_event_t* start_event = bpf_map_lookup_elem(&start_event_map, &pid_tgid);
    int err = bpf_probe_read_kernel(&cmplt_event->start_event, sizeof(struct fstrace_start_event_t), start_event);
    if (err) {
        BPFLOGD(BPF_TRUE, "fstrace event discarded: failed to read fstrace_start_event");
        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
        return -1;
    }
    if (cmplt_event->start_event.type == 0) {
        BPFLOGI(BPF_TRUE, "fstrace event discarded: invalide fstrace start event");
        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
        return -1;
    }

    cmplt_event->ctime = ctime;
    cmplt_event->tracer = FSTRACE;
    cmplt_event->pid = (u32) pid_tgid;
    cmplt_event->tgid = (u32) (pid_tgid >> 32);
    cmplt_event->uid = bpf_get_current_uid_gid();
    cmplt_event->gid = (bpf_get_current_uid_gid() >> 32);
    cmplt_event->retval = retval;
    cmplt_event->nips = 0;
    cmplt_event->ustack_id = -1;
    err = bpf_get_current_comm(cmplt_event->comm, MAX_COMM_LEN);
    if (err) {
        BPFLOGD(BPF_TRUE, "fstrace event discarded: failed to get process command");
        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
        return -1;
    }

    // get user callchain
    if (unwind_stack()) {
        cmplt_event->nips = get_max_stack_depth();
        const u32 ustack_map_key = FSTRACE_STACK_TRACE_INDEX;
        void *ustack_map_ptr = bpf_map_lookup_elem(&ustack_maps_array, &ustack_map_key);
        if (ustack_map_ptr == NULL) {
            BPFLOGD(BPF_TRUE, "fstrace event discarded: failed to lookup ustack map");
            bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
            return -1;
        }
        cmplt_event->ustack_id = (int64_t) bpf_get_stackid(ctx, ustack_map_ptr, USER_STACKID_FLAGS);
        if (cmplt_event->ustack_id < 0) {
            BPFLOGD(BPF_TRUE, "fstrace event discarded: failed to unwind user callchain");
            bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
            return -1;
        }
    }

	// send out the complete event data to perf event buffer
    bpf_ringbuf_submit(cmplt_event, 0);
    return 0;
}

static __always_inline
int handle_pftrace_start_event(struct vm_fault *vmf, u32 type)
{
    struct start_event_t start_event = {};
    __builtin_memset(&start_event, 0, sizeof(start_event));
    struct pftrace_start_event_t *pf_se = &start_event.pf_se;
    pf_se->stime = bpf_ktime_get_ns();
    pf_se->type = type;
    int err = bpf_probe_read_kernel(&pf_se->addr, sizeof(vmf->address), &vmf->address);
    if (err) {
        BPFLOGW(BPF_TRUE, "failed read page fault address for pftrace start event");
        // clear the start event to indicate error
        __builtin_memset(&start_event, 0, sizeof(start_event));
    }
    u64 pid_tgid = bpf_get_current_pid_tgid();
    err = (int) bpf_map_update_elem(&start_event_map, &pid_tgid, &start_event, BPF_ANY);
    if (err < 0) {
        // this should never happens
        BPFLOGF(BPF_TRUE, "failed to update pftrace_start_event for pid_tgid = %lld", pid_tgid);
        return -1;
    }
    return 0;
}

static __always_inline
int read_modify_update_page_fault_stats(u32 type, u32 duration)
{
    const struct pf_stat_t *stat_ptr = bpf_map_lookup_elem(&pftrace_stats_map, &type);
    if (stat_ptr == NULL) {
        BPFLOGD(BPF_TRUE, "failed to lookup pftrace stat");
        return -1;
    }
    struct pf_stat_t stat;
    int err = (int) bpf_probe_read_kernel(&stat, sizeof(stat), stat_ptr);
    if (err < 0) {
        BPFLOGD(BPF_TRUE, "failed to read pftrace stat");
        return -1;
    }
    u32 tot_duration = stat.tot_duration + duration;
    u32 avg_duration = tot_duration / (stat.count + 1);
    stat.dev_duration = (duration * duration + stat.count * stat.dev_duration 
                        + stat.tot_duration * stat.avg_duration - tot_duration * avg_duration)
                        / (stat.count + 1);
    ++stat.count;
    stat.tot_duration = tot_duration;
    stat.avg_duration = avg_duration;
    if (duration < stat.min_duration) {
        stat.min_duration = duration;
    } else if (duration > stat.max_duration) {
        stat.max_duration = duration;
    }
    err = (int) bpf_map_update_elem(&pftrace_stats_map, &type, &stat, BPF_ANY);
    if (err < 0) {
        BPFLOGD(BPF_TRUE, "failed to update pftrace stat");
        return -1;
    }

    return 0;
}

static __always_inline
int read_modify_update_current_type(u32 type)
{
    u64 pid_tgid = bpf_get_current_pid_tgid();
    const struct pftrace_start_event_t *se_ptr = bpf_map_lookup_elem(&start_event_map, &pid_tgid);
    struct pftrace_start_event_t start_event;
    int err = bpf_probe_read_kernel(&start_event, sizeof(start_event), se_ptr);
    if (err) {
        BPFLOGW(BPF_TRUE, "failed to read pftrace start event to update event type");
        return 0;
    }
    u32 old_type = start_event.type;
    if (type) {
        start_event.type = type;
    }
    return old_type;
}

static __always_inline
int handle_return_value(struct pftrace_cmplt_event_t* cmplt_event, long long retval)
{
    switch (read_modify_update_current_type(0)) {
        case PF_PAGE_CACHE_HIT:
        {
            struct file *fpin = (struct file *) retval;
            if (fpin == NULL) {
                cmplt_event->size = 0;
            } else {
                cmplt_event->size = 1;
            }
            break;
        }
        case PF_FILE_BACKED_IN:
        case PF_SWAP_FROM_ZRAM:
        case PF_SWAP_FROM_DISK:
        case PF_ZERO_FILL_PAGE:
        case PF_FAKE_ZERO_PAGE:
        case PF_COPY_ON_WRITE:
        {
            vm_fault_t vmf_flags = (vm_fault_t) retval;
            if (vmf_flags & VM_FAULT_ERROR) {
                cmplt_event->size = 0;
            } else {
                cmplt_event->size = 1;
            }
            break;
        }
        default: return -1;
    }
    return 0;
}

static __always_inline
int emit_pftrace_event(void* ctx, int64_t retval)
{
    // get timestamp as soon as possible
    u64 ctime =  bpf_ktime_get_ns();
    u64 pid_tgid = bpf_get_current_pid_tgid();

    const u64 event_size = sizeof(struct pftrace_cmplt_event_t);
    struct pftrace_cmplt_event_t* cmplt_event = bpf_ringbuf_reserve(&bpf_ringbuf_map, event_size, 0);
    if (cmplt_event == NULL) {
        BPFLOGD(BPF_TRUE, "failed to reserve space for pftrace event from BPF ringbuffer");
        return -1;
    }
    __builtin_memset(cmplt_event, 0, event_size);

    const struct pftrace_start_event_t *se_ptr = bpf_map_lookup_elem(&start_event_map, &pid_tgid);
    int err = bpf_probe_read_kernel(&cmplt_event->start_event, sizeof(cmplt_event->start_event), se_ptr);
    if (err) {
        BPFLOGI(BPF_TRUE, "pftrace event discarded: failed to read pftrace start event");
        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
        return -1;
    }
    if (cmplt_event->start_event.type == 0) {
        BPFLOGI(BPF_TRUE, "pftrace event discarded: invalide pftrace start event");
        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
        return -1;
    }
    cmplt_event->tracer = PFTRACE;
    cmplt_event->ctime = ctime;
    cmplt_event->pid = (u32) pid_tgid;
    cmplt_event->tgid = (pid_tgid >> 32);
    cmplt_event->uid = bpf_get_current_uid_gid();
    cmplt_event->gid = (bpf_get_current_uid_gid() >> 32);
    cmplt_event->nips = 0;
    cmplt_event->ustack_id = -1;
    err = bpf_get_current_comm(cmplt_event->comm, MAX_COMM_LEN);
    if (err < 0) {
        BPFLOGD(BPF_TRUE, "pftrace event discarded: failed to get process command");
        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
        return -1;
    }
    err = handle_return_value(cmplt_event, retval);
    if (err) {
        BPFLOGW(BPF_TRUE, "pftrace event discarded: failed to handle pftrace return value");
        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
        return -1;
    }

    // get user callchain
    if (unwind_stack()) {
        cmplt_event->nips = get_max_stack_depth();
        const u32 ustack_map_key = PFTRACE_STACK_TRACE_INDEX;
        void *ustack_map_ptr = bpf_map_lookup_elem(&ustack_maps_array, &ustack_map_key);
        if (ustack_map_ptr == NULL) {
            BPFLOGD(BPF_TRUE, "pftrace event discarded: failed to lookup ustack map");
            bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
            return -1;
        }
        cmplt_event->ustack_id = (int64_t) bpf_get_stackid(ctx, ustack_map_ptr, USER_STACKID_FLAGS);
        if (cmplt_event->ustack_id < 0) {
            BPFLOGD(BPF_TRUE, "pftrace event discarded: failed to unwind user callchain");
            // bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
            // return -1;
            cmplt_event->nips = 0;
        } else {
            cmplt_event->nips = MAX_STACK_DEPTH;
        }
    }

    if (read_modify_update_page_fault_stats(cmplt_event->start_event.type, cmplt_event->ctime - cmplt_event->start_event.stime) != 0) {
        BPFLOGD(BPF_TRUE, "pftrace event discarded: failed to update pftrace stats");
        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
        return -1;
    }
    bpf_ringbuf_submit(cmplt_event, 0);

    return 0;
}

static __always_inline
int emit_event(void* ctx, int64_t retval, u32 tracer)
{
    switch (tracer) {
        case FSTRACE:   return emit_fstrace_event(ctx, retval);
        case PFTRACE:   return emit_pftrace_event(ctx, retval);
    }
    BPFLOGD(BPF_TRUE, "unkonwn event source with id = %d", tracer);
    return -1;
}

static __always_inline
int is_target_process(const char* target_comm, const size_t comm_size)
{
    char curr_comm[MAX_COMM_LEN] = "\0";
	long retval = bpf_get_current_comm(curr_comm, sizeof(curr_comm));
	if (retval == 0) {
        size_t min_comm = comm_size < sizeof(curr_comm) ? comm_size : sizeof(curr_comm);
		for (size_t j = 0; j <= min_comm; ++j) {
			if (j == min_comm) {
				char fmt[] = "current comm is %s\n";
				bpf_trace_printk(fmt, sizeof(fmt), curr_comm);
				return 0;
			}
			if (target_comm[j] != curr_comm[j]) {
				break;
			}
		}
    }
    return -1;
}

// if both the pid and tgid values are - 1, check whether the event occurs in the current process
static __always_inline
int check_current_pid(const int32_t pid, const int32_t tgid)
{
    u32 index = 0;
    const int32_t* tracer_pid_ptr = bpf_map_lookup_elem(&config_var_map, &index);
    int32_t tracer_pid = -1;
    bpf_probe_read_kernel(&tracer_pid, sizeof(int32_t), tracer_pid_ptr);
    int32_t curr_pid = pid;
    if (curr_pid < 0) {
        curr_pid = bpf_get_current_pid_tgid();
    }
    int32_t curr_tgid = tgid;
    if (curr_tgid < 0) {
        curr_tgid = (bpf_get_current_pid_tgid() >> 32);
    }
    // BPFLOGD(
    //     BPF_TRUE,
    //     "tracer pid = %d, curr_pid = %d, curr_tgid = %d",
    //     tracer_pid, curr_pid, curr_tgid);
    if (curr_pid == tracer_pid || curr_tgid == tracer_pid) {
        // currrent process is not a target process
        return -1;
    }
    const u32 trace_all_index = 0;
    int32_t target_pids[MAX_TARGET_PIDS + 1];
    __builtin_memset(target_pids, 0, sizeof(target_pids));
    const int32_t *target_pids_ptr = bpf_map_lookup_elem(&target_pid_map, &trace_all_index);
    if (target_pids_ptr == NULL) {
        BPFLOGW(BPF_TRUE, "failed to lookup target pid map, will trace all processes");
        return 0;
    }
    int err = bpf_probe_read_kernel(target_pids, sizeof(int32_t) * (MAX_TARGET_PIDS + 1), target_pids_ptr);
    if (err) {
        BPFLOGW(BPF_TRUE, "failed to read target pids, will trace all processes");
        return 0;
    }
    if (target_pids[trace_all_index] != 0) {
        return 0;
    }
    for (u32 index = 1; index != MAX_TARGET_PIDS; ++index) {
        if (target_pids[index] < 0) {
            break;
        }
        if (target_pids[index] == curr_pid || target_pids[index] == curr_tgid) {
            return 0;
        }
    }
    return -1;
}

static __always_inline
int get_mountpoint_by_inode(char *filename, int len, const struct inode *host)
{
    const struct super_block *superBlock = NULL;
    if (0 != bpf_probe_read_kernel(&superBlock, sizeof(superBlock), &host->i_sb)) {
        BPFLOGD(BPF_TRUE, "failed to get super block");
        return 0;
    }
    const struct list_head *mountsHead = NULL;
    if (0 != bpf_probe_read_kernel(&mountsHead, sizeof(mountsHead), &superBlock->s_mounts.next)) {
        BPFLOGD(BPF_TRUE, "failed to get mount point");
        return 0;
    }
    struct mount *mnt = NULL;
    mnt = list_entry(mountsHead, struct mount, mnt_instance);
    const u32 MAX_MOUNT_POINT = 5;
    size_t pos = 0;
    for (u32 cnt = MAX_MOUNT_POINT; cnt != 0; --cnt) {
        const struct dentry *mpDentry = NULL;
        if (0 != bpf_probe_read_kernel(&mpDentry, sizeof(mpDentry), &mnt->mnt_mountpoint)) {
            BPFLOGD(BPF_TRUE, "failed to get dentry of mount point");
            break;
        }

        struct qstr d_name = {0};
        if (0 != bpf_probe_read_kernel(&d_name, sizeof(d_name), &mpDentry->d_name)) {
            BPFLOGD(BPF_TRUE, "failed to get dentry name");
            break;
        }
        if (d_name.len < 1) {
            BPFLOGD(BPF_TRUE, "failed to read dentry name from qstr");
            break;
        }
        if (pos + d_name.len + 1 >= len) {
            break;
        }

        int name_len = 0;
        char buffer[MAX_DENTRY_NAME_LEN];
        __builtin_memset(buffer, 0, MAX_DENTRY_NAME_LEN);
        name_len = bpf_probe_read_kernel_str(buffer, MAX_DENTRY_NAME_LEN, d_name.name);
        if (buffer[0] == '/') { // && name_len == 2
            BPFLOGD(BPF_TRUE, "reach root mount point");
            break;
        }
        name_len = bpf_probe_read_kernel_str(filename + pos, MAX_DENTRY_NAME_LEN, buffer);
        if (name_len <= 1) {
            BPFLOGD(BPF_TRUE, "failed to read dentry name from kernel stack buffer");
            break;
        }
        pos += name_len;
        filename[pos - 1] = '/';

        if (0 != bpf_probe_read_kernel(&mnt, sizeof(mnt), &mnt->mnt_parent)) {
            BPFLOGD(BPF_TRUE, "failed to get parent mount point");
            break;
        }
    }

    return pos;
}

static __always_inline
int get_filename_by_inode(char *filename, const size_t len, const struct inode *host)
{
    int err = 0;
    // walk through the dentry-tree from leaf to root
    struct hlist_head hlist_head_node = {};
    __builtin_memset(&hlist_head_node, 0, sizeof(struct hlist_head));
    err = bpf_probe_read_kernel(&hlist_head_node, sizeof(struct hlist_head), &host->i_dentry);
    if (err) {
        BPFLOGD(BPF_TRUE, "failed to get head node of dentry list");
        return -1;
    }
    const struct hlist_node *curr_hlist_node = hlist_head_node.first;
    if (curr_hlist_node == NULL) {
        BPFLOGD(BPF_TRUE, "failed to get alias dentries of the inode");
        return -1;
    }

    const struct dentry *curr_dentry = NULL;
    const u32 MAX_ALIAS_DENTRY = 100;
    for (u32 cnt = MAX_ALIAS_DENTRY; cnt != 0; --cnt) {
        curr_dentry = container_of(curr_hlist_node, struct dentry, d_u);
        struct inode *curr_inode = NULL;
        err = bpf_probe_read_kernel(&curr_inode, sizeof(curr_inode), &curr_dentry->d_inode);
        if (err || curr_inode == NULL) {
            BPFLOGD(BPF_TRUE, "failed to get the current inode");
            return -1;
        }
        if (curr_inode == host) {
            break;
        }
        err = bpf_probe_read_kernel(
            &curr_hlist_node,
            sizeof(curr_hlist_node),
            &curr_hlist_node->next);
        if (err || curr_hlist_node == NULL) {
            BPFLOGD(BPF_TRUE, "failed to get the next hlist_node");
            curr_dentry = NULL;
            break;
        }
    }

    size_t pos = 0;
    const u32 MAX_BACKTRACE_DEPTH = 20;
    for (u32 cnt = MAX_BACKTRACE_DEPTH; cnt != 0; --cnt) {
        if (err || curr_dentry == NULL) {
            break;
        }
        struct qstr d_name = {};
        __builtin_memset(&d_name, 0, sizeof(d_name));
        err = bpf_probe_read_kernel(&d_name, sizeof(d_name), &curr_dentry->d_name);
        if (err) {
            BPFLOGD(BPF_TRUE, "failed to get dentry name");
            break;
        }
        if (pos + d_name.len >= len) {
            break;
        }

        int name_len = 0;
        char buffer[MAX_DENTRY_NAME_LEN];
        __builtin_memset(buffer, 0, MAX_DENTRY_NAME_LEN);
        name_len = bpf_probe_read_kernel_str(buffer, MAX_DENTRY_NAME_LEN, d_name.name);
        if (d_name.len <= 1) {
            BPFLOGD(BPF_TRUE, "failed to read dentry name from qstr");
            break;
        }
        name_len = bpf_probe_read_kernel_str(filename + pos, MAX_DENTRY_NAME_LEN, buffer);
        if (name_len <= 1) {
            BPFLOGD(BPF_TRUE, "failed to read dentry name from kernel stack buffer");
            break;
        }
        pos += name_len;
        filename[pos - 1] = '/';
        err = bpf_probe_read_kernel(&curr_dentry, sizeof(curr_dentry), &curr_dentry->d_parent);
        BPFLOGD(err, "failed to get parent dentry");
    }
    pos += get_mountpoint_by_inode(filename + pos, len - pos, host);
    return pos + 1;
}

static __always_inline
int get_filename_by_bio(char *filename, const size_t len, const struct bio *bio)
{
    if (filename == NULL || len == 0 || bio == NULL) {
        BPFLOGD(BPF_TRUE, "get_filename_by_bio() error: invalid argument");
        return -1;
    }
    int err = 0;
    //find the bio_vec object address
    const struct bio_vec *bi_io_vec = NULL;
    err = bpf_probe_read_kernel(&bi_io_vec, sizeof(bi_io_vec), &bio->bi_io_vec);
    if (err || bi_io_vec == NULL) {
        BPFLOGD(BPF_TRUE, "failed to get the bio associated page address");
        return -1;
    }

    // find the page object address
    const struct page *bv_page = NULL;
    err = bpf_probe_read_kernel(&bv_page, sizeof(bv_page), &bi_io_vec->bv_page);
    if (err || bv_page == NULL) {
        BPFLOGD(BPF_TRUE, "failed to get the bio associated page address");
        return -1;
    }

    // find the address_space address
    const struct address_space *as = NULL;
    err = bpf_probe_read_kernel(&as, sizeof(as), &bv_page->mapping);
    if (err || as == NULL) {
        BPFLOGD(BPF_TRUE, "failed to get the bio associated address_space object");
        return -1;
    }

    // find the inode object address
    const struct inode *host= NULL;
    err = bpf_probe_read_kernel(&host, sizeof(host), &as->host);
    if (err || host == NULL) {
        BPFLOGD(BPF_TRUE, "failed to get the bio associated inode");
        return -1;
    }

    return get_filename_by_inode(filename, len, host);
}

static __always_inline
struct file* get_file_by_fd(const struct files_struct *files, const unsigned int fd)
{
    if (files == NULL) {
        BPFLOGD(BPF_TRUE, "get_file_by_fd() error: invalid argument");
        return NULL;
    }
    int err = 0;
    const struct fdtable *fdt = NULL;
    err = bpf_probe_read_kernel(&fdt, sizeof(fdt), &files->fdt);
    if (err || fdt == NULL) {
        BPFLOGD(BPF_TRUE, "failed to get fdtable");
        return NULL;
    }
    unsigned int max_fds = -1U;
    err = bpf_probe_read_kernel(&max_fds, sizeof(max_fds), &fdt->max_fds);
    if (err) {
        BPFLOGD(BPF_TRUE, "failed to get max number of fds");
        return NULL;
    }
    if (fd >= max_fds) {
        BPFLOGD(BPF_TRUE, "get_file_by_fd() error: invalid argument");
        return NULL;
    }
    const struct file **fd_array = NULL;
    err = bpf_probe_read_kernel(&fd_array, sizeof(fd_array), &fdt->fd);
    if (err || fd_array == NULL) {
        BPFLOGD(BPF_TRUE, "failed to get fd array");
        return NULL;
    }
    struct file *filp = NULL;
    err = bpf_probe_read_kernel(&filp, sizeof(filp), &fd_array[fd]);
    if (err || filp == NULL) {
        BPFLOGD(BPF_TRUE, "failed to get file");
        return NULL;
    }
    return filp;
}

static __always_inline
int get_filename_by_file(char *filename, const size_t len, const struct file *filp)
{
    if (filename == NULL || filp == NULL || len == 0) {
        BPFLOGD(BPF_TRUE, "get_filename_by_file() error: invalid argument");
        return -1;
    }
    int err = 0;
    struct inode *f_inode = NULL;
    err = bpf_probe_read_kernel(&f_inode, sizeof(f_inode), &filp->f_inode);
    if (err || f_inode == NULL) {
        BPFLOGD(BPF_TRUE, "failed to get inode");
        return -1;
    }
    return get_filename_by_inode(filename, len, f_inode);
}

static __always_inline
int emit_strtrace_event(u64 stime, u32 type, const void *addr, u32 stracer)
{
    if (addr == NULL) {
        BPFLOGD(BPF_TRUE, "strtrace event discarded: invalid argument");
        return -1;
    }
    const u64 event_size = sizeof(struct strtrace_cmplt_event_t);
    struct strtrace_cmplt_event_t *cmplt_event = bpf_ringbuf_reserve(&bpf_ringbuf_map, event_size, 0);
    if (cmplt_event == NULL) {
        BPFLOGD(BPF_TRUE, "failed to reserve space for strtrace event from BPF ringbuffer");
        return -1;
    }
    __builtin_memset(cmplt_event, 0, event_size);
    cmplt_event->tracer = STRTRACE;
    cmplt_event->start_event.type = type;
    cmplt_event->start_event.stracer = stracer;
    cmplt_event->start_event.stime = stime;
    cmplt_event->start_event.addr = addr;
    cmplt_event->pid = bpf_get_current_pid_tgid();
    cmplt_event->tgid = (bpf_get_current_pid_tgid() >> 32);
    int err = 0;
    switch (stracer) {
        case BIOTRACE: {
            err = get_filename_by_bio(cmplt_event->filename, MAX_FILENAME_LEN, addr);
            break;
        }
        case FSTRACE: {
            if (type == SYS_OPENAT2) {
                err = bpf_probe_read_user_str(cmplt_event->filename, MAX_FILENAME_LEN, addr);
                break;
            }
            BPFLOGD(BPF_TRUE, "strtrace event discarded: bad source event type = %d of fstrace", type);
            bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
            return -1;
        }
        default: {
            BPFLOGD(BPF_TRUE, "strtrace event discarded: bad strtrace source tracer = %d", stracer);
            bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
            return -1;
        }
    }

    if (err <= 0) {
        BPFLOGD(BPF_TRUE, "strtrace event discarded: failed to read path for tracer = %d", stracer);
        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
        return -1;
    }
    cmplt_event->len = err;
    bpf_ringbuf_submit(cmplt_event, 0);
    return 0;
}

static __always_inline
u32 get_biotrace_event_type_by_flags(unsigned int cmd_flags)
{
    // include/linux/blk_types.h:
    // 	struct bio {
    // 		struct bio		*bi_next;	// request queue link
    // 		struct gendisk		*bi_disk;
    // 		unsigned int		bi_opf;		// bottom bits req flags,
    // 										// top bits REQ_OP. Use
    // 										// accessors.
    // 		...
    // 	}
    //
    // 	#define REQ_OP_BITS	8
    // 	#define REQ_OP_MASK	((1 << REQ_OP_BITS) - 1)
    // 	#define REQ_FLAG_BITS	24
    //
    // 	enum req_opf {
    // 		/* read sectors from the device */
    // 		REQ_OP_READ		= 0,
    // 		/* write sectors to the device */
    // 		REQ_OP_WRITE		= 1,
    // 		/* flush the volatile write cache */
    // 		REQ_OP_FLUSH		= 2,
    // 		/* discard sectors */
    // 		REQ_OP_DISCARD		= 3,
    // 		/* securely erase sectors */
    // 		REQ_OP_SECURE_ERASE	= 5,
    // 		/* write the same sector many times */
    // 		REQ_OP_WRITE_SAME	= 7,
    // 		/* write the zero filled sector many times */
    // 		REQ_OP_WRITE_ZEROES	= 9,
    // 		/* Open a zone */
    // 		REQ_OP_ZONE_OPEN	= 10,
    // 		/* Close a zone */
    // 		REQ_OP_ZONE_CLOSE	= 11,
    // 		/* Transition a zone to full */
    // 		REQ_OP_ZONE_FINISH	= 12,
    // 		/* write data at the current zone write pointer */
    // 		REQ_OP_ZONE_APPEND	= 13,
    // 		/* reset a zone write pointer */
    // 		REQ_OP_ZONE_RESET	= 15,
    // 		/* reset all the zone present on the device */
    // 		REQ_OP_ZONE_RESET_ALL	= 17,
    //
    // 		/* SCSI passthrough using struct scsi_request */
    // 		REQ_OP_SCSI_IN		= 32,
    // 		REQ_OP_SCSI_OUT		= 33,
    // 		/* Driver private requests */
    // 		REQ_OP_DRV_IN		= 34,
    // 		REQ_OP_DRV_OUT		= 35,
    //
    // 		REQ_OP_LAST,
    // 	};
    //
    // 	enum req_flag_bits {
    // 		__REQ_FAILFAST_DEV =	/* no driver retries of device errors */
    // 			REQ_OP_BITS,
    // 		__REQ_FAILFAST_TRANSPORT, /* no driver retries of transport errors */
    // 		__REQ_FAILFAST_DRIVER,	/* no driver retries of driver errors */
    // 		__REQ_SYNC,		/* request is sync (sync write or read) */
    // 		__REQ_META,		/* metadata io request */
    // 		__REQ_PRIO,		/* boost priority in cfq */
    // 		__REQ_NOMERGE,		/* don't touch this for merging */
    // 		__REQ_IDLE,		/* anticipate more IO after this one */
    // 		__REQ_INTEGRITY,	/* I/O includes block integrity payload */
    // 		__REQ_FUA,		/* forced unit access */
    // 		__REQ_PREFLUSH,		/* request for cache flush */
    // 		__REQ_RAHEAD,		/* read ahead, can fail anytime */
    // 		__REQ_BACKGROUND,	/* background IO */
    // 		__REQ_NOWAIT,           /* Don't wait if request will block */
    // 		/*
    // 		 * When a shared kthread needs to issue a bio for a cgroup, doing
    // 		 * so synchronously can lead to priority inversions as the kthread
    // 		 * can be trapped waiting for that cgroup.  CGROUP_PUNT flag makes
    // 		 * submit_bio() punt the actual issuing to a dedicated per-blkcg
    // 		 * work item to avoid such priority inversions.
    // 		 */
    // 		__REQ_CGROUP_PUNT,
    //
    // 		/* command specific flags for REQ_OP_WRITE_ZEROES: */
    // 		__REQ_NOUNMAP,		/* do not free blocks when zeroing */
    //
    // 		__REQ_HIPRI,
    //
    // 		/* for driver use */
    // 		__REQ_DRV,
    // 		__REQ_SWAP,		/* swapping request. */
    // 		__REQ_NR_BITS,		/* stops here */
    // 	};
    //
    // 	#define REQ_FAILFAST_DEV	(1ULL << __REQ_FAILFAST_DEV)
    // 	#define REQ_FAILFAST_TRANSPORT	(1ULL << __REQ_FAILFAST_TRANSPORT)
    // 	#define REQ_FAILFAST_DRIVER	(1ULL << __REQ_FAILFAST_DRIVER)
    // 	#define REQ_SYNC		(1ULL << __REQ_SYNC)
    // 	#define REQ_META		(1ULL << __REQ_META)
    // 	#define REQ_PRIO		(1ULL << __REQ_PRIO)
    // 	#define REQ_NOMERGE		(1ULL << __REQ_NOMERGE)
    // 	#define REQ_IDLE		(1ULL << __REQ_IDLE)
    // 	#define REQ_INTEGRITY		(1ULL << __REQ_INTEGRITY)
    // 	#define REQ_FUA			(1ULL << __REQ_FUA)
    // 	#define REQ_PREFLUSH		(1ULL << __REQ_PREFLUSH)
    // 	#define REQ_RAHEAD		(1ULL << __REQ_RAHEAD)
    // 	#define REQ_BACKGROUND		(1ULL << __REQ_BACKGROUND)
    // 	#define REQ_NOWAIT		(1ULL << __REQ_NOWAIT)
    // 	#define REQ_CGROUP_PUNT		(1ULL << __REQ_CGROUP_PUNT)
    //
    // 	#define REQ_NOUNMAP		(1ULL << __REQ_NOUNMAP)
    // 	#define REQ_HIPRI		(1ULL << __REQ_HIPRI)
    //
    // 	#define REQ_DRV			(1ULL << __REQ_DRV)
    // 	#define REQ_SWAP		(1ULL << __REQ_SWAP)
    //
    // 	#define REQ_FAILFAST_MASK \
    // 		(REQ_FAILFAST_DEV | REQ_FAILFAST_TRANSPORT | REQ_FAILFAST_DRIVER)
    //
    // 	#define REQ_NOMERGE_FLAGS \
    // 		(REQ_NOMERGE | REQ_PREFLUSH | REQ_FUA)
    if (cmd_flags & REQ_META) {
        if ((cmd_flags & REQ_OP_MASK) == REQ_OP_READ) {
            return BIO_METADATA_READ;
        }
        if ((cmd_flags & REQ_OP_MASK) == REQ_OP_WRITE)
        {
            return BIO_METADATA_WRITE;
        }
        return 0;
    }
    if (cmd_flags & REQ_SWAP) {
        if ((cmd_flags & REQ_OP_MASK) == REQ_OP_READ) {
            return BIO_PAGE_IN;
        }
        if ((cmd_flags & REQ_OP_MASK) == REQ_OP_WRITE)
        {
            return BIO_PAGE_OUT;
        }
        return 0;
    }
    if ((cmd_flags & REQ_OP_MASK) == REQ_OP_READ) {
        return BIO_DATA_READ;
    }
    if ((cmd_flags & REQ_OP_MASK) == REQ_OP_WRITE)
    {
        return BIO_DATA_WRITE;
    }
    return 0;
}
/***************************** inline funcs END **********************************/


/***************************** pftrace BPF progs BEGING *****************************/
SEC("kprobe/__do_fault")
int BPF_KPROBE(__do_fault_entry, struct vm_fault *vmf)
{
    if (check_current_pid(-1, -1) != 0) {
        // not any one of target processes, skip it
        return 0;
    }
    return handle_pftrace_start_event(vmf, (u32) PF_FILE_BACKED_IN);
}

SEC("kretprobe/__do_fault")
int BPF_KRETPROBE(__do_fault_exit, int64_t vmf_flags)
{
    if (check_current_pid(-1, -1) != 0) {
        // not any one of target processes, skip it
        return 0;
    }
    return emit_event(ctx, vmf_flags, PFTRACE);
}

// SEC("kprobe/do_async_mmap_readahead")
// int BPF_KPROBE(do_async_mmap_readahead_entry, struct vm_fault *vmf)
// {
//     if (check_current_pid(-1, -1) != 0) {
//         // not any one of target processes, skip it
//         return 0;
//     }
//     return handle_pftrace_start_event(vmf, (u32) PF_PAGE_CACHE_HIT);
// }

// SEC("kretprobe/do_async_mmap_readahead")
// int BPF_KRETPROBE(do_async_mmap_readahead_exit, int64_t fpin)
// {
//     if (check_current_pid(-1, -1) != 0) {
//         // not any one of target processes, skip it
//         return 0;
//     }
//     return emit_event(ctx, fpin, PFTRACE);
// }

// SEC("kprobe/do_anonymous_page")
// int BPF_KPROBE(do_anonymous_page_entry, struct vm_fault *vmf)
// {
//     if (check_current_pid(-1, -1) != 0) {
//         // not any one of target processes, skip it
//         return 0;
//     }
//     u64 flags = 0;
//     int err = bpf_probe_read_kernel(&flags, sizeof(vmf->flags), &vmf->flags);
//     if (err) {
//         BPFLOGW(BPF_TRUE, "failed to read vm_fault flags");
//         return -1;
//     }
//     if (!(flags & FAULT_FLAG_WRITE)) {
//         return handle_pftrace_start_event(vmf, (u32) PF_FAKE_ZERO_PAGE);
//     }
//     return handle_pftrace_start_event(vmf, (u32) PF_ZERO_FILL_PAGE);
// }

// SEC("kretprobe/do_anonymous_page")
// int BPF_KRETPROBE(do_anonymous_page_exit, int64_t vmf_flags)
// {
//     if (check_current_pid(-1, -1) != 0) {
//         // not any one of target processes, skip it
//         return 0;
//     }
//     return emit_event(ctx, vmf_flags, PFTRACE);
// }

SEC("kprobe/do_swap_page")
int BPF_KPROBE(do_swap_page_entry, struct vm_fault *vmf)
{
    if (check_current_pid(-1, -1) != 0) {
        // not any one of target processes, skip it
        return 0;
    }
    return handle_pftrace_start_event(vmf, (u32) PF_SWAP_FROM_DISK);
}

SEC("kretprobe/do_swap_page")
int BPF_KRETPROBE(do_swap_page_exit, int64_t vmf_flags)
{
    if (check_current_pid(-1, -1) != 0) {
        // not any one of target processes, skip it
        return 0;
    }
    return emit_event(ctx, vmf_flags, PFTRACE);
}

// SEC("kprobe/zram_rw_page")
// int BPF_KPROBE(zram_rw_page_entry)
// {
//     if (check_current_pid(-1, -1) != 0) {
//         // not any one of target processes, skip it
//         return 0;
//     }
//     return read_modify_update_current_type(PF_SWAP_FROM_ZRAM);
// }

SEC("kprobe/do_wp_page")
int BPF_KPROBE(do_wp_page_entry, struct vm_fault *vmf)
{
    if (check_current_pid(-1, -1) != 0) {
        // not any one of target processes, skip it
        return 0;
    }
    return handle_pftrace_start_event(vmf, (u32) PF_COPY_ON_WRITE);
}

SEC("kretprobe/do_wp_page")
int BPF_KRETPROBE(do_wp_page_exit, int64_t vmf_flags)
{
    if (check_current_pid(-1, -1) != 0) {
        // not any one of target processes, skip it
        return 0;
    }
    return emit_event(ctx, vmf_flags, PFTRACE);
}
/*************************** pftrace BPF progs END *******************************/


/***************************** bio BPF progs BEGING *****************************/
// SEC("kprobe/blk_account_io_start")
// int BPF_KPROBE(blk_account_io_start_entry, struct request *rq)
// {
//     if (check_current_pid(-1, -1) != 0) {
//         // not any one of target processes, skip it
//         return 0;
//     }
//     struct start_event_t start_event = {};
//     __builtin_memset(&start_event, 0, sizeof(start_event));
//     struct biotrace_start_event_t *bio_se = &start_event.bio_se;

//     // get start time as soon as possible
//     bio_se->stime = bpf_ktime_get_ns();

//     // get event type by request command flags
//     unsigned int cmd_flags = 0;
//     int err = bpf_probe_read_kernel(&cmd_flags, sizeof(cmd_flags), &rq->cmd_flags);
//     const u64 rq_addr = (const u64) rq;
//     struct start_event_t *se_ptr = bpf_map_lookup_elem(&start_event_map, &rq_addr);
//     if (err) {
//         BPFLOGD(BPF_TRUE, "failed to get bio request command flags");
//         if (se_ptr) {
//             // clear the start event to indicate error
//             __builtin_memset(&start_event, 0, sizeof(start_event));
//             err = (int) bpf_map_update_elem(&start_event_map, &rq_addr, &start_event, BPF_ANY);
//             BPFLOGE((err != 0), "failed to store biotrace start event");
//         }
//         return -1;
//     }
//     bio_se->type = get_biotrace_event_type_by_flags(cmd_flags);
//     if (bio_se->type == 0) {
//         BPFLOGD(BPF_TRUE, "failed to get biotrace event type");
//         if (se_ptr) {
//             // clear the start event to indicate error
//             __builtin_memset(&start_event, 0, sizeof(start_event));
//             err = (int) bpf_map_update_elem(&start_event_map, &rq_addr, &start_event, BPF_ANY);
//             BPFLOGE((err != 0), "failed to store biotrace start event");
//         }
//         return -1;
//     }

//     // get file path
//     err = emit_strtrace_event(bio_se->stime, bio_se->type, rq, BIOTRACE);
//     BPFLOGI(err, "failed to emit path for bio with address = %p", rq);

//     // get tag info
//     u64 tgid_pid = bpf_get_current_pid_tgid();
//     bio_se->pid = (u32) tgid_pid;
//     bio_se->tgid = (u32) (tgid_pid >> 32);
//     if (bpf_get_current_comm(bio_se->comm, MAX_COMM_LEN) != 0) {
//         BPFLOGD(BPF_TRUE, "failed to get process command");
//         if  (se_ptr) {
//             // clear the start event to indicate error
//             __builtin_memset(&start_event, 0, sizeof(start_event));
//             err = (int) bpf_map_update_elem(&start_event_map, &rq_addr, &start_event, BPF_ANY);
//             BPFLOGE((err != 0), "failed to store biotrace start event");
//         }
//         return -1;
//     }

//     // get counter
//     u32 blk_size = 10;
//     err = bpf_probe_read_kernel(&blk_size, sizeof(unsigned int), &rq->__data_len);
//     BPFLOGI(BPF_TRUE, "blk_size = %u", blk_size);

//     // get user callchain
//     if (unwind_stack()) {
//         const u32 ustack_map_key = BIOTRACE_STACK_TRACE_INDEX;
//         void *ustack_map_ptr = bpf_map_lookup_elem(&ustack_maps_array, &ustack_map_key);
//         BPFLOGI(BPF_TRUE, "biotrace context pointer = %p", ctx);
//         BPFLOGI(BPF_TRUE, "biotrace ustack map pointer = %p", ustack_map_ptr);
//         if (ustack_map_ptr == NULL) {
//             BPFLOGD(BPF_TRUE, "biotrace event discarded: failed to lookup ustack map");
//             if (se_ptr) {
//                 // clear the start event to indicate error
//                 __builtin_memset(&start_event, 0, sizeof(start_event));
//                 err = (int) bpf_map_update_elem(&start_event_map, &rq_addr, &start_event, BPF_ANY);
//                 BPFLOGE((err != 0), "failed to store biotrace start event");
//             }
//             return -1;
//         }
//         // bio_se->ustack_id = (int64_t) bpf_get_stackid(ctx, ustack_map_ptr, USER_STACKID_FLAGS);
//         bio_se->ustack_id = (int64_t) bpf_get_stackid(ctx, ustack_map_ptr, 0);
//         BPFLOGI(BPF_TRUE, "bio ustack_id = %lld", bio_se->ustack_id);
//         if (bio_se->ustack_id < 0) {
//             BPFLOGD(BPF_TRUE, "biotrace event discarded: failed to unwind user callchain");
//             if (se_ptr) {
//                 // clear the start event to indicate error
//                 __builtin_memset(&start_event, 0, sizeof(start_event));
//                 err = (int) bpf_map_update_elem(&start_event_map, &rq_addr, &start_event, BPF_ANY);
//                 BPFLOGE((err != 0), "failed to store biotrace start event");
//             }
//             return -1;
//         }
//     }

//     err = (int) bpf_map_update_elem(&start_event_map, &rq_addr, &start_event, BPF_ANY);
//     BPFLOGE((err != 0), "failed to store biotrace start event");
//     return 0;
// }

// SEC("kprobe/blk_account_io_merge_bio")
// int BPF_KPROBE(blk_account_io_merge_bio_entry, struct request *rq)
// {
//     char mesg[] = "blk_account_io_merge_bio_entry called";
//     bpf_trace_printk(mesg, sizeof(mesg));
//     return 0;
// }

// SEC("kprobe/blk_account_io_done")
// int BPF_KPROBE(blk_account_io_done_entry, struct request *rq)
// {
//    u64 ctime = bpf_ktime_get_ns();
//    const u64 rq_addr = (u64) rq;
//    const u64 event_size = sizeof(struct biotrace_cmplt_event_t);
//    struct biotrace_cmplt_event_t *cmplt_event = bpf_ringbuf_reserve(&bpf_ringbuf_map, event_size, 0);
//    if (cmplt_event == NULL) {
//        BPFLOGD(BPF_TRUE, "failed to reserve memory for biotrace event");
//        return -1;
//    }
//    const struct biotrace_start_event_t *start_event = bpf_map_lookup_elem(&start_event_map, &rq_addr);
//    int err = bpf_probe_read_kernel(&cmplt_event->start_event, sizeof(struct biotrace_start_event_t), start_event);
//    if (err) {
//        BPFLOGD(BPF_TRUE, "biotrace event discarded: failed to read biotrace_start_event");
//        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
//        return -1;
//    }
//    if (cmplt_event->start_event.type == 0) {
//        BPFLOGI(BPF_TRUE, "biotrace event discarded: invalide biotrace start event");
//        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
//        return -1;
//    }
//    if (check_current_pid(cmplt_event->start_event.pid, cmplt_event->start_event.tgid) != 0) {
//        BPFLOGI(BPF_TRUE,
//            "current pid = %d, tgid = %d is not any one of target processes",
//            cmplt_event->start_event.pid, cmplt_event->start_event.tgid);
//        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
//        return -1;
//    }
//    cmplt_event->tracer = BIOTRACE;
//    cmplt_event->ctime = ctime;
//    err = bpf_probe_read_kernel(&cmplt_event->prio, sizeof(unsigned short), &rq->ioprio);
//    if (err) {
//        BPFLOGD(BPF_TRUE, "biotrace event discarded: failed to get bio priority");
//        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
//        return -1;
//    }
//    err = bpf_probe_read_kernel(&cmplt_event->blkcnt, sizeof(sector_t), &rq->__sector);
//    if (err) {
//        BPFLOGD(BPF_TRUE, "biotrace event discarded: failed to get bio block number");
//        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
//        return -1;
//    }
//    cmplt_event->size = 10;
//    err = bpf_probe_read_kernel(&cmplt_event->size, sizeof(unsigned int), &rq->__data_len);
//    BPFLOGI(BPF_TRUE, "biotrace io size = %u", cmplt_event->size);
//    if (err) {
//        BPFLOGD(BPF_TRUE, "biotrace event discarded: failed to get bio size");
//        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
//        return -1;
//    }

//    cmplt_event->nips = 0;
//    if (unwind_stack()) {
//        cmplt_event->nips = get_max_stack_depth();
//    }
//    bpf_ringbuf_submit(cmplt_event, 0);
//    return 0;
//     return 0;
// }

SEC("kprobe/submit_bio")
int BPF_KPROBE(submit_bio_entry, struct bio *bio)
{
    if (check_current_pid(-1, -1) != 0) {
        // not any one of target processes, skip it
        return 0;
    }
    struct start_event_t start_event = {};
    __builtin_memset(&start_event, 0, sizeof(start_event));
    struct biotrace_start_event_t *bio_se = &start_event.bio_se;
    bio_se->stime = bpf_ktime_get_ns();
    unsigned short bi_opf = 0;
    int err = bpf_probe_read_kernel(&bi_opf, sizeof(bi_opf), &bio->bi_opf);
    const u64 bio_addr = (const u64) bio;
    struct start_event_t *se_ptr = bpf_map_lookup_elem(&start_event_map, &bio_addr);
    if (err) {
        BPFLOGD(BPF_TRUE, "biotrace event discarded: failed to get bio operation flags");
        if (se_ptr) {
            // clear the start event to indicate error
            __builtin_memset(&start_event, 0, sizeof(start_event));
            err = (int) bpf_map_update_elem(&start_event_map, &bio_addr, &start_event, BPF_ANY);
            BPFLOGE((err != 0), "failed to store biotrace start event");
        }
        return -1;
    }
    bio_se->type = get_biotrace_event_type_by_flags(bi_opf);
    if (bio_se->type == 0) {
        BPFLOGD(BPF_TRUE, "failed to get biotrace event type");
        if (se_ptr) {
            // clear the start event to indicate error
            __builtin_memset(&start_event, 0, sizeof(start_event));
            err = (int) bpf_map_update_elem(&start_event_map, &bio_addr, &start_event, BPF_ANY);
            BPFLOGE((err != 0), "failed to store biotrace start event");
        }
        return -1;
    }
    err = emit_strtrace_event(bio_se->stime, bio_se->type, bio, BIOTRACE);
    BPFLOGI(err, "failed to emit path for bio with address = %p", bio);
    u64 tgid_pid = bpf_get_current_pid_tgid();
    bio_se->pid = (u32) tgid_pid;
    bio_se->tgid = (u32) (tgid_pid >> 32);
    if (bpf_get_current_comm(bio_se->comm, MAX_COMM_LEN) != 0) {
        BPFLOGD(BPF_TRUE, "failed to get process command");
        if (se_ptr) {
            // clear the start event to indicate error
            __builtin_memset(&start_event, 0, sizeof(start_event));
            err = (int) bpf_map_update_elem(&start_event_map, &bio_addr, &start_event, BPF_ANY);
            BPFLOGE((err != 0), "failed to store biotrace start event");
        }
        return -1;
    }

    err = bpf_probe_read_kernel(&bio_se->size, sizeof(unsigned int), &bio->bi_iter.bi_size);
    BPFLOGI(BPF_TRUE, "bio size = %u", bio_se->size);
    if (err) {
        BPFLOGD(BPF_TRUE, "biotrace event discarded: failed to get bio size");
        if (se_ptr) {
            // clear the start event to indicate error
            __builtin_memset(&start_event, 0, sizeof(start_event));
            err = (int) bpf_map_update_elem(&start_event_map, &bio_addr, &start_event, BPF_ANY);
            BPFLOGE((err != 0), "failed to store biotrace start event");
        }
        return -1;
    }

    // get callchain
    if (unwind_stack()) {
        const u32 ustack_map_key = BIOTRACE_STACK_TRACE_INDEX;
        void *ustack_map_ptr = bpf_map_lookup_elem(&ustack_maps_array, &ustack_map_key);
        if (ustack_map_ptr == NULL) {
            BPFLOGD(BPF_TRUE, "biotrace event discarded: failed to lookup ustack map");
            if (se_ptr) {
                // clear the start event to indicate error
                __builtin_memset(&start_event, 0, sizeof(start_event));
                err = (int) bpf_map_update_elem(&start_event_map, &bio_addr, &start_event, BPF_ANY);
                BPFLOGE((err != 0), "failed to store biotrace start event");
            }
            return -1;
        }
        bio_se->ustack_id = (int64_t) bpf_get_stackid(ctx, ustack_map_ptr, 0);
        BPFLOGI(BPF_TRUE, "bio kern stack_id = %lld", bio_se->ustack_id);
        if (bio_se->ustack_id < 0) {
            BPFLOGD(BPF_TRUE, "biotrace event discarded: failed to unwind user callchain");
            if (se_ptr) {
                // clear the start event to indicate error
                __builtin_memset(&start_event, 0, sizeof(start_event));
                err = (int) bpf_map_update_elem(&start_event_map, &bio_addr, &start_event, BPF_ANY);
                BPFLOGE((err != 0), "failed to store biotrace start event");
            }
            return -1;
        }
    }

    err = (int) bpf_map_update_elem(&start_event_map, &bio_addr, &start_event, BPF_ANY);
    BPFLOGE((err != 0), "failed to store biotrace start event");
    return 0;
}

SEC("kprobe/bio_endio")
int BPF_KPROBE(bio_endio_entry, struct bio *bio)
{
    u64 ctime = bpf_ktime_get_ns();
    const u64 bio_addr = (u64) bio;
    const u64 event_size = sizeof(struct biotrace_cmplt_event_t);
    struct biotrace_cmplt_event_t *cmplt_event = bpf_ringbuf_reserve(&bpf_ringbuf_map, event_size, 0);
    if (cmplt_event == NULL) {
        BPFLOGD(BPF_TRUE, "failed to reserve space for biotrace event from BPF ringbuffer");
        return -1;
    }
    const struct biotrace_start_event_t *start_event = bpf_map_lookup_elem(&start_event_map, &bio_addr);
    int err = bpf_probe_read_kernel(&cmplt_event->start_event, sizeof(struct biotrace_start_event_t), start_event);
    if (err) {
        BPFLOGD(BPF_TRUE, "biotrace event discarded: failed to read biotrace_start_event");
        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
        return -1;
    }
    if (cmplt_event->start_event.type == 0) {
        BPFLOGI(BPF_TRUE, "biotrace event discarded: invalide biotrace start event");
        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
        return -1;
    }
    if (check_current_pid(cmplt_event->start_event.pid, cmplt_event->start_event.tgid) != 0) {
        BPFLOGI(BPF_TRUE,
            "current pid = %d, tgid = %d is not any one of target processes",
            cmplt_event->start_event.pid, cmplt_event->start_event.tgid);
        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
        return -1;
    }
    cmplt_event->tracer = BIOTRACE;
    cmplt_event->ctime = ctime;
    err = bpf_probe_read_kernel(&cmplt_event->prio, sizeof(short unsigned int), &bio->bi_ioprio);
    if (err) {
        BPFLOGD(BPF_TRUE, "biotrace event discarded: failed to get bio priority");
        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
        return -1;
    }
    err = bpf_probe_read_kernel(&cmplt_event->blkcnt, sizeof(sector_t), &bio->bi_iter.bi_sector);
    if (err) {
        BPFLOGD(BPF_TRUE, "biotrace event discarded: failed to get bio block number");
        bpf_ringbuf_discard(cmplt_event, BPF_RB_NO_WAKEUP);
        return -1;
    }

    cmplt_event->nips = 0;
    if (unwind_stack()) {
        cmplt_event->nips = get_max_stack_depth();
    }
    bpf_ringbuf_submit(cmplt_event, 2);
    return 0;
}
/*************************** bio BPF progs END *******************************/

/*************************** user BPF progs START ****************************/
SEC("uretprobe//system/lib/ld-musl-aarch64.so.1:dlopen")
int BPF_KRETPROBE(uretprobe_dlopen, void *ret)
{
    if (check_current_pid(-1, -1) != 0) {
        return 0;
    }
    if (ret == NULL) {
        return 0;
    }

    const u64 event_size = sizeof(struct dlopen_trace_start_event_t);
    struct dlopen_trace_start_event_t *start_event = bpf_ringbuf_reserve(&bpf_ringbuf_map, event_size, 0);
    if (start_event == NULL) {
        BPFLOGD(BPF_TRUE, "failed to reserve space for biotrace event from BPF ringbuffer");
        return -1;
    }
    start_event->type = DLOPEN_TRACE;
    u64 tgid_pid = bpf_get_current_pid_tgid();
    start_event->tgid = (u32)(tgid_pid >> 32);
    bpf_ringbuf_submit(start_event, 2);
    return 0;
}
/*************************** user BPF progs END ****************************/
#include "fstrace_progs.h"

char _license[] SEC("license") = "GPL";
u32 _version SEC("version") = LINUX_VERSION_CODE;
