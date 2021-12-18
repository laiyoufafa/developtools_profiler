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

import com.google.protobuf.ByteString;

import model.AbstractSdk;
import ohos.devtools.datasources.utils.datahandler.datapoller.UserDataConsumer;
import ohos.devtools.datasources.utils.plugin.IPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.AnalysisType;
import ohos.devtools.datasources.utils.plugin.entity.DPlugin;
import ohos.devtools.datasources.utils.plugin.entity.HiProfilerPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.PluginConf;
import ohos.devtools.datasources.utils.plugin.entity.PluginMode;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.layout.chartview.ProfilerMonitorItem;
import ohos.devtools.views.layout.chartview.user.UserItemView;
import ohos.devtools.views.user.UserManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Sdk Config
 *
 * @since 2021/11/25
 */
@DPlugin
public class UserConfig extends IPluginConfig {
    private static final Logger LOGGER = LogManager.getLogger(UserConfig.class);
    private static final String BASEPATH = "/data/local/tmp/";

    /**
     * createConfig
     *
     * @return PluginConf
     */
    @Override
    public PluginConf createConfig() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createConfig");
        }
        UserManager management = UserManager.getInstance();
        Optional<AbstractSdk> abstractSdk = management.getSdkImpl();
        AbstractSdk sdkImpl = abstractSdk.get();
        ProfilerMonitorItem sdkItem = new ProfilerMonitorItem(20, sdkImpl.getTitleName(), UserItemView.class);
        String path = sdkImpl.getPluginFileName();
        if (!path.contains(BASEPATH)) {
            path = BASEPATH + sdkImpl.getPluginFileName();
        }
        PluginConf sdkConf = new PluginConf(path, sdkImpl.getPluginDataName(), UserDataConsumer.class, true, sdkItem);
        sdkConf.setICreatePluginConfig((deviceIPPortInfo, processInfo) -> {
            ByteString pluginByteString = sdkImpl.getPluginByteString(processInfo.getProcessId());
            return new HiProfilerPluginConfig(40, pluginByteString);
        });
        sdkConf.setPluginMode(PluginMode.ONLINE);
        sdkConf.addAnalysisTypes(AnalysisType.APPLICATION_TYPE);
        return sdkConf;
    }
}
