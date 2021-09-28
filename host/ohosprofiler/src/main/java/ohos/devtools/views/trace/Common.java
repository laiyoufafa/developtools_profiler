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

package ohos.devtools.views.trace;

import ohos.devtools.views.trace.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import static ohos.devtools.views.trace.TracePanel.DURATION;
import static ohos.devtools.views.trace.TracePanel.endNS;
import static ohos.devtools.views.trace.TracePanel.startNS;

/**
 * Common util
 *
 * @date: 2021/5/13 16:40
 */
public class Common {
    private static final Logger LOGGER = LogManager.getLogger(Common.class);
    private static final int LEN = 3;
    private static final int LEFT_PADDING = 2;
    private static final int RIGHT_PADDING = 2;

    /**
     * Get the string Rect
     *
     * @param str str
     * @param graphics graphics
     * @return Rectangle2D
     */
    public static Rectangle2D getStringRect(Graphics graphics, String str) {
        Rectangle2D bounds = graphics.getFontMetrics(graphics.getFont()).getStringBounds(str, graphics);
        return bounds;
    }

    /**
     * x coordinate to time (unit ns)
     *
     * @param coordX coordX
     * @param rect rect
     * @return long time
     */
    public static long x2ns(final int coordX, Rectangle rect) {
        return x2ns(coordX, rect, DURATION);
    }

    /**
     * x coordinate to time (unit ns)
     *
     * @param coordX coordX
     * @param rect rect
     * @param duration duration
     * @return long time
     */
    public static long x2ns(final int coordX, Rectangle rect, long duration) {
        long ns = (long) ((coordX - rect.getX()) * duration / (rect.getWidth() - Utils.getX(rect)));
        return ns;
    }

    /**
     * time to x coordinate
     *
     * @param ns ns
     * @param rect rect
     * @return double x coordinate
     */
    public static double ns2x(long ns, Rectangle rect) {
        return ns2x(ns, rect, DURATION);
    }

    /**
     * time to x coordinate
     *
     * @param ns ns
     * @param rect rect
     * @param duration duration
     * @return double x coordinate
     */
    public static double ns2x(long ns, Rectangle rect, long duration) {
        if (endNS == 0) {
            endNS = duration;
        }
        double xSize = (ns - startNS) * rect.getWidth() / (endNS - startNS);
        if (xSize < 0) {
            xSize = 0;
        }
        if (xSize > rect.getWidth()) {
            xSize = rect.getWidth();
        }
        return xSize;
    }

    /**
     * time to x coordinate by duration
     *
     * @param ns ns
     * @param rect rect
     * @param duration duration
     * @return double x coordinate
     */
    public static double nsToXByDur(long ns, Rectangle rect, long duration) {
        double xSize = ns * rect.getWidth() / duration;
        if (xSize < 0) {
            xSize = 0;
        }
        if (xSize > rect.getWidth()) {
            xSize = rect.getWidth();
        }
        return xSize;
    }

    /**
     * set alpha
     *
     * @param g2 g2
     * @param alpha alpha
     */
    public static void setAlpha(Graphics2D g2, float alpha) {
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    }

    /**
     * draw String in the Center
     *
     * @param graphics graphics
     * @param str str
     * @param rectangle rectangle
     */
    public static void drawStringCenter(Graphics2D graphics, String str, final Rectangle rectangle) {
        Rectangle2D minBound =
            graphics.getFontMetrics(graphics.getFont()).getStringBounds("m...", graphics); // show string min rect
        if (rectangle.width < minBound.getWidth() || rectangle.height < minBound.getHeight()) {
            return;
        }
        Rectangle2D bounds = graphics.getFontMetrics(graphics.getFont()).getStringBounds(str, graphics);
        double chartWidth = bounds.getWidth() / str.length(); // The width of each character

        // How many characters can be displayed in the rectangle
        double chartNum = (rectangle.width - LEFT_PADDING - RIGHT_PADDING) / chartWidth;
        int mY = (int) (rectangle.getY() + rectangle.height / 2 + bounds.getHeight() / 2);
        if (chartNum >= str.length()) {
            graphics.drawString(str, (int) (rectangle.getX() + (rectangle.width - bounds.getWidth()) / 2), mY);
        } else if (chartNum >= LEN + 1) {
            graphics.drawString(str.substring(0, (int) chartNum - LEN) + "...", (int) (rectangle.getX() + LEFT_PADDING),
                mY);
        } else if (chartNum > 1 && chartNum < LEN) { // If only one character can be displayed
            graphics.drawString(str.substring(0, 1), (int) (rectangle.getX() + LEFT_PADDING), mY);
        } else {
            graphics.drawString("", (int) (rectangle.getX() + LEFT_PADDING), mY);
        }
    }

    /**
     * Draw strings centered vertically and horizontally,
     * using this method when graphics are passed in from a single component.
     *
     * @param graphics graphics
     * @param str str
     * @param rectangle rectangle
     */
    public static void drawStringVHCenter(Graphics2D graphics, String str, final Rectangle rectangle) {
        if (rectangle.width < 5 || rectangle.height < 5) {
            return;
        }
        Rectangle2D bounds = graphics.getFontMetrics(graphics.getFont()).getStringBounds(str, graphics);
        double chartWidth = bounds.getWidth() / str.length(); // The width of each character
        // How many characters can be displayed in the rectangle
        double chartNum = (rectangle.width - LEFT_PADDING - RIGHT_PADDING) / chartWidth;
        int mY = (int) (rectangle.height / 2 + bounds.getHeight() / 2);
        if (chartNum >= str.length()) {
            graphics.drawString(str, (int) (rectangle.getX() + (rectangle.width - bounds.getWidth()) / 2), mY);
        } else if (chartNum >= LEN + 1) {
            graphics.drawString(str.substring(0, (int) chartNum - LEN) + "...", (int) (rectangle.getX() + LEFT_PADDING),
                mY);
        } else if (chartNum > 1 && chartNum < LEN) { // If only one character can be displayed
            graphics.drawString(str.substring(0, 1), (int) (rectangle.getX() + LEFT_PADDING), mY);
        } else {
            graphics.drawString("", (int) (rectangle.getX() + LEFT_PADDING), mY);
        }
    }

    /**
     * Draw strings middle height vertically and horizontally,
     * using this method when graphics are passed in from a single component.
     *
     * @param g2 g2
     * @param str str
     * @param rect rect
     */
    public static void drawStringMiddleHeight(Graphics2D g2, String str, final Rectangle rect) {
        Rectangle2D bounds = g2.getFontMetrics().getStringBounds(str, g2);
        double chartWidth = bounds.getWidth() / str.length();
        double chartNum = (rect.width - LEFT_PADDING - RIGHT_PADDING) / chartWidth;
        if (chartNum >= str.length()) {
            g2.drawString(str, Utils.getX(rect),
                (float) (Utils.getY(rect) + rect.height / 2 + bounds.getHeight() / 2 - 3));
        } else {
            if (chartNum >= LEN + 1) {
                g2.drawString(str.substring(0, (int) chartNum - LEN) + "...", Utils.getX(rect),
                    (float) (Utils.getY(rect) + rect.height / 2 + bounds.getHeight() / 2 - 3));
            } else if (chartNum > 1 && chartNum < LEN) {
                g2.drawString(str.substring(0, 1), Utils.getX(rect),
                    (float) (Utils.getY(rect) + rect.height / 2 + bounds.getHeight() / 2 - 3));
            } else {
                LOGGER.info("drawStringMiddleHeight error");
            }
        }
    }
}
