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

package ohos.devtools.views.layout.chartview.memory;

import org.junit.Assert;
import org.junit.Test;

/**
 * MemoryTreeTablePanelTest
 */
public class MemoryTreeTablePanelTest {
    /**
     * getMemoryTreeTablePanelTest
     *
     * @tc.name: getMemoryTreeTablePanelTest
     * @tc.number: OHOS_JAVA_memory_MemoryTreeTablePanel_getMemoryTreeTablePanelTest_0001
     * @tc.desc: getMemoryTreeTablePanelTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getMemoryTreeTablePanelTest01() {
        MemoryTreeTablePanel memoryTreeTablePanel =
                new MemoryTreeTablePanel(new MemoryItemView(), 10L, "chartName");
        Assert.assertNotNull(memoryTreeTablePanel);
    }
}
