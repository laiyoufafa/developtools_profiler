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
#include <memory>
#include "htrace_js_memory_parser.h"
#include "clock_filter.h"
#include "htrace_event_parser.h"
#include "process_filter.h"
#include "stat_filter.h"
namespace SysTuning {
namespace TraceStreamer {
namespace jsonns {
const int OFFSET_FIRST = 1;
const int OFFSET_SECOND = 2;
const int OFFSET_THIRD = 3;
const int OFFSET_FOURTH = 4;
const int OFFSET_FIFTH = 5;
const int OFFSET_SIXTH = 6;
struct Meta {
    std::vector<std::string> nodeFields;
    std::vector<std::vector<std::string>> nodeTypes;
    std::vector<std::string> edgeFields;
    std::vector<std::vector<std::string>> edgeTypes;
    std::vector<std::string> traceFunctionInfoFields;
    std::vector<std::string> traceNodeFields;
    std::vector<std::string> sampleFields;
    std::vector<std::string> locationFields;
};
struct Snapshot {
    Meta meta;
    int nodeCount;
    int edgeCount;
    int traceFunctionCount;
};
void from_json(const json& j, Meta& v)
{
    for (int i = 0; i < j["node_fields"].size(); i++) {
        v.nodeFields.emplace_back(j["node_fields"][i]);
    }
    for (int i = 0; i < j["node_types"].size(); i++) {
        std::vector<std::string> nodeTypes;
        if (j["node_types"][i].is_array()) {
            for (int m = 0; m < j["node_types"][i].size(); m++) {
                nodeTypes.emplace_back(j["node_types"][i][m]);
            }
            v.nodeTypes.emplace_back(nodeTypes);
        } else {
            nodeTypes.emplace_back(j["node_types"][i]);
            v.nodeTypes.emplace_back(nodeTypes);
        }
    }
    for (int i = 0; i < j["edge_fields"].size(); i++) {
        v.edgeFields.emplace_back(j["edge_fields"][i]);
    }
    for (int i = 0; i < j["edge_types"].size(); i++) {
        std::vector<std::string> edgeTypes;
        if (j["edge_types"][i].is_array()) {
            for (int m = 0; m < j["edge_types"][i].size(); m++) {
                edgeTypes.emplace_back(j["edge_types"][i][m]);
            }
            v.edgeTypes.emplace_back(edgeTypes);
        } else {
            edgeTypes.emplace_back(j["edge_types"][i]);
            v.edgeTypes.emplace_back(edgeTypes);
        }
    }
    for (int i = 0; i < j["trace_function_info_fields"].size(); i++) {
        v.traceFunctionInfoFields.emplace_back(j["trace_function_info_fields"][i]);
    }
    for (int i = 0; i < j["trace_node_fields"].size(); i++) {
        v.traceNodeFields.emplace_back(j["trace_node_fields"][i]);
    }
    for (int i = 0; i < j["sample_fields"].size(); i++) {
        v.sampleFields.emplace_back(j["sample_fields"][i]);
    }
    for (int i = 0; i < j["location_fields"].size(); i++) {
        v.locationFields.emplace_back(j["location_fields"][i]);
    }
    return;
}

void from_json(const json& j, Snapshot& v)
{
    j.at("meta").get_to(v.meta);
    j.at("node_count").get_to(v.nodeCount);
    j.at("edge_count").get_to(v.edgeCount);
    j.at("trace_function_count").get_to(v.traceFunctionCount);
    return;
}

struct Nodes {
    std::vector<uint32_t> types;
    std::vector<uint32_t> names;
    std::vector<uint32_t> ids;
    std::vector<uint32_t> selfSizes;
    std::vector<uint32_t> edgeCounts;
    std::vector<uint32_t> traceNodeIds;
    std::vector<uint32_t> detachedness;
};
const int NODES_SINGLE_LENGTH = 7;
std::vector<uint32_t> g_fromNodeIds;
std::vector<uint32_t> g_ids;
void from_json(const json& j, Nodes& v)
{
    int edgeIndex = 0;
    for (int i = 0; i < j.size() / NODES_SINGLE_LENGTH; i++) {
        v.types.emplace_back(j[i * NODES_SINGLE_LENGTH]);
        v.names.emplace_back(j[i * NODES_SINGLE_LENGTH + OFFSET_FIRST]);
        v.ids.emplace_back(j[i * NODES_SINGLE_LENGTH + OFFSET_SECOND]);
        v.selfSizes.emplace_back(j[i * NODES_SINGLE_LENGTH + OFFSET_THIRD]);
        v.edgeCounts.emplace_back(j[i * NODES_SINGLE_LENGTH + OFFSET_FOURTH]);
        for (int m = edgeIndex; m < edgeIndex + v.edgeCounts.at(i); m++) {
            g_fromNodeIds.emplace_back(j[i * NODES_SINGLE_LENGTH + OFFSET_SECOND]);
        }
        edgeIndex += v.edgeCounts.at(i);
        v.traceNodeIds.emplace_back(j[i * NODES_SINGLE_LENGTH + OFFSET_FIFTH]);
        v.detachedness.emplace_back(j[i * NODES_SINGLE_LENGTH + OFFSET_SIXTH]);
    }
    g_ids = v.ids;
}

struct Edges {
    std::vector<uint32_t> types;
    std::vector<uint32_t> nameOrIndexes;
    std::vector<uint32_t> toNodes;
    std::vector<uint32_t> fromNodeIds;
    std::vector<uint32_t> toNodeIds;
};
const int EDGES_SINGLE_LENGTH = 3;
void from_json(const json& j, Edges& v)
{
    v.fromNodeIds = g_fromNodeIds;
    for (int i = 0; i < j.size() / EDGES_SINGLE_LENGTH; i++) {
        v.types.emplace_back(j[i * EDGES_SINGLE_LENGTH]);
        v.nameOrIndexes.emplace_back(j[i * EDGES_SINGLE_LENGTH + OFFSET_FIRST]);
        v.toNodes.emplace_back(j[i * EDGES_SINGLE_LENGTH + OFFSET_SECOND]);
        v.toNodeIds.emplace_back(g_ids[v.toNodes[i] + OFFSET_SECOND]);
    }
    return;
}

struct Location {
    std::vector<uint32_t> objectIndexes;
    std::vector<uint32_t> scriptIds;
    std::vector<uint32_t> lines;
    std::vector<uint32_t> columns;
};
const int LOCATION_SINGLE_LENGTH = 4;
void from_json(const json& j, Location& v)
{
    for (int i = 0; i < j.size() / LOCATION_SINGLE_LENGTH; i++) {
        v.objectIndexes.emplace_back(j[i * LOCATION_SINGLE_LENGTH]);
        v.scriptIds.emplace_back(j[i * LOCATION_SINGLE_LENGTH + OFFSET_FIRST]);
        v.lines.emplace_back(j[i * LOCATION_SINGLE_LENGTH + OFFSET_SECOND]);
        v.columns.emplace_back(j[i * LOCATION_SINGLE_LENGTH + OFFSET_THIRD]);
    }
}

struct Sample {
    std::vector<uint32_t> timestampUs;
    std::vector<uint32_t> lastAssignedIds;
};
const int SAMPLE_SINGLE_LENGTH = 2;
void from_json(const json& j, Sample& v)
{
    v.lastAssignedIds = g_ids;
    for (int i = 0; i < j.size() / SAMPLE_SINGLE_LENGTH; i++) {
        v.timestampUs.emplace_back(j[i * SAMPLE_SINGLE_LENGTH]);
    }
}

struct Strings {
    std::vector<std::string> strings;
};
void from_json(const json& j, Strings& v)
{
    for (int i = 0; i < j.size(); i++) {
        v.strings.emplace_back(j[i]);
    }
}

struct TraceFuncInfo {
    std::vector<uint32_t> functionIds;
    std::vector<uint32_t> names;
    std::vector<uint32_t> scriptNames;
    std::vector<uint32_t> scriptIds;
    std::vector<uint32_t> lines;
    std::vector<uint32_t> columns;
};
const int TRACE_FUNC_INFO_SINGLE_LENGTH = 6;
void from_json(const json& j, TraceFuncInfo& v)
{
    for (int i = 0; i < j.size() / TRACE_FUNC_INFO_SINGLE_LENGTH; i++) {
        v.functionIds.emplace_back(j[i * TRACE_FUNC_INFO_SINGLE_LENGTH]);
        v.names.emplace_back(j[i * TRACE_FUNC_INFO_SINGLE_LENGTH + OFFSET_FIRST]);
        v.scriptNames.emplace_back(j[i * TRACE_FUNC_INFO_SINGLE_LENGTH + OFFSET_SECOND]);
        v.scriptIds.emplace_back(j[i * TRACE_FUNC_INFO_SINGLE_LENGTH + OFFSET_THIRD]);
        v.lines.emplace_back(j[i * TRACE_FUNC_INFO_SINGLE_LENGTH + OFFSET_FOURTH]);
        v.columns.emplace_back(j[i * TRACE_FUNC_INFO_SINGLE_LENGTH + OFFSET_FIFTH]);
    }
}

struct TraceTree {
    std::vector<uint32_t> ids;
    std::vector<uint32_t> functionInfoIndexes;
    std::vector<uint32_t> counts;
    std::vector<uint32_t> sizes;
    std::vector<uint32_t> parentIds;
};

struct ParentFunc {
public:
    uint32_t id;
    uint32_t functionInfoIndex;
    uint32_t count;
    uint32_t size;
    std::vector<ParentFunc> children;
    ParentFunc* parent = nullptr;
};

class TraceParser {
public:
    void parse_trace_node(const json& array, std::vector<ParentFunc>& funcList, ParentFunc* parent = nullptr)
    {
        int singleLength = 5;
        int functionCount = array.size() / singleLength;
        for (int i = 0; i < functionCount; ++i) {
            ParentFunc item;
            if (parent != nullptr) {
                item.parent = parent;
                parent->children.push_back(std::move(funcList.back()));
            }
            item.id = array[i * singleLength];
            item.functionInfoIndex = array[i * singleLength + OFFSET_FIRST];
            item.count = array[i * singleLength + OFFSET_SECOND];
            item.size = array[i * singleLength + OFFSET_THIRD];
            json childrenArray;
            childrenArray.emplace_back(array.begin() + i * singleLength + OFFSET_FOURTH,
                                       array.begin() + (i + OFFSET_FIRST) * singleLength);
            funcList.emplace_back(item);
            if (!childrenArray.empty()) {
                parse_trace_node(childrenArray, funcList, &(funcList.back()));
            }
        }
    }
};
void from_json(const json& j, TraceTree& v)
{
    std::vector<ParentFunc> funcList;
    TraceParser parser;
    parser.parse_trace_node(j, funcList);
    for (auto& func : funcList) {
        v.ids.emplace_back(func.id);
        v.functionInfoIndexes.emplace_back(func.functionInfoIndex);
        v.counts.emplace_back(func.count);
        v.sizes.emplace_back(func.size);
        v.parentIds.emplace_back(func.parent ? func.parent->id : std::numeric_limits<uint32_t>::max());
    }
}
} // namespace jsonns

HtraceJSMemoryParser::HtraceJSMemoryParser(TraceDataCache* dataCache, const TraceStreamerFilters* ctx)
    : HtracePluginTimeParser(dataCache, ctx)
{
}

HtraceJSMemoryParser::~HtraceJSMemoryParser()
{
    TS_LOGI("JS memory ts MIN:%llu, MAX:%llu", static_cast<unsigned long long>(GetPluginStartTime()),
            static_cast<unsigned long long>(GetPluginEndTime()));
}

void HtraceJSMemoryParser::ParseJSMemoryConfig(ProtoReader::BytesView tracePacket)
{
    ProtoReader::JsHeapConfig_Reader jsHeapConfig(tracePacket.data_, tracePacket.size_);
    type_ = jsHeapConfig.type();
    pid_ = jsHeapConfig.pid();
}

void HtraceJSMemoryParser::Parse(ProtoReader::BytesView tracePacket, uint64_t ts)
{
    ProtoReader::JsHeapResult_Reader jsHeapResult(tracePacket.data_, tracePacket.size_);
    auto result = jsHeapResult.result().ToStdString();
    std::string fileName = "";
    if (result == snapshotEnd_ || result == timeLineEnd_) {
        if (type_ == ProtoReader::JsHeapConfig_HeapType::JsHeapConfig_HeapType_SNAPSHOT) {
            fileName = "Snapshot" + std::to_string(fileId_);
            ParseSnapshot(fileId_, jsMemoryString_);
            jsMemoryString_ = "";
        } else if (type_ == ProtoReader::JsHeapConfig_HeapType::JsHeapConfig_HeapType_TIMELINE) {
            if (result == snapshotEnd_) {
                ts = streamFilters_->clockFilter_->ToPrimaryTraceTime(TS_CLOCK_REALTIME, ts);
                UpdatePluginTimeRange(TS_CLOCK_REALTIME, ts, ts);
                startTime_ = ts;
                return;
            }
            fileName = "Timeline";
            ParseTimeLine(fileId_, jsMemoryString_);
            jsMemoryString_ = "";
        }
        ts = streamFilters_->clockFilter_->ToPrimaryTraceTime(TS_CLOCK_REALTIME, ts);
        UpdatePluginTimeRange(TS_CLOCK_REALTIME, ts, ts);
        (void)traceDataCache_->GetJsHeapFilesData()->AppendNewData(fileId_, fileName, startTime_, ts, pid_);
        fileId_++;
        isFirst_ = true;
        return;
    }
    json jMessage = json::parse(result);
    if (jMessage.is_discarded()) {
        streamFilters_->statFilter_->IncreaseStat(TRACE_JS_MEMORY, STAT_EVENT_DATA_INVALID);
        TS_LOGE("json::parse error!\n");
        return;
    }

    if (jMessage["params"]["chunk"].is_string()) {
        if (isFirst_ && type_ == ProtoReader::JsHeapConfig_HeapType::JsHeapConfig_HeapType_SNAPSHOT) {
            ts = streamFilters_->clockFilter_->ToPrimaryTraceTime(TS_CLOCK_REALTIME, ts);
            UpdatePluginTimeRange(TS_CLOCK_REALTIME, ts, ts);
            startTime_ = ts;
            isFirst_ = false;
        }
        jsMemoryString_ += jMessage["params"]["chunk"];
    }
}

void HtraceJSMemoryParser::ParseTimeLine(int fileId, const std::string& jsonString)
{
    json jMessage = json::parse(jsonString);
    ParserJSSnapInfo(fileId, jMessage);
    ParseNodes(fileId, jMessage);
    ParseEdges(fileId, jMessage);
    ParseLocation(fileId, jMessage);
    ParseSample(fileId, jMessage);
    ParseString(fileId, jMessage);
    ParseTraceFuncInfo(fileId, jMessage);
    ParseTraceNode(fileId, jMessage);
    streamFilters_->statFilter_->IncreaseStat(TRACE_JS_MEMORY, STAT_EVENT_RECEIVED);
    return;
}
void HtraceJSMemoryParser::ParserSnapInfo(int fileId,
                                          const std::string& key,
                                          const std::vector<std::vector<std::string>>& types)
{
    for (int m = 0; m < types[0].size(); ++m) {
        (void)traceDataCache_->GetJsHeapInfoData()->AppendNewData(fileId, key, 0, std::numeric_limits<uint32_t>::max(),
                                                                  types[0][m]);
    }
    for (int i = 1; i < types.size(); ++i) {
        (void)traceDataCache_->GetJsHeapInfoData()->AppendNewData(fileId, key, 1, std::numeric_limits<uint32_t>::max(),
                                                                  types[i][0]);
    }
    return;
}

const std::string NODE_TYPES = "node_types";
const std::string EDGE_TYPES = "edge_types";
void HtraceJSMemoryParser::ParserJSSnapInfo(int fileId, const json& jMessage)
{
    jsonns::Snapshot snapshot = jMessage.at("snapshot");
    ParserSnapInfo(fileId, NODE_TYPES, snapshot.meta.nodeTypes);
    ParserSnapInfo(fileId, EDGE_TYPES, snapshot.meta.edgeTypes);
    auto nodeCount = snapshot.nodeCount;
    auto edgeCount = snapshot.edgeCount;
    auto traceFuncCount = snapshot.traceFunctionCount;
    (void)traceDataCache_->GetJsHeapInfoData()->AppendNewData(fileId, "node_count", 0, nodeCount, "");
    (void)traceDataCache_->GetJsHeapInfoData()->AppendNewData(fileId, "edge_count", 0, edgeCount, "");
    (void)traceDataCache_->GetJsHeapInfoData()->AppendNewData(fileId, "trace_function_count", 0, traceFuncCount, "");
    return;
}

void HtraceJSMemoryParser::ParseNodes(int fileId, const json& jMessage)
{
    jsonns::Nodes node = jMessage.at("nodes");
    for (int i = 0; i < node.names.size(); ++i) {
        auto type = node.types[i];
        auto name = node.names[i];
        auto id = node.ids[i];
        auto selfSize = node.selfSizes[i];
        auto edgeCount = node.edgeCounts[i];
        auto traceNodeId = node.traceNodeIds[i];
        auto detachedness = node.detachedness[i];
        (void)traceDataCache_->GetJsHeapNodesData()->AppendNewData(fileId, i, type, name, id, selfSize, edgeCount,
                                                                   traceNodeId, detachedness);
    }
    return;
}

void HtraceJSMemoryParser::ParseEdges(int fileId, const json& jMessage)
{
    jsonns::Edges edge = jMessage.at("edges");
    for (int i = 0; i < edge.types.size(); ++i) {
        auto type = edge.types[i];
        auto nameOrIndex = edge.nameOrIndexes[i];
        auto toNode = edge.toNodes[i];
        auto fromNodeId = edge.fromNodeIds[i];
        auto toNodeid = edge.toNodeIds[i];
        (void)traceDataCache_->GetJsHeapEdgesData()->AppendNewData(fileId, i, type, nameOrIndex, toNode, fromNodeId,
                                                                   toNodeid);
    }
    return;
}

void HtraceJSMemoryParser::ParseLocation(int fileId, const json& jMessage)
{
    jsonns::Location location = jMessage.at("locations");
    for (int i = 0; i < location.columns.size(); ++i) {
        auto objectIndex = location.objectIndexes[i];
        auto scriptId = location.scriptIds[i];
        auto line = location.lines[i];
        auto column = location.columns[i];
        (void)traceDataCache_->GetJsHeapLocationData()->AppendNewData(fileId, objectIndex, scriptId, line, column);
    }
    return;
}
void HtraceJSMemoryParser::ParseSample(int fileId, const json& jMessage)
{
    jsonns::Sample sample = jMessage.at("samples");
    for (int i = 0; i < sample.timestampUs.size(); ++i) {
        auto timestampUs = sample.timestampUs[i];
        auto lastAssignedId = sample.lastAssignedIds[i];
        (void)traceDataCache_->GetJsHeapSampleData()->AppendNewData(fileId, timestampUs, lastAssignedId);
    }
    return;
}
void HtraceJSMemoryParser::ParseString(int fileId, const json& jMessage)
{
    jsonns::Strings string = jMessage.at("strings");
    for (int i = 0; i < string.strings.size(); ++i) {
        (void)traceDataCache_->GetJsHeapStringData()->AppendNewData(fileId, i, string.strings[i]);
    }
    return;
}
void HtraceJSMemoryParser::ParseTraceFuncInfo(int fileId, const json& jMessage)
{
    jsonns::TraceFuncInfo traceFuncInfo = jMessage.at("trace_function_infos");
    for (int i = 0; i < traceFuncInfo.functionIds.size(); ++i) {
        auto functionId = traceFuncInfo.functionIds[i];
        auto name = traceFuncInfo.names[i];
        auto scriptName = traceFuncInfo.scriptNames[i];
        auto scriptId = traceFuncInfo.scriptIds[i];
        auto line = traceFuncInfo.lines[i];
        auto column = traceFuncInfo.columns[i];
        (void)traceDataCache_->GetJsHeapTraceFuncInfoData()->AppendNewData(fileId, i, functionId, name, scriptName,
                                                                           scriptId, line, column);
    }
    return;
}
void HtraceJSMemoryParser::ParseTraceNode(int fileId, const json& jMessage)
{
    jsonns::TraceTree traceTree = jMessage.at("trace_tree");
    for (int i = 0; i < traceTree.ids.size(); ++i) {
        auto id = traceTree.ids[i];
        auto funcInfoIndex = traceTree.functionInfoIndexes[i];
        auto count = traceTree.counts[i];
        auto size = traceTree.sizes[i];
        auto parentId = traceTree.parentIds[i];
        (void)traceDataCache_->GetJsHeapTraceNodeData()->AppendNewData(fileId, id, funcInfoIndex, count, size,
                                                                       parentId);
    }
    return;
}
void HtraceJSMemoryParser::ParseSnapshot(int fileId, const std::string& jsonString)
{
    json jMessage = json::parse(jsonString);
    ParserJSSnapInfo(fileId, jMessage);
    ParseNodes(fileId, jMessage);
    ParseEdges(fileId, jMessage);
    ParseLocation(fileId, jMessage);
    ParseSample(fileId, jMessage);
    ParseString(fileId, jMessage);
    ParseTraceFuncInfo(fileId, jMessage);
    ParseTraceNode(fileId, jMessage);
    streamFilters_->statFilter_->IncreaseStat(TRACE_JS_MEMORY, STAT_EVENT_RECEIVED);
    return;
}
void HtraceJSMemoryParser::Finish()
{
    traceDataCache_->MixTraceTime(GetPluginStartTime(), GetPluginEndTime());
    return;
}
} // namespace TraceStreamer
} // namespace SysTuning
