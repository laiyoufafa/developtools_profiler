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
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import ohos.devtools.views.common.LayoutConstants;

import javax.swing.Icon;
import java.awt.Graphics;
import java.awt.Insets;

/**
 * Custom TextField
 */
public class CustomJBTextField extends JBTextField {
    private final Icon icon;
    private int width;

    /**
     * HosJBTextField
     */
    public CustomJBTextField() {
        icon = AllIcons.Actions.Search;
        Insets insets = JBUI.insetsLeft(LayoutConstants.NUM_20);
        // Set text input distance
        this.setMargin(insets);
    }

    /**
     * paintComponent
     *
     * @param graphics graphics
     */
    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        int iconHeight = icon.getIconHeight();
        int height = this.getHeight();
        width = this.getWidth() - LayoutConstants.THIRTY;
        // Draw the previous picture
        icon.paintIcon(this, graphics, width, (height - iconHeight) / LayoutConstants.NUM_2);
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
