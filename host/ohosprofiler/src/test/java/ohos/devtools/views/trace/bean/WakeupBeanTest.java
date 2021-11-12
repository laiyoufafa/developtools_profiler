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
 * test WakeupBean class
 *
 * @since 2021/4/24 18:05
 */
class WakeupBeanTest {
    /**
     * test get the WakeupTime .
     */
    @Test
    void getWakeupTime() {
        assertEquals(3L, new WakeupBean() {{
            setWakeupTime(3L);
        }}.getWakeupTime());
    }

    /**
     * test set the WakeupTime .
     */
    @Test
    void setWakeupTime() {
        assertEquals(3L, new WakeupBean() {{
            setWakeupTime(3L);
        }}.getWakeupTime());
    }

    /**
     * test get the WakeupCpu .
     */
    @Test
    void getWakeupCpu() {
        assertEquals(3, new WakeupBean() {{
            setWakeupCpu(3);
        }}.getWakeupCpu());
    }

    /**
     * test set the WakeupCpu .
     */
    @Test
    void setWakeupCpu() {
        assertEquals(3, new WakeupBean() {{
            setWakeupCpu(3);
        }}.getWakeupCpu());
    }

    /**
     * test get the WakeupProcess .
     */
    @Test
    void getWakeupProcess() {
        assertEquals("WakeupProcess", new WakeupBean() {{
            setWakeupProcess("WakeupProcess");
        }}.getWakeupProcess());
    }

    /**
     * test set the WakeupProcess .
     */
    @Test
    void setWakeupProcess() {
        assertEquals("WakeupProcess", new WakeupBean() {{
            setWakeupProcess("WakeupProcess");
        }}.getWakeupProcess());
    }

    /**
     * test get the WakeupPid .
     */
    @Test
    void getWakeupPid() {
        assertEquals(0, new WakeupBean() {{
            setWakeupPid(0);
        }}.getWakeupPid());
    }

    /**
     * test set the WakeupPid .
     */
    @Test
    void setWakeupPid() {
        assertEquals(0, new WakeupBean() {{
            setWakeupPid(0);
        }}.getWakeupPid());
    }

    /**
     * test get the WakeupThread .
     */
    @Test
    void getWakeupThread() {
        assertEquals("WakeupThread", new WakeupBean() {{
            setWakeupThread("WakeupThread");
        }}.getWakeupThread());
    }

    /**
     * test set the WakeupThread .
     */
    @Test
    void setWakeupThread() {
        assertEquals("WakeupThread", new WakeupBean() {{
            setWakeupThread("WakeupThread");
        }}.getWakeupThread());
    }

    /**
     * test get the WakeupTid .
     */
    @Test
    void getWakeupTid() {
        assertEquals(0, new WakeupBean() {{
            setWakeupTid(0);
        }}.getWakeupTid());
    }

    /**
     * test set the WakeupTid .
     */
    @Test
    void setWakeupTid() {
        assertEquals(0, new WakeupBean() {{
            setWakeupTid(0);
        }}.getWakeupTid());
    }

    /**
     * test get the SchedulingLatency .
     */
    @Test
    void getSchedulingLatency() {
        assertEquals(3L, new WakeupBean() {{
            setSchedulingLatency(3L);
        }}.getSchedulingLatency());
    }

    /**
     * test set the SchedulingLatency .
     */
    @Test
    void setSchedulingLatency() {
        assertEquals(3L, new WakeupBean() {{
            setSchedulingLatency(3L);
        }}.getSchedulingLatency());
    }

    /**
     * test get the SchedulingDesc .
     */
    @Test
    void getSchedulingDesc() {
        assertEquals("SchedulingDesc", new WakeupBean() {{
            setSchedulingDesc("SchedulingDesc");
        }}.getSchedulingDesc());
    }

    /**
     * test set the SchedulingDesc .
     */
    @Test
    void setSchedulingDesc() {
        assertEquals("SchedulingDesc", new WakeupBean() {{
            setSchedulingDesc("SchedulingDesc");
        }}.getSchedulingDesc());
    }
}