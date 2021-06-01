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

import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.service.MultiDeviceManager;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.process.service.ProcessManager;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.swing.DeviceProcessJpanel;
import ohos.devtools.views.layout.swing.TaskScenePanel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.DefaultComboBoxModel;
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
import java.awt.Dimension;
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

import static ohos.devtools.views.common.Constant.DEVICEREFRESH;

/**
 * DeviceProcessJpanelEvent
 *
 * @version 1.0
 * @date 2021/3/4 19:02
 **/
public class DeviceProcessJpanelEvent extends TaskScenePanelEvent {
    private static final Logger LOGGER = LogManager.getLogger(DeviceProcessJpanelEvent.class);

    private boolean flag = false;

    private Vector<String> oldDevice = new Vector<>();

    // 原始jTable,包含当前表格所有的进程列表
    private List<ProcessInfo> processInfoList;

    private int rowCount = -1;

    /**
     * searchJButtonSelect
     *
     * @param deviceProcessJpanel deviceProcessJpanel
     * @param processInfoList     processInfoList
     */
    public void searchJButtonSelect(DeviceProcessJpanel deviceProcessJpanel, List<ProcessInfo> processInfoList) {
        // 搜索框输入值直接开始搜索
        deviceProcessJpanel.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            /**
             * insertUpdate
             *
             * @param exception exception
             */
            @Override
            public void insertUpdate(DocumentEvent exception) {
                DeviceProcessJpanelEvent.this.processInfoList = deviceProcessJpanel.getProinfos();
                if (!StringUtils.isEmpty(deviceProcessJpanel.getTextField().getText())) {
                    autoComplete(deviceProcessJpanel.getTextField().getText(), deviceProcessJpanel.getTable());
                } else {
                    autoComplete("", deviceProcessJpanel.getTable());
                }
            }

            /**
             * removeUpdate
             *
             * @param exception exception
             */
            @Override
            public void removeUpdate(DocumentEvent exception) {
                DeviceProcessJpanelEvent.this.processInfoList = deviceProcessJpanel.getProinfos();
                if (!StringUtils.isEmpty(deviceProcessJpanel.getTextField().getText())) {
                    autoComplete(deviceProcessJpanel.getTextField().getText(), deviceProcessJpanel.getTable());
                } else {
                    autoComplete("", deviceProcessJpanel.getTable());
                }
            }

            /**
             * changedUpdate
             *
             * @param exception exception
             */
            @Override
            public void changedUpdate(DocumentEvent exception) {
            }
        });
    }

    /**
     * itemStateChanged
     *
     * @param deviceProcessJpanel deviceProcessJpanel
     * @param taskScenePanel      taskScenePanel
     * @param deviceNum           deviceNum
     * @param scrollPane          scrollPane
     */
    public void itemStateChanged(DeviceProcessJpanel deviceProcessJpanel, TaskScenePanel taskScenePanel,
        String deviceNum, JPanel scrollPane) {
        deviceProcessJpanel.getJComboBoxPhone().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent exception) {
                // 获取选中项，设置对象信息用于查询对应的进程信息
                for (DeviceIPPortInfo deviceInfo : deviceProcessJpanel.getDeviceInfos()) {
                    if (deviceInfo.getDeviceName().equals(deviceProcessJpanel.getJComboBoxPhone().getSelectedItem())) {
                        deviceProcessJpanel.createProcessList(deviceInfo, deviceNum, scrollPane, taskScenePanel);
                    }
                }
            }
        });
    }

    /**
     * clickTable
     *
     * @param deviceProcessJpanel deviceProcessJpanel
     * @param deviceNum           deviceNum
     * @param deviceInfo          deviceInfo
     * @param scrollPane          scrollPane
     * @param taskScenePanel      taskScenePanel
     */
    public void clickTable(DeviceProcessJpanel deviceProcessJpanel, String deviceNum, DeviceIPPortInfo deviceInfo,
        JPanel scrollPane, TaskScenePanel taskScenePanel) {
        deviceProcessJpanel.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                int selectedRow = deviceProcessJpanel.getTable().getSelectedRow();
                deviceProcessJpanel.getLabelName()
                    .setText(deviceProcessJpanel.getTable().getValueAt(selectedRow, 0) + "");
                // 获取当前设备下的选中的进程信息
                for (int index = 0; index < deviceProcessJpanel.getProinfos().size(); index++) {
                    ProcessInfo mapValue = deviceProcessJpanel.getProinfos().get(index);
                    if (deviceProcessJpanel.getLabelName().getText()
                        .equals(mapValue.getProcessName() + "(" + mapValue.getProcessId() + ")")) {
                        // 更新map
                        DeviceIPPortInfo deviceIPPortInfo = deviceProcessJpanel.getDeviceInfos().get(0);
                        Map<DeviceIPPortInfo, ProcessInfo> mapObject = new HashMap<>();
                        mapObject.put(deviceIPPortInfo, deviceProcessJpanel.getProinfos().get(index));
                        Constant.map.put(deviceNum, mapObject);
                    }
                }
                // 当选中值后，关闭进程下拉列表
                closeProcessList(deviceProcessJpanel, scrollPane, deviceProcessJpanel.getTaskScenePanelEvent(),
                    taskScenePanel);
            }
        });
    }

    /**
     * 设备信息修改
     *
     * @param deviceProcessJpanel deviceProcessJpanel
     */
    public void devicesInfoJComboBoxUpdate(DeviceProcessJpanel deviceProcessJpanel) {
        QuartzManager.getInstance().addExecutor(DEVICEREFRESH, new Runnable() {
            @Override
            public void run() {
                List<DeviceIPPortInfo> deviceInfos = MultiDeviceManager.getInstance().getAllDeviceIPPortInfos();

                if (!deviceInfos.isEmpty()) {
                    deviceProcessJpanel.setDeviceInfos(deviceInfos);
                    Vector<String> items = new Vector<>();
                    deviceInfos.forEach(deviceInfo -> {
                        items.add(deviceInfo.getDeviceName());
                    });
                    if (!oldDevice.equals(items)) {
                        oldDevice = items;
                        deviceProcessJpanel.getJComboBoxPhone().setModel(new DefaultComboBoxModel(items));
                    }
                } else {
                    // 清空设备列表
                    Vector<String> items = new Vector<>();
                    deviceProcessJpanel.getJComboBoxPhone().setModel(new DefaultComboBoxModel(items));
                    Constant.map.clear();
                    deviceProcessJpanel.getDeviceInfos().clear();
                    // 清空进程列表
                    List<ProcessInfo> processInfos = new ArrayList<>();
                    deviceProcessJpanel.setDeviceInfos(deviceInfos);
                    deviceProcessJpanel.setProinfos(processInfos);
                    deviceProcessJpanel.getLabelName().setText("");
                    Vector columnNames = new Vector();
                    columnNames.add("");
                    Vector processNames = new Vector<>();
                    DefaultTableModel model = new DefaultTableModel(processNames, columnNames);
                    JTable table = deviceProcessJpanel.getTable();
                    table.setModel(model);
                    table.getTableHeader().setVisible(false);
                    table.setRowHeight(LayoutConstants.DEVICE_ADD_HEIGHT);
                }
            }
        });
        QuartzManager.getInstance().startExecutor(DEVICEREFRESH, 0, LayoutConstants.THOUSAND);
    }

    /**
     * 自动完成
     *
     * @param name   name
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

    private void judgmentJbutton(DeviceProcessJpanel deviceProcessJpanel, TaskScenePanel taskScenePanel, String type) {
        if ("open".equals(type) && deviceProcessJpanel.getComponentCount() == LayoutConstants.EIGHT_NUM) {
            taskScenePanel.getJButtonAddDevice().setBounds(LayoutConstants.APP_LABEL_Y1,
                taskScenePanel.getJButtonAddDevice().getY() + LayoutConstants.DEVICE_NAME_WIDTH,
                LayoutConstants.DEVICE_ADD_WIDTH, LayoutConstants.DEVICE_ADD_HEIGHT);
        }
        if ("close".equals(type) && deviceProcessJpanel.getComponentCount() == LayoutConstants.INDEX_SEVEN) {
            taskScenePanel.getJButtonAddDevice().setBounds(LayoutConstants.APP_LABEL_Y1,
                taskScenePanel.getJButtonAddDevice().getY() - LayoutConstants.DEVICE_NAME_WIDTH,
                LayoutConstants.DEVICE_ADD_WIDTH, LayoutConstants.DEVICE_ADD_HEIGHT);
        }
    }

    private void closeOrOpenProcessList(DeviceProcessJpanel deviceProcessJpanel, JPanel scrollPane,
        TaskScenePanelEvent taskScenePanelEvent, String deviceNum, TaskScenePanel taskScenePanel) {
        if (!flag) {
            int numHeight = 0;
            deviceProcessJpanel.add(deviceProcessJpanel.getJPanelProcess());
            deviceProcessJpanel
                .setBounds(LayoutConstants.DEVICE_PRO_X, deviceProcessJpanel.getY(), LayoutConstants.DEVICE_PRO_WIDTH,
                    deviceProcessJpanel.getHeight() + LayoutConstants.DEVICE_NAME_WIDTH);
            numHeight = forCycle(deviceProcessJpanel, scrollPane);
            scrollPane.setPreferredSize(new Dimension(LayoutConstants.DEVICE_PRO_WIDTH, numHeight));
            taskScenePanelEvent.setNum(taskScenePanelEvent.getNum() + LayoutConstants.DEVICE_NAME_WIDTH);
            // 判断当前panel是否含有adddevice按钮
            judgmentJbutton(deviceProcessJpanel, taskScenePanel, "open");
            SwingWorker<HashMap<DeviceIPPortInfo, List<ProcessInfo>>, Integer> task =
                new SwingWorker<HashMap<DeviceIPPortInfo, List<ProcessInfo>>, Integer>() {
                    /**
                     * doInBackground
                     *
                     * @return HashMap<DeviceIPPortInfo, List < ProcessInfo>>
                     * @throws Exception Exception
                     */
                    @Override
                    protected HashMap<DeviceIPPortInfo, List<ProcessInfo>> doInBackground() throws Exception {
                        List<ProcessInfo> processInfos = new ArrayList<>();
                        HashMap<DeviceIPPortInfo, List<ProcessInfo>> map = new HashMap<>();
                        for (DeviceIPPortInfo deviceInfo : deviceProcessJpanel.getDeviceInfos()) {
                            if (deviceInfo.getDeviceName()
                                .equals(deviceProcessJpanel.getJComboBoxPhone().getSelectedItem())) {
                                processInfos = ProcessManager.getInstance().getProcessList(deviceInfo);
                                map.put(deviceInfo, processInfos);
                                break;
                            }
                        }
                        return map;
                    }

                    /**
                     * done
                     */
                    @Override
                    protected void done() {
                        try {
                            doneProcessList(get(), deviceProcessJpanel, deviceNum, scrollPane);
                        } catch (InterruptedException exception) {
                            LOGGER.error(exception.getMessage());
                        } catch (ExecutionException exception) {
                            LOGGER.error(exception.getMessage());
                        }
                    }
                };
            task.execute();
            flag = true;
        } else {
            // 关闭进程下拉列表
            closeProcessList(deviceProcessJpanel, scrollPane, taskScenePanelEvent, taskScenePanel);
        }
    }

    private void doneProcessList(HashMap<DeviceIPPortInfo, List<ProcessInfo>> deviceProcess,
        DeviceProcessJpanel deviceProcessJpanel, String deviceNum, JPanel scrollPane) {
        List<ProcessInfo> processInfos = new ArrayList<>();
        DeviceIPPortInfo deviceInfo = new DeviceIPPortInfo();
        Set<Map.Entry<DeviceIPPortInfo, List<ProcessInfo>>> entries = deviceProcess.entrySet();
        Iterator<Map.Entry<DeviceIPPortInfo, List<ProcessInfo>>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<DeviceIPPortInfo, List<ProcessInfo>> entry = iterator.next();
            deviceInfo = entry.getKey();
            processInfos = entry.getValue();
        }

        deviceProcessJpanel.setProinfos(processInfos);
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
        JTable table = deviceProcessJpanel.getTable();
        table.setModel(model);
        table.getTableHeader().setVisible(false);
        table.setRowHeight(LayoutConstants.DEVICE_ADD_HEIGHT);
        scrollPane.updateUI();
        scrollPane.repaint();
    }

    /**
     * forCycle
     *
     * @param deviceProcessJpanel deviceProcessJpanel
     * @param numHeight           numHeight
     * @param scrollPane          scrollPane
     * @return int
     */
    public int forCycle(DeviceProcessJpanel deviceProcessJpanel, JPanel scrollPane) {
        Component[] component = scrollPane.getComponents();
        int newHeight = 0;
        for (Component componentJpanel : component) {
            JPanel jCom = (JPanel) componentJpanel;
            newHeight += jCom.getHeight();
            if (jCom.getY() > deviceProcessJpanel.getY()) {
                jCom.setBounds(LayoutConstants.DEVICE_PRO_X, jCom.getY() + LayoutConstants.DEVICE_NAME_WIDTH,
                    LayoutConstants.DEVICE_PRO_WIDTH, jCom.getHeight());
            }
        }
        return newHeight;
    }

    private void closeProcessList(DeviceProcessJpanel deviceProcessJpanel, JPanel scrollPane,
        TaskScenePanelEvent taskScenePanelEvent, TaskScenePanel taskScenePanel) {
        deviceProcessJpanel.remove(deviceProcessJpanel.getJPanelProcess());
        deviceProcessJpanel
            .setBounds(LayoutConstants.DEVICE_PRO_X, deviceProcessJpanel.getY(), LayoutConstants.DEVICE_PRO_WIDTH,
                deviceProcessJpanel.getHeight() - LayoutConstants.DEVICE_NAME_WIDTH);
        Component[] component = scrollPane.getComponents();
        for (Component componentJpanel : component) {
            JPanel jCom = null;
            if (componentJpanel instanceof JPanel) {
                jCom = (JPanel) componentJpanel;
                if (jCom.getY() > deviceProcessJpanel.getY()) {
                    jCom.setBounds(LayoutConstants.DEVICE_PRO_X, jCom.getY() - LayoutConstants.DEVICE_NAME_WIDTH,
                        LayoutConstants.DEVICE_PRO_WIDTH, jCom.getHeight());
                }
            }
        }
        scrollPane.setPreferredSize(new Dimension(LayoutConstants.DEVICE_PRO_WIDTH,
            scrollPane.getHeight() - LayoutConstants.DEVICE_NAME_WIDTH));
        taskScenePanelEvent.setNum(taskScenePanelEvent.getNum() - LayoutConstants.DEVICE_NAME_WIDTH);
        // 判断当前panel是否含有adddevice按钮
        judgmentJbutton(deviceProcessJpanel, taskScenePanel, "close");
        scrollPane.updateUI();
        scrollPane.repaint();
        flag = false;
    }

    /**
     * addClickListener
     *
     * @param deviceProcessJpanel deviceProcessJpanel
     * @param scrollPane          scrollPane
     * @param taskScenePanelEvent taskScenePanelEvent
     * @param taskScenePanel      taskScenePanel
     * @param deviceNum           deviceNum
     */
    public void addClickListener(DeviceProcessJpanel deviceProcessJpanel, JPanel scrollPane,
        TaskScenePanelEvent taskScenePanelEvent, TaskScenePanel taskScenePanel, String deviceNum) {
        deviceProcessJpanel.getLabelName().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                closeOrOpenProcessList(deviceProcessJpanel, scrollPane, taskScenePanelEvent, deviceNum, taskScenePanel);
            }
        });
    }

    /**
     * table鼠标悬停效果
     *
     * @param table table
     */
    public void mouseEffectTable(JTable table) {
        table.setDefaultRenderer(Object.class, new TableCellRenderer());
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(final MouseEvent mMoved) {
                rowCount = table.rowAtPoint(mMoved.getPoint());
                int row = table.rowAtPoint(mMoved.getPoint());
                int col = table.columnAtPoint(mMoved.getPoint());
                table.setRowSelectionInterval(row, row);
                table.setColumnSelectionInterval(col, col);
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseReleased(java.awt.event.MouseEvent mre) {
                if (mre.getClickCount() == LayoutConstants.INDEX_ONE && SwingUtilities.isRightMouseButton(mre)) {
                    int row = table.rowAtPoint(mre.getPoint());
                    int col = table.columnAtPoint(mre.getPoint());
                    table.setRowSelectionInterval(row, row);
                    table.setColumnSelectionInterval(col, col);
                }
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
                if (row == rowCount) {
                    label.setBackground(ColorConstants.SELECTED_TABLE_COLOR);
                } else {
                    label.setBackground(ColorConstants.SYSTEM_TUNNING_SETTING_CENTER);
                }
            }
            return label;
        }
    }
}
