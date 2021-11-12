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

package ohos.devtools.views.layout;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.service.MultiDeviceManager;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.process.service.ProcessManager;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.common.customcomp.CustomTextField;
import ohos.devtools.views.common.customcomp.GraphicsLinePanel;
import ohos.devtools.views.layout.event.DeviceProcessPanelEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

/**
 * DeviceProcessPanel
 *
 * @since: 2021/10/25
 */
public class DeviceProcessPanel extends JBPanel {
    private static final Logger LOGGER = LogManager.getLogger(DeviceProcessPanel.class);
    private JBLabel currentDeviceNum;
    private JBLabel deviceLabel;
    private ComboBox<String> connectTypeComboBox;
    private ComboBox<String> deviceNameComboBox;
    private JBLabel applicationDesLabel;
    private CustomTextField selectedProcessName;
    private CustomTextField searchField;
    private JBPanel processPanel;
    private JBTable processTable;
    private JBScrollPane processScrollPane;
    private GraphicsLinePanel graphicsLine;
    private DeviceProcessPanelEvent deviceProcessPanelEvent;
    private List<DeviceIPPortInfo> deviceInfoList;
    private List<ProcessInfo> processInfoList;
    private int deviceNum = 1;
    private int scrollContainerHeight = 0;

    /**
     * DeviceProcessPanel
     *
     * @param deviceConnectScrollPane deviceConnectScrollPane
     */
    public DeviceProcessPanel(JBPanel deviceConnectScrollPane, int deviceNum, int scrollContainerHeight) {
        this.deviceNum = deviceNum;
        this.scrollContainerHeight = scrollContainerHeight;
        initComponents();
        addComponents();
        initDeviceList();
        initProcessList(deviceInfoList.get(0));
        addEvent(deviceConnectScrollPane);
    }

    /**
     * initComponents
     */
    private void initComponents() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initComponents");
        }
        this.setLayout(new MigLayout("insets 0", "[grow,fill]", "[fill,fill]"));
        this.setOpaque(false);
        this.setBounds(10, scrollContainerHeight, 800, 500);
        this.setBackground(JBColor.background());
        currentDeviceNum = new JBLabel("Devices " + String.format(Locale.ENGLISH, "%02d", deviceNum));
        currentDeviceNum.setOpaque(false);
        currentDeviceNum.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
        currentDeviceNum.setForeground(JBColor.foreground().brighter());
        // connect type and device name
        deviceLabel = new JBLabel("Device");
        deviceLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        connectTypeComboBox = new ComboBox<String>();
        connectTypeComboBox.addItem(LayoutConstants.USB);
        connectTypeComboBox.setName(UtConstant.UT_DEVICE_PROCESS_PANEL_CONNECT_TYPE);
        connectTypeComboBox.setBackground(JBColor.background());
        connectTypeComboBox.setOpaque(true);
        deviceNameComboBox = new ComboBox<String>();
        deviceNameComboBox.setName(UtConstant.UT_DEVICE_PROCESS_PANEL_DEVICE_NAME);
        deviceNameComboBox.setBackground(JBColor.background());
        deviceNameComboBox.setOpaque(true);
        // application name
        applicationDesLabel = new JBLabel("Application");
        selectedProcessName = new CustomTextField("device");
        selectedProcessName.setBackground(JBColor.background());
        selectedProcessName.setOpaque(true);
        selectedProcessName.setForeground(JBColor.foreground().brighter());
        selectedProcessName.setEditable(false);
        selectedProcessName.setBorder(BorderFactory.createLineBorder(JBColor.background().darker(), 1));
        searchField = new CustomTextField("press");
        searchField.setBackground(JBColor.background());
        searchField.setOpaque(true);
        searchField.setForeground(JBColor.foreground().brighter());
        searchField.setName(UtConstant.UT_DEVICE_PROCESS_PANEL_SEARCH_FIELD);
        // process scroll
        processPanel = new JBPanel(new MigLayout("insets 0", "[grow,fill]",
            "[fill,fill]"));
        processPanel.setBackground(JBColor.background());
        processPanel.setOpaque(true);
        processTable = new JBTable();
        processPanel.setVisible(false);
        processScrollPane = new JBScrollPane();
        processScrollPane.setBackground(JBColor.background());
        processScrollPane.setOpaque(true);
        graphicsLine = new GraphicsLinePanel();
    }

    private void addComponents() {
        this.add(currentDeviceNum, "wrap, gapx 16, gapy 16");
        this.add(deviceLabel, "wrap, gapx 16");
        this.add(connectTypeComboBox, "gapx 16, width 30%");
        this.add(deviceNameComboBox, "wrap, gapx 0 16, width 70%");
        this.add(applicationDesLabel, "wrap, gapx 16");
        this.add(selectedProcessName, "wrap, span, gapx 16 16");
        this.add(processPanel, "wrap, span, gapx 16 16, height 55%");
    }

    /**
     * get the deviceData
     */
    private void initDeviceList() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initDeviceList");
        }
        deviceInfoList = MultiDeviceManager.getInstance().getOnlineDeviceInfoList();
        if (deviceInfoList.isEmpty()) {
            deviceInfoList.add(new DeviceIPPortInfo());
            processInfoList = new ArrayList<>();
        } else {
            processInfoList = ProcessManager.getInstance().getProcessList(deviceInfoList.get(0));
            Map<DeviceIPPortInfo, ProcessInfo> mapObject = new HashMap<>();
            if (!processInfoList.isEmpty()) {
                mapObject.put(deviceInfoList.get(0), processInfoList.get(0));
            }
            Constant.map.put("Devices " + String.format(Locale.ENGLISH, "%02d", deviceNum), mapObject);
        }
    }

    /**
     * Process drop-down list
     *
     * @param deviceInfo deviceInfo
     */
    public void initProcessList(DeviceIPPortInfo deviceInfo) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initProcessList");
        }
        if (!processInfoList.isEmpty()) {
            selectedProcessName.setText(processInfoList.get(0).getProcessName() +
                    "(" + processInfoList.get(0).getProcessId() + ")");
        } else {
            selectedProcessName.setText("Please select the device process !");
        }
        // create column
        Vector columnNames = new Vector();
        columnNames.add("");
        // get process
        Vector processNames = new Vector<>();
        for (int i = 0; i < processInfoList.size(); i++) {
            ProcessInfo processInfo = processInfoList.get(i);
            Vector<String> vector = new Vector();
            vector.add(processInfo.getProcessName() + "(" + processInfo.getProcessId() + ")");
            processNames.add(vector);
        }
        if (!processInfoList.isEmpty()) {
            // update map
            Map<DeviceIPPortInfo, ProcessInfo> mapObject = new HashMap<>();
            mapObject.put(deviceInfo, processInfoList.get(0));
            Constant.map.put(currentDeviceNum.getText(), mapObject);
        }
        // renderProcessTable
        renderProcessTable(processNames, columnNames);
    }

    private void renderProcessTable(Vector processNames, Vector columnNames) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("renderProcessTable");
        }
        DefaultTableModel model = new DefaultTableModel(processNames, columnNames);
        processTable.setName(UtConstant.UT_DEVICE_PROCESS_PANEL_TABLE);
        processTable.setModel(model);
        processTable.getTableHeader().setVisible(false);
        processTable.setRowHeight(LayoutConstants.DEVICE_ADD_HEIGHT);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setPreferredSize(new Dimension(0, 0));
        renderer.setOpaque(false);
        processTable.getTableHeader().setDefaultRenderer(renderer);
        processScrollPane.setViewportView(processTable);
        processPanel.add(searchField, "wrap, span");
        processPanel.add(processScrollPane, "span");
    }

    private void addEvent(JBPanel deviceConnectScrollPane) {
        deviceProcessPanelEvent = new DeviceProcessPanelEvent();
        deviceProcessPanelEvent.devicesInfoJComboBoxUpdate(this);
        // device and process state change
        deviceProcessPanelEvent.itemStateChanged(this);
        // addClickListener and open the process list
        deviceProcessPanelEvent.addClickListener(this, deviceConnectScrollPane,
            "Devices " + String.format(Locale.ENGLISH, "%02d", deviceNum));
        deviceProcessPanelEvent.connectTypeChanged(deviceInfoList.get(0), connectTypeComboBox);
        deviceProcessPanelEvent.mouseEffectTable(processTable);
        deviceProcessPanelEvent
            .clickTable(this, currentDeviceNum.getText(), deviceConnectScrollPane);
        deviceProcessPanelEvent.searchJButtonSelect(this, processInfoList);
    }

    /**
     * getProcessTable
     *
     * @return JTable
     */
    public JBTable getProcessTable() {
        return processTable;
    }

    /**
     * getSelectedProcessName
     *
     * @return JTextField
     */
    public JTextField getSelectedProcessName() {
        return selectedProcessName;
    }

    /**
     * setDeviceInfoList
     *
     * @param deviceInfoList deviceInfoList
     */
    public void setDeviceInfoList(List<DeviceIPPortInfo> deviceInfoList) {
        this.deviceInfoList = deviceInfoList;
    }

    /**
     * getDeviceInfoList
     *
     * @return List <DeviceIPPortInfo>
     */
    public List<DeviceIPPortInfo> getDeviceInfoList() {
        return deviceInfoList;
    }

    /**
     * getProcessInfoList
     *
     * @return List <ProcessInfo>
     */
    public List<ProcessInfo> getProcessInfoList() {
        return processInfoList;
    }

    /**
     * setProcessInfoList
     *
     * @param processInfoList processInfoList
     */
    public void setProcessInfoList(List<ProcessInfo> processInfoList) {
        this.processInfoList = processInfoList;
    }

    /**
     * getDeviceNameComboBox
     *
     * @return JComboBox
     */
    public JComboBox getDeviceNameComboBox() {
        return deviceNameComboBox;
    }

    /**
     * getSearchField
     *
     * @return JTextField
     */
    public JTextField getSearchField() {
        return searchField;
    }

    /**
     * getProcessPanel
     *
     * @return JPanel
     */
    public JBPanel getProcessPanel() {
        return processPanel;
    }

    /**
     * getGraphicsLine
     *
     * @return GraphicsLinePanel
     */
    public GraphicsLinePanel getGraphicsLine() {
        return graphicsLine;
    }

}
