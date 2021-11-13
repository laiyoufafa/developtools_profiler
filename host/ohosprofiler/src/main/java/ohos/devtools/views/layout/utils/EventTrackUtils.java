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

package ohos.devtools.views.layout.utils;

import com.alibaba.fastjson.JSONObject;
import com.huawei.deveco.common.trace.HarmonyCustomTopic;
import com.huawei.deveco.common.trace.TraceDataBean;
import com.huawei.deveco.common.trace.TraceUtil;

/**
 * EventTrackUtils
 *
 * @since 2021/10/26
 */
public class EventTrackUtils {
    private static final String SID = "10015";
    private static final String ACTION_HOME = "Home";
    private static final String ACTION_HOME_APPLICATION = "Home_Application";
    private static final String ACTION_HOME_SYSTEM = "Home_System";
    private static final String ACTION_HOME_DISTRIBUTED = "Home_Distributed";
    private static final String EVENT_WELCOME_PAGE = "Welcome page";
    private static final String EVENT_TASK_PAGE = "Task page";
    private static final String EVENT_LOG_PAGE = "HiLog page";
    private static final String EVENT_LOG_SWITCH = "LogSwitch";
    private static final String EVENT_HELP = "Help";
    private static final String EVENT_APPLICATION_CONFIG_PAGE = "Config page";
    private static final String EVENT_APPLICATION_CHART_PAGE = "Chart page";
    private static final String EVENT_APPLICATION_CPU = "CPU";
    private static final String EVENT_APPLICATION_TRACE = "Application Trace";
    private static final String EVENT_APPLICATION_PERF_TRACE = "Perf Trace";
    private static final String EVENT_APPLICATION_MEMORY = "Memory";
    private static final String EVENT_APPLICATION_HPROF = "Hprof";
    private static final String EVENT_APPLICATION_DISK_IO = "DiskIO";
    private static final String EVENT_APPLICATION_NETWORK = "NetWork";
    private static final String EVENT_APPLICATION_ENERGY = "Energy";
    private static final String EVENT_APPLICATION_APP = "Application";
    private static final String EVENT_SYSTEM_CONFIG = "config page";
    private static final String EVENT_SYSTEM_TRACE_PAGE = "Trace page";
    private static final String EVENT_DISTRIBUTED_CONFIG = "Config page";
    private static final String EVENT_DISTRIBUTED_PAGE = "Distributed page";
    private static final String JSON_TRIGGER_TIME_STR = "trigger_time";
    private static final String JSON_EVENT_NAME_STR = "event_name";
    private static EventTrackUtils eventTrackUtils;

    /**
     * getInstance
     *
     * @return EventTrackUtils
     */
    public static EventTrackUtils getInstance() {
        if (eventTrackUtils == null) {
            synchronized (EventTrackUtils.class) {
                if (eventTrackUtils == null) {
                    eventTrackUtils = new EventTrackUtils();
                }
            }
        }
        return eventTrackUtils;
    }

    /**
     * trackWelcomePage
     */
    public void trackWelcomePage() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_WELCOME_PAGE);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }

    /**
     * trackTaskPage
     */
    public void trackTaskPage() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_TASK_PAGE);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }

    /**
     * trackHiLogPage
     */
    public void trackHiLogPage() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_LOG_PAGE);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }

    /**
     * trackLogSwitch
     */
    public void trackLogSwitch() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_LOG_SWITCH);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }

    /**
     * trackHelp
     */
    public void trackHelp() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_HELP);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }

    /**
     * trackApplicationConfigPage
     */
    public void trackApplicationConfigPage() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_APPLICATION_CONFIG_PAGE);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME_APPLICATION, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }

    /**
     * trackApplicationChartPage
     */
    public void trackApplicationChartPage() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_APPLICATION_CHART_PAGE);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME_APPLICATION, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }

    /**
     * trackApplicationCpu
     */
    public void trackApplicationCpu() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_APPLICATION_CPU);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME_APPLICATION, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }

    /**
     * trackApplicationTrace
     */
    public void trackApplicationTrace() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_APPLICATION_TRACE);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME_APPLICATION, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }

    /**
     * trackApplicationPerfTrace
     */
    public void trackApplicationPerfTrace() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_APPLICATION_PERF_TRACE);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME_APPLICATION, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }

    /**
     * trackApplicationMemory
     */
    public void trackApplicationMemory() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_APPLICATION_MEMORY);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME_APPLICATION, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }

    /**
     * trackApplicationHprof
     */
    public void trackApplicationHprof() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_APPLICATION_HPROF);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME_APPLICATION, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }

    /**
     * trackApplicationDiskIo
     */
    public void trackApplicationDiskIo() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_APPLICATION_DISK_IO);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME_APPLICATION, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }

    /**
     * trackApplicationNetwork
     */
    public void trackApplicationNetwork() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_APPLICATION_NETWORK);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME_APPLICATION, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }

    /**
     * trackApplicationEnergy
     */
    public void trackApplicationEnergy() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_APPLICATION_ENERGY);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME_APPLICATION, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }

    /**
     * trackApplicationApp
     */
    public void trackApplicationApp() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_APPLICATION_APP);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME_APPLICATION, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }

    /**
     * trackSystemConfig
     */
    public void trackSystemConfig() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_SYSTEM_CONFIG);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME_SYSTEM, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }

    /**
     * trackSystemTrace
     */
    public void trackSystemTrace() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_SYSTEM_TRACE_PAGE);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME_SYSTEM, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }

    /**
     * trackDistributedConfig
     */
    public void trackDistributedConfig() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_DISTRIBUTED_CONFIG);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME_DISTRIBUTED, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }

    /**
     * trackDistributedPage
     */
    public void trackDistributedPage() {
        JSONObject trackJson = new JSONObject();
        trackJson.put(JSON_TRIGGER_TIME_STR, System.currentTimeMillis());
        trackJson.put(JSON_EVENT_NAME_STR, EVENT_DISTRIBUTED_PAGE);
        String trackDetail = trackJson.toJSONString();
        TraceDataBean traceData = new TraceDataBean(SID, "", ACTION_HOME_DISTRIBUTED, trackDetail);
        TraceUtil.trace(traceData, HarmonyCustomTopic.topic);
    }
}
