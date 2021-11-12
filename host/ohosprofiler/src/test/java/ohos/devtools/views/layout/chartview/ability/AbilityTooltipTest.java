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

import com.intellij.ui.components.JBPanel;
import org.junit.Assert;
import org.junit.Test;

import java.awt.event.MouseEvent;

/**
 * AbilityTooltipTest
 *
 * @since 2021/2/1 9:31
 */
public class AbilityTooltipTest {
    /**
     * hideTipTest
     *
     * @tc.name: hideTipTest
     * @tc.number: OHOS_JAVA_ability_AbilityTooltip_hideTipTest_0001
     * @tc.desc: hideTipTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void hideTipTest01() {
        new AbilityTooltip(new ProfilerAppAbility()).hideTip();
        Assert.assertTrue(true);
    }

    /**
     * followWithMouseTest
     *
     * @tc.name: followWithMouseTest
     * @tc.number: OHOS_JAVA_ability_AbilityTooltip_followWithMouseTest_0001
     * @tc.desc: followWithMouseTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void followWithMouseTest01() {
        JBPanel jBPanel = new JBPanel();
        MouseEvent mouseEvent = new MouseEvent(jBPanel, 1, 1, 1, 1, 1, 1, true, 1);
        new AbilityTooltip(new ProfilerAppAbility()).followWithMouse(mouseEvent);
        Assert.assertTrue(true);
    }
}
