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

package ohos.devtools.views.charts.tooltip;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

/**
 * Legend color block in tooltip
 */
public class TooltipColorRect extends JComponent {
    /**
     * Color block size
     */
    private static final int SIZE = 15;

    /**
     * Color
     */
    private final Color color;

    /**
     * Constructor
     *
     * @param color Color
     */
    public TooltipColorRect(Color color) {
        this.color = color;
        this.setOpaque(false);
        fillColor();
    }

    /**
     * Fill color
     */
    private void fillColor() {
        this.setPreferredSize(new Dimension(SIZE, SIZE));
        super.repaint();
        super.validate();
    }

    /**
     * Paint component
     *
     * @param graphics graphics
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.setColor(color);
        graphics.fillRect(0, 0, SIZE, SIZE);
    }
}
