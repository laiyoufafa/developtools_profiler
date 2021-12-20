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

with list as ( SELECT  IP.name as processName,
    IP.name processCmdLine,
    IP.pid as processId,B.cpu,
    A.name,C.id as schedId,
    A.tid,
    A.id, A.type,
    B.dur, B.ts - TR.start_ts AS startTime,
    C.priority, C.end_state
from thread_state AS B
    left join  internal_thread as A
    left join sched_slice AS C
    left join trace_bounds AS TR
    left join internal_process AS IP
where B.utid = A.id and B.cpu = %s
    and B.utid = C.utid and B.ts = C.ts
    and A.upid = IP.id
    and startTime between %s and %s
order by B.rowid )
select * from list;