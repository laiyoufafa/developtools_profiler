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

import com.alibaba.fastjson.JSONObject;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.common.customcomp.CustomJLabel;
import ohos.devtools.views.layout.chartview.TaskScenePanelChart;
import ohos.devtools.views.layout.dialog.SampleDialog;

import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static ohos.devtools.views.common.Constant.DEVICE_REFRESH;

/**
 * TaskScenePanel
 */
public class ApplicationConfigPanel extends JBPanel implements MouseListener, ItemListener {
    private static final String SCENE_TITLE_STR = "Devices & Applications";
    private static final String SCENE_DES_STR = "Task scene: Application tuning";
    private static final String ADD_DEVICE_STR = "Add Device";
    private static final String MONITOR_ITEMS_STR = "Monitor Items";
    private static final String MEMORY_STR = "Memory";
    private static final String CHECKBOX_ALL_STR = "Select All";
    private static final String CHECKBOX_JAVA_STR = "Java";
    private static final String CHECKBOX_NATIVE_STR = "Native";
    private static final String CHECKBOX_GRAPHICS_STR = "Graphics";
    private static final String CHECKBOX_STACK_STR = "Stack";
    private static final String CHECKBOX_CODE_STR = "Code";
    private static final String CHECKBOX_OTHER_STR = "Others";
    private static final String LAST_STEP_BTN_STR = "Last Step";
    private static final String START_TASK_BTN_STR = "Start Task";

    private TaskPanel taskPanel;
    private JBPanel northPanel;
    private JBLabel sceneTitlePanel;
    private JBLabel sceneDesPanel;
    private JBPanel centerPanel;
    private JBPanel deviceConnectPanel;
    private DeviceProcessPanel deviceProcessPanel;
    private JButton addDeviceBtn;
    private JBPanel dataSourceConfigPanel;
    private JBCheckBox[] memoryCheckBoxItems;
    private JBCheckBox allCheckBox;
    private JBCheckBox memoryJavaCheckBox;
    private JBCheckBox memoryNativeCheckBox;
    private JBCheckBox memoryGraphicsCheckBox;
    private JBCheckBox memoryStackCheckBox;
    private JBCheckBox memoryCodeCheckBox;
    private JBCheckBox memoryOthersCheckBox;
    private JBPanel scrollContainer;
    private JBScrollPane deviceConfigScrollPane;
    private JBPanel southPanel;
    private JButton lastStepBtn;
    private JButton startTaskBtn;

    private int deviceNum = 1;
    private int scrollContainerHeight = 0;

    /**
     * Task Scene Panel
     */
    public ApplicationConfigPanel() {
    }

    /**
     * TaskScenePanel
     *
     * @param taskPanel taskPanel
     */
    public ApplicationConfigPanel(TaskPanel taskPanel) {
        this.taskPanel = taskPanel;
        initComponents();
        addEventListener();
    }

    /**
     * initComponents
     */
    private void initComponents() {
        this.setLayout(new MigLayout("insets 0", "15[grow,fill]",
                "15[fill,fill]"));
        // init northPanel
        initNorthPanelItems();
        // init centerPanel
        initCenterPanelItems();
        // init southPanel
        initSouthPanelItems();
        // add the panel
        addPanels();
    }

    /**
     * initCenterPanelItems
     */
    private void initNorthPanelItems() {
        northPanel = new JBPanel(new MigLayout("insets 0"));
        sceneTitlePanel = new JBLabel(SCENE_TITLE_STR);
        sceneTitlePanel.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
        sceneTitlePanel.setForeground(JBColor.foreground().brighter());
        sceneDesPanel = new JBLabel(SCENE_DES_STR);
        sceneDesPanel.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
    }

    /**
     * initCenterPanelItems
     */
    private void initCenterPanelItems() {
        centerPanel = new JBPanel(new MigLayout("insets 0", "[grow,fill]20",
            "[fill,fill]"));
        scrollContainer = new JBPanel(new MigLayout("insets 0", "[grow,fill]",
            "[fill,fill]"));
        scrollContainer.setOpaque(false);
        scrollContainer.setBackground(JBColor.background());
        deviceConfigScrollPane = new JBScrollPane(scrollContainer, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        deviceConfigScrollPane.setBorder(null);
        // set the scroll rat
        deviceConfigScrollPane.getVerticalScrollBar().setUnitIncrement(LayoutConstants.SCROLL_UNIT_INCREMENT);
        deviceConnectPanel = new JBPanel(new MigLayout("insets 0", "[grow,fill]",
            "[fill,fill]"));
        deviceConnectPanel.setOpaque(false);
        deviceConnectPanel.add(deviceConfigScrollPane, "wrap, span");
        deviceConnectPanel.setBackground(JBColor.background());
        addDeviceBtn = new JButton(ADD_DEVICE_STR);
        addDeviceBtn.setName(ADD_DEVICE_STR);
        addDeviceBtn.setFont(new Font(Font.DIALOG, Font.PLAIN, LayoutConstants.OPTION_FONT));
        addDeviceBtn.setOpaque(false);
        addDeviceBtn.setPreferredSize(new Dimension(140, 40));
        deviceProcessPanel = new DeviceProcessPanel(scrollContainer, deviceNum, scrollContainerHeight);
        scrollContainer.add(deviceProcessPanel, "wrap, span");
        initDataSourceConfig();
    }

    /**
     * initDataSourceConfig
     */
    private void initDataSourceConfig() {
        dataSourceConfigPanel = new JBPanel(new MigLayout("insets 0", "20[]80[]",
            "20[]14[]14[]14[]14[]"));
        dataSourceConfigPanel.setOpaque(false);
        dataSourceConfigPanel.setPreferredSize(new Dimension(400, 450));
        dataSourceConfigPanel.setBackground(JBColor.background());
        allCheckBox = new JBCheckBox(CHECKBOX_ALL_STR);
        memoryJavaCheckBox = new JBCheckBox(CHECKBOX_JAVA_STR);
        memoryNativeCheckBox = new JBCheckBox(CHECKBOX_NATIVE_STR);
        memoryGraphicsCheckBox = new JBCheckBox(CHECKBOX_GRAPHICS_STR);
        memoryStackCheckBox = new JBCheckBox(CHECKBOX_STACK_STR);
        memoryCodeCheckBox = new JBCheckBox(CHECKBOX_CODE_STR);
        memoryOthersCheckBox = new JBCheckBox(CHECKBOX_OTHER_STR);
        JBLabel monitorTitleLabel = new JBLabel(MONITOR_ITEMS_STR);
        monitorTitleLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
        monitorTitleLabel.setForeground(JBColor.foreground().brighter());
        memoryCheckBoxItems = new JBCheckBox[LayoutConstants.INDEX_SEVEN];
        memoryCheckBoxItems[LayoutConstants.INDEX_ZERO] = allCheckBox;
        memoryCheckBoxItems[LayoutConstants.INDEX_ONE] = memoryJavaCheckBox;
        memoryCheckBoxItems[LayoutConstants.INDEX_TWO] = memoryNativeCheckBox;
        memoryCheckBoxItems[LayoutConstants.INDEX_THREE] = memoryGraphicsCheckBox;
        memoryCheckBoxItems[LayoutConstants.INDEX_FOUR] = memoryStackCheckBox;
        memoryCheckBoxItems[LayoutConstants.INDEX_FIVE] = memoryCodeCheckBox;
        memoryCheckBoxItems[LayoutConstants.INDEX_SIX] = memoryOthersCheckBox;
        // select all
        allCheckBox.setSelected(true);
        memoryJavaCheckBox.setSelected(true);
        memoryNativeCheckBox.setSelected(true);
        memoryGraphicsCheckBox.setSelected(true);
        memoryStackCheckBox.setSelected(true);
        memoryCodeCheckBox.setSelected(true);
        memoryOthersCheckBox.setSelected(true);
        allCheckBox.setOpaque(false);
        memoryJavaCheckBox.setOpaque(false);
        memoryNativeCheckBox.setOpaque(false);
        memoryGraphicsCheckBox.setOpaque(false);
        memoryStackCheckBox.setOpaque(false);
        memoryCodeCheckBox.setOpaque(false);
        memoryOthersCheckBox.setOpaque(false);
        dataSourceConfigPanel.add(monitorTitleLabel, "wrap");
        JBLabel memoryTitleLabel = new JBLabel(MEMORY_STR);
        dataSourceConfigPanel.add(memoryTitleLabel, "wrap");
        dataSourceConfigPanel.add(allCheckBox);
        dataSourceConfigPanel.add(memoryJavaCheckBox, "wrap");
        dataSourceConfigPanel.add(memoryNativeCheckBox);
        dataSourceConfigPanel.add(memoryGraphicsCheckBox, "wrap");
        dataSourceConfigPanel.add(memoryStackCheckBox);
        dataSourceConfigPanel.add(memoryCodeCheckBox,  "wrap");
        dataSourceConfigPanel.add(memoryOthersCheckBox);
    }

    /**
     * southPanelItems
     */
    private void initSouthPanelItems() {
        southPanel = new JBPanel(new MigLayout("insets 0", "push[]20[]20",
            "[fill,fill]"));
        southPanel.setPreferredSize(new Dimension(1200, 40));
        lastStepBtn = new JButton(LAST_STEP_BTN_STR);
        lastStepBtn.setName(LAST_STEP_BTN_STR);
        lastStepBtn.setFont(new Font(Font.DIALOG, Font.PLAIN, LayoutConstants.OPTION_FONT));
        lastStepBtn.setFocusPainted(false);
        lastStepBtn.setOpaque(false);
        lastStepBtn.setPreferredSize(new Dimension(140, 40));
        startTaskBtn = new JButton(START_TASK_BTN_STR);
        startTaskBtn.setName(UtConstant.UT_TASK_SCENE_PANE_START);
        startTaskBtn.setFont(new Font(Font.DIALOG, Font.PLAIN, LayoutConstants.OPTION_FONT));
        startTaskBtn.setOpaque(false);
        startTaskBtn.setFocusPainted(false);
        startTaskBtn.setPreferredSize(new Dimension(140, 40));
    }

    /**
     * add panels
     */
    private void addPanels() {
        northPanel.setOpaque(false);
        northPanel.add(sceneTitlePanel);
        northPanel.add(sceneDesPanel, "gap 5");
        centerPanel.setOpaque(false);
        centerPanel.add(deviceConnectPanel, "width max(100, 100%)");
        southPanel.setOpaque(false);
        southPanel.add(lastStepBtn);
        southPanel.add(startTaskBtn);
        this.add(northPanel, "wrap");
        this.add(centerPanel, "wrap");
        this.add(southPanel, "wrap");
        this.setBackground(JBColor.background().darker());
        this.setOpaque(true);
    }

    /**
     * addEventListener
     */
    private void addEventListener() {
        allCheckBox.addItemListener(this);
        lastStepBtn.addMouseListener(this);
        startTaskBtn.addMouseListener(this);
        addDeviceBtn.addMouseListener(this);
    }

    /**
     * obtainMap
     *
     * @param jTaskPanel jTaskPanel
     * @return List<HosJLabel>
     */
    public List<CustomJLabel> obtainMap(TaskPanel jTaskPanel) {
        SessionManager sessionManager = SessionManager.getInstance();
        Collection<Map<DeviceIPPortInfo, ProcessInfo>> selectMaps = Constant.map.values();
        if (selectMaps.isEmpty()) {
            return new ArrayList<>();
        }
        ArrayList<CustomJLabel> hosJLabels = new ArrayList<>();
        for (Map<DeviceIPPortInfo, ProcessInfo> seMap : selectMaps) {
            for (Map.Entry<DeviceIPPortInfo, ProcessInfo> entry : seMap.entrySet()) {
                DeviceIPPortInfo mapKey = null;
                DeviceIPPortInfo keyObj = entry.getKey();
                if (keyObj != null) {
                    mapKey = keyObj;
                }
                ProcessInfo mapValue = null;
                ProcessInfo valueObj = entry.getValue();
                if (valueObj != null) {
                    mapValue = valueObj;
                }
                if (mapKey != null && mapValue != null) {
                    Long localSessionID = sessionManager.createSession(mapKey, mapValue);
                    if (localSessionID.equals(ohos.devtools.datasources.utils.common.Constant.ABNORMAL)) {
                        return new ArrayList<>();
                    }
                    jTaskPanel.setLocalSessionId(localSessionID);
                    CustomJLabel hosJLabel = new CustomJLabel();
                    hosJLabel.setSessionId(localSessionID);
                    hosJLabel.setDeviceName(mapKey.getDeviceName());
                    hosJLabel.setProcessName(mapValue.getProcessName() + "(" + mapValue.getProcessId() + ")");
                    hosJLabel.setConnectType(mapKey.getConnectType());
                    // start session
                    sessionManager.startSession(localSessionID, false);
                    // get the data
                    sessionManager.fetchData(localSessionID);
                    hosJLabels.add(hosJLabel);
                }
            }
        }
        return hosJLabels;
    }

    /**
     * Get the value of the drop-down box
     *
     * @return JSONObject
     */
    private JSONObject getCheckBoxJson() {
        JSONObject memoryObject = new JSONObject();
        memoryObject.put(memoryCheckBoxItems[LayoutConstants.INDEX_ONE].getText(),
                memoryCheckBoxItems[LayoutConstants.INDEX_ONE].isSelected());
        memoryObject.put(memoryCheckBoxItems[LayoutConstants.INDEX_TWO].getText(),
                memoryCheckBoxItems[LayoutConstants.INDEX_TWO].isSelected());
        memoryObject.put(memoryCheckBoxItems[LayoutConstants.INDEX_THREE].getText(),
                memoryCheckBoxItems[LayoutConstants.INDEX_THREE].isSelected());
        memoryObject.put(memoryCheckBoxItems[LayoutConstants.INDEX_FOUR].getText(),
                memoryCheckBoxItems[LayoutConstants.INDEX_FOUR].isSelected());
        memoryObject.put(memoryCheckBoxItems[LayoutConstants.INDEX_FIVE].getText(),
                memoryCheckBoxItems[LayoutConstants.INDEX_FIVE].isSelected());
        memoryObject.put(memoryCheckBoxItems[LayoutConstants.INDEX_SIX].getText(),
                memoryCheckBoxItems[LayoutConstants.INDEX_SIX].isSelected());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Memory", memoryObject);
        return jsonObject;
    }

    @Override
    public void mouseClicked(MouseEvent event) {
    }

    @Override
    public void mousePressed(MouseEvent event) {
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        String name = event.getComponent().getName();
        if (name.equals(LAST_STEP_BTN_STR)) {
            taskPanel.getTabContainer().remove(this);
            taskPanel.getTabItem().setVisible(true);
            taskPanel.getTabContainer().repaint();
        }
        if (name.equals(START_TASK_BTN_STR)) {
            startTaskBtn.dispatchEvent(new FocusEvent(startTaskBtn, FocusEvent.FOCUS_GAINED, true));
            startTaskBtn.requestFocusInWindow();
            int itemCount = deviceProcessPanel.getDeviceNameComboBox().getItemCount();
            if (itemCount == 0) {
                new SampleDialog("prompt", "Device list is empty !").show();
                return;
            }
            if ("Please select the device process !"
                    .equals(deviceProcessPanel.getSelectedProcessName().getText())) {
                new SampleDialog("prompt", "Please select the device process !").show();
                return;
            }
            boolean isSelected = memoryJavaCheckBox.isSelected() || memoryNativeCheckBox.isSelected() ||
                    memoryGraphicsCheckBox.isSelected() || memoryStackCheckBox.isSelected() ||
                    memoryCodeCheckBox.isSelected() || memoryOthersCheckBox.isSelected();
            if (!isSelected) {
                new SampleDialog("prompt", "please choose Monitor Items !").show();
                return;
            }
            // get the process map
            List<CustomJLabel> hosJLabels = obtainMap(taskPanel);
            if (!hosJLabels.isEmpty()) {
                taskPanel.getTabContainer().removeAll();
                QuartzManager.getInstance().endExecutor(DEVICE_REFRESH);
                taskPanel.getTabContainer().add(new TaskScenePanelChart(taskPanel, hosJLabels));
                taskPanel.getTabContainer().setOpaque(true);
                taskPanel.getTabContainer().setBackground(JBColor.background());
                taskPanel.getTabContainer().repaint();
            }
        }
        if (name.equals(ADD_DEVICE_STR)) {
            deviceNum++;
            scrollContainerHeight += 450;
            DeviceProcessPanel addDeviceProcessPanel =
                    new DeviceProcessPanel(scrollContainer, deviceNum, scrollContainerHeight);
            scrollContainer.add(addDeviceProcessPanel.getGraphicsLine(), "wrap, gapy 15, gapx 12 12, height 30!, span");
            scrollContainer.add(addDeviceProcessPanel, "wrap, span");
            scrollContainer
                .setPreferredSize(new Dimension(LayoutConstants.DEVICE_PRO_WIDTH, 450 + scrollContainerHeight));
        }
    }

    @Override
    public void mouseEntered(MouseEvent event) {
    }

    @Override
    public void mouseExited(MouseEvent event) {
    }

    @Override
    public void itemStateChanged(ItemEvent event) {
        if (allCheckBox.isSelected()) {
            memoryJavaCheckBox.setSelected(true);
            memoryNativeCheckBox.setSelected(true);
            memoryGraphicsCheckBox.setSelected(true);
            memoryStackCheckBox.setSelected(true);
            memoryCodeCheckBox.setSelected(true);
            memoryOthersCheckBox.setSelected(true);
        } else {
            memoryJavaCheckBox.setSelected(false);
            memoryNativeCheckBox.setSelected(false);
            memoryGraphicsCheckBox.setSelected(false);
            memoryStackCheckBox.setSelected(false);
            memoryCodeCheckBox.setSelected(false);
            memoryOthersCheckBox.setSelected(false);
        }
    }
}
