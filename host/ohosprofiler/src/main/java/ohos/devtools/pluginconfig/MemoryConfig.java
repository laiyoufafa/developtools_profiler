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

import ohos.devtools.datasources.transport.grpc.MemoryPlugHelper;
import ohos.devtools.datasources.transport.grpc.service.MemoryPluginConfig;
import ohos.devtools.datasources.utils.datahandler.datapoller.MemoryDataConsumer;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.plugin.IPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.AnalysisType;
import ohos.devtools.datasources.utils.plugin.entity.DPlugin;
import ohos.devtools.datasources.utils.plugin.entity.HiProfilerPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.PluginConf;
import ohos.devtools.datasources.utils.plugin.entity.PluginMode;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.layout.chartview.ProfilerMonitorItem;
import ohos.devtools.views.layout.chartview.memory.MemoryItemView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static ohos.devtools.datasources.utils.common.Constant.MEMORY_PLUG;
import static ohos.devtools.datasources.utils.device.entity.DeviceType.FULL_HOS_DEVICE;

/**
 * MemoryConfig
 *
 * @since 2021/9/20
 */
@DPlugin
public class MemoryConfig extends IPluginConfig {
    private static final Logger LOGGER = LogManager.getLogger(MemoryConfig.class);
    private static final String MEM_PLUGIN_NAME = "/data/local/tmp/libmemdataplugin.z.so";

    @Override
    public PluginConf createConfig() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createConfig");
        }
        ProfilerMonitorItem memoryItem = new ProfilerMonitorItem(2, "Memory", MemoryItemView.class);
        PluginConf memoryConfig =
            new PluginConf(MEM_PLUGIN_NAME, MEMORY_PLUG, MemoryDataConsumer.class, true, memoryItem);
        memoryConfig.setICreatePluginConfig((deviceIPPortInfo, processInfo) -> new HiProfilerPluginConfig(40,
            getConfig(deviceIPPortInfo, processInfo).toByteString()));
        memoryConfig.setPluginMode(PluginMode.ONLINE);
        memoryConfig.addAnalysisTypes(AnalysisType.APPLICATION_TYPE);
        return memoryConfig;
    }

    private static MemoryPluginConfig.MemoryConfig getConfig(DeviceIPPortInfo device, ProcessInfo process) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getConfig");
        }
        MemoryPluginConfig.MemoryConfig plug;
        if (device.getDeviceType() == FULL_HOS_DEVICE) {
            plug = MemoryPlugHelper.createMemRequest(process.getProcessId(), false, true, true, true);
        } else {
            plug = MemoryPlugHelper.createMemRequest(process.getProcessId(), false, true, true, false);
        }
        return plug;
    }
}
