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

/**
 * AbilityCardInfoTest
 *
 * @since 2021/2/1 9:31
 */
public class AbilityCardInfoTest {
    /**
     * get Instance toStringTest
     *
     * @tc.name: toStringTest
     * @tc.number: OHOS_JAVA_ability_AbilityCardInfo_toStringTest_0001
     * @tc.desc: toStringTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void toStringTest01() {
        String str = new AbilityCardInfo().toString();
        Assert.assertEquals(
            "AbilityCardInfo{sessionId=0, applicationName='null', startTime=0, endTime=0, abilityCardStatus=null}",
            str);
    }
}
