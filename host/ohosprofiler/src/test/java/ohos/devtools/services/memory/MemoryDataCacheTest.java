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

import ohos.devtools.services.memory.memoryservice.MemoryDataCache;
import ohos.devtools.views.charts.model.ChartDataModel;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Memory Data Cache Test
 *
 * @since 2021/5/19 16:39
 */
public class MemoryDataCacheTest {
    private static final long SESSION_ID = 1L;
    private static final long TIMESTAMP = 0L;
    private static final int TIME = 0;

    /**
     * functional test
     *
     * @tc.name: getInstance
     * @tc.number: OHOS_JAVA_Service_MemoryDataCache_getInstance_0001
     * @tc.desc: getInstance
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getInstance01() {
        MemoryDataCache cache = MemoryDataCache.getInstance();
        Assert.assertNotNull(cache);
    }

    /**
     * functional test
     *
     * @tc.name: getInstance
     * @tc.number: OHOS_JAVA_Service_MemoryDataCache_getInstance_0002
     * @tc.desc: getInstance
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getInstance02() {
        MemoryDataCache cache = MemoryDataCache.getInstance();
        MemoryDataCache instance = MemoryDataCache.getInstance();
        Assert.assertEquals(cache, instance);
    }

    /**
     * functional test
     *
     * @tc.name: initCache
     * @tc.number: OHOS_JAVA_Service_MemoryDataCache_addDataModel_0001
     * @tc.desc: addDataModel
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void addDataModel() {
        List<ChartDataModel> list = new ArrayList<>() {
            {
                ChartDataModel model1 = new ChartDataModel();
                model1.setIndex(1);
                model1.setValue(1);
                add(model1);
                ChartDataModel model2 = new ChartDataModel();
                model2.setIndex(2);
                model2.setValue(2);
                add(model2);
            }
        };
        MemoryDataCache.getInstance().addDataModel(SESSION_ID, TIMESTAMP, list);
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: initCache
     * @tc.number: OHOS_JAVA_Service_MemoryDataCache_getData_0001
     * @tc.desc: getData
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getData01() {
        List<ChartDataModel> added = new ArrayList<>() {
            {
                ChartDataModel model1 = new ChartDataModel();
                model1.setIndex(1);
                model1.setValue(1);
                add(model1);
                ChartDataModel model2 = new ChartDataModel();
                model2.setIndex(2);
                model2.setValue(2);
                add(model2);
            }
        };
        MemoryDataCache.getInstance().addDataModel(SESSION_ID, TIMESTAMP, added);
        Assert.assertNotNull(MemoryDataCache.getInstance().getData(SESSION_ID, 0, 1));
    }

    /**
     * functional test
     *
     * @tc.name: initCache
     * @tc.number: OHOS_JAVA_Service_MemoryDataCache_getData_0002
     * @tc.desc: getData
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getData02() {
        List<ChartDataModel> added = new ArrayList<>() {
            {
                ChartDataModel model1 = new ChartDataModel();
                model1.setIndex(1);
                model1.setValue(1);
                add(model1);
                ChartDataModel model2 = new ChartDataModel();
                model2.setIndex(2);
                model2.setValue(2);
                add(model2);
            }
        };
        MemoryDataCache.getInstance().addDataModel(SESSION_ID, TIMESTAMP, added);
        LinkedHashMap<Integer, List<ChartDataModel>> data = MemoryDataCache.getInstance().getData(SESSION_ID, 0, 1);
        LinkedHashMap<Integer, List<ChartDataModel>> map = MemoryDataCache.getInstance().getData(SESSION_ID, 0, 1);
        Assert.assertEquals(data, map);
    }

    /**
     * functional test
     *
     * @tc.name: initCache
     * @tc.number: OHOS_JAVA_Service_MemoryDataCache_clearCacheBySession_0001
     * @tc.desc: getData
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void clearCacheBySession() {
        List<ChartDataModel> added = new ArrayList<>() {
            {
                ChartDataModel model1 = new ChartDataModel();
                model1.setIndex(1);
                model1.setValue(1);
                add(model1);
                ChartDataModel model2 = new ChartDataModel();
                model2.setIndex(2);
                model2.setValue(2);
                add(model2);
            }
        };
        MemoryDataCache.getInstance().addDataModel(SESSION_ID, TIMESTAMP, added);
        MemoryDataCache.getInstance().clearCacheBySession(SESSION_ID);
        Assert.assertTrue(true);
    }
}
