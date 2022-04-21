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

#ifndef ___base_file_utility_h__
#define ___base_file_utility_h__

#include <string>

/*
* 功能：处理文件路径
* 返回处理之后的 字符串，如果处理失败 返回 空串("")
*/
std::string CanonicalizeSpecPath(const char* src);

#endif  /* ___base_file_utility_h__ */
