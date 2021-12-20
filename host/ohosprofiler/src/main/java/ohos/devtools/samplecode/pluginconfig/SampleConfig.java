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

package ohos.devtools.samplecode.pluginconfig;

import ohos.devtools.datasources.transport.grpc.service.SamplePluginConfig;
import ohos.devtools.datasources.utils.plugin.IPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.AnalysisType;
import ohos.devtools.datasources.utils.plugin.entity.DPlugin;
import ohos.devtools.datasources.utils.plugin.entity.HiProfilerPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.PluginConf;
import ohos.devtools.datasources.utils.plugin.entity.PluginMode;
import ohos.devtools.samplecode.datasources.utils.datahandler.datapoller.SampleCodeConsumer;
import ohos.devtools.samplecode.views.layout.chartview.SampleCodeView;
import ohos.devtools.views.layout.chartview.ProfilerMonitorItem;

/**
 * SampleConfig
 *
 * @since 2021/11/24
 */
@DPlugin
public class SampleConfig extends IPluginConfig {
    /**
     * SAMPLE_PLUG
     */
    public static final String SAMPLE_PLUG = "sample-plugin";

    /**
     * SAMPLE_PLUGIN_NAME
     */
    private static final String SAMPLE_PLUGIN_NAME = "/data/local/tmp/libsampleplugin.z.so";

    /**
     * createConfig
     *
     * @return PluginConf
     */
    @Override
    public PluginConf createConfig() {
        ProfilerMonitorItem sampleItem = new ProfilerMonitorItem(5, "sample", SampleCodeView.class);
        PluginConf sample = new PluginConf(SAMPLE_PLUGIN_NAME, SAMPLE_PLUG, SampleCodeConsumer.class, true, sampleItem);
        sample.setICreatePluginConfig((deviceIPPortInfo, processInfo) -> {
            SamplePluginConfig.SampleConfig.Builder builder =
                SamplePluginConfig.SampleConfig.newBuilder().setPid(processInfo.getProcessId());
            return new HiProfilerPluginConfig(40, builder.build().toByteString());
        });
        sample.setPluginMode(PluginMode.ONLINE);
        sample.addAnalysisTypes(AnalysisType.APPLICATION_TYPE);
        return sample;
    }
}