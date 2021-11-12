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
with cpu as (
    select cpu,ts,dur,(case when ro < 99 then ro else 99 end) as ro ,
           (case when ro < 99 then stime+ro*cell else stime + 99 * cell end) as st,
           (case when ro < 99 then stime + (ro+1)*cell else etime end) as et
    from (
        select cpu,ts,A.dur,((ts+A.dur)-D.start_ts)/((D.end_ts-D.start_ts)/100) as ro,D.start_ts as stime,D.end_ts etime,(D.end_ts-D.start_ts)/100 as cell
        from sched_slice A
        left join trace_bounds D
        left join internal_thread B on A.utid = B.id
        left join internal_process C on B.upid = C.id
        where tid != 0 ))
select cpu,ro,
       sum(case
               when ts <= st and ts + dur <= et then (ts + dur - st)
               when ts <= st and ts + dur > et then et-st
               when ts > st and ts + dur <= et then dur
               when ts > st and ts + dur > et then et - ts end)/cast(et-st as float) as rate
from cpu
group by cpu,ro