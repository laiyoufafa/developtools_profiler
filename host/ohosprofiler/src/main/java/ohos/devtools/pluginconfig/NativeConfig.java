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

import ohos.devtools.datasources.transport.grpc.service.NativeHookConfigOuterClass;
import ohos.devtools.datasources.utils.monitorconfig.entity.ConfigInfo;
import ohos.devtools.datasources.utils.monitorconfig.entity.NativeHookConfigInfo;
import ohos.devtools.datasources.utils.plugin.IPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.AnalysisType;
import ohos.devtools.datasources.utils.plugin.entity.DPlugin;
import ohos.devtools.datasources.utils.plugin.entity.HiProfilerPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.PluginBufferConfig;
import ohos.devtools.datasources.utils.plugin.entity.PluginConf;
import ohos.devtools.datasources.utils.plugin.entity.PluginMode;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static ohos.devtools.datasources.utils.device.entity.DeviceType.LEAN_HOS_DEVICE;

/**
 * AgentConfig
 *
 * @since 2021/11/22
 */
@DPlugin
public class NativeConfig extends IPluginConfig {
    /**
     * NATIVE_HOOK_PLUGIN_NAME
     */
    public static final String NATIVE_HOOK_PLUGIN_NAME = "/data/local/data/libnative_hook.z.so";
    private static final Logger LOGGER = LogManager.getLogger(NativeConfig.class);

    @Override
    public PluginConf createConfig() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createConfig");
        }
        PluginConf nativeConfig =
            new PluginConf(NATIVE_HOOK_PLUGIN_NAME, "hookdaemon", null, false, null);
        nativeConfig.setICreatePluginConfig((deviceIPPortInfo, processInfo) -> {
            NativeHookConfigInfo nativeHookConfigInfo = ConfigInfo.getInstance().getNativeHookConfigInfo();
            String fileName = "/data/local/tmp/" + processInfo.getProcessId() + ".nativeHeap";
            NativeHookConfigOuterClass.NativeHookConfig nativeHookConfig = NativeHookConfigOuterClass.NativeHookConfig
                .newBuilder().setPid(processInfo.getProcessId()).setSaveFile(true)
                .setFileName(fileName).setFilterSize(nativeHookConfigInfo.getFilterSizeValue())
                .setSmbPages(nativeHookConfigInfo.getSharedMemorySizeValue())
                .setMaxStackDepth(nativeHookConfigInfo.getUnwind()).build();
            LOGGER.error("nativeHookConfig {}", nativeHookConfig.toString());
            return new HiProfilerPluginConfig(40, nativeHookConfig.toByteString());
        });
        nativeConfig.setPluginBufferConfig(new PluginBufferConfig(3000, PluginBufferConfig.Policy.RECYCLE));
        nativeConfig.setOperationStart(true);
        nativeConfig.setPluginMode(PluginMode.ONLINE);
        nativeConfig.addSupportDeviceTypes(LEAN_HOS_DEVICE);
        nativeConfig.addAnalysisTypes(AnalysisType.APPLICATION_TYPE);
        return nativeConfig;
    }
}
