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

import ohos.devtools.datasources.transport.grpc.service.MemoryPluginResult;
import ohos.devtools.datasources.utils.monitorconfig.service.MonitorConfigManager;
import ohos.devtools.services.memory.ChartDataCache;
import ohos.devtools.services.memory.MemoryDao;
import ohos.devtools.views.charts.ProfilerChart;
import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartStandard;
import ohos.devtools.views.charts.model.ChartDataModel;
import ohos.devtools.views.layout.chartview.event.IChartEventObserver;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static ohos.devtools.views.common.ColorConstants.MEMORY;

/**
 * 一级界面Memory Chart的观察者
 *
 * @since 2021/3/1 14:58
 */
public class MemoryChartObserver implements IChartEventObserver {
    private static final Logger LOGGER = LogManager.getLogger(MemoryChartObserver.class);

    /**
     * Profiler Chart
     */
    private final ProfilerChart chart;

    private final Long sessionId;

    /**
     * 构造函数
     *
     * @param chart Profiler Chart
     */
    public MemoryChartObserver(ProfilerChart chart, Long sessionId) {
        this.sessionId = sessionId;
        this.chart = chart;
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
        // char绘制的跟随时间轴的放大缩小
        chart.setMaxDisplayX(standard.getMaxDisplayMillis());
        chart.setMinMarkIntervalX(standard.getMinMarkInterval());

        // 跟随时间线同步
        chart.setStartTime(startTime);
        chart.setEndTime(endTime);

        chart.repaint();
        chart.revalidate();
    }

    /**
     * 刷新视图
     *
     * @param range          时间范围
     * @param firstTimestamp 本次Chart首次创建并启动刷新时的时间戳
     * @param isUseCache     是否使用缓存
     */
    @Override
    public void refreshView(ChartDataRange range, long firstTimestamp, boolean isUseCache) {
        LinkedHashMap<Integer, List<ChartDataModel>> dataMaps = new LinkedHashMap<>();
        LinkedHashMap<Long, MemoryPluginResult.AppSummary> dataMap;

        boolean isChartStop = chart.getBottomPanel().isFlagEnd() || chart.getBottomPanel().isStopFlag();
        if (isChartStop || !isUseCache) {
            // 点击暂停后，读取数据库的时间
            dataMap = MemoryDao.getInstance()
                .getData(sessionId, range.getStartTime(), range.getEndTime(), firstTimestamp, true);
        } else {
            dataMap = ChartDataCache.getInstance()
                .getDataCache(String.valueOf(sessionId), range.getStartTime(), range.getEndTime(), firstTimestamp);
        }

        Map<String, LinkedList<String>> configMap = MonitorConfigManager.dataMap.get(sessionId);
        if (configMap == null) { // 存放离线采集项数据
            LinkedList<String> offLineIterm = new LinkedList<>();
            offLineIterm.add("Java");
            offLineIterm.add("Native");
            offLineIterm.add("Graphics");
            offLineIterm.add("Stack");
            offLineIterm.add("Code");
            offLineIterm.add("Others");
            // 存放memory离线采集项数据
            Map<String, LinkedList<String>> offLineMap = new HashMap<>();
            offLineMap.put("Memory", offLineIterm);
            // 构建离线memory采集项的dataMap对象
            MonitorConfigManager.dataMap.put(sessionId, offLineMap);
            configMap = MonitorConfigManager.dataMap.get(sessionId);
        }

        List<String> configList = configMap.get("Memory");
        for (Long time : dataMap.keySet()) {
            ChartDataModel total = new ChartDataModel();
            total.setName("Total");
            total.setColor(MEMORY);
            MemoryPluginResult.AppSummary app = dataMap.get(time);
            total.setValue(calcTotalValue(app, configList));
            long showTime = time - firstTimestamp;
            dataMaps.put((int) showTime, Collections.singletonList(total));
        }
        chart.refreshChart(range.getStartTime(), range.getEndTime(), dataMaps);
    }

    /**
     * 计算一节界面的Total值，要根据界面选择的指标项，而非所有指标项的Total值
     *
     * @param app        MemoryPluginResult.AppSummary
     * @param configList List
     * @return int
     */
    private int calcTotalValue(MemoryPluginResult.AppSummary app, List<String> configList) {
        int total = 0;
        if (CollectionUtils.isEmpty(configList)) {
            total = (int) (app.getJavaHeap() + app.getNativeHeap() + app.getGraphics() + app.getStack() + app.getCode()
                + app.getPrivateOther());
            return total;
        }

        if (configList.contains("Java")) {
            total += (int) (app.getJavaHeap());
        }
        if (configList.contains("Native")) {
            total += (int) (app.getNativeHeap());
        }
        if (configList.contains("Graphics")) {
            total += (int) (app.getGraphics());
        }
        if (configList.contains("Stack")) {
            total += (int) (app.getStack());
        }
        if (configList.contains("Code")) {
            total += (int) (app.getCode());
        }
        if (configList.contains("Others")) {
            total += (int) (app.getPrivateOther());
        }
        return total;
    }

}
