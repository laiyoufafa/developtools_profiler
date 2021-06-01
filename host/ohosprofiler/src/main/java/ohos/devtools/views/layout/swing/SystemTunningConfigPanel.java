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

package ohos.devtools.views.layout.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.Clipboard;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.service.MultiDeviceManager;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.event.SystemTunningConfigEvent;

/**
 * 三级System tuning场景界面
 *
 * @version 1.0
 * @date 2021/03/8 11:11
 **/
public class SystemTunningConfigPanel extends JPanel {
    Clipboard clipboard;
    /**
     * 三级界面边框布局管理器，top容器
     */
    private JPanel jPanelNorth = new JPanel(null);

    /**
     * 设备连接方式下拉框(usb，wifi，蓝牙)
     */
    private JComboBox<String> jComboBoxConnect = new JComboBox<String>();

    /**
     * 设备名称下拉框
     */
    private JComboBox<String> jComboBoxPhone = new JComboBox<String>();

    /**
     * 设备信息集合
     */
    private List<DeviceIPPortInfo> deviceInfos = null;

    /**
     * 三级界面边框布局管理器，Center容器
     */
    private JPanel jPanelCenter = new JPanel(new BorderLayout());

    /**
     * 三级界面边框布局管理器，South容器
     */
    private JPanel jPanelSouth = new JPanel(null);

    /**
     * top容器中文本描述
     */
    private JLabel jLabelTaskTun = new JLabel("Trace config & Probes");

    /**
     * top容器中文本描述
     */
    private JLabel jLabelDeviceSet = new JLabel("Task scene: System tuning");

    /**
     * Center容器中，Monitor Items页面复选框容器
     */
    private JPanel jPanelRightMemory = new JPanel(null);

    /**
     * Monitor Items页面复选框存放数组
     */
    private JCheckBox[] jCheckBoxs = new JCheckBox[LayoutConstants.INDEX_SEVEN];

    /**
     * 三级页面事件类
     */
    private SystemTunningConfigEvent taskSystemTunningPanelEvent = new SystemTunningConfigEvent();

    /**
     * Center容器中，左边布局滚动条容器
     */
    private JPanel scrollPane = new JPanel(null);

    /**
     * 返回上一页按钮
     */
    private JButton jButtonLastStep = new JButton("Last Step");

    /**
     * 开始任务按钮
     */
    private JButton jButtonStartTask = new JButton("Start Task");

    public SystemTunningConfigPanel(TaskPanel jTaskPanel) {
        // 设置三级界面布局方式为边框布局管理
        this.setLayout(new BorderLayout());
        // 设置top容器，Center容器，South容器的属性
        setAttributes();
        // Center容器设置
        setjPanelCenter();
        // South容器设置
        setJPanelSouth(jTaskPanel);
    }

    /**
     * 设置top容器，Center容器，South容器的属性
     */
    public void setAttributes() {
        jPanelNorth.setOpaque(true);
        jPanelNorth.setBackground(ColorConstants.TOP_PANEL);
        jPanelCenter.setOpaque(true);
        jPanelCenter.setBackground(ColorConstants.TOP_PANEL);
        jPanelSouth.setOpaque(true);
        jPanelSouth.setBackground(ColorConstants.TOP_PANEL);
        jPanelNorth.setPreferredSize(new Dimension(LayoutConstants.TOP_SOUTH_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_TRACE_RECORDING_SETTING_SLIDE_INT_Y));
        jPanelSouth.setPreferredSize(new Dimension(LayoutConstants.WINDOW_WIDTH, LayoutConstants.TOP_SOUTH_HEIGHT));
        this.add(jPanelNorth, BorderLayout.NORTH);
        this.add(jPanelCenter, BorderLayout.CENTER);
        this.add(jPanelSouth, BorderLayout.SOUTH);

        JLabel jLabelDeviceNum = new JLabel("Device");
        Font fontTaskTun = new Font(Font.DIALOG, Font.BOLD, LayoutConstants.TASK_LABEL_FONT);
        jLabelDeviceNum.setFont(fontTaskTun);
        jLabelDeviceNum.setForeground(Color.white);
        jLabelDeviceNum
            .setBounds(LayoutConstants.CHOOSE_HEIGHT, LayoutConstants.JP_RIGHT_WIDTH, LayoutConstants.TASK_LABEL_WIDTH,
                LayoutConstants.TASK_LABEL_HEIGHT);
        jComboBoxConnect.addItem("USB");
        jComboBoxConnect
            .setBounds(LayoutConstants.CHOOSE_HEIGHT, LayoutConstants.SYSTEM_TUNNING_DEVICE_COMBOX_EQUIPMENT_INIT_Y,
                LayoutConstants.CON_BOX_WIDTH, LayoutConstants.TASK_SCENE_Y);
        deviceInfos = MultiDeviceManager.getInstance().getAllDeviceIPPortInfos();
        taskSystemTunningPanelEvent.devicesInfoJComboBoxUpdate(this);
        jComboBoxPhone
            .setBounds(LayoutConstants.RIGHT_BUN_WIDTH, LayoutConstants.SYSTEM_TUNNING_DEVICE_COMBOX_EQUIPMENT_INIT_Y,
                LayoutConstants.HEIGHT_PRESSE, LayoutConstants.TASK_SCENE_Y);
        taskSystemTunningPanelEvent.itemStateChanged(this);

        jLabelTaskTun.setBounds(LayoutConstants.SYSTEM_TUNNING_CENTER_LEFT_AND_RIGHT_X,
            LayoutConstants.SYSTEM_TUNNING_TRACE_CONFIG_AND_PROBES_INIT_Y, LayoutConstants.TASK_LABEL_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_ALL_LABEL_INIT_HEIGHT);
        jLabelTaskTun.setFont(fontTaskTun);
        jLabelTaskTun.setForeground(Color.WHITE);
        jPanelNorth.add(jLabelDeviceNum);
        jPanelNorth.add(jComboBoxConnect);
        jPanelNorth.add(jComboBoxPhone);
        jPanelNorth.add(jLabelTaskTun);
        Font fontDeviceSet = new Font(Font.DIALOG, Font.BOLD, LayoutConstants.TASK_DEC_FONT);
        jLabelDeviceSet.setFont(fontDeviceSet);
        jLabelDeviceSet.setBounds(LayoutConstants.TASK_DEC_X, LayoutConstants.SYSTEM_TUNNING_SAMLL_TASH_SCENEINIT_Y,
            LayoutConstants.TASK_DEC_WIDTH, LayoutConstants.TASK_DEC_HEIGHT);
        jPanelNorth.add(jLabelDeviceSet);
    }

    /**
     * setDeviceInfos
     *
     * @param deviceInfos deviceInfos
     */
    public void setDeviceInfos(List<DeviceIPPortInfo> deviceInfos) {
        this.deviceInfos = deviceInfos;
    }

    /**
     * getJComboBoxPhone
     *
     * @return JComboBox
     */
    public JComboBox getJComboBoxPhone() {
        return jComboBoxPhone;
    }

    /**
     * getDeviceInfos
     *
     * @return List<DeviceIPPortInfo>
     */
    public List<DeviceIPPortInfo> getDeviceInfos() {
        return deviceInfos;
    }

    /**
     * system tunning 设置页面
     */
    public void setjPanelCenter() {
        // 左右黑色边框
        JPanel jPanelCenterLeft = new JPanel();
        jPanelCenterLeft.setPreferredSize(
            new Dimension(LayoutConstants.SYSTEM_TUNNING_CENTER_LEFT_AND_RIGHT_X, LayoutConstants.DEFAULT_NUMBER));
        jPanelCenterLeft.setBackground(ColorConstants.SYSTEM_TUNNING_SETTING_BACK);
        JPanel jPanelCenterEast = new JPanel();
        jPanelCenterEast.setPreferredSize(
            new Dimension(LayoutConstants.SYSTEM_TUNNING_CENTER_LEFT_AND_RIGHT_X, LayoutConstants.DEFAULT_NUMBER));
        jPanelCenterEast.setBackground(ColorConstants.SYSTEM_TUNNING_SETTING_BACK);

        JLabel traceConfig = new JLabel("Trace config", JLabel.CENTER);
        traceConfig.setForeground(Color.white);
        traceConfig
            .setFont(new Font(Font.DIALOG, Font.BOLD, LayoutConstants.SYSTEM_TUNNING_TABBEDPANE_TRACE_CONFIG_FONT));
        traceConfig.setPreferredSize(new Dimension(LayoutConstants.SYSTEM_TUNNING_TABBEDPANE_TRACE_CONFIG_WIDTH_INIT,
            LayoutConstants.SYSTEM_TUNNING_TABBEDPANE_TRACE_CONFIG_HEIGHT_INIT));
        JLabel probes = new JLabel("Probes", JLabel.CENTER);
        probes.setForeground(Color.white);
        probes.setFont(new Font(Font.DIALOG, Font.BOLD, LayoutConstants.SYSTEM_TUNNING_TABBEDPANE_TRACE_CONFIG_FONT));
        probes.setPreferredSize(new Dimension(LayoutConstants.SYSTEM_TUNNING_TABBEDPANE_TRACE_CONFIG_WIDTH_INIT,
            LayoutConstants.SYSTEM_TUNNING_TABBEDPANE_TRACE_CONFIG_HEIGHT_INIT));

        JPanel traceConfigTab = new JPanel(new BorderLayout());
        traceConfigTab.add(traceConfig);
        JPanel probesTab = new JPanel(new BorderLayout());
        JTabbedPane tab = new JTabbedPane();
        tab.addTab("", traceConfigTab);
        tab.addTab("", probesTab);
        tab.setTabComponentAt(tab.indexOfComponent(traceConfigTab), traceConfig);
        tab.setTabComponentAt(tab.indexOfComponent(probesTab), probes);

        JPanel jPanelCenterTop = new JPanel(new BorderLayout());
        jPanelCenterTop.add(tab);
        jPanelCenterTop.setBackground(ColorConstants.SYSTEM_TUNNING_SETTING_CENTER);
        JPanel jPanelCenterSouthWest = new JPanel(null);
        JPanel jPanelCenterSouthRight = new JPanel(null);
        traceConfig(jPanelCenterSouthWest, jPanelCenterSouthRight);
        traceConfigTab.add(jPanelCenterSouthWest, BorderLayout.WEST);
        traceConfigTab.add(jPanelCenterSouthRight, BorderLayout.CENTER);
        JPanel jPanelCenterSouthWestP2 = new JPanel(null);
        JPanel jPanelCenterSouthEastP2 = new JPanel(null);
        systemTunningProbes(jPanelCenterSouthWestP2, jPanelCenterSouthEastP2);
        probesTab.add(jPanelCenterSouthWestP2, BorderLayout.WEST);
        probesTab.add(jPanelCenterSouthEastP2, BorderLayout.CENTER);
        jPanelCenter.add(jPanelCenterLeft, BorderLayout.WEST);
        jPanelCenter.add(jPanelCenterTop, BorderLayout.CENTER);
        jPanelCenter.add(jPanelCenterEast, BorderLayout.EAST);
    }

    /**
     * traceConfig
     *
     * @param jPanelCenterSouthWest  jPanelCenterSouthWest
     * @param jPanelCenterSouthRight jPanelCenterSouthRight
     */
    public void traceConfig(JPanel jPanelCenterSouthWest, JPanel jPanelCenterSouthRight) {
        JLabel recordSettingLabel = new JLabel(
            "<html><p style=\"margin-left:28px;font-size:13px;" + "text-align:left;color:white;\">Record Setting</p>"
                + "<p style=\"margin-top:0px;margin-left:28px;font-size:9px;text-align:left;"
                + "color:#757784;\">Buffer mode.size and duration</p></html>", JLabel.CENTER);
        recordSettingLabel.setOpaque(true);

        recordSettingLabel.setBounds(LayoutConstants.DEFAULT_NUMBER, LayoutConstants.SYSTEM_TUNNING_LABLE_INTT_Y,
            LayoutConstants.SYSTEM_TUNNING_LABLE_WIDTH, LayoutConstants.SYSTEM_TUNNING_SLIDE_LABLE_HEIGHT);
        JLabel traceCommandLabel = new JLabel("<html><p style=\"margin-top:5px;font-size:13px;"
            + "text-align:left;color:white;\">Trace command</p><p style=\"margin-top:0px;font-size:9px;text-align:left;"
            + "color:#757784;\">Manually record trace</p></html>", JLabel.CENTER);
        traceCommandLabel.setBounds(LayoutConstants.DEFAULT_NUMBER,
            LayoutConstants.SYSTEM_TUNNING_LABLE_INTT_Y + LayoutConstants.SYSTEM_TUNNING_SLIDE_LABLE_SPACING_Y,
            LayoutConstants.SYSTEM_TUNNING_LABLE_WIDTH, LayoutConstants.SYSTEM_TUNNING_SLIDE_LABLE_HEIGHT);

        taskSystemTunningPanelEvent
            .recordSetting(recordSettingLabel, traceCommandLabel, jPanelCenterSouthRight, jPanelCenterSouthWest);
        taskSystemTunningPanelEvent
            .traceCommand(recordSettingLabel, traceCommandLabel, jPanelCenterSouthRight, jPanelCenterSouthWest);

        jPanelCenterSouthWest.add(recordSettingLabel);
        jPanelCenterSouthWest.setPreferredSize(
            new Dimension(LayoutConstants.SYSTEM_TUNNING_LABLE_WIDTH, LayoutConstants.DEFAULT_NUMBER));
        jPanelCenterSouthWest.setBackground(ColorConstants.SYSTEM_TUNNING_WEST_LABEL);
    }

    /**
     * systemTunningProbes
     *
     * @param jPanelCenterSouthWestP2 jPanelCenterSouthWestP2
     * @param jPanelCenterSouthEastP2 jPanelCenterSouthEastP2
     */
    public void systemTunningProbes(JPanel jPanelCenterSouthWestP2, JPanel jPanelCenterSouthEastP2) {
        JLabel probesCpu = new JLabel("<html><p style=\"font-size:13px;"
            + "text-align:left;color:white;\">probes config</p><p style=\"margin-top:0px;font-size:9px;text-align:left;"
            + "color:#757784;\">CPU usage,scheduling," + "<br>" + "wakeups</p></html>", JLabel.CENTER);
        probesCpu.setOpaque(true);
        probesCpu.setBounds(LayoutConstants.DEFAULT_NUMBER, LayoutConstants.SYSTEM_TUNNING_LABLE_INTT_Y,
            LayoutConstants.SYSTEM_TUNNING_LABLE_WIDTH, LayoutConstants.SYSTEM_TUNNING_SLIDE_LABLE_HEIGHT);
        systemTunningProbesCpuRightShow(jPanelCenterSouthEastP2);
        jPanelCenterSouthWestP2.add(probesCpu);
        jPanelCenterSouthWestP2.setPreferredSize(
            new Dimension(LayoutConstants.SYSTEM_TUNNING_LABLE_WIDTH, LayoutConstants.DEFAULT_NUMBER));
        jPanelCenterSouthWestP2.setBackground(ColorConstants.SYSTEM_TUNNING_WEST_LABEL);
    }

    /**
     * jPanelCenterSouthEastP2 settings
     *
     * @param jPanelCenterSouthEastP2 jPanelCenterSouthEastP2
     */
    public void systemTunningProbesCpuRightShow(JPanel jPanelCenterSouthEastP2) {
        jPanelCenterSouthEastP2.removeAll();
        // 右边四个、左侧三个复选框
        jCheckBoxAttributeRight(jPanelCenterSouthEastP2);
        jCheckBoxAttributeLeft(jPanelCenterSouthEastP2);
        JLabel memoryLabel2 = new JLabel(
            "<html><p style=\"margin-top:4px;font-size:9px;text-align:center;color:#757784;\">"
                + "enables high-detailed tracking of scheduling events" + "</p></html>", JLabel.LEFT);
        memoryLabel2.setBounds(LayoutConstants.SYSTEM_TUNNING_LABLE_DESCRIBE_INTT_X,
            LayoutConstants.SYSTEM_TUNNING_PROBES_SCHEDULING_SPACING_Y, LayoutConstants.SYSTEM_TUNNING_LABLE_DESCRIBE,
            LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        jPanelCenterSouthEastP2.add(memoryLabel2);
        JLabel cPUFrequencyLabel2 = cPUFrequencyLabel2();
        jPanelCenterSouthEastP2.add(cPUFrequencyLabel2);
        JLabel syscallsLabel2 = new JLabel("<html><p style=\"font-size:9px;text-align:center;color:#757784;\">"
            + "Tracks the enter and exit of all syscalls" + "</p></html>", JLabel.LEFT);
        syscallsLabel2.setBounds(LayoutConstants.SYSTEM_TUNNING_LABLE_DESCRIBE_INTT_X,
            LayoutConstants.SYSTEM_TUNNING_PROBES_SYSCALLS_SPACING_Y, LayoutConstants.SYSTEM_TUNNING_LABLE_DESCRIBE,
            LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        JLabel boardVoltagesLabel2 = new JLabel("<html><p style=\"font-size:9px;text-align:center;color:#757784;\">"
            + "Tracks voltage and frequency changes from board sensors " + "</p></html>", JLabel.LEFT);
        boardVoltagesLabel2.setBounds(LayoutConstants.SYSTEM_TUNNING_LABLE_DESCRIBE_INTT_RIGHT_X,
            LayoutConstants.SYSTEM_TUNNING_PROBES_SCHEDULING_SPACING_Y, LayoutConstants.SYSTEM_TUNNING_LABLE_DESCRIBE,
            LayoutConstants.SYSTEM_TUNNING_CHECKBOX_LABLE_HEIGHT);
        jPanelCenterSouthEastP2.add(boardVoltagesLabel2);
        JLabel highFrequencyLabel2 = new JLabel("<html><p style=\"font-size:9px;color:#757784;\">"
            + "Allows to track short memory splikes and transitories through ftrace's mm_event. "
            + "rss_stat and ion events. Available only on recent system kernels" + "</p></html>", JLabel.LEFT);
        highFrequencyLabel2.setBounds(LayoutConstants.SYSTEM_TUNNING_LABLE_DESCRIBE_INTT_RIGHT_X,
            LayoutConstants.SYSTEM_TUNNING_PROBES_HIGH_FREQUENCY_MEMORY_LABEL_INIT_Y,
            LayoutConstants.SYSTEM_TUNNING_LABLE_DESCRIBE, LayoutConstants.SYSTEM_TUNNING_CHECKBOX_LABLE_HEIGHT);
        JLabel lowMemoryLabel2 = new JLabel("<html><p style=\"font-size:9px;color:#757784;\">"
            + "Record LMK events. Works both with the old in kernel LMK and "
            + "the newer userspace Imkd. It also tracks OOM score adjustments " + "</p></html>", JLabel.LEFT);
        lowMemoryLabel2.setBounds(LayoutConstants.SYSTEM_TUNNING_LABLE_DESCRIBE_INTT_RIGHT_X,
            LayoutConstants.SYSTEM_TUNNING_PROBES_LOW_MEMORY_KILLER_LABEL_INIT_Y,
            LayoutConstants.SYSTEM_TUNNING_LABLE_DESCRIBE, LayoutConstants.SYSTEM_TUNNING_CHECKBOX_LABLE_HEIGHT);
        jPanelCenterSouthEastP2.add(lowMemoryLabel2);
        JLabel atraceUserspaceLabel2 = new JLabel("<html><p style=\"font-size:9px;color:#757784;\">"
            + "Enables C++ / Java codebase annotations (ATRACE_BEGIN() / os.Trace())" + "</p></html>", JLabel.LEFT);
        atraceUserspaceLabel2.setBounds(LayoutConstants.SYSTEM_TUNNING_LABLE_DESCRIBE_INTT_RIGHT_X,
            LayoutConstants.SYSTEM_TUNNING_PROBES_ATRACE_USERSPACE_ANNOTATIONS_LABEL_INIT_Y,
            LayoutConstants.SYSTEM_TUNNING_LABLE_DESCRIBE, LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        jPanelCenterSouthEastP2.setBackground(ColorConstants.SYSTEM_TUNNING_WEST_LABEL);
        jPanelCenterSouthEastP2.repaint();
    }

    /**
     * cPUFrequencyLabel2
     *
     * @return JLabel
     */
    public JLabel cPUFrequencyLabel2() {
        JLabel cPUFrequencyLabel2 = new JLabel(
            "<html><p style=\"margin-top:4px;font-size:9px;text-align:center;color:#757784;\">"
                + "Records cpu frequency and idle state change viaftrace" + "</p></html>", JLabel.LEFT);
        cPUFrequencyLabel2.setBounds(LayoutConstants.SYSTEM_TUNNING_LABLE_DESCRIBE_INTT_X,
            LayoutConstants.SYSTEM_TUNNING_PROBES_CPU_SPACING_Y, LayoutConstants.SYSTEM_TUNNING_LABLE_DESCRIBE,
            LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        return cPUFrequencyLabel2;
    }

    /**
     * jCheckBoxAttributeRight
     *
     * @param jPanelCenterSouthEastP2 jPanelCenterSouthEastP2
     */
    public void jCheckBoxAttributeRight(JPanel jPanelCenterSouthEastP2) {
        JLabel label1 = new JLabel("Record model");

        label1.setBounds(LayoutConstants.SYSTEM_TUNNING_LABEL_INTT_X,
            LayoutConstants.SYSTEM_TUNNING_RECORD_MODEL_LABLE_INTT_Y, LayoutConstants.SYSTEM_TUNNING_LABLE_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        // 创建JCheckBox对象
        JCheckBox rbScheduling = new JCheckBox("Scheduling details", true);
        JCheckBox rbCpu = new JCheckBox("CPU Frequency and idle states", true);
        JCheckBox rbSyscalls = new JCheckBox("Syscalls", false);
        JCheckBox rbBoard = new JCheckBox("Board voltages & frequency", false);
        rbScheduling.setFont(new Font(Font.DIALOG, Font.BOLD, LayoutConstants.SYSTEM_TUNNING_JCHECKBOX_FONT));
        rbCpu.setFont(new Font(Font.DIALOG, Font.BOLD, LayoutConstants.SYSTEM_TUNNING_JCHECKBOX_FONT));
        rbSyscalls.setFont(new Font(Font.DIALOG, Font.BOLD, LayoutConstants.SYSTEM_TUNNING_JCHECKBOX_FONT));
        rbBoard.setFont(new Font(Font.DIALOG, Font.BOLD, LayoutConstants.SYSTEM_TUNNING_JCHECKBOX_FONT));

        rbScheduling.setBackground(ColorConstants.SYSTEM_TUNNING_WEST_LABEL);
        rbCpu.setBackground(ColorConstants.SYSTEM_TUNNING_WEST_LABEL);
        rbSyscalls.setBackground(ColorConstants.SYSTEM_TUNNING_WEST_LABEL);
        rbBoard.setBackground(ColorConstants.SYSTEM_TUNNING_WEST_LABEL);
        rbScheduling
            .setBounds(LayoutConstants.SYSTEM_TUNNING_LABEL_INTT_X, LayoutConstants.SYSTEM_TUNNING_LABLE_SPACING_Y,
                LayoutConstants.SYSTEM_TUNNING_JCHECKBOX_LABLE_WIDTH, LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        rbCpu.setBounds(LayoutConstants.SYSTEM_TUNNING_LABEL_INTT_X,
            LayoutConstants.SYSTEM_TUNNING_LABLE_SPACING_Y + LayoutConstants.SYSTEM_TUNNING_PROBES_SPACING_Y,
            LayoutConstants.SYSTEM_TUNNING_JCHECKBOX_LABLE_WIDTH, LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        rbSyscalls.setBounds(LayoutConstants.SYSTEM_TUNNING_LABEL_INTT_X,
            LayoutConstants.SYSTEM_TUNNING_LABLE_SPACING_Y + LayoutConstants.SYSTEM_TUNNING_PROBES_DOUBLE_SPACING_Y,
            LayoutConstants.SYSTEM_TUNNING_JCHECKBOX_LABLE_WIDTH, LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        rbBoard.setBounds(LayoutConstants.SYSTEM_TUNNING_LABLE_INTT_RIGHT_X,
            LayoutConstants.SYSTEM_TUNNING_LABLE_SPACING_Y, LayoutConstants.SYSTEM_TUNNING_JCHECKBOX_LABLE_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        jPanelCenterSouthEastP2.add(label1);
        jPanelCenterSouthEastP2.add(rbScheduling);
        jPanelCenterSouthEastP2.add(rbCpu);
        jPanelCenterSouthEastP2.add(rbBoard);
        taskSystemTunningPanelEvent.checkBoxState(rbScheduling, rbCpu, rbSyscalls, rbBoard);
    }

    /**
     * jCheckBoxAttributeLeft
     *
     * @param jPanelCenterSouthEastP2 jPanelCenterSouthEastP2
     */
    public void jCheckBoxAttributeLeft(JPanel jPanelCenterSouthEastP2) {
        JCheckBox rbFrequency = new JCheckBox("High frequency memory", false);
        JCheckBox rbLowMemory = new JCheckBox("Low memory killer", false);
        JCheckBox rbAtrace = new JCheckBox("Atrace userspace annotations", false);
        // 右侧三个复选框
        rbFrequency.setFont(new Font(Font.DIALOG, Font.BOLD, LayoutConstants.SYSTEM_TUNNING_JCHECKBOX_FONT));
        rbLowMemory.setFont(new Font(Font.DIALOG, Font.BOLD, LayoutConstants.SYSTEM_TUNNING_JCHECKBOX_FONT));
        rbAtrace.setFont(new Font(Font.DIALOG, Font.BOLD, LayoutConstants.SYSTEM_TUNNING_JCHECKBOX_FONT));
        rbFrequency.setBackground(ColorConstants.SYSTEM_TUNNING_WEST_LABEL);
        rbLowMemory.setBackground(ColorConstants.SYSTEM_TUNNING_WEST_LABEL);
        rbAtrace.setBackground(ColorConstants.SYSTEM_TUNNING_WEST_LABEL);
        rbFrequency.setBounds(LayoutConstants.SYSTEM_TUNNING_LABLE_INTT_RIGHT_X,
            LayoutConstants.SYSTEM_TUNNING_LABLE_SPACING_Y, LayoutConstants.SYSTEM_TUNNING_JCHECKBOX_LABLE_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        rbLowMemory.setBounds(LayoutConstants.SYSTEM_TUNNING_LABLE_INTT_RIGHT_X,
            LayoutConstants.SYSTEM_TUNNING_LABLE_SPACING_Y + LayoutConstants.SYSTEM_TUNNING_PROBES_SPACING_Y,
            LayoutConstants.SYSTEM_TUNNING_JCHECKBOX_LABLE_WIDTH, LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        rbAtrace.setBounds(LayoutConstants.SYSTEM_TUNNING_LABLE_INTT_RIGHT_X,
            LayoutConstants.SYSTEM_TUNNING_LABLE_SPACING_Y + LayoutConstants.SYSTEM_TUNNING_PROBES_DOUBLE_SPACING_Y
                + LayoutConstants.SYSTEM_TUNNING_PROBES_SPACING_Y, LayoutConstants.SYSTEM_TUNNING_JCHECKBOX_LABLE_WIDTH,
            LayoutConstants.SYSTEM_TUNNING_LABLE_HEIGHT);
        jPanelCenterSouthEastP2.add(rbLowMemory);
        taskSystemTunningPanelEvent.checkBoxStateLeft(rbFrequency, rbLowMemory, rbAtrace);
    }

    /**
     * SouthContainer settings
     *
     * @param jTaskPanel jTaskPanel
     */
    public void setJPanelSouth(TaskPanel jTaskPanel) {
        jButtonLastStep.setBounds((int) (LayoutConstants.NUMBER_STEP + (jPanelSouth.getPreferredSize().getWidth()
                - LayoutConstants.WINDOW_WIDTH)), LayoutConstants.JP_LABEL_HEIGHT, LayoutConstants.DEVICES_WIDTH,
            LayoutConstants.DEVICE_X);
        jButtonLastStep.setBackground(ColorConstants.BLACK_COLOR);
        jButtonLastStep.setFocusPainted(false);
        jButtonStartTask.setForeground(Color.white);

        jButtonStartTask.setBounds((int) (LayoutConstants.POSITION_TASK_X + (jPanelSouth.getPreferredSize().getWidth()
                - LayoutConstants.WINDOW_WIDTH)), LayoutConstants.JP_LABEL_HEIGHT, LayoutConstants.DEVICES_WIDTH,
            LayoutConstants.DEVICE_X);
        jButtonStartTask.setBackground(ColorConstants.ADD_DEVICE_BUN);
        jButtonStartTask.setFocusPainted(false);
        jPanelSouth.add(jButtonLastStep);
        jPanelSouth.add(jButtonStartTask);
        // 监听jPanelSouth大小变化并改变按钮位置
        taskSystemTunningPanelEvent.listenerJPanelSouth(this);
        // jButtonLastStep添加事件返回上一页
        taskSystemTunningPanelEvent.lastStep(this, jTaskPanel);
        // jButtonStartTask添加事件开始任务
        taskSystemTunningPanelEvent.startTask(this, jTaskPanel);
    }

    public JPanel getScrollPane() {
        return scrollPane;
    }

    public JPanel getJPanelSouth() {
        return jPanelSouth;
    }

    public JButton getJButtonLastStep() {
        return jButtonLastStep;
    }

    public JButton getJButtonStartTask() {
        return jButtonStartTask;
    }

    public JCheckBox[] getJCheckBoxs() {
        return jCheckBoxs;
    }

}
