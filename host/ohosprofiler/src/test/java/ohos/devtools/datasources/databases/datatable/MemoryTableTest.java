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

package ohos.devtools.datasources.databases.datatable;

import ohos.devtools.datasources.databases.datatable.enties.ProcessMemInfo;
import ohos.devtools.datasources.transport.grpc.service.MemoryPluginResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Memory Table Test
 */
public class MemoryTableTest {
    private MemoryTable memoryTable;
    private List<ProcessMemInfo> list;

    /**
     * functional testing
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_views_devtools_init_0001
     * @tc.desc: init
     * @tc.type: profiler scrollbar test
     * @tc.require: SR-002-AR-001
     */
    @Before
    public void init() {
        memoryTable = new MemoryTable();
        ProcessMemInfo processMemInfo = new ProcessMemInfo();
        processMemInfo.setSession(1L);
        processMemInfo.setSessionId(1);
        processMemInfo.setTimeStamp(1L);
        MemoryPluginResult.AppSummary appSummary = MemoryPluginResult.AppSummary.newBuilder().setCode(1L).build();
        processMemInfo.setData(appSummary);
        list = new ArrayList<>();
        list.add(processMemInfo);
    }

    /**
     * functional testing
     *
     * @tc.name: memoryTableTest
     * @tc.number: OHOS_JAVA_views_devtools_memoryTableTest_0001
     * @tc.desc: memory Table Test
     * @tc.type: profiler scrollbar test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void memoryTableTest() {
        MemoryTable table = new MemoryTable();
        Assert.assertNotNull(table);
    }

    /**
     * functional testing
     *
     * @tc.name: insertProcessMemInfoTest
     * @tc.number: OHOS_JAVA_views_devtools_insertProcessMemInfoTest_0001
     * @tc.desc: insert ProcessMem Info Test
     * @tc.type: profiler scrollbar test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void insertProcessMemInfoTest() {
        boolean res = memoryTable.insertProcessMemInfo(list);
        Assert.assertTrue(res);
    }

    /**
     * functional testing
     *
     * @tc.name: insertProcessMemInfoTest
     * @tc.number: OHOS_JAVA_views_devtools_insertProcessMemInfoTest_0002
     * @tc.desc: insert ProcessMem Info Test
     * @tc.type: profiler scrollbar test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void insertProcessMemInfoTest02() {
        boolean res = memoryTable.insertProcessMemInfo(null);
        Assert.assertFalse(res);
    }

    /**
     * functional testing
     *
     * @tc.name: insertProcessMemInfoTest
     * @tc.number: OHOS_JAVA_views_devtools_insertProcessMemInfoTest_0003
     * @tc.desc: insert ProcessMem Info Test
     * @tc.type: profiler scrollbar test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void insertProcessMemInfoTest03() {
        boolean res1 = memoryTable.insertProcessMemInfo(list);
        boolean res2 = memoryTable.insertProcessMemInfo(null);
        Assert.assertNotEquals(res1, res2);
    }

    /**
     * functional testing
     *
     * @tc.name: insertProcessMemInfoTest
     * @tc.number: OHOS_JAVA_views_devtools_insertProcessMemInfoTest_0004
     * @tc.desc: insert ProcessMem Info Test
     * @tc.type: profiler scrollbar test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void insertProcessMemInfoTest04() {
        ProcessMemInfo processMemInfo = new ProcessMemInfo();
        processMemInfo.setSession(1L);
        processMemInfo.setSessionId(1);
        processMemInfo.setTimeStamp(1L);
        processMemInfo.setData(null);
        list.add(processMemInfo);
        boolean res = memoryTable.insertProcessMemInfo(list);
        Assert.assertTrue(res);
    }

    /**
     * functional testing
     *
     * @tc.name: insertProcessMemInfoTest
     * @tc.number: OHOS_JAVA_views_devtools_insertProcessMemInfoTest_0005
     * @tc.desc: insert ProcessMem Info Test
     * @tc.type: profiler scrollbar test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void insertProcessMemInfoTest05() {
        for (int i = 0; i < 1000; i++) {
            ProcessMemInfo processMemInfo = new ProcessMemInfo();
            processMemInfo.setSession(1L);
            processMemInfo.setSessionId(10);
            processMemInfo.setTimeStamp(1L);
            MemoryPluginResult.AppSummary appSummary = MemoryPluginResult.AppSummary.newBuilder().setCode(1L).build();
            processMemInfo.setData(appSummary);
            list.add(processMemInfo);
        }
        boolean res = memoryTable.insertProcessMemInfo(list);
        Assert.assertTrue(res);
    }

}
