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
--参数 1 tids 数组，框选范围中选中的 thread id 集合
--参数 2 leftNs 框选范围左边的时间，单位 ns
--参数 3 rightNs 框选范围右边的时间 单位 ns
select
    IP.name as process,
    IP.pid,
    A.name as thread,
    A.tid,
    B.state,
    sum(B.dur) as wallDuration,
    avg(B.dur) as avgDuration,
    count(A.tid) as occurrences
from thread_state AS B
left join internal_thread as A on A.id = B.utid
left join trace_bounds AS TR
left join internal_process AS IP on IP.id=upid
where A.tid in (%s)
and not ((B.ts - TR.start_ts + B.dur < %d) or (B.ts - TR.start_ts > %d))
group by IP.name, IP.pid, A.name, A.tid, B.state
order by wallDuration desc;