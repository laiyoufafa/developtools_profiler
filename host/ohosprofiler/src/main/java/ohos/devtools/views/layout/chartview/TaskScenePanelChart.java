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

package ohos.devtools.views.layout.chartview;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.datasources.utils.plugin.service.PlugManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.views.applicationtrace.AppTracePanel;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.common.customcomp.CustomComboBox;
import ohos.devtools.views.common.customcomp.CustomJButton;
import ohos.devtools.views.common.customcomp.CustomJLabel;
import ohos.devtools.views.layout.TaskPanel;
import ohos.devtools.views.layout.event.TaskScenePanelChartEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static ohos.devtools.datasources.utils.common.Constant.DEVICE_STAT_OFFLINE;

/**
 * TaskScenePanelChart
 */
public class TaskScenePanelChart extends JBPanel {
    private static final Logger LOGGER = LogManager.getLogger(TaskScenePanelChart.class);
    private static final int SELECTED_INDEX = 4;
    private static final int DUMP_LABLE_WIDTH = 160;
    private static final int SESSION_LIST_HEIGHT = 60;
    private static final int SESSION_LIST_WIDTH = 220;
    private static final int LABLE_FIRST_ITEM = 0;
    private static final int LABLE_TWO_ITEM = 1;
    private static final int LABLE_THREE_ITEM = 2;
    private static final int SESSION_LABLE_LENGTH = 3;
    private static final int SESSION_LEFT_LABLE_WIDTH = 8;

    /**
     * Task Scene Panel Chart
     */
    public TaskScenePanelChart() {
    }

    /**
     * 整体页面top容器
     */
    private JBPanel panelTop;

    /**
     * 整体页面center容器
     */
    private JBPanel panelMiddle;

    /**
     * 整体页面Bottom容器
     */
    private JBPanel panelBottom;

    /**
     * panelTop中，west容器
     */
    private JBPanel jPanelWest;

    /**
     * panelTop中，Center容器
     */
    private JBPanel jPanelCenter;

    /**
     * panelTop中，East容器
     */
    private JBPanel jPanelEast;

    /**
     * 选项卡标签命名
     */
    private JBLabel jLabelSetting;

    /**
     * 停止按钮
     */
    private CustomJButton jButtonStop;

    /**
     * 暂停按钮
     */
    private CustomJButton jButtonSuspend;

    /**
     * 保存任务按钮
     */
    private CustomJButton jButtonSave;

    /**
     * 删除任务按钮
     */
    private CustomJButton jButtonDelete;

    /**
     * 新增配置项按钮
     */
    private CustomJButton configButton;

    /**
     * 向下扩展页面按钮
     */
    private CustomJButton jButtonBottom;

    /**
     * 向左扩展页面按钮
     */
    private CustomJButton jButtonLeft;

    /**
     * 切换上一页按钮
     */
    private CustomJButton jButtonUp;

    /**
     * 切换下一页按钮
     */
    private CustomJButton jButtonNext;

    /**
     * Run xx of xx 信息文本
     */
    private JBLabel jLabelMidde;

    /**
     * 00:24:27 chart计时容器
     */
    private JBPanel jPanelLabel;

    /**
     * 计时器文字显示
     */
    private JBLabel jTextArea;

    /**
     * panelMiddle容器中左边容器
     */
    private JBPanel jPanelMiddleLeft;

    /**
     * panelMiddle容器中右边容器
     */
    private JBPanel jPanelMiddleRight;

    /**
     * panelMiddle容器分割线
     */
    private JSplitPane splitPane;

    /**
     * jPanelMiddleLeft容器子容器
     */
    private JBPanel jScrollCardsPanel;

    /**
     * jScrollCardsPanel容器子容器
     */
    private JBPanel jScrollCardsPanelInner;

    /**
     * jPanelMiddleLeft滚动条
     */
    private JBScrollPane jScrollPane;

    /**
     * 卡片式布局的面板，用于多个chart页面
     */
    private JBPanel cards;

    /**
     * 卡片模型对象
     */
    private CardLayout cardLayout;

    /**
     * 用于jPanelMiddleLeft布局和内容显示
     */
    private int number;
    private int numberJlabel;
    private TaskScenePanelChartEvent taskScenePanelChartEvent;
    private CountingThread counting;
    private ProfilerChartsView profilerView;
    private JBPanel jPanelSuspension;
    private JBPanel jpanelSupen;
    private CustomComboBox jComboBox;
    private CustomComboBox timeJComboBox;
    private int sumInt;
    private boolean greyFlag;
    private List<CustomJLabel> sessionList;
    private List<SubSessionListJBPanel> dumpOrHookSessionList;

    /**
     * getCardLayout
     *
     * @param cards cards
     */
    private void getCardLayout(JBPanel cards) {
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
     * @param jTaskPanel jTaskPanel
     * @param hosJLabelList hosJLabelList
     */
    public TaskScenePanelChart(TaskPanel jTaskPanel, List<CustomJLabel> hosJLabelList) {
        init();
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
        setTaskLoop(numberSum, hosJLabelList);
        // 默认显示第一页
        cardLayout.show(cards, "card0");
        // 监听窗体大小使悬浮窗紧贴窗体
        taskScenePanelChartEvent.setSceneSize(jTaskPanel, this);
        // 给删除按钮添加点击事件
        taskScenePanelChartEvent.clickDelete(this);
        // 给上一个页面按钮,下一个页面按钮添加点击事件
        taskScenePanelChartEvent.clickUpAndNext(this);
        // 给jsplitPane添加监听事件
        taskScenePanelChartEvent.splitPaneChange(this, numberSum);
        // 给jButtonLeft按钮添加点击事件，向左放大页面
        taskScenePanelChartEvent.clickLeft(this, hosJLabelList);
        jButtonLeft.setName(UtConstant.UT_TASK_SCENE_PANEL_CHART_LEFT);
        // 给jButton按钮添加点击时间，保存trace文件
        taskScenePanelChartEvent.clickSave(jButtonSave, this);
        // memory配置项新增点击事件
        taskScenePanelChartEvent.clickConfig(this, profilerView);
        // Performance analysis index configuration
        PerformanceIndexPopupMenu itemMenu =
            new PerformanceIndexPopupMenu(profilerView, this.getJButtonDelete().getSessionId());
        taskScenePanelChartEvent.clickIndexConfig(configButton, itemMenu);
        // trace导入，不需要这些按钮
        if (!hosJLabelList.get(0).isOnline()) {
            jPanelWest.removeAll();
        } else {
            // 开始计时
            counting = new CountingThread(jTextArea);
            counting.start();
        }
        // 刷新页面
        jTaskPanel.getTabContainer().repaint();
    }

    private void init() {
        panelTop = new JBPanel(new BorderLayout());
        panelMiddle = new JBPanel(new BorderLayout());
        panelBottom = new JBPanel(new BorderLayout());
        jPanelWest = new JBPanel();
        jPanelCenter = new JBPanel();
        jPanelEast = new JBPanel();
        jLabelSetting = new JBLabel();
        jButtonStop = new CustomJButton(AllIcons.Debugger.Db_set_breakpoint, "Stop");
        jButtonSuspend = new CustomJButton(AllIcons.Process.ProgressPauseHover, "Suspend");
        jButtonSave = new CustomJButton(AllIcons.Actions.Menu_saveall, "Save current task");
        jButtonDelete = new CustomJButton(IconLoader.getIcon("/images/gc.png", getClass()), "Delete current task");
        configButton = new CustomJButton(AllIcons.General.Add, "");
        jButtonBottom = new CustomJButton(IconLoader.getIcon("/images/previewDetailsVertically_grey.png", getClass()),
            "Expand page down");
        jButtonLeft = new CustomJButton(AllIcons.Actions.PreviewDetails, "Expand page left");
        jButtonUp = new CustomJButton(AllIcons.General.ArrowLeft, "Previous page");
        jButtonNext = new CustomJButton(AllIcons.General.ArrowRight, "Next page");
        jLabelMidde = new JBLabel();
        jPanelLabel = new JBPanel(new GridLayout());
        jTextArea = new JBLabel();
        jPanelMiddleLeft = new JBPanel();
        jPanelMiddleRight = new JBPanel();
        splitPane = new JSplitPane();
        jScrollCardsPanel = new JBPanel(new BorderLayout());
        jScrollCardsPanelInner = new JBPanel();
        jScrollPane = new JBScrollPane(jScrollCardsPanel, JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        cards = new JBPanel(new CardLayout());
        taskScenePanelChartEvent = new TaskScenePanelChartEvent();
        jPanelSuspension = new JBPanel();
        jpanelSupen = new JBPanel();
        jComboBox = new CustomComboBox();
        timeJComboBox = new CustomComboBox();
        sessionList = new ArrayList<>();
        dumpOrHookSessionList = new ArrayList<>();
    }

    /**
     * 创建表格
     *
     * @param panelBottom panelBottom
     * @param jTaskPanel jTaskPanel
     */
    public void createTable(JBPanel panelBottom, TaskPanel jTaskPanel) {
        JButton jButtonSuspen = new JButton("Suspension frame");
        panelBottom.add(jButtonSuspen);
        taskScenePanelChartEvent.showSuspension(this, jTaskPanel, jButtonSuspen);
    }

    /**
     * chart display
     *
     * @param num num
     * @param jcardsPanel jcardsPanel
     * @param hosJLabel hosJLabel
     */
    private void chartDisplay(int num, JBPanel jcardsPanel, CustomJLabel hosJLabel) {
        // sessionId绑定按钮
        if (num == 0) {
            jButtonStop.setSessionId(hosJLabel.getSessionId());
            jButtonSuspend.setSessionId(hosJLabel.getSessionId());
            jButtonSave.setSessionId(hosJLabel.getSessionId());
            jButtonSave.setDeviceName(hosJLabel.getDeviceName());
            jButtonSave.setProcessName(hosJLabel.getProcessName());
            jButtonDelete.setSessionId(hosJLabel.getSessionId());
            jButtonDelete.setDeviceName(hosJLabel.getDeviceName());
            jButtonDelete.setProcessName(hosJLabel.getProcessName());
            configButton.setSessionId(hosJLabel.getSessionId());
            jButtonBottom.setSessionId(hosJLabel.getSessionId());
            jButtonLeft.setSessionId(hosJLabel.getSessionId());
            jComboBox.setSessionId(hosJLabel.getSessionId());
            timeJComboBox.setSessionId(hosJLabel.getSessionId());
        }
        // 判断是导入还是实时
        if (!hosJLabel.isOnline()) {
            // 添加chart
            profilerView = new ProfilerChartsView(hosJLabel.getSessionId(), true, this);
            jcardsPanel.add(profilerView);
            addMonitorItem(hosJLabel.getSessionId());
            profilerView.getPublisher().showTraceResult(hosJLabel.getStartTime(), hosJLabel.getEndTime());
            taskScenePanelChartEvent.clickZoomEvery(timeJComboBox, profilerView);
        } else {
            // 添加chart
            profilerView = new ProfilerChartsView(hosJLabel.getSessionId(), false, this);
            jcardsPanel.add(profilerView);
            addMonitorItem(hosJLabel.getSessionId());
            // 显示Loading标识，等数据库初始化完成时再显示chart
            profilerView.showLoading();
            taskScenePanelChartEvent.clickZoomEvery(timeJComboBox, profilerView);
            // 给开始暂停按钮添加点击事件
            taskScenePanelChartEvent.clickRunAndStop(this, profilerView);
        }
    }

    private void addMonitorItem(long sessionId) {
        List<ProfilerMonitorItem> profilerMonitorItems =
            PlugManager.getInstance().getProfilerMonitorItemList(sessionId);
        profilerMonitorItems.forEach(item -> {
            try {
                profilerView.addMonitorItemView(item);
            } catch (InvocationTargetException
                | NoSuchMethodException
                | InstantiationException
                | IllegalAccessException exception) {
                LOGGER.error("addMonitorItemView failed {} ", item.getName());
            }
        });
    }

    private void setTaskLoop(int numberSum, List<CustomJLabel> hosJLabelList) {
        for (int index = 0; index < numberSum; index++) {
            sumInt += LayoutConstants.SIXTY;
            JBPanel jcardsPanel = new JBPanel(new BorderLayout());
            jcardsPanel.setOpaque(true);
            CustomJLabel hosJLabel = hosJLabelList.get(index);
            // sessionId绑定按钮,判断是导入还是实时
            chartDisplay(index, jcardsPanel, hosJLabel);
            String labelText = getLabelText(hosJLabel);
            // 显示设备进程名称
            CustomJLabel jLabelRight = new CustomJLabel(labelText);
            jLabelRight.setName(UtConstant.UT_TASK_SCENE_PANEL_CHART_SESSION_MANAGE);
            // 绑定sessionid，进程设备信息
            jLabelRight.setSessionId(hosJLabel.getSessionId());
            jLabelRight.setDeviceName(hosJLabel.getDeviceName());
            jLabelRight.setProcessName(hosJLabel.getProcessName());
            jLabelRight.setOpaque(true);
            // 判断显示具体颜色布局
            judge(index, jLabelRight, hosJLabel);
            // 每个设备进程信息用jpanel包围
            JBPanel jMultiplePanel = new JBPanel(new FlowLayout(0, 0, 0));
            jMultiplePanel.setBounds(0, number, LayoutConstants.SESSION_LIST_DIVIDER_WIDTH, LayoutConstants.SIXTY);
            number += LayoutConstants.SIXTY;
            numberJlabel += LayoutConstants.INDEX_THREE;
            sessionList.add(jLabelRight);
            // margin left lable
            if (hosJLabel.isOnline()) {
                CustomJLabel left = new CustomJLabel("");
                left.setOpaque(true);
                left.setPreferredSize(new Dimension(SESSION_LEFT_LABLE_WIDTH, LayoutConstants.SIXTY));
                left.setBackground(ColorConstants.SELECTED_COLOR);
                sessionList.add(left);
                jLabelRight.setLeft(left);
                jMultiplePanel.add(left);
            }
            jMultiplePanel.add(jLabelRight);
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
     * getLabelText
     *
     * @param hosJLabel hosJLabel
     * @return String
     */
    private String getLabelText(CustomJLabel hosJLabel) {
        String labelText = "";
        if (DEVICE_STAT_OFFLINE.equals(hosJLabel.getConnectType())) {
            String[] strs = hosJLabel.getProcessName().split(";");
            if (strs.length == SESSION_LABLE_LENGTH) {
                labelText = "<html><p style=\"white-space:nowrap;overflow:hidden;margin-top: 1px;"
                    + "text-overflow:ellipsis;margin-left: 0.5cm;line-height:10px;font-size:10px\">"
                    + strs[LABLE_FIRST_ITEM] + "<br>" + strs[LABLE_TWO_ITEM] + "<br> <span style=\"color:#A4A4A4;\">"
                    + strs[LABLE_THREE_ITEM] + "</span></p><html>";
            }
        } else {
            labelText = "<html><p style=\"word-break:keep-all;white-space:nowrap;overflow:hidden;"
                + "text-overflow:ellipsis;\">" + "&nbsp;&nbsp;" + hosJLabel.getProcessName() + "<br>" + "(" + hosJLabel
                .getDeviceName() + ")" + "</p><html>";
        }
        return labelText;
    }

    /**
     * Determine the specific color layout
     *
     * @param index index
     * @param jLabelRight jLabelRight
     * @param hosJLabel hosJLabel
     */
    public void judge(int index, JBLabel jLabelRight, CustomJLabel hosJLabel) {
        if (index == 0) {
            jLabelRight.setBackground(ColorConstants.SELECTED_COLOR);
            jLabelRight.setForeground(JBColor.WHITE);
            jLabelRight.setPreferredSize(new Dimension(LayoutConstants.SESSION_LIST_WIDTH, LayoutConstants.SIXTY));
            if (!hosJLabel.isOnline()) {
                jLabelRight.setPreferredSize(new Dimension(LayoutConstants.NUM_200, LayoutConstants.SIXTY));
                splitPane.setDividerLocation(LayoutConstants.NUM_200);
            }
        } else {
            jLabelRight.setForeground(JBColor.gray);
            jLabelRight.setPreferredSize(new Dimension(LayoutConstants.SESSION_LIST_WIDTH, LayoutConstants.SIXTY));
        }
        Icon imageIcon = null;
        if (LayoutConstants.USB.equals(hosJLabel.getConnectType())) {
            imageIcon = IconLoader.getIcon("/images/icon_usb.png", getClass());
        } else if (DEVICE_STAT_OFFLINE.equals(hosJLabel.getConnectType())) {
            jLabelRight.setIcon(null);
        } else {
            imageIcon = IconLoader.getIcon("/images/icon_wifi.png", getClass());
        }
        jLabelRight.setIcon(imageIcon);
    }

    /**
     * 布局jPanelMiddleLeft容器
     */
    public void setScrollPane() {
        jPanelMiddleLeft.setLayout(new BorderLayout());
        jScrollCardsPanelInner.setOpaque(true);
        jScrollPane.setBorder(null);
        jScrollPane.getVerticalScrollBar().setUnitIncrement(LayoutConstants.MEMORY_Y);
        jScrollPane.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
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
        splitPane.setDividerLocation(LayoutConstants.SESSION_LIST_DIVIDER_WIDTH);
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerSize(1);
        splitPane.setLeftComponent(jPanelMiddleLeft);
        splitPane.setRightComponent(jPanelMiddleRight);
        splitPane.setEnabled(false);
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
                for (int index = 0; index < 200; index++) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(200);
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
        jButtonSuspend.setName(UtConstant.UT_TASK_SCENE_PANEL_CHART_STOP_BUTTON);
        jButtonStop.setName(UtConstant.UT_TASK_SCENE_PANEL_CHART_RUN_BUTTON);
        jButtonSave.setName(UtConstant.UT_TASK_SCENE_PANEL_CHART_SAVE_BUTTON);
        jButtonDelete.setName(UtConstant.UT_TASK_SCENE_PANEL_CHART_DELETE_BUTTON);
        timeJComboBox.setBorder(BorderFactory.createLineBorder(JBColor.background().brighter()));
        timeJComboBox.setPreferredSize(new Dimension(LayoutConstants.SE_PANEL_Y_TWO, LayoutConstants.APP_LABEL_X));
        timeJComboBox.setName(UtConstant.UT_TASK_SCENE_PANEL_CHART_TIME);
        timeJComboBox.addItem("200ms");
        timeJComboBox.addItem("400ms");
        timeJComboBox.addItem("600ms");
        timeJComboBox.addItem("800ms");
        timeJComboBox.addItem("1000ms");
        configButton.setName(UtConstant.UT_TASK_SCENE_PANEL_CHART_CONFIG);
        timeJComboBox.setSelectedIndex(SELECTED_INDEX);
        jPanelWest.setLayout(new FlowLayout(FlowLayout.LEADING, 10, 0));
        jPanelEast.setLayout(new FlowLayout(FlowLayout.LEADING, 10, 0));
        jPanelWest.add(jButtonStop);
        jPanelWest.add(jButtonSuspend);
        jPanelWest.add(jButtonUp);
        jPanelWest.add(jLabelMidde);
        jPanelWest.add(jPanelLabel);
        jPanelWest.add(jButtonNext);
        jPanelWest.add(jButtonSave);
        jPanelWest.add(jButtonDelete);
        jPanelEast.add(timeJComboBox);
        jPanelEast.add(configButton);
        jPanelEast.add(jButtonBottom);
        jPanelEast.add(jButtonLeft);
    }

    /**
     * 设置按钮属性
     */
    public void setButtonAttributes() {
        this.setButtonStyle(jButtonUp, "Previous page");
        this.setButtonStyle(jButtonNext, "Next page");
        this.setButtonStyle(jButtonStop, "Stop");
        this.setButtonStyle(jButtonSuspend, "Suspend");
        this.setButtonStyle(jButtonSave, "Save current task");
        this.setButtonStyle(jButtonDelete, "Delete current task");
        this.setButtonStyle(configButton, "Data Source");
        this.setButtonStyle(jButtonBottom, "Expand page down");
        this.setButtonStyle(jButtonLeft, "Expand page left");
    }

    /**
     * set HosJButton Style
     *
     * @param button button
     * @param tipText tipText
     */
    private void setButtonStyle(CustomJButton button, String tipText) {
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setToolTipText(tipText);
    }

    /**
     * Overall page layout settings
     *
     * @param jTaskPanel jTaskPanel
     * @param hosJLabelList hosJLabelList
     */
    public void setLayAttributes(TaskPanel jTaskPanel, List<CustomJLabel> hosJLabelList) {
        this.setLayout(new BorderLayout());
        panelTop.setBackground(JBColor.background());
        panelTop.setPreferredSize(new Dimension(LayoutConstants.DEVICES_WIDTH, LayoutConstants.TOP_PANEL_HEIGHT));
        // 页面中间部分
        panelMiddle.setBackground(JBColor.WHITE);
        panelMiddle.setPreferredSize(new Dimension(LayoutConstants.DEVICES_WIDTH, LayoutConstants.JAVA_WIDTH));
        // 页面下面部分
        panelBottom.setBackground(ColorConstants.BLACK_COLOR);
        panelBottom.setPreferredSize(new Dimension(LayoutConstants.DEVICES_WIDTH, LayoutConstants.LABEL_NAME_WIDTH));
        this.add(panelTop, BorderLayout.NORTH);
        setPanelTop(panelTop);
        this.add(panelMiddle, BorderLayout.CENTER);
        jPanelWest.setOpaque(false);
        jPanelCenter.setOpaque(false);
        jPanelEast.setOpaque(false);
        jPanelWest.setPreferredSize(new Dimension(LayoutConstants.EAST_LABEL_WIDTH, LayoutConstants.LABEL_NAME_HEIGHT));
        jPanelCenter.setPreferredSize(new Dimension(LayoutConstants.DEVICES_WIDTH, LayoutConstants.LABEL_NAME_HEIGHT));
        jPanelEast
            .setPreferredSize(new Dimension(LayoutConstants.TOP_PANEL_EAST_WIDTH, LayoutConstants.LABEL_NAME_HEIGHT));
        panelTop.add(jPanelWest, BorderLayout.WEST);
        panelTop.add(jPanelCenter, BorderLayout.CENTER);
        panelTop.add(jPanelEast, BorderLayout.EAST);
        CustomJLabel hosJLabel = hosJLabelList.get(0);
        jLabelSetting = new JBLabel(hosJLabel.getProcessName() + "(" + hosJLabel.getDeviceName() + ")");
        jLabelSetting.setBounds(0, 0, LayoutConstants.EAST_LABEL_WIDTH, LayoutConstants.LABEL_NAME_HEIGHT);
        jTaskPanel.getTabLeftPanel().removeAll();
        jTaskPanel.getTabRightPanel().removeAll();
        jTaskPanel.getTabLeftPanel().add(jLabelSetting);
        jTaskPanel.getTabRightPanel().add(jTaskPanel.getTabCloseBtn());
        jTextArea.setOpaque(true);
        jTextArea.setBackground(JBColor.background());
        jPanelLabel.add(jTextArea);
    }

    /**
     * createNativeHook
     *
     * @param name Native Hook Recoding name
     * @param startTime Native Hook Recoding Time
     * @param sessionId Native Hook Session Id
     * @param dbPath dbPath
     */
    public void createSessionList(String name, String startTime, long sessionId, String dbPath) {
        JBPanel jScrollCardsPanelSession = this.getJScrollCardsPanelInner();
        Component[] innerPanel = jScrollCardsPanelSession.getComponents();
        SubSessionListJBPanel sessionListPanel = null;
        CustomJLabel labelSave = null;
        for (Component inner : innerPanel) {
            Component[] innerLable;
            if (inner instanceof JBPanel) {
                innerLable = ((JBPanel) inner).getComponents();
                for (Component item : innerLable) {
                    if (item instanceof CustomJLabel && ((CustomJLabel) item).getSessionId() == sessionId) {
                        // 添加Dump
                        CustomJLabel nameLable = new CustomJLabel(name);
                        nameLable.setPreferredSize(new Dimension(DUMP_LABLE_WIDTH, LayoutConstants.THIRTY));
                        String btnStr = "Save Heap Dump";
                        if (name.contains("Native Hook")) {
                            btnStr = "Save Native Hook Recording";
                        }
                        sessionListPanel = new SubSessionListJBPanel();
                        MigLayout layout = new MigLayout();
                        sessionListPanel.setLayout(layout);
                        if (name.contains(LayoutConstants.TRACE_SYSTEM_CALLS)) {
                            nameLable.setIcon(IconLoader.getIcon("/images/cpu.png", getClass()));
                            sessionListPanel.add(nameLable, "gapleft 15,wrap 5");
                        } else {
                            nameLable.setIcon(IconLoader.getIcon("/images/icon_heap_dump_normal.png", getClass()));
                            labelSave = new CustomJLabel();
                            labelSave.setIcon(IconLoader.getIcon("/images/menu-saveall.png", getClass()));
                            labelSave.setToolTipText(btnStr);
                            sessionListPanel.add(nameLable, "gapleft 15");
                            sessionListPanel.add(labelSave, "wrap 5,width 15:15:15,height 15:15:15");
                            taskScenePanelChartEvent.saveButtonAddClick(labelSave, name);
                        }
                        CustomJLabel timeLabel = new CustomJLabel(" " + startTime);
                        timeLabel.setBounds(LayoutConstants.TIMELABLE_XY, LayoutConstants.TIMELABLE_XY,
                            LayoutConstants.HUNDRED_EIGHTY, LayoutConstants.THIRTY);
                        Font font = new Font(Font.DIALOG, Font.PLAIN, LayoutConstants.OPTION_FONT);
                        timeLabel.setFont(font);
                        sessionListPanel.add(timeLabel, "gapleft 28");
                        sessionListPanel.setHosJLabel(nameLable);
                        sessionListPanel.setStartTime(startTime);
                        dumpOrHookSessionList.add(sessionListPanel);
                        sessionListPanel.setBounds(0, number, SESSION_LIST_WIDTH, SESSION_LIST_HEIGHT);
                        jScrollCardsPanelSession.add(sessionListPanel);
                        if (number > LayoutConstants.LEFT_TOP_WIDTH) {
                            jScrollCardsPanelSession
                                .setPreferredSize(new Dimension(SESSION_LIST_WIDTH, number + SESSION_LIST_HEIGHT));
                        }
                        taskScenePanelChartEvent.sessionListPanelAddClick(sessionListPanel, this);
                        number += SESSION_LIST_HEIGHT;
                    }
                }
            }
        }
        if (sessionListPanel != null) {
            sessionListPanel.setBackground(ColorConstants.SELECTED_COLOR);
        }
        String cardName = "";
        if (name.contains(LayoutConstants.TRACE_SYSTEM_CALLS)) {
            cardName = "traceApp" + startTime;
            Objects.requireNonNull(sessionListPanel).setPanelName(cardName);
            sessionListPanel.setDbPath(dbPath);
            showAppTrace(dbPath, sessionId);
        }
        // set Button disabled
        greyFlag = true;
        setButtonEnable(greyFlag, cardName);
    }

    /**
     * showAppTrace
     *
     * @param dbPathParam dbPathParam
     * @param sessionId sessionId
     */
    public void showAppTrace(String dbPathParam, long sessionId) {
        this.remove(panelTop);
        AppTracePanel component = new AppTracePanel();
        component.load(dbPathParam, SessionManager.getInstance().tempPath() + "cpuDb",
            (int) SessionManager.getInstance().getPid(sessionId), true);
        cards.add(component, dbPathParam);
        cardLayout.show(cards, dbPathParam);
    }

    /**
     * handleFailed
     *
     * @param sessionListPanel sessionListPanel
     */
    public void handleFailed(SubSessionListJBPanel sessionListPanel) {
        if (sessionListPanel != null && sessionList.size() >= 1) {
            this.getJScrollCardsPanelInner().remove(sessionListPanel);
            cardLayout.show(cards, "card0");
            sessionList.get(0).setBackground(ColorConstants.SELECTED_COLOR);
            number -= SESSION_LIST_HEIGHT;
        }
    }

    /**
     * showSubSessionList
     *
     * @param list list
     */
    public void showSubSessionList(List<SubSessionListJBPanel> list) {
        SubSessionListJBPanel tempSub;
        for (int index = 0; index < list.size(); index++) {
            tempSub = list.get(index);
            if (index == 0) {
                number = 0;
                tempSub.setBounds(0, 0, SESSION_LIST_WIDTH, SESSION_LIST_HEIGHT);
                if (tempSub.getPanelName().contains("heapDump") || (tempSub.getPanelName().contains("nativeHook"))) {
                    cardLayout.show(cards, tempSub.getPanelName());
                    setButtonEnable(true, "");
                } else {
                    cardLayout.show(cards, tempSub.getDbPath());
                    this.remove(panelTop);
                }
                tempSub.setBackground(ColorConstants.SELECTED_COLOR);
            } else {
                number += SESSION_LIST_HEIGHT;
                tempSub.setBounds(0, number, SESSION_LIST_WIDTH, SESSION_LIST_HEIGHT);
            }
        }
    }

    /**
     * Set button available or not  and set sessionList not selected background
     *
     * @param flag flag
     * @param panelName panelName
     */
    public void setButtonEnable(boolean flag, String panelName) {
        if (flag) {
            jButtonStop.setIcon(IconLoader.getIcon("/images/db_set_breakpoint_grey.png", getClass()));
            jButtonSuspend.setIcon(AllIcons.Process.ProgressPause);
            jButtonDelete.setIcon(IconLoader.getIcon("/images/gc_grey.png", getClass()));
            for (CustomJLabel customJLabel : sessionList) {
                customJLabel.setBackground(JBColor.background().brighter());
            }
            for (SubSessionListJBPanel tempsubSession : dumpOrHookSessionList) {
                if (!tempsubSession.getPanelName().equals(panelName)) {
                    tempsubSession.setBackground(JBColor.background().brighter());
                }
                if (tempsubSession.getDbPath() != null && !tempsubSession.getDbPath().equals(panelName) && panelName
                    .contains(".db")) {
                    tempsubSession.setBackground(JBColor.background().brighter());
                }
            }
        } else {
            jButtonStop.setIcon(AllIcons.Debugger.Db_set_breakpoint);
            jButtonSuspend.setIcon(AllIcons.Process.ProgressPauseHover);
            jButtonDelete.setIcon(IconLoader.getIcon("/images/gc.png", getClass()));
            // disable all dump or native hook
            for (SubSessionListJBPanel subSessionListJBPanel : dumpOrHookSessionList) {
                subSessionListJBPanel.setBackground(JBColor.background().brighter());
            }
        }
    }

    /**
     * getJButtonDelete
     *
     * @return CustomJButton
     */
    public CustomJButton getJButtonDelete() {
        return jButtonDelete;
    }

    /**
     * getjButtonRun
     *
     * @return CustomJButton
     */
    public CustomJButton getjButtonRun() {
        return jButtonStop;
    }

    /**
     * getjButtonStop
     *
     * @return CustomJButton
     */
    public CustomJButton getjButtonStop() {
        return jButtonSuspend;
    }

    /**
     * getjButtonSave
     *
     * @return CustomJButton
     */
    public CustomJButton getjButtonSave() {
        return jButtonSave;
    }

    /**
     * getConfigButton
     *
     * @return CustomJButton
     */
    public CustomJButton getConfigButton() {
        return configButton;
    }

    /**
     * getjButtonBottom
     *
     * @return CustomJButton
     */
    public CustomJButton getjButtonBottom() {
        return jButtonBottom;
    }

    /**
     * getjButtonLeft
     *
     * @return CustomJButton
     */
    public CustomJButton getjButtonLeft() {
        return jButtonLeft;
    }

    /**
     * getjButtonUp
     *
     * @return JButton
     */
    public JButton getjButtonUp() {
        return jButtonUp;
    }

    /**
     * getjButtonNext
     *
     * @return JButton
     */
    public JButton getjButtonNext() {
        return jButtonNext;
    }

    /**
     * getSplitPane
     *
     * @return JSplitPane
     */
    public JSplitPane getSplitPane() {
        return splitPane;
    }

    /**
     * getJScrollCardsPanelInner
     *
     * @return JBPanel
     */
    public JBPanel getJScrollCardsPanelInner() {
        return jScrollCardsPanelInner;
    }

    /**
     * getCards
     *
     * @return JBPanel
     */
    public JBPanel getCards() {
        return cards;
    }

    /**
     * CardLayout
     *
     * @return CardLayout
     */
    public CardLayout getCardLayout() {
        return cardLayout;
    }

    /**
     * getjPanelSuspension
     *
     * @return JBPanel
     */
    public JBPanel getjPanelSuspension() {
        return jPanelSuspension;
    }

    /**
     * getjComboBox
     *
     * @return CustomComboBox
     */
    public CustomComboBox getjComboBox() {
        return jComboBox;
    }

    /**
     * getTimeJComboBox
     *
     * @return CustomComboBox
     */
    public CustomComboBox getTimeJComboBox() {
        return timeJComboBox;
    }

    /**
     * getCounting
     *
     * @return CountingThread
     */
    public CountingThread getCounting() {
        return counting;
    }

    /**
     * setCounting
     *
     * @param counting counting
     */
    public void setCounting(CountingThread counting) {
        this.counting = counting;
    }

    /**
     * getjTextArea
     *
     * @return JBLabel
     */
    public JBLabel getjTextArea() {
        return jTextArea;
    }

    /**
     * isGreyFlag
     *
     * @return boolean
     */
    public boolean isGreyFlag() {
        return greyFlag;
    }

    /**
     * setGreyFlag
     *
     * @param greyFlag greyFlag
     */
    public void setGreyFlag(boolean greyFlag) {
        this.greyFlag = greyFlag;
    }

    /**
     * getjPanelMiddleRight
     *
     * @return JBPanel
     */
    public JBPanel getjPanelMiddleRight() {
        return jPanelMiddleRight;
    }

    /**
     * getPanelTop
     *
     * @return JBPanel
     */
    public JBPanel getPanelTop() {
        return panelTop;
    }

    /**
     * setPanelTop
     *
     * @param panelTop panelTop
     */
    public void setPanelTop(JBPanel panelTop) {
        this.panelTop = panelTop;
    }

    /**
     * getDumpOrHookSessionList
     *
     * @return List <SubSessionListJBPanel>
     */
    public List<SubSessionListJBPanel> getDumpOrHookSessionList() {
        return dumpOrHookSessionList;
    }
}