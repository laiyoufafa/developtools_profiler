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
select A.*,A.ipid as upid,B.itid as utid, B.cpu, B.ts-TR.start_ts AS startTime,B.dur,B.state,IP.pid,IP.name as processName
                from thread_state AS B
                left join thread as A
                left join trace_range AS TR
                left join process AS IP on IP.id=A.ipid
                where A.id=B.itid and tid = %s;