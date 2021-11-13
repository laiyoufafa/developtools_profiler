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

import ohos.devtools.services.ability.AbilityActivityInfo;
import ohos.devtools.services.ability.AbilityDao;
import ohos.devtools.services.ability.AbilityDataCache;
import ohos.devtools.services.ability.AbilityEventInfo;
import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.layout.chartview.ability.AbilityCardInfo;
import ohos.devtools.views.layout.chartview.ability.ProfilerAppAbility;
import ohos.devtools.views.layout.chartview.event.IChartEventObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * AbilityObserver
 *
 * @since 2021/10/25
 */
public class AbilityObserver implements IChartEventObserver {
    private static final Logger LOGGER = LogManager.getLogger(AbilityObserver.class);
    private ProfilerAppAbility profilerAppAbility;
    private final long sessionId;

    /**
     * Sign of pause
     */
    private final boolean pause;

    /**
     * Sign of stop
     */
    private final boolean stop;

    /**
     * Ability Observer
     *
     * @param profilerAppAbility profilerAppAbility
     * @param sessionId sessionId
     * @param pause pause
     * @param stop stop
     */
    public AbilityObserver(ProfilerAppAbility profilerAppAbility, long sessionId, boolean pause, boolean stop) {
        this.profilerAppAbility = profilerAppAbility;
        this.sessionId = sessionId;
        this.pause = pause;
        this.stop = stop;
    }

    /**
     * refresh Standard
     *
     * @param startTime Start time of chart
     * @param endTime End time of chart
     * @param maxDisplayMillis Maximum display time on view
     * @param minMarkInterval The minimum scale interval
     */
    @Override
    public void refreshStandard(int startTime, int endTime, int maxDisplayMillis, int minMarkInterval) {
        profilerAppAbility.setMaxDisplayTime(maxDisplayMillis);
        profilerAppAbility.setMinMarkInterval(minMarkInterval);
        profilerAppAbility.setStartTime(startTime);
        profilerAppAbility.setEndTime(endTime);
        profilerAppAbility.repaint();
        profilerAppAbility.revalidate();
    }

    /**
     * refresh View
     *
     * @param range Chart display time range
     * @param firstTimestamp The first time stamp of this chart's data
     * @param useCache whether or not use cache
     */
    @Override
    public void refreshView(ChartDataRange range, long firstTimestamp, boolean useCache) {
        if (profilerAppAbility == null) {
            return;
        }
        int startTime = range.getStartTime();
        int endTime = range.getEndTime();
        List<AbilityActivityInfo> queryActivityResult;
        List<AbilityEventInfo> queryEventResult;
        profilerAppAbility.refreshAbilityTime(startTime, endTime, firstTimestamp);
        if (pause || stop || !useCache) {
            queryActivityResult = AbilityDao.getInstance().getAllData(sessionId);
            queryEventResult = AbilityDao.getInstance().getEventData(sessionId, startTime, endTime, firstTimestamp);
        } else {
            // Data acquisition of application life cycle
            queryActivityResult =
                AbilityDataCache.getInstance().getActivityData(sessionId, startTime, endTime, firstTimestamp);
            // Application event data acquisition
            queryEventResult =
                AbilityDataCache.getInstance().getEventData(sessionId, startTime, endTime, firstTimestamp);
        }
        // Generate data types from the beginning to the end of the life cycle
        List<AbilityCardInfo> activityCardData =
            AbilityDataCache.getInstance().getActivityCardData(sessionId, firstTimestamp);
        // refresh ability card data Used for tooltip display
        if (activityCardData != null && activityCardData.size() > 0) {
            profilerAppAbility.refreshActivityToolTip(activityCardData);
        }
        // refresh ability activity data
        if (queryActivityResult != null && queryActivityResult.size() > 0) {
            profilerAppAbility.refreshActivityAbility(queryActivityResult);
        }
        // refresh ability event data
        if (queryEventResult != null && queryEventResult.size() > 0) {
            profilerAppAbility.refreshEventAbility(queryEventResult);
        }
        profilerAppAbility.repaint();
        profilerAppAbility.revalidate();
    }
}
