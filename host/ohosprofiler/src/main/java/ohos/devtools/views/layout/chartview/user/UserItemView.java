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

package ohos.devtools.views.layout.chartview.user;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import model.AbstractSdk;
import model.bean.ChartEnum;
import model.bean.Legend;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.charts.FilledLineChart;
import ohos.devtools.views.charts.LineChart;
import ohos.devtools.views.charts.ProfilerChart;
import ohos.devtools.views.charts.model.ChartDataModel;
import ohos.devtools.views.charts.model.ChartLegendColorRect;
import ohos.devtools.views.charts.tooltip.TooltipItem;
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.layout.chartview.ItemsView;
import ohos.devtools.views.layout.chartview.MonitorItemView;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.ProfilerMonitorItem;
import ohos.devtools.views.layout.chartview.observer.UserChartObserver;
import ohos.devtools.views.user.UserManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static ohos.devtools.views.charts.utils.ChartUtils.divide;

/**
 * Sdk Item View
 *
 * @since 2021/11/22
 */
public class UserItemView extends MonitorItemView {
    private static final Logger LOGGER = LogManager.getLogger(UserItemView.class);
    private static final int NUM = 2;

    /**
     * KB，MB转换时的单位
     */
    private static final int UNIT = 1024;

    private UserChartObserver chartObserver;
    private JBLabel foldBtn;
    private ProfilerMonitorItem item;
    private List<Map<JBLabel, ChartLegendColorRect>> list = new LinkedList<>();
    private List<Legend> legends = new LinkedList<>();
    private AbstractSdk sdkImpl;

    /**
     * Sdk Item View
     */
    public UserItemView() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("create UserItemView");
        }
    }

    @Override
    public void init(ProfilerChartsView bottomPanel, ItemsView parent, ProfilerMonitorItem item) {
        this.bottomPanel = bottomPanel;
        this.parent = parent;
        this.item = item;
        this.setLayout(new BorderLayout());
        UserManager manager = UserManager.getInstance();
        sdkImpl = manager.getSdkImpl().get();
        setLegendsComp();
        addChart();
        UserTitleView titleView = new UserTitleView();
        this.add(titleView, BorderLayout.NORTH);
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                int chartHeight = event.getComponent().getHeight() - titleView.getHeight();
                UserItemView.this.chart.setPreferredSize(new Dimension(event.getComponent().getWidth(), chartHeight));
                UserItemView.this.chart.repaint();
            }
        });
    }

    private void setLegendsComp() {
        List<Legend> legendList = sdkImpl.getLegends();
        legends = legendList;
        for (Legend item : legendList) {
            Map<JBLabel, ChartLegendColorRect> map = new LinkedHashMap<>();
            JBLabel legendLabel = new JBLabel();
            legendLabel.setText(item.getLegendName());
            ChartLegendColorRect legendLabelColor =
                new ChartLegendColorRect(item.getLegendWidth(), item.getLegendHeight());
            legendLabel.setOpaque(false);
            legendLabelColor.setColor(item.getLegendColor());
            legendLabelColor.setOpaque(false);
            map.put(legendLabel, legendLabelColor);
            list.add(map);
        }
    }

    /**
     * Add chart panel
     */
    private void addChart() {
        ChartEnum type = sdkImpl.getChartType();
        if (type.getName().equals("chart")) {
            chart = generateChart();
        }
        if (type.getName().equals("broken_line")) {
            chart = generateBrokenLine();
        }
        // Register the chart observer to the ProfilerChartsView and listen to the refresh events of the main interface
        chartObserver = new UserChartObserver(chart, bottomPanel.getSessionId(), true, sdkImpl);
        this.bottomPanel.getPublisher().attach(chartObserver);
        this.add(chart, BorderLayout.CENTER);
    }

    /**
     * generateChart
     *
     * @return ProfilerChart
     */
    private ProfilerChart generateBrokenLine() {
        ProfilerChart diskIoChart = new LineChart(this.bottomPanel, item.getName()) {
            @Override
            protected void initLegends() {
                UserItemView.this.initChartLegends(legends);
            }

            @Override
            protected void buildLegends(List<ChartDataModel> lastModels) {
                UserItemView.this.buildChartLegends(lastModels);
            }

            @Override
            protected void buildTooltip(int showKey, int actualKey, boolean newChart) {
                List<TooltipItem> tooltipItems = buildTooltipItems(actualKey, dataMap);
                tooltip.showTip(this, showKey + "", "", tooltipItems, newChart, axisLabelY);
            }
        };
        diskIoChart.setMaxDisplayX(this.bottomPanel.getPublisher().getStandard().getMaxDisplayMillis());
        diskIoChart.setMinMarkIntervalX(this.bottomPanel.getPublisher().getStandard().getMinMarkInterval());
        diskIoChart.setSectionNumY(NUM);
        diskIoChart.setAxisLabelY(sdkImpl.getUnit());
        diskIoChart.setFold(true);
        diskIoChart.setEnableSelect(false);
        return diskIoChart;
    }

    private ProfilerChart generateChart() {
        ProfilerChart memoryChart = new FilledLineChart(this.bottomPanel, item.getName(), true) {
            @Override
            protected void initLegends() {
                UserItemView.this.initChartLegends(legends);
            }

            @Override
            protected String getYaxisLabelStr(int value) {
                // Here we get KB, we need to convert it to MB
                return value == maxUnitY ? divide(value, UNIT) + " " + axisLabelY : divide(value, UNIT) + "";
            }

            @Override
            protected void buildLegends(List<ChartDataModel> lastModels) {
                UserItemView.this.buildChartLegends(lastModels);
            }

            @Override
            protected void buildTooltip(int showKey, int actualKey, boolean newChart) {
                String totalValue = calcTotal(actualKey, dataMap);
                List<TooltipItem> tooltipItems = buildTooltipItems(actualKey, dataMap);
                tooltip.showTip(this, showKey + "", totalValue, tooltipItems, newChart, axisLabelY);
            }
        };
        memoryChart.setMaxDisplayX(this.bottomPanel.getPublisher().getStandard().getMaxDisplayMillis());
        memoryChart.setMinMarkIntervalX(this.bottomPanel.getPublisher().getStandard().getMinMarkInterval());
        memoryChart.setSectionNumY(NUM);
        memoryChart.setAxisLabelY(sdkImpl.getUnit());
        memoryChart.setFold(true);
        memoryChart.setEnableSelect(false);
        return memoryChart;
    }

    private String calcTotal(int time, LinkedHashMap<Integer, List<ChartDataModel>> dataMap) {
        List<ChartDataModel> models = dataMap.get(time);
        if (models == null || models.size() == 0) {
            return "";
        }
        // Here we get KB, we need to convert it to MB
        int value = chart.getListSum(models, 0);
        return divide(value, UNIT).toString();
    }

    /**
     * Build tooltip items
     *
     * @param time Current time
     * @param dataMap dataMap
     * @return List　<TooltipItem>
     */
    private List<TooltipItem> buildTooltipItems(int time, LinkedHashMap<Integer, List<ChartDataModel>> dataMap) {
        List<TooltipItem> tooltipItems = new ArrayList<>();
        if (dataMap == null || dataMap.size() == 0 || dataMap.get(time) == null) {
            return tooltipItems;
        }
        List<ChartDataModel> chartDataModels = dataMap.get(time);
        for (int index = 0; index < chartDataModels.size(); index++) {
            ChartDataModel model = chartDataModels.get(index);
            Legend legend = legends.get(index);
            String text = String
                .format(Locale.ENGLISH, "%s%s%s", legend.getLegendName(), model.getValue(), chart.getAxisLabelY());
            TooltipItem tooltipItem = new TooltipItem(legend.getLegendColor(), text);
            tooltipItems.add(tooltipItem);
        }
        return tooltipItems;
    }

    /**
     * Init legend components of chart
     *
     * @param legends legends
     */
    private void initChartLegends(JBPanel legends) {
        for (Map<JBLabel, ChartLegendColorRect> map : list) {
            map.forEach((key, value) -> {
                checkAndAdd(legends, value);
                checkAndAdd(legends, key);
            });
        }
    }

    private void checkAndAdd(JBPanel legends, Component component) {
        boolean contain = false;
        for (Component legend : legends.getComponents()) {
            if (legend.equals(component)) {
                contain = true;
                break;
            }
        }
        if (!contain) {
            legends.add(component);
        }
        component.setVisible(true);
    }

    private void buildChartLegends(List<ChartDataModel> lastModels) {
        for (int index = 0; index < list.size(); index++) {
            Map<JBLabel, ChartLegendColorRect> map = list.get(index);
            Legend legend = legends.get(index);
            int finalIndex = index;
            map.forEach((key, value) -> {
                key.setText(legend.getLegendName() + lastModels.get(finalIndex).getValue() + sdkImpl.getUnit());
            });
        }
    }

    class UserTitleView extends JBPanel {
        UserTitleView() {
            this.setLayout(new FlowLayout(FlowLayout.LEFT));
            initFixedComps();
            this.setBackground(JBColor.background().brighter());
        }

        private void initFixedComps() {
            foldBtn = new JBLabel();
            foldBtn.setName(UtConstant.UT_DISK_IO_VIEW_FOLD);
            foldBtn.setIcon(AllIcons.General.ArrowRight);
            this.add(foldBtn);
            foldBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    foldBtnClick();
                }
            });
            JBLabel title = new JBLabel(item.getName());
            this.add(title);
        }

        private void foldBtnClick() {
            fold = !fold;
            // Item fold, buttons hide
            if (fold) {
                chart.setFold(true);
                chart.setEnableSelect(false);
                chart.getTooltip().hideTip();
                foldBtn.setIcon(AllIcons.General.ArrowRight);
            } else {
                chart.setFold(false);
                chart.setEnableSelect(false);
                foldBtn.setIcon(AllIcons.General.ArrowDown);
            }
            parent.itemFoldOrExpend(fold, UserItemView.this);
            // Initialize the maximum value of Y axis here,  because it may change after fold/expand
            chart.initMaxUnitY();
            chartObserver.setChartFold(fold);
        }
    }

}
