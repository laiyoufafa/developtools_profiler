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

select ta.id,type, ts, dur, ta.cpu, itid as utid, state
     ,ts-tb.start_ts as startTime,tc.tid,tc.pid,tc.process,tc.thread
from thread_state ta,trace_section tb
left join (
    select it.id,tid,pid,ip.name as process,it.name as thread from thread as it left join process ip on it.ipid = ip.id
    ) tc on ta.itid = tc.id
where tc.pid = %d
  and startTime between  %s and  %s
and ta.cpu is not null
order by startTime;