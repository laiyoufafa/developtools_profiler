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
 * @version 1.0
 * @date 2021/4/24 18:04
 **/
class CpuFreqDataTest {
    /**
     * test get the number of cpu .
     */
    @Test
    void getCpu() {
        assertEquals(3, new CpuFreqData() {{
            setCpu(3);
        }}.getCpu());
    }

    /**
     * test set the number of cpu .
     */
    @Test
    void setCpu() {
        assertEquals(3, new CpuFreqData() {{
            setCpu(3);
        }}.getCpu());
    }

    /**
     * test set the number .
     */
    @Test
    void getValue() {
        assertEquals(3L, new CpuFreqData() {{
            setValue(3L);
        }}.getValue());
    }

    /**
     * test set the value .
     */
    @Test
    void setValue() {
        assertEquals(3L, new CpuFreqData() {{
            setValue(3L);
        }}.getValue());
    }

    /**
     * test get the StartTime .
     */
    @Test
    void getStartTime() {
        assertEquals(3L, new CpuFreqData() {{
            setStartTime(3L);
        }}.getStartTime());
    }

    /**
     * test set the StartTime .
     */
    @Test
    void setStartTime() {
        assertEquals(3L, new CpuFreqData() {{
            setStartTime(3L);
        }}.getStartTime());
    }

    /**
     * test get the Duration .
     */
    @Test
    void getDuration() {
        assertEquals(3L, new CpuFreqData() {{
            setDuration(3L);
        }}.getDuration());
    }

    /**
     * test set the Duration .
     */
    @Test
    void setDuration() {
        assertEquals(3L, new CpuFreqData() {{
            setDuration(3L);
        }}.getDuration());
    }

    /**
     * test get the root .
     */
    @Test
    void getRoot() {
        JComponent jComponent = new JButton();
        assertEquals(jComponent, new CpuFreqData() {{
            setRoot(jComponent);
        }}.getRoot());
    }

    /**
     * test set the root .
     */
    @Test
    void setRoot() {
        JComponent jComponent = new JButton();
        assertEquals(jComponent, new CpuFreqData() {{
            setRoot(jComponent);
        }}.getRoot());
    }

    /**
     * test get the FlagFocus .
     */
    @Test
    void isFlagFocus() {
        boolean flagFocus = true;
        assertEquals(flagFocus, new CpuFreqData() {{
            setFlagFocus(flagFocus);
        }}.isFlagFocus());
    }

    /**
     * test set the FlagFocus .
     */
    @Test
    void setFlagFocus() {
        boolean flagFocus = true;
        assertEquals(flagFocus, new CpuFreqData() {{
            setFlagFocus(flagFocus);
        }}.isFlagFocus());
    }

    /**
     * test get the max .
     */
    @Test
    void getMax() {
        assertEquals(10.0D, new CpuFreqData() {{
            setMax(10.0D);
        }}.getMax());
    }

    /**
     * test set the max .
     */
    @Test
    void setMax() {
        assertEquals(10.0D, new CpuFreqData() {{
            setMax(10.0D);
        }}.getMax());
    }

}