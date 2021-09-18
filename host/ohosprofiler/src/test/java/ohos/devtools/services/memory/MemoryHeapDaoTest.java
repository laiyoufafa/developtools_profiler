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
import ohos.devtools.services.memory.agentbean.MemoryHeapInfo;
import ohos.devtools.services.memory.agentdao.MemoryHeapDao;
import org.apache.logging.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Memory Heap Dao Test
 */
public class MemoryHeapDaoTest {
    private MemoryHeapInfo memoryHeapInfo;
    private MemoryHeapDao memoryHeapDao;
    private MemoryHeapInfo memoryHeap;

    /**
     * functional testing init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_memory_MemoryHeapDao_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Before
    public void init() {
        ProfilerLogManager.getSingleton().updateLogLevel(Level.ERROR);
        DataBaseApi apo = DataBaseApi.getInstance();
        apo.initDataSourceManager();
        memoryHeapDao = MemoryHeapDao.getInstance();

        memoryHeapDao.createMemoryHeapInfo();
        memoryHeapInfo = new MemoryHeapInfo();
        memoryHeap = new MemoryHeapInfo();
        memoryHeapInfo.setcId(1);
        memoryHeapInfo.setHeapId(1);
        memoryHeapInfo.setSessionId(1L);
        memoryHeapInfo.setAllocations(10);
        memoryHeapInfo.setDeallocations(0);
        memoryHeapInfo.setTotalCount(22);
        memoryHeapInfo.setShallowSize(22L);
        memoryHeapInfo.setCreateTime(1L);
        memoryHeapInfo.setInstanceId(2);
        memoryHeap.setcId(2);
        memoryHeap.setHeapId(2);
        memoryHeap.setSessionId(1L);
        memoryHeap.setAllocations(10);
        memoryHeap.setDeallocations(0);
        memoryHeap.setTotalCount(11);
        memoryHeap.setShallowSize(11L);
        memoryHeap.setCreateTime(2L);
        memoryHeap.setInstanceId(2);
        List<MemoryHeapInfo> list = new ArrayList<>();
        list.add(memoryHeap);
        list.add(memoryHeapInfo);

        memoryHeapDao.insertMemoryHeapInfos(list);
    }

    /**
     * functional testing insertMemoryHeapInfos
     *
     * @tc.name: insertMemoryHeapInfos
     * @tc.number: OHOS_JAVA_memory_MemoryHeapDao_insertMemoryHeapInfos_0001
     * @tc.desc: insertMemoryHeapInfos
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testinsertMemoryHeapInfos() {
        List<MemoryHeapInfo> list = new ArrayList<>();
        list.add(memoryHeap);
        memoryHeapDao.insertMemoryHeapInfos(list);
    }

    /**
     * functional testing getAllMemoryHeapInfos
     *
     * @tc.name: getAllMemoryHeapInfos
     * @tc.number: OHOS_JAVA_memory_MemoryHeapDao_getAllMemoryHeapInfos_0001
     * @tc.desc: getAllMemoryHeapInfos
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgetAllMemoryHeapInfos01() {
        List<MemoryHeapInfo> list = memoryHeapDao.getAllMemoryHeapInfos(1L);
        Assert.assertNotNull(list);
    }

    /**
     * functional testing getAllMemoryHeapInfos
     *
     * @tc.name: getAllMemoryHeapInfos
     * @tc.number: OHOS_JAVA_memory_MemoryHeapDao_getAllMemoryHeapInfos_0002
     * @tc.desc: getAllMemoryHeapInfos
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgetAllMemoryHeapInfos02() {
        List<MemoryHeapInfo> list = memoryHeapDao.getAllMemoryHeapInfos(1L);
        ArrayList<MemoryHeapInfo> allMemoryHeapInfos = memoryHeapDao.getAllMemoryHeapInfos(1L);
        Assert.assertNotEquals(list, allMemoryHeapInfos);
    }

    /**
     * functional testing insertMemoryHeapInfos
     *
     * @tc.name: getMemoryHeapInfos
     * @tc.number: OHOS_JAVA_memory_MemoryHeapDao_getMemoryHeapInfos_0001
     * @tc.desc: getMemoryHeapInfos
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgetMemoryHeapInfos01() {
        List<AgentHeapBean> list = memoryHeapDao.getMemoryHeapInfos(1L, 0L, 4L);
        Assert.assertNotNull(list);
    }

    /**
     * functional testing insertMemoryHeapInfos
     *
     * @tc.name: getMemoryHeapInfos
     * @tc.number: OHOS_JAVA_memory_MemoryHeapDao_getMemoryHeapInfos_0001
     * @tc.desc: getMemoryHeapInfos
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgetMemoryHeapInfos02() {
        List<AgentHeapBean> list = memoryHeapDao.getMemoryHeapInfos(1L, 0L, 4L);
        List<AgentHeapBean> memoryHeapInfos = memoryHeapDao.getMemoryHeapInfos(1L, 0L, 4L);
        Assert.assertEquals(list, memoryHeapInfos);
    }

    /**
     * functional testing deleteSessionData
     *
     * @tc.name: deleteSessionData
     * @tc.number: OHOS_JAVA_memory_MemoryHeapDao_deleteSessionData_0001
     * @tc.desc: deleteSessionData
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testdeleteSessionData() {
        memoryHeapDao.deleteSessionData(1L);
    }
}
