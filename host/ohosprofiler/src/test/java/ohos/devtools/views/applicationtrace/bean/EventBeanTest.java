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

package ohos.devtools.views.applicationtrace.bean;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EventBeanTest {

    @Test
    void getStartTime() throws NoSuchFieldException, IllegalAccessException {
        EventBean eventBean = new EventBean();
        final Field field = eventBean.getClass().getDeclaredField("startTime");
        field.setAccessible(true);
        field.set(eventBean, 1L);
        assertEquals(1L, eventBean.getStartTime());
        final Field zeroField = eventBean.getClass().getDeclaredField("startTime");
        zeroField.setAccessible(true);
        zeroField.set(eventBean, 0L);
        assertEquals(0L, eventBean.getStartTime());
        final Field negativeField = eventBean.getClass().getDeclaredField("startTime");
        negativeField.setAccessible(true);
        negativeField.set(eventBean, -1L);
        assertEquals(-1L, eventBean.getStartTime());
    }

    @Test
    void setStartTime() throws NoSuchFieldException, IllegalAccessException {
        EventBean eventBean = new EventBean();
        final Field field = eventBean.getClass().getDeclaredField("startTime");
        field.setAccessible(true);
        eventBean.setStartTime(1L);
        assertEquals(1L, field.get(eventBean));
        final Field nullField = eventBean.getClass().getDeclaredField("startTime");
        nullField.setAccessible(true);
        eventBean.setStartTime(0L);
        assertEquals(0L, nullField.get(eventBean));
        final Field negativeField = eventBean.getClass().getDeclaredField("startTime");
        negativeField.setAccessible(true);
        eventBean.setStartTime(-1L);
        assertEquals(-1L, negativeField.get(eventBean));
    }

    @Test
    void getName() throws NoSuchFieldException, IllegalAccessException {
        EventBean eventBean = new EventBean();
        final Field field = eventBean.getClass().getDeclaredField("name");
        field.setAccessible(true);
        field.set(eventBean, "name");
        assertEquals("name", eventBean.getName());
        final Field zeroField = eventBean.getClass().getDeclaredField("name");
        zeroField.setAccessible(true);
        zeroField.set(eventBean, null);
        assertNull(eventBean.getName());
        final Field negativeField = eventBean.getClass().getDeclaredField("name");
        negativeField.setAccessible(true);
        negativeField.set(eventBean, "");
        assertEquals("", eventBean.getName());
    }

    @Test
    void setName() throws NoSuchFieldException, IllegalAccessException {
        EventBean eventBean = new EventBean();
        final Field field = eventBean.getClass().getDeclaredField("name");
        field.setAccessible(true);
        eventBean.setName("");
        assertEquals("", field.get(eventBean));
        final Field nullField = eventBean.getClass().getDeclaredField("name");
        nullField.setAccessible(true);
        eventBean.setName(null);
        assertNull(nullField.get(eventBean));
        final Field negativeField = eventBean.getClass().getDeclaredField("name");
        negativeField.setAccessible(true);
        eventBean.setName("name");
        assertEquals("name", negativeField.get(eventBean));
    }

    @Test
    void getWallDuration() throws NoSuchFieldException, IllegalAccessException {
        EventBean eventBean = new EventBean();
        final Field field = eventBean.getClass().getDeclaredField("wallDuration");
        field.setAccessible(true);
        field.set(eventBean, 1L);
        assertEquals(1L, eventBean.getWallDuration());
        final Field zeroField = eventBean.getClass().getDeclaredField("wallDuration");
        zeroField.setAccessible(true);
        zeroField.set(eventBean, 0L);
        assertEquals(0L, eventBean.getWallDuration());
        final Field negativeField = eventBean.getClass().getDeclaredField("wallDuration");
        negativeField.setAccessible(true);
        negativeField.set(eventBean, -1L);
        assertEquals(-1L, eventBean.getWallDuration());
    }

    @Test
    void setWallDuration() throws NoSuchFieldException, IllegalAccessException {
        EventBean eventBean = new EventBean();
        final Field field = eventBean.getClass().getDeclaredField("wallDuration");
        field.setAccessible(true);
        eventBean.setWallDuration(1L);
        assertEquals(1L, field.get(eventBean));
        final Field nullField = eventBean.getClass().getDeclaredField("wallDuration");
        nullField.setAccessible(true);
        eventBean.setWallDuration(0L);
        assertEquals(nullField.get(eventBean), 0L);
        final Field negativeField = eventBean.getClass().getDeclaredField("wallDuration");
        negativeField.setAccessible(true);
        eventBean.setWallDuration(-1L);
        assertEquals(-1L, negativeField.get(eventBean));
    }

    @Test
    void getSelfTime() throws NoSuchFieldException, IllegalAccessException {
        EventBean eventBean = new EventBean();
        final Field field = eventBean.getClass().getDeclaredField("selfTime");
        field.setAccessible(true);
        field.set(eventBean, 1L);
        assertEquals(1L, eventBean.getSelfTime());
        final Field zeroField = eventBean.getClass().getDeclaredField("selfTime");
        zeroField.setAccessible(true);
        zeroField.set(eventBean, 0L);
        assertEquals(0L, eventBean.getSelfTime());
        final Field negativeField = eventBean.getClass().getDeclaredField("selfTime");
        negativeField.setAccessible(true);
        negativeField.set(eventBean, -1L);
        assertEquals(-1L, eventBean.getSelfTime());
    }

    @Test
    void setSelfTime() throws NoSuchFieldException, IllegalAccessException {
        EventBean eventBean = new EventBean();
        final Field field = eventBean.getClass().getDeclaredField("selfTime");
        field.setAccessible(true);
        eventBean.setSelfTime(1L);
        assertEquals(1L, field.get(eventBean));
        final Field nullField = eventBean.getClass().getDeclaredField("selfTime");
        nullField.setAccessible(true);
        eventBean.setSelfTime(0L);
        assertEquals(nullField.get(eventBean), 0L);
        final Field negativeField = eventBean.getClass().getDeclaredField("selfTime");
        negativeField.setAccessible(true);
        eventBean.setSelfTime(-1L);
        assertEquals(-1L, negativeField.get(eventBean));
    }

    @Test
    void getCpuDuration() throws NoSuchFieldException, IllegalAccessException {
        EventBean eventBean = new EventBean();
        final Field field = eventBean.getClass().getDeclaredField("cpuDuration");
        field.setAccessible(true);
        field.set(eventBean, 1L);
        assertEquals(1L, eventBean.getCpuDuration());
        final Field zeroField = eventBean.getClass().getDeclaredField("cpuDuration");
        zeroField.setAccessible(true);
        zeroField.set(eventBean, 0L);
        assertEquals(0L, eventBean.getCpuDuration());
        final Field negativeField = eventBean.getClass().getDeclaredField("cpuDuration");
        negativeField.setAccessible(true);
        negativeField.set(eventBean, -1L);
        assertEquals(-1L, eventBean.getCpuDuration());
    }

    @Test
    void setCpuDuration() throws NoSuchFieldException, IllegalAccessException {
        EventBean eventBean = new EventBean();
        final Field field = eventBean.getClass().getDeclaredField("cpuDuration");
        field.setAccessible(true);
        eventBean.setCpuDuration(1L);
        assertEquals(1L, field.get(eventBean));
        final Field nullField = eventBean.getClass().getDeclaredField("cpuDuration");
        nullField.setAccessible(true);
        eventBean.setCpuDuration(0L);
        assertEquals(nullField.get(eventBean), 0L);
        final Field negativeField = eventBean.getClass().getDeclaredField("cpuDuration");
        negativeField.setAccessible(true);
        eventBean.setCpuDuration(-1L);
        assertEquals(-1L, negativeField.get(eventBean));
    }

    @Test
    void getCpuSelfTime() throws NoSuchFieldException, IllegalAccessException {
        EventBean eventBean = new EventBean();
        final Field field = eventBean.getClass().getDeclaredField("cpuSelfTime");
        field.setAccessible(true);
        field.set(eventBean, 1L);
        assertEquals(1L, eventBean.getCpuSelfTime());
        final Field zeroField = eventBean.getClass().getDeclaredField("cpuSelfTime");
        zeroField.setAccessible(true);
        zeroField.set(eventBean, 0L);
        assertEquals(0L, eventBean.getCpuSelfTime());
        final Field negativeField = eventBean.getClass().getDeclaredField("cpuSelfTime");
        negativeField.setAccessible(true);
        negativeField.set(eventBean, -1L);
        assertEquals(-1L, eventBean.getCpuSelfTime());
    }

    @Test
    void setCpuSelfTime() throws NoSuchFieldException, IllegalAccessException {
        EventBean eventBean = new EventBean();
        final Field field = eventBean.getClass().getDeclaredField("cpuSelfTime");
        field.setAccessible(true);
        eventBean.setCpuSelfTime(1L);
        assertEquals(1L, field.get(eventBean));
        final Field nullField = eventBean.getClass().getDeclaredField("cpuSelfTime");
        nullField.setAccessible(true);
        eventBean.setCpuSelfTime(0L);
        assertEquals(nullField.get(eventBean), 0L);
        final Field negativeField = eventBean.getClass().getDeclaredField("cpuSelfTime");
        negativeField.setAccessible(true);
        eventBean.setCpuSelfTime(-1L);
        assertEquals(-1L, negativeField.get(eventBean));
    }
}