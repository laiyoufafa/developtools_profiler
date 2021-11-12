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

package ohos.devtools.views.layout.chartview.ability;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

/**
 * ProfilerAppAbilityTest
 *
 * @since 2021/2/1 9:31
 */
public class ProfilerAppAbilityTest {
    /**
     * refreshAbilityTimeTest
     *
     * @tc.name: refreshAbilityTimeTest
     * @tc.number: OHOS_JAVA_ability_ProfilerAppAbility_refreshAbilityTimeTest_0001
     * @tc.desc: refreshAbilityTimeTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void refreshAbilityTimeTest01() {
        new ProfilerAppAbility().refreshAbilityTime(10, 100, -1L);
        Assert.assertTrue(true);
    }

    /**
     * refreshActivityAbilityTest
     *
     * @tc.name: refreshActivityAbilityTest
     * @tc.number: OHOS_JAVA_ability_ProfilerAppAbility_refreshActivityAbilityTest_0001
     * @tc.desc: refreshActivityAbilityTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void refreshActivityAbilityTest01() {
        new ProfilerAppAbility().refreshActivityAbility(new ArrayList<>());
        Assert.assertTrue(true);
    }

    /**
     * refreshEventAbilityTest
     *
     * @tc.name: refreshEventAbilityTest
     * @tc.number: OHOS_JAVA_ability_ProfilerAppAbility_refreshEventAbilityTest_0001
     * @tc.desc: refreshEventAbilityTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void refreshEventAbilityTest01() {
        new ProfilerAppAbility().refreshEventAbility(new ArrayList<>());
        Assert.assertTrue(true);
    }
}
