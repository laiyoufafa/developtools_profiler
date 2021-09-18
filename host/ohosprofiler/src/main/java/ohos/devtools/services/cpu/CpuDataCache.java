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

import ohos.devtools.services.CacheMap;
import ohos.devtools.views.charts.model.ChartDataModel;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static ohos.devtools.views.common.LayoutConstants.INITIAL_VALUE;

/**
 * CpuDataCache
 */
public class CpuDataCache {
    /**
     * Map default load factor
     */
    private static final float LOAD_FACTOR = 0.75F;

    /**
     * Cache max size
     *
     * @see "Cache about 40 seconds of data"
     */
    private static final int CACHE_MAX_SIZE = 1500;

    /**
     * Singleton
     */
    private static CpuDataCache instance;

    /**
     * Map of memory data saved
     *
     * @see "Map<SessionId, Map<Time(Relative time), List<Data>>>"
     */
    private final Map<Long, CacheMap<Integer, List<ChartDataModel>>> cpuDataCacheMap = new ConcurrentHashMap<>();
    private final Map<Long, CacheMap<Integer, List<ChartDataModel>>> threadDataCacheMap = new ConcurrentHashMap<>();

    /**
     * Map of first data timestamp saved
     *
     * @see "Map<SessionId, Fisrt timestamp>"
     */
    private final Map<Long, Long> cpuFirstTsMap = new HashMap<>();
    private final Map<Long, Long> threadFirstTsMap = new HashMap<>();

    /**
     * Private constructor
     */
    private CpuDataCache() {
    }

    /**
     * Instance getter
     *
     * @return MemoryDataCache
     */
    public static CpuDataCache getInstance() {
        if (instance == null) {
            instance = new CpuDataCache();
        }
        return instance;
    }

    /**
     * Add Cpu data to cache map
     *
     * @param sessionId Session id
     * @param timestamp Timestamp of data
     * @param dataModels Data model list
     */
    public void addCpuDataModel(long sessionId, long timestamp, List<ChartDataModel> dataModels) {
        CacheMap<Integer, List<ChartDataModel>> cache = cpuDataCacheMap.get(sessionId);
        // If cache map is null, generate the new map and save the current timestamp as first timestamp
        if (cache == null) {
            cache = genNewSessionCache();
            cpuDataCacheMap.put(sessionId, cache);
            cpuFirstTsMap.put(sessionId, timestamp);
        }
        synchronized (cpuDataCacheMap.get(sessionId)) {
            // Save relative time
            int time = (int) (timestamp - cpuFirstTsMap.get(sessionId));
            cache.put(time, dataModels);
            // Here we need to sort the map, otherwise the key(timestamp) of the map will be out of order
            CacheMap<Integer, List<ChartDataModel>> sorted =
                cache.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors
                    .toMap(Map.Entry::getKey, Map.Entry::getValue, (paramX, paramY) -> paramX, CacheMap::new));
            cpuDataCacheMap.put(sessionId, sorted);
        }
    }

    /**
     * Add Cpu data to cache map
     *
     * @param sessionId Session id
     * @param timestamp Timestamp of data
     * @param dataModels Data model list
     */
    public void addThreadDataModel(long sessionId, long timestamp, List<ChartDataModel> dataModels) {
        CacheMap<Integer, List<ChartDataModel>> cache = threadDataCacheMap.get(sessionId);
        // If cache map is null, generate the new map and save the current timestamp as first timestamp
        if (cache == null) {
            cache = genNewSessionCache();
            threadDataCacheMap.put(sessionId, cache);
            threadFirstTsMap.put(sessionId, timestamp);
        }
        synchronized (threadDataCacheMap.get(sessionId)) {
            // Save relative time
            int time = (int) (timestamp - threadFirstTsMap.get(sessionId));
            cache.put(time, dataModels);
            // Here we need to sort the map, otherwise the key(timestamp) of the map will be out of order
            CacheMap<Integer, List<ChartDataModel>> sorted =
                cache.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(Collectors
                    .toMap(Map.Entry::getKey, Map.Entry::getValue, (paramX, paramY) -> paramX, CacheMap::new));
            threadDataCacheMap.put(sessionId, sorted);
        }
    }

    /**
     * Generate new session cache map
     *
     * @return CacheMap <Integer, List<ChartDataModel>>
     */
    private CacheMap<Integer, List<ChartDataModel>> genNewSessionCache() {
        return new CacheMap();
    }

    /**
     * Get cpu data
     *
     * @param sessionId Session id
     * @param startTime start time
     * @param endTime end time
     * @return LinkedHashMap <Integer, List<ChartDataModel>> Data map
     */
    public LinkedHashMap<Integer, List<ChartDataModel>> getCpuData(long sessionId, int startTime, int endTime) {
        LinkedHashMap<Integer, List<ChartDataModel>> result = new LinkedHashMap<>();
        int timeBeforeStart = INITIAL_VALUE;
        LinkedHashMap<Integer, List<ChartDataModel>> cache = cpuDataCacheMap.get(sessionId);
        if (cache == null) {
            return result;
        }
        synchronized (cpuDataCacheMap.get(sessionId)) {
            Set<Map.Entry<Integer, List<ChartDataModel>>> entries = cache.entrySet();
            for (Map.Entry<Integer, List<ChartDataModel>> entry : entries) {
                int time = entry.getKey();
                // Get the previous time of startTime
                if (time < startTime) {
                    timeBeforeStart = time;
                    continue;
                }
                // Save the previous time and data of startTime, fill the chart blank and solve the boundary flicker
                if (!result.containsKey(timeBeforeStart) && timeBeforeStart != INITIAL_VALUE) {
                    // Save time, do not save value. Otherwise, it will cause concurrent exception
                    result.put(timeBeforeStart, null);
                }
                if (time <= endTime) {
                    // Data saved between startTime and endTime
                    result.put(time, entry.getValue());
                } else {
                    // Save the next time and data of endTime, fill the chart blank and solve the boundary flicker
                    result.put(time, entry.getValue());
                    // Then break the loop
                    break;
                }
            }
        }
        // Save the value of timeBeforeStart now
        if (timeBeforeStart != INITIAL_VALUE) {
            result.put(timeBeforeStart, cache.get(timeBeforeStart));
        }
        return result;
    }

    /**
     * Get thread data
     *
     * @param sessionId Session id
     * @param startTime start time
     * @param endTime end time
     * @return LinkedHashMap <Integer, List<ChartDataModel>> Data map
     */
    public LinkedHashMap<Integer, List<ChartDataModel>> getThreadData(long sessionId, int startTime, int endTime) {
        LinkedHashMap<Integer, List<ChartDataModel>> result = new LinkedHashMap<>();
        int timeBeforeStart = INITIAL_VALUE;
        LinkedHashMap<Integer, List<ChartDataModel>> cache = threadDataCacheMap.get(sessionId);
        if (cache == null) {
            return result;
        }
        synchronized (threadDataCacheMap.get(sessionId)) {
            Set<Map.Entry<Integer, List<ChartDataModel>>> entries = cache.entrySet();
            for (Map.Entry<Integer, List<ChartDataModel>> entry : entries) {
                int time = entry.getKey();
                // Get the previous time of startTime
                if (time < startTime) {
                    timeBeforeStart = time;
                    continue;
                }
                // Save the previous time and data of startTime, fill the chart blank and solve the boundary flicker
                if (!result.containsKey(timeBeforeStart) && timeBeforeStart != INITIAL_VALUE) {
                    // Save time, do not save value. Otherwise, it will cause concurrent exception
                    result.put(timeBeforeStart, null);
                }
                if (time <= endTime) {
                    // Data saved between startTime and endTime
                    result.put(time, entry.getValue());
                } else {
                    // Save the next time and data of endTime, fill the chart blank and solve the boundary flicker
                    result.put(time, entry.getValue());
                    // Then break the loop
                    break;
                }
            }
        }
        // Save the value of timeBeforeStart now
        if (timeBeforeStart != INITIAL_VALUE) {
            result.put(timeBeforeStart, cache.get(timeBeforeStart));
        }
        return result;
    }

    /**
     * Clear cache by session id when the session was deleted
     *
     * @param sessionId Session id
     */
    public void clearCacheBySession(long sessionId) {
        cpuDataCacheMap.remove(sessionId);
        cpuFirstTsMap.remove(sessionId);
        threadDataCacheMap.remove(sessionId);
        threadFirstTsMap.remove(sessionId);
    }
}
