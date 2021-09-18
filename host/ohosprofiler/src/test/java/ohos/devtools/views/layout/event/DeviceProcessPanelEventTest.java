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

package ohos.devtools.views.layout.event;

import com.intellij.ui.components.JBPanel;
import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.utils.device.service.MultiDeviceManager;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.views.layout.DeviceProcessPanel;
import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Device Process panel Event Test
 */
public class DeviceProcessPanelEventTest {
    private DeviceProcessPanelEvent deviceProcessPanelEvent;
    private DeviceProcessPanel deviceProcessJpanel;
    private List<ProcessInfo> processInfos;

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_DeviceProcessJpanelEvent_init_0001
     * @tc.desc: chart test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-001
     */
    @Before
    public void init() {
        SessionManager.getInstance().setDevelopMode(true);
        ProfilerLogManager.getSingleton().updateLogLevel(Level.ERROR);
        DataBaseApi apo = DataBaseApi.getInstance();
        apo.initDataSourceManager();
        MultiDeviceManager.getInstance();
        processInfos = new ArrayList<>();
        ProcessInfo process = new ProcessInfo();
        process.setDeviceId("fffff");
        process.setProcessId(43543);
        process.setProcessName("435gs");
        processInfos.add(process);
        deviceProcessJpanel = new DeviceProcessPanel(new JBPanel(), 1, 0);
        deviceProcessPanelEvent = new DeviceProcessPanelEvent();
        deviceProcessPanelEvent.searchJButtonSelect(deviceProcessJpanel, processInfos);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_DeviceProcessJpanelEvent_searchJButtonSelect_0001
     * @tc.desc: chart test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-001
     */
    @Test
    public void searchJButtonSelectTest() {
        List<ProcessInfo> processInfo = new ArrayList<>();
        deviceProcessPanelEvent.searchJButtonSelect(deviceProcessJpanel, processInfo);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_DeviceProcessJpanelEvent_itemStateChanged_0001
     * @tc.desc: chart test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-001
     */
    @Test
    public void itemStateChangedTest01() {
        deviceProcessPanelEvent.itemStateChanged(deviceProcessJpanel);
    }
}