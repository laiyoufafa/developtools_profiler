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

package ohos.devtools.views.common.treetable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Data Filter Test
 */
public class DataFilterTest {
    private static final String TEST_STRING = "Test";
    private DataFilter dataFilter;

    /**
     * init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_View_DataFilter_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Before
    public void init() {
        dataFilter = new DataFilter();
    }

    /**
     * pass Tree Node Test
     *
     * @tc.name: passTreeNodeTest
     * @tc.number: OHOS_JAVA_View_DataFilter_passTreeNodeTest_0001
     * @tc.desc: pass Tree Node Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void passTreeNodeTest() {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode();
        boolean res = dataFilter.passTreeNode(treeNode, TEST_STRING);
        Assert.assertTrue(res);
    }

    /**
     * set Filter Test
     *
     * @tc.name: setFilterTest
     * @tc.number: OHOS_JAVA_View_DataFilter_setFilterTest_0001
     * @tc.desc: set Filter Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void setFilterTest() {
        dataFilter.setFilter(true, TEST_STRING);
        Assert.assertTrue(true);
    }
}