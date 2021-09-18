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

import ohos.devtools.datasources.databases.datatable.MemoryTable;
import ohos.devtools.datasources.databases.datatable.enties.ProcessMemInfo;
import ohos.devtools.services.memory.memorydao.MemoryDao;
import ohos.devtools.views.charts.model.ChartDataModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import ohos.devtools.datasources.transport.grpc.service.MemoryPluginResult;

/**
 * MemoryDaoTest
 */
public class MemoryDaoTest {
    private MemoryTable memoryTable;
    private List<ProcessMemInfo> processMemInfoList;
    private MemoryPluginResult.AppSummary appSummary;

    /**
     * init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_Service_memory_MemoryDao_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-003
     */
    @Before
    public void init() {
        ProcessMemInfo processMemInfo = new ProcessMemInfo();
        processMemInfo.setSession(1L);
        processMemInfo.setTimeStamp(1L);
        processMemInfo.setSessionId(1);
        appSummary = MemoryPluginResult.AppSummary.newBuilder().setCode(1L).build();
        processMemInfo.setData(appSummary);
        processMemInfoList = new ArrayList<>();
        memoryTable = new MemoryTable();
        processMemInfoList.add(processMemInfo);
    }

    /**
     * get All Data Test
     *
     * @tc.name: getAllDataTest
     * @tc.number: OHOS_JAVA_Service_memory_MemoryDao_getAllDataTest_0001
     * @tc.desc: get All Data Test
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-003
     */
    @Test
    public void getAllDataTest01() {
        memoryTable.insertProcessMemInfo(processMemInfoList);
        List<ProcessMemInfo> list = MemoryDao.getInstance().getAllData(1L);
        int num = list.size();
        Assert.assertNotEquals(0, num);
    }

    /**
     * get All Data Test
     *
     * @tc.name: getAllDataTest
     * @tc.number: OHOS_JAVA_Service_memory_MemoryDao_getAllDataTest_0002
     * @tc.desc: get All Data Test
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-003
     */
    @Test
    public void getAllDataTest02() {
        ProcessMemInfo processMemInfo = new ProcessMemInfo();
        processMemInfo.setSession(10L);
        processMemInfo.setTimeStamp(1L);
        processMemInfo.setSessionId(1);
        processMemInfo.setData(appSummary);
        processMemInfoList.add(processMemInfo);
        memoryTable.insertProcessMemInfo(processMemInfoList);
        List<ProcessMemInfo> list = MemoryDao.getInstance().getAllData(10L);
        List<ProcessMemInfo> allData = MemoryDao.getInstance().getAllData(1L);
        Assert.assertNotEquals(list, allData);
    }

    /**
     * get All Data Test
     *
     * @tc.name: getAllDataTest
     * @tc.number: OHOS_JAVA_Service_memory_MemoryDao_getAllDataTest_0003
     * @tc.desc: get All Data Test
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-003
     */
    @Test
    public void getAllDataTest03() {
        ProcessMemInfo processMemInfo = new ProcessMemInfo();
        processMemInfo.setSession(Long.MAX_VALUE);
        processMemInfo.setTimeStamp(1L);
        processMemInfo.setSessionId(1);
        processMemInfo.setData(appSummary);
        processMemInfoList.add(processMemInfo);
        memoryTable.insertProcessMemInfo(processMemInfoList);
        List<ProcessMemInfo> list = MemoryDao.getInstance().getAllData(Long.MAX_VALUE);
        int num = list.size();
        Assert.assertNotEquals(0, num);
    }

    /**
     * get All Data Test
     *
     * @tc.name: getAllDataTest
     * @tc.number: OHOS_JAVA_Service_memory_MemoryDao_getAllDataTest_0004
     * @tc.desc: get All Data Test
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-003
     */
    @Test
    public void getAllDataTest04() {
        ProcessMemInfo processMemInfo = new ProcessMemInfo();
        processMemInfo.setSession(-1L);
        processMemInfo.setTimeStamp(1L);
        processMemInfo.setSessionId(1);
        processMemInfo.setData(appSummary);
        processMemInfoList.add(processMemInfo);
        memoryTable.insertProcessMemInfo(processMemInfoList);
        List<ProcessMemInfo> list = MemoryDao.getInstance().getAllData(-1L);
        int num = list.size();
        Assert.assertNotEquals(0, num);
    }

    /**
     * get All Data Test
     *
     * @tc.name: getAllDataTest
     * @tc.number: OHOS_JAVA_Service_memory_MemoryDao_getAllDataTest_0005
     * @tc.desc: get All Data Test
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-003
     */
    @Test
    public void getAllDataTest05() {
        ProcessMemInfo processMemInfo = new ProcessMemInfo();
        processMemInfo.setSession(0L);
        processMemInfo.setTimeStamp(1L);
        processMemInfo.setSessionId(1);
        processMemInfo.setData(appSummary);
        processMemInfoList.add(processMemInfo);
        memoryTable.insertProcessMemInfo(processMemInfoList);
        List<ProcessMemInfo> list = MemoryDao.getInstance().getAllData(0L);
        int num = list.size();
        Assert.assertNotEquals(0, num);
    }

    /**
     * get Data Test
     *
     * @tc.name: getDataTest
     * @tc.number: OHOS_JAVA_Service_memory_MemoryDao_getDataTest_0001
     * @tc.desc: get Data Test
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-003
     */
    @Test
    public void getDataTest01() {
        memoryTable.insertProcessMemInfo(processMemInfoList);
        LinkedHashMap<Integer, List<ChartDataModel>> memoryData =
            MemoryDao.getInstance().getData(1L, 0, 1000, 0L, false);
        int num = memoryData.size();
        Assert.assertNotEquals(0, num);
    }

    /**
     * get Data Test
     *
     * @tc.name: getDataTest
     * @tc.number: OHOS_JAVA_Service_memory_MemoryDao_getDataTest_0002
     * @tc.desc: get Data Test
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-003
     */
    @Test
    public void getDataTest02() {
        ProcessMemInfo processMemInfo = new ProcessMemInfo();
        processMemInfo.setSession(10L);
        processMemInfo.setTimeStamp(1L);
        processMemInfo.setSessionId(1);
        processMemInfo.setData(appSummary);
        processMemInfoList.add(processMemInfo);
        memoryTable.insertProcessMemInfo(processMemInfoList);
        LinkedHashMap<Integer, List<ChartDataModel>> memoryData1 =
            MemoryDao.getInstance().getData(1L, 0, 1000, 0L, false);
        LinkedHashMap<Integer, List<ChartDataModel>> memoryData2 =
            MemoryDao.getInstance().getData(10L, 0, 1000, 0L, false);
        Assert.assertNotEquals(memoryData1, memoryData2);
    }

    /**
     * get Data Test
     *
     * @tc.name: getDataTest
     * @tc.number: OHOS_JAVA_Service_memory_MemoryDao_getDataTest_0003
     * @tc.desc: get Data Test
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-003
     */
    @Test
    public void getDataTest03() {
        ProcessMemInfo processMemInfo = new ProcessMemInfo();
        processMemInfo.setSession(Long.MAX_VALUE);
        processMemInfo.setTimeStamp(1L);
        processMemInfo.setSessionId(1);
        processMemInfo.setData(appSummary);
        processMemInfoList.add(processMemInfo);
        memoryTable.insertProcessMemInfo(processMemInfoList);
        LinkedHashMap<Integer, List<ChartDataModel>> memoryData =
            MemoryDao.getInstance().getData(Long.MAX_VALUE, 0, 1000, 0L, true);
        int num = memoryData.size();
        Assert.assertNotEquals(0, num);
    }

    /**
     * get Data Test
     *
     * @tc.name: getDataTest
     * @tc.number: OHOS_JAVA_Service_memory_MemoryDao_getDataTest_0004
     * @tc.desc: get Data Test
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-003
     */
    @Test
    public void getDataTest04() {
        memoryTable.insertProcessMemInfo(processMemInfoList);
        LinkedHashMap<Integer, List<ChartDataModel>> memoryData =
            MemoryDao.getInstance().getData(1L, 10, 1000, 0L, true);
        int num = memoryData.size();
        Assert.assertNotEquals(0, num);
    }

    /**
     * get Data Test
     *
     * @tc.name: getDataTest
     * @tc.number: OHOS_JAVA_Service_memory_MemoryDao_getDataTest_0005
     * @tc.desc: get Data Test
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-003
     */
    @Test
    public void getDataTest05() {
        ProcessMemInfo processMemInfo = new ProcessMemInfo();
        processMemInfo.setSession(0L);
        processMemInfo.setTimeStamp(1L);
        processMemInfo.setSessionId(1);
        processMemInfo.setData(appSummary);
        processMemInfoList.add(processMemInfo);
        memoryTable.insertProcessMemInfo(processMemInfoList);
        LinkedHashMap<Integer, List<ChartDataModel>> memoryData =
            MemoryDao.getInstance().getData(0L, 10, 1000, 0L, true);
        int num = memoryData.size();
        Assert.assertNotEquals(0, num);
    }
}
