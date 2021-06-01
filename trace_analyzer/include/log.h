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

#ifndef INCLUDE_TUNING_BASE_LOGGING_H_
#define INCLUDE_TUNING_BASE_LOGGING_H_

#include <stdio.h>
#include <string.h>

namespace SysTuning {
namespace base {
#define TUNING_LOGI(format, ...) fprintf(stderr, "[%s][%d]: " format "\n", __FILE__, __LINE__, ##__VA_ARGS__)

#ifdef NDEBUG
#define TUNING_LOGD(format, ...)
#define TUNING_LOGF(format, ...)
#define TUNING_ASSERT(x)
#else
#define TUNING_CRASH           \
    do {                         \
        __builtin_trap();        \
        __builtin_unreachable(); \
    } while (0)


#define TUNING_LOGD(format, ...) fprintf(stderr, "[%s][%s][%d]: " format "\n", __FILE__, __FUNCTION__, \
                                __LINE__, ##__VA_ARGS__)

#define TUNING_LOGF(format, ...)     \
    do {                             \
        TUNING_CRASH;              \
    } while (0)

#define TUNING_ASSERT(x)        \
    do {                        \
        if (!(x)) {                \
            TUNING_CRASH;     \
        }                       \
    } while (0)

#endif
} // namespace base
} // namespace SysTuning

#endif // INCLUDE_TUNING_BASE_LOGGING_H_
