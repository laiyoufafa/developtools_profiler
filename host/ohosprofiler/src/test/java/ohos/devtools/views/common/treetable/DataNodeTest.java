/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package ohos.devtools.views.common.treetable;

import ohos.devtools.views.common.Constant;
import ohos.devtools.views.common.ViewConstants;
import ohos.devtools.views.common.chart.treetable.DataNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * DataNodeTest
 *
 * @Description DataNode test
 * @Date 2021/4/3 20:29
 **/
public class DataNodeTest {
    /**
     * id
     */
    private Integer id = ViewConstants.NUM_2;

    /**
     * cId
     */
    private Integer cId = ViewConstants.NUM_3;

    /**
     * heapId
     */
    private Integer heapId = ViewConstants.NUM_4;

    /**
     * sessionId
     */
    private Long sessionId = Constant.ABNORMAL;

    /**
     * className
     */
    private String className = Constant.CPU_PLUG_NAME;

    /**
     * allocations
     */
    private Integer allocations = ViewConstants.NUM_5;

    /**
     * deallocations
     */
    private Integer deallocations = ViewConstants.NUM_6;

    /**
     * totalCount
     */
    private Integer totalCount = ViewConstants.NUM_7;

    /**
     * shallowSize
     */
    private Long shallowSize = Constant.ABNORMAL;

    /**
     * dataNode
     */
    private DataNode dataNode = new DataNode();

    /**
     * dataNodeOteher
     */
    private DataNode dataNodeOteher = new DataNode();

    /**
     * children
     */
    private ArrayList<DataNode> children = new ArrayList<>();

    /**
     * functional testing
     *
     * @tc.name: sort
     * @tc.number: OHOS_JAVA_treetable_DataNode_initObj
     * @tc.desc: sort
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Before
    public void initObj() {
        dataNode.setId(id);
        dataNode.setcId(cId);
        dataNode.setHeapId(heapId);
        dataNode.setSessionId(sessionId);
        dataNode.setClassName(className);
        dataNode.setAllocations(allocations);
        dataNode.setDeallocations(deallocations);
        dataNode.setTotalCount(totalCount);
        dataNode.setShallowSize(shallowSize);
        dataNode.setChildren(children);
        dataNodeOteher.setId(2);
        dataNodeOteher.setcId(2);
        dataNodeOteher.setHeapId(2);
    }

    /**
     * functional testing
     *
     * @tc.name: sort
     * @tc.number: OHOS_JAVA_treetable_DataNode_initObj
     * @tc.desc: sort
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void getIdTest() {
        int idTest = dataNode.getId();
        Assert.assertEquals(idTest, ViewConstants.NUM_2);
    }

    /**
     * functional testing
     *
     * @tc.name: sort
     * @tc.number: OHOS_JAVA_treetable_DataNode_initObj
     * @tc.desc: sort
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void getCIdTest() {
        int cidTest = dataNode.getcId();
        Assert.assertEquals(cidTest, ViewConstants.NUM_3);
    }

    /**
     * functional testing
     *
     * @tc.name: sort
     * @tc.number: OHOS_JAVA_treetable_DataNode_initObj
     * @tc.desc: sort
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void getHeapIdTest() {
        int heapIdTest = dataNode.getHeapId();
        Assert.assertEquals(heapIdTest, ViewConstants.NUM_4);
    }

    /**
     * functional testing
     *
     * @tc.name: sort
     * @tc.number: OHOS_JAVA_treetable_DataNode_initObj
     * @tc.desc: sort
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void getSessionIdTest() {
        long num = dataNode.getSessionId();
        Assert.assertEquals(num, -1);
    }

    /**
     * functional testing
     *
     * @tc.name: sort
     * @tc.number: OHOS_JAVA_treetable_DataNode_initObj
     * @tc.desc: sort
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void getClassNameTest() {
        String name = dataNode.getClassName();
        Assert.assertEquals(name, Constant.CPU_PLUG_NAME);
    }

    /**
     * functional testing
     *
     * @tc.name: sort
     * @tc.number: OHOS_JAVA_treetable_DataNode_initObj
     * @tc.desc: sort
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void getAllocationsTest() {
        int num = dataNode.getAllocations();
        Assert.assertEquals(num, ViewConstants.NUM_5);
    }

    /**
     * functional testing
     *
     * @tc.name: sort
     * @tc.number: OHOS_JAVA_treetable_DataNode_initObj
     * @tc.desc: sort
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void getDeallocationsTest() {
        int num = dataNode.getDeallocations();
        Assert.assertEquals(num, ViewConstants.NUM_6);
    }

    /**
     * functional testing
     *
     * @tc.name: sort
     * @tc.number: OHOS_JAVA_treetable_DataNode_initObj
     * @tc.desc: sort
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void getTotalCountTest() {
        int num = dataNode.getTotalCount();
        Assert.assertEquals(num, ViewConstants.NUM_7);
    }

    /**
     * functional testing
     *
     * @tc.name: sort
     * @tc.number: OHOS_JAVA_treetable_DataNode_initObj
     * @tc.desc: sort
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void getShallowSizeTest() {
        long num = dataNode.getShallowSize();
        Assert.assertEquals(num, -1);
    }

    /**
     * functional testing
     *
     * @tc.name: sort
     * @tc.number: OHOS_JAVA_treetable_DataNode_initObj
     * @tc.desc: sort
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void addChildrenTest() {
        dataNode.addChildren(dataNode);
        ArrayList<DataNode> childrenTest = dataNode.getChildren();
        Assert.assertNotNull(childrenTest);
    }

    /**
     * functional testing
     *
     * @tc.name: sort
     * @tc.number: OHOS_JAVA_treetable_DataNode_initObj
     * @tc.desc: sort
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void equalsTest() {
        boolean flag = dataNode.equals(dataNodeOteher);
        Assert.assertFalse(flag);
    }

    /**
     * functional testing
     *
     * @tc.name: sort
     * @tc.number: OHOS_JAVA_treetable_DataNode_initObj
     * @tc.desc: sort
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void equalsTest1() {
        boolean flag = dataNode.equals(dataNode);
        Assert.assertTrue(flag);
    }

    /**
     * functional testing
     *
     * @tc.name: sort
     * @tc.number: OHOS_JAVA_treetable_DataNode_initObj
     * @tc.desc: sort
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void hashCodeTest() {
        int num = dataNode.hashCode();
        Assert.assertNotNull(num);
    }

    /**
     * functional testing
     *
     * @tc.name: sort
     * @tc.number: OHOS_JAVA_treetable_DataNode_initObj
     * @tc.desc: sort
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void toStrTest() {
        String str = dataNode.toStr();
        Assert.assertNotNull(str);
    }

    /**
     * functional testing
     *
     * @tc.name: sort
     * @tc.number: OHOS_JAVA_treetable_DataNode_initObj
     * @tc.desc: sort
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void toStringTest() {
        String str = dataNode.toString();
        Assert.assertNotNull(str);
    }

}
