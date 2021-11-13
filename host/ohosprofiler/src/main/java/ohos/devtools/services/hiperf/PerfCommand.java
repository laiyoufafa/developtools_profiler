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
package ohos.devtools.services.hiperf;

import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.datasources.utils.session.entity.SessionInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Perf Command Parent Class
 *
 * @since: 2021/8/25
 */
public abstract class PerfCommand {
    /**
     * PERF_TRACE_PATH
     */
    public static final String PERF_TRACE_PATH = "/data/local/tmp/perf_data";

    /**
     * perf data path
     */
    protected static final String PERF_DATA_PATH = "/data/local/tmp/perf.data";

    /**
     * default frequency
     */
    protected static final int DEFAULT_FREQUENCY = 1000;

    /**
     * deviceId
     */
    protected String deviceId;

    /**
     * sessionInfo
     */
    protected SessionInfo sessionInfo;

    /**
     * isLeakOhos
     */
    protected boolean isLeakOhos;

    /**
     * outPath
     */
    protected String outPath;
    private int lastSize;

    /**
     * Construction.
     *
     * @param sessionId current session id
     * @param isLeakOhos is leak ohos
     * @param deviceId devices id
     * @param fileStorePath out file path
     */
    public PerfCommand(long sessionId, boolean isLeakOhos, String deviceId, String fileStorePath) {
        sessionInfo = SessionManager.getInstance().getSessionInfo(sessionId);
        this.isLeakOhos = isLeakOhos;
        this.deviceId = deviceId;
        outPath = fileStorePath;
    }

    /**
     * execute Record command
     * hiperf record -p xxx --app xxx -o perf.data -f 1000 --offcpu
     */
    public abstract void executeRecord();

    /**
     * execute report command
     * hiperf record --proto -i perf.data -o xxx.trace
     *
     * @return execute Command result
     */
    public abstract List<String> executeReport();

    /**
     * Stop Record by kill perf pid.
     */
    public abstract void stopRecord();

    /**
     * generate Cmd Head
     *
     * @param recordCommand command
     * @param isLeakOhos judge use hdc or hdc_std
     * @param deviceId select device id
     */
    protected void generateCmdHead(ArrayList<String> recordCommand, boolean isLeakOhos, String deviceId) {
        String pluginPath;
        if (isLeakOhos) {
            pluginPath = SessionManager.getInstance().getHdcStdPath();
        } else {
            pluginPath = SessionManager.getInstance().getHdcPath();
        }
        recordCommand.add(pluginPath);
        recordCommand.add("-t");
        recordCommand.add(deviceId);
        recordCommand.add("shell");
    }

    /**
     * wait record file generate done.
     * adjust perf.data size will not increase.
     */
    public void checkData() {
        while (true) {
            ArrayList<String> checkData = new ArrayList<>();
            generateCmdHead(checkData, isLeakOhos, deviceId);
            checkData.add("du");
            checkData.add(PERF_DATA_PATH);
            String execResult = HdcWrapper.getInstance().execCmdBy(checkData);
            String result = execResult.trim().replaceAll("\\s+", " ");
            try {
                int currentSize = Integer.parseInt(result.split(" ")[0]);
                // if perf.data current Size equals last 1s second size
                // generate perf.data is done
                if (currentSize == lastSize && currentSize != 0) {
                    break;
                }
                lastSize = currentSize;
            } catch (NumberFormatException e) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * list to string
     *
     * @param command list
     * @return string
     */
    @NotNull
    protected String getCmd(@NotNull ArrayList<String> command) {
        StringBuilder cmd = new StringBuilder();
        for (String line : command) {
            cmd.append(line).append(" ");
        }
        return cmd.toString();
    }
}
