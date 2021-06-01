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
 * @Description constant
 * @Date 2021/3/4 15:58
 **/
public class Constant {
    /**
     * Destination path
     */
    public static final String DEST_PATH = "/data/local/tmp";

    /**
     * Devtools plug-in (V8) path
     */
    public static final String DEVTOOLS_PLUGINS_V8_PATH = "devtools.tar";

    /**
     * Devtools plug-in (V7) path
     */
    public static final String DEVTOOLS_PLUGINS_V7_PATH = "devtools.tar";

    /**
     * Unzip shell plug-in path
     */
    public static final String UNZIP_SHELL_PLUGINS_PATH = "ohosprofiler";

    /**
     * File name
     */
    public static final String FILE_NAME = "hiprofilerd";

    /**
     * Plug-in name
     */
    public static final String HIPRO_FILER_NAME = "hiprofiler_cmd";

    /**
     * Plug-in name
     */
    public static final String PLUGINS_NAME = "hiprofiler_plugins";

    /**
     * HIPRO filer command name
     */
    public static final String HIPRO_FILER_CMDNAME = "hiprofiler_cmd -q";

    /**
     * HIPRO filer result: OK
     */
    public static final String HIPRO_FILER_RESULT_OK = "OK";

    /**
     * Installation result: success
     */
    public static final String INSTALL_SUCCESS = "Success";

    /**
     * Device state: online
     */
    public static final String DEVICE_STAT_ONLINE = "device";

    /**
     * Device SATA state: pushed
     */
    public static final String DEVICE_SATA_STAT_PUSHED = "pushed";

    /**
     * Device state: error
     */
    public static final String DEVICE_STAT_ERROR = "error";

    /**
     * Device state: closed
     */
    public static final String DEVICE_STAT_CLOSED = "closed";

    /**
     * Device state: not found
     */
    public static final String DEVICE_STAT_NOT_FOUND = "not found";

    /**
     * Device state: FAIL
     */
    public static final String DEVICE_STAT_FAIL = "FAIL";

    /**
     * Device state: offline
     */
    public static final String DEVICE_STAT_OFFLINE = "offline";

    /**
     * Device state: unauthorized
     */
    public static final String DEVICE_STST_UNAUTHORIZED = "unauthorized";

    /**
     * Device full type
     */
    public static final String DEVICE_FULL_TYPE = "arm64-v8a";

    /**
     * Device lean type
     */
    public static final String DEVICE_LEAN_TYPE = "arm64-v7a";

    /**
     * Memory plug-in file name source file path of plug-in push
     */
    public static final String SOURCE_FILEPATH = "src/main/resources/plugins/";

    /**
     * Cpu plug-in file name
     */
    public static final String CPU_PLUG_NAME = "Cpu";

    /**
     * Gpu plug-in file name
     */
    public static final String GPU_PLUG_NAME = "Gpu";

    /**
     * Process plug in file name
     */
    public static final String PROCESS_PLUG_NAME = "Process";

    /**
     * Target file path of plug-in push
     */
    public static final String TARGET_PLUG_PATH = "/data/local/tmp/";

    /**
     * File suffix pushed by plug-in
     */
    public static final String FILE_SUFFIX = ".so";

    /**
     * default sampling interval
     */
    public static final int SAMPLE_INTERVAL_DEFAULT = 1000;

    /**
     * Radix for conversion from BigInteger to string
     */
    public static final int RADIX = 16;

    /**
     * End side return to normal flag
     */
    public static final int NORMAL_STATUS = 0;

    /**
     * File importing scene (1: real-time)
     */
    public static final int REALTIME_SCENE = 1;

    /**
     * File importing scene (2: real-time)
     */
    public static final int FILE_IMPORT_SCENE = 2;

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

    private Constant() {
    }
}
