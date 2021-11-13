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

import ohos.devtools.datasources.transport.grpc.service.StreamPluginConfig;
import ohos.devtools.datasources.utils.plugin.IPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.HiProfilerPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.PluginConf;
import ohos.devtools.datasources.utils.plugin.entity.PluginMode;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Data Verification Config
 *
 * @since IPluginConfig
 */
public class DataCheckConfig extends IPluginConfig {
    private static final Logger LOGGER = LogManager.getLogger(DataCheckConfig.class);
    private final String DATA_CHECK_PLUGIN_NAME = "/data/local/tmp/libdatacheckplugin.z.so";
    private final String DATA_CHECK_PLUG = "/data/local/tmp/libdatacheckplugin.z.so";

    @Override
    public PluginConf createConfig() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createConfig");
        }
        PluginConf pluginConf = new PluginConf(DATA_CHECK_PLUGIN_NAME, DATA_CHECK_PLUG, null, false, null);
        pluginConf.setICreatePluginConfig((deviceIPPortInfo, processInfo) -> {
            StreamPluginConfig.StreamConfig stream =
                StreamPluginConfig.StreamConfig.newBuilder().setPid(processInfo.getProcessId()).build();
            return new HiProfilerPluginConfig(40, stream.toByteString());
        });
        pluginConf.setPluginMode(PluginMode.ONLINE);
        return pluginConf;
    }
}
