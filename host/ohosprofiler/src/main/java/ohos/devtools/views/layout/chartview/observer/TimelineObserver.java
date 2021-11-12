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
import ohos.devtools.views.layout.chartview.ProfilerTimeline;
import ohos.devtools.views.layout.chartview.event.IChartEventObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Observer of profiler timeline
 *
 * @since 2021/2/1 10:36
 */
public class TimelineObserver implements IChartEventObserver {
    private static final Logger LOGGER = LogManager.getLogger(TimelineObserver.class);

    /**
     * Profiler timeline
     */
    private final ProfilerTimeline timeline;

    /**
     * Constructor
     *
     * @param timeline Profiler timeline
     */
    public TimelineObserver(ProfilerTimeline timeline) {
        this.timeline = timeline;
    }

    /**
     * Refresh chart drawing standard
     *
     * @param startTime Start time of chart
     * @param endTime End time of chart
     * @param maxDisplayMillis Maximum display time on view
     * @param minMarkInterval The minimum scale interval
     */
    @Override
    public void refreshStandard(int startTime, int endTime, int maxDisplayMillis, int minMarkInterval) {
        timeline.setMaxDisplayTime(maxDisplayMillis);
        timeline.setMinMarkInterval(minMarkInterval);
        timeline.setStartTime(startTime);
        timeline.setEndTime(endTime);
        timeline.repaint();
        timeline.revalidate();
    }

    /**
     * Refresh view
     *
     * @param range Chart display time range
     * @param firstTimestamp The first time stamp of this chart's data
     * @param useCache whether or not use cache
     */
    @Override
    public void refreshView(ChartDataRange range, long firstTimestamp, boolean useCache) {
        timeline.setStartTime(range.getStartTime());
        timeline.setEndTime(range.getEndTime());
        timeline.repaint();
        timeline.revalidate();
    }
}
