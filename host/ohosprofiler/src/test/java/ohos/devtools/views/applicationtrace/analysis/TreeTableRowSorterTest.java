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

package ohos.devtools.views.applicationtrace.analysis;

import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.ui.ColumnInfo;
import ohos.devtools.views.distributed.bean.DetailBean;
import ohos.devtools.views.distributed.util.DetailTreeTableColumn;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import javax.swing.tree.DefaultMutableTreeNode;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TreeTableRowSorterTest {
    private DefaultMutableTreeNode root = new DefaultMutableTreeNode(new DetailBean());
    private ColumnInfo[] columns = new ColumnInfo[] {new TreeColumnInfo("Name"),
        new DetailTreeTableColumn<>("Params", DetailBean.class, String.class) {
            @Override
            @NotNull
            public String getCompareValue(DetailBean nodeData) {
                return nodeData.getParams();
            }
        }};
    private ListTreeTableModelOnColumns tableModelOnColumns = new ListTreeTableModelOnColumns(root, columns);
    private ExpandTreeTable jbTreeTable = new ExpandTreeTable(tableModelOnColumns);
    private TreeTableRowSorter sorter = new TreeTableRowSorter(jbTreeTable.getTable().getModel());

    @Test
    void sortDescTree() {
        TreeTableRowSorter.sortDescTree(root, columns[1].getComparator(), jbTreeTable.getTree());
        assertNotNull(sorter);
    }

    @Test
    void sortDescTreeNoChild() {
        root.removeAllChildren();
        TreeTableRowSorter.sortDescTree(root, columns[1].getComparator(), jbTreeTable.getTree());
        assertNotNull(sorter);
    }

    @Test
    void sortDescTreeOneEmptyChild() {
        root.removeAllChildren();
        DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode(null);
        root.add(defaultMutableTreeNode);
        TreeTableRowSorter.sortDescTree(root, columns[1].getComparator(), jbTreeTable.getTree());
        assertNotNull(sorter);
    }

    @Test
    void sortDescTreeTwoEmptyChild() {
        root.removeAllChildren();
        DetailBean childDetail1 = new DetailBean();
        childDetail1.setParams("2");
        DefaultMutableTreeNode child1 = new DefaultMutableTreeNode(childDetail1);
        DetailBean childDetail2 = new DetailBean();
        childDetail1.setParams("1");
        DefaultMutableTreeNode child2 = new DefaultMutableTreeNode(childDetail2);
        root.add(child1);
        root.add(child2);
        TreeTableRowSorter.sortDescTree(root, columns[1].getComparator(), jbTreeTable.getTree());
        assertNotNull(sorter);
    }

    @Test
    void sortDescTreeTwoChild() {
        root.removeAllChildren();
        DefaultMutableTreeNode child1 = new DefaultMutableTreeNode();
        DefaultMutableTreeNode child2 = new DefaultMutableTreeNode();
        child1.add(child2);
        root.add(child1);
        TreeTableRowSorter.sortDescTree(root, columns[1].getComparator(), jbTreeTable.getTree());
        assertNotNull(sorter);
    }

    @Test
    void sortTree() {
        TreeTableRowSorter.sortTree(root, columns[1].getComparator(), jbTreeTable.getTree());
        assertNotNull(sorter);
    }

    @Test
    void sortTreeNoChild() {
        root.removeAllChildren();
        TreeTableRowSorter.sortTree(root, columns[1].getComparator(), jbTreeTable.getTree());
        assertNotNull(sorter);
    }

    @Test
    void sortTreeOneEmptyChild() {
        root.removeAllChildren();
        DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode(null);
        root.add(defaultMutableTreeNode);
        TreeTableRowSorter.sortTree(root, columns[1].getComparator(), jbTreeTable.getTree());
        assertNotNull(sorter);
    }

    @Test
    void sortTreeTwoEmptyChild() {
        root.removeAllChildren();
        DetailBean childDetail1 = new DetailBean();
        childDetail1.setParams("2");
        DefaultMutableTreeNode child1 = new DefaultMutableTreeNode(childDetail1);
        DetailBean childDetail2 = new DetailBean();
        childDetail1.setParams("1");
        DefaultMutableTreeNode child2 = new DefaultMutableTreeNode(childDetail2);
        root.add(child1);
        root.add(child2);
        TreeTableRowSorter.sortTree(root, columns[1].getComparator(), jbTreeTable.getTree());
        assertNotNull(sorter);
    }

    @Test
    void sortTreeTwoChild() {
        root.removeAllChildren();
        DefaultMutableTreeNode child1 = new DefaultMutableTreeNode();
        DefaultMutableTreeNode child2 = new DefaultMutableTreeNode();
        child1.add(child2);
        root.add(child1);
        TreeTableRowSorter.sortTree(root, columns[1].getComparator(), jbTreeTable.getTree());
        assertNotNull(sorter);
    }

    @Test
    void setListener() {
        sorter.setListener((columns, sortOrder) -> {
            sortOrder.toString();
        });
        assertNotNull(sorter);
    }

    @Test
    void toggleSortOrder() {
        sorter.toggleSortOrder(1);
        assertNotNull(sorter);
    }

    @Test
    void toggleSortOrderError() {
        sorter.toggleSortOrder(-1);
        assertNotNull(sorter);
    }

    @Test
    void toggleSortOrderMax() {
        sorter.toggleSortOrder(Integer.MAX_VALUE);
        assertNotNull(sorter);
    }

    @Test
    void toggleSortOrderMin() {
        sorter.toggleSortOrder(Integer.MIN_VALUE);
        assertNotNull(sorter);
    }

    @Test
    void toggleSortOrderZero() {
        sorter.toggleSortOrder(0);
        assertNotNull(sorter);
    }

    @Test
    void sort() {
        sorter.sort();
        assertNotNull(sorter);
    }
}