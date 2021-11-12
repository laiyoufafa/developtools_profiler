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
import org.junit.Test;

import java.awt.Color;

/**
 * Tool tip Color Rect Test
 */
public class TooltipColorRectTest {
    /**
     * functional test
     *
     * @tc.name: test
     * @tc.number: OHOS_JAVA_View_TooltipColorRect_test_0001
     * @tc.desc: test
     * @tc.type: functional testing
     * @tc.require: AR000FK5SM
     */
    @Test
    public void test01() {
        TooltipColorRect tooltipColorRect = new TooltipColorRect(new Color(255, 255, 255));
        Assert.assertNotNull(tooltipColorRect);
    }
}