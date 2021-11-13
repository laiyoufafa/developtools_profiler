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
select * from
    ( select ts as wakeTs,start_ts from instants,trace_bounds
       where name = 'sched_waking'
       and ref = %s
       and ts < start_ts + %s
       order by ts desc limit 1) TA
       left join
    (select ts as preRow from sched,trace_bounds
       where utid = %s
       and ts < start_ts + %s
       order by ts desc limit 1) TB