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

package ohos.devtools.views.distributed.statistics.bean;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MemoryTest {
    private Memory memory = new Memory();

    @Test
    void getMaxNum() {
        memory.setMaxNum(1);
        assertEquals(memory.getMaxNum(), 1);
    }

    @Test
    void setMaxNum() {
        memory.setMaxNum(1);
        assertEquals(memory.getMaxNum(), 1);
    }

    @Test
    void getMinNum() {
        memory.setMinNum(1);
        assertEquals(memory.getMinNum(), 1);
    }

    @Test
    void setMinNum() {
        memory.setMinNum(1);
        assertEquals(memory.getMinNum(), 1);
    }

    @Test
    void getName() {
        memory.setName("name");
        assertEquals(memory.getName(), "name");
    }

    @Test
    void setName() {
        memory.setName("name");
        assertEquals(memory.getName(), "name");
    }

    @Test
    void getProcessName() {
        memory.setProcessName("ProcessName");
        assertEquals(memory.getProcessName(), "ProcessName");
    }

    @Test
    void setProcessName() {
        memory.setProcessName("ProcessName");
        assertEquals(memory.getProcessName(), "ProcessName");
    }

    @Test
    void testToString() {
        assertNotNull(memory.toString());
    }
}