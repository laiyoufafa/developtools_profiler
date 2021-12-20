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

import com.intellij.icons.AllIcons;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartStandard;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.customcomp.CustomJButton;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.event.IChartEventObserver;
import ohos.devtools.views.layout.chartview.TaskScenePanelChart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static ohos.devtools.views.layout.chartview.utils.ChartViewConstants.CHART_START_DELAY;

/**
 * ProfilerChartsViewObserver test
 *
 * @since 2021/11/22
 */
public class ProfilerChartsViewPublisherTest {
    /**
     * log
     */
    private static final Logger LOGGER = LogManager.getLogger(ProfilerChartsViewPublisherTest.class);

    /**
     * Chart RUN_NAME
     */
    private static final String RUN_NAME = "ProfilerChartsViewMonitorTimer";

    /**
     * Chart RUN_NAME_SCROLLBAR
     */
    private static final String RUN_NAME_SCROLLBAR = "ScrollbarTimer";

    private static final String CHART_NAME = "Test";

    private static final long TEST_START_LONG = 1617188313000L;

    private static final long TEST_END_LONG = 1617188323111L;

    private static final int TEST_START = 0;

    private static final int TEST_END = 9000;

    private static final int TEST_DISPLAY = 10000;

    private static final int TEST_MARK = 1000;

    private static final int TEST_NEW_START = 2000;

    private static final int TEST_NEW_END = 12000;

    private ProfilerChartsViewPublisher observer;

    private TaskScenePanelChart taskScenePanelChart;

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_init_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Before
    public void init() {
        taskScenePanelChart = new TaskScenePanelChart();
        ProfilerChartsView view = new ProfilerChartsView(LayoutConstants.NUM_L, true, taskScenePanelChart);
        observer = view.getPublisher();
        boolean isScrollbarShow = false;
        ChartStandard standard = new ChartStandard(324L);
        standard.setFirstTimestamp(32478L);
        standard.setLastTimestamp(3274973L);
        standard.updateSelectedStart(CHART_NAME, 3);
        standard.updateSelectedEnd(CHART_NAME, 34453);
        QuartzManager.getInstance().addExecutor(RUN_NAME, () -> {
            // 保存LastTimestamp，为当前时间戳减去Chart启动延迟
            standard.setLastTimestamp(DateTimeUtil.getNowTimeLong() - CHART_START_DELAY);
            int end = (int) (standard.getLastTimestamp() - standard.getFirstTimestamp());
            int start = end > standard.getMaxDisplayMillis() ? end - standard.getMaxDisplayMillis() : 0;
        });
        QuartzManager.getInstance().addExecutor(RUN_NAME_SCROLLBAR, () -> {
            // 保存LastTimestamp，为当前时间戳减去Chart启动延迟
            standard.setLastTimestamp(DateTimeUtil.getNowTimeLong() - CHART_START_DELAY);
            int end = (int) (standard.getLastTimestamp() - standard.getFirstTimestamp());
            int start = end > standard.getMaxDisplayMillis() ? end - standard.getMaxDisplayMillis() : 0;
        });
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_showTraceResult_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void showTraceResultTest() {
        observer.showTraceResult(TEST_START_LONG, TEST_END_LONG);
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_startRefresh_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void startRefreshTest() {
        observer.startRefresh(TEST_START_LONG);
        destroy();
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_pauseRefresh_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void pauseRefreshTest() {
        observer.startRefresh(TEST_START_LONG);
        observer.pauseRefresh();
        destroy();
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_stopRefresh_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void stopRefreshTest() {
        observer.startRefresh(TEST_START_LONG);
        taskScenePanelChart.setjButtonStop(new CustomJButton(AllIcons.Process.ProgressResumeHover, "Start"));
        observer.stopRefresh(false);
        destroy();
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_restartRefresh_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void restartRefreshTest() {
        observer.startRefresh(TEST_START_LONG);
        observer.stopRefresh(false);
        observer.restartRefresh();
        destroy();
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_attach_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void attachTest() {
        IChartEventObserver ob = new IChartEventObserver() {
            @Override
            public void refreshStandard(int startTime, int endTime, int maxDisplayMillis, int minMarkInterval) {
            }

            @Override
            public void refreshView(ChartDataRange range, long firstTimestamp, boolean useCache) {
            }
        };
        observer.attach(ob);
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_detach_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void detachTest() {
        IChartEventObserver ob = new IChartEventObserver() {
            @Override
            public void refreshStandard(int startTime, int endTime, int maxDisplayMillis, int minMarkInterval) {
            }

            @Override
            public void refreshView(ChartDataRange range, long firstTimestamp, boolean useCache) {
            }
        };
        observer.attach(ob);
        observer.detach(ob);
        observer.detach(null);
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_notifyRefresh_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void notifyRefreshTest() {
        observer.notifyRefresh(TEST_START, TEST_END);
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_charZoom_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void charZoomTest() {
        observer.charZoom(TEST_START, TEST_END, TEST_DISPLAY);
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_msTimeZoom_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    @Test
    public void msTimeZoomTest() {
        observer.msTimeZoom(TEST_DISPLAY, TEST_MARK, TEST_NEW_START, TEST_NEW_END);
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerChartsView_destroy_0001
     * @tc.desc: chart Memory test
     * @tc.type: functional test
     * @tc.require: SR-001-AR-003
     */
    private void destroy() {
        QuartzManager.getInstance().deleteExecutor(RUN_NAME);
        QuartzManager.getInstance().deleteExecutor(RUN_NAME_SCROLLBAR);
    }
}
