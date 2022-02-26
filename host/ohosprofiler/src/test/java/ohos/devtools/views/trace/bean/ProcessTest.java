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
 * test Process class
 *
 * @date 2021/4/24 18:05
 */
class ProcessTest {
    /**
     * test get the Pid .
     */
    @Test
    void getPid() {
        Process process = new Process();
        process.setPid(3);
        assertEquals(3, process.getPid());
    }

    /**
     * test set the Pid .
     */
    @Test
    void setPid() {
        Process process = new Process();
        process.setPid(3);
        assertEquals(3, process.getPid());
    }

    /**
     * test get the Name .
     */
    @Test
    void getName() {
        Process process = new Process();
        process.setName("Process");
        assertEquals("Process", process.getName());
    }

    /**
     * test set the Name .
     */
    @Test
    void setName() {
        Process process = new Process();
        process.setName("Process");
        assertEquals("Process", process.getName());
    }
}