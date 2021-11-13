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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * test TimeUtils class .
 *
 * @since 2021/4/24 17:03
 */
class TimeUtilsTest {
    /**
     * test function the getSecondFromNSecond .
     */
    @Test
    void getSecondFromNSecond() {
        String str = TimeUtils.getSecondFromNSecond(1000L);
        assertEquals("1.0μs", str);
    }

    /**
     * test function the getTimeString .
     */
    @Test
    void getTimeString() {
        String timeString = TimeUtils.getTimeString(1000L);
        assertEquals("1μs", timeString);
    }
}