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
 * @date 2021/4/24 18:05
 */
class ProcessDataTest {
    /**
     * test get the id .
     */
    @Test
    void getId() {
        assertEquals(3, new ProcessData() {{
            setId(3);
        }}.getId());
    }

    /**
     * test set the id .
     */
    @Test
    void setId() {
        assertEquals(3, new ProcessData() {{
            setId(3);
        }}.getId());
    }

    /**
     * test get the utid .
     */
    @Test
    void getUtid() {
        assertEquals(3, new ProcessData() {{
            setUtid(3);
        }}.getUtid());
    }

    /**
     * test set the utid .
     */
    @Test
    void setUtid() {
        assertEquals(3, new ProcessData() {{
            setUtid(3);
        }}.getUtid());
    }

    /**
     * test et the cpu .
     */
    @Test
    void getCpu() {
        assertEquals(3, new ProcessData() {{
            setCpu(3);
        }}.getCpu());
    }

    /**
     * test set the cpu .
     */
    @Test
    void setCpu() {
        assertEquals(3, new ProcessData() {{
            setCpu(3);
        }}.getCpu());
    }

    /**
     * test get the StartTime .
     */
    @Test
    void getStartTime() {
        assertEquals(3L, new ProcessData() {{
            setStartTime(3L);
        }}.getStartTime());
    }

    /**
     * test set the StartTime .
     */
    @Test
    void setStartTime() {
        assertEquals(3L, new ProcessData() {{
            setStartTime(3L);
        }}.getStartTime());
    }

    /**
     * test get the Duration .
     */
    @Test
    void getDuration() {
        assertEquals(3L, new ProcessData() {{
            setDuration(3L);
        }}.getDuration());
    }

    /**
     * test set the Duration .
     */
    @Test
    void setDuration() {
        assertEquals(3L, new ProcessData() {{
            setDuration(3L);
        }}.getDuration());
    }

    /**
     * test get the State .
     */
    @Test
    void getState() {
        assertEquals("state", new ProcessData() {{
            setState("state");
        }}.getState());
    }

    /**
     * test set the State .
     */
    @Test
    void setState() {
        assertEquals("state", new ProcessData() {{
            setState("state");
        }}.getState());
    }
}