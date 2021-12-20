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

package ohos.devtools.views.distributed;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.JBTreeTable;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.ui.ColumnInfo;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.applicationtrace.analysis.ExpandTreeTable;
import ohos.devtools.views.applicationtrace.analysis.TreeTableRowSorter;
import ohos.devtools.views.applicationtrace.util.TimeUtils;
import ohos.devtools.views.distributed.bean.DetailBean;
import ohos.devtools.views.distributed.bean.DistributedFuncBean;
import ohos.devtools.views.distributed.util.DetailTreeTableColumn;
import ohos.devtools.views.distributed.util.DistributedCache;
import ohos.devtools.views.trace.EventDispatcher;
import ohos.devtools.views.trace.Tip;
import ohos.devtools.views.trace.util.Final;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ohos.devtools.views.distributed.util.DistributedDataPraser.collectByName;

/**
 * DistributedDataDetailPane
 *
 * @since 2021/08/05 16:06
 */
public class DistributedDataDetailPane extends JBPanel implements IDistributedData {
    private static final Logger LOGGER = LogManager.getLogger(DistributedDataDetailPane.class);

    private JBTextField search = new JBTextField();
    private JBTreeTable treeTable;
    private DefaultMutableTreeNode root = new DefaultMutableTreeNode(new DetailBean());
    private int currentSortKey = 2;
    private SortOrder currentOrder = SortOrder.DESCENDING;
    private String searchText = "";
    private DetailBean currentSelectBean = null;
    private ColumnInfo[] columns = new ColumnInfo[] {new TreeColumnInfo("Name"),
        new DetailTreeTableColumn<>("Params", DetailBean.class, String.class) {
            @Override
            public String getCompareValue(DetailBean nodeData) {
                return nodeData.getParams();
            }
        }, new DetailTreeTableColumn<>("Total", DetailBean.class, Long.class) {
        @Override
        public Long getCompareValue(DetailBean nodeData) {
            return nodeData.getTotalNS();
        }
    }, new DetailTreeTableColumn<>("Delay", DetailBean.class, Long.class) {
        @Override
        public Long getCompareValue(DetailBean nodeData) {
            return nodeData.getDelayNS();
        }
    }};
    private ListTreeTableModelOnColumns tableModelOnColumns;

    /**
     * DistributedDataDetailPane
     */
    public DistributedDataDetailPane() {
        setLayout(new MigLayout("inset 0"));
        initSearch();
        initTable();
        initData();
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
                if (nextElement.getUserObject() instanceof DetailBean) {
                    DetailBean userObject = (DetailBean) nextElement.getUserObject();
                    userObject.setContainType(0);
                }
            }
        }
    }

    /**
     * getCurrentSelectBean
     *
     * @return currentSelectBean currentSelectBean
     */
    public DetailBean getCurrentSelectBean() {
        return currentSelectBean;
    }

    /**
     * setCurrentSelectBean
     *
     * @param currentSelectBean DetailBean
     */
    public void setCurrentSelectBean(DetailBean currentSelectBean) {
        this.currentSelectBean = currentSelectBean;
    }

    private void initSearch() {
        search.setTextToTriggerEmptyTextStatus("Search");
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                searchText = search.getText().toLowerCase(Locale.ENGLISH);
                getNodeContainSearch(root, searchText);
                treeResort(root);
                tableModelOnColumns.reload();
                expandTree(treeTable.getTree());
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                searchText = search.getText().toLowerCase(Locale.ENGLISH);
                if (searchText.isEmpty()) {
                    resetAllNode(root);
                } else {
                    getNodeContainSearch(root, searchText);
                }
                treeResort(root);
                tableModelOnColumns.reload();
                expandTree(treeTable.getTree());
            }

            @Override
            public void changedUpdate(DocumentEvent even) {
            }
        });
        add(search, "w 300:400:500,wrap");
    }

    private void initTable() {
        tableModelOnColumns = new ListTreeTableModelOnColumns(root, columns);
        treeTable = new ExpandTreeTable(tableModelOnColumns);
        treeTable.setColumnProportion(0.2F);
        treeTable.getTree().setExpandsSelectedPaths(true);
        treeTable.getTree().setExpandableItemsEnabled(true);
        treeTable.getTree().setCellRenderer(new TreeCellRender());
        treeTable.setDefaultRenderer(String.class, new ColorCellRenderer());
        addListener();
        add(treeTable, "push,grow");
        TreeTableRowSorter sorter = new TreeTableRowSorter(treeTable.getTable().getModel());
        sorter.setListener((columnIndex, sortOrder) -> {
            if (columnIndex <= 0 || columnIndex > columns.length) {
                return;
            }
            currentSortKey = columnIndex;
            currentOrder = sortOrder;
            treeResort(root);
            tableModelOnColumns.reload();
            expandTree(treeTable.getTree());
        });
        treeTable.getTable().setRowSorter(sorter);
    }

    private void addListener() {
        treeTable.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent event) {
                super.mouseExited(event);
                Tip.getInstance().hidden();
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                super.mouseClicked(event);
                if (currentSelectBean != null) {
                    List<String> stringList = currentSelectBean.getStringList();
                    Tip.getInstance().display(event, stringList);
                    EventDispatcher.dispatcherFuncChangeListener(currentSelectBean.getId());
                }
            }
        });
        treeTable.getTree().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent event) {
                super.mouseExited(event);
                Tip.getInstance().hidden();
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                super.mouseClicked(event);
                if (currentSelectBean != null) {
                    List<String> stringList = currentSelectBean.getStringList();
                    Tip.getInstance().display(event, stringList);
                    EventDispatcher.dispatcherFuncChangeListener(currentSelectBean.getId());
                }
            }
        });
        treeTable.getTree().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent event) {
                if (treeTable.getTree().getLastSelectedPathComponent() instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode node =
                        (DefaultMutableTreeNode) treeTable.getTree().getLastSelectedPathComponent();
                    if (node == null) {
                        return;
                    }
                    if (node.getUserObject() instanceof DetailBean) {
                        currentSelectBean = (DetailBean) node.getUserObject();
                    }
                }
            }
        });
    }

    private void initData() {
        EventDispatcher.addThreadRangeListener((startNS, endNS, threadIds) -> {
            if ("A".equals(DistributedCache.getCurrentDBFlag())) {
                dataSelectChangeA(startNS, endNS, threadIds);
            } else if ("B".equals(DistributedCache.getCurrentDBFlag())) {
                dataSelectChangeB(startNS, endNS, threadIds);
            } else {
                if (ProfilerLogManager.isDebugEnabled()) {
                    LOGGER.debug("DistributedCache flag error");
                }
            }
        });
    }

    /**
     * freshTreeData fresh current tree data
     *
     * @param nodes List<DefaultMutableTreeNode>
     */
    public void freshTreeData(List<DefaultMutableTreeNode> nodes) {
        if (Objects.isNull(nodes)) {
            return;
        }
        root.removeAllChildren();
        nodes.forEach(item -> root.add(item));
        treeResort(root);
        tableModelOnColumns.reload();
        expandTree(treeTable.getTree());
        Enumeration<TreeNode> enumeration = root.depthFirstEnumeration();
        while (enumeration.hasMoreElements()) {
            TreeNode treNode = enumeration.nextElement();
            if (treNode instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode) treNode;
                if (nextElement.getUserObject() instanceof DetailBean) {
                    DetailBean userObject = (DetailBean) nextElement.getUserObject();
                    if (DistributedFuncBean.currentSelectedFunc != null && DistributedFuncBean.currentSelectedFunc
                        .getId().equals(userObject.getId())) {
                        treeTable.getTree().setSelectionPath(new TreePath(nextElement.getPath()));
                    }
                }
            }
        }
    }

    /**
     * freshTreeData fresh current tree data
     */
    public void freshTreeData() {
        List<DefaultMutableTreeNode> nodes = new ArrayList<>();
        Enumeration<TreeNode> children = root.children();
        while (children.hasMoreElements()) {
            TreeNode treeNode = children.nextElement();
            if (treeNode instanceof DefaultMutableTreeNode) {
                nodes.add((DefaultMutableTreeNode) treeNode);
            }
        }
        root.removeAllChildren();
        tableModelOnColumns.reload();
        nodes.forEach(item -> root.add(item));
        tableModelOnColumns.reload();
        expandTree(treeTable.getTree());
        Enumeration<TreeNode> enumeration = root.depthFirstEnumeration();
        while (enumeration.hasMoreElements()) {
            TreeNode treNode = enumeration.nextElement();
            if (treNode instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode) treNode;
                if (nextElement.getUserObject() instanceof DetailBean) {
                    DetailBean userObject = (DetailBean) nextElement.getUserObject();
                    if (DistributedFuncBean.currentSelectedFunc != null && DistributedFuncBean.currentSelectedFunc
                        .getId().equals(userObject.getId())) {
                        treeTable.getTree().setSelectionPath(new TreePath(nextElement.getPath()));
                    }
                }
            }
        }
    }

    private void treeResort(DefaultMutableTreeNode node) {
        if (currentOrder == SortOrder.ASCENDING) {
            TreeTableRowSorter.sortDescTree(node, columns[currentSortKey].getComparator(), treeTable.getTree());
        } else {
            TreeTableRowSorter.sortTree(node, columns[currentSortKey].getComparator(), treeTable.getTree());
        }
    }

    /**
     * get node contains keyword
     *
     * @param node node
     * @param searchText keyword
     * @return getNodeContainSearch
     */
    public boolean getNodeContainSearch(DefaultMutableTreeNode node, String searchText) {

        // Set node type 0 OK 1 based on keywords
        // There are keywords 2 children there keywords 3 no keywords
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
                    if (nextElement.getUserObject() instanceof DetailBean) {
                        DetailBean bean = (DetailBean) nextElement.getUserObject();
                        if (getNodeContainSearch(nextElement, searchText)) {
                            if (!hasKeyWord) {
                                hasKeyWord = true;
                            }
                            bean.setContainType(2);
                        } else {
                            bean.setContainType(3);
                        }
                        if (bean.toString().toLowerCase(Locale.ENGLISH).contains(searchText)) {
                            hasKeyWord = true;
                            bean.setContainType(1);
                        }
                    }
                }
            }
        } else {
            if (node.getUserObject() instanceof DetailBean) {
                DetailBean bean = (DetailBean) node.getUserObject();
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

    private void dataPrase(List<DistributedFuncBean> list) { // Calculations are not merged
        List<DefaultMutableTreeNode> nodes = new ArrayList<>();
        if (list.size() == 0) {
            freshTreeData(nodes);
            return;
        }
        DetailBean middleBean = collectByName(list);
        Map<Integer, DefaultMutableTreeNode> funcBeanMap =
            list.stream().collect(Collectors.toMap(DistributedFuncBean::getId, (bean) -> {
                DetailBean detailBean = new DetailBean();
                detailBean.mergeFuncBean(bean);
                detailBean.mergeDetailcBean(middleBean);
                return new DefaultMutableTreeNode(detailBean);
            }));
        funcBeanMap.values().forEach((node) -> {
            if (node.getUserObject() instanceof DetailBean) {
                DetailBean funcBean = (DetailBean) node.getUserObject();
                if (funcBean.getParentId() == 0) {
                    funcBean.setDelayNS(0L);
                    nodes.add(node);
                } else {
                    if (funcBeanMap.containsKey(funcBean.getParentId())) {
                        DefaultMutableTreeNode parentNode = funcBeanMap.get(funcBean.getParentId());
                        parentNode.add(node);
                    }
                }
            }
        });
        freshTreeData(nodes);
    }

    private void dataSelectChangeA(long startNS, long endNS, List<Integer> threadIds) {
        List<DistributedFuncBean> funcList = new ArrayList<>();
        DistributedCache.FUNC_MAP_A.entrySet().stream().filter((entry) -> threadIds.contains(entry.getKey()))
            .forEach((entry) -> {
                funcList.addAll(entry.getValue().stream()
                    .filter((item) -> TimeUtils.isRangeCross(item.getStartTs(), item.getEndTs(), startNS, endNS))
                    .peek((item) -> {
                        if (item.getDepth() == 0) {
                            item.setDelay(0L);
                        } else if (DistributedCache.ID_FUNC_BEAN_MAP_A.containsKey(item.getParentId())) {
                            item.setDelay(item.getStartTs() - DistributedCache.ID_FUNC_BEAN_MAP_A
                                .get(item.getParentId()).getStartTs());
                        } else {
                            if (ProfilerLogManager.isDebugEnabled()) {
                                LOGGER.debug("item Depth error");
                            }
                        }
                    }).collect(Collectors.toList()));
            });
        dataPrase(funcList);
    }

    private void dataSelectChangeB(long startNS, long endNS, List<Integer> threadIds) {
        List<DistributedFuncBean> funcList = new ArrayList<>();
        DistributedCache.FUNC_MAP_B.entrySet().stream().filter((entry) -> threadIds.contains(entry.getKey()))
            .forEach((entry) -> {
                funcList.addAll(entry.getValue().stream()
                    .filter((item) -> TimeUtils.isRangeCross(item.getStartTs(), item.getEndTs(), startNS, endNS))
                    .peek((item) -> {
                        if (item.getDepth() == 0) {
                            item.setDelay(0L);
                        } else if (DistributedCache.ID_FUNC_BEAN_MAP_B.containsKey(item.getParentId())) {
                            item.setDelay(item.getStartTs() - DistributedCache.ID_FUNC_BEAN_MAP_B
                                .get(item.getParentId()).getStartTs());
                        } else {
                            if (ProfilerLogManager.isDebugEnabled()) {
                                LOGGER.debug("item Depth error");
                            }
                        }
                    }).collect(Collectors.toList()));
            });
        dataPrase(funcList);
    }

    private class TreeCellRender extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
            JBLabel jbLabel = new JBLabel();
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                if (node.getUserObject() instanceof DetailBean) {
                    DetailBean userObject = (DetailBean) node.getUserObject();
                    if (userObject.getChainId() != null && !userObject.getChainId().isEmpty()) {
                        jbLabel.setText(
                            userObject.getName() + "[" + userObject.getChainId() + "," + userObject.getSpanId() + ","
                                + userObject.getParentSpanId() + "]");
                    } else {
                        jbLabel.setText(userObject.getName());
                    }
                    switch (userObject.getContainType()) {
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

    /**
     * ColorCellRenderer set the cell color from renderer
     */
    public class ColorCellRenderer extends DefaultTableCellRenderer {
        /**
         * getTableCellRendererComponent
         *
         * @param table table
         * @param value value
         * @param isSelected isSelected
         * @param hasFocus hasFocus
         * @param row row
         * @param column column
         * @return Component
         */
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
            if (value instanceof String) {
                setForeground(JBColor.foreground());
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else if (value instanceof DetailBean) {
                DetailBean bean = (DetailBean) value;
                if (column == 1 && bean.getTotalNS() > bean.getMiddleNs() * DistributedCache.getTotalMedianTimes()) {
                    setForeground(JBColor.RED);
                } else if (column == 2
                    && bean.getDelayNS() > bean.getMiddleDelayNS() * DistributedCache.getDelayMedianTimes()) {
                    setForeground(JBColor.RED);
                } else {
                    setForeground(JBColor.foreground());
                }
                super.getTableCellRendererComponent(table,
                    TimeUtils.getTimeWithUnit(column == 1 ? bean.getTotalNS() : bean.getDelayNS()), isSelected,
                    hasFocus, row, column);
            } else {
                if (ProfilerLogManager.isDebugEnabled()) {
                    LOGGER.debug("TableCellRenderer Inconsistent types");
                }
            }
            return this;
        }
    }
}
