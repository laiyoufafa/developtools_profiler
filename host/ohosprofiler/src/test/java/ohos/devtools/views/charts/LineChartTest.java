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

import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.TaskScenePanelChart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class of Chart-related tool classes
 *
 * @since 2021/2/1 9:31
 */
public class LineChartTest {
    /**
     * ProfilerChartsView
     */
    private ProfilerChartsView view;
    private LineChart lineC;

    /**
     * functional testing
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_View_LineChart_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Before
    public void init() {
        view = new ProfilerChartsView(LayoutConstants.NUM_L, true, new TaskScenePanelChart());
        lineC = new LineChart(view, "");
    }

    /**
     * functional testing
     *
     * @tc.name: LineChartTest
     * @tc.number: OHOS_JAVA_View_LineChart_LineChartTest_0001
     * @tc.desc: LineChartTest
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void lineChartTest() {
        LineChart lineChart = new LineChart(view, "");
        Assert.assertNotNull(lineChart);
    }
}
