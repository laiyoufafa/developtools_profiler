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
        FunctionBean functionBean = new FunctionBean();
        functionBean.setTid(3);
        assertEquals(3, functionBean.getTid());
    }

    /**
     * test set the tid .
     */
    @Test
    void setTid() {
        FunctionBean functionBean = new FunctionBean();
        functionBean.setTid(3);
        assertEquals(3, functionBean.getTid());
    }

    /**
     * test get the ThreadName .
     */
    @Test
    void getThreadName() {
        FunctionBean functionBean = new FunctionBean();
        functionBean.setThreadName("ThreadName");
        assertEquals("ThreadName", functionBean.getThreadName());
    }

    /**
     * test set the ThreadName .
     */
    @Test
    void setThreadName() {
        FunctionBean functionBean = new FunctionBean();
        functionBean.setThreadName("ThreadName");
        assertEquals("ThreadName", functionBean.getThreadName());
    }

    /**
     * test get the IsMainThread .
     */
    @Test
    void getIsMainThread() {
        FunctionBean functionBean = new FunctionBean();
        functionBean.setIsMainThread(1);
        assertEquals(1, functionBean.getIsMainThread());
    }

    /**
     * test set the IsMainThread .
     */
    @Test
    void setIsMainThread() {
        FunctionBean functionBean = new FunctionBean();
        functionBean.setIsMainThread(1);
        assertEquals(1, functionBean.getIsMainThread());
    }

    /**
     * test get the TrackId .
     */
    @Test
    void getTrackId() {
        FunctionBean functionBean = new FunctionBean();
        functionBean.setTrackId(1);
        assertEquals(1, functionBean.getTrackId());
    }

    /**
     * test set the TrackId .
     */
    @Test
    void setTrackId() {
        FunctionBean functionBean = new FunctionBean();
        functionBean.setTrackId(1);
        assertEquals(1, functionBean.getTrackId());
    }

    /**
     * test get the StartTime .
     */
    @Test
    void getStartTime() {
        FunctionBean functionBean = new FunctionBean();
        functionBean.setStartTime(1L);
        assertEquals(1L, functionBean.getStartTime());
    }

    /**
     * test set the StartTime .
     */
    @Test
    void setStartTime() {
        FunctionBean functionBean = new FunctionBean();
        functionBean.setStartTime(1L);
        assertEquals(1L, functionBean.getStartTime());
    }

    /**
     * test get the Duration .
     */
    @Test
    void getDuration() {
        FunctionBean functionBean = new FunctionBean();
        functionBean.setDuration(1L);
        assertEquals(1L, functionBean.getDuration());
    }

    /**
     * test set the Duration .
     */
    @Test
    void setDuration() {
        FunctionBean functionBean = new FunctionBean();
        functionBean.setDuration(1L);
        assertEquals(1L, functionBean.getDuration());
    }

    /**
     * test get the FunName .
     */
    @Test
    void getFunName() {
        FunctionBean functionBean = new FunctionBean();
        functionBean.setFunName("FunName");
        assertEquals("FunName", functionBean.getFunName());
    }

    /**
     * test set the FunName .
     */
    @Test
    void setFunName() {
        FunctionBean functionBean = new FunctionBean();
        functionBean.setFunName("FunName");
        assertEquals("FunName", functionBean.getFunName());
    }

    /**
     * test get the Depth .
     */
    @Test
    void getDepth() {
        FunctionBean functionBean = new FunctionBean();
        functionBean.setDepth(3);
        assertEquals(3, functionBean.getDepth());
    }

    /**
     * test set the Depth .
     */
    @Test
    void setDepth() {
        FunctionBean functionBean = new FunctionBean();
        functionBean.setDepth(3);
        assertEquals(3, functionBean.getDepth());
    }

    /**
     * test get the Category .
     */
    @Test
    void getCategory() {
        FunctionBean functionBean = new FunctionBean();
        functionBean.setCategory("Category");
        assertEquals("Category", functionBean.getCategory());
    }

    /**
     * test set the Category .
     */
    @Test
    void setCategory() {
        FunctionBean functionBean = new FunctionBean();
        functionBean.setCategory("Category");
        assertEquals("Category", functionBean.getCategory());
    }

    /**
     * test get the Selected .
     */
    @Test
    void isSelected() {
        boolean selected = true;
        FunctionBean functionBean = new FunctionBean();
        functionBean.setSelected(selected);
        assertEquals(selected, functionBean.isSelected());
    }

    /**
     * test set the Selected .
     */
    @Test
    void setSelected() {
        boolean selected = true;
        FunctionBean functionBean = new FunctionBean();
        functionBean.setSelected(selected);
        assertEquals(selected, functionBean.isSelected());
    }
}