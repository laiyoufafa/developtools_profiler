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

package ohos.devtools.services.cpu;

import ohos.devtools.datasources.databases.datatable.enties.ProcessCpuData;
import ohos.devtools.views.charts.model.ChartDataModel;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * CpuDao Test
 */
public class CpuDaoTest {
    private static final int TEST_START = 0;
    private static final int TEST_END = 1000;

    /**
     * getInstance Test
     *
     * @tc.name: getInstanceTest
     * @tc.number: OHOS_JAVA_Service_cpu_CpuDao_getInstanceTest_0001
     * @tc.desc: getInstance Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getInstanceTest01() {
        CpuDao cpuDao = CpuDao.getInstance();
        Assert.assertNotNull(cpuDao);
    }

    /**
     * getInstance Test
     *
     * @tc.name: getInstanceTest
     * @tc.number: OHOS_JAVA_Service_cpu_CpuDao_getInstanceTest_0002
     * @tc.desc: getInstance Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getInstanceTest02() {
        CpuDao cpuDao = CpuDao.getInstance();
        CpuDao dao = CpuDao.getInstance();
        Assert.assertEquals(cpuDao, dao);
    }

    /**
     * get All Data Test
     *
     * @tc.name: getAllDataTest
     * @tc.number: OHOS_JAVA_Service_cpu_CpuDao_getAllDataTest_0001
     * @tc.desc: get All Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getAllDataTest01() {
        List<ProcessCpuData> allData = CpuDao.getInstance().getAllData(1L);
        Assert.assertNotNull(allData);
    }

    /**
     * get All Data Test
     *
     * @tc.name: getAllDataTest
     * @tc.number: OHOS_JAVA_Service_cpu_CpuDao_getAllDataTest_0002
     * @tc.desc: get All Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getAllDataTest02() {
        List<ProcessCpuData> allData = CpuDao.getInstance().getAllData(1L);
        List<ProcessCpuData> list = CpuDao.getInstance().getAllData(1L);
        Assert.assertEquals(allData, list);
    }

    /**
     * get Cpu Data Test
     *
     * @tc.name: getCpuDataTest
     * @tc.number: OHOS_JAVA_Service_cpu_CpuDao_getCpuDataTest_0001
     * @tc.desc: get Cpu Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getCpuDataTest01() {
        LinkedHashMap<Integer, List<ChartDataModel>> cpuData =
            CpuDao.getInstance().getCpuData(1L, TEST_START, TEST_END, 0L, true);
        Assert.assertNotNull(cpuData);
    }

    /**
     * get Cpu Data Test
     *
     * @tc.name: getCpuDataTest
     * @tc.number: OHOS_JAVA_Service_cpu_CpuDao_getCpuDataTest_0002
     * @tc.desc: get Cpu Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getCpuDataTest02() {
        LinkedHashMap<Integer, List<ChartDataModel>> cpuData =
            CpuDao.getInstance().getCpuData(1L, TEST_START, TEST_END, 0L, true);
        LinkedHashMap<Integer, List<ChartDataModel>> map =
            CpuDao.getInstance().getCpuData(1L, TEST_START, TEST_END, 0L, true);
        Assert.assertEquals(cpuData, map);
    }

    /**
     * get Thread Data Test
     *
     * @tc.name: getThreadDataTest
     * @tc.number: OHOS_JAVA_Service_cpu_CpuDao_getThreadDataTest_0001
     * @tc.desc: get Thread Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getThreadDataTest01() {
        LinkedHashMap<Integer, List<ChartDataModel>> threadData =
            CpuDao.getInstance().getThreadData(1L, TEST_START, TEST_END, 0L, true);
        Assert.assertNotNull(threadData);
    }

    /**
     * get Thread Data Test
     *
     * @tc.name: getThreadDataTest
     * @tc.number: OHOS_JAVA_Service_cpu_CpuDao_getThreadDataTest_0002
     * @tc.desc: get Thread Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getThreadDataTest02() {
        LinkedHashMap<Integer, List<ChartDataModel>> threadData =
            CpuDao.getInstance().getThreadData(1L, TEST_START, TEST_END, 0L, true);
        LinkedHashMap<Integer, List<ChartDataModel>> map =
            CpuDao.getInstance().getThreadData(1L, TEST_START, TEST_END, 0L, true);
        Assert.assertEquals(threadData, map);
    }
}