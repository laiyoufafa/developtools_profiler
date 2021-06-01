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
import ohos.devtools.views.layout.event.HomeWindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import static ohos.devtools.views.common.LayoutConstants.FONT_SIZE;
import static ohos.devtools.views.common.LayoutConstants.WINDOW_HEIGHT;
import static ohos.devtools.views.common.LayoutConstants.WINDOW_WIDTH;

/**
 * 主界面
 *
 * @version 1.0
 * @date 2021/02/27 11:07
 **/
public class HomeWindow extends JPanel {
    // 菜单栏
    private static JPanel menuPanel = new JPanel(new BorderLayout());

    // 存放二级界面的容器
    private static JPanel taskPanel = new JPanel(new GridLayout());

    private static JMenu jFileMenu = new JMenu("File");

    private static JMenu jViewMenu = new JMenu("View");

    private static JMenu jSetMenu = new JMenu("Setting");

    private static JMenu jHelpMenu = new JMenu("Help");

    private static JMenuItem jNewRealTimeTaskJMenuItem = new JMenuItem("New Task");

    private static JMenuItem jImportFilesJMenuItem = new JMenuItem("Open File");

    private static JMenuItem jSaveTaskJMenuItem = new JMenuItem("Save as");

    private static JMenuItem jQuitJMenuItem = new JMenuItem("Quit");

    private static JMenuItem jLogSwitch = new JMenuItem("Path to Log");

    private HomeWindowEvent homeWindowEvent = new HomeWindowEvent();

    private TaskPanelWelcome taskPanelWelcome = new TaskPanelWelcome();

    /**
     * HomeWindow
     */
    public HomeWindow() {
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        initComponents();
    }

    /**
     * 初始化一级界面
     */
    private void initComponents() {
        JLabel logo = new JLabel("HosProfiler           ", JLabel.RIGHT);
        logo.setIcon(new ImageIcon(HomeWindow.class.getClassLoader().getResource("images/logo.png")));
        logo.setBackground(ColorConstants.HOME_PANE);
        logo.setForeground(Color.WHITE);
        Font fontZhu = new Font(Font.DIALOG, Font.BOLD, FONT_SIZE);
        logo.setFont(fontZhu);
        jFileMenu.add(jNewRealTimeTaskJMenuItem);
        jSetMenu.add(jLogSwitch);
        JMenuBar jMenuBar = new JMenuBar();
        JLabel logoKong = new JLabel("         ", JLabel.RIGHT);
        jMenuBar.setFont(fontZhu);
        jMenuBar.add(logoKong);
        jMenuBar.add(logo);
        jMenuBar.add(jSetMenu);
        menuPanel.add(jMenuBar);
        jFileMenu.setBackground(ColorConstants.TOP_COLOR);
        jSetMenu.setBackground(ColorConstants.TOP_COLOR);
        jMenuBar.setBackground(ColorConstants.TOP_COLOR);
        menuPanel.setPreferredSize(new Dimension(LayoutConstants.WINDOW_WIDTH, LayoutConstants.SVGWIDTH_FOUR));
        // 将二级界面面板添加到主界面中
        this.add(menuPanel, BorderLayout.NORTH);
        this.add(taskPanel, BorderLayout.CENTER);
        this.setBackground(ColorConstants.CENTER_COLOR);
        homeWindowEvent.clickUpdateLogLevel(this);
        this.addTaskPanel(taskPanelWelcome);
    }

    /**
     * 将二级界面添加到二级界面的面板中
     *
     * @param jPanel 二级界面
     */
    public void addTaskPanel(JPanel jPanel) {
        taskPanel.add(jPanel);
        if (jPanel instanceof TaskPanelWelcome) {
            taskPanelWelcome = (TaskPanelWelcome) jPanel;
        }
        // 刷新页面
        this.setVisible(true);
    }

    /**
     * getJNewRealTimeTaskJMenuItem
     *
     * @return JMenuItem
     */
    public JMenuItem getJNewRealTimeTaskJMenuItem() {
        return jNewRealTimeTaskJMenuItem;
    }

    /**
     * getJLogSwitch
     *
     * @return JMenuItem
     */
    public static JMenuItem getJLogSwitch() {
        return jLogSwitch;
    }

    /**
     * getTaskPanel
     *
     * @return JPanel
     */
    public JPanel getTaskPanel() {
        return taskPanel;
    }

    /**
     * getTaskPanelWelcome
     *
     * @return TaskPanelWelcome
     */
    public TaskPanelWelcome getTaskPanelWelcome() {
        return taskPanelWelcome;
    }
}
