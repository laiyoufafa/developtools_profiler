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

select tid,
    A.start_ts,
    A.end_ts,
    A.name as threadName,
    is_main_thread,
    c.callid as track_id,
    c.ts-D.start_ts as startTs,
    c.ts + c.dur as endTs,
    c.dur,
    c.name as funName,
    c.depth,
   c.parent_id,
   c.id
from thread A,trace_section D
left join callstack C on A.id = C.callid
where startTs not null and A.tid = ${tid};