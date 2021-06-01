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

##设备详情信息表
CREATE TABLE DeviceInfo
(
    ID         integer primary key autoincrement NOT NULL,
    deviceID   varchar(50)  NOT NULL,
    deviceName varchar(100) NOT NULL,
    ramInfo    varchar(100),
    romInfo    varchar(100)
);
##进程列表
CREATE TABLE ProcessInfo
(
    processId   integer primary key not null,
    deviceId    varchar(50)         not null,
    processName varchar(100),
    state       integer(50),
    startTime   integer(50),
    arch        varchar(100),
    agentStatus varchar(100)
);

##设备实时动态表
CREATE TABLE DeviceIPPortInfo
(
    ID          integer primary key autoincrement not null,
    deviceID    varchar(100) not null,
    deviceName  varchar(100) NOT NULL,
    ip          varchar(100) not null,
    deviceType  varchar(100) not null,
    port        int          not null,
    forwardPort int          not null
);

##监控项配置表
CREATE TABLE MonitorInfo
(
    localSessionId INT PRIMARY KEY NOT NULL,
    monitorType    varchar(64)     NOT NULL,
    parameter      varchar(64)     NOT NULL,
    value          varchar(256) DEFAULT ''
);
##自研插件表
CREATE TABLE HiProfilerPlugin
(
    deviceId       INT PRIMARY KEY NOT NULL,
    plugId         INT             NOT NULL,
    name           varchar(64)     NOT NULL,
    statue         INT             NOT NULL,
    version        INT             NOT NULL,
    plugSha256     varchar(256)    NOT NULL,
    sampleInterval INT             NOT NULL,
    configData     varbinary(500)
);
