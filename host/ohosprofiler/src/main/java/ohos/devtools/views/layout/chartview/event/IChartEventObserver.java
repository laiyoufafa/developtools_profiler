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
 * Observer of chart refreshes event
 *
 * @since 2021/1/26 19:28
 */
public interface IChartEventObserver {
    /**
     * Refresh chart drawing standard
     *
     * @param startTime        Start time of chart
     * @param endTime          End time of chart
     * @param maxDisplayMillis Maximum display time on view
     * @param minMarkInterval  The minimum scale interval
     */
    void refreshStandard(int startTime, int endTime, int maxDisplayMillis, int minMarkInterval);

    /**
     * Refresh view
     *
     * @param range          Chart display time range
     * @param firstTimestamp The first time stamp of this chart's data
     * @param useCache       whether or not use cache
     */
    void refreshView(ChartDataRange range, long firstTimestamp, boolean useCache);
}
