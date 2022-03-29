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

package ohos.devtools.views.distributed.component;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.Consumer;
import com.intellij.util.ui.JBUI;
import ohos.devtools.views.applicationtrace.util.TimeUtils;
import ohos.devtools.views.distributed.util.DistributedCommon;
import ohos.devtools.views.trace.ITimeRange;
import ohos.devtools.views.trace.Tip;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.Utils;

import java.awt.AlphaComposite;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_D;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_W;
import static ohos.devtools.views.distributed.component.DistributedTracePanel.endNS;
import static ohos.devtools.views.distributed.component.DistributedTracePanel.startNS;

/**
 * The timescale
 *
 * @since 2021/5/12 16:39
 */
public class DistributedTimeShaft extends JBPanel implements KeyListener, MouseListener, MouseMotionListener {
    private static final int SELECT_BORDER_WIDTH = 3;

    private final ITimeRange rangeListener;
    private final Consumer keyReleaseHandler;
    private final Consumer mouseReleaseHandler;
    private final AlphaComposite alpha20 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .2f);
    private final AlphaComposite alpha40 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .4f);
    private final AlphaComposite alpha100 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
    private final long[] scales =
        new long[] {50, 100, 200, 500, 1_000, 2_000, 5_000, 10_000, 20_000, 50_000, 100_000, 200_000, 500_000,
            1_000_000, 2_000_000, 5_000_000, 10_000_000, 20_000_000, 50_000_000, 100_000_000, 200_000_000, 500_000_000,
            1_000_000_000, 2_000_000_000, 5_000_000_000L, 10_000_000_000L, 20_000_000_000L, 50_000_000_000L,
            100_000_000_000L, 200_000_000_000L, 500_000_000_000L};
    private int startX;
    private int endX;
    private Rectangle selectRect = new Rectangle();
    private Rectangle selectLeftRect = new Rectangle();
    private Rectangle selectRightRect = new Rectangle();
    private Rectangle selectTopRect = new Rectangle();
    private Cursor wCursor = new Cursor(Cursor.W_RESIZE_CURSOR);
    private Cursor eCursor = new Cursor(Cursor.E_RESIZE_CURSOR);
    private Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
    private Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    private Status status = Status.DRAG;
    private int offset;
    private int length;
    private double ratio1;
    private double ratio2;
    private double wheelNS;
    private long scale;
    private boolean isInit;
    private DecimalFormat formatter = new DecimalFormat("#.##%");
    private Map<Integer, Double> rateMap = new HashMap<>();
    private double realW;
    private int ruleStartX;

    /**
     * structure function
     *
     * @param range range
     * @param keyReleaseHandler keyReleaseHandler
     * @param mouseReleaseHandler mouseReleaseHandler
     */
    public DistributedTimeShaft(ITimeRange range, Consumer<KeyEvent> keyReleaseHandler,
        Consumer<MouseEvent> mouseReleaseHandler) {
        this.rangeListener = range;
        this.keyReleaseHandler = keyReleaseHandler;
        this.mouseReleaseHandler = mouseReleaseHandler;
        this.setOpaque(true);
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                super.componentResized(event);
                startX = (int) (getWidth() * ratio1);
                endX = (int) (getWidth() * ratio2);
                Utils.setX(selectRect, Math.min(startX, endX));
                selectRect.width = Math.abs(endX - startX);
                setAllRect();
            }
        });
        addMouseListener(this);
        addKeyListener(this);
        addMouseMotionListener(this);
    }

    /**
     * mouseClicked function
     *
     * @param event event
     */
    public void mouseClicked(MouseEvent event) {
        event.getID();
    }

    /**
     * set TimeShaft Consumer function
     *
     * @param event event
     */
    public void mousePressed(MouseEvent event) {
        if (getVisibleRect().contains(event.getPoint())) {
            if (status == Status.DRAG) {
                startX = event.getX();
                endX = event.getX() + SELECT_BORDER_WIDTH * 3;
                selectRect.setLocation(event.getX(), 0);
                selectRect.height = getHeight();
                selectRect.width = SELECT_BORDER_WIDTH * 3;
                setAllRect();
                notifyRangeChange(startX, endX);
            } else if (status == Status.MOVE) {
                offset = Math.abs(event.getX() - Math.min(startX, endX));
                length = Math.abs(endX - startX);
                notifyRangeChange(startX, endX);
            } else {
                notifyRangeChange(startX, endX);
            }
        }
    }

    /**
     * when the mouse released event
     *
     * @param event event
     */
    public void mouseReleased(MouseEvent event) {
        if (startX > endX) {
            int tmp = startX;
            startX = endX;
            endX = tmp;
        }
        mouseReleaseHandler.consume(event);
    }

    /**
     * when the mouse Entered event
     *
     * @param event event
     */
    public void mouseEntered(MouseEvent event) {
        requestFocusInWindow();
    }

    /**
     * when the mouse Entered event
     *
     * @param event event
     */
    public void mouseExited(MouseEvent event) {
        Tip.getInstance().hidden();
    }

    /**
     * when the mouse Entered event
     *
     * @param event event
     */
    public void mouseDragged(MouseEvent event) {
        Tip.getInstance().hidden();
        if (status == Status.DRAG) {
            endX = event.getX();
        } else if (status == Status.LEFT) {
            startX = event.getX();
        } else if (status == Status.RIGHT) {
            endX = event.getX();
        } else if (status == Status.MOVE) {
            if (event.getX() - offset < 0) {
                startX = 0;
                endX = startX + length + event.getX() - offset;
            } else {
                startX = event.getX() - offset;
                if (startX + length > getWidth()) {
                    endX = getWidth();
                } else {
                    endX = startX + length;
                }
            }
        } else {
            endX = endX;
        }
        if (startX < 0) {
            startX = 0;
        }
        if (endX < 0) {
            endX = 0;
        }
        if (startX > getWidth()) {
            startX = getWidth();
        }
        if (endX > getWidth()) {
            endX = getWidth();
        }
        Utils.setX(selectRect, Math.min(startX, endX));
        selectRect.width = Math.abs(endX - startX);

        setAllRect();
        notifyRangeChange(Math.min(startX, endX), Math.max(startX, endX));
    }

    /**
     * when the mouse move event
     *
     * @param event event
     */
    public void mouseMoved(MouseEvent event) {
        if (this.getVisibleRect().contains(event.getPoint())) {
            if (selectLeftRect.contains(event.getPoint())) {
                setCursor(wCursor);
                status = Status.LEFT;
            } else if (selectRightRect.contains(event.getPoint())) {
                setCursor(eCursor);
                status = Status.RIGHT;
            } else if (selectTopRect.contains(event.getPoint())) {
                setCursor(handCursor);
                status = Status.MOVE;
            } else {
                status = Status.DRAG;
                setCursor(defaultCursor);
                tip(event);
            }
        }
    }

    private void tip(MouseEvent event) {
        String timeString = TimeUtils.getTimeFormatString(DistributedCommon.x2ns(event.getX(), this.getBounds()));
        Double rate = rateMap.get(event.getX());
        if (rate == null) {
            rate = 0.0d;
        }
        List<String> strings = Arrays.asList(timeString);
        Tip.getInstance().display(this, event.getPoint(), strings);
    }

    /**
     * when the key Typed  event
     *
     * @param event event
     */
    public void keyTyped(KeyEvent event) {
        event.getKeyCode();
    }

    /**
     * when the mouse press event
     *
     * @param event event
     */
    public void keyPressed(KeyEvent event) {
        switch (event.getExtendedKeyCode()) {
            case VK_A:
                wheelNS = (endNS - startNS) * -0.2;
                translation();
                break;
            case VK_D:
                wheelNS = (endNS - startNS) * 0.2;
                translation();
                break;
            case VK_W:
                wheelNS = (endNS - startNS) * 0.2;
                if (wheelNS == 0) {
                    wheelNS = 50L;
                }
                scale();
                break;
            case VK_S:
                wheelNS = (endNS - startNS) * -0.2;
                if (wheelNS == 0) {
                    wheelNS = -50L;
                }
                scale();
                break;
            default:
                break;
        }
    }

    /**
     * on the key released
     *
     * @param event event
     */
    public void keyReleased(KeyEvent event) {
        if (event.getExtendedKeyCode() == VK_A || event.getExtendedKeyCode() == VK_S
            || event.getExtendedKeyCode() == VK_D || event.getExtendedKeyCode() == VK_W) {
            keyReleaseHandler.consume(event);
        }
    }

    /**
     * put Rate Map
     *
     * @param xPoint xPoint
     * @param rate rate
     */
    public void putRateMap(int xPoint, double rate) {
        rateMap.put(xPoint, rate);
    }

    /**
     * set the current range
     *
     * @param mStartNS mStartNS
     * @param mEndNS mEndNS
     */
    public void setRange(long mStartNS, long mEndNS) {
        if (mStartNS < mEndNS) {
            startX = (int) (mStartNS * getWidth() * 1.0 / DistributedTracePanel.getDURATION());
            endX = (int) (mEndNS * getWidth() * 1.0 / DistributedTracePanel.getDURATION());
        } else {
            endX = (int) (mStartNS * getWidth() * 1.0 / DistributedTracePanel.getDURATION());
            startX = (int) (mEndNS * getWidth() * 1.0 / DistributedTracePanel.getDURATION());
        }
        refreshScale(mStartNS, mEndNS);
        if (startX == 0 && endX == 0) {
            endX = 1;
        }
        Utils.setX(selectRect, Math.min(startX, endX));
        selectRect.width = Math.abs(endX - startX);
        setAllRect();
    }

    private void refreshScale(long mStartNS, long mEndNS) {
        double l20 = (mEndNS - mStartNS) * 1.0 / 20;
        long min;
        long max;
        for (int index = 0; index < scales.length; index++) {
            if (scales[index] > l20) {
                if (index > 0) {
                    min = scales[index - 1];
                } else {
                    min = 0;
                }
                max = scales[index];
                double weight = (l20 - min) * 1.0 / (max - min);
                if (weight > 0.243) {
                    scale = max;
                } else {
                    scale = min;
                }
                break;
            }
        }
        if (scale == 0) {
            scale = scales[0];
        }
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (graphics instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setColor(JBColor.background().darker());
            g2.setComposite(alpha40);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setComposite(alpha100);
            g2.setColor(JBColor.foreground());
            // g2.drawString("CPU Usage", 3, 13);
            g2.setComposite(alpha100);
            if (startX == 0 && endX == 0) {
                startX = 0;
                endX = getWidth();
                selectRect.setLocation(0, 0);
                selectRect.width = getWidth();
                selectRect.height = getHeight();
                setAllRect();
            }
            g2.setColor(JBUI.CurrentTheme.Link.linkColor());
            g2.setComposite(alpha20);
            g2.fillRect(Utils.getX(selectRect), Utils.getY(selectRect), selectRect.width, selectRect.height);
            g2.setComposite(alpha40);
            g2.fillRect(Utils.getX(selectTopRect), Utils.getY(selectTopRect), selectTopRect.width,
                selectTopRect.height);
            g2.setComposite(alpha100);
            g2.fillRect(Utils.getX(selectLeftRect), Utils.getY(selectLeftRect), selectLeftRect.width,
                selectLeftRect.height);
            g2.fillRect(Utils.getX(selectRightRect), Utils.getY(selectRightRect), selectRightRect.width,
                selectRightRect.height);
            if (!isInit) {
                isInit = true;
                setRange(0L, DistributedTracePanel.getDURATION());
                notifyRangeChange(0L, DistributedTracePanel.getDURATION());
            }
            drawLeft2Right(g2);
        }
    }

    private void drawLeft2Right(Graphics2D graphics) {
        if (scale == 0) {
            return;
        }
        long tmpNs = 0L;
        long yu = startNS % scale;
        Rectangle rect = new Rectangle(0, 20, getBounds().width, getBounds().height);
        if (endNS - startNS <= 0) {
            return;
        }
        realW = (scale * rect.width) / (endNS - startNS);
        ruleStartX = Utils.getX(rect);
        if (yu != 0) {
            float firstNodeWidth = (float) ((yu * 1.0) / scale * realW);
            ruleStartX += firstNodeWidth;
            tmpNs += yu;
            graphics.setColor(JBColor.background().darker());
            final AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f);
            graphics.setComposite(alpha);
            graphics.drawLine(ruleStartX, Utils.getY(rect), ruleStartX, Utils.getY(rect) + rect.height);
        }
        graphics.setColor(JBColor.foreground().darker());
        String str;
        while (tmpNs < endNS - startNS) {
            graphics.setColor(JBColor.background().darker());
            final AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f);
            graphics.setComposite(alpha);
            graphics.drawLine(ruleStartX, Utils.getY(rect), ruleStartX, Utils.getY(rect) + rect.height);
            str = ohos.devtools.views.trace.util.TimeUtils.getSecondFromNSecond(tmpNs);
            if (str.isEmpty()) {
                str = "0s";
            }
            graphics.setColor(JBColor.foreground().darker());
            final AlphaComposite alphaFull = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
            graphics.setComposite(alphaFull);
            String timS = "+" + str;
            Rectangle2D bounds = graphics.getFontMetrics(Final.LITTER_FONT).getStringBounds(timS, graphics);
            graphics.drawString(timS, ruleStartX, (int) (Utils.getY(rect) + bounds.getHeight()));
            ruleStartX += realW;
            tmpNs += scale;
        }
    }

    private void drawTotal(Graphics2D g2) {
        g2.setColor(JBColor.background());
        g2.drawRect(0, 0, 100, 40);
    }

    private void setAllRect() {
        ratio1 = startX * 1.0 / getWidth();
        if (ratio1 < 0) {
            ratio1 = 0;
        }
        ratio2 = endX * 1.0 / getWidth();
        if (ratio2 > 1) {
            ratio2 = 1;
        }
        selectTopRect.setLocation(Utils.getX(selectRect), Utils.getY(selectRect));
        selectTopRect.width = selectRect.width;
        selectTopRect.height = SELECT_BORDER_WIDTH * 5;
        selectLeftRect.setLocation(Utils.getX(selectRect), Utils.getY(selectRect));
        selectLeftRect.width = SELECT_BORDER_WIDTH;
        selectLeftRect.height = selectRect.height;
        selectRightRect
            .setLocation(Utils.getX(selectRect) + selectRect.width - SELECT_BORDER_WIDTH, Utils.getY(selectRect));
        selectRightRect.width = selectLeftRect.width;
        selectRightRect.height = selectRect.height;
    }

    private void translation() {
        if (startNS + wheelNS <= 0) {
            startNS = 0;
            endNS = endNS - startNS;
            setRange(startNS, endNS);
        } else if (endNS + wheelNS >= DistributedTracePanel.getDURATION()) {
            startNS = DistributedTracePanel.getDURATION() - (endNS - startNS);
            endNS = DistributedTracePanel.getDURATION();
            setRange(startNS, endNS);
        } else {
            startNS = (long) (startNS + wheelNS);
            endNS = (long) (endNS + wheelNS);
            setRange(startNS, endNS);
        }
        notifyRangeChange(startNS, endNS);
    }

    private void scale() {
        startNS = (long) (startNS + wheelNS);
        endNS = (long) (endNS - wheelNS);
        if (startNS <= 0) {
            startNS = 0L;
        }
        if (endNS >= DistributedTracePanel.getDURATION()) {
            endNS = DistributedTracePanel.getDURATION();
        }
        setRange(startNS, endNS);
        notifyRangeChange(startNS, endNS);
    }

    private void notifyRangeChange(int xPoint, int yPoint) {
        long ns1 = DistributedCommon.x2ns(xPoint, getVisibleRect());
        long ns2 = DistributedCommon.x2ns(yPoint, getVisibleRect());
        startNS = Math.min(ns1, ns2);
        endNS = Math.max(ns1, ns2);
        refreshScale(startNS, endNS);
        repaint();
        Optional.ofNullable(rangeListener).ifPresent(range -> range.change(startNS, endNS, scale));
    }

    /**
     * notifyRangeChange
     *
     * @param ns1 ns1
     * @param ns2 ns2
     */
    public void notifyRangeChange(long ns1, long ns2) {
        startNS = Math.min(ns1, ns2);
        endNS = Math.max(ns1, ns2);
        repaint();
        Optional.ofNullable(rangeListener).ifPresent(range -> range.change(startNS, endNS, scale));
    }

    enum Status {
        DRAG, LEFT, RIGHT, MOVE
    }
}
