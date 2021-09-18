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

##设备实时动态表
CREATE TABLE DeviceIPPortInfo
(
    ID          integer primary key autoincrement not null,
    deviceID    varchar(100) not null,
    deviceName  varchar(100) NOT NULL,
    ip          varchar(100) ,
    deviceType  varchar(100) not null,
    connectType  varchar(100) not null,
    deviceStatus  int        not null,
    retryNum    int          not null,
    port        int          not null,
    forwardPort int          not null
);
