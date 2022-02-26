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

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBLayeredPane;
import com.intellij.ui.components.JBPanel;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.datasources.utils.plugin.service.PlugManager;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.common.customcomp.GraphicsLinePanel;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.utils.EventTrackUtils;
import ohos.devtools.views.layout.utils.OpenFileDialogUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Objects;

import static ohos.devtools.views.common.Constant.DEVICE_REFRESH;

/**
 * TaskPanel
 *
 * @since 2021/11/22
 */
public class TaskPanel extends JBLayeredPane implements MouseListener {
    private static final Logger LOGGER = LogManager.getLogger(TaskPanel.class);
    private static final String TAB_STR = "NewTask-Configure";
    private static final String TAB_CLOSE_STR = "x";
    private static final String TAB_ADD_STR = "+";
    private static final String TASK_SCENE_STR = "Task scene";
    private static final String TASK_SCENE_TIP_STR = "Choose the most suitable scene.";
    private static final String CHOOSE_BTN_STR = "Choose";
    private static final String OPEN_FILE_STR = "Open File";
    private static final String APPLICATION_TITLE_STR = "Application Tuning";
    private static final String SYSTEM_TITLE_STR = "System Tuning";
    private static final String DISTRIBUTED_TITLE_STR = "Distributed Scenario";
    private static final String APPLICATION_TIP_STR = "<html>Application Tuning<br/><br/>"
        + "Use the performance profiler to check the CPU,memory,network and energy status of the application</html>";
    private static final String SYSTEM_TIP_STR = "<html>System Tuning<br/><br/>"
        + "Collect system-wide performance traces from Harmony devices from a variety of data sources</html>";
    private static final String DISTRIBUTED_TIP_STR =
        "<html>Distributed Scenario<br/><br/>" + "Collect performance data for distributed scenarios</html>";
    private static final String GPU_TIP_STR =
        "<html>GPU Counter<br/><br/>" + "Collect performance data for GPU Counter</html>";

    private JBPanel parentPanel;
    private JBPanel welcomePanel;
    private JBPanel tabPanel;
    private JBPanel tabLeftPanel;
    private JBPanel tabRightPanel;
    private JBLabel tabText;
    private JBLabel tabCloseBtn;
    private JButton tabAddBtn;
    private JBPanel tabContainer;
    private JBPanel tabItem;
    private JBLabel taskSceneLabel;
    private JBLabel taskSceneLabelTip;
    private JBLabel applicationBtn;
    private JBLabel systemBtn;
    private JBLabel distributedBtn;
    private JBLabel tipIconLabel;
    private JBLabel tipInfoLabel;
    private JButton chooseBtn;
    private JButton openFileBtn;
    private JBPanel btnPanel;
    private GraphicsLinePanel graphicsLineUp;
    private GraphicsLinePanel graphicsLineDown;
    private Long localSessionId;

    /**
     * Task Panel
     */
    public TaskPanel() {
        super();
    }

    /**
     * Task Panel
     *
     * @param containerPanel JBPanel
     * @param welcomePanel WelcomePanel
     */
    public TaskPanel(JBPanel containerPanel, WelcomePanel welcomePanel) {
        EventTrackUtils.getInstance().trackTaskPage();
        parentPanel = containerPanel;
        this.welcomePanel = welcomePanel;
        initComponents();
        initTab(containerPanel);
        initTaskSceneItems();
        addEventListener();
    }

    /**
     * initComponents
     */
    private void initComponents() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initComponents");
        }
        // init tabPanel
        tabPanel = new JBPanel();
        tabLeftPanel = new JBPanel();
        tabRightPanel = new JBPanel();
        tabRightPanel.setName(UtConstant.UT_TASK_PANEL_CLOSE);
        tabText = new JBLabel(TAB_STR);
        tabCloseBtn = new JBLabel(TAB_CLOSE_STR);
        tabAddBtn = new JButton(TAB_ADD_STR);
        // init tab
        tabContainer = new JBPanel(new BorderLayout());
        tabContainer.setOpaque(true);
        tabContainer.setBackground(JBColor.background().darker());
        tabItem = new JBPanel();
        // init items
        taskSceneLabel = new JBLabel(TASK_SCENE_STR);
        taskSceneLabelTip = new JBLabel(TASK_SCENE_TIP_STR);
        applicationBtn = new JBLabel(IconLoader.getIcon("/images/application_tuning.png", getClass()));
        systemBtn = new JBLabel(IconLoader.getIcon("/images/system_tuning.png", getClass()));
        distributedBtn = new JBLabel(IconLoader.getIcon("/images/distributed_scenario.png", getClass()));
        tipIconLabel = new JBLabel(IconLoader.getIcon("/images/application_tuning.png", getClass()));
        tipInfoLabel = new JBLabel(APPLICATION_TIP_STR);
        graphicsLineUp = new GraphicsLinePanel();
        graphicsLineDown = new GraphicsLinePanel();
        chooseBtn = new JButton(CHOOSE_BTN_STR);
        openFileBtn = new JButton(OPEN_FILE_STR);
    }

    /**
     * setTabAttributes
     *
     * @param containerPanel containerPanel
     */
    private void initTab(JBPanel containerPanel) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initTab");
        }
        setPanelData();
        containerPanel.setLayout(new BorderLayout());
        tabAddBtn.setFont(new Font(Font.DIALOG, Font.PLAIN, LayoutConstants.DEVICES_HEIGHT));
        tabAddBtn.setBorderPainted(false);
        tabAddBtn.setBounds(LayoutConstants.NUMBER_X_ADD * Constant.jtasksTab.getTabCount(), LayoutConstants.NUMBER_Y,
            LayoutConstants.BUTTON_HEIGHT, LayoutConstants.BUTTON_HEIGHT);
        Constant.jtasksTab.setBounds(0, 0, containerPanel.getWidth(), containerPanel.getHeight());
        this.add(Constant.jtasksTab);
        containerPanel.add(this);
        double result = Constant.jtasksTab.getTabCount() * LayoutConstants.NUMBER_X;
        if (result > containerPanel.getWidth()) {
            for (int index = 0; index < Constant.jtasksTab.getTabCount(); index++) {
                Object tabObj = Constant.jtasksTab.getTabComponentAt(index);
                if (tabObj instanceof JBPanel) {
                    ((JBPanel) tabObj).getComponents()[0].setPreferredSize(new Dimension(
                        (((containerPanel.getWidth() - LayoutConstants.MEMORY_WIDTH) / Constant.jtasksTab.getTabCount())
                            - LayoutConstants.TASK_DEC_NUM) - LayoutConstants.JAVA_HEIGHT,
                        LayoutConstants.JAVA_HEIGHT));
                }
                Constant.jtasksTab.getTabComponentAt(index).setPreferredSize(new Dimension(
                    ((containerPanel.getWidth() - LayoutConstants.MEMORY_WIDTH) / Constant.jtasksTab.getTabCount())
                        - LayoutConstants.TASK_DEC_NUM, LayoutConstants.JAVA_HEIGHT));
                tabAddBtn.setBounds(containerPanel.getWidth() - LayoutConstants.TASK_LABEL_X, LayoutConstants.NUMBER_Y,
                    LayoutConstants.BUTTON_HEIGHT, LayoutConstants.BUTTON_HEIGHT);
            }
        }
    }

    /**
     * setPanelData
     */
    private void setPanelData() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("setPanelData");
        }
        tabItem.setLayout(new MigLayout("insets 0", "[grow,fill]", "15[fill,fill]20[]push[][][]20[]"));
        tabPanel.setOpaque(false);
        tabPanel.setPreferredSize(new Dimension(LayoutConstants.JPA_LABEL_WIDTH, LayoutConstants.DEVICES_HEIGHT));
        tabLeftPanel.setOpaque(false);
        tabLeftPanel.setLayout(null);
        tabLeftPanel.setPreferredSize(new Dimension(LayoutConstants.JP_LEFT_WIDTH, LayoutConstants.JP_LEFT_HEIGHT));
        tabRightPanel.setOpaque(false);
        tabRightPanel.setLayout(new GridLayout());
        tabRightPanel.setPreferredSize(new Dimension(LayoutConstants.JP_RIGHT_WIDTH, LayoutConstants.JP_RIGHT_HEIGHT));
        tabPanel.setLayout(new BorderLayout());
        tabPanel.add(tabLeftPanel, BorderLayout.WEST);
        tabPanel.add(tabRightPanel, BorderLayout.CENTER);
        tabText.setBounds(0, 0, LayoutConstants.JP_SET_WIDTH, LayoutConstants.JP_SET_HEIGHT);
        tabLeftPanel.add(tabText);
        tabRightPanel.add(tabCloseBtn);
        tabCloseBtn.setHorizontalAlignment(JBLabel.RIGHT);
        tabCloseBtn.setName(TAB_CLOSE_STR);
        tipIconLabel
            .setBounds(LayoutConstants.DEVICES_X, LayoutConstants.DESCRIPTION_NUMBER, LayoutConstants.HIGTHSCEECS,
                LayoutConstants.HIGTHSCEECS);
        tipInfoLabel
            .setBounds(LayoutConstants.JAVA_WIDTH, LayoutConstants.DESCRIPTION_NUMBER, LayoutConstants.APP_LABEL_WIDTH,
                LayoutConstants.JLABEL_SIZE);
        Font fontTaskTun = new Font(Font.DIALOG, Font.BOLD, LayoutConstants.TUN_LABEL_FONT);
        tipInfoLabel.setFont(fontTaskTun);
        tabContainer.add(tabItem);
        Constant.jtasksTab.addTab("TaskTab", tabContainer);
        Constant.jtasksTab.setTabComponentAt(Constant.jtasksTab.indexOfComponent(tabContainer), tabPanel);
    }

    /**
     * init TaskScene Items
     */
    private void initTaskSceneItems() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initTaskSceneItems");
        }
        setButtonAttr();
        setApplicationBtnData();
        setSystemBtnData();
        distributedBtn.setText(DISTRIBUTED_TITLE_STR);
        distributedBtn.setName(UtConstant.UT_TASK_PANEL_DISTRIBUTED);
        distributedBtn.setPreferredSize(new Dimension(210, 155));
        distributedBtn.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        distributedBtn.setVerticalTextPosition(JBLabel.BOTTOM);
        distributedBtn.setHorizontalTextPosition(JBLabel.CENTER);
        distributedBtn.setOpaque(true);
        distributedBtn.setBackground(JBColor.background());
        JBPanel taskScenePanel = new JBPanel(new MigLayout("insets 0"));
        taskScenePanel.setOpaque(false);
        taskScenePanel.add(taskSceneLabel, "gap 15");
        taskScenePanel.add(taskSceneLabelTip);
        JBPanel sceneButtonPanel = new JBPanel(new MigLayout("insets 0"));
        sceneButtonPanel.add(applicationBtn, "gap 15");
        sceneButtonPanel.add(systemBtn, "gap 15");
        sceneButtonPanel.setOpaque(false);
        sceneButtonPanel.add(distributedBtn, "gap 15");
        JBPanel tipPanel = new JBPanel(new MigLayout("insets 0"));
        tipPanel.add(tipIconLabel, "gap 30");
        tipPanel.add(tipInfoLabel);
        tipPanel.setOpaque(false);
        btnPanel = new JBPanel(new MigLayout("insets 0", "push[]15[]15", "[fill, fill]"));
        btnPanel.add(openFileBtn);
        btnPanel.add(chooseBtn);
        btnPanel.setOpaque(false);
        tabItem.add(taskScenePanel, "wrap, height 30!");
        tabItem.add(sceneButtonPanel, "wrap, height 150!");
        tabItem.add(graphicsLineUp, "wrap, height 10!, gapx 15 15");
        tabItem.add(tipPanel, "wrap");
        tabItem.add(graphicsLineDown, "wrap, height 10!, gapy 10, gapx 15 15");
        tabItem.add(btnPanel, "wrap, span, height 40!");
        tabItem.setOpaque(true);
        tabItem.setBackground(JBColor.background().darker());
    }

    /**
     * setSystemBtnData
     */
    private void setSystemBtnData() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("setSystemBtnData");
        }
        systemBtn.setText(SYSTEM_TITLE_STR);
        systemBtn.setName(UtConstant.UT_TASK_PANEL_SYSTEM);
        systemBtn.setPreferredSize(new Dimension(210, 155));
        systemBtn.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        systemBtn.setVerticalTextPosition(JBLabel.BOTTOM);
        systemBtn.setHorizontalTextPosition(JBLabel.CENTER);
        systemBtn.setOpaque(true);
        systemBtn.setBackground(JBColor.background());
    }

    /**
     * setApplicationBtnData
     */
    private void setApplicationBtnData() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("setApplicationBtnData");
        }
        taskSceneLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
        taskSceneLabel.setForeground(JBColor.foreground().brighter());
        taskSceneLabelTip.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        applicationBtn.setText(APPLICATION_TITLE_STR);
        applicationBtn.setName(UtConstant.UT_TASK_PANEL_APPLICATION);
        applicationBtn.setPreferredSize(new Dimension(210, 155));
        applicationBtn.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        applicationBtn.setVerticalTextPosition(JBLabel.BOTTOM);
        applicationBtn.setHorizontalTextPosition(JBLabel.CENTER);
        applicationBtn.setOpaque(true);
        applicationBtn.setBackground(JBColor.background());
        applicationBtn.setForeground(JBColor.foreground().brighter());
        applicationBtn.setBorder(BorderFactory.createLineBorder(new Color(0, 117, 255), 2));
    }

    /**
     * addEventListener
     */
    private void addEventListener() {
        tabCloseBtn.addMouseListener(this);
        applicationBtn.addMouseListener(this);
        systemBtn.addMouseListener(this);
        distributedBtn.addMouseListener(this);
        openFileBtn.addMouseListener(this);
        chooseBtn.addMouseListener(this);
    }

    /**
     * setButtonAttr
     */
    private void setButtonAttr() {
        chooseBtn.setName(UtConstant.UT_TASK_PANEL_CHOOSE);
        chooseBtn.setFont(new Font(Font.DIALOG, Font.PLAIN, LayoutConstants.OPTION_FONT));
        chooseBtn.setFocusPainted(false);
        chooseBtn.setOpaque(false);
        chooseBtn.setPreferredSize(new Dimension(140, 40));
        openFileBtn.setName(UtConstant.UT_TASK_PANEL_OPEN_FILE);
        openFileBtn.setFont(new Font(Font.DIALOG, Font.PLAIN, LayoutConstants.OPTION_FONT));
        openFileBtn.setOpaque(false);
        openFileBtn.setFocusPainted(false);
        openFileBtn.setPreferredSize(new Dimension(140, 40));
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        String name = mouseEvent.getComponent().getName();
        if (name.equals(APPLICATION_TITLE_STR)) {
            tipInfoLabel.setText(APPLICATION_TIP_STR);
            tipIconLabel.setIcon(IconLoader.getIcon("/images/application_tuning.png", getClass()));
            applicationBtn.setBorder(BorderFactory.createLineBorder(new Color(0, 117, 255), 2));
            systemBtn.setBorder(null);
            distributedBtn.setBorder(null);
            applicationBtn.setForeground(JBColor.foreground().brighter());
            systemBtn.setForeground(JBColor.foreground());
            distributedBtn.setForeground(JBColor.foreground());
        }
        if (name.equals(SYSTEM_TITLE_STR)) {
            tipInfoLabel.setText(SYSTEM_TIP_STR);
            tipIconLabel.setIcon(IconLoader.getIcon("/images/system_tuning.png", getClass()));
            applicationBtn.setBorder(null);
            systemBtn.setBorder(BorderFactory.createLineBorder(new Color(0, 117, 255), 2));
            distributedBtn.setBorder(null);
            applicationBtn.setForeground(JBColor.foreground());
            systemBtn.setForeground(JBColor.foreground().brighter());
            distributedBtn.setForeground(JBColor.foreground());
        }
        if (name.equals(DISTRIBUTED_TITLE_STR)) {
            tipInfoLabel.setText(DISTRIBUTED_TIP_STR);
            tipIconLabel.setIcon(IconLoader.getIcon("/images/distributed_scenario.png", getClass()));
            applicationBtn.setBorder(null);
            systemBtn.setBorder(null);
            distributedBtn.setBorder(BorderFactory.createLineBorder(new Color(0, 117, 255), 2));
            applicationBtn.setForeground(JBColor.foreground());
            systemBtn.setForeground(JBColor.foreground());
            distributedBtn.setForeground(JBColor.foreground().brighter());
        }
        mouseReleasedExtra(name);
    }

    /**
     * mouseReleasedExtra
     *
     * @param name name
     */
    private void mouseReleasedExtra(String name) {
        if (name.equals(CHOOSE_BTN_STR)) {
            chooseBtn.dispatchEvent(new FocusEvent(chooseBtn, FocusEvent.FOCUS_GAINED, true));
            chooseBtn.requestFocusInWindow();
            tabItem.setVisible(false);
            setTabContainer();
            tabContainer.repaint();
        }
        if (name.equals(OPEN_FILE_STR)) {
            OpenFileDialogUtils.getInstance().showFileOpenDialog(tabItem, this);
        }
        if (name.equals(TAB_CLOSE_STR)) {
            removeAll();
            HomePanel.setTaskIsOpen(false);
            Constant.jtasksTab.remove(Constant.jtasksTab.indexOfTabComponent(tabPanel));
            add(Constant.jtasksTab);
            parentPanel.add(this);
            parentPanel.repaint();
            Constant.jtasksTab.updateUI();
            if (Constant.jtasksTab.getTabCount() == 0) {
                if (localSessionId != null && localSessionId != 0L) {
                    SessionManager sessionManager = SessionManager.getInstance();
                    sessionManager.deleteSession(localSessionId);
                }
                removeAll();
                Constant.jtasksTab = null;
                Component[] components = parentPanel.getComponents();
                for (Component item : components) {
                    if (item instanceof TaskPanel) {
                        parentPanel.remove(item);
                    }
                }
                welcomePanel.setVisible(true);
            }
            QuartzManager.getInstance().deleteExecutor(DEVICE_REFRESH);
            PlugManager.getInstance().clearProfilerMonitorItemMap();
            ProfilerChartsView profilerChartsView = ProfilerChartsView.sessionMap.get(localSessionId);
            if (Objects.nonNull(profilerChartsView)) {
                profilerChartsView.getPublisher().stopRefresh(false);
            }
        }
    }

    /**
     * setTabContainer
     */
    private void setTabContainer() {
        if (tipInfoLabel.getText().contains(APPLICATION_TITLE_STR)) {
            ApplicationConfigPanel taskScenePanel = new ApplicationConfigPanel(this);
            tabContainer.setLayout(new BorderLayout());
            tabContainer.setOpaque(true);
            tabContainer.setBackground(JBColor.background().darker());
            tabContainer.add(taskScenePanel);
        } else if (tipInfoLabel.getText().contains(SYSTEM_TITLE_STR)) {
            SystemConfigPanel configPanel = new SystemConfigPanel(this);
            tabContainer.setLayout(new BorderLayout());
            tabContainer.setOpaque(true);
            tabContainer.setBackground(JBColor.background().darker());
            tabContainer.add(configPanel);
        } else if (tipInfoLabel.getText().contains(DISTRIBUTED_TITLE_STR)) {
            DistributedConfigPanel configPanel = new DistributedConfigPanel(this);
            tabContainer.setLayout(new BorderLayout());
            tabContainer.setOpaque(true);
            tabContainer.setBackground(JBColor.background().darker());
            tabContainer.add(configPanel);
        } else {
            // Distributed Scenario
            tabItem.setVisible(true);
        }
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
    }

    public JBLabel getTabCloseBtn() {
        return tabCloseBtn;
    }

    /**
     * getChooseButton
     *
     * @return JButton
     */
    public JButton getChooseButton() {
        return chooseBtn;
    }

    public JBPanel getTabContainer() {
        return tabContainer;
    }

    public JBPanel getTabItem() {
        return tabItem;
    }

    public JBPanel getTabRightPanel() {
        return tabRightPanel;
    }

    public JBPanel getTabLeftPanel() {
        return tabLeftPanel;
    }

    public JButton getTabAddBtn() {
        return tabAddBtn;
    }

    public JBPanel getBtnPanel() {
        return btnPanel;
    }

    public void setLocalSessionId(Long localSessionId) {
        this.localSessionId = localSessionId;
    }

    public Long getLocalSessionId() {
        return localSessionId;
    }
}
