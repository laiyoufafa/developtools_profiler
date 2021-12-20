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

with tracks as (
    select process_measure_filter.id as trackId,
                process_measure_filter.name as trackName, ipid, process_view.pid,
                process_view.name as processName, process_view.start_ts as startTs
                from process_measure_filter
                join process_view using(ipid)
                where trackName='VSYNC-app'
    order by trackName
)
select c.*,c.filter_id as track_id,c.ts-tb.start_ts startTime from measure c,trace_section tb where filter_id in (select tracks.trackId from tracks);