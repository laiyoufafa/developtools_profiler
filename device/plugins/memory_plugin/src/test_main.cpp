/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
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

#include <iostream>
#include <stdlib.h>
#include <string.h>
#include <vector>

namespace {
const size_t MB_PER_BYTE = 0x100000;
}

int main(int agrc, char* agrv[]) {
    std::vector<char*> cache;
    size_t size = 0;
    char *buf = nullptr;

    for (int i = 1; i < agrc; i++) {
        size = atoi(agrv[i]);
        if (size <= 0) {
            printf("ready malloc size(%zu)Mb invalid,", size);
            continue;
        }
        buf = (char *)malloc(size * MB_PER_BYTE);
        if (buf == NULL) {
            printf("malloc %zu fail, err(%s:%d)", size, strerror(errno), errno);
            continue;
        }
        printf("malloc size(%zu)Mb succ\r\n", size);
        cache.emplace(cache.begin() + i - 1, buf);
    }
    while(true);
    return 0;
}