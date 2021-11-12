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

package ohos.devtools.services.ability;

import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.layout.chartview.ability.AbilityCardInfo;
import ohos.devtools.views.layout.chartview.ability.AbilityCardStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * AbilityDataCache
 *
 * @since: 2021/8/25
 */
public class AbilityDataCache {
    private static final Logger LOGGER = LogManager.getLogger(AbilityDataCache.class);
    private static AbilityDataCache instance;

    /**
     * Map of Ability activity data saved
     *
     * @see "Map<SessionId, List<Data>>>"
     */
    private final Map<Long, CopyOnWriteArrayList<AbilityActivityInfo>> activityDataCacheMap = new ConcurrentHashMap<>();

    /**
     * Map of Ability event data saved
     *
     * @see "Map<SessionId, List<Data>>>"
     */
    private final Map<Long, CopyOnWriteArrayList<AbilityEventInfo>> eventDataCacheMap = new ConcurrentHashMap<>();

    private AbilityDataCache() {
    }

    /**
     * Instance getter
     *
     * @return AbilityDataCache
     */
    public static AbilityDataCache getInstance() {
        if (instance == null) {
            synchronized (AbilityDataCache.class) {
                if (instance == null) {
                    instance = new AbilityDataCache();
                }
            }
        }
        return instance;
    }

    /**
     * Add Ability data to cache map
     *
     * @param sessionId Session id
     * @param dataModels Data model list
     */
    public void addActivityDataModel(long sessionId, AbilityActivityInfo dataModels) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addDataModel");
        }
        CopyOnWriteArrayList<AbilityActivityInfo> cache = activityDataCacheMap.get(sessionId);
        // If cache map is null, generate the new map and save the current timestamp as first timestamp
        if (Objects.deepEquals(null, cache)) {
            cache = new CopyOnWriteArrayList<>();
        }
        cache.add(dataModels);
        activityDataCacheMap.put(sessionId, cache);
    }

    /**
     * Add Ability data to cache map
     *
     * @param sessionId Session id
     * @param dataModels Data model list
     */
    public void addEventDataModel(long sessionId, AbilityEventInfo dataModels) {
        CopyOnWriteArrayList<AbilityEventInfo> cache = eventDataCacheMap.get(sessionId);
        // If cache map is null, generate the new map and save the current timestamp as first timestamp
        if (Objects.deepEquals(null, cache)) {
            cache = new CopyOnWriteArrayList<>();
        }
        cache.add(dataModels);
        eventDataCacheMap.put(sessionId, cache);
    }

    /**
     * Get Ability Card data
     *
     * @param sessionId Session id
     * @param startTime startTime
     * @param endTime endTime
     * @param firstTimestamp firstTimestamp
     * @return Data map
     */
    public List<AbilityCardInfo> getActivityCardData(long sessionId, long firstTimestamp) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getActivityData");
        }
        CopyOnWriteArrayList<AbilityCardInfo> result = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<AbilityActivityInfo> cache = activityDataCacheMap.get(sessionId);
        if (cache == null) {
            return result;
        }
        for (int index = 0; index < cache.size(); index++) {
            AbilityActivityInfo beforeAbilityData = cache.get(index);
            String beforeStateName = beforeAbilityData.getAbilityStateName();
            int beforeState = beforeAbilityData.getAbilityState();
            long beforeTime = beforeAbilityData.getTimeStamp() - firstTimestamp;
            boolean isStart = beforeState != 6 && beforeState != 4;
            if (cache.size() > (index + 1)) {
                AbilityActivityInfo afterAbilityData = cache.get(index + 1);
                int afterState = afterAbilityData.getAbilityState();
                long afterTime = afterAbilityData.getTimeStamp() - firstTimestamp;
                boolean isEnd = afterState == 6 || afterState == 4;
                if (isStart && isEnd) {
                    AbilityCardInfo abilityCardInfo = new AbilityCardInfo();
                    abilityCardInfo.setSessionId(sessionId);
                    abilityCardInfo.setStartTime(beforeTime);
                    abilityCardInfo.setEndTime(afterTime);
                    abilityCardInfo.setAbilityCardStatus(AbilityCardStatus.INITIAL);
                    abilityCardInfo.setApplicationName(beforeStateName);
                    result.add(abilityCardInfo);
                }
            } else {
                if (isStart) {
                    AbilityCardInfo abilityCardInfo = new AbilityCardInfo();
                    abilityCardInfo.setSessionId(sessionId);
                    abilityCardInfo.setStartTime(beforeTime);
                    abilityCardInfo.setAbilityCardStatus(AbilityCardStatus.ACTIVE);
                    abilityCardInfo.setApplicationName(beforeStateName);
                    result.add(abilityCardInfo);
                }
            }
        }
        return result;
    }

    /**
     * get Activity Data
     *
     * @param sessionId sessionId
     * @param startTime startTime
     * @param endTime endTime
     * @param firstTimestamp firstTimestamp
     * @return List <AbilityActivityInfo>
     */
    public List<AbilityActivityInfo> getActivityData(long sessionId, int startTime, int endTime, long firstTimestamp) {
        List<AbilityActivityInfo> result = new ArrayList<>();
        long timeBefore = -1;
        AbilityActivityInfo beforeActivityData = null;
        List<AbilityActivityInfo> cache = activityDataCacheMap.get(sessionId);
        if (cache == null) {
            return result;
        }
        if (cache.size() > 0) {
            for (AbilityActivityInfo activityData : cache) {
                long realTimeStamp = activityData.getTimeStamp() - firstTimestamp;
                // activityData.setTimeStamp(realTimeStamp);
                if (realTimeStamp < startTime) {
                    timeBefore = realTimeStamp;
                    beforeActivityData = activityData;
                    continue;
                }
                if (timeBefore != -1 && !result.contains(beforeActivityData)) {
                    result.add(beforeActivityData);
                }

                if (realTimeStamp <= endTime) {
                    // Data saved between startTime and endTime
                    result.add(activityData);
                } else {
                    // Save the next time and data of endTime, fill the chart blank and solve the boundary flicker
                    result.add(activityData);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * get Event Data
     *
     * @param sessionId sessionId
     * @param startTime startTime
     * @param endTime endTime
     * @param firstTimestamp firstTimestamp
     * @return List <AbilityEventInfo>
     */
    public List<AbilityEventInfo> getEventData(long sessionId, int startTime, int endTime, long firstTimestamp) {
        List<AbilityEventInfo> result = new ArrayList<>();
        long timeBefore = -1;
        AbilityEventInfo beforeActivityData = null;
        List<AbilityEventInfo> cache = eventDataCacheMap.get(sessionId);
        if (cache == null) {
            return result;
        }
        if (cache.size() > 0) {
            for (AbilityEventInfo activityData : cache) {
                long realTimeStamp = activityData.getTimeStamp() - firstTimestamp;
                if (realTimeStamp < startTime) {
                    timeBefore = realTimeStamp;
                    beforeActivityData = activityData;
                    continue;
                }
                if (timeBefore != -1 && !result.contains(beforeActivityData)) {
                    result.add(beforeActivityData);
                }

                if (realTimeStamp <= endTime) {
                    // Data saved between startTime and endTime
                    result.add(activityData);
                } else {
                    // Save the next time and data of endTime, fill the chart blank and solve the boundary flicker
                    result.add(activityData);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Clear cache by session id when the session was deleted
     *
     * @param sessionId Session id
     */
    public void clearCacheBySession(long sessionId) {
        activityDataCacheMap.remove(sessionId);
        eventDataCacheMap.remove(sessionId);
    }
}
