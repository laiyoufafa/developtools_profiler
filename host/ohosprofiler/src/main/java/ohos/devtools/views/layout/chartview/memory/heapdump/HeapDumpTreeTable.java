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

package ohos.devtools.views.layout.chartview.memory.heapdump;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBTreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreePath;
import java.util.Vector;

/**
 * HeapDumpTreeTable
 *
 * @since 2021/11/22
 */
public class HeapDumpTreeTable extends JBTreeTable {
    private final Vector<TreePath> expandList = new Vector<>();
    private final Vector<Integer> expandRowList = new Vector<>();

    /**
     * HeapDumpTreeTable
     *
     * @param model model
     */
    public HeapDumpTreeTable(@NotNull TreeTableModel model) {
        super(model);
        this.getTree().setBackground(JBColor.background());
        getTree().addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                if (event.getPath() != null && !expandList.contains(event.getPath())) {
                    expandList.add(event.getPath());
                }
                if (event.getPath() != null && !expandRowList.contains(getTree().getRowForPath(event.getPath()))) {
                    expandRowList.add(getTree().getRowForPath(event.getPath()));
                }
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                if (event.getPath() != null && expandList.contains(event.getPath())) {
                    expandList.remove(event.getPath());
                }
                if (event.getPath() != null && expandRowList.contains(getTree().getRowForPath(event.getPath()))) {
                    Object rowForPathObject = getTree().getRowForPath(event.getPath());
                    if (rowForPathObject instanceof Integer) {
                        expandRowList.remove((Integer) rowForPathObject);
                    }
                }
            }
        });
    }

    /**
     * freshTreeExpand
     */
    public void freshTreeExpand() {
        // Two executions are not allowed within 200 ms
        expandList.forEach(item -> {
            if (!getTree().isExpanded(item)) {
                getTree().expandPath(item);
            }
        });
    }

    /**
     * freshTreeRowExpand
     */
    public void freshTreeRowExpand() {
        expandRowList.forEach(item -> {
            getTree().expandRow(item);
        });
    }

    /**
     * setTreeTableModel
     *
     * @param treeTableModel treeTableModel
     */
    public void setTreeTableModel(TreeTableModel treeTableModel) {
        super.setModel(treeTableModel);
        expandList.clear();
        getTree().addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                if (event.getPath() != null && !expandList.contains(event.getPath())) {
                    expandList.add(event.getPath());
                }
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                if (event.getPath() != null) {
                    expandList.remove(event.getPath());
                }
            }
        });
    }
}
