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
 * @date 2021/4/24 18:04
 */
class CPUProcessBeanTest {
    /**
     * test get the AvgDuration .
     */
    @Test
    void getAvgDuration() {
        assertEquals(3L, new CPUProcessBean(0, 0, "", "", "") {{
            setAvgDuration(3L);
        }}.getAvgDuration());
    }

    /**
     * test set the AvgDuration .
     */
    @Test
    void setAvgDuration() {
        assertEquals(3L, new CPUProcessBean(0, 0, "", "", "") {{
            setAvgDuration(3L);
        }}.getAvgDuration());
    }

    /**
     * test get the WallDuration .
     */
    @Test
    void getWallDuration() {
        assertEquals(3L, new CPUProcessBean(0, 0, "", "", "") {{
            setWallDuration(3L);
        }}.getWallDuration());
    }

    /**
     * test set the WallDuration .
     */
    @Test
    void setWallDuration() {
        assertEquals(3L, new CPUProcessBean(0, 0, "", "", "") {{
            setWallDuration(3L);
        }}.getWallDuration());
    }

    /**
     * test get the Pid .
     */
    @Test
    void getPid() {
        assertEquals("pid", new CPUProcessBean(0, 0, "", "", "") {{
            setPid("pid");
        }}.getPid());
    }

    /**
     * test set the Pid .
     */
    @Test
    void setPid() {
        assertEquals("pid", new CPUProcessBean(0, 0, "", "", "") {{
            setPid("pid");
        }}.getPid());
    }

    /**
     * test get the Occurrences .
     */
    @Test
    void getOccurrences() {
        assertEquals("Occurrences", new CPUProcessBean(0, 0, "", "", "") {{
            setOccurrences("Occurrences");
        }}.getOccurrences());
    }

    /**
     * test set the Occurrences .
     */
    @Test
    void setOccurrences() {
        assertEquals("Occurrences", new CPUProcessBean(0, 0, "", "", "") {{
            setOccurrences("Occurrences");
        }}.getOccurrences());
    }

    /**
     * test get the Process .
     */
    @Test
    void getProcess() {
        assertEquals("Process", new CPUProcessBean(0, 0, "", "", "") {{
            setProcess("Process");
        }}.getProcess());
    }

    /**
     * test set the Process .
     */
    @Test
    void setProcess() {
        assertEquals("Process", new CPUProcessBean(0, 0, "", "", "") {{
            setProcess("Process");
        }}.getProcess());
    }
}