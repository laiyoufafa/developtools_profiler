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

class StatsTest {
    Stats stats = new Stats();

    @Test
    void getName() {
        stats.setName("name");
        assertEquals(stats.getName(), "name");
    }

    @Test
    void setName() {
        stats.setName("name");
        assertEquals(stats.getName(), "name");
    }

    @Test
    void getType() {
        stats.setType("type");
        assertEquals(stats.getType(), "type");
    }

    @Test
    void setType() {
        stats.setType("type");
        assertEquals(stats.getType(), "type");
    }

    @Test
    void getCount() {
        stats.setCount(1);
        assertEquals(stats.getCount(), 1);
    }

    @Test
    void setCount() {
        stats.setCount(1);
        assertEquals(stats.getCount(), 1);
    }

    @Test
    void getSeverity() {
        stats.setSeverity("Severity");
        assertEquals(stats.getSeverity(), "Severity");
    }

    @Test
    void setSeverity() {
        stats.setSeverity("Severity");
        assertEquals(stats.getSeverity(), "Severity");
    }

    @Test
    void getSource() {
        stats.setSource("Source");
        assertEquals(stats.getSource(), "Source");
    }

    @Test
    void setSource() {
        stats.setSource("Source");
        assertEquals(stats.getSource(), "Source");
    }

    @Test
    void testToString() {
        assertNotNull(stats.toString());
    }
}