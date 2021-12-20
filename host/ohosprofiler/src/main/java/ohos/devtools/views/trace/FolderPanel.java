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

import com.intellij.ui.JBColor;
import com.intellij.ui.UtilUiBundle;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.StartupUiUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;

/**
 * FolderPanel
 *
 * @since 2021/5/18 23:18
 */
public class FolderPanel extends JPanel {
    /**
     * LEFT_KEY_STROKE
     */
    public static final KeyStroke LEFT_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);

    /**
     * LEFT_KEY_STROKE
     */
    public static final KeyStroke RIGHT_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);

    /**
     * expand word
     */
    @NonNls
    public static final String EXPAND = "expand";

    /**
     * collapse word
     */
    @NonNls
    public static final String COLLAPSE = "collapse";

    private final JButton myToggleCollapseButton;
    private final JComponent myContent;
    private final Collection<CollapsingListener> myListeners = ContainerUtil.createLockFreeCopyOnWriteList();
    private final Icon myExpandIcon;
    private final Icon myCollapseIcon;
    private boolean myIsCollapsed;
    private boolean myIsInitialized = false;
    private Label myTitleLabel;

    /**
     * structure
     *
     * @param content content
     * @param collapseButtonAtLeft collapseButtonAtLeft
     * @param isCollapsed isCollapsed
     * @param collapseIcon collapseIcon
     * @param expandIcon expandIcon
     * @param title title
     */
    public FolderPanel(JComponent content, boolean collapseButtonAtLeft, boolean isCollapsed, Icon collapseIcon,
        Icon expandIcon, String title) {
        super(new GridBagLayout());
        myContent = content;
        setBackground(content.getBackground());
        myExpandIcon = expandIcon;
        myCollapseIcon = collapseIcon;
        myToggleCollapseButton = new JButton();
        myToggleCollapseButton.setOpaque(false);
        myToggleCollapseButton.setBorderPainted(false);
        myToggleCollapseButton.setBackground(new Color(0, 0, 0, 0));
        final Dimension buttonDimension = getButtonDimension();
        myToggleCollapseButton.setSize(buttonDimension);
        myToggleCollapseButton.setPreferredSize(buttonDimension);
        myToggleCollapseButton.setMinimumSize(buttonDimension);
        myToggleCollapseButton.setMaximumSize(buttonDimension);
        myToggleCollapseButton.setFocusable(true);
        myToggleCollapseButton.getActionMap().put(COLLAPSE, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                collapse();
            }
        });
        myToggleCollapseButton.getActionMap().put(EXPAND, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                expand();
            }
        });
        myToggleCollapseButton.getInputMap().put(LEFT_KEY_STROKE, COLLAPSE);
        myToggleCollapseButton.getInputMap().put(RIGHT_KEY_STROKE, EXPAND);
        final int iconAnchor = collapseButtonAtLeft ? GridBagConstraints.WEST : GridBagConstraints.EAST;
        add(myToggleCollapseButton, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, iconAnchor, GridBagConstraints.CENTER,
            new Insets(0, collapseButtonAtLeft ? 0 : 5, 0, collapseButtonAtLeft ? 0 : 0), 0, 0));
        if (title != null) {
            myTitleLabel = new Label(title);
            myTitleLabel.setFont(StartupUiUtil.getLabelFont().deriveFont(Font.BOLD));
            myTitleLabel.setBackground(content.getBackground());
            add(myTitleLabel,
                new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
                    new Insets(0, 20, 0, 0), 0, 0));
        }
        myToggleCollapseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                setCollapsed(!myIsCollapsed);
            }
        });
        setCollapsed(isCollapsed);
    }

    /**
     * structure
     *
     * @param content content
     * @param collapseButtonAtLeft collapseButtonAtLeft
     */
    public FolderPanel(JComponent content, boolean collapseButtonAtLeft) {
        this(content, collapseButtonAtLeft, false, null, null, null);
    }

    private Dimension getButtonDimension() {
        if (myExpandIcon == null) {
            return new Dimension(7, 7);
        } else {
            return new Dimension(myExpandIcon.getIconWidth(), myExpandIcon.getIconHeight());
        }
    }

    @Nls
    private String getToggleButtonToolTipText() {
        if (myIsCollapsed) {
            return UtilUiBundle.message("collapsible.panel.collapsed.state.tooltip.text");
        } else {
            return UtilUiBundle.message("collapsible.panel.expanded.state.tooltip.text");
        }
    }

    private Icon getIcon() {
        if (myIsCollapsed) {
            return myExpandIcon;
        } else {
            return myCollapseIcon;
        }
    }

    private void notifyListeners() {
        for (CollapsingListener listener : myListeners) {
            listener.onCollapsingChanged(this, isCollapsed());
        }
    }

    /**
     * addCollapsingListener
     *
     * @param listener listener
     */
    public void addCollapsingListener(CollapsingListener listener) {
        myListeners.add(listener);
    }

    /**
     * removeCollapsingListener
     *
     * @param listener listener
     */
    public void removeCollapsingListener(CollapsingListener listener) {
        myListeners.remove(listener);
    }

    /**
     * get myIsCollapsed
     *
     * @return isCollapsed
     */
    public boolean isCollapsed() {
        return myIsCollapsed;
    }

    /**
     * set the collapse
     *
     * @param collapse collapse
     */
    protected void setCollapsed(boolean collapse) {
        try {
            if (collapse) {
                if (myIsInitialized) {
                    remove(myContent);
                }
            } else {
                add(myContent,
                    new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0));
            }
            myIsCollapsed = collapse;
            Icon icon = getIcon();
            if (icon != null) {
                myToggleCollapseButton.setIcon(icon);
                myToggleCollapseButton.setBorder(null);
                myToggleCollapseButton.setBorderPainted(false);
                myToggleCollapseButton.setToolTipText(getToggleButtonToolTipText());
            }
            if (collapse) {
                setFocused(true);
                setSelected(true);
            } else {
                myContent.requestFocusInWindow();
            }
            notifyListeners();
            revalidate();
            repaint();
        } finally {
            myIsInitialized = true;
        }
    }

    /**
     * expand
     */
    public void expand() {
        if (myIsCollapsed) {
            setCollapsed(false);
        }
    }

    /**
     * collapse
     */
    public void collapse() {
        if (!myIsCollapsed) {
            setCollapsed(true);
        }
    }

    /**
     * setFocused
     *
     * @param focused focused
     */
    public void setFocused(boolean focused) {
        myToggleCollapseButton.requestFocusInWindow();
    }

    /**
     * setSelected
     *
     * @param selected selected
     */
    public void setSelected(boolean selected) {
        myToggleCollapseButton.setSelected(selected);
    }

    /**
     * getCollapsibleActionMap
     *
     * @return InputMap
     */
    public ActionMap getCollapsibleActionMap() {
        return myToggleCollapseButton.getActionMap();
    }

    /**
     * getCollapsibleInputMap
     *
     * @return InputMap
     */
    public InputMap getCollapsibleInputMap() {
        return myToggleCollapseButton.getInputMap();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        updatePanel();
        super.paintComponent(graphics);
    }

    private void updatePanel() {
        if (paintAsSelected()) {
            setBackground(UIUtil.getTableSelectionBackground(true));
        } else {
            setBackground(myContent.getBackground());
        }
    }

    @Override
    protected void paintChildren(Graphics graphics) {
        if (myTitleLabel != null) {
            updateTitle();
        }
        updateToggleButton();
        super.paintChildren(graphics);
    }

    private void updateToggleButton() {
        if (paintAsSelected()) {
            myToggleCollapseButton.setBackground(JBColor.background().brighter());
        } else {
            myToggleCollapseButton.setBackground(myContent.getBackground());
        }
    }

    private void updateTitle() {
        if (paintAsSelected()) {
            myTitleLabel.setForeground(JBColor.foreground());
            myTitleLabel.setBackground(JBColor.background().brighter());
        } else {
            myTitleLabel.setForeground(JBColor.foreground());
            myTitleLabel.setBackground(JBColor.background().brighter());
        }
    }

    private boolean paintAsSelected() {
        return myToggleCollapseButton.hasFocus() && isCollapsed();
    }

    /**
     * CollapsingListener
     *
     * @since 2021/5/18 23:18
     */
    public interface CollapsingListener {
        /**
         * onCollapsingChanged
         *
         * @param panel panel
         * @param newValue newValue
         */
        void onCollapsingChanged(@NotNull FolderPanel panel, boolean newValue);
    }

}
