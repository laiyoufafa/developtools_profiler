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
 * test ProcessMem class
 *
 * @date 2021/4/24 18:05
 */
class ProcessMemTest {
    /**
     * test get the TrackId .
     */
    @Test
    void getTrackId() {
        ProcessMem processMem = new ProcessMem();
        processMem.setTrackId(3);
        assertEquals(3, processMem.getTrackId());
    }

    /**
     * test set the TrackId .
     */
    @Test
    void setTrackId() {
        ProcessMem processMem = new ProcessMem();
        processMem.setTrackId(3);
        assertEquals(3, processMem.getTrackId());
    }

    /**
     * test get the ProcessName .
     */
    @Test
    void getProcessName() {
        ProcessMem processMem = new ProcessMem();
        processMem.setProcessName("ProcessName");
        assertEquals("ProcessName", processMem.getProcessName());
    }

    /**
     * test set the ProcessName .
     */
    @Test
    void setProcessName() {
        ProcessMem processMem = new ProcessMem();
        processMem.setProcessName("ProcessName");
        assertEquals("ProcessName", processMem.getProcessName());
    }

    /**
     * test get the Pid .
     */
    @Test
    void getPid() {
        ProcessMem processMem = new ProcessMem();
        processMem.setPid(1);
        assertEquals(1, processMem.getPid());
    }

    /**
     * test set the Pid .
     */
    @Test
    void setPid() {
        ProcessMem processMem = new ProcessMem();
        processMem.setPid(1);
        assertEquals(1, processMem.getPid());
    }

    /**
     * test get the Upid .
     */
    @Test
    void getUpid() {
        ProcessMem processMem = new ProcessMem();
        processMem.setUpid(1);
        assertEquals(1, processMem.getUpid());
    }

    /**
     * test set the Upid .
     */
    @Test
    void setUpid() {
        ProcessMem processMem = new ProcessMem();
        processMem.setUpid(1);
        assertEquals(1, processMem.getUpid());
    }

    /**
     * test get the TrackName .
     */
    @Test
    void getTrackName() {
        ProcessMem processMem = new ProcessMem();
        processMem.setTrackName("TrackName");
        assertEquals("TrackName", processMem.getTrackName());
    }

    /**
     * test get the TrackName .
     */
    @Test
    void setTrackName() {
        ProcessMem processMem = new ProcessMem();
        processMem.setTrackName("TrackName");
        assertEquals("TrackName", processMem.getTrackName());
    }
}