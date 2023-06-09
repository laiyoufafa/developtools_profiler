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

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import ohos.devtools.services.cpu.CpuDao;
import ohos.devtools.services.cpu.CpuDataCache;
import ohos.devtools.views.charts.ProfilerChart;
import ohos.devtools.views.charts.RectChart;
import ohos.devtools.views.charts.model.ChartDataModel;
import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartStandard;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.event.IChartEventObserver;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Observer of cpu chart
 *
 * @since 2021/11/22
 */
public class CpuChartObserver implements IChartEventObserver {
    private static final Logger LOGGER = LogManager.getLogger(CpuChartObserver.class);
    private final ProfilerChart chart;
    private final long sessionId;
    private boolean chartFold;
    private JBPanel threadInfoPanel;
    private JBLabel threadLabel;
    private ProfilerChartsView bottomPanel;
    private HashMap<Integer, String> threadMap = new HashMap<Integer, String>();
    private ArrayList<RectChart> rectChartList = new ArrayList<RectChart>();

    /**
     * Constructor
     *
     * @param chart chart
     * @param threadInfoPanel threadInfoPanel
     * @param bottomPanel bottomPanel
     * @param chartFold chartFold
     * @param threadLabel threadLabel
     */
    public CpuChartObserver(ProfilerChart chart, JBPanel threadInfoPanel, ProfilerChartsView bottomPanel,
        boolean chartFold, JBLabel threadLabel) {
        this.bottomPanel = bottomPanel;
        this.sessionId = bottomPanel.getSessionId();
        this.chart = chart;
        this.chartFold = chartFold;
        this.threadInfoPanel = threadInfoPanel;
        this.threadLabel = threadLabel;
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
        rectChartList.forEach(chart -> {
            chart.setMaxDisplayX(maxDisplayMillis);
            chart.setMinMarkIntervalX(minMarkInterval);
            chart.setStartTime(startTime);
            chart.setEndTime(endTime);
            chart.repaint();
            chart.revalidate();
        });
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
        LinkedHashMap<Integer, List<ChartDataModel>> cpuResult;
        LinkedHashMap<Integer, List<ChartDataModel>> threadResult;
        LinkedHashMap<Integer, List<ChartDataModel>> deadThreadData;
        int start = range.getStartTime();
        int end = range.getEndTime();
        boolean chartStop = chart.getBottomPanel().isPause() || chart.getBottomPanel().isStop();
        if (chartStop || !useCache) {
            cpuResult = CpuDao.getInstance().getCpuData(sessionId, start, end, firstTimestamp, true);
            threadResult = CpuDao.getInstance().getThreadData(sessionId, start, end, firstTimestamp, true);
            deadThreadData = CpuDao.getInstance().getDeadThreadData(sessionId, start, end, firstTimestamp);
        } else {
            cpuResult = CpuDataCache.getInstance().getCpuData(sessionId, start, end);
            threadResult = CpuDataCache.getInstance().getThreadData(sessionId, start, end);
            deadThreadData = CpuDataCache.getInstance().getDeadThreadData(sessionId, start, end);
        }
        LinkedHashMap<Integer, List<ChartDataModel>> showDataMap = new LinkedHashMap<>();
        for (int time : cpuResult.keySet()) {
            List<ChartDataModel> dataModels = cpuResult.get(time);
            if (chartFold) {
                ChartDataModel total = new ChartDataModel();
                total.setName("System");
                total.setColor(ColorConstants.CPU);
                total.setValue(sumChosenItems(dataModels));
                total.setDoubleValue(sumChosenItems(dataModels));
                showDataMap.put(time, Collections.singletonList(total));
            } else {
                showDataMap.put(time, dataModels);
            }

        }
        chart.refreshChart(range.getStartTime(), end, showDataMap);
        refreshThreadPanel(range.getStartTime(), end, threadResult, deadThreadData);
    }

    /**
     * Sum according to the selected cpu monitor items
     *
     * @param dataModels Data list
     * @return int
     */
    private int sumChosenItems(List<ChartDataModel> dataModels) {
        int total = 0;
        if (dataModels != null && dataModels.size() > 0) {
            ChartDataModel systemChartDataModel =
                dataModels.stream().filter(each -> StringUtils.equals(each.getName(), "System")).findFirst().get();
            return systemChartDataModel.getValue();
        }
        return total;
    }

    /**
     * Setter
     *
     * @param chartFold true: Chart fold/expand
     */
    public void setChartFold(boolean chartFold) {
        this.chartFold = chartFold;
        ChartStandard standard = chart.getBottomPanel().getPublisher().getStandard();
        ChartDataRange range = standard.getDisplayRange();
        long firstTs = standard.getFirstTimestamp();
        refreshView(range, firstTs, !chart.getBottomPanel().getPublisher().isTraceFile());
    }

    /**
     * refreshThreadPanel
     *
     * @param startTime startTime
     * @param endTime endTime
     * @param dataMap dataMap
     * @param deadThreadMap deadThreadMap
     */
    private void refreshThreadPanel(int startTime, int endTime, LinkedHashMap<Integer, List<ChartDataModel>> dataMap,
        LinkedHashMap<Integer, List<ChartDataModel>> deadThreadMap) {
        dataMap.forEach((integer, chartDataModels) -> {
            chartDataModels.forEach(chartDataModel -> {
                // add view
                if (!threadMap.containsKey(chartDataModel.getIndex())) {
                    threadMap.put(chartDataModel.getIndex(), chartDataModel.getName());
                    JBPanel itemPanel = new JBPanel(null);
                    itemPanel.setOpaque(false);
                    JBLabel label =
                        new JBLabel("     " + chartDataModel.getName() + "(" + chartDataModel.getIndex() + ")");
                    label.setBounds(0, 0, 200, 35);
                    label.setOpaque(true);
                    label.setBackground(JBColor.background());
                    itemPanel.add(label);
                    RectChart rectChart = new RectChart(this.bottomPanel, "Cpu") {
                        @Override
                        protected void buildTooltip(int showKey, int actualKey, boolean newChart) {
                            ChartDataModel dataModel = buildTooltipItem(actualKey, dataMap, chartDataModel.getIndex());
                            tooltip.showThreadStatusTip(this, showKey + "", dataModel, newChart);
                        }
                    };
                    buildThreadPanel(chartDataModel, itemPanel, rectChart);
                }
            });
            List<ChartDataModel> chartDataModels1 = deadThreadMap.get(integer);
            if (chartDataModels1 != null && chartDataModels1.size() > 0) {
                chartDataModels1.forEach(chartDataModel -> {
                    String name = chartDataModel.getName();
                    int index = chartDataModel.getIndex();
                    threadMap.remove(index);
                    rectChartList.removeIf(cha -> cha.getName().equals(name + index + "chart"));
                    Component[] components = threadInfoPanel.getComponents();
                    Iterator<Component> iterator = Arrays.stream(components).iterator();
                    while (iterator.hasNext()) {
                        Component next = iterator.next();
                        if (next.getName().equals(name + index + "item")) {
                            threadInfoPanel.remove(next);
                        }
                    }
                });
            }
        });
        if (!rectChartList.isEmpty()) {
            threadLabel.setText("Thread(" + rectChartList.size() + ")");
            for (RectChart rectChart : rectChartList) {
                rectChart.refreshChart(startTime, endTime, dataMap);
            }
        } else {
            threadLabel.setText("Thread(0)");
        }
    }

    private void buildThreadPanel(ChartDataModel chartDataModel, JBPanel itemPanel, RectChart rectChart) {
        rectChart.setFold(false);
        rectChart.setMaxDisplayX(this.bottomPanel.getPublisher().getStandard().getMaxDisplayMillis());
        rectChart.setMinMarkIntervalX(this.bottomPanel.getPublisher().getStandard().getMinMarkInterval());
        rectChart.setSectionNumY(1);
        rectChart.setAxisLabelY("");
        rectChart.setEnableSelect(false);
        rectChart.setThreadId(chartDataModel.getIndex());
        rectChart.setBounds(0, 0, bottomPanel.getWidth(), 35);
        rectChart.setOpaque(true);
        rectChart.setName(chartDataModel.getName() + chartDataModel.getIndex() + "chart");
        itemPanel.add(rectChart);
        itemPanel.setName(chartDataModel.getName() + chartDataModel.getIndex() + "item");
        rectChartList.add(rectChart);
        rectChart.setOpaque(false);
        threadInfoPanel.setOpaque(false);
        threadInfoPanel.setBackground(JBColor.background().darker());
        threadInfoPanel.add(itemPanel, "wrap, height 30!, gapy 0");
    }

    /**
     * buildTooltipItem
     *
     * @param time time
     * @param dataMap dataMap
     * @param index index
     * @return ChartDataModel
     */
    private ChartDataModel buildTooltipItem(int time, LinkedHashMap<Integer, List<ChartDataModel>> dataMap, int index) {
        ChartDataModel chartDataModel = new ChartDataModel();
        if (dataMap == null || dataMap.size() == 0 || dataMap.get(time) == null) {
            return chartDataModel;
        }
        for (ChartDataModel model : dataMap.get(time)) {
            if (index == model.getIndex()) {
                chartDataModel = model;
            }
        }
        return chartDataModel;
    }
}
