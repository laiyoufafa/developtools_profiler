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
 * Rect Chart Test
 */
public class RectChartTest {
    private static final String NAME = "Test";
    private static final int TEST_START = 0;
    private static final int TEST_END = 1000;
    private ProfilerChartsView view;

    /**
     * init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_View_RectChart_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Before
    public void init() {
        initView();
    }

    private void initView() {
        view = new ProfilerChartsView(LayoutConstants.NUM_L, true, new TaskScenePanelChart());
        view.getPublisher().getStandard().updateDisplayTimeRange(TEST_START, TEST_END);
    }

    /**
     * Rect Chart Test
     *
     * @tc.name: RectChartTest
     * @tc.number: OHOS_JAVA_View_RectChart_RectChartTest_0001
     * @tc.desc: Rect Chart Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void rectChartTest() {
        RectChart chart = new RectChart(view, NAME);
        Assert.assertNotNull(chart);
    }
}