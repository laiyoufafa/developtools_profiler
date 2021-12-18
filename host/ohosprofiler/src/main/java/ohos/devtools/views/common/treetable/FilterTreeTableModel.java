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

package ohos.devtools.views.common.treetable;

import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.SortableColumnModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JTree;
import javax.swing.RowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Enumeration;

/**
 * FilterTreeTableModel
 *
 * @since 2021/11/22
 */
public class FilterTreeTableModel extends DefaultTreeModel implements TreeTableModel, SortableColumnModel {
    private static final Logger LOGGER = LogManager.getLogger(FilterTreeTableModel.class);
    DataFilter filter;
    String filterString;
    private ColumnInfo[] myColumns;
    private JTree myTree;

    /**
     * FilterTreeTableModel
     *
     * @param root root
     * @param columns columns
     * @param filter filter
     */
    public FilterTreeTableModel(DefaultMutableTreeNode root, ColumnInfo[] columns, DataFilter filter) {
        super(root);
        this.myColumns = columns;
        this.filter = filter;
    }

    /**
     * fireTreeStructureChanged
     *
     * @param source source
     * @param path path
     * @param childIndices childIndices
     * @param children children
     */
    @Override
    protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
        super.fireTreeStructureChanged(source, path, childIndices, children);
    }

    @Override
    public ColumnInfo[] getColumnInfos() {
        return this.myColumns;
    }

    @Override
    public void setSortable(boolean sortable) {
    }

    @Override
    public boolean isSortable() {
        return false;
    }

    @Override
    public Object getRowValue(int row) {
        TreePath path = this.myTree.getPathForRow(row);
        return path != null ? path.getLastPathComponent() : null;
    }

    @Override
    public RowSorter.SortKey getDefaultSortKey() {
        return null;
    }

    @Override
    public int getColumnCount() {
        return this.myColumns.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return this.myColumns[columnIndex].getName();
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return this.myColumns[columnIndex].getColumnClass();
    }

    @Override
    public Object getValueAt(Object value, int column) {
        if (value instanceof DefaultMutableTreeNode && column != 0) {
            DefaultMutableTreeNode nodeValue = (DefaultMutableTreeNode) value;
            if (nodeValue.isLeaf()) {
                return this.myColumns[column].valueOf(value);
            }
            long parentNodeData = 0L;
            Enumeration<TreeNode> children = nodeValue.children();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode treeNode = null;
                Object nextElementObject = children.nextElement();
                if (nextElementObject instanceof DefaultMutableTreeNode) {
                    treeNode = (DefaultMutableTreeNode) nextElementObject;
                    if (filter.passTreeNode(treeNode, filterString)) {
                        Object valueOf = this.myColumns[column].valueOf(treeNode);
                        try {
                            parentNodeData = Long.parseLong(valueOf.toString()) + parentNodeData;
                        } catch (NumberFormatException numberFormatException) {
                            return String.valueOf(valueOf);
                        }
                    }
                }
            }
            return String.valueOf(parentNodeData);
        } else {
            return this.myColumns[column].valueOf(value);
        }
    }

    @Override
    public boolean isCellEditable(Object obj, int index) {
        return false;
    }

    @Override
    public void setValueAt(Object obj, Object objOne, int columnIndex) {
        this.myColumns[columnIndex].setValue(objOne, obj);
    }

    @Override
    public void setTree(JTree jTree) {
        this.myTree = jTree;
    }

    /**
     * getChildCount
     *
     * @param parent parent
     * @return int
     */
    @Override
    public int getChildCount(Object parent) {
        int realCount = super.getChildCount(parent);
        if (filter.aBoolean) {
            return realCount;
        }
        int filterCount = 0;
        for (int index = 0; index < realCount; index++) {
            TreeNode child = null;
            Object childObject = super.getChild(parent, index);
            if (childObject instanceof TreeNode) {
                child = (TreeNode) childObject;
                if (filter.passTreeNode(child, filterString)) {
                    filterCount++;
                }
            }
        }
        return filterCount;
    }

    /**
     * getChild
     *
     * @param parent parent
     * @param index index
     * @return return
     */
    @Override
    public Object getChild(Object parent, int index) {
        if (filter.aBoolean) {
            return ((TreeNode) parent).getChildAt(index);
        }
        int childrenIndex = -1;
        for (int count = 0; count < super.getChildCount(parent); count++) {
            TreeNode child = null;
            Object childObject = super.getChild(parent, count);
            if (childObject instanceof TreeNode) {
                child = (TreeNode) childObject;
                if (filter.passTreeNode(child, filterString)) {
                    childrenIndex++;
                }
                if (childrenIndex == index) {
                    return child;
                }
            }
        }
        return null;
    }
}