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

package ohos.devtools.views.layout.chartview.observer;

import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartStandard;
import org.junit.Before;
import org.junit.Test;

/**
 * @Description CacheObserver test
 * @Date 2021/4/2 11:25
 **/
public class CacheObserverTest {
    /**
     * Cache observer
     */
    public CacheObserver cacheObserver;

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_CacheObserver_init_0001
     * @tc.desc: chart CacheObserver test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Before
    public void init() {
        cacheObserver = new CacheObserver(37L);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_CacheObserver_refreshStandard_0001
     * @tc.desc: chart CacheObserver test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void refreshStandard() {
        cacheObserver.refreshStandard(new ChartStandard(37L), 10, 50, 30);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_CacheObserver_refreshView_0001
     * @tc.desc: chart CacheObserver test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void refreshView01() {
        cacheObserver.refreshView(new ChartDataRange(), 21L, true);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_CacheObserver_refreshView_0002
     * @tc.desc: chart CacheObserver test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void refreshView02() {
        ChartDataRange chartDataRange = new ChartDataRange();
        chartDataRange.setEndTime(-2);
        cacheObserver.refreshView(chartDataRange, 21L, false);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_CacheObserver_refreshView_0003
     * @tc.desc: chart CacheObserver test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void refreshView03() {
        ChartDataRange chartDataRange = new ChartDataRange();
        chartDataRange.setEndTime(510);
        cacheObserver.refreshView(chartDataRange, 21L, true);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_CacheObserver_refreshView_0004
     * @tc.desc: chart CacheObserver test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void refreshView04() {
        ChartDataRange chartDataRange = new ChartDataRange();
        chartDataRange.setEndTime(510);
        cacheObserver.refreshView(chartDataRange, 21L, false);
    }
}