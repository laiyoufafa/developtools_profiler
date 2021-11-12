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

package ohos.devtools.views.distributed.component;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.trace.ExpandPanel;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * DeviceExpandPanel
 *
 * @since 2021/5/19 16:39
 */
public class DeviceExpandPanel extends JBPanel {
    private JComponent myContent;
    private Icon myExpandIcon;
    private Icon myCollapseIcon;
    private JButton myToggleCollapseButton;
    private JBLabel myTitleLabel;
    private boolean myIsInitialized;
    private boolean myIsCollapsed;

    // A library or B library
    private String dbFlag;

    /**
     * Constructor
     *
     * @param title title
     * @param dbFlag dbFlag
     */
    public DeviceExpandPanel(String title, String dbFlag) {
        super(new MigLayout("insets 0", "0[115!]0[grow,fill]0", "0[grow,fill]0"));
        this.dbFlag = dbFlag;
        myContent = new JBPanel();
        myContent.setLayout(new MigLayout("insets 0,wrap 1", "0[]0", "0[]0"));
        myExpandIcon = AllIcons.General.ArrowRight;
        myCollapseIcon = AllIcons.General.ArrowDown;
        myToggleCollapseButton = new JButton();
        myToggleCollapseButton.setBorderPainted(false);
        myToggleCollapseButton.setContentAreaFilled(false);
        myToggleCollapseButton.setBackground(new Color(0, 0, 0, 0));
        myToggleCollapseButton.setMaximumSize(new Dimension(myExpandIcon.getIconWidth(), myExpandIcon.getIconHeight()));
        myToggleCollapseButton.setFocusable(true);
        add(myToggleCollapseButton, "split 2,w 5!,gapleft 5,gapright 5");
        if (title != null) {
            myTitleLabel = new JBLabel(title);
            myTitleLabel.setIcon(AllIcons.Nodes.Controller);
            myTitleLabel.setToolTipText(title);
            myTitleLabel.setBackground(JBColor.foreground());
            add(myTitleLabel, "h 30!,w 95!,align left");
            JBPanel jbLabel = new JBPanel();
            jbLabel.setBackground(JBColor.background().darker());
            add(jbLabel, "h 30!,wrap,growx,pushx");
        }
        myToggleCollapseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setCollapsed(!myIsCollapsed);
            }
        });
        setCollapsed(false);
    }

    /**
     * Constructor
     *
     * @param content content
     * @param isCollapsed isCollapsed
     * @param collapseIcon collapseIcon
     * @param expandIcon expandIcon
     * @param title title
     */
    public DeviceExpandPanel(JComponent content, boolean isCollapsed, Icon collapseIcon, Icon expandIcon,
        String title) {
        super(new MigLayout("insets 0", "0[115!]0[grow,fill]0", "0[grow,fill]0"));
        myContent = content;
        myExpandIcon = expandIcon;
        myCollapseIcon = collapseIcon;
        myToggleCollapseButton = new JButton();
        myToggleCollapseButton.setBorderPainted(false);
        myToggleCollapseButton.setContentAreaFilled(false);
        myToggleCollapseButton.setBackground(new Color(0, 0, 0, 0));
        myToggleCollapseButton.setMaximumSize(new Dimension(myExpandIcon.getIconWidth(), myExpandIcon.getIconHeight()));
        myToggleCollapseButton.setFocusable(true);
        add(myToggleCollapseButton, "split 2,w 5!,gapleft 5,gapright 5");
        if (title != null) {
            myTitleLabel = new JBLabel(title);
            myTitleLabel.setIcon(AllIcons.Nodes.Method);
            add(myTitleLabel, "h 30!,w 100!,align center,wrap");
            myTitleLabel.setBackground(JBColor.background().darker());
        }
        myToggleCollapseButton.addActionListener(actionEvent -> setCollapsed(!myIsCollapsed));
        setCollapsed(isCollapsed);
    }

    /**
     * get db flag
     *
     * @return String string
     */
    public String getDbFlag() {
        return dbFlag;
    }

    /**
     * get title
     *
     * @return title text
     */
    public String getTitle() {
        return myTitleLabel.getText();
    }

    /**
     * set title
     *
     * @param title title
     */
    public void setTitle(String title) {
        myTitleLabel.setText(title);
    }

    /**
     * add component
     *
     * @param component component
     * @param constraints constraints
     */
    public void addRow(ExpandPanel component, String constraints) {
        myContent.add(component, constraints);
    }

    /**
     * get myContent
     *
     * @return myContent
     */
    public JComponent getContent() {
        return myContent;
    }

    /**
     * fresh myContent
     *
     * @param startNS startNS
     * @param endNS endNS
     */
    public void refresh(long startNS, long endNS) {
        if (!myIsCollapsed) {
            for (Component component : myContent.getComponents()) {
                if (component instanceof ExpandPanel) {
                    ((ExpandPanel) component).refresh(startNS, endNS);
                }
            }
        }
    }

    /**
     * is collapsed
     *
     * @return isCollapsed
     */
    public boolean isCollapsed() {
        return myIsCollapsed;
    }

    private void setCollapsed(boolean isCollapsed) {
        try {
            if (isCollapsed) {
                if (myIsInitialized) {
                    remove(myContent);
                }
            } else {
                add(myContent, "span 2 1,pushx,growx,wrap");
            }
            myIsCollapsed = isCollapsed;
            Icon icon = myIsCollapsed ? myExpandIcon : myCollapseIcon;
            if (icon != null) {
                myToggleCollapseButton.setIcon(icon);
                myToggleCollapseButton.setBorder(null);
                myToggleCollapseButton.setBorderPainted(false);
            }
            if (isCollapsed) {
                myToggleCollapseButton.requestFocusInWindow();
                myToggleCollapseButton.setSelected(true);
            } else {
                myContent.requestFocusInWindow();
            }
            revalidate();
            repaint();
        } finally {
            myIsInitialized = true;
        }
    }
}
