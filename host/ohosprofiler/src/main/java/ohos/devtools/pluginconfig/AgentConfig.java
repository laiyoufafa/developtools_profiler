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

package ohos.devtools.pluginconfig;

import ohos.devtools.datasources.transport.grpc.HiProfilerClient;
import ohos.devtools.datasources.transport.grpc.service.AgentPluginConfig;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.datasources.utils.datahandler.datapoller.AgentDataConsumer;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.plugin.IPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.AnalysisType;
import ohos.devtools.datasources.utils.plugin.entity.DPlugin;
import ohos.devtools.datasources.utils.plugin.entity.HiProfilerPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.PluginBufferConfig;
import ohos.devtools.datasources.utils.plugin.entity.PluginConf;
import ohos.devtools.datasources.utils.plugin.entity.PluginMode;
import ohos.devtools.datasources.utils.process.service.ProcessManager;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_START_JAVAHEAP;
import static ohos.devtools.datasources.transport.hdc.HdcWrapper.conversionCommand;
import static ohos.devtools.datasources.utils.common.Constant.JVMTI_AGENT_PLUG;
import static ohos.devtools.datasources.utils.device.entity.DeviceType.FULL_HOS_DEVICE;
import static ohos.devtools.datasources.utils.device.entity.DeviceType.LEAN_HOS_DEVICE;

/**
 * AgentConfig
 *
 * @since 2021/5/19 16:39
 */
@DPlugin
public class AgentConfig extends IPluginConfig {
    private static final Logger LOGGER = LogManager.getLogger(AgentConfig.class);
    private static final String AGENT_PLUGIN_NAME = "/data/local/tmp/libagentplugin.z.so";

    @Override
    public PluginConf createConfig() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createConfig");
        }
        PluginConf agentConfig =
            new PluginConf(AGENT_PLUGIN_NAME, JVMTI_AGENT_PLUG, AgentDataConsumer.class, false, null);
        agentConfig.setICreatePluginConfig((deviceIPPortInfo, processInfo) -> {
            AgentPluginConfig.AgentConfig agent =
                AgentPluginConfig.AgentConfig.newBuilder().setPid(processInfo.getProcessId()).setStackDepth(5).build();
            LOGGER.error("agent {}", agent.toString());
            return new HiProfilerPluginConfig(40, agent.toByteString());
        });
        agentConfig.setPluginBufferConfig(new PluginBufferConfig(10000, PluginBufferConfig.Policy.RECYCLE));
        agentConfig.setSpecialStart(true);
        agentConfig.setGetPluginName((deviceIPPortInfo, processInfo) -> {
            return "jvmtiagent_" + processInfo.getProcessName();
        });
        agentConfig.setSpecialStartPlugMethod((deviceIPPortInfo, processInfo) -> {
            if (!ProcessManager.getInstance().checkIsDebuggerProcess(deviceIPPortInfo, processInfo)) {
                return false;
            }
            if (deviceIPPortInfo.getDeviceType() == LEAN_HOS_DEVICE) {
                return false;
            }
            String pluginName = agentConfig.getGetPluginName().getPluginName(deviceIPPortInfo, processInfo);
            boolean startJavaHeap = isStartJavaHeap(deviceIPPortInfo, pluginName);
            String proc = processInfo.getProcessName();
            if (StringUtils.isNotBlank(proc) && (!startJavaHeap)) {
                if (deviceIPPortInfo.getDeviceType() == FULL_HOS_DEVICE) {
                    ArrayList cmd = conversionCommand(HDC_START_JAVAHEAP, deviceIPPortInfo.getDeviceID(), proc);
                    String res = HdcWrapper.getInstance().getHdcStringResult(cmd);
                    if (res.contains("javaHeapSuccess")) {
                        if (ProfilerLogManager.isInfoEnabled()) {
                            LOGGER.info("start Agent Success");
                        }
                        startJavaHeap = true;
                    }
                    if (ProfilerLogManager.isErrorEnabled()) {
                        LOGGER.error("start Agent  {}", startJavaHeap);
                    }
                }
            }
            return startJavaHeap;
        });
        agentConfig.setPluginMode(PluginMode.ONLINE);
        agentConfig.addSupportDeviceTypes(FULL_HOS_DEVICE);
        agentConfig.addAnalysisTypes(AnalysisType.APPLICATION_TYPE);
        return agentConfig;
    }

    private boolean isStartJavaHeap(DeviceIPPortInfo device, String agentPlugName) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("isStartJavaHeap");
        }
        boolean startJavaHeap = false;
        ProfilerServiceTypes.GetCapabilitiesResponse response =
            HiProfilerClient.getInstance().getCapabilities(device.getIp(), device.getForwardPort());
        List<ProfilerServiceTypes.ProfilerPluginCapability> capabilitiesResponse = response.getCapabilitiesList();
        List<ProfilerServiceTypes.ProfilerPluginCapability> agentStatus =
            getLibDataPlugin(capabilitiesResponse, agentPlugName);
        if (!agentStatus.isEmpty()) {
            startJavaHeap = true;
        }
        return startJavaHeap;
    }

    private List<ProfilerServiceTypes.ProfilerPluginCapability> getLibDataPlugin(
        List<ProfilerServiceTypes.ProfilerPluginCapability> capabilities, String libDataPlugin) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getLibDataPlugin");
        }
        return capabilities.stream()
            .filter(profilerPluginCapability -> profilerPluginCapability.getName().contains(libDataPlugin))
            .collect(Collectors.toList());
    }
}
