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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpuFreqTest {

    @Test
    void getCpu() throws NoSuchFieldException, IllegalAccessException {
        CpuFreq cpuFreq = new CpuFreq();
        final Field field = cpuFreq.getClass().getDeclaredField("cpu");
        field.setAccessible(true);
        field.set(cpuFreq, 1);
        assertEquals(1, cpuFreq.getCpu());
        final Field zeroField = cpuFreq.getClass().getDeclaredField("cpu");
        zeroField.setAccessible(true);
        zeroField.set(cpuFreq, 0);
        assertEquals(0, cpuFreq.getCpu());
        final Field negativeField = cpuFreq.getClass().getDeclaredField("cpu");
        negativeField.setAccessible(true);
        negativeField.set(cpuFreq, -1);
        assertEquals(-1, cpuFreq.getCpu());
    }

    @Test
    void setCpu() throws NoSuchFieldException, IllegalAccessException {
        CpuFreq cpuFreq = new CpuFreq();
        cpuFreq.setCpu(1);
        final Field field = cpuFreq.getClass().getDeclaredField("cpu");
        field.setAccessible(true);
        assertEquals(field.get(cpuFreq), 1);
        final Field negativeField = cpuFreq.getClass().getDeclaredField("cpu");
        negativeField.setAccessible(true);
        cpuFreq.setCpu(-1);
        assertEquals(negativeField.get(cpuFreq), -1);
        final Field zeroField = cpuFreq.getClass().getDeclaredField("cpu");
        zeroField.setAccessible(true);
        cpuFreq.setCpu(0);
        assertEquals(zeroField.get(cpuFreq), 0);

    }

    @Test
    void getValue() throws NoSuchFieldException, IllegalAccessException {
        CpuFreq cpuFreq = new CpuFreq();
        final Field field = cpuFreq.getClass().getDeclaredField("value");
        field.setAccessible(true);
        field.set(cpuFreq, 1L);
        assertEquals(1L, cpuFreq.getValue());
        final Field zeroField = cpuFreq.getClass().getDeclaredField("value");
        zeroField.setAccessible(true);
        zeroField.set(cpuFreq, 0L);
        assertEquals(0L, cpuFreq.getValue());
        final Field negativeField = cpuFreq.getClass().getDeclaredField("value");
        negativeField.setAccessible(true);
        negativeField.set(cpuFreq, -1L);
        assertEquals(-1L, cpuFreq.getValue());
    }

    @Test
    void setValue() throws NoSuchFieldException, IllegalAccessException {
        CpuFreq cpuFreq = new CpuFreq();
        cpuFreq.setValue(1L);
        final Field field = cpuFreq.getClass().getDeclaredField("value");
        field.setAccessible(true);
        assertEquals(field.get(cpuFreq), 1L);
        final Field negativeField = cpuFreq.getClass().getDeclaredField("value");
        negativeField.setAccessible(true);
        cpuFreq.setValue(-1L);
        assertEquals(negativeField.get(cpuFreq), -1L);
        final Field zeroField = cpuFreq.getClass().getDeclaredField("value");
        zeroField.setAccessible(true);
        cpuFreq.setValue(0L);
        assertEquals(zeroField.get(cpuFreq), 0L);
    }

    @Test
    void getStartTime() throws NoSuchFieldException, IllegalAccessException {
        CpuFreq cpuFreq = new CpuFreq();
        final Field field = cpuFreq.getClass().getDeclaredField("startTime");
        field.setAccessible(true);
        field.set(cpuFreq, 1L);
        assertEquals(1L, cpuFreq.getStartTime());
        final Field zeroField = cpuFreq.getClass().getDeclaredField("startTime");
        zeroField.setAccessible(true);
        zeroField.set(cpuFreq, 0L);
        assertEquals(0L, cpuFreq.getStartTime());
        final Field negativeField = cpuFreq.getClass().getDeclaredField("startTime");
        negativeField.setAccessible(true);
        negativeField.set(cpuFreq, -1L);
        assertEquals(-1L, cpuFreq.getStartTime());
    }

    @Test
    void setStartTime() throws NoSuchFieldException, IllegalAccessException {
        CpuFreq cpuFreq = new CpuFreq();
        cpuFreq.setStartTime(1L);
        final Field field = cpuFreq.getClass().getDeclaredField("startTime");
        field.setAccessible(true);
        assertEquals(field.get(cpuFreq), 1L);
        final Field negativeField = cpuFreq.getClass().getDeclaredField("startTime");
        negativeField.setAccessible(true);
        cpuFreq.setStartTime(-1L);
        assertEquals(negativeField.get(cpuFreq), -1L);
        final Field zeroField = cpuFreq.getClass().getDeclaredField("startTime");
        zeroField.setAccessible(true);
        cpuFreq.setStartTime(0L);
        assertEquals(zeroField.get(cpuFreq), 0L);
    }

    @Test
    void getDuration() throws NoSuchFieldException, IllegalAccessException {
        CpuFreq cpuFreq = new CpuFreq();
        final Field field = cpuFreq.getClass().getDeclaredField("duration");
        field.setAccessible(true);
        field.set(cpuFreq, 1L);
        assertEquals(1L, cpuFreq.getDuration());
        final Field zeroField = cpuFreq.getClass().getDeclaredField("duration");
        zeroField.setAccessible(true);
        zeroField.set(cpuFreq, 0L);
        assertEquals(0L, cpuFreq.getDuration());
        final Field negativeField = cpuFreq.getClass().getDeclaredField("duration");
        negativeField.setAccessible(true);
        negativeField.set(cpuFreq, -1L);
        assertEquals(-1L, cpuFreq.getDuration());
    }

    @Test
    void setDuration() throws NoSuchFieldException, IllegalAccessException {
        CpuFreq cpuFreq = new CpuFreq();
        cpuFreq.setDuration(1L);
        final Field field = cpuFreq.getClass().getDeclaredField("duration");
        field.setAccessible(true);
        assertEquals(field.get(cpuFreq), 1L);
        final Field negativeField = cpuFreq.getClass().getDeclaredField("duration");
        negativeField.setAccessible(true);
        cpuFreq.setDuration(-1L);
        assertEquals(negativeField.get(cpuFreq), -1L);
        final Field zeroField = cpuFreq.getClass().getDeclaredField("duration");
        zeroField.setAccessible(true);
        cpuFreq.setDuration(0L);
        assertEquals(zeroField.get(cpuFreq), 0L);
    }

    @Test
    void isFlagFocus() throws NoSuchFieldException, IllegalAccessException {
        CpuFreq cpuFreq = new CpuFreq();
        final Field field = cpuFreq.getClass().getDeclaredField("flagFocus");
        field.setAccessible(true);
        field.set(cpuFreq, true);
        assertTrue(cpuFreq.isFlagFocus());
        final Field zeroField = cpuFreq.getClass().getDeclaredField("flagFocus");
        zeroField.setAccessible(true);
        zeroField.set(cpuFreq, false);
        assertFalse(cpuFreq.isFlagFocus());
    }

    @Test
    void setFlagFocus() throws NoSuchFieldException, IllegalAccessException {
        CpuFreq cpuFreq = new CpuFreq();
        cpuFreq.setFlagFocus(true);
        final Field field = cpuFreq.getClass().getDeclaredField("flagFocus");
        field.setAccessible(true);
        assertTrue((boolean) field.get(cpuFreq));
        final Field negativeField = cpuFreq.getClass().getDeclaredField("flagFocus");
        negativeField.setAccessible(true);
        cpuFreq.setFlagFocus(false);
        assertFalse((boolean) field.get(cpuFreq));
    }

    @Test
    void getMax() throws NoSuchFieldException, IllegalAccessException {
        CpuFreq cpuFreq = new CpuFreq();
        final Field field = cpuFreq.getClass().getDeclaredField("max");
        field.setAccessible(true);
        field.set(cpuFreq, 1D);
        assertEquals(1D, cpuFreq.getMax());
        final Field zeroField = cpuFreq.getClass().getDeclaredField("max");
        zeroField.setAccessible(true);
        zeroField.set(cpuFreq, 0D);
        assertEquals(0D, cpuFreq.getMax());
        final Field negativeField = cpuFreq.getClass().getDeclaredField("max");
        negativeField.setAccessible(true);
        negativeField.set(cpuFreq, -1D);
        assertEquals(-1D, cpuFreq.getMax());
    }

    @Test
    void setMax() throws NoSuchFieldException, IllegalAccessException {
        CpuFreq cpuFreq = new CpuFreq();
        cpuFreq.setMax(1D);
        final Field field = cpuFreq.getClass().getDeclaredField("max");
        field.setAccessible(true);
        assertEquals(field.get(cpuFreq), 1D);
        final Field negativeField = cpuFreq.getClass().getDeclaredField("max");
        negativeField.setAccessible(true);
        cpuFreq.setMax(-1D);
        assertEquals(negativeField.get(cpuFreq), -1D);
        final Field zeroField = cpuFreq.getClass().getDeclaredField("max");
        zeroField.setAccessible(true);
        cpuFreq.setMax(0D);
        assertEquals(zeroField.get(cpuFreq), 0D);
    }

}