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

package ohos.devtools.views.layout.chartview.memory.javaagent;

import com.intellij.ui.HighlightableCellRenderer;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.ui.ColumnInfo;
import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.services.memory.agentbean.AgentHeapBean;
import ohos.devtools.services.memory.agentdao.ClassInfoManager;
import ohos.devtools.services.memory.agentdao.MemoryHeapManager;
import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartStandard;
import ohos.devtools.views.common.treetable.ExpandTreeTable;
import ohos.devtools.views.common.treetable.TreeTableColumn;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.memory.MemoryItemView;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.BoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.SortOrder;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ohos.devtools.views.common.Constant.MEMORY_AGENT_INIT_COUNT;

/**
 * MemoryAgentHeapInfoPanel
 *
 * @since : 2021/10/25
 */
public class MemoryAgentHeapInfoPanel extends JBPanel {
    private static final Logger LOGGER = LogManager.getLogger(MemoryAgentHeapInfoPanel.class);

    /**
     * columns
     */
    public final ColumnInfo[] columns =
        new ColumnInfo[] {new TreeColumnInfo("Class Name"), new TreeTableColumn<>("Allocations", AgentHeapBean.class) {
            @Override
            public String getColumnValue(AgentHeapBean nodeData) {
                return String.valueOf(nodeData.getAgentAllocationsCount());
            }
        }, new TreeTableColumn<>("Deallocations", AgentHeapBean.class) {
            @Override
            public String getColumnValue(AgentHeapBean nodeData) {
                return String.valueOf(nodeData.getAgentDeAllocationsCount());
            }
        }, new TreeTableColumn<>("Total Count", AgentHeapBean.class) {
            @Override
            public String getColumnValue(AgentHeapBean nodeData) {
                return String.valueOf(nodeData.getAgentTotalInstanceCount());
            }
        }, new TreeTableColumn<>("Shallow Size", AgentHeapBean.class) {
            @Override
            public String getColumnValue(AgentHeapBean nodeData) {
                return String.valueOf(nodeData.getAgentTotalshallowSize());
            }
        }};

    /**
     * allAgentDatas
     */
    private List<AgentHeapBean> allAgentDatas;

    /**
     * lastDataNode
     */
    private AgentHeapBean lastDataNode;

    /**
     * mouseMotionAdapter
     */
    private MouseMotionAdapter mouseMotionAdapter;

    private ExpandTreeTable treeTable;

    /**
     * mouseListener
     */
    private MouseAdapter mouseListener;

    /**
     * MemoryAgentHeapInfoPanel
     *
     * @param memoryItemView memoryItemView
     * @param sessionId long
     * @param chartName String
     */
    public MemoryAgentHeapInfoPanel(MemoryItemView memoryItemView, long sessionId, String chartName) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("MemoryAgentHeapInfoPanel");
        }
        setLayout(new BorderLayout());
        SwingWorker<ExpandTreeTable, Object> task = new SwingWorker<>() {
            /**
             * doInBackground
             *
             * @return JTreeTable
             * @throws Exception Exception
             */
            @Override
            protected ExpandTreeTable doInBackground() {
                return createTable(memoryItemView, sessionId, chartName);
            }

            /**
             * done
             */
            @Override
            protected void done() {
                if (Objects.nonNull(treeTable)) {
                    add(treeTable);
                }
            }
        };
        task.execute();
    }

    /**
     * Copy list according to subscript
     *
     * @param dataList dataList
     * @param startIndex startIndex
     * @param endIndex endIndex
     * @return list
     */
    public List<AgentHeapBean> listCopy(List<AgentHeapBean> dataList, int startIndex, int endIndex) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("listCopy");
        }
        List list = new ArrayList();
        for (int index = startIndex + 1; index < endIndex; index++) {
            if (index < dataList.size()) {
                AgentHeapBean agentHeapBean = dataList.get(index);
                if (agentHeapBean != null) {
                    list.add(agentHeapBean);
                }
            }
        }
        return list;
    }

    /**
     * createTreeTable
     *
     * @param memoryItemView memoryItemView
     * @param sessionId long
     * @param chartName String
     * @return ExpandTreeTable
     */
    public ExpandTreeTable createTable(MemoryItemView memoryItemView, long sessionId, String chartName) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createTable");
        }
        DefaultMutableTreeNode root = initData(sessionId, chartName);
        ListTreeTableModelOnColumns tableModelOnColumns = new ListTreeTableModelOnColumns(root, columns);
        ExpandTreeTable treeTables = new ExpandTreeTable(tableModelOnColumns);
        DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();
        defaultTableCellRenderer.setHorizontalAlignment(JLabel.RIGHT);
        treeTables.getTable().setDefaultRenderer(Object.class, defaultTableCellRenderer);
        JScrollBar tableVerticalScrollBar1 = treeTables.getVerticalScrollBar();
        getMouseMotionAdapter(tableModelOnColumns, treeTables);
        tableVerticalScrollBar1.addMouseMotionListener(mouseMotionAdapter);
        AgentTreeTableRowSorter sorter = new AgentTreeTableRowSorter(treeTables.getTable().getModel());
        sorter.setListener((columnIndex, sortOrder) -> {
            if (columnIndex <= 0 || columnIndex > columns.length) {
                return;
            }
            if (sortOrder == SortOrder.ASCENDING) {
                AgentTreeTableRowSorter
                    .sortDescTree(MemoryAgentHeapInfoPanel.this, columns[columnIndex].getName(), tableModelOnColumns);
            } else {
                AgentTreeTableRowSorter
                    .sortTree(MemoryAgentHeapInfoPanel.this, columns[columnIndex].getName(), tableModelOnColumns);
            }
            tableModelOnColumns.reload();
        });
        treeTables.getTree().setRootVisible(true);
        treeTables.getTree().setExpandsSelectedPaths(true);
        treeTables.getTable().setRowSorter(sorter);
        treeTables.getTree().setCellRenderer(new HighlightableCellRenderer());
        treeTables.getTree().getExpandableItemsHandler().setEnabled(true);
        mouseListener = getTreeTableMouseListener(memoryItemView, sessionId, chartName);
        treeTables.getTable().addMouseListener(mouseListener);
        treeTables.getTree().addMouseListener(mouseListener);
        treeTable = treeTables;
        return treeTables;
    }

    private MouseAdapter getTreeTableMouseListener(MemoryItemView memoryItemView, long sessionId, String chartName) {
        return new MouseAdapter() {
            /**
             * mouseClicked
             *
             * @param mouseEvent mouseEvent
             */
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 1) {
                    int selectedRow = treeTable.getTable().getSelectedRow();
                    TreePath treePath = treeTable.getTree().getPathForRow(selectedRow);
                    if (treePath == null) {
                        return;
                    }
                    Object rowNode = treePath.getLastPathComponent();
                    if (rowNode instanceof DefaultMutableTreeNode) {
                        DefaultMutableTreeNode rowData = (DefaultMutableTreeNode) rowNode;
                        Object dataNode = rowData.getUserObject();
                        if (dataNode instanceof AgentHeapBean) {
                            String className = ((AgentHeapBean) dataNode).getAgentClazzName();
                            int cid = new ClassInfoManager().getClassIdByClassName(className);
                            // Need to be obtained from the first-level interface
                            JBPanel instanceViewPanel =
                                memoryItemView.setSecondLevelTreeTable(sessionId, cid, className, chartName);
                            memoryItemView.instanceAndDetailSplitter.setFirstComponent(instanceViewPanel);
                            memoryItemView.instanceAndDetailSplitter.setSecondComponent(new JBSplitter(false, 1));
                            if (memoryItemView.instanceViewTable != null) {
                                memoryItemView.instanceViewTable.addMouseListener(new MouseAdapter() {
                                    /**
                                     * mouseClicked
                                     *
                                     * @param mouseEvent mouseEvent
                                     */
                                    @Override
                                    public void mouseClicked(MouseEvent mouseEvent) {
                                        int selectedRow = memoryItemView.instanceViewTable.getSelectedRow();
                                        if (selectedRow < 0) {
                                            return;
                                        }
                                        Object id = memoryItemView.instanceViewTable.getValueAt(selectedRow, 3);
                                        if (id instanceof Integer) {
                                            Integer instanceId = (Integer) id;
                                            JBPanel callStack =
                                                memoryItemView.setThirdLevelTreeTable(sessionId, instanceId, className);
                                            memoryItemView.instanceAndDetailSplitter.setSecondComponent(callStack);
                                        }
                                    }
                                });
                            }
                            memoryItemView.agentHeapSplitter
                                .setSecondComponent(memoryItemView.instanceAndDetailSplitter);
                        }
                    }
                }
            }
        };
    }

    /**
     * getMouseMotionAdapter
     *
     * @param tableModelOnColumns tableModelOnColumns
     * @param treeTables treeTables
     */
    private void getMouseMotionAdapter(ListTreeTableModelOnColumns tableModelOnColumns, ExpandTreeTable treeTables) {
        mouseMotionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                JScrollBar jScrollBar = null;
                Object sourceObject = mouseEvent.getSource();
                if (sourceObject instanceof JScrollBar) {
                    jScrollBar = (JScrollBar) sourceObject;
                    BoundedRangeModel model = jScrollBar.getModel();
                    if (model.getExtent() + model.getValue() == model.getMaximum()) {
                        ListTreeTableModelOnColumns model1 = null;
                        Object modelObject = treeTables.getTree().getModel();
                        if (modelObject instanceof ListTreeTableModelOnColumns) {
                            model1 = (ListTreeTableModelOnColumns) modelObject;
                            DefaultMutableTreeNode root1 = null;
                            Object rootObject = model1.getRoot();
                            if (rootObject instanceof DefaultMutableTreeNode) {
                                root1 = (DefaultMutableTreeNode) rootObject;
                                int index = allAgentDatas.indexOf(lastDataNode);
                                List<AgentHeapBean> list = listCopy(allAgentDatas, index, index + 20);
                                for (AgentHeapBean agentDataNode : list) {
                                    DefaultMutableTreeNode defaultMutableTreeNode =
                                        new DefaultMutableTreeNode(agentDataNode);
                                    tableModelOnColumns
                                        .insertNodeInto(defaultMutableTreeNode, root1, root1.getChildCount());
                                    lastDataNode = agentDataNode;
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    /**
     * init treeTable Data
     *
     * @param sessionId long
     * @param chartName String
     * @return DefaultMutableTreeNode
     */
    public DefaultMutableTreeNode initData(long sessionId, String chartName) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initData");
        }
        ChartStandard standard = ProfilerChartsView.sessionMap.get(sessionId).getPublisher().getStandard();
        MemoryHeapManager memoryHeapManager = new MemoryHeapManager();
        ChartDataRange selectedRang = standard.getSelectedRange(chartName);
        String db = DataBaseApi.getInstance().checkTableRegister("ClassInfo");
        if (selectedRang == null || StringUtils.isBlank(db)) {
            return new DefaultMutableTreeNode();
        }
        long firstTime = standard.getFirstTimestamp();
        long endTimeNew = firstTime + selectedRang.getEndTime();
        allAgentDatas = memoryHeapManager.getMemoryHeapInfos(sessionId, 0L, endTimeNew);
        AgentHeapBean agentHeapBean = new AgentHeapBean();
        DefaultMutableTreeNode appNode = new DefaultMutableTreeNode();
        int totalAllocations = 0;
        int totalDeallocations = 0;
        int totalTotalCount = 0;
        long totalShallowSize = 0L;
        if (!allAgentDatas.isEmpty()) {
            for (int index = 0; index < MEMORY_AGENT_INIT_COUNT; index++) {
                AgentHeapBean node = allAgentDatas.get(index);
                if (index == MEMORY_AGENT_INIT_COUNT - 1) {
                    lastDataNode = node;
                }
                appNode.add(new DefaultMutableTreeNode(node));
            }
        }
        for (AgentHeapBean meInfo : allAgentDatas) {
            totalAllocations = totalAllocations + meInfo.getAgentAllocationsCount();
            totalDeallocations = totalDeallocations + meInfo.getAgentDeAllocationsCount();
            totalTotalCount = totalTotalCount + meInfo.getAgentTotalInstanceCount();
            totalShallowSize = totalShallowSize + meInfo.getAgentTotalshallowSize();
        }
        agentHeapBean.setAgentClazzName("app heap");
        agentHeapBean.setAgentAllocationsCount(totalAllocations);
        agentHeapBean.setAgentDeAllocationsCount(totalDeallocations);
        agentHeapBean.setAgentTotalInstanceCount(totalTotalCount);
        agentHeapBean.setAgentTotalshallowSize(totalShallowSize);
        appNode.setUserObject(agentHeapBean);
        return appNode;
    }

    public ExpandTreeTable getTreeTable() {
        return treeTable;
    }

    public List<AgentHeapBean> getAllAgentDatas() {
        return allAgentDatas;
    }

    public void setAllAgentDatas(List<AgentHeapBean> allAgentDatas) {
        this.allAgentDatas = allAgentDatas;
    }

    public AgentHeapBean getLastDataNode() {
        return lastDataNode;
    }

    public void setLastDataNode(AgentHeapBean lastDataNode) {
        this.lastDataNode = lastDataNode;
    }

    public MouseMotionAdapter getMouseMotionAdapter() {
        return mouseMotionAdapter;
    }

    public void setMouseMotionAdapter(MouseMotionAdapter mouseMotionAdapter) {
        this.mouseMotionAdapter = mouseMotionAdapter;
    }
}
