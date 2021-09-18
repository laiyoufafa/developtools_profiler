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

/**
 * TabCounterBean
 *
 * @date: 2021/5/12 16:34
 */
public class TabCounterBean {
    private Integer trackId;
    private String name;
    private Long deltaValue; // Calculate the rule. box selection range last value - first value
    private Double rate; // Calculate the rule .box selection range time delta value / range time(单位换算成 s )
    private Double weightAvgValue;

    // The calculation rule calculates the weighted average by value as a weight of time in the selected time area
    private Integer count; // Calculate the rules Statistics the number of counters in the selected range
    private Long firstValue; // Box within the selection room first value
    private Long lastValue; // Box within the selection room last value
    private Long minValue; // Box within the selection room min value
    private Long maxValue; // Box within the selection room max value

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

    /**
     * Gets the value of deltaValue .
     *
     * @return the value of deltaValue .
     */
    public Long getDeltaValue() {
        return deltaValue;
    }

    /**
     * Sets the deltaValue .
     * <p>You can use getDeltaValue() to get the value of deltaValue.</p>
     *
     * @param param .
     */
    public void setDeltaValue(final Long param) {
        this.deltaValue = param;
    }

    /**
     * Gets the value of rate .
     *
     * @return the value of rate .
     */
    public Double getRate() {
        return rate;
    }

    /**
     * Sets the rate .
     * <p>You can use getRate() to get the value of rate.</p>
     *
     * @param param .
     */
    public void setRate(final Double param) {
        this.rate = param;
    }

    /**
     * Gets the value of weightAvgValue .
     *
     * @return the value of weightAvgValue .
     */
    public Double getWeightAvgValue() {
        return weightAvgValue;
    }

    /**
     * Sets the weightAvgValue .
     * <p>You can use getWeightAvgValue() to get the value of weightAvgValue.</p>
     *
     * @param param .
     */
    public void setWeightAvgValue(final Double param) {
        this.weightAvgValue = param;
    }

    /**
     * Gets the value of count .
     *
     * @return the value of count .
     */
    public Integer getCount() {
        return count;
    }

    /**
     * Sets the count .
     * <p>You can use getCount() to get the value of count.</p>
     *
     * @param param .
     */
    public void setCount(final Integer param) {
        this.count = param;
    }

    /**
     * Gets the value of firstValue .
     *
     * @return the value of firstValue .
     */
    public Long getFirstValue() {
        return firstValue;
    }

    /**
     * Sets the firstValue .
     * <p>You can use getFirstValue() to get the value of firstValue.</p>
     *
     * @param param .
     */
    public void setFirstValue(final Long param) {
        this.firstValue = param;
    }

    /**
     * Gets the value of lastValue .
     *
     * @return the value of lastValue .
     */
    public Long getLastValue() {
        return lastValue;
    }

    /**
     * Sets the lastValue .
     * <p>You can use getLastValue() to get the value of lastValue.</p>
     *
     * @param param .
     */
    public void setLastValue(final Long param) {
        this.lastValue = param;
    }

    /**
     * Gets the value of minValue .
     *
     * @return the value of minValue .
     */
    public Long getMinValue() {
        return minValue;
    }

    /**
     * Sets the minValue .
     * <p>You can use getMinValue() to get the value of minValue.</p>
     *
     * @param param .
     */
    public void setMinValue(final Long param) {
        this.minValue = param;
    }

    /**
     * Gets the value of maxValue .
     *
     * @return the value of maxValue .
     */
    public Long getMaxValue() {
        return maxValue;
    }

    /**
     * Sets the maxValue .
     * <p>You can use getMaxValue() to get the value of maxValue.</p>
     *
     * @param param .
     */
    public void setMaxValue(final Long param) {
        this.maxValue = param;
    }
}
