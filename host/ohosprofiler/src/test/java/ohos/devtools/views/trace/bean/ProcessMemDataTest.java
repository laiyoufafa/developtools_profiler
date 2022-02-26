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
 * test ProcessMemData class
 *
 * @date 2021/4/24 18:05
 */
class ProcessMemDataTest {
    /**
     * test get the MaxValue .
     */
    @Test
    void getMaxValue() {
        ProcessMemData processMemData = new ProcessMemData();
        processMemData.setMaxValue(3);
        assertEquals(3, processMemData.getMaxValue());
    }

    /**
     * test set the MaxValue .
     */
    @Test
    void setMaxValue() {
        ProcessMemData processMemData = new ProcessMemData();
        processMemData.setMaxValue(3);
        assertEquals(3, processMemData.getMaxValue());
    }

    /**
     * test get the id .
     */
    @Test
    void getId() {
        ProcessMemData processMemData = new ProcessMemData();
        processMemData.setId(3);
        assertEquals(3, processMemData.getId());
    }

    /**
     * test set the id .
     */
    @Test
    void setId() {
        ProcessMemData processMemData = new ProcessMemData();
        processMemData.setId(3);
        assertEquals(3, processMemData.getId());
    }

    /**
     * test get the type .
     */
    @Test
    void getType() {
        ProcessMemData processMemData = new ProcessMemData();
        processMemData.setType("type");
        assertEquals("type", processMemData.getType());
    }

    /**
     * test set the type .
     */
    @Test
    void setType() {
        ProcessMemData processMemData = new ProcessMemData();
        processMemData.setType("type");
        assertEquals("type", processMemData.getType());
    }

    /**
     * test get the TrackId .
     */
    @Test
    void getTrackId() {
        ProcessMemData processMemData = new ProcessMemData();
        processMemData.setTrackId(3);
        assertEquals(3, processMemData.getTrackId());
    }

    /**
     * test set the TrackId .
     */
    @Test
    void setTrackId() {
        ProcessMemData processMemData = new ProcessMemData();
        processMemData.setTrackId(3);
        assertEquals(3, processMemData.getTrackId());
    }

    /**
     * test get the Value .
     */
    @Test
    void getValue() {
        ProcessMemData processMemData = new ProcessMemData();
        processMemData.setValue(3);
        assertEquals(3, processMemData.getValue());
    }

    /**
     * test set the Value .
     */
    @Test
    void setValue() {
        ProcessMemData processMemData = new ProcessMemData();
        processMemData.setValue(3);
        assertEquals(3, processMemData.getValue());
    }

    /**
     * test get the StartTime .
     */
    @Test
    void getStartTime() {
        ProcessMemData processMemData = new ProcessMemData();
        processMemData.setStartTime(3L);
        assertEquals(3L, processMemData.getStartTime());
    }

    /**
     * test set the StartTime .
     */
    @Test
    void setStartTime() {
        ProcessMemData processMemData = new ProcessMemData();
        processMemData.setStartTime(3L);
        assertEquals(3L, processMemData.getStartTime());
    }

    /**
     * test get the Duration .
     */
    @Test
    void getDuration() {
        ProcessMemData processMemData = new ProcessMemData();
        processMemData.setDuration(3L);
        assertEquals(3L, processMemData.getDuration());
    }

    /**
     * test set the Duration .
     */
    @Test
    void setDuration() {
        ProcessMemData processMemData = new ProcessMemData();
        processMemData.setDuration(3L);
        assertEquals(3L, processMemData.getDuration());
    }
}