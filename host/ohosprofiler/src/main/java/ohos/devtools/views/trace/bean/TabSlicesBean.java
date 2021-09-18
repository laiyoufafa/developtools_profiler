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
 * TabSlicesBean
 *
 * @version 1.0.1
 * @date 2021/04/20 12:12
 */
public class TabSlicesBean {
    /**
     * function name
     */
    @DField(name = "funName")
    private String funName;

    /**
     * total wall duration
     */
    @DField(name = "wallDuration")
    private Long wallDuration;

    /**
     * avg wall duration
     */
    @DField(name = "avgDuration")
    private Double avgDuration;

    /**
     * count
     */
    @DField(name = "occurrences")
    private Integer occurrences;

    /**
     * Gets the value of funName .
     *
     * @return the value of funName .
     */
    public String getFunName() {
        return funName;
    }

    /**
     * Sets the threadName .
     * <p>You can use getThreadName() to get the value of threadName.</p>
     *
     * @param param .
     */
    public void setFunName(final String param) {
        this.funName = param;
    }

    /**
     * Gets the value of allDuration .
     *
     * @return the value of allDuration .
     */
    public Long getWallDuration() {
        return wallDuration;
    }

    /**
     * Sets the allDuration .
     * <p>You can use getAllDuration() to get the value of allDuration.</p>
     *
     * @param param .
     */
    public void setWallDuration(final long param) {
        this.wallDuration = param;
    }

    /**
     * Gets the value of avgDuration .
     *
     * @return the value of avgDuration .
     */
    public Double getAvgDuration() {
        return avgDuration;
    }

    /**
     * Sets the avgDuration .
     * <p>You can use getAvgDuration() to get the value of avgDuration.</p>
     *
     * @param param .
     */
    public void setAvgDuration(final double param) {
        this.avgDuration = param;
    }

    /**
     * Gets the value of occurrences .
     *
     * @return the value of occurrences .
     */
    public Integer getOccurrences() {
        return occurrences;
    }

    /**
     * Sets the occurrences .
     * <p>You can use getOccurrences() to get the value of occurrences.</p>
     *
     * @param param .
     */
    public void setOccurrences(final int param) {
        this.occurrences = param;
    }
}
