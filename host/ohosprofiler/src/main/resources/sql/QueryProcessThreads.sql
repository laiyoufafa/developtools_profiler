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
                      the_tracks.upid,
                      the_tracks.utid,
                      total_dur as hasSched,
--                      hasHeapProfiles,
                      process.pid as pid,
                      thread.tid as tid,
                      process.name as processName,
                      thread.name as threadName
                    from (
--                      select upid, 0 as utid from process_track
--                      union
--                      select upid, 0 as utid from process_counter_track
--                      union
--                      select upid, utid from thread_counter_track join thread using(utid)
--                      union
--                      select upid, utid from thread_track join thread using(utid)
--                      union
                      select upid, utid from sched join thread using(utid) group by utid
--                      union
--                      select upid, utid from (
--                        select distinct(utid) from cpu_profile_stack_sample
--                      ) join thread using(utid)
--                      union
--                      select distinct(upid) as upid, 0 as utid from heap_profile_allocation
--                      union
--                      select distinct(upid) as upid, 0 as utid from heap_graph_object
                    ) the_tracks
                    left join (select upid, sum(dur) as total_dur
                      from sched join thread using(utid)
                      group by upid
                    ) using(upid)
--                    left join (
--                      select
--                        distinct(upid) as upid,
--                        true as hasHeapProfiles
--                      from heap_profile_allocation
--                      union
--                      select
--                        distinct(upid) as upid,
--                        true as hasHeapProfiles
--                      from heap_graph_object
--                   ) using (upid)
                    left join thread using(utid)
                    left join process using(upid)
                    order by
--                    hasHeapProfiles,
                      total_dur desc,
                      the_tracks.upid,
                      the_tracks.utid