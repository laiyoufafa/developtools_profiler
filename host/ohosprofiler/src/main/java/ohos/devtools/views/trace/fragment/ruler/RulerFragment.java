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

import ohos.devtools.views.trace.bean.FlagBean;
import ohos.devtools.views.trace.component.AnalystPanel;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.TimeUtils;

import javax.swing.JComponent;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Timeline zoom size
 *
 * @version 1.0
 * @date 2021/4/22 12:25
 **/
public class RulerFragment extends AbstractFragment implements FlagBean.IEventListener {
    private static final BasicStroke BOLD_STORE = new BasicStroke(2);
    private long leftNS;
    private long rightNS;
    private final long[] scales =
        new long[] {50, 100, 200, 500, 1_000, 2_000, 5_000, 10_000, 20_000, 50_000, 100_000, 200_000, 500_000,
            1_000_000, 2_000_000, 5_000_000, 10_000_000, 20_000_000, 50_000_000, 100_000_000, 200_000_000, 500_000_000,
            1_000_000_000, 2_000_000_000, 5_000_000_000L, 10_000_000_000L, 20_000_000_000L, 50_000_000_000L,
            100_000_000_000L, 200_000_000_000L, 500_000_000_000L};

    // The current time selection range is based on 20 units.
    // The zoom level obtained by the position in the scales array is 70ns if calculated
    private long min;

    // Then in the middle of 50L 100L min=50L
    // The current time selection range The position in the zoom level scales array based on 20 units.
    // If calculated, it is 70ns
    private long max;

    // Then it is in the middle of 50L 100L max = 100L
    private long l20; // The current time selection range is based on 20 units

    // When the weight ratio is greater than 24.3%, scale=max; otherwise, scale=min schematic diagram
    private long scale;

    // min---l20-------max
    private long centerNS;  // Select from left to right is true

    // From right to left is false;
    // when moving from left to right, use the left as the reference,
    // and fill the cell to the left; when moving from right to left, fill the cell to the right
    private double weight; // (l20-min)/(max-min) to get the proportion
    private int extendHeight;
    private int selectX;
    private int selectY;
    private FlagBean focusFlag = new FlagBean();
    private List<FlagBean> flags = new ArrayList<>();
    private double realW;
    private double startX;
    private IChange changeListener;

    /**
     * time range change listener
     */
    public interface IChange {
        /**
         * Time range change monitoring
         *
         * @param startNS Starting time
         * @param endNS   End Time
         */
        void change(long startNS, long endNS);
    }

    /**
     * Constructor
     *
     * @param root     Parent component
     * @param listener monitor
     */
    public RulerFragment(final JComponent root, final IChange listener) {
        this.changeListener = listener;
        this.setRoot(root);
        getRect().setBounds(200, 94, root.getWidth(), 40);
        setRange(0, AnalystPanel.DURATION, 0);
    }

    /**
     * Set time range
     *
     * @param left   left
     * @param right  right
     * @param center center
     */
    public void setRange(final long left, final long right, final long center) {
        this.centerNS = center;
        this.leftNS = left;
        this.rightNS = right;
        l20 = (this.rightNS - left) / 20;
        for (int index = 0; index < scales.length; index++) {
            if (scales[index] > l20) {
                if (index > 0) {
                    min = scales[index - 1];
                } else {
                    min = 0;
                }
                max = scales[index];
                weight = (l20 - min) * 1.0 / (max - min);
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
        for (FlagBean flag : flags) {
            flag.rect.x = getX(flag.getNs());
        }
        repaint();
    }

    /**
     * Drawing method
     *
     * @param graphics graphics
     */
    @Override
    public void draw(final Graphics2D graphics) {
        getRect().width = getRoot().getWidth() - getRect().x;
        graphics.setFont(Final.SMALL_FONT);
        graphics.setColor(getRoot().getForeground());
        if (rightNS - leftNS == 0) {
            return;
        }
        if (scale == 0) {
            setRange(0, AnalystPanel.DURATION, 0);
        }
        if (changeListener != null) {
            changeListener.change(leftNS, rightNS);
        }
        if (centerNS == leftNS) { // Fill from left to right
            drawLeft2Right(graphics);
        }
        if (centerNS == rightNS) { // Fill from right to left
            drawRight2Left(graphics);
        }
        for (FlagBean flagBean : flags) {
            graphics.setColor(flagBean.getColor());
            graphics.setStroke(new BasicStroke(2));
            int xAxis = flagBean.rect.x;
            if (xAxis > 0) {
                graphics.drawLine(xAxis + getRect().x, getRect().y + getRect().height / 2, getRect().x + xAxis,
                    getRect().y + getRect().height - 2);
                graphics.fillRect(xAxis + getRect().x, getRect().y + getRect().height / 2, 10, 10);
                graphics.fillRect(xAxis + getRect().x + 7, getRect().y + getRect().height / 2 + 2, 7, 7);
                flagBean.draw(graphics);
            }
        }
        drawFocusFlag(graphics);
    }

    private void drawRight2Left(Graphics2D graphics) {
        graphics.setColor(getRoot().getForeground());
        final AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        graphics.setComposite(alpha);
        graphics.drawLine(getRect().x, getRect().y, getRect().x + getRect().width, getRect().y);
        long tmpNs = rightNS - leftNS;
        startX = getRect().x + getRect().width;
        realW = (scale * getRect().width) / (rightNS - leftNS);
        String str;
        while (tmpNs > 0) {
            str = TimeUtils.getSecondFromNSecond(tmpNs);
            if (str.isEmpty()) {
                str = "0s";
            }
            graphics.setColor(getRoot().getForeground());
            final AlphaComposite alpha50 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f);
            graphics.setComposite(alpha50);
            graphics.drawLine((int) startX, getRect().y, (int) startX, getRect().y + getRect().height + extendHeight);
            graphics.setColor(getRoot().getForeground());
            final AlphaComposite alphaFul = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
            graphics.setComposite(alphaFul);
            graphics.drawString("+" + str, (int) startX,
                getRect().y + (int) (graphics.getFontMetrics().getStringBounds("+" + str, graphics).getHeight()));
            startX -= realW;
            tmpNs -= scale;
        }
    }

    private void drawLeft2Right(Graphics2D graphics) {
        if (scale == 0) {
            return;
        }
        long tmpNs = 0;
        long yu = leftNS % scale;
        realW = (scale * getRect().width) / (rightNS - leftNS);
        startX = getRect().x;
        if (yu != 0) {
            float firstNodeWidth = (float) ((yu * 1.0) / scale * realW);
            startX += firstNodeWidth;
            tmpNs += yu;
            graphics.setColor(getRoot().getForeground());
            final AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f);
            graphics.setComposite(alpha);
            graphics.drawLine((int) startX, getRect().y, (int) startX, getRect().y + getRect().height);
        }
        graphics.setColor(getRoot().getForeground());
        graphics.drawLine(getRect().x, getRect().y, getRect().x + getRect().width, getRect().y);
        String str;
        while (tmpNs < rightNS - leftNS) {
            graphics.setColor(getRoot().getForeground());
            final AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f);
            graphics.setComposite(alpha);
            graphics.drawLine((int) startX, getRect().y, (int) startX, getRect().y + getRect().height + extendHeight);
            str = TimeUtils.getSecondFromNSecond(tmpNs);
            if (str.isEmpty()) {
                str = "0s";
            }
            graphics.setColor(getRoot().getForeground());
            final AlphaComposite alphaFull = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f);
            graphics.setComposite(alphaFull);
            String timS = "+" + str;
            Rectangle2D bounds = graphics.getFontMetrics(Final.SMALL_FONT).getStringBounds(timS, graphics);
            graphics.drawString(timS, (int) startX, (int) (getRect().y + bounds.getHeight()));
            startX += realW;
            tmpNs += scale;
        }
    }

    private void drawFocusFlag(Graphics2D graphics) {
        if (focusFlag.isVisible()) {
            final int side = 10;
            graphics.setColor(focusFlag.getColor());
            graphics.setStroke(BOLD_STORE);
            graphics.drawLine(focusFlag.rect.x + getRect().x, getRect().y + getRect().height / 2,
                getRect().x + focusFlag.rect.x, getRect().y + getRect().height + extendHeight);
            graphics.fillRect(focusFlag.rect.x + getRect().x, getRect().y + getRect().height / 2, side, side);
            final int offset = 7;
            graphics
                .fillRect(focusFlag.rect.x + getRect().x + offset, getRect().y + getRect().height / 2 + 2, side, side);
        }
    }

    /**
     * Convert x coordinate according to time
     *
     * @param ns ns
     * @return int int
     */
    public int getX(final long ns) {
        return (int) ((ns - leftNS) * getRect().width / ((rightNS - leftNS) * 1.0));
    }

    /**
     * Mouse movement event
     *
     * @param event event
     */
    public void mouseMoved(final MouseEvent event) {
        final int leftW = 200;
        if (selectY > getRect().y && selectY < getRect().y + getRect().height && event.getX() >= leftW) {
            Optional<FlagBean> first = flags.stream().filter(bean -> event.getX() >= bean.rect.x + getRect().x
                && event.getX() <= bean.rect.x + getRect().x + bean.rect.width).findFirst();
            if (first.isPresent()) {
                focusFlag.setVisible(false);
            } else {
                focusFlag.setVisible(true);
                focusFlag.rect.x = event.getX() - getRect().x;
                focusFlag.rect.y = getRect().y + getRect().height / 2;
                focusFlag.rect.width = 17;
                focusFlag.rect.height = getRect().height / 2;
            }
        } else {
            focusFlag.setVisible(false);
        }
        getRoot().repaint();
    }

    /**
     * Mouse pressed event
     *
     * @param event event
     */
    public void mousePressed(final MouseEvent event) {
        final int leftW = 200;
        if (selectY > getRect().y && selectY < getRect().y + getRect().height && event.getX() >= leftW) {
            Optional<FlagBean> first = flags.stream().filter(bean -> event.getX() >= bean.rect.x + getRect().x
                && event.getX() <= bean.rect.x + getRect().x + bean.rect.width).findFirst();
            if (first.isPresent()) {
                FlagBean flagBean = first.get();
                flagBean.onClick(event);
            } else {
                FlagBean flagBean = new FlagBean();
                flagBean.root = getRoot();
                flagBean.rect.x = event.getX() - getRect().x;
                flagBean.rect.y = getRect().y + getRect().height / 2;
                flagBean.setEventListener(this);
                flagBean.rect.width = 17;
                flagBean.rect.height = getRect().height / 2;
                flagBean.setNs((long) ((rightNS - leftNS) / (getRect().width * 1.0) * flagBean.rect.x) + leftNS);
                flagBean.setVisible(true);
                flags.add(flagBean);
            }
            repaint();
        }
    }

    /**
     * FlagBean object click event
     *
     * @param event event
     * @param data  data
     */
    @Override
    public void click(final MouseEvent event, final FlagBean data) {
        if (AnalystPanel.iFlagClick != null) {
            AnalystPanel.iFlagClick.click(data);
        }
    }

    /**
     * Loss of focus event
     *
     * @param event event
     * @param data  data
     */
    @Override
    public void blur(final MouseEvent event, final FlagBean data) {
    }

    /**
     * Get focus event
     *
     * @param event event
     * @param data  data
     */
    @Override
    public void focus(final MouseEvent event, final FlagBean data) {
    }

    /**
     * Mouse move event
     *
     * @param event event
     * @param data  data
     */
    @Override
    public void mouseMove(final MouseEvent event, final FlagBean data) {
    }

    /**
     * Remove the flag mark
     *
     * @param data data
     */
    @Override
    public void delete(final FlagBean data) {
        flags.removeIf(bean -> bean.getNs() == data.getNs());
        repaint();
    }

    /**
     * Gets the value of leftNS .
     *
     * @return the value of long
     */
    public long getLeftNS() {
        return leftNS;
    }

    /**
     * Sets the leftNS .
     * <p>You can use getLeftNS() to get the value of leftNS</p>
     *
     * @param ns ns
     */
    public void setLeftNS(final long ns) {
        this.leftNS = ns;
    }

    /**
     * Gets the value of rightNS .
     *
     * @return the value of long
     */
    public long getRightNS() {
        return rightNS;
    }

    /**
     * Sets the rightNS .
     * <p>You can use getRightNS() to get the value of rightNS</p>
     *
     * @param ns ns
     */
    public void setRightNS(final long ns) {
        this.rightNS = ns;
    }

    /**
     * Gets the value of scale .
     *
     * @return the value of long
     */
    public long getScale() {
        return scale;
    }

    /**
     * Sets the scale .
     * <p>You can use getScale() to get the value of scale</p>
     *
     * @param scale scale
     */
    public void setScale(final long scale) {
        this.scale = scale;
    }

    /**
     * Gets the value of centerNS .
     *
     * @return the value of long
     */
    public long getCenterNS() {
        return centerNS;
    }

    /**
     * Sets the centerNS .
     * <p>You can use getCenterNS() to get the value of centerNS</p>
     *
     * @param ns ns
     */
    public void setCenterNS(final long ns) {
        this.centerNS = ns;
    }

    /**
     * Gets the value of extendHeight .
     *
     * @return the value of int
     */
    public int getExtendHeight() {
        return extendHeight;
    }

    /**
     * Sets the extendHeight .
     * <p>You can use getExtendHeight() to get the value of extendHeight</p>
     *
     * @param height height
     */
    public void setExtendHeight(final int height) {
        this.extendHeight = height;
    }

    /**
     * Gets the value of selectX .
     *
     * @return the value of int
     */
    public int getSelectX() {
        return selectX;
    }

    /**
     * Sets the selectX .
     * <p>You can use getSelectX() to get the value of selectX</p>
     *
     * @param select select
     */
    public void setSelectX(final int select) {
        this.selectX = select;
    }

    /**
     * Gets the value of selectY .
     *
     * @return the value of int
     */
    public int getSelectY() {
        return selectY;
    }

    /**
     * Sets the selectY .
     * <p>You can use getSelectY() to get the value of selectY</p>
     *
     * @param select select
     */
    public void setSelectY(final int select) {
        this.selectY = select;
    }
}
