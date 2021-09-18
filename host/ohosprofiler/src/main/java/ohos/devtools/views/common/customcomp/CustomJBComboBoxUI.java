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

package ohos.devtools.views.common.customcomp;

import com.intellij.icons.AllIcons;
import com.intellij.util.ui.components.JBComponent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * Hos JBComboBox
 */
public class CustomJBComboBoxUI extends BasicComboBoxUI {
    /**
     * createUI
     *
     * @param component JComponent
     * @return ComponentUI
     */
    public static ComponentUI createUI(JBComponent component) {
        return new CustomJBComboBoxUI();
    }

    /**
     * createArrowButton
     *
     * @return JButton
     */
    @Override
    protected JButton createArrowButton() {
        JButton dropDownButton = new JButton();
        dropDownButton.setContentAreaFilled(false);
        dropDownButton.setFocusPainted(false);
        dropDownButton.setBorder(BorderFactory.createEmptyBorder());
        dropDownButton.setOpaque(true);
        dropDownButton.setIcon(AllIcons.Actions.FindAndShowNextMatchesSmall);
        return dropDownButton;
    }

    @Override
    public void paintCurrentValue(Graphics graphics, Rectangle bounds, boolean hasFocus) {
        ListCellRenderer<Object> renderer = comboBox.getRenderer();
        Component component;

        if (hasFocus && !isPopupVisible(comboBox)) {
            component = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, true, false);
        } else {
            component = renderer.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, false, false);
            component.setBackground(UIManager.getColor("ComboBox.background"));
        }
        component.setFont(comboBox.getFont());
        if (hasFocus && !isPopupVisible(comboBox)) {
            component.setBackground(UIManager.getColor("ComboBox.background"));
        } else {
            if (comboBox.isEnabled()) {
                component.setBackground(UIManager.getColor("ComboBox.background"));
            } else {
                component.setBackground(UIManager.getColor("ComboBox.background"));
            }
        }
        boolean shouldValidate = false;
        if (component instanceof JPanel) {
            shouldValidate = true;
        }
        int boundX = (int) bounds.getX();
        int boundY = (int) bounds.getY();
        int boundW = bounds.width;
        int boundH = bounds.height;
        if (padding != null) {
            boundX = (int) (bounds.getX() + padding.left);
            boundY = (int) (bounds.getY() + padding.top);
            boundW = bounds.width - (padding.left + padding.right);
            boundH = bounds.height - (padding.top + padding.bottom);
        }
        currentValuePane.paintComponent(graphics, component, comboBox, boundX, boundY, boundW, boundH, shouldValidate);
    }
}
