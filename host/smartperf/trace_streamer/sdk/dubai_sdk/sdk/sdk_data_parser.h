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

#ifndef SDK_DATA_PARSER_H
#define SDK_DATA_PARSER_H

#include <functional>
#include <mutex>
#include "file.h"
#include "../parser/htrace_plugin_time_parser.h"
#include "../table/table_base.h"
#include "../trace_streamer/trace_streamer_selector.h"

namespace SysTuning {
namespace TraceStreamer {

enum Third_Party_Wasm_Id {
    DATA_TYPE_MOCK_PLUGIN = 0,
    DATA_TYPE_CLOCK = 100,
};
class SDKDataParser : public HtracePluginTimeParser {
public:
    using TraceRangeCallbackFunction = std::function<void(const std::string)>;
    using QueryResultCallbackFunction = std::function<void(const std::string /* result */, int, int)>;
    SDKDataParser(TraceDataCache* dataCache, const TraceStreamerFilters* ctx);
    ~SDKDataParser(){};

    // third_party
    int CreateTableByJson();
    int SetTableName(const char* counterTableName,
                     const char* counterObjectTableName,
                     const char* sliceTableName,
                     const char* sliceObjectName);
    int GetJsonConfig(QueryResultCallbackFunction queryResultCallbackFunction);
    int GetPluginName(std::string pluginName);
    int GetPluginName(const uint8_t* data, int len);
    int ParseDataOver(TraceRangeCallbackFunction traceRangeCallbackFunction);
    int ParserData(const uint8_t* data, int len, int componentId);
    int AppendCounterObject(int counterId, const char* columnName);
    int AppendCounter(int counterId, uint64_t ts, double value);
    int AppendSliceObject(int sliceId, const char* columnName);
    int AppendSlice(int sliceId, uint64_t ts, uint64_t endTs, std::string start_time, std::string end_time, double value);

private:
    int CreateCounterObjectTable(const std::string& tableName);
    int CreateCounterTable(const std::string& tableName);
    int CreateSliceObjectTable(const std::string& tableName);
    int CreateSliceTable(const std::string& tableName);
    int ParserClock(const uint8_t* data, int len);
    int UpdateJson();

public:
    std::string counterTableName_ = "counter_table";
    std::string counterObjectTableName_ = "gpu_counter_object";
    std::string sliceTableName_ = "slice_table";
    std::string sliceObjectName_ = "slice_object_table";
    std::string jsonConfig_ =
        "{\"tableConfig\":{\"showType\":[{\"tableName\":\"counter_table\",\"inner\":{\"tableName\":\"gpu_counter_"
        "object\","
        "\"columns\":[{\"column\":\"counter_name\",\"type\":\"STRING\",\"displayName\":\"\",\"showType\":[0]},{"
        "\"column\":"
        "\"counter_id\",\"type\":\"INTEGER\",\"displayName\":\"\",\"showType\":[0]}]},\"columns\":[{\"column\":\"ts\","
        "\"type\":\"INTEGER\",\"displayName\":\"TimeStamp\",\"showType\":[1,3]},{\"column\":\"counter_id\",\"type\":"
        "\"INTEGER\",\"displayName\":\"MonitorValue\",\"showType\":[1,3]},{\"column\":\"value\",\"type\":\"INTEGER\","
        "\"displayName\":\"Value\",\"showType\":[1,3]}]},{\"tableName\":\"slice_table\",\"inner\":{\"tableName\":"
        "\"slice_"
        "object_table\",\"columns\":[{\"column\":\"slice_name\",\"type\":\"STRING\",\"displayName\":\"\",\"showType\":["
        "0]},"
        "{\"column\":\"slice_id\",\"type\":\"INTEGER\",\"displayName\":\"\",\"showType\":[0]}]},\"columns\":[{"
        "\"column\":"
        "\"start_ts\",\"type\":\"INTEGER\",\"displayName\":\"startts\",\"showType\":[2,3]},{\"column\":\"end_ts\","
        "\"type\":"
        "\"INTEGER\",\"displayName\":\"endts\",\"showType\":[2,3]},{\"column\":\"start\",\"type\":\"STRING\","
        "\"displayName\":\"start_time\",\"showType\":[2,3]},{\"column\":\"end\",\"type\":\"STRING\","
        "\"displayName\":\"end_time\",\"showType\":[2,3]},{\"column\":\"slice_id\",\"type\":\"INTEGER\","
        "\"displayName\":\"slice_id\",\"showType\":[2,3]},{\"column\":\"value\",\"type\":\"INTEGER\",\"displayName\":"
        "\"Value\",\"showType\":[2,3]}]}]},\"settingConfig\":{\"name\":\"mailG77\",\"configuration\":{\"version\":{"
        "\"type\":\"number\",\"default\":\"1\",\"description\":\"gatordversion\"},\"counters\":{\"type\":\"string\","
        "\"enum\":[\"ARM_Mali-TTRx_JS1_ACTIVE\",\"ARM_Mali-TTRx_JS0_ACTIVE\",\"ARM_Mali-TTRx_GPU_ACTIVE\",\"ARM_Mali-"
        "TTRx_FRAG_ACTIVE\"]},\"stop_gator\":{\"type\":\"boolean\",\"default\":\"true\",\"description\":\"stop_gator\"}"
        "}}}";
};
} // namespace TraceStreamer
} // namespace SysTuning
#endif // SDK_DATA_PARSER_H
