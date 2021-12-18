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

import com.intellij.ui.JBColor;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBTreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import ohos.devtools.views.layout.chartview.memory.heapdump.HeapDumpThirdInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.util.Vector;

/**
 * ExpandTreeTable
 */
public class ExpandTreeTable extends JBTreeTable {
    private static final Logger LOGGER = LogManager.getLogger(ExpandTreeTable.class);

    private final Vector<TreePath> expandList = new Vector<>();
    private final Vector<Integer> expandRowList = new Vector<>();
    private long firstTime;
    private JScrollBar verticalScrollBar;
    private int count = 100;
    private int nextIndex = 0;

    /**
     * ExpandTreeTable
     *
     * @param model model
     */
    public ExpandTreeTable(@NotNull TreeTableModel model) {
        super(model);
        this.getTree().setBackground(JBColor.background());
        Component[] components = super.getComponents();
        for (Component component : components) {
            if (component instanceof OnePixelSplitter) {
                OnePixelSplitter splitter = (OnePixelSplitter) component;
                JScrollPane firstComponent = null;
                Object componentObject = splitter.getFirstComponent();
                if (componentObject instanceof JScrollPane) {
                    firstComponent = (JScrollPane) componentObject;
                    verticalScrollBar = firstComponent.getVerticalScrollBar();
                }
            }
        }
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
                loadNodeCollapse(event);
            }
        });
    }

    public JScrollBar getVerticalScrollBar() {
        return verticalScrollBar;
    }

    /**
     * freshTreeExpand
     */
    public void freshTreeExpand() {
        // Two executions are not allowed within 200 ms
        firstTime = System.currentTimeMillis();
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

    /**
     * loadNodeCollapse
     *
     * @param event event
     */
    private void loadNodeCollapse(TreeExpansionEvent event) {
        DefaultMutableTreeNode currentNode = null;
        Object lastPathComponentObject = event.getPath().getLastPathComponent();
        if (lastPathComponentObject instanceof DefaultMutableTreeNode) {
            currentNode = (DefaultMutableTreeNode) lastPathComponentObject;
            Object userObj = currentNode.getUserObject();
            if (!(userObj instanceof HeapDumpThirdInfo)) {
                return;
            }
            currentNode.removeAllChildren();
            // add None to proved node can expand
            currentNode.add(new DefaultMutableTreeNode());
            count = 0;
        }
    }
}
