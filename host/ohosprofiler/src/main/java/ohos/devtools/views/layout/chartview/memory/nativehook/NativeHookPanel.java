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

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.datasources.utils.common.util.CommonUtil;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.session.entity.SessionInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.services.memory.nativeservice.NativeDataExternalInterface;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.customcomp.CustomComboBox;
import ohos.devtools.views.common.customcomp.CustomJBComboBoxUI;
import ohos.devtools.views.common.customcomp.CustomJBTextField;
import ohos.devtools.views.common.customcomp.CustomJLabel;
import ohos.devtools.views.layout.chartview.TaskScenePanelChart;
import ohos.devtools.views.layout.dialog.SampleDialog;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.awt.Image.SCALE_DEFAULT;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_PULL_FILE;
import static ohos.devtools.datasources.transport.hdc.HdcWrapper.conversionCommand;
import static ohos.devtools.views.common.Constant.IS_DEVELOP_MODE;
import static ohos.devtools.views.layout.chartview.utils.ChartViewConstants.LOADING_SIZE;

/**
 * Native Hook Panel
 *
 * @since : 2021/10/25
 */
public class NativeHookPanel extends JBPanel {
    private static final Logger LOGGER = LogManager.getLogger(NativeHookPanel.class);
    private static final int BORDER_TOP = 5;
    private static final int RECORD_BORDER_TOP = 8;
    private static final int HORIZONTAL_GAP = 10;

    private TaskScenePanelChart taskScenePanelChart;
    private NativeHookTreeTablePanel recordTable;

    /**
     * NativeHookPanel
     *
     * @param taskScenePanelChart taskScenePanelChart
     */
    public NativeHookPanel(TaskScenePanelChart taskScenePanelChart) {
        this.taskScenePanelChart = taskScenePanelChart;
    }

    /**
     * load native hook data
     *
     * @param sessionId sessionId
     * @param labelSave labelSave
     * @param isOnline isOnline
     * @param filePath filePath
     */
    public void load(long sessionId, CustomJLabel labelSave, boolean isOnline, String filePath) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("load");
        }
        JBLabel loadingLabel = showLoading(this);
        new SwingWorker<NativeDataExternalInterface, Void>() {
            @Override
            protected NativeDataExternalInterface doInBackground() {
                return getNativeDataExternalInterface(isOnline, sessionId, labelSave, filePath);
            }

            @Override
            protected void done() {
                // Move the result of the time-consuming task to done for processing,
                // and close the rotating waiting box after processing
                NativeDataExternalInterface result = null;
                try {
                    result = get();
                    NativeHookPanel.this.remove(loadingLabel);
                    if (result.getNativeInstanceMap().isEmpty()) {
                        new SampleDialog("prompt", "pull file failed, please try again ").show();
                        return;
                    }
                    NativeHookPanel.this.setLayout(new BorderLayout());
                    JBTabbedPane tabbedPane = getJbTabbedPane(NativeHookPanel.this, sessionId, result);
                    // Set the label panel changeListener
                    tabbedPane.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent event) {
                        }
                    });
                } catch (InterruptedException | ExecutionException exception) {
                    exception.printStackTrace();
                }
            }
        }.execute();
    }

    @Nullable
    private NativeDataExternalInterface getNativeDataExternalInterface(boolean isOnline, long sessionId,
        CustomJLabel labelSave, String filePath) {
        String tmpFilePath;
        if (isOnline) {
            SessionInfo sessionInfo = SessionManager.getInstance().getSessionInfo(sessionId);
            int pid = sessionInfo.getPid();
            String srcFilePath = "/data/local/tmp/" + pid + ".nativehook";
            String targetFile = pid + "_" + CommonUtil.getLocalSessionId() + ".nativehook";
            labelSave.setName(targetFile);
            tmpFilePath = SessionManager.getInstance().tempPath() + targetFile;
            ArrayList cmdStr =
                conversionCommand(HDC_STD_PULL_FILE, sessionInfo.getDeviceIPPortInfo().getDeviceID(),
                    srcFilePath, tmpFilePath);
            String res = HdcWrapper.getInstance().execCmdBy(cmdStr);
            if (!res.contains("FileTransfer finish")) {
                return new NativeDataExternalInterface();
            }
        } else {
            tmpFilePath = filePath;
        }
        NativeDataExternalInterface nativeDataExternalInterface = new NativeDataExternalInterface();
        nativeDataExternalInterface.parseNativeFile(tmpFilePath);
        return nativeDataExternalInterface;
    }

    private JBLabel showLoading(JBPanel panel) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("showLoading");
        }
        SpringLayout spring = new SpringLayout();
        panel.setLayout(spring);
        JBLabel loadingLabel = new JBLabel();
        URL url = TaskScenePanelChart.class.getClassLoader().getResource("/images/loading.gif");
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            icon.setImage(icon.getImage().getScaledInstance(LOADING_SIZE, LOADING_SIZE, SCALE_DEFAULT));
            loadingLabel.setIcon(icon);
        }
        panel.add(loadingLabel);
        // 增加约束，保持Loading图在组件中间
        SpringLayout.Constraints loadingCons = spring.getConstraints(loadingLabel);
        loadingCons.setX(Spring.constant((taskScenePanelChart.getjPanelMiddleRight().getWidth() - LOADING_SIZE) / 2));
        loadingCons.setY(Spring.constant((taskScenePanelChart.getjPanelMiddleRight().getHeight() - LOADING_SIZE) / 2));
        return loadingLabel;
    }

    /**
     * getJbTabbedPane
     *
     * @param nativePanel nativePanel
     * @param sessionId sessionId
     * @param dataInterface dataInterface
     * @return JBTabbedPane
     */
    private JBTabbedPane getJbTabbedPane(JBPanel nativePanel, long sessionId,
        NativeDataExternalInterface dataInterface) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getJbTabbedPane");
        }
        // recordTitle
        JBPanel recordTitle = new JBPanel(new BorderLayout(HORIZONTAL_GAP, 0));
        recordTitle.setBackground(JBColor.background().brighter());
        recordTitle.setOpaque(true);
        recordTitle.setPreferredSize(new Dimension(LayoutConstants.RECORD_TABLE_WIDTH, LayoutConstants.THIRTY_TWO));
        recordTitle.add(getTitleLabel(), BorderLayout.WEST);
        nativePanel.add(recordTitle, BorderLayout.NORTH);
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
        jbTabbedPane
            .setPreferredSize(new Dimension(LayoutConstants.RECORD_TABLE_WIDTH, LayoutConstants.RECORD_TABBED_HEIGHT));
        jbTabbedPane.setBorder(
            BorderFactory.createEmptyBorder(LayoutConstants.NUM_2, LayoutConstants.NUM_20, LayoutConstants.NUM_2, 0));
        // tableTab
        JBPanel tableTab = createTableTab(sessionId, dataInterface);
        tableTab.setBorder(BorderFactory.createEmptyBorder(RECORD_BORDER_TOP, 0, RECORD_BORDER_TOP, 0));
        // Custom jbTabbedPane
        jbTabbedPane.addTab("", tableTab);
        jbTabbedPane.setTabComponentAt(jbTabbedPane.indexOfComponent(tableTab), leftTab);
        if (IS_DEVELOP_MODE) {
            // rightTab
            JBLabel rightTab = new JBLabel("Visualization");
            rightTab.setBorder(BorderFactory
                .createEmptyBorder(LayoutConstants.NUM_2, LayoutConstants.RECORD_TABBED_BORDER, LayoutConstants.NUM_2,
                    0));
            rightTab.setOpaque(false);
            rightTab.setPreferredSize(new Dimension(LayoutConstants.JPA_LABEL_WIDTH, LayoutConstants.DEVICES_HEIGHT));
            rightTab.setFont(font);
            rightTab.setBounds(LayoutConstants.RECORD_TABBED_BOUNDS_POINT, LayoutConstants.NUM_2,
                LayoutConstants.RECORD_TABBED_BOUNDS_WIDTH, LayoutConstants.NUM_20);
            // viewTab
            JBPanel viewTab = createViewTab(sessionId);
            viewTab.setBorder(BorderFactory.createEmptyBorder(RECORD_BORDER_TOP, 0, RECORD_BORDER_TOP, 0));
            // Custom jbTabbedPane
            jbTabbedPane.addTab("", viewTab);
            jbTabbedPane.setTabComponentAt(jbTabbedPane.indexOfComponent(viewTab), rightTab);
        }
        nativePanel.add(jbTabbedPane, BorderLayout.CENTER);
        return jbTabbedPane;
    }

    private JBLabel getTitleLabel() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getTitleLabel");
        }
        // recordTitle add title
        JBLabel titleLabel = new JBLabel("      Record Native Allocations (loaded from file)");
        titleLabel.setBorder(BorderFactory
            .createEmptyBorder(LayoutConstants.RECORD_TABBED_TITLE_SPACE, 0, LayoutConstants.RECORD_TABBED_TITLE_SPACE,
                0));
        Font titleFont = new Font(Font.DIALOG, Font.PLAIN, LayoutConstants.TUN_LABEL_FONT);
        titleLabel.setFont(titleFont);
        return titleLabel;
    }

    private JBPanel createTableTab(long sessionId, NativeDataExternalInterface dataInterface) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createTableTab");
        }
        JBPanel tablePanel = new JBPanel(new BorderLayout());
        tablePanel.setPreferredSize(new Dimension());
        tablePanel.setBorder(BorderFactory.createEmptyBorder(BORDER_TOP, 0, BORDER_TOP, 0));
        tablePanel.setOpaque(true);
        List<String> itemBox = new ArrayList<>();
        itemBox.add("Arrange by allocation method");
        itemBox.add("Arrange by callstack");
        // Reserved Tree Table panel
        recordTable = new NativeHookTreeTablePanel(sessionId, dataInterface);
        recordTable.setOpaque(true);
        recordTable.setBackground(JBColor.background().brighter());
        recordTable
            .setPreferredSize(new Dimension(LayoutConstants.RECORD_TABLE_WIDTH, LayoutConstants.RECORD_TABLE_HEIGHT));
        // add to table Tab Options panel
        tablePanel.add(createRecordFeatures(itemBox, sessionId, true), BorderLayout.NORTH);
        tablePanel.add(recordTable, BorderLayout.CENTER);
        return tablePanel;
    }

    private JBPanel createViewTab(long sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createViewTab");
        }
        JBPanel viewPanel = new JBPanel(new BorderLayout());
        viewPanel.setBorder(BorderFactory.createEmptyBorder(BORDER_TOP, 0, BORDER_TOP, 0));
        viewPanel.setOpaque(true);
        List<String> itemBox = new ArrayList<>();
        itemBox.add("Allocation Size");
        itemBox.add("Callstack Size");
        // Reserved Tree Table panel
        JBPanel recordTableView = new JBPanel();
        recordTableView.setOpaque(true);
        recordTableView.setBorder(BorderFactory.createEmptyBorder(BORDER_TOP, 0, 0, 0));
        recordTableView
            .setPreferredSize(new Dimension(LayoutConstants.RECORD_TABLE_WIDTH, LayoutConstants.RECORD_TABLE_HEIGHT));
        // add to table Tab Options panel
        viewPanel.add(createRecordFeatures(itemBox, sessionId, false), BorderLayout.NORTH);
        viewPanel.add(recordTableView, BorderLayout.CENTER);
        return viewPanel;
    }

    private JBPanel createRecordFeatures(List<String> items, long sessionId, boolean tabType) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createRecordFeatures");
        }
        // recordFeatures
        JBPanel recordFeatures = new JBPanel();
        recordFeatures.setOpaque(false);
        recordFeatures.setPreferredSize(
            new Dimension(LayoutConstants.RECORD_TABLE_WIDTH, LayoutConstants.RECORD_FEATURES_HEIGHT));
        recordFeatures.setBackground(JBColor.background().darker());
        recordFeatures.setBorder(BorderFactory.createEmptyBorder(RECORD_BORDER_TOP, 0, RECORD_BORDER_TOP, 0));
        recordFeatures.setLayout(new BorderLayout(LayoutConstants.RECORD_FEATURES_SPACE, 0));

        // recordFeatures add Arrange by
        CustomComboBox arrangeBox = new CustomComboBox();
        arrangeBox.setFont(new Font("PingFang SC", Font.PLAIN, LayoutConstants.FONT_SIZE));
        arrangeBox.setUI(new CustomJBComboBoxUI());
        arrangeBox.setBorder(BorderFactory.createLineBorder(ColorConstants.NATIVE_RECORD_BORDER, 1));
        arrangeBox.setPreferredSize(
            new Dimension(LayoutConstants.RECORD_COMBO_BOX_WIDTH, LayoutConstants.RECORD_SEARCH_HEIGHT));
        for (String item : items) {
            arrangeBox.addItem(item);
        }
        arrangeBox.setSelectedIndex(0);
        recordFeatures.add(arrangeBox, BorderLayout.WEST);

        JBPanel searchPanel = new JBPanel();
        searchPanel.setOpaque(true);
        searchPanel.setLayout(new BorderLayout());

        // recordFeatures Search
        CustomJBTextField search = new CustomJBTextField();
        search.setBorder(BorderFactory.createLineBorder(ColorConstants.NATIVE_RECORD_BORDER, 1));
        search.setText("Search");
        addBoxAndSearchListener(search);
        search
            .setPreferredSize(new Dimension(LayoutConstants.RECORD_SEARCH_WIDTH, LayoutConstants.RECORD_SEARCH_HEIGHT));
        searchPanel.add(search, BorderLayout.WEST);
        if (tabType) {
            recordFeatures.add(searchPanel, BorderLayout.CENTER);
        }
        arrangeBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                String selectItem = arrangeBox.getSelectedItem().toString();
                refreshNativeHookView(search, selectItem);
            }
        });
        return recordFeatures;
    }

    /**
     * ArrangeBox drop-down add refresh TreeTable event
     *
     * @param search search
     * @param selectItem selectItem
     */
    public void refreshNativeHookView(CustomJBTextField search, String selectItem) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("refreshNativeHookView");
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                search.setText("Search");
                recordTable.refreshNativeHookData(selectItem, search.getText());
            }
        });
    }

    private void addBoxAndSearchListener(CustomJBTextField search) {
        search.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                if ("Search".equals(search.getText())) {
                    search.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                if (search.getText().length() < 1) {
                    search.setText("Search");
                }
            }
        });
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                String text = search.getText();
                if (StringUtils.isBlank(text)) {
                    return;
                }
                if (!("Search".equals(text)) && !text.equals("")) {
                    recordTable.handleInsertSearchText(text);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                String text = search.getText();
                recordTable.handleRemoveSearchText(text);
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
}
