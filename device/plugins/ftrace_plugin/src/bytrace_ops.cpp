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
 *
 * Description: BytraceOps implements
 */
#include "bytrace_ops.h"

#include "logging.h"
#include "string_utils.h"

FTRACE_NS_BEGIN
BytraceOps::BytraceOps() : TraceOps("/system/bin/bytrace", "bytrace", BYTRACE) {}

BytraceOps::~BytraceOps() {}

bool BytraceOps::PrepareListCategoriesCmd()
{
    args_.push_back("-l");
    return true;
}

bool BytraceOps::PrepareEnableCategoriesCmd()
{
    args_.push_back("--trace_begin");
    args_.push_back("-t");
    args_.push_back("1");

    for (auto& category : targetCategories_) {
        args_.push_back(category);
    }
    return true;
}

bool BytraceOps::PrepareDisableCategoriesCmd()
{
    args_.push_back("--trace_finish");
    return true;
}
FTRACE_NS_END