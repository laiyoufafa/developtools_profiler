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
import com.intellij.ui.components.JBTreeTable;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.JBUI;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.applicationtrace.analysis.TreeTableRowSorter;
import ohos.devtools.views.applicationtrace.util.TimeUtils;
import ohos.devtools.views.distributed.bean.DetailBean;
import ohos.devtools.views.distributed.bean.DistributedFuncBean;
import ohos.devtools.views.distributed.util.DetailTreeTableColumn;
import ohos.devtools.views.distributed.util.DistributedCache;
import ohos.devtools.views.distributed.util.DistributedDataPraser;
import ohos.devtools.views.trace.EventDispatcher;
import ohos.devtools.views.trace.Tip;
import ohos.devtools.views.trace.util.ImageUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * DistributedDataCallTreePane
 *
 * @since 2021/08/05 16:07
 */
public class DistributedDataCallTreePane extends JBPanel implements IDistributedData {
    private static final Logger LOGGER = LogManager.getLogger(DistributedDataCallTreePane.class);

    private Map<String, List<DistributedFuncBean>> chainIdFuncBeanMapA = new HashMap<>();
    private Map<String, List<DistributedFuncBean>> chainIdFuncBeanMapB = new HashMap<>();
    private Map<Integer, List<DistributedFuncBean>> parentIdFuncBeanMapA = new HashMap<>();
    private Map<Integer, List<DistributedFuncBean>> parentIdFuncBeanMapB = new HashMap<>();
    private DetailBean currentSelectBean = null;
    private ImageIcon usbIcon = new ImageIcon();
    private ImageIcon wifiIcon = new ImageIcon();
    private JBTreeTable treeTable;
    private DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    private ColumnInfo[] columns = new ColumnInfo[] {new TreeColumnInfo("Name"),
        new DetailTreeTableColumn<>("Params", DetailBean.class, String.class) {
            @Override
            public String getCompareValue(DetailBean nodeData) {
                return nodeData.getParams() == null ? "" : nodeData.getParams();
            }
        }, new DetailTreeTableColumn<>("Total (μs)", DetailBean.class, Long.class) {
        @Override
        public Long getCompareValue(DetailBean nodeData) {
            return nodeData.getTotalNS();
        }
    }, new DetailTreeTableColumn<>("Delay (μs)", DetailBean.class, Long.class) {
        @Override
        public Long getCompareValue(DetailBean nodeData) {
            return nodeData.getDelayNS();
        }
    }};

    private ListTreeTableModelOnColumns tableModelOnColumns;

    /**
     * DistributedDataCallTreePane
     */
    public DistributedDataCallTreePane() {
        setLayout(new MigLayout("inset 0"));
        usbIcon.setImage(ImageUtils.getInstance().getIconUsb());
        wifiIcon.setImage(ImageUtils.getInstance().getIconWifi());
        initTable();
        initData();
    }

    private void initTable() {
        tableModelOnColumns = new ListTreeTableModelOnColumns(root, columns);
        treeTable = new JBTreeTable(tableModelOnColumns);
        treeTable.setColumnProportion(0.2F);
        treeTable.getTree().setExpandsSelectedPaths(true);
        treeTable.getTree().setExpandableItemsEnabled(true);
        treeTable.getTree().setCellRenderer(new TreeCellRender());
        treeTable.setDefaultRenderer(String.class, new ColorCellRenderer());
        add(treeTable, "push,grow");
        TreeTableRowSorter sorter = new TreeTableRowSorter(treeTable.getTable().getModel());
        sorter.setListener((columnIndex, sortOrder) -> {
            if (columnIndex <= 0 || columnIndex > columns.length) {
                return;
            }
            tableModelOnColumns.reload();
        });
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
        addTreeTableMouseListener();
        treeTable.getTree().addTreeSelectionListener(event -> {
            if (treeTable.getTree().getLastSelectedPathComponent() instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node =
                    (DefaultMutableTreeNode) treeTable.getTree().getLastSelectedPathComponent();
                if (node != null && node.getUserObject() instanceof DetailBean) {
                    currentSelectBean = (DetailBean) node.getUserObject();
                }
            }
        });
    }

    private void addTreeTableMouseListener() {
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
    }

    private void initData() {
        EventDispatcher.addClickListener(it -> {
            if (it instanceof DistributedFuncBean) {
                DistributedFuncBean func = (DistributedFuncBean) it;
                freshAllDataBySelectData(func);
            }
        });
    }

    /**
     * freshFuncSelectData fresh all Func with SelectData
     */
    public void freshFuncSelectData() {
        if (DistributedFuncBean.currentSelectedFunc != null) {
            freshAllDataBySelectData(DistributedFuncBean.currentSelectedFunc);
        } else {
            freshTreeData(new ArrayList<>(), null);
        }
    }

    private void freshAllDataBySelectData(DistributedFuncBean selectBean) {
        if (chainIdFuncBeanMapA.size() == 0) {
            chainIdFuncBeanMapA =
                DistributedCache.ID_FUNC_BEAN_MAP_A.values().stream().filter((bean) -> !bean.getChainId().isEmpty())
                    .sorted(Comparator.comparingInt(DistributedFuncBean::getId)).collect(
                    Collectors.groupingBy(DistributedFuncBean::getChainId, LinkedHashMap::new, Collectors.toList()));
        }
        if (chainIdFuncBeanMapB.size() == 0) {
            chainIdFuncBeanMapB =
                DistributedCache.ID_FUNC_BEAN_MAP_B.values().stream().filter((bean) -> !bean.getChainId().isEmpty())
                    .sorted(Comparator.comparingInt(DistributedFuncBean::getId)).collect(
                    Collectors.groupingBy(DistributedFuncBean::getChainId, LinkedHashMap::new, Collectors.toList()));
        }
        if (parentIdFuncBeanMapA.size() == 0) {
            parentIdFuncBeanMapA = DistributedCache.ID_FUNC_BEAN_MAP_A.values().stream()
                .collect(Collectors.groupingBy(DistributedFuncBean::getParentId));
        }
        if (parentIdFuncBeanMapB.size() == 0) {
            parentIdFuncBeanMapB = DistributedCache.ID_FUNC_BEAN_MAP_B.values().stream()
                .collect(Collectors.groupingBy(DistributedFuncBean::getParentId));
        }
        DistributedFuncBean head = findHead(selectBean);
        if (head != null) {
            List<DefaultMutableTreeNode> treeNodes = getTreeNodes(head);
            freshTreeData(treeNodes, selectBean);
        }
    }

    /**
     * freshTreeData fresh all data with select func
     *
     * @param nodes nodeList
     * @param func func
     */
    public void freshTreeData(List<DefaultMutableTreeNode> nodes, DistributedFuncBean func) {
        if (Objects.isNull(nodes)) {
            return;
        }
        root.removeAllChildren();
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
                    if (func.getId().equals(userObject.getId())) {
                        treeTable.getTree().setSelectionPath(new TreePath(nextElement.getPath()));
                    }
                }
            }
        }
    }

    /**
     * findHead find the head func by current func
     *
     * @param selectBean selectBean
     * @return DistributedFuncBean DistributedFuncBean
     */
    public DistributedFuncBean findHead(DistributedFuncBean selectBean) {
        if (selectBean.getParentId() == 0) {

            // Determines whether the current node is top-level and,
            // if so, whether it is non-S-non-s and top-level
            if (!selectBean.getFlag().equals("S")) {
                if (selectBean.getSpanId() == selectBean.getParentSpanId()) {
                    return selectBean;
                } else {
                    return findTopBean(selectBean);
                }
            } else {
                return findHead(findBeanDistributed(selectBean));
            }
        } else {

            // The current node is not top-level
            // Determines whether the current node is s If a non-s node is found up for s
            DistributedFuncBean parentBeanDistributed = findParentBeanDistributed(selectBean);
            if (!selectBean.getFlag().equals("S")) {
                return findHead(parentBeanDistributed);
            } else { // Recursively find the top node
                if (parentBeanDistributed.getChainId().isEmpty()) { // The parent node is a non-distributed node
                    DistributedFuncBean beanDistributed = findBeanDistributed(selectBean);
                    if (beanDistributed != null) {
                        return findHead(beanDistributed);
                    }
                } else {
                    return findHead(parentBeanDistributed);
                }
            }
        }
        return selectBean;
    }

    private DistributedFuncBean findParentBeanDistributed(
        DistributedFuncBean selectBean) { // The parent node was found based on the praentid
        DistributedFuncBean parentA = DistributedCache.ID_FUNC_BEAN_MAP_A.get(selectBean.getParentId());
        DistributedFuncBean parentB = DistributedCache.ID_FUNC_BEAN_MAP_B.get(selectBean.getParentId());
        if (selectBean.getCurrentType().equals(DistributedFuncBean.BeanDataType.TYPE_A)) {
            return parentA;
        } else {
            return parentB;
        }
    }

    private DistributedFuncBean findTopBean(DistributedFuncBean selectBean) {
        if (selectBean.getCurrentType().equals(DistributedFuncBean.BeanDataType.TYPE_A)) {
            DistributedFuncBean topA = chainIdFuncBeanMapA.get(selectBean.getChainId()).stream()
                .filter((item) -> item.getSpanId().equals(selectBean.getParentSpanId()) && !item.getFlag().equals("S"))
                .findFirst().orElse(null);
            if (topA != null) {
                return findHead(topA);
            }
            return selectBean;
        } else {
            DistributedFuncBean topB = chainIdFuncBeanMapB.get(selectBean.getChainId()).stream()
                .filter((item) -> item.getSpanId().equals(selectBean.getParentSpanId()) && !item.getFlag().equals("S"))
                .findFirst().orElse(null);
            if (topB != null) {
                return findHead(topB);
            }
            return selectBean;
        }
    }

    private DistributedFuncBean findBeanDistributed(
        DistributedFuncBean selectBean) { // Find the parent nodes based on spanid and parenspanid flag c in a and b
        if (chainIdFuncBeanMapA.containsKey(selectBean.getChainId())) { // Find the corresponding bean in a
            DistributedFuncBean findBeanA = chainIdFuncBeanMapA.get(selectBean.getChainId()).stream().filter(
                (item) -> item.getSpanId().equals(selectBean.getSpanId()) && item.getParentSpanId()
                    .equals(selectBean.getParentSpanId()) && item.getFlag().equals("C")).findFirst().orElse(null);
            if (findBeanA == null) {
                if (chainIdFuncBeanMapB.containsKey(selectBean.getChainId())) { // The corresponding bean was found in b
                    DistributedFuncBean findBeanB = chainIdFuncBeanMapB.get(selectBean.getChainId()).stream().filter(
                        (item) -> item.getSpanId().equals(selectBean.getSpanId()) && item.getParentSpanId()
                            .equals(selectBean.getParentSpanId()) && item.getFlag().equals("C")).findFirst()
                        .orElse(null);
                    if (findBeanB == null) { // Data outage
                        return null;
                    }
                    return findBeanB;
                }
            } else {
                return findBeanA;
            }
        }
        return null;
    }

    private List<DefaultMutableTreeNode> getTreeNodes(DistributedFuncBean headBean) {
        List<DefaultMutableTreeNode> nodes = new ArrayList<>();
        List<DistributedFuncBean> list = new ArrayList<>();
        headBean.setDelay(0L); // The head node delay is 0
        headToEndBean(headBean, list);
        AtomicInteger index = new AtomicInteger(0); // The subscript for the loop
        list.forEach((item) -> {
            if (index.get() != 0) {
                item.setDelay(item.getStartTs() - list.get(index.get() - 1).getStartTs());
            }
            index.getAndAdd(1);
        });
        DetailBean middleBean = DistributedDataPraser.collectByName(list);
        Map<Integer, DefaultMutableTreeNode> funcBeanMap =
            list.stream().collect(Collectors.toMap(DistributedFuncBean::getId, (bean) -> {
                DetailBean detailBean = new DetailBean();
                detailBean.mergeFuncBean(bean);
                detailBean.mergeDetailcBean(middleBean);
                return new DefaultMutableTreeNode(detailBean);
            }));

        list.forEach((item) -> {
            DefaultMutableTreeNode node = funcBeanMap.get(item.getId());
            if (item.getParentId() == 0) {
                DetailBean detailBean = new DetailBean();
                detailBean.setName(getPackageName(item.getCurrentType()));
                detailBean.setCurrentType(item.getCurrentType());
                DefaultMutableTreeNode parentRootNode = new DefaultMutableTreeNode(detailBean);
                parentRootNode.add(node);
                nodes.add(parentRootNode);
            } else {
                if (funcBeanMap.containsKey(item.getParentId())) {
                    funcBeanMap.get(item.getParentId()).add(node);
                }
            }
        });
        return nodes;
    }

    private String getPackageName(DistributedFuncBean.BeanDataType type) {
        if (type.equals(DistributedFuncBean.BeanDataType.TYPE_A)) {
            return DistributedCache.getDistribuetedParams().getPkgNameA() + "(" + DistributedCache
                .getDistribuetedParams().getDeviceNameA() + ")";
        } else {
            return DistributedCache.getDistribuetedParams().getPkgNameB() + "(" + DistributedCache
                .getDistribuetedParams().getDeviceNameB() + ")";
        }
    }

    private void headToEndBean(DistributedFuncBean headBean,
        List<DistributedFuncBean> list) { // Find all the tree nodes from top to bottom
        list.add(headBean);
        if (headBean.getCurrentType().equals(DistributedFuncBean.BeanDataType.TYPE_A)) {
            if (parentIdFuncBeanMapA.containsKey(headBean.getId())) {
                forEachToMap(parentIdFuncBeanMapA, headBean.getId(), list);
            } else {
                parentIdFuncNotFound(headBean, list);
            }
        } else if (headBean.getCurrentType().equals(DistributedFuncBean.BeanDataType.TYPE_B)) {
            if (parentIdFuncBeanMapB.containsKey(headBean.getId())) {
                forEachToMap(parentIdFuncBeanMapB, headBean.getId(), list);
            } else {
                if (headBean.getFlag().equals("C")) {
                    toNextThreadFunc(headBean, list);
                } else if (headBean.getFlag().equals("S")) {
                    DistributedFuncBean childSBeanB = chainIdFuncBeanMapB.get(headBean.getChainId()).stream()
                        .filter((item) -> item.getParentSpanId() == headBean.getSpanId()).findFirst().orElse(null);
                    if (childSBeanB != null) {
                        headToEndBean(childSBeanB, list);
                    }
                } else if (headBean.getParentId() == 0 && headBean.getParentSpanId() == headBean.getSpanId()
                    && !headBean.getChainId().isEmpty()) { // Flag is an empty head node
                    DistributedFuncBean childBeanB = chainIdFuncBeanMapB.get(headBean.getChainId()).stream()
                        .filter((item) -> item.getParentSpanId() == headBean.getSpanId() && item.getFlag().equals("C"))
                        .findFirst().orElse(null);
                    if (childBeanB != null) {
                        headToEndBean(childBeanB, list);
                    }
                } else {
                    if (ProfilerLogManager.isDebugEnabled()) {
                        LOGGER.debug("headBean status error");
                    }
                }
            }
        } else {
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("CurrentType error");
            }
        }
    }

    private void parentIdFuncNotFound(DistributedFuncBean headBean, List<DistributedFuncBean> list) {
        if (headBean.getFlag().equals("C")) {
            toNextThreadFunc(headBean, list);
        } else if (headBean.getFlag().equals("S")) {
            DistributedFuncBean childSBeanA = chainIdFuncBeanMapA.get(headBean.getChainId()).stream()
                .filter((item) -> item.getParentSpanId() == headBean.getSpanId()).findFirst().orElse(null);
            if (childSBeanA != null) {
                headToEndBean(childSBeanA, list);
            }
        } else if (headBean.getParentId() == 0 && headBean.getParentSpanId() == headBean.getSpanId()
            && !headBean.getChainId().isEmpty()) { // Flag is an empty head node
            DistributedFuncBean childBeanA = chainIdFuncBeanMapA.get(headBean.getChainId()).stream()
                .filter((item) -> item.getParentSpanId() == headBean.getSpanId() && item.getFlag().equals("C"))
                .findFirst().orElse(null);
            if (childBeanA != null) {
                headToEndBean(childBeanA, list);
            }
        } else {
            if (ProfilerLogManager.isDebugEnabled()) {
                LOGGER.debug("headBean status error");
            }
        }
    }

    private void forEachToMap(Map<Integer, List<DistributedFuncBean>> map, Integer id, List<DistributedFuncBean> list) {
        map.get(id).forEach((item) -> {
            headToEndBean(item, list);
        });
    }

    private void supplementParentBean(DistributedFuncBean headBean, List<DistributedFuncBean> list) {

        // When C goes to the S of another thread,
        // the next thread method may appear that is not the top-of-stack method and needs to find the head
        if (headBean.getDepth() != 0) {
            DistributedFuncBean parentBean;
            if (headBean.getCurrentType().equals(DistributedFuncBean.BeanDataType.TYPE_A)) {
                parentBean = DistributedCache.ID_FUNC_BEAN_MAP_A.get(headBean.getParentId());
            } else {
                parentBean = DistributedCache.ID_FUNC_BEAN_MAP_B.get(headBean.getParentId());
            }
            list.add(parentBean);
            supplementParentBean(parentBean, list);
        }
    }

    private void toNextThreadFunc(DistributedFuncBean headBean, List<DistributedFuncBean> list) {
        if (headBean.getChainId() == null || headBean.getChainId().isEmpty()) {
            return;
        }
        DistributedFuncBean first = chainIdFuncBeanMapA.get(headBean.getChainId()).stream().filter(
            (item) -> item.getSpanId().equals(headBean.getSpanId()) && item.getParentSpanId()
                .equals(headBean.getParentSpanId()) && item.getFlag().equals("S")).findFirst().orElse(null);
        if (first != null) {
            supplementParentBean(first, list);
            headToEndBean(first, list);
        } else {
            chainIdFuncBeanMapB.get(headBean.getChainId()).stream().filter(
                (item) -> item.getSpanId().equals(headBean.getSpanId()) && item.getParentSpanId()
                    .equals(headBean.getParentSpanId()) && item.getFlag().equals("S")).findFirst()
                .ifPresent(startBean -> {
                    supplementParentBean(startBean, list);
                    headToEndBean(startBean, list);
                });
        }
    }

    private class TreeCellRender extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
            JBLabel jbLabel = new JBLabel();
            jbLabel.setBorder(JBUI.Borders.empty(5, 5));
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            DetailBean userObject = (DetailBean) node.getUserObject();
            if (userObject != null) {
                if (node.getParent() != null && node.getParent() instanceof DefaultMutableTreeNode
                    && ((DefaultMutableTreeNode) node.getParent()).isRoot()) {
                    if (userObject.getCurrentType().equals(DistributedFuncBean.BeanDataType.TYPE_A)) {
                        jbLabel.setIcon(usbIcon);
                    } else {
                        jbLabel.setIcon(wifiIcon);
                    }
                }
                if (userObject.getChainId() != null && !userObject.getChainId().isEmpty()) {
                    jbLabel.setText(
                        userObject.getName() + "[" + userObject.getChainId() + "," + userObject.getSpanId() + ","
                            + userObject.getParentSpanId() + "]");
                } else {
                    jbLabel.setText(userObject.getName());
                }
            }
            return jbLabel;
        }
    }

    /**
     * ColorCellRenderer set the cell color from renderer
     */
    public class ColorCellRenderer extends DefaultTableCellRenderer {
        @Override
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
                    LOGGER.debug("TableCellRenderer Type does not match");
                }
            }
            return this;
        }
    }
}
