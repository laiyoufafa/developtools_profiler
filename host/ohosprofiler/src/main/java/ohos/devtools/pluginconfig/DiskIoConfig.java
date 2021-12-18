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

import ohos.devtools.datasources.transport.grpc.DiskIoHelper;
import ohos.devtools.datasources.transport.grpc.service.DiskioPluginConfig;
import ohos.devtools.datasources.utils.datahandler.datapoller.DiskIoDataConsumer;
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
import ohos.devtools.views.layout.chartview.diskio.DiskIoView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static ohos.devtools.datasources.utils.common.Constant.DISK_IO_PLUG;

/**
 * DiskIoConfig
 *
 * @since 2021/9/20
 */
@DPlugin
public class DiskIoConfig extends IPluginConfig {
    private static final Logger LOGGER = LogManager.getLogger(DiskIoConfig.class);
    private static final String DISK_IO_PLUGIN_NAME = "/data/local/tmp/libdiskiodataplugin.z.so";

    @Override
    public PluginConf createConfig() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createConfig");
        }
        ProfilerMonitorItem diskIoItem = new ProfilerMonitorItem(3, "DiskIO", DiskIoView.class);
        PluginConf diskIo =
            new PluginConf(DISK_IO_PLUGIN_NAME, DISK_IO_PLUG, DiskIoDataConsumer.class, true, diskIoItem);
        diskIo.setICreatePluginConfig((deviceIPPortInfo, processInfo) -> {
            DiskioPluginConfig.DiskioConfig plug = DiskIoHelper.createDiskIORequest(processInfo.getProcessId());
            return new HiProfilerPluginConfig(40, plug.toByteString());
        });
        diskIo.setPluginMode(PluginMode.ONLINE);
        diskIo.addAnalysisTypes(AnalysisType.APPLICATION_TYPE);
        return diskIo;
    }

    private DiskioPluginConfig.DiskioConfig getDiskIoConfig(DeviceIPPortInfo device, ProcessInfo process) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getDiskIoConfig");
        }
        return DiskIoHelper.createDiskIORequest(process.getProcessId());
    }
}
