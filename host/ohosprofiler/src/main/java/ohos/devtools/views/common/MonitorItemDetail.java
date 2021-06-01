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

package ohos.devtools.views.common;

import java.awt.Color;

/**
 * 监控项详情，各个二级界面监控项的监控项
 *
 * @since 2021/3/1 16:50
 */
public enum MonitorItemDetail {
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
    NETWORK_RCV(1, "received", ColorConstants.NETWORK_RCV),

    /**
     * Network监控项：Sent
     */
    NETWORK_SENT(1, "Sent", ColorConstants.NETWORK_SENT),

    /**
     * Network监控项：connections
     */
    NETWORK_CONN(1, "connections", ColorConstants.NETWORK_CONN),

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
