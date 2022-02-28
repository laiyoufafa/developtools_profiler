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
 * test CPUThreadBean class
 *
 * @date 2021/4/24 18:04
 */
class CPUThreadBeanTest {
    /**
     * test get the avgDuration .
     */
    @Test
    void getAvgDuration() {
        CPUThreadBean cpuThreadBean = new CPUThreadBean(0, 0, "", "", "", "", "");
        cpuThreadBean.setAvgDuration(3L);
        assertEquals(3L, cpuThreadBean.getAvgDuration());
    }

    /**
     * test set the avgDuration .
     */
    @Test
    void setAvgDuration() {
        CPUThreadBean cpuThreadBean = new CPUThreadBean(0, 0, "", "", "", "", "");
        cpuThreadBean.setAvgDuration(3L);
        assertEquals(3L, cpuThreadBean.getAvgDuration());
    }

    /**
     * test get the WallDuration .
     */
    @Test
    void getWallDuration() {
        CPUThreadBean cpuThreadBean = new CPUThreadBean(0, 0, "", "", "", "", "");
        cpuThreadBean.setWallDuration(3L);
        assertEquals(3L, cpuThreadBean.getWallDuration());
    }

    /**
     * test set the WallDuration .
     */
    @Test
    void setWallDuration() {
        CPUThreadBean cpuThreadBean = new CPUThreadBean(0, 0, "", "", "", "", "");
        cpuThreadBean.setWallDuration(3L);
        assertEquals(3L, cpuThreadBean.getWallDuration());
    }

    /**
     * test get the Pid .
     */
    @Test
    void getPid() {
        CPUThreadBean cpuThreadBean = new CPUThreadBean(0, 0, "", "", "", "", "");
        cpuThreadBean.setPid("Pid");
        assertEquals("Pid", cpuThreadBean.getPid());
    }

    /**
     * test set the Pid .
     */
    @Test
    void setPid() {
        CPUThreadBean cpuThreadBean = new CPUThreadBean(0, 0, "", "", "", "", "");
        cpuThreadBean.setPid("Pid");
        assertEquals("Pid", cpuThreadBean.getPid());
    }

    /**
     * test get the Tid .
     */
    @Test
    void getTid() {
        CPUThreadBean cpuThreadBean = new CPUThreadBean(0, 0, "", "", "", "", "");
        cpuThreadBean.setTid("Tid");
        assertEquals("Tid", cpuThreadBean.getTid());
    }

    /**
     * test set the Tid .
     */
    @Test
    void setTid() {
        CPUThreadBean cpuThreadBean = new CPUThreadBean(0, 0, "", "", "", "", "");
        cpuThreadBean.setTid("Tid");
        assertEquals("Tid", cpuThreadBean.getTid());
    }

    /**
     * test get the Occurrences .
     */
    @Test
    void getOccurrences() {
        CPUThreadBean cpuThreadBean = new CPUThreadBean(0, 0, "", "", "", "", "");
        cpuThreadBean.setOccurrences("Occurrences");
        assertEquals("Occurrences", cpuThreadBean.getOccurrences());
    }

    /**
     * test set the Occurrences .
     */
    @Test
    void setOccurrences() {
        CPUThreadBean cpuThreadBean = new CPUThreadBean(0, 0, "", "", "", "", "");
        cpuThreadBean.setOccurrences("Occurrences");
        assertEquals("Occurrences", cpuThreadBean.getOccurrences());
    }

    /**
     * test get the Process .
     */
    @Test
    void getProcess() {
        CPUThreadBean cpuThreadBean = new CPUThreadBean(0, 0, "", "", "", "", "");
        cpuThreadBean.setProcess("Process");
        assertEquals("Process", cpuThreadBean.getProcess());
    }

    /**
     * test set the Process .
     */
    @Test
    void setProcess() {
        CPUThreadBean cpuThreadBean = new CPUThreadBean(0, 0, "", "", "", "", "");
        cpuThreadBean.setProcess("Process");
        assertEquals("Process", cpuThreadBean.getProcess());
    }

    /**
     * test get the Thread .
     */
    @Test
    void getThread() {
        CPUThreadBean cpuThreadBean = new CPUThreadBean(0, 0, "", "", "", "", "");
        cpuThreadBean.setThread("Thread");
        assertEquals("Thread", cpuThreadBean.getThread());
    }

    /**
     * test set the Thread .
     */
    @Test
    void setThread() {
        CPUThreadBean cpuThreadBean = new CPUThreadBean(0, 0, "", "", "", "", "");
        cpuThreadBean.setThread("Thread");
        assertEquals("Thread", cpuThreadBean.getThread());
    }
}