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

import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.chartview.ItemsView;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.ProfilerMonitorItem;
import ohos.devtools.views.layout.chartview.TaskScenePanelChart;
import ohos.devtools.views.layout.chartview.memory.javaagent.MemoryAgentHeapInfoPanel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Memory Agent Heap Info Panel Test
 *
 * @since 2021/2/1 9:31
 */
public class MemoryAgentHeapInfoPanelTest {
    private static final int TEST_START = 0;

    private static final int TEST_END = 1000;

    private MemoryItemView memoryItemView;
    private MemoryAgentHeapInfoPanel memoryAgentHeapInfoPanel;
    private ProfilerChartsView view;

    private void initView() {
        view = new ProfilerChartsView(LayoutConstants.NUM_L, true, new TaskScenePanelChart());
        view.getPublisher().getStandard().updateDisplayTimeRange(TEST_START, TEST_END);
    }

    /**
     * init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_View_MemoryAgentHeapInfoPanel_init_0001
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
        memoryItemView.init(view, itemsView, memoryItem);
        memoryAgentHeapInfoPanel = new MemoryAgentHeapInfoPanel(memoryItemView, 1L, "Test");
    }

    /**
     * Memory Agent Heap Info Panel Test
     *
     * @tc.name: MemoryAgentHeapInfoPanelTest
     * @tc.number: OHOS_JAVA_View_MemoryAgentHeapInfoPanel_MemoryAgentHeapInfoPanelTest_0001
     * @tc.desc: Memory Agent Heap Info Panel Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void memoryAgentHeapInfoPanelTest() {
        ItemsView itemsView = new ItemsView(view);
        memoryItemView = new MemoryItemView();
        ProfilerMonitorItem memoryItem = new ProfilerMonitorItem(2, "Memory", MemoryItemView.class);
        memoryItemView.init(view, itemsView, memoryItem);
        MemoryAgentHeapInfoPanel memoryAgentHeapInfo = new MemoryAgentHeapInfoPanel(memoryItemView, 1L, "Test");
        Assert.assertNotNull(memoryAgentHeapInfo);
    }
}