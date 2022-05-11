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
#include "string_help.h"
#include <cstdio>
#include <string>
#include <memory.h>
int memcpy_s(void *det, uint32_t detSize, const void * src, size_t srcSize)
{
	if (srcSize > detSize || src == nullptr || det == nullptr) {
		return -1;
	} else {
		memcpy(det, src, srcSize);
    }
	return 0;
}
