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
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * execute perf command on devices.
 *
 * @since 2021/8/22
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
     * Construction.
     *
     * @param isLeakOhos is leak ohos
     * @param deviceId devices id
     */
    public HiPerfCommand(boolean isLeakOhos, String deviceId) {
        super(isLeakOhos, deviceId);
    }

    /**
     * execute Record command
     * hiperf record -p xxx --app xxx -o perf.data -f 1000 --offcpu
     *
     * @param config config
     * @return  List<String> list
     */
    public List<String> executeRecord(PerfConfig config) {
        stopRecord(); // first kill all of perf process to prevent data mistake
        ArrayList<String> recordCommand = HdcWrapper.getInstance().generateDeviceCmdHead(isLeakOhos, deviceId);
        recordCommand.add(HIPERF_COMMAND);
        recordCommand.add("record");
        recordCommand.add("-p");
        recordCommand.add(sessionInfo.getPid() + "");
        recordCommand.add("-o");
        recordCommand.add(PERF_DATA_PATH);
        recordCommand.add("-f");
        recordCommand.add(config.getFrequency() + "");
        recordCpu(config, recordCommand);
        if (config.getCpuPercent() != 0) {
            recordCommand.add("--cpu-limit");
            recordCommand.add(config.getCpuPercent() + "");
        }
        recordEvent(config, recordCommand);
        recordEnum(config, recordCommand);
        if (config.isOffCpu()) {
            recordCommand.add("--offcpu");
        }
        if (config.isInherit()) {
            recordCommand.add("--no-inherit");
        }
        if (config.getMmapPages() != 0) {
            recordCommand.add("-m");
            recordCommand.add(config.getMmapPages() + "");
        }
        String cmd = getCmd(recordCommand);
        LOGGER.info(cmd);
        return HdcWrapper.getInstance().execCmd(recordCommand);
    }

    private void recordEvent(PerfConfig config, ArrayList<String> recordCommand) {
        List<?> eventList = config.getEventList();
        if (!config.getEventList().isEmpty()) {
            recordCommand.add("-e");
            StringBuilder builder = new StringBuilder();
            for (Object event : eventList) {
                builder.append(event);
                if (eventList.indexOf(event) != eventList.size() - 1) {
                    builder.append(",");
                }
            }
            recordCommand.add(builder.toString());
        }
    }

    private void recordCpu(PerfConfig config, ArrayList<String> recordCommand) {
        List<?> cpuList = config.getCpuList();
        if (!cpuList.isEmpty()) {
            recordCommand.add("-c");
            StringBuilder builder = new StringBuilder();
            for (Object obj : cpuList) {
                builder.append(obj);
                if (cpuList.indexOf(obj) != cpuList.size() - 1) {
                    builder.append(",");
                }
            }
            recordCommand.add(builder.toString());
        }
    }

    private void recordEnum(PerfConfig config, ArrayList<String> recordCommand) {
        if (config.getCallStack() != 0) {
            String callStackType = PerfConfig.CallStack.class.getEnumConstants()[config.getCallStack()].getName();
            recordCommand.add("--call-stack");
            recordCommand.add(callStackType);
        }
        if (config.getBranch() != 0) {
            String branchType = PerfConfig.Branch.class.getEnumConstants()[config.getBranch()].getName();
            recordCommand.add("-j");
            recordCommand.add(branchType);
        }
        if (config.getClockId() != 0) {
            String clockType = PerfConfig.Clock.class.getEnumConstants()[config.getClockId()].getName();
            recordCommand.add("--clockid");
            recordCommand.add(clockType);
        }
    }

    /**
     * Stop Record by kill perf pid.
     */
    public void stopRecord() {
        ArrayList<String> stopRecordCommand = HdcWrapper.getInstance().generateDeviceCmdHead(isLeakOhos, deviceId);
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
        ArrayList<String> reportCommand = HdcWrapper.getInstance().generateDeviceCmdHead(isLeakOhos, deviceId);
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

    @Override
    public final Map<String, List<String>> getSupportEvents() {
        ArrayList<String> command = HdcWrapper.getInstance().generateDeviceCmdHead(isLeakOhos, deviceId);
        command.add(HIPERF_COMMAND);
        command.add("list");
        List<String> result = HdcWrapper.getInstance().execCmd(command);
        return parseEvent(result);
    }

    private Map<String, List<String>> parseEvent(List<String> result) {
        Map<String, List<String>> eventMap = new HashMap<>();
        List<String> events;
        String type = "";
        for (String line : result) {
            line = line.strip();
            if (line.startsWith("Supported")) {
                String startSign = "for";
                type = line.substring(line.indexOf(startSign) + startSign.length(), line.lastIndexOf(":")).strip();
                events = new ArrayList<>();
                eventMap.put(type, events);
            } else if (line.contains("not support") || line.isEmpty() || line.contains("Text file busy")) {
                // do not need deal with it
                if (ProfilerLogManager.isDebugEnabled()) {
                    LOGGER.debug("do not need deal with {}", line);
                }
            } else {
                String event = line.split(" ")[0];
                if (eventMap.get(type) != null) {
                    eventMap.get(type).add(event);
                }
            }
        }
        return eventMap;
    }
}
