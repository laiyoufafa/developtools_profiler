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
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.common.Constant;
import ohos.devtools.views.common.UtConstant;

import javax.swing.SwingConstants;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * WelcomePanel class
 */
public class WelcomePanel extends JBPanel implements MouseListener {
    private static final String NEW_BUTTON_STR = " + New Task";
    private static final String WELCOME_TITLE_STR = "Welcome HosProfiler";
    private static final String WELCOME_TIP_STR = "Click New Task Button to process or load a capture";

    private JBLabel newTaskBtn;
    private JBPanel newTaskBgPanel;
    private JBLabel picImg;
    private JBLabel welcomeTitleLabel;
    private JBLabel welcomeTipLabel;

    /**
     * WelcomePanel
     */
    public WelcomePanel() {
        initComponents();
    }

    /**
     * initComponents
     */
    private void initComponents() {
        // init
        setLayout(new MigLayout("inset 0", "[grow,fill]",
            "[fill,fill]push[]push"));
        // init newTaskBtn
        newTaskBtn = new JBLabel(NEW_BUTTON_STR, JBLabel.CENTER);
        newTaskBtn.setOpaque(true);
        newTaskBtn.setBackground(JBColor.background().darker());
        newTaskBtn.setBounds(20, 6, 117, 20);
        newTaskBtn.setName(UtConstant.UT_WELCOME_PANEL_NEW_TASK_BTN);
        // init newTaskBgPanel
        newTaskBgPanel = new JBPanel(null);
        newTaskBgPanel.setBackground(JBColor.background());
        newTaskBgPanel.add(newTaskBtn);
        // init tip
        picImg = new JBLabel();
        picImg.setIcon(IconLoader.getIcon("/images/pic.png", getClass()));
        picImg.setHorizontalAlignment(SwingConstants.CENTER);
        picImg.setVerticalAlignment(SwingConstants.CENTER);
        welcomeTitleLabel = new JBLabel();
        welcomeTitleLabel.setText(WELCOME_TITLE_STR);
        welcomeTitleLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 24));
        welcomeTitleLabel.setOpaque(false);
        welcomeTitleLabel.setForeground(JBColor.foreground().brighter());
        welcomeTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeTipLabel = new JBLabel();
        welcomeTipLabel.setText(WELCOME_TIP_STR);
        welcomeTipLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        welcomeTipLabel.setForeground(JBColor.foreground().darker());
        welcomeTipLabel.setOpaque(false);
        welcomeTipLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JBPanel containPanel = new JBPanel(new MigLayout("inset 0", "[grow,fill]",
            "[fill,fill]"));
        containPanel.add(picImg, "wrap");
        containPanel.add(welcomeTitleLabel, "wrap, height 30!");
        containPanel.add(welcomeTipLabel, "wrap, height 30!");
        containPanel.setBackground(JBColor.background().darker());
        containPanel.setOpaque(true);
        // add the Component
        add(newTaskBgPanel, "wrap, height 30!");
        add(containPanel, "center");
        setBackground(JBColor.background().darker());
        setOpaque(true);
        newTaskBtn.addMouseListener(this);
    }

    /**
     * mouseClicked
     *
     * @param event MouseEvent
     */
    @Override
    public void mouseClicked(MouseEvent event) {
        Object parentComponent = getParent();
        if (parentComponent instanceof JBPanel) {
            JBPanel containerPanel = (JBPanel) parentComponent;
            if (Constant.jtasksTab == null || Constant.jtasksTab.getTabCount() == 0) {
                Constant.jtasksTab = new JBTabbedPane();
            }
            new TaskPanel(containerPanel, this);
            setVisible(false);
            containerPanel.updateUI();
            containerPanel.repaint();
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
     * getNewTaskBtn
     *
     * @return JBLabel
     */
    public JBLabel getNewTaskBtn() {
        return newTaskBtn;
    }
}
