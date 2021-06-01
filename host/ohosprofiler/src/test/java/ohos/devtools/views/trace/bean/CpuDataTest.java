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

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * test cpuData class
 *
 * @version 1.0
 * @date 2021/4/24 18:04
 **/
class CpuDataTest {
    /**
     * test get the number of cpu .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void getCpu() throws NoSuchFieldException, IllegalAccessException {
        int random = (int) (Math.random() * 100);
        CpuData cpuData = new CpuData();
        final Field field = cpuData.getClass().getDeclaredField("cpu");
        field.setAccessible(true);
        field.set(cpuData, random);
        assertEquals(random, cpuData.getCpu());
    }

    /**
     * test set the number of cpu .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void setCpu() throws NoSuchFieldException, IllegalAccessException {
        int random = (int) (Math.random() * 100);
        CpuData cpuData = new CpuData();
        cpuData.setCpu(random);
        final Field field = cpuData.getClass().getDeclaredField("cpu");
        field.setAccessible(true);
        assertEquals(random, field.get(cpuData));
    }

    /**
     * test set the name .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void getName() throws NoSuchFieldException, IllegalAccessException {
        String name = "name";
        CpuData cpuData = new CpuData();
        final Field field = cpuData.getClass().getDeclaredField("name");
        field.setAccessible(true);
        field.set(cpuData, name);
        assertEquals(name, cpuData.getName());
    }

    /**
     * test set the name .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void setName() throws NoSuchFieldException, IllegalAccessException {
        String name = "name";
        CpuData cpuData = new CpuData();
        cpuData.setName(name);
        final Field field = cpuData.getClass().getDeclaredField("name");
        field.setAccessible(true);
        assertEquals(name, field.get(cpuData));
    }

    /**
     * test set the stats .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void getStats() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<Integer> stats = new ArrayList<>() {{
            add(1);
        }};
        CpuData cpuData = new CpuData();
        final Field field = cpuData.getClass().getDeclaredField("stats");
        field.setAccessible(true);
        field.set(cpuData, stats);
        assertEquals(stats, cpuData.getStats());
    }

    /**
     * test set the stats .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void setStats() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<Integer> stats = new ArrayList<>() {{
            add(1);
        }};
        CpuData cpuData = new CpuData();
        cpuData.setStats(stats);
        final Field field = cpuData.getClass().getDeclaredField("stats");
        field.setAccessible(true);
        assertEquals(stats, field.get(cpuData));
    }

    /**
     * test set the endState .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void getEndState() throws NoSuchFieldException, IllegalAccessException {
        String endState = "endState";
        CpuData cpuData = new CpuData();
        final Field field = cpuData.getClass().getDeclaredField("endState");
        field.setAccessible(true);
        field.set(cpuData, endState);
        assertEquals(endState, cpuData.getEndState());
    }

    /**
     * test set the endState .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void setEndState() throws NoSuchFieldException, IllegalAccessException {
        String endState = "endState";
        CpuData cpuData = new CpuData();
        cpuData.setEndState(endState);
        final Field field = cpuData.getClass().getDeclaredField("endState");
        field.setAccessible(true);
        assertEquals(endState, field.get(cpuData));
    }

    /**
     * test get the priority .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void getPriority() throws NoSuchFieldException, IllegalAccessException {
        int priority = (int) (Math.random() * 100);
        CpuData cpuData = new CpuData();
        final Field field = cpuData.getClass().getDeclaredField("priority");
        field.setAccessible(true);
        field.set(cpuData, priority);
        assertEquals(priority, cpuData.getPriority());
    }

    /**
     * test set the priority .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void setPriority() throws NoSuchFieldException, IllegalAccessException {
        int priority = (int) (Math.random() * 100);
        CpuData cpuData = new CpuData();
        cpuData.setPriority(priority);
        final Field field = cpuData.getClass().getDeclaredField("priority");
        field.setAccessible(true);
        assertEquals(priority, field.get(cpuData));
    }

    /**
     * test get the schedId .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void getSchedId() throws NoSuchFieldException, IllegalAccessException {
        int schedId = (int) (Math.random() * 100);
        CpuData cpuData = new CpuData();
        final Field field = cpuData.getClass().getDeclaredField("schedId");
        field.setAccessible(true);
        field.set(cpuData, schedId);
        assertEquals(schedId, cpuData.getSchedId());
    }

    /**
     * test set the schedId .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void setSchedId() throws NoSuchFieldException, IllegalAccessException {
        int schedId = (int) (Math.random() * 100);
        CpuData cpuData = new CpuData();
        cpuData.setPriority(schedId);
        assertEquals(schedId, cpuData.getPriority());
    }

    /**
     * test get the startTime .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void getStartTime() throws NoSuchFieldException, IllegalAccessException {
        long startTime = Double.doubleToLongBits((Math.random() * 100));
        CpuData cpuData = new CpuData();
        final Field field = cpuData.getClass().getDeclaredField("startTime");
        field.setAccessible(true);
        field.set(cpuData, startTime);
        assertEquals(startTime, cpuData.getStartTime());
    }

    /**
     * test set the startTime .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void setStartTime() throws NoSuchFieldException, IllegalAccessException {
        long startTime = Double.doubleToLongBits((Math.random() * 100));
        CpuData cpuData = new CpuData();
        cpuData.setStartTime(startTime);
        final Field field = cpuData.getClass().getDeclaredField("startTime");
        field.setAccessible(true);
        assertEquals(startTime, field.get(cpuData));
    }

    /**
     * test get the duration .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void getDuration() throws NoSuchFieldException, IllegalAccessException {
        long duration = Double.doubleToLongBits((Math.random() * 100));
        CpuData cpuData = new CpuData();
        final Field field = cpuData.getClass().getDeclaredField("duration");
        field.setAccessible(true);
        field.set(cpuData, duration);
        assertEquals(duration, cpuData.getDuration());
    }

    /**
     * test set the duration .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void setDuration() throws NoSuchFieldException, IllegalAccessException {
        long duration = Double.doubleToLongBits((Math.random() * 100));
        CpuData cpuData = new CpuData();
        cpuData.setDuration(duration);
        final Field field = cpuData.getClass().getDeclaredField("duration");
        field.setAccessible(true);
        assertEquals(duration, field.get(cpuData));
    }

    /**
     * test get the type .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void getType() throws NoSuchFieldException, IllegalAccessException {
        String type = "type";
        CpuData cpuData = new CpuData();
        final Field field = cpuData.getClass().getDeclaredField("type");
        field.setAccessible(true);
        field.set(cpuData, type);
        assertEquals(type, cpuData.getType());
    }

    /**
     * test set the type .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void setType() throws NoSuchFieldException, IllegalAccessException {
        String type = "type";
        CpuData cpuData = new CpuData();
        cpuData.setType(type);
        final Field field = cpuData.getClass().getDeclaredField("type");
        field.setAccessible(true);
        assertEquals(type, field.get(cpuData));
    }

    /**
     * test get the id .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void getId() throws NoSuchFieldException, IllegalAccessException {
        int id = 1;
        CpuData cpuData = new CpuData();
        final Field field = cpuData.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(cpuData, id);
        assertEquals(id, cpuData.getId());
    }

    /**
     * test set the id .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void setId() throws NoSuchFieldException, IllegalAccessException {
        int id = 1;
        CpuData cpuData = new CpuData();
        cpuData.setId(id);
        final Field field = cpuData.getClass().getDeclaredField("id");
        field.setAccessible(true);
        assertEquals(id, field.get(cpuData));
    }

    /**
     * test get the tid .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void getTid() throws NoSuchFieldException, IllegalAccessException {
        int tid = 1;
        CpuData cpuData = new CpuData();
        final Field field = cpuData.getClass().getDeclaredField("tid");
        field.setAccessible(true);
        field.set(cpuData, tid);
        assertEquals(tid, cpuData.getTid());
    }

    /**
     * test set the tid .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void setTid() throws NoSuchFieldException, IllegalAccessException {
        CpuData cpuData = new CpuData();
        cpuData.setId(1);
        assertEquals(1, cpuData.getId());
    }

    /**
     * test get the ProcessCmdLine .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void getProcessCmdLine() throws NoSuchFieldException, IllegalAccessException {
        String processCmdLine = "processCmdLine";
        CpuData cpuData = new CpuData();
        final Field field = cpuData.getClass().getDeclaredField("processCmdLine");
        field.setAccessible(true);
        field.set(cpuData, processCmdLine);
        assertEquals(processCmdLine, cpuData.getProcessCmdLine());
    }

    /**
     * test set the ProcessCmdLine .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void setProcessCmdLine() throws NoSuchFieldException, IllegalAccessException {
        String processCmdLine = "processCmdLine";
        CpuData cpuData = new CpuData();
        cpuData.setProcessCmdLine(processCmdLine);
        final Field field = cpuData.getClass().getDeclaredField("processCmdLine");
        field.setAccessible(true);
        assertEquals(processCmdLine, field.get(cpuData));
    }

    /**
     * test get the ProcessName .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void getProcessName() throws NoSuchFieldException, IllegalAccessException {
        String processName = "processName";
        CpuData cpuData = new CpuData();
        final Field field = cpuData.getClass().getDeclaredField("processName");
        field.setAccessible(true);
        field.set(cpuData, processName);
        assertEquals(processName, cpuData.getProcessName());
    }

    /**
     * test set the ProcessName .
     */
    @Test
    void setProcessName() {
        String processName = "processName";
        CpuData cpuData = new CpuData();
        cpuData.setProcessCmdLine(processName);
        assertEquals(processName, cpuData.getProcessCmdLine());
    }

    /**
     * test get the ProcessId .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void getProcessId() throws NoSuchFieldException, IllegalAccessException {
        CpuData cpuData = new CpuData();
        cpuData.setProcessId(1);
        assertEquals(1, cpuData.getProcessId());
    }

    /**
     * test set the ProcessName .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void setProcessId() throws NoSuchFieldException, IllegalAccessException {
        CpuData cpuData = new CpuData();
        cpuData.setProcessId(1);
        assertEquals(1, cpuData.getProcessId());
    }

    /**
     * test set the select .
     *
     * @throws NoSuchFieldException   throw NoSuchFieldException
     * @throws IllegalAccessException throw IllegalAccessException
     */
    @Test
    void select() throws NoSuchFieldException, IllegalAccessException {
        CpuData cpuData = new CpuData();
        cpuData.select(true);
        final Field field = cpuData.getClass().getDeclaredField("isSelected");
        field.setAccessible(true);
        assertEquals(true, field.get(cpuData));
    }

}