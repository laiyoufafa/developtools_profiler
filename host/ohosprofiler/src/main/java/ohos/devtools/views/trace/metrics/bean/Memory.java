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

package ohos.devtools.views.trace.metrics.bean;

import ohos.devtools.views.trace.DField;

/**
 * Memory data
 *
 */
public class Memory {
    @DField(name = "maxNum")
    private Integer maxNum;
    @DField(name = "minNum")
    private Integer minNum;
    @DField(name = "avgNum")
    private float avgNum;
    @DField(name = "name")
    private String name;
    @DField(name = "processName")
    private String processName;

    public Integer getMaxNum() {
        return maxNum;
    }

    public void setMaxNum(Integer maxNum) {
        this.maxNum = maxNum;
    }

    public Integer getMinNum() {
        return minNum;
    }

    public void setMinNum(Integer minNum) {
        this.minNum = minNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public float getAvgNum() {
        return avgNum;
    }

    public void setAvgNum(float avgNum) {
        this.avgNum = avgNum;
    }

    @Override
    public String toString() {
        return "Memory{" + "maxNum=" + maxNum + ", minNum=" + minNum + ", avgNum=" + avgNum + ", name='" + name + '\''
            + ", processName='" + processName + '\'' + '}';
    }
}
