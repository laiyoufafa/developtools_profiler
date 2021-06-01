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
import org.apache.logging.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * MemoryInstanceDetailsDaoTest
 *
 * @version 1.0
 * @date 2021/04/26 15:56
 **/
public class MemoryInstanceDetailsDaoTest {
    /**
     * MemoryInstanceDetailsInfo
     */
    private MemoryInstanceDetailsInfo memoryInstanceDetailsInfo;

    /**
     * MemoryInstanceDetailsDao
     */
    private MemoryInstanceDetailsDao memoryInstanceDetailsDao;

    /**
     * functional testing init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_memory_MemoryInstanceDetailsDao_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Before
    public void init() {
        // 应用初始化 Step1 初始化数据中心
        ProfilerLogManager.getSingleton().updateLogLevel(Level.ERROR);
        DataBaseApi apo = DataBaseApi.getInstance();
        apo.initDataSourceManager();
        memoryInstanceDetailsDao = MemoryInstanceDetailsDao.getInstance();
        memoryInstanceDetailsInfo = new MemoryInstanceDetailsInfo();
        memoryInstanceDetailsInfo.setInstanceId(1);
        memoryInstanceDetailsInfo.setClassName("Class");
        memoryInstanceDetailsInfo.setFieldName("field");
        memoryInstanceDetailsInfo.setMethodName("method");
        memoryInstanceDetailsInfo.setFrameId(1);
        memoryInstanceDetailsInfo.setLineNumber(1);
        memoryInstanceDetailsInfo.setId(1);
    }

    /**
     * functional testing getInstance
     *
     * @tc.name: getInstance
     * @tc.number: OHOS_JAVA_memory_MemoryInstanceDetailsDao_getInstance_0001
     * @tc.desc: getInstance
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testGetInstance() {
        MemoryInstanceDetailsDao instance = MemoryInstanceDetailsDao.getInstance();
        Assert.assertNotNull(instance);
    }

    /**
     * functional testing insertMemoryInstanceDetailsInfo
     *
     * @tc.name: insertMemoryInstanceDetailsInfo
     * @tc.number: OHOS_JAVA_memory_MemoryInstanceDetailsDao_insertMemoryInstanceDetailsInfo_0001
     * @tc.desc: insertMemoryInstanceDetailsInfo
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testinsertMemoryInstanceDetailsInfo() {
        memoryInstanceDetailsDao.insertMemoryInstanceDetailsInfo(memoryInstanceDetailsInfo);
    }

    /**
     * functional testing insertMemoryInstanceDetailsInfo
     *
     * @tc.name: insertMemoryInstanceDetailsInfo
     * @tc.number: OHOS_JAVA_memory_MemoryInstanceDetailsDao_insertMemoryInstanceDetailsInfo_0001
     * @tc.desc: insertMemoryInstanceDetailsInfo
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testinsertMemoryInstanceDetailsInfos() {
        List<MemoryInstanceDetailsInfo> list = new ArrayList<>();
        list.add(memoryInstanceDetailsInfo);
        memoryInstanceDetailsDao.insertMemoryInstanceDetailsInfo(list);
    }

    /**
     * functional testing getMemoryInstanceDetails
     *
     * @tc.name: getMemoryInstanceDetails
     * @tc.number: OHOS_JAVA_memory_MemoryInstanceDetailsDao_getMemoryInstanceDetails_0001
     * @tc.desc: getMemoryInstanceDetails
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgetMemoryInstanceDetails() {
        List<MemoryInstanceDetailsInfo> list = memoryInstanceDetailsDao.getMemoryInstanceDetails(1);
        Assert.assertNotNull(list);
    }

    /**
     * functional testing getAllMemoryInstanceDetails
     *
     * @tc.name: getAllMemoryInstanceDetails
     * @tc.number: OHOS_JAVA_memory_MemoryInstanceDetailsDao_getAllMemoryInstanceDetails_0001
     * @tc.desc: getAllMemoryInstanceDetails
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgetAllMemoryInstanceDetails() {
        List<MemoryInstanceDetailsInfo> list = memoryInstanceDetailsDao.getAllMemoryInstanceDetails();
        Assert.assertNotNull(list);
    }

    /**
     * functional testing deleteSessionData
     *
     * @tc.name: deleteSessionData
     * @tc.number: OHOS_JAVA_memory_MemoryInstanceDetailsDao_deleteSessionData_0001
     * @tc.desc: deleteSessionData
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testdeleteSessionData() {
        memoryInstanceDetailsDao.deleteSessionData(1L);
    }

}
