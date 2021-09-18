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
 * HdcCmdList
 */
public class HdcCmdList {
    /**
     * pluginPath
     */
    public static final String PLUGIN_PATH = SessionManager.getInstance().getHdcPath();

    /**
     * HDC LIST TARGETS STR
     */
    public static ArrayList<String> HDC_LIST_TARGETS_STR = new ArrayList<>() {
        private static final long serialVersionUID = -2441056417543656558L;
        {
            add(PLUGIN_PATH);
            add("list");
            add("targets");
            add("-v");
        }
    };

    /**
     * HDC GET TYPE
     */
    public static ArrayList<String> HDC_GET_TYPE = new ArrayList<>() {
        private static final long serialVersionUID = -2394024522865456503L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("shell");
            add("getprop");
            add("ro.product.cpu.abi");
        }
    };

    /**
     * HDC RUN OHOS
     */
    public static ArrayList<String> HDC_RUN_OHOS = new ArrayList<>() {
        private static final long serialVersionUID = -103210883565193436L;
        {
            add(PLUGIN_PATH);
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
            add("unzipStart");
            add("%s");
        }
    };

    /**
     * hdc start profilerd
     */
    public static ArrayList<String> HDC_START_PROFILERD = new ArrayList<>() {
        private static final long serialVersionUID = -6892425692255758759L;
        {
            add(PLUGIN_PATH);
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
            add("restart");
        }
    };

    /**
     * hdc start javaHeap
     */
    public static ArrayList<String> HDC_START_JAVAHEAP = new ArrayList<>() {
        private static final long serialVersionUID = -7953946430216933650L;
        {
            add(PLUGIN_PATH);
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
            add("startHeap");
            add("%s");
        }
    };

    /**
     * HDC CHECK SERVER
     */
    public static ArrayList<String> HDC_CHECK_SERVER = new ArrayList<>() {
        private static final long serialVersionUID = -6734438879494283706L;
        {
            add(PLUGIN_PATH);
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
            add("check_server");
        }
    };

    /**
     * HDC PUSH OHOS SHELL
     */
    public static ArrayList<String> HDC_PUSH_OHOS_SHELL = new ArrayList<>() {
        private static final long serialVersionUID = 7407520677035151364L;

        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("file");
            add("send");
            add("%s");
            add("/data/local/tmp/ohosprofiler");
        }
    };

    /**
     * HDC PUSH FILE SHELL
     */
    public static ArrayList<String> HDC_PUSH_FILE_SHELL = new ArrayList<>() {
        private static final long serialVersionUID = 118323663337186414L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("file");
            add("send");
            add("%s");
            add("%s");
        }
    };

    /**
     * HDC FOR PORT
     */
    public static ArrayList<String> HDC_FOR_PORT = new ArrayList<>() {
        private static final long serialVersionUID = -3646030545566427790L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("fport");
            add("tcp:%s");
            add("tcp:50051");
        }
    };

    /**
     * HDC PUSH CMD
     */
    public static ArrayList<String> HDC_PUSH_CMD = new ArrayList<>() {
        private static final long serialVersionUID = 7249365681950995900L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("file");
            add("send");
            add("%s");
            add("/data/local/tmp/developtools");
        }
    };

    /**
     * HDC CLEAR CMD
     */
    public static ArrayList<String> HDC_CLEAR_CMD = new ArrayList<>() {
        private static final long serialVersionUID = 7563399100805170044L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("shell");
            add("rm");
            add("-rf");
            add("/data/local/tmp/developtools/");
        }
    };

    /**
     * HDC ROOT CLEAR CMD
     */
    public static ArrayList<String> HDC_ROOT_CLEAR_CMD = new ArrayList<>() {
        private static final long serialVersionUID = 471178417841389616L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("shell");
            add("su 0 rm -rf /data/local/tmp/developtools/");
        }
    };

    /**
     * HDC GET PLUGIN MD5S
     */
    public static ArrayList<String> HDC_GET_PLUGIN_MD5S = new ArrayList<>() {
        private static final long serialVersionUID = 5290498389644237835L;
        {
            add(PLUGIN_PATH);
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
     * hdc create heapDump
     */
    public static ArrayList<String> HDC_CREATE_HEAPDUMP = new ArrayList<>() {
        private static final long serialVersionUID = 7647678596290819135L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("shell");
            add("cd");
            add("/data/local/tmp");
            add("&&");
            add("am");
            add("dumpheap");
            add("%s");
            add("%s");
        }
    };

    /**
     * hdc recV heapDump
     */
    public static ArrayList<String> HDC_RECV_HEAPDUMP = new ArrayList<>() {
        private static final long serialVersionUID = -8073939164877060309L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("file");
            add("recv");
            add("/data/local/tmp/%s");
            add("%s");
        }
    };

    /**
     * HDC GET TIME
     */
    public static ArrayList<String> HDC_GET_TIME = new ArrayList<>() {
        private static final long serialVersionUID = -564370535864629715L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("shell");
            add("date");
            add("+%s%N");
        }
    };

    /**
     * HDC HAS TRACE FILE INFO
     */
    public static ArrayList<String> HDC_HAS_TRACE_FILE_INFO = new ArrayList<>() {
        private static final long serialVersionUID = -6669823431200909892L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("shell");
            add("du");
            add("%s");
        }
    };

    /**
     * HDC PULL TRACE FILE
     */
    public static ArrayList<String> HDC_PULL_TRACE_FILE = new ArrayList<>() {
        private static final long serialVersionUID = -827799272692157779L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("file");
            add("recv");
            add("%s");
            add("%s");
        }
    };

    /**
     * HDC CHMOD PROC
     */
    public static ArrayList<String> HDC_CHMOD_PROC = new ArrayList<>() {
        private static final long serialVersionUID = -7606240047012790952L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("shell");
            add("su 0 chmod 777 /proc/stat");
        }
    };

    /**
     * HDC GET SimPERF FILE INFO
     */
    public static ArrayList<String> HDC_GET_SIMPER_FILE_INFO = new ArrayList<>() {
        private static final long serialVersionUID = -7258927152502785089L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("shell");
            add("du");
            add("%s");
        }
    };

    /**
     * HDC GET SimPERF FILE
     */
    public static ArrayList<String> HDC_GET_SIMPER_FILE = new ArrayList<>() {
        private static final long serialVersionUID = -4007861967368888669L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("file");
            add("recv");
            add("%s");
            add("%s");
        }
    };

    /**
     * HDC GET TRACE FILE INFO
     */
    public static ArrayList<String> HDC_GET_TRACE_FILE_INFO = new ArrayList<>() {
        private static final long serialVersionUID = -6950108406906264163L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("shell");
            add("du");
            add("%s");
        }
    };

    /**
     * HDC GET TRACE FILE
     */
    public static ArrayList<String> HDC_GET_TRACE_FILE = new ArrayList<>() {
        private static final long serialVersionUID = -1592462036082926191L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("file");
            add("recv");
            add("%s");
            add("%s");
        }
    };

    /**
     * HDC SHELL HiLOG
     */
    public static ArrayList<String> HDC_SHELL_HILOG = new ArrayList<>() {
        private static final long serialVersionUID = 2078981290462881684L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("shell");
            add("hilog");
        }
    };

    /**
     * HDC HiLOG
     */
    public static ArrayList<String> HDC_HILOG = new ArrayList<>() {
        private static final long serialVersionUID = -5609660539341002374L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("hilog");
        }
    };

    /**
     * HDC HiLOG C
     */
    public static ArrayList<String> HDC_HILOG_C = new ArrayList<>() {
        private static final long serialVersionUID = 8580129953374994618L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("shell");
            add("hilog");
            add("-c");
        }
    };

    /**
     * HDC HiLOG R
     */
    public static ArrayList<String> HDC_HILOG_R = new ArrayList<>() {
        private static final long serialVersionUID = 370115372353170396L;
        {
            add(PLUGIN_PATH);
            add("-t");
            add("%s");
            add("shell");
            add("hilog");
            add("-r");
        }
    };

    /**
     * TRACE STREAMER LOAD
     */
    public static ArrayList<String> TRACE_STREAMER_LOAD = new ArrayList<>() {
        private static final long serialVersionUID = -3159139340525115732L;
        {
            add("%s");
            add("%s");
            add("-e");
            add("%s");
        }
    };

    /**
     * Add permissions
     */
    public static ArrayList<String> CHMOD_TO_OHOS = new ArrayList<>() {
        private static final long serialVersionUID = -8550606962513461794L;
        {
            add("chmod");
            add("-R");
            add("777");
            add("%s");
        }
    };
}
