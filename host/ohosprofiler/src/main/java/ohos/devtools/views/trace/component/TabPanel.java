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

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.Consumer;
import com.intellij.util.ui.JBUI;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.Utils;

import javax.imageio.ImageIO;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.Objects;

/**
 * tab component
 *
 * @date 2021/04/20 12:12
 */
public class TabPanel extends JBTabbedPane implements MouseMotionListener {
    private static int mHeight = 300;
    private static int barHeight;
    private final int iconWH = 20;
    private Rectangle topRect;
    private Rectangle bottomRect;
    private Image topImage = null;
    private Image bottomImage = null;
    private Rectangle rootRect;

    private Point startPoint;
    private Point endPoint;
    private Rectangle srcBounds;
    private Consumer<Rectangle> boundsChangeListener;

    /**
     * structure function
     */
    public TabPanel() {
        setFont(Final.NORMAL_FONT);
        setBorder(JBUI.Borders.customLine(JBColor.background().darker(), 1, 0, 0, 0));
        this.addMouseMotionListener(this);
        try {
            topImage = ImageIO.read(getClass().getResourceAsStream("/assets/top.png"));
            bottomImage = ImageIO.read(getClass().getResourceAsStream("/assets/bottom.png"));
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
                srcBounds = TabPanel.this.getBounds();
                startPoint = SwingUtilities
                    .convertPoint(TabPanel.this, event.getPoint(), TabPanel.this.getRootPane().getLayeredPane());
            }

            @Override
            public void mouseReleased(final MouseEvent event) {
                super.mouseReleased(event);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    // /**
    //  * Sets the layerRect .
    //  * <p>You can use getLayerRect() to get the value of layerRect</p>
    //  *
    //  * @param rect LayerRect
    //  */
    // public void setLayerRect(Rectangle rect) {
    //     this.layerRect = rect;
    // }

    /**
     * Sets the rootRect .
     * <p>You can use getRootRect() to get the value of rootRect</p>
     *
     * @param rect RootRect
     */
    public void setRootRect(Rectangle rect) {
        this.rootRect = rect;
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
        if (topImage != null) {
            graphics.drawImage(topImage, Utils.getX(topRect) + 2, Utils.getY(topRect) + 2, iconWH - 5, iconWH - 5,
                null);
        }
        if (bottomImage != null) {
            graphics.drawImage(bottomImage, Utils.getX(bottomRect) + 2, Utils.getY(bottomRect) + 2, iconWH - 5,
                iconWH - 5, null);
        }
    }

    @Override
    public void mouseDragged(final MouseEvent event) {
        if (getCursor().getType() == Cursor.N_RESIZE_CURSOR) {
            endPoint = SwingUtilities.convertPoint(TabPanel.this, event.getPoint(),
                TabPanel.this.getRootPane().getLayeredPane());
            int yPosition = Utils.getY(endPoint) - Utils.getY(startPoint);
            if (srcBounds.height - yPosition < barHeight) {
                return;
            } else if (srcBounds.height - yPosition
                > TabPanel.this.getRootPane().getLayeredPane().getHeight() - barHeight) {
                return;
            } else {
                TabPanel.this.setBounds(Utils.getX(srcBounds), Utils.getY(srcBounds) + yPosition, srcBounds.width,
                    srcBounds.height - yPosition);
                TabPanel.this.revalidate();
                if (boundsChangeListener != null) {
                    boundsChangeListener.consume(TabPanel.this.getBounds());
                }
            }
        }
    }

    @Override
    public void mouseMoved(final MouseEvent event) {
        int xNum = 0;
        if (getTabCount() > 0) {
            Rectangle rect = getUI().getTabBounds(this, getTabCount() - 1);
            xNum = rect.width + Utils.getX(rect) + 10;
        }
        if (event.getY() > 0 && event.getY() < barHeight && event.getX() > xNum) {
            if (topRect.contains(event.getPoint()) || bottomRect.contains(event.getPoint())) {
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
        if (topRect.contains(event.getPoint())) {
            int bottomHeight =
                getRootPane().getLayeredPane().getBounds().height - Utils.getY(rootRect) - rootRect.height;
            mHeight = getRootPane().getLayeredPane().getBounds().height - bottomHeight;
            setBounds(Utils.getX(rootRect), 0, rootRect.width, mHeight);
        }
    }

    /**
     * Click to bottom
     *
     * @param event Mouse event
     */
    private void clickBottom(final MouseEvent event) {
        if (bottomRect.contains(event.getPoint())) {
            hideInBottom();
        }
    }

    /**
     * Minimize the bottom tab
     */
    public void hideInBottom() {
        mHeight = barHeight;
        int bottomHeight = getRootPane().getLayeredPane().getBounds().height - Utils.getY(rootRect) - rootRect.height;
        setBounds(Utils.getX(rootRect), getRootPane().getLayeredPane().getBounds().height - bottomHeight - mHeight,
            getWidth(),
            mHeight);
    }

    /**
     * hide bottom tab
     */
    public void hidden() {
        if (Objects.nonNull(getRootPane()) && Objects.nonNull(getRootPane().getLayeredPane())) {
            getRootPane().getLayeredPane().setLayer(this, JLayeredPane.UNDEFINED_CONDITION);
            this.setVisible(false);
        }
    }

    /**
     * display current panel
     */
    public void display() {
        mHeight = rootRect.height / 5 * 3;
        setVisible(true);
        getRootPane().getLayeredPane().setLayer(this, JLayeredPane.DRAG_LAYER);
        setBounds(new Rectangle(Utils.getX(rootRect), Utils.getY(rootRect) + mHeight, rootRect.width,
            rootRect.height - mHeight));
    }

    /**
     * display current panel
     *
     * @param rectangle rectangle
     */
    public void display(Rectangle rectangle) {
        setRootRect(rectangle);
        mHeight = rootRect.height / 5 * 3;
        setVisible(true);
        if (getRootPane() != null) {
            getRootPane().getLayeredPane().setLayer(this, JLayeredPane.DRAG_LAYER);
            setBounds(new Rectangle(Utils.getX(rectangle), Utils.getY(rectangle) + mHeight, rectangle.width,
                rectangle.height - mHeight));
        }
    }

}
