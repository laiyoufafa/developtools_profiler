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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.ui.ColumnInfo;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.applicationtrace.bean.TreeTableBean;
import ohos.devtools.views.applicationtrace.listener.IAllThreadDataListener;
import ohos.devtools.views.applicationtrace.listener.IOtherThreadDataListener;
import ohos.devtools.views.trace.EventPanel;
import ohos.devtools.views.trace.TracePanel;
import ohos.devtools.views.trace.util.Final;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JTree;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import java.awt.Component;
import java.awt.Font;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * TopBottomPanel
 *
 * @date: 2021/5/24 11:57
 */
public class TopBottomPanel extends EventPanel {
    private JBTextField search = new JBTextField();
    private DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    private ListTreeTableModelOnColumns tableModelOnColumns;
    private int currentSortKey = 1;
    private SortOrder currentOrder = SortOrder.DESCENDING;
    private String searchText = "";
    private ColumnInfo[] columns = new ColumnInfo[] {new TreeColumnInfo("Name"),
        new TreeTableColumn<>("Total(µs)", TreeTableBean.class, Long.class) {
            @Override
            @NotNull
            Long getCompareValue(TreeTableBean nodeData) {
                return nodeData.getTotalNum();
            }
        }, new TreeTableColumn<>("%", TreeTableBean.class, Double.class) {
        @Override
        @NotNull
        Double getCompareValue(TreeTableBean nodeData) {
            return nodeData.getTotalPercentNum();
        }
    }, new TreeTableColumn<>("Self(µs)", TreeTableBean.class, Long.class) {
        @Override
        @NotNull
        Long getCompareValue(TreeTableBean nodeData) {
            return nodeData.getSelfNum();
        }
    }, new TreeTableColumn<>("%", TreeTableBean.class, Double.class) {
        @Override
        @NotNull
        Double getCompareValue(TreeTableBean nodeData) {
            return nodeData.getSelfPercentNum();
        }
    }, new TreeTableColumn<>("Children(µs)", TreeTableBean.class, Long.class) {
        @Override
        @NotNull
        Long getCompareValue(TreeTableBean nodeData) {
            return nodeData.getChildrenNum();
        }
    }, new TreeTableColumn<>("%", TreeTableBean.class, Double.class) {
        @Override
        @NotNull
        Double getCompareValue(TreeTableBean nodeData) {
            return nodeData.getChildrenPercentNum();
        }
    }};
    private ExpandTreeTable jbTreeTable;
    private IAllThreadDataListener iAllThreadDataListener;
    private IOtherThreadDataListener iOtherThreadDataListener;

    /**
     * constructor
     */
    public TopBottomPanel() {
        this(null, null);
    }

    /**
     * constructor with listener
     *
     * @param iAllThreadDataListener all thread listener
     * @param iOtherThreadDataListener other thread listener
     */
    public TopBottomPanel(IAllThreadDataListener iAllThreadDataListener,
        IOtherThreadDataListener iOtherThreadDataListener) {
        this.iAllThreadDataListener = iAllThreadDataListener;
        this.iOtherThreadDataListener = iOtherThreadDataListener;
        initPanel();
    }

    /**
     * get node contains keyword
     * Set node type 0 OK 1 based on keywords There are keywords 2 children there keywords 3 no keywords
     *
     * @param node node
     * @param searchText keyword
     * @return getNodeContainSearch
     */
    public static boolean getNodeContainSearch(DefaultMutableTreeNode node, String searchText) {
        boolean hasKeyWord = false;
        if (searchText.isEmpty()) {
            return false;
        }
        if (!node.isLeaf()) {
            Enumeration<TreeNode> children = node.children();
            while (children.hasMoreElements()) {
                TreeNode treNode = children.nextElement();
                if (treNode instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode) treNode;
                    if (nextElement.getUserObject() instanceof TreeTableBean) {
                        TreeTableBean bean = (TreeTableBean) nextElement.getUserObject();
                        if (getNodeContainSearch(nextElement, searchText)) {
                            if (!hasKeyWord) {
                                hasKeyWord = true;
                            }
                            bean.setContainType(2);
                        } else {
                            bean.setContainType(3);
                        }
                        if (nextElement.getUserObject().toString().toLowerCase(Locale.ENGLISH).contains(searchText)) {
                            hasKeyWord = true;
                            bean.setContainType(1);
                        }
                    }
                }
            }
        } else {
            if (node.getUserObject() instanceof TreeTableBean) {
                TreeTableBean bean = (TreeTableBean) node.getUserObject();
                if (bean.getName().toLowerCase(Locale.ENGLISH).contains(searchText)) {
                    hasKeyWord = true;
                    bean.setContainType(1);
                } else {
                    bean.setContainType(3);
                }
            }
        }
        return hasKeyWord;
    }

    /**
     * reset nodes
     *
     * @param node node
     */
    public static void resetAllNode(DefaultMutableTreeNode node) { // Put all nodes in a healthy state of 0
        Enumeration<TreeNode> enumeration = node.breadthFirstEnumeration();
        while (enumeration.hasMoreElements()) {
            TreeNode treNode = enumeration.nextElement();
            if (treNode instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode) treNode;
                if (nextElement.getUserObject() instanceof TreeTableBean) {
                    ((TreeTableBean) nextElement.getUserObject()).setContainType(0);
                }
            }
        }
    }

    private void initPanel() {
        setLayout(new MigLayout("inset 10", "[grow,fill]", "[][grow,fill]"));
        search.setTextToTriggerEmptyTextStatus("Search");
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchText = search.getText().toLowerCase(Locale.ENGLISH);
                getNodeContainSearch(root, searchText);
                treeResort(root);
                tableModelOnColumns.reload();
                jbTreeTable.freshTreeRowExpand();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchText = search.getText().toLowerCase(Locale.ENGLISH);
                if (searchText.isEmpty()) {
                    resetAllNode(root);
                } else {
                    getNodeContainSearch(root, searchText);
                }
                treeResort(root);
                tableModelOnColumns.reload();
                jbTreeTable.freshTreeRowExpand();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        add(search, "wrap");
        setBackground(JBColor.background().brighter());
        initTree();
    }

    private void initTree() {
        tableModelOnColumns = new ListTreeTableModelOnColumns(root, columns);
        jbTreeTable = new ExpandTreeTable(tableModelOnColumns);
        jbTreeTable.setColumnProportion(0.12F);
        jbTreeTable.getTree().addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                Object lpc = event.getPath().getLastPathComponent();
                if (lpc != null && lpc instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) lpc;
                    treeResort(lastPathComponent);
                }
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
            }
        });
        TreeTableRowSorter sorter = new TreeTableRowSorter(jbTreeTable.getTable().getModel());
        sorter.setListener((columnIndex, sortOrder) -> {
            if (columnIndex <= 0 || columnIndex > columns.length) {
                return;
            }
            currentSortKey = columnIndex;
            currentOrder = sortOrder;
            treeResort(root);
            tableModelOnColumns.reload();
            jbTreeTable.freshTreeExpand();

        });
        jbTreeTable.getTree().setExpandsSelectedPaths(true);
        jbTreeTable.getTable().setRowSorter(sorter);
        jbTreeTable.getTree().setCellRenderer(new TableTreeCellRender());
        jbTreeTable.getTree().getExpandableItemsHandler().setEnabled(true);
        add(jbTreeTable);
    }

    private void treeResort(DefaultMutableTreeNode node) {
        if (currentOrder == SortOrder.ASCENDING) {
            TreeTableRowSorter.sortDescTree(node, columns[currentSortKey].getComparator(), jbTreeTable.getTree());
        } else {
            TreeTableRowSorter.sortTree(node, columns[currentSortKey].getComparator(), jbTreeTable.getTree());
        }
    }

    /**
     * refresh tree data
     *
     * @param nodes nodes list
     */
    public void freshTreeData(List<DefaultMutableTreeNode> nodes) {
        if (Objects.isNull(nodes)) {
            return;
        }
        root.removeAllChildren();
        nodes.forEach(item -> root.add(item));
        getNodeContainSearch(root, searchText);
        treeResort(root);
        tableModelOnColumns.reload();
        jbTreeTable.freshTreeRowExpand();
    }

    @Override
    public void change(long startNS, long endNS, long scale) {
        if (iAllThreadDataListener != null) {
            freshTreeData(iAllThreadDataListener.getAllThreadData(startNS, endNS, scale));
        }
        if (iOtherThreadDataListener != null && TracePanel.rangeStartNS == null && TracePanel.rangeEndNS == null) {
            freshTreeData(
                iOtherThreadDataListener.getOtherThreadData(startNS, endNS, TracePanel.currentSelectThreadIds));
        }
    }

    @Override
    public void change(long startNS, long endNS, List<Integer> threadIds) {
        if (threadIds.size() != 0) {
            if (iOtherThreadDataListener != null) {
                freshTreeData(iOtherThreadDataListener.getOtherThreadData(startNS, endNS, threadIds));
            }
        }
    }

    /**
     * the class of tree table column
     *
     * @param <T> type
     * @param <N> nType
     */
    public abstract class TreeTableColumn<T, N extends Number> extends ColumnInfo<DefaultMutableTreeNode, String> {
        private final Class<T> type;
        private final Class<N> nType;

        /**
         * construction
         *
         * @param name name of table
         * @param typeParameterClass param type
         * @param nTypeParameterClass param type
         */
        public TreeTableColumn(@NlsContexts.ColumnName String name, Class<T> typeParameterClass,
            Class<N> nTypeParameterClass) {
            super(name);
            type = typeParameterClass;
            nType = nTypeParameterClass;
        }

        @Override
        @Nullable
        public String valueOf(DefaultMutableTreeNode defaultMutableTreeNode) {
            if (type.isInstance(defaultMutableTreeNode.getUserObject())) {
                T nodeData = type.cast(defaultMutableTreeNode.getUserObject());
                if (this.getCompareValue(nodeData) instanceof Double) {
                    return String.format(Locale.ENGLISH, "%.2f", this.getCompareValue(nodeData));
                } else {
                    return String.format(Locale.ENGLISH, "%0$,9d", this.getCompareValue(nodeData));
                }
            }
            return "";
        }

        @Override
        @Nullable
        public Comparator<DefaultMutableTreeNode> getComparator() {
            return (o1, o2) -> {
                if (type.isInstance(o1.getUserObject()) && type.isInstance(o2.getUserObject())) {
                    T start = type.cast(o1.getUserObject());
                    T end = type.cast(o2.getUserObject());
                    try {
                        if (this.getCompareValue(start) instanceof Double && this
                            .getCompareValue(end) instanceof Double) {
                            return Double
                                .compare((double) this.getCompareValue(start), (double) this.getCompareValue(end));
                        } else if (this.getCompareValue(start) instanceof Long && this
                            .getCompareValue(end) instanceof Long) {
                            return Long.compare((long) this.getCompareValue(start), (long) this.getCompareValue(end));
                        } else {
                            return 0;
                        }
                    } catch (ClassCastException e) {
                        return 0;
                    }
                }
                return 0;
            };
        }

        @NotNull
        abstract N getCompareValue(T nodeData);
    }

    class TableTreeCellRender extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
            JBLabel jbLabel = new JBLabel();
            jbLabel.setIcon(AllIcons.Nodes.Method);
            jbLabel.setText(value.toString());
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                if (node.getUserObject() instanceof TreeTableBean) {
                    TreeTableBean tableBean = (TreeTableBean) node.getUserObject();
                    switch (tableBean.getContainType()) {
                        case 0:
                        case 2:
                            jbLabel.setFont(new Font(Final.FONT_NAME, Font.PLAIN, Final.NORMAL_FONT_SIZE));
                            jbLabel.setForeground(JBColor.foreground());
                            break;
                        case 1:
                            jbLabel.setFont(new Font(Final.FONT_NAME, Font.BOLD, Final.NORMAL_FONT_SIZE));
                            jbLabel.setForeground(JBColor.foreground());
                            break;
                        case 3:
                            jbLabel.setFont(new Font(Final.FONT_NAME, Font.PLAIN, Final.NORMAL_FONT_SIZE));
                            jbLabel.setForeground(JBColor.foreground().darker());
                            break;
                    }
                }
            }
            return jbLabel;
        }
    }
}
