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

import javax.swing.JButton;
import javax.swing.JComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * test CpuFreqData class
 *
 * @since 2021/4/24 18:04
 */
class CpuFreqDataTest {
    /**
     * test get the number of cpu .
     */
    @Test
    void getCpu() {
        CpuFreqData cpuFreqData = new CpuFreqData();
        cpuFreqData.setCpu(3);
        assertEquals(3, cpuFreqData.getCpu());
    }

    /**
     * test set the number of cpu .
     */
    @Test
    void setCpu() {
        CpuFreqData cpuFreqData = new CpuFreqData();
        cpuFreqData.setCpu(3);
        assertEquals(3, cpuFreqData.getCpu());
    }

    /**
     * test set the number .
     */
    @Test
    void getValue() {
        CpuFreqData cpuFreqData = new CpuFreqData();
        cpuFreqData.setValue(3L);
        assertEquals(3L, cpuFreqData.getValue());
    }

    /**
     * test set the value .
     */
    @Test
    void setValue() {
        CpuFreqData cpuFreqData = new CpuFreqData();
        cpuFreqData.setValue(3L);
        assertEquals(3L, cpuFreqData.getValue());
    }

    /**
     * test get the StartTime .
     */
    @Test
    void getStartTime() {
        CpuFreqData cpuFreqData = new CpuFreqData();
        cpuFreqData.setStartTime(3L);
        assertEquals(3L, cpuFreqData.getStartTime());
    }

    /**
     * test set the StartTime .
     */
    @Test
    void setStartTime() {
        CpuFreqData cpuFreqData = new CpuFreqData();
        cpuFreqData.setStartTime(3L);
        assertEquals(3L, cpuFreqData.getStartTime());
    }

    /**
     * test get the Duration .
     */
    @Test
    void getDuration() {
        CpuFreqData cpuFreqData = new CpuFreqData();
        cpuFreqData.setDuration(3L);
        assertEquals(3L, cpuFreqData.getDuration());
    }

    /**
     * test set the Duration .
     */
    @Test
    void setDuration() {
        CpuFreqData cpuFreqData = new CpuFreqData();
        cpuFreqData.setDuration(3L);
        assertEquals(3L, cpuFreqData.getDuration());
    }

    /**
     * test get the root .
     */
    @Test
    void getRoot() {
        JComponent jComponent = new JButton();
        CpuFreqData cpuFreqData = new CpuFreqData();
        cpuFreqData.setRoot(jComponent);
        assertEquals(jComponent, cpuFreqData.getRoot());
    }

    /**
     * test set the root .
     */
    @Test
    void setRoot() {
        JComponent jComponent = new JButton();
        CpuFreqData cpuFreqData = new CpuFreqData();
        cpuFreqData.setRoot(jComponent);
        assertEquals(jComponent, cpuFreqData.getRoot());
    }

    /**
     * test get the FlagFocus .
     */
    @Test
    void isFlagFocus() {
        boolean flagFocus = true;
        CpuFreqData cpuFreqData = new CpuFreqData();
        cpuFreqData.setFlagFocus(cpuFreqData.isFlagFocus());
        assertEquals(flagFocus, cpuFreqData.isFlagFocus());
    }

    /**
     * test set the FlagFocus .
     */
    @Test
    void setFlagFocus() {
        boolean flagFocus = true;
        CpuFreqData cpuFreqData = new CpuFreqData();
        cpuFreqData.setFlagFocus(cpuFreqData.isFlagFocus());
        assertEquals(flagFocus, cpuFreqData.isFlagFocus());
    }

    /**
     * test get the max .
     */
    @Test
    void getMax() {
        CpuFreqData cpuFreqData = new CpuFreqData();
        cpuFreqData.setMax(10.0D);
        assertEquals(10.0D, cpuFreqData.getMax());
    }

    /**
     * test set the max .
     */
    @Test
    void setMax() {
        CpuFreqData cpuFreqData = new CpuFreqData();
        cpuFreqData.setMax(10.0D);
        assertEquals(10.0D, cpuFreqData.getMax());
    }

}