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
-- Query the asynchronous event method call information under the process
select tid,
    p.pid,
    A.name as threadName,
    is_main_thread,
    c.callid as track_id,
    c.ts-D.start_ts as startTs,
    c.dur,
    c.name as funName,
    c.cookie,
    c.parent_id,
    c.id,
    c.depth
from thread A,trace_range D
left join callstack C on A.id = C.callid
left join process p on A.ipid = p.id
where startTs not null  and c.cookie not null and p.pid = %s