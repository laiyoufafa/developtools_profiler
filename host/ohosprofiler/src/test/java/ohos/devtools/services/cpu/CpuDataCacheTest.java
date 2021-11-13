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

import ohos.devtools.views.charts.model.ChartDataModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Cpu Data Cache Test
 */
public class CpuDataCacheTest {
    private static final int TEST_START = 0;
    private static final int TEST_END = 1000;
    private static final String STR = "TEST";
    private List<ChartDataModel> chartDataModels = new ArrayList<>();
    private ChartDataModel chartDataModel;
    private CpuDataCache cache;

    /**
     * init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_Service_cpu_CpuDataCache_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Before
    public void init() {
        cache = CpuDataCache.getInstance();
        chartDataModel = new ChartDataModel();
        chartDataModel.setValue(1);
        chartDataModel.setName(STR);
        chartDataModel.setIndex(0);
        chartDataModels.add(chartDataModel);
    }

    /**
     * get Instance Test
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_Service_cpu_CpuDataCache_getInstanceTest_0001
     * @tc.desc: get Instance Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getInstanceTest01() {
        CpuDataCache cpuDataCache = CpuDataCache.getInstance();
        Assert.assertNotNull(cpuDataCache);
    }

    /**
     * get Instance Test
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_Service_cpu_CpuDataCache_getInstanceTest_0002
     * @tc.desc: get Instance Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getInstanceTest02() {
        CpuDataCache cpuDataCache = CpuDataCache.getInstance();
        CpuDataCache dataCache = CpuDataCache.getInstance();
        Assert.assertEquals(cpuDataCache, dataCache);
    }

    /**
     * add Cpu Data Model Test
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_Service_cpu_CpuDataCache_addCpuDataModelTest_0001
     * @tc.desc: add Cpu Data Model Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void addCpuDataModelTest() {
        cache.addCpuDataModel(1L, 0L, chartDataModels);
        Assert.assertTrue(true);
    }

    /**
     * add Thread Data Model Test
     *
     * @tc.name: addThreadDataModelTest
     * @tc.number: OHOS_JAVA_Service_cpu_CpuDataCache_addThreadDataModelTest_0001
     * @tc.desc: add Thread Data Model Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void addThreadDataModelTest() {
        cache.addThreadDataModel(1L, 0L, chartDataModels);
        Assert.assertTrue(true);
    }

    /**
     * get Cpu Data Test
     *
     * @tc.name: getCpuDataTest
     * @tc.number: OHOS_JAVA_Service_cpu_CpuDataCache_getCpuDataTest_0001
     * @tc.desc: get Cpu Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getCpuDataTest01() {
        LinkedHashMap<Integer, List<ChartDataModel>> cpuData = cache.getCpuData(1L, TEST_START, TEST_END);
        Assert.assertNotNull(cpuData);
    }

    /**
     * get Cpu Data Test
     *
     * @tc.name: getCpuDataTest
     * @tc.number: OHOS_JAVA_Service_cpu_CpuDataCache_getCpuDataTest_0002
     * @tc.desc: get Cpu Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getCpuDataTest02() {
        LinkedHashMap<Integer, List<ChartDataModel>> cpuData = cache.getCpuData(1L, TEST_START, TEST_END);
        LinkedHashMap<Integer, List<ChartDataModel>> map = cache.getCpuData(1L, TEST_START, TEST_END);
        Assert.assertEquals(cpuData, map);
    }

    /**
     * get Thread Data Test
     *
     * @tc.name: getThreadDataTest
     * @tc.number: OHOS_JAVA_Service_cpu_CpuDataCache_getThreadDataTest_0001
     * @tc.desc: get Thread Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getThreadDataTest01() {
        LinkedHashMap<Integer, List<ChartDataModel>> threadData = cache.getThreadData(1L, TEST_START, TEST_END);
        Assert.assertNotNull(threadData);
    }

    /**
     * get Thread Data Test
     *
     * @tc.name: getThreadDataTest
     * @tc.number: OHOS_JAVA_Service_cpu_CpuDataCache_getThreadDataTest_0002
     * @tc.desc: get Thread Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getThreadDataTest02() {
        LinkedHashMap<Integer, List<ChartDataModel>> threadData = cache.getThreadData(1L, TEST_START, TEST_END);
        LinkedHashMap<Integer, List<ChartDataModel>> map = cache.getThreadData(1L, TEST_START, TEST_END);
        Assert.assertEquals(threadData, map);
    }

    /**
     * clear Cache By Session Test
     *
     * @tc.name: clearCacheBySessionTest
     * @tc.number: OHOS_JAVA_Service_cpu_CpuDataCache_clearCacheBySessionTest_0001
     * @tc.desc: clear Cache By Session Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void clearCacheBySessionTest() {
        cache.clearCacheBySession(1L);
        Assert.assertTrue(true);
    }
}