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

package ohos.devtools.fixture;

import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.utils.device.service.MultiDeviceManager;
import ohos.devtools.datasources.utils.plugin.IPluginConfig;
import ohos.devtools.datasources.utils.plugin.service.PlugManager;
import ohos.devtools.datasources.utils.process.service.ProcessManager;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.pluginconfig.AgentConfig;
import ohos.devtools.pluginconfig.BytraceConfig;
import ohos.devtools.pluginconfig.CpuConfig;
import ohos.devtools.pluginconfig.DiskIoConfig;
import ohos.devtools.pluginconfig.FtraceConfig;
import ohos.devtools.pluginconfig.HilogConfig;
import ohos.devtools.pluginconfig.MemoryConfig;
import ohos.devtools.pluginconfig.ProcessConfig;
import ohos.devtools.pluginconfig.UserConfig;
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.layout.HomePanel;
import ohos.devtools.views.user.UserManager;
import org.apache.logging.log4j.Level;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JFrame;
import java.awt.AWTException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Close Main Interface Fixture Test
 *
 * @since 2021/2/1 9:31
 */
public class CloseMainInterfaceFixtureTest {
    private FrameFixture frameFixture;

    /**
     * init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     * @throws AWTException AWTException
     */
    @Before
    public void init() throws AWTException {
        // Initialization data
        ProcessManager.getInstance().setIsRequest(false);
        SessionManager.getInstance().settingPermissions();
        ProfilerLogManager.getSingleton().updateLogLevel(Level.ERROR);
        PlugManager.getInstance().unzipStdDevelopTools();
        DataBaseApi.getInstance().initDataSourceManager();
        MultiDeviceManager.getInstance().start();
        List<Class<? extends IPluginConfig>> plugConfigList = new ArrayList();
        plugConfigList.add(ProcessConfig.class);
        plugConfigList.add(AgentConfig.class);
        plugConfigList.add(BytraceConfig.class);
        plugConfigList.add(FtraceConfig.class);
        plugConfigList.add(CpuConfig.class);
        plugConfigList.add(HilogConfig.class);
        if (Objects.nonNull(UserManager.getInstance().getSdkImpl())) {
            plugConfigList.add(UserConfig.class);
        }
        plugConfigList.add(DiskIoConfig.class);
        plugConfigList.add(MemoryConfig.class);
        PlugManager.getInstance().loadingPlugs(plugConfigList);
        while (true) {
            if (MultiDeviceManager.getInstance().getOnlineDeviceInfoList().size() > 0) {
                break;
            }
        }
        JFrame frame = new JFrame();
        frame.add(new HomePanel());
        frameFixture = new FrameFixture(frame);
        // Display the frame
        frameFixture.show();
    }

    /**
     * test Window
     *
     * @tc.name: testWindow
     * @tc.number: OHOS_JAVA_testWindow_0001
     * @tc.desc: test Window
     * @tc.type: test Window
     * @tc.require: AR000FK5UI
     * @throws InterruptedException InterruptedException
     */
    @Test
    public void testWindow() throws InterruptedException {
        frameFixture.label(UtConstant.UT_WELCOME_PANEL_NEW_TASK_BTN).click();
        frameFixture.label(UtConstant.UT_TASK_PANEL_APPLICATION).click();
        frameFixture.button(UtConstant.UT_TASK_PANEL_CHOOSE).requireText(UtConstant.UT_TASK_PANEL_CHOOSE);
        frameFixture.button(UtConstant.UT_TASK_PANEL_CHOOSE).click();
        TimeUnit.SECONDS.sleep(5);
        frameFixture.button(UtConstant.UT_TASK_SCENE_PANE_START).click();
        TimeUnit.SECONDS.sleep(10);
        // Close the main interface
        frameFixture.panel(UtConstant.UT_TASK_PANEL_CLOSE).click();
        TimeUnit.SECONDS.sleep(5);
        SessionManager.getInstance().stopAllSession();
        TimeUnit.SECONDS.sleep(6);

    }

    /**
     * tear Down
     *
     * @tc.name: tearDown
     * @tc.number: OHOS_JAVA_tearDown_0001
     * @tc.desc: tear Down
     * @tc.type: test Window
     * @tc.require: AR000FK5UI
     */
    @After
    public void tearDown() {
        frameFixture.close();
        frameFixture.cleanUp();
    }
}
