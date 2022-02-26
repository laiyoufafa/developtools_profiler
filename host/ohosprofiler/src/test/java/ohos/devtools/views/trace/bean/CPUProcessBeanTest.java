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
 * test CPUProcessBean class
 *
 * @since 2021/4/24 18:04
 */
class CPUProcessBeanTest {
    /**
     * test get the AvgDuration .
     */
    @Test
    void getAvgDuration() {
        CPUProcessBean cpuProcessBean = new CPUProcessBean(0, 0, "", "", "");
        cpuProcessBean.setAvgDuration(3L);
        assertEquals(3L, cpuProcessBean.getAvgDuration());
    }

    /**
     * test set the AvgDuration .
     */
    @Test
    void setAvgDuration() {
        CPUProcessBean cpuProcessBean = new CPUProcessBean(0, 0, "", "", "");
        cpuProcessBean.setAvgDuration(3L);
        assertEquals(3L, cpuProcessBean.getAvgDuration());
    }

    /**
     * test get the WallDuration .
     */
    @Test
    void getWallDuration() {
        CPUProcessBean cpuProcessBean = new CPUProcessBean(0, 0, "", "", "");
        cpuProcessBean.setWallDuration(3L);
        assertEquals(3L, cpuProcessBean.getWallDuration());
    }

    /**
     * test set the WallDuration .
     */
    @Test
    void setWallDuration() {
        CPUProcessBean cpuProcessBean = new CPUProcessBean(0, 0, "", "", "");
        cpuProcessBean.setWallDuration(3L);
        assertEquals(3L, cpuProcessBean.getWallDuration());
    }

    /**
     * test get the Pid .
     */
    @Test
    void getPid() {
        CPUProcessBean cpuProcessBean = new CPUProcessBean(0, 0, "", "", "");
        cpuProcessBean.setPid("pid");
        assertEquals("pid", cpuProcessBean.getPid());
    }

    /**
     * test set the Pid .
     */
    @Test
    void setPid() {
        CPUProcessBean cpuProcessBean = new CPUProcessBean(0, 0, "", "", "");
        cpuProcessBean.setPid("pid");
        assertEquals("pid", cpuProcessBean.getPid());
    }

    /**
     * test get the Occurrences .
     */
    @Test
    void getOccurrences() {
        CPUProcessBean cpuProcessBean = new CPUProcessBean(0, 0, "", "", "");
        cpuProcessBean.setOccurrences("Occurrences");
        assertEquals("Occurrences", cpuProcessBean.getOccurrences());
    }

    /**
     * test set the Occurrences .
     */
    @Test
    void setOccurrences() {
        CPUProcessBean cpuProcessBean = new CPUProcessBean(0, 0, "", "", "");
        cpuProcessBean.setOccurrences("Occurrences");
        assertEquals("Occurrences", cpuProcessBean.getOccurrences());
    }

    /**
     * test get the Process .
     */
    @Test
    void getProcess() {
        CPUProcessBean cpuProcessBean = new CPUProcessBean(0, 0, "", "", "");
        cpuProcessBean.setProcess("Process");
        assertEquals("Process", cpuProcessBean.getProcess());
    }

    /**
     * test set the Process .
     */
    @Test
    void setProcess() {
        CPUProcessBean cpuProcessBean = new CPUProcessBean(0, 0, "", "", "");
        cpuProcessBean.setProcess("Process");
        assertEquals("Process", cpuProcessBean.getProcess());
    }
}