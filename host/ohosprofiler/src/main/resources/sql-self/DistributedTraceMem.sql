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
select max(value) as maxNum,min(value) as minNum,result.name,result.processName from measure inner join
(
select filter.id,filter.name,p.name as processName from process_measure_filter as filter
left join process as p
on filter.ipid=p.id where filter.name = "mem.rss.anon"
) as result on result.id = filter_id
where filter_id > 0 group by filter_id