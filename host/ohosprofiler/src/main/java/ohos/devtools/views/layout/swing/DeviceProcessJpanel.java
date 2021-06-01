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

import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.service.MultiDeviceManager;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.process.service.ProcessManager;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.event.DeviceProcessJpanelEvent;
import ohos.devtools.views.layout.event.TaskScenePanelEvent;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

/**
 * 设备进程面板
 *
 * @version 1.0
 * @date 2021/03/02
 **/
public class DeviceProcessJpanel extends JPanel {
    /**
     * 设备描述
     */
    private JLabel jLabelDevice = new JLabel("Device");

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
     * 进程信息集合
     */
    private List<ProcessInfo> proinfos = null;

    /**
     * 进程描述
     */
    private JLabel jLabelApply = new JLabel("Application");

    /**
     * 进程信息输入框容器
     */
    private JPanel selectPanel = new JPanel(new GridLayout());

    /**
     * 进程信息输入框
     */
    private JTextFieldTable labelName = new JTextFieldTable("device");

    /**
     * 进程信息搜索框
     */
    private JTextFieldTable textField = new JTextFieldTable("press");

    /**
     * 进程信息下拉箭头
     */
    private JLabel labelSvg = new JLabel();

    /**
     * 进程信息下拉列表容器
     */
    private JPanel jPanelProcess = new JPanel(new BorderLayout());

    /**
     * 进程信息下拉列表容器top容器
     */
    private JPanel jPanelProcessTop = new JPanel(new GridLayout());

    /**
     * 进程信息下拉列表容器Center容器
     */
    private JPanel jPanelProcessCenter = new JPanel(new GridLayout());

    /**
     * 进程信息列表
     */
    private JTable table = new JTable();

    /**
     * 进程信息列表滚动条
     */
    private JScrollPane jScrollPane = new JScrollPane();

    private DeviceProcessJpanelEvent deviceProcessJpanelEvent = new DeviceProcessJpanelEvent();

    private TaskScenePanelEvent taskScenePanelEvent = new TaskScenePanelEvent();

    /**
     * DeviceProcessJpanel
     *
     * @param taskScenePanelEvent taskScenePanelEvent
     * @param scrollPane          scrollPane
     * @param taskScenePanel      taskScenePanel
     */
    public DeviceProcessJpanel(TaskScenePanelEvent taskScenePanelEvent, JPanel scrollPane,
        TaskScenePanel taskScenePanel) {
        this.setLayout(null);
        this.setOpaque(true);
        this.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
        this.setBounds(LayoutConstants.DEVICE_PRO_X, taskScenePanelEvent.getNum(), LayoutConstants.DEVICE_PRO_WIDTH,
            LayoutConstants.WIDTHS);
        Font fontTaskTun = new Font(Font.DIALOG, Font.BOLD, LayoutConstants.DEVICES_FONT);
        JLabel jLabelDeviceNum =
            new JLabel("Devices " + String.format(Locale.ENGLISH, "%02d", taskScenePanelEvent.getNumDevices()));
        jLabelDeviceNum.setFont(fontTaskTun);
        jLabelDeviceNum.setForeground(Color.white);
        jLabelDeviceNum
            .setBounds(LayoutConstants.CHOOSE_HEIGHT, LayoutConstants.JP_RIGHT_WIDTH, LayoutConstants.DEVICES_WIDTH,
                LayoutConstants.DEVICES_HEIGHT);
        // 属性设置
        setAttributes(taskScenePanelEvent, scrollPane, taskScenePanel);
        DeviceIPPortInfo deviceIPPortInfo;
        // 进程下拉列表
        if (deviceInfos.isEmpty()) {
            deviceIPPortInfo = new DeviceIPPortInfo();
        } else {
            deviceIPPortInfo = deviceInfos.get(0);
        }
        createProcessList(deviceIPPortInfo, jLabelDeviceNum.getText(), scrollPane, taskScenePanel);
        // 获取add device按钮
        taskScenePanel.getJButtonAddDevice()
            .setBounds(LayoutConstants.APP_LABEL_Y1, LayoutConstants.TWO_HUNDRED_TEN, LayoutConstants.DEVICE_ADD_WIDTH,
                LayoutConstants.DEVICE_ADD_HEIGHT);
        this.add(jLabelDeviceNum);
        this.add(jLabelDevice);
        this.add(jComboBoxConnect);
        this.add(jComboBoxPhone);
        this.add(jLabelApply);
        this.add(selectPanel);
    }

    /**
     * Process drop-down list
     *
     * @param deviceInfo     deviceInfo
     * @param deviceNum      deviceNum
     * @param scrollPane     scrollPane
     * @param taskScenePanel taskScenePanel
     */
    public void createProcessList(DeviceIPPortInfo deviceInfo, String deviceNum, JPanel scrollPane,
        TaskScenePanel taskScenePanel) {
        jPanelProcess
            .setBounds(LayoutConstants.SELECT_ALL_Y, LayoutConstants.PRECCE_HEIGHT_Y, LayoutConstants.PRECCE_WIDTH,
                LayoutConstants.SE_PANEL_WIDTH);
        jPanelProcessTop
            .setPreferredSize(new Dimension(LayoutConstants.SE_PANEL_WIDTH, LayoutConstants.DEVICE_ADD_HEIGHT));
        jPanelProcessCenter.setPreferredSize(new Dimension(LayoutConstants.PRECCE_WIDTH, LayoutConstants.TASK_DEC_X));
        jPanelProcess.add(jPanelProcessTop, BorderLayout.NORTH);
        jPanelProcess.add(jPanelProcessCenter, BorderLayout.CENTER);
        textField.setPreferredSize(new Dimension(LayoutConstants.TASK_DEC_X, LayoutConstants.DEVICE_ADD_HEIGHT));
        jPanelProcessTop.add(textField);
        // 创建列表
        Vector columnNames = new Vector();
        columnNames.add("");
        // 根据设备信息获取进程信息
        Vector processNames = new Vector<>();
        for (int i = 0; i < proinfos.size(); i++) {
            ProcessInfo processInfo = proinfos.get(i);
            Vector<String> vector = new Vector();
            vector.add(processInfo.getProcessName() + "(" + processInfo.getProcessId() + ")");
            processNames.add(vector);
        }
        if (!proinfos.isEmpty()) {
            // 更新map
            Map<DeviceIPPortInfo, ProcessInfo> mapObject = new HashMap<>();
            mapObject.put(deviceInfo, proinfos.get(0));
            Constant.map.put(deviceNum, mapObject);
        }
        DefaultTableModel model = new DefaultTableModel(processNames, columnNames);
        table.setModel(model);
        table.getTableHeader().setVisible(false);
        table.setRowHeight(LayoutConstants.DEVICE_ADD_HEIGHT);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setPreferredSize(new Dimension(0, 0));
        table.getTableHeader().setDefaultRenderer(renderer);
        // table鼠标悬停效果
        deviceProcessJpanelEvent.mouseEffectTable(table);
        jScrollPane.setViewportView(table);
        jPanelProcessCenter.add(jScrollPane);
        // table点击获取选中值
        deviceProcessJpanelEvent.clickTable(this, deviceNum, deviceInfo, scrollPane, taskScenePanel);
        // 搜索按钮添加搜索事件
        deviceProcessJpanelEvent.searchJButtonSelect(this, proinfos);
    }

    private void setComponent() {
        jLabelDevice.setBounds(LayoutConstants.SELECT_ALL_Y, LayoutConstants.DEVICE_PRO_X, LayoutConstants.APP_BUT_X,
            LayoutConstants.DEVICES_HEIGHT);
        jComboBoxConnect.addItem("USB");
        jComboBoxConnect
            .setBounds(LayoutConstants.CHOOSE_HEIGHT, LayoutConstants.JLABEL_SIZE, LayoutConstants.CON_BOX_WIDTH,
                LayoutConstants.TASK_SCENE_Y);
        jComboBoxPhone
            .setBounds(LayoutConstants.RIGHT_BUN_WIDTH, LayoutConstants.JLABEL_SIZE, LayoutConstants.HEIGHT_PRESSE,
                LayoutConstants.TASK_SCENE_Y);
        jLabelApply
            .setBounds(LayoutConstants.APP_NAME_X, LayoutConstants.EMB_BUT_HEIGHT, LayoutConstants.APP_NAME_WIDTH,
                LayoutConstants.APP_NAME_HEIGHT);
        selectPanel.setBounds(LayoutConstants.SELECT_ALL_Y, LayoutConstants.BUTTON_WIDTH, LayoutConstants.PRECCE_WIDTH,
            LayoutConstants.TASK_SCENE_Y);
        labelName.setEditable(false);
        selectPanel.add(labelName);
    }

    /**
     * Property setting
     *
     * @param taskScenePanelEvent taskScenePanelEvent
     * @param scrollPane          scrollPane
     * @param taskScenePanel      taskScenePanel
     */
    public void setAttributes(TaskScenePanelEvent taskScenePanelEvent, JPanel scrollPane,
        TaskScenePanel taskScenePanel) {
        setComponent();
        deviceInfos = MultiDeviceManager.getInstance().getAllDeviceIPPortInfos();
        deviceProcessJpanelEvent.devicesInfoJComboBoxUpdate(this);
        if (deviceInfos.isEmpty()) {
            proinfos = new ArrayList<>();
        } else {
            proinfos = ProcessManager.getInstance().getProcessList(deviceInfos.get(0));
            Map<DeviceIPPortInfo, ProcessInfo> mapObject = new HashMap<>();
            if (!proinfos.isEmpty()) {
                mapObject.put(deviceInfos.get(0), proinfos.get(0));
            }
            Constant.map.put("Devices " + String.format(Locale.ENGLISH, "%02d", taskScenePanelEvent.getNumDevices()),
                mapObject);
        }
        if (!proinfos.isEmpty()) {
            labelName.setText(proinfos.get(0).getProcessName() + "(" + proinfos.get(0).getProcessId() + ")");
        } else {
            labelName.setText("Please select the device process !");
            labelName.setForeground(ColorConstants.TOP_PANEL_APPLICATION);
        }
        // 设备和进程信息联动
        deviceProcessJpanelEvent.itemStateChanged(this, taskScenePanel,
            "Devices " + String.format(Locale.ENGLISH, "%02d", taskScenePanelEvent.getNumDevices()), scrollPane);
        // 给进程列表框添加点击事件，点击后展开进程列表
        deviceProcessJpanelEvent.addClickListener(this, scrollPane, taskScenePanelEvent, taskScenePanel,
            "Devices " + String.format(Locale.ENGLISH, "%02d", taskScenePanelEvent.getNumDevices()));
    }

    /**
     * getTable
     *
     * @return JTable
     */
    public JTable getTable() {
        return table;
    }

    /**
     * getLabelName
     *
     * @return JTextField
     */
    public JTextField getLabelName() {
        return labelName;
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
     * getDeviceInfos
     *
     * @return List<DeviceIPPortInfo>
     */
    public List<DeviceIPPortInfo> getDeviceInfos() {
        return deviceInfos;
    }

    /**
     * getProinfos
     *
     * @return List<ProcessInfo>
     */
    public List<ProcessInfo> getProinfos() {
        return proinfos;
    }

    /**
     * setProinfos
     *
     * @param proinfos proinfos
     */
    public void setProinfos(List<ProcessInfo> proinfos) {
        this.proinfos = proinfos;
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
     * getTextField
     *
     * @return JTextField
     */
    public JTextField getTextField() {
        return textField;
    }

    /**
     * getLabelSvg
     *
     * @return JLabel
     */
    public JLabel getLabelSvg() {
        return labelSvg;
    }

    /**
     * getJPanelProcess
     *
     * @return JPanel
     */
    public JPanel getJPanelProcess() {
        return jPanelProcess;
    }

    public TaskScenePanelEvent getTaskScenePanelEvent() {
        return taskScenePanelEvent;
    }

    public void setTaskScenePanelEvent(TaskScenePanelEvent taskScenePanelEvent) {
        this.taskScenePanelEvent = taskScenePanelEvent;
    }
}
