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

package ohos.devtools.views.charts.utils;

import org.junit.Assert;
import org.junit.Test;

import java.awt.Color;

/**
 * @Description ChartConstantsTest
 * @Date 2021/4/3 18:00
 **/
public class ChartConstantsTest {
    /**
     * functional test
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_View_ChartConstants_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    public void init() {
    }

    /**
     * functional test
     *
     * @tc.name: test
     * @tc.number: OHOS_JAVA_View_ChartConstants_test_0001
     * @tc.desc: test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void test() {
        int test01 = ChartConstants.INITIAL_VALUE;
        Assert.assertEquals(-1, test01);
        int test02 = ChartConstants.CHART_HEADER_HEIGHT;
        Assert.assertEquals(20, test02);
        int test03 = ChartConstants.SCALE_LINE_LEN;
        Assert.assertEquals(4, test03);
        int test04 = ChartConstants.UNIT;
        Assert.assertEquals(1024, test04);
        float test05 = ChartConstants.OPAQUE_VALUE;
        Assert.assertNotNull(test05);
        int test06 = ChartConstants.Y_AXIS_STR_OFFSET_X;
        Assert.assertEquals(15, test06);
        int test07 = ChartConstants.Y_AXIS_STR_OFFSET_Y;
        Assert.assertEquals(5, test07);
        float test08 = ChartConstants.TRANSLUCENT_VALUE;
        Assert.assertNotNull(test08);
        Color test09 = ChartConstants.DEFAULT_CHART_COLOR;
        Assert.assertNotNull(test09);
    }
}