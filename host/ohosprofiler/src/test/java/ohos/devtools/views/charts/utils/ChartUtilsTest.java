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

import java.math.BigDecimal;

/**
 * Test class of Chart-related tool classes
 */
public class ChartUtilsTest {
    private static final int[] TEST_ARRAY = {0, 10, 14, 24, 29, 32, 37, 40, 45};
    private static final int SEARCH_VALUE = 34;
    private static final int EXPECT_VALUE = 5;
    private static final double TEST_NUM1 = 23.51D;
    private static final int TEST_NUM2 = 4;
    private static final int MULTIPLY_RESULT_INT = 94;
    private static final int DIVIDE_RESULT_INT = 5;
    private static final long TEST_MS = 32349;
    private static final String FORMAT_RESULT = "00:32:349 ";

    /**
     * functional test
     *
     * @tc.name: searchClosestIndex
     * @tc.number: OHOS_JAVA_View_ChartUtils_searchClosestIndex_0001
     * @tc.desc: searchClosestIndex
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void searchClosestIndexTest() {
        int index = ChartUtils.searchClosestIndex(TEST_ARRAY, SEARCH_VALUE);
        Assert.assertEquals(EXPECT_VALUE, index);
    }

    /**
     * functional test
     *
     * @tc.name: multiply
     * @tc.number: OHOS_JAVA_View_ChartUtils_multiply_0001
     * @tc.desc: multiply
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void multiplyTest() {
        BigDecimal num1 = new BigDecimal(TEST_NUM1);
        int result = ChartUtils.multiply(num1, TEST_NUM2);
        Assert.assertEquals(MULTIPLY_RESULT_INT, result);
    }

    /**
     * functional test
     *
     * @tc.name: divide
     * @tc.number: OHOS_JAVA_View_ChartUtils_divide_0001
     * @tc.desc: divide
     * @tc.type: functional testing
     * @tc.require: AR000FK5SN
     */
    @Test
    public void divideTest1() {
        int result = ChartUtils.divide(TEST_NUM1, TEST_NUM2).intValue();
        Assert.assertEquals(DIVIDE_RESULT_INT, result);
    }

    /**
     * functional test
     *
     * @tc.name: divideInt
     * @tc.number: OHOS_JAVA_View_ChartUtils_divideInt_0001
     * @tc.desc: divideInt
     * @tc.type: functional testing
     * @tc.require: AR000FK5SN
     */
    @Test
    public void divideIntTest2() {
        int result = ChartUtils.divideInt(TEST_NUM1, TEST_NUM2);
        Assert.assertEquals(DIVIDE_RESULT_INT, result);
    }

    /**
     * functional test
     *
     * @tc.name: divide
     * @tc.number: OHOS_JAVA_View_ChartUtils_divide_0001
     * @tc.desc: divide
     * @tc.type: functional testing
     * @tc.require: AR000FK5SN
     */
    @Test
    public void divideTest2() {
        int result = ChartUtils.divide(TEST_NUM1, new BigDecimal(TEST_NUM2));
        Assert.assertEquals(DIVIDE_RESULT_INT, result);
    }

    /**
     * functional test
     *
     * @tc.name: formatTime
     * @tc.number: OHOS_JAVA_View_ChartUtils_formatTime_0001
     * @tc.desc: formatTime
     * @tc.type: functional testing
     * @tc.require: AR000FK5SN
     */
    @Test
    public void formatTimeTest() {
        String result = ChartUtils.formatTime(TEST_MS);
        Assert.assertEquals(FORMAT_RESULT, result);
    }
}
