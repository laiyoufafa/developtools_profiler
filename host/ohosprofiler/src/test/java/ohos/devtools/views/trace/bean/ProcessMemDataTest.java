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
        assertEquals(3, new ProcessMemData() {{
            setMaxValue(3);
        }}.getMaxValue());
    }

    /**
     * test set the MaxValue .
     */
    @Test
    void setMaxValue() {
        assertEquals(3, new ProcessMemData() {{
            setMaxValue(3);
        }}.getMaxValue());
    }

    /**
     * test get the id .
     */
    @Test
    void getId() {
        assertEquals(3, new ProcessMemData() {{
            setId(3);
        }}.getId());
    }

    /**
     * test set the id .
     */
    @Test
    void setId() {
        assertEquals(3, new ProcessMemData() {{
            setId(3);
        }}.getId());
    }

    /**
     * test get the type .
     */
    @Test
    void getType() {
        assertEquals("type", new ProcessMemData() {{
            setType("type");
        }}.getType());
    }

    /**
     * test set the type .
     */
    @Test
    void setType() {
        assertEquals("type", new ProcessMemData() {{
            setType("type");
        }}.getType());
    }

    /**
     * test get the TrackId .
     */
    @Test
    void getTrackId() {
        assertEquals(3, new ProcessMemData() {{
            setTrackId(3);
        }}.getTrackId());
    }

    /**
     * test set the TrackId .
     */
    @Test
    void setTrackId() {
        assertEquals(3, new ProcessMemData() {{
            setTrackId(3);
        }}.getTrackId());
    }

    /**
     * test get the Value .
     */
    @Test
    void getValue() {
        assertEquals(3, new ProcessMemData() {{
            setValue(3);
        }}.getValue());
    }

    /**
     * test set the Value .
     */
    @Test
    void setValue() {
        assertEquals(3, new ProcessMemData() {{
            setValue(3);
        }}.getValue());
    }

    /**
     * test get the StartTime .
     */
    @Test
    void getStartTime() {
        assertEquals(3L, new ProcessMemData() {{
            setStartTime(3L);
        }}.getStartTime());
    }

    /**
     * test set the StartTime .
     */
    @Test
    void setStartTime() {
        assertEquals(3L, new ProcessMemData() {{
            setStartTime(3L);
        }}.getStartTime());
    }

    /**
     * test get the Duration .
     */
    @Test
    void getDuration() {
        assertEquals(3L, new ProcessMemData() {{
            setDuration(3L);
        }}.getDuration());
    }

    /**
     * test set the Duration .
     */
    @Test
    void setDuration() {
        assertEquals(3L, new ProcessMemData() {{
            setDuration(3L);
        }}.getDuration());
    }
}