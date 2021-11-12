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
import org.junit.jupiter.api.Test;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.lang.reflect.Field;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Expand Tree Table Test
 */
class ExpandTreeTableTest {
    private DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    private ColumnInfo[] columns = new ColumnInfo[] {new TreeColumnInfo("Name")};
    private ListTreeTableModelOnColumns tableModelOnColumns = new ListTreeTableModelOnColumns(root, columns);
    private ExpandTreeTable expandTreeTable = new ExpandTreeTable(tableModelOnColumns);

    @Test
    void freshTreeExpand() {
        expandTreeTable.freshTreeExpand();
        assertNotNull(expandTreeTable);
    }

    @Test
    void freshTreeRowExpand() {
        expandTreeTable.freshTreeRowExpand();
        assertNotNull(expandTreeTable);
    }

    @Test
    void getExpandList() throws NoSuchFieldException, IllegalAccessException {
        Vector<TreePath> vector = new Vector<>();
        final Field field = expandTreeTable.getClass().getDeclaredField("expandList");
        field.setAccessible(true);
        field.set(expandTreeTable, vector);
        assertEquals(vector, expandTreeTable.getExpandList());
    }

    @Test
    void setExpandList() throws NoSuchFieldException, IllegalAccessException {
        Vector<TreePath> vector = new Vector<>();
        expandTreeTable.setExpandList(vector);
        final Field field = expandTreeTable.getClass().getDeclaredField("expandList");
        field.setAccessible(true);
        assertEquals(vector, field.get(expandTreeTable));
    }
}