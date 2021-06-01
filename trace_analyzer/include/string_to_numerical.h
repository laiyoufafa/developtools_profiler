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

#ifndef INCLUDE_BASE_STRING_TO_NUMERICAL_H_
#define INCLUDE_BASE_STRING_TO_NUMERICAL_H_

#include <optional>
#include <string>

namespace SysTuning {
namespace base {
inline std::optional<uint32_t> StrToUInt32(const std::string& str, int base = 10)
{
    if (!str.empty()) {
        uint32_t value = static_cast<uint32_t>(std::stoul(str, nullptr, base));
        return std::make_optional(value);
    }

    return std::nullopt;
}

inline std::optional<int32_t> StrToInt32(const std::string& str, int base = 10)
{
    if (!str.empty()) {
        int32_t value = static_cast<int32_t>(std::stol(str, nullptr, base));
        return std::make_optional(value);
    }

    return std::nullopt;
}

inline std::optional<uint64_t> StrToUInt64(const std::string& str, int base = 10)
{
    if (!str.empty()) {
        uint64_t value = static_cast<uint64_t>(std::stoull(str, nullptr, base));
        return std::make_optional(value);
    }

    return std::nullopt;
}

inline std::optional<int64_t> StrToInt64(const std::string& str, int base = 10)
{
    if (!str.empty()) {
        int64_t value = static_cast<int64_t>(std::stoll(str, nullptr, base));
        return std::make_optional(value);
    }
    return std::nullopt;
}

inline std::optional<double> StrToDouble(const std::string& str)
{
    if (!str.empty()) {
        double value = std::stod(str);
        return std::make_optional(value);
    }

    return std::nullopt;
}
} // namespace base
} // namespace SysTuning

#endif // INCLUDE_TUNING_EXT_BASE_STRING_UTILS_H_
