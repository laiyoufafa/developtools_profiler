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

import com.alibaba.fastjson.JSONObject;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.views.common.Common;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.hoscomp.HosJLabel;
import ohos.devtools.views.layout.swing.DeviceProcessJpanel;
import ohos.devtools.views.layout.swing.SampleDialogWrapper;
import ohos.devtools.views.layout.swing.TaskPanel;
import ohos.devtools.views.layout.swing.TaskScenePanel;
import ohos.devtools.views.layout.swing.TaskScenePanelChart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static ohos.devtools.views.common.Constant.DEVICEREFRESH;

/**
 * 设备进程容器
 *
 * @version 1.0
 * @date 2021/03/02
 **/
public class TaskScenePanelEvent {
    private static final Logger LOGGER = LogManager.getLogger(TaskScenePanelEvent.class);

    /**
     * 设备进程容器标题计数
     */
    private int numDevices = LayoutConstants.INDEX_ONE;

    private int num = LayoutConstants.DEVICE_PRO_Y;

    private boolean flag = false;

    private Common common = new Common();

    /**
     * startTask
     *
     * @param taskScenePanel taskScenePanel
     * @param jTaskPanel     jTaskPanel
     */
    public void startTask(TaskScenePanel taskScenePanel, TaskPanel jTaskPanel) {
        taskScenePanel.getJButtonStartTask().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);

                // 判断设备下拉框是否有数据
                int itemCount = taskScenePanel.getDeviceProcessJpanel().getJComboBoxPhone().getItemCount();
                if (itemCount == 0) {
                    new SampleDialogWrapper("prompt", "Device list is empty !").show();
                    return;
                }

                // 判断进程是否选择
                if ("Please select the device process !"
                    .equals(taskScenePanel.getDeviceProcessJpanel().getLabelName().getText())) {
                    new SampleDialogWrapper("prompt", "Please select the device process !").show();
                    return;
                }

                boolean isSelected =
                    taskScenePanel.getCheckBoxMemoryJava().isSelected() || taskScenePanel.getCheckBoxGpuMemoryNative()
                        .isSelected() || taskScenePanel.getCheckBoxGraphics().isSelected() || taskScenePanel
                        .getCheckBoxStack().isSelected() || taskScenePanel
                        .getCheckBoxCode().isSelected() || taskScenePanel.getCheckBoxOthers().isSelected();
                if (!isSelected) {
                    new SampleDialogWrapper("prompt", "please choose Monitor Items !").show();
                    return;
                }
                // 获取所有进程设备信息map。用于请求后端接口
                List<HosJLabel> hosJLabels = obtainMap(taskScenePanel, jTaskPanel);
                if (!hosJLabels.isEmpty()) {
                    // 移除三级页面，添加新的三级页面
                    jTaskPanel.getOptionJPanel().removeAll();
                    QuartzManager.getInstance().endExecutor(DEVICEREFRESH);
                    TaskScenePanelChart taskScenePanelChart = new TaskScenePanelChart(jTaskPanel, hosJLabels);
                    jTaskPanel.getOptionJPanel().add(taskScenePanelChart);
                    // 更新所有的run -- of --
                    common.updateNum(Constant.jtasksTab);
                    jTaskPanel.getOptionJPanel().repaint();
                }
            }
        });
    }

    /**
     * Get the value of the drop-down box
     *
     * @param taskScenePanel taskScenePanel
     * @return JSONObject
     */
    private JSONObject getValueJCheckBoxs(TaskScenePanel taskScenePanel) {
        JSONObject memoryObject = new JSONObject();
        memoryObject.put(taskScenePanel.getJCheckBoxs()[LayoutConstants.INDEX_ONE].getText(),
            taskScenePanel.getJCheckBoxs()[LayoutConstants.INDEX_ONE].isSelected());
        memoryObject.put(taskScenePanel.getJCheckBoxs()[LayoutConstants.INDEX_TWO].getText(),
            taskScenePanel.getJCheckBoxs()[LayoutConstants.INDEX_TWO].isSelected());
        memoryObject.put(taskScenePanel.getJCheckBoxs()[LayoutConstants.INDEX_THREE].getText(),
            taskScenePanel.getJCheckBoxs()[LayoutConstants.INDEX_THREE].isSelected());
        memoryObject.put(taskScenePanel.getJCheckBoxs()[LayoutConstants.INDEX_FOUR].getText(),
            taskScenePanel.getJCheckBoxs()[LayoutConstants.INDEX_FOUR].isSelected());
        memoryObject.put(taskScenePanel.getJCheckBoxs()[LayoutConstants.INDEX_FIVE].getText(),
            taskScenePanel.getJCheckBoxs()[LayoutConstants.INDEX_FIVE].isSelected());
        memoryObject.put(taskScenePanel.getJCheckBoxs()[LayoutConstants.INDEX_SIX].getText(),
            taskScenePanel.getJCheckBoxs()[LayoutConstants.INDEX_SIX].isSelected());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Memory", memoryObject);
        return jsonObject;
    }

    /**
     * obtainMap
     *
     * @param taskScenePanel taskScenePanel
     * @param jTaskPanel     jTaskPanel
     * @return List<HosJLabel>
     */
    public List<HosJLabel> obtainMap(TaskScenePanel taskScenePanel, TaskPanel jTaskPanel) {
        // 获取下拉框的值
        JSONObject jsonObject = getValueJCheckBoxs(taskScenePanel);
        SessionManager sessionManager = SessionManager.getInstance();
        Collection<Map<DeviceIPPortInfo, ProcessInfo>> selectMaps = Constant.map.values();
        if (selectMaps.isEmpty()) {
            return new ArrayList();
        }
        ArrayList<HosJLabel> hosJLabels = new ArrayList<>();
        for (Map<DeviceIPPortInfo, ProcessInfo> seMap : selectMaps) {
            for (Map.Entry<DeviceIPPortInfo, ProcessInfo> entry : seMap.entrySet()) {
                DeviceIPPortInfo mapKey = null;
                Object keyObj = entry.getKey();
                if (keyObj instanceof DeviceIPPortInfo) {
                    mapKey = (DeviceIPPortInfo) keyObj;
                }
                ProcessInfo mapValue = null;
                Object valueObj = entry.getValue();
                if (valueObj instanceof ProcessInfo) {
                    mapValue = (ProcessInfo) valueObj;
                }

                if (mapKey != null && mapValue != null) {
                    Long localSessionID = sessionManager.createSession(mapKey, mapValue, 1, jsonObject);
                    if (localSessionID == ohos.devtools.datasources.utils.common.Constant.ABNORMAL
                        || localSessionID == null) {
                        return new ArrayList();
                    }
                    jTaskPanel.setLocalSessionId(localSessionID);
                    HosJLabel hosJLabel = new HosJLabel();
                    hosJLabel.setSessionId(localSessionID);
                    hosJLabel.setDeviceName(mapKey.getDeviceName());
                    hosJLabel.setProcessName(mapValue.getProcessName() + "(" + mapValue.getProcessId() + ")");
                    // 开启session
                    sessionManager.startSession(localSessionID, false);
                    // 获取数据
                    long date = DateTimeUtil.getNowTimeLong();
                    sessionManager.fetchData(localSessionID);
                    hosJLabel.setFirstStamp(date);
                    hosJLabels.add(hosJLabel);
                }
            }
        }
        return hosJLabels;
    }

    /**
     * Get device process information in the drop-down box for display
     *
     * @param scrollPane scrollPane
     * @return String
     */
    public String gain(JPanel scrollPane) {
        String processName = "";
        Component[] component = scrollPane.getComponents();
        for (Component componentOut : component) {
            if (componentOut instanceof JPanel) {
                JPanel jPanel = (JPanel) componentOut;
                Component[] componentInner = jPanel.getComponents();
                processName = judgCompontent(componentInner, processName);
            }
        }
        return processName;
    }

    /**
     * judgCompontent
     *
     * @param componentInner componentInner
     * @param processName    processName
     * @return String
     */
    public String judgCompontent(Component[] componentInner, String processName) {
        String processNameNew = processName;
        for (Component componentAll : componentInner) {
            if (componentAll instanceof JComboBox) {
                JComboBox jComboBox = (JComboBox) componentAll;
                processNameNew = processName + jComboBox.getSelectedItem().toString() + ",";
            }
            if (componentAll instanceof JPanel) {
                JPanel jPanelText = (JPanel) componentAll;
                Component[] componentText = jPanelText.getComponents();
                for (Component componentTextFile : componentText) {
                    if (componentTextFile instanceof JTextField) {
                        JTextField jTextField = (JTextField) componentTextFile;
                        processNameNew = processName + jTextField.getText() + ",";
                    }
                }
            }
        }
        return processNameNew;
    }

    /**
     * lastStep
     *
     * @param taskScenePanel taskScenePanel
     * @param jTaskPanel     jTaskPanel
     */
    public void lastStep(TaskScenePanel taskScenePanel, TaskPanel jTaskPanel) {
        taskScenePanel.getJButtonLastStep().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                super.mouseClicked(event);
                jTaskPanel.getOptionJPanel().remove(taskScenePanel);
                jTaskPanel.getOptionJPanelContent().setVisible(true);
                jTaskPanel.getOptionJPanel().repaint();
            }
        });
    }

    /**
     * 监听器
     *
     * @param taskScenePanel taskScenePanel
     */
    public void listenerJPanelSouth(TaskScenePanel taskScenePanel) {
        taskScenePanel.getJPanelSouth().addComponentListener(new ComponentAdapter() {
            /**
             * componentResized
             *
             * @param exception exception
             */
            public void componentResized(ComponentEvent exception) {
                int width = taskScenePanel.getJPanelSouth().getWidth();
                taskScenePanel.getJButtonLastStep()
                    .setBounds(LayoutConstants.NUMBER_STEP + (width - LayoutConstants.WINDOW_WIDTH),
                        LayoutConstants.DEVICES_HEIGHT, LayoutConstants.DEVICE_ADD_WIDTH,
                        LayoutConstants.CHOOSE_HEIGHT);
                taskScenePanel.getJButtonStartTask()
                    .setBounds(LayoutConstants.POSITION_TASK_X + (width - LayoutConstants.WINDOW_WIDTH),
                        LayoutConstants.DEVICES_HEIGHT, LayoutConstants.DEVICE_ADD_WIDTH,
                        LayoutConstants.CHOOSE_HEIGHT);
            }
        });
    }

    /**
     * 复选框选取
     *
     * @param taskScenePanel taskScenePanel
     */
    public void checkBoxSelect(TaskScenePanel taskScenePanel) {
        taskScenePanel.getCheckBoxSelectAll().addItemListener(new ItemListener() {
            /**
             * itemStateChanged
             *
             * @param event event
             */
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (taskScenePanel.getCheckBoxSelectAll().isSelected()) {
                    taskScenePanel.getCheckBoxMemoryJava().setSelected(true);
                    taskScenePanel.getCheckBoxGpuMemoryNative().setSelected(true);
                    taskScenePanel.getCheckBoxGraphics().setSelected(true);
                    taskScenePanel.getCheckBoxStack().setSelected(true);
                    taskScenePanel.getCheckBoxCode().setSelected(true);
                    taskScenePanel.getCheckBoxOthers().setSelected(true);
                } else {
                    taskScenePanel.getCheckBoxMemoryJava().setSelected(false);
                    taskScenePanel.getCheckBoxGpuMemoryNative().setSelected(false);
                    taskScenePanel.getCheckBoxGraphics().setSelected(false);
                    taskScenePanel.getCheckBoxStack().setSelected(false);
                    taskScenePanel.getCheckBoxCode().setSelected(false);
                    taskScenePanel.getCheckBoxOthers().setSelected(false);
                }
            }
        });
    }

    /**
     * Add device
     *
     * @param taskScenePanel      taskScenePanel
     * @param taskScenePanelEvent taskScenePanelEvent
     */
    public void clickAddDevice(TaskScenePanel taskScenePanel, TaskScenePanelEvent taskScenePanelEvent) {
        taskScenePanel.getJButtonAddDevice().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                numDevices++;
                num += LayoutConstants.WIDTHS;
                DeviceProcessJpanel deviceProcessJpanel =
                    new DeviceProcessJpanel(taskScenePanelEvent, taskScenePanel.getScrollPane(), taskScenePanel);
                taskScenePanel.getScrollPane().add(deviceProcessJpanel);
                taskScenePanel.getScrollPane()
                    .setPreferredSize(new Dimension(LayoutConstants.DEVICE_PRO_WIDTH, LayoutConstants.WIDTHS + num));
                taskScenePanel.getScrollPane().updateUI();
                taskScenePanel.getScrollPane().repaint();
            }
        });
    }

    /**
     * getNumDevices
     *
     * @return int
     */
    public int getNumDevices() {
        return numDevices;
    }

    /**
     * setNumDevices
     *
     * @param numDevices numDevices
     */
    public void setNumDevices(int numDevices) {
        this.numDevices = numDevices;
    }

    /**
     * getNum
     *
     * @return int
     */
    public int getNum() {
        return num;
    }

    /**
     * setNum
     *
     * @param num num
     */
    public void setNum(int num) {
        this.num = num;
    }
}
