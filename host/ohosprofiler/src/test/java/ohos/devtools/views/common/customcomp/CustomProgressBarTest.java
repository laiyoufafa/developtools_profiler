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

package ohos.devtools.views.common.customcomp;

import org.junit.Assert;
import org.junit.Test;

import javax.swing.JPanel;

/**
 * Custom Progress Bar Test
 *
 * @since 2021/2/1 9:31
 */
public class CustomProgressBarTest {
    /**
     * CustomProgressBarTest
     *
     * @tc.name: CustomProgressBarTest
     * @tc.number: OHOS_JAVA_View_CustomProgressBar_CustomProgressBarTest_0001
     * @tc.desc: Custom Progress Bar Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void customProgressBarTest() {
        CustomProgressBar customProgressBar = new CustomProgressBar(new JPanel());
        Assert.assertNotNull(customProgressBar);
    }
}