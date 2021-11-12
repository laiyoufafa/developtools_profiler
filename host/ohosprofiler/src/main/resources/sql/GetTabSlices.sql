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
--数 1 tids 数组，框选范围中选中的 thread id 集合
--参数 2 leftNs 框选范围左边的时间，单位 ns
--参数 3 rightNs 框选范围右边的时间 单位 ns
select
      c.name as funName,
      sum(c.dur) as wallDuration,
      avg(c.dur) as avgDuration,
      count(c.name) as occurrences
from internal_thread A,trace_bounds D
left join thread_track B on A.id = B.utid
left join internal_slice C on B.id = C.track_id
where C.ts not null
      and A.tid in (%s)
      and not ((C.ts - D.start_ts + C.dur < %d) or (C.ts - D.start_ts > %d))
group by c.name
order by wallDuration desc;