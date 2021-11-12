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
 * Wakeup Time data
 *
 * @since 2021/5/14 15:52
 */
public class WakeupTime {
    @DField(name = "wakeTs")
    private Long wakeTs;
    @DField(name = "start_ts")
    private Long startTs;
    @DField(name = "preRow")
    private Long preRow;

    /**
     * Gets the value of wakeTs .
     *
     * @return the value of wakeTs .
     */
    public Long getWakeTs() {
        return wakeTs;
    }

    /**
     * Sets the wakeTs .
     * <p>You can use getWakeTs() to get the value of wakeTs.</p>
     *
     * @param param .
     */
    public void setWakeTs(final Long param) {
        this.wakeTs = param;
    }

    /**
     * Gets the value of startTs .
     *
     * @return the value of startTs .
     */
    public Long getStartTs() {
        return startTs;
    }

    /**
     * Sets the startTs .
     * <p>You can use getStartTs() to get the value of startTs.</p>
     *
     * @param param .
     */
    public void setStartTs(final Long param) {
        this.startTs = param;
    }

    /**
     * Gets the value of preRow .
     *
     * @return the value of preRow .
     */
    public Long getPreRow() {
        return preRow;
    }

    /**
     * Sets the preRow .
     * <p>You can use getPreRow() to get the value of preRow.</p>
     *
     * @param param .
     */
    public void setPreRow(final Long param) {
        this.preRow = param;
    }
}
