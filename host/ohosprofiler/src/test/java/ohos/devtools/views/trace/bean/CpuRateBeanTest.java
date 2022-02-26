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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * test CpuRateBean class
 *
 * @date 2021/4/24 18:04
 */
class CpuRateBeanTest {
    /**
     * test get the number of cpu .
     */
    @Test
    void getCpu() {
        CpuRateBean cpuRateBean = new CpuRateBean();
        cpuRateBean.setCpu(3);
        assertEquals(3, cpuRateBean.getCpu());
    }

    /**
     * test set the number of cpu .
     */
    @Test
    void setCpu() {
        CpuRateBean cpuRateBean = new CpuRateBean();
        cpuRateBean.setCpu(3);
        assertEquals(3, cpuRateBean.getCpu());
    }

    /**
     * test get the index .
     */
    @Test
    void getIndex() {
        CpuRateBean cpuRateBean = new CpuRateBean();
        cpuRateBean.setIndex(3);
        assertEquals(3, cpuRateBean.getIndex());
    }

    /**
     * test set the index .
     */
    @Test
    void setIndex() {
        CpuRateBean cpuRateBean = new CpuRateBean();
        cpuRateBean.setIndex(3);
        assertEquals(3, cpuRateBean.getIndex());
    }

    /**
     * test get the rate .
     */
    @Test
    void getRate() {
        CpuRateBean cpuRateBean = new CpuRateBean();
        cpuRateBean.setRate(3.0D);
        assertEquals(3.0D, cpuRateBean.getRate());
    }

    /**
     * test set the rate .
     */
    @Test
    void setRate() {
        CpuRateBean cpuRateBean = new CpuRateBean();
        cpuRateBean.setRate(3.0D);
        assertEquals(3.0D, cpuRateBean.getRate());
    }
}