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

package ohos.devtools.views.charts.tooltip;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.Color;

/**
 * Tool tip Item Test
 *
 * @since 2021/2/1 9:31
 */
public class TooltipItemTest {
    private TooltipItem tooltipItem;

    /**
     * init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_View_TooltipItem_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: SR000FK5SL
     */
    @Before
    public void init() {
        tooltipItem = new TooltipItem(Color.GREEN, "Test");
    }

    /**
     * TooltipItemTest
     *
     * @tc.name: TooltipItemTest
     * @tc.number: OHOS_JAVA_View_TooltipItem_TooltipItemTest_0001
     * @tc.desc: TooltipItemTest
     * @tc.type: functional testing
     * @tc.require: SR000FK5SL
     */
    @Test
    public void tooltipItemTest() {
        TooltipItem item = new TooltipItem(Color.BLUE, "Test");
        Assert.assertNotNull(item);
    }
}