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
 * Process memory data
 *
 * @since 2021/04/22 12:25
 */
public class ProcessMemData extends AbstractGraph {
    private final int padding1 = 2;
    private final int padding2 = 4;
    private int maxValue;
    private int id;
    private final float alpha90 = .9f;
    private final int strOffsetY = 16;
    private final int redOff = 40;
    private final int greenOff = 60;
    private final int blueOff = 75;
    private boolean isSelected; // Whether to be selected
    @DField(name = "type")
    private String type;
    @DField(name = "track_id")
    private int trackId;
    @DField(name = "value")
    private int value;
    @DField(name = "startTime")
    private long startTime;
    private long duration;
    private Long delta;
    private IEventListener eventListener;

    /**
     * Gets the value of maxValue .
     *
     * @return the value of int
     */
    public int getMaxValue() {
        return maxValue;
    }

    /**
     * Sets the maxValue .
     * <p>You can use getMaxValue() to get the value of maxValue</p>
     *
     * @param max max
     */
    public void setMaxValue(final int max) {
        this.maxValue = max;
    }

    /**
     * Gets the value of id .
     *
     * @return the value of int
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id .
     * <p>You can use getId() to get the value of id</p>
     *
     * @param id id
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Gets the value of type .
     *
     * @return the value of java.lang.String
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type .
     * <p>You can use getType() to get the value of type</p>
     *
     * @param type type
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Gets the value of trackId .
     *
     * @return the value of int
     */
    public int getTrackId() {
        return trackId;
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
     * Sets the trackId .
     * <p>You can use getTrackId() to get the value of trackId</p>
     *
     * @param id id
     */
    public void setTrackId(final int id) {
        this.trackId = id;
    }

    /**
     * Gets the value of value .
     *
     * @return the value of int
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets the value .
     * <p>You can use getValue() to get the value of value</p>
     *
     * @param value value
     */
    public void setValue(final int value) {
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
     * @param time time
     */
    public void setStartTime(final long time) {
        this.startTime = time;
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
     * @param dur dur
     */
    public void setDuration(final long dur) {
        this.duration = dur;
    }

    /**
     * Draw the corresponding shape according to the brush
     *
     * @param graphics graphics
     */
    @Override
    public void draw(final Graphics2D graphics) {
        Color color = ColorUtils.MD_PALETTE[trackId % ColorUtils.MD_PALETTE.length];
        int height = 0;
        if (maxValue > 0) {
            height = ((rect.height - 5) * value) / maxValue;
            graphics.setColor(color);
            graphics.fillRect(Utils.getX(rect), Utils.getY(rect) + rect.height - height, rect.width, height);
        }
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
        Color color = ColorUtils.colorForTid(maxValue);
        graphics.setColor(color);
        double tmpHeight = (rect.height - 5) * value * 1.0 / maxValue;
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
        Color borderColor = new Color(red <= redOff ? 0 : red - redOff, green <= greenOff ? 0 : green - greenOff,
            blue <= blueOff ? 0 : blue - blueOff);
        graphics.setColor(borderColor);
        graphics.fillRect(xAxis, yAxis, rect.width, 3);
    }

    private void drawNoSelect(final Graphics2D graphics) {
        Color color = ColorUtils.colorForTid(maxValue);
        graphics.setColor(color);
        //        int offset = rect.height/5;//value 为max的话 y = offset
        double tmpHeight = (rect.height - 5) * value * 1.0 / maxValue;
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
         * @param data data
         */
        void click(MouseEvent event, ProcessMemData data);

        /**
         * Mouse blur event
         *
         * @param event event
         * @param data data
         */
        void blur(MouseEvent event, ProcessMemData data);

        /**
         * Mouse focus event
         *
         * @param event event
         * @param data data
         */
        void focus(MouseEvent event, ProcessMemData data);

        /**
         * Mouse move event
         *
         * @param event event
         * @param data data
         */
        void mouseMove(MouseEvent event, ProcessMemData data);
    }
}
