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

package ohos.devtools.views.layout.chartview;

import ohos.devtools.views.common.LayoutConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Items View Test
 *
 * @since 2021/5/19 16:39
 */
public class ItemsViewTest {
    private static final int TEST_START = 0;

    private static final int TEST_END = 1000;

    private ItemsView items;

    private ProfilerChartsView view;

    private void initView() {
        view = new ProfilerChartsView(LayoutConstants.NUM_L, true, new TaskScenePanelChart());
        view.getPublisher().getStandard().updateDisplayTimeRange(TEST_START, TEST_END);
    }

    /**
     * init
     *
     * @tc.name: DiskIoViewTest
     * @tc.number: OHOS_JAVA_View_ItemsView_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Before
    public void init() {
        initView();
        items = new ItemsView(view);
    }

    /**
     * Items View Test
     *
     * @tc.name: ItemsViewTest
     * @tc.number: OHOS_JAVA_View_ItemsView_ItemsViewTest_0001
     * @tc.desc: Items View Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void itemsViewTest() {
        ItemsView itemsView = new ItemsView(view);
        Assert.assertNotNull(itemsView);
    }

    /**
     * update Show Height Test
     *
     * @tc.name: updateShowHeightTest
     * @tc.number: OHOS_JAVA_View_ItemsView_updateShowHeightTest_0001
     * @tc.desc: update Show Height Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void updateShowHeightTest() {
        items.updateShowHeight(1);
        Assert.assertTrue(true);
    }
}