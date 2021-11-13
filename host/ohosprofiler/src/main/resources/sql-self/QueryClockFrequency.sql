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
with freq as (  select measure.filter_id, measure.ts, measure.type, measure.value from clock_event_filter
left join measure
where clock_event_filter.name = '%s' and clock_event_filter.type = 'clock_set_rate' and clock_event_filter.id = measure.filter_id
order by measure.ts)
select freq.filter_id,freq.ts - r.start_ts as ts,freq.type,freq.value from freq,trace_range r;