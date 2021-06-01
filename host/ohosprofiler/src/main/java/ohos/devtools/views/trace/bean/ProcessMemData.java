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

import ohos.devtools.views.trace.fragment.graph.AbstractGraph;
import ohos.devtools.views.trace.util.ColorUtils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

/**
 * Process memory data
 *
 * @version 1.0
 * @date 2021/04/22 12:25
 **/
public class ProcessMemData extends AbstractGraph {
    private int maxValue;

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

    private int id;

    private String type;

    private int trackId;

    private int value;

    private long startTime;

    private long duration;

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
            graphics.fillRect(rect.x, rect.y + rect.height - height, rect.width, height);
        }
    }

    /**
     * Focus acquisition event
     *
     * @param event event
     */
    @Override
    public void onFocus(final MouseEvent event) {
    }

    /**
     * Focus cancel event
     *
     * @param event event
     */
    @Override
    public void onBlur(final MouseEvent event) {
    }

    /**
     * Click event
     *
     * @param event event
     */
    @Override
    public void onClick(final MouseEvent event) {
    }

    /**
     * Mouse movement event
     *
     * @param event event
     */
    @Override
    public void onMouseMove(final MouseEvent event) {
    }
}
