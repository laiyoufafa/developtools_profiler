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

package ohos.devtools.views.layout.event;

import static ohos.devtools.views.common.Constant.DEVICEREFRESH;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ohos.devtools.datasources.utils.common.GrpcException;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.service.MultiDeviceManager;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.datasources.utils.trace.service.TraceManager;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.SystemTunningProbesCheckbox;
import ohos.devtools.views.layout.swing.SystemTunningLoadDialog;
import ohos.devtools.views.layout.swing.TaskPanel;
import ohos.devtools.views.layout.swing.SystemTunningConfigPanel;

/**
 * SystemTunningConfigEvent
 *
 * @version 1.0
 * @date 2021/04/14 20:13
 **/
public class SystemTunningConfigEvent implements ClipboardOwner {
    /**
     * 显示maxDuration label的值
     */
    private int hours = 0;
    private int minutes = 0;
    private int seconds = 10;

    /**
     * oldDevice
     */
    private Vector<String> oldDevice = new Vector<>();

    /**
     * getUserCheckBoxForPerfettoStr
     */
    private String getUserCheckBoxForPerfettoStr = "";

    /**
     * 用于判断从bytrace还是ptrace 0:bytrace 1:ptrace 2:perfetto
     */
    private int differentRequests = 0;

    /**
     * 用于获取startsession后返回的sessionId
     */
    private String sessionId = null;

    /**
     * probes 复选框
     */
    private Boolean rbSchedulingIsSelect = true;

    /**
     * rb2IsSelect
     */
    private Boolean rbCpuIsSelect = true;

    /**
     * rb3IsSelect
     */
    private Boolean rbSyscallsIsSelect = false;

    /**
     * rb4IsSelect
     */
    private Boolean rbBoardIsSelect = false;

    /**
     * rb5IsSelect
     */
    private Boolean rbFrequencyIsSelect = false;

    /**
     * rb6IsSelect
     */
    private Boolean rbLowMemoryIsSelect = false;

    /**
     * rb7IsSelect
     */
    private Boolean rbAtraceIsSelect = false;

    /**
     * RecordSetting 的参数值
     */
    private int inMemoryValue = LayoutConstants.SYSTEM_TUNNING_RECORD_SETTING_DEFAULT_NUMBER;

    /**
     * maxDuration
     */
    private int maxDuration = LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_MIN_DURATION_NUMBER;

    /**
     * maxFileSize
     */
    private int maxFileSize = LayoutConstants.SYSTEM_TUNNING_RECORD_SETTING_DEFAULT_NUMBER;

    /**
     * flushOnDisk
     */
    private int flushOnDisk = LayoutConstants.SYSTEM_TUNNING_RECORD_SETTING_DEFAULT_NUMBER;

    /**
     * param
     */
    private HashMap<String, ArrayList<String>> param = new HashMap<>();

    /**
     * paramRecordSetting
     */
    private HashMap<String, Integer> paramRecordSetting = new HashMap<>();

    /**
     * clipboard
     */
    private Clipboard clipboard;

    /**
     * deviceIPPortInfo
     */
    private DeviceIPPortInfo deviceIPPortInfo = null;

    /**
     * checkBoxState
     *
     * @param rbScheduling rbScheduling
     * @param rbCpu        rbCpu
     * @param rbSyscalls   rbSyscalls
     * @param rbBoard      rbBoard
     */
    public void checkBoxState(JCheckBox rbScheduling, JCheckBox rbCpu, JCheckBox rbSyscalls, JCheckBox rbBoard) {
        rbScheduling.addMouseListener(new MouseAdapter() {
            /**
             * mouseClicked
             *
             * @param mouseEvent mouseEvent
             */
            public void mouseClicked(MouseEvent mouseEvent) {
                rbSchedulingIsSelect = rbScheduling.isSelected();
            }
        });
        rbCpu.addMouseListener(new MouseAdapter() {
            /**
             * mouseClicked
             *
             * @param mouseEvent mouseEvent
             */
            public void mouseClicked(MouseEvent mouseEvent) {
                rbCpuIsSelect = rbCpu.isSelected();
            }
        });
        rbSyscalls.addMouseListener(new MouseAdapter() {
            /**
             * mouseClicked
             *
             * @param mouseEvent mouseEvent
             */
            public void mouseClicked(MouseEvent mouseEvent) {
                rbSyscallsIsSelect = rbSyscalls.isSelected();
            }
        });
        rbBoard.addMouseListener(new MouseAdapter() {
            /**
             * mouseClicked
             *
             * @param mouseEvent mouseEvent
             */
            public void mouseClicked(MouseEvent mouseEvent) {
                rbBoardIsSelect = rbBoard.isSelected();
            }
        });
    }

    /**
     * checkBoxStateLeft
     *
     * @param rbFrequency rbFrequency
     * @param rbLowMemory rbLowMemory
     * @param rbAtrace    rbAtrace
     */
    public void checkBoxStateLeft(JCheckBox rbFrequency, JCheckBox rbLowMemory, JCheckBox rbAtrace) {
        rbFrequency.addMouseListener(new MouseAdapter() {
            /**
             * mouseClicked
             *
             * @param mouseEvent mouseEvent
             */
            public void mouseClicked(MouseEvent mouseEvent) {
                rbFrequencyIsSelect = rbFrequency.isSelected();
            }
        });
        rbLowMemory.addMouseListener(new MouseAdapter() {
            /**
             * mouseClicked
             *
             * @param mouseEvent mouseEvent
             */
            public void mouseClicked(MouseEvent mouseEvent) {
                rbLowMemoryIsSelect = rbLowMemory.isSelected();
            }
        });
        rbAtrace.addMouseListener(new MouseAdapter() {
            /**
             * mouseClicked
             *
             * @param mouseEvent mouseEvent
             */
            public void mouseClicked(MouseEvent mouseEvent) {
                rbAtraceIsSelect = rbAtrace.isSelected();
            }
        });
    }

    /**
     * getUserCheckBox
     */
    public void getUserCheckBox() {
        paramRecordSetting.clear();
        paramRecordSetting.put("inMemoryValue", inMemoryValue);
        paramRecordSetting.put("maxDuration", maxDuration);
        paramRecordSetting.put("maxFileSize", maxFileSize);
        paramRecordSetting.put("flushOnDisk", flushOnDisk);
        param.clear();
        if (rbSchedulingIsSelect) {
            getParamListFromString(param,
                SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_SCHEDULING_DETAIL_FTRACE);
        }
        if (rbCpuIsSelect) {
            getParamListFromString(param,
                SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_CPU_FREQUENCY_FTRACE);
        }
        if (rbSyscallsIsSelect) {
            getParamListFromString(param,
                SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_SYSCALLS_FTRACE);
        }
        if (rbBoardIsSelect) {
            getParamListFromString(param,
                SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_BOARD_VOLTAGES_FTRACE);
        }
        if (rbFrequencyIsSelect) {
            getParamListFromString(param,
                SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_HIGH_FREQUENCY_FTRACE);
        }
        if (rbLowMemoryIsSelect) {
            getParamListFromString(param,
                SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_LOW_MEMORY_FTRACE);
        }
        if (rbSchedulingIsSelect || rbCpuIsSelect || rbBoardIsSelect) {
            getParamListFromString(param, SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_CONTAIN_SHARE_FTRACE_LABLE);
        }
    }

    /**
     * getUserCheckBoxForPerfetto
     */
    public void getUserCheckBoxForPerfetto() {
        getUserCheckBoxForPerfettoStr = "";
        if (rbSchedulingIsSelect) {
            getUserCheckBoxForPerfettoStr = getUserCheckBoxForPerfettoStr
                .concat(SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_SCHEDULING_DETAIL_FTRACE);
        }
        if (rbCpuIsSelect) {
            getUserCheckBoxForPerfettoStr = getUserCheckBoxForPerfettoStr
                .concat(SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_CPU_FREQUENCY_FTRACE);
        }
        if (rbSyscallsIsSelect) {
            getUserCheckBoxForPerfettoStr = getUserCheckBoxForPerfettoStr
                .concat(SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_SYSCALLS_FTRACE);
        }
        if (rbBoardIsSelect) {
            getUserCheckBoxForPerfettoStr = getUserCheckBoxForPerfettoStr
                .concat(SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_BOARD_VOLTAGES_FTRACE);
        }
        if (rbFrequencyIsSelect) {
            getUserCheckBoxForPerfettoStr = getUserCheckBoxForPerfettoStr
                .concat(SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_HIGH_FREQUENCY_FTRACE);
        }
        if (rbLowMemoryIsSelect) {
            getUserCheckBoxForPerfettoStr = getUserCheckBoxForPerfettoStr
                .concat(SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_SECOND_MIDDLE_LOW_MEMORY_FTRACE);
        }
        if (rbSchedulingIsSelect || rbCpuIsSelect || rbBoardIsSelect) {
            getUserCheckBoxForPerfettoStr = getUserCheckBoxForPerfettoStr
                .concat(SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_CONTAIN_SHARE_FTRACE_LABLE);
        }
    }

    /**
     * Get user checkBox ForBytrace
     */
    private void getUserCheckBoxForBytrace() {
        getUserCheckBoxForPerfettoStr = "";
        if (rbSchedulingIsSelect || rbCpuIsSelect || rbBoardIsSelect || rbFrequencyIsSelect) {
            getUserCheckBoxForPerfettoStr = getUserCheckBoxForPerfettoStr.concat(";")
                .concat(SystemTunningProbesCheckbox.CPU_FREQUENCY_BYTRACE_ONE_AND_TWO_AND_FOUR);
        }
        if (rbSchedulingIsSelect || rbLowMemoryIsSelect) {
            getUserCheckBoxForPerfettoStr = getUserCheckBoxForPerfettoStr.concat(";")
                .concat(SystemTunningProbesCheckbox.SCHEDULING_DETAIL_BYTRACE_ONE_AND_SIX);
        }
    }

    /**
     * startTask
     *
     * @param taskSystemTunningPanel taskSystemTunningPanel
     * @param jTaskPanel             jTaskPanel
     */
    public void startTask(SystemTunningConfigPanel taskSystemTunningPanel, TaskPanel jTaskPanel) {
        taskSystemTunningPanel.getJButtonStartTask().addMouseListener(new MouseAdapter() {
            /**
             * mouseClicked
             *
             * @param mouseEvent mouseEvent
             */
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                // create system tunning load dialog object
                SystemTunningLoadDialog taskSystemLoad = new SystemTunningLoadDialog();
                super.mouseClicked(mouseEvent);
                try {
                    if (differentRequests == 0) {
                        getUserCheckBoxForBytrace();
                        sessionId = new TraceManager()
                            .createSessionByTraceRequest(deviceIPPortInfo, getUserCheckBoxForPerfettoStr, maxDuration,
                                inMemoryValue, false);
                    }
                    if (differentRequests == 1) {
                        getUserCheckBoxForPerfetto();
                        getStrResultForPerfetto();
                        sessionId = new TraceManager()
                            .createSessionRequestPerfetto(deviceIPPortInfo, getUserCheckBoxForPerfettoStr, maxDuration,
                                false);
                    }
                    taskSystemLoad.load(jTaskPanel, differentRequests, maxDuration, sessionId, deviceIPPortInfo);
                } catch (GrpcException grpcException) {
                    grpcException.printStackTrace();
                }
                jTaskPanel.getOptionJPanel().repaint();
            }
        });
    }

    /**
     * getStrResultForPerfetto
     */
    public void getStrResultForPerfetto() {
        getUserCheckBoxForPerfettoStr = SystemTunningProbesCheckbox.PERFETTO_DURATION_MS_ONE
            .concat(String.valueOf(maxDuration * SystemTunningProbesCheckbox.SECOND_TO_MS)).concat("\n")
            .concat(SystemTunningProbesCheckbox.PERFETTO_DURATION_MS_TWO)
            .concat(String.valueOf(inMemoryValue * SystemTunningProbesCheckbox.MEMORY_MB_TO_KB)).concat("\n")
            .concat(SystemTunningProbesCheckbox.PERFETTO_DURATION_MS_THREE).concat(getUserCheckBoxForPerfettoStr)
            .concat(SystemTunningProbesCheckbox.PERFETTO_DURATION_MS_FOUT);
    }

    /**
     * lastStep
     *
     * @param taskSystemTunningPanel taskSystemTunningPanel
     * @param jTaskPanel             jTaskPanel
     */
    public void lastStep(SystemTunningConfigPanel taskSystemTunningPanel, TaskPanel jTaskPanel) {
        taskSystemTunningPanel.getJButtonLastStep().addMouseListener(new MouseAdapter() {
            /**
             * mouseClicked
             *
             * @param mouseEvent mouseEvent
             */
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                jTaskPanel.getOptionJPanel().remove(taskSystemTunningPanel);
                jTaskPanel.getOptionJPanelContent().setVisible(true);
                jTaskPanel.getOptionJPanel().repaint();
            }
        });
    }

    /**
     * listenerJPanelSouth
     *
     * @param taskSystemTunningPanel taskSystemTunningPanel
     */
    public void listenerJPanelSouth(SystemTunningConfigPanel taskSystemTunningPanel) {
        taskSystemTunningPanel.getJPanelSouth().addComponentListener(new ComponentAdapter() {
            /**
             * componentResized
             *
             * @param componentEvent componentEvent
             */
            public void componentResized(ComponentEvent componentEvent) {
                int width = taskSystemTunningPanel.getJPanelSouth().getWidth();
                taskSystemTunningPanel.getJButtonLastStep()
                    .setBounds(LayoutConstants.NUMBER_STEP + (width - LayoutConstants.WINDOW_WIDTH),
                        LayoutConstants.DEVICES_HEIGHT, LayoutConstants.DEVICE_ADD_WIDTH,
                        LayoutConstants.CHOOSE_HEIGHT);
                taskSystemTunningPanel.getJButtonStartTask()
                    .setBounds(LayoutConstants.POSITION_TASK_X + (width - LayoutConstants.WINDOW_WIDTH),
                        LayoutConstants.DEVICES_HEIGHT, LayoutConstants.DEVICE_ADD_WIDTH,
                        LayoutConstants.CHOOSE_HEIGHT);
            }
        });
    }

    /**
     * recordSetting
     *
     * @param recordSettingLabel     recordSettingLabel
     * @param traceCommandLabel      traceCommandLabel
     * @param jPanelCenterSouthRight jPanelCenterSouthRight
     * @param jPanelCenterSouthWest  jPanelCenterSouthWest
     */
    public void recordSetting(JLabel recordSettingLabel, JLabel traceCommandLabel, JPanel jPanelCenterSouthRight,
        JPanel jPanelCenterSouthWest) {
        recordSettingLabelRightShow(jPanelCenterSouthRight);
        recordSettingLabel.addMouseListener(new MouseAdapter() {
            /**
             * mouseClicked
             *
             * @param mouseEvent mouseEvent
             */
            public void mouseClicked(MouseEvent mouseEvent) {
                recordSettingLabel.setOpaque(true);
                traceCommandLabel.setOpaque(false);
                recordSettingLabelRightShow(jPanelCenterSouthRight);
                jPanelCenterSouthWest.updateUI();
                jPanelCenterSouthWest.repaint();
            }
        });
    }

    /**
     * radioButton
     *
     * @param jPanelCenterSouthRight jPanelCenterSouthRight
     * @param rb1                    rb1
     * @param rb2                    rb2
     * @param rb3                    rb3
     */
    public void radioButton(JPanel jPanelCenterSouthRight, JRadioButton rb1, JRadioButton rb2, JRadioButton rb3) {
        jPanelCenterSouthRight.removeAll();
        rb1.setBackground(ColorConstants.SYSTEM_TUNNING_WEST_LABEL);
        rb2.setBackground(ColorConstants.SYSTEM_TUNNING_WEST_LABEL);
        rb3.setBackground(ColorConstants.SYSTEM_TUNNING_WEST_LABEL);
        JLabel jLabel = new JLabel("Record model：");
        jLabel.setBounds(LayoutConstants.SYSTEM_TUNNING_LABEL_INTT_X,
            LayoutConstants.SYSTEM_TUNNING_RECORD_MODEL_LABLE_INTT_Y, LayoutConstants.SYSTEM_TUNNING_LABLE_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(rb1);
        rb1.setBounds(LayoutConstants.SYSTEM_TUNNING_LABLE_RADIO_INTT_X, LayoutConstants.SYSTEM_TUNNING_LABLE_INTT_Y,
            LayoutConstants.SYSTEM_TUNNING_RECORD_SETTING_RADIO_WIDTH, LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        rb2.setBounds(LayoutConstants.SYSTEM_TUNNING_LABLE_RADIO_DECOND_INTT_X,
            LayoutConstants.SYSTEM_TUNNING_LABLE_INTT_Y, LayoutConstants.SYSTEM_TUNNING_RECORD_SETTING_RADIO_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        rb3.setBounds(LayoutConstants.SYSTEM_TUNNING_LABLE_RADIO_THREE_INTT_X,
            LayoutConstants.SYSTEM_TUNNING_LABLE_INTT_Y, LayoutConstants.SYSTEM_TUNNING_RECORD_SETTING_RADIO_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        jPanelCenterSouthRight.add(jLabel);
        jPanelCenterSouthRight.add(rb1);
    }

    /**
     * recordSettingLabelRightShow
     *
     * @param jPanelCenterSouthRight jPanelCenterSouthRight
     */
    public void recordSettingLabelRightShow(JPanel jPanelCenterSouthRight) {
        // 单选按钮、进度条等
        JRadioButton rb1 = new JRadioButton("Stop when full", true);
        JRadioButton rb2 = new JRadioButton("Ring buffer"); // 创建JRadioButton对象
        JRadioButton rb3 = new JRadioButton("Long trace");
        radioButton(jPanelCenterSouthRight, rb1, rb2, rb3);
        // Long trace 选中显示
        JLabel memoryLabel3 = new JLabel("Max file size:");
        memoryLabel3.setBounds(LayoutConstants.SYSTEM_TUNNING_LABEL_INTT_X,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_MAX_FILE_SIZE_SLIDE_INT_LABEL_Y,
            LayoutConstants.SYSTEM_TUNNING_LABLE_WIDTH, LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        JSlider sliderMaxFileSize = new JSlider(LayoutConstants.DEFAULT_NUMBER,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_MAX_IN_MEMORY_NUMBER);
        sliderMaxFileSize.setValue(LayoutConstants.SYSTEM_TUNNING_RECORD_SETTING_DEFAULT_NUMBER);
        // memoryThree
        JLabel memoryThree = this.memoryThreeAttribute();
        JLabel memoryLabel4 = new JLabel("Flush on disk every:");
        memoryLabel4.setBounds(LayoutConstants.SYSTEM_TUNNING_LABEL_INTT_X,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_FLUSH_ON_DISK_SLIDE_INT_Y,
            LayoutConstants.SYSTEM_TUNNING_LABLE_WIDTH, LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        JSlider sliderFLushOnDisk = new JSlider(LayoutConstants.DEFAULT_NUMBER,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_MAX_IN_MEMORY_NUMBER);
        sliderFLushOnDisk.setValue(LayoutConstants.SYSTEM_TUNNING_RECORD_SETTING_DEFAULT_NUMBER);
        // memoryFLushOnDisk
        JLabel memoryFLushOnDisk = this.memoryFLushOnDisk();
        // Long trace 选中显示---结束
        rb1.addActionListener(new ActionListener() {
            /**
             * actionPerformed
             *
             * @param actionEvent actionEvent
             */
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                jPanelCenterSouthRight.remove(memoryLabel3);
                jPanelCenterSouthRight.remove(memoryThree);
                jPanelCenterSouthRight.remove(sliderMaxFileSize);
                jPanelCenterSouthRight.remove(memoryLabel4);
                jPanelCenterSouthRight.remove(memoryFLushOnDisk);
                jPanelCenterSouthRight.remove(sliderFLushOnDisk);
                jPanelCenterSouthRight.updateUI();
                jPanelCenterSouthRight.repaint();
            }
        });
        jPanelCenterSouthRightAddInMemory(jPanelCenterSouthRight);
        jPanelCenterSouthRightAddMaxDuration(jPanelCenterSouthRight);
        jPanelCenterSouthRight.setBackground(ColorConstants.SYSTEM_TUNNING_WEST_LABEL);
        jPanelCenterSouthRight.updateUI();
        jPanelCenterSouthRight.repaint();
    }

    /**
     * memoryThreeAttribute
     *
     * @return JLabel
     */
    public JLabel memoryThreeAttribute() {
        JLabel memoryThree =
            new JLabel("" + LayoutConstants.SYSTEM_TUNNING_RECORD_SETTING_DEFAULT_NUMBER + " MB", JLabel.CENTER);
        memoryThree.setFont(new Font(Font.SANS_SERIF, Font.BOLD, LayoutConstants.SYSTEM_TUNNING_LABEL_FONT));
        memoryThree.setForeground(Color.white);
        memoryThree.setVerticalTextPosition(JLabel.CENTER);
        memoryThree.setHorizontalTextPosition(JLabel.CENTER);
        memoryThree.setOpaque(true);
        memoryThree.setBounds(LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SHOW_NUMBER_INTI_X,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_MAX_FILE_SIZE_SLIDE_LABEL_INT_LABEL_Y,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SHOW_NUMBER_INTI_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_RECORD_SETTING_SLIDE_LABEL);
        return memoryThree;
    }

    /**
     * memoryFLushOnDisk
     *
     * @return JLabel
     */
    public JLabel memoryFLushOnDisk() {
        JLabel memoryFLushOnDisk =
            new JLabel("" + LayoutConstants.SYSTEM_TUNNING_RECORD_SETTING_DEFAULT_NUMBER + " ms", JLabel.CENTER);
        memoryFLushOnDisk.setFont(new Font(Font.SANS_SERIF, Font.BOLD, LayoutConstants.SYSTEM_TUNNING_LABEL_FONT));
        memoryFLushOnDisk.setForeground(Color.white);
        memoryFLushOnDisk.setVerticalTextPosition(JLabel.CENTER);
        memoryFLushOnDisk.setHorizontalTextPosition(JLabel.CENTER);
        memoryFLushOnDisk.setOpaque(true);
        memoryFLushOnDisk.setBounds(LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SHOW_NUMBER_INTI_X,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_FLUSH_ON_DISK_SLIDE_LABEL_INT_Y,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SHOW_NUMBER_INTI_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_RECORD_SETTING_SLIDE_LABEL);
        return memoryFLushOnDisk;
    }

    /**
     * jPanelCenterSouthRightAddInMemory
     *
     * @param jPanelCenterSouthRight jPanelCenterSouthRight
     */
    public void jPanelCenterSouthRightAddInMemory(JPanel jPanelCenterSouthRight) {
        JLabel memoryLabel2 = new JLabel("In-memory buffer size:");
        memoryLabel2.setBounds(LayoutConstants.SYSTEM_TUNNING_LABEL_INTT_X,
            LayoutConstants.SYSTEM_TUNNING_LABLE_INTT_Y + LayoutConstants.SYSTEM_TUNNING_LABLE_SPACING_Y,
            LayoutConstants.SYSTEM_TUNNING_LABLE_WIDTH, LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        jPanelCenterSouthRight.add(memoryLabel2);
        // memoryTWO
        JLabel memoryTWO =
            new JLabel("" + LayoutConstants.SYSTEM_TUNNING_RECORD_SETTING_DEFAULT_NUMBER + " MB", JLabel.CENTER);
        memoryTWO.setFont(new Font(Font.SANS_SERIF, Font.BOLD, LayoutConstants.SYSTEM_TUNNING_LABEL_FONT));
        memoryTWO.setForeground(Color.white);
        memoryTWO.setVerticalTextPosition(JLabel.CENTER);
        memoryTWO.setHorizontalTextPosition(JLabel.CENTER);
        memoryTWO.setOpaque(true);
        memoryTWO.setBounds(LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SHOW_NUMBER_INTI_X,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_RIGHT_LABEL_INT_Y,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SHOW_NUMBER_INTI_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_RECORD_SETTING_SLIDE_LABEL);
        jPanelCenterSouthRight.add(memoryTWO);
        jProgressBarInMemory(jPanelCenterSouthRight, memoryTWO);
    }

    /**
     * jPanelCenterSouthRightAddMaxDuration
     *
     * @param jPanelCenterSouthRight jPanelCenterSouthRight
     */
    public void jPanelCenterSouthRightAddMaxDuration(JPanel jPanelCenterSouthRight) {
        JLabel durationLabel = new JLabel("Max duration:");
        durationLabel.setBounds(LayoutConstants.SYSTEM_TUNNING_LABEL_INTT_X,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_MAX_DURATION_LABEL,
            LayoutConstants.SYSTEM_TUNNING_LABLE_WIDTH, LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        jPanelCenterSouthRight.add(durationLabel);
        // durationTWO
        JLabel durationTWO = new JLabel(
            "00:00:" + LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_MIN_DURATION_NUMBER + " h:m:s ",
            JLabel.CENTER);
        durationTWO.setFont(new Font(Font.SANS_SERIF, Font.BOLD, LayoutConstants.SYSTEM_TUNNING_LABEL_FONT));
        durationTWO.setForeground(Color.white);
        durationTWO.setVerticalTextPosition(JLabel.CENTER);
        durationTWO.setHorizontalTextPosition(JLabel.CENTER);
        durationTWO.setOpaque(true);
        durationTWO.setBounds(LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SHOW_NUMBER_INTI_X,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_MAX_DURATION_LABEL_INIT_Y,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SHOW_NUMBER_INTI_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_RECORD_SETTING_SLIDE_LABEL);
        jPanelCenterSouthRight.add(durationTWO);
        jProgressBarDuration(jPanelCenterSouthRight, durationTWO);
    }

    /**
     * jProgressBarInMemory
     *
     * @param jPanelCenterSouthRight jPanelCenterSouthRight
     * @param memoryTWO              memoryTWO
     * @return JSlider
     */
    public JSlider jProgressBarInMemory(JPanel jPanelCenterSouthRight, JLabel memoryTWO) {
        JSlider slider = new JSlider(LayoutConstants.DEFAULT_NUMBER,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_MAX_IN_MEMORY_NUMBER);
        slider.setValue(LayoutConstants.SYSTEM_TUNNING_RECORD_SETTING_DEFAULT_NUMBER);
        slider.setPreferredSize(
            new Dimension(LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_MAX_DURATION_INTI_Y,
                LayoutConstants.DEFAULT_NUMBER));
        slider.setBounds(LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_INT_X,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_INT_Y,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_INT_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_INT_HIGHT);
        jPanelCenterSouthRight.add(slider);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                memoryTWO.setText("" + slider.getValue() + " MB");
                memoryTWO.setFont(new Font(Font.SANS_SERIF, Font.BOLD, LayoutConstants.SYSTEM_TUNNING_LABEL_FONT));
                memoryTWO.setForeground(Color.white);
                memoryTWO.setVerticalTextPosition(JLabel.CENTER);
                memoryTWO.setHorizontalTextPosition(JLabel.CENTER);
                inMemoryValue = slider.getValue();
            }
        });
        return slider;
    }

    /**
     * jProgressBarMaxFileSize
     *
     * @param jPanelCenterSouthRight jPanelCenterSouthRight
     * @param memoryThree            memoryThree
     * @param sliderMaxFileSize      sliderMaxFileSize
     * @return JSlider
     */
    public JSlider jProgressBarMaxFileSize(JPanel jPanelCenterSouthRight, JLabel memoryThree,
        JSlider sliderMaxFileSize) {
        sliderMaxFileSize.setValue(LayoutConstants.SYSTEM_TUNNING_RECORD_SETTING_DEFAULT_NUMBER);
        sliderMaxFileSize.setPreferredSize(
            new Dimension(LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_MAX_DURATION_INTI_Y,
                LayoutConstants.DEFAULT_NUMBER));
        sliderMaxFileSize.setBounds(LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_INT_X,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_MAX_FILE_SIZE_SLIDE_INT_LABEL_Y
                + LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_AND_LABEL_SPACING_Y,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_INT_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_INT_HIGHT);
        jPanelCenterSouthRight.add(sliderMaxFileSize);
        sliderMaxFileSize.addChangeListener(new ChangeListener() {
            /**
             * stateChanged
             *
             * @param changeEvent changeEvent
             */
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                memoryThree.setText("" + sliderMaxFileSize.getValue() + " MB");
                memoryThree.setFont(new Font(Font.SANS_SERIF, Font.BOLD, LayoutConstants.SYSTEM_TUNNING_LABEL_FONT));
                memoryThree.setForeground(Color.white);
                memoryThree.setVerticalTextPosition(JLabel.CENTER);
                memoryThree.setHorizontalTextPosition(JLabel.CENTER);
                maxFileSize = sliderMaxFileSize.getValue();
            }
        });
        return sliderMaxFileSize;
    }

    /**
     * jProgressBarFLushOnDisk
     *
     * @param jPanelCenterSouthRight jPanelCenterSouthRight
     * @param memoryFLushOnDisk      memoryFLushOnDisk
     * @param sliderFLushOnDisk      sliderFLushOnDisk
     * @return JSlider
     */
    public JSlider jProgressBarFLushOnDisk(JPanel jPanelCenterSouthRight, JLabel memoryFLushOnDisk,
        JSlider sliderFLushOnDisk) {
        sliderFLushOnDisk.setValue(LayoutConstants.SYSTEM_TUNNING_RECORD_SETTING_DEFAULT_NUMBER);
        sliderFLushOnDisk.setPreferredSize(
            new Dimension(LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_MAX_DURATION_INTI_Y,
                LayoutConstants.DEFAULT_NUMBER));
        sliderFLushOnDisk.setBounds(LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_INT_X,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_FLUSH_ON_DISK_SLIDE_INT_Y
                + LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_AND_LABEL_SPACING_Y,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_INT_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_INT_HIGHT);
        jPanelCenterSouthRight.add(sliderFLushOnDisk);
        sliderFLushOnDisk.addChangeListener(new ChangeListener() {
            /**
             * stateChanged
             *
             * @param changeEvent changeEvent
             */
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                memoryFLushOnDisk.setText("" + sliderFLushOnDisk.getValue() + " ms");
                memoryFLushOnDisk
                    .setFont(new Font(Font.SANS_SERIF, Font.BOLD, LayoutConstants.SYSTEM_TUNNING_LABEL_FONT));
                memoryFLushOnDisk.setForeground(Color.white);
                memoryFLushOnDisk.setVerticalTextPosition(JLabel.CENTER);
                memoryFLushOnDisk.setHorizontalTextPosition(JLabel.CENTER);
                flushOnDisk = sliderFLushOnDisk.getValue();
            }
        });
        return sliderFLushOnDisk;
    }

    /**
     * jProgressBarDuration
     *
     * @param jPanelCenterSouthRight jPanelCenterSouthRight
     * @param durationTWO            durationTWO
     * @return JSlider
     */
    public JSlider jProgressBarDuration(JPanel jPanelCenterSouthRight, JLabel durationTWO) {
        JSlider slider = new JSlider(LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_MIN_DURATION_NUMBER,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_MAX_DURATION_NUMBER);
        slider.setValue(LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_MIN_DURATION_NUMBER);
        slider.setPreferredSize(
            new Dimension(LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_MAX_DURATION_INTI_Y,
                LayoutConstants.DEFAULT_NUMBER));
        slider.setBounds(LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_INT_X,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_MAX_DURATION_LABEL
                + LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_AND_LABEL_SPACING_Y,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_INT_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_INT_HIGHT);
        jPanelCenterSouthRight.add(slider);
        slider.addChangeListener(new ChangeListener() {
            /**
             * stateChanged
             *
             * @param changeEvent changeEvent
             */
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                seconds = slider.getValue() % SystemTunningProbesCheckbox.TIME_CONVERSION_UNIT;
                minutes = (slider.getValue() / SystemTunningProbesCheckbox.TIME_CONVERSION_UNIT)
                    % SystemTunningProbesCheckbox.TIME_CONVERSION_UNIT;
                hours = slider.getValue() / (SystemTunningProbesCheckbox.TIME_CONVERSION_UNIT
                    * SystemTunningProbesCheckbox.TIME_CONVERSION_UNIT);
                durationTWO.setText(" " + String.format(Locale.ENGLISH, "%02d", hours) + ":" + String
                    .format(Locale.ENGLISH, "%02d", minutes) + ":" + String.format(Locale.ENGLISH, "%02d", seconds)
                    + " h:m:s ");
                durationTWO.setFont(new Font(Font.SANS_SERIF, Font.BOLD, LayoutConstants.SYSTEM_TUNNING_LABEL_FONT));
                durationTWO.setForeground(Color.white);
                durationTWO.setVerticalTextPosition(JLabel.CENTER);
                durationTWO.setHorizontalTextPosition(JLabel.CENTER);
                maxDuration = slider.getValue();
            }
        });
        return slider;
    }

    /**
     * traceCommand
     *
     * @param recordSettingLabel     recordSettingLabel
     * @param traceCommandLabel      traceCommandLabel
     * @param jPanelCenterSouthRight jPanelCenterSouthRight
     * @param jPanelCenterSouthWest  jPanelCenterSouthWest
     */
    public void traceCommand(JLabel recordSettingLabel, JLabel traceCommandLabel, JPanel jPanelCenterSouthRight,
        JPanel jPanelCenterSouthWest) {
        traceCommandLabel.addMouseListener(new MouseAdapter() {
            /**
             * mouseClicked
             *
             * @param mouseEvent mouseEvent
             */
            public void mouseClicked(MouseEvent mouseEvent) {
                recordSettingLabel.setOpaque(false);
                traceCommandLabel.setOpaque(true);
                try {
                    traceCommandLabelRightShow(jPanelCenterSouthRight);
                } catch (GrpcException ex) {
                    ex.printStackTrace();
                }
                jPanelCenterSouthWest.updateUI();
                jPanelCenterSouthWest.repaint();
            }
        });
    }

    /**
     * traceCommandLabelRightShow
     *
     * @param jPanelCenterSouthRight jPanelCenterSouthRight
     * @throws GrpcException GrpcException
     */
    public void traceCommandLabelRightShow(JPanel jPanelCenterSouthRight) throws GrpcException {
        jPanelCenterSouthRight.removeAll();
        String str = SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_NO_SELECT_HEAD;
        // 获得用户选中的复选框
        getUserCheckBox();
        // 走perfetto from siwei
        getUserCheckBoxForPerfetto();
        getStrResultForPerfetto();
        str = str.concat(new TraceManager()
            .createSessionRequestPerfetto(deviceIPPortInfo, getUserCheckBoxForPerfettoStr, maxDuration, true));
        if (str == null) {
            str = "";
        }
        str = str.concat(SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_NO_SELECT_END);
        JTextArea jta = new JTextArea(str);
        jta.setLineWrap(true); // 设置文本域中的文本为自动换行
        jta.setForeground(Color.gray); // 设置组件的背景色
        jta.setEditable(false);
        jta.setBackground(ColorConstants.SYSTEM_TUNNING_WEST_LABEL); // 设置按钮背景色
        JScrollPane jsp = new JScrollPane(jta); // 将文本域放入滚动窗口
        jsp.setBounds(LayoutConstants.SYSTEM_TUNNING_TRACE_COMMAND_TEXT_AREA_INTT_X,
            LayoutConstants.SYSTEM_TUNNING_TRACE_COMMAND_TEXT_AREA_INTT_Y,
            LayoutConstants.SYSTEM_TUNNING_TRACE_COMMAND_TEXTAREA_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_TRACE_COMMAND_TEXTAREA_HEIGHT);
        jPanelCenterSouthRight.add(jsp);
        JButton jButtonSave = new JButton(new ImageIcon(
            SystemTunningConfigEvent.class.getClassLoader().getResource("images/copy.png")));
        jButtonSave.setOpaque(true);
        jButtonSave.setCursor(new Cursor(LayoutConstants.SYSTEM_TUNNING_TRACE_COMMAND_COPY_FUNCTION_CURSOR));
        jButtonSave.setBounds(LayoutConstants.SYSTEM_TUNNING_TRACE_COMMAND_COPY_FUNCTION_INIT_X,
            LayoutConstants.SYSTEM_TUNNING_TRACE_COMMAND_TEXT_AREA_INTT_Y,
            LayoutConstants.SYSTEM_TUNNING_TRACE_COMMAND_COPY_FUNCTION_INIT_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_TRACE_COMMAND_COPY_FUNCTION_INIT_WIDTH);
        jButtonSave.setBorderPainted(false);
        jButtonSave.addActionListener(new ActionListener() {
            /**
             * actionPerformed
             *
             * @param actionEvent actionEvent
             */
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection contents = new StringSelection(jta.getText());
                clipboard.setContents(contents, SystemTunningConfigEvent.this);
            }
        });
        jPanelCenterSouthRight.add(jButtonSave);
        jPanelCenterSouthRight.repaint();
    }

    /**
     * getParamListFromString
     *
     * @param param param
     * @param str   str
     */
    public void getParamListFromString(HashMap<String, ArrayList<String>> param, String str) {
        if (str != null && !str.isEmpty()) {
            String[] oneCheckBoxArray = str.split("\n");
            for (int i = 0; i < oneCheckBoxArray.length; i++) {
                if (oneCheckBoxArray[i] != null && !oneCheckBoxArray[i].isEmpty()) {
                    String[] everyLine = oneCheckBoxArray[i].split(":");
                    if (oneCheckBoxArray[i].contains(everyLine[0].trim())) {
                        if (param.get(everyLine[0].trim()) != null) {
                            param.get(everyLine[0].trim()).add(oneCheckBoxArray[i]);
                        } else {
                            ArrayList<String> everyList = new ArrayList<>();
                            everyList.add(oneCheckBoxArray[i]);
                            param.put(everyLine[0].trim(), everyList);
                        }
                    }
                }
            }
        }
    }

    /**
     * devicesInfoJComboBoxUpdate
     *
     * @param taskSystemTunningPanel taskSystemTunningPanel
     */
    public void devicesInfoJComboBoxUpdate(SystemTunningConfigPanel taskSystemTunningPanel) {
        QuartzManager.getInstance().addExecutor(DEVICEREFRESH, new Runnable() {
            /**
             * run
             */
            @Override
            public void run() {
                List<DeviceIPPortInfo> deviceInfos = MultiDeviceManager.getInstance().getAllDeviceIPPortInfos();
                taskSystemTunningPanel.setDeviceInfos(deviceInfos);
                Vector<String> items = new Vector<>();
                deviceInfos.forEach(deviceInfo -> {
                    items.add(deviceInfo.getDeviceName());
                });
                if (!oldDevice.equals(items)) {
                    oldDevice = items;
                    taskSystemTunningPanel.getJComboBoxPhone().setModel(new DefaultComboBoxModel(items));
                }
            }
        });
        QuartzManager.getInstance()
            .startExecutor(DEVICEREFRESH, LayoutConstants.DEFAULT_NUMBER, LayoutConstants.NUMBER_THREAD);
    }

    /**
     * itemStateChanged
     *
     * @param taskSystemTunningPanel taskSystemTunningPanel
     */
    public void itemStateChanged(SystemTunningConfigPanel taskSystemTunningPanel) {
        taskSystemTunningPanel.getJComboBoxPhone().addItemListener(new ItemListener() {
            /**
             * itemStateChanged
             *
             * @param itemEvent itemEvent
             */
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                // 获取选中项，设置对象信息用于查询对应的进程信息
                for (DeviceIPPortInfo deviceInfo : taskSystemTunningPanel.getDeviceInfos()) {
                    if (deviceInfo.getDeviceName()
                        .equals(taskSystemTunningPanel.getJComboBoxPhone().getSelectedItem())) {
                        deviceIPPortInfo = deviceInfo;
                    }
                }
            }
        });
        if (taskSystemTunningPanel.getDeviceInfos() != null && taskSystemTunningPanel.getDeviceInfos().size() > 0
            && deviceIPPortInfo == null) {
            deviceIPPortInfo = taskSystemTunningPanel.getDeviceInfos().get(0);
        }
    }

    /**
     * lostOwnership
     *
     * @param clipboard clipboard
     * @param contents  contents
     */
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
}
