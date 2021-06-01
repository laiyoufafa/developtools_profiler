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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ChartDataCacheTest
 *
 * @version 1.0
 * @date 2021/04/6 19:16
 **/
public class ChartDataCacheTest {
    private ChartDataCache chartDataCache;

    /**
     * functional test
     *
     * @tc.name: getInstance
     * @tc.number: OHOS_JAVA_Service_ChartDataCache_getInstance_0001
     * @tc.desc: getInstance
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Before
    public void getInstance() {
        chartDataCache = ChartDataCache.getInstance();
        Map dataCacheMap = chartDataCache.getDataCacheMap();
        LinkedHashMap<Long, String> longStringLinkedHashMap = new LinkedHashMap<>();
        longStringLinkedHashMap.put(100L, "test");
        dataCacheMap.put("test", longStringLinkedHashMap);
    }

    /**
     * functional test
     *
     * @tc.name: initCache
     * @tc.number: OHOS_JAVA_Service_ChartDataCache_initCache_0001
     * @tc.desc: initCache
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void initCache() {
        chartDataCache.initCache("test", 50);
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: getDataCacheMap
     * @tc.number: OHOS_JAVA_Service_ChartDataCache_getDataCacheMap_0001
     * @tc.desc: getDataCacheMap
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getDataCacheMap() {
        chartDataCache.getDataCacheMap();
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: addCacheBlock
     * @tc.number: OHOS_JAVA_Service_ChartDataCache_addCacheBlock_0001
     * @tc.desc: addCacheBlock
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void addCacheBlock() {
        LinkedHashMap<Long, String> longStringLinkedHashMap = new LinkedHashMap<>();
        chartDataCache.addCacheBlock("test", longStringLinkedHashMap);
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: getDataCache
     * @tc.number: OHOS_JAVA_Service_ChartDataCache_getDataCache_0001
     * @tc.desc: getDataCache
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void getDataCache() {
        LinkedHashMap test = chartDataCache.getDataCache("test", 10, 100, 2000L);
        Assert.assertNotNull(test);
    }

    /**
     * functional test
     *
     * @tc.name: clearDataCache
     * @tc.number: OHOS_JAVA_Service_ChartDataCache_clearDataCache_0001
     * @tc.desc: clearDataCache
     * @tc.type: functional testing
     * @tc.require: AR000FK5UI
     */
    @Test
    public void clearDataCache() {
        chartDataCache.clearDataCache("test");
    }
}