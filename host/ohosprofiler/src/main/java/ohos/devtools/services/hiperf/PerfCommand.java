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
import ohos.devtools.datasources.utils.monitorconfig.entity.PerfConfig;
import ohos.devtools.datasources.utils.session.entity.SessionInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Perf Command Parent Class
 *
 * @since 2021/8/25
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
        this(isLeakOhos, deviceId);
        sessionInfo = SessionManager.getInstance().getSessionInfo(sessionId);
        outPath = fileStorePath;
    }

    /**
     * PerfCommand
     *
     * @param isLeakOhos isLeakOhos
     * @param deviceId deviceId
     */
    public PerfCommand(boolean isLeakOhos, String deviceId) {
        this.isLeakOhos = isLeakOhos;
        this.deviceId = deviceId;
    }

    /**
     * execute Record command
     * hiperf record -p xxx --app xxx -o perf.data -f 1000 --offcpu
     *
     * @param config config
     * @return List<String>
     */
    public abstract List<String> executeRecord(PerfConfig config);

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
     * get Support Event
     *
     * @return Map<String, List<String>>
     */
    public abstract Map<String, List<String>> getSupportEvents();

    /**
     * wait record file generate done.
     * adjust perf.data size will not increase.
     */
    public void checkData() {
        while (true) {
            ArrayList<String> checkData = HdcWrapper.getInstance().generateDeviceCmdHead(isLeakOhos, deviceId);
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
            } catch (NumberFormatException exception) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
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
