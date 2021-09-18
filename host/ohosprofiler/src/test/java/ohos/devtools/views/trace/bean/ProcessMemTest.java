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
        assertEquals(3, new ProcessMem() {{
            setTrackId(3);
        }}.getTrackId());
    }

    /**
     * test set the TrackId .
     */
    @Test
    void setTrackId() {
        assertEquals(3, new ProcessMem() {{
            setTrackId(3);
        }}.getTrackId());
    }

    /**
     * test get the ProcessName .
     */
    @Test
    void getProcessName() {
        assertEquals("ProcessName", new ProcessMem() {{
            setProcessName("ProcessName");
        }}.getProcessName());
    }

    /**
     * test set the ProcessName .
     */
    @Test
    void setProcessName() {
        assertEquals("ProcessName", new ProcessMem() {{
            setProcessName("ProcessName");
        }}.getProcessName());
    }

    /**
     * test get the Pid .
     */
    @Test
    void getPid() {
        assertEquals(1, new ProcessMem() {{
            setPid(1);
        }}.getPid());
    }

    /**
     * test set the Pid .
     */
    @Test
    void setPid() {
        assertEquals(1, new ProcessMem() {{
            setPid(1);
        }}.getPid());
    }

    /**
     * test get the Upid .
     */
    @Test
    void getUpid() {
        assertEquals(1, new ProcessMem() {{
            setUpid(1);
        }}.getUpid());
    }

    /**
     * test set the Upid .
     */
    @Test
    void setUpid() {
        assertEquals(1, new ProcessMem() {{
            setUpid(1);
        }}.getUpid());
    }

    /**
     * test get the TrackName .
     */
    @Test
    void getTrackName() {
        assertEquals("TrackName", new ProcessMem() {{
            setTrackName("TrackName");
        }}.getTrackName());
    }

    /**
     * test get the TrackName .
     */
    @Test
    void setTrackName() {
        assertEquals("TrackName", new ProcessMem() {{
            setTrackName("TrackName");
        }}.getTrackName());
    }
}