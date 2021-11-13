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

package ohos.devtools.views.layout.chartview.cpu;

import static ohos.devtools.services.hiperf.PerfCommand.PERF_TRACE_PATH;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.datasources.transport.grpc.SystemTraceHelper;
import ohos.devtools.datasources.utils.common.GrpcException;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.services.hiperf.HiPerfCommand;
import ohos.devtools.services.hiperf.PerfCommand;
import ohos.devtools.views.charts.FilledLineChart;
import ohos.devtools.views.charts.ProfilerChart;
import ohos.devtools.views.charts.model.ChartDataModel;
import ohos.devtools.views.charts.model.ChartLegendColorRect;
import ohos.devtools.views.charts.tooltip.TooltipItem;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.common.customcomp.DottedLine;
import ohos.devtools.views.layout.chartview.ItemsView;
import ohos.devtools.views.layout.chartview.MonitorItemView;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.ProfilerMonitorItem;
import ohos.devtools.views.layout.chartview.observer.CpuChartObserver;
import ohos.devtools.views.layout.dialog.CpuItemViewLoadDialog;
import ohos.devtools.views.layout.dialog.SampleDialog;
import ohos.devtools.views.layout.utils.EventTrackUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Cpu monitor item view
 *
 * @since : 2021/10/25
 */
public class CpuItemView extends MonitorItemView {
    private static final Logger LOGGER = LogManager.getLogger(CpuItemView.class);

    /**
     * Chart组件的初始宽度
     */
    private static final int CHART_INIT_WIDTH = 1000;

    /**
     * Chart的默认高度
     */
    private static final int CHART_DEFAULT_HEIGHT = 150;

    private static final int NUM_2 = 2;

    ArrayList<String> schedulingEvents = new ArrayList<String>(Arrays.asList(
            "sched/sched_switch",
            "power/suspend_resume",
            "sched/sched_wakeup",
            "sched/sched_wakeup_new",
            "sched/sched_waking",
            "sched/sched_process_exit",
            "sched/sched_process_free",
            "task/task_newtask",
            "task/task_rename"));

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    private CpuChartObserver chartObserver;
    private JBLabel foldBtn;
    private final JBLabel totalLabel = new JBLabel();
    private final ChartLegendColorRect totalColor = new ChartLegendColorRect();
    private final JBLabel appLabel = new JBLabel();
    private final ChartLegendColorRect appColor = new ChartLegendColorRect();
    private final JBLabel systemLabel = new JBLabel();
    private final ChartLegendColorRect systemColor = new ChartLegendColorRect();
    private long sessionId;
    private JComboBox<String> jComboBoxType = new JComboBox<String>();
    private JButton jButtonRecord = new JButton("Record");
    private JBPanel threadNorthPanel = new JBPanel(null);
    private JBScrollPane threadScrollPane;
    private JBPanel threadCenterPanel = new JBPanel();
    private JBLabel threadLabel = new JBLabel("Threads (10)");
    private JBPanel southCpuThreadPanel = new JBPanel(new BorderLayout());
    private JBPanel threadInfoPanel;
    private ProfilerMonitorItem item;
    private ProcessInfo processInfo;

    /**
     * Constructors
     */
    public CpuItemView() {
    }

    @Override
    public void init(ProfilerChartsView bottomPanel, ItemsView parent, ProfilerMonitorItem item) {
        this.setName(UtConstant.UT_CPU_ITEM_VIEW);
        this.item = item;
        this.bottomPanel = bottomPanel;
        this.parent = parent;
        this.sessionId = bottomPanel.getSessionId();
        processInfo = new ProcessInfo();
        this.setLayout(new BorderLayout());
        this.add(new CpuTitleView(), BorderLayout.NORTH);
        initLegendsComp();
        addChart();
    }

    /**
     * initLegendsComp
     */
    private void initLegendsComp() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initLegendsComp");
        }
        totalLabel.setPreferredSize(new Dimension(60, 13));
        totalLabel.setOpaque(false);
        totalColor.setColor(ColorConstants.ENERGY_EVENT_LOCATION);
        totalColor.setOpaque(false);
        appLabel.setPreferredSize(new Dimension(60, 13));
        appLabel.setOpaque(false);
        appColor.setColor(ColorConstants.MEM_JAVA);
        appColor.setOpaque(false);
        systemLabel.setPreferredSize(new Dimension(70, 13));
        systemLabel.setOpaque(false);
        systemColor.setColor(ColorConstants.MEM_NATIVE);
        systemColor.setOpaque(false);
    }

    /**
     * Add chart panel
     */
    private void addChart() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addChart");
        }
        chart = generateChart();
        chart.setBounds(0, 0, CHART_INIT_WIDTH, CHART_DEFAULT_HEIGHT);
        southCpuThreadPanel.setPreferredSize(new Dimension(50, 0));
        threadNorthPanel.setPreferredSize(new Dimension(50, 30));
        southCpuThreadPanel.add(threadNorthPanel, BorderLayout.NORTH);
        threadInfoPanel = new JBPanel(new MigLayout("inset 0, gapy 0", "[grow,fill]",
            "[fill,fill]"));
        threadScrollPane = new JBScrollPane(threadInfoPanel);
        southCpuThreadPanel.add(threadScrollPane, BorderLayout.CENTER);
        threadLabel.setBounds(15, 0, 100, 30);
        threadLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        threadNorthPanel.add(threadLabel);
        threadNorthPanel.setOpaque(true);
        threadNorthPanel.setBackground(JBColor.background());
        this.add(southCpuThreadPanel, BorderLayout.SOUTH);
        // Register the chart observer to the ProfilerChartsView and listen to the refresh events of the main interface
        chartObserver = new CpuChartObserver(chart, threadInfoPanel, bottomPanel, true, threadLabel);
        this.bottomPanel.getPublisher().attach(chartObserver);
        this.add(chart, BorderLayout.CENTER);
    }

    private ProfilerChart generateChart() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("generateChart");
        }
        ProfilerChart cpuChart = new FilledLineChart(this.bottomPanel, item.getName(), true) {
            @Override
            protected void initLegends() {
                CpuItemView.this.initChartLegends(legends);
            }

            @Override
            protected void buildLegends(List<ChartDataModel> lastModels) {
                CpuItemView.this.buildChartLegends(lastModels);
            }

            @Override
            protected void buildTooltip(int showKey, int actualKey, boolean newChart) {
                String totalValue = calcTotal(actualKey, dataMap);
                List<TooltipItem> tooltipItems = buildTooltipItems(actualKey, dataMap);
                tooltip.showTip(this, showKey + "", totalValue, tooltipItems, newChart, "%");
            }
        };
        cpuChart.setFold(true);
        cpuChart.setMaxDisplayX(this.bottomPanel.getPublisher().getStandard().getMaxDisplayMillis());
        cpuChart.setMinMarkIntervalX(this.bottomPanel.getPublisher().getStandard().getMinMarkInterval());
        cpuChart.setSectionNumY(NUM_2);
        cpuChart.setAxisLabelY("%");
        cpuChart.setMaxUnitY(100);
        cpuChart.setEnableSelect(false);
        return cpuChart;
    }

    /**
     * Calculate the total value at a time
     *
     * @param time time
     * @param dataMap dataMap
     * @return Totalֵ
     */
    private String calcTotal(int time, LinkedHashMap<Integer, List<ChartDataModel>> dataMap) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("calcTotal");
        }
        List<ChartDataModel> models = dataMap.get(time);
        if (models == null || models.size() == 0) {
            return "";
        }
        int value = chart.getListSum(models, 0);
        return String.valueOf(value);
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
        checkAndAdd(legends, totalColor);
        checkAndAdd(legends, totalLabel);
        checkAndAdd(legends, systemColor);
        checkAndAdd(legends, systemLabel);
        checkAndAdd(legends, appColor);
        checkAndAdd(legends, appLabel);
    }

    /**
     * checkAndAdd
     *
     * @param legends legends
     * @param component component
     */
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
    }

    /**
     * buildChartLegends
     *
     * @param lastModels lastModels
     */
    private void buildChartLegends(List<ChartDataModel> lastModels) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("buildChartLegends");
        }
        new SwingWorker<>() {
            @Override
            protected Object doInBackground() {
                // Total label
                int total = chart.getListSum(lastModels, 0);
                String totalText;
                if (fold) {
                    totalText = String.format(Locale.ENGLISH, "Total:%s%s", total, chart.getAxisLabelY());
                    systemLabel.setVisible(false);
                    systemColor.setVisible(false);
                    appLabel.setVisible(false);
                    appColor.setVisible(false);
                    totalLabel.setText(totalText);
                    totalColor.setVisible(true);
                    totalLabel.setVisible(true);
                } else {
                    totalText = String.format(Locale.ENGLISH, "Total:%s%s", total, chart.getAxisLabelY());
                    if (lastModels != null) {
                        lastModels.forEach(chartDataModel -> CpuItemView.this.parseModelToLegend(chartDataModel));
                    }
                    totalColor.setVisible(false);
                    totalLabel.setVisible(false);
                }
                return new Object();
            }
        }.execute();
    }

    /**
     * Processing data into legend and remove from allItemLegendMap
     *
     * @param model Data model
     */
    private void parseModelToLegend(ChartDataModel model) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("parseModelToLegend");
        }
        String itemParam = model.getName();
        switch (itemParam) {
            case "System":
                systemLabel.setText("System:" + model.getValue() + chart.getAxisLabelY());
                systemLabel.setVisible(true);
                systemColor.setVisible(true);
                break;
            case "App":
                appLabel.setText("App:" + model.getValue() + chart.getAxisLabelY());
                appLabel.setVisible(true);
                appColor.setVisible(true);
                break;
            default:
                break;
        }
    }

    /**
     * Build tooltip items
     *
     * @param time Current time
     * @param dataMap dataMap
     * @return List
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

    private class CpuTitleView extends JBPanel {
        /**
         * Save the components should be hidden when item fold
         */
        private JBPanel hiddenComp;

        CpuTitleView() {
            this.setLayout(new FlowLayout(FlowLayout.LEFT));
            initFixedComps();
            initHiddenComponents();
            this.setBackground(JBColor.background().brighter());
        }

        private void initFixedComps() {
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("initFixedComps");
            }
            foldBtn = new JBLabel();
            foldBtn.setName(UtConstant.UT_CPU_ITEM_VIEW_FOLD);
            foldBtn.setIcon(AllIcons.General.ArrowRight);
            this.add(foldBtn);
            foldBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    super.mouseClicked(event);
                    foldBtnClick();
                }
            });
            JBLabel title = new JBLabel(item.getName());
            this.add(title);
        }

        private void initHiddenComponents() {
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("initHiddenComponents");
            }
            hiddenComp = new JBPanel(new FlowLayout(FlowLayout.LEFT));
            hiddenComp.setBackground(JBColor.background().brighter());
            hiddenComp.add(new DottedLine());
            this.add(hiddenComp);
            jComboBoxType.setName(UtConstant.UT_CPU_ITEM_VIEW_COMBO);
            jComboBoxType.addItem(LayoutConstants.SAMPLE_PERF_DATA);
            jComboBoxType.addItem(LayoutConstants.TRACE_SYSTEM_CALLS);
            jComboBoxType.setBounds(880, 0, 200, 30);
            jButtonRecord.setBounds(1100, 0, 60, 30);
            jButtonRecord.setOpaque(false);
            jButtonRecordAddListener();
            hiddenComp.add(jComboBoxType);
            hiddenComp.add(jButtonRecord);
            hiddenComp.setVisible(false);
        }

        private void jButtonRecordAddListener() {
            jButtonRecord.addActionListener(new ActionListener() {
                CpuItemViewLoadDialog cpuItemViewLoadDialog = null;
                String requestSessionId = null;
                DeviceIPPortInfo deviceIPPortInfo = SessionManager.getInstance().getDeviceInfoBySessionId(sessionId);

                @Override
                public void actionPerformed(ActionEvent e) {
                    Date date = new Date();
                    if (jComboBoxType.getSelectedItem().toString().equals(LayoutConstants.TRACE_SYSTEM_CALLS)) {
                        ArrayList<ArrayList<String>> eventsList = new ArrayList();
                        eventsList.add(schedulingEvents);
                        EventTrackUtils.getInstance().trackApplicationTrace();
                        try {
                            //  requestSessionId = new SystemTraceHelper()
                            //  .createSessionByTraceRequestNoParam(deviceIPPortInfo, sdf.format(date), null);
                            requestSessionId = new SystemTraceHelper()
                                .createSessionHtraceRequestForCpu(deviceIPPortInfo, eventsList, sdf.format(date));
                        } catch (GrpcException grpcException) {
                            grpcException.printStackTrace();
                        }
                        if (Optional.ofNullable(requestSessionId).isPresent()) {
                            cpuItemViewLoadDialog = new CpuItemViewLoadDialog(bottomPanel, false, requestSessionId);
                            cpuItemViewLoadDialog.load(deviceIPPortInfo, requestSessionId, sdf.format(date));
                        } else {
                            new SampleDialog("prompt", "The corresponding file is missing!").show();
                        }
                    } else {
                        String processName = SessionManager.getInstance().getProcessName(sessionId);
                        processInfo.setProcessName(processName);
                        EventTrackUtils.getInstance().trackApplicationPerfTrace();
                        String fileStorePath = PERF_TRACE_PATH.concat(sdf.format(date)).concat(".trace");
                        boolean isLeakOhos = deviceIPPortInfo.getDeviceType() == DeviceType.LEAN_HOS_DEVICE;
                        PerfCommand perfCommand =
                            new HiPerfCommand(sessionId, isLeakOhos, deviceIPPortInfo.getDeviceID(), fileStorePath);
                        perfCommand.executeRecord();
                        cpuItemViewLoadDialog = new CpuItemViewLoadDialog(bottomPanel, true, requestSessionId);
                        cpuItemViewLoadDialog.setCommand(perfCommand);
                        cpuItemViewLoadDialog.load(deviceIPPortInfo, requestSessionId, sdf.format(date));

                    }
                }
            });
        }

        private void foldBtnClick() {
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("foldBtnClick");
            }
            fold = !fold;
            // Item fold, buttons hide
            hiddenComp.setVisible(!fold);
            if (fold) {
                chart.setFold(true);
                southCpuThreadPanel.setPreferredSize(new Dimension(50, 0));
                chart.getTooltip().hideTip();
                foldBtn.setIcon(AllIcons.General.ArrowRight);
            } else {
                EventTrackUtils.getInstance().trackApplicationCpu();
                chart.setFold(false);
                southCpuThreadPanel.setPreferredSize(new Dimension(50, 200));
                foldBtn.setIcon(AllIcons.General.ArrowDown);
            }
            parent.itemFoldOrExpend(fold, CpuItemView.this);
            chartObserver.setChartFold(fold);
        }
    }
}
