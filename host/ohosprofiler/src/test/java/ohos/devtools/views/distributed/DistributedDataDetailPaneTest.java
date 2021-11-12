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

package ohos.devtools.views.distributed;

import ohos.devtools.views.distributed.bean.DetailBean;
import org.junit.jupiter.api.Test;

import javax.swing.tree.DefaultMutableTreeNode;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DistributedDataDetailPaneTest {
    DistributedDataDetailPane distributedDataDetailPane = new DistributedDataDetailPane();

    @Test
    void resetAllNode() {
        DistributedDataDetailPane.resetAllNode(new DefaultMutableTreeNode());
        assertNotNull(distributedDataDetailPane);
    }

    @Test
    void getCurrentSelectBean() {
        distributedDataDetailPane.setCurrentSelectBean(new DetailBean());
        assertNotNull(distributedDataDetailPane.getCurrentSelectBean());
    }

    @Test
    void setCurrentSelectBean() {
        distributedDataDetailPane.setCurrentSelectBean(new DetailBean());
        assertNotNull(distributedDataDetailPane.getCurrentSelectBean());
    }

    @Test
    void testFreshTreeData() {
        distributedDataDetailPane.freshTreeData(null);
        assertNotNull(distributedDataDetailPane);
    }

    @Test
    void getNodeContainSearch() {
        assert !distributedDataDetailPane.getNodeContainSearch(new DefaultMutableTreeNode(), "searchText");
    }
}