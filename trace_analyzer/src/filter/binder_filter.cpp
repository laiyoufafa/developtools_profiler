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
#include "binder_filter.h"
#include "measure_filter.h"
#include "process_filter.h"
#include "slice_filter.h"
#include "stat_filter.h"
#include "string_to_numerical.h"
namespace SysTuning {
namespace TraceStreamer {
BinderFilter::BinderFilter(TraceDataCache* dataCache, const TraceStreamerFilters* filter)
    : FilterBase(dataCache, filter)
{
    binderFlagDescs_ = {{noReturnMsgFlag_, " this is a one-way call: async, no return; "},
                        {rootObjectMsgFlag_, " contents are the components root object; "},
                        {statusCodeMsgFlag_, " contents are a 32-bit status code; "},
                        {acceptFdsMsgFlag_, " allow replies with file descriptors; "},
                        {noFlagsMsgFlag_, " No Flags Set"}};
}
BinderFilter::~BinderFilter() = default;

std::string BinderFilter::GetBinderFlagsDesc(uint32_t flag)
{
    std::string str;
    if (flag & noReturnMsgFlag_) {
        str += binderFlagDescs_.at(noReturnMsgFlag_);
    }
    if (flag & rootObjectMsgFlag_) {
        str += binderFlagDescs_.at(rootObjectMsgFlag_);
    }
    if (flag & statusCodeMsgFlag_) {
        str += binderFlagDescs_.at(statusCodeMsgFlag_);
    }
    if (flag & acceptFdsMsgFlag_) {
        str += binderFlagDescs_.at(acceptFdsMsgFlag_);
    }
    if (flag == noFlagsMsgFlag_) {
        str += binderFlagDescs_.at(noFlagsMsgFlag_);
    }
    return str;
}
void BinderFilter::MaybeDealEvent()
{
    if (tsBinderEventQueue_.size() > MAX_CACHE_SIZE) {
        DealEvent(tsBinderEventQueue_.begin()->second.get());
        tsBinderEventQueue_.erase(tsBinderEventQueue_.begin());
    }
}

void BinderFilter::FinishBinderEvent()
{
    for (auto it = tsBinderEventQueue_.begin(); it != tsBinderEventQueue_.end(); it++) {
        DealEvent(it->second.get());
    }
    tsBinderEventQueue_.clear();
}

void BinderFilter::SendTraction(int64_t ts,
                                uint32_t tid,
                                uint64_t transactionId,
                                int32_t destNode,
                                int32_t destTgid,
                                int32_t destTid,
                                bool isReply,
                                int32_t flags,
                                int32_t code)
{
    auto sendTractionEvent = std::make_unique<TSSendTractionEvent>(ts, tid, transactionId, destNode, destTgid, destTid, isReply, flags, code);
    auto binderEvent = std::make_unique<TSBinderEvent>();
    binderEvent->type_ = TS_EVENT_BINDER_SEND;
    binderEvent->senderBinderEvent_ = std::move(sendTractionEvent);
    tsBinderEventQueue_.insert(std::make_pair(ts, std::move(binderEvent)));
    MaybeDealEvent();
}
void BinderFilter::ReceiveTraction(int64_t ts, uint32_t pid, uint64_t transactionId)
{
    auto receiveTractionEvent = std::make_unique<TSReceiveTractionEvent>(ts, pid, transactionId);
    auto binderEvent = std::make_unique<TSBinderEvent>();
    binderEvent->type_ = TS_EVENT_BINDER_RECIVED;
    binderEvent->receivedBinderEvent_ = std::move(receiveTractionEvent);
    tsBinderEventQueue_.insert(std::make_pair(ts,
        std::move(binderEvent)));
    MaybeDealEvent();
}
void BinderFilter::TransactionAllocBuf(int64_t ts, uint32_t pid, uint64_t dataSize, uint64_t offsetsSize)
{
    auto tractionAllocBufEvent = std::make_unique<TSTransactionAllocBufEvent>(ts, pid, dataSize, offsetsSize);
    auto binderEvent = std::make_unique<TSBinderEvent>();
    binderEvent->type_ = TS_EVENT_BINDER_ALLOC_BUF;
    binderEvent->binderAllocBufEvent_ = std::move(tractionAllocBufEvent);
    tsBinderEventQueue_.insert(std::make_pair(ts,
        std::move(binderEvent)));
    MaybeDealEvent();
}
void BinderFilter::TractionLock(int64_t ts, uint32_t pid, const std::string& tag)
{
    auto tractionLockEvent = std::make_unique<TSTractionLockEvent>(ts, pid, tag);
    auto binderEvent = std::make_unique<TSBinderEvent>();
    binderEvent->type_ = TS_EVENT_BINDER_LOCK;
    binderEvent->binderLockEvent_ = std::move(tractionLockEvent);
    tsBinderEventQueue_.insert(std::make_pair(ts,
        std::move(binderEvent)));
    MaybeDealEvent();
}
void BinderFilter::TractionLocked(int64_t ts, uint32_t pid, const std::string& tag)
{
    auto tractionLockedEvent = std::make_unique<TSTractionLockEvent>(ts, pid, tag);
    auto binderEvent = std::make_unique<TSBinderEvent>();
    binderEvent->type_ = TS_EVENT_BINDER_LOCKED;
    binderEvent->binderLockedEvent_ = std::move(tractionLockedEvent);
    tsBinderEventQueue_.insert(std::make_pair(ts,
        std::move(binderEvent)));
    MaybeDealEvent();
}
void BinderFilter::TractionUnlock(int64_t ts, uint32_t pid, const std::string& tag)
{
    auto tractionUnlockEvent = std::make_unique<TSTractionLockEvent>(ts, pid, tag);
    auto binderEvent = std::make_unique<TSBinderEvent>();
    binderEvent->type_ = TS_EVENT_BINDER_UNLOCK;
    binderEvent->binderUnlockEvent_ = std::move(tractionUnlockEvent);
    tsBinderEventQueue_.insert(std::make_pair(ts,
        std::move(binderEvent)));
    MaybeDealEvent();
}
void BinderFilter::DealEvent(const TSBinderEvent* event)
{
    switch (static_cast<size_t>(event->type_)) {
        case TS_EVENT_BINDER_SEND:
            ExecSendTraction(event->senderBinderEvent_->ts_, event->senderBinderEvent_->tid_,
                             event->senderBinderEvent_->transactionId_, event->senderBinderEvent_->destNode_,
                             event->senderBinderEvent_->destTgid_, event->senderBinderEvent_->destTid_,
                             event->senderBinderEvent_->isReply_, event->senderBinderEvent_->flags_,
                             event->senderBinderEvent_->code_);
            break;
        case TS_EVENT_BINDER_RECIVED:
            ExecReceiveTraction(event->receivedBinderEvent_->ts_, event->receivedBinderEvent_->pid_,
                                event->receivedBinderEvent_->transactionId_);
            break;
        case TS_EVENT_BINDER_ALLOC_BUF:
            ExecTransactionAllocBuf(event->binderAllocBufEvent_->ts_, event->binderAllocBufEvent_->pid_,
                                    event->binderAllocBufEvent_->dataSize_, event->binderAllocBufEvent_->offsetsSize_);
            break;
        case TS_EVENT_BINDER_LOCK:
            ExecTractionLock(event->binderLockEvent_->ts_, event->binderLockEvent_->pid_,
                             event->binderLockEvent_->tag_);
            break;
        case TS_EVENT_BINDER_LOCKED:
            ExecTractionLocked(event->binderLockedEvent_->ts_, event->binderLockedEvent_->pid_,
                               event->binderLockedEvent_->tag_);
            break;
        case TS_EVENT_BINDER_UNLOCK:
            ExecTractionUnlock(event->binderUnlockEvent_->ts_, event->binderUnlockEvent_->pid_,
                               event->binderUnlockEvent_->tag_);
            break;
        default:
            break;
    }
}
void BinderFilter::ExecSendTraction(int64_t ts,
                                    uint32_t tid,
                                    uint64_t transactionId,
                                    int32_t destNode,
                                    int32_t destTgid,
                                    int32_t destTid,
                                    bool isReply,
                                    int32_t flags,
                                    int32_t code)
{
    auto flagsStr = traceDataCache_->GetDataIndex("0x" + base::number(flags, base::INTEGER_RADIX_TYPE_HEX) +
                                                  GetBinderFlagsDesc(flags));
    DataIndex codeStr = traceDataCache_->GetDataIndex("0x" + base::number(code, base::INTEGER_RADIX_TYPE_HEX) +
                                                      " Java Layer Dependent");
    ArgsSet argsSend;
    argsSend.AppendArg(transId_, BASE_DATA_TYPE_INT, transactionId);
    argsSend.AppendArg(destNodeId_, BASE_DATA_TYPE_INT, destNode);
    argsSend.AppendArg(destProcessId_, BASE_DATA_TYPE_INT, destTgid);
    argsSend.AppendArg(isReplayId_, BASE_DATA_TYPE_BOOLEAN, isReply);
    argsSend.AppendArg(flagsId_, BASE_DATA_TYPE_STRING, flagsStr);
    argsSend.AppendArg(codeId_, BASE_DATA_TYPE_STRING, codeStr);
    argsSend.AppendArg(callingTid_, BASE_DATA_TYPE_INT, tid);

    if (isReply) {
        // Add dest information to Reply slices, the Begin msg is from TAG-2
        InternalTid dstItid = streamFilters_->processFilter_->UpdateOrCreateThread(ts, destTid);
        const auto destThreadName = traceDataCache_->GetConstThreadData(dstItid).nameIndex_;
        argsSend.AppendArg(destThreadId_, BASE_DATA_TYPE_INT, destTid);
        argsSend.AppendArg(destThreadNameId_, BASE_DATA_TYPE_STRING, destThreadName);
        streamFilters_->sliceFilter_->EndBinder(ts, tid, nullStringId_, nullStringId_, argsSend);
        transReplyWaitingReply_.insert(transactionId);
        return;
    } else {
        bool needReply = !isReply && !(flags & noReturnMsgFlag_);
        if (needReply) {
            // transaction needs reply TAG-1
            streamFilters_->sliceFilter_->BeginBinder(ts, tid, binderCatalogId_, transSliceId_, argsSend);
            transWaitingRcv_[transactionId] = tid;
        } else {
            // transaction not need reply
            streamFilters_->sliceFilter_->BeginAsyncBinder(ts, tid, binderCatalogId_, transAsyncId_, argsSend);
            transNoNeedReply_[transactionId] = argsSend;
        }
    }
}
void BinderFilter::ExecReceiveTraction(int64_t ts, uint32_t pid, uint64_t transactionId)
{
    InternalTid internalTid = streamFilters_->processFilter_->UpdateOrCreateThread(ts, pid);
    const auto threadName = traceDataCache_->GetConstThreadData(internalTid).nameIndex_;
    if (transReplyWaitingReply_.count(transactionId)) {
        streamFilters_->sliceFilter_->EndBinder(ts, pid);
        transReplyWaitingReply_.erase(transactionId);
        return;
    }

    if (transWaitingRcv_.count(transactionId)) {
        // First, begin the reply, the reply will be end in "SendTraction" func, and the isReply will be true, TAG-2
        auto replySliceid = streamFilters_->sliceFilter_->BeginBinder(ts, pid, binderCatalogId_, replyId_);
        // Add dest info to the reply
        ArgsSet args;
        args.AppendArg(destThreadId_, BASE_DATA_TYPE_INT, pid);
        args.AppendArg(destThreadNameId_, BASE_DATA_TYPE_STRING, threadName);
        if (IsValidUint32(static_cast<uint32_t>(replySliceid))) {
            args.AppendArg(destSliceId_, BASE_DATA_TYPE_INT, replySliceid);
        }
        // Add dest args
        auto transSliceId = streamFilters_->sliceFilter_->AddArgs(transWaitingRcv_[transactionId], binderCatalogId_,
                                                                  transSliceId_, args);

        // remeber dest slice-id to the argset form "SendTraction" TAG-1
        ArgsSet replyDestInserter;
        if (IsValidUint32(transSliceId)) {
            replyDestInserter.AppendArg(destSliceId_, BASE_DATA_TYPE_INT, transSliceId);
        }
        streamFilters_->sliceFilter_->AddArgs(pid, binderCatalogId_, replyId_, replyDestInserter);
        transWaitingRcv_.erase(transactionId);
        return;
    }
    // the code below can be hard to understand, may be a EndBinder will be better
    // this problem cna be test after the IDE is finished
    if (transNoNeedReply_.count(transactionId)) {
        auto args = transNoNeedReply_[transactionId];
        streamFilters_->sliceFilter_->BeginAsyncBinder(ts, pid, binderCatalogId_, asyncRcvId_, args);
        transNoNeedReply_.erase(transactionId);
        return;
    }
}
void BinderFilter::ExecTransactionAllocBuf(int64_t ts, uint32_t pid, uint64_t dataSize, uint64_t offsetsSize)
{
    ArgsSet args;
    args.AppendArg(dataSizeId_, BASE_DATA_TYPE_INT, dataSize);
    args.AppendArg(dataOffsetSizeId_, BASE_DATA_TYPE_INT, offsetsSize);
    streamFilters_->sliceFilter_->AddArgs(pid, binderCatalogId_, transSliceId_, args);
    UNUSED(ts);
}
void BinderFilter::ExecTractionLock(int64_t ts, uint32_t pid, const std::string& tag)
{
    lastEventTs_[pid] = ts;
    streamFilters_->sliceFilter_->BeginBinder(ts, pid, binderCatalogId_, lockTryId_);
}
void BinderFilter::ExecTractionLocked(int64_t ts, uint32_t pid, const std::string& tag)
{
    if (!lastEventTs_.count(pid)) {
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_BINDER_TRANSACTION_LOCKED, STAT_EVENT_NOTMATCH);
        return;
    }
    streamFilters_->sliceFilter_->EndBinder(ts, pid);
    streamFilters_->sliceFilter_->BeginBinder(ts, pid, binderCatalogId_, lockHoldId_);
    lastEventTs_.erase(pid);
    lastEventTs_[pid] = ts;
}
void BinderFilter::ExecTractionUnlock(int64_t ts, uint32_t pid, const std::string& tag)
{
    if (!lastEventTs_.count(pid)) {
        streamFilters_->statFilter_->IncreaseStat(TRACE_EVENT_BINDER_TRANSACTION_UNLOCK, STAT_EVENT_NOTMATCH);
        return;
    }
    streamFilters_->sliceFilter_->EndBinder(ts, pid);
    lastEventTs_.erase(pid);
    lastEventTs_[pid] = ts;
}
} // namespace TraceStreamer
} // namespace SysTuning
