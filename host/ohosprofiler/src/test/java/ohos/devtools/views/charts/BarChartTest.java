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

import java.util.ArrayList;

/**
 * Bar Chart Test
 *
 * @since 2021/2/1 9:31
 */
public class BarChartTest {
    private TaskScenePanelChart taskScenePanelChart;
    private ProfilerChartsView bottomPanel;
    private BarChart bar;

    /**
     * init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_View_BarChart_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: SR000FK5SL
     */
    @Before
    public void init() {
        taskScenePanelChart = new TaskScenePanelChart();
        bottomPanel = new ProfilerChartsView(1L, true, taskScenePanelChart);
        bar = new BarChart(bottomPanel, "name", new ArrayList<>());
    }

    /**
     * Bar Chart Test
     *
     * @tc.name: BarChartTest
     * @tc.number: OHOS_JAVA_View_BarChart_BarChartTest_0001
     * @tc.desc: BarChartTest
     * @tc.type: functional testing
     * @tc.require: SR000FK5SL
     */
    @Test
    public void barChartTest() {
        BarChart barChart = new BarChart(bottomPanel, "name", new ArrayList<>());
        Assert.assertNotNull(barChart);
    }
}