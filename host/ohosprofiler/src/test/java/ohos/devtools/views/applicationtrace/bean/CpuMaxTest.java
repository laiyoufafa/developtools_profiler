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

/**
 * Cpu Max Test
 */
class CpuMaxTest {

    @Test
    void getCpu() throws NoSuchFieldException, IllegalAccessException {
        CpuMax cpuMax = new CpuMax();
        final Field field = cpuMax.getClass().getDeclaredField("cpu");
        field.setAccessible(true);
        field.set(cpuMax, 1);
        assertEquals(1, cpuMax.getCpu());
        final Field zeroField = cpuMax.getClass().getDeclaredField("cpu");
        zeroField.setAccessible(true);
        zeroField.set(cpuMax, null);
        assertNull(cpuMax.getCpu());
        final Field negativeField = cpuMax.getClass().getDeclaredField("cpu");
        negativeField.setAccessible(true);
        negativeField.set(cpuMax, -1);
        assertEquals(-1, cpuMax.getCpu());
    }

    @Test
    void setCpu() throws NoSuchFieldException, IllegalAccessException {
        CpuMax cpuMax = new CpuMax();
        final Field field = cpuMax.getClass().getDeclaredField("cpu");
        field.setAccessible(true);
        cpuMax.setCpu(1);
        assertEquals(1, field.get(cpuMax));
        final Field nullField = cpuMax.getClass().getDeclaredField("cpu");
        nullField.setAccessible(true);
        cpuMax.setCpu(null);
        assertNull(nullField.get(cpuMax));
        final Field negativeField = cpuMax.getClass().getDeclaredField("cpu");
        negativeField.setAccessible(true);
        cpuMax.setCpu(-1);
        assertEquals(-1, negativeField.get(cpuMax));
    }
}