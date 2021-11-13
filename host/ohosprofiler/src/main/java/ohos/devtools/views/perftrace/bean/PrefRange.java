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

package ohos.devtools.views.perftrace.bean;

import ohos.devtools.views.trace.DField;

/**
 * PrefRange from  db file
 *
 * @since 2021/5/12 16:34
 */
public class PrefRange {
    @DField(name = "startTime")
    private long startTime;

    @DField(name = "endTime")
    private long endTime;

    /**
     * get the startTime
     *
     * @return long startTime
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * set the startTime
     *
     * @param startTime startTime
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * get the endTime
     *
     * @return long endTime
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * set the endTime
     *
     * @param endTime endTime
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

}
