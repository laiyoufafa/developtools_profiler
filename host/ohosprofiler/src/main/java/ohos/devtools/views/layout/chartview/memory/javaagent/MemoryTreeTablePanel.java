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

package ohos.devtools.views.layout.chartview.memory.javaagent;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.treeStructure.treetable.ListTreeTableModelOnColumns;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.services.memory.agentbean.AgentHeapBean;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.common.customcomp.CustomComboBox;
import ohos.devtools.views.common.treetable.ExpandTreeTable;
import ohos.devtools.views.common.customcomp.CustomJBComboBoxUI;
import ohos.devtools.views.common.customcomp.CustomJBTextField;
import ohos.devtools.views.layout.chartview.memory.MemoryItemView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ohos.devtools.views.common.Constant.IS_DEVELOP_MODE;

/**
 * MemoryTreeTablePanel
 *
 * @since : 2021/10/25
 */
public class MemoryTreeTablePanel extends JBPanel {
    private static final Logger LOGGER = LogManager.getLogger(MemoryTreeTablePanel.class);
    private static final int FEATURES_WIDTH = 1520;

    private static final int NUM_5 = 5;
    private static final int NUM_8 = 8;

    private MemoryAgentHeapInfoPanel memoryAgentHeapInfoPanel;

    /**
     * MemoryTreeTablePanel
     *
     * @param memoryItemView memoryItemView
     * @param sessionId sessionId
     * @param chartName chartName
     */
    public MemoryTreeTablePanel(MemoryItemView memoryItemView, long sessionId, String chartName) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("MemoryTreeTablePanel");
        }
        this.setOpaque(true);
        this.setLayout(new BorderLayout());
        // leftTab
        JBLabel leftTab = new JBLabel("Table");
        leftTab.setBorder(BorderFactory
            .createEmptyBorder(LayoutConstants.NUM_2, LayoutConstants.RECORD_BORDER_SPACE, LayoutConstants.NUM_2, 0));
        leftTab.setOpaque(false);
        leftTab.setPreferredSize(new Dimension(LayoutConstants.JPA_LABEL_WIDTH, LayoutConstants.DEVICES_HEIGHT));
        Font font = new Font(Font.DIALOG, Font.PLAIN, LayoutConstants.FONT_SIZE);
        leftTab.setFont(font);
        leftTab.setBounds(LayoutConstants.RECORD_BORDER_SPACE, LayoutConstants.NUM_2,
            LayoutConstants.RECORD_TABBED_BOUNDS_WIDTH, LayoutConstants.NUM_20);
        // TAB
        JBTabbedPane jbTabbedPane = new JBTabbedPane();
        jbTabbedPane.setBorder(
            BorderFactory.createEmptyBorder(LayoutConstants.NUM_2, LayoutConstants.NUM_20, LayoutConstants.NUM_2, 0));
        // tableTab
        JBPanel tableTab = createTableTab(memoryItemView, sessionId, chartName);
        tableTab.setBorder(BorderFactory.createEmptyBorder(NUM_8, 0, NUM_8, 0));
        // Custom jbTabbedPane
        jbTabbedPane.addTab("", tableTab);
        jbTabbedPane.setTabComponentAt(jbTabbedPane.indexOfComponent(tableTab), leftTab);
        // rightTab
        JBLabel rightTab = new JBLabel("Visualization");
        rightTab.setBorder(BorderFactory
            .createEmptyBorder(LayoutConstants.NUM_2, LayoutConstants.RECORD_TABBED_BORDER, LayoutConstants.NUM_2, 0));
        rightTab.setOpaque(false);
        rightTab.setPreferredSize(new Dimension(LayoutConstants.JPA_LABEL_WIDTH, LayoutConstants.DEVICES_HEIGHT));
        rightTab.setFont(font);
        rightTab.setBounds(LayoutConstants.RECORD_TABBED_BOUNDS_POINT, LayoutConstants.NUM_2,
            LayoutConstants.RECORD_TABBED_BOUNDS_WIDTH, LayoutConstants.NUM_20);
        // viewTab
        JBPanel viewTab = createViewTab();
        viewTab.setBorder(BorderFactory.createEmptyBorder(NUM_8, 0, NUM_8, 0));
        // Custom jbTabbedPane
        if (IS_DEVELOP_MODE) {
            jbTabbedPane.addTab("", viewTab);
            jbTabbedPane.setTabComponentAt(jbTabbedPane.indexOfComponent(viewTab), rightTab);
        }
        this.add(jbTabbedPane, BorderLayout.CENTER);
        // Set the label panel changeListener
        jbTabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent event) {
            }
        });
    }

    private JBPanel createTableTab(MemoryItemView memoryItemView, long sessionId, String chartName) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createTableTab");
        }
        JBPanel tablePanel = new JBPanel(new MigLayout("insets 0"));
        tablePanel.setBorder(BorderFactory.createEmptyBorder(NUM_5, 0, NUM_5, 0));
        tablePanel.setOpaque(true);

        // itemViewBox
        List<String> itemViewBox = new ArrayList<>();
        itemViewBox.add("View app heaps");

        // itemViewBox
        List<String> itemArraysBox = new ArrayList<>();
        itemArraysBox.add("Arrange by class");

        // Reserved Tree Table panel
        memoryAgentHeapInfoPanel = new MemoryAgentHeapInfoPanel(memoryItemView, sessionId, chartName);
        memoryAgentHeapInfoPanel.setOpaque(true);
        memoryAgentHeapInfoPanel.setBackground(JBColor.background().brighter());

        // add to table Tab Options panel
        tablePanel.add(createHeapFeatures(itemViewBox, itemArraysBox), "wrap");
        tablePanel.add(memoryAgentHeapInfoPanel, "dock south");

        return tablePanel;
    }

    private JBPanel createViewTab() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createViewTab");
        }
        JBPanel viewPanel = new JBPanel(new BorderLayout());
        viewPanel.setBorder(BorderFactory.createEmptyBorder(NUM_5, 0, NUM_5, 0));
        viewPanel.setOpaque(true);

        List<String> itemBox = new ArrayList<>();
        itemBox.add("View all heaps");
        itemBox.add("View app heaps");
        itemBox.add("View zygote heaps");
        itemBox.add("View image heaps");
        itemBox.add("View JNI heaps");

        // Reserved Tree Table panel
        JBPanel recordTable = new JBPanel();
        recordTable.setOpaque(true);
        recordTable.setBorder(BorderFactory.createEmptyBorder(NUM_5, 0, 0, 0));

        // add to table Tab Options panel
        viewPanel.add(createHeapFeatures(itemBox, itemBox), BorderLayout.NORTH);
        viewPanel.add(recordTable, BorderLayout.CENTER);

        return viewPanel;
    }

    private JBPanel createHeapFeatures(List<String> itemViewBox, List<String> itemArrangeBox) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createHeapFeatures");
        }
        // tableFeatures
        JBPanel tableFeatures = new JBPanel();
        tableFeatures.setOpaque(false);
        tableFeatures.setPreferredSize(new Dimension(FEATURES_WIDTH, LayoutConstants.RECORD_FEATURES_HEIGHT));
        tableFeatures.setBackground(JBColor.background().darker());
        tableFeatures.setBorder(BorderFactory.createEmptyBorder(NUM_8, 0, NUM_8, 0));
        tableFeatures.setLayout(new MigLayout("insets 0"));
        tableFeatures.setBorder(BorderFactory.createEmptyBorder(NUM_8, 0, NUM_8, 0));

        // tableFeatures add itemViewBox
        CustomComboBox viewBox = new CustomComboBox();
        viewBox.setName(UtConstant.UT_MEMORY_TREE_TABLE_VIEW);
        viewBox.setUI(new CustomJBComboBoxUI());
        viewBox.setBorder(BorderFactory.createLineBorder(ColorConstants.NATIVE_RECORD_BORDER, 1));
        viewBox.setPreferredSize(
            new Dimension(LayoutConstants.RECORD_COMBO_BOX_WIDTH, LayoutConstants.RECORD_SEARCH_HEIGHT));
        for (String item : itemViewBox) {
            viewBox.addItem(item);
        }
        viewBox.setSelectedIndex(0);
        tableFeatures.add(viewBox);

        // tableFeatures add itemArrangeBox
        CustomComboBox arrangeBox = new CustomComboBox();
        arrangeBox.setName(UtConstant.UT_MEMORY_TREE_TABLE_ARRANGE);
        arrangeBox.setUI(new CustomJBComboBoxUI());
        arrangeBox.setBorder(BorderFactory.createLineBorder(ColorConstants.NATIVE_RECORD_BORDER, 1));
        arrangeBox.setPreferredSize(
            new Dimension(LayoutConstants.RECORD_COMBO_BOX_WIDTH, LayoutConstants.RECORD_SEARCH_HEIGHT));
        for (String item : itemArrangeBox) {
            arrangeBox.addItem(item);
        }
        arrangeBox.setSelectedIndex(0);
        tableFeatures.add(arrangeBox);

        JBPanel searchPanel = new JBPanel();
        searchPanel.setOpaque(true);
        searchPanel.setLayout(new BorderLayout());

        // tableFeatures Search
        CustomJBTextField search = new CustomJBTextField();
        search.setBorder(BorderFactory.createLineBorder(ColorConstants.NATIVE_RECORD_BORDER, 1));
        search.setText("Search");
        search
            .setPreferredSize(new Dimension(LayoutConstants.RECORD_SEARCH_WIDTH, LayoutConstants.RECORD_SEARCH_HEIGHT));
        addSearchListener(search);
        searchPanel.add(search, BorderLayout.WEST);

        tableFeatures.add(searchPanel, "wrap");
        return tableFeatures;
    }

    private void addSearchListener(CustomJBTextField search) {
        search.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                if ("Search".equals(search.getText())) {
                    search.setText("");
                    memoryAgentHeapInfoPanel.getTreeTable().getVerticalScrollBar()
                        .removeMouseMotionListener(memoryAgentHeapInfoPanel.getMouseMotionAdapter());
                }
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                if (search.getText().length() < 1) {
                    search.setText("Search");
                    memoryAgentHeapInfoPanel.getTreeTable().getVerticalScrollBar()
                        .addMouseMotionListener(memoryAgentHeapInfoPanel.getMouseMotionAdapter());
                }
            }
        });
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                searchInsertUpdate(search);
            }
            @Override
            public void removeUpdate(DocumentEvent event) {
                // search
                searchRemoveUpdate(search);
            }

            /**
             * Gives notification that an attribute or set of attributes changed.
             *
             * @param documentEvent the document event
             */
            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
            }
        });
    }

    /**
     * searchRemoveUpdate
     *
     * @param search search
     */
    public void searchRemoveUpdate(CustomJBTextField search) {
        String text = search.getText();
        ExpandTreeTable treeTable = memoryAgentHeapInfoPanel.getTreeTable();
        TreeTableModel model = treeTable.getModel();
        if (model instanceof ListTreeTableModelOnColumns) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ListTreeTableModelOnColumns tableModel = (ListTreeTableModelOnColumns) model;
                    DefaultMutableTreeNode root = null;
                    Object tableModelRoot = tableModel.getRoot();
                    if (tableModelRoot instanceof DefaultMutableTreeNode) {
                        root = (DefaultMutableTreeNode) tableModelRoot;
                        List<AgentHeapBean> datas = memoryAgentHeapInfoPanel.getAllAgentDatas().stream()
                                .filter(agentDataNode -> agentDataNode.getAgentClazzName().contains(text))
                                .collect(Collectors.toList());
                        root.removeAllChildren();
                        int totalAllocations = 0;
                        int totalDeallocations = 0;
                        int totalTotalCount = 0;
                        long totalShallowSize = 0L;
                        for (AgentHeapBean agentHeapBean : datas) {
                            totalAllocations = totalAllocations + agentHeapBean.getAgentAllocationsCount();
                            totalDeallocations =
                                    totalDeallocations + agentHeapBean.getAgentDeAllocationsCount();
                            totalTotalCount = totalTotalCount + agentHeapBean.getAgentTotalInstanceCount();
                            totalShallowSize = totalShallowSize + agentHeapBean.getAgentTotalshallowSize();
                            root.add(new DefaultMutableTreeNode(agentHeapBean));
                        }
                        AgentHeapBean userObject = null;
                        Object rootUserObject = root.getUserObject();
                        if (rootUserObject instanceof AgentHeapBean) {
                            userObject = (AgentHeapBean) rootUserObject;
                            userObject.setAgentAllocationsCount(totalAllocations);
                            userObject.setAgentDeAllocationsCount(totalDeallocations);
                            userObject.setAgentTotalInstanceCount(totalTotalCount);
                            userObject.setAgentTotalshallowSize(totalShallowSize);
                            root.setUserObject(userObject);
                        }
                    }
                    tableModel.reload();
                    memoryAgentHeapInfoPanel.getTreeTable().getVerticalScrollBar().setValue(0);
                }
            });
        }
    }

    /**
     * searchInsertUpdate
     *
     * @param search search
     */
    public void searchInsertUpdate(CustomJBTextField search) {
        String text = search.getText();
        ExpandTreeTable treeTable = memoryAgentHeapInfoPanel.getTreeTable();
        TreeTableModel model = treeTable.getModel();
        if (model instanceof ListTreeTableModelOnColumns) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ListTreeTableModelOnColumns tableModel = (ListTreeTableModelOnColumns) model;
                    DefaultMutableTreeNode root = null;
                    Object tableModelRoot = tableModel.getRoot();
                    if (tableModelRoot instanceof DefaultMutableTreeNode) {
                        root = (DefaultMutableTreeNode) tableModelRoot;
                        List<AgentHeapBean> datas = memoryAgentHeapInfoPanel.getAllAgentDatas().stream()
                                .filter(agentDataNode -> agentDataNode.getAgentClazzName().contains(text))
                                .collect(Collectors.toList());
                        root.removeAllChildren();
                        int totalAllocations = 0;
                        int totalDeallocations = 0;
                        int totalTotalCount = 0;
                        long totalShallowSize = 0L;
                        for (AgentHeapBean agentHeapBean : datas) {
                            totalAllocations = totalAllocations + agentHeapBean.getAgentAllocationsCount();
                            totalDeallocations =
                                    totalDeallocations + agentHeapBean.getAgentDeAllocationsCount();
                            totalTotalCount = totalTotalCount + agentHeapBean.getAgentTotalInstanceCount();
                            totalShallowSize = totalShallowSize + agentHeapBean.getAgentTotalshallowSize();
                            root.add(new DefaultMutableTreeNode(agentHeapBean));
                        }
                        AgentHeapBean userObject = null;
                        Object rootUserObject = root.getUserObject();
                        if (rootUserObject instanceof AgentHeapBean) {
                            userObject = (AgentHeapBean) rootUserObject;
                            userObject.setAgentAllocationsCount(totalAllocations);
                            userObject.setAgentDeAllocationsCount(totalDeallocations);
                            userObject.setAgentTotalInstanceCount(totalTotalCount);
                            userObject.setAgentTotalshallowSize(totalShallowSize);
                            root.setUserObject(userObject);
                        }
                    }
                    tableModel.reload();
                    memoryAgentHeapInfoPanel.getTreeTable().getVerticalScrollBar().setValue(0);
                }
            });
        }
    }

    public MemoryAgentHeapInfoPanel getMemoryAgentHeapInfoPanel() {
        return memoryAgentHeapInfoPanel;
    }
}
