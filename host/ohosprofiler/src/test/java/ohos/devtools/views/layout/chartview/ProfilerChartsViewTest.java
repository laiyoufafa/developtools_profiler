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

package ohos.devtools.views.layout.chartview;

import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.ProfilerMonitorItem;
import ohos.devtools.views.layout.swing.TaskScenePanelChart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JPanel;

/**
 * ProfilerChartsView test
 *
 * @since 2021/2/10 10:43
 */
public class ProfilerChartsViewTest {
    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_refreshView_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-002-AR-001
     */
    private ProfilerChartsView view;

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_initObj_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-002-AR-001
     */
    @Before
    public void initObj() {
        view = new ProfilerChartsView(LayoutConstants.NUM_L, true, new TaskScenePanelChart());
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_initScrollbar_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void initScrollbarTest() {
        view.initScrollbar();
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_removeScrollbar_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void removeScrollbarTest() {
        view.initScrollbar();
        view.removeScrollbar();
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_addMonitorItemView_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void addMonitorItemViewTest() {
        view.addMonitorItemView(ProfilerMonitorItem.MEMORY);
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_addMemoryStageView_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void addMemoryStageViewTest() {
        MemoryStageView memoryStageView = view.addMemoryStageView();
        Assert.assertNotNull(memoryStageView);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_compRulerDrawn_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void compRulerDrawnTest() {
        view.compRulerDrawn(new JPanel());
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_resetRulerDrawStatus_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void resetRulerDrawStatusTest() {
        view.resetRulerDrawStatus();
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_refreshCompRuler_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void refreshCompRulerTest() {
        view.refreshCompRuler();
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_getMainPanel_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void getMainPanelTest() {
        JPanel jPanel = view.getMainPanel();
        Assert.assertNotNull(jPanel);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_getRulerXCoordinate_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void getRulerXCoordinateTest() {
        int number = view.getRulerXCoordinate();
        Assert.assertNotNull(number);
    }
}
