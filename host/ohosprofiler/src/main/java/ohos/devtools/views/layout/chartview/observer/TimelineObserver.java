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

package ohos.devtools.views.layout.chartview.observer;

import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartStandard;
import ohos.devtools.views.common.chart.ProfilerTimeline;
import ohos.devtools.views.layout.chartview.event.IChartEventObserver;

/**
 * Profiler时间线的观察者
 *
 * @since 2021/2/1 10:36
 */
public class TimelineObserver implements IChartEventObserver {
    /**
     * Profiler时间线
     */
    private final ProfilerTimeline timeline;

    /**
     * 构造函数
     *
     * @param timeline Profiler时间线
     */
    public TimelineObserver(ProfilerTimeline timeline) {
        this.timeline = timeline;
    }

    /**
     * 刷新绘图标准
     *
     * @param standard       绘图标准
     * @param startTime      startTime
     * @param endTime        endTime
     * @param maxDisplayTime maxDisplayTime
     */
    @Override
    public void refreshStandard(ChartStandard standard, int startTime, int endTime, int maxDisplayTime) {
        // timeline绘制的缩放尺寸设置
        timeline.setMaxDisplayTime(standard.getMaxDisplayMillis());
        timeline.setMinMarkInterval(standard.getMinMarkInterval());

        // 重新更新开始和结束时间
        timeline.setStartTime(startTime);
        timeline.setEndTime(endTime);
        timeline.repaint();
        timeline.revalidate();
    }

    /**
     * 刷新视图
     *
     * @param range          时间范围
     * @param firstTimestamp 本次Chart首次创建并启动刷新时的时间戳
     * @param isUseCache     是否使用缓存机制
     */
    @Override
    public void refreshView(ChartDataRange range, long firstTimestamp, boolean isUseCache) {
        timeline.setStartTime(range.getStartTime());
        timeline.setEndTime(range.getEndTime());

        timeline.repaint();
        timeline.revalidate();
    }
}
