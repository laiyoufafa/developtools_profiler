/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <fcntl.h>

#include <hwext/gtest-ext.h>
#include <hwext/gtest-tag.h>

#include "file.h"
#include "log.h"
#include "trace_analyzer.h"

static char g_argv[3][100] = {"./test.systrace.txt", "-e", "./test.db"};

using namespace SysTuning;
using namespace base;
using namespace trace_analyzer;

// TestSuite:
class TraceAnalyzerTest : public ::testing::Test {
public:
    static void SetUpTestCase() {}
    static void TearDownTestCase() {}
    void SetUp() override {}

    void TearDown() override {}
};

HWTEST_F(TraceAnalyzerTest, EnterTraceAnalyzer)
{
    int argc = 3;
    ExportStatusToLog(ABNORMAL);
    int result = SysTuning::trace_analyzer::EnterTraceAnalyzer(argc, &g_argv);
    ExportStatusToLog(GetAnalysisResult());
    EXPECT_EQ(result, 1);
}

HWTEST_F(TraceAnalyzerTest, EnterTraceAnalyzer_args_too_short)
{
    int argc = 1;
    int result = SysTuning::trace_analyzer::EnterTraceAnalyzer(argc, &argv);
    EXPECT_EQ(result, 0);
}

HWTEST_F(TraceAnalyzerTest, EnterTraceAnalyzer_version)
{
    int argc = 1;
    char argv[3][100] = {"-v"};
    int result = SysTuning::trace_analyzer::EnterTraceAnalyzer(argc, &argv);
    EXPECT_EQ(result, 1);

    char argv1[3][100] = {"--v"};
    int result = SysTuning::trace_analyzer::EnterTraceAnalyzer(argc, &1argv);
    EXPECT_EQ(result, 1);

    char argv2[3][100] = {"--version"};
    int result = SysTuning::trace_analyzer::EnterTraceAnalyzer(argc, &2argv);
    EXPECT_EQ(result, 1);

    char argv3[3][100] = {"-version"};
    int result = SysTuning::trace_analyzer::EnterTraceAnalyzer(argc, &3argv);
    EXPECT_EQ(result, 1);
}
