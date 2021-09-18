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

#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>

#include "filter_filter.h"
#include "measure_filter.h"
#include "process_filter.h"
#include "slice_filter.h"
#include "trace_streamer_selector.h"

using namespace testing::ext;
using namespace SysTuning::TraceStreamer;
namespace SysTuning {
namespace TraceStreamer {
class SliceFilterTest : public ::testing::Test {
public:
    void SetUp()
    {
        stream_.InitFilter();
    }

    void TearDown() {}
public:
    TraceStreamerSelector stream_;
};

HWTEST_F(SliceFilterTest, SliceTest1, TestSize.Level1)
{
    TS_LOGI("test1");
    DataIndex splitStrIndex = stream_.traceDataCache_->GetDataIndex("call_function_one");
    stream_.streamFilters_->sliceFilter_->BeginSlice(168758662957000, 2532, 2519, 0, splitStrIndex);
    stream_.streamFilters_->sliceFilter_->EndSlice(168758663011000, 2532, 2519);
    auto slices = stream_.traceDataCache_->GetInternalSlicesData();
    EXPECT_TRUE(slices->Size() == 1);
    EXPECT_TRUE(slices->DursData()[0] == 168758663011000 - 168758662957000);
}

HWTEST_F(SliceFilterTest, SliceTest2, TestSize.Level1)
{
    TS_LOGI("test2");
    DataIndex splitStrIndex = stream_.traceDataCache_->GetDataIndex("call_function_one");
    stream_.streamFilters_->sliceFilter_->BeginSlice(168758670506000, 1298, 1298, 0, splitStrIndex);
    splitStrIndex = stream_.traceDataCache_->GetDataIndex("call_function_two");
    stream_.streamFilters_->sliceFilter_->BeginSlice(168758670523000, 1298, 1298, 0, splitStrIndex);
    stream_.streamFilters_->sliceFilter_->EndSlice(168758670720000, 1298, 1298);
    stream_.streamFilters_->sliceFilter_->EndSlice(168758670732000, 1298, 1298);
    auto slices = stream_.traceDataCache_->GetInternalSlicesData();
    EXPECT_TRUE(slices->Size() == 2);
    EXPECT_TRUE(slices->DursData()[0] == 168758670732000 - 168758670506000);
    EXPECT_TRUE(slices->DursData()[1] == 168758670720000 - 168758670523000);
    EXPECT_TRUE(slices->Depths()[1] == 1);
}

HWTEST_F(SliceFilterTest, SliceTest3, TestSize.Level1)
{
    TS_LOGI("test3");
    DataIndex splitStrIndex = stream_.traceDataCache_->GetDataIndex("thread_one_call_function_one");
    stream_.streamFilters_->sliceFilter_->BeginSlice(168758663018000, 2532, 2519, 0, splitStrIndex); // slice 0
    splitStrIndex = stream_.traceDataCache_->GetDataIndex("thread_two_call_function_one");
    stream_.streamFilters_->sliceFilter_->BeginSlice(168758663028000, 2533, 2529, 0, splitStrIndex);
    splitStrIndex = stream_.traceDataCache_->GetDataIndex("thread_one_call_function_two");
    stream_.streamFilters_->sliceFilter_->BeginSlice(168758679303000, 2532, 2519, 0, splitStrIndex); // slice 2
    // end thread_one_call_function_two
    stream_.streamFilters_->sliceFilter_->EndSlice(168758682466000, 2532, 2519);
    // end thread_one_call_function_one
    stream_.streamFilters_->sliceFilter_->EndSlice(168758682476000, 2532, 2519);
    // end thread_two_call_function_one slice 1
    stream_.streamFilters_->sliceFilter_->EndSlice(168758689323000, 2533, 2529);
    auto slices = stream_.traceDataCache_->GetInternalSlicesData();
    EXPECT_TRUE(slices->Size() == 3);
    EXPECT_TRUE(slices->DursData()[0] == 168758682476000 - 168758663018000); // slice 0
    EXPECT_TRUE(slices->Depths()[0] == 0);
    EXPECT_TRUE(slices->DursData()[1] == 168758689323000 - 168758663028000); // slice 1
    EXPECT_TRUE(slices->Depths()[1] == 0);
    EXPECT_TRUE(slices->DursData()[2] == 168758682466000 - 168758679303000); // slice 2
    EXPECT_TRUE(slices->Depths()[2] == 1);
}

HWTEST_F(SliceFilterTest, SliceTest4, TestSize.Level1)
{
    TS_LOGI("test3");
    DataIndex splitStrIndex = stream_.traceDataCache_->GetDataIndex("thread_one_call_function_one");
    stream_.streamFilters_->sliceFilter_->BeginSlice(168758663018000, 2532, 2519, 0, splitStrIndex); // slice 0
    splitStrIndex = stream_.traceDataCache_->GetDataIndex("thread_two_call_function_one");
    stream_.streamFilters_->sliceFilter_->BeginSlice(168758663028000, 2532, 2519, 0, splitStrIndex);
    // end thread_one_call_function_three
    stream_.streamFilters_->sliceFilter_->EndSlice(168758682456000, 2532, 2519);
    // end thread_one_call_function_two
    stream_.streamFilters_->sliceFilter_->EndSlice(168758682466000, 2532, 2519);
    auto slices = stream_.traceDataCache_->GetInternalSlicesData();
    EXPECT_TRUE(slices->Size() == 2);
    EXPECT_TRUE(slices->DursData()[0] == 168758682466000 - 168758663018000); // slice 0
    EXPECT_TRUE(slices->Depths()[0] == 0);
    EXPECT_TRUE(slices->DursData()[1] == 168758682456000 - 168758663028000); // slice 1
    EXPECT_TRUE(slices->Depths()[1] == 1);
}
HWTEST_F(SliceFilterTest, SliceTest5, TestSize.Level1)
{
    TS_LOGI("test3");
    DataIndex splitStrIndex = stream_.traceDataCache_->GetDataIndex("thread_one_call_function_one");
    stream_.streamFilters_->sliceFilter_->BeginSlice(168758663018000, 2532, 2519, 0, splitStrIndex); // slice 0
    splitStrIndex = stream_.traceDataCache_->GetDataIndex("thread_two_call_function_one");
    stream_.streamFilters_->sliceFilter_->BeginSlice(168758663028000, 2533, 2529, 0, splitStrIndex);
    splitStrIndex = stream_.traceDataCache_->GetDataIndex("thread_one_call_function_two");
    stream_.streamFilters_->sliceFilter_->BeginSlice(168758679303000, 2532, 2519, 0, splitStrIndex); // slice 2
    splitStrIndex = stream_.traceDataCache_->GetDataIndex("thread_two_call_function_two");
    stream_.streamFilters_->sliceFilter_->BeginSlice(168758679312000, 2533, 2529, 0, splitStrIndex);
    splitStrIndex = stream_.traceDataCache_->GetDataIndex("thread_one_call_function_three");
    stream_.streamFilters_->sliceFilter_->BeginSlice(168758679313000, 2532, 2519, 0, splitStrIndex); // slice 4
    splitStrIndex = stream_.traceDataCache_->GetDataIndex("thread_two_call_function_three");
    stream_.streamFilters_->sliceFilter_->BeginSlice(168758679323000, 2533, 2529, 0, splitStrIndex);
    // end thread_one_call_function_three
    stream_.streamFilters_->sliceFilter_->EndSlice(168758682456000, 2532, 2519);
    // end thread_one_call_function_two
    stream_.streamFilters_->sliceFilter_->EndSlice(168758682466000, 2532, 2519);
    // end thread_one_call_function_one
    stream_.streamFilters_->sliceFilter_->EndSlice(168758682476000, 2532, 2519);
    // end thread_two_call_function_three slice 5
    stream_.streamFilters_->sliceFilter_->EndSlice(168758679343000, 2533, 2529);
    // end thread_two_call_function_two slice 3
    stream_.streamFilters_->sliceFilter_->EndSlice(168758679344000, 2533, 2529);
    // end thread_two_call_function_one slice 1
    stream_.streamFilters_->sliceFilter_->EndSlice(168758689323000, 2533, 2529);
    auto slices = stream_.traceDataCache_->GetInternalSlicesData();
    EXPECT_TRUE(slices->Size() == 6);
    EXPECT_TRUE(slices->DursData()[0] == 168758682476000 - 168758663018000); // slice 0
    EXPECT_TRUE(slices->Depths()[0] == 0);
    EXPECT_TRUE(slices->DursData()[1] == 168758689323000 - 168758663028000); // slice 1
    EXPECT_TRUE(slices->Depths()[1] == 0);
    EXPECT_TRUE(slices->DursData()[2] == 168758682466000 - 168758679303000); // slice 2
    EXPECT_TRUE(slices->Depths()[2] == 1);
    EXPECT_TRUE(slices->DursData()[3] == 168758679344000 - 168758679312000); // slice 3
    EXPECT_TRUE(slices->Depths()[3] == 1);
    EXPECT_TRUE(slices->DursData()[4] == 168758682456000 - 168758679313000); // slice 4
    EXPECT_TRUE(slices->DursData()[5] == 168758679343000 - 168758679323000); // slice 5
}

HWTEST_F(SliceFilterTest, SyncTest1, TestSize.Level1)
{
    TS_LOGI("test3");
    DataIndex splitStrIndex = stream_.traceDataCache_->GetDataIndex("async_call_function_one");
    stream_.streamFilters_->sliceFilter_->StartAsyncSlice(168758663018000, 2532, 2519, 0, splitStrIndex); // slice 0
    splitStrIndex = stream_.traceDataCache_->GetDataIndex("async_call_function_one");
    // end thread_one_call_function_three
    stream_.streamFilters_->sliceFilter_->FinishAsyncSlice(168758682456000, 2532, 2519, 0, splitStrIndex);
    auto slices = stream_.traceDataCache_->GetInternalSlicesData();
    EXPECT_TRUE(slices->Size() == 1);
    EXPECT_TRUE(slices->DursData()[0] == 168758682456000 - 168758663018000); // slice 0
}

HWTEST_F(SliceFilterTest, SyncTest2, TestSize.Level1)
{
    TS_LOGI("test3");
    DataIndex splitStrIndex = stream_.traceDataCache_->GetDataIndex("async_call_function_one");
    stream_.streamFilters_->sliceFilter_->StartAsyncSlice(168758663018000, 2532, 2519, 0, splitStrIndex); // slice 0
    splitStrIndex = stream_.traceDataCache_->GetDataIndex("async_call_function_one");
    // end thread_one_call_function_three
    stream_.streamFilters_->sliceFilter_->FinishAsyncSlice(168758682456000, 2532, 2519, 0, splitStrIndex);
    auto slices = stream_.traceDataCache_->GetInternalSlicesData();
    EXPECT_TRUE(slices->Size() == 1);
    EXPECT_TRUE(slices->DursData()[0] == 168758682456000 - 168758663018000); // slice 0
}

HWTEST_F(SliceFilterTest, BeginSliceMulti3, TestSize.Level1)
{
    TS_LOGI("test3");
    DataIndex splitStrIndex = stream_.traceDataCache_->GetDataIndex("thread_one_call_function_one");
    stream_.streamFilters_->sliceFilter_->StartAsyncSlice(168758663018000, 2532, 2519, 0, splitStrIndex); // slice 0
    DataIndex splitStrIndex2 = stream_.traceDataCache_->GetDataIndex("thread_two_call_function_one");
    stream_.streamFilters_->sliceFilter_->StartAsyncSlice(168758663028000, 2533, 2529, 0, splitStrIndex2);
    // end thread_one_call_function_three
    stream_.streamFilters_->sliceFilter_->FinishAsyncSlice(168758682456000, 2532, 2519, 0, splitStrIndex);
    // end thread_two_call_function_three slice 5
    stream_.streamFilters_->sliceFilter_->FinishAsyncSlice(168758679343000, 2533, 2529, 0, splitStrIndex2);
    auto slices = stream_.traceDataCache_->GetInternalSlicesData();
    EXPECT_TRUE(slices->Size() == 2);
    EXPECT_TRUE(slices->DursData()[0] == 168758682456000 - 168758663018000); // slice 0
    EXPECT_TRUE(slices->Depths()[0] == 0);
    EXPECT_TRUE(slices->DursData()[1] == 168758679343000 - 168758663028000); // slice 1
}

HWTEST_F(SliceFilterTest, BeginSliceMulti4, TestSize.Level1)
{
    TS_LOGI("test3");
    DataIndex splitStrIndex = stream_.traceDataCache_->GetDataIndex("thread_one_call_function_one");
    stream_.streamFilters_->sliceFilter_->StartAsyncSlice(168758663018000, 2532, 2519, 0, splitStrIndex); // slice 0
    DataIndex splitStrIndex2 = stream_.traceDataCache_->GetDataIndex("thread_two_call_function_one");
    stream_.streamFilters_->sliceFilter_->StartAsyncSlice(168758663028000, 2533, 2529, 0, splitStrIndex2);
    DataIndex splitStrIndex3 = stream_.traceDataCache_->GetDataIndex("thread_three_call_function_two");
    stream_.streamFilters_->sliceFilter_->StartAsyncSlice(168758679303000, 2532, 2519, 1, splitStrIndex3); // slice 2
    // end thread_one_call_function_three
    stream_.streamFilters_->sliceFilter_->FinishAsyncSlice(168758682456000, 2532, 2519, 0, splitStrIndex);
    // end thread_one_call_function_two
    stream_.streamFilters_->sliceFilter_->FinishAsyncSlice(168758682466000, 2532, 2519, 1, splitStrIndex3);
    // end thread_two_call_function_three slice 5
    stream_.streamFilters_->sliceFilter_->FinishAsyncSlice(168758679343000, 2533, 2529, 0, splitStrIndex2);
    auto slices = stream_.traceDataCache_->GetInternalSlicesData();
    EXPECT_TRUE(slices->Size() == 3);
    EXPECT_TRUE(slices->DursData()[0] == 168758682456000 - 168758663018000); // slice 0
    EXPECT_TRUE(slices->Depths()[0] == 0);
    EXPECT_TRUE(slices->DursData()[1] == 168758679343000 - 168758663028000); // slice 1
    EXPECT_TRUE(slices->Depths()[1] == 0);
    EXPECT_TRUE(slices->DursData()[2] == 168758682466000 - 168758679303000); // slice 2
}

HWTEST_F(SliceFilterTest, BeginSliceMulti5, TestSize.Level1)
{
    TS_LOGI("test3");
    DataIndex splitStrIndex = stream_.traceDataCache_->GetDataIndex("thread_one_call_function_one");
    stream_.streamFilters_->sliceFilter_->StartAsyncSlice(168758663018000, 2532, 2519, 0, splitStrIndex); // slice 0
    DataIndex splitStrIndex2 = stream_.traceDataCache_->GetDataIndex("thread_two_call_function_one");
    stream_.streamFilters_->sliceFilter_->StartAsyncSlice(168758663028000, 2533, 2529, 0, splitStrIndex2);
    DataIndex splitStrIndex3 = stream_.traceDataCache_->GetDataIndex("thread_one_call_function_two");
    stream_.streamFilters_->sliceFilter_->StartAsyncSlice(168758679303000, 2532, 2519, 1, splitStrIndex3); // slice 2
    DataIndex splitStrIndex4 = stream_.traceDataCache_->GetDataIndex("thread_two_call_function_two");
    stream_.streamFilters_->sliceFilter_->StartAsyncSlice(168758679312000, 2533, 2529, 1, splitStrIndex4);
    DataIndex splitStrIndex5 = stream_.traceDataCache_->GetDataIndex("thread_one_call_function_three");
    stream_.streamFilters_->sliceFilter_->StartAsyncSlice(168758679313000, 2532, 2519, 2, splitStrIndex5); // slice 4
    DataIndex splitStrIndex6 = stream_.traceDataCache_->GetDataIndex("thread_two_call_function_three");
    stream_.streamFilters_->sliceFilter_->StartAsyncSlice(168758679323000, 2533, 2529, 1, splitStrIndex6);
    // end thread_one_call_function_three
    stream_.streamFilters_->sliceFilter_->FinishAsyncSlice(168758682456000, 2532, 2519, 0, splitStrIndex);
    // end thread_one_call_function_two
    stream_.streamFilters_->sliceFilter_->FinishAsyncSlice(168758682466000, 2532, 2519, 1, splitStrIndex3);
    // end thread_one_call_function_one
    stream_.streamFilters_->sliceFilter_->FinishAsyncSlice(168758682476000, 2532, 2519, 2, splitStrIndex5);
    // end thread_two_call_function_three slice 5
    stream_.streamFilters_->sliceFilter_->FinishAsyncSlice(168758679343000, 2533, 2529, 0, splitStrIndex2);
    // end thread_two_call_function_two slice 3
    stream_.streamFilters_->sliceFilter_->FinishAsyncSlice(168758679344000, 2533, 2529, 1, splitStrIndex4);
    // end thread_two_call_function_one slice 1
    stream_.streamFilters_->sliceFilter_->FinishAsyncSlice(168758689323000, 2533, 2529, 1, splitStrIndex6);
    auto slices = stream_.traceDataCache_->GetInternalSlicesData();
    EXPECT_TRUE(slices->Size() == 6);
    EXPECT_TRUE(slices->DursData()[0] == 168758682456000 - 168758663018000); // slice 0
    EXPECT_TRUE(slices->Depths()[0] == 0);
    EXPECT_TRUE(slices->DursData()[1] == 168758679343000 - 168758663028000); // slice 1
    EXPECT_TRUE(slices->Depths()[1] == 0);
    EXPECT_TRUE(slices->DursData()[2] == 168758682466000 - 168758679303000); // slice 2
    EXPECT_TRUE(slices->Depths()[2] == 0);
    EXPECT_TRUE(slices->DursData()[3] == 168758679344000 - 168758679312000); // slice 3
    EXPECT_TRUE(slices->Depths()[3] == 0);
    EXPECT_TRUE(slices->DursData()[4] == 168758682476000 - 168758679313000); // slice 4
    EXPECT_TRUE(slices->DursData()[5] == 168758689323000 - 168758679323000); // slice 5
}

}
}
