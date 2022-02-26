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

import com.intellij.ui.components.JBPanel;
import org.junit.Assert;
import org.junit.Test;

/**
 * SystemPanelTest
 *
 * @since 2021/5/19 16:39
 */
public class SystemPanelTest {
    /**
     * get Instance getSystemPanelTest
     *
     * @tc.name: getSystemPanelTest
     * @tc.number: OHOS_JAVA_layout_SystemPanel_getSystemPanelTest_0001
     * @tc.desc: getSystemPanelTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getSystemPanelTest01() {
        SystemPanel systemPanel = new SystemPanel(new JBPanel(), null);
        Assert.assertNotNull(systemPanel);
    }
}
