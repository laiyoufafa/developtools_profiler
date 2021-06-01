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

import ohos.devtools.views.charts.FilledLineChart;
import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.tooltip.LegendTooltip;
import ohos.devtools.views.layout.chartview.observer.MemoryChartObserver;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static ohos.devtools.views.common.ColorConstants.CHART_BG;
import static ohos.devtools.views.common.ViewConstants.CHART_DEFAULT_HEIGHT;
import static ohos.devtools.views.common.ViewConstants.CHART_INIT_WIDTH;
import static ohos.devtools.views.common.ViewConstants.NUM_2;

/**
 * 内存指标分析项的视图面板
 *
 * @since 2021/1/23 11:40
 */
public class MemoryItemView extends AbsItemView {
    /**
     * 视图名称
     */
    private static final String NAME = "Memory";

    /**
     * 构造函数
     *
     * @param bottomPanel 最底层面板
     * @param parent      当前视图的父级容器
     * @param coordinateY 当前面板在父级面板中的Y轴坐标，即Y轴偏移量
     */
    public MemoryItemView(ProfilerChartsView bottomPanel, ItemViewsPanel parent, int coordinateY) {
        super(bottomPanel, parent, coordinateY);
        // 初始化标题面板
        initTitlePanel(NAME);
        // 添加Chart
        addChart();
    }

    /**
     * 添加Chart面板
     */
    private void addChart() {
        chart = new FilledLineChart(this.bottomPanel);
        chart.setMaxDisplayX(this.bottomPanel.getObserver().getStandard().getMaxDisplayMillis());
        chart.setMinMarkIntervalX(this.bottomPanel.getObserver().getStandard().getMinMarkInterval());
        chart.setSectionNumY(NUM_2);
        chart.setAxisLabelY("MB");
        // 这里宽度给一个预设值，后面要根据chartPanel的宽度改变
        chart.setBounds(0, 0, CHART_INIT_WIDTH, CHART_DEFAULT_HEIGHT);
        // Chart添加点击事件，点击后进入二级界面
        chart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                MemoryStageView memoryStageView = bottomPanel.addMemoryStageView();
                memoryStageView.addChart();
                // 隐藏Tooltip，否则进入二级界面后，Tooltip仍然显示一级界面的数组
                LegendTooltip.getInstance().hideTip();
                // 手动刷新一次界面，否则暂停后进入二级界面Chart不会绘制
                ChartDataRange range = bottomPanel.getObserver().getStandard().getDisplayRange();
                bottomPanel.getObserver().notifyRefresh(range.getStartTime(), range.getEndTime());
                // 根据AbleUnfoldTable属性判断是否是选中状态
                if (bottomPanel.isAbleUnfoldTable()) {
                    bottomPanel.getTaskScenePanelChart().getjButtonBottom().setIcon(new ImageIcon(
                        MemoryItemView.class.getClassLoader().getResource("images/button_bottom_bar.png")));
                } else {
                    bottomPanel.getTaskScenePanelChart().getjButtonBottom().setIcon(new ImageIcon(
                        MemoryItemView.class.getClassLoader().getResource("images/button_bottom_bar_grey.png")));
                }
            }
        });

        // 创建只存放Chart的面板，保证Chart可以正常添加至其他面板
        JPanel onlyChartPanel = new JPanel(null);
        onlyChartPanel.add(chart);
        // 添加chart面板背景颜色
        onlyChartPanel.setBackground(CHART_BG);
        // 添加监听事件，面板宽高改变的时候要修改Chart的宽高
        onlyChartPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                chart.setBounds(0, 0, componentEvent.getComponent().getWidth(),
                    componentEvent.getComponent().getHeight());
                onlyChartPanel.repaint();
                onlyChartPanel.revalidate();
            }
        });

        // Chart需要绘制标尺，把他加入集合
        this.bottomPanel.addRulerComp(chart);
        // 把Chart观察者注册到主界面，监听主界面的刷新时事件
        MemoryChartObserver chartObserver = new MemoryChartObserver(chart, bottomPanel.getSessionId());
        this.bottomPanel.getObserver().attach(chartObserver);

        this.add(onlyChartPanel, BorderLayout.CENTER);
    }
}
