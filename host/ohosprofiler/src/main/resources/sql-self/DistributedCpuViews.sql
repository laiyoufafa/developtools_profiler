*
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
DROP VIEW  if exists cpu_frequency_view;
CREATE VIEW cpu_frequency_view AS
SELECT
  B.cpu,
  A.ts as start_ts,
  LEAD(A.ts, 1, (SELECT end_ts from trace_range))
    OVER (PARTITION by A.filter_id ORDER BY ts) AS end_ts,
  LEAD(A.ts, 1, (SELECT end_ts from trace_range))
    OVER (PARTITION by A.filter_id ORDER BY ts) - ts AS dur,
  value as freq
FROM measure  AS A, cpu_measure_filter AS B
WHERE B.name = 'cpu_frequency' and A.filter_id=B.id;

DROP VIEW  if exists cpu_thread_view;
CREATE VIEW cpu_thread_view AS
SELECT S.ts,
       S.ts + S.dur as end_ts,
       S.cpu,
       T.ipid,
       S.itid AS itid,
       P.pid as pid,
       T.name AS thread_name,
       P.name AS process_name
FROM thread AS T, sched_slice AS S, process as P
where T.id = S.itid and T.ipid=P.id;

DROP VIEW  if exists tmp;
CREATE VIEW tmp AS
SELECT (MIN(cpu_frequency_view.end_ts, cpu_thread_view.end_ts) - MAX(cpu_frequency_view.start_ts, cpu_thread_view.ts)) AS duration,
             freq,
             cpu_thread_view.cpu as cpu,
             itid,
             ipid,
             process_name,
             thread_name
      FROM cpu_frequency_view JOIN cpu_thread_view ON(cpu_frequency_view.cpu = cpu_thread_view.cpu)
      WHERE cpu_frequency_view.start_ts < cpu_thread_view.end_ts AND cpu_frequency_view.end_ts > cpu_thread_view.ts;

DROP VIEW  if exists cpu_per_thread;
-- CPU info aggregated per CPU and thread.
CREATE VIEW cpu_per_thread AS
SELECT itid,
       ipid,
       cpu,
       CAST(SUM(duration) AS INT) AS duration,
       CAST(MIN(freq) AS INT) AS min_freq,
       CAST(MAX(freq) AS INT) AS max_freq,
       CAST((SUM(duration * freq) / SUM(duration)) AS INT) AS avg_frequency,
       process_name,
       thread_name
FROM tmp
GROUP BY itid, cpu;