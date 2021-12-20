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

import java.math.BigDecimal;

import static java.math.BigDecimal.ROUND_HALF_UP;

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
     * @return the value of java.lang.String
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
     * @return the value of java.lang.Long
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
     * @return the value of java.lang.Integer
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
     * get the math maxFreq .
     *
     * @return CpuFreqMax CpuFreqMax
     */
    public CpuFreqMax math() {
        StringBuilder builder = new StringBuilder(" ");
        setName(" ");
        if (maxFreq > 0) {
            double log10 = Math.ceil(Math.log10(maxFreq));
            double pow10 = Math.pow(10, log10);
            double afterCeil = Math.ceil(maxFreq / (pow10 / 4)) * (pow10 / 4);
            setValue(afterCeil);
            double unitIndex = Math.floor(log10 / 3);
            BigDecimal divide =
                BigDecimal.valueOf(afterCeil).divide(BigDecimal.valueOf(Math.pow(10, unitIndex * 3)), 5, ROUND_HALF_UP);
            builder.append(divide);
            builder.append(units[(int) unitIndex + 1]);
            builder.append("hz");
        }
        setName(builder.toString());
        return this;
    }
}
