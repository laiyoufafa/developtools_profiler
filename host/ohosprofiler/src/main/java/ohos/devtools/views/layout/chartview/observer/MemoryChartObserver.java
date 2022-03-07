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

import ohos.devtools.datasources.utils.monitorconfig.service.MonitorConfigManager;
import ohos.devtools.services.memory.memorydao.MemoryDao;
import ohos.devtools.services.memory.memoryservice.MemoryDataCache;
import ohos.devtools.views.charts.ProfilerChart;
import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartStandard;
import ohos.devtools.views.charts.model.ChartDataModel;
import ohos.devtools.views.layout.chartview.event.IChartEventObserver;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static ohos.devtools.views.common.ColorConstants.MEMORY;
import static ohos.devtools.views.layout.chartview.MonitorItemDetail.MEM_CODE;
import static ohos.devtools.views.layout.chartview.MonitorItemDetail.MEM_GRAPHICS;
import static ohos.devtools.views.layout.chartview.MonitorItemDetail.MEM_JAVA;
import static ohos.devtools.views.layout.chartview.MonitorItemDetail.MEM_NATIVE;
import static ohos.devtools.views.layout.chartview.MonitorItemDetail.MEM_OTHERS;
import static ohos.devtools.views.layout.chartview.MonitorItemDetail.MEM_STACK;

/**
 * Observer of memory chart
 *
 * @since 2021/3/1 14:58
 */
public class MemoryChartObserver implements IChartEventObserver {
    private static final Logger LOGGER = LogManager.getLogger(MemoryChartObserver.class);
    private final ProfilerChart chart;
    private final long sessionId;
    private boolean chartFold;

    /**
     * Constructor
     *
     * @param chart Profiler Chart
     * @param sessionId Session id
     * @param chartFold true: Chart fold/expand
     */
    public MemoryChartObserver(ProfilerChart chart, long sessionId, boolean chartFold) {
        this.sessionId = sessionId;
        this.chart = chart;
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

    /**
     * Refresh view
     *
     * @param range Chart display time range
     * @param firstTimestamp The first time stamp of this chart's data
     * @param useCache whether or not use cache
     */
    @Override
    public void refreshView(ChartDataRange range, long firstTimestamp, boolean useCache) {
        LinkedHashMap<Integer, List<ChartDataModel>> queryResult;
        int start = range.getStartTime();
        int end = range.getEndTime();
        boolean chartStop = chart.getBottomPanel().isPause() || chart.getBottomPanel().isStop();
        if (chartStop || !useCache) {
            queryResult = MemoryDao.getInstance().getData(sessionId, start, end, firstTimestamp, true);
        } else {
            queryResult = MemoryDataCache.getInstance().getData(sessionId, start, end);
        }

        Map<String, LinkedList<String>> configMap = MonitorConfigManager.DATA_MAP.get(sessionId);
        List<String> monitorItemList = new ArrayList<>();
        if (configMap != null) {
            monitorItemList = configMap.get("Memory");
        }

        LinkedHashMap<Integer, List<ChartDataModel>> showDataMap = new LinkedHashMap<>();
        for (int time : queryResult.keySet()) {
            List<ChartDataModel> dataModels = queryResult.get(time);
            if (chartFold) {
                ChartDataModel total = new ChartDataModel();
                total.setName("Total");
                total.setColor(MEMORY);
                total.setValue(sumChosenItems(dataModels, monitorItemList));
                showDataMap.put(time, Collections.singletonList(total));
            } else {
                List<ChartDataModel> chosenModels = getChosenItems(dataModels, monitorItemList);
                showDataMap.put(time, chosenModels);
            }
        }
        chart.refreshChart(range.getStartTime(), end, showDataMap);
    }

    /**
     * Sum according to the selected memory monitor items
     *
     * @param dataModels Data list
     * @param monitorItemList List
     * @return int
     */
    private int sumChosenItems(List<ChartDataModel> dataModels, List<String> monitorItemList) {
        int total = 0;
        boolean configEmpty = CollectionUtils.isEmpty(monitorItemList);
        for (ChartDataModel model : dataModels) {
            if (configEmpty) {
                total += model.getValue();
                continue;
            }
            if (monitorItemList.contains(MEM_JAVA.getName()) && model.getName().equals(MEM_JAVA.getName())) {
                total += model.getValue();
                continue;
            }
            if (monitorItemList.contains(MEM_NATIVE.getName()) && model.getName().equals(MEM_NATIVE.getName())) {
                total += model.getValue();
                continue;
            }
            if (monitorItemList.contains(MEM_GRAPHICS.getName()) && model.getName().equals(MEM_GRAPHICS.getName())) {
                total += model.getValue();
                continue;
            }
            if (monitorItemList.contains(MEM_STACK.getName()) && model.getName().equals(MEM_STACK.getName())) {
                total += model.getValue();
                continue;
            }
            if (monitorItemList.contains(MEM_CODE.getName()) && model.getName().equals(MEM_CODE.getName())) {
                total += model.getValue();
                continue;
            }
            if (monitorItemList.contains(MEM_OTHERS.getName()) && model.getName().equals(MEM_OTHERS.getName())) {
                total += model.getValue();
            }
        }
        return total;
    }

    private List<ChartDataModel> getChosenItems(List<ChartDataModel> dataModels, List<String> monitorItemList) {
        if (CollectionUtils.isEmpty(monitorItemList)) {
            return dataModels;
        }

        List<ChartDataModel> result = new ArrayList<>();
        for (ChartDataModel model : dataModels) {
            if (monitorItemList.contains(MEM_JAVA.getName()) && model.getName().equals(MEM_JAVA.getName())) {
                result.add(model);
                continue;
            }
            if (monitorItemList.contains(MEM_NATIVE.getName()) && model.getName().equals(MEM_NATIVE.getName())) {
                result.add(model);
                continue;
            }
            if (monitorItemList.contains(MEM_GRAPHICS.getName()) && model.getName().equals(MEM_GRAPHICS.getName())) {
                result.add(model);
                continue;
            }
            if (monitorItemList.contains(MEM_STACK.getName()) && model.getName().equals(MEM_STACK.getName())) {
                result.add(model);
                continue;
            }
            if (monitorItemList.contains(MEM_CODE.getName()) && model.getName().equals(MEM_CODE.getName())) {
                result.add(model);
                continue;
            }
            if (monitorItemList.contains(MEM_OTHERS.getName()) && model.getName().equals(MEM_OTHERS.getName())) {
                result.add(model);
            }
        }
        return result;
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
