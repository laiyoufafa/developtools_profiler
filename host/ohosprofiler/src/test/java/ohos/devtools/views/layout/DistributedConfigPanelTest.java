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

package ohos.devtools.views.layout;

import org.junit.Assert;
import org.junit.Test;

/**
 * Distributed Config Panel Test
 *
 * @since 2021/2/1 9:31
 */
public class DistributedConfigPanelTest {
    /**
     * get Instance getDistributedConfigPanel
     *
     * @tc.name: getDistributedConfigPanel
     * @tc.number: OHOS_JAVA_layout_DistributedConfigPanel_getDistributedConfigPanel_0001
     * @tc.desc: getDistributedConfigPanel
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getDistributedConfigPanelTest01() {
        DistributedConfigPanel distributedConfigPanel = new DistributedConfigPanel(new TaskPanel());
        Assert.assertNotNull(distributedConfigPanel);
    }
}
