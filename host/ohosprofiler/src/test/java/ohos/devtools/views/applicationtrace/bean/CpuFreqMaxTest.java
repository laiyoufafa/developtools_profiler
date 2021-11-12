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

class CpuFreqMaxTest {

    @Test
    void getName() throws NoSuchFieldException, IllegalAccessException {
        CpuFreqMax cpuFreqMax = new CpuFreqMax();
        final Field field = cpuFreqMax.getClass().getDeclaredField("name");
        field.setAccessible(true);
        field.set(cpuFreqMax, "1");
        assertEquals("1", cpuFreqMax.getName());
        final Field zeroField = cpuFreqMax.getClass().getDeclaredField("name");
        zeroField.setAccessible(true);
        zeroField.set(cpuFreqMax, null);
        assertNull(cpuFreqMax.getName());
        final Field negativeField = cpuFreqMax.getClass().getDeclaredField("name");
        negativeField.setAccessible(true);
        negativeField.set(cpuFreqMax, "");
        assertEquals("", cpuFreqMax.getName());
    }

    @Test
    void setName() throws NoSuchFieldException, IllegalAccessException {
        CpuFreqMax cpuFreqMax = new CpuFreqMax();
        cpuFreqMax.setName("1");
        final Field field = cpuFreqMax.getClass().getDeclaredField("name");
        field.setAccessible(true);
        assertEquals(field.get(cpuFreqMax), "1");
        final Field negativeField = cpuFreqMax.getClass().getDeclaredField("name");
        negativeField.setAccessible(true);
        cpuFreqMax.setName("");
        assertEquals(negativeField.get(cpuFreqMax), "");
        final Field zeroField = cpuFreqMax.getClass().getDeclaredField("name");
        zeroField.setAccessible(true);
        cpuFreqMax.setName("name");
        assertEquals(zeroField.get(cpuFreqMax), "name");
    }

    @Test
    void getValue() throws NoSuchFieldException, IllegalAccessException {
        CpuFreqMax cpuFreqMax = new CpuFreqMax();
        final Field field = cpuFreqMax.getClass().getDeclaredField("value");
        field.setAccessible(true);
        field.set(cpuFreqMax, 1D);
        assertEquals(1D, cpuFreqMax.getValue());
        final Field zeroField = cpuFreqMax.getClass().getDeclaredField("value");
        zeroField.setAccessible(true);
        zeroField.set(cpuFreqMax, 0D);
        assertEquals(cpuFreqMax.getValue(), 0D);
        final Field negativeField = cpuFreqMax.getClass().getDeclaredField("value");
        negativeField.setAccessible(true);
        negativeField.set(cpuFreqMax, -1D);
        assertEquals(-1D, cpuFreqMax.getValue());
    }

    @Test
    void setValue() throws NoSuchFieldException, IllegalAccessException {
        CpuFreqMax cpuFreqMax = new CpuFreqMax();
        cpuFreqMax.setValue(1D);
        final Field field = cpuFreqMax.getClass().getDeclaredField("value");
        field.setAccessible(true);
        assertEquals(field.get(cpuFreqMax), 1D);
        final Field negativeField = cpuFreqMax.getClass().getDeclaredField("value");
        negativeField.setAccessible(true);
        cpuFreqMax.setValue(0D);
        assertEquals(negativeField.get(cpuFreqMax), 0D);
        final Field zeroField = cpuFreqMax.getClass().getDeclaredField("value");
        zeroField.setAccessible(true);
        cpuFreqMax.setValue(-1D);
        assertEquals(zeroField.get(cpuFreqMax), -1D);
    }

    @Test
    void getMaxFreq() throws NoSuchFieldException, IllegalAccessException {
        CpuFreqMax cpuFreqMax = new CpuFreqMax();
        final Field field = cpuFreqMax.getClass().getDeclaredField("maxFreq");
        field.setAccessible(true);
        field.set(cpuFreqMax, 1);
        assertEquals(1, cpuFreqMax.getMaxFreq());
        final Field zeroField = cpuFreqMax.getClass().getDeclaredField("maxFreq");
        zeroField.setAccessible(true);
        zeroField.set(cpuFreqMax, 0);
        assertEquals(cpuFreqMax.getMaxFreq(), 0);
        final Field negativeField = cpuFreqMax.getClass().getDeclaredField("maxFreq");
        negativeField.setAccessible(true);
        negativeField.set(cpuFreqMax, -1);
        assertEquals(-1, cpuFreqMax.getMaxFreq());
    }

    @Test
    void setMaxFreq() throws NoSuchFieldException, IllegalAccessException {
        CpuFreqMax cpuFreqMax = new CpuFreqMax();
        cpuFreqMax.setMaxFreq(1);
        final Field field = cpuFreqMax.getClass().getDeclaredField("maxFreq");
        field.setAccessible(true);
        assertEquals(field.get(cpuFreqMax), 1);
        final Field negativeField = cpuFreqMax.getClass().getDeclaredField("maxFreq");
        negativeField.setAccessible(true);
        cpuFreqMax.setMaxFreq(0);
        assertEquals(negativeField.get(cpuFreqMax), 0);
        final Field zeroField = cpuFreqMax.getClass().getDeclaredField("maxFreq");
        zeroField.setAccessible(true);
        cpuFreqMax.setMaxFreq(-1);
        assertEquals(zeroField.get(cpuFreqMax), -1);
    }
}