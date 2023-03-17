/*
 * Copyright (C) 2021 Huawei Device Co., Ltd.
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

#ifndef EDITOR_COMMAND_H
#define EDITOR_COMMAND_H

namespace OHOS {
namespace SmartPerf {
class EditorCommand {
public:
    const int threeParamMore = 3;
    EditorCommand(int argc, char *argv[]);
    ~EditorCommand() {};
    float coldStart(char *argv[]);
    float hotStart(char *argv[]);
    float completeTime(char *argv[]);
    float responseTime(char *argv[]);
};
}
}
#endif // SMARTPERF_COMMAND_H