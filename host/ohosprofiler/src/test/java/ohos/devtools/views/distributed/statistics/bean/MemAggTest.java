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

class MemAggTest {
    private MemAgg memAgg = new MemAgg();

    @Test
    void getValue() {
        memAgg.setValue("value");
        assertEquals(memAgg.getValue(), "value");
    }

    @Test
    void setValue() {
        memAgg.setValue("value");
        assertEquals(memAgg.getValue(), "value");
    }

    @Test
    void getTime() {
        memAgg.setTime("time");
        assertEquals(memAgg.getTime(), "time");
    }

    @Test
    void setTime() {
        memAgg.setTime("time");
        assertEquals(memAgg.getTime(), "time");
    }

    @Test
    void getName() {
        memAgg.setName("name");
        assertEquals(memAgg.getName(), "name");
    }

    @Test
    void setName() {
        memAgg.setName("name");
        assertEquals(memAgg.getName(), "name");
    }

    @Test
    void getProcessName() {
        memAgg.setProcessName("processName");
        assertEquals(memAgg.getProcessName(), "processName");
    }

    @Test
    void setProcessName() {
        memAgg.setProcessName("processName");
        assertEquals(memAgg.getProcessName(), "processName");
    }

    @Test
    void testToString() {
        assertNotNull(memAgg.toString());
    }
}