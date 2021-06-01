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

import ohos.devtools.views.charts.model.ChartDataModel;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.swing.TaskScenePanelChart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Chart的抽象父类的测试类
 *
 * @since 2021/3/31 16:26
 */
public class ProfilerChartTest {
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
    }

    private void initView() {
        view = new ProfilerChartsView(LayoutConstants.NUM_L, true, new TaskScenePanelChart());
        view.getObserver().getStandard().updateDisplayTimeRange(TEST_START, TEST_END);
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
        ProfilerChart chart = new FilledLineChart(view);
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
        ProfilerChartsView profilerChartsView = new FilledLineChart(view).getBottomPanel();
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
        int endTime = new FilledLineChart(view).getEndTime();
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
        int startTime = new FilledLineChart(view).getStartTime();
        Assert.assertNotNull(startTime);
    }

}
