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

package ohos.devtools.views.layout.swing;

import ohos.devtools.services.memory.ClassInfoManager;
import ohos.devtools.services.memory.MemoryHeapInfo;
import ohos.devtools.services.memory.MemoryHeapManager;
import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartStandard;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.chart.treetable.DataNode;
import ohos.devtools.views.common.chart.treetable.DataNodeCompares;
import ohos.devtools.views.common.chart.treetable.AgentDataModel;
import ohos.devtools.views.common.chart.treetable.JTreeTable;
import ohos.devtools.views.common.chart.treetable.TreeTableModel;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.Position;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 层级面板
 *
 * @version 1.0
 * @date 2021/02/27 15:31
 **/
public class LevelTablePanel extends JPanel {
    private static final Logger LOGGER = LogManager.getLogger(LevelTablePanel.class);

    private boolean flag = false;
    private JPanel suspensionTable = null;
    private long sessionId;
    private JTreeTable treeTable;

    /**
     * getSuspensionTable
     *
     * @return JPanel
     */
    public JPanel getSuspensionTable() {
        return suspensionTable;
    }

    /**
     * setSuspensionTable
     *
     * @param suspensionTable suspensionTable
     */
    public void setSuspensionTable(JPanel suspensionTable) {
        this.suspensionTable = suspensionTable;
    }

    private SuspensionTablePanel suspensionTablePanel = new SuspensionTablePanel();

    /**
     * LevelTablePanel
     *
     * @param jpanelSupenn jpanelSupenn
     * @param sessionId    long
     */
    public LevelTablePanel(JPanel jpanelSupenn, long sessionId) {
        this.setLayout(new BorderLayout());
        this.setOpaque(true);
        this.setBackground(ColorConstants.TRACE_TABLE_COLOR);
        jpanelSupenn.setBackground(ColorConstants.TRACE_TABLE_COLOR);
        SwingWorker<JTreeTable, Object> task = new SwingWorker<>() {
            /**
             * doInBackground
             *
             * @return JTreeTable
             * @throws Exception Exception
             */
            @Override
            protected JTreeTable doInBackground() {
                JTreeTable table = null;
                try {
                    table = createTable(jpanelSupenn, sessionId);
                    return table;
                } catch (Exception exception) {
                    LOGGER.error("createTable Exception {}", exception.getMessage());
                }
                return table;
            }

            /**
             * done
             */
            @Override
            protected void done() {
                // 此方法将在后台任务完成后在事件调度线程中被回调
                JTreeTable table = null;
                try {
                    // 获取计算结果
                    table = get();
                    if (table == null) {
                        return;
                    }
                    if (treeTable == null && (table != null)) {
                        treeTable = table;
                    }
                    DefaultTableCellRenderer tcr = new DefaultTableCellRenderer();
                    tcr.setHorizontalAlignment(SwingConstants.CENTER);
                    treeTable.setDefaultRenderer(Object.class, tcr);
                    // 初始化层级table
                    RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(treeTable.getModel());
                    treeTable.setRowSorter(sorter);
                    sorter.addRowSorterListener(new RowSorterListener() {
                        @Override
                        public void sorterChanged(RowSorterEvent event) {
                            if (!sorter.getSortKeys().isEmpty()) {
                                List<? extends RowSorter.SortKey> keys = sorter.getSortKeys();
                                AgentDataModel treeTableModel = null;
                                TreeTableModel treeTModel = treeTable.treeTableModel;
                                if (treeTModel instanceof AgentDataModel) {
                                    treeTableModel = (AgentDataModel) treeTModel;
                                }
                                DataNode rootNode = treeTableModel.getDataNode();

                                ArrayList<DataNode> datNode = rootNode.getChildren();
                                RowSorter.SortKey key = keys.get(0);
                                String sortOrder = key.getSortOrder().name();
                                Comparator comparator =
                                    new DataNodeCompares().chooseCompare(key.getColumn(), sortOrder);
                                if (comparator != null) {
                                    Collections.sort(datNode, comparator);
                                }
                                rootNode.setChildren(datNode);
                                AgentDataModel agentDataModel = new AgentDataModel(rootNode);
                                treeTable.treeTableModel = agentDataModel;
                                treeTable.treeTableCellRenderer = treeTable.new TreeTableCellRenderer(agentDataModel);
                                treeTable.treeTableCellRenderer.setRowHeight(treeTable.getRowHeight());
                                treeTable.setDefaultRenderer(TreeTableModel.class, treeTable.treeTableCellRenderer);
                            }
                        }
                    });
                    JScrollPane jScrollPane = new JScrollPane(treeTable);
                    jScrollPane.getViewport().setOpaque(true);
                    jScrollPane.getViewport().setBackground(ColorConstants.TRACE_TABLE_COLOR);
                    jScrollPane.setBorder(BorderFactory.createEmptyBorder());
                    jScrollPane.setBackground(ColorConstants.BLACK_COLOR);
                    treeTable.setBackground(ColorConstants.TRACE_TABLE_COLOR);
                    LevelTablePanel.this.add(jScrollPane, BorderLayout.CENTER);
                    // 这块，初始化模糊查询
                    JPanel jPanel = selectData(treeTable);
                    LevelTablePanel.this.add(jPanel, BorderLayout.NORTH);
                } catch (InterruptedException | ExecutionException exception) {
                    LOGGER.error(" Exception {}", exception.getMessage());
                }
            }
        };
        task.execute();
    }

    /**
     * 创建表格
     *
     * @param jpanelSupenn jpanelSupenn
     * @param sessionId    long
     * @return JTreeTable
     */
    public JTreeTable createTable(JPanel jpanelSupenn, long sessionId) {
        Icon icon1 = new ImageIcon(LevelTablePanel.class.getClassLoader().getResource("images/right.png"));
        Icon icon2 = new ImageIcon(LevelTablePanel.class.getClassLoader().getResource("images/down.png"));
        Icon icon3 = new ImageIcon("");
        UIManager.put("Tree.collapsedIcon", icon1);
        UIManager.put("Tree.textBackground", ColorConstants.TRACE_TABLE_COLOR);
        UIManager.put("Tree.expandedIcon", icon2);
        UIManager.put("Tree.openIcon", icon3);
        UIManager.put("Tree.closedIcon", icon3);
        UIManager.put("Tree.leafIcon", icon3);
        AgentDataModel agentDataModel = new AgentDataModel(initData(sessionId));
        JTreeTable treeTables = new JTreeTable(agentDataModel);
        treeTables.getTableHeader().setBackground(ColorConstants.TRACE_TABLE_COLOR);
        treeTables.treeTableCellRenderer.putClientProperty("JTree.lineStyle", "None");
        treeTables.treeTableCellRenderer.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
                // 获取被选中的相关节点
                TreePath path = treeSelectionEvent.getPath();
            }
        });
        treeTables.addMouseListener(new MouseAdapter() {
            /**
             * mouseClicked
             *
             * @param mouseEvent mouseEvent
             */
            public void mouseClicked(MouseEvent mouseEvent) {
                treeTables.repaint();
                if (mouseEvent.getClickCount() == 1) {
                    // 单击悬浮展示table 页面
                    jpanelSupenn
                        .setPreferredSize(new Dimension(LayoutConstants.THREE_HUNDRED, LayoutConstants.THREE_HUNDRED));
                    // 获取选中行
                    int selectedRow = treeTables.getSelectedRow();
                    Object className = treeTables.getValueAt(selectedRow, 0);
                    if (className instanceof String) {
                        String clazzName = (String) className;
                        int cid = new ClassInfoManager().getClassIdByClassName(clazzName);
                        suspensionTable =
                            suspensionTablePanel.createSuspensionTable(jpanelSupenn, cid, sessionId, clazzName);
                        suspensionTable.revalidate();
                        suspensionTable.setVisible(true);
                    }
                }
            }
        });
        return treeTables;
    }

    /**
     * 初始化treeTable数据
     *
     * @param sessionId long
     * @return DataNode
     */
    public DataNode initData(long sessionId) {
        MemoryHeapManager memoryHeapManager = new MemoryHeapManager();
        ChartStandard standard = ProfilerChartsView.sessionMap.get(sessionId).getObserver().getStandard();
        ChartDataRange selectedRang = standard.getSelectedRange();
        long firstTime = standard.getFirstTimestamp();
        long startTimeNew = firstTime + selectedRang.getStartTime();
        long endTimeNew = firstTime + selectedRang.getEndTime();

        List<MemoryHeapInfo> memoryHeapInfos =
            memoryHeapManager.getMemoryHeapInfos(sessionId, startTimeNew, endTimeNew);
        DataNode dataNode = new DataNode();
        Integer totalAllocations = 0;
        Integer totalDeallocations = 0;
        Integer totalTotalCount = 0;
        Long totalShallowSize = 0L;
        for (MemoryHeapInfo meInfo : memoryHeapInfos) {
            dataNode.addChildren(buildClassNode(meInfo));
            totalAllocations = totalAllocations + meInfo.getAllocations();
            totalDeallocations = totalDeallocations + meInfo.getDeallocations();
            totalTotalCount = totalTotalCount + meInfo.getTotalCount();
            totalShallowSize = totalShallowSize + meInfo.getShallowSize();
        }
        dataNode.setClassName("app heap");
        dataNode.setAllocations(totalAllocations);
        dataNode.setDeallocations(totalDeallocations);
        dataNode.setTotalCount(totalTotalCount);
        dataNode.setShallowSize(totalShallowSize);
        return dataNode;
    }

    /**
     * 初始化treeTable数据
     *
     * @param sessionId long
     * @return DataNode
     */
    public DataNode reData(long sessionId) {
        MemoryHeapManager memoryHeapManager = new MemoryHeapManager();
        ChartStandard standard = ProfilerChartsView.sessionMap.get(sessionId).getObserver().getStandard();
        long firstTime = standard.getFirstTimestamp();
        ChartDataRange selectedRange = standard.getSelectedRange();
        long startTimeNew = selectedRange.getStartTime() + firstTime;
        long endTimeNew = selectedRange.getEndTime() + firstTime;
        List<MemoryHeapInfo> memoryHeapInfos =
            memoryHeapManager.getMemoryHeapInfos(standard.getSessionId(), startTimeNew, endTimeNew);
        DataNode dataNode = new DataNode();
        Integer totalAllocations = 0;
        Integer totalDeallocations = 0;
        Integer totalTotalCount = 0;
        Long totalShallowSize = 0L;
        for (MemoryHeapInfo meInfo : memoryHeapInfos) {
            dataNode.addChildren(buildClassNode(meInfo));
            totalAllocations = totalAllocations + meInfo.getAllocations();
            totalDeallocations = totalDeallocations + meInfo.getDeallocations();
            totalTotalCount = totalTotalCount + meInfo.getTotalCount();
            totalShallowSize = totalShallowSize + meInfo.getShallowSize();
        }
        dataNode.setClassName("app heap");
        dataNode.setAllocations(totalAllocations);
        dataNode.setDeallocations(totalDeallocations);
        dataNode.setTotalCount(totalTotalCount);
        dataNode.setShallowSize(totalShallowSize);
        return dataNode;
    }

    /**
     * buildClassNode
     *
     * @param mhi mhi
     * @return DataNode
     */
    public DataNode buildClassNode(MemoryHeapInfo mhi) {
        if (mhi == null) {
            return new DataNode();
        }
        DataNode dataNode = new DataNode();
        dataNode.setId(mhi.getId());
        dataNode.setcId(mhi.getcId());
        dataNode.setHeapId(mhi.getHeapId());
        dataNode.setSessionId(mhi.getSessionId());
        dataNode.setClassName(mhi.getClassName());
        dataNode.setAllocations(mhi.getAllocations());
        dataNode.setDeallocations(mhi.getDeallocations());
        dataNode.setTotalCount(mhi.getTotalCount());
        dataNode.setShallowSize(mhi.getShallowSize());
        return dataNode;
    }

    /**
     * 选取数据
     *
     * @param treeTable treeTable
     * @return JPanel
     */
    public JPanel selectData(JTreeTable treeTable) {
        JPanel jpanel = new JPanel(null);
        jpanel.setPreferredSize(new Dimension(LayoutConstants.DEVICES_WIDTH, LayoutConstants.DEVICE_Y));
        JTextFieldTable jTextField = new JTextFieldTable("level");
        jTextField.setBounds(LayoutConstants.SEARCH_NUM, 0, LayoutConstants.DEVICE_NAME_X, LayoutConstants.DEVICE_Y);
        jpanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                super.componentResized(event);
                jTextField.setBounds(LayoutConstants.SEARCH_NUM + (jpanel.getWidth() - LayoutConstants.WINDOW_WIDTH), 0,
                    LayoutConstants.DEVICE_NAME_X, LayoutConstants.DEVICE_Y);
            }
        });
        Label lb = new Label("Table");
        lb.setBounds(LayoutConstants.JP_RIGHT_WIDTH, 0, LayoutConstants.DEVICES_WIDTH, LayoutConstants.DEVICE_Y);
        jpanel.setBackground(ColorConstants.TRACE_TABLE_COLOR);
        jpanel.add(jTextField);
        jpanel.add(lb);
        jTextField.getDocument().addDocumentListener(new DocumentListener() {
            /**
             * insertUpdate
             *
             * @param exception exception
             */
            @Override
            public void insertUpdate(DocumentEvent exception) {
                TreePath pathForRow = treeTable.treeTableCellRenderer.getPathForRow(treeTable.getSelectedRow());
                TreePath com1 = treeTable.treeTableCellRenderer
                    .getNextMatch(jTextField.getText(), LayoutConstants.NEGATIVE_ONE, Position.Bias.Forward);
                treeTable.treeTableCellRenderer.setSelectionPath(com1);
            }

            /**
             * removeUpdate
             *
             * @param exception exception
             */
            @Override
            public void removeUpdate(DocumentEvent exception) {
            }

            /**
             * changedUpdate
             *
             * @param exception exception
             */
            @Override
            public void changedUpdate(DocumentEvent exception) {
            }
        });
        jTextField.addActionListener(new ActionListener() {
            /**
             * actionPerformed
             *
             * @param actionEvent actionEvent
             */
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                TreePath pathForRow = treeTable.treeTableCellRenderer.getPathForRow(treeTable.getSelectedRow());
                TreePath com1 = treeTable.treeTableCellRenderer
                    .getNextMatch(jTextField.getText(), treeTable.getSelectedRow(), Position.Bias.Forward);
                treeTable.treeTableCellRenderer.setSelectionPath(com1);
            }
        });
        return jpanel;
    }

    /**
     * setTreeData
     *
     * @param sessionId long
     * @return FileSystemModel
     */
    public AgentDataModel getTreeDataModel(long sessionId) {
        DataNode data = reData(sessionId);
        return new AgentDataModel(data);
    }

    /**
     * getTreeTable
     *
     * @return JTreeTable
     */
    public JTreeTable getTreeTable() {
        return treeTable;
    }
}
