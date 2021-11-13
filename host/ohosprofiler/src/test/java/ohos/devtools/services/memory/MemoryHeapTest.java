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

import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.services.memory.agentbean.AgentHeapBean;
import ohos.devtools.services.memory.agentbean.ClassInfo;
import ohos.devtools.services.memory.agentbean.MemoryHeapInfo;
import ohos.devtools.services.memory.agentbean.MemoryInstanceDetailsInfo;
import ohos.devtools.services.memory.agentbean.MemoryInstanceInfo;
import ohos.devtools.services.memory.agentdao.ClassInfoManager;
import ohos.devtools.services.memory.agentdao.MemoryHeapManager;
import ohos.devtools.services.memory.agentdao.MemoryInstanceDetailsManager;
import ohos.devtools.services.memory.agentdao.MemoryInstanceManager;
import org.apache.logging.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Heap data test
 */
public class MemoryHeapTest {
    private MemoryHeapManager memoryHeapManager;
    private MemoryInstanceManager memoryInstanceManager;
    private MemoryInstanceDetailsManager memoryInstanceDetailsManager;
    private ClassInfoManager classInfoManager;
    private ClassInfo classInfo;
    private MemoryInstanceInfo memoryInstanceInfo;
    private MemoryInstanceDetailsInfo memoryInstanceDetailsInfo;
    private MemoryHeapInfo memoryHeapInfo;

    /**
     * functional test
     *
     * @tc.name: initObj
     * @tc.number: OHOS_JAVA_Service_MemoryHeap_initObj_0001
     * @tc.desc: initObj
     * @tc.type: functional testing
     * @tc.require: SR000FK61Q
     */
    @Before
    public void initObj() {
        // 应用初始化 Step1 初始化数据中心
        ProfilerLogManager.getSingleton().updateLogLevel(Level.ERROR);
        DataBaseApi apo = DataBaseApi.getInstance();
        apo.initDataSourceManager();

        memoryHeapManager = new MemoryHeapManager();
        memoryInstanceManager = new MemoryInstanceManager();
        memoryInstanceDetailsManager = new MemoryInstanceDetailsManager();
        classInfoManager = new ClassInfoManager();
        classInfo = new ClassInfo();
        classInfo.setcId(2);
        classInfo.setClassName("java/Lang/String");
        memoryInstanceInfo = new MemoryInstanceInfo();
        memoryInstanceInfo.setInstanceId(2);
        memoryInstanceInfo.setcId(1);
        memoryInstanceInfo.setInstance("java/Lang/String");
        memoryInstanceInfo.setCreateTime(20210326L);
        memoryInstanceInfo.setAllocTime(20210326L);
        memoryInstanceInfo.setDeallocTime(20210328L);
        memoryInstanceDetailsInfo = new MemoryInstanceDetailsInfo();
        memoryInstanceDetailsInfo.setInstanceId(1);
        memoryInstanceDetailsInfo.setFrameId(1);
        memoryInstanceDetailsInfo.setClassName("java/Lang/String");
        memoryInstanceDetailsInfo.setMethodName("init");
        memoryInstanceDetailsInfo.setFieldName("name");
        memoryInstanceDetailsInfo.setLineNumber(2);
        memoryHeapInfo = new MemoryHeapInfo();
        memoryHeapInfo.setcId(1);
        memoryHeapInfo.setHeapId(1);
        memoryHeapInfo.setSessionId(1L);
        memoryHeapInfo.setAllocations(10);
        memoryHeapInfo.setDeallocations(0);
        memoryHeapInfo.setTotalCount(79);
        memoryHeapInfo.setShallowSize(348L);
        memoryHeapInfo.setCreateTime(20210406L);
    }

    /**
     * functional test
     *
     * @tc.name: getMemoryHeap
     * @tc.number: OHOS_JAVA_Service_MemoryHeap_getMemoryHeap_0001
     * @tc.desc: getMemoryHeap
     * @tc.type: functional testing
     * @tc.require: SR000FK61Q
     */
    @Test
    public void getMemoryHeap() {
        List<AgentHeapBean> memoryHeapInfos =
            memoryHeapManager.getMemoryHeapInfos(19354329L, 20210317L, 20210322L);
        Assert.assertNotNull(memoryHeapInfos);
    }

    /**
     * functional test
     *
     * @tc.name: getMemoryInstance
     * @tc.number: OHOS_JAVA_Service_MemoryHeap_getMemoryInstance_0001
     * @tc.desc: getMemoryInstance
     * @tc.type: functional testing
     * @tc.require: SR000FK61Q
     */
    @Test
    public void getMemoryInstance() {
        ArrayList<MemoryInstanceInfo> memoryInstanceInfos =
            memoryInstanceManager.getMemoryInstanceInfos(2, 20210326L, 20210330L);
        Assert.assertNotNull(memoryInstanceInfos);
    }

    /**
     * functional test
     *
     * @tc.name: getMemoryInstanceDetails
     * @tc.number: OHOS_JAVA_Service_MemoryHeap_getMemoryInstanceDetails_0001
     * @tc.desc: getMemoryInstanceDetails
     * @tc.type: functional testing
     * @tc.require: SR000FK61Q
     */
    @Test
    public void getMemoryInstanceDetails() {
        ArrayList<MemoryInstanceDetailsInfo> memoryInstanceDetailsInfos =
            memoryInstanceDetailsManager.getMemoryInstanceDetailsInfos(2);
        Assert.assertNotNull(memoryInstanceDetailsInfos);
    }

    /**
     * functional test
     *
     * @tc.name: insertClassInfo
     * @tc.number: OHOS_JAVA_Service_MemoryHeap_insertClassInfo_0001
     * @tc.desc: insertClassInfo
     * @tc.type: functional testing
     * @tc.require: SR000FK61Q
     */
    @Test
    public void insertClassInfo() {
        classInfoManager.insertClassInfo(classInfo);
    }
}
