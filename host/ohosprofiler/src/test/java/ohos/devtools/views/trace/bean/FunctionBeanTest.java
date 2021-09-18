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
 * test FunctionBean class
 *
 * @date 2021/4/24 18:05
 */
class FunctionBeanTest {
    /**
     * test get the tid .
     */
    @Test
    void getTid() {
        assertEquals(3, new FunctionBean() {{
            setTid(3);
        }}.getTid());
    }

    /**
     * test set the tid .
     */
    @Test
    void setTid() {
        assertEquals(3, new FunctionBean() {{
            setTid(3);
        }}.getTid());
    }

    /**
     * test get the ThreadName .
     */
    @Test
    void getThreadName() {
        assertEquals("ThreadName", new FunctionBean() {{
            setThreadName("ThreadName");
        }}.getThreadName());
    }

    /**
     * test set the ThreadName .
     */
    @Test
    void setThreadName() {
        assertEquals("ThreadName", new FunctionBean() {{
            setThreadName("ThreadName");
        }}.getThreadName());
    }

    /**
     * test get the IsMainThread .
     */
    @Test
    void getIsMainThread() {
        assertEquals(1, new FunctionBean() {{
            setIsMainThread(1);
        }}.getIsMainThread());
    }

    /**
     * test set the IsMainThread .
     */
    @Test
    void setIsMainThread() {
        assertEquals(1, new FunctionBean() {{
            setIsMainThread(1);
        }}.getIsMainThread());
    }

    /**
     * test get the TrackId .
     */
    @Test
    void getTrackId() {
        assertEquals(1, new FunctionBean() {{
            setTrackId(1);
        }}.getTrackId());
    }

    /**
     * test set the TrackId .
     */
    @Test
    void setTrackId() {
        assertEquals(1, new FunctionBean() {{
            setTrackId(1);
        }}.getTrackId());
    }

    /**
     * test get the StartTime .
     */
    @Test
    void getStartTime() {
        assertEquals(1L, new FunctionBean() {{
            setStartTime(1L);
        }}.getStartTime());
    }

    /**
     * test set the StartTime .
     */
    @Test
    void setStartTime() {
        assertEquals(1L, new FunctionBean() {{
            setStartTime(1L);
        }}.getStartTime());
    }

    /**
     * test get the Duration .
     */
    @Test
    void getDuration() {
        assertEquals(1L, new FunctionBean() {{
            setDuration(1L);
        }}.getDuration());
    }

    /**
     * test set the Duration .
     */
    @Test
    void setDuration() {
        assertEquals(1L, new FunctionBean() {{
            setDuration(1L);
        }}.getDuration());
    }

    /**
     * test get the FunName .
     */
    @Test
    void getFunName() {
        assertEquals("FunName", new FunctionBean() {{
            setFunName("FunName");
        }}.getFunName());
    }

    /**
     * test set the FunName .
     */
    @Test
    void setFunName() {
        assertEquals("FunName", new FunctionBean() {{
            setFunName("FunName");
        }}.getFunName());
    }

    /**
     * test get the Depth .
     */
    @Test
    void getDepth() {
        assertEquals(3, new FunctionBean() {{
            setDepth(3);
        }}.getDepth());
    }

    /**
     * test set the Depth .
     */
    @Test
    void setDepth() {
        assertEquals(3, new FunctionBean() {{
            setDepth(3);
        }}.getDepth());
    }

    /**
     * test get the Category .
     */
    @Test
    void getCategory() {
        assertEquals("Category", new FunctionBean() {{
            setCategory("Category");
        }}.getCategory());
    }

    /**
     * test set the Category .
     */
    @Test
    void setCategory() {
        assertEquals("Category", new FunctionBean() {{
            setCategory("Category");
        }}.getCategory());
    }

    /**
     * test get the Selected .
     */
    @Test
    void isSelected() {
        boolean selected = true;
        assertEquals(selected, new FunctionBean() {{
            setSelected(selected);
        }}.isSelected());
    }

    /**
     * test set the Selected .
     */
    @Test
    void setSelected() {
        boolean selected = true;
        assertEquals(selected, new FunctionBean() {{
            setSelected(selected);
        }}.isSelected());
    }

}