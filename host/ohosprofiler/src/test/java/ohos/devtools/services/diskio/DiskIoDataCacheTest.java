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

package ohos.devtools.services.diskio;

import ohos.devtools.views.charts.model.ChartDataModel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * DiskIo Data Cache Test
 *
 * @since 2021/2/1 9:31
 */
public class DiskIoDataCacheTest {
    private DiskIoDataCache diskIoDataCache;
    private List<ChartDataModel> chartDataModels;

    /**
     * init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoDataCache_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Before
    public void init() {
        chartDataModels = new ArrayList<>();
        diskIoDataCache = DiskIoDataCache.getInstance();
        ChartDataModel chartDataModel = new ChartDataModel();
        chartDataModel.setValue(1);
        chartDataModel.setColor(Color.GREEN);
        chartDataModel.setIndex(1);
        chartDataModel.setName("Test");
        chartDataModels.add(chartDataModel);
    }

    /**
     * get Instance Test
     *
     * @tc.name: getInstanceTest
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoDataCache_getInstanceTest_0001
     * @tc.desc: get Instance Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getInstanceTest01() {
        DiskIoDataCache cache = DiskIoDataCache.getInstance();
        Assert.assertNotNull(cache);
    }

    /**
     * get Instance Test
     *
     * @tc.name: getInstanceTest
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoDataCache_getInstanceTest_0002
     * @tc.desc: get Instance Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getInstanceTest02() {
        DiskIoDataCache cache = DiskIoDataCache.getInstance();
        DiskIoDataCache dataCache = DiskIoDataCache.getInstance();
        Assert.assertEquals(cache, dataCache);
    }

    /**
     * add Data Model Test
     *
     * @tc.name: addDataModelTest
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoDataCache_addDataModelTest_0001
     * @tc.desc: add Data Model Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void addDataModelTest() {
        diskIoDataCache.addDataModel(1L, 1L, chartDataModels);
        Assert.assertTrue(true);
    }

    /**
     * get Data Test
     *
     * @tc.name: getDataTest
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoDataCache_getDataTest_0001
     * @tc.desc: get Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getDataTest01() {
        LinkedHashMap<Integer, List<ChartDataModel>> data = diskIoDataCache.getData(1L, 1, 2);
        Assert.assertNotNull(data);
    }

    /**
     * get Data Test
     *
     * @tc.name: getDataTest
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoDataCache_getDataTest_0002
     * @tc.desc: get Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getDataTest02() {
        LinkedHashMap<Integer, List<ChartDataModel>> data = diskIoDataCache.getData(1L, 1, 2);
        LinkedHashMap<Integer, List<ChartDataModel>> map = diskIoDataCache.getData(1L, 1, 2);
        Assert.assertEquals(data, map);
    }

    /**
     * clear Cache By Session Test
     *
     * @tc.name: clearCacheBySessionTest
     * @tc.number: OHOS_JAVA_Service_diskIo_DiskIoDataCache_clearCacheBySessionTest_0001
     * @tc.desc: get Data Test
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void clearCacheBySessionTest() {
        diskIoDataCache.clearCacheBySession(1L);
        Assert.assertTrue(true);
    }
}