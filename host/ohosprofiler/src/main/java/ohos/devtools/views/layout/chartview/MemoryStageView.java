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
import ohos.devtools.views.charts.ProfilerChart;
import ohos.devtools.views.layout.chartview.observer.MemoryStageObserver;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import static ohos.devtools.views.common.ColorConstants.CHART_BG;
import static ohos.devtools.views.common.ViewConstants.CHART_DEFAULT_HEIGHT;
import static ohos.devtools.views.common.ViewConstants.CHART_INIT_WIDTH;
import static ohos.devtools.views.common.ViewConstants.NUM_2;

/**
 * MemoryStageView类
 *
 * @version 1.0
 * @date 2021/2/2 19:02
 **/
public class MemoryStageView extends JPanel {
    /**
     * 视图名称
     */
    private static final String NAME = "Memory";

    /**
     * 最底层面板
     */
    private final ProfilerChartsView bottomPanel;

    private MemoryStageTitleView memoryStageTitleView;

    private ProfilerChart chart;

    private MemoryStageObserver chartObserver;

    /**
     * 构造函数
     *
     * @param bottomPanel 最底层面板
     */
    public MemoryStageView(ProfilerChartsView bottomPanel) {
        super(true);
        this.setLayout(new BorderLayout());
        this.bottomPanel = bottomPanel;
        initTitlePanel();
    }

    /**
     * 初始化标题面板
     */
    private void initTitlePanel() {
        memoryStageTitleView = new MemoryStageTitleView(this.bottomPanel, NAME, this);
        this.add(memoryStageTitleView, BorderLayout.NORTH);
        // 标题面板需要绘制标尺，把他加入集合
        this.bottomPanel.addRulerComp(memoryStageTitleView);
    }

    /**
     * 添加Chart面板
     */
    public void addChart() {
        chart = new FilledLineChart(this.bottomPanel);
        chart.setMaxDisplayX(this.bottomPanel.getObserver().getStandard().getMaxDisplayMillis());
        chart.setMinMarkIntervalX(this.bottomPanel.getObserver().getStandard().getMinMarkInterval());
        chart.setSectionNumY(NUM_2);
        chart.setAxisLabelY("MB");
        // 这里宽度给一个预设值，后面要根据chartPanel的宽度改变
        chart.setBounds(0, 0, CHART_INIT_WIDTH, CHART_DEFAULT_HEIGHT);

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
        chartObserver = new MemoryStageObserver(chart, bottomPanel.getSessionId());
        this.bottomPanel.getObserver().attach(chartObserver);
        this.add(onlyChartPanel, BorderLayout.CENTER);
    }

    ProfilerChart getChart() {
        return chart;
    }

    MemoryStageTitleView getItemTitleStagePanel() {
        return memoryStageTitleView;
    }

    /**
     * getChartObserver
     *
     * @return MemoryStageObserver
     */
    public MemoryStageObserver getChartObserver() {
        return chartObserver;
    }
}
