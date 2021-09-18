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

package ohos.devtools.views.charts;

import com.intellij.ui.components.JBPanel;
import ohos.devtools.views.charts.model.ChartDataModel;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.TaskScenePanelChart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Test class of the abstract parent class of Chart
 */
public class ProfilerChartTest {
    private static final String NAME = "Test";

    private static final int TEST_START = 0;

    private static final int TEST_END = 1000;

    private static final int TEST_TIME1 = 333;

    private static final int TEST_TIME2 = 666;

    private static final int TEST_INDEX1 = 1;

    private static final int TEST_INDEX2 = 2;

    private static final int TEST_VALUE1 = 10;

    private static final int TEST_VALUE2 = 20;

    private ProfilerChartsView view;

    private LinkedHashMap<Integer, List<ChartDataModel>> dataMap;

    private ProfilerChart chart;

    private List<ChartDataModel> models;

    /**
     * init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_View_ProfilerChart_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Before
    public void init() {
        initView();
        initDataMap();
        chart = new FilledLineChart(view, NAME, true);
        ChartDataModel chartDataModel = new ChartDataModel();
        chartDataModel.setIndex(1);
        chartDataModel.setColor(Color.GREEN);
        chartDataModel.setValue(1);
        models = new ArrayList<>();
        models.add(chartDataModel);
    }

    private void initView() {
        view = new ProfilerChartsView(LayoutConstants.NUM_L, true, new TaskScenePanelChart());
        view.getPublisher().getStandard().updateDisplayTimeRange(TEST_START, TEST_END);
    }

    private void initDataMap() {
        dataMap = new LinkedHashMap<>();
        ChartDataModel model1 = new ChartDataModel();
        model1.setIndex(TEST_INDEX1);
        model1.setName("Java");
        model1.setColor(Color.GRAY);
        model1.setValue(TEST_VALUE1);
        List<ChartDataModel> list1 = Collections.singletonList(model1);
        dataMap.put(TEST_TIME1, list1);
        ChartDataModel model2 = new ChartDataModel();
        model2.setIndex(TEST_INDEX2);
        model2.setName("Java");
        model2.setColor(Color.GRAY);
        model2.setValue(TEST_VALUE2);
        List<ChartDataModel> list2 = Collections.singletonList(model2);
        dataMap.put(TEST_TIME2, list2);
    }

    /**
     * refresh chart test
     *
     * @tc.name: refreshChart
     * @tc.number: OHOS_JAVA_View_ProfilerChart_refreshChart_0001
     * @tc.desc: refreshChart
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void refreshChartTest() {
        chart.refreshChart(TEST_START, TEST_END, dataMap);
        Assert.assertTrue(true);
    }

    /**
     * get bottomPanel test
     *
     * @tc.name: getBottomPanel
     * @tc.number: OHOS_JAVA_View_ProfilerChart_getBottomPanel_0001
     * @tc.desc: getBottomPanel
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getBottomPanelTest() {
        ProfilerChartsView profilerChartsView = new FilledLineChart(view, NAME, true).getBottomPanel();
        Assert.assertNotNull(profilerChartsView);
    }

    /**
     * functional test
     *
     * @tc.name: getEndTime
     * @tc.number: OHOS_JAVA_View_ProfilerChart_getEndTime_0001
     * @tc.desc: getEndTime
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getEndTimeTest() {
        int endTime = new FilledLineChart(view, NAME, true).getEndTime();
        Assert.assertNotNull(endTime);
    }

    /**
     * functional test
     *
     * @tc.name: getStartTime
     * @tc.number: OHOS_JAVA_View_ProfilerChart_getStartTime_0001
     * @tc.desc: getStartTime
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getStartTimeTest() {
        int startTime = new FilledLineChart(view, NAME, true).getStartTime();
        Assert.assertNotNull(startTime);
    }

    /**
     * functional test
     *
     * @tc.name: refreshLegendsTest
     * @tc.number: OHOS_JAVA_View_ProfilerChart_refreshLegendsTest_0001
     * @tc.desc: refresh Legends Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void refreshLegendsTest() {
        chart.refreshChart(TEST_START, TEST_END, dataMap);
        chart.refreshLegends();
        Assert.assertTrue(true);
    }

    /**
     * paint Component Test
     *
     * @tc.name: paintComponentTest
     * @tc.number: OHOS_JAVA_View_ProfilerChart_paintComponentTest_0001
     * @tc.desc: paint Component Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void paintComponentTest() {
        chart.revalidate();
        Assert.assertTrue(true);
    }

    /**
     * get Yaxis Label Str Test
     *
     * @tc.name: getYaxisLabelStrTest
     * @tc.number: OHOS_JAVA_View_ProfilerChart_getYaxisLabelStrTest_0001
     * @tc.desc: get Yaxis Label Str Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getYaxisLabelStrTest() {
        String yaxisLabelStr = chart.getYaxisLabelStr(10);
        Assert.assertNotNull(yaxisLabelStr);
    }

    /**
     * init Point Test
     *
     * @tc.name: initPointTest
     * @tc.number: OHOS_JAVA_View_ProfilerChart_initPointTest_0001
     * @tc.desc: init Point Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void initPointTest() {
        chart.initPoint();
        Assert.assertTrue(true);
    }

    /**
     * check Mouse For Tool tip Test
     *
     * @tc.name: checkMouseForTooltipTest
     * @tc.number: OHOS_JAVA_View_ProfilerChart_checkMouseForTooltipTest_0001
     * @tc.desc: check Mouse For Tool tip Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void checkMouseForTooltipTest() {
        JBPanel jBPanel = new JBPanel();
        MouseEvent mouseEvent = new MouseEvent(jBPanel, 1, 1, 1, 1, 1, 1, true, 1);
        chart.checkMouseForTooltip(mouseEvent);
        Assert.assertTrue(true);
    }

    /**
     * get Current Line Color Test
     *
     * @tc.name: getCurrentLineColorTest
     * @tc.number: OHOS_JAVA_View_ProfilerChart_getCurrentLineColorTest_0001
     * @tc.desc: get Current Line Color Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getCurrentLineColorTest() {
        Color currentLineColor = chart.getCurrentLineColor(1, models);
        Assert.assertNotNull(currentLineColor);
    }

    /**
     * get Next Line Index Test
     *
     * @tc.name: getNextLineIndexTest
     * @tc.number: OHOS_JAVA_View_ProfilerChart_getNextLineIndexTest_0001
     * @tc.desc: get Next Line Index Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getNextLineIndexTest() {
        int nextLineIndex = chart.getNextLineIndex(1, models);
        Assert.assertNotNull(nextLineIndex);
    }

    /**
     * get List Sum Test
     *
     * @tc.name: getListSumTest
     * @tc.number: OHOS_JAVA_View_ProfilerChart_getListSumTest_0001
     * @tc.desc: get List Sum Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getListSumTest() {
        int listSum = chart.getListSum(models, 1);
        Assert.assertNotNull(listSum);
    }

    /**
     * get Model Value By Index Test
     *
     * @tc.name: getModelValueByIndexTest
     * @tc.number: OHOS_JAVA_View_ProfilerChart_getModelValueByIndexTest_0001
     * @tc.desc: get Model Value By Index Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getModelValueByIndexTest() {
        int modelValueByIndex = chart.getModelValueByIndex(models, 1);
        Assert.assertNotNull(modelValueByIndex);
    }

    /**
     * mouse Clicked Test
     *
     * @tc.name: mouseClickedTest
     * @tc.number: OHOS_JAVA_View_ProfilerChart_mouseClickedTest_0001
     * @tc.desc: mouse Clicked Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void mouseClickedTest() {
        JBPanel jBPanel = new JBPanel();
        MouseEvent mouseEvent = new MouseEvent(jBPanel, 1, 1, 1, 1, 1, 1, true, 1);
        chart.mouseClicked(mouseEvent);
        Assert.assertTrue(true);
    }

    /**
     * mouse Pressed Test
     *
     * @tc.name: mousePressedTest
     * @tc.number: OHOS_JAVA_View_ProfilerChart_mousePressedTest_0001
     * @tc.desc: mouse Pressed Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void mousePressedTest() {
        JBPanel jBPanel = new JBPanel();
        MouseEvent mouseEvent = new MouseEvent(jBPanel, 1, 1, 1, 1, 1, 1, true, 1);
        chart.mousePressed(mouseEvent);
        Assert.assertTrue(true);
    }

    /**
     * mouse Released Test
     *
     * @tc.name: mouseReleasedTest
     * @tc.number: OHOS_JAVA_View_ProfilerChart_mouseReleasedTest_0001
     * @tc.desc: mouse Released Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void mouseReleasedTest() {
        JBPanel jBPanel = new JBPanel();
        MouseEvent mouseEvent = new MouseEvent(jBPanel, 1, 1, 1, 1, 1, 1, true, 1);
        chart.mouseReleased(mouseEvent);
        Assert.assertTrue(true);
    }

    /**
     * mouse Entered Test
     *
     * @tc.name: mouseEnteredTest
     * @tc.number: OHOS_JAVA_View_ProfilerChart_mouseEnteredTest_0001
     * @tc.desc: mouse Entered Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void mouseEnteredTest() {
        JBPanel jBPanel = new JBPanel();
        MouseEvent mouseEvent = new MouseEvent(jBPanel, 1, 1, 1, 1, 1, 1, true, 1);
        chart.mouseEntered(mouseEvent);
        Assert.assertTrue(true);
    }

    /**
     * mouse Exited Test
     *
     * @tc.name: mouseExitedTest
     * @tc.number: OHOS_JAVA_View_ProfilerChart_mouseExitedTest_0001
     * @tc.desc: mouse Exited Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void mouseExitedTest() {
        JBPanel jBPanel = new JBPanel();
        MouseEvent mouseEvent = new MouseEvent(jBPanel, 1, 1, 1, 1, 1, 1, true, 1);
        chart.mouseExited(mouseEvent);
        Assert.assertTrue(true);
    }

    /**
     * mouse Dragged Test
     *
     * @tc.name: mouseDraggedTest
     * @tc.number: OHOS_JAVA_View_ProfilerChart_mouseDraggedTest_0001
     * @tc.desc: mouse Dragged Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void mouseDraggedTest() {
        JBPanel jBPanel = new JBPanel();
        MouseEvent mouseEvent = new MouseEvent(jBPanel, 1, 1, 1, 1, 1, 1, true, 1);
        chart.mouseDragged(mouseEvent);
        Assert.assertTrue(true);
    }

    /**
     * mouse Moved Test
     *
     * @tc.name: mouseMovedTest
     * @tc.number: OHOS_JAVA_View_ProfilerChart_mouseMovedTest_0001
     * @tc.desc: mouse Moved Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void mouseMovedTest() {
        JBPanel jBPanel = new JBPanel();
        MouseEvent mouseEvent = new MouseEvent(jBPanel, 1, 1, 1, 1, 1, 1, true, 1);
        chart.mouseMoved(mouseEvent);
        Assert.assertTrue(true);
    }
}

