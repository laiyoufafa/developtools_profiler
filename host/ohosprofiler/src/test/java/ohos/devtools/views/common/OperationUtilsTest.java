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

package ohos.devtools.views.common;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * ViewUtils test
 *
 * @since 2021/3/31 14:13
 */
public class OperationUtilsTest {
    private static final int[] TEST_ARRAY = {0, 10, 14, 24, 29, 32, 37, 40, 45};

    private static final double TEST_NUM1 = 23.51D;

    private static final int TEST_NUM2 = 4;

    private static final int MULTIPLY_RESULT_INT = 94;

    private static final int DIVIDE_RESULT_INT = 5;

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ViewUtils_multiply_0001
     * @tc.desc: chart Timeline test
     * @tc.type: functional test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void multiplyTest() {
        BigDecimal num1 = new BigDecimal(TEST_NUM1);
        int result = OperationUtils.multiply(num1, TEST_NUM2);
        Assert.assertEquals(MULTIPLY_RESULT_INT, result);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ViewUtils_divide_0001
     * @tc.desc: chart Timeline test
     * @tc.type: functional test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void divideTest1() {
        int result = OperationUtils.divide(TEST_NUM1, TEST_NUM2).intValue();
        Assert.assertEquals(DIVIDE_RESULT_INT, result);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ViewUtils_divideInt_0001
     * @tc.desc: chart Timeline test
     * @tc.type: functional test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void divideIntTest2() {
        int result = OperationUtils.divideInt(TEST_NUM1, TEST_NUM2);
        Assert.assertEquals(DIVIDE_RESULT_INT, result);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ViewUtils_divide_0001
     * @tc.desc: chart Timeline test
     * @tc.type: functional test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void divideTest2() {
        int result = OperationUtils.divide(TEST_NUM1, new BigDecimal(TEST_NUM2));
        Assert.assertEquals(DIVIDE_RESULT_INT, result);
    }
}
