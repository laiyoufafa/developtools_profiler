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

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.service.MultiDeviceManager;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.process.service.ProcessManager;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.DeviceProcessPanel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import static ohos.devtools.views.common.Constant.DEVICE_REFRESH;

/**
 * DeviceProcessPanelEvent
 */
public class DeviceProcessPanelEvent {
    private static final Logger LOGGER = LogManager.getLogger(DeviceProcessPanelEvent.class);
    private Vector<String> oldDevice = new Vector<>();
    private List<ProcessInfo> processInfoList;
    private int rowCount = -1;

    /**
     * searchJButtonSelect
     *
     * @param deviceProcessPanel deviceProcessPanel
     * @param processInfoList processInfoList
     */
    public void searchJButtonSelect(DeviceProcessPanel deviceProcessPanel, List<ProcessInfo> processInfoList) {
        deviceProcessPanel.getSearchField().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent exception) {
                DeviceProcessPanelEvent.this.processInfoList = deviceProcessPanel.getProcessInfoList();
                if (!StringUtils.isEmpty(deviceProcessPanel.getSearchField().getText())) {
                    autoComplete(deviceProcessPanel.getSearchField().getText(), deviceProcessPanel.getProcessTable());
                } else {
                    autoComplete("", deviceProcessPanel.getProcessTable());
                }
            }

            @Override
            public void removeUpdate(DocumentEvent exception) {
                DeviceProcessPanelEvent.this.processInfoList = deviceProcessPanel.getProcessInfoList();
                if (!StringUtils.isEmpty(deviceProcessPanel.getSearchField().getText())) {
                    autoComplete(deviceProcessPanel.getSearchField().getText(), deviceProcessPanel.getProcessTable());
                } else {
                    autoComplete("", deviceProcessPanel.getProcessTable());
                }
            }

            @Override
            public void changedUpdate(DocumentEvent exception) {
            }
        });
    }

    /**
     * itemStateChanged
     *
     * @param deviceProcessPanel deviceProcessPanel
     */
    public void itemStateChanged(DeviceProcessPanel deviceProcessPanel) {
        deviceProcessPanel.getDeviceNameComboBox().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent exception) {
                for (DeviceIPPortInfo deviceInfo : deviceProcessPanel.getDeviceInfoList()) {
                    if (deviceInfo.getDeviceName()
                        .equals(deviceProcessPanel.getDeviceNameComboBox().getSelectedItem())) {
                        deviceProcessPanel.initProcessList(deviceInfo);
                    }
                }
            }
        });
    }

    /**
     * clickTable
     *
     * @param deviceProcessPanel deviceProcessPanel
     * @param deviceNum deviceNum
     * @param scrollPane scrollPane
     */
    public void clickTable(DeviceProcessPanel deviceProcessPanel, String deviceNum, JPanel scrollPane) {
        deviceProcessPanel.getProcessTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                int selectedRow = deviceProcessPanel.getProcessTable().getSelectedRow();
                deviceProcessPanel.getSelectedProcessName()
                    .setText(deviceProcessPanel.getProcessTable().getValueAt(selectedRow, 0) + "");
                // 获取当前设备下的选中的进程信息
                for (int index = 0; index < deviceProcessPanel.getProcessInfoList().size(); index++) {
                    ProcessInfo mapValue = deviceProcessPanel.getProcessInfoList().get(index);
                    if (deviceProcessPanel.getSelectedProcessName().getText()
                        .equals(mapValue.getProcessName() + "(" + mapValue.getProcessId() + ")")) {
                        // 更新map
                        DeviceIPPortInfo deviceIPPortInfo = deviceProcessPanel.getDeviceInfoList().get(0);
                        Map<DeviceIPPortInfo, ProcessInfo> mapObject = new HashMap<>();
                        mapObject.put(deviceIPPortInfo, deviceProcessPanel.getProcessInfoList().get(index));
                        Constant.map.put(deviceNum, mapObject);
                    }
                }
                closeProcessList(deviceProcessPanel, scrollPane);
            }
        });
    }

    /**
     * devicesInfoJComboBoxUpdate
     *
     * @param deviceProcessPanel deviceProcessPanel
     */
    public void devicesInfoJComboBoxUpdate(DeviceProcessPanel deviceProcessPanel) {
        QuartzManager.getInstance().addExecutor(DEVICE_REFRESH, new Runnable() {
            @Override
            public void run() {
                List<DeviceIPPortInfo> deviceInfos = MultiDeviceManager.getInstance().getOnlineDeviceInfoList();

                if (!deviceInfos.isEmpty()) {
                    deviceProcessPanel.setDeviceInfoList(deviceInfos);
                    Vector<String> items = new Vector<>();
                    deviceInfos.forEach(deviceInfo -> {
                        items.add(deviceInfo.getDeviceName());
                    });
                    if (!oldDevice.equals(items)) {
                        oldDevice = items;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                deviceProcessPanel.getDeviceNameComboBox().setModel(new DefaultComboBoxModel(items));
                            }
                        });
                    }
                } else {
                    // clear the device info
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Vector<String> items = new Vector<>();
                            deviceProcessPanel.getDeviceNameComboBox().setModel(new DefaultComboBoxModel(items));
                            Constant.map.clear();
                            deviceProcessPanel.getDeviceInfoList().clear();
                            // clear the process info
                            List<ProcessInfo> processInfos = new ArrayList<>();
                            deviceProcessPanel.setDeviceInfoList(deviceInfos);
                            deviceProcessPanel.setProcessInfoList(processInfos);
                            deviceProcessPanel.getSelectedProcessName().setText("");
                            Vector columnNames = new Vector();
                            columnNames.add("");
                            Vector processNames = new Vector<>();
                            DefaultTableModel model = new DefaultTableModel(processNames, columnNames);
                            JTable table = deviceProcessPanel.getProcessTable();
                            table.setModel(model);
                            table.getTableHeader().setVisible(false);
                            table.setRowHeight(LayoutConstants.DEVICE_ADD_HEIGHT);
                        }
                    });
                }
            }
        });
        QuartzManager.getInstance().startExecutor(DEVICE_REFRESH, 0, LayoutConstants.THOUSAND);
    }

    /**
     * autoComplete
     *
     * @param name name
     * @param jTable jTable
     */
    public void autoComplete(String name, JTable jTable) {
        int rowCountNew = processInfoList.size();
        String[] columnNames = {""};
        if (!name.isEmpty()) {
            int numTableValues = 0;
            int count = 0;
            for (int index = 0; index < rowCountNew; index++) {
                ProcessInfo processInfo = processInfoList.get(index);
                String processName = processInfo.getProcessName();
                if (processName.contains(name)) {
                    count++;
                }
            }
            String[][] tableValues = new String[count][1];
            for (int index = 0; index < rowCountNew; index++) {
                ProcessInfo processInfo = processInfoList.get(index);
                String processName = processInfo.getProcessName();
                if (processName.contains(name)) {
                    tableValues[numTableValues][0] =
                        processInfo.getProcessName() + "(" + processInfo.getProcessId() + ")";
                    numTableValues++;
                }
            }
            DefaultTableModel model = new DefaultTableModel(tableValues, columnNames);
            jTable.setModel(model);
        } else {
            int numTableValues = 0;
            String[][] tableValues = new String[rowCountNew][1];
            for (int index = 0; index < rowCountNew; index++) {
                ProcessInfo processInfo = processInfoList.get(index);
                tableValues[numTableValues][0] = processInfo.getProcessName() + "(" + processInfo.getProcessId() + ")";
                numTableValues++;
            }
            DefaultTableModel model = new DefaultTableModel(tableValues, columnNames);
            jTable.setModel(model);
        }
    }

    private void switchProcessList(DeviceProcessPanel deviceProcessPanel, JBPanel scrollPane, String deviceNum) {
        if (!deviceProcessPanel.getProcessPanel().isVisible()) {
            openProcessList(deviceProcessPanel, scrollPane, deviceNum);
        } else {
            closeProcessList(deviceProcessPanel, scrollPane);
        }
    }

    private void openProcessList(DeviceProcessPanel deviceProcessPanel, JPanel scrollPane, String deviceNum) {
        deviceProcessPanel.getProcessPanel().setVisible(true);
        SwingWorker<HashMap<DeviceIPPortInfo, List<ProcessInfo>>, Integer> task =
            new SwingWorker<HashMap<DeviceIPPortInfo, List<ProcessInfo>>, Integer>() {
                @Override
                protected HashMap<DeviceIPPortInfo, List<ProcessInfo>> doInBackground() {
                    List<ProcessInfo> processInfos = new ArrayList<>();
                    HashMap<DeviceIPPortInfo, List<ProcessInfo>> map = new HashMap<>();
                    for (DeviceIPPortInfo deviceInfo : deviceProcessPanel.getDeviceInfoList()) {
                        if (deviceInfo.getDeviceName()
                            .equals(deviceProcessPanel.getDeviceNameComboBox().getSelectedItem())) {
                            ProcessManager instance = ProcessManager.getInstance();
                            if (!instance.isRequestProcess()) {
                                processInfos = ProcessManager.getInstance().getProcessList(deviceInfo);
                                map.put(deviceInfo, processInfos);
                            }
                            break;
                        }
                    }
                    return map;
                }

                @Override
                protected void done() {
                    try {
                        doneProcessList(get(), deviceProcessPanel, deviceNum, scrollPane);
                    } catch (InterruptedException interruptedException) {
                        LOGGER.error(interruptedException.getMessage());
                    } catch (ExecutionException executionException) {
                        LOGGER.error(executionException.getMessage());
                    }
                }
            };
        task.execute();
    }

    private void closeProcessList(DeviceProcessPanel deviceProcessPanel, JPanel scrollPane) {
        deviceProcessPanel.getProcessPanel().setVisible(false);
    }

    private void doneProcessList(HashMap<DeviceIPPortInfo, List<ProcessInfo>> deviceProcess,
        DeviceProcessPanel deviceProcessPanel, String deviceNum, JPanel scrollPane) {
        List<ProcessInfo> processInfos = new ArrayList<>();
        DeviceIPPortInfo deviceInfo = new DeviceIPPortInfo();
        if (deviceProcess.isEmpty()) {
            return;
        }
        Set<Map.Entry<DeviceIPPortInfo, List<ProcessInfo>>> entries = deviceProcess.entrySet();
        Iterator<Map.Entry<DeviceIPPortInfo, List<ProcessInfo>>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<DeviceIPPortInfo, List<ProcessInfo>> entry = iterator.next();
            deviceInfo = entry.getKey();
            processInfos = entry.getValue();
        }

        deviceProcessPanel.setProcessInfoList(processInfos);
        // 创建列表
        Vector columnNames = new Vector();
        columnNames.add("");
        // 根据设备信息获取进程信息
        Vector processNames = new Vector<>();
        for (int index = 0; index < processInfos.size(); index++) {
            ProcessInfo processInfo = processInfos.get(index);
            Vector<String> vector = new Vector();
            vector.add(processInfo.getProcessName() + "(" + processInfo.getProcessId() + ")");
            processNames.add(vector);
        }
        if (!processInfos.isEmpty()) {
            // 更新map
            Map<DeviceIPPortInfo, ProcessInfo> mapObject = new HashMap<>();
            mapObject.put(deviceInfo, processInfos.get(0));
            Constant.map.put(deviceNum, mapObject);
        }

        DefaultTableModel model = new DefaultTableModel(processNames, columnNames);
        JTable table = deviceProcessPanel.getProcessTable();
        table.setModel(model);
        table.getTableHeader().setVisible(false);
        table.setRowHeight(LayoutConstants.DEVICE_ADD_HEIGHT);
        scrollPane.updateUI();
        scrollPane.repaint();
    }

    /**
     * addClickListener
     *
     * @param deviceProcessPanel deviceProcessPanel
     * @param scrollPane scrollPane
     * @param deviceNum deviceNum
     */
    public void addClickListener(DeviceProcessPanel deviceProcessPanel, JBPanel scrollPane, String deviceNum) {
        deviceProcessPanel.getSelectedProcessName().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                switchProcessList(deviceProcessPanel, scrollPane, deviceNum);
            }
        });
    }

    /**
     * mouseEffectTable
     *
     * @param table table
     */
    public void mouseEffectTable(JTable table) {
        table.setDefaultRenderer(Object.class, new TableCellRenderer());
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(final MouseEvent mMoved) {
                try {
                    rowCount = table.rowAtPoint(mMoved.getPoint());
                    int row = table.rowAtPoint(mMoved.getPoint());
                    int col = table.columnAtPoint(mMoved.getPoint());
                    table.setRowSelectionInterval(row, row);
                    table.setColumnSelectionInterval(col, col);
                } catch (IllegalArgumentException illegalArgumentException) {
                    LOGGER.error("DeviceProcessJpanelEvent happen llegalArgumentException");
                }
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent mre) {
                try {
                    if (mre.getClickCount() == LayoutConstants.INDEX_ONE && SwingUtilities.isRightMouseButton(mre)) {
                        int row = table.rowAtPoint(mre.getPoint());
                        int col = table.columnAtPoint(mre.getPoint());
                        table.setRowSelectionInterval(row, row);
                        table.setColumnSelectionInterval(col, col);
                    }
                } catch (IllegalArgumentException illegalArgumentException) {
                    LOGGER.error("DeviceProcessPanelEvent happen illegalArgumentException");
                }
            }
        });
    }

    /**
     * Device connection mode changed, set to the latest value
     *
     * @param deviceInfo deviceInfo
     * @param jComboBoxConnect jComboBoxConnect
     */
    public void connectTypeChanged(DeviceIPPortInfo deviceInfo, JComboBox<String> jComboBoxConnect) {
        jComboBoxConnect.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent exception) {
                deviceInfo.setConnectType(jComboBoxConnect.getSelectedItem().toString());
            }
        });
    }

    class TableCellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = LayoutConstants.SERIALVERSIONUID;

        @Override
        public Component getTableCellRendererComponent(JTable jtable, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
            JLabel label = null;
            Component tableCellRendererComponent =
                super.getTableCellRendererComponent(jtable, value, isSelected, hasFocus, row, column);

            if (tableCellRendererComponent instanceof JLabel) {
                label = (JLabel) tableCellRendererComponent;
                label.setForeground(JBColor.foreground().brighter());
                if (row == rowCount) {
                    label.setBackground(JBColor.background().darker());
                } else {
                    label.setBackground(JBColor.background());
                }
            }
            return label;
        }
    }
}
