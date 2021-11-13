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

import ohos.devtools.services.memory.nativeservice.NativeDataExternalInterface;
import ohos.devtools.views.layout.chartview.memory.nativehook.NativeHookTreeTablePanel;
import org.junit.Assert;
import org.junit.Test;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * NativeHookTreeTablePanelTest
 *
 * @since 2021/2/1 9:31
 */
public class NativeHookTreeTablePanelTest {
    private NativeDataExternalInterface nativeDataExternalInterface = new NativeDataExternalInterface();

    /**
     * arrangeCallStackDataNodeTest
     *
     * @tc.name: arrangeCallStackDataNodeTest
     * @tc.number: OHOS_JAVA_memory_NativeHookTreeTablePanel_arrangeCallStackDataNodeTest_0001
     * @tc.desc: arrangeCallStackDataNodeTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void arrangeCallStackDataNodeTest01() {
        DefaultMutableTreeNode defaultMutableTreeNode =
            new NativeHookTreeTablePanel(10L, nativeDataExternalInterface).arrangeCallStackDataNode();
        Assert.assertNotNull(defaultMutableTreeNode);
    }

    /**
     * arrangeAllocationMethodDataNodeTest
     *
     * @tc.name: arrangeAllocationMethodDataNodeTest
     * @tc.number: OHOS_JAVA_memory_NativeHookTreeTablePanel_arrangeAllocationMethodDataNodeTest_0001
     * @tc.desc: arrangeAllocationMethodDataNodeTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void arrangeAllocationMethodDataNodeTest01() {
        DefaultMutableTreeNode defaultMutableTreeNode =
            new NativeHookTreeTablePanel(10L, nativeDataExternalInterface).arrangeAllocationMethodDataNode();
        Assert.assertNotNull(defaultMutableTreeNode);
    }

    /**
     * createTreeTableTest
     *
     * @tc.name: createTreeTableTest
     * @tc.number: OHOS_JAVA_memory_NativeHookTreeTablePanel_createTreeTableTest_0001
     * @tc.desc: createTreeTableTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createTreeTableTest01() {
        new NativeHookTreeTablePanel(10L, nativeDataExternalInterface).createTreeTable(new DefaultMutableTreeNode());
        Assert.assertTrue(true);
    }
}
