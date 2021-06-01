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
import ohos.devtools.views.common.hoscomp.HosJButton;
import ohos.devtools.views.common.hoscomp.HosJComboBox;
import ohos.devtools.views.common.hoscomp.HosJLabel;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.event.TaskScenePanelChartEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.awt.LayoutManager;

import static ohos.devtools.views.common.ProfilerMonitorItem.MEMORY;
import static ohos.devtools.views.common.ViewConstants.NUM_1000;
import static ohos.devtools.views.common.ViewConstants.NUM_4;

/**
 * chart监测页面三级容器
 *
 * @version 1.0
 * @date 2021/03/02
 **/
public class TaskScenePanelChart extends JPanel {
    private static final Logger LOGGER = LogManager.getLogger(TaskScenePanelChart.class);
    private Component add;

    /**
     * Task Scene Panel Chart
     */
    public TaskScenePanelChart() {
    }

    /**
     * 整体页面top容器
     */
    private JPanel panelTop = new JPanel(new BorderLayout());

    /**
     * 整体页面center容器
     */
    private JPanel panelMiddle = new JPanel(new BorderLayout());

    /**
     * 整体页面Bottom容器
     */
    private JPanel panelBottom = new JPanel(new BorderLayout());

    /**
     * panelTop中，west容器
     */
    private JPanel jPanelWest = new JPanel();

    /**
     * panelTop中，Center容器
     */
    private JPanel jPanelCenter = new JPanel();

    /**
     * panelTop中，East容器
     */
    private JPanel jPanelEast = new JPanel();

    /**
     * 选项卡标签命名
     */
    private JLabel jLabelSetting = new JLabel();

    /**
     * 停止按钮
     */
    private HosJButton jButtonRun = new HosJButton(
        new ImageIcon(TaskScenePanelChart.class.getClassLoader().getResource("images/button_record.png")), "停止任务");

    /**
     * 暂停按钮
     */
    private HosJButton jButtonStop =
        new HosJButton(new ImageIcon(TaskScenePanelChart.class.getClassLoader().getResource("images/button_stop.png")),
            "暂停任务");

    /**
     * 保存任务按钮
     */
    private HosJButton jButtonSave =
        new HosJButton(new ImageIcon(TaskScenePanelChart.class.getClassLoader().getResource("images/button_save.png")),
            "保存任务");

    /**
     * 删除任务按钮
     */
    private HosJButton jButtonDelete = new HosJButton(
        new ImageIcon(TaskScenePanelChart.class.getClassLoader().getResource("images/button_delete.png")), "删除任务");

    /**
     * 新增配置项按钮
     */
    private HosJButton jButtonInsert =
        new HosJButton(new ImageIcon(TaskScenePanelChart.class.getClassLoader().getResource("images/button_add.png")),
            "新增配置项");

    /**
     * 向下扩展页面按钮
     */
    private HosJButton jButtonBottom = new HosJButton(
        new ImageIcon(TaskScenePanelChart.class.getClassLoader().getResource("images/button_bottom_bar_grey.png")),
        "向下扩展");

    /**
     * 帮助按钮
     */
    private HosJButton jButtonHelp =
        new HosJButton(new ImageIcon(TaskScenePanelChart.class.getClassLoader().getResource("images/button_help.png")),
            "帮助");

    /**
     * 向左扩展页面按钮
     */
    private HosJButton jButtonLeft = new HosJButton(
        new ImageIcon(TaskScenePanelChart.class.getClassLoader().getResource("images/button_left_bar.png")), "向左扩展");

    /**
     * 切换上一页按钮
     */
    private HosJButton jButtonUp =
        new HosJButton(new ImageIcon(TaskScenePanelChart.class.getClassLoader().getResource("images/left_grey.png")),
            "上一页");

    /**
     * 切换下一页按钮
     */
    private HosJButton jButtonNext =
        new HosJButton(new ImageIcon(TaskScenePanelChart.class.getClassLoader().getResource("images/right_grey.png")),
            "下一页");

    /**
     * Run xx of xx 信息文本
     */
    private JLabel jLabelMidde = new JLabel();

    /**
     * 00:24:27 chart计时容器
     */
    private JPanel jPanelLabel = new JPanel(new GridLayout());

    /**
     * 计时器文字显示
     */
    private JLabel jTextArea = new JLabel();

    /**
     * panelMiddle容器中左边容器
     */
    private JPanel jPanelMiddleLeft = new JPanel();

    /**
     * panelMiddle容器中右边容器
     */
    private JPanel jPanelMiddleRight = new JPanel();

    /**
     * panelMiddle容器分割线
     */
    private JSplitPane splitPane = new JSplitPane();

    /**
     * jPanelMiddleLeft容器子容器
     */
    private JPanel jScrollCardsPanel = new JPanel(new BorderLayout());

    /**
     * jScrollCardsPanel容器子容器
     */
    private JPanel jScrollCardsPanelInner = new JPanel();

    /**
     * jPanelMiddleLeft滚动条
     */
    private JScrollPane jScrollPane = new JScrollPane(jScrollCardsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    /**
     * 卡片式布局的面板，用于多个chart页面
     */
    private JPanel cards = new JPanel(new CardLayout());

    /**
     * 卡片模型对象
     */
    private CardLayout cardLayout;

    /**
     * 用于jPanelMiddleLeft布局和内容显示
     */
    private int number = 0;

    private int numberJlabel = 0;

    private TaskScenePanelChartEvent taskScenePanelChartEvent = new TaskScenePanelChartEvent();

    private JLabel jLabelLeft = new JLabel();

    private CountingThread counting;

    private ProfilerChartsView profilerView;

    /**
     * 悬浮框
     */
    private JPanel jPanelSuspension = new JPanel();

    private JPanel jpanelSupen = new JPanel();

    private HosJComboBox jComboBox = new HosJComboBox();

    private HosJComboBox timeJComboBox = new HosJComboBox();

    /**
     * getCardLayout
     *
     * @param cards cards
     */
    private void getCardLayout(JPanel cards) {
        if (cards != null) {
            LayoutManager layout = cards.getLayout();
            if (layout instanceof CardLayout) {
                cardLayout = (CardLayout) layout;
            }
        }
    }

    /**
     * chart监测页面三级容器
     *
     * @param jTaskPanel    jTaskPanel
     * @param hosJLabelList hosJLabelList
     */
    public TaskScenePanelChart(TaskPanel jTaskPanel, List<HosJLabel> hosJLabelList) {
        getCardLayout(cards);
        // 整体页面布局设置
        setLayAttributes(jTaskPanel, hosJLabelList);
        // 设置按钮属性
        setButtonAttributes();
        // 布局panelTop容器，添加按钮
        setPanelTopAttributes();
        // 设置标签页标题滚动显示
        new DynamicThread().start();
        // 布局panelBigTwo中间容器
        panelMiddle.setLayout(new BorderLayout());
        // 创建页面分割事件
        createSplitPanel();
        // 布局jPanelMiddleLeft容器
        setScrollPane();
        jPanelMiddleRight.add(cards);
        // 获取有多少个设备开始了任务，并循环设置
        int numberSum = hosJLabelList.size();
        int sum = 0;
        setTaskLoop(numberSum, sum, hosJLabelList);
        // 初始化绘图标准刻度间隔
        refreshTime();
        // 默认显示第一页
        cardLayout.show(cards, "card0");
        // 监听窗体大小使悬浮窗紧贴窗体
        taskScenePanelChartEvent.setSceneSize(jTaskPanel, this);
        // 给删除按钮添加点击事件
        taskScenePanelChartEvent.clickDelete(this);
        // 给上一个页面按钮,下一个页面按钮添加点击事件
        taskScenePanelChartEvent.clickUpAndNext(this);
        // 给jButtonBottom按钮添加点击事件,向下放大页面
        taskScenePanelChartEvent.clickBottom(this, profilerView);
        // 给jsplitPane添加监听事件
        taskScenePanelChartEvent.splitPaneChange(this, numberSum);
        // 给jButtonLeft按钮添加点击事件，向左放大页面
        taskScenePanelChartEvent.clickLeft(this);
        // 给jButton按钮添加点击时间，保存trace文件
        taskScenePanelChartEvent.clickSave(jButtonSave);
        // memory配置项新增点击事件
        taskScenePanelChartEvent.clickConfig(this, profilerView);
        // trace导入，不需要这些按钮
        if (hosJLabelList.get(0).getDeviceType()) {
            jPanelWest.removeAll();
        } else {
            // 开始计时
            counting = new CountingThread(jTextArea);
            counting.start();
        }
        // 刷新页面
        jTaskPanel.getOptionJPanel().repaint();
    }

    /**
     * 创建表格
     *
     * @param panelBottom panelBottom
     * @param jTaskPanel  jTaskPanel
     */
    public void createTable(JPanel panelBottom, TaskPanel jTaskPanel) {
        JButton jButtonSuspen = new JButton("悬浮框");
        panelBottom.add(jButtonSuspen);
        taskScenePanelChartEvent.showSuspension(this, jTaskPanel, jButtonSuspen);
    }

    private void refreshTime() {
        long sessionId = jComboBox.getSessionId();
        ProfilerChartsView view = ProfilerChartsView.sessionMap.get(sessionId);
        if (view != null) {
            view.getObserver().getStandard().updateSizeTime(NUM_1000);
        }
    }

    /**
     * chart display
     *
     * @param num         num
     * @param jcardsPanel jcardsPanel
     * @param hosJLabel   hosJLabel
     */
    private void chartDisplay(int num, JPanel jcardsPanel, HosJLabel hosJLabel) {
        // sessionId绑定按钮
        if (num == 0) {
            jButtonRun.setSessionId(hosJLabel.getSessionId());
            jButtonStop.setSessionId(hosJLabel.getSessionId());
            jButtonSave.setSessionId(hosJLabel.getSessionId());
            jButtonSave.setDeviceName(hosJLabel.getDeviceName());
            jButtonSave.setProcessName(hosJLabel.getProcessName());
            jButtonDelete.setSessionId(hosJLabel.getSessionId());
            jButtonDelete.setDeviceName(hosJLabel.getDeviceName());
            jButtonDelete.setProcessName(hosJLabel.getProcessName());
            jButtonInsert.setSessionId(hosJLabel.getSessionId());
            jButtonBottom.setSessionId(hosJLabel.getSessionId());
            jButtonLeft.setSessionId(hosJLabel.getSessionId());
            jComboBox.setSessionId(hosJLabel.getSessionId());
            timeJComboBox.setSessionId(hosJLabel.getSessionId());
        }
        // 判断是导入还是实时
        if (hosJLabel.getDeviceType()) {
            // 添加chart
            profilerView = new ProfilerChartsView(hosJLabel.getSessionId(), true, this);
            jcardsPanel.add(profilerView);
            profilerView.addMonitorItemView(MEMORY);
            profilerView.getObserver().showTraceResult(hosJLabel.getStartTime(), hosJLabel.getEndTime());
            taskScenePanelChartEvent.clickZoomEvery(timeJComboBox, profilerView);
        } else {
            // 添加chart
            profilerView = new ProfilerChartsView(hosJLabel.getSessionId(), false, this);
            jcardsPanel.add(profilerView);
            profilerView.addMonitorItemView(MEMORY);
            // 显示Loading标识，等数据库初始化完成时再显示chart
            profilerView.showLoading();
            taskScenePanelChartEvent.clickZoomEvery(timeJComboBox, profilerView);
            // 给开始暂停按钮添加点击事件
            taskScenePanelChartEvent.clickRunAndStop(this, profilerView);
        }
    }

    /**
     * Set PanelMidder container layout and content cyclically
     *
     * @param numberSum     numberSum
     * @param sum           sum
     * @param hosJLabelList hosJLabelList
     */
    public void setTaskLoop(int numberSum, int sum, List<HosJLabel> hosJLabelList) {
        int sumInt = sum;
        for (int index = 0; index < numberSum; index++) {
            sumInt += LayoutConstants.HEIGHT;
            JPanel jcardsPanel = new JPanel(new BorderLayout());
            jcardsPanel.setOpaque(true);
            HosJLabel hosJLabel = hosJLabelList.get(index);
            // sessionId绑定按钮,判断是导入还是实时
            chartDisplay(index, jcardsPanel, hosJLabel);
            // 显示设备进程名称
            HosJLabel jLabelRight = new HosJLabel(
                "<html><p style=\"word-break:keep-all;white-space:nowrap;overflow:hidden;"
                    + "text-overflow:ellipsis;\">" + hosJLabel.getProcessName() + "<br>" + "(" + hosJLabel
                    .getDeviceName() + ")" + "</p><html>");
            // 绑定sessionid，进程设备信息
            jLabelRight.setSessionId(hosJLabel.getSessionId());
            jLabelRight.setDeviceName(hosJLabel.getDeviceName());
            jLabelRight.setProcessName(hosJLabel.getProcessName());
            jLabelRight.setOpaque(true);
            // 显示颜色
            JLabel jLabelLefts = new JLabel();
            // 判断显示具体颜色布局
            judge(index, jLabelLefts, jLabelRight);
            // 每个设备进程信息用jpanel包围
            JPanel jMultiplePanel = new JPanel(new BorderLayout());
            jMultiplePanel.setBounds(0, number, LayoutConstants.HEIGHT_Y, LayoutConstants.HEIGHT);
            number += LayoutConstants.HEIGHT;
            numberJlabel += LayoutConstants.INDEX_THREE;

            jMultiplePanel.add(jLabelRight, BorderLayout.CENTER);
            jScrollCardsPanelInner.add(jMultiplePanel);
            cards.add(jcardsPanel, "card" + index);
            // 存放点击选择的进程信息用于标签动态展示
            String jLabelSelect = hosJLabel.getProcessName() + "(" + hosJLabel.getDeviceName() + ")";
            // 给每个jpanel添加点击事件
            taskScenePanelChartEvent.clickEvery(this, jLabelRight, numberSum, jLabelSelect, jMultiplePanel);
        }
        if (sumInt > LayoutConstants.SCROPNUM) {
            jScrollCardsPanelInner.setPreferredSize(new Dimension(LayoutConstants.HEIGHT_Y, sumInt));
        }
    }

    /**
     * Determine the specific color layout
     *
     * @param index       index
     * @param jLabelLeft  jLabelLeft
     * @param jLabelRight jLabelRight
     */
    public void judge(int index, JLabel jLabelLeft, JLabel jLabelRight) {
        if (index == 0) {
            jLabelRight.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
            jLabelRight.setForeground(Color.WHITE);
            jLabelRight.setPreferredSize(new Dimension(LayoutConstants.WIDTH, LayoutConstants.HEIGHT));
            jLabelLeft.setOpaque(true);
            jLabelLeft.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
            jLabelLeft.setPreferredSize(new Dimension(LayoutConstants.DEVICE_ADD_X, LayoutConstants.HEIGHT));
        } else {
            jLabelRight.setBackground(ColorConstants.SCROLL_PANE);
            jLabelRight.setForeground(Color.gray);
            jLabelRight.setPreferredSize(new Dimension(LayoutConstants.WIDTH, HEIGHT));
            jLabelLeft.setOpaque(true);
            jLabelLeft.setBackground(ColorConstants.SCROLL_PANE);
            jLabelLeft.setPreferredSize(new Dimension(LayoutConstants.DEVICE_ADD_X, LayoutConstants.HEIGHT));
        }
        jLabelRight
            .setIcon(new ImageIcon(TaskScenePanelChart.class.getClassLoader().getResource("images/icon_usb.png")));
    }

    /**
     * 布局jPanelMiddleLeft容器
     */
    public void setScrollPane() {
        jPanelMiddleLeft.setLayout(new BorderLayout());
        jScrollCardsPanelInner.setOpaque(true);
        jScrollCardsPanelInner.setBackground(ColorConstants.BLACK_COLOR);
        jScrollPane.setBorder(null);
        jScrollPane.getVerticalScrollBar().setUnitIncrement(LayoutConstants.MEMORY_Y);
        jScrollCardsPanel.add(jScrollCardsPanelInner);
        jScrollCardsPanelInner.setPreferredSize(new Dimension(LayoutConstants.HEIGHT_Y, LayoutConstants.SCROPNUM));
        jPanelMiddleLeft.add(jScrollPane);
        jScrollCardsPanelInner.setLayout(null);
    }

    /**
     * 创建页面分割事件
     */
    public void createSplitPanel() {
        jPanelMiddleLeft.setMinimumSize(new Dimension(LayoutConstants.HEIGHT_Y, LayoutConstants.JAVA_WIDTH));
        jPanelMiddleRight.setMinimumSize(new Dimension(0, LayoutConstants.JAVA_WIDTH));

        jpanelSupen.setPreferredSize(new Dimension(0, LayoutConstants.HUNDRED));
        jPanelMiddleLeft.setLayout(new GridLayout());
        jPanelMiddleRight.setLayout(new GridLayout());
        jPanelMiddleLeft.setOpaque(true);
        jPanelMiddleRight.setOpaque(true);
        jPanelMiddleLeft.setBackground(ColorConstants.BLACK_COLOR);
        jPanelMiddleRight.setBackground(Color.white);
        jPanelMiddleLeft.setPreferredSize(new Dimension(LayoutConstants.HEIGHT_Y, LayoutConstants.JAVA_WIDTH));
        // 让分割线显示出箭头
        splitPane.setOneTouchExpandable(false);
        splitPane.setContinuousLayout(true);
        // 设定分割线的距离左边的位置.
        splitPane.setDividerLocation(LayoutConstants.HEIGHT_Y);
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerSize(1);
        splitPane.setLeftComponent(jPanelMiddleLeft);
        splitPane.setRightComponent(jPanelMiddleRight);
        panelMiddle.add(splitPane);
        panelMiddle.add(jpanelSupen, BorderLayout.EAST);
    }

    /**
     * 使用内部类完成标签移动操作
     */
    private class DynamicThread extends Thread {
        @Override
        public void run() {
            while (true) {
                for (int index = 0; index < LayoutConstants.NUMBER_FOR; index++) {
                    try {
                        Thread.sleep(LayoutConstants.NUMBER_SLEEEP);
                    } catch (InterruptedException exception) {
                        LOGGER.error(exception.getMessage());
                    }
                    jLabelSetting.setLocation(-index, 0);
                }
            }
        }
    }

    /**
     * 布局panelTop容器，添加按钮
     */
    public void setPanelTopAttributes() {
        timeJComboBox.setBorder(BorderFactory.createLineBorder(Color.black));
        timeJComboBox.setPreferredSize(new Dimension(LayoutConstants.SE_PANEL_Y_TWO, LayoutConstants.APP_LABEL_X));
        timeJComboBox.addItem("200ms");
        timeJComboBox.addItem("400ms");
        timeJComboBox.addItem("600ms");
        timeJComboBox.addItem("800ms");
        timeJComboBox.addItem("1000ms");
        timeJComboBox.setSelectedIndex(NUM_4);
        jPanelWest.setLayout(
            new FlowLayout(FlowLayout.LEADING, LayoutConstants.JP_LABEL_HEIGHT, LayoutConstants.JP_LABEL_HEIGHT));
        jPanelEast.setLayout(
            new FlowLayout(FlowLayout.LEADING, LayoutConstants.JP_LABEL_HEIGHT, LayoutConstants.JP_LABEL_HEIGHT));
        jButtonRun.setPreferredSize(new Dimension(LayoutConstants.BUTTON_WIDTHS, LayoutConstants.BUTTON_SIZE));
        jButtonStop.setPreferredSize(new Dimension(LayoutConstants.BUTTON_WIDTHS, LayoutConstants.BUTTON_SIZE));
        jPanelWest.add(jButtonRun);
        jPanelWest.add(jButtonStop);
        jPanelWest.add(jButtonUp);
        jPanelWest.add(jLabelMidde);
        jPanelWest.add(jPanelLabel);
        jPanelWest.add(jButtonNext);
        jPanelWest.add(jButtonSave);
        jPanelWest.add(jButtonDelete);
        jPanelEast.add(timeJComboBox);
        jPanelEast.add(jButtonInsert);
        jPanelEast.add(jButtonBottom);
        jPanelEast.add(jButtonLeft);
        jPanelEast.add(jButtonHelp);
    }

    /**
     * 设置按钮属性
     */
    public void setButtonAttributes() {
        jButtonUp.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
        jButtonUp.setFocusPainted(false);
        jButtonUp.setBorderPainted(false);
        jButtonUp.setToolTipText("上一页");
        jButtonNext.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
        jButtonNext.setFocusPainted(false);
        jButtonNext.setBorderPainted(false);
        jButtonNext.setToolTipText("下一页");
        jButtonRun.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
        jButtonRun.setFocusPainted(false);
        jButtonRun.setBorderPainted(false);
        jButtonRun.setToolTipText("停止");
        jButtonStop.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
        jButtonStop.setFocusPainted(false);
        jButtonStop.setBorderPainted(false);
        jButtonStop.setToolTipText("暂停");
        jButtonSave.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
        jButtonSave.setFocusPainted(false);
        jButtonSave.setBorderPainted(false);
        jButtonSave.setToolTipText("保存当前任务");
        jButtonDelete.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
        jButtonDelete.setFocusPainted(false);
        jButtonDelete.setBorderPainted(false);
        jButtonDelete.setToolTipText("删除当前任务");
        jButtonInsert.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
        jButtonInsert.setFocusPainted(false);
        jButtonInsert.setBorderPainted(false);
        jButtonInsert.setToolTipText("新增配置");
        jButtonBottom.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
        jButtonBottom.setFocusPainted(false);
        jButtonBottom.setBorderPainted(false);
        jButtonBottom.setToolTipText("向下展开页面");
        jButtonHelp.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
        jButtonHelp.setFocusPainted(false);
        jButtonHelp.setBorderPainted(false);
        jButtonHelp.setToolTipText("帮助");
        jButtonLeft.setBackground(ColorConstants.DEVICE_PROCESS_PANEL);
        jButtonLeft.setFocusPainted(false);
        jButtonLeft.setBorderPainted(false);
        jButtonLeft.setToolTipText("向左展开页面");
    }

    /**
     * Overall page layout settings
     *
     * @param jTaskPanel    jTaskPanel
     * @param hosJLabelList hosJLabelList
     */
    public void setLayAttributes(TaskPanel jTaskPanel, List<HosJLabel> hosJLabelList) {
        this.setLayout(new BorderLayout());
        panelTop.setBackground(ColorConstants.BLACK_COLOR);
        panelTop.setPreferredSize(new Dimension(LayoutConstants.DEVICES_WIDTH, LayoutConstants.APP_LABEL_HIGHT));
        // 页面中间部分
        panelMiddle.setBackground(Color.white);
        panelMiddle.setPreferredSize(new Dimension(LayoutConstants.DEVICES_WIDTH, LayoutConstants.JAVA_WIDTH));
        // 页面下面部分
        panelBottom.setBackground(ColorConstants.BLACK_COLOR);
        panelBottom.setPreferredSize(new Dimension(LayoutConstants.DEVICES_WIDTH, LayoutConstants.LABEL_NAME_WIDTH));
        this.add(panelTop, BorderLayout.NORTH);
        this.add(panelMiddle, BorderLayout.CENTER);
        jPanelWest.setOpaque(false);
        jPanelCenter.setOpaque(false);
        jPanelEast.setOpaque(false);
        jPanelWest.setPreferredSize(new Dimension(LayoutConstants.EAST_LABEL_WIDTH, LayoutConstants.LABEL_NAME_HEIGHT));
        jPanelCenter.setPreferredSize(new Dimension(LayoutConstants.DEVICES_WIDTH, LayoutConstants.LABEL_NAME_HEIGHT));
        jPanelEast.setPreferredSize(new Dimension(LayoutConstants.TASK_WIDTH, LayoutConstants.LABEL_NAME_HEIGHT));
        panelTop.add(jPanelWest, BorderLayout.WEST);
        panelTop.add(jPanelCenter, BorderLayout.CENTER);
        panelTop.add(jPanelEast, BorderLayout.EAST);
        HosJLabel hosJLabel = hosJLabelList.get(0);
        jLabelSetting = new JLabel(hosJLabel.getProcessName() + "(" + hosJLabel.getDeviceName() + ")");

        jLabelSetting.setBounds(0, 0, LayoutConstants.EAST_LABEL_WIDTH, LayoutConstants.LABEL_NAME_HEIGHT);
        jTaskPanel.getJPanelLeft().removeAll();
        jTaskPanel.getJPanelRight().removeAll();
        jTaskPanel.getJPanelLeft().add(jLabelSetting);
        jTaskPanel.getJPanelRight().add(jTaskPanel.getJLabelClose());
        jTextArea.setOpaque(true);
        jTextArea.setBackground(ColorConstants.BLACK_COLOR);
        jPanelLabel.add(jTextArea);
    }

    public HosJButton getJButtonDelete() {
        return jButtonDelete;
    }

    public HosJButton getjButtonRun() {
        return jButtonRun;
    }

    public HosJButton getjButtonStop() {
        return jButtonStop;
    }

    public HosJButton getjButtonSave() {
        return jButtonSave;
    }

    public HosJButton getjButtonInsert() {
        return jButtonInsert;
    }

    public HosJButton getjButtonBottom() {
        return jButtonBottom;
    }

    public JButton getjButtonHelp() {
        return jButtonHelp;
    }

    public HosJButton getjButtonLeft() {
        return jButtonLeft;
    }

    public JButton getjButtonUp() {
        return jButtonUp;
    }

    public JButton getjButtonNext() {
        return jButtonNext;
    }

    public JPanel getPanelBottom() {
        return panelBottom;
    }

    public JSplitPane getSplitPane() {
        return splitPane;
    }

    public JPanel getJScrollCardsPanelInner() {
        return jScrollCardsPanelInner;
    }

    public JLabel getjLabelLeft() {
        return jLabelLeft;
    }

    public JPanel getCards() {
        return cards;
    }

    public CardLayout getCardLayout() {
        return cardLayout;
    }

    public JPanel getjPanelSuspension() {
        return jPanelSuspension;
    }

    public HosJComboBox getjComboBox() {
        return jComboBox;
    }

    public HosJComboBox getTimeJComboBox() {
        return timeJComboBox;
    }

    public CountingThread getCounting() {
        return counting;
    }

    public void setCounting(CountingThread counting) {
        this.counting = counting;
    }

    public JLabel getjTextArea() {
        return jTextArea;
    }

    public JPanel getJpanelSupen() {
        return jpanelSupen;
    }

}
