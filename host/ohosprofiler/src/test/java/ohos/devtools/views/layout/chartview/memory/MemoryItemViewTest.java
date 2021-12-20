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

package ohos.devtools.views.layout.chartview.memory;

import com.intellij.ui.components.JBPanel;
import ohos.devtools.datasources.utils.session.entity.SessionInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.chartview.ItemsView;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.ProfilerMonitorItem;
import ohos.devtools.views.layout.chartview.TaskScenePanelChart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Memory Item View Test
 *
 * @since 2021/11/22
 */
public class MemoryItemViewTest {
    private static final int TEST_START = 0;
    private static final int TEST_END = 1000;

    private MemoryItemView memoryItemView;
    private ProfilerChartsView view;
    private SessionInfo sessionInfo;

    private void initView() {
        view = new ProfilerChartsView(LayoutConstants.NUM_L, true, new TaskScenePanelChart());
        view.getPublisher().getStandard().updateDisplayTimeRange(TEST_START, TEST_END);
    }

    /**
     * init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_View_MemoryItemView_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Before
    public void init() {
        initView();
        ItemsView itemsView = new ItemsView(view);
        memoryItemView = new MemoryItemView();
        ProfilerMonitorItem memoryItem = new ProfilerMonitorItem(2, "Memory", MemoryItemView.class);
        sessionInfo = SessionInfo.builder().sessionId(32947)
            .sessionName("Test").pid(2).processName("processName").build();
        SessionManager.getInstance().getProfilingSessions().put(32947L, sessionInfo);
        memoryItemView.init(view, itemsView, memoryItem);
    }

    /**
     * get SemiSimplified Clock String Test
     *
     * @tc.name: getSemiSimplifiedClockStringTest
     * @tc.number: OHOS_JAVA_View_MemoryItemView_getSemiSimplifiedClockStringTest_0001
     * @tc.desc: get SemiSimplified Clock String Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getSemiSimplifiedClockStringTest() {
        String semiSimplifiedClockString = memoryItemView.getSemiSimplifiedClockString(1L);
        Assert.assertNotNull(semiSimplifiedClockString);
    }

    /**
     * get Full Clock String Tes
     *
     * @tc.name: getFullClockStringTes
     * @tc.number: OHOS_JAVA_View_MemoryItemView_getFullClockStringTes_0001
     * @tc.desc: get Full Clock String Tes
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getFullClockStringTes() {
        String fullClockString = memoryItemView.getFullClockString(1L);
        Assert.assertNotNull(fullClockString);
    }

    /**
     * set Third Level TreeTable Test
     *
     * @tc.name: setThirdLevelTreeTableTest
     * @tc.number: OHOS_JAVA_View_MemoryItemView_setThirdLevelTreeTableTest_0001
     * @tc.desc: set Third Level TreeTable Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void setThirdLevelTreeTableTest() {
        JBPanel jbPanel = memoryItemView.setThirdLevelTreeTable(1L, 1, "Test");
        Assert.assertNotNull(jbPanel);
    }
}