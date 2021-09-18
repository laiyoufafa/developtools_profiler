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

import com.intellij.ui.JBColor;

/**
 * Chart相关的常量类
 *
 * @since 2021/1/21 11:33
 */
public final class ChartConstants {
    /**
     * 默认半透明度的值
     */
    public static final float TRANSLUCENT_VALUE = 0.8F;

    /**
     * Chart绘制区域离顶部的偏移量（页眉高度）
     */
    public static final int CHART_HEADER_HEIGHT = 20;

    /**
     * 默认刻度线长度
     */
    public static final int SCALE_LINE_LEN = 4;

    /**
     * 绘制Y轴刻度值时，str在X轴上的偏移量
     */
    public static final int Y_AXIS_STR_OFFSET_X = 15;

    /**
     * 绘制Y轴刻度值时，str在Y轴上的偏移量
     */
    public static final int Y_AXIS_STR_OFFSET_Y = 5;

    /**
     * 默认Chart绘制颜色
     */
    public static final JBColor DEFAULT_CHART_COLOR = JBColor.GRAY;

    /**
     * 折线图线条默认宽度
     */
    public static final int DEFAULT_LINE_WIDTH = 3;

    /**
     * 数字
     */
    public static final int NUM_24 = 24;

    /**
     * 数字
     */
    public static final int NUM_60 = 60;

    /**
     * 数字
     */
    public static final int NUM_1000 = 1000;

    /**
     * 计算时默认保留的小数位数
     */
    public static final int DECIMAL_COUNTS = 5;

    /**
     * Default time range selected after clicking chart
     */
    public static final int DEFAULT_SELECT = 10;

    /**
     * Chart的Y轴最大单位
     */
    public static final int CHART_MAX_Y = 2;

    /**
     * Chart的Y轴坐标刻度分段数量
     */
    public static final int CHART_SECTION_NUM_Y = 2;

    /**
     * 时间线上的默认最大毫秒数
     */
    public static final int DEFAULT_MAX_MILLIS = 10000;

    /**
     * 时间线上的默认最小时间单位，单位为毫秒
     */
    public static final int DEFAULT_TIME_UNIT = 200;

    /**
     * 构造函数
     */
    private ChartConstants() {
    }
}
