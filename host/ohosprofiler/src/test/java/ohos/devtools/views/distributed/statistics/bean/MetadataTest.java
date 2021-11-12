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

class MetadataTest {
    private Metadata metadata = new Metadata();

    @Test
    void getName() {
        metadata.setName("name");
        assertEquals(metadata.getName(), "name");
    }

    @Test
    void setName() {
        metadata.setName("name");
        assertEquals(metadata.getName(), "name");
    }

    @Test
    void getValue() {
        metadata.setValue("value");
        assertEquals(metadata.getValue(), "value");
    }

    @Test
    void setValue() {
        metadata.setValue("value");
        assertEquals(metadata.getValue(), "value");
    }

    @Test
    void testToString() {
        assertNotNull(metadata.toString());
    }
}