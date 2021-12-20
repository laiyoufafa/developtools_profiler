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

with list as (
    select distinct name from clock_event_filter
    where  clock_event_filter.type = 'clock_set_rate' order by name
),freq as(
    select measure.filter_id, measure.ts, measure.type, measure.value , clock_event_filter.name from clock_event_filter
    left join measure
    where  clock_event_filter.type = 'clock_set_rate' and clock_event_filter.id = measure.filter_id
    order by measure.ts
),state as (
    select filter_id, ts, endts, endts-ts as dur, type, value,name from
    (select measure.filter_id, measure.ts, lead(ts, 1, null) over( order by measure.ts) endts, measure.type, measure.value,clock_event_filter.name from clock_event_filter,trace_section
    left join measure
    where clock_event_filter.type != 'clock_set_rate' and clock_event_filter.id = measure.filter_id
    order by measure.ts)
),count_freq as (
    select COUNT(*) num,name srcname from freq group by name
),count_state as (
    select COUNT(*) num,name srcname from state group by name
)
select count_freq.srcname||' Frequency' as name,* from count_freq union select count_state.srcname||' State' as name,* from count_state order by name;
