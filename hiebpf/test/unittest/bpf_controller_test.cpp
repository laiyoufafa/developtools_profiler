/*
 * Copyright (c) 2022 Huawei Device Co., Ltd.
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
#include <string>
#include <thread>

#include <gtest/gtest.h>

#include "bpf_controller.h"

using namespace testing::ext;

namespace {
constexpr int ROUND_COUNT = 1000;
constexpr int BUF_SIZE = 512;
const std::string FILE_NAME = "/data/local/tmp/hiebpf.txt";
const std::string HIEBPF_FILE_NAME = "/data/local/tmp/hiebpf.data";
constexpr int FILE_MODE = 0644;
} // namespace

namespace OHOS {
namespace Developtools {
namespace Hiebpf {
class BpfControllerTest : public ::testing::Test {
public:
    static void SetUpTestCase() {};
    static void TearDownTestCase()
    {
        if (access(FILE_NAME.c_str(), F_OK) == 0) {
            std::string cmd = "rm " + FILE_NAME;
            system(cmd.c_str());
        }
        if (access(HIEBPF_FILE_NAME.c_str(), F_OK) == 0) {
            std::string cmd = "rm " + HIEBPF_FILE_NAME;
            system(cmd.c_str());
        }
    }

    void SetUp() {}
    void TearDown() {}
};
} // namespace Hiebpf
} // namespace Developtools
} // namespace OHOS
