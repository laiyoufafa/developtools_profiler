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

import ohos.devtools.datasources.utils.plugin.IPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.AnalysisType;
import ohos.devtools.datasources.utils.plugin.entity.DPlugin;
import ohos.devtools.datasources.utils.plugin.entity.PluginConf;
import ohos.devtools.datasources.utils.plugin.entity.PluginMode;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * FtraceConfig
 *
 * @since 2021/9/20
 */
@DPlugin
public class ProcessConfig extends IPluginConfig {
    private static final Logger LOGGER = LogManager.getLogger(ProcessConfig.class);
    private static final String PLUGIN_NAME = "/data/local/tmp/libprocessplugin.z.so";

    @Override
    public PluginConf createConfig() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createConfig");
        }
        PluginConf processConfig = new PluginConf(PLUGIN_NAME, "", null, false, null);
        processConfig.setPluginMode(PluginMode.OFFLINE);
        processConfig.addAnalysisTypes(AnalysisType.APPLICATION_TYPE);
        processConfig.addAnalysisTypes(AnalysisType.SYSTEM_TYPE);
        processConfig.addAnalysisTypes(AnalysisType.DISTRIBUTED_TYPE);
        return processConfig;
    }
}
