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

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.hilog.HiLogPanel;
import ohos.devtools.views.layout.dialog.HelpContentsDialog;
import ohos.devtools.views.layout.dialog.HelpDialog;
import ohos.devtools.views.layout.utils.EventTrackUtils;
import ohos.devtools.views.layout.utils.OpenFileDialogUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager.isInfoEnabled;
import static ohos.devtools.views.common.LayoutConstants.WINDOW_HEIGHT;
import static ohos.devtools.views.common.LayoutConstants.WINDOW_WIDTH;

/**
 * HomePanel
 *
 * @since 2021/10/26
 */
public class HomePanel extends JBPanel implements ActionListener, MouseListener {
    /**
     * taskIsOpen
     */
    private static boolean taskIsOpen = false;

    private static final Logger LOGGER = LogManager.getLogger(HomePanel.class);
    private static final String LOG_SWITCH_STR = "Path to Log";
    private static final String FILE_MENU_STR = "  File  ";
    private static final String NEW_TASK_STR = "New Task";
    private static final String OPEN_FILE_STR = "Open File";
    private static final String SAVE_AS_STR = "Save as";
    private static final String QUIT_STR = "Quit";
    private static final String SETTING_STR = "Setting";
    private static final String HILOG = "HiLog";
    private static final String HELP = "Help";
    private static final String ABOUT = "About";
    private static final String HELP_CONTENTS = "Help Contents";

    private JBPanel menuPanel;
    private WelcomePanel welcomePanel;
    private JBPanel containerPanel;
    private JMenu fileMenu;
    private JMenu settingMenu;
    private JMenu hiLogMenu;
    private JMenu helpMenu;
    private JBMenuItem newTaskItem;
    private JBMenuItem openFileItem;
    private JBMenuItem saveAsItem;
    private JBMenuItem quitItem;
    private JBMenuItem aboutItem;
    private JBMenuItem helpContentsItem;
    private final Icon selected = IconLoader.getIcon("/images/icon_radio_selected.png", HomePanel.class);
    private final Icon unselected = IconLoader.getIcon("/images/icon_radio_normal.png", HomePanel.class);
    private JBMenuItem offLog;
    private JBMenuItem errorLog;
    private JBMenuItem warnLog;
    private JBMenuItem infoLog;
    private JBMenuItem debugLog;
    private List<JBMenuItem> menuItems = new ArrayList<>();

    /**
     * HomePanel
     */
    public HomePanel() {
        EventTrackUtils.getInstance().trackWelcomePage();
        initComponents();
    }

    /**
     * init Components
     */
    private void initComponents() {
        if (isInfoEnabled()) {
            LOGGER.info("initComponents");
        }
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        menuPanel = new JBPanel(new BorderLayout());
        menuPanel.setName("menuPanel");
        menuPanel.setBackground(JBColor.background().brighter());
        containerPanel = new JBPanel(new GridLayout());
        welcomePanel = new WelcomePanel();
        // init fileMenu
        fileMenu = new JMenu(FILE_MENU_STR);
        newTaskItem = new JBMenuItem(NEW_TASK_STR);
        openFileItem = new JBMenuItem(OPEN_FILE_STR);
        saveAsItem = new JBMenuItem(SAVE_AS_STR);
        quitItem = new JBMenuItem(QUIT_STR);
        fileMenu.add(openFileItem);
        fileMenu.setIcon(IconLoader.getIcon("/images/file.png", getClass()));
        // init settingMenu
        settingMenu = new JMenu(SETTING_STR);
        settingMenu.setIcon(AllIcons.Actions.InlayGear);
        aboutItem = new JBMenuItem(ABOUT);
        helpContentsItem = new JBMenuItem(HELP_CONTENTS);
        hiLogMenu = new JMenu(HILOG);
        hiLogMenu.setIcon(AllIcons.Actions.Copy);
        hiLogMenu.setName(UtConstant.UT_HOME_PANEL_HILOG_MENU);
        helpMenu = new JMenu(HELP);
        helpMenu.setIcon(IconLoader.getIcon("/images/help.png", getClass()));
        helpMenu.setName(UtConstant.UT_HOME_PANEL_HELP_MENU);
        initSettingMenu();
        JMenuBar settingMenuBar = new JMenuBar();
        settingMenuBar.add(fileMenu);
        settingMenuBar.add(hiLogMenu);
        settingMenuBar.add(settingMenu);
        settingMenuBar.add(helpMenu);
        helpMenu.add(aboutItem);
        helpMenu.add(helpContentsItem);
        // MenuPanel set
        menuPanel.add(settingMenuBar);
        menuPanel.setPreferredSize(new Dimension(LayoutConstants.WINDOW_WIDTH, LayoutConstants.THIRTY));
        containerPanel.add(welcomePanel);
        add(menuPanel, BorderLayout.NORTH);
        add(containerPanel, BorderLayout.CENTER);
        newTaskItem.addActionListener(this);
        openFileItem.addActionListener(this);
        aboutItem.addActionListener(this);
        helpContentsItem.addActionListener(this);
        hiLogMenu.addMouseListener(this);
    }

    private void initSettingMenu() {
        offLog = new JBMenuItem("OFF", selected);
        offLog.setPreferredSize(new Dimension(80, 30));
        menuItems.add(offLog);
        errorLog = new JBMenuItem("Error", unselected);
        errorLog.setPreferredSize(new Dimension(80, 30));
        menuItems.add(errorLog);
        warnLog = new JBMenuItem("Warn", unselected);
        warnLog.setPreferredSize(new Dimension(80, 30));
        menuItems.add(warnLog);
        infoLog = new JBMenuItem("Info", unselected);
        infoLog.setPreferredSize(new Dimension(80, 30));
        menuItems.add(infoLog);
        debugLog = new JBMenuItem("Debug", unselected);
        debugLog.setPreferredSize(new Dimension(80, 30));
        menuItems.add(debugLog);
        JMenu logLevelMenu = new JMenu("Level to Log");
        logLevelMenu.setPreferredSize(new Dimension(100, 30));
        settingMenu.add(logLevelMenu);
        logLevelMenu.add(offLog);
        logLevelMenu.add(errorLog);
        logLevelMenu.add(warnLog);
        logLevelMenu.add(infoLog);
        logLevelMenu.add(debugLog);
        offLog.addActionListener(this);
        errorLog.addActionListener(this);
        warnLog.addActionListener(this);
        infoLog.addActionListener(this);
        debugLog.addActionListener(this);
    }

    private void updateIcon(String selectMenuItem) {
        menuItems.forEach(menuItem -> {
            if (menuItem.getText().equals(selectMenuItem)) {
                menuItem.setIcon(selected);
            } else {
                menuItem.setIcon(unselected);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String actionCommand = actionEvent.getActionCommand();
        // switch log
        handleLogLevel(actionCommand);
        // new task
        if (actionCommand.equals(NEW_TASK_STR)) {
            if (Constant.jtasksTab == null || Constant.jtasksTab.getTabCount() == 0) {
                Constant.jtasksTab = new JBTabbedPane();
            }
            new TaskPanel(containerPanel, welcomePanel);
            welcomePanel.setVisible(false);
        }
        // open file
        if (actionCommand.equals(OPEN_FILE_STR)) {
            if (Constant.jtasksTab == null || Constant.jtasksTab.getTabCount() == 0) {
                Constant.jtasksTab = new JBTabbedPane();
            }
            TaskPanel taskPanel = null;
            Component[] components1 = containerPanel.getComponents();
            for (Component item : components1) {
                if (item instanceof TaskPanel) {
                    // filter TaskPanel
                    taskPanel = (TaskPanel) item;
                }
            }
            if (Objects.isNull(taskPanel)) {
                taskPanel = new TaskPanel(containerPanel, welcomePanel);
            }
            OpenFileDialogUtils.getInstance().showFileOpenDialog(taskPanel.getTabItem(), taskPanel);
            welcomePanel.setVisible(false);
        }
        if (actionCommand.equals(ABOUT)) {
            EventTrackUtils.getInstance().trackHelp();
            new HelpDialog();
        }
        // help contents
        if (actionCommand.equals(HELP_CONTENTS)) {
            new HelpContentsDialog().show();
        }
    }

    private void handleLogLevel(String actionCommand) {
        switch (actionCommand) {
            case "OFF":
                ProfilerLogManager.updateLogLevel(Level.OFF);
                updateIcon(actionCommand);
                break;
            case "Error":
                ProfilerLogManager.updateLogLevel(Level.ERROR);
                updateIcon(actionCommand);
                break;
            case "Warn":
                ProfilerLogManager.updateLogLevel(Level.WARN);
                updateIcon(actionCommand);
                break;
            case "Info":
                ProfilerLogManager.updateLogLevel(Level.INFO);
                updateIcon(actionCommand);
                break;
            case "Debug":
                ProfilerLogManager.updateLogLevel(Level.DEBUG);
                updateIcon(actionCommand);
                break;
            default:
                ProfilerLogManager.updateLogLevel(Level.ERROR);
                updateIcon("Error");
                break;
        }
    }

    /**
     * getContainerPanel
     *
     * @return JPanel
     */
    public JBPanel getContainerPanel() {
        return containerPanel;
    }

    /**
     * mouseClicked
     *
     * @param event MouseEvent
     */
    @Override
    public void mouseClicked(MouseEvent event) {
        if (event.getComponent().getName().equals(UtConstant.UT_HOME_PANEL_HILOG_MENU)) {
            if (Constant.jtasksTab == null || (Constant.jtasksTab != null && Constant.jtasksTab.getTabCount() == 0)) {
                Constant.jtasksTab = new JBTabbedPane();
            }
            if (!HiLogPanel.isIsOpen()) {
                new HiLogPanel(containerPanel, welcomePanel);
                welcomePanel.setVisible(false);
            }
        }
    }

    /**
     * mousePressed
     *
     * @param event MouseEvent
     */
    @Override
    public void mousePressed(MouseEvent event) {
    }

    /**
     * mouseReleased
     *
     * @param event MouseEvent
     */
    @Override
    public void mouseReleased(MouseEvent event) {
    }

    /**
     * mouseEntered
     *
     * @param event MouseEvent
     */
    @Override
    public void mouseEntered(MouseEvent event) {
    }

    /**
     * mouseExited
     *
     * @param event MouseEvent
     */
    @Override
    public void mouseExited(MouseEvent event) {
    }

    /**
     * isTaskIsOpen
     *
     * @return boolean
     */
    public static boolean isTaskIsOpen() {
        return taskIsOpen;
    }

    /**
     * setTaskIsOpen
     *
     * @param taskIsOpen taskIsOpen
     */
    public static void setTaskIsOpen(boolean taskIsOpen) {
        HomePanel.taskIsOpen = taskIsOpen;
    }
}
