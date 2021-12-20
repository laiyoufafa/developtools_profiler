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
  the_tracks.ipid as upid,
  the_tracks.itid as utid,
  total_dur as hasSched,
  process_view.pid as pid,
  thread_view.tid as tid,
  process_view.name as processName,
  thread_view.name as threadName
from (
  select ipid, itid from sched_view join thread_view using(itid) group by itid
) the_tracks
left join (select ipid, sum(dur) as total_dur
  from sched_view join thread_view using(itid)
  group by ipid
) using(ipid)
left join thread_view using(itid)
left join process_view using(ipid)
order by
  total_dur desc,
  the_tracks.ipid,
  the_tracks.itid