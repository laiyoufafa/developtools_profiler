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
import ohos.devtools.views.trace.component.AnalystPanel;
import ohos.devtools.views.trace.fragment.graph.AbstractGraph;
import ohos.devtools.views.trace.util.ColorUtils;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.Utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

/**
 * clock data
 *
 * @date 2021/04/22 12:25
 */
public class ClockData extends AbstractGraph {
    private final int padding1 = 2;
    private final int padding2 = 4;
    private final float alpha90 = .9f;
    private final int strOffsetY = 16;
    private final int redOff = 40;
    private final int greenOff = 60;
    private final int blueOff = 75;
    @DField(name = "filter_id")
    private Integer filterId;
    @DField(name = "ts")
    private Long startTime;
    @DField(name = "end_ts")
    private Long endTime;
    @DField(name = "dur")
    private Long duration;
    @DField(name = "type")
    private String type;
    @DField(name = "value")
    private Long value;
    private Long delta;
    private Long minValue; // Save the smallest value in the entire row of data
    private Long maxValue; // Save the largest value in the entire row of data
    private javax.swing.JComponent root;
    private boolean isSelected; // Whether to be selected
    private IEventListener eventListener;

    /**
     * ui control extension field.
     */
    public ClockData() {
    }

    /**
     * get min value
     *
     * @return Long min value
     */
    public Long getMinValue() {
        return minValue;
    }

    /**
     * set min value
     *
     * @param minValue value
     */
    public void setMinValue(Long minValue) {
        this.minValue = minValue;
    }

    /**
     * get max value
     *
     * @return long max value
     */
    public Long getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Long maxValue) {
        this.maxValue = maxValue;
    }

    /**
     * get the number of cpu .
     *
     * @return int Returns the number of cpu
     */
    public int getFilterId() {
        return filterId;
    }

    /**
     * set the value of cpu .
     *
     * @param filterId Set the number of cpu
     */
    public void setFilterId(final int filterId) {
        this.filterId = filterId;
    }

    /**
     * get the startTime .
     *
     * @return long
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * set the startTime .
     *
     * @param startTime startTime
     */
    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }

    /**
     * get the duration .
     *
     * @return long
     */
    public Long getDuration() {
        return duration;
    }

    /**
     * set the duration .
     *
     * @param duration duration
     */
    public void setDuration(final Long duration) {
        this.duration = duration;
    }

    /**
     * get the type .
     *
     * @return String
     */
    public String getType() {
        return type;
    }

    /**
     * set the type .
     *
     * @param type type
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * get value
     *
     * @return value
     */
    public Long getValue() {
        return value;
    }

    /**
     * set value
     *
     * @param value value
     */
    public void setValue(Long value) {
        this.value = value;
    }

    /**
     * get delta value
     *
     * @return delta value
     */
    public Long getDelta() {
        return delta;
    }

    /**
     * set delta value
     *
     * @param delta delta value
     */
    public void setDelta(Long delta) {
        this.delta = delta;
    }

    /**
     * Get rootcomponent
     *
     * @return javax.swing.JComponent
     */
    public javax.swing.JComponent getRoot() {
        return root;
    }

    /**
     * Set to get rootcomponent
     *
     * @param root root
     */
    public void setRoot(final javax.swing.JComponent root) {
        this.root = root;
    }

    /**
     * Draw graphics based on attributes
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
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        graphics.setColor(Color.white);
        Rectangle rectangle = new Rectangle();
        graphics.setFont(Final.SMALL_FONT);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha90));
        rectangle.setRect(rect.getX(), rect.getY() + strOffsetY, rect.getWidth(), rect.getHeight());
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        graphics.setFont(Final.NORMAL_FONT);
    }

    private void drawSelect(final Graphics2D graphics) {
        Color color = ColorUtils.colorForTid(maxValue.intValue());
        graphics.setColor(color);
        double tmpHeight = rect.height * value * 1.0 / maxValue;
        if (tmpHeight <= 0) {
            tmpHeight = 1;
        }
        int yAxis = (int) (rect.getY() + rect.height - tmpHeight);
        int xAxis = (int) rect.getX();
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        graphics.fillRect(xAxis, yAxis, rect.width, (int) tmpHeight);
        graphics.drawRect(xAxis, yAxis, rect.width, (int) tmpHeight);
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        Color borderColor = new Color(red <= redOff ? 0 : red - redOff,
            green <= greenOff ? 0 : green - greenOff, blue <= blueOff ? 0 : blue - blueOff);
        graphics.setColor(borderColor);
        graphics.fillRect(xAxis, yAxis, rect.width, 3);
    }

    private void drawNoSelect(final Graphics2D graphics) {
        Color color = ColorUtils.colorForTid(maxValue.intValue());
        graphics.setColor(color);
        double tmpHeight = rect.height * value * 1.0 / maxValue;
        if (tmpHeight <= 0) {
            tmpHeight = 1;
        }
        int yAxis = (int) (rect.getY() + rect.height - tmpHeight);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        graphics.fillRect((int) rect.getX(), yAxis, rect.width, (int) tmpHeight);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        graphics.drawRect((int) rect.getX(), yAxis, rect.width, (int) tmpHeight);
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
     * Redraw the current page
     */
    public void repaint() {
        if (root != null) {
            root.repaint(Utils.getX(rect), Utils.getY(rect) - padding1, rect.width, rect.height + padding2);
        }
    }

    /**
     * Focus acquisition event
     *
     * @param event event
     */
    @Override
    public void onFocus(final MouseEvent event) {
        if (eventListener != null) {
            eventListener.focus(event, this);
        }
    }

    /**
     * Focus loss event
     *
     * @param event event
     */
    @Override
    public void onBlur(final MouseEvent event) {
        if (eventListener != null) {
            eventListener.blur(event, this);
        }
    }

    /**
     * Click event
     *
     * @param event event
     */
    @Override
    public void onClick(final MouseEvent event) {
        if (eventListener != null) {
            AnalystPanel.clicked = true;
            eventListener.click(event, this);
        }
    }

    /**
     * Mouse movement event
     *
     * @param event event
     */
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
    public void setEventListener(final IEventListener eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * Listener
     */
    public interface IEventListener {
        /**
         * Mouse click event
         *
         * @param event event
         * @param data  data
         */
        void click(MouseEvent event, ClockData data);

        /**
         * Mouse blur event
         *
         * @param event event
         * @param data  data
         */
        void blur(MouseEvent event, ClockData data);

        /**
         * Mouse focus event
         *
         * @param event event
         * @param data  data
         */
        void focus(MouseEvent event, ClockData data);

        /**
         * Mouse move event
         *
         * @param event event
         * @param data  data
         */
        void mouseMove(MouseEvent event, ClockData data);
    }
}
