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

package ohos.devtools.views.layout.chartview;

import com.intellij.ui.components.JBLabel;
import ohos.devtools.views.common.ProfilerMonitorItem;
import ohos.devtools.views.common.chart.ProfilerScrollbar;
import ohos.devtools.views.common.chart.ProfilerTimeline;
import ohos.devtools.views.layout.chartview.observer.CacheObserver;
import ohos.devtools.views.layout.chartview.observer.ProfilerChartsViewObserver;
import ohos.devtools.views.layout.chartview.observer.TimelineObserver;
import ohos.devtools.views.layout.swing.TaskScenePanelChart;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static java.awt.Image.SCALE_DEFAULT;
import static javax.swing.SpringLayout.Constraints;
import static ohos.devtools.views.common.ColorConstants.CHART_BG;
import static ohos.devtools.views.common.LayoutConstants.LOADING_SIZE;
import static ohos.devtools.views.common.ViewConstants.NUM_2;
import static ohos.devtools.views.common.ViewConstants.TIMELINE_HEIGHT;
import static ohos.devtools.views.common.ViewConstants.TIMELINE_WIDTH;

/**
 * 监控界面Chart面板，包括顶部Timeline和Chart
 *
 * @since 2021/1/25 9:30
 */
public class ProfilerChartsView extends JPanel {
    /**
     * sessionMap
     */
    public static Map<Long, ProfilerChartsView> sessionMap = new HashMap<>();

    /**
     * sessionId
     */
    private final long sessionId;

    /**
     * Chart显示界面的Monitor
     */
    private final ProfilerChartsViewObserver observer;

    /**
     * 保存时间线和监控项的Pane
     *
     * @see "加这个Panel是由于Java heap的table之前会add到south，和滚动条位置冲突，造成两个组件闪烁"
     */
    private final JPanel mainPanel;

    /**
     * 保存各个指标项的面板
     */
    private final ItemViewsPanel itemsPanel;

    /**
     * 需要绘制标尺的组件集合
     *
     * @see "Map<组件，是否已经绘制了标尺>"
     */
    private final Map<JComponent, Boolean> rulerCompMap = new HashMap<>();

    /**
     * 绘制标尺的X坐标，以Chart和Timeline起点为0点
     */
    private int rulerXCoordinate = 0;

    /**
     * 鼠标当前进入的组件
     */
    private JComponent currentEntered;

    /**
     * 自定义水平滚动条
     */
    private ProfilerScrollbar horizontalBar;

    /**
     * 暂停标志
     */
    private boolean flagEnd = false;

    /**
     * 停止标志
     */
    private boolean stopFlag = false;

    /**
     * 向下扩展标志
     */
    private boolean flagDown = false;

    /**
     * 新增配置项标志
     */
    private boolean addItemFlag = false;

    private final TaskScenePanelChart taskScenePanelChart;

    private ProfilerTimeline timeline;

    private JPanel loadingPanel;

    /**
     * chart界面的层级判断
     *
     * @see "true：二级界面，false：一级界面"
     */
    private boolean chartLevel = false;

    /**
     * chart界面的是否展开
     *
     * @see "true：二级界面，false：一级界面"
     */
    private boolean ableUnfoldTable = false;

    /**
     * Chart是否在加载（初始化时等待数据库处理数据）
     */
    private boolean isLoading = false;

    /**
     * 构造函数
     *
     * @param sessionId           JPanel
     * @param isTraceFile         是否为Trace文件静态导入模式
     * @param taskScenePanelChart 用于chart滚动条拖动，获取对象，设置暂停或者开始按钮图标
     */
    public ProfilerChartsView(long sessionId, boolean isTraceFile, TaskScenePanelChart taskScenePanelChart) {
        super(true);
        this.setOpaque(true);
        this.setLayout(new BorderLayout());
        this.setBackground(CHART_BG);

        // 初始化mainPanel并add至当前View
        this.mainPanel = new JPanel(new BorderLayout());
        this.mainPanel.setOpaque(true);
        this.add(mainPanel, BorderLayout.CENTER);

        this.sessionId = sessionId;
        this.taskScenePanelChart = taskScenePanelChart;
        this.observer = new ProfilerChartsViewObserver(this, isTraceFile);
        // 初始化Timeline
        initTimeline(taskScenePanelChart);
        // 添加
        CacheObserver chartObserver = new CacheObserver(sessionId);
        this.getObserver().attach(chartObserver);

        // 初始化并添加保存各个指标项的布局
        this.itemsPanel = new ItemViewsPanel(this);
        this.mainPanel.add(itemsPanel, BorderLayout.CENTER);
        sessionMap.put(this.sessionId, this);
        // 添加组件大小变化的监听器
        addResizedListener();
    }

    /**
     * 添加组件大小变化的监听器
     */
    private void addResizedListener() {
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent exception) {
                // 组件大小变化时，要调整滚动条的大小和位置
                if (horizontalBar != null) {
                    horizontalBar.resizeAndReposition();
                }
            }
        });
    }

    /**
     * 初始化Timeline
     *
     * @param taskScenePanelChart taskScenePanelChart
     */
    private void initTimeline(TaskScenePanelChart taskScenePanelChart) {
        // 初始化时间线，这里宽高给一个预设值
        timeline = new ProfilerTimeline(this, TIMELINE_WIDTH, TIMELINE_HEIGHT, taskScenePanelChart);
        // 保存时间线的绘图标准
        timeline.setMaxDisplayTime(observer.getStandard().getMaxDisplayMillis());
        timeline.setMinMarkInterval(observer.getStandard().getMinMarkInterval());

        // 创建Timeline的观察者，并注册至主界面
        TimelineObserver timelineObserver = new TimelineObserver(timeline);
        observer.attach(timelineObserver);
        // Timeline需要绘制标尺，把他加入集合
        this.addRulerComp(timeline);

        // 组件添加至mainPanel
        this.mainPanel.add(timeline, BorderLayout.NORTH);
    }

    /**
     * 初始化水平滚动条
     */
    public void initScrollbar() {
        this.horizontalBar = new ProfilerScrollbar(this);
        this.mainPanel.add(horizontalBar, BorderLayout.SOUTH);
        this.observer.setScrollbarShow(true);
    }

    /**
     * 移除水平滚动条
     */
    public void removeScrollbar() {
        this.observer.setScrollbarShow(false);
        if (horizontalBar != null) {
            this.mainPanel.remove(horizontalBar);
            this.horizontalBar = null;
        }
    }

    /**
     * 显示Loading标识，并且禁用停止和暂停按钮事件
     */
    public void showLoading() {
        SpringLayout spring = new SpringLayout();
        loadingPanel = new JPanel(spring);
        loadingPanel.setBackground(CHART_BG);
        JBLabel loadingLabel = new JBLabel();

        new SwingWorker<>() {
            @Override
            protected Object doInBackground() {
                URL url = ProfilerChartsView.class.getClassLoader().getResource("images/loading.gif");
                if (url != null) {
                    ImageIcon icon = new ImageIcon(url);
                    icon.setImage(icon.getImage().getScaledInstance(LOADING_SIZE, LOADING_SIZE, SCALE_DEFAULT));
                    loadingLabel.setIcon(icon);
                }
                loadingPanel.add(loadingLabel);

                return loadingPanel;
            }

            @Override
            protected void done() {
                // 增加约束，保持Loading图在组件中间
                Constraints loadingCons = spring.getConstraints(loadingLabel);
                loadingCons.setX(Spring.constant((loadingPanel.getWidth() - LOADING_SIZE) / NUM_2));
                loadingCons.setY(Spring.constant((loadingPanel.getHeight() - LOADING_SIZE) / NUM_2));
                // 手动触发组件布局事件
                loadingPanel.revalidate();

                loadingPanel.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent componentEvent) {
                        super.componentResized(componentEvent);
                        Constraints loadingCons = spring.getConstraints(loadingLabel);
                        loadingCons.setX(Spring.constant((loadingPanel.getWidth() - LOADING_SIZE) / NUM_2));
                        loadingCons.setY(Spring.constant((loadingPanel.getHeight() - LOADING_SIZE) / NUM_2));
                        loadingPanel.revalidate();
                    }
                });

                // Loading界面加载完成，现在开始检查Loading结果：Loading完成后启动刷新
                observer.checkLoadingResult();
            }
        }.execute();

        isLoading = true;
        this.remove(mainPanel);
        this.add(loadingPanel, BorderLayout.CENTER);
    }

    /**
     * 隐藏Loading标识，并且禁用停止和暂停按钮事件
     */
    public void hideLoading() {
        if (loadingPanel != null) {
            this.remove(loadingPanel);
        }
        isLoading = false;
        this.add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * 添加一项监控的指标项视图
     *
     * @param item 指标项枚举类
     */
    public void addMonitorItemView(ProfilerMonitorItem item) {
        itemsPanel.addMonitorItemView(item);
    }

    /**
     * 添加内存信息二级视图
     *
     * @return MemoryStageView
     */
    public MemoryStageView addMemoryStageView() {
        chartLevel = true;
        removeMonitorItemView();
        MemoryStageView memoryStageItem = new MemoryStageView(this);
        this.mainPanel.add(memoryStageItem, BorderLayout.CENTER);
        return memoryStageItem;
    }

    /**
     * 创建二级界面时，移除一级界面的监控项视图
     *
     * @see "不能通过visible来实现，这样从二级界面返回一级时，itemsPanel宽高无法自适应"
     */
    private void removeMonitorItemView() {
        this.mainPanel.remove(itemsPanel);
    }

    /**
     * 移除二级界面视图
     *
     * @param stageView MemoryStageView
     */
    void removeStageView(MemoryStageView stageView) {
        this.mainPanel.remove(stageView);
        // Chart和Title也从标尺Map中移除
        rulerCompMap.remove(stageView.getChart());
        rulerCompMap.remove(stageView.getItemTitleStagePanel());
    }

    /**
     * 返回一级界面时，重新添加监控项视图
     */
    void resumeMonitorItemView() {
        // 返回一级界面时增加关闭展开
        flagDown = false;
        chartLevel = false;
        for (Component comp : itemsPanel.getComponents()) {
            if (comp instanceof AbsItemView) {
                AbsItemView absItemView = (AbsItemView) comp;
                absItemView.getChart().initMaxUnitY();
            }
        }
        this.mainPanel.add(itemsPanel);
    }

    /**
     * 添加需要绘制标尺的组件
     *
     * @param comp JComponent
     */
    void addRulerComp(JComponent comp) {
        rulerCompMap.put(comp, Boolean.FALSE);
    }

    /**
     * 组件的标尺已绘制
     *
     * @param comp JComponent
     */
    public void compRulerDrawn(JComponent comp) {
        rulerCompMap.put(comp, Boolean.TRUE);
    }

    /**
     * 重置组件标尺状态为未绘制
     */
    public void resetRulerDrawStatus() {
        rulerCompMap.keySet().forEach((comp -> rulerCompMap.put(comp, Boolean.FALSE)));
    }

    /**
     * 刷新组件列表的标尺
     */
    public void refreshCompRuler() {
        rulerCompMap.forEach((comp, isDrawn) -> {
            // 如果已经绘制过，不需要再重复绘制，防止无限循环
            if (!isDrawn) {
                comp.repaint();
                comp.revalidate();
            }
        });
    }

    public ProfilerChartsViewObserver getObserver() {
        return observer;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public int getRulerXCoordinate() {
        return rulerXCoordinate;
    }

    public void setRulerXCoordinate(int rulerXCoordinate) {
        this.rulerXCoordinate = rulerXCoordinate;
    }

    public JComponent getCurrentEntered() {
        return currentEntered;
    }

    public void setCurrentEntered(JComponent currentEntered) {
        this.currentEntered = currentEntered;
    }

    public ProfilerScrollbar getHorizontalBar() {
        return horizontalBar;
    }

    public long getSessionId() {
        return sessionId;
    }

    public boolean isFlagEnd() {
        return flagEnd;
    }

    public void setFlagEnd(boolean flagEnd) {
        this.flagEnd = flagEnd;
    }

    public boolean isStopFlag() {
        return stopFlag;
    }

    public void setStopFlag(boolean stopFlag) {
        this.stopFlag = stopFlag;
    }

    public boolean isFlagDown() {
        return flagDown;
    }

    public void setFlagDown(boolean flagDown) {
        this.flagDown = flagDown;
    }

    public boolean isAddItemFlag() {
        return addItemFlag;
    }

    public void setAddItemFlag(boolean addItemFlag) {
        this.addItemFlag = addItemFlag;
    }

    public ProfilerTimeline getTimeline() {
        return timeline;
    }

    public TaskScenePanelChart getTaskScenePanelChart() {
        return taskScenePanelChart;
    }

    public boolean isChartLevel() {
        return chartLevel;
    }

    public boolean isAbleUnfoldTable() {
        return ableUnfoldTable;
    }

    public void setAbleUnfoldTable(boolean ableUnfoldTable) {
        this.ableUnfoldTable = ableUnfoldTable;
    }

    public boolean isLoading() {
        return isLoading;
    }
}
