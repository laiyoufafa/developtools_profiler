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

package ohos.devtools.views.layout.chartview.memory;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.datasources.utils.session.entity.SessionInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.services.memory.agentbean.AgentHeapBean;
import ohos.devtools.services.memory.agentbean.MemoryInstanceDetailsInfo;
import ohos.devtools.services.memory.agentbean.MemoryInstanceInfo;
import ohos.devtools.services.memory.agentdao.MemoryInstanceDetailsManager;
import ohos.devtools.services.memory.agentdao.MemoryInstanceManager;
import ohos.devtools.views.charts.FilledLineChart;
import ohos.devtools.views.charts.ProfilerChart;
import ohos.devtools.views.charts.model.ChartDataModel;
import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartLegendColorRect;
import ohos.devtools.views.charts.model.ChartStandard;
import ohos.devtools.views.charts.tooltip.TooltipItem;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.chartview.MonitorItemDetail;
import ohos.devtools.views.layout.chartview.ProfilerMonitorItem;
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.common.customcomp.DottedLine;
import ohos.devtools.views.common.treetable.ExpandTreeTable;
import ohos.devtools.views.layout.chartview.ItemsView;
import ohos.devtools.views.layout.chartview.MonitorItemView;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.observer.MemoryChartObserver;

import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import static ohos.devtools.views.layout.chartview.utils.ChartViewConstants.NUM_1024;
import static ohos.devtools.views.charts.utils.ChartUtils.divide;
import static ohos.devtools.views.layout.chartview.MonitorItemDetail.MEM_CODE;
import static ohos.devtools.views.layout.chartview.MonitorItemDetail.MEM_GRAPHICS;
import static ohos.devtools.views.layout.chartview.MonitorItemDetail.MEM_JAVA;
import static ohos.devtools.views.layout.chartview.MonitorItemDetail.MEM_NATIVE;
import static ohos.devtools.views.layout.chartview.MonitorItemDetail.MEM_OTHERS;
import static ohos.devtools.views.layout.chartview.MonitorItemDetail.MEM_STACK;

/**
 * Memory monitor item view
 */
public class MemoryItemView extends MonitorItemView {
    /**
     * Splitter PROPORTION
     */
    private static final float PROPORTION_SEGMENT_HEAP = 0.5f;

    /**
     * instance title width
     */
    private static final int INSTANCE_TITLE_WIDTH = 630;

    /**
     * instance title height
     */
    private static final int INSTANCE_TITLE_HEIGHT = 34;

    /**
     * instance pane height
     */
    private static final int INSTANCE_PANE_HEIGHT = 450;

    /**
     * instance jScrollPane height
     */
    private static final int INSTANCE_SCROLL_HEIGHT = 425;

    /**
     * instance view width
     */
    private static final float PROPORTION_SEGMENT_VIEW = 0.4f;

    /**
     * instance view width
     */
    private static final int HEAP_VIEW_PANEL_WIDTH = 410;

    private static final int NUM_418 = 418;

    private static final int NUM_384 = 384;

    private static final int NUM_2 = 2;

    /**
     * KB，MB转换时的单位
     */
    private static final int UNIT = 1024;

    /**
     * instanceViewTable
     */
    public JBTable instanceViewTable;

    /**
     * agentHeapSplitter
     */
    public JBSplitter agentHeapSplitter = new JBSplitter(false, PROPORTION_SEGMENT_HEAP);

    /**
     * instanceAndDetailSplitter
     */
    public JBSplitter instanceAndDetailSplitter = new JBSplitter(false, PROPORTION_SEGMENT_HEAP);
    private final JBLabel totalLabel = new JBLabel();
    private final JBLabel javaLabel = new JBLabel();
    private final ChartLegendColorRect javaColor = new ChartLegendColorRect();
    private final JBLabel nativeLabel = new JBLabel();
    private final ChartLegendColorRect nativeColor = new ChartLegendColorRect();
    private final JBLabel graphicsLabel = new JBLabel();
    private final ChartLegendColorRect graphicsColor = new ChartLegendColorRect();
    private final JBLabel stackLabel = new JBLabel();
    private final ChartLegendColorRect stackColor = new ChartLegendColorRect();
    private final JBLabel codeLabel = new JBLabel();
    private final ChartLegendColorRect codeColor = new ChartLegendColorRect();
    private final JBLabel othersLabel = new JBLabel();
    private final ChartLegendColorRect othersColor = new ChartLegendColorRect();
    private MemoryChartObserver chartObserver;
    private JBPanel heapViewPanel;
    private MemoryTreeTablePanel memoryTreeTablePanel;
    private JBLabel foldBtn;
    private JBLabel heapDumpBtn;
    private JBLabel detailCfgBtn;
    private JButton nativeBtn;
    private ProfilerMonitorItem item;

    /**
     * Constructor
     */
    public MemoryItemView() {
    }

    @Override
    public void init(ProfilerChartsView bottomPanel, ItemsView parent, ProfilerMonitorItem item) {
        this.setName(UtConstant.UT_MEMORY_ITEM_VIEW);
        this.bottomPanel = bottomPanel;
        this.parent = parent;
        this.item = item;
        this.setLayout(new BorderLayout());
        initLegendsComp();
        addChart();
        MemoryTitleView titleView = new MemoryTitleView();
        this.add(titleView, BorderLayout.NORTH);
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                int height = bottomPanel.getHeight() / 5 * 3;
                if (heapViewPanel != null) {
                    chart.setPreferredSize(new Dimension(bottomPanel.getWidth(), height));
                    repaint();
                }
            }
        });
    }

    private void initLegendsComp() {
        totalLabel.setOpaque(false);
        totalLabel.setOpaque(false);
        javaLabel.setOpaque(false);
        nativeLabel.setOpaque(false);
        graphicsLabel.setOpaque(false);
        stackLabel.setOpaque(false);
        codeLabel.setOpaque(false);
        othersLabel.setOpaque(false);
        addDivideMouseListener(agentHeapSplitter);
        addDivideMouseListener(instanceAndDetailSplitter);
    }

    /**
     * Splitter Divide add MouseListener
     *
     * @param splitter jbSplitter
     */
    private void addDivideMouseListener(JBSplitter splitter) {
        splitter.getDivider().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                bottomPanel.getTaskScenePanelChart().getSplitPane().setEnabled(true);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                bottomPanel.getTaskScenePanelChart().getSplitPane().setEnabled(false);
            }
        });
    }

    /**
     * Add chart panel
     */
    private void addChart() {
        chart = generateChart();
        // Register the chart observer to the ProfilerChartsView and listen to the refresh events of the main interface
        chartObserver = new MemoryChartObserver(chart, bottomPanel.getSessionId(), true);
        this.bottomPanel.getPublisher().attach(chartObserver);
        this.add(chart, BorderLayout.CENTER);
    }

    private ProfilerChart generateChart() {
        ProfilerChart memoryChart = new FilledLineChart(this.bottomPanel, item.getName(), true) {
            @Override
            protected void initLegends() {
                MemoryItemView.this.initChartLegends(legends);
            }

            @Override
            protected String getYaxisLabelStr(int value) {
                // Here we get KB, we need to convert it to MB
                return value == maxUnitY ? divide(value, UNIT) + " " + axisLabelY : divide(value, UNIT) + "";
            }

            @Override
            protected void buildLegends(List<ChartDataModel> lastModels) {
                MemoryItemView.this.buildChartLegends(lastModels);
            }

            @Override
            protected void buildTooltip(int showKey, int actualKey, boolean newChart) {
                String totalValue = calcTotal(actualKey, dataMap);
                List<TooltipItem> tooltipItems = buildTooltipItems(actualKey, dataMap);
                tooltip.showTip(this, showKey + "", totalValue, tooltipItems, newChart, axisLabelY);
            }

            @Override
            protected void leftMouseClickEvent(MouseEvent event) {
                MemoryItemView.this.chartLeftMouseClick();
            }

            @Override
            protected void rightMouseClickEvent(MouseEvent event) {
                MemoryItemView.this.chartRightMouseClick();
            }

            @Override
            protected void mouseDraggedEvent(MouseEvent event) {
                MemoryItemView.this.chartMouseDragged();
            }

            @Override
            protected void mouseReleaseEvent(MouseEvent event) {
                MemoryItemView.this.chartMouseRelease();
            }
        };
        memoryChart.setMaxDisplayX(this.bottomPanel.getPublisher().getStandard().getMaxDisplayMillis());
        memoryChart.setMinMarkIntervalX(this.bottomPanel.getPublisher().getStandard().getMinMarkInterval());
        memoryChart.setSectionNumY(NUM_2);
        memoryChart.setAxisLabelY("MB");
        memoryChart.setFold(true);
        memoryChart.setEnableSelect(false);
        return memoryChart;
    }

    /**
     * Init legend components of chart
     *
     * @param legends legends
     */
    private void initChartLegends(JBPanel legends) {
        checkAndAdd(legends, totalLabel);
        checkAndAdd(legends, javaColor);
        checkAndAdd(legends, javaLabel);
        checkAndAdd(legends, nativeColor);
        checkAndAdd(legends, nativeLabel);
        checkAndAdd(legends, graphicsColor);
        checkAndAdd(legends, graphicsLabel);
        checkAndAdd(legends, stackColor);
        checkAndAdd(legends, stackLabel);
        checkAndAdd(legends, codeColor);
        checkAndAdd(legends, codeLabel);
        checkAndAdd(legends, othersColor);
        checkAndAdd(legends, othersLabel);
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
        component.setVisible(false);
    }

    private void buildChartLegends(List<ChartDataModel> lastModels) {
        new SwingWorker<>() {
            @Override
            protected Object doInBackground() {
                // Total label
                BigDecimal totalMB = divide(new BigDecimal(chart.getListSum(lastModels, 0)), new BigDecimal(NUM_1024));
                String totalText;
                if (fold) {
                    totalText = totalMB + chart.getAxisLabelY();
                } else {
                    totalText = String.format(Locale.ENGLISH, "Total:%s%s", totalMB, chart.getAxisLabelY());
                }
                totalLabel.setText(totalText);
                totalLabel.setVisible(true);
                // Initialize a map of full memory legends
                Map<MonitorItemDetail, List<JComponent>> allItemLegendMap = initItemLegends();
                // Processing data into legend and remove from allItemLegendMap
                lastModels.forEach(model -> parseModelToLegend(model, allItemLegendMap));
                // There are only unselected monitoring items in the map, which need to be hidden
                allItemLegendMap
                    .forEach((item, components) -> components.forEach(component -> component.setVisible(false)));
                return new Object();
            }
        }.execute();
    }

    /**
     * Initialize a map of full memory legends
     *
     * @return Map<Monitor item, component of legend>
     */
    private Map<MonitorItemDetail, List<JComponent>> initItemLegends() {
        Map<MonitorItemDetail, List<JComponent>> map = new HashMap<>();
        map.put(MEM_JAVA, Arrays.asList(javaColor, javaLabel));
        map.put(MEM_NATIVE, Arrays.asList(nativeColor, nativeLabel));
        map.put(MEM_GRAPHICS, Arrays.asList(graphicsColor, graphicsLabel));
        map.put(MEM_STACK, Arrays.asList(stackColor, stackLabel));
        map.put(MEM_CODE, Arrays.asList(codeColor, codeLabel));
        map.put(MEM_OTHERS, Arrays.asList(othersColor, othersLabel));
        return map;
    }

    /**
     * Processing data into legend and remove from allItemLegendMap
     *
     * @param model Data model
     * @param allItemLegendMap Map of memory legends
     */
    private void parseModelToLegend(ChartDataModel model, Map<MonitorItemDetail, List<JComponent>> allItemLegendMap) {
        MonitorItemDetail itemParam = MonitorItemDetail.getItemByName(model.getName());
        switch (itemParam) {
            case MEM_JAVA:
                refreshColorText(javaColor, javaLabel, model);
                // If the model is saved as the current monitoring item, its components will be displayed
                allItemLegendMap.get(MEM_JAVA).forEach(component -> component.setVisible(true));
                // After the component is set to display, it is removed from the map. After the loop is completed,
                // only unselected monitoring items are left in the map and need to be hidden
                allItemLegendMap.remove(MEM_JAVA);
                break;
            case MEM_NATIVE:
                refreshColorText(nativeColor, nativeLabel, model);
                allItemLegendMap.get(MEM_NATIVE).forEach(component -> component.setVisible(true));
                allItemLegendMap.remove(MEM_NATIVE);
                break;
            case MEM_GRAPHICS:
                refreshColorText(graphicsColor, graphicsLabel, model);
                allItemLegendMap.get(MEM_GRAPHICS).forEach(component -> component.setVisible(true));
                allItemLegendMap.remove(MEM_GRAPHICS);
                break;
            case MEM_STACK:
                refreshColorText(stackColor, stackLabel, model);
                allItemLegendMap.get(MEM_STACK).forEach(component -> component.setVisible(true));
                allItemLegendMap.remove(MEM_STACK);
                break;
            case MEM_CODE:
                refreshColorText(codeColor, codeLabel, model);
                allItemLegendMap.get(MEM_CODE).forEach(component -> component.setVisible(true));
                allItemLegendMap.remove(MEM_CODE);
                break;
            case MEM_OTHERS:
                refreshColorText(othersColor, othersLabel, model);
                allItemLegendMap.get(MEM_OTHERS).forEach(component -> component.setVisible(true));
                allItemLegendMap.remove(MEM_OTHERS);
                break;
            default:
                break;
        }
    }

    /**
     * Update the color and text of the legend
     *
     * @param colorRect Color component
     * @param label text label
     * @param model data
     */
    private void refreshColorText(ChartLegendColorRect colorRect, JBLabel label, ChartDataModel model) {
        String showValue = divide(model.getValue(), NUM_1024).toString();
        String text = String.format(Locale.ENGLISH, "%s:%s%s", model.getName(), showValue, chart.getAxisLabelY());
        colorRect.setColor(model.getColor());
        if (!label.getText().equals(text)) {
            label.setText(text);
        }
    }

    /**
     * Calculate the total value at a time
     *
     * @param time 时间
     * @param dataMap dataMap
     * @return Total值
     */
    private String calcTotal(int time, LinkedHashMap<Integer, List<ChartDataModel>> dataMap) {
        List<ChartDataModel> models = dataMap.get(time);
        if (models == null || models.size() == 0) {
            return "";
        }
        // Here we get KB, we need to convert it to MB
        int value = chart.getListSum(models, 0);
        return divide(value, NUM_1024).toString();
    }

    /**
     * Build tooltip items
     *
     * @param time Current time
     * @param dataMap dataMap
     * @return List
     */
    private List<TooltipItem> buildTooltipItems(int time, LinkedHashMap<Integer, List<ChartDataModel>> dataMap) {
        List<TooltipItem> tooltipItems = new ArrayList<>();
        if (dataMap == null || dataMap.size() == 0 || dataMap.get(time) == null) {
            return tooltipItems;
        }
        for (ChartDataModel model : dataMap.get(time)) {
            BigDecimal showValue = divide(model.getValue(), NUM_1024);
            String text = String.format(Locale.ENGLISH, "%s:%s%s", model.getName(), showValue, chart.getAxisLabelY());
            TooltipItem tooltipItem = new TooltipItem(model.getColor(), text);
            tooltipItems.add(tooltipItem);
        }
        return tooltipItems;
    }

    /**
     * chartLeftMouseClick
     */
    private void chartLeftMouseClick() {
        long sessionId = this.bottomPanel.getSessionId();
        SessionInfo sessionInfo = SessionManager.getInstance().getSessionInfo(sessionId);
        if (!sessionInfo.isOfflineMode()
            && sessionInfo.getDeviceIPPortInfo().getDeviceType() == DeviceType.LEAN_HOS_DEVICE) {
            return;
        }
        if (memoryTreeTablePanel != null) {
            refreshAgentHeapInfo(sessionId);
        } else {
            memoryTreeTablePanel = new MemoryTreeTablePanel(this, sessionId, item.getName());
            parent.itemChartClick(this);
            instanceAndDetailSplitter.setOpaque(true);
            memoryTreeTablePanel.setOpaque(true);
            agentHeapSplitter.setFirstComponent(memoryTreeTablePanel);
            // heapView external total panel
            heapViewPanel = new JBPanel();
            int height = bottomPanel.getHeight() / 5 * 2;
            heapViewPanel.setPreferredSize(new Dimension(bottomPanel.getWidth(), height));
            heapViewPanel.setLayout(new MigLayout("insets 0", "[grow,fill]", "[grow,fill]"));
            heapViewPanel.add(agentHeapSplitter, "span");
        }
        this.add(heapViewPanel, BorderLayout.SOUTH);
    }

    /**
     * refresh agent heap info
     *
     * @param sessionId sessionId
     */
    private void refreshAgentHeapInfo(long sessionId) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (memoryTreeTablePanel != null) {
                    MemoryAgentHeapInfoPanel memoryAgentHeapInfoPanel =
                        memoryTreeTablePanel.getMemoryAgentHeapInfoPanel();
                    if (memoryAgentHeapInfoPanel != null && memoryAgentHeapInfoPanel.getTreeTable() != null) {
                        DefaultMutableTreeNode root = memoryAgentHeapInfoPanel.initData(sessionId, item.getName());
                        ListTreeTableModelOnColumns tableModelOnColumns =
                            new ListTreeTableModelOnColumns(root, memoryAgentHeapInfoPanel.columns);
                        ExpandTreeTable agentTreeTable = memoryAgentHeapInfoPanel.getTreeTable();
                        agentTreeTable.setModel(tableModelOnColumns);
                        JScrollBar scrollBar = agentTreeTable.getVerticalScrollBar();
                        scrollBar.setValue(0);
                        scrollBar.removeMouseMotionListener(memoryAgentHeapInfoPanel.mouseMotionAdapter);
                        createMouseMotionAdapter(memoryAgentHeapInfoPanel, tableModelOnColumns, agentTreeTable);
                        scrollBar.addMouseMotionListener(memoryAgentHeapInfoPanel.mouseMotionAdapter);
                        AgentTreeTableRowSorter sorter =
                            new AgentTreeTableRowSorter(agentTreeTable.getTable().getModel());
                        sorter.setListener((columnIndex, sortOrder) -> {
                            if (columnIndex <= 0 || columnIndex > memoryAgentHeapInfoPanel.columns.length) {
                                return;
                            }
                            if (sortOrder == SortOrder.ASCENDING) {
                                AgentTreeTableRowSorter.sortDescTree(memoryAgentHeapInfoPanel,
                                    memoryAgentHeapInfoPanel.columns[columnIndex].getName(), tableModelOnColumns);
                            } else {
                                AgentTreeTableRowSorter.sortTree(memoryAgentHeapInfoPanel,
                                    memoryAgentHeapInfoPanel.columns[columnIndex].getName(), tableModelOnColumns);
                            }
                            tableModelOnColumns.reload();
                        });
                        agentTreeTable.getTable().setRowSorter(sorter);
                        agentHeapSplitter.setSecondComponent(new JBSplitter(false, 1));
                    }
                }
            }
        });
    }

    private void createMouseMotionAdapter(MemoryAgentHeapInfoPanel memoryAgentHeapInfoPanel,
        ListTreeTableModelOnColumns tableModelOnColumns, ExpandTreeTable agentTreeTable) {
        memoryAgentHeapInfoPanel.mouseMotionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                JScrollBar jScrollBar = null;
                Object sourceObject = mouseEvent.getSource();
                if (sourceObject instanceof JScrollBar) {
                    jScrollBar = (JScrollBar) sourceObject;
                    BoundedRangeModel model = jScrollBar.getModel();
                    if (model.getExtent() + model.getValue() == model.getMaximum()) {
                        ListTreeTableModelOnColumns nodeModel = null;
                        Object modelObject = agentTreeTable.getTree().getModel();
                        if (modelObject instanceof ListTreeTableModelOnColumns) {
                            nodeModel = (ListTreeTableModelOnColumns) modelObject;
                            DefaultMutableTreeNode rootNode = null;
                            Object rootObject = nodeModel.getRoot();
                            if (rootObject instanceof DefaultMutableTreeNode) {
                                rootNode = (DefaultMutableTreeNode) rootObject;
                                int index = memoryAgentHeapInfoPanel.allAgentDatas
                                    .indexOf(memoryAgentHeapInfoPanel.lastDataNode);
                                List<AgentHeapBean> list = memoryAgentHeapInfoPanel
                                    .listCopy(memoryAgentHeapInfoPanel.allAgentDatas, index, index + 20);
                                for (AgentHeapBean agentDataNode : list) {
                                    DefaultMutableTreeNode defaultMutableTreeNode =
                                        new DefaultMutableTreeNode(agentDataNode);
                                    tableModelOnColumns
                                        .insertNodeInto(defaultMutableTreeNode, rootNode, rootNode.getChildCount());
                                    memoryAgentHeapInfoPanel.lastDataNode = agentDataNode;
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    /**
     * Secondary treeTable interface
     *
     * @param sessionId sessionId
     * @param clazzId clazzId
     * @param className className
     * @param chartName chartName
     * @return JBPanel
     */
    public JBPanel setSecondLevelTreeTable(long sessionId, int clazzId, String className, String chartName) {
        agentHeapSplitter.setProportion(LayoutConstants.PROPORTION_SEGMENT);
        // instance Table panel
        JBPanel instanceView = new JBPanel();
        instanceView.setLayout(new BorderLayout());
        instanceView.setOpaque(true);
        instanceView.setBackground(JBColor.background().darker());

        setTitleStyle(className, instanceView, 1);

        instanceView.setPreferredSize(new Dimension(INSTANCE_TITLE_WIDTH, NUM_418));
        // Table header
        Vector<String> columnNames = new Vector<>();
        columnNames.add("instance");
        columnNames.add("allocTime");
        columnNames.add("DealloTime");
        columnNames.add("InstanceId");
        // Display JBTable
        DefaultTableModel defaultTableModel = new DefaultTableModel() {
            /**
             * isCellEditable
             *
             * @param row row
             * @param column column
             * @return boolean
             */
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        defaultTableModel.setColumnIdentifiers(columnNames);
        instanceViewTable = new JBTable(defaultTableModel);
        setExtracted(defaultTableModel, instanceViewTable);
        // Initialize table data
        initData(defaultTableModel, clazzId, className, sessionId, chartName);
        JBScrollPane jScrollPane = new JBScrollPane(instanceViewTable);
        jScrollPane.setPreferredSize(new Dimension(INSTANCE_TITLE_WIDTH, INSTANCE_PANE_HEIGHT));
        instanceView.add(jScrollPane, BorderLayout.CENTER);
        return instanceView;
    }

    /**
     * 初始化table 表格
     *
     * @param model model
     * @param cId cId
     * @param className className
     * @param sessionId long
     * @param chartName chartName
     */
    public void initData(DefaultTableModel model, Integer cId, String className, long sessionId, String chartName) {
        MemoryInstanceManager memoryInstanceManager = new MemoryInstanceManager();
        ChartStandard standard = ProfilerChartsView.sessionMap.get(sessionId).getPublisher().getStandard();
        long firstTime = standard.getFirstTimestamp();
        ChartDataRange selectedRange = standard.getSelectedRange(chartName);
        long endTime = selectedRange.getEndTime() + firstTime;
        List<MemoryInstanceInfo> memoryInstanceInfos = memoryInstanceManager.getMemoryInstanceInfos(cId, 0L, endTime);
        memoryInstanceInfos.forEach(memoryInstanceInfo -> {
            long deallocTime = memoryInstanceInfo.getDeallocTime();
            long alloc = TimeUnit.MILLISECONDS.toMicros(memoryInstanceInfo.getAllocTime() - firstTime);
            String allocTime = getSemiSimplifiedClockString(alloc);
            String deAllocTime = " - ";
            if (deallocTime != 0) {
                long deAllocations = TimeUnit.MILLISECONDS.toMicros(memoryInstanceInfo.getDeallocTime() - firstTime);
                deAllocTime = getSemiSimplifiedClockString(deAllocations);
            }
            Integer instanceId = memoryInstanceInfo.getInstanceId();
            Vector<Object> rowData = new Vector<>();
            rowData.add(className);
            rowData.add(allocTime);
            rowData.add(deAllocTime);
            rowData.add(instanceId);
            model.addRow(rowData);
        });
    }

    /**
     * Return a formatted time String in the form of "hh:mm:ss.sss"".
     * Hide hours value if both hours and minutes value are zero.
     * Default format for Tooltips.
     *
     * @param micro micro
     * @return String
     */
    public String getSemiSimplifiedClockString(long micro) {
        long micros = Math.max(0, micro);
        String result = getFullClockString(micros);
        return result;
    }

    /**
     * Return a formatted time String in the form of "hh:mm:ss.sss".
     * Default format for Range description.
     *
     * @param micro micro
     * @return String
     */
    public String getFullClockString(long micro) {
        long micros = Math.max(0, micro);
        long milli = TimeUnit.MICROSECONDS.toMillis(micros) % TimeUnit.SECONDS.toMillis(1);
        long sec = TimeUnit.MICROSECONDS.toSeconds(micros) % TimeUnit.MINUTES.toSeconds(1);
        long min = TimeUnit.MICROSECONDS.toMinutes(micros) % TimeUnit.HOURS.toMinutes(1);
        long hour = TimeUnit.MICROSECONDS.toHours(micros);
        return String.format(Locale.ENGLISH, "%02d:%02d:%02d.%03d", hour, min, sec, milli);
    }

    private void setExtracted(DefaultTableModel model, JBTable table) {
        table.getTableHeader().setFont(new Font("PingFang SC", Font.PLAIN, LayoutConstants.FONT_SIZE));
        table.setFont(new Font("PingFang SC", Font.PLAIN, LayoutConstants.TWELVE));
        table.setOpaque(true);
        table.setShowHorizontalLines(false);
        table.getTableHeader().getColumnModel().getColumn(3).setMaxWidth(0);
        table.getTableHeader().getColumnModel().getColumn(3).setMinWidth(0);
        table.getTableHeader().getColumnModel().getColumn(3).setPreferredWidth(0);
        RowSorter<TableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
    }

    /**
     * Three-level treeTable interface
     *
     * @param sessionId sessionId
     * @param instanceId instanceId
     * @param name name
     * @return JBPanel
     */
    public JBPanel setThirdLevelTreeTable(long sessionId, int instanceId, String name) {
        agentHeapSplitter.setProportion(PROPORTION_SEGMENT_VIEW);
        instanceAndDetailSplitter.setProportion(PROPORTION_SEGMENT_HEAP);
        // Reserved Tree Table panel
        JBPanel fieldsView = new JBPanel(new BorderLayout());
        fieldsView.setOpaque(true);
        setTitleStyle(name, fieldsView, 0);
        fieldsView.setPreferredSize(new Dimension(NUM_384, NUM_418));
        Vector<String> columnNames = new Vector<>();
        columnNames.add("Allocation Call Stack");
        DefaultTableModel model = new DefaultTableModel() {
            /**
             * Returns true regardless of parameter values.
             *
             * @param row the row whose value is to be queried
             * @param column the column whose value is to be queried
             * @return true
             */
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.setColumnIdentifiers(columnNames);
        JBTable callStackTable = new JBTable(model);
        DefaultTableCellRenderer defaultCellRender = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
                JLabel jLabel = null;
                Object jLabelObject =
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (jLabelObject instanceof JLabel) {
                    jLabel = (JLabel) jLabelObject;
                    jLabel.setIcon(AllIcons.Nodes.Method);
                }
                return jLabel;
            }
        };
        defaultCellRender.getTableCellRendererComponent(callStackTable, null, false, false, 1, 0);
        callStackTable.getColumnModel().getColumn(0).setCellRenderer(defaultCellRender);
        MemoryInstanceDetailsManager detailsManager = new MemoryInstanceDetailsManager();
        ArrayList<MemoryInstanceDetailsInfo> detailsInfos = detailsManager.getMemoryInstanceDetailsInfos(instanceId);
        detailsInfos.forEach(detailsInfo -> {
            Vector<String> rowData = new Vector<>();
            rowData.add(
                detailsInfo.getMethodName() + ":" + detailsInfo.getLineNumber() + "," + detailsInfo.getClassName()
                    + "; (" + detailsInfo.getFieldName() + ")");
            model.addRow(rowData);
        });
        callStackTable.setShowHorizontalLines(false);
        JBScrollPane jScrollPane = new JBScrollPane(callStackTable);
        jScrollPane.setPreferredSize(new Dimension(INSTANCE_TITLE_WIDTH, INSTANCE_SCROLL_HEIGHT));
        fieldsView.add(jScrollPane, BorderLayout.CENTER);
        return fieldsView;
    }

    private void setTitleStyle(String name, JBPanel viewStyle, int status) {
        JBLabel jbLabel = new JBLabel();
        if (status == 0) {
            jbLabel.setText("  Instance Details-" + name);
        } else {
            jbLabel.setText("  Instance view");
        }
        Font font = new Font("PingFang SC", Font.PLAIN, LayoutConstants.FONT_SIZE);
        jbLabel.setFont(font);
        JBLabel closeLabel = new JBLabel(AllIcons.Actions.Close);
        closeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                if (status == 0) {
                    instanceAndDetailSplitter.setSecondComponent(new JBSplitter(false, 1));
                } else {
                    agentHeapSplitter.setSecondComponent(new JBSplitter(false, 1));
                }
            }
        });

        JBPanel titlePanel = new JBPanel(new BorderLayout());
        titlePanel.setOpaque(true);
        titlePanel.setBackground(JBColor.background().darker());
        titlePanel.setPreferredSize(new Dimension(NUM_384, INSTANCE_TITLE_HEIGHT));
        titlePanel.add(jbLabel, BorderLayout.WEST);
        titlePanel.add(closeLabel, BorderLayout.EAST);
        viewStyle.add(titlePanel, BorderLayout.NORTH);
    }

    private void chartRightMouseClick() {
        if (heapViewPanel != null) {
            this.remove(heapViewPanel);
            heapViewPanel = null;
            memoryTreeTablePanel = null;
        }
    }

    private void chartMouseDragged() {
    }

    private void chartMouseRelease() {
        long sessionId = this.bottomPanel.getSessionId();
        SessionInfo sessionInfo = SessionManager.getInstance().getSessionInfo(sessionId);
        if (!sessionInfo.isOfflineMode()
            && sessionInfo.getDeviceIPPortInfo().getDeviceType() == DeviceType.LEAN_HOS_DEVICE) {
            return;
        }
        refreshAgentHeapInfo(sessionId);
    }

    private class MemoryTitleView extends JBPanel {
        /**
         * Save the components should be hidden when item fold
         */
        private JBPanel hiddenComp;

        MemoryTitleView() {
            this.setLayout(new FlowLayout(FlowLayout.LEFT));
            initFixedComps();
            initHiddenComponents();
            this.setBackground(JBColor.background().brighter());
        }

        private void initFixedComps() {
            foldBtn = new JBLabel();
            foldBtn.setName(UtConstant.UT_MEMORY_ITEM_VIEW_FOLD);
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
            hiddenComp.setVisible(!fold);
            if (fold) {
                chart.setFold(true);
                chart.setEnableSelect(false);
                chart.getTooltip().hideTip();
                foldBtn.setIcon(AllIcons.General.ArrowRight);
                if (heapViewPanel != null) {
                    MemoryItemView.this.remove(heapViewPanel);
                    heapViewPanel = null;
                }
                if (memoryTreeTablePanel != null) {
                    MemoryItemView.this.remove(memoryTreeTablePanel);
                    memoryTreeTablePanel = null;
                }
            } else {
                // Uncheck the box after re-expanding
                bottomPanel.getPublisher().getStandard().clearSelectedRange(item.getName());
                chart.setFold(false);
                chart.setEnableSelect(true);
                foldBtn.setIcon(AllIcons.General.ArrowDown);
            }
            parent.itemFoldOrExpend(fold, MemoryItemView.this);
            // Initialize the maximum value of Y axis here,  because it may change after fold/expand
            chart.initMaxUnitY();
            chartObserver.setChartFold(fold);
        }

        private void initHiddenComponents() {
            hiddenComp = new JBPanel(new FlowLayout(FlowLayout.LEFT));
            hiddenComp.setBackground(JBColor.background().brighter());
            this.add(hiddenComp);
            // Add components
            hiddenComp.add(new DottedLine());
            initDetailCfgBtn();
            initTrackingLabel();
            initCollectBox();
            hiddenComp.add(new DottedLine());
            // The initial state is folded and hiddenComp needs to be hidden
            hiddenComp.setVisible(false);
        }

        private void initDetailCfgBtn() {
            detailCfgBtn = new JBLabel(IconLoader.getIcon("/images/icon_dataselection_normal.png", getClass()));
            detailCfgBtn.setName(UtConstant.UT_MEMORY_ITEM_VIEW_DETAIL);
            hiddenComp.add(detailCfgBtn);
            MemoryItemPopupMenu memoryPopupMenu = new MemoryItemPopupMenu(bottomPanel.getSessionId(), chartObserver);
            detailCfgBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    memoryPopupMenu.showMemoryItems(detailCfgBtn, mouseEvent);
                }
            });
        }

        private void initTrackingLabel() {
            hiddenComp.add(new JBLabel("Allocation tracking"));
        }

        private void initCollectBox() {
            ComboBox<String> collectBox = new ComboBox<>();
            collectBox.setName(UtConstant.UT_MEMORY_ITEM_VIEW_COLLECT_BOX);
            collectBox.addItem("Full");
            hiddenComp.add(collectBox);
        }
    }

    public JBPanel getHeapViewPanel() {
        return heapViewPanel;
    }
}
