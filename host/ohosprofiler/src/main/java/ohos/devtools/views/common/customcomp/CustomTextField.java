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
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.JBColor;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.LayoutConstants;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.Graphics;
import java.awt.Insets;

/**
 * CustomTextField
 *
 * @since 2021/5/19 16:39
 */
public class CustomTextField extends JTextField {
    private Icon icon;

    private String type;

    private Border line = BorderFactory.createLineBorder(JBColor.background().darker());

    private CompoundBorder border;

    private Border empty;

    /**
     * CustomTextField
     *
     * @param name name
     */
    public CustomTextField(String name) {
        type = name;
        icon = AllIcons.Actions.Find;
        if ("level".equals(type)) {
            this.setBackground(ColorConstants.TRACE_TABLE_COLOR);
            empty = new EmptyBorder(0, LayoutConstants.TASK_LABEL_Y, 0, 0);
        }
        if ("press".equals(type)) {
            empty = new EmptyBorder(0, LayoutConstants.TASK_LABEL_Y, 0, 0);
        }
        if ("Analysis".equals(type)) {
            empty = new EmptyBorder(0, LayoutConstants.DEVICE_ADD_Y, 0, LayoutConstants.TASK_LABEL_Y);
        }
        if ("device".equals(type)) {
            icon = IconLoader.getIcon(("/images/down.png"), getClass());
            empty = new EmptyBorder(0, LayoutConstants.DEVICE_ADD_Y, 0, LayoutConstants.TASK_LABEL_Y);
        }
        border = new CompoundBorder(line, empty);
        this.setBorder(border);
    }

    @Override
    public void paintComponent(Graphics graphics) {
        Insets insets = getInsets();
        super.paintComponent(graphics);
        int iconWidth = icon.getIconWidth();
        int iconHeight = icon.getIconHeight();
        int height = this.getHeight();
        if ("level".equals(type) || "press".equals(type)) {
            icon.paintIcon(this, graphics, (insets.left - iconWidth) / LayoutConstants.DEVICES_Y,
                (height - iconHeight) / LayoutConstants.DEVICES_Y);
        }
        if ("Analysis".equals(type)) {
            icon.paintIcon(this, graphics, LayoutConstants.LEFT_TOP_WIDTH / LayoutConstants.DEVICES_Y,
                (height - iconHeight) / LayoutConstants.DEVICES_Y);
        }
        if ("device".equals(type)) {
            icon.paintIcon(this, graphics, getWidth() - 20, (height - iconHeight) / LayoutConstants.DEVICES_Y);
        }
    }
}
