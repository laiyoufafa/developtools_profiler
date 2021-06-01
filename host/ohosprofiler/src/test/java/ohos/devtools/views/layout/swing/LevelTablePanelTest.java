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

import ohos.devtools.views.common.chart.treetable.DataNode;
import ohos.devtools.views.common.chart.treetable.JTreeTable;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JPanel;

/**
 * @Description LevelTablePanelTest
 * @Date 2021/4/2 12:57
 **/
public class LevelTablePanelTest {
    private JPanel jPanel = new JPanel();
    private LevelTablePanel LevelTablePanel;
    private JTreeTable jTreeTable;

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_LevelTablePanel_getLevelTablePanel_0001
     * @tc.desc: chart table test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-004
     */
    @Before
    public void getLevelTablePanel() {
        TaskScenePanelChart panel = new TaskScenePanelChart();
        ProfilerChartsView view = new ProfilerChartsView(0L, true, panel);
        ProfilerChartsView.sessionMap.put(0L, view);

        view.getObserver().getStandard().updateSelectedStart(3);
        view.getObserver().getStandard().updateSelectedEnd(34453);
        LevelTablePanel = new LevelTablePanel(jPanel, 0L);
        Assert.assertNotNull(LevelTablePanel);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_LevelTablePanel_createTable_0001
     * @tc.desc: chart table test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-004
     */
    @Test
    public void createTableTest() {
        LevelTablePanel.createTable(jPanel, 0L);
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_LevelTablePanel_getSuspensionTable_0001
     * @tc.desc: chart table test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-004
     */
    @Test
    public void getSuspensionTableTest() {
        LevelTablePanel.getSuspensionTable();
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_LevelTablePanel_setSuspensionTable_0001
     * @tc.desc: chart table test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-004
     */
    @Test
    public void setSuspensionTableTest() {
        LevelTablePanel.setSuspensionTable(jPanel);
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_LevelTablePanel_initData_0001
     * @tc.desc: chart table test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-004
     */
    @Test
    public void initDataTest() {
        LevelTablePanel.initData(0L);
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_LevelTablePanel_buildClassNode_0001
     * @tc.desc: chart table test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-004
     */
    @Test
    public void buildClassNodeTest() {
        DataNode dataNode = LevelTablePanel.buildClassNode(null);
        Assert.assertNotNull(dataNode);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_LevelTablePanel_buildClassNode_0002
     * @tc.desc: chart table test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-004
     */
    @Test
    public void buildClassNodeTest01() {
        DataNode dataNode01 = LevelTablePanel.buildClassNode(null);
        DataNode dataNode02 = LevelTablePanel.buildClassNode(null);
        Assert.assertEquals(dataNode01, dataNode02);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_LevelTablePanel_selectData_0001
     * @tc.desc: chart table test
     * @tc.type: functional testing
     * @tc.require: SR-001-AR-004
     */
    @Test
    public void selectDataTest() {
        JPanel jPanels = LevelTablePanel.selectData(jTreeTable);
        Assert.assertNotNull(jPanels);
    }

}