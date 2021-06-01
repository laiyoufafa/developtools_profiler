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

import java.math.BigDecimal;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static ohos.devtools.views.common.ViewConstants.DECIMAL_COUNTS;

/**
 * View related tool classes
 *
 * @since 2021/2/2 10:25
 */
public final class OperationUtils {
    /**
     * 构造函数
     */
    private OperationUtils() {
    }

    /**
     * 乘法运算
     *
     * @param num1 数字1
     * @param num2 数字2
     * @return int结果
     */
    public static int multiply(BigDecimal num1, int num2) {
        return num1.multiply(new BigDecimal(num2)).intValue();
    }

    /**
     * 除法运算，返回BigDecimal
     *
     * @param num1 数字1
     * @param num2 数字2
     * @return BigDecimal结果
     */
    public static BigDecimal divide(double num1, double num2) {
        return new BigDecimal(num1).divide(new BigDecimal(num2), DECIMAL_COUNTS, ROUND_HALF_UP);
    }

    /**
     * 除法运算，返回int
     *
     * @param num1 数字1
     * @param num2 数字2
     * @return int结果
     */
    public static int divideInt(double num1, double num2) {
        return new BigDecimal(num1).divide(new BigDecimal(num2), DECIMAL_COUNTS, ROUND_HALF_UP).intValue();
    }

    /**
     * 除法运算，返回int
     *
     * @param num1 数字1
     * @param num2 数字2
     * @return int结果
     */
    public static int divide(double num1, BigDecimal num2) {
        return new BigDecimal(num1).divide(num2, DECIMAL_COUNTS, ROUND_HALF_UP).intValue();
    }

}
