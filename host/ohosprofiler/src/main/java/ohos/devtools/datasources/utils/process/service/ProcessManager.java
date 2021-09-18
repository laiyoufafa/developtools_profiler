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

package ohos.devtools.datasources.utils.process.service;

import ohos.devtools.datasources.transport.grpc.HiProfilerClient;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.device.dao.DeviceDao;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 进程数据处理对象
 */
public class ProcessManager {
    private static final Logger LOGGER = LogManager.getLogger(ProcessManager.class);
    private static final String MEM_PLUGIN_NAME = "/data/local/tmp/libmemdataplugin.z.so";

    private boolean isRequestProcess = false;

    /**
     * 单例进程对象
     */
    private static class SingletonClassInstance {
        private static final ProcessManager INSTANCE = new ProcessManager();
    }

    /**
     * getInstance
     *
     * @return ProcessManager
     */
    public static ProcessManager getInstance() {
        return ProcessManager.SingletonClassInstance.INSTANCE;
    }

    private ProcessManager() {
    }

    /**
     * getProcessList
     *
     * @param deviceInfo deviceInfo
     * @return List<ProcessInfo>
     */
    public List<ProcessInfo> getProcessList(DeviceIPPortInfo deviceInfo) {
        LOGGER.info("start to GetProcessList {}", DateTimeUtil.getNowTimeLong());
        if (deviceInfo == null || StringUtils.isBlank(deviceInfo.getIp())) {
            return new ArrayList<ProcessInfo>();
        }
        String deviceId = deviceInfo.getDeviceID();
        DeviceIPPortInfo deviceIPPortInfo = new DeviceDao().getDeviceIPPortInfo(deviceId).get();
        ProfilerServiceTypes.GetCapabilitiesResponse response =
            HiProfilerClient.getInstance().getCapabilities(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
        List<ProfilerServiceTypes.ProfilerPluginCapability> capabilities = response.getCapabilitiesList();
        Optional<ProfilerServiceTypes.ProfilerPluginCapability> libPlugin = getLibPlugin(capabilities, MEM_PLUGIN_NAME);
        List<ProcessInfo> processInfos = new ArrayList<>();
        try {
            if (libPlugin.isPresent()) {
                LOGGER.info("process Session start", DateTimeUtil.getNowTimeLong());
                isRequestProcess = true;
                int sessionId = HiProfilerClient.getInstance()
                    .requestCreateSession(deviceIPPortInfo.getForwardPort(), libPlugin.get().getName(), 0,
                        true, deviceInfo.getDeviceType());
                if (sessionId == -1) {
                    LOGGER.info("createSession failed");
                    return processInfos;
                }
                boolean startResult = HiProfilerClient.getInstance()
                    .requestStartSession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId);
                if (startResult) {
                    processInfos = HiProfilerClient.getInstance()
                        .fetchProcessData(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId);
                }
                Long stopTime = DateTimeUtil.getNowTimeLong();
                LOGGER.info("startStopSession {}", stopTime);
                HiProfilerClient.getInstance()
                    .requestStopSession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId, false);
                LOGGER.info("startStopEndSession {}", DateTimeUtil.getNowTimeLong());
                HiProfilerClient.getInstance()
                    .requestDestroySession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId);
                return processInfos;
            }
        } finally {
            isRequestProcess = false;
        }
        LOGGER.info("end to GetProcessList {}", DateTimeUtil.getNowTimeLong());
        return processInfos;
    }

    private Optional<ProfilerServiceTypes.ProfilerPluginCapability> getLibPlugin(
        List<ProfilerServiceTypes.ProfilerPluginCapability> capabilities, String libDataPlugin) {
        Optional<ProfilerServiceTypes.ProfilerPluginCapability> ability = capabilities.stream()
            .filter(profilerPluginCapability -> profilerPluginCapability.getName().contains(libDataPlugin)).findFirst();
        return ability;
    }

    /**
     * isRequestProcess
     *
     * @return boolean boolean
     */
    public boolean isRequestProcess() {
        return isRequestProcess;
    }
}
