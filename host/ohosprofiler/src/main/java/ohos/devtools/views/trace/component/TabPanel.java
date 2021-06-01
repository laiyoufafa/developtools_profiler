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

package ohos.devtools.views.trace.component;

import com.intellij.ui.components.JBTabbedPane;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.Utils;

import javax.imageio.ImageIO;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

/**
 * tab component
 *
 * @version 1.0.1
 * @date 2021/04/20 12:12
 */
public class TabPanel extends JBTabbedPane implements MouseMotionListener {
    private static int mHeight = 300;
    private static int barHeight;

    private final int iconWH = 20;
    private Rectangle topRect;
    private Rectangle bottomRect;
    private int pressedY;
    private Image top = null;
    private Image bottom = null;

    /**
     * construct
     */
    public TabPanel() {
        setFont(Final.NORMAL_FONT);
        this.addMouseMotionListener(this);
        try {
            top = ImageIO.read(getClass().getResourceAsStream("/assets/top.png"));
            bottom = ImageIO.read(getClass().getResourceAsStream("/assets/bottom.png"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent event) {
                clickTop(event);
                clickBottom(event);
            }

            @Override
            public void mousePressed(final MouseEvent event) {
                pressedY = event.getYOnScreen();
            }

            @Override
            public void mouseReleased(final MouseEvent event) {
                super.mouseReleased(event);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        if (getTabCount() > 0) {
            Rectangle tabBounds = getUI().getTabBounds(this, 0);
            barHeight = tabBounds.height;
        }
        topRect = new Rectangle(getWidth() - 65, (barHeight - iconWH) / 2, iconWH, iconWH);
        bottomRect = new Rectangle(getWidth() - 35, (barHeight - iconWH) / 2, iconWH, iconWH);
        if (top != null) {
            graphics.drawImage(top, topRect.x + 2, topRect.y + 2, iconWH - 5, iconWH - 5, null);
        }
        if (bottom != null) {
            graphics.drawImage(bottom, bottomRect.x + 2, bottomRect.y + 2, iconWH - 5, iconWH - 5, null);
        }
    }

    @Override
    public void mouseDragged(final MouseEvent event) {
        if (getCursor().getType() == Cursor.N_RESIZE_CURSOR) {
            Rectangle rect = getBounds();
            int drag = event.getYOnScreen() - pressedY;
            pressedY = event.getYOnScreen();
            rect.y = rect.y + drag;
            int ph = getParent().getHeight();
            int heightTmp = ph - rect.y;
            if (heightTmp < barHeight) {
                heightTmp = barHeight;
                rect.y = ph - barHeight;
            }
            if (heightTmp > ph) {
                rect.y = 0;
                heightTmp = ph;
            }
            rect.height = heightTmp;
            mHeight = rect.height;
            setBounds(rect);
        }
    }

    @Override
    public void mouseMoved(final MouseEvent event) {
        int xAxis = event.getX();
        int yAxis = event.getY();
        int xNum = 0;
        if (getTabCount() > 0) {
            Rectangle rect = getUI().getTabBounds(this, getTabCount() - 1);
            xNum = rect.width + rect.x + 10;
        }
        if (yAxis > 0 && yAxis < barHeight && xAxis > xNum) {
            if (Utils.pointInRect(topRect, xAxis, yAxis) || Utils.pointInRect(bottomRect, xAxis, yAxis)) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else {
                setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
            }
        } else {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Click to top
     *
     * @param event Mouse event
     */
    private void clickTop(final MouseEvent event) {
        if (Utils.pointInRect(topRect, event.getX(), event.getY())) {
            mHeight = getParent().getHeight();
            setBounds(0, 0, getWidth(), mHeight);
        }
    }

    /**
     * Click to bottom
     *
     * @param event Mouse event
     */
    private void clickBottom(final MouseEvent event) {
        if (Utils.pointInRect(bottomRect, event.getX(), event.getY())) {
            hideInBottom();
        }
    }

    /**
     * Restore the default height of the bottom tab
     */
    public void recovery() {
        if (getMyHeight() == 0) {
            mHeight = 300;
            setBounds(0, getParent().getHeight() - mHeight, getWidth(), mHeight);
        }
    }

    /**
     * Minimize the bottom tab
     */
    public void hideInBottom() {
        if (mHeight != barHeight && barHeight > 0) {
            mHeight = barHeight;
            setBounds(0, getParent().getHeight() - barHeight, getWidth(), mHeight);
        }
    }

    /**
     * hide bottom tab
     */
    public void hide() {
        if (mHeight != 0) {
            mHeight = 0;
            setBounds(0, getParent().getHeight(), getWidth(), mHeight);
        }
    }

    /**
     * Get the height of the current tab
     *
     * @return height
     */
    public int getMHeight() {
        return mHeight;
    }

    /**
     * Get the height of the current tab
     *
     * @return height
     */
    public static int getMyHeight() {
        return mHeight == barHeight || barHeight == 0 ? 0 : mHeight;
    }
}
