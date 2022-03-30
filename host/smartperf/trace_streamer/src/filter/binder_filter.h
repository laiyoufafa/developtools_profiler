/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
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

#ifndef BINDER_FILTER_H
#define BINDER_FILTER_H

#include <unordered_set>
#include "args_set.h"
#include "filter_base.h"
#include "trace_data_cache.h"
#include "trace_streamer_filters.h"
namespace SysTuning {
namespace TraceStreamer {
class BinderFilter : private FilterBase {
public:
    BinderFilter(TraceDataCache*, const TraceStreamerFilters*);
    BinderFilter(const BinderFilter&) = delete;
    BinderFilter& operator=(const BinderFilter&) = delete;
    ~BinderFilter() override;

public:
    void SendTraction(int64_t ts,
                      uint32_t tid,
                      uint64_t transactionId,
                      int32_t destNode,
                      int32_t destTgid,
                      int32_t destTid,
                      bool isReply,
                      int32_t flags,
                      int32_t code);
    void ReceiveTraction(int64_t ts, uint32_t pid, uint64_t transactionId);
    void TransactionAllocBuf(int64_t ts, uint32_t pid, uint64_t dataSize, uint64_t offsetsSize);
    void TractionLock(int64_t ts, uint32_t pid, const std::string& tag);
    void TractionLocked(int64_t ts, uint32_t pid, const std::string& tag);
    void TractionUnlock(int64_t ts, uint32_t pid, const std::string& tag);
    void FinishBinderEvent();

private:
    void MaybeDealEvent();

    class TSSendTractionEvent {
    public:
        TSSendTractionEvent(int64_t ts,
                            uint32_t tid,
                            uint64_t transactionId,
                            int32_t destNode,
                            int32_t destTgid,
                            int32_t destTid,
                            bool isReply,
                            int32_t flags,
                            int32_t code)
            : ts_(ts),
              tid_(tid),
              transactionId_(transactionId),
              destNode_(destNode),
              destTgid_(destTgid),
              destTid_(destTid),
              isReply_(isReply),
              flags_(flags),
              code_(code)
        {
        }
        ~TSSendTractionEvent() {}
        int64_t ts_;
        uint32_t tid_;
        uint64_t transactionId_;
        int32_t destNode_;
        int32_t destTgid_;
        int32_t destTid_;
        bool isReply_;
        int32_t flags_;
        int32_t code_;
    };
    class TSReceiveTractionEvent {
    public:
        TSReceiveTractionEvent(int64_t ts, uint32_t pid, uint64_t transactionId)
            : ts_(ts), pid_(pid), transactionId_(transactionId)
        {
        }
        ~TSReceiveTractionEvent() {}
        uint64_t ts_;
        uint32_t pid_;
        uint64_t transactionId_;
    };
    class TSTractionLockEvent {
    public:
        TSTractionLockEvent(int64_t ts, uint32_t pid, const std::string& tag) : ts_(ts), pid_(pid), tag_(tag) {}
        ~TSTractionLockEvent() {}
        uint64_t ts_;
        uint32_t pid_;
        const std::string tag_;
    };
    class TSTransactionAllocBufEvent {
    public:
        TSTransactionAllocBufEvent(int64_t ts, uint32_t pid, uint64_t dataSize, uint64_t offsetsSize)
            : ts_(ts), pid_(pid), dataSize_(dataSize), offsetsSize_(offsetsSize)
        {
        }
        ~TSTransactionAllocBufEvent() {}
        uint64_t ts_;
        uint32_t pid_;
        uint64_t dataSize_;
        uint64_t offsetsSize_;
    };
    enum TSBinderEventType {
        TS_EVENT_BINDER_SEND,
        TS_EVENT_BINDER_RECIVED,
        TS_EVENT_BINDER_ALLOC_BUF,
        TS_EVENT_BINDER_LOCK,
        TS_EVENT_BINDER_LOCKED,
        TS_EVENT_BINDER_UNLOCK
    };
    class TSBinderEvent {
    public:
        TSBinderEvent() {}
        ~TSBinderEvent() {}
        TSBinderEventType type_;
        // us union below will be a good choice
        // but union with unique_ptr can bring about runtime error on windows and mac,only work well on linux
        std::unique_ptr<TSSendTractionEvent> senderBinderEvent_ = {};
        std::unique_ptr<TSReceiveTractionEvent> receivedBinderEvent_ = {};
        std::unique_ptr<TSTransactionAllocBufEvent> binderAllocBufEvent_ = {};
        std::unique_ptr<TSTractionLockEvent> binderLockEvent_ = {};
        std::unique_ptr<TSTractionLockEvent> binderLockedEvent_ = {};
        std::unique_ptr<TSTractionLockEvent> binderUnlockEvent_ = {};
    };
    void DealEvent(const TSBinderEvent* event);

    void ExecSendTraction(int64_t ts,
                          uint32_t tid,
                          uint64_t transactionId,
                          int32_t destNode,
                          int32_t destTgid,
                          int32_t destTid,
                          bool isReply,
                          int32_t flags,
                          int32_t code);
    void ExecReceiveTraction(int64_t ts, uint32_t pid, uint64_t transactionId);
    void ExecTransactionAllocBuf(int64_t ts, uint32_t pid, uint64_t dataSize, uint64_t offsetsSize);
    void ExecTractionLock(int64_t ts, uint32_t pid, const std::string& tag);
    void ExecTractionLocked(int64_t ts, uint32_t pid, const std::string& tag);
    void ExecTractionUnlock(int64_t ts, uint32_t pid, const std::string& tag);
    std::string GetBinderFlagsDesc(uint32_t flag);
    bool IsValidUint32(uint32_t value) const
    {
        return (value != INVALID_UINT32);
    }
    uint32_t noReturnMsgFlag_ = 0x01;
    uint32_t rootObjectMsgFlag_ = 0x04;
    uint32_t statusCodeMsgFlag_ = 0x08;
    uint32_t acceptFdsMsgFlag_ = 0x10;
    uint32_t noFlagsMsgFlag_ = 0;
    DataIndex binderCatalogId_ = traceDataCache_->GetDataIndex("binder");
    DataIndex replyId_ = traceDataCache_->GetDataIndex("binder reply");
    DataIndex isReplayId_ = traceDataCache_->GetDataIndex("reply transaction?");
    DataIndex flagsId_ = traceDataCache_->GetDataIndex("flags");
    DataIndex transSliceId_ = traceDataCache_->GetDataIndex("binder transaction");
    DataIndex transId_ = traceDataCache_->GetDataIndex("transaction id");
    DataIndex asyncRcvId_ = traceDataCache_->GetDataIndex("binder async rcv");
    DataIndex codeId_ = traceDataCache_->GetDataIndex("code");
    DataIndex callingTid_ = traceDataCache_->GetDataIndex("calling tid");
    DataIndex destNodeId_ = traceDataCache_->GetDataIndex("destination node");
    DataIndex destThreadId_ = traceDataCache_->GetDataIndex("destination thread");
    DataIndex destThreadNameId_ = traceDataCache_->GetDataIndex("destination name");
    DataIndex destSliceId_ = traceDataCache_->GetDataIndex("destination slice id");
    DataIndex destProcessId_ = traceDataCache_->GetDataIndex("destination process");
    DataIndex transAsyncId_ = traceDataCache_->GetDataIndex("binder transaction async");
    DataIndex lockTryId_ = traceDataCache_->GetDataIndex("binder lock waiting");
    DataIndex lockHoldId_ = traceDataCache_->GetDataIndex("binder lock held");
    DataIndex dataSizeId_ = traceDataCache_->GetDataIndex("data size");
    DataIndex dataOffsetSizeId_ = traceDataCache_->GetDataIndex("offsets size");
    DataIndex nullStringId_ = traceDataCache_->GetDataIndex("null");
    std::unordered_map<uint64_t, int64_t> lastEventTs_ = {};
    std::unordered_set<uint64_t> transReplyWaitingReply_ = {};
    std::unordered_map<uint64_t, FilterId> transWaitingRcv_ = {};
    std::unordered_map<uint64_t, ArgsSet> transNoNeedReply_ = {};
    std::unordered_map<int, std::string> binderFlagDescs_ = {};
    std::multimap<uint64_t, std::unique_ptr<TSBinderEvent>> tsBinderEventQueue_;
    // timestamp of ftrace events from different cpu can be outof order
    // keep a cache of ftrace events in memory and keep msg in order
    // the value below is the count of msg, maybe you can change it
    const size_t MAX_CACHE_SIZE = 10000;
};
} // namespace TraceStreamer
} // namespace SysTuning

#endif // BINDER_FILTER_H
