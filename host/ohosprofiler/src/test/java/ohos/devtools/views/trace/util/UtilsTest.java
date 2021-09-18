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

import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * test Utils class .
 *
 * @date 2021/4/24 17:51
 */
class UtilsTest {
    /**
     * test function the getStatusMap .
     */
    @Test
    void getStatusMap() {
        Map<String, String> statusMap = Utils.getInstance().getStatusMap();
        assertNotNull(statusMap);
        assertEquals("Sleeping", statusMap.get("S"));
    }

    /**
     * test function the getInstance .
     */
    @Test
    void getInstance() {
        Utils instance = Utils.getInstance();
        assertNotEquals(null, instance);
    }

    /**
     * test function the pointInRect .
     */
    @Test
    void pointInRect() {
        boolean pointInRect = Utils.pointInRect(new Rectangle(0, 0, 100, 100), 50, 50);
        assertTrue(pointInRect);
    }

    /**
     * test function the getEndState .
     */
    @Test
    void getEndState() {
        String endState = Utils.getEndState("S");
        assertEquals("Sleeping", endState);
    }
}