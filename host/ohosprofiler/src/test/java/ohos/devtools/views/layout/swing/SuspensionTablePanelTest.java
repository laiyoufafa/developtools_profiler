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

package ohos.devtools.views.layout.swing;

import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;

/**
 * @Description SuspensionTablePanelTest
 * @Date 2021/4/3 10:33
 **/
public class SuspensionTablePanelTest {
    /**
     * SESSIONID
     */
    private static final long SESSIONID = 0L;

    /**
     * SuspensionTablePanel
     */
    private SuspensionTablePanel suspensionTablePanel = new SuspensionTablePanel();

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_SuspensionTablePanel_createSuspensionTableTest_0001
     * @tc.desc: chart table test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-005
     */
    @Before
    public void createSuspensionTableTest() {
        SessionManager.getInstance().setDevelopMode(true);
        TaskScenePanelChart panel = new TaskScenePanelChart();
        ProfilerChartsView view = new ProfilerChartsView(SESSIONID, true, panel);
        ProfilerChartsView.sessionMap.put(SESSIONID, view);

        view.getObserver().getStandard().updateSelectedStart(3);
        view.getObserver().getStandard().updateSelectedEnd(34453);
        JPanel suspensionTable = suspensionTablePanel.createSuspensionTable(new JPanel(), 1, SESSIONID, "test");
        Assert.assertNotNull(suspensionTable);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_SuspensionTablePanel_initData_0001
     * @tc.desc: chart table test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-005
     */
    @Test
    public void initDataTest() {
        suspensionTablePanel.initData(new DefaultTableModel(), 1, "test", SESSIONID);
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_SuspensionTablePanel_createChildTable_0001
     * @tc.desc: chart table test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-005
     */
    @Test
    public void createChildTableTrueTest() {
        suspensionTablePanel.createChildTable(1, true, new JPanel(), new JLayeredPane());
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_SuspensionTablePanel_createChildTable_0001
     * @tc.desc: chart table test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-005
     */
    @Test
    public void createChildTableFalseTest() {
        suspensionTablePanel.createChildTable(1, false, new JPanel(), new JLayeredPane());
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_SuspensionTablePanel_selectData_0001
     * @tc.desc: chart table test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-005
     */
    @Test
    public void selectDataTest() {
        suspensionTablePanel.selectData();
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_SuspensionTablePanel_insertData_0001
     * @tc.desc: chart table test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-005
     */
    @Test
    public void insertDataTest() {
        suspensionTablePanel.insertData(new DefaultTableModel(), "2", "");
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_SuspensionTablePanel_selectData_0001
     * @tc.desc: chart table test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-005
     */
    @Test
    public void selectData() {
        suspensionTablePanel.selectData(new DefaultTableModel());
        Assert.assertTrue(true);
    }
}