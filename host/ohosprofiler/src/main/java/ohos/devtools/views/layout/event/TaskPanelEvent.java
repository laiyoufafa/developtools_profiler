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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.Common;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.swing.SystemTunningConfigPanel;
import ohos.devtools.views.layout.swing.TaskPanel;
import ohos.devtools.views.layout.swing.TaskPanelWelcome;
import ohos.devtools.views.layout.swing.TaskScenePanel;

/**
 * 设备进程容器
 *
 * @version 1.0
 * @date 2021/03/02
 **/
public class TaskPanelEvent {
    private static final Logger LOGGER = LogManager.getLogger(TaskPanelEvent.class);

    private Common common = new Common();

    /**
     * AddClick
     *
     * @param jTaskPanel       jTaskPanel
     * @param taskPanel        taskPanel
     * @param taskPanelWelcome taskPanelWelcome
     */
    public void jButtonAddClick(TaskPanel jTaskPanel, JPanel taskPanel, TaskPanelWelcome taskPanelWelcome) {
        jTaskPanel.getJButtonAdd().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                taskPanel.removeAll();
                if (Constant.jtasksTab == null || (Constant.jtasksTab != null
                    && Constant.jtasksTab.getTabCount() == 0)) {
                    Constant.jtasksTab = new JTabbedPane();
                }
                new TaskPanel(taskPanel, taskPanelWelcome);
                // 更新所有的run -- of --
                common.updateNum(Constant.jtasksTab);
                taskPanel.getParent().getParent().getParent().getParent().setVisible(true);
            }
        });
    }

    /**
     * When the total length of the tab label is too long, the size of each label is automatically
     * changed so that all the labels are in one line
     *
     * @param jButton   jButton
     * @param taskPanel taskPanel
     */
    private void tabAdaptive(JButton jButton, JPanel taskPanel) {
        int countX = Constant.jtasksTab.getTabCount() * LayoutConstants.NUMBER_X;
        if (countX > taskPanel.getWidth()) {
            for (int index = 0; index < Constant.jtasksTab.getTabCount(); index++) {
                Constant.jtasksTab.getTabComponentAt(index).setPreferredSize(new Dimension(
                    ((taskPanel.getWidth() - LayoutConstants.APP_BUT_X) / Constant.jtasksTab.getTabCount())
                        - LayoutConstants.TASK_DEC_NUM, LayoutConstants.JAVA_HEIGHT));
                jButton.setBounds(taskPanel.getWidth() - LayoutConstants.TASK_LABEL_X, LayoutConstants.NUMBER_Y,
                    LayoutConstants.BUTTON_HEIGHT, LayoutConstants.BUTTON_HEIGHT);
            }
        } else {
            for (int index = 0; index < Constant.jtasksTab.getTabCount(); index++) {
                Component taskTable = Constant.jtasksTab.getTabComponentAt(index);
                if (taskTable instanceof JPanel) {
                    JPanel taskTablePanel = (JPanel) taskTable;
                    taskTablePanel.getComponents()[0]
                        .setPreferredSize(new Dimension(LayoutConstants.JP_LEFT_WIDTH, LayoutConstants.JAVA_HEIGHT));
                }
                Constant.jtasksTab.getTabComponentAt(index)
                    .setPreferredSize(new Dimension(LayoutConstants.JPA_LABEL_WIDTH, LayoutConstants.JAVA_HEIGHT));
                jButton.setBounds(LayoutConstants.NUMBER_X_ADD * Constant.jtasksTab.getTabCount(),
                    LayoutConstants.NUMBER_Y, LayoutConstants.BUTTON_HEIGHT, LayoutConstants.BUTTON_HEIGHT);
            }
        }
    }

    /**
     * XButton to add close tab function
     *
     * @param jTaskPanel       jTaskPanel
     * @param taskPanel        taskPanel
     * @param taskPanelWelcome taskPanelWelcome
     */
    public void clickClose(TaskPanel jTaskPanel, JPanel taskPanel, TaskPanelWelcome taskPanelWelcome) {
        jTaskPanel.getJLabelClose().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                // 清除Constant.tracePath
                Constant.tracePath = "";
                jTaskPanel.removeAll();
                taskPanel.removeAll();
                Constant.jtasksTab.remove(Constant.jtasksTab.indexOfTabComponent(jTaskPanel.getJPanelTabLabel()));
                // 移除现有组件重新添加，解决+号位置问题
                JButton jButton = jTaskPanel.getJButtonAdd();
                // 当选项卡标签总长度过长时自动改变每个标签的大小使所有的标签在一行
                tabAdaptive(jButton, taskPanel);
                jTaskPanel.setjButtonAdd(jButton);
                jTaskPanel.add(jButton);
                jTaskPanel.add(Constant.jtasksTab);
                taskPanel.add(jTaskPanel);
                taskPanel.repaint();
                // 更新所有的run -- of --
                common.updateNum(Constant.jtasksTab);
                // 移除页面更新信息
                if (Constant.jtasksTab.getTabCount() == 0) {
                    Long localSessionId = jTaskPanel.getLocalSessionId();
                    if (localSessionId != null && localSessionId != 0L) {
                        SessionManager sessionManager = SessionManager.getInstance();
                        sessionManager.deleteSession(localSessionId);
                    }
                    taskPanel.removeAll();
                    Constant.jtasksTab = null;
                    // 添加首页
                    taskPanel.add(taskPanelWelcome);
                    taskPanel.updateUI();
                    taskPanel.repaint();
                }
            }
        });
    }

    /**
     * XAdd the button to move the mouse into the display close button
     *
     * @param jTaskPanel jTaskPanel
     */
    public void mouseEntered(TaskPanel jTaskPanel) {
        jTaskPanel.getJLabelClose().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                jTaskPanel.getJLabelClose().setText("X");
            }
        });
    }

    /**
     * X button is added when the mouse moves out without showing the close button
     *
     * @param jTaskPanel jTaskPanel
     */
    public void mouseExited(TaskPanel jTaskPanel) {
        jTaskPanel.getJLabelClose().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                jTaskPanel.getJLabelClose().setText("");
            }
        });
    }

    /**
     * Add a click event to the figure JButtonApplyTun
     *
     * @param taskPanel  taskPanel
     * @param jTaskPanel jTaskPanel
     */
    public void pplyTunMouseListener(JPanel taskPanel, TaskPanel jTaskPanel) {
        jTaskPanel.getJButtonApplyTun().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                jTaskPanel.getJLabelTaskTun().setText(
                    "<html>Application tuning<br/><br/>Tune application Launch performance with "
                        + "a 5 second time profile and a thread state trace.</html>");
                jTaskPanel.getjLabelIcon().setIcon(new ImageIcon(
                    TaskPanelEvent.class.getClassLoader().getResource("images/application_tuning.png")));
                taskPanel.repaint();
            }
        });
    }

    /**
     * Add a click event to the figure JButtonSystemTun
     *
     * @param taskPanel  taskPanel
     * @param jTaskPanel jTaskPanel
     */
    public void addSystemMouseListener(JPanel taskPanel, TaskPanel jTaskPanel) {
        jTaskPanel.getJButtonSystemTun().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                jTaskPanel.getJLabelTaskTun().setText(
                    "<html>System tuning<br/><br/>A comprehensive view of what’s happing in the "
                        + "operating system. See how threads are being scheduled across CPUs.</html>");
                jTaskPanel.getjLabelIcon().setIcon(new ImageIcon(
                    TaskPanelEvent.class.getClassLoader().getResource("images/system_tuning.png")));
                taskPanel.repaint();
            }
        });
    }

    /**
     * Add a click event to the Choose button
     *
     * @param jTaskPanel jTaskPanel
     */
    public void applicationTuningClickListener(TaskPanel jTaskPanel) {
        jTaskPanel.getChooseButton().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                // 选项卡页面移除场景选择并添加三级页面
                jTaskPanel.getOptionJPanelContent().setVisible(false);
                if (jTaskPanel.getJLabelTaskTun().getText().contains("System tuning")) {
                    SystemTunningConfigPanel taskSystemTuningPanel = new SystemTunningConfigPanel(jTaskPanel);
                    // 将三级界面添加进二级界面容器
                    jTaskPanel.getOptionJPanel().setLayout(new BorderLayout());
                    jTaskPanel.getOptionJPanel().add(taskSystemTuningPanel);
                } else {
                    TaskScenePanel taskScenePanel = new TaskScenePanel(jTaskPanel);
                    // 将三级界面添加进二级界面容器
                    jTaskPanel.getOptionJPanel().setLayout(new BorderLayout());
                    jTaskPanel.getOptionJPanel().add(taskScenePanel);
                }
                // 刷新页面
                jTaskPanel.getOptionJPanel().repaint();
            }
        });
    }

    /**
     * Monitor window size changes to make graphicsJpanel size adaptive
     *
     * @param taskPanel      taskPanel
     * @param graphicsJpanel graphicsJpanel
     */
    public void listenerGraphicsJpanel(JPanel graphicsJpanel, TaskPanel taskPanel) {
        taskPanel.getOptionJPanelContent().addComponentListener(new ComponentAdapter() {
            /**
             * componentResized
             *
             * @param exception exception
             */
            public void componentResized(ComponentEvent exception) {
                graphicsJpanel.setBounds(0, LayoutConstants.WIDTHSUPEN, taskPanel.getOptionJPanelContent().getWidth(),
                    LayoutConstants.JAVA_WIDTH);
            }
        });
    }

    /**
     * Monitor the size of taskPanel, and link the size of jTabbedPane to it
     *
     * @param taskPanel  taskPanel
     * @param jButtonAdd jButtonAdd
     */
    public void listenerTaskPanel(JPanel taskPanel, JButton jButtonAdd) {
        taskPanel.addComponentListener(new ComponentAdapter() {
            /**
             * componentResized
             *
             * @param exception exception
             */
            public void componentResized(ComponentEvent exception) {
                int width = taskPanel.getWidth();
                int height = taskPanel.getHeight();
                Constant.jtasksTab.setBounds(LayoutConstants.DEVICE_PRO_Y, LayoutConstants.DEVICE_PRO_Y,
                    LayoutConstants.WINDOW_WIDTH + (width - LayoutConstants.WINDOW_WIDTH),
                    LayoutConstants.WINDOW_HEIGHT + (height - LayoutConstants.WINDOW_HEIGHT));
                double result = Constant.jtasksTab.getTabCount() * LayoutConstants.NUMBER_X;
                if (result > taskPanel.getWidth()) {
                    for (int index = 0; index < Constant.jtasksTab.getTabCount(); index++) {
                        Component taskTable = Constant.jtasksTab.getTabComponentAt(index);
                        if (taskTable instanceof JPanel) {
                            JPanel taskTablePanel = (JPanel) taskTable;
                            taskTablePanel.getComponents()[0].setPreferredSize(new Dimension(
                                (((taskPanel.getWidth() - LayoutConstants.APP_BUT_X)
                                    / Constant.jtasksTab.getTabCount()) - LayoutConstants.TASK_DEC_NUM)
                                    - LayoutConstants.JAVA_HEIGHT, LayoutConstants.JAVA_HEIGHT));
                        }
                        Constant.jtasksTab.getTabComponentAt(index).setPreferredSize(new Dimension(
                            ((taskPanel.getWidth() - LayoutConstants.APP_BUT_X) / Constant.jtasksTab.getTabCount())
                                - LayoutConstants.TASK_DEC_NUM, LayoutConstants.JAVA_HEIGHT));
                        jButtonAdd
                            .setBounds(taskPanel.getWidth() - LayoutConstants.TASK_LABEL_X, LayoutConstants.NUMBER_Y,
                                LayoutConstants.BUTTON_HEIGHT, LayoutConstants.BUTTON_HEIGHT);
                    }
                } else {
                    for (int index = 0; index < Constant.jtasksTab.getTabCount(); index++) {
                        Object tabObj = Constant.jtasksTab.getTabComponentAt(index);
                        if (tabObj instanceof JPanel) {
                            ((JPanel) tabObj).getComponents()[0].setPreferredSize(
                                new Dimension(LayoutConstants.JP_LEFT_WIDTH, LayoutConstants.JAVA_HEIGHT));
                        }
                        Constant.jtasksTab.getTabComponentAt(index).setPreferredSize(
                            new Dimension(LayoutConstants.JPA_LABEL_WIDTH, LayoutConstants.JAVA_HEIGHT));
                        jButtonAdd.setBounds(LayoutConstants.NUMBER_X_ADD * Constant.jtasksTab.getTabCount(),
                            LayoutConstants.NUMBER_Y, LayoutConstants.BUTTON_HEIGHT, LayoutConstants.BUTTON_HEIGHT);
                    }
                }
            }
        });
        listenerTabPanel(taskPanel, jButtonAdd);
    }

    /**
     * Tab listening event
     *
     * @param taskPanel  taskPanel
     * @param jButtonAdd jButtonAdd
     */
    private void listenerTabPanel(JPanel taskPanel, JButton jButtonAdd) {
        Constant.jtasksTab.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent exception) {
                double result = Constant.jtasksTab.getTabCount() * LayoutConstants.NUMBER_X;
                if (result > taskPanel.getWidth()) {
                    for (int index = 0; index < Constant.jtasksTab.getTabCount(); index++) {
                        Object tabObj = Constant.jtasksTab.getTabComponentAt(index);
                        if (tabObj instanceof JPanel) {
                            ((JPanel) tabObj).getComponents()[0].setPreferredSize(new Dimension(
                                (((taskPanel.getWidth() - LayoutConstants.APP_BUT_X)
                                    / Constant.jtasksTab.getTabCount()) - LayoutConstants.TASK_DEC_NUM)
                                    - LayoutConstants.JAVA_HEIGHT, LayoutConstants.JAVA_HEIGHT));
                        }
                        Constant.jtasksTab.getTabComponentAt(index).setPreferredSize(new Dimension(
                            ((taskPanel.getWidth() - LayoutConstants.APP_BUT_X) / Constant.jtasksTab.getTabCount())
                                - LayoutConstants.TASK_DEC_NUM, LayoutConstants.JAVA_HEIGHT));
                        jButtonAdd
                            .setBounds(taskPanel.getWidth() - LayoutConstants.TASK_LABEL_X,
                                LayoutConstants.NUMBER_Y, LayoutConstants.BUTTON_HEIGHT, LayoutConstants.BUTTON_HEIGHT);
                    }
                } else {
                    jButtonAdd.setBounds(LayoutConstants.NUMBER_X_ADD * Constant.jtasksTab.getTabCount(),
                        LayoutConstants.NUMBER_Y, LayoutConstants.BUTTON_HEIGHT, LayoutConstants.BUTTON_HEIGHT);
                }
            }
        });
    }

    /**
     * 场景选择，添加选择样式。
     *
     * @param taskPanel 二级界面
     */
    public void sigleClick(TaskPanel taskPanel) {
        taskPanel.getJButtonApplyTun().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                taskPanel.getJButtonApplyTun().setBackground(ColorConstants.APPLYTUN_COLOR);
                taskPanel.getJButtonSystemTun().setBackground(ColorConstants.SYSTEM_COLOR);
                taskPanel.getJButtonHadoop().setBackground(ColorConstants.SYSTEM_COLOR);
            }
        });
        taskPanel.getJButtonSystemTun().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                taskPanel.getJButtonApplyTun().setBackground(ColorConstants.SYSTEM_COLOR);
                taskPanel.getJButtonSystemTun().setBackground(ColorConstants.APPLYTUN_COLOR);
                taskPanel.getJButtonHadoop().setBackground(ColorConstants.SYSTEM_COLOR);
            }
        });
        taskPanel.getJButtonHadoop().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                taskPanel.getJButtonApplyTun().setBackground(ColorConstants.SYSTEM_COLOR);
                taskPanel.getJButtonSystemTun().setBackground(ColorConstants.SYSTEM_COLOR);
                taskPanel.getJButtonHadoop().setBackground(ColorConstants.APPLYTUN_COLOR);
            }
        });
    }

    /**
     * 监听窗体大小改变组件位置
     *
     * @param taskPanel  taskPanel
     * @param jTaskPanel jTaskPanel
     */
    public void listenerWindow(JPanel taskPanel, TaskPanel jTaskPanel) {
        taskPanel.addComponentListener(new ComponentAdapter() {
            /**
             * componentResized
             *
             * @param exception exception
             */
            public void componentResized(ComponentEvent exception) {
                int width = taskPanel.getWidth();
                int height = taskPanel.getHeight();
                jTaskPanel.getChooseButton()
                    .setBounds(LayoutConstants.CHOSSE_X + (width - LayoutConstants.WINDOW_WIDTH),
                        LayoutConstants.CHOOSE_Y + (height - LayoutConstants.WINDOW_HEIGHT),
                        LayoutConstants.DEVICES_WIDTH, LayoutConstants.CHOOSE_HEIGHT);
                jTaskPanel.getTraceButton().setBounds(LayoutConstants.TASK_SCENE_X,
                    LayoutConstants.CHOOSE_Y + (height - LayoutConstants.WINDOW_HEIGHT), LayoutConstants.CHOOSE_WIDTH,
                    LayoutConstants.CHOOSE_HEIGHT);
            }
        });
    }
}
