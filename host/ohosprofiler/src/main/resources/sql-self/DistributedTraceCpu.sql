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
       itid as tid,
       ipid as pid,
       group_concat(cpu,",") as cpu,
       group_concat(duration,",") as duration,
       group_concat(min_freq,",") as min_freq,
       group_concat(max_freq,",") as max_freq,
       group_concat(avg_frequency,",") as avg_frequency,
       process_name,
       thread_name
    from
    (
       SELECT itid,
       ipid,
       cpu,
       CAST(SUM(duration) AS INT) AS duration,
       CAST(MIN(freq) AS INT) AS min_freq,
       CAST(MAX(freq) AS INT) AS max_freq,
       CAST((SUM(duration * freq) / SUM(duration)) AS INT) AS avg_frequency,
       process_name,
       thread_name
       FROM (SELECT (MIN(cpu_frequency_view.end_ts, cpu_thread_view.end_ts) - MAX(cpu_frequency_view.start_ts, cpu_thread_view.ts)) AS duration,
             freq,
             cpu_thread_view.cpu as cpu,
             itid,
             ipid,
             process_name,
             thread_name
      FROM cpu_frequency_view JOIN cpu_thread_view ON(cpu_frequency_view.cpu = cpu_thread_view.cpu)
      WHERE cpu_frequency_view.start_ts < cpu_thread_view.end_ts AND cpu_frequency_view.end_ts > cpu_thread_view.ts
      ) GROUP BY itid, cpu
     )
GROUP BY ipid, itid order by ipid