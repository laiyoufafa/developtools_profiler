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

class ProcessThreadTest {
    private ProcessThread processThread = new ProcessThread();

    @Test
    void getPid() {
        processThread.setPid(1);
        assertEquals(processThread.getPid(), 1);
    }

    @Test
    void setPid() {
        processThread.setPid(1);
        assertEquals(processThread.getPid(), 1);
    }

    @Test
    void getProcessName() {
        processThread.setProcessName("ProcessName");
        assertEquals(processThread.getProcessName(), "ProcessName");
    }

    @Test
    void setProcessName() {
        processThread.setProcessName("ProcessName");
        assertEquals(processThread.getProcessName(), "ProcessName");
    }

    @Test
    void getThreadName() {
        processThread.setThreadName("ThreadName");
        assertEquals(processThread.getThreadName(), "ThreadName");
    }

    @Test
    void setThreadName() {
        processThread.setThreadName("ThreadName");
        assertEquals(processThread.getThreadName(), "ThreadName");
    }

    @Test
    void testToString() {
        assertNotNull(processThread.toString());
    }
}