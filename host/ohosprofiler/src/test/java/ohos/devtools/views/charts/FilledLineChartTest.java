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

import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.TaskScenePanelChart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Filled Line Chart Test
 */
public class FilledLineChartTest {
    private ProfilerChartsView profilerChartsView;

    private FilledLineChart filledLineChart;

    /**
     * functional test
     *
     * @tc.name: getFilledLineChart
     * @tc.number: OHOS_JAVA_View_FilledLineChart_getFilledLineChart_0001
     * @tc.desc: getFilledLineChart
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Before
    public void getFilledLineChart() {
        profilerChartsView = new ProfilerChartsView(39999, true, new TaskScenePanelChart());
        filledLineChart = new FilledLineChart(profilerChartsView, "", true);
        Assert.assertNotNull(filledLineChart);
    }

    /**
     * functional test
     *
     * @tc.name: paintComponent
     * @tc.number: OHOS_JAVA_View_FilledLineChart_paintComponent_0001
     * @tc.desc: paintComponent
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void paintComponentTest() {
        filledLineChart.revalidate();
    }
}