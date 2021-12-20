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

package ohos.devtools.views.layout.chartview;

import ohos.devtools.views.common.ColorConstants;

import java.awt.Color;

/**
 * 监控项详情，各个二级界面监控项的监控项
 *
 * @since 2021/11/22
 */
public enum MonitorItemDetail {
    /**
     * 内存监控项：Java
     */
    CPU_APP(1, "App", ColorConstants.MEM_JAVA),

    /**
     * 内存监控项：Native
     */
    CPU_SYSTEM(0, "System", ColorConstants.MEM_NATIVE),

    /**
     * 内存监控项：Java
     */
    MEM_JAVA(0, "Java", ColorConstants.MEM_JAVA),

    /**
     * 内存监控项：Native
     */
    MEM_NATIVE(1, "Native", ColorConstants.MEM_NATIVE),

    /**
     * 内存监控项：Graphics
     */
    MEM_GRAPHICS(2, "Graphics", ColorConstants.MEM_GRAPHICS),

    /**
     * 内存监控项：Stack
     */
    MEM_STACK(3, "Stack", ColorConstants.MEM_STACK),

    /**
     * 内存监控项：Code
     */
    MEM_CODE(4, "Code", ColorConstants.MEM_CODE),

    /**
     * 内存监控项：Others
     */
    MEM_OTHERS(5, "Others", ColorConstants.MEM_OTHERS),

    /**
     * Network监控项：received
     */
    NETWORK_RCV(0, "Receiving", ColorConstants.NETWORK_RCV),

    /**
     * Network监控项：Sent
     */
    NETWORK_SENT(1, "Sending", ColorConstants.NETWORK_SENT),

    /**
     * Network监控项：connections
     */
    NETWORK_CONN(2, "Connections", ColorConstants.NETWORK_CONN),

    /**
     * Energy monitoring item: CPU
     */
    ENERGY_CPU(1, "CPU", ColorConstants.ENERGY_CPU),

    /**
     * Energy monitoring item: Network
     */
    ENERGY_NETWORK(2, "Network", ColorConstants.ENERGY_NETWORK),

    /**
     * Energy monitoring item: Location
     */
    ENERGY_LOCATION(3, "Location", ColorConstants.ENERGY_LOCATION),

    /**
     * Energy monitoring item: System event Location
     */
    ENERGY_EVENT_LOCATION(1, "Event Location", ColorConstants.ENERGY_EVENT_LOCATION),

    /**
     * Energy monitoring item: System event Wake Locks
     */
    ENERGY_EVENT_LOCKS(2, "Wake Locks", ColorConstants.ENERGY_EVENT_LOCKS),

    /**
     * Energy monitoring item: System event Alarms&Jobs
     */
    ENERGY_EVENT_ALARMS_JOBS(3, "Alarms&Jobs", ColorConstants.ENERGY_EVENT_ALARMS_JOBS),

    /**
     * diskIO：Read
     */
    DISK_IO_READ(0, "DiskIO_read", ColorConstants.DISK_IO_READ),

    /**
     * diskIO：write
     */
    DISK_IO_WRITE(1, "DiskIO_write", ColorConstants.DISK_IO_WRITE),

    /**
     * 未知项
     */
    UNRECOGNIZED(-1, "Unrecognized", null);

    private final int index;
    private final String name;
    private final Color color;

    MonitorItemDetail(int index, String name, Color color) {
        this.index = index;
        this.name = name;
        this.color = color;
    }

    /**
     * 通过名称获取监控项详情
     *
     * @param name 名称
     * @return 监控项详情
     */
    public static MonitorItemDetail getItemByName(String name) {
        MonitorItemDetail result = UNRECOGNIZED;
        for (MonitorItemDetail item : MonitorItemDetail.values()) {
            if (item.getName().equals(name)) {
                result = item;
            }
        }
        return result;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }
}
