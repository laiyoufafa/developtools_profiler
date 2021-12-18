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

package ohos.devtools.samplecode.views.layout.chartview.observer;

import ohos.devtools.samplecode.services.samplecode.SampleCodeDao;
import ohos.devtools.samplecode.services.samplecode.SampleCodeDataCache;
import ohos.devtools.views.charts.ProfilerChart;
import ohos.devtools.views.charts.model.ChartDataModel;
import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartStandard;
import ohos.devtools.views.layout.chartview.event.IChartEventObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * SampleCodeChartObserver
 *
 * @since 2021/11/24
 */
public class SampleCodeChartObserver implements IChartEventObserver {
    private static final Logger LOGGER = LogManager.getLogger(SampleCodeChartObserver.class);

    private final ProfilerChart chart;
    private final long sessionId;
    private boolean chartFold;

    /**
     * SampleCodeChartObserver.
     *
     * @param chart chart
     * @param sessionId sessionId
     * @param chartFold chartFold
     */
    public SampleCodeChartObserver(ProfilerChart chart, long sessionId, boolean chartFold) {
        this.chart = chart;
        this.sessionId = sessionId;
        this.chartFold = chartFold;
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
        chart.setMaxDisplayX(maxDisplayMillis);
        chart.setMinMarkIntervalX(minMarkInterval);
        chart.setStartTime(startTime);
        chart.setEndTime(endTime);
        chart.repaint();
        chart.revalidate();
    }

    @Override
    public void refreshView(ChartDataRange range, long firstTimestamp, boolean useCache) {
        LinkedHashMap<Integer, List<ChartDataModel>> queryResult;
        int start = range.getStartTime();
        int end = range.getEndTime();
        boolean chartStop = chart.getBottomPanel().isPause() || chart.getBottomPanel().isStop();
        if (chartStop || !useCache) {
            queryResult = SampleCodeDao.getInstance().getData(sessionId, start, end, firstTimestamp, true);
        } else {
            queryResult = SampleCodeDataCache.getInstance().getData(sessionId, start, end);
        }
        LinkedHashMap<Integer, List<ChartDataModel>> showDataMap = new LinkedHashMap<>();
        for (int time : queryResult.keySet()) {
            List<ChartDataModel> dataModels = queryResult.get(time);
            showDataMap.put(time, dataModels);
        }
        chart.refreshChart(range.getStartTime(), end, showDataMap);
    }

    /**
     * Update fold status and refresh current chart
     *
     * @param chartFold true: Chart fold/expand
     */
    public void setChartFold(boolean chartFold) {
        this.chartFold = chartFold;
        refreshManually();
    }

    /**
     * Refresh current chart manually
     */
    public void refreshManually() {
        ChartStandard standard = chart.getBottomPanel().getPublisher().getStandard();
        ChartDataRange range = standard.getDisplayRange();
        long firstTs = standard.getFirstTimestamp();
        refreshView(range, firstTs, !chart.getBottomPanel().getPublisher().isTraceFile());
    }
}
