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

import io.grpc.StatusRuntimeException;
import ohos.devtools.datasources.transport.grpc.HiProfilerClient;
import ohos.devtools.datasources.transport.grpc.ProfilerClient;
import ohos.devtools.datasources.transport.grpc.ProfilerServiceHelper;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.ProcessPluginConfig;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.utils.common.util.CommonUtil;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.device.dao.DeviceDao;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.common.LayoutConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ohos.devtools.datasources.transport.grpc.HiProfilerClient.getSTDSha256;
import static ohos.devtools.datasources.transport.grpc.HiProfilerClient.getSha256;
import static ohos.devtools.datasources.utils.device.entity.DeviceType.LEAN_HOS_DEVICE;
import static ohos.devtools.views.common.Constant.IS_SUPPORT_NEW_HDC;

/**
 * processManager
 *
 * @since 2021/11/22
 */
public class ProcessManager {
    private static final Logger LOGGER = LogManager.getLogger(ProcessManager.class);
    private static final String PROCESS_PLUGIN_NAME = "/data/local/tmp/libprocessplugin.z.so";

    private boolean isRequestProcess = false;

    /**
     * SingletonClassInstance
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
     * @return List <ProcessInfo>
     */
    public List<ProcessInfo> getProcessList(DeviceIPPortInfo deviceInfo) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getProcessList");
        }
        if (deviceInfo == null || StringUtils.isBlank(deviceInfo.getIp())) {
            return new ArrayList<ProcessInfo>();
        }
        String deviceId = deviceInfo.getDeviceID();
        Optional<DeviceIPPortInfo> deviceIPPortInfoOpt = new DeviceDao().getDeviceIPPortInfo(deviceId);
        List<ProcessInfo> processInfos = new ArrayList<>();
        if (deviceIPPortInfoOpt.isPresent()) {
            DeviceIPPortInfo deviceIPPortInfo = deviceIPPortInfoOpt.get();
            ProfilerServiceTypes.GetCapabilitiesResponse response = HiProfilerClient.getInstance()
                .getCapabilities(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
            if (Objects.nonNull(response)) {
                List<ProfilerServiceTypes.ProfilerPluginCapability> capabilities =
                    Objects.requireNonNull(response).getCapabilitiesList();
                Optional<ProfilerServiceTypes.ProfilerPluginCapability> libPlugin = getLibPlugin(capabilities);
                if (libPlugin.isPresent()) {
                    isRequestProcess = true;
                    int sessionId = processListCreateSession(deviceIPPortInfo.getForwardPort(),
                        libPlugin.get().getName(), 0, true, deviceInfo.getDeviceType());
                    if (sessionId == -1) {
                        if (ProfilerLogManager.isErrorEnabled()) {
                            LOGGER.error("createSession failed");
                        }
                        return processInfos;
                    }
                    HiProfilerClient.getInstance()
                        .keepSession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId);
                    boolean startResult = HiProfilerClient.getInstance()
                        .requestStartSession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId);
                    if (startResult) {
                        processInfos = HiProfilerClient.getInstance()
                            .fetchProcessData(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId);
                    }
                    HiProfilerClient.getInstance().requestStopSession(deviceIPPortInfo.getIp(), deviceIPPortInfo
                        .getForwardPort(), sessionId, false);
                    HiProfilerClient.getInstance()
                        .requestDestroySession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId);
                    return processInfos;
                }
            }
        }
        return processInfos;
    }

    /**
     * createSession for ListProcess
     *
     * @param port port number
     * @param name name
     * @param pid pid
     * @param reportProcessTree report process tree
     * @param deviceType DeviceType
     * @return int
     */
    public int processListCreateSession(int port, String name, int pid, boolean reportProcessTree,
        DeviceType deviceType) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("processListCreateSession port {}", port);
        }
        if (port <= 0 || port > LayoutConstants.PORT) {
            return -1;
        }
        ProcessPluginConfig.ProcessConfig plug =
            ProcessPluginConfig.ProcessConfig.newBuilder().setReportProcessTree(true).build();
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfig = ProfilerServiceHelper
            .profilerSessionConfig(true, null, 10,
                ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE, 5000);
        String sha256;
        if (IS_SUPPORT_NEW_HDC && deviceType == LEAN_HOS_DEVICE) {
            sha256 = getSTDSha256("/data/local/tmp/libprocessplugin.z.so");
        } else {
            sha256 = getSha256("/data/local/tmp/libprocessplugin.z.so");
        }
        CommonTypes.ProfilerPluginConfig plugConfig =
            ProfilerServiceHelper.profilerPluginConfig(name, sha256, 2, plug.toByteString());
        List<CommonTypes.ProfilerPluginConfig> plugs = new ArrayList();
        plugs.add(plugConfig);
        ProfilerServiceTypes.CreateSessionRequest request =
            ProfilerServiceHelper.createSessionRequest(CommonUtil.getRequestId(), sessionConfig, plugs);
        ProfilerServiceTypes.CreateSessionResponse response = null;
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient("", port);
        try {
            response = client.createSession(request);
            LOGGER.info("process Session start444 {} ", DateTimeUtil.getNowTimeLong());
        } catch (StatusRuntimeException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("processListCreateSession ", exception);
            }
            return -1;
        }
        return response.getSessionId();
    }

    private Optional<ProfilerServiceTypes.ProfilerPluginCapability> getLibPlugin(
        List<ProfilerServiceTypes.ProfilerPluginCapability> capabilities) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getLibPlugin {}", capabilities);
        }
        return capabilities.stream()
            .filter(profilerPluginCapability -> profilerPluginCapability.getName().contains(PROCESS_PLUGIN_NAME))
            .findFirst();
    }

    public void setIsRequest(boolean isRequestProcess) {
        this.isRequestProcess = isRequestProcess;
    }

    /**
     * getDebuggerProcessList
     *
     * @param deviceInfo deviceInfo
     * @return List<ProcessInfo>
     */
    public List<ProcessInfo> getDebuggerProcessList(DeviceIPPortInfo deviceInfo) {
        return new ArrayList<>();
    }

    /**
     * check Process is Debug
     *
     * @param deviceInfo deviceInfo
     * @param processInfo processInfo
     * @return boolean
     */
    public boolean checkIsDebuggerProcess(DeviceIPPortInfo deviceInfo, ProcessInfo processInfo) {
        return false;
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
