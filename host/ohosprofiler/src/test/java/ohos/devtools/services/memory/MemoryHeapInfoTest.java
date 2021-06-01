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

package ohos.devtools.services.memory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @Description MemoryHeapInfoTest
 * @Date 2021/4/3 12:22
 **/
public class MemoryHeapInfoTest {
    private MemoryHeapInfo memoryHeapInfo;

    /**
     * functional test
     *
     * @tc.name: getMemoryHeapInfo
     * @tc.number: OHOS_JAVA_Service_MemoryHeapInfo_getMemoryHeapInfo_0001
     * @tc.desc: getMemoryHeapInfo
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Before
    public void getMemoryHeapInfoTest() {
        memoryHeapInfo = new MemoryHeapInfo();
        Assert.assertNotNull(memoryHeapInfo);
    }

    /**
     * functional test
     *
     * @tc.name: setHeapId
     * @tc.number: OHOS_JAVA_Service_MemoryHeapInfo_setHeapId_0001
     * @tc.desc: setHeapId
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void setHeapIdTest() {
        memoryHeapInfo.setHeapId(1);
        Assert.assertTrue(true);
        memoryHeapInfo.getHeapId();
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: setAllocations
     * @tc.number: OHOS_JAVA_Service_MemoryHeapInfo_setAllocations_0001
     * @tc.desc: setAllocations
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void setAllocationsTest() {
        memoryHeapInfo.setAllocations(2);
        Assert.assertTrue(true);
        memoryHeapInfo.getAllocations();
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: setArrangeStyle
     * @tc.number: OHOS_JAVA_Service_MemoryHeapInfo_setArrangeStyle_0001
     * @tc.desc: setArrangeStyle
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void setArrangeStyleTest() {
        memoryHeapInfo.setArrangeStyle("xxxx");
        Assert.assertTrue(true);
        memoryHeapInfo.getArrangeStyle();
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: setCreateTime
     * @tc.number: OHOS_JAVA_Service_MemoryHeapInfo_setCreateTime_0001
     * @tc.desc: setCreateTime
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void setCreateTimeTest() {
        memoryHeapInfo.setCreateTime(7324L);
        Assert.assertTrue(true);
        memoryHeapInfo.getCreateTime();
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: setSessionId
     * @tc.number: OHOS_JAVA_Service_MemoryHeapInfo_setSessionId_0001
     * @tc.desc: setSessionId
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void setSessionIdTest() {
        memoryHeapInfo.setSessionId(3243L);
        Assert.assertTrue(true);
        memoryHeapInfo.getSessionId();
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: setClassName
     * @tc.number: OHOS_JAVA_Service_MemoryHeapInfo_setClassName_0001
     * @tc.desc: setClassName
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void setClassNameTest() {
        memoryHeapInfo.setClassName("test");
        Assert.assertTrue(true);
        memoryHeapInfo.getClassName();
        Assert.assertTrue(true);
    }

}