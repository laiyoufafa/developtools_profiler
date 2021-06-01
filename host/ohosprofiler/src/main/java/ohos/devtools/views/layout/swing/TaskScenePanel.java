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

import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.event.TaskScenePanelEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/**
 * 三级场景界面
 *
 * @version 1.0
 * @date 2021/02/27 11:11
 **/
public class TaskScenePanel extends JPanel {
    /**
     * 三级界面边框布局管理器，top容器
     */
    private JPanel jPanelNorth = new JPanel(null);

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
    private JLabel jLabelTaskTun = new JLabel("Devices & Applications");

    /**
     * top容器中文本描述
     */
    private JLabel jLabelDeviceSet = new JLabel("Task scene: Application tuning");

    /**
     * Center容器中，左边容器
     */
    private JPanel jPanelCenterWest = new JPanel(new BorderLayout());

    /**
     * Center容器中，右边容器
     */
    private JPanel jPanelCenterRight = new JPanel(null);

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
    private TaskScenePanelEvent taskScenePanelEvent = new TaskScenePanelEvent();

    /**
     * Center容器左边部分边框布局top容器
     */
    private JPanel jPanelCenterWestTop = new JPanel(new BorderLayout());

    /**
     * Center容器左边部分边框布局center容器
     */
    private JPanel jPanelCenterWestCenter = new JPanel(new BorderLayout());

    /**
     * Center容器左边部分边框布局center容器中左边容器，用于布局按钮位置
     */
    private JPanel jPanelCenterWestCenterLeft = new JPanel();

    /**
     * Center容器左边部分边框布局center容器中右边容器，用于添加 Add Device按钮
     */
    private JPanel jPanelCenterWestCenterRight = new JPanel(null);

    /**
     * Add Device按钮，添加设备
     */
    private JLabel jButtonAddDevice = new JLabel("Add Device", JLabel.CENTER);

    /**
     * Monitor Items页面复选框内容
     */
    private JLabel jLabelMemory = new JLabel("Memory");
    private JCheckBox checkBoxSelectAll = new JCheckBox("Select All");
    private JCheckBox checkBoxMemoryJava = new JCheckBox("Java");
    private JCheckBox checkBoxGpuMemoryNative = new JCheckBox("Native");
    private JCheckBox checkBoxGraphics = new JCheckBox("Graphics");
    private JCheckBox checkBoxStack = new JCheckBox("Stack");
    private JCheckBox checkBoxCode = new JCheckBox("Code");
    private JCheckBox checkBoxOthers = new JCheckBox("Others");

    /**
     * Center容器中，左边布局滚动条容器
     */
    private JPanel scrollPane = new JPanel(null);

    /**
     * 返回上一页按钮
     */
    private JLabel jButtonLastStep = new JLabel("Last Step", JLabel.CENTER);

    /**
     * 开始任务按钮
     */
    private JLabel jButtonStartTask = new JLabel("Start Task", JLabel.CENTER);

    private JScrollPane jScrollPane =
        new JScrollPane(scrollPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    private DeviceProcessJpanel deviceProcessJpanel;

    /**
     * Task Scene Panel
     */
    public TaskScenePanel() {
    }

    /**
     * TaskScenePanel
     *
     * @param jTaskPanel jTaskPanel
     */
    public TaskScenePanel(TaskPanel jTaskPanel) {
        // 设置三级界面布局方式为边框布局管理
        this.setLayout(new BorderLayout());
        // 设置top容器，Center容器，South容器的属性
        setAttributes();
        // 给Center容器分为左右边框布局
        setBorderLeftRight();
        // Center容器左边部分分为上下边框布局
        setBorderTopCenter();
        // 三级页面中Monitor Items页面设置
        setJPanelCenterRight();
        // 添加复选框
        addCheckBox();
        // South容器设置
        setJPanelSouth(jTaskPanel);
    }

    /**
     * SouthContainer settings
     *
     * @param jTaskPanel jTaskPanel
     */
    public void setJPanelSouth(TaskPanel jTaskPanel) {
        Font font = new Font(Font.DIALOG, Font.PLAIN, LayoutConstants.OPTION_FONT);
        jButtonLastStep.setFont(font);
        jButtonLastStep.setBorder(BorderFactory.createLineBorder(ColorConstants.BORDER_COLOR));
        jButtonLastStep.setOpaque(true);
        jButtonLastStep.setBounds(LayoutConstants.NUMBER_STEP + (jPanelSouth.getWidth() - LayoutConstants.WINDOW_WIDTH),
            LayoutConstants.JP_LABEL_HEIGHT, LayoutConstants.JP_LEFT_WIDTH, LayoutConstants.DEVICE_X);
        jButtonLastStep.setBackground(ColorConstants.CENTER_COLOR);
        jButtonStartTask.setForeground(Color.white);
        jButtonStartTask.setOpaque(true);
        jButtonStartTask
            .setBounds(LayoutConstants.POSITION_TASK_X + (jPanelSouth.getWidth() - LayoutConstants.WINDOW_WIDTH),
                LayoutConstants.JP_LABEL_HEIGHT, LayoutConstants.JP_LEFT_WIDTH, LayoutConstants.DEVICE_X);
        jButtonStartTask.setBackground(ColorConstants.CHOOSE_BUTTON);
        jButtonStartTask.setFont(font);
        jPanelSouth.add(jButtonLastStep);
        jPanelSouth.add(jButtonStartTask);
        // 监听jPanelSouth大小变化并改变按钮位置
        taskScenePanelEvent.listenerJPanelSouth(this);
        // jButtonLastStep添加事件返回上一页
        taskScenePanelEvent.lastStep(this, jTaskPanel);
        // jButtonStartTask添加事件开始任务
        taskScenePanelEvent.startTask(this, jTaskPanel);
    }

    /**
     * Center容器左边部分分为边框布局上下两个部分
     */
    public void setBorderTopCenter() {
        scrollPane.setBackground(ColorConstants.SCROLL_PANE);
        jScrollPane.setBorder(null);
        // 设置鼠标滚轮速度
        jScrollPane.getVerticalScrollBar().setUnitIncrement(LayoutConstants.SCROLL_UNIT_INCREMENT);
        jPanelCenterWest.add(jScrollPane);
        // Add Device按钮事件
        taskScenePanelEvent.clickAddDevice(this, taskScenePanelEvent);
        deviceProcessJpanel = new DeviceProcessJpanel(taskScenePanelEvent, scrollPane, this);
        scrollPane.add(deviceProcessJpanel);
    }

    /**
     * Monitor Items页面，添加复选框
     */
    public void addCheckBox() {
        jCheckBoxs[LayoutConstants.INDEX_ZERO] = checkBoxSelectAll;
        jCheckBoxs[LayoutConstants.INDEX_ONE] = checkBoxMemoryJava;
        jCheckBoxs[LayoutConstants.INDEX_TWO] = checkBoxGpuMemoryNative;
        jCheckBoxs[LayoutConstants.INDEX_THREE] = checkBoxGraphics;
        jCheckBoxs[LayoutConstants.INDEX_FOUR] = checkBoxStack;
        jCheckBoxs[LayoutConstants.INDEX_FIVE] = checkBoxCode;
        jCheckBoxs[LayoutConstants.INDEX_SIX] = checkBoxOthers;
        // 默认全选
        checkBoxSelectAll.setSelected(true);
        checkBoxMemoryJava.setSelected(true);
        checkBoxGpuMemoryNative.setSelected(true);
        checkBoxGraphics.setSelected(true);
        checkBoxStack.setSelected(true);
        checkBoxCode.setSelected(true);
        checkBoxOthers.setSelected(true);
        jLabelMemory.setBounds(LayoutConstants.MEMORY_X, LayoutConstants.MEMORY_Y, LayoutConstants.MEMORY_WIDTH,
            LayoutConstants.MEMORY_HEIGHT);
        checkBoxSelectAll
            .setBounds(LayoutConstants.SELECT_ALL_X, LayoutConstants.SELECT_ALL_Y, LayoutConstants.SELECT_ALL_WIDTH,
                LayoutConstants.SELECT_ALL_HEIGHT);
        checkBoxMemoryJava.setBounds(LayoutConstants.JAVA_X, LayoutConstants.JAVA_Y, LayoutConstants.JAVA_WIDTH,
            LayoutConstants.JAVA_HEIGHT);
        checkBoxGpuMemoryNative
            .setBounds(LayoutConstants.NATIVE_X, LayoutConstants.NATIVE_Y, LayoutConstants.NATIVE_WIDTH,
                LayoutConstants.NATIVE_HEIGHT);
        checkBoxGraphics
            .setBounds(LayoutConstants.GRAPHICS_X, LayoutConstants.GRAPHICS_Y, LayoutConstants.GRAPHICS_WIDTH,
                LayoutConstants.GRAPHICS_HEIGHT);
        checkBoxStack.setBounds(LayoutConstants.STACK_X, LayoutConstants.STACK_Y, LayoutConstants.STACK_WIDTH,
            LayoutConstants.STACK_HEIGHT);
        checkBoxCode.setBounds(LayoutConstants.CODE_X, LayoutConstants.CODE_Y, LayoutConstants.CODE_WIDTH,
            LayoutConstants.CODE_HEIGHT);
        checkBoxOthers.setBounds(LayoutConstants.OTHERS_X, LayoutConstants.OTHERS_Y, LayoutConstants.OTHERS_WIDTH,
            LayoutConstants.OTHERS_HEIGHT);
        jPanelRightMemory.add(jLabelMemory);
        jPanelRightMemory.add(checkBoxSelectAll);
        jPanelRightMemory.add(checkBoxMemoryJava);
        jPanelRightMemory.add(checkBoxGpuMemoryNative);
        jPanelRightMemory.add(checkBoxGraphics);
        jPanelRightMemory.add(checkBoxStack);
        jPanelRightMemory.add(checkBoxCode);
        jPanelRightMemory.add(checkBoxOthers);
        // 复选框选中事件
        taskScenePanelEvent.checkBoxSelect(this);
    }

    /**
     * 三级页面中Monitor Items页面设置
     */
    public void setJPanelCenterRight() {
        JPanel jPanelRightTop = new JPanel(null);
        jPanelRightTop.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
        jPanelRightTop.setBounds(LayoutConstants.MONITOR_PANEL_X, LayoutConstants.MONITOR_PANEL_Y,
            LayoutConstants.MONITOR_PANEL_WIDTH, LayoutConstants.MONITOR_PANEL_HEIGHT);
        JLabel jLabel = new JLabel("Monitor Items");
        Font fontTaskTun = new Font(Font.DIALOG, Font.BOLD, LayoutConstants.MONITOR_BUN_FONT);
        jLabel.setFont(fontTaskTun);
        jLabel.setForeground(Color.WHITE);
        jLabel
            .setBounds(LayoutConstants.MONITOR_BUN_X, LayoutConstants.MONITOR_BUN_Y, LayoutConstants.MONITOR_BUN_WIDTH,
                LayoutConstants.MONITOR_BUN_HEIGHT);
        jPanelRightTop.add(jLabel);
        jPanelRightMemory
            .setBounds(LayoutConstants.MONITOR_OPT_X, LayoutConstants.MONITOR_OPT_Y, LayoutConstants.MONITOR_OPT_WIDTH,
                LayoutConstants.MONITOR_OPT_HEIGHT);
        jPanelRightMemory.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
        jPanelCenterRight.add(jPanelRightTop);
        jPanelCenterRight.add(jPanelRightMemory);

        // 设置背景色
        checkBoxSelectAll.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
        checkBoxMemoryJava.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
        checkBoxGpuMemoryNative.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
        checkBoxGraphics.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
        checkBoxStack.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
        checkBoxCode.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
        checkBoxOthers.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
    }

    /**
     * 给Center容器分为左右边框布局
     */
    public void setBorderLeftRight() {
        jPanelCenterWest.setOpaque(false);
        jPanelCenterRight.setOpaque(false);
        jPanelCenterWest
            .setPreferredSize(new Dimension(LayoutConstants.WEST_LABEL_WIDTH, LayoutConstants.WEST_LABEL_HEIGHT));
        jPanelCenterRight
            .setPreferredSize(new Dimension(LayoutConstants.EAST_LABEL_WIDTH, LayoutConstants.EAST_LABEL_HEIGHT));
        jPanelCenter.add(jPanelCenterWest, BorderLayout.WEST);
        jPanelCenter.add(jPanelCenterRight, BorderLayout.CENTER);
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
        jPanelNorth.setPreferredSize(new Dimension(LayoutConstants.TOP_SOUTH_WIDTH, LayoutConstants.TOP_SOUTH_HEIGHT));
        jPanelSouth.setPreferredSize(new Dimension(LayoutConstants.TOP_SOUTH_WIDTH, LayoutConstants.TOP_SOUTH_HEIGHT));
        this.add(jPanelNorth, BorderLayout.NORTH);
        this.add(jPanelCenter, BorderLayout.CENTER);
        this.add(jPanelSouth, BorderLayout.SOUTH);
        jLabelTaskTun
            .setBounds(LayoutConstants.TASK_LABEL_X, LayoutConstants.TASK_LABEL_Y, LayoutConstants.TASK_LABEL_WIDTH,
                LayoutConstants.TASK_LABEL_HEIGHT);
        Font fontTaskTun = new Font(Font.DIALOG, Font.BOLD, LayoutConstants.TASK_LABEL_FONT);
        jLabelTaskTun.setFont(fontTaskTun);
        jLabelTaskTun.setForeground(Color.WHITE);
        jPanelNorth.add(jLabelTaskTun);
        Font fontDeviceSet = new Font(Font.DIALOG, Font.BOLD, LayoutConstants.TASK_DEC_FONT);
        jLabelDeviceSet.setFont(fontDeviceSet);
        jLabelDeviceSet.setForeground(ColorConstants.BORDER_COLOR);
        jLabelDeviceSet
            .setBounds(LayoutConstants.TASK_DEC_X, LayoutConstants.TASK_DEC_Y, LayoutConstants.TASK_DEC_WIDTH,
                LayoutConstants.TASK_DEC_HEIGHT);
        jPanelNorth.add(jLabelDeviceSet);
    }

    public JCheckBox getCheckBoxSelectAll() {
        return checkBoxSelectAll;
    }

    public JCheckBox getCheckBoxMemoryJava() {
        return checkBoxMemoryJava;
    }

    public JCheckBox getCheckBoxGpuMemoryNative() {
        return checkBoxGpuMemoryNative;
    }

    public JCheckBox getCheckBoxGraphics() {
        return checkBoxGraphics;
    }

    public JCheckBox getCheckBoxStack() {
        return checkBoxStack;
    }

    public JCheckBox getCheckBoxCode() {
        return checkBoxCode;
    }

    public JCheckBox getCheckBoxOthers() {
        return checkBoxOthers;
    }

    public JLabel getJButtonAddDevice() {
        return jButtonAddDevice;
    }

    public JPanel getScrollPane() {
        return scrollPane;
    }

    public JPanel getJPanelSouth() {
        return jPanelSouth;
    }

    public JLabel getJButtonLastStep() {
        return jButtonLastStep;
    }

    public JLabel getJButtonStartTask() {
        return jButtonStartTask;
    }

    public JCheckBox[] getJCheckBoxs() {
        return jCheckBoxs;
    }

    public DeviceProcessJpanel getDeviceProcessJpanel() {
        return deviceProcessJpanel;
    }
}
