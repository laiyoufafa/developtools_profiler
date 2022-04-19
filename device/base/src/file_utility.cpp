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

#include "file_utility.h"
#include <limits.h>
#include <unistd.h>

#include "logging.h"

std::string CanonicalizeSpecPath(const char* src)
{
    if (src == nullptr || strlen(src) >= PATH_MAX) {
         HILOG_ERROR(LOG_CORE, "%s:Error: CanonicalizeSpecPath %s failed", __func__, src);
         return "";
    }
    char resolvedPath[PATH_MAX] = { 0 };
    if (access(src, F_OK) == 0) {
        if (realpath(src, resolvedPath) == nullptr) {
           HILOG_ERROR(LOG_CORE, "%s:Error: realpath %s failed", __func__, src); 
            return "";
        }
    } else {
        std::string fileName(src);
        // 文件路径中不能包含 (..)
        if (fileName.find("..") == std::string::npos) {
            if (sprintf_s(resolvedPath, PATH_MAX, "%s", src) == -1) {
                HILOG_ERROR(LOG_CORE, "%s:Error: sprintf_s %s failed", __func__, src); 
                return "";
            }
        } else {
            HILOG_ERROR(LOG_CORE, "%s:Error: find.. %s failed", __func__, src); 
             return "";
        }
    }
    std::string res(resolvedPath);
    return res;
}
