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

package ohos.devtools;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;
import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.utils.device.service.MultiDeviceManager;
import ohos.devtools.datasources.utils.plugin.IPluginConfig;
import ohos.devtools.datasources.utils.plugin.service.PlugManager;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.pluginconfig.AgentConfig;
import ohos.devtools.pluginconfig.BytraceConfig;
import ohos.devtools.pluginconfig.CpuConfig;
import ohos.devtools.pluginconfig.FtraceConfig;
import ohos.devtools.pluginconfig.MemoryConfig;
import ohos.devtools.views.layout.HomePanel;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Profiler Tool Window Factory
 */
public class ProfilerToolWindowFactory implements ToolWindowFactory {
    private static final Logger LOGGER = LogManager.getLogger(ProfilerToolWindowFactory.class);

    /**
     * createToolWindowContent
     *
     * @param project project
     * @param toolWindow toolWindow
     */
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        LOGGER.error("ohos Profiler Start OS is {}", System.getProperty("os.name"));
        ProfilerLogManager.getSingleton().updateLogLevel(Level.ERROR);
        DataBaseApi.getInstance().initDataSourceManager();
        MultiDeviceManager.getInstance().start();
        List<Class<? extends IPluginConfig>> plugConfigList = new ArrayList();
        plugConfigList.add(AgentConfig.class);
        plugConfigList.add(BytraceConfig.class);
        plugConfigList.add(CpuConfig.class);
        plugConfigList.add(MemoryConfig.class);
        PlugManager.getInstance().loadingPlugs(plugConfigList);
        toolWindow.getContentManager()
            .addContent(ContentFactory.SERVICE.getInstance().createContent(new HomePanel(), "", false));
        // hook the Runtime thread
        Runtime.getRuntime().addShutdownHook(new WindowShutdownHook());
    }

    private class WindowShutdownHook extends Thread {
        @Override
        public void run() {
            SessionManager.getInstance().stopAllSession();
        }
    }
}
