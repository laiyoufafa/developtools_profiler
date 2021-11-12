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
 * CpuFreqMax
 *
 * @since 2021/5/27 12:29
 */
public class CpuFreqMax {
    private final String[] units = new String[] {"", "K", "M", "G", "T", "E"};
    @DField(name = "maxFreq")
    private Integer maxFreq;
    private String name = "0 Ghz";
    private Double value = 0D;

    /**
     * Gets the value of name .
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name .
     * <p>You can use getName() to get the value of name</p>
     *
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the value of value .
     *
     * @return Long
     */
    public Double getValue() {
        return value;
    }

    /**
     * Sets the value .
     * <p>You can use getValue() to get the value of value</p>
     *
     * @param value value
     */
    public void setValue(Double value) {
        this.value = value;
    }

    /**
     * Gets the value of maxFreq .
     *
     * @return Integer
     */
    public Integer getMaxFreq() {
        return maxFreq;
    }

    /**
     * Sets the maxFreq .
     * <p>You can use getMaxFreq() to get the value of maxFreq</p>
     *
     * @param maxFreq maxFreq
     */
    public void setMaxFreq(Integer maxFreq) {
        this.maxFreq = maxFreq;
    }

    /**
     * get the max cpu freq
     *
     * @return CpuFreqMax
     */
    public CpuFreqMax math() {
        StringBuilder sb = new StringBuilder(" ");
        setName(" ");
        if (maxFreq > 0) {
            double log10 = Math.ceil(Math.log10(maxFreq));
            double pow10 = Math.pow(10, log10);
            double afterCeil = Math.ceil(maxFreq / (pow10 / 4)) * (pow10 / 4);
            setValue(afterCeil);
            double unitIndex = Math.floor(log10 / 3);
            sb.append(afterCeil / Math.pow(10, unitIndex * 3));
            sb.append(units[(int) unitIndex + 1]);
            sb.append("hz");
        }
        setName(sb.toString());
        return this;
    }
}
