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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.datasources.utils.session.entity.SessionInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartStandard;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.customcomp.CustomJButton;
import ohos.devtools.views.layout.chartview.CountingThread;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.event.IChartEventObserver;
import ohos.devtools.views.layout.chartview.event.IChartEventPublisher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.SwingWorker;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static ohos.devtools.views.common.LayoutConstants.INITIAL_VALUE;
import static ohos.devtools.views.layout.chartview.utils.ChartViewConstants.CHART_START_DELAY;
import static ohos.devtools.views.layout.chartview.utils.ChartViewConstants.REFRESH_FREQ;

/**
 * 监控界面保存Chart的面板的事件发布者
 */
public class ProfilerChartsViewPublisher implements IChartEventPublisher {
    private static final Logger LOGGER = LogManager.getLogger(ProfilerChartsViewPublisher.class);

    /**
     * Chart监控界面的定时刷新线程的名称
     */
    private static final String RUN_NAME = "ProfilerChartsViewMonitorTimer";

    /**
     * Chart监控界面的定时刷新线程的进度条名称
     */
    private static final String RUN_NAME_SCROLLBAR = "ScrollbarTimer";

    private static final int NUM_10 = 10;

    /**
     * 监听的视图
     */
    private final ProfilerChartsView view;

    /**
     * 是否为Trace文件静态导入模式
     *
     * @see "true表示静态导入，false表示动态实时跟踪"
     */
    private final boolean traceFile;

    /**
     * 监听者的集合
     */
    private final List<IChartEventObserver> observers = new ArrayList<>();

    /**
     * 绘图标准
     */
    private final ChartStandard standard;

    /**
     * Chart刷新线程是否在运行
     */
    private boolean refreshing = false;

    /**
     * 滚动条是否显示
     */
    private boolean displayScrollbar = false;

    /**
     * 启动任务时，本机时间和数据流中的时间偏移量
     */
    private long startOffset = INITIAL_VALUE;

    /**
     * 构造函数
     *
     * @param view 监听的视图
     * @param traceFile 是否为Trace文件静态导入模式
     */
    public ProfilerChartsViewPublisher(ProfilerChartsView view, boolean traceFile) {
        this.view = view;
        this.traceFile = traceFile;
        standard = new ChartStandard(view.getSessionId());
    }

    /**
     * 展示Trace文件分析结果
     *
     * @param firstTimestamp Trace文件中数据的开始时间
     * @param lastTimestamp Trace文件中数据的结束时间
     */
    public void showTraceResult(long firstTimestamp, long lastTimestamp) {
        if (!traceFile) {
            return;
        }

        standard.setFirstTimestamp(firstTimestamp);
        // 保存trace文件导入模式下最后一个数据的时间戳
        standard.setLastTimestamp(lastTimestamp);
        int end = (int) (lastTimestamp - firstTimestamp);
        int start = 0;
        if (end > standard.getMaxDisplayMillis()) {
            start = end - standard.getMaxDisplayMillis();
            // 这里需要异步初始化滚动条，否则会因为view没有渲染导致滚动条无法显示
            new SwingWorker<>() {
                @Override
                protected Object doInBackground() {
                    try {
                        TimeUnit.MILLISECONDS.sleep(LayoutConstants.FIVE_HUNDRED);
                    } catch (InterruptedException exception) {
                        if (ProfilerLogManager.isErrorEnabled()) {
                            LOGGER.error("Asynchronous initialization scrollbar failed!", exception);
                        }
                    }
                    return new Object();
                }

                @Override
                protected void done() {
                    view.initScrollbar();
                    view.getHorizontalBar().resizeAndReposition();
                    displayScrollbar = true;
                }
            }.execute();
        }
        notifyRefresh(start, end);
    }

    /**
     * 检查Loading结果：Loading完成后启动刷新
     */
    public void checkLoadingResult() {
        new SwingWorker<SessionInfo, Object>() {
            @Override
            protected SessionInfo doInBackground() throws InterruptedException {
                SessionInfo info = SessionManager.getInstance().getSessionInfo(standard.getSessionId());
                while (info == null || !info.isStartRefsh()) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(NUM_10);
                    } catch (InterruptedException exception) {
                        if (ProfilerLogManager.isErrorEnabled()) {
                            LOGGER.error("checkLoadingResult InterruptedException", exception);
                        }
                    }
                    info = SessionManager.getInstance().getSessionInfo(standard.getSessionId());
                }
                // 等待一段时间再启动刷新Chart，否则会导致查询的数据还未入库完成
                TimeUnit.MILLISECONDS.sleep(CHART_START_DELAY);
                return info;
            }

            @Override
            protected void done() {
                try {
                    SessionInfo info = get();
                    long first = info.getStartTimestamp();
                    view.hideLoading();
                    startRefresh(first);
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error(String.format(Locale.ENGLISH, "Error occur when loading done: %s", e.toString()));
                }
            }
        }.execute();
    }

    /**
     * 开始刷新Chart
     *
     * @param firstTimestamp 本次Chart第一个数据的时间戳
     */
    public void startRefresh(long firstTimestamp) {
        if (traceFile) {
            return;
        }
        view.setStop(false);
        view.setPause(false);
        standard.setFirstTimestamp(firstTimestamp);
        startOffset = DateTimeUtil.getNowTimeLong() - standard.getFirstTimestamp();
        // 启动Chart绘制定时器
        startChartTimer();
        refreshing = true;
    }

    /**
     * 启动Chart绘制定时器
     */
    private void startChartTimer() {
        // 启动绘制Chart线程
        QuartzManager.getInstance().addExecutor(RUN_NAME, () -> {
            try {
                // 保存LastTimestamp，为当前时间戳减去Chart启动延迟
                standard.setLastTimestamp(DateTimeUtil.getNowTimeLong() - startOffset - CHART_START_DELAY);
                int end = (int) (standard.getLastTimestamp() - standard.getFirstTimestamp());
                int start = end > standard.getMaxDisplayMillis() ? end - standard.getMaxDisplayMillis() : 0;
                notifyRefresh(start, end);
            } catch (Exception exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("Exception", exception);
                }
            }
        });
        // 刷新间隔暂定30ms
        QuartzManager.getInstance().startExecutor(RUN_NAME, 0, REFRESH_FREQ);

        // 如果有线程在刷新，则不需要再另起一个轮询线程。
        if (!refreshing) {
            QuartzManager.getInstance().addExecutor(RUN_NAME_SCROLLBAR, () -> {
                // 保存LastTimestamp，为当前时间戳减去Chart启动延迟
                standard.setLastTimestamp(DateTimeUtil.getNowTimeLong() - startOffset - CHART_START_DELAY);
                int end = (int) (standard.getLastTimestamp() - standard.getFirstTimestamp());
                int start = end > standard.getMaxDisplayMillis() ? end - standard.getMaxDisplayMillis() : 0;
                if (view.getAbilitySlice() != null) {
                    // Set the last time of the chart
                    view.getAbilitySlice().setLastTimestamp(end);
                }
                // 当end大于最大展示时间时，且滚动条未显示时，初始化显示滚动条，并把isScrollbarShow置为true
                if (end > standard.getMaxDisplayMillis() && !displayScrollbar) {
                    // isScrollbarShow判断必须保留，否则会导致Scrollbar重复初始化，频繁闪烁
                    view.initScrollbar();
                    displayScrollbar = true;
                }
                notifyRefreshScrollbar(start, end);
            });

            // 刷新间隔暂定30ms
            QuartzManager.getInstance().startExecutor(RUN_NAME_SCROLLBAR, 0, REFRESH_FREQ);
        }
    }

    /**
     * 暂停刷新Chart
     */
    public void pauseRefresh() {
        if (refreshing) {
            QuartzManager.getInstance().deleteExecutor(RUN_NAME);
            refreshing = false;
            view.setPause(true);
        }
    }

    /**
     * 停止刷新Chart
     *
     * @param isOffline 设备是否断连
     */
    public void stopRefresh(boolean isOffline) {
        QuartzManager.getInstance().deleteExecutor(RUN_NAME);
        QuartzManager.getInstance().deleteExecutor(RUN_NAME_SCROLLBAR);
        refreshing = false;
        view.setStop(true);
        view.setPause(true);
        if (isOffline) {
            CustomJButton buttonRun = view.getTaskScenePanelChart().getjButtonRun();
            CustomJButton buttonStop = view.getTaskScenePanelChart().getjButtonStop();
            buttonRun.setIcon(IconLoader.getIcon("/images/breakpoint_grey.png", getClass()));
            buttonRun.setEnabled(true);
            ActionListener[] actionListenersRun = buttonRun.getActionListeners();
            for (ActionListener listener : actionListenersRun) {
                buttonRun.removeActionListener(listener);
            }

            buttonStop.setIcon(AllIcons.Process.ProgressResumeHover);
            buttonStop.setEnabled(true);
            ActionListener[] actionListenersStop = buttonStop.getActionListeners();
            for (ActionListener listener : actionListenersStop) {
                buttonStop.removeActionListener(listener);
            }
            CountingThread countingThread = view.getTaskScenePanelChart().getCounting();
            countingThread.setStopFlag(true);
        }
    }

    /**
     * 暂停后重新开始刷新Chart
     */
    public void restartRefresh() {
        if (traceFile) {
            return;
        }

        if (view.isStop()) {
            // 如果是已停止状态，则返回
            return;
        }

        if (!refreshing) {
            refreshing = true;
            view.setPause(false);
            startChartTimer();
            // 重新开始时，也要移除框选状态（暂定）
            standard.clearAllSelectedRanges();
        }
    }

    /**
     * Add observer
     *
     * @param observer Observer of chart refreshes event
     */
    @Override
    public void attach(IChartEventObserver observer) {
        observers.add(observer);
    }

    /**
     * Remove observer
     *
     * @param observer Observer of chart refreshes event
     */
    @Override
    public void detach(IChartEventObserver observer) {
        observers.remove(observer);
    }

    /**
     * notify to refresh
     *
     * @param start Start time of chart
     * @param end End time of chart
     */
    @Override
    public void notifyRefresh(int start, int end) {
        standard.updateDisplayTimeRange(start, end);
        ChartDataRange range = standard.getDisplayRange();
        observers.forEach(lis -> lis.refreshView(range, standard.getFirstTimestamp(), !traceFile));
    }

    /**
     * 通知刷新滚动条
     *
     * @param start 开始时间
     * @param end 结束时间
     */
    private void notifyRefreshScrollbar(int start, int end) {
        // 当前时间超过最大展示时间，则调整滚动条长度和位置
        if (end > standard.getMaxDisplayMillis()) {
            if (view.getHorizontalBar() != null) {
                view.getHorizontalBar().resizeAndReposition();
            }
        }
    }

    /**
     * 时间线和char缩放
     *
     * @param startTime 缩放后的界面开始时间
     * @param endTime 结束时间的界面开始时间
     * @param maxDisplayTime 窗体上可以显示的最大毫秒数
     */
    public void charZoom(int startTime, int endTime, int maxDisplayTime) {
        // 修改char展示的时间范围
        standard.setMaxDisplayMillis(maxDisplayTime);
        standard.updateDisplayTimeRange(startTime, endTime);
        observers.forEach(lis -> {
            standard.setMaxDisplayMillis(maxDisplayTime);
            lis.refreshStandard(startTime, endTime, maxDisplayTime, standard.getMinMarkInterval());
            lis.refreshView(standard.getDisplayRange(), standard.getFirstTimestamp(), !traceFile);
        });
    }

    /**
     * 界面毫秒数的时间刻度缩放
     *
     * @param maxDisplayTime 窗体上可以显示的最大毫秒数
     * @param minMarkInterval 窗体上可以显示的时间刻度的单位
     * @param newStartTime 新的开始时间
     * @param newEndTime 新的结束时间
     */
    public void msTimeZoom(int maxDisplayTime, int minMarkInterval, int newStartTime, int newEndTime) {
        standard.setMaxDisplayMillis(maxDisplayTime);
        standard.setMinMarkInterval(minMarkInterval);
        standard.updateDisplayTimeRange(newStartTime, newEndTime);
        observers.forEach(lis -> {
            lis.refreshStandard(newStartTime, newEndTime, maxDisplayTime, minMarkInterval);
            lis.refreshView(standard.getDisplayRange(), standard.getFirstTimestamp(), !traceFile);
        });
    }

    /**
     * Getter
     *
     * @return ChartStandard
     */
    public ChartStandard getStandard() {
        return standard;
    }

    /**
     * Getter
     *
     * @return traceFile
     */
    public boolean isTraceFile() {
        return traceFile;
    }

    public List<IChartEventObserver> getObservers() {
        return observers;
    }

    public boolean isRefreshing() {
        return refreshing;
    }

    public boolean isDisplayScrollbar() {
        return displayScrollbar;
    }

    public void setDisplayScrollbar(boolean displayScrollbar) {
        this.displayScrollbar = displayScrollbar;
    }
}
