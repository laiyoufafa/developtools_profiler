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

package ohos.devtools.views.layout.chartview.memory.nativehook;

import ohos.devtools.views.applicationtrace.listener.ITreeTableSortChangeListener;

import javax.swing.DefaultRowSorter;
import javax.swing.JTree;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * class of tree table row sorter
 *
 * @since: 2021/10/25
 */
public class NativeHookTreeTableRowSorter<M extends TableModel> extends DefaultRowSorter<M, Integer> {
    private int sorterColumn = -1;
    private M treeTablemodel;
    private TreeTableRowSorterModelWrapper modelWrapper;
    private ITreeTableSortChangeListener listener;

    /**
     * constructor with model
     *
     * @param mode model
     */
    public NativeHookTreeTableRowSorter(M mode) {
        treeTablemodel = mode;
        modelWrapper = new TreeTableRowSorterModelWrapper();
        setModelWrapper(modelWrapper);
    }

    /**
     * sort desc tree
     *
     * @param root      root node
     * @param condition sort condition
     * @param jtree     tree
     */
    public static void sortDescTree(DefaultMutableTreeNode root, Comparator<TreeNode> condition, JTree jtree) {
        Consumer<DefaultMutableTreeNode> sort = parent -> {
            Enumeration<TreeNode> children = parent.children();
            List<DefaultMutableTreeNode> childs = new ArrayList<>();
            while (children.hasMoreElements()) {
                TreeNode node = children.nextElement();
                if (node instanceof DefaultMutableTreeNode) {
                    childs.add((DefaultMutableTreeNode) node);
                }
            }
            parent.removeAllChildren();
            childs.stream().sorted(condition).collect(Collectors.toList()).forEach(parent::add);
        };
        Enumeration enumeration = root.depthFirstEnumeration();
        while (enumeration.hasMoreElements()) {
            Object nodeObj = enumeration.nextElement();
            if (nodeObj instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeObj;
                TreePath path = new TreePath(node.getPath());
                boolean leafFlag = !node.isLeaf() && node.getChildCount() > 1;
                boolean rootFlag = (node.isRoot() || jtree.isExpanded(jtree.getRowForPath(path)) || !root.isRoot());
                if (leafFlag && rootFlag) {
                    sort.accept(node);
                }
            }
        }
    }

    /**
     * sort tree
     *
     * @param root      root node
     * @param condition sort condition
     * @param jtree     tree
     */
    public static void sortTree(DefaultMutableTreeNode root, Comparator<TreeNode> condition, JTree jtree) {
        Consumer<DefaultMutableTreeNode> sort = parent -> {
            Enumeration<TreeNode> children = parent.children();
            List<DefaultMutableTreeNode> childs = new ArrayList<>();
            while (children.hasMoreElements()) {
                TreeNode node = children.nextElement();
                if (node instanceof DefaultMutableTreeNode) {
                    childs.add((DefaultMutableTreeNode) node);
                }
            }
            parent.removeAllChildren();
            childs.stream().sorted(condition.reversed()).collect(Collectors.toList())
                .forEach(parent::add);
        };
        Enumeration enumeration = root.depthFirstEnumeration();
        while (enumeration.hasMoreElements()) {
            Object nodeObj = enumeration.nextElement();
            if (nodeObj instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeObj;
                TreePath path = new TreePath(node.getPath());
                boolean leafFlag = !node.isLeaf() && node.getChildCount() > 1;
                boolean rootFlag = (node.isRoot() || jtree.isExpanded(jtree.getRowForPath(path)) || !root.isRoot());
                if (leafFlag && rootFlag) {
                    sort.accept(node);
                }
            }
        }
    }

    /**
     * set sort change listener
     *
     * @param listener listener
     */
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
            } else if (sortKeys.stream()
                .anyMatch(item -> item.getColumn() == column && item.getSortOrder() == SortOrder.DESCENDING)) {
                sortOrder = SortOrder.ASCENDING;
            } else {
                sortOrder = SortOrder.DESCENDING;
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

    @Override
    public void sort() {
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
                return Optional.of(null);
            }
            return treeTablemodel.getValueAt(row, column);
        }

        @Override
        public Integer getIdentifier(int row) {
            return row;
        }
    }
}
