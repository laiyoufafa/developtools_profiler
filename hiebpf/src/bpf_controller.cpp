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

#include <iostream>
#include <memory>
#include <chrono>
#include <ctime>
#include <mutex>
#include <iomanip>

#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <signal.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>
#include <linux/perf_event.h>
#include <linux/bpf.h>
#include <signal.h>
#include <errno.h>
#include <sys/resource.h>

#include "bpf.h"
#include "libbpf_logger.h"
#include "bpf_controller.h"
#include "elf_file.h"

namespace {
std::unique_ptr<LIBBPFLogger> libbpfLogger {nullptr};
const std::string THIRD_PARTY_MUSL_ADDR = "/system/lib/ld-musl-aarch64.so.1";
constexpr int32_t SYM_32_VALUE_OFFSET = 4;
constexpr int32_t SYM_64_VALUE_OFFSET = 8;
} // namespace

int BPFController::LIBBPFPrintFunc(enum libbpf_print_level level, const char *format, va_list args)
{
    if (libbpfLogger) {
        return libbpfLogger->Printf(level, format, args);
    }
    return 0;
}

BPFController::~BPFController()
{
    Stop();
    if (rb_) {
        // release bpf ringbuffer
        ring_buffer__free(rb_);
        rb_ = nullptr;
    }
    if (ips_) {
        delete[] ips_;
        ips_ = nullptr;
    }
    if (skel_) {
        hiebpf_bpf__destroy(skel_);
        skel_ = nullptr;
    }

    for (size_t k = 0; k < receivers_.size(); ++k) {
        receivers_[k]->Stop();
    }
    if (bpfLogReader_) {
        bpfLogReader_->Stop();
    }
}

std::unique_ptr<BPFController> BPFController::MakeUnique(const BPFConfig& config)
{
    std::unique_ptr<BPFController> bpfctlr {new(std::nothrow) BPFController {config}};
    if (bpfctlr == nullptr) {
        HHLOGD(true, "failed to instantiate BPFController");
        return nullptr;
    }
    HHLOGI(true, "BPFController instantiated");

    if (bpfctlr->VerifyConfigurations() != 0) {
        HHLOGD(true, "failed to verify config");
        return nullptr;
    }
    HHLOGI(true, "BPFConfig verified");

    if (bpfctlr->SetUpBPF() != 0) {
        HHLOGD(true, "failed to set up BPF");
        return nullptr;
    }
    HHLOGI(true, "BPF setup done");

    return bpfctlr;
}

static inline int VerifyDumpEvents(const __u32 nr)
{
    if (nr > BPFController::DUMP_EVENTS_LIMIT) {
        HHLOGD(true, "dump events exceeds limit");
        return -1;
    }
    return 0;
}

static inline int VerifyTraceDuration(const __u32 duration)
{
    if (duration > BPFController::TRACE_DURATION_LIMIT) {
        HHLOGD(true, "trace duration exceeds limit");
        return -1;
    }
    return 0;
}

static inline int VerifyMaxStackDepth(const __u32 depth)
{
    if (depth > MAX_STACK_LIMIT) {
        HHLOGD(true, "max stack depth exceeds limit");
        return -1;
    }
    return 0;
}

int BPFController::VerifySelectEventGroups(const std::set<HiebpfEventGroup> &selectEventGroups)
{
    if (selectEventGroups.empty()) {
        HHLOGE(true, "VerifySelectEventGroups() failed: event group list is empty");
        return -1;
    }
    selectEventGroups_ = selectEventGroups;
    return 0;
}

int BPFController::VerifyConfigurations()
{
    if (VerifySelectEventGroups(config_.selectEventGroups_) != 0) {
        return -1;
    }
    HHLOGI(true, "VerifySelectEventGroups() done");
    if (VerifyDumpEvents(config_.dumpEvents_) != 0) {
        HHLOGD(true, "VerifyDumpEvents() failed: dump events = %u", config_.dumpEvents_);
        return -1;
    }
    HHLOGI(true, "VerifyDumpEents() done");
    if (VerifyTraceDuration(config_.traceDuration_) != 0) {
        HHLOGD(true, "VerifyTraceDuration() failed: duration = %u", config_.traceDuration_);
        return -1;
    }
    HHLOGI(true, "VerifyTraceDuration() done");
    if (VerifyMaxStackDepth(config_.maxStackDepth_) != 0) {
        HHLOGD(true, "VerifyMaxStackDepth() failed: max stack depth = %u", config_.maxStackDepth_);
        return -1;
    }
    HHLOGI(true, "VerifyMaxStackDepth() done");
    return 0;
}

int BPFController::SetUpBPF()
{
    if (ConfigLIBBPFLogger() != 0) {
        HHLOGD(true, "failed to configure LIBBPF logger");
        return -1;
    }
    HHLOGI(true, "ConfigLIBBPFLogger() done");

    // set up libbpf deubug level
    libbpf_set_strict_mode(LIBBPF_STRICT_ALL);
    // set RLIMIT_MEMLOCK
    struct rlimit r = {RLIM_INFINITY, RLIM_INFINITY};
    setrlimit(RLIMIT_MEMLOCK, &r);

    skel_ = hiebpf_bpf__open();
    int err = libbpf_get_error(skel_);
    if (err) {
        HHLOGD(true, "failed to open BPF skeleton: %s", strerror(-err));
        return err;
    }
    HHLOGI(true, "BPF skeleton opened");
    if (config_.unwindStack_) {
        ips_ = new(std::nothrow) __u64[config_.maxStackDepth_];
        if (ips_ == nullptr) {
            HHLOGD(true, "failed to allocate memory for ips");
            return -1;
        }
    }
    HHLOGI(true, "allocate ips buffer done");
    dataFile_ = HiebpfDataFile::MakeShared(config_.cmd_, config_.outputFile_);
    if (dataFile_ == nullptr) {
        HHLOGD(true, "failed to make hiebpf data file");
        return -1;
    }
    if (FilterProgByEvents() != 0) {
        HHLOGD(true, "failed to load BPF objects");
        return -1;
    }
    HHLOGI(true, "make HiebpfDataFile done");
    skel_->rodata->g_stack_limit = config_.maxStackDepth_;
    err = hiebpf_bpf__load(skel_);
    if (err) {
        HHLOGD(true, "failed to load BPF skeleton: %s", strerror(-err));
        return err;
    }
    if (ConfigureBPF() != 0) {
        HHLOGD(true, "failed to configure BPF");
        return -1;
    }
    HHLOGI(true, "BPF configuration done");

    return 0;
}

int BPFController::FilterProgByEvents()
{
    // check each one hiebpf_bpf.progs in hiebpf.skel.h
    // hiebpf_bpf.progs is autoload by default
    if (selectEventGroups_.find(FS_GROUP_ALL) == selectEventGroups_.end()) {
        if (selectEventGroups_.find(FS_GROUP_OPEN) == selectEventGroups_.end()) {
            bpf_program__set_autoload(skel_->progs.do_sys_openat2_entry, false);
            bpf_program__set_autoload(skel_->progs.do_sys_openat2_exit, false);
        }
        if (selectEventGroups_.find(FS_GROUP_READ) == selectEventGroups_.end()) {
            bpf_program__set_autoload(skel_->progs.do_readv_entry, false);
            bpf_program__set_autoload(skel_->progs.do_readv_exit, false);

            bpf_program__set_autoload(skel_->progs.do_preadv_entry, false);
            bpf_program__set_autoload(skel_->progs.do_preadv_exit, false);

            bpf_program__set_autoload(skel_->progs.ksys_read_entry, false);
            bpf_program__set_autoload(skel_->progs.ksys_read_exit, false);

            bpf_program__set_autoload(skel_->progs.ksys_pread64_entry, false);
            bpf_program__set_autoload(skel_->progs.ksys_pread64_exit, false);
        }
        if (selectEventGroups_.find(FS_GROUP_WRITE) == selectEventGroups_.end()) {
            bpf_program__set_autoload(skel_->progs.do_writev_entry, false);
            bpf_program__set_autoload(skel_->progs.do_writev_exit, false);

            bpf_program__set_autoload(skel_->progs.do_pwritev_entry, false);
            bpf_program__set_autoload(skel_->progs.do_pwritev_exit, false);

            bpf_program__set_autoload(skel_->progs.ksys_write_entry, false);
            bpf_program__set_autoload(skel_->progs.ksys_write_exit, false);

            bpf_program__set_autoload(skel_->progs.ksys_pwrite64_entry, false);
            bpf_program__set_autoload(skel_->progs.ksys_pwrite64_exit, false);
        }
        if (selectEventGroups_.find(FS_GROUP_CLOSE) == selectEventGroups_.end()) {
            bpf_program__set_autoload(skel_->progs.__close_fd_entry, false);
            bpf_program__set_autoload(skel_->progs.__close_fd_exit, false);
        }
    }

    if (selectEventGroups_.find(MEM_GROUP_ALL) == selectEventGroups_.end()) {
        bpf_program__set_autoload(skel_->progs.__do_fault_entry, false);
        bpf_program__set_autoload(skel_->progs.__do_fault_exit, false);

        bpf_program__set_autoload(skel_->progs.do_swap_page_entry, false);
        bpf_program__set_autoload(skel_->progs.do_swap_page_exit, false);

        bpf_program__set_autoload(skel_->progs.do_wp_page_entry, false);
        bpf_program__set_autoload(skel_->progs.do_wp_page_exit, false);
    }
    if (selectEventGroups_.find(BIO_GROUP_ALL) == selectEventGroups_.end()) {
        bpf_program__set_autoload(skel_->progs.block_issue, false);
        bpf_program__set_autoload(skel_->progs.blk_update_request, false);
    } else {
        dataFile_->WriteKernelSymbol();
    }
    return 0;
}

static int InitTracerPid(const int fd, bool excludeTracer)
{
    int32_t pid = -1;
    if (excludeTracer) {
        /* we write the tracer pid into BPF map to notify BPF progs
         * to exclude the tracer itself
        */
        pid = static_cast<int32_t>(getpid());
        if (pid < 0) {
            HHLOGD(true, "failed to get current pid");
            return -1;
        }
    }
    constexpr __u32 pididx {TRACER_PID_INDEX};
    int err = bpf_map_update_elem(fd, &pididx, &pid, BPF_ANY);
    if (err) {
        HHLOGD(true, "failed to update tracer pid %d in config_var_map", pid);
        return -1;
    }
    return 0;
}

static inline int InitBPFLogLevel(const int fd, const __u32 level)
{
    if (level == BPF_LOG_NONE) {
        HHLOGD(true, "bpf log level is NONE!");
        return 0;
    }
    constexpr __u32 levelidx {BPF_LOG_LEVEL_INDEX};
    int err = bpf_map_update_elem(fd, &levelidx, &level, BPF_ANY);
    if (err) {
        HHLOGD(true, "failed to set bpf log level in config_var_map");
        return -1;
    }
    return 0;
}

static inline int InitUnwindFlag(const int fd, bool unwind)
{
    constexpr __u32 uflagidx {UNWIND_FLAG_INDEX};
    __u32 uflag {0};
    if (unwind) {
        uflag = 1;
    }
    int err = bpf_map_update_elem(fd, &uflagidx, &uflag, BPF_ANY);
    if (err) {
        HHLOGD(true, "failed to set unwind stack flag in config_var_map");
        return -1;
    }
    return 0;
}

int BPFController::InitBPFVariables() const
{
    int fd = bpf_map__fd(skel_->maps.config_var_map);
    if (fd < 0) {
        HHLOGD(true, "failed to get fd of config_var_map");
        return -1;
    }
    HHLOGI(true, "InitBPFVariables() done");
    if (InitTracerPid(fd, config_.excludeTracer_) != 0) {
        HHLOGD(true, "failed to init tracer pid in config_var_map");
        return -1;
    }
    HHLOGI(true, "InitTracerPid() done");
    if (InitBPFLogLevel(fd, config_.BPFLogLevel_) != 0) {
        HHLOGD(true, "failed to init BPF log level in config_var_map");
        return -1;
    }
    HHLOGI(true, "InitBPFLogLevel() done");
    if (InitUnwindFlag(fd, config_.unwindStack_) != 0) {
        HHLOGD(true, "failed to init unwind stack flag in config_var_map");
        return -1;
    }
    HHLOGI(true, "InitUnwindFlag() done");
    return 0;
}

int BPFController::FillTargetPidMap() const
{
    int fd = bpf_map__fd(skel_->maps.target_pid_map);
    if (fd < 0) {
        HHLOGD(true, "failed to get fd of target_pid_map");
        return -1;
    }
    int index {0};
    uint32_t val {1}; // target_pid_Map[0] = 1 means tracing all processes
    int err {0};
    int numPids {config_.targetPids_.size()};
    HHLOGD(true, "target pid num = %d", numPids);
    if (numPids == 0) {
        // no target pid specified, trace all processes
        err = bpf_map_update_elem(fd, &index, &val, BPF_ANY);
        if (err) {
            HHLOGD(true, "failed to set target pid = %u", val);
            return -1;
        }
        return 0;
    }
    if (numPids > MAX_TARGET_PIDS) {
        HHLOGW(true, "BPFController WARN: number of target pids exceeds the maximum limit");
        numPids = MAX_TARGET_PIDS;
    }
    for (index = 1; index <= numPids; ++index) {
        val = static_cast<uint32_t>(config_.targetPids_[index - 1]);
        HHLOGD(true, "target pid = %d", val);
        std::cout << "target pid = " << val << std::endl;
        err = bpf_map_update_elem(fd, &index, &val, BPF_ANY);
        if (err) {
            HHLOGD(true, "failed to set target pid = %d", val);
            return -1;
        }
    }
    return 0;
}

inline int BPFController::ConfigBPFLogger()
{
    if (config_.BPFLogLevel_ == BPF_LOG_NONE) {
        HHLOGD(true, "bpf log level is NONE!");
        return 0;
    }
#if defined(BPF_LOGGER_DEBUG) || defined(BPF_LOGGER_INFO) || defined(BPF_LOGGER_WARN) ||    \
    defined(BPF_LOGGER_ERROR) || defined(BPF_LOGGER_FATAL)
    bpfLogReader_ = BPFLogReader::MakeUnique(config_.BPFLogFile_);
    if (bpfLogReader_ == nullptr) {
        HHLOGD(true, "failed to initialize BPFLogReader");
        return -1;
    }
#endif
    return 0;
}

inline int BPFController::ConfigLIBBPFLogger() const
{
    // set up libbpf print callback
    HHLOGI(true, "libbpf logger: file = %s, level = %d", config_.LIBBPFLogFile_.c_str(), config_.LIBBPFLogLevel_);
    libbpf_set_print(BPFController::LIBBPFPrintFunc);
    if (config_.LIBBPFLogLevel_ == LIBBPF_NONE) {
        HHLOGD(true, "libbpf log level is NONE!");
        return 0;
    }
    libbpfLogger = LIBBPFLogger::MakeUnique(config_.LIBBPFLogFile_, config_.LIBBPFLogLevel_);
    if (libbpfLogger == nullptr) {
        return -1;
    }
    return 0;
}

int BPFController::ConfigReceivers()
{
    if (config_.dumpEvents_ == 0) {
        rb_ = ring_buffer__new(
            bpf_map__fd(skel_->maps.bpf_ringbuf_map),
            BPFController::HandleEvent,
            this, nullptr);
        int err = libbpf_get_error(rb_);
        if (err) {
            HHLOGD(true, "failed to make BPF ring buffer: %s", strerror(-err));
            return err;
        }
        if (config_.pipelines_ == 0) {
            config_.pipelines_ = MIN_PIPELINES_LIMIT;
        }
        for (__u32 cnt = config_.pipelines_; cnt != 0; --cnt) {
            receivers_.push_back(BPFEventReceiver::MakeShared(dataFile_));
        }
        if (receivers_.size() != config_.pipelines_) {
            HHLOGD(true, "failed to make BPF event receivers");
            return -1;
        }
        last_ = 0;
    } else {
        rb_ = ring_buffer__new(
            bpf_map__fd(skel_->maps.bpf_ringbuf_map),
            BPFController::DumpEvent,
            this, nullptr);
        int err = libbpf_get_error(rb_);
        if (err) {
            HHLOGD(true, "failed to make BPF ring buffer: %s", strerror(-err));
            return err;
        }
    }
    return 0;
}

uint64_t BPFController::GetSymOffset(const std::string &path, const std::string &symbol)
{
    if (access(path.c_str(), F_OK) != 0) {
        HHLOGD(true, "the file does not exist");
        return 0;
    }
    using namespace OHOS::Developtools::Hiebpf;
    std::unique_ptr<ElfFile> elfFile = ElfFile::MakeUnique(path);
    if (elfFile == nullptr) {
        HHLOGD(true, "ELF file open failed");
        return 0;
    }
    const std::string dynsym {".dynsym"};
    if (elfFile->shdrs_.find(dynsym) == elfFile->shdrs_.end()) {
        HHLOGD(true, "section dynsym failed to obtain data");
        return 0;
    }
    const auto &sym = elfFile->shdrs_[dynsym];
    const uint8_t *symData = elfFile->GetSectionData(sym->secIndex_);

    const std::string dynstr {".dynstr"};
    if (elfFile->shdrs_.find(dynstr) == elfFile->shdrs_.end()) {
        HHLOGD(true, "section dynstr failed to obtain data");
        return 0;
    }
    const auto &str = elfFile->shdrs_[dynstr];
    const uint8_t *strData = elfFile->GetSectionData(str->secIndex_);

    uint32_t st_name = 0;
    uint64_t stepLength = 0;
    uint64_t vaddr = 0;
    while (stepLength < sym->secSize_) {
        int ret = memcpy_s(&st_name, sizeof(uint32_t), symData + stepLength, sizeof(uint32_t));
        if (ret != EOK) {
            HHLOGE(true, "failed to memcpy symData");
            return 0;
        }
        auto name = const_cast<uint8_t*>(strData + st_name);
        if (name != nullptr && std::string(reinterpret_cast<char*>(name)).compare(symbol) == 0) {
            int32_t valueOffset = sym->secEntrySize_ == sizeof(Elf64_Sym) ? SYM_64_VALUE_OFFSET : SYM_32_VALUE_OFFSET;
            int32_t valueSize = valueOffset == SYM_64_VALUE_OFFSET ? sizeof(uint64_t) : sizeof(uint32_t);
            ret = memcpy_s(&vaddr, sizeof(uint64_t), symData + stepLength + valueOffset, valueSize);
            if (ret != EOK) {
                HHLOGE(true, "failed to memcpy symData");
                return 0;
            }
            break;
        }
        stepLength += sym->secEntrySize_;
    }
    if (vaddr == 0) {
        HHLOGD(true, "get vaddr failed");
        return 0;
    }

    const std::string text {".text"};
    if (elfFile->shdrs_.find(text) == elfFile->shdrs_.end()) {
        HHLOGD(true, "section text failed to obtain data");
        return 0;
    }
    const auto &textPtr = elfFile->shdrs_[text];
    return vaddr - textPtr->secVaddr_ + textPtr->fileOffset_;
}

int32_t BPFController::ConfigDlopenBPFProg()
{
    uint64_t symOffset = GetSymOffset(THIRD_PARTY_MUSL_ADDR, "dlopen");
    if (symOffset == 0) {
        HHLOGD(true, "get symOffset failed");
        return -1;
    }
    skel_->links.uretprobe_dlopen = bpf_program__attach_uprobe(skel_->progs.uretprobe_dlopen,
                                                               true,
                                                               -1,
                                                               THIRD_PARTY_MUSL_ADDR.c_str(),
                                                               symOffset);
    if (!skel_->links.uretprobe_dlopen) {
        HHLOGD(true, "failed to attach uretprobe_dlopen");
        return -1;
    }
    return 0;
}

int BPFController::ConfigureBPF()
{
    if (InitBPFVariables() != 0) {
        HHLOGD(true, "failed to fill config_var_map");
        return -1;
    }
    HHLOGI(true, "InitBPFVariables() done");
    if (FillTargetPidMap() != 0) {
        HHLOGD(true, "failed to fill target_pid_map");
        return -1;
    }
    HHLOGI(true, "FillTargetPidMap() done");
    if (ConfigBPFLogger() != 0) {
        HHLOGW(true, "failed to configure BPF logger");
        return -1;
    }
    HHLOGI(true, "ConfigBPFLogger() done");
    if (ConfigReceivers() != 0) {
        HHLOGD(true, "failed to configure BPF ringbuffer");
        return -1;
    }
    HHLOGI(true, "ConfigReceivers() done");
    if (ConfigDlopenBPFProg() != 0) {
        HHLOGD(true, "failed to configure user BPF prog");
        return -1;
    }
    return 0;
}

int BPFController::Start()
{
#if defined(BPF_LOGGER_DEBUG) || defined(BPF_LOGGER_INFO) || defined(BPF_LOGGER_WARN) ||    \
    defined(BPF_LOGGER_ERROR) || defined(BPF_LOGGER_FATAL)
    if (StartBPFLogReader() != 0) {
        HHLOGD(true, "failed to start BPF log reader");
        return -1;
    }
#endif
    HHLOGI(true, "BPF log reader started");
    if (StartReceivers() != 0) {
        HHLOGD(true, "failed to start receivers");
        return -1;
    }
    HHLOGI(true, "receivers started");
    // activate events
    int err = hiebpf_bpf__attach(skel_);
    if (err) {
        HHLOGD(true, "failed to attach bpf object: %s", strerror(-err));
        return -1;
    }
    HHLOGI(true, "BPF events activated");

    const auto endTime = std::chrono::steady_clock::now() + std::chrono::seconds(config_.traceDuration_);
    while (!loopStop_) {
        if (BPFEventLoopOnce() != 0) {
            printf("libbpf error occured, hiebpf exit\n");
            err = -1;
            break;
        }
        if (std::chrono::steady_clock::now() >= endTime) {
            printf("timeout(%us), hiebpf exit\n", config_.traceDuration_);
            break;
        }
    }
    // receivers_ must stop after BPFEventLoopOnce();
    for (size_t k = 0; k < receivers_.size(); ++k) {
        receivers_[k]->Stop();
    }
    if (bpfLogReader_) {
        bpfLogReader_->Stop();
    }

    HHLOGI(true, "hiebpf stopped");

    return err;
}

void BPFController::Stop()
{
    loopStop_ = true;
}

int BPFController::HandleEvent(void *ctx, void *data, size_t dataSize)
{
    // get the next running receiver
    BPFController *bpfctlr = static_cast<BPFController *>(ctx);
    auto wrecv = bpfctlr->NextActiveReceiver();
    auto receiver = wrecv.lock();
    if (receiver == nullptr) {
        HHLOGF(true, "all receivers have stopped, will stop BPF event loop");
        bpfctlr->Stop();
        return -1;
    }

    // move data and notify receiver
    int ret = receiver->Put(data, dataSize);
    HHLOGE((ret < 0), "event lost: failed to move data to receiver"); // try other receivers ?
    HHLOGF(
        (0 <= ret and ret < static_cast<int>(dataSize)),
        "incomplete data movement: this should never happen");
    return ret;
}

static int DumpOpenat2Args(const struct fstrace_cmplt_event_t &cmplt_event)
{
    std::cout << "\nArgs:";
    std::cout << "\n    dfd = " << cmplt_event.start_event.openat2_args.dfd;
    std::cout << "\n    filename = " << static_cast<void*>(cmplt_event.start_event.openat2_args.filename);
    std::cout << "\n    how = " << static_cast<void*>(cmplt_event.start_event.openat2_args.how);
    return 0;
}

static int DumpReadvArgs(const struct fstrace_cmplt_event_t &cmplt_event)
{
    std::cout << "\nArgs:";
    std::cout << "\n    fd = " << cmplt_event.start_event.readv_args.fd;
    std::cout << "\n    vec = " << static_cast<void*>(cmplt_event.start_event.readv_args.vec);
    std::cout << "\n    vlen = " << cmplt_event.start_event.readv_args.vlen;
    std::cout << "\n    flags = " << cmplt_event.start_event.readv_args.flags;
    return 0;
}

static int DumpPreadvArgs(const struct fstrace_cmplt_event_t &cmplt_event)
{
    std::cout << "\nArgs:";
    std::cout << "\n    fd = " << cmplt_event.start_event.preadv_args.fd;
    std::cout << "\n    vec = " << static_cast<void*>(cmplt_event.start_event.preadv_args.vec);
    std::cout << "\n    vlen = " << cmplt_event.start_event.preadv_args.vlen;
    std::cout << "\n    pos = " << cmplt_event.start_event.preadv_args.pos;
    std::cout << "\n    flags = " << cmplt_event.start_event.preadv_args.flags;
    return 0;
}

static int DumpReadArgs(const struct fstrace_cmplt_event_t &cmplt_event)
{
    std::cout << "\nArgs:";
    std::cout << "\n    fd = " << cmplt_event.start_event.read_args.fd;
    std::cout << "\n    buf = " << static_cast<void*>(cmplt_event.start_event.read_args.buf);
    std::cout << "\n    count = " << cmplt_event.start_event.read_args.count;
    return 0;
}

static int DumpPread64Args(const struct fstrace_cmplt_event_t &cmplt_event)
{
    std::cout << "\nArgs:";
    std::cout << "\n    fd = " << cmplt_event.start_event.pread64_args.fd;
    std::cout << "\n    buf = " << static_cast<void*>(cmplt_event.start_event.pread64_args.buf);
    std::cout << "\n    count = " << cmplt_event.start_event.pread64_args.count;
    std::cout << "\n    pos = " << cmplt_event.start_event.pread64_args.pos;
    return 0;
}

static int DumpWritevArgs(const struct fstrace_cmplt_event_t &cmplt_event)
{
    std::cout << "\nArgs:";
    std::cout << "\n    fd = " << cmplt_event.start_event.writev_args.fd;
    std::cout << "\n    vec = " << static_cast<void*>(cmplt_event.start_event.writev_args.vec);
    std::cout << "\n    vlen = " << cmplt_event.start_event.writev_args.vlen;
    std::cout << "\n    flags = " << cmplt_event.start_event.writev_args.flags;
    return 0;
}

static int DumpPwritevArgs(const struct fstrace_cmplt_event_t &cmplt_event)
{
    std::cout << "\nArgs:";
    std::cout << "\n    fd = " << cmplt_event.start_event.pwritev_args.fd;
    std::cout << "\n    vec = " << static_cast<void*>(cmplt_event.start_event.pwritev_args.vec);
    std::cout << "\n    vlen = " << cmplt_event.start_event.pwritev_args.vlen;
    std::cout << "\n    pos = " << cmplt_event.start_event.pwritev_args.pos;
    std::cout << "\n    flags = " << cmplt_event.start_event.pwritev_args.flags;
    return 0;
}

static int DumpWriteArgs(const struct fstrace_cmplt_event_t &cmplt_event)
{
    std::cout << "\nArgs:";
    std::cout << "\n    fd = " << cmplt_event.start_event.write_args.fd;
    std::cout << "\n    buf = " << static_cast<void*>(cmplt_event.start_event.write_args.buf);
    std::cout << "\n    count = " << cmplt_event.start_event.write_args.count;
    return 0;
}

static int DumpPwrite64Args(const struct fstrace_cmplt_event_t &cmplt_event)
{
    std::cout << "\nArgs:";
    std::cout << "\n    fd = " << cmplt_event.start_event.pwrite64_args.fd;
    std::cout << "\n    buf = " << static_cast<void*>(cmplt_event.start_event.pwrite64_args.buf);
    std::cout << "\n    count = " << cmplt_event.start_event.pwrite64_args.count;
    std::cout << "\n    pos = " << cmplt_event.start_event.pwrite64_args.pos;
    return 0;
}

static int DumpCloseArgs(const struct fstrace_cmplt_event_t &cmplt_event)
{
    std::cout << "\nArgs:";
    std::cout << "\n    files = " << cmplt_event.start_event.close_args.files;
    std::cout << "\n    fd = " << cmplt_event.start_event.close_args.fd;
    return 0;
}

static int DumpTypeAndArgs(const struct fstrace_cmplt_event_t &cmplt_event)
{
    std::cout << "\nevent type:     ";
    switch (cmplt_event.start_event.type) {
        case SYS_OPENAT2: std::cout << "openat2"; return DumpOpenat2Args(cmplt_event);

        case SYS_READV: std::cout << "readv"; return DumpReadvArgs(cmplt_event);
        case SYS_PREADV: std::cout << "preadv"; return DumpPreadvArgs(cmplt_event);
        case SYS_READ: std::cout << "read"; return DumpReadArgs(cmplt_event);
        case SYS_PREAD64: std::cout << "pread64"; return DumpPread64Args(cmplt_event);

        case SYS_WRITEV: std::cout << "writev"; return DumpWritevArgs(cmplt_event);
        case SYS_PWRITEV: std::cout << "pwritev"; return DumpPwritevArgs(cmplt_event);
        case SYS_WRITE: std::cout << "write"; return DumpWriteArgs(cmplt_event);
        case SYS_PWRITE64: std::cout << "pwrite64"; return DumpPwrite64Args(cmplt_event);

        case SYS_CLOSE: std::cout << "close"; return DumpCloseArgs(cmplt_event);
    }
    HHLOGE(true, "unreognized fstrace event type = %d", cmplt_event.start_event.type);
    return -1;
}

int BPFController::DumpFSTraceEvent(BPFController *bpfctlr, void *data, size_t dataSize)
{
    if (dataSize != sizeof(fstrace_cmplt_event_t)) {
        std::cout << "DumpFSTraceEvent ERROR: size dismatch:"
                  << " data size = " << dataSize
                  << " fstrace event size = " << sizeof(fstrace_cmplt_event_t)
                  << std::endl;
        return -1;
    }
    struct fstrace_cmplt_event_t cmplt_event {};
    if (memcpy_s(&cmplt_event, sizeof(fstrace_cmplt_event_t), data, dataSize) != EOK) {
        std::cout << "failed to copy data to fstrace_cmplt_event_t" << std::endl;
        return -1;
    }
    std::cout << "\nFSTrace Event:"
              << "\ndata size:      " << dataSize;
    DumpTypeAndArgs(cmplt_event);
    std::cout << "\nretval:         " << cmplt_event.retval
              << "\nstart time:     " << cmplt_event.start_event.stime
              << "\nexit time:      " << cmplt_event.ctime
              << "\npid:            " << cmplt_event.pid
              << "\ntgid:           " << cmplt_event.tgid
              << "\ncomm:           " << cmplt_event.comm
              << "\nips:            " << cmplt_event.nips
              << "\nips:"
              << std::setw(16) << std::hex;
    for (int i = 0; i < cmplt_event.nips; ++i) {
        std::cout << "\n    " << cmplt_event.ips[i];
    }
    std::cout << std::dec << std::endl;
    return 0;
}

int BPFController::DumpPFTraceEvent(BPFController *bpfctlr, void *data, size_t dataSize)
{
    if (dataSize != sizeof(pftrace_cmplt_event_t)) {
        std::cout << "DumpPFTraceEvent ERROR: size dismatch:"
                  << " data size = " << dataSize
                  << " pftrace event size = " << sizeof(pftrace_cmplt_event_t)
                  << std::endl;
        return -1;
    }
    struct pftrace_cmplt_event_t cmplt_event {};
    if (memcpy_s(&cmplt_event, sizeof(pftrace_cmplt_event_t), data, dataSize) != EOK) {
        std::cout << "failed to copy data to pftrace_cmplt_event_t" << std::endl;
        return -1;
    }
    std::cout << "PFTrace Event:"
              << "\ndata size:      " << dataSize
              << "\nevent type:     ";
    switch (cmplt_event.start_event.type) {
        case PF_COPY_ON_WRITE:  std::cout << "Copy On  Write"; break;
        case PF_FAKE_ZERO_PAGE: std::cout << "Zero FAKE Page"; break;
        case PF_FILE_BACKED_IN: std::cout << "File Backed In"; break;
        case PF_PAGE_CACHE_HIT: std::cout << "Page Cache Hit"; break;
        case PF_SWAP_FROM_DISK: std::cout << "Swap From Disk"; break;
        case PF_SWAP_FROM_ZRAM: std::cout << "Swap From Zram"; break;
        case PF_ZERO_FILL_PAGE: std::cout << "Zero Fill Page"; break;
        default: std::cout << cmplt_event.start_event.type;
    }
    std::cout << "\naddress:        " << cmplt_event.start_event.addr
              << "\nsize:           " << cmplt_event.size
              << "\nstart time:     " << cmplt_event.start_event.stime
              << "\nexit time:      " << cmplt_event.ctime
              << "\npid:            " << cmplt_event.pid
              << "\ntgid:           " << cmplt_event.tgid
              << "\ncomm:           " << cmplt_event.comm
              << "\nips:            " << cmplt_event.nips
              << std::setw(16) << std::hex;
    for (int i = 0; i < cmplt_event.nips; ++i) {
        std::cout << "\n    " << cmplt_event.ips[i];
    }
    std::cout << std::dec << std::endl;
    return 0;
}

int BPFController::DumpBIOTraceEvent(BPFController *bpfctlr, void *data, size_t dataSize)
{
    if (dataSize != sizeof(biotrace_cmplt_event_t)) {
        std::cout << "DumpBIOTraceEvent ERROR: size dismatch:"
                  << " data size = " << dataSize
                  << " biotrace event size = " << sizeof(biotrace_cmplt_event_t)
                  << std::endl;
        return -1;
    }
    struct biotrace_cmplt_event_t cmplt_event {};
    if (memcpy_s(&cmplt_event, sizeof(biotrace_cmplt_event_t), data, dataSize) != EOK) {
        std::cout << "failed to copy data to biotrace_cmplt_event_t" << std::endl;
        return -1;
    }
    std::cout << "BIOTrace Event:"
              << "\ndata size:      " << dataSize
              << "\nevent type:     ";
    switch (cmplt_event.start_event.type) {
        case BIO_DATA_READ: std::cout << "DATA_READ"; break;
        case BIO_DATA_WRITE: std::cout << "DATA_WRITE"; break;
        case BIO_METADATA_READ: std::cout << "METADATA_READ"; break;
        case BIO_METADATA_WRITE: std::cout << "METADATA_WRITE"; break;
        case BIO_PAGE_IN: std::cout << "PAGE_IN"; break;
        case BIO_PAGE_OUT: std::cout << "PAGE_OUT"; break;
        default: std::cout << cmplt_event.start_event.type;
    }

    std::cout << "\nstart time:     " << cmplt_event.start_event.stime
              << "\nexit time:      " << cmplt_event.ctime
              << "\npid:            " << cmplt_event.start_event.pid
              << "\ntgid:           " << cmplt_event.start_event.tgid
              << "\ncomm:           " << cmplt_event.start_event.comm
              << "\nprio:           " << cmplt_event.prio
              << "\nsize:           " << cmplt_event.start_event.size
              << "\nblkcnt:         " << cmplt_event.blkcnt
              << "\nips:            " << cmplt_event.nips
              << std::setw(16) << std::hex;
    for (int i = 0; i < cmplt_event.nips; ++i) {
        std::cout << "\n    " << cmplt_event.ips[i];
    }
    std::cout << std::dec << std::endl;
    return 0;
}

int BPFController::DumpSTRTraceEvent(void *data, size_t dataSize)
{
    if (dataSize != sizeof(strtrace_cmplt_event_t)) {
        std::cout << "DumpSTRTraceEvent ERROR: size dismatch:"
                  << " data size = " << dataSize
                  << " strtrace event size = " << sizeof(strtrace_cmplt_event_t)
                  << std::endl;
        return -1;
    }
    struct strtrace_cmplt_event_t cmplt_event {};
    if (memcpy_s(&cmplt_event, sizeof(strtrace_cmplt_event_t), data, dataSize) != EOK) {
        std::cout << "failed to copy data to strtrace_cmplt_event_t" << std::endl;
        return -1;
    }
    std::cout << "STRTrace Event:"
              << "\ndata size:      " << dataSize
              << "\ntracer:         " << cmplt_event.start_event.stracer
              << "\ntype:           " << cmplt_event.start_event.type
              << "\naddress:        " << cmplt_event.start_event.addr
              << "\nstart time:     " << cmplt_event.start_event.stime
              << "\npid:            " << cmplt_event.pid
              << "\ntgid:           " << cmplt_event.tgid
              << "\nfilename len:   " << cmplt_event.len
              << "\nfilename:       " << cmplt_event.filename
              << std::endl;
    return 0;
}

int BPFController::DumpEvent(void *ctx, void *data, size_t dataSize)
{
    const __u32 *tracer = static_cast<const __u32 *>(data);
    BPFController *bpfctlr = static_cast<BPFController *>(ctx);
    if (bpfctlr->config_.dumpEvents_) {
        --bpfctlr->config_.dumpEvents_;
        static __u32 counter {0};
        std::cout << "\ncounter = " << ++counter;
        switch (*tracer) {
            case FSTRACE: return DumpFSTraceEvent(bpfctlr, data, dataSize);
            case PFTRACE: return DumpPFTraceEvent(bpfctlr, data, dataSize);
            case BIOTRACE: return DumpBIOTraceEvent(bpfctlr, data, dataSize);
            case STRTRACE: return DumpSTRTraceEvent(data, dataSize);
        }
        std::cout << "DumpEvent ERROR: bad tracer type = " << (*tracer) << std::endl;
    }
    return 0;
}

std::weak_ptr<BPFEventReceiver> BPFController::NextActiveReceiver()
{
    __u32 next = last_ + 1;
    __u32 total = receivers_.size();
    for (;;) {
        if (next >= total) {
            next -= total;
        }
        if (receivers_[next]->Running() or next == last_) {
            break;
        }
        ++next;
    }
    if (receivers_[next]->Running()) {
        last_ = next;
        return receivers_[last_];
    }
    return std::weak_ptr<BPFEventReceiver>();
}