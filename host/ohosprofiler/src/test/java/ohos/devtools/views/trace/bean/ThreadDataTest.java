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
 * test ThreadData class
 *
 * @version 1.0
 * @date 2021/4/24 18:05
 **/
class ThreadDataTest {
    /**
     * test get the uPid .
     */
    @Test
    void getuPid() {
        assertEquals(3, new ThreadData() {{
            setuPid(3);
        }}.getuPid());
    }

    /**
     * test set the uPid .
     */
    @Test
    void setuPid() {
        assertEquals(3, new ThreadData() {{
            setuPid(3);
        }}.getuPid());
    }

    /**
     * test set the uTid .
     */
    @Test
    void getuTid() {
        assertEquals(3, new ThreadData() {{
            setuTid(3);
        }}.getuTid());
    }

    /**
     * test set the uTid .
     */
    @Test
    void setuTid() {
        assertEquals(3, new ThreadData() {{
            setuTid(3);
        }}.getuTid());
    }

    /**
     * test get the Pid .
     */
    @Test
    void getPid() {
        assertEquals(3, new ThreadData() {{
            setPid(3);
        }}.getPid());
    }

    /**
     * test set the Pid .
     */
    @Test
    void setPid() {
        assertEquals(3, new ThreadData() {{
            setPid(3);
        }}.getPid());
    }

    /**
     * test get the Tid .
     */
    @Test
    void getTid() {
        assertEquals(3, new ThreadData() {{
            setTid(3);
        }}.getTid());
    }

    /**
     * test set the Tid .
     */
    @Test
    void setTid() {
        assertEquals(3, new ThreadData() {{
            setTid(3);
        }}.getTid());
    }

    /**
     * test get the ProcessName .
     */
    @Test
    void getProcessName() {
        assertEquals("ProcessName", new ThreadData() {{
            setProcessName("ProcessName");
        }}.getProcessName());
    }

    /**
     * test set the ProcessName .
     */
    @Test
    void setProcessName() {
        assertEquals("ProcessName", new ThreadData() {{
            setProcessName("ProcessName");
        }}.getProcessName());
    }

    /**
     * test get the ThreadName .
     */
    @Test
    void getThreadName() {
        assertEquals("ThreadName", new ThreadData() {{
            setThreadName("ThreadName");
        }}.getThreadName());
    }

    /**
     * test set the ThreadName .
     */
    @Test
    void setThreadName() {
        assertEquals("ThreadName", new ThreadData() {{
            setThreadName("ThreadName");
        }}.getThreadName());
    }

    /**
     * test get the State .
     */
    @Test
    void getState() {
        assertEquals("State", new ThreadData() {{
            setState("State");
        }}.getState());
    }

    /**
     * test set the State .
     */
    @Test
    void setState() {
        assertEquals("State", new ThreadData() {{
            setState("State");
        }}.getState());
    }

    /**
     * test get the StartTime .
     */
    @Test
    void getStartTime() {
        assertEquals(3L, new ThreadData() {{
            setStartTime(3L);
        }}.getStartTime());
    }

    /**
     * test set the StartTime .
     */
    @Test
    void setStartTime() {
        assertEquals(3L, new ThreadData() {{
            setStartTime(3L);
        }}.getStartTime());
    }

    /**
     * test get the Duration .
     */
    @Test
    void getDuration() {
        assertEquals(3L, new ThreadData() {{
            setDuration(3L);
        }}.getDuration());
    }

    /**
     * test set the Duration .
     */
    @Test
    void setDuration() {
        assertEquals(3L, new ThreadData() {{
            setDuration(3L);
        }}.getDuration());
    }

    /**
     * test get the number of Cpu .
     */
    @Test
    void getCpu() {
        assertEquals(3, new ThreadData() {{
            setCpu(3);
        }}.getCpu());
    }

    /**
     * test set the number of Cpu .
     */
    @Test
    void setCpu() {
        assertEquals(3, new ThreadData() {{
            setCpu(3);
        }}.getCpu());
    }
}