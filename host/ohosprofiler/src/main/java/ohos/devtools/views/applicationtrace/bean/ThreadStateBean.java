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

/**
 * TabThreadStates
 *
 * @since 2021/04/20 12:12
 */
public class ThreadStateBean {
    private String state;
    private String duration;
    private String percent;
    private String occurrences;

    /**
     * Gets the value of state .
     *
     * @return the value of state .
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state .
     * <p>You can use getState() to get the value of state.</p>
     *
     * @param param .
     */
    public void setState(final String param) {
        this.state = param;
    }

    /**
     * Gets the value of duration .
     *
     * @return the value of duration .
     */
    public String getDuration() {
        return duration;
    }

    /**
     * Sets the duration .
     * <p>You can use getDuration() to get the value of duration.</p>
     *
     * @param param .
     */
    public void setDuration(final String param) {
        this.duration = param;
    }

    /**
     * Gets the value of percent .
     *
     * @return the value of percent .
     */
    public String getPercent() {
        return percent;
    }

    /**
     * Sets the percent .
     * <p>You can use getPercent() to get the value of percent.</p>
     *
     * @param param .
     */
    public void setPercent(final String param) {
        this.percent = param;
    }

    /**
     * Gets the value of occurrences .
     *
     * @return the value of occurrences .
     */
    public String getOccurrences() {
        return occurrences;
    }

    /**
     * Sets the occurrences .
     * <p>You can use getOccurrences() to get the value of occurrences.</p>
     *
     * @param param .
     */
    public void setOccurrences(final String param) {
        this.occurrences = param;
    }
}
