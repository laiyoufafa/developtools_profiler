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
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

/**
 * cpu frequency data
 *
 * @since 2021/04/22 12:25
 */
public class CpuFreqData extends AbstractGraph {
    @DField(name = "cpu")
    private int cpu;

    @DField(name = "value")
    private long value;

    @DField(name = "startNS")
    private long startTime;

    private long duration;

    private JComponent root;

    private boolean flagFocus;

    private double max;

    /**
     * Empty parameter construction method
     */
    public CpuFreqData() {
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
     * Rewrite drawing method
     *
     * @param graphics graphics
     */
    @Override
    public void draw(final Graphics2D graphics) {
        double drawHeight = (value * (rect.height - 5) * 1.0) / max;
        graphics.setColor(ColorUtils.MD_PALETTE[cpu]);
        graphics.fillRect(Utils.getX(rect), Utils.getY(rect) + rect.height - (int) drawHeight, rect.width,
            (int) drawHeight);
    }

    @Override
    public void onFocus(final MouseEvent event) {
    }

    @Override
    public void onBlur(final MouseEvent event) {
    }

    @Override
    public void onClick(final MouseEvent event) {
    }

    @Override
    public void onMouseMove(final MouseEvent event) {
    }

}
