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

package ohos.devtools.views.layout.event;

import ohos.devtools.views.common.hoscomp.HosJButton;
import ohos.devtools.views.layout.swing.TaskScenePanelChart;
import org.junit.Before;
import org.junit.Test;

/**
 * @Description TaskPanelEventTest
 * @Date 2021/4/3 14:35
 **/
public class TaskScenePanelChartEventTest {
    private TaskScenePanelChartEvent taskScenePanelChartEvent;

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskScenePanelChartEvent_getTaskScenePanelChartEvent_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Before
    public void getTaskScenePanelChartEvent() {
        taskScenePanelChartEvent = new TaskScenePanelChartEvent();
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskScenePanelChartEvent_clickDelete_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void clickDelete() {
        taskScenePanelChartEvent.clickDelete(new TaskScenePanelChart());
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TaskScenePanelChartEvent_getjButtonUp_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void getjButtonUp() {
        TaskScenePanelChart taskScenePanelChart = new TaskScenePanelChart();
        taskScenePanelChartEvent.clickDelete(taskScenePanelChart);
        HosJButton jButtonDelete = taskScenePanelChart.getJButtonDelete();
        jButtonDelete.doClick();
    }

}