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
 * test ProcessData class
 *
 * @since 2021/4/24 18:05
 */
class ProcessDataTest {
    /**
     * test get the id .
     */
    @Test
    void getId() {
        ProcessData processData = new ProcessData();
        processData.setId(3);
        assertEquals(3, processData.getId());
    }

    /**
     * test set the id .
     */
    @Test
    void setId() {
        ProcessData processData = new ProcessData();
        processData.setId(3);
        assertEquals(3, processData.getId());
    }

    /**
     * test get the utid .
     */
    @Test
    void getUtid() {
        ProcessData processData = new ProcessData();
        processData.setUtid(3);
        assertEquals(3, processData.getUtid());
    }

    /**
     * test set the utid .
     */
    @Test
    void setUtid() {
        ProcessData processData = new ProcessData();
        processData.setUtid(3);
        assertEquals(3, processData.getUtid());
    }

    /**
     * test et the cpu .
     */
    @Test
    void getCpu() {
        ProcessData processData = new ProcessData();
        processData.setCpu(3);
        assertEquals(3, processData.getCpu());
    }

    /**
     * test set the cpu .
     */
    @Test
    void setCpu() {
        ProcessData processData = new ProcessData();
        processData.setCpu(3);
        assertEquals(3, processData.getCpu());
    }

    /**
     * test get the StartTime .
     */
    @Test
    void getStartTime() {
        ProcessData processData = new ProcessData();
        processData.setStartTime(3L);
        assertEquals(3L, processData.getStartTime());
    }

    /**
     * test set the StartTime .
     */
    @Test
    void setStartTime() {
        ProcessData processData = new ProcessData();
        processData.setStartTime(3L);
        assertEquals(3L, processData.getStartTime());
    }

    /**
     * test get the Duration .
     */
    @Test
    void getDuration() {
        ProcessData processData = new ProcessData();
        processData.setDuration(3L);
        assertEquals(3L, processData.getDuration());
    }

    /**
     * test set the Duration .
     */
    @Test
    void setDuration() {
        ProcessData processData = new ProcessData();
        processData.setDuration(3L);
        assertEquals(3L, processData.getDuration());
    }

    /**
     * test get the State .
     */
    @Test
    void getState() {
        ProcessData processData = new ProcessData();
        processData.setState("state");
        assertEquals("state", processData.getState());
    }

    /**
     * test set the State .
     */
    @Test
    void setState() {
        ProcessData processData = new ProcessData();
        processData.setState("state");
        assertEquals("state", processData.getState());
    }
}