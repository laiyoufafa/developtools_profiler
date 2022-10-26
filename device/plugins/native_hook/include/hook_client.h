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

#ifndef __ohos_malloc_hook_client_h__
#define __ohos_malloc_hook_client_h__

#include <stdlib.h>
#include "musl_malloc_dispatch.h"

#ifdef __cplusplus
extern "C" {
#endif

bool ohos_malloc_hook_initialize(const MallocDispatchType*, bool*, const char*);
bool ohos_malloc_hook_get_hook_flag(void);
bool ohos_malloc_hook_set_hook_flag(bool);
void ohos_malloc_hook_finalize(void);
bool ohos_malloc_hook_on_start(void);
bool ohos_malloc_hook_on_end(void);
void* ohos_malloc_hook_malloc(size_t);
void* ohos_malloc_hook_realloc(void*, size_t);
void* ohos_malloc_hook_calloc(size_t, size_t);
void* ohos_malloc_hook_valloc(size_t);
void ohos_malloc_hook_free(void*);
void* ohos_malloc_hook_memalign(size_t, size_t);
size_t ohos_malloc_hook_malloc_usable_size(void*);
void* ohos_malloc_hook_mmap(void*, size_t, int, int, int, off_t);
int ohos_malloc_hook_munmap(void*, size_t);
void ohos_malloc_hook_memtag(void* addr, size_t size, char* tag, size_t tagLen);
bool ohos_set_filter_size(size_t size, void* ret);
pid_t ohos_get_real_pid(void);
int ohos_convert_pid(char* buf);
bool ohos_pid_changed(void);
void* ohos_release_on_end(void*);

#ifdef __cplusplus
}
#endif


#endif /* __ohos_malloc_hook_client_h__ */