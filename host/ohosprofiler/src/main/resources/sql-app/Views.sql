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

CREATE VIEW IF NOT EXISTS thread AS SELECT id as utid, * FROM internal_thread;
CREATE VIEW IF NOT EXISTS process AS SELECT id as upid, * FROM internal_process;
CREATE VIEW IF NOT EXISTS sched AS SELECT *, ts + dur as ts_end FROM sched_slice;
CREATE VIEW IF NOT EXISTS instants AS SELECT *, 0.0 as value FROM instant;
