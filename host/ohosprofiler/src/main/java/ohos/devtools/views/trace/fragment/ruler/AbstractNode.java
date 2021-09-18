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

package ohos.devtools.views.trace.fragment.ruler;

import ohos.devtools.views.trace.fragment.graph.AbstractGraph;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

/**
 * all of data node graph node
 *
 * @date 2021/4/26 14:33
 */
public abstract class AbstractNode {
    /**
     * Left padding
     */
    static final int LEFT_PADDING = 5;

    /**
     * Right padding
     */
    static final int RIGHT_PADDING = 5;

    /**
     * Draw string
     *
     * @param graphics graphics
     * @param rectangle rectangle
     * @param str str
     * @param placement placement
     */
    public void drawString(final Graphics graphics, final Rectangle rectangle, final String str,
        final AbstractGraph.Placement placement) {
        if (str == null || rectangle.width < 5) {
            return;
        }
        final int len = 3;
        if (placement == Placement.CENTER) {
            Rectangle2D bounds = graphics.getFontMetrics(graphics.getFont()).getStringBounds(str, graphics);
            double chartWidth = bounds.getWidth() / str.length(); // The width of each character
            // How many characters can be displayed in the rectangle
            double chartNum = (rectangle.getWidth() - LEFT_PADDING - RIGHT_PADDING) / chartWidth;
            int mY = (int) (rectangle.getY() + bounds.getHeight());
            // If the width is enough to display, the display text is in the middle of the rectangle
            if (chartNum >= str.length()) {
                graphics.drawString(str, (int) (rectangle.getX() + (rectangle.width - bounds.getWidth()) / 2), mY);
            } else if (chartNum >= len + 1) {
                // If the width is not enough to display, cut out the part that can be displayed with an ellipsis behind
                graphics
                    .drawString(str.substring(0, (int) chartNum - len) + "...", (int) (rectangle.getX() + LEFT_PADDING),
                        mY);
            } else if (chartNum > 1 && chartNum < len) { // If only one character can be displayed
                graphics.drawString(str.substring(0, 1), (int) (rectangle.getX() + LEFT_PADDING), mY);
            } else {
                graphics.drawString("", (int) (rectangle.getX() + LEFT_PADDING), mY);
            }
        }
        if (placement == Placement.CENTER_LINE) {
            Rectangle2D bounds = graphics.getFontMetrics(graphics.getFont()).getStringBounds(str, graphics);
            double chartWidth = bounds.getWidth() / str.length(); // The width of each character
            // How many characters can be displayed in the rectangle
            double chartNum = (rectangle.width - LEFT_PADDING - RIGHT_PADDING) / chartWidth;
            int mY = (int) (rectangle.getY() + rectangle.height / 2 + bounds.getHeight() / 2);
            if (chartNum >= str.length()) {
                // If the width is enough to display, the display text is in the middle of the rectangle
                graphics.drawString(str, (int) (rectangle.getX() + (rectangle.width - bounds.getWidth()) / 2), mY);
            } else if (chartNum >= len + 1) {
                graphics
                    .drawString(str.substring(0, (int) chartNum - len) + "...", (int) (rectangle.getX() + LEFT_PADDING),
                        mY);
            } else if (chartNum > 1 && chartNum < len) { // If only one character can be displayed
                graphics.drawString(str.substring(0, 1), (int) (rectangle.getX() + LEFT_PADDING), mY);
            } else {
                graphics.drawString("", (int) (rectangle.getX() + LEFT_PADDING), mY);
            }
        }
    }

    /**
     * Direction Enum
     */
    public enum Placement {
        /**
         * center
         */
        CENTER,
        /**
         * Multi-line
         */
        MULTILINE,
        /**
         * Middle row
         */
        CENTER_LINE
    }
}
