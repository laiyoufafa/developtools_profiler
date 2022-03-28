/*
* Copyright (C) 2021 Huawei Device Co., Ltd.
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
#ifndef GP_UTILS_H
#define GP_UTILS_H
#include <string>
#include <vector>
#include "gp_data.h"
namespace OHOS {
    namespace SmartPerf {
        namespace GPUtils {

            void mSplit(const std::string &content, const std::string &sp, std::vector<std::string> &out);

            bool canOpen(const std::string &path);

            std::string readFile(const std::string &cmd);

            std::string freadFile(const std::string &path);

            std::string getNumber(const std::string &str);

            void writeCsv(const std::string &path, std::vector<GPData> &vmap);

        };
    }
}

#endif // GP_UTILS_H
