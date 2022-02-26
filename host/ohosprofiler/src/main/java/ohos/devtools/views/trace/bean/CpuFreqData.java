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

package ohos.devtools.views.trace.bean;

import ohos.devtools.views.trace.DField;
import ohos.devtools.views.trace.fragment.graph.AbstractGraph;
import ohos.devtools.views.trace.util.ColorUtils;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JComponent;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

/**
 * cpu frequency data
 *
 * @since 2021/04/22 12:25
 */
public class CpuFreqData extends AbstractGraph {
    /**
     * flagFocus
     */
    private boolean flagFocus;
    private final int redOff = 40;
    private final int greenOff = 60;
    private final int blueOff = 75;
    @DField(name = "cpu")
    private int cpu;

    @DField(name = "value")
    private long value;

    @DField(name = "startNS")
    private long startTime;

    private long duration;
    private JComponent root;
    private double max;
    private NodeEventListener eventListener;
    private boolean isSelected; // Whether to be selected

    /**
     * Empty parameter construction method
     */
    public CpuFreqData() {
        super();
    }

    /**
     * Gets the value of cpu .
     *
     * @return the value of int
     */
    public int getCpu() {
        return cpu;
    }

    /**
     * Sets the cpu .
     * <p>You can use getCpu() to get the value of cpu</p>
     *
     * @param cpu cpu
     */
    public void setCpu(final int cpu) {
        this.cpu = cpu;
    }

    /**
     * Gets the value of value .
     *
     * @return the value of long
     */
    public long getValue() {
        return value;
    }

    /**
     * Sets the value .
     * <p>You can use getValue() to get the value of value</p>
     *
     * @param value value
     */
    public void setValue(final long value) {
        this.value = value;
    }

    /**
     * Gets the value of startTime .
     *
     * @return the value of long
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Sets the startTime .
     * <p>You can use getStartTime() to get the value of startTime</p>
     *
     * @param startTime startTime
     */
    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the value of duration .
     *
     * @return the value of long
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Sets the duration .
     * <p>You can use getDuration() to get the value of duration</p>
     *
     * @param duration duration
     */
    public void setDuration(final long duration) {
        this.duration = duration;
    }

    /**
     * Gets the value of root .
     *
     * @return the value of javax.swing.JComponent
     */
    public JComponent getRoot() {
        return root;
    }

    /**
     * Sets the root .
     * <p>You can use getRoot() to get the value of root</p>
     *
     * @param root root
     */
    public void setRoot(final JComponent root) {
        this.root = root;
    }

    /**
     * Gets the value of flagFocus .
     *
     * @return the value of boolean
     */
    public boolean isFlagFocus() {
        return flagFocus;
    }

    /**
     * Sets the flagFocus .
     * <p>You can use getFlagFocus() to get the value of flagFocus</p>
     *
     * @param flagFocus flagFocus
     */
    public void setFlagFocus(final boolean flagFocus) {
        this.flagFocus = flagFocus;
    }

    /**
     * Gets the value of max .
     *
     * @return the value of double
     */
    public double getMax() {
        return max;
    }

    /**
     * Sets the max .
     * <p>You can use getMax() to get the value of max</p>
     *
     * @param max max
     */
    public void setMax(double max) {
        this.max = max;
    }

    /**
     * Set selected state
     *
     * @param isSelected isSelected
     */
    public void select(final boolean isSelected) {
        this.isSelected = isSelected;
    }

    /**
     * Rewrite drawing method
     *
     * @param graphics graphics
     */
    @Override
    public void draw(final Graphics2D graphics) {
        if (isSelected) {
            drawSelect(graphics);
        } else {
            drawNoSelect(graphics);
        }
    }

    private void drawSelect(Graphics2D graphics) {
        double drawHeight = (value * (rect.height - 5) * 1.0) / max;
        Color color = ColorUtils.MD_PALETTE[cpu];
        graphics.setColor(color);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .7f));
        graphics.fillRect(Utils.getX(rect), Utils.getY(rect) + rect.height - (int) drawHeight, rect.width,
            (int) drawHeight);
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        Color borderColor = new Color(red <= redOff ? 0 : red - redOff, green <= greenOff ? 0 : green - greenOff,
            blue <= blueOff ? 0 : blue - blueOff);
        graphics.setColor(borderColor);
        graphics.fillRect(Utils.getX(rect), Utils.getY(rect) + rect.height - (int) drawHeight, rect.width, 3);
    }

    private void drawNoSelect(Graphics2D graphics) {
        double drawHeight = (value * (rect.height - 5) * 1.0) / max;
        Color color = ColorUtils.MD_PALETTE[cpu];
        graphics.setColor(color);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .7f));
        graphics.fillRect(Utils.getX(rect), Utils.getY(rect) + rect.height - (int) drawHeight, rect.width,
            (int) drawHeight);
    }

    @Override
    public void onFocus(final MouseEvent event) {
        if (eventListener != null) {
            eventListener.focus(event, this);
        }
    }

    @Override
    public void onBlur(final MouseEvent event) {
        if (eventListener != null) {
            eventListener.blur(event, this);
        }
    }

    @Override
    public void onClick(final MouseEvent event) {
    }

    @Override
    public void onMouseMove(final MouseEvent event) {
        if (edgeInspect(event)) {
            if (eventListener != null) {
                eventListener.mouseMove(event, this);
            }
        }
    }

    /**
     * Set callback event listener
     *
     * @param eventListener eventListener
     */
    public void setEventListener(final NodeEventListener eventListener) {
        this.eventListener = eventListener;
    }
}
