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

package ohos.devtools.views.layout.swing;

import org.junit.Test;

/**
 * @Description GraphicsJpanelTest
 * @Date 2021/4/3 14:35
 **/
public class GraphicsJpanelTest {
    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_GraphicsJpanel_paint_0001
     * @tc.desc: chart scene test
     * @tc.type: functional testing
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void paint() {
        GraphicsJpanel graphicsJpanel = new GraphicsJpanel(new TaskPanel());
        graphicsJpanel.repaint();
    }
}