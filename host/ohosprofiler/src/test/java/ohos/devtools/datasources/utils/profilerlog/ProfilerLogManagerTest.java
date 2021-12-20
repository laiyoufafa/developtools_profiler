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

package ohos.devtools.datasources.utils.profilerlog;

import org.apache.logging.log4j.Level;
import org.junit.Assert;
import org.junit.Test;

/**
 * Profiler Log Manager Test
 *
 * @since 2021/2/1 9:31
 */
public class ProfilerLogManagerTest {
    /**
     * functional testing updateLogLevel
     *
     * @tc.name: ProfilerLogManager updateLogLevel
     * @tc.number: OHOS_JAVA_profiler_ProfilerLogManager_updateLogLevel_0001
     * @tc.desc: ProfilerLogManager updateLogLevel
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void updateLogLevelTest01() {
        boolean res = ProfilerLogManager.updateLogLevel(null);
        Assert.assertFalse(res);
    }

    /**
     * functional testing updateLogLevel
     *
     * @tc.name: ProfilerLogManager updateLogLevel
     * @tc.number: OHOS_JAVA_profiler_ProfilerLogManager_updateLogLevel_0002
     * @tc.desc: ProfilerLogManager updateLogLevel
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void updateLogLevelTest02() {
        boolean res = ProfilerLogManager.updateLogLevel(Level.OFF);
        Assert.assertTrue(res);
    }

    /**
     * functional testing updateLogLevel
     *
     * @tc.name: ProfilerLogManager updateLogLevel
     * @tc.number: OHOS_JAVA_profiler_ProfilerLogManager_updateLogLevel_0003
     * @tc.desc: ProfilerLogManager updateLogLevel
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void updateLogLevelTest03() {
        boolean res = ProfilerLogManager.updateLogLevel(Level.ERROR);
        Assert.assertTrue(res);
    }
}
