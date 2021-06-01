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
import ohos.devtools.datasources.utils.device.dao.DeviceUtil;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 进程数据处理对象
 *
 * @version 1.0
 * @date 2021/02/07 11:06
 **/
public class ProcessManager {
    private static final Logger LOGGER = LogManager.getLogger(ProcessManager.class);

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
     * 调用接口，获取进程列表详情数据
     *
     * @param deviceInfo 设备对象
     * @return List<ProcessInfo>
     * @date 2021/02/07 11:06
     */
    public List<ProcessInfo> getProcessList(DeviceIPPortInfo deviceInfo) {
        LOGGER.info("start to GetProcessList {}", DateTimeUtil.getNowTimeLong());
        if (deviceInfo == null || StringUtils.isBlank(deviceInfo.getIp())) {
            return new ArrayList<ProcessInfo>();
        }
        String deviceId = deviceInfo.getDeviceID();
        DeviceIPPortInfo deviceIPPortInfo = new DeviceUtil().getDeviceIPPortInfo(deviceId);
        ProfilerServiceTypes.GetCapabilitiesResponse response =
            HiProfilerClient.getInstance().getCapabilities(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
        List<ProfilerServiceTypes.ProfilerPluginCapability> capabilities = response.getCapabilitiesList();
        List<ProcessInfo> processInfos = new ArrayList<>();
        for (ProfilerServiceTypes.ProfilerPluginCapability profilerPluginCapability : capabilities) {
            LOGGER.info("get Device capabilities {}", profilerPluginCapability.getName());
            if (profilerPluginCapability.getName().contains("libmemdataplugin")) {
                LOGGER.info("process Session start", DateTimeUtil.getNowTimeLong());
                int sessionId = HiProfilerClient.getInstance()
                    .requestCreateSession(deviceIPPortInfo.getForwardPort(), profilerPluginCapability.getName(), 0,
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
                boolean stopRes = HiProfilerClient.getInstance()
                    .requestStopSession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId, false);

                LOGGER.info("startStopEndSession {}", DateTimeUtil.getNowTimeLong());
                boolean request = HiProfilerClient.getInstance()
                    .requestDestroySession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId);
                boolean res = HiProfilerClient.getInstance()
                    .destroyProfiler(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
                LOGGER.info("get ProcessList is {}", processInfos);
                return processInfos;
            }
        }
        LOGGER.info("end to GetProcessList {}", DateTimeUtil.getNowTimeLong());
        return processInfos;
    }

}
