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
 * cup usage rate entity class
 *
 * @version 1.0
 * @date 2021/04/22 12:25
 **/
public class CpuRateBean {
    private int cpu;

    private int index;

    private double rate;

    /**
     * Gets the value of cpu .
     *
     * @return the value of int
     */
    public int getCpu() {
        return cpu;
    }

    /**
     * Sets the cpu .
     * <p>You can use getCpu() to get the value of cpu</p>
     *
     * @param cpu cpu
     */
    public void setCpu(final int cpu) {
        this.cpu = cpu;
    }

    /**
     * Gets the value of index .
     *
     * @return the value of int
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the index .
     * <p>You can use getIndex() to get the value of index</p>
     *
     * @param index index
     */
    public void setIndex(final int index) {
        this.index = index;
    }

    /**
     * Gets the value of rate .
     *
     * @return the value of double
     */
    public double getRate() {
        return rate;
    }

    /**
     * Sets the rate .
     * <p>You can use getRate() to get the value of rate</p>
     *
     * @param rate rate
     */
    public void setRate(final double rate) {
        this.rate = rate;
    }
}
