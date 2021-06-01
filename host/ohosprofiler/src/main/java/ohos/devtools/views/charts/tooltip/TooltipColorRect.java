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
 * Tooltip中的图例色块
 *
 * @since 2021/1/19 21:35
 */
public class TooltipColorRect extends JComponent {
    /**
     * 图例色块大小
     */
    private static final int SIZE = 15;

    /**
     * 颜色
     */
    private final Color color;

    /**
     * 构造函数
     *
     * @param color 图例色块的颜色
     */
    public TooltipColorRect(Color color) {
        this.color = color;
        this.setOpaque(false);
        fillColor();
    }

    /**
     * 填充颜色
     */
    private void fillColor() {
        this.setPreferredSize(new Dimension(SIZE, SIZE));
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
        graphics.setColor(color);
        graphics.fillRect(0, 0, SIZE, SIZE);
    }
}
