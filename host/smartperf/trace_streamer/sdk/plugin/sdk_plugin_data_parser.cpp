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
#include "sdk_plugin_data_parser.h"
#include <string>
#include "sdk/ts_sdk_api.h"

namespace SysTuning {
namespace TraceStreamer {
extern "C" {
void sdk_plugin_init_table_name()
{
    SDK_SetTableName("counter_table",
                     "gpu_counter_object",
                     "slice_table",
                     "slice_object_table");
}
int sdk_plugin_data_parser(const uint8_t* data, int len)
{
    std::unique_ptr<uint8_t[]> buf = std::make_unique<uint8_t[]>(len);
    std::copy(data, data + len, buf.get());
    MockDataArr mockDataArr;
    mockDataArr.ParseFromArray(buf.get(), len);
    int size = mockDataArr.mockdata_size();
    if (size > 1) {
        for (auto m = 0; m < size; m++) {
            auto mockData = mockDataArr.mockdata().at(m);
            sdk_plugin_parser(data, len, mockData);
        }
    } else {
        MockData mockData;
        mockData.ParseFromArray(buf.get(), len);
        sdk_plugin_parser(data, len, mockData);
    }
    return 0;
}

int sdk_plugin_parser(const uint8_t* data, int len, MockData mockData)
{
    // 解析counterObject
    int counterId = 0;
    std::string counterName = 0;
    for (auto i = 0; i < mockData.counterobj_size(); i++) {
        counterId = mockData.counterobj(i).id();
        counterName = mockData.counterobj(i).name();
        SDK_AppendCounterObject(counterId, counterName.c_str());
    }

    // 解析counterInfo
    int counterKey = 0;
    int value = 0;
    uint64_t ts = 0;
    for (auto i = 0; i < mockData.counterinfo_size(); i++) {
        CounterInfo counterInfo;
        counterInfo = mockData.counterinfo(i);
        SDK_AppendCounter(counterInfo.key(), counterInfo.ts(), counterInfo.value());
    }

    // 解析SliceObj
    int sliceId = 0;
    std::string sliceName = 0;
    for (auto i = 0; i < mockData.sliceobj_size(); i++) {
        sliceId = mockData.sliceobj(i).id();
        sliceName = mockData.sliceobj(i).name();
        SDK_AppendSliceObject(sliceId, sliceName.c_str());
    }

    // 解析SliceInfo
    int sliceKey = 0;
    int sliceValue = 0;
    uint64_t startTime = 0;
    uint64_t endTime = 0;
    for (auto i = 0; i < mockData.sliceinfo_size(); i++) {
        sliceKey = mockData.sliceinfo(i).id();
        sliceValue = mockData.sliceinfo(i).value();
        startTime = mockData.sliceinfo(i).start_time();
        endTime = mockData.sliceinfo(i).end_time();
        SDK_AppendSlice(sliceKey, startTime, endTime, sliceValue);
    }
    return 0;
}
}
} // namespace TraceStreamer
} // namespace SysTuning
