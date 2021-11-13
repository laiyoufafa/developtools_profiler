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

package ohos.devtools.views.layout.chartview.diskio;

import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.chartview.ItemsView;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.ProfilerMonitorItem;
import ohos.devtools.views.layout.chartview.TaskScenePanelChart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Cpu Item View Test
 *
 * @since 2021/2/1 9:31
 */
public class DiskIoViewTest {
    private static final int TEST_START = 0;

    private static final int TEST_END = 1000;

    private ProfilerChartsView view;

    private void initView() {
        view = new ProfilerChartsView(LayoutConstants.NUM_L, true, new TaskScenePanelChart());
        view.getPublisher().getStandard().updateDisplayTimeRange(TEST_START, TEST_END);
    }

    /**
     * init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_View_DiskIoView_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Before
    public void init() {
        initView();
    }

    /**
     * DiskIo View Test
     *
     * @tc.name: DiskIoViewTest
     * @tc.number: OHOS_JAVA_View_DiskIoView_DiskIoViewTest_0001
     * @tc.desc: DiskIo View Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void diskIoViewTest() {
        ItemsView itemsView = new ItemsView(view);
        DiskIoView diskIoView = new DiskIoView();
        ProfilerMonitorItem profilerMonitorItem = new ProfilerMonitorItem(3, "DiskIO", DiskIoView.class);
        diskIoView.init(view, itemsView, profilerMonitorItem);
        Assert.assertNotNull(diskIoView);
    }
}