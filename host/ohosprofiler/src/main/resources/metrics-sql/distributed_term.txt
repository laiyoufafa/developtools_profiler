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
select
    group_concat(thread.id,",") as threadId,
    group_concat(thread.name,",") as threadName,
    group_concat(process.id,",") as processId,
    group_concat(process.name,",") as processName,
    group_concat(callstack.name,",") as funName,
    group_concat(callstack.dur,",") as dur,
    group_concat(callstack.ts,",") as ts,
    cast(callstack.chainId as varchar) as chainId,
    callstack.spanId,
    callstack.parentSpanId,
    group_concat(callstack.flag,",") as flag,
    (select value from meta where name="source_name") as trace_name
from callstack
inner join thread on callstack.callid = thread.id
inner join process on process.id = thread.ipid
where (callstack.flag="S" or callstack.flag="C")
group by callstack.chainId,callstack.spanId,callstack.parentSpanId