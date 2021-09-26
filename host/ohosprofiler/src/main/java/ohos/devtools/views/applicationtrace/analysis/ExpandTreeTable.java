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

import com.intellij.ui.components.JBTreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreePath;
import java.util.Vector;

/**
 * ExpandTreeTable
 *
 * @date: 2021/5/19 16:39
 */
public class ExpandTreeTable extends JBTreeTable {
    private Vector<TreePath> expandList = new Vector();
    private Vector<Integer> expandRowList = new Vector();

    /**
     * Constructor
     *
     * @param model model
     */
    public ExpandTreeTable(@NotNull TreeTableModel model) {
        super(model);
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
                    expandRowList.remove(Integer.valueOf(getTree().getRowForPath(event.getPath())));
                }
            }
        });
    }

    /**
     * fresh the tree expand
     */
    public void freshTreeExpand() {
        expandList.forEach(item -> {
            if (!getTree().isExpanded(item)) {
                getTree().expandPath(item);
            }
        });
    }

    /**
     * fresh the tree row expand
     */
    public void freshTreeRowExpand() {
        expandRowList.forEach(item -> {
            getTree().expandRow(item);
        });
    }

    /**
     * get the expand list
     *
     * @return selected Vector TreePath
     */
    public Vector<TreePath> getExpandList() {
        return expandList;
    }

    /**
     * set the expand list
     *
     * @param expandList expandList
     */
    public void setExpandList(Vector<TreePath> expandList) {
        this.expandList = expandList;
    }

}
