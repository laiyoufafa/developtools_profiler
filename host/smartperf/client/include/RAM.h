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
#ifndef RAM_H
#define RAM_H
#include <map>
#include <string>
#include <pthread.h>

namespace OHOS {
    namespace SmartPerf {
        class RAM {
        public:
            static RAM *getInstance();
            void setPkgName(std::string ss);
            std::map<std::string, std::string> getRamInfo(std::string pkg_name, int pid);
            static pthread_mutex_t mutex;
        private:
            RAM();
            ~RAM();
            static RAM *instance;
            std::string pkgName;
        };
    }
}
#endif
