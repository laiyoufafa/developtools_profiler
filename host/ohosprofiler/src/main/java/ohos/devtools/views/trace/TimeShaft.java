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

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.Consumer;
import com.intellij.util.ui.JBUI;
import ohos.devtools.views.applicationtrace.util.TimeUtils;
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
import static ohos.devtools.views.trace.TracePanel.DURATION;
import static ohos.devtools.views.trace.TracePanel.endNS;
import static ohos.devtools.views.trace.TracePanel.startNS;

/**
 * The timescale
 *
 * @since 2021/5/12 16:39
 */
public class TimeShaft extends JBPanel implements KeyListener, MouseListener, MouseMotionListener {
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
    private Consumer<Graphics2D> timeShaftConsumer;
    private DecimalFormat formatter = new DecimalFormat("#.##%");
    private Map<Integer, Double> rateMap = new HashMap<>();

    /**
     * TimeShaft
     *
     * @param range range
     * @param keyReleaseHandler keyReleaseHandler
     * @param mouseReleaseHandler mouseReleaseHandler
     */
    public TimeShaft(ITimeRange range, Consumer<KeyEvent> keyReleaseHandler, Consumer<MouseEvent> mouseReleaseHandler) {
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
        event.getX();
    }

    /**
     * set TimeShaft Consumer function
     *
     * @param consumer consumer
     */
    public void setTimeShaftConsumer(Consumer<Graphics2D> consumer) {
        this.timeShaftConsumer = consumer;
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
                Utils.setX(selectRect, event.getX());
                Utils.setY(selectRect, 0);
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
        String timeString = TimeUtils.getTimeFormatString(Common.x2ns(event.getX(), this.getBounds()));
        Double rate = rateMap.get(event.getX());
        if (rate == null) {
            rate = 0.0d;
        }
        List<String> strings = Arrays.asList(timeString, "CPU Usage    ", formatter.format(rate), "Select to inspect");
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
        if (rateMap.containsKey(xPoint)) {
            if (rateMap.get(xPoint) == 0d) {
                rateMap.put(xPoint, rate);
            }
        } else {
            rateMap.put(xPoint, rate);
        }
    }

    /**
     * clear map
     */
    public void clearMap() {
        rateMap.clear();
    }

    /**
     * set the current range
     *
     * @param mStartNS mStartNS
     * @param mEndNS mEndNS
     */
    public void setRange(long mStartNS, long mEndNS) {
        startX = (int) (mStartNS * getWidth() * 1.0 / DURATION);
        endX = (int) (mEndNS * getWidth() * 1.0 / DURATION);
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
        if (startX == 0 && endX == 0) {
            endX = 1;
        }
        Utils.setX(selectRect, Math.min(startX, endX));
        selectRect.width = Math.abs(endX - startX);
        setAllRect();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (graphics instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) graphics;
            Optional.ofNullable(timeShaftConsumer).ifPresent(it -> it.consume(g2));
            g2.setColor(JBColor.background().darker());
            g2.setComposite(alpha40);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setComposite(alpha100);
            g2.setColor(JBColor.foreground());
            g2.drawString("CPU Usage", 3, 13);
            g2.setComposite(alpha100);
            if (startX == 0 && endX == 0) {
                startX = 0;
                endX = getWidth();
                Utils.setX(selectRect, 0);
                Utils.setY(selectRect, 0);
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
                setRange(0L, DURATION);
                notifyRangeChange(0L, DURATION);
            }
        }
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
        Utils.setX(selectTopRect, Utils.getX(selectRect));
        Utils.setY(selectTopRect, Utils.getY(selectRect));
        selectTopRect.width = selectRect.width;
        selectTopRect.height = SELECT_BORDER_WIDTH * 5;
        Utils.setX(selectLeftRect, Utils.getX(selectRect));
        Utils.setY(selectLeftRect, Utils.getY(selectRect));
        selectLeftRect.width = SELECT_BORDER_WIDTH;
        selectLeftRect.height = selectRect.height;
        Utils.setX(selectRightRect, Utils.getX(selectRect) + selectRect.width - SELECT_BORDER_WIDTH);
        Utils.setY(selectRightRect, Utils.getY(selectRect));
        selectRightRect.width = selectLeftRect.width;
        selectRightRect.height = selectRect.height;
    }

    private void translation() {
        if (startNS + wheelNS <= 0) {
            startNS = 0;
            endNS = endNS - startNS;
            setRange(startNS, endNS);
        } else if (endNS + wheelNS >= DURATION) {
            startNS = DURATION - (endNS - startNS);
            endNS = DURATION;
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
        if (endNS >= DURATION) {
            endNS = DURATION;
        }
        setRange(startNS, endNS);
        notifyRangeChange(startNS, endNS);
    }

    private void notifyRangeChange(int xPoint, int yPoint) {
        long ns1 = Common.x2ns(xPoint, getVisibleRect());
        long ns2 = Common.x2ns(yPoint, getVisibleRect());
        startNS = Math.min(ns1, ns2);
        endNS = Math.max(ns1, ns2);
        repaint();
        Optional.ofNullable(rangeListener).ifPresent(range -> range.change(startNS, endNS, scale));
    }

    private void notifyRangeChange(long ns1, long ns2) {
        startNS = Math.min(ns1, ns2);
        endNS = Math.max(ns1, ns2);
        repaint();
        Optional.ofNullable(rangeListener).ifPresent(range -> range.change(startNS, endNS, scale));
    }

    enum Status {
        DRAG, LEFT, RIGHT, MOVE
    }
}
