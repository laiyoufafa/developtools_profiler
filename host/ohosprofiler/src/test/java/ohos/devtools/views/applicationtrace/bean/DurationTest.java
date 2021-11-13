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

class DurationTest {

    @Test
    void getTotal() throws NoSuchFieldException, IllegalAccessException {
        Duration duration = new Duration();
        final Field field = duration.getClass().getDeclaredField("total");
        field.setAccessible(true);
        field.set(duration, 1L);
        assertEquals(1L, duration.getTotal());
        final Field zeroField = duration.getClass().getDeclaredField("total");
        zeroField.setAccessible(true);
        zeroField.set(duration, null);
        assertNull(duration.getTotal());
        final Field negativeField = duration.getClass().getDeclaredField("total");
        negativeField.setAccessible(true);
        negativeField.set(duration, -1L);
        assertEquals(-1L, duration.getTotal());
    }

    @Test
    void setTotal() throws NoSuchFieldException, IllegalAccessException {
        Duration duration = new Duration();
        final Field field = duration.getClass().getDeclaredField("total");
        field.setAccessible(true);
        duration.setTotal(1L);
        assertEquals(1L, field.get(duration));
        final Field nullField = duration.getClass().getDeclaredField("total");
        nullField.setAccessible(true);
        duration.setTotal(null);
        assertNull(nullField.get(duration));
        final Field negativeField = duration.getClass().getDeclaredField("total");
        negativeField.setAccessible(true);
        duration.setTotal(-1L);
        assertEquals(-1L, negativeField.get(duration));
    }

    @Test
    void getStartTs() throws NoSuchFieldException, IllegalAccessException {
        Duration duration = new Duration();
        final Field field = duration.getClass().getDeclaredField("startTs");
        field.setAccessible(true);
        field.set(duration, 1L);
        assertEquals(1L, duration.getStartTs());
        final Field zeroField = duration.getClass().getDeclaredField("startTs");
        zeroField.setAccessible(true);
        zeroField.set(duration, null);
        assertNull(duration.getStartTs());
        final Field negativeField = duration.getClass().getDeclaredField("startTs");
        negativeField.setAccessible(true);
        negativeField.set(duration, -1L);
        assertEquals(-1L, duration.getStartTs());
    }

    @Test
    void setStartTs() throws NoSuchFieldException, IllegalAccessException {
        Duration duration = new Duration();
        final Field field = duration.getClass().getDeclaredField("startTs");
        field.setAccessible(true);
        duration.setStartTs(1L);
        assertEquals(1L, field.get(duration));
        final Field nullField = duration.getClass().getDeclaredField("startTs");
        nullField.setAccessible(true);
        duration.setStartTs(null);
        assertNull(nullField.get(duration));
        final Field negativeField = duration.getClass().getDeclaredField("startTs");
        negativeField.setAccessible(true);
        duration.setStartTs(-1L);
        assertEquals(-1L, negativeField.get(duration));
    }

    @Test
    void getEndTs() throws NoSuchFieldException, IllegalAccessException {
        Duration duration = new Duration();
        final Field field = duration.getClass().getDeclaredField("endTs");
        field.setAccessible(true);
        field.set(duration, 1L);
        assertEquals(1L, duration.getEndTs());
        final Field zeroField = duration.getClass().getDeclaredField("endTs");
        zeroField.setAccessible(true);
        zeroField.set(duration, null);
        assertNull(duration.getEndTs());
        final Field negativeField = duration.getClass().getDeclaredField("endTs");
        negativeField.setAccessible(true);
        negativeField.set(duration, -1L);
        assertEquals(-1L, duration.getEndTs());
    }

    @Test
    void setEndTs() throws NoSuchFieldException, IllegalAccessException {
        Duration duration = new Duration();
        final Field field = duration.getClass().getDeclaredField("endTs");
        field.setAccessible(true);
        duration.setEndTs(1L);
        assertEquals(1L, field.get(duration));
        final Field nullField = duration.getClass().getDeclaredField("endTs");
        nullField.setAccessible(true);
        duration.setEndTs(null);
        assertNull(nullField.get(duration));
        final Field negativeField = duration.getClass().getDeclaredField("endTs");
        negativeField.setAccessible(true);
        duration.setEndTs(-1L);
        assertEquals(-1L, negativeField.get(duration));
    }
}