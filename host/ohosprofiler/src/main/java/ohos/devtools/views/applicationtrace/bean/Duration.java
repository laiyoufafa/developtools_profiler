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

import ohos.devtools.views.trace.DField;

/**
 * Duration
 *
 * @since 2021/5/27 12:01
 */
public class Duration {
    @DField(name = "total")
    private Long total;
    @DField(name = "start_ts")
    private Long startTs;
    @DField(name = "end_ts")
    private Long endTs;

    /**
     * Gets the value of total .
     *
     * @return the value of java.lang.Long
     */
    public Long getTotal() {
        return total;
    }

    /**
     * Sets the total .
     * <p>You can use getTotal() to get the value of total</p>
     *
     * @param total total
     */
    public void setTotal(Long total) {
        this.total = total;
    }

    /**
     * Gets the value of startTs .
     *
     * @return the value of java.lang.Long
     */
    public Long getStartTs() {
        return startTs;
    }

    /**
     * Sets the startTs .
     * <p>You can use getStartTs() to get the value of startTs</p>
     *
     * @param startTs startTs
     */
    public void setStartTs(Long startTs) {
        this.startTs = startTs;
    }

    /**
     * Gets the value of endTs .
     *
     * @return the value of java.lang.Long
     */
    public Long getEndTs() {
        return endTs;
    }

    /**
     * Sets the endTs .
     * <p>You can use getEndTs() to get the value of endTs</p>
     *
     * @param endTs endTs
     */
    public void setEndTs(Long endTs) {
        this.endTs = endTs;
    }
}
