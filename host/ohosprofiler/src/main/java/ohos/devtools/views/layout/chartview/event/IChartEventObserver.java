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

package ohos.devtools.views.layout.chartview.event;

import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartStandard;

/**
 * Chart刷新事件观察者
 *
 * @since 2021/1/26 19:28
 */
public interface IChartEventObserver {
    /**
     * 刷新绘图标准
     *
     * @param standard       绘图标准
     * @param startTime      开始时间
     * @param endTime        结束时间
     * @param maxDisplayTime 最大显示时间
     */
    void refreshStandard(ChartStandard standard, int startTime, int endTime, int maxDisplayTime);

    /**
     * 刷新视图
     *
     * @param range          时间范围
     * @param firstTimestamp 本次Chart首次创建并启动刷新时的时间戳
     * @param isUseCache     是否使用缓存机制
     */
    void refreshView(ChartDataRange range, long firstTimestamp, boolean isUseCache);
}
