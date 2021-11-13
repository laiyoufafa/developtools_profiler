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

package ohos.devtools.views.layout.chartview.diskio;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.charts.LineChart;
import ohos.devtools.views.charts.ProfilerChart;
import ohos.devtools.views.charts.model.ChartDataModel;
import ohos.devtools.views.charts.model.ChartLegendColorRect;
import ohos.devtools.views.charts.tooltip.TooltipItem;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.layout.chartview.ItemsView;
import ohos.devtools.views.layout.chartview.MonitorItemView;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.ProfilerMonitorItem;
import ohos.devtools.views.layout.chartview.observer.DiskIoChartObserver;
import ohos.devtools.views.layout.utils.EventTrackUtils;
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
import java.util.List;
import java.util.Locale;

/**
 * DiskIoView
 *
 * @since : 2021/10/25
 */
public class DiskIoView extends MonitorItemView {
    private static final Logger LOGGER = LogManager.getLogger(DiskIoView.class);
    private static final int NUM_2 = 2;

    private DiskIoChartObserver chartObserver;
    private final JBLabel diskIORead = new JBLabel();
    private final ChartLegendColorRect diskIOReadColor = new ChartLegendColorRect(12, 3);
    private final JBLabel diskIOWrite = new JBLabel();
    private final ChartLegendColorRect diskIOWriteColor = new ChartLegendColorRect(12, 3);
    private JBLabel foldBtn;
    private ProfilerMonitorItem item;

    /**
     * DiskIoView
     */
    public DiskIoView() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("DiskIoView init");
        }
    }

    @Override
    public void init(ProfilerChartsView bottomPanel, ItemsView parent, ProfilerMonitorItem item) {
        this.setName(UtConstant.UT_DISK_IO_ITEM_VIEW);
        this.bottomPanel = bottomPanel;
        this.parent = parent;
        this.item = item;
        this.setLayout(new BorderLayout());
        initLegendsComp();
        addChart();
        DiskIOTitleView titleView = new DiskIOTitleView();
        this.add(titleView, BorderLayout.NORTH);
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                int chartHeight = event.getComponent().getHeight() - titleView.getHeight();
                chart.setPreferredSize(new Dimension(event.getComponent().getWidth(), chartHeight));
                chart.repaint();
            }
        });
    }

    /**
     * initLegendsComp
     */
    private void initLegendsComp() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initLegendsComp");
        }
        diskIORead.setOpaque(false);
        diskIOReadColor.setColor(ColorConstants.DISK_IO_READ);
        diskIOReadColor.setOpaque(false);
        diskIOWrite.setOpaque(false);
        diskIOWriteColor.setColor(ColorConstants.DISK_IO_WRITE);
        diskIOWriteColor.setOpaque(false);
    }

    /**
     * Add chart panel
     */
    private void addChart() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addChart");
        }
        chart = generateChart();
        // Register the chart observer to the ProfilerChartsView and listen to the refresh events of the main interface
        chartObserver = new DiskIoChartObserver(chart, bottomPanel.getSessionId(), true);
        this.bottomPanel.getPublisher().attach(chartObserver);
        this.add(chart, BorderLayout.CENTER);
    }

    /**
     * generateChart
     *
     * @return ProfilerChart
     */
    private ProfilerChart generateChart() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("generateChart");
        }
        ProfilerChart diskIoChart = new LineChart(this.bottomPanel, item.getName()) {
            @Override
            protected void initLegends() {
                DiskIoView.this.initChartLegends(legends);
            }

            @Override
            protected void buildLegends(List<ChartDataModel> lastModels) {
                DiskIoView.this.buildChartLegends(lastModels);
            }

            @Override
            protected void buildTooltip(int showKey, int actualKey, boolean newChart) {
                List<TooltipItem> tooltipItems = buildTooltipItems(actualKey, dataMap);
                tooltip.showTip(this, showKey + "", "", tooltipItems, newChart, axisLabelY);
            }
        };
        diskIoChart.setMaxDisplayX(this.bottomPanel.getPublisher().getStandard().getMaxDisplayMillis());
        diskIoChart.setMinMarkIntervalX(this.bottomPanel.getPublisher().getStandard().getMinMarkInterval());
        diskIoChart.setSectionNumY(NUM_2);
        diskIoChart.setAxisLabelY("KB/s");
        diskIoChart.setFold(true);
        diskIoChart.setEnableSelect(false);
        return diskIoChart;
    }

    /**
     * Build tooltip items
     *
     * @param time Current time
     * @param dataMap dataMap
     * @return List　<TooltipItem>
     */
    private List<TooltipItem> buildTooltipItems(int time, LinkedHashMap<Integer, List<ChartDataModel>> dataMap) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("buildTooltipItems");
        }
        List<TooltipItem> tooltipItems = new ArrayList<>();
        if (dataMap == null || dataMap.size() == 0 || dataMap.get(time) == null) {
            return tooltipItems;
        }
        for (ChartDataModel model : dataMap.get(time)) {
            String text =
                String.format(Locale.ENGLISH, "%s:%s%s", model.getName(), model.getValue(), chart.getAxisLabelY());
            TooltipItem tooltipItem = new TooltipItem(model.getColor(), text);
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initChartLegends");
        }
        checkAndAdd(legends, diskIOReadColor);
        checkAndAdd(legends, diskIORead);
        checkAndAdd(legends, diskIOWriteColor);
        checkAndAdd(legends, diskIOWrite);
    }

    private void checkAndAdd(JBPanel legends, Component component) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("checkAndAdd");
        }
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
        diskIORead.setText("DiskIO_read:" + lastModels.get(0).getValue() + "KB/s     ");
        diskIOWrite.setText("DiskIO_write:" + lastModels.get(1).getValue() + "KB/s");
    }

    class DiskIOTitleView extends JBPanel {
        DiskIOTitleView() {
            this.setLayout(new FlowLayout(FlowLayout.LEFT));
            initFixedComps();
            this.setBackground(JBColor.background().brighter());
        }

        private void initFixedComps() {
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("initFixedComps");
            }
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
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("foldBtnClick");
            }
            fold = !fold;
            // Item fold, buttons hide
            if (fold) {
                chart.setFold(true);
                chart.setEnableSelect(false);
                chart.getTooltip().hideTip();
                foldBtn.setIcon(AllIcons.General.ArrowRight);
            } else {
                EventTrackUtils.getInstance().trackApplicationDiskIo();
                chart.setFold(false);
                chart.setEnableSelect(false);
                foldBtn.setIcon(AllIcons.General.ArrowDown);
            }
            parent.itemFoldOrExpend(fold, DiskIoView.this);
            // Initialize the maximum value of Y axis here,  because it may change after fold/expand
            chart.initMaxUnitY();
            chartObserver.setChartFold(fold);
        }
    }
}
