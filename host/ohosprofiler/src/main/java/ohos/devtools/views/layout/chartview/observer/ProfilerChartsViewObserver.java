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

import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.datasources.utils.session.entity.SessionInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.services.memory.ChartDataCache;
import ohos.devtools.services.memory.MemoryService;
import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartStandard;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.hoscomp.HosJButton;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.event.IChartEventObserver;
import ohos.devtools.views.layout.chartview.event.IChartEventPublisher;
import ohos.devtools.views.layout.swing.CountingThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.ImageIcon;
import javax.swing.SwingWorker;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static ohos.devtools.views.common.LayoutConstants.CHART_START_DELAY;
import static ohos.devtools.views.common.ViewConstants.INITIAL_VALUE;
import static ohos.devtools.views.common.ViewConstants.NUM_10;
import static ohos.devtools.views.common.ViewConstants.REFRESH_FREQ;

/**
 * 监控界面保存Chart的面板的事件发布者
 *
 * @since 2021/1/26 20:34
 */
public class ProfilerChartsViewObserver implements IChartEventPublisher {
    /**
     * 日志
     */
    private static final Logger LOGGER = LogManager.getLogger(ProfilerChartsViewObserver.class);

    /**
     * Chart监控界面的定时刷新线程的名称
     */
    private static final String RUN_NAME = "ProfilerChartsViewMonitorTimer";

    /**
     * Chart监控界面的定时刷新线程的进度条名称
     */
    private static final String RUN_NAME_SCROLLBAR = "ScrollbarTimer";

    /**
     * 监听的视图
     */
    private final ProfilerChartsView view;

    /**
     * 是否为Trace文件静态导入模式
     *
     * @see "true表示静态导入，false表示动态实时跟踪"
     */
    private final boolean isTraceFile;

    /**
     * 监听者的集合
     */
    private final List<IChartEventObserver> listeners = new ArrayList<>();

    /**
     * 异步初始化滚动条线程池
     */
    private final ThreadPoolExecutor scrollBarThreadPool =
        new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    /**
     * 绘图标准
     */
    private final ChartStandard standard;

    /**
     * Chart刷新线程是否在运行
     */
    private boolean isRefreshing = false;

    /**
     * 滚动条是否显示
     */
    private boolean isScrollbarShow = false;

    /**
     * 启动任务时，本机时间和数据流中的时间偏移量
     */
    private long startOffset = INITIAL_VALUE;

    /**
     * 刷新缓存所需要的时间范围
     */
    private ChartDataRange range = new ChartDataRange();

    /**
     * 构造函数
     *
     * @param view        监听的视图
     * @param isTraceFile 是否为Trace文件静态导入模式
     */
    public ProfilerChartsViewObserver(ProfilerChartsView view, boolean isTraceFile) {
        this.view = view;
        this.isTraceFile = isTraceFile;
        standard = new ChartStandard(view.getSessionId());
    }

    /**
     * 展示Trace文件分析结果
     *
     * @param firstTimestamp Trace文件中数据的开始时间
     * @param lastTimestamp  Trace文件中数据的结束时间
     */
    public void showTraceResult(long firstTimestamp, long lastTimestamp) {
        if (!isTraceFile) {
            return;
        }

        standard.setFirstTimestamp(firstTimestamp);
        // 保存trace文件导入模式下最后一个数据的时间戳
        standard.setLastTimestamp(lastTimestamp);
        int end = (int) (lastTimestamp - firstTimestamp);
        int start;
        if (end > standard.getMaxDisplayMillis()) {
            start = end - standard.getMaxDisplayMillis();
            // 这里需要异步初始化滚动条，否则会因为view没有渲染导致滚动条无法显示
            scrollBarThreadPool.execute(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(LayoutConstants.FIVE_HUNDRED);
                    view.initScrollbar();
                    view.getHorizontalBar().resizeAndReposition();
                    isScrollbarShow = true;
                } catch (InterruptedException exception) {
                    LOGGER.error("Asynchronous initialization scrollbar failed!", exception);
                }
            });
        } else {
            start = 0;
        }
        notifyRefresh(start, end);
    }

    /**
     * 检查Loading结果：Loading完成后启动刷新
     */
    public void checkLoadingResult() {
        new SwingWorker<SessionInfo, Object>() {
            @Override
            protected SessionInfo doInBackground() {
                SessionInfo info = SessionManager.getInstance().isRefsh(standard.getSessionId());
                while (info == null || !info.isStartRefsh()) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(NUM_10);
                    } catch (InterruptedException exception) {
                        LOGGER.info("InterruptedException");
                    }
                    info = SessionManager.getInstance().isRefsh(standard.getSessionId());
                }
                return info;
            }

            @Override
            protected void done() {
                try {
                    SessionInfo info = get();
                    long first = info.getStartTimestamp();
                    // 等待一段时间再启动刷新Chart，否则会导致查询的数据还未入库完成
                    TimeUnit.MILLISECONDS.sleep(CHART_START_DELAY);
                    view.hideLoading();
                    // 启动缓存和Chart
                    ChartDataCache.getInstance()
                        .initCache(String.valueOf(standard.getSessionId()), LayoutConstants.TWENTY_FIVE);
                    MemoryService.getInstance().addData(standard.getSessionId(), 0, CHART_START_DELAY, first);
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
     * @param firstTimestamp 本次Chart最后一个数据的时间戳
     */
    public void startRefresh(long firstTimestamp) {
        if (isTraceFile) {
            return;
        }
        view.setStopFlag(false);
        view.setFlagEnd(false);
        standard.setFirstTimestamp(firstTimestamp);
        startOffset = DateTimeUtil.getNowTimeLong() - standard.getFirstTimestamp();
        // 启动Chart绘制定时器
        startChartTimer();
        isRefreshing = true;
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
                LOGGER.error(exception.getMessage());
            }
        });
        // 刷新间隔暂定30ms
        QuartzManager.getInstance().startExecutor(RUN_NAME, 0, REFRESH_FREQ);

        // 如果有线程在刷新，则不需要再另起一个轮询线程。
        if (!isRefreshing) {
            QuartzManager.getInstance().addExecutor(RUN_NAME_SCROLLBAR, () -> {
                // 保存LastTimestamp，为当前时间戳减去Chart启动延迟
                standard.setLastTimestamp(DateTimeUtil.getNowTimeLong() - startOffset - CHART_START_DELAY);
                int end = (int) (standard.getLastTimestamp() - standard.getFirstTimestamp());
                int start = end > standard.getMaxDisplayMillis() ? end - standard.getMaxDisplayMillis() : 0;
                // 当end大于最大展示时间时，且滚动条未显示时，初始化显示滚动条，并把isScrollbarShow置为true
                if (end > standard.getMaxDisplayMillis() && !isScrollbarShow) {
                    // isScrollbarShow判断必须保留，否则会导致Scrollbar重复初始化，频繁闪烁
                    view.initScrollbar();
                    isScrollbarShow = true;
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
        if (isRefreshing) {
            QuartzManager.getInstance().endExecutor(RUN_NAME);
            isRefreshing = false;
            view.setFlagEnd(true);
        }
    }

    /**
     * 停止刷新Chart
     *
     * @param isOffline 设备是否断连
     */
    public void stopRefresh(boolean isOffline) {
        QuartzManager.getInstance().endExecutor(RUN_NAME);
        QuartzManager.getInstance().endExecutor(RUN_NAME_SCROLLBAR);
        isRefreshing = false;
        view.setStopFlag(true);
        view.setFlagEnd(true);
        if (isOffline) {
            HosJButton buttonRun = view.getTaskScenePanelChart().getjButtonRun();
            HosJButton buttonStop = view.getTaskScenePanelChart().getjButtonStop();
            buttonRun.setIcon(
                new ImageIcon(ProfilerChartsViewObserver.class.getClassLoader().getResource("images/over.png")));
            buttonRun.setEnabled(true);
            ActionListener[] actionListenersRun = buttonRun.getActionListeners();
            for (ActionListener listener : actionListenersRun) {
                buttonRun.removeActionListener(listener);
            }
            buttonStop.setIcon(
                new ImageIcon(ProfilerChartsViewObserver.class.getClassLoader().getResource("images/suspended.png")));
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
        if (isTraceFile) {
            return;
        }

        if (view.isStopFlag()) {
            // 如果是已停止状态，则返回
            return;
        }

        if (!isRefreshing) {
            isRefreshing = true;
            view.setFlagEnd(false);
            startChartTimer();
            // 重新开始时，也要移除框选状态（暂定）
            standard.clearSelectedRange();
        }
    }

    /**
     * 添加监听者
     *
     * @param listener IChartEventListener
     */
    @Override
    public void attach(IChartEventObserver listener) {
        listeners.add(listener);
    }

    /**
     * 移除监听者
     *
     * @param listener IChartEventListener
     */
    @Override
    public void detach(IChartEventObserver listener) {
        listeners.remove(listener);
    }

    /**
     * 通知刷新
     *
     * @param start 开始时间
     * @param end   结束时间
     */
    @Override
    public void notifyRefresh(int start, int end) {
        standard.updateDisplayTimeRange(start, end);
        listeners.forEach((lis) -> {
            if (!(lis instanceof CacheObserver)) {
                lis.refreshView(standard.getDisplayRange(), standard.getFirstTimestamp(), !isTraceFile);
            }
        });
    }

    /**
     * 通知刷新滚动条
     *
     * @param start 开始时间
     * @param end   结束时间
     */
    private void notifyRefreshScrollbar(int start, int end) {
        // 当前时间超过最大展示时间，则调整滚动条长度和位置
        if (end > standard.getMaxDisplayMillis()) {
            if (view.getHorizontalBar() != null) {
                view.getHorizontalBar().resizeAndReposition();
            }
        }
        // 不暂停缓存的观察者
        listeners.forEach((lis) -> {
            if (lis instanceof CacheObserver) {
                range.setStartTime(start);
                range.setEndTime(end);
                lis.refreshView(range, standard.getFirstTimestamp(), !isTraceFile);
            }
        });
    }

    /**
     * 时间线和char缩放
     *
     * @param startTime      缩放后的界面开始时间
     * @param endTime        结束时间的界面开始时间
     * @param maxDisplayTime 窗体上可以显示的最大毫秒数
     */
    public void charZoom(int startTime, int endTime, int maxDisplayTime) {
        // 修改char展示的时间范围
        standard.setMaxDisplayMillis(maxDisplayTime);
        standard.updateDisplayTimeRange(startTime, endTime);
        // standard绘图标准（这个参数传输需要创建新的对象，而缩放功能只是修改部分时间线和char的标准，所以这个参数建议移出）
        listeners.forEach((lis) -> {
            if (!(lis instanceof CacheObserver)) {
                standard.setMaxDisplayMillis(maxDisplayTime);
                lis.refreshStandard(standard, startTime, endTime, maxDisplayTime);
                lis.refreshView(standard.getDisplayRange(), standard.getFirstTimestamp(), !isTraceFile);
            }
        });
    }

    /**
     * 界面毫秒数的时间刻度缩放
     *
     * @param maxDisplayTime  窗体上可以显示的最大毫秒数
     * @param minMarkInterval 窗体上可以显示的时间刻度的单位
     * @param newStartTime    新的开始时间
     * @param newEndTime      新的结束时间
     */
    public void msTimeZoom(int maxDisplayTime, int minMarkInterval, int newStartTime, int newEndTime) {
        standard.setMaxDisplayMillis(maxDisplayTime);
        standard.setMinMarkInterval(minMarkInterval);
        standard.updateDisplayTimeRange(newStartTime, newEndTime);
        // standard绘图标准（这个参数传输需要创建新的对象，而缩放功能只是修改部分时间线和chart的标准，所以这个参数建议移出）
        listeners.forEach((lis) -> {
            if (!(lis instanceof CacheObserver)) {
                lis.refreshStandard(standard, newStartTime, newEndTime, maxDisplayTime);
                lis.refreshView(standard.getDisplayRange(), standard.getFirstTimestamp(), !isTraceFile);
            }
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
     * @return isTraceFile
     */
    public boolean isTraceFile() {
        return isTraceFile;
    }

    public List<IChartEventObserver> getListeners() {
        return listeners;
    }

    public boolean isRefreshing() {
        return isRefreshing;
    }

    public boolean isScrollbarShow() {
        return isScrollbarShow;
    }

    public void setScrollbarShow(boolean scrollbarShow) {
        isScrollbarShow = scrollbarShow;
    }
}
