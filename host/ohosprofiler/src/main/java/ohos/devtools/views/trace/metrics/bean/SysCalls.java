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

import java.util.Objects;

/**
 * Sys Calls
 */
public class SysCalls {
    @DField(name = "minDur")
    private int minDur;
    @DField(name = "avgDur")
    private int avgDur;
    @DField(name = "maxDur")
    private int maxDur;
    @DField(name = "funName")
    private String funName;

    public int getMinDur() {
        return minDur;
    }

    public void setMinDur(int minDur) {
        this.minDur = minDur;
    }

    public int getAvgDur() {
        return avgDur;
    }

    public void setAvgDur(int avgDur) {
        this.avgDur = avgDur;
    }

    public int getMaxDur() {
        return maxDur;
    }

    public void setMaxDur(int maxDur) {
        this.maxDur = maxDur;
    }

    public String getFunName() {
        return funName;
    }

    public void setFunName(String funName) {
        this.funName = funName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SysCalls sysCalls = (SysCalls) obj;
        return minDur == sysCalls.minDur && avgDur == sysCalls.avgDur && maxDur == sysCalls.maxDur && Objects
            .equals(funName, sysCalls.funName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minDur, avgDur, maxDur, funName);
    }

    @Override
    public String toString() {
        return "SysCalls{" + "minDur=" + minDur + ", avgDur=" + avgDur + ", maxDur=" + maxDur + ", funName='" + funName
            + '\'' + '}';
    }
}
