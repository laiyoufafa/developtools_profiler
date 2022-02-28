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
 * @date 2021/4/24 18:05
 */
class WakeupBeanTest {
    /**
     * test get the WakeupTime .
     */
    @Test
    void getWakeupTime() {
        WakeupBean wakeupBean = new WakeupBean();
        wakeupBean.setWakeupTime(3L);
        assertEquals(3L, wakeupBean.getWakeupTime());
    }

    /**
     * test set the WakeupTime .
     */
    @Test
    void setWakeupTime() {
        WakeupBean wakeupBean = new WakeupBean();
        wakeupBean.setWakeupTime(3L);
        assertEquals(3L, wakeupBean.getWakeupTime());
    }

    /**
     * test get the WakeupCpu .
     */
    @Test
    void getWakeupCpu() {
        WakeupBean wakeupBean = new WakeupBean();
        wakeupBean.setWakeupCpu(3);
        assertEquals(3, wakeupBean.getWakeupCpu());
    }

    /**
     * test set the WakeupCpu .
     */
    @Test
    void setWakeupCpu() {
        WakeupBean wakeupBean = new WakeupBean();
        wakeupBean.setWakeupCpu(3);
        assertEquals(3, wakeupBean.getWakeupCpu());
    }

    /**
     * test get the WakeupProcess .
     */
    @Test
    void getWakeupProcess() {
        WakeupBean wakeupBean = new WakeupBean();
        wakeupBean.setWakeupProcess("WakeupProcess");
        assertEquals("WakeupProcess", wakeupBean.getWakeupProcess());
    }

    /**
     * test set the WakeupProcess .
     */
    @Test
    void setWakeupProcess() {
        WakeupBean wakeupBean = new WakeupBean();
        wakeupBean.setWakeupProcess("WakeupProcess");
        assertEquals("WakeupProcess", wakeupBean.getWakeupProcess());
    }

    /**
     * test get the WakeupPid .
     */
    @Test
    void getWakeupPid() {
        WakeupBean wakeupBean = new WakeupBean();
        wakeupBean.setWakeupPid(0);
        assertEquals(0, wakeupBean.getWakeupPid());
    }

    /**
     * test set the WakeupPid .
     */
    @Test
    void setWakeupPid() {
        WakeupBean wakeupBean = new WakeupBean();
        wakeupBean.setWakeupPid(0);
        assertEquals(0, wakeupBean.getWakeupPid());
    }

    /**
     * test get the WakeupThread .
     */
    @Test
    void getWakeupThread() {
        WakeupBean wakeupBean = new WakeupBean();
        wakeupBean.setWakeupThread("WakeupThread");
        assertEquals("WakeupThread", wakeupBean.getWakeupThread());
    }

    /**
     * test set the WakeupThread .
     */
    @Test
    void setWakeupThread() {
        WakeupBean wakeupBean = new WakeupBean();
        wakeupBean.setWakeupThread("WakeupThread");
        assertEquals("WakeupThread", wakeupBean.getWakeupThread());
    }

    /**
     * test get the WakeupTid .
     */
    @Test
    void getWakeupTid() {
        WakeupBean wakeupBean = new WakeupBean();
        wakeupBean.setWakeupTid(0);
        assertEquals(0, wakeupBean.getWakeupTid());
    }

    /**
     * test set the WakeupTid .
     */
    @Test
    void setWakeupTid() {
        WakeupBean wakeupBean = new WakeupBean();
        wakeupBean.setWakeupTid(0);
        assertEquals(0, wakeupBean.getWakeupTid());
    }

    /**
     * test get the SchedulingLatency .
     */
    @Test
    void getSchedulingLatency() {
        WakeupBean wakeupBean = new WakeupBean();
        wakeupBean.setSchedulingLatency(3L);
        assertEquals(3L, wakeupBean.getSchedulingLatency());
    }

    /**
     * test set the SchedulingLatency .
     */
    @Test
    void setSchedulingLatency() {
        WakeupBean wakeupBean = new WakeupBean();
        wakeupBean.setSchedulingLatency(3L);
        assertEquals(3L, wakeupBean.getSchedulingLatency());
    }

    /**
     * test get the SchedulingDesc .
     */
    @Test
    void getSchedulingDesc() {
        WakeupBean wakeupBean = new WakeupBean();
        wakeupBean.setSchedulingDesc("SchedulingDesc");
        assertEquals("SchedulingDesc", wakeupBean.getSchedulingDesc());
    }

    /**
     * test set the SchedulingDesc .
     */
    @Test
    void setSchedulingDesc() {
        WakeupBean wakeupBean = new WakeupBean();
        wakeupBean.setSchedulingDesc("SchedulingDesc");
        assertEquals("SchedulingDesc", wakeupBean.getSchedulingDesc());
    }
}