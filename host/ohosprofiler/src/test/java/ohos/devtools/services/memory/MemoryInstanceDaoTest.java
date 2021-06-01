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
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ohos.devtools.datasources.utils.session.service.SessionManager;

/**
 * MemoryInstanceDaoTest
 *
 * @version 1.0
 * @date 2021/04/05 17:12
 **/
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
        SessionManager.getInstance().setDevelopMode(true);
        memoryInstanceDao = MemoryInstanceDao.getInstance();
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
        SessionManager.getInstance().setDevelopMode(true);
        boolean createMemoryResult = memoryInstanceDao.createMemoryInstance();
        Assert.assertTrue(createMemoryResult);
    }

    /**
     * functional testing insertMemoryInstanceInfo
     *
     * @tc.name: insertMemoryInstanceInfo
     * @tc.number: OHOS_JAVA_Service_MemoryInstanceDao_insertMemoryInstanceInfo_0001
     * @tc.desc: insertMemoryInstanceInfo
     * @tc.type: functional testing
     * @tc.require: AR000FK61M
     */
    @Test
    public void testInsertMemoryInstanceInfo() {
        memoryInstanceDao.insertMemoryInstanceInfo(memoryInstanceInfo);
    }

    /**
     * functional testing insertMemoryInstanceInfos
     *
     * @tc.name: insertMemoryInstanceInfos
     * @tc.number: OHOS_JAVA_Service_MemoryInstanceDao_insertMemoryInstanceInfos_0001
     * @tc.desc: insertMemoryInstanceInfos
     * @tc.type: functional testing
     * @tc.require: AR000FK61M
     */
    @Test
    public void testInsertMemoryInstanceInfos() {
        List<MemoryInstanceInfo> list = new ArrayList<>();
        list.add(memoryInstanceInfo);
        memoryInstanceDao.insertMemoryInstanceInfos(list);
    }

    /**
     * functional testing getMemoryInstanceInfos
     *
     * @tc.name: getMemoryInstanceInfos
     * @tc.number: OHOS_JAVA_Service_MemoryInstanceDao_getMemoryInstanceInfos_0001
     * @tc.desc: getMemoryInstanceInfos
     * @tc.type: functional testing
     * @tc.require: AR000FK61M
     */
    @Test
    public void testGetMemoryInstanceInfos() {
        List<MemoryInstanceInfo> list = new ArrayList<>();
        list = memoryInstanceDao.getMemoryInstanceInfos(1, 0L, 5L);
        Assert.assertEquals(1, list.size());
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
        ArrayList<MemoryInstanceInfo> allMemoryInstanceInfos = memoryInstanceDao.getAllMemoryInstanceInfos();
        Assert.assertNotNull(allMemoryInstanceInfos);
    }

    /**
     * functional testing updateInstanceInfos
     *
     * @tc.name: updateInstanceInfos
     * @tc.number: OHOS_JAVA_Service_MemoryInstanceDao_updateInstanceInfos_0001
     * @tc.desc: updateInstanceInfos
     * @tc.type: functional testing
     * @tc.require: AR000FK61M
     */
    @Test
    public void testUpdateInstanceInfos() {
        memoryInstanceDao.updateInstanceInfos(13L, 2);
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