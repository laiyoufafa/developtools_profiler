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

class CpuTest {
    private Cpu cpu = new Cpu();

    @Test
    void getTid() {
        cpu.setTid(1);
        assertEquals(cpu.getTid(), 1);
    }

    @Test
    void setTid() {
        cpu.setTid(1);
        assertEquals(cpu.getTid(), 1);
    }

    @Test
    void getPid() {
        cpu.setPid(1);
        assertEquals(cpu.getPid(), 1);
    }

    @Test
    void setPid() {
        cpu.setPid(1);
        assertEquals(cpu.getPid(), 1);
    }

    @Test
    void getCpu() {
        cpu.setCpu("CPU");
        assertEquals(cpu.getCpu(), "CPU");
    }

    @Test
    void setCpu() {
        cpu.setCpu("CPU");
        assertEquals(cpu.getCpu(), "CPU");
    }

    @Test
    void getDuration() {
        cpu.setDuration("DURATION");
        assertEquals(cpu.getDuration(), "DURATION");
    }

    @Test
    void setDuration() {
        cpu.setDuration("DURATION");
        assertEquals(cpu.getDuration(), "DURATION");
    }

    @Test
    void getMinFreq() {
        cpu.setMinFreq("MinFreq");
        assertEquals(cpu.getMinFreq(), "MinFreq");
    }

    @Test
    void setMinFreq() {
        cpu.setMinFreq("MinFreq");
        assertEquals(cpu.getMinFreq(), "MinFreq");
    }

    @Test
    void getMaxFreq() {
        cpu.setMaxFreq("MaxFreq");
        assertEquals(cpu.getMaxFreq(), "MaxFreq");
    }

    @Test
    void setMaxFreq() {
        cpu.setMaxFreq("MaxFreq");
        assertEquals(cpu.getMaxFreq(), "MaxFreq");
    }

    @Test
    void getAvgFrequency() {
        cpu.setAvgFrequency("AvgFrequency");
        assertEquals(cpu.getAvgFrequency(), "AvgFrequency");
    }

    @Test
    void setAvgFrequency() {
        cpu.setAvgFrequency("AvgFrequency");
        assertEquals(cpu.getAvgFrequency(), "AvgFrequency");
    }

    @Test
    void getProcessName() {
        cpu.setProcessName("ProcessName");
        assertEquals(cpu.getProcessName(), "ProcessName");
    }

    @Test
    void setProcessName() {
        cpu.setProcessName("ProcessName");
        assertEquals(cpu.getProcessName(), "ProcessName");
    }

    @Test
    void getThreadName() {
        cpu.setThreadName("ThreadName");
        assertEquals(cpu.getThreadName(), "ThreadName");
    }

    @Test
    void setThreadName() {
        cpu.setThreadName("ThreadName");
        assertEquals(cpu.getThreadName(), "ThreadName");
    }

    @Test
    void testToString() {
        assertNotNull(cpu.toString());
    }
}