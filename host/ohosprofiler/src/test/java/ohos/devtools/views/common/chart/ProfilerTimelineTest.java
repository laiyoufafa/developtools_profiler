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

package ohos.devtools.views.common.chart;

import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.swing.TaskScenePanelChart;
import org.junit.Before;
import org.junit.Test;

/**
 * @Description profiler time line test
 * @Date 2021/4/3 20:29
 **/
public class ProfilerTimelineTest {
    private ProfilerTimeline profilerTimeline;

    private ProfilerChartsView profilerChartsView;

    private TaskScenePanelChart taskScenePanelChart;

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: DFX_DFT_Hilog_Java_views_0005
     * @tc.desc: chart Timeline functional test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Before
    public void init() {
        taskScenePanelChart = new TaskScenePanelChart();
        profilerChartsView = new ProfilerChartsView(23423L, true, new TaskScenePanelChart());
        profilerTimeline = new ProfilerTimeline(profilerChartsView, 200, 200, taskScenePanelChart);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerTimeline_addTablePanel_0001
     * @tc.desc: chart Timeline functional test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void addTablePanel01() {
        profilerTimeline.addTablePanel();
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerTimeline_removeTablePanel_0001
     * @tc.desc: chart Timeline functional test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void removeTablePanel01() {
        profilerTimeline.removeTablePanel();
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerTimeline_setEndTime_0001
     * @tc.desc: chart Timeline functional test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void setEndTime01() {
        profilerTimeline.setEndTime(234);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerTimeline_setMaxDisplayTime_0001
     * @tc.desc: chart Timeline functional test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void setMaxDisplayTime01() {
        profilerTimeline.setMaxDisplayTime(234);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerTimeline_setMinMarkInterval_0001
     * @tc.desc: chart Timeline functional test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void setMinMarkInterval01() {
        profilerTimeline.setMinMarkInterval(234);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerTimeline_setStartTime_0001
     * @tc.desc: chart Timeline functional test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void setStartTime01() {
        profilerTimeline.setStartTime(234);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerTimeline_getEndTime_0001
     * @tc.desc: chart Timeline functional test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void getEndTime01() {
        profilerTimeline.getEndTime();
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerTimeline_getStartTime_0001
     * @tc.desc: chart Timeline functional test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void getStartTime01() {
        profilerTimeline.getStartTime();
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerTimeline_paint_0001
     * @tc.desc: chart Timeline functional test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void paint01() {
        profilerTimeline.revalidate();
        profilerTimeline.repaint();
    }

}