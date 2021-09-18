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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.services.cpu.CpuDataCache;
import ohos.devtools.services.memory.memoryservice.MemoryDataCache;
import ohos.devtools.views.applicationtrace.AppTracePanel;
import ohos.devtools.views.charts.model.ChartStandard;
import ohos.devtools.views.charts.utils.ChartUtils;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.common.customcomp.CustomComboBox;
import ohos.devtools.views.common.customcomp.CustomJButton;
import ohos.devtools.views.common.customcomp.CustomJLabel;
import ohos.devtools.views.layout.chartview.PerformanceIndexPopupMenu;
import ohos.devtools.views.layout.chartview.SubSessionListJBPanel;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.CountingThread;
import ohos.devtools.views.layout.dialog.ExportFileChooserDialog;
import ohos.devtools.views.layout.dialog.SampleDialog;
import ohos.devtools.views.layout.TaskPanel;
import ohos.devtools.views.layout.chartview.TaskScenePanelChart;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 任务场景面板图事件类
 */
public class TaskScenePanelChartEvent {
    private static final int NUM_2 = 2;
    private static final int NUM_10 = 10;
    private static final Long ABNORMAL = -1L;
    private boolean flagLeft = false;
    private boolean flag = false;
    private Container obj = null;
    private boolean buttonAvailable = true;
    private boolean resultDelete = false;

    /**
     * 点击删除
     *
     * @param taskScenePanelChart taskScenePanelChart
     */
    public void clickDelete(TaskScenePanelChart taskScenePanelChart) {
        taskScenePanelChart.getJButtonDelete().addActionListener(new ActionListener() {
            /**
             * actionPerformed
             *
             * @param event event
             */
            @Override
            public void actionPerformed(ActionEvent event) {
                if (taskScenePanelChart.isGreyFlag()) {
                    return;
                }
                long localSessionId = taskScenePanelChart.getJButtonDelete().getSessionId();
                Font font = new Font("", 0, LayoutConstants.SIXTEEN);
                UIManager.put("OptionPane.messageFont", font);
                if (localSessionId < 0) {
                    new SampleDialog("prompt", "Please select the task to delete first !").show();
                    return;
                }
                SampleDialog sampleDialogWrapper =
                    new SampleDialog("prompt", "Are you sure you want to delete this task ？");
                boolean flags = sampleDialogWrapper.showAndGet();
                if (flags) {
                    // 调用SessionManager的删除sessionId
                    if (!SessionManager.getInstance().endSession(localSessionId)) {
                        return;
                    }
                    CountingThread countingThread = taskScenePanelChart.getCounting();
                    countingThread.setStopFlag(true);
                    // 容器中销毁char和Session的panel
                    // 删除界面cards的Session
                    deleteSession(localSessionId, taskScenePanelChart);
                    // 删除界面char
                    deleteChart(localSessionId, taskScenePanelChart);
                    // After deleting the chart list, there are sub options that should be displayed
                    List<SubSessionListJBPanel> list = taskScenePanelChart.getDumpOrHookSessionList();
                    if (list.size() > 0) {
                        taskScenePanelChart.showSubSessionList(list);
                    }
                    taskScenePanelChart.getJButtonDelete().setSessionId(ABNORMAL);
                    taskScenePanelChart.repaint();
                    // 清除sessionid，保存trace文件时根据这个判断是否可以保存
                    taskScenePanelChart.getjButtonSave().setSessionId(0);
                    taskScenePanelChart.getjButtonRun()
                        .setIcon(IconLoader.getIcon("/images/breakpoint_grey.png", getClass()));
                    taskScenePanelChart.getjButtonStop().setIcon(AllIcons.Process.ProgressResumeHover);
                    resultDelete = true;
                    clearTimeCounting(taskScenePanelChart);
                }
            }
        });
    }

    /**
     * Delete interface chart
     *
     * @param localSessionId localSessionId
     * @param taskScenePanelChart taskScenePanelChart
     */
    private void deleteChart(long localSessionId, TaskScenePanelChart taskScenePanelChart) {
        Component[] carts = taskScenePanelChart.getCards().getComponents();
        for (Component cart : carts) {
            if (cart instanceof JPanel) {
                Component[] jcardsPanels = ((JPanel) cart).getComponents();
                for (Component item : jcardsPanels) {
                    ProfilerChartsView profilerView = null;
                    if (item instanceof ProfilerChartsView) {
                        profilerView = (ProfilerChartsView) item;
                        if (profilerView.getSessionId() == localSessionId) {
                            ((JPanel) cart).remove(profilerView);
                        }
                    }
                }
            }
        }
    }

    /**
     * Delete Session
     *
     * @param localSessionId localSessionId
     * @param taskScenePanelChart taskScenePanelChart
     */
    private void deleteSession(long localSessionId, TaskScenePanelChart taskScenePanelChart) {
        JPanel jScrollCardsPanelInner = taskScenePanelChart.getJScrollCardsPanelInner();
        Component[] innerJpanel = jScrollCardsPanelInner.getComponents();
        for (Component inner : innerJpanel) {
            Component[] innerLable = null;
            if (inner instanceof JPanel) {
                innerLable = ((JPanel) inner).getComponents();
                for (Component item : innerLable) {
                    if (item instanceof CustomJLabel && localSessionId == ((CustomJLabel) item).getSessionId()) {
                        jScrollCardsPanelInner.remove(inner);
                    }
                }
            }
        }
    }

    /**
     * clickUpAndNext
     *
     * @param taskScenePanelChart taskScenePanelChart
     */
    public void clickUpAndNext(TaskScenePanelChart taskScenePanelChart) {
        // 给上一个页面按钮添加点击事件
        taskScenePanelChart.getjButtonUp().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent exception) {
                if (Constant.jtasksTab.getSelectedIndex() > 0) {
                    Constant.jtasksTab.setSelectedIndex(Constant.jtasksTab.getSelectedIndex() - 1);
                }
            }
        });
        // 给下一个页面按钮添加点击事件
        taskScenePanelChart.getjButtonNext().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent exception) {
                if (Constant.jtasksTab.getSelectedIndex() != Constant.jtasksTab.getTabCount() - 1) {
                    Constant.jtasksTab.setSelectedIndex(Constant.jtasksTab.getSelectedIndex() + 1);
                }
            }
        });
    }

    /**
     * saveButtonAddClick
     *
     * @param labelSave labelSave
     * @param name name
     */
    public void saveButtonAddClick(CustomJLabel labelSave, String name) {
        labelSave.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                String fileType = "";
                if (name.contains("Native Hook")) {
                    fileType = "nativehook";
                } else {
                    fileType = "hprof";
                }
                ExportFileChooserDialog fileChooserDialogWrapper =
                    new ExportFileChooserDialog("Export As", fileType);
                boolean showAndGet = fileChooserDialogWrapper.showAndGet();
                if (showAndGet) {
                    String exportFileName = labelSave.getName();
                    boolean saveResult =
                        SessionManager.getInstance().exportDumpOrHookFile(exportFileName, fileChooserDialogWrapper);
                    if (saveResult) {
                        new SampleDialog("prompt", "Save Successfully !").show();
                    } else {
                        new SampleDialog("prompt", "Save failure !").show();
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent event) {
                labelSave.setBorder(BorderFactory.createTitledBorder(""));
            }

            @Override
            public void mouseExited(MouseEvent event) {
                labelSave.setBorder(null);
            }
        });
    }

    /**
     * dumpPanelAddClick
     *
     * @param dumpPanel dumpPanel
     * @param taskScenePanelChart taskScenePanelChart
     */
    public void sessionListPanelAddClick(SubSessionListJBPanel dumpPanel, TaskScenePanelChart taskScenePanelChart) {
        dumpPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                taskScenePanelChart.setButtonEnable(true, dumpPanel.getPanelName());
                if (dumpPanel.getPanelName().contains("traceApp") || dumpPanel.getPanelName().contains("nativePerf")) {
                    Component[] components = taskScenePanelChart.getCards().getComponents();
                    for (Component component : components) {
                        if (component instanceof AppTracePanel) {
                            taskScenePanelChart.getCards().remove(component);
                        }
                    }
                    if (dumpPanel.getPanelName().contains("traceApp")) {
                        taskScenePanelChart
                            .showAppTrace(dumpPanel.getDbPath(), taskScenePanelChart.getjButtonRun().getSessionId());
                    }
                    taskScenePanelChart.setButtonEnable(true, dumpPanel.getDbPath());
                } else {
                    taskScenePanelChart.getCardLayout().show(taskScenePanelChart.getCards(), dumpPanel.getPanelName());
                }
                taskScenePanelChart.setGreyFlag(true);
                dumpPanel.setBackground(ColorConstants.SELECTED_COLOR);
            }

            @Override
            public void mouseEntered(MouseEvent event) {
            }

            @Override
            public void mouseExited(MouseEvent event) {
            }
        });
    }

    private void stopTask(ProfilerChartsView profilerView, CustomJButton stopButton,
        TaskScenePanelChart taskScenePanelChart) {
        profilerView.getPublisher().stopRefresh(false);
        stopButton.setIcon(IconLoader.getIcon("/images/breakpoint.png", getClass()));
        taskScenePanelChart.getjButtonStop().setIcon(AllIcons.Process.ProgressResumeHover);
        buttonAvailable = false;
        CountingThread countingThread = taskScenePanelChart.getCounting();
        countingThread.setStopFlag(true);
        stopButton.setToolTipText("Start");
        SessionManager.getInstance().endSession(stopButton.getSessionId());
        // Clear cache of all monitor items
        MemoryDataCache.getInstance().clearCacheBySession(stopButton.getSessionId());
        CpuDataCache.getInstance().clearCacheBySession(stopButton.getSessionId());
        profilerView.setStop(true);
        profilerView.setPause(true);
    }

    private void startTask(ProfilerChartsView profilerView, CustomJButton stopButton,
        TaskScenePanelChart taskScenePanelChart) {
        stopButton.setIcon(AllIcons.Debugger.Db_set_breakpoint);
        taskScenePanelChart.getjButtonStop().setIcon(AllIcons.Process.ProgressPauseHover);
        buttonAvailable = true;
        stopButton.setToolTipText("Stop");
        // Set the icon change after the pause button is clicked Open the session to get data
        long sessionId = stopButton.getSessionId();
        SessionManager sessionManager = SessionManager.getInstance();
        sessionManager.startSession(sessionId, true);
        sessionManager.fetchData(sessionId);
        if (profilerView.getPublisher().isDisplayScrollbar()) {
            profilerView.removeScrollbar();
        }
        JLabel jTextArea = taskScenePanelChart.getjTextArea();
        CountingThread counting = new CountingThread(jTextArea);
        taskScenePanelChart.setCounting(counting);
        counting.start();
        // Clear the selected time after restarting
        profilerView.getPublisher().getStandard().clearAllSelectedRanges();
        profilerView.showLoading();
        profilerView.setStop(false);
        profilerView.setPause(false);
    }

    /**
     * clickRunAndStop
     *
     * @param taskScenePanelChart taskScenePanelChart
     * @param profilerView profilerView
     */
    public void clickRunAndStop(TaskScenePanelChart taskScenePanelChart, ProfilerChartsView profilerView) {
        CustomJButton stopButton = taskScenePanelChart.getjButtonRun();
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent exception) {
                if (taskScenePanelChart.isGreyFlag()) {
                    return;
                }
                if (resultDelete) {
                    return;
                }
                if (profilerView.isLoading()) {
                    return;
                }
                if (!profilerView.isStop()) {
                    stopTask(profilerView, stopButton, taskScenePanelChart);
                } else {
                    startTask(profilerView, stopButton, taskScenePanelChart);
                }
            }
        });
        pauseButtonEvent(taskScenePanelChart, profilerView);
    }

    /**
     * Pause button event
     *
     * @param taskScenePanelChart taskScenePanelChart
     * @param profilerView profilerView
     */
    private void pauseButtonEvent(TaskScenePanelChart taskScenePanelChart, ProfilerChartsView profilerView) {
        taskScenePanelChart.getjButtonStop().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent exception) {
                if (taskScenePanelChart.isGreyFlag()) {
                    return;
                }
                if (resultDelete) {
                    return;
                }
                if (profilerView.isLoading()) {
                    return;
                }
                if (buttonAvailable) {
                    if (!profilerView.isPause()) {
                        taskScenePanelChart.getjButtonStop().setIcon(AllIcons.Process.ProgressResumeHover);
                        taskScenePanelChart.getjButtonStop().setToolTipText("Start");
                        profilerView.getPublisher().pauseRefresh();
                        profilerView.setPause(true);
                    } else {
                        taskScenePanelChart.getjButtonStop().setIcon(AllIcons.Process.ProgressPauseHover);
                        taskScenePanelChart.getjButtonBottom()
                            .setIcon(IconLoader.getIcon("/images/previewDetailsVertically_grey.png", getClass()));
                        taskScenePanelChart.getjButtonStop().setToolTipText("Suspend");
                        profilerView.getPublisher().restartRefresh();
                        profilerView.setPause(false);
                    }
                }
            }
        });
    }

    /**
     * splitPaneChange
     *
     * @param taskScenePanelChart taskScenePanelChart
     * @param numberSum numberSum
     */
    public void splitPaneChange(TaskScenePanelChart taskScenePanelChart, int numberSum) {
        taskScenePanelChart.getSplitPane().addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            /**
             * propertyChange
             *
             * @param evt evt
             */
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(JSplitPane.DIVIDER_LOCATION_PROPERTY)) {
                    int numEvt = 0;
                    for (int index = 0; index < numberSum; index++) {
                        JPanel jPanel = null;
                        Object objNew = taskScenePanelChart.getJScrollCardsPanelInner().getComponentAt(0, numEvt);
                        if (objNew instanceof JPanel) {
                            jPanel = (JPanel) objNew;
                            if (Integer.parseInt(evt.getNewValue().toString())
                                < LayoutConstants.SESSION_LIST_DIVIDER_WIDTH) {
                                jPanel.setBounds(0, numEvt, LayoutConstants.SESSION_LIST_DIVIDER_WIDTH,
                                    LayoutConstants.SIXTY);
                            } else {
                                jPanel.setBounds(0, numEvt, LayoutConstants.SESSION_LIST_DIVIDER_WIDTH + Integer
                                    .parseInt(evt.getNewValue().toString())
                                    - LayoutConstants.SESSION_LIST_DIVIDER_WIDTH, LayoutConstants.SIXTY);
                            }

                            numEvt += LayoutConstants.SIXTY;
                            jPanel.updateUI();
                            jPanel.repaint();
                        }
                    }
                }
            }
        });
    }

    /**
     * clickLeft
     *
     * @param taskScenePanelChart taskScenePanelChart
     * @param hosJLabelList hosJLabelList
     */
    public void clickLeft(TaskScenePanelChart taskScenePanelChart, List<CustomJLabel> hosJLabelList) {
        taskScenePanelChart.getjButtonLeft().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent exception) {
                if (!flagLeft) {
                    taskScenePanelChart.getSplitPane().setDividerLocation(1);
                    taskScenePanelChart.getSplitPane().updateUI();
                    taskScenePanelChart.getSplitPane().repaint();
                    flagLeft = true;
                } else {
                    if (hosJLabelList.size() > 0 && hosJLabelList.get(0).isOnline()) {
                        taskScenePanelChart.getSplitPane()
                            .setDividerLocation(LayoutConstants.SESSION_LIST_DIVIDER_WIDTH);
                    } else {
                        taskScenePanelChart.getSplitPane().setDividerLocation(LayoutConstants.NUM_200);
                    }
                    flagLeft = false;
                    taskScenePanelChart.getSplitPane().updateUI();
                    taskScenePanelChart.getSplitPane().repaint();
                }
            }
        });
        taskScenePanelChart.addComponentListener(new ComponentAdapter() {
            /**
             * componentResized
             *
             * @param exception exception
             */
            public void componentResized(ComponentEvent exception) {
                if (flagLeft) {
                    taskScenePanelChart.getSplitPane().setDividerLocation(1);
                    taskScenePanelChart.getSplitPane().updateUI();
                    taskScenePanelChart.getSplitPane().repaint();
                    flagLeft = true;
                } else {
                    taskScenePanelChart.getSplitPane()
                        .setDividerLocation(taskScenePanelChart.getSplitPane().getDividerLocation());
                    taskScenePanelChart.getSplitPane().updateUI();
                    taskScenePanelChart.getSplitPane().repaint();
                }
            }
        });
    }

    /**
     * bindSessionId
     *
     * @param taskScenePanelChart taskScenePanelChart
     * @param jLabelRight jLabelRight
     * @param jMultiplePanel jMultiplePanel
     */
    private void bindSessionId(TaskScenePanelChart taskScenePanelChart, CustomJLabel jLabelRight,
        JPanel jMultiplePanel) {
        taskScenePanelChart.getjButtonRun().setSessionId(jLabelRight.getSessionId());
        taskScenePanelChart.getjButtonStop().setSessionId(jLabelRight.getSessionId());
        taskScenePanelChart.getjButtonSave().setSessionId(jLabelRight.getSessionId());
        taskScenePanelChart.getjButtonSave().setDeviceName(jLabelRight.getDeviceName());
        taskScenePanelChart.getjButtonSave().setProcessName(jLabelRight.getProcessName());
        taskScenePanelChart.getJButtonDelete().setSessionId(jLabelRight.getSessionId());
        taskScenePanelChart.getJButtonDelete().setDeviceName(jLabelRight.getDeviceName());
        taskScenePanelChart.getJButtonDelete().setProcessName(jLabelRight.getProcessName());
        taskScenePanelChart.getConfigButton().setSessionId(jLabelRight.getSessionId());
        taskScenePanelChart.getjButtonBottom().setSessionId(jLabelRight.getSessionId());
        taskScenePanelChart.getjButtonLeft().setSessionId(jLabelRight.getSessionId());
        taskScenePanelChart.getjComboBox().setSessionId(jLabelRight.getSessionId());
        taskScenePanelChart.getTimeJComboBox().setSessionId(jLabelRight.getSessionId());
        taskScenePanelChart.getJScrollCardsPanelInner().remove(jMultiplePanel);
        taskScenePanelChart.getJScrollCardsPanelInner().repaint();
    }

    /**
     * clickEvery
     *
     * @param taskScenePanelChart taskScenePanelChart
     * @param jLabelRight jLabelRight
     * @param numberSum numberSum
     * @param jLabelSelect jLabelSelect
     * @param jMultiplePanel jMultiplePanel
     */
    public void clickEvery(TaskScenePanelChart taskScenePanelChart, CustomJLabel jLabelRight, int numberSum,
        String jLabelSelect, JPanel jMultiplePanel) {
        jLabelRight.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                // 绑定sessionId
                bindSessionId(taskScenePanelChart, jLabelRight, jMultiplePanel);
                // 获取其他jlabel设置背景色
                Component[] component = taskScenePanelChart.getJScrollCardsPanelInner().getComponents();
                for (Component componentEvery : component) {
                    JPanel jPanelEvery = null;
                    if (componentEvery instanceof JPanel) {
                        jPanelEvery = (JPanel) componentEvery;
                        Component[] componentjPanelEvery = jPanelEvery.getComponents();
                        for (Component componentjPanelEverySet : componentjPanelEvery) {
                            JLabel jLabelEvery = null;
                            if (componentjPanelEverySet instanceof JLabel) {
                                jLabelEvery = (JLabel) componentjPanelEverySet;
                                jLabelEvery.setBackground(ColorConstants.BLACK_COLOR);
                                jLabelEvery.setForeground(Color.gray);
                            }
                        }
                    }
                }
                taskScenePanelChart.getJScrollCardsPanelInner().add(jMultiplePanel);
                taskScenePanelChart.getJScrollCardsPanelInner().repaint();
                jLabelRight.setBackground(ColorConstants.SELECTED_COLOR);
                if (jLabelRight.getLeft() != null) {
                    jLabelRight.getLeft().setBackground(ColorConstants.SELECTED_COLOR);
                }
                jLabelRight.setForeground(Color.white);
                int numy = 0;
                // 循环确定擦片布局显示哪个页面
                for (int index = 0; index < numberSum; index++) {
                    JPanel jPanel = null;
                    Object innerObj = taskScenePanelChart.getJScrollCardsPanelInner().getComponentAt(0, numy);
                    if (innerObj instanceof JPanel) {
                        jPanel = (JPanel) innerObj;
                        numy += LayoutConstants.HEIGHT;
                        Color colorBack = jPanel.getComponent(0).getBackground();
                        if (colorBack == ColorConstants.SELECTED_COLOR) {
                            taskScenePanelChart.getCardLayout().show(taskScenePanelChart.getCards(), "card" + index);
                            taskScenePanelChart.add(taskScenePanelChart.getPanelTop(), BorderLayout.NORTH);
                        }
                    }
                }
                taskScenePanelChart.setButtonEnable(false, "");
                taskScenePanelChart.setGreyFlag(false);
                // Replace the content of the tab with the content of the clicked device information
                replaceDevicesInfo(jLabelSelect);
            }
        });
    }

    /**
     * Replace device information
     *
     * @param jLabelSelect Selected jLabel
     */
    private void replaceDevicesInfo(String jLabelSelect) {
        JPanel jCompent = null;
        Object tabComObj = Constant.jtasksTab.getTabComponentAt(Constant.jtasksTab.getSelectedIndex());
        if (tabComObj instanceof JPanel) {
            jCompent = (JPanel) tabComObj;
            JPanel jCompentLeft = null;
            Object componentObj = jCompent.getComponent(0);
            if (componentObj instanceof JPanel) {
                jCompentLeft = (JPanel) componentObj;
                Object leftObj = jCompentLeft.getComponent(0);
                if (leftObj instanceof JLabel) {
                    ((JLabel) leftObj).setText(jLabelSelect);
                }
            }
        }
    }

    /**
     * 按钮增加点击时间触发时间刻度的选择
     *
     * @param jComboBox jComboBox
     * @param profilerView profilerView
     */
    public void clickZoomEvery(CustomComboBox jComboBox, ProfilerChartsView profilerView) {
        jComboBox.addItemListener(event -> {
            long sessionId = jComboBox.getSessionId();
            int newSizeTime = 0;
            if (event.getStateChange() == 1) {
                // 获取点击时间选择的时间刻度间隔
                String[] msTime = jComboBox.getSelectedItem().toString().split("m");
                newSizeTime = Integer.parseInt(msTime[0]);
            }
            ChartStandard standard = profilerView.getPublisher().getStandard();
            if (standard != null) {
                checkNewTime(standard, newSizeTime, profilerView);
            }
        });
    }

    /**
     * 检查频率切换后的时间是否需要修正
     *
     * @param standard ChartStandard
     * @param newSizeTime 新的时间大小
     * @param view ProfilerChartsView
     */
    private void checkNewTime(ChartStandard standard, int newSizeTime, ProfilerChartsView view) {
        int oldStart = standard.getDisplayRange().getStartTime();
        // 获取刻度间隔对应的最小时间单位值
        int minSize = ChartUtils.divideInt(newSizeTime, NUM_10) * NUM_2;
        // 刷新绘图标准的最大展示时间和最小时间单位
        int newMaxDisplay = newSizeTime * NUM_10;
        int lastTime = (int) (standard.getLastTimestamp() - standard.getFirstTimestamp());

        int newStart;
        int newEnd;
        // 场景1：频率切换后oldStart + newDisplay超过了lastTime，这时要修正start
        if (oldStart + newMaxDisplay > lastTime) {
            // 场景1.1：切换后newDisplay > lastTime，这时就变成任务刚启动时，Chart没有铺满的场景，start为0，end为lastTime
            // 场景1.2：切换后newDisplay < lastTime，修正start为lastTime - display
            newStart = Math.max(lastTime - newMaxDisplay, 0);
            newEnd = lastTime;
        } else {
            // 场景2：切换后oldStart + newDisplay未超过lastTime，说明可以正常显示，无需修正start
            newStart = oldStart;
            newEnd = oldStart + newMaxDisplay;
        }

        view.getPublisher().msTimeZoom(newMaxDisplay, minSize, newStart, newEnd);
        // 如果newDisplay > lastTime，这时候要隐藏滚动条
        if (newMaxDisplay > lastTime) {
            view.removeScrollbar();
            view.revalidate();
        } else {
            if (view.getHorizontalBar() == null) {
                view.initScrollbar();
            }
            view.getHorizontalBar().resizeAndReposition();
        }
    }

    /**
     * showSuspension
     *
     * @param taskScenePanelChart taskScenePanelChart
     * @param jTaskPanel jTaskPanel
     * @param jButtonSuspen jButtonSuspen
     */
    public void showSuspension(TaskScenePanelChart taskScenePanelChart, TaskPanel jTaskPanel, JButton jButtonSuspen) {
        jButtonSuspen.addActionListener(new ActionListener() {
            /**
             *  actionPerformed
             *
             * @param exception exception
             */
            @Override
            public void actionPerformed(ActionEvent exception) {
                if (!flag) {
                    taskScenePanelChart.getjPanelSuspension().setBackground(Color.RED);
                    taskScenePanelChart.getjPanelSuspension().setBounds(
                        LayoutConstants.WINDOW_HEIGHT + (jTaskPanel.getWidth() - LayoutConstants.WINDOW_WIDTH),
                        LayoutConstants.DEVICES_WIDTH + (jTaskPanel.getHeight() - LayoutConstants.WINDOW_HEIGHT),
                        LayoutConstants.WIDTHSUPEN, LayoutConstants.DEVICE_PRO_WIDTH);
                    jTaskPanel.add(taskScenePanelChart.getjPanelSuspension(), JLayeredPane.DRAG_LAYER);
                    flag = true;
                    jTaskPanel.repaint();
                } else {
                    jTaskPanel.remove(taskScenePanelChart.getjPanelSuspension());
                    flag = false;
                    jTaskPanel.repaint();
                }
            }
        });
    }

    /**
     * setSceneSize
     *
     * @param jTaskPanel jTaskPanel
     * @param taskScenePanelChart taskScenePanelChart
     */
    public void setSceneSize(TaskPanel jTaskPanel, TaskScenePanelChart taskScenePanelChart) {
        jTaskPanel.addComponentListener(new ComponentAdapter() {
            /**
             * componentResized
             *
             * @param event event
             */
            public void componentResized(ComponentEvent event) {
                int width = jTaskPanel.getWidth();
                int height = jTaskPanel.getHeight();
                taskScenePanelChart.getjPanelSuspension()
                    .setBounds(LayoutConstants.WINDOW_HEIGHT + (width - LayoutConstants.WINDOW_WIDTH),
                        LayoutConstants.DEVICES_WIDTH + (height - LayoutConstants.WINDOW_HEIGHT),
                        LayoutConstants.WIDTHSUPEN, LayoutConstants.DEVICE_PRO_WIDTH);
            }
        });
    }

    /**
     * Memory新增配置项
     *
     * @param taskScenePanelChart taskScenePanelChart
     * @param profilerView profilerView
     */
    public void clickConfig(TaskScenePanelChart taskScenePanelChart, ProfilerChartsView profilerView) {
        obj = taskScenePanelChart;
    }

    /**
     * click Performance analysis index configuration
     *
     * @param configButton configButton
     * @param itemMenu itemMenu
     */
    public void clickIndexConfig(CustomJButton configButton, PerformanceIndexPopupMenu itemMenu) {
        configButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                itemMenu.showItemMenu(mouseEvent);
            }
        });
    }

    /**
     * click save
     *
     * @param jButton jButton
     * @param taskScenePanelChart taskScenePanelChart
     */
    public void clickSave(CustomJButton jButton, TaskScenePanelChart taskScenePanelChart) {
        jButton.addActionListener(new ActionListener() {
            /**
             * actionPerformed
             *
             * @param event event
             */
            @Override
            public void actionPerformed(ActionEvent event) {
                if (taskScenePanelChart.isGreyFlag()) {
                    return;
                }
                if (jButton.getSessionId() == 0) {
                    new SampleDialog("prompt", "Please select a process record !").show();
                    return;
                }
                ExportFileChooserDialog fileChooserDialogWrapper =
                    new ExportFileChooserDialog("Export As", UtConstant.FILE_TYPE_TRACE);
                boolean showAndGet = fileChooserDialogWrapper.showAndGet();
                if (showAndGet) {
                    fileChooserDialogWrapper.saveDataToFile(jButton);
                }
            }
        });
    }

    private void clearTimeCounting(TaskScenePanelChart taskScenePanelChart) {
        JLabel jTextArea = taskScenePanelChart.getjTextArea();
        while (taskScenePanelChart.getCounting().isAlive()) {
            SwingUtilities.invokeLater(new Runnable() {
                /**
                 * run
                 */
                public void run() {
                    jTextArea.setText("Run 1 of 1   |   00:00:00");
                }
            });
        }
        SwingUtilities.invokeLater(new Runnable() {
            /**
             * run
             */
            public void run() {
                jTextArea.setText("Run 1 of 1   |   00:00:00");
            }
        });
    }
}
