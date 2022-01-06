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

package ohos.devtools.datasources.transport.hdc;

import ohos.devtools.datasources.utils.session.service.SessionManager;

import java.util.ArrayList;

/**
 * HdcStdCmdList
 */
public class HdcStdCmdList {
    /**
     * pluginPath
     */
    public static String pluginPath = SessionManager.getInstance().getHdcStdPath();

    /**
     * HDC STD LIST TARGETS STR
     */
    public static final ArrayList<String> HDC_STD_LIST_TARGETS_STR = new ArrayList<>() {
        private static final long serialVersionUID = 7328508897834721709L;
        {
            add(pluginPath);
            add("list");
            add("targets");
            add("-v");
        }
    };

    /**
     * HDC STD RUN OHOS
     */
    public static final ArrayList<String> HDC_STD_RUN_OHOS = new ArrayList<>() {
        private static final long serialVersionUID = -1497892508289310831L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("shell");
            add("cd");
            add("/data/local/tmp");
            add("&&");
            add("chmod");
            add("+x");
            add("ohosprofiler");
            add("&&");
            add("sh");
            add("ohosprofiler");
            add("untarStart");
            add("%s");
        }
    };

    /**
     * HDC STD START PROFILERD
     */
    public static final ArrayList<String> HDC_STD_START_PROFILER = new ArrayList<>() {
        private static final long serialVersionUID = -7496488704314430824L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("shell");
            add("cd");
            add("/data/local/tmp");
            add("&&");
            add("chmod");
            add("+x");
            add("ohosprofiler");
            add("&&");
            add("sh");
            add("ohosprofiler");
            add("start_std_daemon");
        }
    };

    /**
     * HDC STD CHECK SERVER
     */
    public static final ArrayList<String> HDC_STD_CHECK_SERVER = new ArrayList<>() {
        private static final long serialVersionUID = 2946832952090077766L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("shell");
            add("cd");
            add("/data/local/tmp");
            add("&&");
            add("chmod");
            add("+x");
            add("ohosprofiler");
            add("&&");
            add("sh");
            add("ohosprofiler");
            add("check_std_server");
        }
    };

    /**
     * HDC STD REMOVE Shell
     */
    public static final ArrayList<String> HDC_STD_REMOVE_SHELL = new ArrayList<>() {
        private static final long serialVersionUID = 2946832952090077766L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("shell");
            add("cd");
            add("/data/local/tmp");
            add("&&");
            add("rm");
            add("-f");
            add("ohosprofiler");
        }
    };

    /**
     * HDC STD PUSH OHOS SHELL
     */
    public static final ArrayList<String> HDC_STD_PUSH_OHOS_SHELL = new ArrayList<>() {
        private static final long serialVersionUID = 4164605095978787447L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("file");
            add("send");
            add("%s");
            add("/data/local/tmp/ohosprofiler");
        }
    };

    /**
     * HDC STD PUSH FILE SHELL
     */
    public static final ArrayList<String> HDC_STD_PUSH_FILE_SHELL = new ArrayList<>() {
        private static final long serialVersionUID = 1061960274001695519L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("file");
            add("send");
            add("%s");
            add("%s");
        }
    };

    /**
     * HDC STD FOR PORT
     */
    public static final ArrayList<String> HDC_STD_FOR_PORT = new ArrayList<>() {
        private static final long serialVersionUID = -7030468377917652399L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("fport");
            add("tcp:%s");
            add("tcp:50051");
        }
    };

    /**
     * HDC STD PUSH CMD
     */
    public static final ArrayList<String> HDC_STD_PUSH_CMD = new ArrayList<>() {
        private static final long serialVersionUID = 8398848389297819410L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("file");
            add("send");
            add("%s");
            add("/data/local/tmp/stddeveloptool.tar");
        }
    };

    /**
     * HDC STD ROOT CLEAR CMD
     */
    public static final ArrayList<String> HDC_STD_ROOT_CLEAR_CMD = new ArrayList<>() {
        private static final long serialVersionUID = 6514979196500138508L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("shell");
            add("rm -rf /data/local/tmp/stddeveloptools/");
        }
    };

    /**
     * HDC STD ROOT CLEAR HTRACE COMMAND FILE
     */
    public static final ArrayList<String> HDC_STD_ROOT_CLEAR_COMMAND = new ArrayList<>() {
        private static final long serialVersionUID = 6514979196500138508L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("shell");
            add("rm");
            add("-f");
            add("/data/local/tmp/command*.txt");
        }
    };

    /**
     * HDC STD GET PLUGIN MD5S
     */
    public static final ArrayList<String> HDC_STD_GET_PLUGIN_MD5S = new ArrayList<>() {
        private static final long serialVersionUID = 3619030319143131356L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("shell");
            add("cd");
            add("/data/local/tmp/developtools/");
            add("&&");
            add("md5sum * | grep -v \"developtools\"");
        }
    };

    /**
     * HDC STD GET TIME
     */
    public static final ArrayList<String> HDC_STD_GET_TIME = new ArrayList<>() {
        private static final long serialVersionUID = 1547277822359912982L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("shell");
            add("date");
            add("+%s%N");
        }
    };

    /**
     * HDC STD HAS TRACE FILE INFO
     */
    public static final ArrayList<String> HDC_STD_HAS_TRACE_FILE_INFO = new ArrayList<>() {
        private static final long serialVersionUID = 7222695108774866461L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("shell");
            add("du");
            add("%s");
        }
    };

    /**
     * HDC STD PULL TRACE FILE
     */
    public static final ArrayList<String> HDC_STD_PULL_TRACE_FILE = new ArrayList<>() {
        private static final long serialVersionUID = -1262290397066935333L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("file");
            add("recv");
            add("%s");
            add("%s");
        }
    };

    /**
     * HDC STD CHMOD PROC
     */
    public static final ArrayList<String> HDC_STD_CHMOD_PROC = new ArrayList<>() {
        private static final long serialVersionUID = 4523522563780898863L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("shell");
            add("chmod");
            add("777");
            add("/proc/stat");
        }
    };

    /**
     * HDC STD GET SimPERF FILE INFO
     */
    public static final ArrayList<String> HDC_STD_GET_SIMPERF_FILE_INFO = new ArrayList<>() {
        private static final long serialVersionUID = -6777810598852359424L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("shell");
            add("du");
            add("%s");
        }
    };

    /**
     * HDC STD GET SimPERF FILE
     */
    public static final ArrayList<String> HDC_STD_GET_SIMPERF_FILE = new ArrayList<>() {
        private static final long serialVersionUID = -7999933171772577521L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("file");
            add("recv");
            add("%s");
            add("%s");
        }
    };

    /**
     * HDC STD GET TRACE FILE INFO
     */
    public static final ArrayList<String> HDC_STD_GET_TRACE_FILE_INFO = new ArrayList<>() {
        private static final long serialVersionUID = -5460775654047993957L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("shell");
            add("du");
            add("%s");
        }
    };

    /**
     * HDC STD GET TRACE FILE
     */
    public static final ArrayList<String> HDC_STD_GET_TRACE_FILE = new ArrayList<>() {
        private static final long serialVersionUID = 4766180836351032855L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("file");
            add("recv");
            add("%s");
            add("%s");
        }
    };

    /**
     * HDC STD SHELL HiLOG
     */
    public static final ArrayList<String> HDC_STD_SHELL_HI_LOG = new ArrayList<>() {
        private static final long serialVersionUID = -8828490298923994828L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("shell");
            add("hilog");
        }
    };

    /**
     * HDC STD HiLOG R
     */
    public static final ArrayList<String> HDC_STD_HILOG_R = new ArrayList<>() {
        private static final long serialVersionUID = -8297376293467393930L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("shell");
            add("hilog");
            add("-r");
        }
    };

    /**
     * HDC STD START NATIVE HOOK
     */
    public static final ArrayList<String> HDC_STD_START_NATIVE_HOOK = new ArrayList<>() {
        private static final long serialVersionUID = 5995494856526394813L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("shell");
            add("kill");
            add("-36");
            add("%s");
        }
    };

    /**
     * HDC STD STOP NATIVE HOOK
     */
    public static final ArrayList<String> HDC_STD_STOP_NATIVE_HOOK = new ArrayList<>() {
        private static final long serialVersionUID = -6856650349955834515L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("shell");
            add("kill");
            add("-37");
            add("%s");
        }
    };

    /**
     * HDC STD PULL FILE
     */
    public static final ArrayList<String> HDC_STD_PULL_FILE = new ArrayList<>() {
        private static final long serialVersionUID = -7214023004212969508L;
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("file");
            add("recv");
            add("%s");
            add("%s");
        }
    };


    /**
     * HDC STD CHECK FPROT
     */
    public static final ArrayList<String> HDC_STD_CHECK_FPORT = new ArrayList<>() {
        {
            add(pluginPath);
            add("-t");
            add("%s");
            add("fport");
            add("ls");
        }
    };

    /**
     * HDC_GET_DEVICES
     */
    public static final ArrayList<String> HDC_STD_GET_DEVICES = new ArrayList<>() {
        {
            add(pluginPath);
            add("list targets");
            add("|");
            add("%s");
            add("%s");
        }
    };

    /**
     * HDC RESET
     */
    public static final ArrayList<String> HDC_STD_RESET = new ArrayList<>() {
        {
            add(pluginPath);
            add("kill -r");
        }
    };
}
