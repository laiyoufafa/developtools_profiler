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
import com.intellij.ui.components.JBTextField;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.datasources.utils.monitorconfig.entity.ConfigInfo;
import ohos.devtools.datasources.utils.monitorconfig.entity.NativeHookConfigInfo;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.common.customcomp.CustomComboBox;
import ohos.devtools.views.common.customcomp.CustomJBComboBoxUI;
import ohos.devtools.views.layout.dialog.CustomDialog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * NativeConfigDialog
 *
 * @param <T>
 * @since 2021/11/17 16:56
 */
public class NativeConfigDialog<T extends JBPanel<T>> {
    private static final Logger LOGGER = LogManager.getLogger(NativeConfigDialog.class);

    /**
     * config Panel min width
     */
    private static final int CONFIG_PANEL_MIN_WIDTH = 350;

    /**
     * config Panel min height
     */
    private static final int CONFIG_PANEL_MIN_HEIGHT = 350;

    /**
     * config button width
     */
    private static final int CONFIG_BUTTON_WIDTH = 80;

    /**
     * config button height
     */
    private static final int CONFIG_BUTTON_HEIGHT = 30;

    /**
     * label Font
     */
    private final Font titleFont = new Font("PingFangSC-Regular;", Font.PLAIN, 14);

    /**
     * Cancel button
     */
    private final JButton buttonCancel = new JButton("Cancel");

    /**
     * Save button
     */
    private final JButton buttonSave = new JButton("Save");

    /**
     * Save button
     */
    private final JButton restoreConfig = new JButton("Restore Config");

    /**
     * hook Config Panel
     */
    private JBPanel<T> hookConfigPanel;

    /**
     * hook parameter Config Panel
     */
    private JBPanel<T> parameterConfigPanel;

    /**
     * perfConfig
     */
    private NativeHookConfigInfo restorePerfConfig;

    /**
     * nativeHookConfigInfo
     */
    private NativeHookConfigInfo nativeHookConfigInfo;

    /**
     * configInfo
     */
    private final ConfigInfo configInfo;

    /**
     * shared TextField
     */
    private final JBTextField sharedTextField = new JBTextField();

    /**
     * filter TextField
     */
    private final JBTextField filterTextField = new JBTextField();

    /**
     * unWind TextField
     */
    private final JBTextField unWindTextField = new JBTextField();

    /**
     * shared CustomComboBox
     */
    private CustomComboBox<String> sharedCustomComboBox;

    /**
     * filter CustomComboBox
     */
    private CustomComboBox<String> filterCustomComboBox;

    /**
     * shared TitleLabel
     */
    private JBLabel sharedTitleLabel;

    /**
     * filter TitleLabel
     */
    private JBLabel filterTitleLabel;

    /**
     * unWind TitleLabel
     */
    private JBLabel unWindTitleLabel;

    /**
     * is Able Save
     */
    private boolean isAbleSave;

    /**
     * CpuConfigDialog
     */
    public NativeConfigDialog() {
        configInfo = ConfigInfo.getInstance();
        initDefaultData();
        // init ConfigTab
        hookConfigPanel = new JBPanel<>(new MigLayout("insets 0", "[80%!,fill][10%!,fill]", "[grow][grow]"));
        addPerfConfigTab();
        JBPanel<T> configPanel = new JBPanel<>(new MigLayout("insets 0 10 5 0", "[grow]", "[grow][grow]"));
        configPanel.add(hookConfigPanel, "span, growx, growy");
        configPanel.setMinimumSize(new Dimension(CONFIG_PANEL_MIN_WIDTH, CONFIG_PANEL_MIN_HEIGHT));
        CustomDialog customDialog = new CustomDialog("Config", configPanel);
        initConfigStyle(configPanel);
        addConfigBtnListener(customDialog);
        customDialog.setResizable(false);
        customDialog.show();
    }

    /**
     * init Default Data
     */
    private void initDefaultData() {
        nativeHookConfigInfo = configInfo.getNativeHookConfigInfo();
        restorePerfConfig = nativeHookConfigInfo;
    }

    /**
     * init Config panel Style
     *
     * @param configPanel configPanel
     */
    private void initConfigStyle(JBPanel<T> configPanel) {
        buttonCancel.setFont(titleFont);
        buttonSave.setFont(titleFont);
        restoreConfig.setFont(titleFont);
        buttonCancel.setPreferredSize(new Dimension(CONFIG_BUTTON_WIDTH, CONFIG_BUTTON_HEIGHT));
        buttonSave.setPreferredSize(new Dimension(CONFIG_BUTTON_WIDTH, CONFIG_BUTTON_HEIGHT));
        restoreConfig.setPreferredSize(new Dimension(CONFIG_BUTTON_WIDTH, CONFIG_BUTTON_HEIGHT));
        JBPanel<T> buttonPanel = new JBPanel<>(new MigLayout("insets 0", "[]150[][]"));
        buttonPanel.add(restoreConfig);
        buttonPanel.add(buttonCancel);
        buttonPanel.add(buttonSave);
        configPanel.add(buttonPanel);
    }

    /**
     * add Perf Config Tab
     */
    private void addPerfConfigTab() {
        parameterConfigPanel = new JBPanel<>(new MigLayout("insets 0", "[85%!,fill][10%!,fill]", "[][]"));
        // title label
        JBLabel titleLabel = new JBLabel("NativeHeapConfig");
        titleLabel.setOpaque(true);
        titleLabel.setFont(new Font("PingFangSC-Regular;", Font.PLAIN, 16));
        hookConfigPanel.add(titleLabel, "span,wrap");
        addParameterComponent("Shared memory size", sharedTextField, nativeHookConfigInfo.getSharedMemorySize());
        addParameterComponent("Filter memory size", filterTextField, nativeHookConfigInfo.getFilterSize());
        addParameterComponent("Max unwind level", unWindTextField, String.valueOf(nativeHookConfigInfo.getUnwind()));
        addSharedChangedListener();
        hookConfigPanel.add(parameterConfigPanel, "span,wrap");
    }

    /**
     * addParameterComponent
     *
     * @param name name
     * @param textField textField
     * @param size size
     */
    private void addParameterComponent(String name, JBTextField textField, String size) {
        String[] split = size.split(":");
        // Memory Label
        JBLabel memoryLabel = new JBLabel(name);
        memoryLabel.setOpaque(true);
        memoryLabel.setFont(titleFont);

        textField.setName(name);
        String unitIndex = "MB";
        parameterConfigPanel.add(memoryLabel, "span");
        isAbleSave = true;
        if (name.contains("Shared")) {
            // title Label
            sharedTitleLabel = new JBLabel();
            sharedTitleLabel.setOpaque(true);
            sharedTitleLabel.setFont(titleFont);
            sharedTitleLabel.setForeground(JBColor.red);
            sharedTitleLabel.setText(" ");
            textField.setText(split[0]);
            parameterConfigPanel.add(textField, "growx, growy");
            unitIndex = split[1];
            sharedCustomComboBox = addDropDownBox(unitIndex);
            parameterConfigPanel.add(sharedCustomComboBox, "wrap");
            parameterConfigPanel.add(sharedTitleLabel, "span,wrap");
        } else if (name.contains("Filter")) {
            // title Label
            filterTitleLabel = new JBLabel();
            filterTitleLabel.setOpaque(true);
            filterTitleLabel.setFont(titleFont);
            filterTitleLabel.setForeground(JBColor.red);
            filterTitleLabel.setText(" ");
            textField.setText(split[0]);
            parameterConfigPanel.add(textField, "growx, growy");
            unitIndex = split[1];
            filterCustomComboBox = addDropDownBox(unitIndex);
            parameterConfigPanel.add(filterCustomComboBox, "wrap");
            parameterConfigPanel.add(filterTitleLabel, "span,wrap");
            addTextChangedListener(filterTextField, filterTitleLabel);
        } else {
            // title Label
            unWindTitleLabel = new JBLabel();
            unWindTitleLabel.setOpaque(true);
            unWindTitleLabel.setFont(titleFont);
            unWindTitleLabel.setForeground(JBColor.red);
            unWindTitleLabel.setText(" ");
            textField.setText(size);
            parameterConfigPanel.add(textField, "span,wrap");
            parameterConfigPanel.add(unWindTitleLabel, "span,wrap");
            addTextChangedListener(unWindTextField, unWindTitleLabel);
        }
    }

    /**
     * add DropDownBox
     *
     * @param unitIndex unitIndex
     * @return CustomComboBox <String>
     */
    private CustomComboBox<String> addDropDownBox(String unitIndex) {
        // recordFeatures add Arrange by
        CustomComboBox<String> arrangeBox = new CustomComboBox<>();
        CustomJBComboBoxUI customJBComboBoxUI = new CustomJBComboBoxUI();
        arrangeBox.setFont(titleFont);
        arrangeBox.setUI(customJBComboBoxUI);
        arrangeBox.setPreferredSize(new Dimension(60, 22));
        arrangeBox.setOpaque(true);
        arrangeBox.setBackground(JBColor.background().brighter());
        arrangeBox.addItem("MB");
        arrangeBox.addItem("KB");
        if (unitIndex.equals("MB")) {
            arrangeBox.setSelectedIndex(0);
        } else {
            arrangeBox.setSelectedIndex(1);
        }
        arrangeBox.setForeground(JBColor.background().brighter());
        return arrangeBox;
    }

    /**
     * add Config Panel Button Listener
     *
     * @param customDialog customDialog
     */
    private void addConfigBtnListener(CustomDialog customDialog) {
        buttonSave.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                try {
                    nativeHookConfigInfo.setUnwind(Integer.parseInt(unWindTextField.getText()));
                } catch (NumberFormatException exception) {
                    isAbleSave = false;
                    unWindTitleLabel.setText("The input size is wrong, please try again!");
                }
                if (isAbleSave) {
                    String sharedUnit = sharedCustomComboBox.getSelectedIndex() == 0 ? "MB" : "KB";
                    nativeHookConfigInfo.setSharedMemorySize(sharedTextField.getText() + ":" + sharedUnit);
                    String filterUnit = filterCustomComboBox.getSelectedIndex() == 0 ? "MB" : "KB";
                    nativeHookConfigInfo.setFilterSize(filterTextField.getText() + ":" + filterUnit);

                    configInfo.setNativeHookConfigInfo(nativeHookConfigInfo);
                    customDialog.close(1);
                }
            }
        });
        buttonCancel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                super.mouseClicked(event);
                nativeHookConfigInfo = restorePerfConfig;
                customDialog.close(1);
            }
        });
        restoreConfig.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                super.mouseClicked(event);
                configInfo.restoreNativeHookConfigInfoDefault();
                hookConfigPanel.removeAll();
                initDefaultData();
                addPerfConfigTab();
                parameterConfigPanel.repaint();
                parameterConfigPanel.validate();
                hookConfigPanel.repaint();
                hookConfigPanel.validate();
            }
        });
    }

    /**
     * add SharedChanged Listener
     */
    private void addSharedChangedListener() {
        sharedTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                inputValueCheckListener();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                inputValueCheckListener();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                inputValueCheckListener();
            }
        });

        sharedCustomComboBox.addItemListener(event -> {
            inputValueCheckListener();
        });

        filterCustomComboBox.addItemListener(event -> {
            inputTypeCheckListener(filterTextField, filterTitleLabel);
        });
    }

    /**
     * add TextChanged Listener
     *
     * @param textField textField
     * @param titleLabel titleLabel
     */
    private void addTextChangedListener(JBTextField textField, JBLabel titleLabel) {
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                inputTypeCheckListener(textField, titleLabel);
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                inputTypeCheckListener(textField, titleLabel);
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                inputTypeCheckListener(textField, titleLabel);
            }
        });
    }

    /**
     * input Type Check
     *
     * @param textField textField
     * @param titleLabel titleLabel
     */
    private void inputTypeCheckListener(JBTextField textField, JBLabel titleLabel) {
        char[] chars = textField.getText().toCharArray();
        int textValue = -1;
        for (char number : chars) {
            if (Character.isDigit(number)) {
                titleLabel.setText(" ");
                isAbleSave = true;
            } else {
                titleLabel.setText("The input type is wrong, please try again!");
                isAbleSave = false;
            }
        }
        try {
            textValue = Integer.parseInt(sharedTextField.getText());
        } catch (NumberFormatException exception) {
            titleLabel.setText("The input size is wrong, please try again!");
        }
    }

    /**
     * input Value Check
     */
    private void inputValueCheckListener() {
        int textValue = -1;
        inputTypeCheckListener(sharedTextField, sharedTitleLabel);
        try {
            textValue = Integer.parseInt(sharedTextField.getText());
        } catch (NumberFormatException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("The input type is wrong, please select a number as input!");
            }
        }
        if (sharedCustomComboBox.getSelectedIndex() == 0) {
            if (textValue > 50 || textValue < 1) {
                isAbleSave = false;
                sharedTitleLabel.setText("The input size should be in [1,50] M,  please re-type!");
                return;
            }
        }

        if (sharedCustomComboBox.getSelectedIndex() == 1) {
            // only support 4K - 50M
            if (textValue > 51200 || textValue < 4) {
                isAbleSave = false;
                sharedTitleLabel.setText("The input size should be in [4,51200] K, please re-type!");
                return;
            }

            if ((textValue % 4) != 0) {
                isAbleSave = false;
                sharedTitleLabel.setText("The input size has to be multiple of 4k, please re-type!");
                return;
            }
        }
        isAbleSave = true;
        sharedTitleLabel.setText(" ");
    }
}
