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

package ohos.devtools.views.layout.chartview.memory.nativehook;

import com.intellij.ui.components.JBPanel;
import com.intellij.ui.treeStructure.treetable.TreeColumnInfo;
import com.intellij.util.ui.ColumnInfo;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.services.memory.nativebean.HookDataBean;
import ohos.devtools.services.memory.nativebean.NativeFrame;
import ohos.devtools.services.memory.nativebean.NativeInstanceObject;
import ohos.devtools.services.memory.nativeservice.NativeDataExternalInterface;
import ohos.devtools.views.common.treetable.TreeTableColumn;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.SortOrder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import static javax.swing.tree.DefaultMutableTreeNode.EMPTY_ENUMERATION;
import static ohos.devtools.views.layout.chartview.memory.nativehook.NativeHookTreeTableRenderer.HookDataBeanEnum.CALLSTACK_ENUM;
import static ohos.devtools.views.layout.chartview.memory.nativehook.NativeHookTreeTableRenderer.HookDataBeanEnum.HEAP_ENUM;
import static ohos.devtools.views.layout.chartview.memory.nativehook.NativeHookTreeTableRenderer.HookDataBeanEnum.MALLOC_ENUM;

/**
 * NativeHookTreeTablePanel
 *
 * @since 2021/10/25
 */
public class NativeHookTreeTablePanel extends JBPanel {
    private static final Logger LOGGER = LogManager.getLogger(NativeHookTreeTablePanel.class);

    /**
     * columns
     */
    public final ColumnInfo[] columns = new ColumnInfo[] {new TreeColumnInfo("Allocation function"),
        new TreeTableColumn<>("ModuleName", HookDataBean.class) {
            @Override
            public String getColumnValue(HookDataBean nodeData) {
                return nodeData.getHookModuleName();
            }
        }, new TreeTableColumn<>("Allocations", HookDataBean.class) {
        @Override
        public String getColumnValue(HookDataBean nodeData) {
            return String.valueOf(nodeData.getHookAllocationCount());
        }
    }, new TreeTableColumn<>("Deallocations", HookDataBean.class) {
        @Override
        public String getColumnValue(HookDataBean nodeData) {
            return String.valueOf(nodeData.getHookDeAllocationCount());
        }
    }, new TreeTableColumn<>("Allocations Size", HookDataBean.class) {
        @Override
        public String getColumnValue(HookDataBean nodeData) {
            return String.valueOf(nodeData.getHookAllocationMemorySize());
        }
    }, new TreeTableColumn<>("Deallocations Size", HookDataBean.class) {
        @Override
        public String getColumnValue(HookDataBean nodeData) {
            return String.valueOf(nodeData.getHookDeAllocationMemorySize());
        }
    }, new TreeTableColumn<>("Total Count", HookDataBean.class) {
        @Override
        public String getColumnValue(HookDataBean nodeData) {
            return String.valueOf(nodeData.getTotalCount());
        }
    }, new TreeTableColumn<>("Remainning Size", HookDataBean.class) {
        @Override
        public String getColumnValue(HookDataBean nodeData) {
            return String.valueOf(nodeData.getReaminSize());
        }
    }};

    private NativeDataExternalInterface dataInterface;
    private DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
    private NativeHookTreeTable treeTable;
    private MultiValueMap nativeData;
    private int currentSortKey = 1;
    private SortOrder currentOrder = SortOrder.DESCENDING;
    private NativeHookTreeTableModel tableModelOnColumns;
    private String searchText = "";

    /**
     * NativeHookTreeTablePanel
     *
     * @param sessionId sessionId
     * @param dataInterface dataInterface
     */
    public NativeHookTreeTablePanel(long sessionId, NativeDataExternalInterface dataInterface) {
        this.dataInterface = dataInterface;
        setLayout(new BorderLayout());
        root = arrangeAllocationMethodDataNode();
        createTreeTable(root);
        add(treeTable, BorderLayout.CENTER);
    }

    /**
     * arrange CallStackDataNode
     *
     * @return DefaultMutableTreeNode
     */
    public DefaultMutableTreeNode arrangeCallStackDataNode() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("arrangeCallStackDataNode");
        }
        if (nativeData == null || nativeData.size() == 0) {
            nativeData = dataInterface.getNativeInstanceMap();
        }
        DefaultMutableTreeNode appNode = new DefaultMutableTreeNode();
        Set<String> endCallNamesSet = nativeData.keySet();
        int totalAllocations = 0;
        int totalDeallocations = 0;
        long totalAllocationsSize = 0L;
        long totalDeallocationsSize = 0L;
        for (String endCallName : endCallNamesSet) {
            Collection collections = nativeData.getCollection(endCallName);
            Iterator iterator = collections.iterator();
            while (iterator.hasNext()) {
                NativeInstanceObject nativeInstance = null;
                Object nextObject = iterator.next();
                if (nextObject instanceof NativeInstanceObject) {
                    nativeInstance = (NativeInstanceObject) nextObject;
                    totalAllocations += nativeInstance.getInstanceCount();
                    totalAllocationsSize += nativeInstance.getAllowSize();
                    if (nativeInstance.isDeAllocated()) {
                        totalDeallocations += nativeInstance.getInstanceCount();
                        totalDeallocationsSize += nativeInstance.getAllowSize();
                    }
                    createTreeNode(nativeInstance, appNode);
                }
            }
        }
        HookDataBean dataNode = new HookDataBean();
        dataNode.setHookMethodName("Native heap");
        dataNode.setHookAllocationCount(totalAllocations);
        dataNode.setHookDeAllocationCount(totalDeallocations);
        dataNode.setHookAllocationMemorySize(totalAllocationsSize);
        dataNode.setHookDeAllocationMemorySize(totalDeallocationsSize);
        dataNode.setBeanEnum(HEAP_ENUM);
        appNode.setUserObject(dataNode);
        return appNode;
    }

    private void createTreeNode(NativeInstanceObject nativeInstance, DefaultMutableTreeNode root) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createTreeNode");
        }
        Enumeration<TreeNode> children = root.children();
        ArrayList<NativeFrame> nativeFrames = nativeInstance.getNativeFrames();
        ArrayList<NativeFrame> dest = new ArrayList<>(nativeFrames);
        Collections.reverse(dest);
        if (children == EMPTY_ENUMERATION) {
            DefaultMutableTreeNode nodeZero = crateNode(nativeInstance, dest, 0);
            root.add(nodeZero);
            HookDataBean hookDataBean;
            Object userObjectNew = nodeZero.getUserObject();
            if (userObjectNew instanceof HookDataBean) {
                hookDataBean = (HookDataBean) userObjectNew;
                HookDataBean rootNode;
                Object object = root.getUserObject();
                if (Objects.isNull(object)) {
                    rootNode = new HookDataBean();
                    rootNode.setHookAllocationCount(hookDataBean.getHookAllocationCount());
                    rootNode.setHookAllocationMemorySize(hookDataBean.getHookAllocationMemorySize());
                    rootNode.setHookDeAllocationCount(hookDataBean.getHookDeAllocationCount());
                    rootNode.setHookDeAllocationMemorySize(hookDataBean.getHookDeAllocationMemorySize());
                    root.setUserObject(rootNode);
                }
            }
        } else {
            insertNodeInTreeNode(nativeInstance, dest, root, 0);
        }
    }

    private void insertNodeInTreeNode(NativeInstanceObject nativeInstance, ArrayList<NativeFrame> nativeFrames,
        DefaultMutableTreeNode parentNode, int startIndex) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertNodeInTreeNode");
        }
        Enumeration<TreeNode> children = parentNode.children();
        boolean insert = false;
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child = null;
            Object elementObject = children.nextElement();
            if (elementObject instanceof DefaultMutableTreeNode) {
                child = (DefaultMutableTreeNode) elementObject;
                HookDataBean userObject = null;
                Object object = child.getUserObject();
                if (object instanceof HookDataBean) {
                    userObject = (HookDataBean) object;
                }
                if (userObject != null && userObject.getHookMethodName()
                    .equals(nativeFrames.get(startIndex).getFunctionName().trim())) {
                    int index = startIndex + 1;
                    if (userObject.getBeanEnum() == MALLOC_ENUM && nativeFrames.size() == index) {
                        HookDataBean newUserObject = new HookDataBean();
                        newUserObject.setBeanEnum(userObject.getBeanEnum());
                        newUserObject.setHookMethodName(userObject.getHookMethodName());
                        newUserObject.setHookModuleName(userObject.getHookModuleName());
                        newUserObject.setHookAllocationMemorySize(
                            userObject.getHookAllocationMemorySize() + nativeInstance.getSize());
                        newUserObject.setHookAllocationCount(
                            userObject.getHookAllocationCount() + nativeInstance.getInstanceCount());
                        if (nativeInstance.isDeAllocated()) {
                            newUserObject.setHookDeAllocationCount(
                                userObject.getHookDeAllocationCount() + nativeInstance.getInstanceCount());
                            newUserObject.setHookDeAllocationMemorySize(
                                userObject.getHookDeAllocationMemorySize() + nativeInstance.getSize());
                        } else {
                            newUserObject.setHookDeAllocationCount(userObject.getHookDeAllocationCount());
                            newUserObject.setHookDeAllocationMemorySize(userObject.getHookDeAllocationMemorySize());
                        }
                        child.setUserObject(newUserObject);
                        updateParentByAddInstance(child, nativeInstance);
                    } else {
                        insertNodeInTreeNode(nativeInstance, nativeFrames, child, index);
                    }
                    return;
                } else {
                    insert = true;
                }
            }
        }
        addNewNodeAndUpdateParentObject(nativeInstance, nativeFrames, parentNode, startIndex, insert);
    }

    /**
     * doUpdateParentUserObject
     *
     * @param nativeInstance nativeInstance
     * @param nativeFrames nativeFrames
     * @param parentNode parentNode
     * @param startIndex startIndex
     * @param insert insert
     */
    private void addNewNodeAndUpdateParentObject(NativeInstanceObject nativeInstance,
        ArrayList<NativeFrame> nativeFrames, DefaultMutableTreeNode parentNode, int startIndex, boolean insert) {
        if (!insert) {
            return;
        }
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addNewNodeAndUpdateParentObject");
        }
        DefaultMutableTreeNode childNode = crateNode(nativeInstance, nativeFrames, startIndex);
        HookDataBean childNodeUserObject = null;
        Object userObjectNew = childNode.getUserObject();
        if (userObjectNew instanceof HookDataBean) {
            childNodeUserObject = (HookDataBean) userObjectNew;
        }
        HookDataBean parNode = null;
        Object parNodeObject = parentNode.getUserObject();
        if (parNodeObject instanceof HookDataBean) {
            parNode = (HookDataBean) parNodeObject;
        }
        HookDataBean newParentNode = new HookDataBean();
        if (parNode != null && childNodeUserObject != null) {
            newParentNode.setHookMethodName(parNode.getHookMethodName());
            newParentNode.setHookModuleName(parNode.getHookModuleName());
            newParentNode.setHookAllocationCount(
                parNode.getHookAllocationCount() + childNodeUserObject.getHookAllocationCount());
            newParentNode.setHookAllocationMemorySize(
                parNode.getHookAllocationMemorySize() + childNodeUserObject.getHookAllocationMemorySize());
            newParentNode.setHookDeAllocationCount(
                parNode.getHookDeAllocationCount() + childNodeUserObject.getHookDeAllocationCount());
            newParentNode.setHookDeAllocationMemorySize(
                parNode.getHookDeAllocationMemorySize() + childNodeUserObject.getHookDeAllocationMemorySize());
            newParentNode.setBeanEnum(parNode.getBeanEnum());
            parentNode.setUserObject(newParentNode);
            parentNode.add(childNode);
            updateParentUserObject(parentNode, childNodeUserObject);
        }
    }

    private void updateParentUserObject(DefaultMutableTreeNode parentNode, HookDataBean childNodeUserObject) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("updateParentUserObject");
        }
        if (parentNode.isRoot()) {
            return;
        }
        DefaultMutableTreeNode parent = null;
        Object object = parentNode.getParent();
        if (object instanceof DefaultMutableTreeNode) {
            parent = (DefaultMutableTreeNode) object;
            HookDataBean parNode = null;
            Object parentUserObject = parent.getUserObject();
            if (parentUserObject instanceof HookDataBean) {
                parNode = (HookDataBean) parentUserObject;
                parNode.setHookAllocationCount(
                    parNode.getHookAllocationCount() + childNodeUserObject.getHookAllocationCount());
                parNode.setHookAllocationMemorySize(
                    parNode.getHookAllocationMemorySize() + childNodeUserObject.getHookAllocationMemorySize());
                parNode.setHookDeAllocationCount(
                    parNode.getHookDeAllocationCount() + childNodeUserObject.getHookDeAllocationCount());
                parNode.setHookDeAllocationMemorySize(
                    parNode.getHookDeAllocationMemorySize() + childNodeUserObject.getHookDeAllocationMemorySize());
                parent.setUserObject(parNode);
                updateParentUserObject(parent, childNodeUserObject);
            }
        }
    }

    private void updateParentByAddInstance(DefaultMutableTreeNode parentNode, NativeInstanceObject nativeInstance) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("updateParentByAddInstance");
        }
        if (parentNode.isRoot()) {
            return;
        }
        DefaultMutableTreeNode parent = null;
        Object object = parentNode.getParent();
        if (object instanceof DefaultMutableTreeNode) {
            parent = (DefaultMutableTreeNode) object;
            HookDataBean parNode = null;
            Object parentUserObject = parent.getUserObject();
            if (parentUserObject instanceof HookDataBean) {
                parNode = (HookDataBean) parentUserObject;
                parNode.setHookAllocationCount(parNode.getHookAllocationCount() + nativeInstance.getInstanceCount());
                parNode.setHookAllocationMemorySize(parNode.getHookAllocationMemorySize() + nativeInstance.getSize());
                if (nativeInstance.isDeAllocated()) {
                    parNode.setHookDeAllocationCount(
                        parNode.getHookDeAllocationCount() + nativeInstance.getInstanceCount());
                    parNode.setHookDeAllocationMemorySize(
                        parNode.getHookDeAllocationMemorySize() + nativeInstance.getSize());
                } else {
                    parNode.setHookDeAllocationCount(parNode.getHookDeAllocationCount());
                    parNode.setHookDeAllocationMemorySize(parNode.getHookDeAllocationMemorySize());
                }
                parent.setUserObject(parNode);
                updateParentByAddInstance(parent, nativeInstance);
            }
        }
    }

    private DefaultMutableTreeNode crateNode(NativeInstanceObject nativeInstanceObject,
        ArrayList<NativeFrame> nativeFrames, int startIndex) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("crateNode");
        }
        NativeFrame nativeFrame = nativeFrames.get(startIndex);
        HookDataBean hookDataBean = new HookDataBean();
        hookDataBean.setHookMethodName(nativeFrame.getFunctionName());
        hookDataBean.setHookModuleName(nativeFrame.getFileName());
        hookDataBean.setHookAllocationCount(nativeInstanceObject.getInstanceCount());
        hookDataBean.setHookAllocationMemorySize(nativeInstanceObject.getSize());
        if (nativeInstanceObject.isDeAllocated()) {
            hookDataBean.setHookDeAllocationCount(nativeInstanceObject.getInstanceCount());
            hookDataBean.setHookDeAllocationMemorySize(nativeInstanceObject.getSize());
        } else {
            hookDataBean.setHookDeAllocationCount(0);
            hookDataBean.setHookDeAllocationMemorySize(0);
        }
        if (startIndex < (nativeFrames.size() - 1)) {
            hookDataBean.setBeanEnum(CALLSTACK_ENUM);
        } else {
            hookDataBean.setBeanEnum(MALLOC_ENUM);
        }
        DefaultMutableTreeNode endNode = new DefaultMutableTreeNode(hookDataBean);
        if (startIndex < (nativeFrames.size() - 1)) {
            int count = startIndex + 1;
            DefaultMutableTreeNode childNode = crateNode(nativeInstanceObject, nativeFrames, count);
            endNode.add(childNode);
            return endNode;
        }
        return endNode;
    }

    /**
     * DefaultMutableTreeNode
     *
     * @return DefaultMutableTreeNode
     */
    public DefaultMutableTreeNode arrangeAllocationMethodDataNode() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("arrangeAllocationMethodDataNode");
        }
        if (nativeData == null || nativeData.size() == 0) {
            nativeData = dataInterface.getNativeInstanceMap();
        }
        Set<String> endCallNamesSet = nativeData.keySet();
        int totalAllocations = 0;
        int totalDeallocations = 0;
        long totalAllocationsSize = 0L;
        long totalDeallocationsSize = 0L;
        DefaultMutableTreeNode appNode = new DefaultMutableTreeNode();
        for (String endCallName : endCallNamesSet) {
            Collection collections = nativeData.getCollection(endCallName);
            Iterator iterator = collections.iterator();
            int myDeltaAllocations = 0;
            int myDeltaDeallocations = 0;
            long myDeltaAllocationsSize = 0L;
            long myDeltaDeallocationsSize = 0L;
            while (iterator.hasNext()) {
                NativeInstanceObject nativeInstance = null;
                Object object = iterator.next();
                if (object instanceof NativeInstanceObject) {
                    nativeInstance = (NativeInstanceObject) object;
                    myDeltaAllocations += nativeInstance.getInstanceCount();
                    myDeltaAllocationsSize += nativeInstance.getAllowSize();
                    if (nativeInstance.isDeAllocated()) {
                        myDeltaDeallocations += nativeInstance.getInstanceCount();
                        myDeltaDeallocationsSize += nativeInstance.getAllowSize();
                    }
                }
            }
            HookDataBean nativeDataBean = new HookDataBean();
            nativeDataBean.setHookMethodName(endCallName);
            nativeDataBean.setHookAllocationCount(myDeltaAllocations);
            nativeDataBean.setHookAllocationMemorySize(myDeltaAllocationsSize);
            nativeDataBean.setHookDeAllocationCount(myDeltaDeallocations);
            nativeDataBean.setHookDeAllocationMemorySize(myDeltaDeallocationsSize);
            nativeDataBean.setBeanEnum(MALLOC_ENUM);
            appNode.add(new DefaultMutableTreeNode(nativeDataBean));
            totalAllocations += myDeltaAllocations;
            totalDeallocations += myDeltaDeallocations;
            totalAllocationsSize += myDeltaAllocationsSize;
            totalDeallocationsSize += myDeltaDeallocationsSize;
        }
        HookDataBean dataNode =
            getHookDataBean(totalAllocations, totalDeallocations, totalAllocationsSize, totalDeallocationsSize);
        appNode.setUserObject(dataNode);
        return appNode;
    }

    /**
     * getHookDataBean
     *
     * @param totalAllocations totalAllocations
     * @param totalDeallocations totalDeallocations
     * @param totalAllocationsSize totalAllocationsSize
     * @param totalDeallocationsSize totalDeallocationsSize
     * @return HookDataBean
     */
    private HookDataBean getHookDataBean(int totalAllocations, int totalDeallocations, long totalAllocationsSize,
        long totalDeallocationsSize) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getHookDataBean");
        }
        HookDataBean dataNode = new HookDataBean();
        dataNode.setHookMethodName("Native heap");
        dataNode.setHookAllocationCount(totalAllocations);
        dataNode.setHookDeAllocationCount(totalDeallocations);
        dataNode.setHookAllocationMemorySize(totalAllocationsSize);
        dataNode.setHookDeAllocationMemorySize(totalDeallocationsSize);
        dataNode.setBeanEnum(HEAP_ENUM);
        return dataNode;
    }

    /**
     * treeTable
     *
     * @return NativeHookTreeTable
     */
    public NativeHookTreeTable getTreeTable() {
        return treeTable;
    }

    /**
     * handleInsertSearchText
     *
     * @param searchText searchText
     */
    public void handleInsertSearchText(String searchText) {
        this.searchText = searchText;
        getNodeContainSearch(root, searchText);
        tableModelOnColumns.setFiltered(false);
        tableModelOnColumns.reload();
        treeTable.freshTreeRowExpand();
    }

    /**
     * reset nodes
     *
     * @param node node
     */
    private void resetAllNode(DefaultMutableTreeNode node) { // Put all nodes in a healthy state of 0
        Enumeration<TreeNode> enumeration = node.breadthFirstEnumeration();
        while (enumeration.hasMoreElements()) {
            TreeNode treNode = enumeration.nextElement();
            if (treNode instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode) treNode;
                if (nextElement.getUserObject() instanceof HookDataBean) {
                    ((HookDataBean) nextElement.getUserObject()).setContainType(0);
                }
            }
        }
    }

    /**
     * handleRemoveSearchText
     *
     * @param searchText searchText
     */
    public void handleRemoveSearchText(String searchText) {
        this.searchText = searchText;
        if (searchText.isEmpty()) {
            tableModelOnColumns.setFiltered(true);
            resetAllNode(root);
        } else {
            tableModelOnColumns.setFiltered(false);
            getNodeContainSearch(root, searchText);
        }
        tableModelOnColumns.reload();
        treeTable.freshTreeRowExpand();
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
        if (searchText == null || searchText.isEmpty()) {
            return false;
        }
        if (!node.isLeaf()) {
            Enumeration<TreeNode> children = node.children();
            while (children.hasMoreElements()) {
                TreeNode treNode = children.nextElement();
                if (treNode instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode) treNode;
                    if (nextElement.getUserObject() instanceof HookDataBean) {
                        HookDataBean bean = (HookDataBean) nextElement.getUserObject();
                        if (getNodeContainSearch(nextElement, searchText)) {
                            if (!hasKeyWord) {
                                hasKeyWord = true;
                            }
                            bean.setContainType(2);
                            updateContainType(nextElement);
                        } else {
                            bean.setContainType(3);
                        }
                        if (nextElement.getUserObject().toString().contains(searchText)) {
                            hasKeyWord = true;
                            bean.setContainType(1);
                            updateContainType(nextElement);
                        }
                    }
                }
            }
        } else {
            if (node.getUserObject() instanceof HookDataBean) {
                HookDataBean bean = (HookDataBean) node.getUserObject();
                if (bean.getHookMethodName().contains(searchText)) {
                    hasKeyWord = true;
                    bean.setContainType(1);
                } else {
                    bean.setContainType(3);
                }
            }
        }
        return hasKeyWord;
    }

    private static void updateContainType(DefaultMutableTreeNode node) {
        if (node.isLeaf()) {
            Object userObject = node.getUserObject();
            if (userObject instanceof HookDataBean) {
                HookDataBean bean = (HookDataBean) userObject;
                bean.setContainType(2);
            }
        } else {
            Enumeration<TreeNode> children = node.children();
            while (children.hasMoreElements()) {
                TreeNode treNode = children.nextElement();
                if (treNode instanceof DefaultMutableTreeNode) {
                    DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode) treNode;
                    if (nextElement.getUserObject() instanceof HookDataBean) {
                        HookDataBean bean = (HookDataBean) nextElement.getUserObject();
                        bean.setContainType(2);
                        updateContainType(nextElement);
                    }
                }
            }
        }
    }

    /**
     * createTreeTable
     *
     * @param root root
     */
    public void createTreeTable(DefaultMutableTreeNode root) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createTreeTable");
        }
        tableModelOnColumns = new NativeHookTreeTableModel(root, columns);
        treeTable = new NativeHookTreeTable(tableModelOnColumns);
        NativeHookTreeTableRowSorter sorter = new NativeHookTreeTableRowSorter(treeTable.getTable().getModel());
        treeTable.getTree().addTreeWillExpandListener(new TreeWillExpandListener() {
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
        sorter.setListener((columnIndex, sortOrder) -> {
            if (columnIndex <= 0 || columnIndex > columns.length) {
                return;
            }
            currentSortKey = columnIndex;
            currentOrder = sortOrder;
            treeResort(root);
            tableModelOnColumns.reload();
            treeTable.freshTreeExpand();
        });
        treeTable.getTree().setRootVisible(true);
        treeTable.getTree().setExpandsSelectedPaths(true);
        treeTable.getTable().setRowSorter(sorter);
        treeTable.getTree().setCellRenderer(new NativeHookTreeTableRenderer());
        treeTable.getTree().getExpandableItemsHandler().setEnabled(true);

    }

    private void treeResort(DefaultMutableTreeNode node) {
        if (currentOrder == SortOrder.ASCENDING) {
            NativeHookTreeTableRowSorter
                .sortDescTree(node, columns[currentSortKey].getComparator(), treeTable.getTree());
        } else {
            NativeHookTreeTableRowSorter.sortTree(node, columns[currentSortKey].getComparator(), treeTable.getTree());
        }
    }

    /**
     * refreshNativeHookData
     *
     * @param selectItem selectItem
     * @param text text
     */
    public void refreshNativeHookData(String selectItem, String text) {
        if ("Arrange by allocation method".equals(selectItem)) {
            root = arrangeAllocationMethodDataNode();
        } else {
            root = arrangeCallStackDataNode();
        }
        tableModelOnColumns = new NativeHookTreeTableModel(root, columns);
        NativeHookTreeTable heapTreeTable = getTreeTable();
        heapTreeTable.setTreeTableModel(tableModelOnColumns);
        NativeHookTreeTableRowSorter sorter = new NativeHookTreeTableRowSorter(heapTreeTable.getTable().getModel());
        sorter.setListener((columnIndex, sortOrder) -> {
            currentSortKey = columnIndex;
            currentOrder = sortOrder;
            treeResort(root);
            tableModelOnColumns.reload();
            treeTable.freshTreeExpand();
        });
        heapTreeTable.getTable().setRowSorter(sorter);
    }
}
