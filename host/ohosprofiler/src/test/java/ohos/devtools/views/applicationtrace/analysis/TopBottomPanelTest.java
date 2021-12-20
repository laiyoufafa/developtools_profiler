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

package ohos.devtools.views.applicationtrace.analysis;

import org.junit.jupiter.api.Test;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Top Bottom Panel Test
 *
 * @since 2021/2/1 9:31
 */
class TopBottomPanelTest {
    TopBottomPanel topBottomPanel = new TopBottomPanel();

    @Test
    void getNodeContainSearch() {
        boolean nodeContainSearch = TopBottomPanel.getNodeContainSearch(new DefaultMutableTreeNode(), "");
        assertFalse(nodeContainSearch);
    }

    @Test
    void resetAllNode() {
        TopBottomPanel.resetAllNode(new DefaultMutableTreeNode());
        assertNotNull(topBottomPanel);
    }

    @Test
    void change() {
        topBottomPanel.change(0, 0, new ArrayList<>());
        assertNotNull(topBottomPanel);
    }

    @Test
    void testChange() {
        topBottomPanel.change(0, 0, 0);
        assertNotNull(topBottomPanel);
    }
}