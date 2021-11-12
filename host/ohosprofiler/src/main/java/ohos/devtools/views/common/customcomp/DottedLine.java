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

import com.intellij.ui.components.JBPanel;
import ohos.devtools.views.common.ColorConstants;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import static java.awt.BasicStroke.CAP_BUTT;
import static java.awt.BasicStroke.JOIN_ROUND;

/**
 * Draw a vertical dotted line
 */
public class DottedLine extends JBPanel {
    private static final int DEFAULT_WIDTH = 10;
    private static final int DEFAULT_HEIGHT = 20;
    private static final int DEFAULT_POINT_X = 5;

    /**
     * DottedLine
     */
    public DottedLine() {
        this.setOpaque(false);
        this.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        repaint();
    }

    /**
     * paintComponent
     *
     * @param graphics graphics
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2d = castGraphics2D(graphics);
        if (g2d == null) {
            return;
        }
        float[] dash = {1.0f, 0f, 1.0f};
        BasicStroke bs = new BasicStroke(1, CAP_BUTT, JOIN_ROUND, 1.0f, dash, 1.0f);
        g2d.setColor(ColorConstants.RULER);
        g2d.setStroke(bs);
        g2d.drawLine(DEFAULT_POINT_X, 0, DEFAULT_POINT_X, this.getHeight());
    }

    private Graphics2D castGraphics2D(Graphics graphics) {
        Graphics2D graph = null;
        if (graphics instanceof Graphics2D) {
            graph = (Graphics2D) graphics;
        }
        return graph;
    }

}
