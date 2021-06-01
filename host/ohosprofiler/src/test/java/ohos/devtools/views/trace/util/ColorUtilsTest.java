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

package ohos.devtools.views.trace.util;

import ohos.devtools.views.trace.bean.CpuData;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.Assert.assertEquals;

/**
 * test ColorUtils class .
 *
 * @version 1.0
 * @date 2021/4/24 17:01
 **/
class ColorUtilsTest {
    /**
     * test function the testGetColor .
     */
    @Test
    void testGetColor() {
        Color color = ColorUtils.getColor(1);
        assertEquals(152, color.getRed());
        assertEquals(150, color.getGreen());
        assertEquals(128, color.getBlue());
    }

    /**
     * test function the testHash .
     */
    @Test
    void testHash() {
        assertEquals(16, ColorUtils.hash("1", ColorUtils.MD_PALETTE.length));
    }

    /**
     * test function the testColorForThread .
     */
    @Test
    void testColorForThread() {
        CpuData cpuData = new CpuData();
        cpuData.setProcessId(1);
        assertEquals(ColorUtils.MD_PALETTE[16], ColorUtils.colorForThread(cpuData));
    }

    /**
     * test function the testColorForTid .
     */
    @Test
    void testColorForTid() {
        assertEquals(ColorUtils.MD_PALETTE[16], ColorUtils.colorForTid(1));
    }

}