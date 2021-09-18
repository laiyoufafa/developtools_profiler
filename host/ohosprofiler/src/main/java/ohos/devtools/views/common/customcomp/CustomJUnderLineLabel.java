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

import com.intellij.ui.components.JBLabel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;

import static java.awt.BasicStroke.CAP_BUTT;

/**
 * CustomJ UnderLine Label
 */
public class CustomJUnderLineLabel extends JBLabel {
    private Color underLineColor;

    /**
     * CustomJUnderLineLabel
     *
     * @param text text
     */
    public CustomJUnderLineLabel(String text) {
        super(text);
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        Rectangle rectangle = graphics.getClipBounds();
        int texX = 0;
        int texY = 0;
        if (this.getBorder() != null && this.getBorder().getBorderInsets(this) != null) {
            Insets inserts = this.getBorder().getBorderInsets(this);
            texX = inserts.left;
            texY = inserts.bottom;
        }
        int pointX = 0;
        int pointY = 0;
        pointX = texX;
        pointY = rectangle.height - texY - getFontMetrics(getFont()).getDescent();

        int point2X = 0;
        int point2Y = 0;
        point2Y = pointY;
        point2X = pointX + getFontMetrics(getFont()).stringWidth(getText());
        if (underLineColor != null) {
            graphics.setColor(underLineColor);
        }
        if (graphics instanceof Graphics2D) {
            Graphics2D graphics2D = (Graphics2D) graphics;
            // The array shows the length of each dotted line
            float[] typeArray = {point2X - pointX};
            BasicStroke dottedLIne = new BasicStroke(1, CAP_BUTT, 1, 1, typeArray, 1);
            graphics2D.setStroke(dottedLIne);
            graphics2D.drawLine(pointX, pointY, point2X, point2Y);
        }
    }

    public void setUnderLineColor(Color underLineColor) {
        this.underLineColor = underLineColor;
    }
}
