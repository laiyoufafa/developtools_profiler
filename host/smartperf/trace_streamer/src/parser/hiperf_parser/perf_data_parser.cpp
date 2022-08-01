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
#include "perf_data_parser.h"
#include "perf_data_filter.h"
#include "stat_filter.h"

namespace SysTuning {
namespace TraceStreamer {
PerfDataParser::PerfDataParser(TraceDataCache* dataCache, const TraceStreamerFilters* ctx)
    : HtracePluginTimeParser(dataCache, ctx)
{
    configNameIndex_ = traceDataCache_->dataDict_.GetStringIndex("config_name");
    workloaderIndex_ = traceDataCache_->dataDict_.GetStringIndex("workload_cmd");
    cmdlineIndex_ = traceDataCache_->dataDict_.GetStringIndex("cmdline");
    runingStateIndex_ = traceDataCache_->dataDict_.GetStringIndex("Running");
    suspendStatIndex_ = traceDataCache_->dataDict_.GetStringIndex("Suspend");
    unkonwnStateIndex_ = traceDataCache_->dataDict_.GetStringIndex("-");
}
void PerfDataParser::InitPerfDataAndLoad(const std::deque<uint8_t> dequeBuffer)
{
    bufferSize_ = dequeBuffer.size();
    buffer_ = std::make_unique<uint8_t[]>(dequeBuffer.size());
    std::copy(dequeBuffer.begin(), dequeBuffer.end(), buffer_.get());
    LoadPerfData();
}
PerfDataParser::~PerfDataParser()
{
    TS_LOGI("perf data ts MIN:%llu, MAX:%llu", static_cast<unsigned long long>(GetPluginStartTime()),
            static_cast<unsigned long long>(GetPluginEndTime()));
}

bool PerfDataParser::LoadPerfData()
{
    TS_LOGI("enter");
    // try load the perf data
    recordDataReader_ = PerfFileReader::Instance(buffer_.get(), bufferSize_);
    buffer_.release();
    if (recordDataReader_ == nullptr) {
        return false;
    }

    if (!recordDataReader_->ReadFeatureSection()) {
        printf("record format error.\n");
        return false;
    }
    // update perf report table
    UpdateEventConfigInfo();
    UpdateReportWorkloadInfo();
    UpdateCmdlineInfo();

    // update perf Files table
    UpdateSymbolAndFilesData();

    TS_LOGD("process record");
    UpdateClockType();
    recordDataReader_->ReadDataSection(std::bind(&PerfDataParser::RecordCallBack, this, std::placeholders::_1));
    TS_LOGD("process record completed");
    TS_LOGI("load perf data done");
    return true;
}

void PerfDataParser::UpdateEventConfigInfo()
{
    auto features = recordDataReader_->GetFeatures();
    cpuOffMode_ = find(features.begin(), features.end(), FEATURE::HIPERF_CPU_OFF) != features.end();
    if (cpuOffMode_) {
        TS_LOGD("this is cpuOffMode ");
    }
    const PerfFileSection* featureSection = recordDataReader_->GetFeatureSection(FEATURE::EVENT_DESC);
    if (featureSection) {
        TS_LOGI("have EVENT_DESC");
        LoadEventDesc();
    } else {
        TS_LOGE("Do not have EVENT_DESC !!!");
    }
}

void PerfDataParser::LoadEventDesc()
{
    const auto featureSection = recordDataReader_->GetFeatureSection(FEATURE::EVENT_DESC);
    const auto& sectionEventdesc = *static_cast<const PerfFileSectionEventDesc*>(featureSection);
    TS_LOGI("Event descriptions: %zu", sectionEventdesc.eventDesces_.size());
    for (size_t i = 0; i < sectionEventdesc.eventDesces_.size(); i++) {
        const auto& fileAttr = sectionEventdesc.eventDesces_[i];
        TS_LOGI("event name[%zu]: %s ids: %s", i, fileAttr.name.c_str(), VectorToString(fileAttr.ids).c_str());
        for (uint64_t id : fileAttr.ids) {
            report_.configIdIndexMaps_[id] = report_.configs_.size(); // setup index
            TS_LOGI("add config id map %" PRIu64 " to %zu", id, report_.configs_.size());
        }
        // when cpuOffMode_ , don't use count mode , use time mode.
        auto& config = report_.configs_.emplace_back(fileAttr.name, fileAttr.attr.type, fileAttr.attr.config,
                                                     cpuOffMode_ ? false : true);
        config.ids_ = fileAttr.ids;
        TS_ASSERT(config.ids_.size() > 0);

        auto perfReportData = traceDataCache_->GetPerfReportData();
        auto configValueIndex = traceDataCache_->dataDict_.GetStringIndex(fileAttr.name.c_str());
        perfReportData->AppendNewPerfReport(configNameIndex_, configValueIndex);
    }
}

void PerfDataParser::UpdateReportWorkloadInfo()
{
    // workload
    auto featureSection = recordDataReader_->GetFeatureSection(FEATURE::HIPERF_WORKLOAD_CMD);
    std::string workloader = "";
    if (featureSection) {
        TS_LOGI("found HIPERF_META_WORKLOAD_CMD");
        auto sectionString = static_cast<const PerfFileSectionString*>(featureSection);
        workloader = sectionString->toString();
    } else {
        TS_LOGW("NOT found HIPERF_META_WORKLOAD_CMD");
    }
    if (workloader.empty()) {
        TS_LOGW("NOT found HIPERF_META_WORKLOAD_CMD");
        return;
    }
    auto perfReportData = traceDataCache_->GetPerfReportData();
    auto workloaderValueIndex = traceDataCache_->dataDict_.GetStringIndex(workloader.c_str());
    perfReportData->AppendNewPerfReport(workloaderIndex_, workloaderValueIndex);
}

void PerfDataParser::UpdateCmdlineInfo()
{
    auto cmdline = recordDataReader_->GetFeatureString(FEATURE::CMDLINE);
    auto perfReportData = traceDataCache_->GetPerfReportData();
    auto cmdlineValueIndex = traceDataCache_->dataDict_.GetStringIndex(cmdline.c_str());
    perfReportData->AppendNewPerfReport(cmdlineIndex_, cmdlineValueIndex);
}

void PerfDataParser::UpdateSymbolAndFilesData()
{
    // we need unwind it (for function name match) even not give us path
    report_.virtualRuntime_.SetDisableUnwind(false);

    // found symbols in file
    const auto featureSection = recordDataReader_->GetFeatureSection(FEATURE::HIPERF_FILES_SYMBOL);
    if (featureSection != nullptr) {
        const PerfFileSectionSymbolsFiles* sectionSymbolsFiles =
            static_cast<const PerfFileSectionSymbolsFiles*>(featureSection);
        report_.virtualRuntime_.UpdateFromPerfData(sectionSymbolsFiles->symbolFileStructs_);
    }
    uint64_t fileId = 0;
    for (auto& symbolsFile : report_.virtualRuntime_.GetSymbolsFiles()) {
        auto filePathIndex = traceDataCache_->dataDict_.GetStringIndex(symbolsFile->filePath_.c_str());
        uint32_t serial = 0;
        for (auto& symbol : symbolsFile->GetSymbols()) {
            auto symbolIndex = traceDataCache_->dataDict_.GetStringIndex(symbol.Name().data());
            streamFilters_->statFilter_->IncreaseStat(TRACE_PERF, STAT_EVENT_RECEIVED);
            streamFilters_->perfDataFilter_->AppendPerfFiles(fileId, serial++, symbolIndex, filePathIndex);
        }
        ++fileId;
    }
}
void PerfDataParser::UpdateClockType()
{
    const auto& attrIds_ = recordDataReader_->GetAttrSection();
    if (attrIds_.size() > 0) {
        useClockId_ = attrIds_[0].attr.use_clockid;
        clockId_ = attrIds_[0].attr.clockid;
        TS_LOGE("useClockId_ = %u, clockId_ = %u", useClockId_, clockId_);
    }
}
bool PerfDataParser::RecordCallBack(std::unique_ptr<PerfEventRecord> record)
{
    // tell process tree what happend for rebuild symbols
    report_.virtualRuntime_.UpdateFromRecord(*record);

    if (record->GetType() == PERF_RECORD_SAMPLE) {
        std::unique_ptr<PerfRecordSample> sample(static_cast<PerfRecordSample*>(record.release()));
        ProcessSample(sample);
    } else if (record->GetType() == PERF_RECORD_COMM) {
        auto recordComm = static_cast<PerfRecordComm*>(record.get());
        auto range = tidToPid_.equal_range(recordComm->data_.tid);
        for (auto it = range.first; it != range.second; it++) {
            if (it->second == recordComm->data_.pid) {
                return true;
            }
        }
        tidToPid_.insert(std::make_pair(recordComm->data_.tid, recordComm->data_.pid));
        auto perfThreadData = traceDataCache_->GetPerfThreadData();
        auto threadNameIndex = traceDataCache_->dataDict_.GetStringIndex(recordComm->data_.comm);
        perfThreadData->AppendNewPerfThread(recordComm->data_.pid, recordComm->data_.tid, threadNameIndex);
    }
    return true;
}

void PerfDataParser::ProcessSample(std::unique_ptr<PerfRecordSample>& sample)
{
    uint64_t callChainId = 0;
    for (const CallFrame& frame : sample->callFrames_) {
        auto fileId = 0;
        for (auto fileIt = report_.virtualRuntime_.GetSymbolsFiles().begin();
             fileIt != report_.virtualRuntime_.GetSymbolsFiles().end(); fileIt++) {
            if (fileIt->get()->filePath_ == frame.filePath_) {
                fileId = fileIt - report_.virtualRuntime_.GetSymbolsFiles().begin();
                break;
            }
        }
        auto symbolId = frame.symbolIndex_;
        streamFilters_->perfDataFilter_->AppendPerfCallChain(sampleId_, callChainId, frame.vaddrInFile_, fileId,
                                                             symbolId);
        callChainId++;
    }
    auto perfSampleData = traceDataCache_->GetPerfSampleData();
    uint64_t newTimeStamp = 0;
    if (useClockId_ == 0) {
        newTimeStamp = sample->data_.time;
    } else {
        newTimeStamp =
            streamFilters_->clockFilter_->ToPrimaryTraceTime(perfToTSClockType_.at(clockId_), sample->data_.time);
    }
    UpdatePluginTimeRange(perfToTSClockType_.at(clockId_), sample->data_.time, newTimeStamp);

    DataIndex threadStatIndex = unkonwnStateIndex_;
    auto threadState = report_.GetConfigName(sample->data_.id);
    if (threadState.compare(wakingEventName_) == 0) {
        threadStatIndex = runingStateIndex_;
    } else if (threadState.compare(cpuOffEventName_) == 0) {
        threadStatIndex = suspendStatIndex_;
    }
    auto configIndex = report_.GetConfigIndex(sample->data_.id);
    perfSampleData->AppendNewPerfSample(sampleId_, sample->data_.time, sample->data_.tid, sample->data_.period,
                                        configIndex, newTimeStamp, sample->data_.cpu, threadStatIndex);
    sampleId_++;
}

void PerfDataParser::Finish()
{
    streamFilters_->perfDataFilter_->Finish();
    traceDataCache_->MixTraceTime(GetPluginStartTime(), GetPluginEndTime());
}
} // namespace TraceStreamer
} // namespace SysTuning
