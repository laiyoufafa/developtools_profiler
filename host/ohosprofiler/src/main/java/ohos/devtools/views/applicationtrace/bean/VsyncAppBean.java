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

package ohos.devtools.views.applicationtrace.bean;

import com.intellij.util.ui.JBUI;
import ohos.devtools.views.trace.AbstractNode;
import ohos.devtools.views.trace.DField;
import ohos.devtools.views.trace.util.Utils;

import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;

/**
 * VsyncAppBean
 *
 * @version 1.0
 * @date: 2021/5/26 15:38
 */
public class VsyncAppBean extends AbstractNode {
    @DField(name = "id")
    private Integer id;
    @DField(name = "type")
    private String type;
    @DField(name = "track_id")
    private Integer trackId;
    @DField(name = "value")
    private Double value;
    @DField(name = "startTime")
    private Long startTime;
    private Long duration;

    /**
     * Gets the value of duration .
     *
     * @return the value of java.lang.Long
     */
    public Long getDuration() {
        return duration;
    }

    /**
     * Sets the duration .
     * <p>You can use getDuration() to get the value of duration</p>
     *
     * @param dur duration
     */
    public void setDuration(final Long dur) {
        this.duration = dur;
    }

    /**
     * Gets the value of id .
     *
     * @return the value of java.lang.Integer
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the id .
     * <p>You can use getId() to get the value of id</p>
     *
     * @param param id
     */
    public void setId(final Integer param) {
        this.id = param;
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
     * @param param type
     */
    public void setType(final String param) {
        this.type = param;
    }

    /**
     * Gets the value of trackId .
     *
     * @return the value of java.lang.Integer
     */
    public Integer getTrackId() {
        return trackId;
    }

    /**
     * Sets the trackId .
     * <p>You can use getTrackId() to get the value of trackId</p>
     *
     * @param param track id
     */
    public void setTrackId(final Integer param) {
        this.trackId = param;
    }

    /**
     * Gets the value of value .
     *
     * @return the value of java.lang.Double
     */
    public Double getValue() {
        return value;
    }

    /**
     * Sets the value .
     * <p>You can use getValue() to get the value of value</p>
     *
     * @param param value
     */
    public void setValue(final Double param) {
        this.value = param;
    }

    /**
     * Gets the value of startTime .
     *
     * @return the value of java.lang.Integer
     */
    public Long getStartTime() {
        return startTime;
    }

    /**
     * Sets the startTime .
     * <p>You can use getStartTime() to get the value of startTime</p>
     *
     * @param param start time
     */
    public void setStartTime(final Long param) {
        this.startTime = param;
    }

    @Override
    public void draw(Graphics2D paint) {
        paint.setColor(JBUI.CurrentTheme.Link.linkColor().brighter());
        if (value == 0) {
            paint.drawLine(Utils.getX(rect), Utils.getY(rect) + rect.height - 1, Utils.getX(rect) + rect.width,
                Utils.getY(rect) + rect.height - 1);
        } else {
            paint.drawLine(Utils.getX(rect), Utils.getY(rect), Utils.getX(rect), Utils.getY(rect) + rect.height - 1);
            paint.drawLine(Utils.getX(rect), Utils.getY(rect), Utils.getX(rect) + rect.width, Utils.getY(rect));
            paint.drawLine(Utils.getX(rect) + rect.width, Utils.getY(rect), Utils.getX(rect) + rect.width,
                Utils.getY(rect) + rect.height - 1);
        }
    }

    @Override
    public List<String> getStringList(String time) {
        return Arrays.asList(
            time,
            "Value: " + value.intValue()
        );
    }
}
