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

package ohos.devtools.views.layout.chartview.utils;

/**
 * ChartViewConstants
 */
public final class ChartViewConstants {
    /**
     * 时间线默认宽度
     */
    public static final int TIMELINE_WIDTH = 1000;

    /**
     * 时间线默认高度
     */
    public static final int TIMELINE_HEIGHT = 30;

    /**
     * Chart界面刷新频率，单位为毫秒
     */
    public static final int REFRESH_FREQ = 20;

    /**
     * 时间线字体大小
     */
    public static final int TIMELINE_FONT_SIZE = 11;

    /**
     * 每隔N个minMarkInterval绘制坐标轴数字和大刻度
     */
    public static final int TIMELINE_MARK_COUNTS = 5;

    /**
     * 数字
     */
    public static final int NUM_1024 = 1024;

    /**
     * Chart延迟启动的时长，单位为毫秒
     */
    public static final int CHART_START_DELAY = 500;

    /**
     * Chart loading图的大小
     */
    public static final int LOADING_SIZE = 100;

    /**
     * 构造函数
     */
    private ChartViewConstants() {
    }
}
