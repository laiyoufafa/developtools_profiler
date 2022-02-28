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

import java.util.ArrayList;

import ohos.devtools.services.memory.agentbean.MemoryInstanceInfo;
import ohos.devtools.services.memory.agentdao.MemoryInstanceDao;
import ohos.devtools.services.memory.agentdao.MemoryUpdateInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Memory Instance Dao Test
 *
 * @since 2021/2/1 9:31
 */
public class MemoryInstanceDaoTest {
    private MemoryInstanceDao memoryInstanceDao;
    private MemoryInstanceInfo memoryInstanceInfo;
    private MemoryInstanceInfo memoryInstance;

    /**
     * functional testing getInstance
     *
     * @tc.name: getInstance
     * @tc.number: OHOS_JAVA_Service_MemoryInstanceDao_getInstance_0001
     * @tc.desc: getInstance
     * @tc.type: functional testing
     * @tc.require: AR000FK61M
     */
    @Before
    public void getInstance() {
        memoryInstanceDao = new MemoryInstanceDao();
        memoryInstanceInfo = new MemoryInstanceInfo();
        memoryInstanceInfo.setId(1);
        memoryInstanceInfo.setcId(1);
        memoryInstanceInfo.setInstance("String Lang");
        memoryInstanceInfo.setInstanceId(2);
        memoryInstanceInfo.setAllocTime(1L);
        memoryInstanceInfo.setCreateTime(2L);
        memoryInstanceInfo.setAllocTime(3L);
        memoryInstanceInfo.setDeallocTime(12L);
        memoryInstance = new MemoryInstanceInfo();
        memoryInstance.setId(1);
        memoryInstance.setcId(1);
        memoryInstance.setInstance("god");
        memoryInstance.setInstanceId(3);
        memoryInstance.setCreateTime(4L);
        memoryInstance.setAllocTime(11L);
        memoryInstance.setDeallocTime(22L);
    }

    /**
     * functional testing createMemoryInstance
     *
     * @tc.name: createMemoryInstance
     * @tc.number: OHOS_JAVA_Service_MemoryInstanceDao_createMemoryInstance_0001
     * @tc.desc: createMemoryInstance
     * @tc.type: functional testing
     * @tc.require: AR000FK61M
     */
    @Test
    public void createMemoryInstance() {
        boolean createMemoryResult = memoryInstanceDao.createMemoryInstance();
        Assert.assertTrue(createMemoryResult);
    }

    /**
     * functional testing getAllMemoryInstanceInfos
     *
     * @tc.name: getAllMemoryInstanceInfos
     * @tc.number: OHOS_JAVA_Service_MemoryInstanceDao_getAllMemoryInstanceInfos_0001
     * @tc.desc: getAllMemoryInstanceInfos
     * @tc.type: functional testing
     * @tc.require: AR000FK61M
     */
    @Test
    public void testGetAllMemoryInstanceInfos() {
        ArrayList<MemoryUpdateInfo> allMemoryInstanceInfos = memoryInstanceDao.getAllMemoryInstanceInfos();
        Assert.assertNotNull(allMemoryInstanceInfos);
    }

    /**
     * functional testing deleteSessionData
     *
     * @tc.name: deleteSessionData
     * @tc.number: OHOS_JAVA_Service_MemoryInstanceDao_deleteSessionData_0001
     * @tc.desc: deleteSessionData
     * @tc.type: functional testing
     * @tc.require: AR000FK61M
     */
    @Test
    public void testDeleteSessionData() {
        memoryInstanceDao.deleteSessionData(1);
    }
}