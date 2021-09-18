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

package ohos.devtools.views.charts.model;

import com.intellij.ui.components.JBPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

/**
 * legend Color Block In Chart
 *
 * @since 2021/3/1 9:48
 */
public class ChartLegendColorRect extends JBPanel {
    /**
     * legend Color Block Default Size
     */
    private static final int DEFAULT_SIZE = 10;

    private int rectWidth;

    private int rectHeight;

    /**
     * color
     */
    private Color color;

    /**
     * Chart Legend Color Rect
     */
    public ChartLegendColorRect() {
        this(DEFAULT_SIZE, DEFAULT_SIZE);
    }

    /**
     * Chart Legend Color Rect
     *
     * @param rectWidth rectWidth
     * @param rectHeight rectHeight
     */
    public ChartLegendColorRect(int rectWidth, int rectHeight) {
        this.rectWidth = rectWidth;
        this.rectHeight = rectHeight;
        this.setOpaque(false);
        fillColor();
    }

    /**
     * fillColor
     */
    private void fillColor() {
        this.setPreferredSize(new Dimension(rectWidth, rectHeight));
        super.repaint();
        super.validate();
    }

    /**
     * paintComponent
     *
     * @param graphics graphics
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (color == null) {
            color = Color.GRAY;
        }
        graphics.setColor(color);
        graphics.fillRect(0, 0, rectWidth, rectHeight);
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
