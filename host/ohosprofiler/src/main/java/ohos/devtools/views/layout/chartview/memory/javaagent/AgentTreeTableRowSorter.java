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

import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.services.memory.agentbean.AgentHeapBean;
import ohos.devtools.views.applicationtrace.listener.ITreeTableSortChangeListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.DefaultRowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ohos.devtools.views.common.Constant.MEMORY_AGENT_INIT_COUNT;

/**
 * AgentTreeTableRowSorter
 *
 * @since : 2021/10/25
 */
public class AgentTreeTableRowSorter<M extends TableModel> extends DefaultRowSorter<M, Integer> {
    private static final Logger LOGGER = LogManager.getLogger(AgentTreeTableRowSorter.class);
    private int sorterColumn = -1;
    private M treeTablemodel;
    private TreeTableRowSorterModelWrapper modelWrapper;
    private ITreeTableSortChangeListener listener;

    /**
     * TreeTableRowSorter
     *
     * @param mode mode
     */
    public AgentTreeTableRowSorter(M mode) {
        treeTablemodel = mode;
        modelWrapper = new TreeTableRowSorterModelWrapper();
        setModelWrapper(modelWrapper);
    }

    public void setListener(ITreeTableSortChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void toggleSortOrder(int column) {
        if (column < 0 || column > treeTablemodel.getColumnCount()) {
            return;
        }
        sorterColumn = column;
        List<SortKey> newKeys = new ArrayList<>();
        List<? extends SortKey> sortKeys = getSortKeys();
        SortOrder sortOrder;
        if (sortKeys.size() > 0) {
            if (sortKeys.stream()
                .anyMatch(item -> item.getColumn() == column && item.getSortOrder() == SortOrder.ASCENDING)) {
                sortOrder = SortOrder.DESCENDING;
            } else {
                sortOrder = SortOrder.ASCENDING;
            }
        } else {
            sortOrder = SortOrder.DESCENDING;
        }
        newKeys.add(new SortKey(column, sortOrder));
        setSortKeys(newKeys);
        fireSortOrderChanged();
        if (listener != null) {
            listener.reSort(column, sortOrder);
        }
    }

    /**
     * sort
     */
    @Override
    public void sort() {
    }

    /**
     * sortDescTree
     *
     * @param agentDataNodes agentDataNodes
     * @param name name
     * @param listTreeTableModelOnColumns listTreeTableModelOnColumns
     */
    public static void sortDescTree(MemoryAgentHeapInfoPanel agentDataNodes, String name,
        ListTreeTableModelOnColumns listTreeTableModelOnColumns) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("sortDescTree");
        }
        Comparator<AgentHeapBean> comparator = chooseCompare(name);
        List<AgentHeapBean> agentDatas =
            agentDataNodes.getAllAgentDatas().stream().sorted(comparator).collect(Collectors.toList());
        agentDataNodes.setAllAgentDatas(agentDatas);
        DefaultMutableTreeNode root = null;
        Object columnsRoot = listTreeTableModelOnColumns.getRoot();
        if (columnsRoot instanceof DefaultMutableTreeNode) {
            root = (DefaultMutableTreeNode) columnsRoot;
            root.removeAllChildren();
            for (int index = 0; index < MEMORY_AGENT_INIT_COUNT; index++) {
                if (agentDataNodes.getAllAgentDatas().size() <= index) {
                    return;
                }
                AgentHeapBean node = agentDataNodes.getAllAgentDatas().get(index);
                if (index == MEMORY_AGENT_INIT_COUNT - 1) {
                    agentDataNodes.setLastDataNode(node);
                }
                root.add(new DefaultMutableTreeNode(node));
            }
        }
    }

    /**
     * sortDescTree
     *
     * @param agentDataNodes agentDataNodes
     * @param name name
     * @param listTreeTableModelOnColumns listTreeTableModelOnColumns
     */
    public static void sortTree(MemoryAgentHeapInfoPanel agentDataNodes, String name,
        ListTreeTableModelOnColumns listTreeTableModelOnColumns) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("sortTree");
        }
        Comparator<AgentHeapBean> comparator = chooseCompare(name);
        List<AgentHeapBean> allAgentDatas =
            agentDataNodes.getAllAgentDatas().stream().sorted(comparator.reversed()).collect(Collectors.toList());
        agentDataNodes.setAllAgentDatas(allAgentDatas);
        DefaultMutableTreeNode root = null;
        Object columnsRoot = listTreeTableModelOnColumns.getRoot();
        if (columnsRoot instanceof DefaultMutableTreeNode) {
            root = (DefaultMutableTreeNode) columnsRoot;
            root.removeAllChildren();
            for (int index = 0; index < MEMORY_AGENT_INIT_COUNT; index++) {
                if (agentDataNodes.getAllAgentDatas().size() <= index) {
                    return;
                }
                AgentHeapBean node = agentDataNodes.getAllAgentDatas().get(index);
                if (index == MEMORY_AGENT_INIT_COUNT - 1) {
                    agentDataNodes.setLastDataNode(node);
                }
                root.add(new DefaultMutableTreeNode(node));
            }
        }
    }

    private static Comparator<AgentHeapBean> chooseCompare(String name) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("chooseCompare");
        }
        Comparator<AgentHeapBean> agentDataNodeComparator = null;
        switch (name) {
            case "Allocations":
                agentDataNodeComparator = (previousNode, lastNode) -> Integer
                    .compare(previousNode.getAgentAllocationsCount(), lastNode.getAgentAllocationsCount());
                break;
            case "Deallocations":
                agentDataNodeComparator = (previousNode, lastNode) -> Integer
                    .compare(previousNode.getAgentDeAllocationsCount(), lastNode.getAgentDeAllocationsCount());
                break;
            case "Total Count"
                : agentDataNodeComparator = (previousNode, lastNode) -> Integer
                    .compare(previousNode.getAgentTotalInstanceCount(), lastNode.getAgentTotalInstanceCount());
                break;
            case "Shallow Size"
                : agentDataNodeComparator = (previousNode, lastNode) -> Long
                    .compare(previousNode.getAgentTotalshallowSize(), lastNode.getAgentTotalshallowSize());
                break;
            default:
                agentDataNodeComparator = (previousNode, lastNode) -> previousNode.toString()
                    .compareTo(lastNode.toString());
        }
        return agentDataNodeComparator;
    }

    private class TreeTableRowSorterModelWrapper extends ModelWrapper<M, Integer> {
        @Override
        public M getModel() {
            return treeTablemodel;
        }

        @Override
        public int getColumnCount() {
            if (treeTablemodel == null) {
                return 0;
            }
            return treeTablemodel.getColumnCount();
        }

        @Override
        public int getRowCount() {
            if (treeTablemodel == null) {
                return 0;
            }
            return treeTablemodel.getRowCount();
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (treeTablemodel == null) {
                return null;
            }
            return treeTablemodel.getValueAt(row, column);
        }

        @Override
        public Integer getIdentifier(int row) {
            return row;
        }
    }
}
