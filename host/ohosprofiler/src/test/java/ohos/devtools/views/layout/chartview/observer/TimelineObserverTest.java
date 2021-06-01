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
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.event.IChartEventObserver;
import ohos.devtools.views.layout.swing.TaskScenePanelChart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * TimelineObserver test
 *
 * @since 2021/3/31 19:51
 */
public class TimelineObserverTest {
    private static final int TEST_START = 0;

    private static final int TEST_END = 9000;

    private static final int TEST_DISPLAY = 10000;

    private static final int TEST_MARK = 1000;

    private TimelineObserver observer;

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TimelineObserver_init_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Before
    public void init() {
        ProfilerChartsView view = new ProfilerChartsView(LayoutConstants.NUM_L, true, new TaskScenePanelChart());
        List<IChartEventObserver> observers = view.getObserver().getListeners();
        for (IChartEventObserver event : observers) {
            if (event instanceof TimelineObserver) {
                observer = (TimelineObserver) event;
                break;
            }
        }
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TimelineObserver_refreshStandard_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void refreshStandardTest() {
        ChartStandard newStandard = new ChartStandard(0L);
        newStandard.setMaxDisplayMillis(TEST_DISPLAY);
        newStandard.setMinMarkInterval(TEST_MARK);
        observer.refreshStandard(newStandard, TEST_START, TEST_END, TEST_DISPLAY);
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_TimelineObserver_refreshView_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void refreshViewTest() {
        ChartDataRange range = new ChartDataRange();
        range.setStartTime(TEST_START);
        range.setEndTime(TEST_END);
        observer.refreshView(range, 0L, false);
        Assert.assertTrue(true);
    }
}
