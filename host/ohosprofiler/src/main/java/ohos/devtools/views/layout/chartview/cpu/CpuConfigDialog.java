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

package ohos.devtools.views.layout.chartview.cpu;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextField;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.datasources.utils.monitorconfig.entity.AppTraceConfig;
import ohos.devtools.datasources.utils.monitorconfig.entity.ConfigInfo;
import ohos.devtools.datasources.utils.monitorconfig.entity.PerfConfig;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.customcomp.CustomComboBox;
import ohos.devtools.views.common.customcomp.CustomJBComboBoxUI;
import ohos.devtools.views.layout.chartview.utils.DigitCheckDocument;
import ohos.devtools.views.layout.chartview.utils.MultiComboBox;
import ohos.devtools.views.layout.dialog.CustomDialog;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * CpuConfigDialog
 *
 * @since 2021/11/15 10:20
 */
public class CpuConfigDialog<T extends JBPanel<T>> {
    private static final Logger LOGGER = LogManager.getLogger(CpuConfigDialog.class);

    /**
     * config Panel min width
     */
    private static final int CONFIG_PANEL_MIN_WIDTH = 440;

    /**
     * config Panel min height
     */
    private static final int CONFIG_PANEL_MIN_HEIGHT = 780;

    /**
     * config button width
     */
    private static final int CONFIG_BUTTON_WIDTH = 80;

    /**
     * config button height
     */
    private static final int CONFIG_BUTTON_HEIGHT = 30;

    /**
     * mMapPages Max value
     */
    private static final int M_MAP_PAGES_MAX_VALUE = 10;

    /**
     * mMapPages Min value
     */
    private static final int M_MAP_PAGES_MIN_VALUE = 1;

    /**
     * CPU percent max
     */
    private static final int CPU_PERCENT_MAX = 100;

    /**
     * view Border distance
     */
    private static final int VIEW_BORDER_DISTANCE = 8;

    /**
     * detailed bounds size
     */
    private static final int BOUNDS_SIZE = 20;

    /**
     * detailed border size
     */
    private static final int BORDER_SIZE = 2;

    /**
     * normal on
     */
    private final Icon NORMAL_ON = IconLoader.getIcon("/images/normal_on.png", CpuConfigDialog.class);

    /**
     * normal off
     */
    private final Icon NORMAL_OFF = IconLoader.getIcon("/images/normal_off.png", CpuConfigDialog.class);

    /**
     * label Font
     */
    private final Font titleFont = new Font("PingFangSC-Regular;", Font.PLAIN, 14);

    /**
     * area Font
     */
    private final Font messageFont = new Font("PingFangSC-Regular", Font.PLAIN, 12);

    /**
     * label font
     */
    private final Font font = new Font(Font.DIALOG, Font.PLAIN, LayoutConstants.FONT_SIZE);

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
     * deviceIPPortInfo
     */
    private DeviceIPPortInfo deviceIPPortInfo;

    /**
     * perf Config Panel
     */
    private JBPanel<T> perfConfigPanel;

    /**
     * appTrace Config Panel
     */
    private JBPanel<T> appTraceConfigPanel;

    /**
     * tabbedPane
     */
    private JBTabbedPane tabbedPane;

    /**
     * cpu List
     */
    private final List<String> cpuList = new ArrayList<>();

    /**
     * perf Config
     */
    private PerfConfig<String> perfConfig;

    /**
     * AppTrace Config
     */
    private AppTraceConfig appTraceConfig;

    /**
     * perfConfig
     */
    private PerfConfig<String> restorePerfConfig;

    /**
     * cpu Num
     */
    private int cpuNumber = 1;

    /**
     * isLeakOhos
     */
    private boolean isLeakOhos;

    /**
     * eventList
     */
    private List<String> eventList = new ArrayList<>();

    /**
     * cpu Multi List
     */
    private List<String> cpuMultiList = new ArrayList<>();

    /**
     * event Multi List
     */
    private List<String> eventMultiList = new ArrayList<>();

    /**
     * configInfo
     */
    private ConfigInfo configInfo;

    /**
     * configPanel
     */
    private JBPanel<T> configPanel;

    /**
     * customDialog
     */
    private CustomDialog customDialog;

    /**
     * configButton
     */
    private JBLabel configButton;

    /**
     * multiCpuComboBox
     */
    private MultiComboBox<String> multiCpuComboBox;

    /**
     * multiEventComboBox
     */
    private MultiComboBox<String> multiEventComboBox;

    /**
     * CpuConfigDialog
     *
     * @param configButton configButton
     * @param deviceIPPortInfo deviceIPPortInfo
     */
    public CpuConfigDialog(JBLabel configButton, DeviceIPPortInfo deviceIPPortInfo) {
        this.configButton = configButton;
        this.deviceIPPortInfo = deviceIPPortInfo;
        configButton.setName("open");
        configInfo = ConfigInfo.getInstance();
        initConfigPanel();
    }

    private void initConfigPanel() {
        new SwingWorker<>() {
            @Override
            protected Object doInBackground() {
                initDefaultData(deviceIPPortInfo);
                return new Object();
            }

            @Override
            protected void done() {
                // JBTabbedPane
                tabbedPane = new JBTabbedPane();
                // init ConfigTab
                perfConfigPanel = new JBPanel<>(new MigLayout("insets 0", "[grow]", "[grow]"));
                appTraceConfigPanel = new JBPanel<>(new MigLayout("insets 0", "[grow]", "[grow]"));
                addTabPanel(perfConfigPanel, " PerfConfig");
                addPerfConfigTab();
                addTabPanel(appTraceConfigPanel, " APPTraceConfig");
                addAppTraceConfigTab();
                configPanel = new JBPanel<>(new MigLayout("insets 0 10 5 0", "[grow]", "[90%!,fill][10%!,fill]"));
                configPanel.add(tabbedPane, "span, growx, growy");
                configPanel.setMinimumSize(new Dimension(CONFIG_PANEL_MIN_WIDTH, CONFIG_PANEL_MIN_HEIGHT));
                initConfigStyle(configPanel);
                customDialog = new CustomDialog("Config", configPanel);
                addConfigListener();
                customDialog.setResizable(false);
                customDialog.show();
            }
        }.execute();
    }

    private void addTabPanel(JBPanel<T> configPanel, String name) {
        configPanel.setOpaque(true);
        configPanel.setBorder(BorderFactory.createLineBorder(JBColor.green));
        // Config　Tab
        configPanel.setBorder(BorderFactory.createEmptyBorder(VIEW_BORDER_DISTANCE, 0, VIEW_BORDER_DISTANCE, 0));
        // Custom Config Tab
        tabbedPane.addTab("", configPanel);
        // PerfConfig Tab
        JBLabel configLabel = new JBLabel(name);
        configLabel.setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, BOUNDS_SIZE, BORDER_SIZE, 0));
        configLabel.setOpaque(false);
        configLabel.setPreferredSize(new Dimension(LayoutConstants.JPA_LABEL_WIDTH, LayoutConstants.DEVICES_HEIGHT));
        configLabel.setFont(font);
        configLabel
            .setBounds(LayoutConstants.RECORD_BORDER_SPACE, BORDER_SIZE, LayoutConstants.RECORD_TABBED_BOUNDS_WIDTH,
                BOUNDS_SIZE);
        tabbedPane.setTabComponentAt(tabbedPane.indexOfComponent(configPanel), configLabel);
    }

    /**
     * init Default Data
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     */
    private void initDefaultData(DeviceIPPortInfo deviceIPPortInfo) {
        isLeakOhos = deviceIPPortInfo.getDeviceType() == DeviceType.LEAN_HOS_DEVICE;
        String deviceID = deviceIPPortInfo.getDeviceID();
        perfConfig = configInfo.getPerfConfig(isLeakOhos);
        appTraceConfig = configInfo.getAppTraceConfig();
        // set default cpu Number
        eventList = perfConfig.getSoftHardWareEvents(isLeakOhos, deviceID);
        cpuNumber = perfConfig.getCpuCount(isLeakOhos, deviceID);
        restorePerfConfig = perfConfig;
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
        JBPanel<T> buttonPanel = new JBPanel<>(new MigLayout("insets 0", "[]210[][]"));
        buttonPanel.add(restoreConfig);
        buttonPanel.add(buttonCancel);
        buttonPanel.add(buttonSave);
        configPanel.add(buttonPanel);
    }

    /**
     * add Config Panel Listener
     */
    private void addConfigListener() {
        buttonSave.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                eventMultiList = getInputContent(multiEventComboBox.getTextField().getText());
                perfConfig.setEventList(eventMultiList);
                cpuMultiList = getInputContent(multiCpuComboBox.getTextField().getText());
                perfConfig.setCpuList(cpuMultiList);
                configInfo.setPerfConfig(perfConfig);
                configInfo.setAppTraceConfig(appTraceConfig);
                configButton.setName("close");
                customDialog.close(1);
            }
        });
        buttonCancel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                super.mouseClicked(event);
                perfConfig = restorePerfConfig;
                configButton.setName("close");
                customDialog.close(1);
            }
        });
        restoreConfig.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                super.mouseClicked(event);
                configInfo.restorePerfDefault(isLeakOhos);
                perfConfigPanel.removeAll();
                appTraceConfigPanel.removeAll();
                initDefaultData(deviceIPPortInfo);
                appTraceConfig.setMemoryBufferSize(10);
                addPerfConfigTab();
                addAppTraceConfigTab();
                perfConfigPanel.repaint();
                perfConfigPanel.validate();
            }
        });

        customDialog.getWindow().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                super.windowClosed(event);
                configButton.setName("close");
            }
        });
    }

    private void addAppTraceConfigTab() {
        JBPanel<T> appTracePanel =
            new JBPanel<>(new MigLayout("insets 0", "[80%!,fill]10[10%!,fill]", "10[5%!,fill][5%!,fill][80%!,fill]"));
        // title label
        JBLabel titleLabel = new JBLabel("In-memory buffer size");
        titleLabel.setOpaque(true);
        titleLabel.setFont(titleFont);
        appTracePanel.add(titleLabel, "span,wrap");
        // mMapPages Slider
        JSlider unitSlider = new JSlider(0, 110);
        // buffer size
        int bufferSize = appTraceConfig.getMemoryBufferSize();
        unitSlider.setValue(bufferSize);
        unitSlider.setMajorTickSpacing(10);
        unitSlider.setMinorTickSpacing(1);
        // ValuePanel
        JBLabel valuePanel = new JBLabel("" + bufferSize + " MB", JBLabel.CENTER);
        valuePanel.setFont(titleFont);
        valuePanel.setVerticalTextPosition(JBLabel.CENTER);
        valuePanel.setHorizontalTextPosition(JBLabel.CENTER);
        valuePanel.setBackground(JBColor.background().brighter());
        valuePanel.setOpaque(true);
        appTracePanel.add(unitSlider, "growx,growy");
        appTracePanel.add(valuePanel, "growx,growy,wrap");
        appTracePanel.add(new JBLabel(), "span,growx,growy");
        unitSlider.addChangeListener(event -> {
            valuePanel.setText("" + unitSlider.getValue() + " MB");
            appTraceConfig.setMemoryBufferSize(unitSlider.getValue());
        });
        appTraceConfigPanel.add(appTracePanel, "span,growx,growy");
    }

    /**
     * add Perf Config Tab
     */
    private void addPerfConfigTab() {
        addParameterComponent("CPU", "Record assign cpu num such as 0,1,2", ComponentType.MULTI_DROP_DOWN_BOX);
        addParameterComponent("Event List", "Event type Default is cpu cycles", ComponentType.MULTI_DROP_DOWN_BOX);
        addParameterComponent("CPU Percent", "Set the max percent of cpu time used for recording",
            ComponentType.SLIDER_BAR);
        addParameterComponent("Frequency", "Set event sampling frequency", ComponentType.INPUT_BOX);
        addParameterComponent("Period", "Set event sampling period for trace point events", ComponentType.INPUT_BOX);
        addParameterComponent("Is Off CPU", "Trace when threads are scheduled off cpu", ComponentType.TOGGLE_BUTTON);
        addParameterComponent("No Inherit", "Don't trace child processes", ComponentType.TOGGLE_BUTTON);
        addParameterComponent("Call Stack", "Setup and enable call stack recording", ComponentType.DROP_DOWN_BOX);
        addParameterComponent("Branch", "Taken branch stack sampling", ComponentType.DROP_DOWN_BOX);
        addParameterComponent("Mmap Pages", "Used to receiving record data from kernel", ComponentType.SLIDER_BAR);
        addParameterComponent("Clock Type",
            "Set the clock id to use for the various time fields in the perf_event_type records",
            ComponentType.DROP_DOWN_BOX);
    }

    /**
     * addParameterComponent
     *
     * @param name name
     * @param Message Message
     * @param componentType componentType
     */
    private void addParameterComponent(String name, String Message, ComponentType componentType) {
        // title label
        JBLabel titleLabel = new JBLabel(name);
        titleLabel.setOpaque(true);
        titleLabel.setFont(titleFont);
        // message Label
        JBLabel messageLabel = new JBLabel(Message);
        messageLabel.setFont(messageFont);
        messageLabel.setForeground(JBColor.foreground().darker());
        messageLabel.setOpaque(false);
        switch (componentType) {
            case INPUT_BOX:
                perfConfigPanel.add(titleLabel, "split 2");
                perfConfigPanel.add(messageLabel, "span,flowx,wrap");
                buildInputBox(name);
                break;
            case SLIDER_BAR:
                perfConfigPanel.add(titleLabel, "split 2");
                perfConfigPanel.add(messageLabel, "span,flowx,wrap");
                buildSliderBar(name);
                break;
            case DROP_DOWN_BOX:
                perfConfigPanel.add(titleLabel, "split 2");
                perfConfigPanel.add(messageLabel, "span, flowx, wrap");
                buildDropDownBox(name);
                break;
            case TOGGLE_BUTTON:
                perfConfigPanel.add(titleLabel, "split 2");
                perfConfigPanel.add(messageLabel, "flowx");
                buildToggleButton(name);
                break;
            case MULTI_DROP_DOWN_BOX:
                perfConfigPanel.add(titleLabel, "split 2");
                perfConfigPanel.add(messageLabel, "span, flowx, wrap");
                buildMultiComboBox(name);
                break;
            default:
                break;
        }
    }

    /**
     * build Multi DropDown Box
     *
     * @param name name
     */
    private void buildMultiComboBox(String name) {
        if (name.equals("CPU")) {
            ArrayList<String> cpuArray = new ArrayList<>();
            cpuArray.add("Select All");
            for (int index = 0; index < cpuNumber; index++) {
                cpuArray.add(index + "");
            }
            multiCpuComboBox = new MultiComboBox<>(cpuArray);
            multiCpuComboBox.setSelectValues(perfConfig.getCpuList());
            multiCpuComboBox.setName(name);
            multiCpuComboBox.setFont(titleFont);
            perfConfigPanel.add(multiCpuComboBox, "growx,growy,span,wrap");
        }
        if (name.equals("Event List")) {
            ArrayList<String> eventArray = new ArrayList<>();
            eventArray.add("Select All");
            eventArray.addAll(this.eventList);
            multiEventComboBox = new MultiComboBox<>(eventArray);
            multiEventComboBox.setSelectValues(perfConfig.getEventList());
            multiEventComboBox.setName(name);
            multiEventComboBox.setFont(titleFont);
            perfConfigPanel.add(multiEventComboBox, "growx,growy,span,wrap");
        }
    }

    private List<String> getInputContent(String content) {
        String contentText = content.replace("[", "").replace("]", "")
            .replace(" ", "").strip();
        if (StringUtils.isBlank(content)) {
            return new ArrayList<>();
        }
        return Arrays.asList(contentText.split(","));
    }

    /**
     * build DropDown Box
     *
     * @param name name
     */
    private void buildDropDownBox(String name) {
        List<String> dropList = new ArrayList<>();
        boolean isAddAll = false;
        int selectIndex = 0;
        switch (name) {
            case "Call Stack": {
                for (Enum anEnum : PerfConfig.CallStack.values()) {
                    dropList.add(anEnum.name().toLowerCase(Locale.ENGLISH));
                }
                selectIndex = perfConfig.getCallStack();
                break;
            }
            case "Branch": {
                for (Enum anEnum : PerfConfig.Branch.values()) {
                    dropList.add(anEnum.name().toLowerCase(Locale.ENGLISH));
                }
                selectIndex = perfConfig.getBranch();
                break;
            }
            case "Clock Type": {
                dropList = new ArrayList<>();
                for (Enum anEnum : PerfConfig.Clock.values()) {
                    dropList.add(anEnum.name().toLowerCase(Locale.ENGLISH));
                }
                selectIndex = perfConfig.getClockId();
                break;
            }
            default: {
                break;
            }
        }
        // recordFeatures add Arrange by
        CustomComboBox<String> arrangeBox = new CustomComboBox<>();
        arrangeBox.setFont(titleFont);
        arrangeBox.setUI(new CustomJBComboBoxUI());
        arrangeBox.setBorder(BorderFactory.createLineBorder(ColorConstants.NATIVE_RECORD_BORDER, 1));
        if (isAddAll) {
            arrangeBox.addItem("All");
            cpuList.add(String.valueOf(cpuList.size()));
        }
        for (String itemName : dropList) {
            arrangeBox.addItem(itemName);
        }
        arrangeBox.setSelectedIndex(selectIndex);
        perfConfigPanel.add(arrangeBox, "growx,growy,span,wrap");
        addArrangeBoxListener(name, arrangeBox);
    }

    /**
     * build SliderBar
     *
     * @param name name
     */
    private void buildSliderBar(String name) {
        JSlider unitSlider;
        JBLabel valuePanel;
        if (name.contains("CPU")) {
            int cpuPercent = perfConfig.getCpuPercent();
            // cpuPercent Slider
            unitSlider = new JSlider(0, CPU_PERCENT_MAX);
            unitSlider.setValue(cpuPercent);
            unitSlider.setMajorTickSpacing(1);
            // ValuePanel
            valuePanel = new JBLabel("" + cpuPercent + " %", JBLabel.CENTER);
        } else {
            int mMapPages = perfConfig.getMmapPages();
            // mMapPages Slider
            unitSlider = new JSlider(M_MAP_PAGES_MIN_VALUE, M_MAP_PAGES_MAX_VALUE);
            unitSlider.setValue(logTwo(mMapPages));
            unitSlider.setMajorTickSpacing(12);
            unitSlider.setMinorTickSpacing(2);
            // ValuePanel
            valuePanel = new JBLabel("" + mMapPages + " MB", JBLabel.CENTER);
        }
        valuePanel.setFont(titleFont);
        valuePanel.setVerticalTextPosition(JBLabel.CENTER);
        valuePanel.setHorizontalTextPosition(JBLabel.CENTER);
        valuePanel.setOpaque(true);
        perfConfigPanel.add(unitSlider, "growx,growy");
        perfConfigPanel.add(valuePanel, "growx,growy,wrap");
        valuePanel.setBackground(JBColor.background().brighter());
        addUnitSliderListener(unitSlider, valuePanel);
        valuePanel.setFont(titleFont);
        valuePanel.setVerticalTextPosition(JBLabel.CENTER);
        valuePanel.setHorizontalTextPosition(JBLabel.CENTER);
    }

    /**
     * build InputBox
     *
     * @param name name
     */
    private void buildInputBox(String name) {
        JBTextField textField = new JBTextField();
        if (name.contains("Frequency")) {
            textField.setName("Frequency");
            textField.setText(String.valueOf(perfConfig.getFrequency()));
        } else {
            textField.setName("Period");
            textField.setText(String.valueOf(perfConfig.getPeriod()));
        }
        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent event) {
                super.mouseExited(event);
                if (textField.getName().contains("Frequency")) {
                    if (!textField.getText().equals("")) {
                        perfConfig.setFrequency(Integer.parseInt(textField.getText()));
                    }
                }
                if (textField.getName().contains("Period")) {
                    if (!textField.getText().equals("")) {
                        perfConfig.setPeriod(Integer.parseInt(textField.getText()));
                    }
                }
            }
        });
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                updateTextConfig(name, textField, event);
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                removeTextConfig(name, textField, event);
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                updateTextConfig(name, textField, event);
            }
        });
        perfConfigPanel.add(textField, "span,growx,growy,wrap");
    }

    private void updateTextConfig(String name, JBTextField textField, DocumentEvent event) {
        textField.setDocument(new DigitCheckDocument(5));
        int inputNumber = 0;
        try {
            String text = event.getDocument()
                .getText(event.getDocument().getStartPosition().getOffset(), event.getDocument().getLength());
            inputNumber = Integer.parseInt(text);
        } catch (BadLocationException | NumberFormatException exception) {
            textField.setToolTipText("The input type is wrong, please select a number as input!");
        }
        if (name.contains("Frequency")) {
            perfConfig.setFrequency(inputNumber);
        } else {
            perfConfig.setPeriod(inputNumber);
        }
    }

    private void removeTextConfig(String name, JBTextField textField, DocumentEvent event) {
        try {
            String text = event.getDocument()
                .getText(event.getDocument().getStartPosition().getOffset(), event.getDocument().getLength());
            if (text == null) {
                if (name.contains("Frequency")) {
                    perfConfig.setFrequency(0);
                } else {
                    perfConfig.setPeriod(0);
                }
            }
        } catch (BadLocationException exception) {
            textField.setToolTipText("The input type is wrong, please select a number as input!");
        }
    }

    /**
     * build ToggleButton
     *
     * @param name name
     */
    private void buildToggleButton(String name) {
        // JBRadioButton
        JToggleButton toggleButton = name.contains("Is Off CPU") ? new JToggleButton("", perfConfig.isOffCpu())
            : new JToggleButton("", perfConfig.isInherit());
        toggleButton.setOpaque(true);
        toggleButton.setBorderPainted(false);
        toggleButton.setSelectedIcon(NORMAL_ON);
        toggleButton.setIcon(NORMAL_OFF);

        toggleButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent event) {
                super.mouseReleased(event);
                if (name.contains("Is Off CPU")) {
                    perfConfig.setOffCpu(toggleButton.isSelected());
                }
                if (name.contains("No Inherit")) {
                    perfConfig.setInherit(toggleButton.isSelected());
                }
            }
        });
        perfConfigPanel.add(toggleButton, "wrap");
    }

    private void addArrangeBoxListener(String name, CustomComboBox<String> arrangeBox) {
        arrangeBox.addActionListener(event -> {
            int selectedIndex = arrangeBox.getSelectedIndex();
            if (name.contains("Call Stack")) {
                perfConfig.setCallStack(selectedIndex);
            }
            if (name.contains("Branch")) {
                perfConfig.setBranch(selectedIndex);
            }
            if (name.contains("Clock Type")) {
                perfConfig.setClockId(selectedIndex);
            }
        });
    }

    private void addUnitSliderListener(JSlider unitSlider, JBLabel valuePanel) {
        unitSlider.addChangeListener(event -> {
            if (unitSlider.getMajorTickSpacing() == 1) {
                valuePanel.setText("" + unitSlider.getValue() + " %");
                perfConfig.setCpuPercent(unitSlider.getValue());
            } else {
                double mMapValue = Math.pow(2, unitSlider.getValue());
                valuePanel.setText("" + (int) mMapValue + " MB");
                perfConfig.setMmapPages((int) mMapValue);
            }
        });
    }

    /**
     * getPerfConfig
     *
     * @return PerfConfig <String>
     */
    public PerfConfig<String> getPerfConfig() {
        return perfConfig;
    }

    /**
     * setPerfConfig
     *
     * @param perfConfig perfConfig
     */
    public void setPerfConfig(PerfConfig<String> perfConfig) {
        this.perfConfig = perfConfig;
    }

    /**
     * ComponentType enum
     */
    private enum ComponentType {
        DROP_DOWN_BOX, SLIDER_BAR, INPUT_BOX, TOGGLE_BUTTON, MULTI_DROP_DOWN_BOX
    }

    private int logTwo(int value) {
        if (value == 1) {
            return 0;
        } else {
            return 1 + logTwo(value >> 1);
        }
    }
}
