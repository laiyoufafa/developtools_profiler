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

#include <debug_logger_test.h>

#include <gtest/gtest_prod.h>
#include <debug_logger.h>
#include <random>

using namespace testing::ext;
using namespace std;
#ifndef CONFIG_NO_HILOG
using namespace OHOS::HiviewDFX;
#endif

static const char* OHOS_HILOG_TAG  = "DebugTest";

namespace OHOS {
namespace Developtools {
namespace NativeDaemon {
#ifdef DEFAULT_LOG_LEVEL
DebugLevel DebugLogger::debugLevel_ = DEFAULT_LOG_LEVEL;
bool DebugLogger::logDisabled_ = false;
#endif
class DebugLoggerTest : public testing::Test {
public:
    static void SetUpTestCase(void);
    static void TearDownTestCase(void);
    void SetUp();
    void TearDown();
    void LogLevelTest(DebugLevel testlevel, bool useStdout = false);
    const std::string TEST_LOG_MESSAGE = "<HELLO_TEST_LOG_MESSAGE>";
    std::random_device rd_;
};

void DebugLoggerTest::SetUpTestCase() {}

void DebugLoggerTest::TearDownTestCase() {}

void DebugLoggerTest::SetUp() {}

void DebugLoggerTest::TearDown() {}

void DebugLoggerTest::LogLevelTest(DebugLevel testlevel, bool useStdout)
{
    StdoutRecord stdoutRecord;
    std::string log;
    // backup
    DebugLogger::GetInstance()->exitOnFatal_ = false;
    DebugLevel oldLevel = DebugLogger::GetInstance()->GetLogLevel();
    DebugLogger::GetInstance()->SetLogLevel(testlevel);

    const std::string logMessage =
        TEST_LOG_MESSAGE + std::to_string(rd_()) + std::to_string(testlevel);

    if (useStdout) {
        stdoutRecord.Start();
    }

    HLOGF("%s", logMessage.c_str());
    HLOGE("%s", logMessage.c_str());
    HLOGW("%s", logMessage.c_str());
    HLOGI("%s", logMessage.c_str());
    HLOGD("%s", logMessage.c_str());
    HLOGV("%s", logMessage.c_str());
    HLOGM("%s", logMessage.c_str());

    if (useStdout) {
        log = stdoutRecord.Stop();
    } else {
        fflush(DebugLogger::GetInstance()->file_);
        log = ReadFileToString(DebugLogger::GetInstance()->logPath_);
    }

    // we have 7 level log
    // so the logout line is : (all log level - curr log level) + curr log level self
    if (testlevel > LEVEL_FATAL or DebugLogger::GetInstance()->enableHilog_ or
        DebugLogger::GetInstance()->logDisabled_) {
        EXPECT_EQ(SubStringCount(log, logMessage), 0u);
    } else {
        EXPECT_EQ(SubStringCount(log, logMessage),
            static_cast<size_t>(LEVEL_FATAL) - static_cast<size_t>(testlevel) + 1u);
    }
    if (HasFailure()) {
        HLOGD("LogLevelTest failed. testlevel is %d, logMessage is '%s'", testlevel,
            logMessage.c_str());
        if (useStdout) {
            printf("%s", log.c_str());
        }
    }
    // restore
    DebugLogger::GetInstance()->SetLogLevel(oldLevel);
    DebugLogger::GetInstance()->exitOnFatal_ = true;
}

/**
 * @tc.name: Log
 * @tc.desc:
 * @tc.type: FUNC
 */
HWTEST_F(DebugLoggerTest, Log, TestSize.Level1)
{
    DebugLogger::GetInstance()->SetMixLogOutput(true);
    StdoutRecord stdoutRecord;
    std::string log;

    stdoutRecord.Start();
    HLOGD("123");
    log = stdoutRecord.Stop();
    ASSERT_TRUE(log.size() >= 4u);
    EXPECT_STREQ(log.substr(log.size() - 4u).c_str(), "123\n");

    stdoutRecord.Start();
    HLOGD("%% %% %%");
    log = stdoutRecord.Stop();
    EXPECT_STREQ(log.substr(log.size() - 6u).c_str(), "% % %\n");

    DebugLogger::GetInstance()->SetMixLogOutput(false);

    stdoutRecord.Start();
    printf("123\n");
    log = stdoutRecord.Stop();
    EXPECT_STREQ(log.c_str(), "123\n");

    stdoutRecord.Start();
    printf("%% %% %%\n");
    log = stdoutRecord.Stop();
    EXPECT_STREQ(log.c_str(), "% % %\n");
}

/**
 * @tc.name: SetMixLogOutput
 * @tc.desc:
 * @tc.type: FUNC
 */
HWTEST_F(DebugLoggerTest, SetMixLogOutput, TestSize.Level1)
{
    DebugLogger::GetInstance()->SetMixLogOutput(true);
    for (int loglevel = LEVEL_MUCH; loglevel < LEVEL_MAX; loglevel++) {
        LogLevelTest(static_cast<DebugLevel>(loglevel), true);
    }

    DebugLogger::GetInstance()->SetMixLogOutput(false);
    for (int loglevel = LEVEL_MUCH; loglevel < LEVEL_MAX; loglevel++) {
        LogLevelTest(static_cast<DebugLevel>(loglevel), false);
    }
}

/**
 * @tc.name: SetLogLevel
 * @tc.desc:
 * @tc.type: FUNC
 */
HWTEST_F(DebugLoggerTest, SetLogLevel, TestSize.Level1)
{
    for (int loglevel = LEVEL_MUCH; loglevel < LEVEL_MAX; loglevel++) {
        LogLevelTest(static_cast<DebugLevel>(loglevel));
    }
}

/**
 * @tc.name: GetInstance
 * @tc.desc:
 * @tc.type: FUNC
 */
HWTEST_F(DebugLoggerTest, GetInstance, TestSize.Level1)
{
    EXPECT_NE(DebugLogger::GetInstance(), nullptr);
}

/**
 * @tc.name: SetLogPath
 * @tc.desc:
 * @tc.type: FUNC
 */
HWTEST_F(DebugLoggerTest, SetLogPath, TestSize.Level1)
{
    const std::string LOG_FILE1 = "log1.txt";
    const std::string LOG_FILE2 = "log2.txt";
    const std::string LOG_FILE3 = "log3.txt";
    const std::string LOG_FILE_ERR = "!@#$%^&*()";

    EXPECT_EQ(access(DEFAULT_LOG_PATH.c_str(), F_OK), 0);

    DebugLogger::GetInstance()->SetLogPath(LOG_FILE1);
    EXPECT_NE(access(DEFAULT_LOG_PATH.c_str(), F_OK), 0);
    EXPECT_EQ(access(LOG_FILE1.c_str(), F_OK), 0);

    DebugLogger::GetInstance()->SetLogPath(LOG_FILE2);
    EXPECT_NE(access(LOG_FILE1.c_str(), F_OK), 0);
    EXPECT_EQ(access(LOG_FILE2.c_str(), F_OK), 0);

    DebugLogger::GetInstance()->SetLogPath(LOG_FILE3);
    EXPECT_NE(access(LOG_FILE2.c_str(), F_OK), 0);
    EXPECT_EQ(access(LOG_FILE3.c_str(), F_OK), 0);

    DebugLogger::GetInstance()->SetLogPath(DEFAULT_LOG_PATH);
    EXPECT_NE(access(LOG_FILE3.c_str(), F_OK), 0);
    EXPECT_EQ(access(DEFAULT_LOG_PATH.c_str(), F_OK), 0);
}

/**
 * @tc.name: SetLogPath
 * @tc.desc:
 * @tc.type: FUNC
 */
HWTEST_F(DebugLoggerTest, SetLogTags, TestSize.Level1)
{
    const std::string ERROR_LOG_TAG = "errtag";
    const std::string ERROR_LOG_TAGS = "errtag,errtag,errtag";
    const std::string MIX_LOG_TAGS = std::string("errtag,errtag,") + OHOS_HILOG_TAG + string(",errtag");

    DebugLogger::GetInstance()->SetLogTags(OHOS_HILOG_TAG);

    EXPECT_EQ(DebugLogger::GetInstance()->ShouldLog(LEVEL_MUCH, OHOS_HILOG_TAG), true);
    EXPECT_EQ(DebugLogger::GetInstance()->ShouldLog(LEVEL_DEBUG, OHOS_HILOG_TAG), true);

    EXPECT_EQ(DebugLogger::GetInstance()->ShouldLog(LEVEL_MUCH, ERROR_LOG_TAG), false);
    EXPECT_EQ(DebugLogger::GetInstance()->ShouldLog(LEVEL_DEBUG, ERROR_LOG_TAG), true);

    DebugLogger::GetInstance()->SetLogTags(ERROR_LOG_TAGS);

    EXPECT_EQ(DebugLogger::GetInstance()->ShouldLog(LEVEL_MUCH, OHOS_HILOG_TAG), false);
    EXPECT_EQ(DebugLogger::GetInstance()->ShouldLog(LEVEL_DEBUG, OHOS_HILOG_TAG), true);

    EXPECT_EQ(DebugLogger::GetInstance()->ShouldLog(LEVEL_MUCH, ERROR_LOG_TAG), true);
    EXPECT_EQ(DebugLogger::GetInstance()->ShouldLog(LEVEL_DEBUG, ERROR_LOG_TAG), true);

    DebugLogger::GetInstance()->SetLogTags(MIX_LOG_TAGS);

    EXPECT_EQ(DebugLogger::GetInstance()->ShouldLog(LEVEL_MUCH, OHOS_HILOG_TAG), true);
    EXPECT_EQ(DebugLogger::GetInstance()->ShouldLog(LEVEL_DEBUG, OHOS_HILOG_TAG), true);

    // back to default
    DebugLogger::GetInstance()->SetLogTags("");
}

/**
 * @tc.name: Disable
 * @tc.desc:
 * @tc.type: FUNC
 */
HWTEST_F(DebugLoggerTest, Disable, TestSize.Level1)
{
    HLOGD("log disabled");
    DebugLogger::GetInstance()->Disable(true);
    ASSERT_EQ(DebugLogger::GetInstance()->logDisabled_, true);
    for (int loglevel = LEVEL_MUCH; loglevel < LEVEL_MAX; loglevel++) {
        LogLevelTest(static_cast<DebugLevel>(loglevel));
    }
    ASSERT_EQ(DebugLogger::GetInstance()->logDisabled_, true);

    HLOGD("log enabled");
    DebugLogger::GetInstance()->Disable(false);
    ASSERT_EQ(DebugLogger::GetInstance()->logDisabled_, false);
    for (int loglevel = LEVEL_MUCH; loglevel < LEVEL_MAX; loglevel++) {
        LogLevelTest(static_cast<DebugLevel>(loglevel));
    }
}

/**
 * @tc.name: EnableHiLog
 * @tc.desc:
 * @tc.type: FUNC
 */
HWTEST_F(DebugLoggerTest, EnableHiLog, TestSize.Level1)
{
#if is_ohos
    DebugLogger::GetInstance()->EnableHiLog(true);
    for (int loglevel = LEVEL_MUCH; loglevel < LEVEL_MAX; loglevel++) {
        LogLevelTest(static_cast<DebugLevel>(loglevel));
    }
    DebugLogger::GetInstance()->EnableHiLog(false);
#endif
}
} // namespace NativeDaemon
} // namespace Developtools
} // namespace OHOS
