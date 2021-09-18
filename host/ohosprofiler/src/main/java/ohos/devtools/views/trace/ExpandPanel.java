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

package ohos.devtools.views.trace;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ExpandPanel
 *
 * @date: 2021/5/19 16:39
 */
public class ExpandPanel extends JBPanel {
    private JComponent myContent;
    private Icon myExpandIcon;
    private Icon myCollapseIcon;
    private JButton myToggleCollapseButton;
    private JBLabel myTitleLabel;
    private boolean myIsInitialized;
    private boolean myIsCollapsed;

    /**
     * Constructor
     *
     * @param title title
     */
    public ExpandPanel(String title) {
        super(new MigLayout("insets 0", "0[grow,fill]0", "0[grow,fill]0"));
        myContent = new JBPanel();
        myContent.setLayout(new MigLayout("insets 0,wrap 1", "0[]0", "0[]0"));
        myExpandIcon = AllIcons.General.ArrowRight;
        myCollapseIcon = AllIcons.General.ArrowDown;
        setBackground(JBColor.background().brighter());
        myToggleCollapseButton = new JButton();
        myToggleCollapseButton.setBorderPainted(false);
        myToggleCollapseButton.setContentAreaFilled(false);
        myToggleCollapseButton.setBackground(new Color(0, 0, 0, 0));
        myToggleCollapseButton.setMaximumSize(new Dimension(myExpandIcon.getIconWidth(), myExpandIcon.getIconHeight()));
        myToggleCollapseButton.setFocusable(true);
        add(myToggleCollapseButton, "split 2 ,w 5!,gapleft 5,gapright 5");
        if (title != null) {
            myTitleLabel = new JBLabel(title);
            myTitleLabel.setToolTipText(title);
            add(myTitleLabel, "h 30!,align center,wrap");
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
     * @param title title
     * @param labelWidth labelWidth
     */
    public ExpandPanel(String title, int labelWidth) {
        super(new MigLayout("insets 0", "0[" + labelWidth + "!]0[grow,fill]0", "0[grow,fill]0"));
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
            JBPanel rPanel = new JBPanel();
            add(myTitleLabel, "h 30!,w " + (labelWidth - 15) + "!,align left");
            add(rPanel, "h 30!,pushx,growx,wrap");
            rPanel.setBackground(JBColor.background().darker());
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
    public ExpandPanel(JComponent content, boolean isCollapsed, Icon collapseIcon, Icon expandIcon, String title) {
        super(new MigLayout("insets 0", "0[grow,fill]0", "0[grow,fill]0"));
        myContent = content;
        myExpandIcon = expandIcon;
        myCollapseIcon = collapseIcon;
        setBackground(JBColor.background().brighter());
        myToggleCollapseButton = new JButton();
        myToggleCollapseButton.setBorderPainted(false);
        myToggleCollapseButton.setContentAreaFilled(false);
        myToggleCollapseButton.setBackground(new Color(0, 0, 0, 0));
        myToggleCollapseButton.setMaximumSize(new Dimension(myExpandIcon.getIconWidth(), myExpandIcon.getIconHeight()));
        myToggleCollapseButton.setFocusable(true);
        add(myToggleCollapseButton, "split 2,w 5!,gapleft 5,gapright 5");
        if (title != null) {
            myTitleLabel = new JBLabel(title);
            add(myTitleLabel, "h 30!,align center,wrap");
        }
        myToggleCollapseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setCollapsed(!myIsCollapsed);
            }
        });
        setCollapsed(isCollapsed);
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
     * add TraceRow
     *
     * @param row row
     */
    public void addTraceRow(AbstractRow row) {
        myContent.add(row, "pushx,growx,h 30::");
    }

    /**
     * add TraceRow
     *
     * @param row row
     * @param constraints constraints
     */
    public void addTraceRow(AbstractRow row, String constraints) {
        myContent.add(row, constraints);
    }

    /**
     * add component
     *
     * @param component component
     * @param constraints constraints
     */
    public void addRow(JComponent component, String constraints) {
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
                if (component instanceof AbstractRow) {
                    ((AbstractRow) component).refresh(startNS, endNS);
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
