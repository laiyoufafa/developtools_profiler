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

/**
 * Counter
 *
 * @date 2021/04/20 12:24
 */
public class Counter {
    /**
     * id
     */
    @DField(name = "id")
    private Integer id;

    /**
     * track id
     */
    @DField(name = "trackId")
    private Integer trackId;

    /**
     * track name
     */
    @DField(name = "name")
    private String name;

    /**
     * value
     */
    @DField(name = "value")
    private Long value;

    /**
     * start time
     */
    @DField(name = "startTime")
    private Long startTime;

    /**
     * Gets the value of id .
     *
     * @return the value of id .
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the id .
     * <p>You can use getId() to get the value of id.</p>
     *
     * @param param .
     */
    public void setId(final Integer param) {
        this.id = param;
    }

    /**
     * Gets the value of trackId .
     *
     * @return the value of trackId .
     */
    public Integer getTrackId() {
        return trackId;
    }

    /**
     * Sets the trackId .
     * <p>You can use getTrackId() to get the value of trackId.</p>
     *
     * @param param .
     */
    public void setTrackId(final Integer param) {
        this.trackId = param;
    }

    /**
     * Gets the value of value .
     *
     * @return the value of value .
     */
    public Long getValue() {
        return value;
    }

    /**
     * Sets the value .
     * <p>You can use getValue() to get the value of value.</p>
     *
     * @param param .
     */
    public void setValue(final Long param) {
        this.value = param;
    }

    /**
     * Gets the value of startTime .
     *
     * @return the value of startTime .
     */
    public Long getStartTime() {
        return startTime;
    }

    /**
     * Sets the startTime .
     * <p>You can use getStartTime() to get the value of startTime.</p>
     *
     * @param param .
     */
    public void setStartTime(final Long param) {
        this.startTime = param;
    }

    /**
     * Gets the value of name .
     *
     * @return the value of name .
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name .
     * <p>You can use getName() to get the value of name.</p>
     *
     * @param param .
     */
    public void setName(final String param) {
        this.name = param;
    }
}
