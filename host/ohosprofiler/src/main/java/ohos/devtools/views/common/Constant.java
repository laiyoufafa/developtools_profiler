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

import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;

import javax.swing.JTabbedPane;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description Constants
 * @version 1.0
 * @date 2021/3/4 10:55
 **/
public class Constant {
    /**
     * Default polling frequency.
     */
    public static final String POLLING_FREQUEBCY_DEFAULT = "0/1 * * * * ?";

    /**
     * The source file path pushed by the plugin
     */
    public static final String SOURCE_FILEPATH = "src/main/resources/";

    /**
     * Cpu plugin file name
     */
    public static final String CPU_PLUG_NAME = "Cpu";

    /**
     * Gpu plugin file name
     */
    public static final String GPU_PLUG_NAME = "Gpu";

    /**
     * Memory plugin file name
     */
    public static final String MEMORY_PLUG_NAME = "Memory";

    /**
     * Process plugin file name
     */
    public static final String PROCESS_PLUG_NAME = "Process";

    /**
     * The target file path of the plug-in push
     */
    public static final String TARGET_PLUG_PATH = "/data/local/tmp/hiprofiler/bin/";

    /**
     * File suffix pushed by the plugin
     */
    public static final String FILE_SUFFIX = ".so";

    /**
     * The default value of SAMPLE INTERVAL
     */
    public static final int SAMPLE_INTERVAL_DEFAULT = 1000;

    /**
     * STATUS
     */
    public static final int NORMAL_STATUS = 0;

    /**
     * Real-time scene
     */
    public static final int REALTIME_SCENE = 1;

    /**
     * File import scene
     */
    public static final int FILE_IMPORT_SCENE = 2;

    /**
     * MB
     */
    public static final int MB = 1024;

    /**
     * Abnormal state
     */
    public static final Long ABNORMAL = -1L;

    /**
     * Common tab
     */
    public static JTabbedPane jtasksTab = null;

    /**
     * Real-time refresh task name in the device selection box
     */
    public static final String DEVICEREFRESH = "deviceRefresh";

    /**
     * Maps stored in all process devices
     */
    public static Map<String, Map<DeviceIPPortInfo, ProcessInfo>> map = new HashMap<>();

    /**
     * File suffix pushed by the plugin
     */
    public static final String TRACE_SUFFIX = ".trace";

    /**
     * trace file path
     */
    public static String path = "";

    /**
     * trace文件保存路径
     */
    public static String tracePath = "";
}
