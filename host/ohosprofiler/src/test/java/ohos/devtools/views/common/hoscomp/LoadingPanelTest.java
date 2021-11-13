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

package ohos.devtools.views.common.hoscomp;

import ohos.devtools.views.layout.chartview.LoadingPanel;
import org.junit.Assert;
import org.junit.Test;

/**
 * Loading Panel Test
 */
public class LoadingPanelTest {
    /**
     * Loading Panel Test
     *
     * @tc.name: LoadingPanelTest
     * @tc.number: OHOS_JAVA_View_LoadingPanel_LoadingPanelTest_0001
     * @tc.desc: Loading Panel Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void loadingPanelTest() {
        LoadingPanel loadingPanel = new LoadingPanel();
        Assert.assertNotNull(loadingPanel);
    }
}