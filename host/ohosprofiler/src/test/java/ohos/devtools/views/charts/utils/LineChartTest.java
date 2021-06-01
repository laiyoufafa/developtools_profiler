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

package ohos.devtools.views.charts.utils;

import ohos.devtools.views.charts.LineChart;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.swing.TaskScenePanelChart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Chart相关的工具类的测试类
 *
 * @since 2021/4/25 14:02
 */
public class LineChartTest {
    /**
     * ProfilerChartsView
     */
    private ProfilerChartsView view;

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
    public void LineChartTest() {
        LineChart lineChart = new LineChart(view);
        Assert.assertNotNull(lineChart);
    }
}
