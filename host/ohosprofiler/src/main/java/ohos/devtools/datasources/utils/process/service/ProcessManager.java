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
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 进程数据处理对象
 */
public class ProcessManager {
    private static final Logger LOGGER = LogManager.getLogger(ProcessManager.class);
    private static final String PROCESS_PLUGIN_NAME = "/data/local/tmp/libprocessplugin.z.so";
    private static final ProcessManager INSTANCE = new ProcessManager();

    private boolean isRequestProcess = false;

    /**
     * getInstance
     *
     * @return ProcessManager
     */
    public static ProcessManager getInstance() {
        return ProcessManager.INSTANCE;
    }

    private ProcessManager() {
    }

    /**
     * getProcessList
     *
     * @param deviceInfo deviceInfo
     * @return List <ProcessInfo>
     */
    public List<ProcessInfo> getProcessList(DeviceIPPortInfo deviceInfo) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getProcessList");
        }
        if (deviceInfo == null || StringUtils.isBlank(deviceInfo.getIp())) {
            return new ArrayList<ProcessInfo>();
        }
        Optional<DeviceIPPortInfo> deviceIPPortInfoOpt = new DeviceDao().getDeviceIPPortInfo(deviceInfo.getDeviceID());
        List<ProcessInfo> processInfos = new ArrayList<>();
        if (deviceIPPortInfoOpt.isPresent()) {
            DeviceIPPortInfo deviceIPPortInfo = deviceIPPortInfoOpt.get();
            ProfilerServiceTypes.GetCapabilitiesResponse response = HiProfilerClient.getInstance()
                .getCapabilities(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
            List<ProfilerServiceTypes.ProfilerPluginCapability> capabilities =
                Objects.requireNonNull(response).getCapabilitiesList();
            Optional<ProfilerServiceTypes.ProfilerPluginCapability> libPlugin = getLibPlugin(capabilities);
            try {
                if (libPlugin.isPresent()) {
                    isRequestProcess = true;
                    int sessionId = HiProfilerClient.getInstance()
                        .processListCreateSession(deviceIPPortInfo.getForwardPort(), libPlugin.get().getName(), 0, true,
                            deviceInfo.getDeviceType());
                    if (sessionId == -1) {
                        if (ProfilerLogManager.isErrorEnabled()) {
                            LOGGER.error("createSession failed");
                        }
                        return processInfos;
                    }
                    boolean startResult = HiProfilerClient.getInstance()
                        .requestStartSession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId);
                    if (startResult) {
                        processInfos = HiProfilerClient.getInstance()
                            .fetchProcessData(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId);
                    }
                    HiProfilerClient.getInstance()
                        .requestStopSession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId,
                            false);
                    HiProfilerClient.getInstance()
                        .requestDestroySession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId);
                    return processInfos;
                }
            } finally {
                isRequestProcess = false;
            }
        }
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("end to GetProcessList {}", DateTimeUtil.getNowTimeLong());
        }
        return processInfos;
    }

    private Optional<ProfilerServiceTypes.ProfilerPluginCapability> getLibPlugin(
        List<ProfilerServiceTypes.ProfilerPluginCapability> capabilities) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getLibPlugin");
        }
        return capabilities.stream()
            .filter(profilerPluginCapability -> profilerPluginCapability.getName().contains(PROCESS_PLUGIN_NAME))
            .findFirst();
    }


    /**
     * isRequestProcess
     *
     * @return boolean
     */
    public boolean isRequestProcess() {
        return isRequestProcess;
    }
}
