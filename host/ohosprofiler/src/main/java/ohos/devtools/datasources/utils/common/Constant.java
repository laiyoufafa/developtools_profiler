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

package ohos.devtools.datasources.utils.common;

/**
 * constant
 */
public class Constant {
    /**
     * Devtools plug-in (V8) path
     */
    public static final String DEVTOOLS_PLUGINS_V8_PATH = "developtools";

    /**
     * Devtools plug-in (V7) path
     */
    public static final String DEVTOOLS_PLUGINS_V7_PATH = "developtool.tar";

    /**
     * Unzip shell plug-in path
     */
    public static final String UNZIP_SHELL_PLUGINS_PATH = "ohosprofiler";

    /**
     * File name
     */
    public static final String FILE_NAME = "hiprofilerd";

    /**
     * HIPRO filer result: OK
     */
    public static final String PLUGIN_RESULT_OK = "OK";

    /**
     * need to update Version
     */
    public static final String UPDATE_PLUGIN = "UPDATE PLUGIN";

    /**
     * Device state: not found
     */
    public static final String PLUGIN_NOT_FOUND = "not found";

    /**
     * Device SATA state: pushed
     */
    public static final String DEVICE_SATA_STAT_PUSHED = "pushed";

    /**
     * Device state: FAIL
     */
    public static final String DEVICE_STAT_FAIL = "FAIL";

    /**
     * Device state: offline
     */
    public static final String DEVICE_STAT_OFFLINE = "offline";

    /**
     * Radix for conversion from BigInteger to string
     */
    public static final int RADIX = 16;

    /**
     * size
     */
    public static final int MB = 1024;

    /**
     * Abnormal state
     */
    public static final Long ABNORMAL = -1L;

    /**
     * Memomy plug-in
     */
    public static final String MEMORY_PLUG = "memory-plugin";

    /**
     * JVMTI agent plug-in
     */
    public static final String JVMTI_AGENT_PLUG = "jvmtiagent";

    /**
     * memory plug
     */
    public static final String MEMORY_PLUGS_NAME = "/data/local/tmp/libmemdataplugin.z.so";

    /**
     * memory plug name
     */
    public static final String MEMORY_PLUGS = "libmemdataplugin";

    /**
     * cpu plug
     */
    public static final String CPU_PLUGS_NAME = "/data/local/tmp/libcpudataplugin.z.so";

    /**
     * Cpu plug-in
     */
    public static final String CPU_PLUG = "cpu-plugin";

    /**
     * cpu plug
     */
    public static final String ENERGY_PLUGS_NAME = "/data/local/tmp/libcpudataplugin.z.so";

    private Constant() {
    }
}
