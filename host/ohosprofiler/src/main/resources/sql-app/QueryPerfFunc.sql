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
select perf_callchain.callchain_id,
sample.sample_id,
sample.thread_id,
sample.timestamp,
perf_callchain.file_id,
perf_callchain.symbol_id
     ,perf_callchain.vaddr_in_file
     from perf_callchain inner join (select * from perf_sample where thread_id = %s) as sample
    on sample.sample_id = perf_callchain.sample_id order by sample.id,perf_callchain.callchain_id desc ;