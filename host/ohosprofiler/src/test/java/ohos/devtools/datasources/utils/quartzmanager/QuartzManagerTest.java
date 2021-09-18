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

package ohos.devtools.datasources.utils.quartzmanager;

import org.junit.Assert;
import org.junit.Test;

/**
 * Timed task management
 */
public class QuartzManagerTest {
    /**
     * functional testing getInstance
     *
     * @tc.name: QuartzManager getInstance
     * @tc.number: OHOS_JAVA_quartz_QuartzManager_getInstance_0001
     * @tc.desc: QuartzManager getInstance
     * @tc.type: functional testing
     * @tc.require: SR-011
     */
    @Test
    public void getInstanceTest() {
        QuartzManager quartzManager = QuartzManager.getInstance();
        Assert.assertNotNull(quartzManager);
    }
}
