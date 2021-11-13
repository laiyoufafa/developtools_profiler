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
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CpuTest {

    @Test
    void getCpu() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        final Field field = cpu.getClass().getDeclaredField("cpu");
        field.setAccessible(true);
        field.set(cpu, -1);
        assertEquals(-1, cpu.getCpu());
        final Field nullField = cpu.getClass().getDeclaredField("cpu");
        nullField.setAccessible(true);
        nullField.set(cpu, null);
        assertNull(cpu.getCpu());
        final Field emptyField = cpu.getClass().getDeclaredField("cpu");
        emptyField.setAccessible(true);
        emptyField.set(cpu, 0);
        assertEquals(0, cpu.getCpu());
    }

    @Test
    void setCpu() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        cpu.setCpu(0);
        final Field field = cpu.getClass().getDeclaredField("cpu");
        field.setAccessible(true);
        assertEquals(field.get(cpu), 0);
        cpu.setCpu(-1);
        final Field emptyField = cpu.getClass().getDeclaredField("cpu");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(cpu), -1);
        cpu.setCpu(null);
        final Field nullField = cpu.getClass().getDeclaredField("cpu");
        nullField.setAccessible(true);
        assertNull(nullField.get(cpu));
    }

    @Test
    void getName() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        final Field field = cpu.getClass().getDeclaredField("name");
        field.setAccessible(true);
        field.set(cpu, "cpu");
        assertEquals("cpu", cpu.getName());
        final Field nullField = cpu.getClass().getDeclaredField("name");
        nullField.setAccessible(true);
        nullField.set(cpu, null);
        assertNull(cpu.getName());
        final Field emptyField = cpu.getClass().getDeclaredField("name");
        emptyField.setAccessible(true);
        emptyField.set(cpu, "");
        assertEquals("", cpu.getName());
    }

    @Test
    void setName() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        cpu.setName("name");
        final Field field = cpu.getClass().getDeclaredField("name");
        field.setAccessible(true);
        assertEquals(field.get(cpu), "name");
        cpu.setName("");
        final Field emptyField = cpu.getClass().getDeclaredField("name");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(cpu), "");
        cpu.setName(null);
        final Field nullField = cpu.getClass().getDeclaredField("name");
        nullField.setAccessible(true);
        assertNull(nullField.get(cpu));
    }

    @Test
    void getStats() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        ArrayList<Integer> ints = new ArrayList<>();
        ints.add(1);
        ints.add(2);
        ints.add(3);
        ints.add(4);
        ints.add(5);
        ints.add(6);
        final Field field = cpu.getClass().getDeclaredField("stats");
        field.setAccessible(true);
        field.set(cpu, ints);
        assertEquals(ints, cpu.getStats());
        final Field nullField = cpu.getClass().getDeclaredField("stats");
        nullField.setAccessible(true);
        nullField.set(cpu, null);
        assertNull(cpu.getStats());
        final Field emptyField = cpu.getClass().getDeclaredField("stats");
        emptyField.setAccessible(true);
        emptyField.set(cpu, ints);
        assertEquals(ints, cpu.getStats());
    }

    @Test
    void setStats() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        ArrayList<Integer> ints = new ArrayList<>();
        ints.add(1);
        ints.add(2);
        ints.add(3);
        ints.add(4);
        ints.add(5);
        ints.add(6);
        cpu.setStats(ints);
        final Field field = cpu.getClass().getDeclaredField("stats");
        field.setAccessible(true);
        assertEquals(field.get(cpu), ints);
        ArrayList<Integer> emptyInts = new ArrayList<>();
        cpu.setStats(emptyInts);
        final Field emptyField = cpu.getClass().getDeclaredField("stats");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(cpu), emptyInts);
        cpu.setStats(null);
        final Field nullField = cpu.getClass().getDeclaredField("stats");
        nullField.setAccessible(true);
        assertNull(nullField.get(cpu));
    }

    @Test
    void getEndState() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        final Field field = cpu.getClass().getDeclaredField("endState");
        field.setAccessible(true);
        field.set(cpu, "endState");
        assertEquals("endState", cpu.getEndState());
        final Field nullField = cpu.getClass().getDeclaredField("endState");
        nullField.setAccessible(true);
        nullField.set(cpu, null);
        assertNull(cpu.getEndState());
        final Field emptyField = cpu.getClass().getDeclaredField("endState");
        emptyField.setAccessible(true);
        emptyField.set(cpu, "");
        assertEquals("", cpu.getEndState());

    }

    @Test
    void setEndState() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        cpu.setEndState("endState");
        final Field field = cpu.getClass().getDeclaredField("endState");
        field.setAccessible(true);
        assertEquals(field.get(cpu), "endState");
        cpu.setEndState("");
        final Field emptyField = cpu.getClass().getDeclaredField("endState");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(cpu), "");
        cpu.setEndState(null);
        final Field nullField = cpu.getClass().getDeclaredField("endState");
        nullField.setAccessible(true);
        assertNull(nullField.get(cpu));
    }

    @Test
    void getPriority() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        final Field field = cpu.getClass().getDeclaredField("priority");
        field.setAccessible(true);
        field.set(cpu, 0);
        assertEquals(0, cpu.getPriority());
        final Field nullField = cpu.getClass().getDeclaredField("priority");
        nullField.setAccessible(true);
        nullField.set(cpu, null);
        assertNull(cpu.getPriority());
        final Field emptyField = cpu.getClass().getDeclaredField("priority");
        emptyField.setAccessible(true);
        emptyField.set(cpu, -1);
        assertEquals(-1, cpu.getPriority());
    }

    @Test
    void setPriority() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        cpu.setPriority(0);
        final Field field = cpu.getClass().getDeclaredField("priority");
        field.setAccessible(true);
        assertEquals(field.get(cpu), 0);
        cpu.setPriority(-1);
        final Field emptyField = cpu.getClass().getDeclaredField("priority");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(cpu), -1);
        cpu.setPriority(null);
        final Field nullField = cpu.getClass().getDeclaredField("priority");
        nullField.setAccessible(true);
        assertNull(nullField.get(cpu));
    }

    @Test
    void getSchedId() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        final Field field = cpu.getClass().getDeclaredField("schedId");
        field.setAccessible(true);
        field.set(cpu, 0);
        assertEquals(0, cpu.getSchedId());
        final Field nullField = cpu.getClass().getDeclaredField("schedId");
        nullField.setAccessible(true);
        nullField.set(cpu, null);
        assertNull(cpu.getSchedId());
        final Field emptyField = cpu.getClass().getDeclaredField("schedId");
        emptyField.setAccessible(true);
        emptyField.set(cpu, -1);
        assertEquals(-1, cpu.getSchedId());
    }

    @Test
    void setSchedId() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        cpu.setSchedId(0);
        final Field field = cpu.getClass().getDeclaredField("schedId");
        field.setAccessible(true);
        assertEquals(field.get(cpu), 0);
        cpu.setSchedId(-1);
        final Field emptyField = cpu.getClass().getDeclaredField("schedId");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(cpu), -1);
        cpu.setSchedId(null);
        final Field nullField = cpu.getClass().getDeclaredField("schedId");
        nullField.setAccessible(true);
        assertNull(nullField.get(cpu));
    }

    @Test
    void getStartTime() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        final Field field = cpu.getClass().getDeclaredField("startTime");
        field.setAccessible(true);
        field.set(cpu, 0L);
        assertEquals(0L, cpu.getStartTime());
        final Field nullField = cpu.getClass().getDeclaredField("startTime");
        nullField.setAccessible(true);
        nullField.set(cpu, 1L);
        assertEquals(1L, cpu.getStartTime());
        final Field emptyField = cpu.getClass().getDeclaredField("startTime");
        emptyField.setAccessible(true);
        emptyField.set(cpu, -1L);
        assertEquals(-1L, cpu.getStartTime());
    }

    @Test
    void setStartTime() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        cpu.setStartTime(0L);
        final Field field = cpu.getClass().getDeclaredField("startTime");
        field.setAccessible(true);
        assertEquals(field.get(cpu), 0L);
        cpu.setStartTime(-1L);
        final Field emptyField = cpu.getClass().getDeclaredField("startTime");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(cpu), -1L);
        cpu.setStartTime(1L);
        final Field nullField = cpu.getClass().getDeclaredField("startTime");
        nullField.setAccessible(true);
        assertEquals(nullField.get(cpu), 1L);
    }

    @Test
    void getDuration() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        final Field field = cpu.getClass().getDeclaredField("duration");
        field.setAccessible(true);
        field.set(cpu, 0L);
        assertEquals(0L, cpu.getDuration());
        final Field nullField = cpu.getClass().getDeclaredField("duration");
        nullField.setAccessible(true);
        nullField.set(cpu, 1L);
        assertEquals(1L, cpu.getDuration());
        final Field emptyField = cpu.getClass().getDeclaredField("duration");
        emptyField.setAccessible(true);
        emptyField.set(cpu, -1L);
        assertEquals(-1L, cpu.getDuration());
    }

    @Test
    void setDuration() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        cpu.setDuration(0L);
        final Field field = cpu.getClass().getDeclaredField("duration");
        field.setAccessible(true);
        assertEquals(field.get(cpu), 0L);
        cpu.setDuration(-1L);
        final Field emptyField = cpu.getClass().getDeclaredField("duration");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(cpu), -1L);
        cpu.setDuration(1L);
        final Field nullField = cpu.getClass().getDeclaredField("duration");
        nullField.setAccessible(true);
        assertEquals(nullField.get(cpu), 1L);
    }

    @Test
    void getType() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        final Field field = cpu.getClass().getDeclaredField("type");
        field.setAccessible(true);
        field.set(cpu, "type");
        assertEquals("type", cpu.getType());
        final Field nullField = cpu.getClass().getDeclaredField("type");
        nullField.setAccessible(true);
        nullField.set(cpu, null);
        assertNull(cpu.getType());
        final Field emptyField = cpu.getClass().getDeclaredField("type");
        emptyField.setAccessible(true);
        emptyField.set(cpu, "");
        assertEquals("", cpu.getType());
    }

    @Test
    void setType() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        cpu.setType("type");
        final Field field = cpu.getClass().getDeclaredField("type");
        field.setAccessible(true);
        assertEquals(field.get(cpu), "type");
        cpu.setType("");
        final Field emptyField = cpu.getClass().getDeclaredField("type");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(cpu), "");
        cpu.setType(null);
        final Field nullField = cpu.getClass().getDeclaredField("type");
        nullField.setAccessible(true);
        assertNull(nullField.get(cpu));
    }

    @Test
    void getId() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        final Field field = cpu.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(cpu, 0);
        assertEquals(0, cpu.getId());
        final Field nullField = cpu.getClass().getDeclaredField("id");
        nullField.setAccessible(true);
        nullField.set(cpu, null);
        assertNull(cpu.getId());
        final Field emptyField = cpu.getClass().getDeclaredField("id");
        emptyField.setAccessible(true);
        emptyField.set(cpu, -1);
        assertEquals(-1, cpu.getId());
    }

    @Test
    void setId() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        cpu.setId(0);
        final Field field = cpu.getClass().getDeclaredField("id");
        field.setAccessible(true);
        assertEquals(field.get(cpu), 0);
        cpu.setId(-1);
        final Field emptyField = cpu.getClass().getDeclaredField("id");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(cpu), -1);
        cpu.setId(null);
        final Field nullField = cpu.getClass().getDeclaredField("id");
        nullField.setAccessible(true);
        assertNull(nullField.get(cpu));
    }

    @Test
    void getTid() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        final Field field = cpu.getClass().getDeclaredField("tid");
        field.setAccessible(true);
        field.set(cpu, 0);
        assertEquals(0, cpu.getTid());
        final Field nullField = cpu.getClass().getDeclaredField("tid");
        nullField.setAccessible(true);
        nullField.set(cpu, null);
        assertNull(cpu.getTid());
        final Field emptyField = cpu.getClass().getDeclaredField("tid");
        emptyField.setAccessible(true);
        emptyField.set(cpu, -1);
        assertEquals(-1, cpu.getTid());
    }

    @Test
    void setTid() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        cpu.setTid(0);
        final Field field = cpu.getClass().getDeclaredField("tid");
        field.setAccessible(true);
        assertEquals(field.get(cpu), 0);
        cpu.setTid(-1);
        final Field emptyField = cpu.getClass().getDeclaredField("tid");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(cpu), -1);
        cpu.setTid(null);
        final Field nullField = cpu.getClass().getDeclaredField("tid");
        nullField.setAccessible(true);
        assertNull(nullField.get(cpu));
    }

    @Test
    void getProcessCmdLine() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        final Field field = cpu.getClass().getDeclaredField("processCmdLine");
        field.setAccessible(true);
        field.set(cpu, "processCmdLine");
        assertEquals("processCmdLine", cpu.getProcessCmdLine());
        final Field nullField = cpu.getClass().getDeclaredField("processCmdLine");
        nullField.setAccessible(true);
        nullField.set(cpu, null);
        assertNull(cpu.getProcessCmdLine());
        final Field emptyField = cpu.getClass().getDeclaredField("processCmdLine");
        emptyField.setAccessible(true);
        emptyField.set(cpu, "");
        assertEquals("", cpu.getProcessCmdLine());
    }

    @Test
    void setProcessCmdLine() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        cpu.setProcessCmdLine("processCmdLine");
        final Field field = cpu.getClass().getDeclaredField("processCmdLine");
        field.setAccessible(true);
        assertEquals(field.get(cpu), "processCmdLine");
        cpu.setProcessCmdLine("");
        final Field emptyField = cpu.getClass().getDeclaredField("processCmdLine");
        emptyField.setAccessible(true);
        assertEquals(emptyField.get(cpu), "");
        cpu.setProcessCmdLine(null);
        final Field nullField = cpu.getClass().getDeclaredField("processCmdLine");
        nullField.setAccessible(true);
        assertNull(nullField.get(cpu));
    }

    @Test
    void getProcessName() throws NoSuchFieldException, IllegalAccessException {
        Cpu cpu = new Cpu();
        final Field field = cpu.getClass().getDeclaredField("processName");
        field.setAccessible(true);
        field.set(cpu, "processName");
        assertEquals("processName", cpu.getProcessName());
        final Field nullField = cpu.getClass().getDeclaredField("processName");
        nullField.setAccessible(true);
        nullField.set(cpu, null);
        assertNull(cpu.getProcessName());
        final Field emptyField = cpu.getClass().getDeclaredField("processName");
        emptyField.setAccessible(true);
        emptyField.set(cpu, "");
        assertEquals("", cpu.getProcessName());
    }
}