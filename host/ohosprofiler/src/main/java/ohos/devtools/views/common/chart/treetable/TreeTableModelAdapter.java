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

package ohos.devtools.views.common.chart.treetable;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

/**
 * 表模型适配器
 *
 * @version 1.0
 * @date 2021/2/2 19:02
 **/
public class TreeTableModelAdapter extends AbstractTableModel {
    private JTree jTree;
    private TreeTableModel treeTableModel;

    public TreeTableModelAdapter(TreeTableModel treeTableModel, JTree jTree) {
        this.jTree = jTree;
        this.treeTableModel = treeTableModel;
        jTree.addTreeExpansionListener(new TreeExpansionListener() {
            /**
             * 树展开
             *
             * @param event event
             */
            public void treeExpanded(TreeExpansionEvent event) {
                fireTableDataChanged();
            }

            /**
             * 表数据更改
             *
             * @param event event
             */
            public void treeCollapsed(TreeExpansionEvent event) {
                fireTableDataChanged();
            }
        });
    }

    /**
     * getColumnCount
     *
     * @return int
     */
    @Override
    public int getColumnCount() {
        return treeTableModel.getColumnCount();
    }

    /**
     * getColumnName
     *
     * @param column column
     * @return String
     */
    @Override
    public String getColumnName(int column) {
        return treeTableModel.getColumnName(column);
    }

    /**
     * getColumnClass
     *
     * @param column column
     * @return Class
     */
    @Override
    public Class getColumnClass(int column) {
        return treeTableModel.getColumnClass(column);
    }

    /**
     * getRowCount
     *
     * @return int
     */
    @Override
    public int getRowCount() {
        return jTree.getRowCount();
    }

    /**
     * getValueAt
     *
     * @param row    row
     * @param column column
     * @return Object
     */
    @Override
    public Object getValueAt(int row, int column) {
        TreePath treePath = jTree.getPathForRow(row);
        Object rowNode = treePath.getLastPathComponent();
        return treeTableModel.getValueAt(rowNode, column);
    }

    /**
     * 单元格可编辑
     *
     * @param row    row
     * @param column column
     * @return boolean
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
