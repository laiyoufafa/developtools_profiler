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

import ohos.devtools.datasources.transport.grpc.CpuPlugHelper;
import ohos.devtools.datasources.transport.grpc.service.CpuPluginConfig;
import ohos.devtools.datasources.utils.datahandler.datapoller.CpuDataConsumer;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.plugin.IPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.DPlugin;
import ohos.devtools.datasources.utils.plugin.entity.PluginConf;
import ohos.devtools.datasources.utils.plugin.entity.HiProfilerPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.PluginMode;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.views.layout.chartview.ProfilerMonitorItem;
import ohos.devtools.views.layout.chartview.cpu.CpuItemView;

import static ohos.devtools.datasources.utils.common.Constant.CPU_PLUG;

/**
 * Cpu Config
 */
@DPlugin
public class CpuConfig extends IPluginConfig {
    private static final String CPU_PLUGIN_NAME = "/data/local/tmp/libcpudataplugin.z.so";

    @Override
    public PluginConf createConfig() {
        ProfilerMonitorItem cpuItem = new ProfilerMonitorItem(1, "Cpu", CpuItemView.class);
        PluginConf cpuConfig = new PluginConf(CPU_PLUGIN_NAME, CPU_PLUG, CpuDataConsumer.class, true, cpuItem);
        cpuConfig.setICreatePluginConfig(((deviceIPPortInfo, processInfo) -> {
            CpuPluginConfig.CpuConfig plug = CpuPlugHelper.createCpuRequest(processInfo.getProcessId());
            return new HiProfilerPluginConfig(40, plug.toByteString());
        }));
        cpuConfig.setPluginMode(PluginMode.ONLINE);
        return cpuConfig;
    }

    private static CpuPluginConfig.CpuConfig getCpuConfig(DeviceIPPortInfo device, ProcessInfo process) {
        CpuPluginConfig.CpuConfig plug = CpuPlugHelper.createCpuRequest(process.getProcessId());
        return plug;
    }
}
