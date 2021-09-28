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

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

/**
 * GraphicsLinePanel
 */
public class GraphicsLinePanel extends JBPanel {
    /**
     * Graphics panel
     */
    public GraphicsLinePanel() {
        this.setOpaque(false);
        this.setLayout(null);
    }

    /**
     * draw line
     *
     * @param graphics graphics
     */
    public void paint(Graphics graphics) {
        super.paint(graphics);
        graphics.setColor(JBColor.background().brighter());
        if (graphics instanceof Graphics2D) {
            Graphics2D lineGraphics = (Graphics2D) graphics;
            Line2D linTop = new Line2D.Float(0, 0, this.getWidth(), 0);
            lineGraphics.draw(linTop);
        }
    }
}
