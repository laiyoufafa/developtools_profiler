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
 * Metrics Button Fixture Test
 *
 * @since 2021/2/1 9:31
 */
public class MetricsButtonFixtureTest {
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
     * system Start Task Test
     *
     * @tc.name: systemStartTaskTest
     * @tc.number: OHOS_JAVA_systemStartTaskTest_0001
     * @tc.desc: system Start Task Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     * @throws InterruptedException InterruptedException
     */
    @Test
    public void systemStartTaskTest() throws InterruptedException {
        frameFixture.label(UtConstant.UT_WELCOME_PANEL_NEW_TASK_BTN).click();
        frameFixture.label(UtConstant.UT_TASK_PANEL_SYSTEM).click();
        frameFixture.button(UtConstant.UT_TASK_PANEL_CHOOSE).click();
        frameFixture.label(UtConstant.UT_SYSTEM_TUNING_LABEL).click();
        frameFixture.checkBox(UtConstant.UT_SYSTEM_TUNING_CATEGORIES).click();
        frameFixture.button(UtConstant.UT_TASK_SCENE_PANE_START).click();
        TimeUnit.SECONDS.sleep(20);
        frameFixture.button(UtConstant.UT_SYSTEM_TUNING_METRICS).click();
        TimeUnit.SECONDS.sleep(5);
        frameFixture.panel(UtConstant.UT_TASK_PANEL_CLOSE).click();
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
        frameFixture.cleanUp();
    }
}
