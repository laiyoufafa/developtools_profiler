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

import java.math.BigDecimal;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static ohos.devtools.views.charts.utils.ChartConstants.DECIMAL_COUNTS;
import static ohos.devtools.views.charts.utils.ChartConstants.NUM_1000;
import static ohos.devtools.views.charts.utils.ChartConstants.NUM_24;
import static ohos.devtools.views.charts.utils.ChartConstants.NUM_60;
import static ohos.devtools.views.common.LayoutConstants.INITIAL_VALUE;

/**
 * Chart相关的工具类
 */
public final class ChartUtils {
    /**
     * 二分法计算时除以2的常量
     */
    private static final int HALF_VALUE = 2;

    /**
     * init number
     */
    private static final long INIT_NUMBER = -1L;

    /**
     * 构造函数
     */
    private ChartUtils() {
    }

    /**
     * 在有序数组中找到与目标值最接近的值的index
     *
     * @param arr 有序数组
     * @param value 目标值
     * @return 数组中与目标值最接近的值的index
     */
    public static int searchClosestIndex(int[] arr, int value) {
        // 开始位置
        int low = 0;
        // 结束位置
        int high = arr.length - 1;
        // 先判断下是不是大于最大值或者小于最小值，是的话直接返回
        if (value <= arr[low]) {
            return low;
        }
        if (value >= arr[high]) {
            return high;
        }
        int index = INITIAL_VALUE;
        while (low <= high) {
            int middle = (low + high) / HALF_VALUE;

            // 如果值正好相等，则直接返回查询到的索引
            if (value == arr[middle]) {
                index = middle;
                break;
            }
            // 如果已经是最后一个数据，则返回其索引
            if (middle == arr.length - 1) {
                index = middle;
                break;
            }
            // 大于当前index的值，小于下一个index的值，表明落在该区间内，取最接近的索引
            if (value > arr[middle] && value < arr[middle + 1]) {
                int dif1 = value - arr[middle];
                int dif2 = arr[middle + 1] - value;
                // 返回查询到的索引
                index = dif1 < dif2 ? middle : middle + 1;
                break;
            }
            if (value > arr[middle]) {
                low = middle + 1;
            }
            if (value < arr[middle]) {
                high = middle - 1;
            }
        }
        return index;
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
     * 除法运算，返回BigDecimal
     *
     * @param num1 数字1
     * @param num2 数字2
     * @return BigDecimal结果
     */
    public static BigDecimal divide(BigDecimal num1, BigDecimal num2) {
        return num1.divide(num2, DECIMAL_COUNTS, ROUND_HALF_UP);
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

    /**
     * 将毫秒转换为分:秒:毫秒的格式
     *
     * @param ms 处理的毫秒数
     * @return java.lang.String 转换为 分:秒:毫秒的格式
     */
    public static String formatTime(Long ms) {
        int ss = NUM_1000;
        int mi = ss * NUM_60;
        int hh = mi * NUM_60;
        int dd = hh * NUM_24;

        long day = INIT_NUMBER;
        if (dd != 0) {
            day = ms / dd;
        }
        long hour = INIT_NUMBER;
        if (hh != 0) {
            hour = (ms - day * dd) / hh;
        }
        long minute = INIT_NUMBER;
        if (mi != 0) {
            minute = (ms - day * dd - hour * hh) / mi;
        }
        long second = INIT_NUMBER;
        if (ss != 0) {
            second = (ms - day * dd - hour * hh - minute * mi) / ss;
        }
        long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        StringBuilder builder = new StringBuilder();
        if (day > 0) {
            builder.append(day).append(":");
        }
        if (hour > 0) {
            builder.append(hour).append(":");
        }
        if (minute > 0) {
            builder.append(minute).append(":");
        } else {
            builder.append("00:");
        }
        if (second > 0) {
            builder.append(second).append(":");
        } else {
            builder.append("00:");
        }
        if (milliSecond > 0) {
            builder.append(milliSecond).append(" ");
        } else {
            builder.append("00:");
        }
        return builder.toString();
    }
}
