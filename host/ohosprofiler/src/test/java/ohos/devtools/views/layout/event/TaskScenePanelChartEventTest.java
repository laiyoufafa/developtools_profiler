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

import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import ohos.devtools.datasources.utils.session.entity.SessionInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.common.customcomp.CustomJLabel;
import ohos.devtools.views.layout.TaskPanel;
import ohos.devtools.views.layout.WelcomePanel;
import ohos.devtools.views.layout.chartview.TaskScenePanelChart;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Task Panel Event Test
 *
 * @since 2021/2/1 9:31
 */
public class TaskScenePanelChartEventTest {
    private SessionInfo sessionInfo;
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
        sessionInfo = SessionInfo.builder().sessionId(0).sessionName("Test").pid(2).processName("processName").build();
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
        ArrayList<CustomJLabel> hosJLabels = new ArrayList<CustomJLabel>();
        CustomJLabel hosJLabel = new CustomJLabel();
        hosJLabels.add(hosJLabel);
        Constant.jtasksTab = new JBTabbedPane();
        TaskPanel taskPanel = new TaskPanel(new JBPanel(), new WelcomePanel());
        SessionManager.getInstance().getProfilingSessions().put(0L, sessionInfo);
        TaskScenePanelChart taskScenePanelChart = new TaskScenePanelChart(taskPanel, hosJLabels);
        taskScenePanelChartEvent.clickDelete(taskScenePanelChart);
    }
}