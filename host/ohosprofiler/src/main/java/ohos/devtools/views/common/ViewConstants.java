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

/**
 * 视图相关的常量类
 *
 * @since 2021/2/27 14:58
 */
public final class ViewConstants {
    /**
     * 时间线默认宽度
     */
    public static final int TIMELINE_WIDTH = 1000;

    /**
     * 时间线默认高度
     */
    public static final int TIMELINE_HEIGHT = 30;

    /**
     * Label的默认宽度
     */
    public static final int LABEL_DEFAULT_WIDTH = 90;

    /**
     * Label的默认高度
     */
    public static final int LABEL_DEFAULT_HEIGHT = 25;

    /**
     * Chart组件的初始宽度
     */
    public static final int CHART_INIT_WIDTH = 1000;

    /**
     * Chart的默认高度
     */
    public static final int CHART_DEFAULT_HEIGHT = 150;

    /**
     * Chart的Y轴最大单位
     */
    public static final int CHART_MAX_Y = 2;

    /**
     * Chart的Y轴坐标刻度分段数量
     */
    public static final int CHART_SECTION_NUM_Y = 2;

    /**
     * Chart界面刷新频率，单位为毫秒
     */
    public static final int REFRESH_FREQ = 30;

    /**
     * 初始值
     */
    public static final int INITIAL_VALUE = -1;

    /**
     * 时间线字体大小
     */
    public static final int TIMELINE_FONT_SIZE = 11;

    /**
     * 时间线上的默认最大毫秒数
     */
    public static final int DEFAULT_MAX_MILLIS = 10000;

    /**
     * 时间线上的默认最小时间单位，单位为毫秒
     */
    public static final int DEFAULT_TIME_UNIT = 200;

    /**
     * 每隔N个minMarkInterval绘制坐标轴数字和大刻度
     */
    public static final int TIMELINE_MARK_COUNTS = 5;

    /**
     * 计算时默认保留的小数位数
     */
    public static final int DECIMAL_COUNTS = 5;

    /**
     * 鼠标滚轮的缩放跨度时间
     */
    public static final int ZOOM_TIME = 500;

    /**
     * 鼠标滚轮的缩放跨度时间
     */
    public static final int ZOOM_IN_MIN = 5000;

    /**
     * 鼠标滚轮的缩放跨度时间
     */
    public static final int ZOOM_OUT_MAX = 12000;

    /**
     * 时间刻度的默认毫秒数200ms
     */
    public static final int SIZE_TIME = 1000;

    /**
     * 数字2
     */
    public static final int NUM_2 = 2;

    /**
     * 数字3
     */
    public static final int NUM_3 = 3;

    /**
     * 数字4
     */
    public static final int NUM_4 = 4;

    /**
     * 数字5
     */
    public static final int NUM_5 = 5;

    /**
     * 数字6
     */
    public static final int NUM_6 = 6;

    /**
     * 数字7
     */
    public static final int NUM_7 = 7;

    /**
     * 数字8
     */
    public static final int NUM_8 = 8;

    /**
     * 数字9
     */
    public static final int NUM_9 = 9;

    /**
     * 数字10
     */
    public static final int NUM_10 = 10;

    /**
     * 数字40
     */
    public static final int NUM_40 = 40;

    /**
     * 数字100
     */
    public static final int NUM_100 = 100;

    /**
     * 数字200
     */
    public static final int NUM_200 = 200;

    /**
     * 数字1000
     */
    public static final int NUM_1000 = 1000;

    /**
     * 构造函数
     */
    private ViewConstants() {
    }
}
