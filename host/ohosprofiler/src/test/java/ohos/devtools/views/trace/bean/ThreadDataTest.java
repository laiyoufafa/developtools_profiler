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
 * @date 2021/4/24 18:05
 */
class ThreadDataTest {
    /**
     * test get the uPid .
     */
    @Test
    void getuPid() {
        ThreadData threadData = new ThreadData();
        threadData.setuPid(3);
        assertEquals(3, threadData.getuPid());
    }

    /**
     * test set the uPid .
     */
    @Test
    void setuPid() {
        ThreadData threadData = new ThreadData();
        threadData.setuPid(3);
        assertEquals(3, threadData.getuPid());
    }

    /**
     * test set the uTid .
     */
    @Test
    void getuTid() {
        ThreadData threadData = new ThreadData();
        threadData.setuTid(3);
        assertEquals(3, threadData.getuTid());
    }

    /**
     * test set the uTid .
     */
    @Test
    void setuTid() {
        ThreadData threadData = new ThreadData();
        threadData.setuTid(3);
        assertEquals(3, threadData.getuTid());
    }

    /**
     * test get the Pid .
     */
    @Test
    void getPid() {
        ThreadData threadData = new ThreadData();
        threadData.setPid(3);
        assertEquals(3, threadData.getPid());
    }

    /**
     * test set the Pid .
     */
    @Test
    void setPid() {
        ThreadData threadData = new ThreadData();
        threadData.setPid(3);
        assertEquals(3, threadData.getPid());
    }

    /**
     * test get the Tid .
     */
    @Test
    void getTid() {
        ThreadData threadData = new ThreadData();
        threadData.setTid(3);
        assertEquals(3, threadData.getTid());
    }

    /**
     * test set the Tid .
     */
    @Test
    void setTid() {
        ThreadData threadData = new ThreadData();
        threadData.setTid(3);
        assertEquals(3, threadData.getTid());
    }

    /**
     * test get the ProcessName .
     */
    @Test
    void getProcessName() {
        ThreadData threadData = new ThreadData();
        threadData.setProcessName("ProcessName");
        assertEquals("ProcessName", threadData.getProcessName());
    }

    /**
     * test set the ProcessName .
     */
    @Test
    void setProcessName() {
        ThreadData threadData = new ThreadData();
        threadData.setProcessName("ProcessName");
        assertEquals("ProcessName", threadData.getProcessName());
    }

    /**
     * test get the ThreadName .
     */
    @Test
    void getThreadName() {
        ThreadData threadData = new ThreadData();
        threadData.setThreadName("ThreadName");
        assertEquals("ThreadName", threadData.getThreadName());
    }

    /**
     * test set the ThreadName .
     */
    @Test
    void setThreadName() {
        ThreadData threadData = new ThreadData();
        threadData.setThreadName("ThreadName");
        assertEquals("ThreadName", threadData.getThreadName());
    }

    /**
     * test get the State .
     */
    @Test
    void getState() {
        ThreadData threadData = new ThreadData();
        threadData.setState("State");
        assertEquals("State", threadData.getState());
    }

    /**
     * test set the State .
     */
    @Test
    void setState() {
        ThreadData threadData = new ThreadData();
        threadData.setState("State");
        assertEquals("State", threadData.getState());
    }

    /**
     * test get the StartTime .
     */
    @Test
    void getStartTime() {
        ThreadData threadData = new ThreadData();
        threadData.setStartTime(3L);
        assertEquals(3L, threadData.getStartTime());
    }

    /**
     * test set the StartTime .
     */
    @Test
    void setStartTime() {
        ThreadData threadData = new ThreadData();
        threadData.setStartTime(3L);
        assertEquals(3L, threadData.getStartTime());
    }

    /**
     * test get the Duration .
     */
    @Test
    void getDuration() {
        ThreadData threadData = new ThreadData();
        threadData.setDuration(3L);
        assertEquals(3L, threadData.getDuration());
    }

    /**
     * test set the Duration .
     */
    @Test
    void setDuration() {
        ThreadData threadData = new ThreadData();
        threadData.setDuration(3L);
        assertEquals(3L, threadData.getDuration());
    }

    /**
     * test get the number of Cpu .
     */
    @Test
    void getCpu() {
        ThreadData threadData = new ThreadData();
        threadData.setCpu(3);
        assertEquals(3, threadData.getCpu());
    }

    /**
     * test set the number of Cpu .
     */
    @Test
    void setCpu() {
        ThreadData threadData = new ThreadData();
        threadData.setCpu(3);
        assertEquals(3, threadData.getCpu());
    }
}