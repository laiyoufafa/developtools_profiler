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

#ifndef HIEBPF_EVENT_STRUCTS_H
#define HIEBPF_EVENT_STRUCTS_H


/******************************* fstrace types BEGIN *******************************/
#include "fstrace_types.h"
struct fstrace_cmplt_event_t {
    __u32 tracer;
    struct fstrace_start_event_t start_event;
    __u64 ctime;
    __u32 pid;
    __u32 tgid;
    __u32 uid;
    __u32 gid;
    char comm[MAX_COMM_LEN];
    int32_t retval;
    __u32 nips;
    int64_t ustack_id;
};
/******************************* fstrace types END *******************************/


/******************************* pftrace types BEGIN *******************************/
enum PFTraceEventType:int {
    PF_FILE_BACKED_IN = 1,
    PF_PAGE_CACHE_HIT,
    PF_SWAP_FROM_ZRAM,
    PF_SWAP_FROM_DISK,
    PF_ZERO_FILL_PAGE,
    PF_FAKE_ZERO_PAGE,
    PF_COPY_ON_WRITE,
    PF_MAX_EVENT_TYPE,
};

struct pftrace_start_event_t {
    __u32 type;
    __u64 stime;
    __u64 addr;
};

struct pftrace_cmplt_event_t {
    __u32 tracer;
    struct pftrace_start_event_t start_event;
    __u64 ctime;
    __u32 pid;
    __u32 tgid;
    __u32 uid;
    __u32 gid;
    char comm[MAX_COMM_LEN];
    __u32 size;     // number pf pages operated, generally either is 1 or 0
    __u32 nips;
    int64_t ustack_id;
};

struct pf_stat_t {
    __u64 count;          // total number of the event
    __u64 tot_duration;   // total duration
    __u32 min_duration;   // minimum duration
    __u32 avg_duration;   // average duration
    __u32 dev_duration;   // square of standard deviation duration
    __u32 max_duration;   // maximum duration
};
/******************************* pstrace types END *******************************/


/******************************* biotrace types END *******************************/
enum BIOTraceEventType:__u32 {
    BIO_DATA_READ = 1,
    BIO_DATA_WRITE,
    BIO_METADATA_READ,
    BIO_METADATA_WRITE,
    BIO_PAGE_IN,
    BIO_PAGE_OUT,
};

struct biotrace_start_event_t {
    __u32 type;
    __u32 pid;
    __u64 stime;
    int64_t ustack_id;
    __u32 tgid;
    __u32 size;
    char comm[MAX_COMM_LEN];
};

struct biotrace_cmplt_event_t {
    __u32 tracer;
    struct biotrace_start_event_t start_event;
    __u64 ctime;
    __u64 blkcnt;
    __u32 prio;
    __u32 nips;
};
/******************************* biotrace types END *******************************/


/******************************* strtrace types BEGIN *******************************/
/* strtrace is artificial tracer used to collect user space strings */
struct strtrace_start_event_t {
    __u32 type;
    __u32 stracer;  // the source tracer which generates the strtrace event
    __u64 stime;
    const void *addr;
};

struct strtrace_cmplt_event_t {
    __u32 tracer;
    struct strtrace_start_event_t start_event;
    __u32 pid;
    __u32 tgid;
    __u32 len;
    char filename[MAX_FILENAME_LEN];
};
/******************************* strtrace types END *******************************/

/******************************* uprobe types BEGIN *******************************/
struct dlopen_trace_start_event_t {
    __u32 type;
    __u32 tgid;
};
/******************************* uprobe types END *********************************/

/******************************* hiebpf types BEGIN *******************************/
struct start_event_t {
    /* useless definition */
    union {
        struct fstrace_start_event_t fs_se;
        struct pftrace_start_event_t pf_se;
        struct biotrace_start_event_t bio_se;
    };
};

enum TracerType:__u32 {
    MAPSTRACE = 0,
    SYMBOLTRACE,
    FSTRACE,
    PFTRACE,
    BIOTRACE,
    STRTRACE,
    DLOPEN_TRACE,
    KERNEL_SYM = 0x10001,
    BADTRACE,
};

enum LIBBPFLogLevel:int {
    LIBBPF_NONE = -10,
    LIBBPF_FATAL,
    LIBBPF_ERROR,
};

enum FSTraceEventGroup:int {
    SYS_GROUP_OPEN = 1,
    SYS_GROUP_READ,
    SYS_GROUP_WRITE,
    SYS_GROUP_CLOSE,
};

/******************************* hiebpf types END *******************************/
#endif