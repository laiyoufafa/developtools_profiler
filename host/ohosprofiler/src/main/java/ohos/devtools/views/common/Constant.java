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

import com.intellij.ui.components.JBTabbedPane;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Constants
 */
public class Constant {
    /**
     * develop mode
     */
    public static final boolean IS_DEVELOP_MODE = false;

    /**
     * IS_SUPPORT_NEW_HDC
     */
    public static final boolean IS_SUPPORT_NEW_HDC = true;

    /**
     * Common tab
     */
    public static JBTabbedPane jtasksTab = null;

    /**
     * Real-time refresh task name in the device selection box
     */
    public static final String DEVICE_REFRESH = "deviceRefresh";

    /**
     * Real-time refresh task name in the device selection box
     */
    public static final String DISTRIBUTED_REFRESH = "DistributedRefresh";

    /**
     * Maps stored in all process devices
     */
    public static Map<String, Map<DeviceIPPortInfo, ProcessInfo>> map = new HashMap<>();

    /**
     * File suffix pushed by the plugin
     */
    public static final String TRACE_SUFFIX = ".trace";

    /**
     * treeTable init count
     */
    public static int MEMORY_AGENT_INIT_COUNT = 1000;

}
