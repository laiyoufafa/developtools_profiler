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
 * HilogConfig
 *
 * @since 2021/9/20
 */
@DPlugin
public class HilogConfig extends IPluginConfig {
    private static final Logger LOGGER = LogManager.getLogger(HilogConfig.class);
    private static final String TRACE_PLUGIN_NAME = "/data/local/tmp/libhilogplugin.z.so";

    @Override
    public PluginConf createConfig() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createConfig");
        }
        PluginConf hilogConfig = new PluginConf(TRACE_PLUGIN_NAME, "", null, false, null);
        hilogConfig.setPluginMode(PluginMode.OFFLINE);
        hilogConfig.addAnalysisTypes(AnalysisType.APPLICATION_TYPE);
        hilogConfig.addAnalysisTypes(AnalysisType.SYSTEM_TYPE);
        return hilogConfig;
    }
}
