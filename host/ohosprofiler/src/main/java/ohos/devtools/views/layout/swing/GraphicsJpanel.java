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

package ohos.devtools.views.layout.swing;

import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.LayoutConstants;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

/**
 * @Description GraphicsJpanel
 * @Date 2021/4/13 13:15
 **/
public class GraphicsJpanel extends JPanel {
    private TaskPanel taskPanel;

    /**
     * GraphicsJpanel
     *
     * @param taskPanel taskPanel
     */
    public GraphicsJpanel(TaskPanel taskPanel) {
        this.setOpaque(false);
        this.setLayout(null);
        this.taskPanel = taskPanel;
    }

    /**
     * 直线绘制
     *
     * @param graphics graphics
     */
    public void paint(Graphics graphics) {
        super.paint(graphics);
        graphics.setColor(ColorConstants.TOP_COLOR);
        Graphics2D graphicsTop = (Graphics2D) graphics;
        Graphics2D graphicsBottom = null;
        if (graphics instanceof Graphics2D) {
            graphicsBottom = (Graphics2D) graphics;
        }
        Line2D linTop = new Line2D.Float(LayoutConstants.THIRTY, 0, this.getWidth() - LayoutConstants.THIRTY, 0);
        Line2D linBottom = new Line2D.Float(LayoutConstants.THIRTY, LayoutConstants.JP_LEFT_WIDTH,
            this.getWidth() - LayoutConstants.THIRTY, LayoutConstants.JP_LEFT_WIDTH);
        graphicsTop.draw(linTop);
        if (graphicsBottom != null) {
            graphicsBottom.draw(linBottom);
        }
    }
}
