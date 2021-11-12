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
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * execute perf command on devices.
 *
 * @since 2021/10/25
 */
public class HiPerfCommand extends PerfCommand {

    private static final String HIPERF_COMMAND = "hiperf";
    private static final Logger LOGGER = LogManager.getLogger(HiPerfCommand.class);

    /**
     * Construction.
     *
     * @param sessionId current session id
     * @param isLeakOhos is leak ohos
     * @param deviceId devices id
     * @param fileStorePath out file path
     */
    public HiPerfCommand(long sessionId, boolean isLeakOhos, String deviceId, String fileStorePath) {
        super(sessionId, isLeakOhos, deviceId, fileStorePath);
    }

    /**
     * execute Record command
     * hiperf record -p xxx --app xxx -o perf.data -f 1000 --offcpu
     */
    public void executeRecord() {
        stopRecord(); // first kill all of perf process to prevent data mistake

        ArrayList<String> recordCommand = new ArrayList<>();
        generateCmdHead(recordCommand, isLeakOhos, deviceId);
        recordCommand.add(HIPERF_COMMAND);

        recordCommand.add("record");
        recordCommand.add("-p");
        recordCommand.add(sessionInfo.getPid() + "");
        // recordCommand.add("--app")
        // recordCommand.add(sessionInfo.getProcessName())
        recordCommand.add("-o");
        recordCommand.add(PERF_DATA_PATH);
        recordCommand.add("-f");
        recordCommand.add(DEFAULT_FREQUENCY + "");
        recordCommand.add("--call-stack");
        recordCommand.add("dwarf");
        recordCommand.add("--offcpu");

        String cmd = getCmd(recordCommand);
        ExecutorService executorService =
            new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        executorService.execute(() -> HdcWrapper.getInstance().execCmdBy(recordCommand));
        LOGGER.info(cmd);
    }

    /**
     * Stop Record by kill perf pid.
     */
    public void stopRecord() {
        ArrayList<String> stopRecordCommand = new ArrayList<>();
        generateCmdHead(stopRecordCommand, isLeakOhos, deviceId);
        stopRecordCommand.add("killall");
        stopRecordCommand.add("-2");
        stopRecordCommand.add(HIPERF_COMMAND);
        String cmd = getCmd(stopRecordCommand);
        String result = HdcWrapper.getInstance().execCmdBy(stopRecordCommand);
        LOGGER.info(cmd);
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("Stop Record result is {}", result);
        }

    }

    /**
     * execute report command
     * hiperf record --proto -i perf.data -o xxx.trace
     *
     * @return execute Command result
     */
    public List<String> executeReport() {
        ArrayList<String> reportCommand = new ArrayList<>();
        generateCmdHead(reportCommand, isLeakOhos, deviceId);

        reportCommand.add(HIPERF_COMMAND);
        reportCommand.add("report");
        reportCommand.add("--proto");

        reportCommand.add("-i");
        reportCommand.add(PERF_DATA_PATH);
        reportCommand.add("-o");
        reportCommand.add(outPath);
        String cmd = getCmd(reportCommand);
        List<String> result = HdcWrapper.getInstance().execCmd(reportCommand);
        LOGGER.info(cmd);
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("report Command result is {}", result);
        }
        return result;
    }
}
