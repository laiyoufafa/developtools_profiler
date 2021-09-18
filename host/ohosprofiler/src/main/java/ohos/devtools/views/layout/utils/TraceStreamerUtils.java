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

import ohos.devtools.datasources.utils.session.service.SessionManager;

import java.io.File;
import java.util.Locale;

/**
 * TraceStreamerUtils
 */
public class TraceStreamerUtils {
    private static TraceStreamerUtils traceStreamerUtils;
    private static final String DIR_STR = "trace_streamer" + File.separator;
    private static final String LOG_STR = "trace_streamer" + File.separator + "trace_streamer.log";
    private static final String DB_NAME = "trace_streamer.db";
    private static final String WIN_APP = "trace_streamer.exe";
    private static final String MAC_APP = "trace_streamer";

    /**
     * getInstance
     *
     * @return TraceStreamerUtils
     */
    public static TraceStreamerUtils getInstance() {
        if (traceStreamerUtils == null) {
            traceStreamerUtils = new TraceStreamerUtils();
        }
        return traceStreamerUtils;
    }

    /**
     * getBaseDir
     *
     * @return String
     */
    public String getBaseDir() {
        String pluginPath = SessionManager.getInstance().getPluginPath();
        return pluginPath + DIR_STR;
    }

    /**
     * getLogPath
     *
     * @return String
     */
    public String getLogPath() {
        String pluginPath = SessionManager.getInstance().getPluginPath();
        String logPath = pluginPath + LOG_STR;
        return logPath;
    }

    /**
     * getDbPath
     *
     * @return String
     */
    public String getDbPath() {
        String dbPath = getBaseDir() + DB_NAME;
        return dbPath;
    }

    /**
     * getDbPath
     *
     * @param dbName dbName
     * @return String
     */
    public String getDbPath(String dbName) {
        String dbPath = getBaseDir() + dbName;
        return dbPath;
    }

    /**
     * getTraceStreamerApp
     *
     * @return String
     */
    public String getTraceStreamerApp() {
        String traceStreamerApp = WIN_APP;
        if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("mac")) {
            traceStreamerApp = MAC_APP;
        }
        return traceStreamerApp;
    }
}
