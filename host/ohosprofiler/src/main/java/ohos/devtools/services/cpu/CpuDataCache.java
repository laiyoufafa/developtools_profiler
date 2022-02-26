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

import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.services.LRUCache;
import ohos.devtools.views.charts.model.ChartDataModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CpuDataCache
 *
 * @since 2021/5/19 16:39
 */
public class CpuDataCache {
    private static final Logger LOGGER = LogManager.getLogger(CpuDataCache.class);
    private static final float LOAD_FACTOR = 0.75F;
    private static final int CACHE_MAX_SIZE = 1500;
    private static CpuDataCache instance = new CpuDataCache();

    private final Map<Long, LRUCache<List<ChartDataModel>>> cpuDataCacheMap = new ConcurrentHashMap<>();
    private final Map<Long, LRUCache<List<ChartDataModel>>> threadDataCacheMap =
        new ConcurrentHashMap<>();
    private final Map<Long, LRUCache<List<ChartDataModel>>> threadDeadCacheMap =
        new ConcurrentHashMap<>();
    private final Map<Long, Long> cpuFirstTsMap = new HashMap<>();
    private final Map<Long, Long> threadFirstTsMap = new HashMap<>();

    private CpuDataCache() {
    }

    /**
     * Instance getter
     *
     * @return MemoryDataCache
     */
    public static CpuDataCache getInstance() {
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addCpuDataModel");
        }
        LRUCache<List<ChartDataModel>> cache = cpuDataCacheMap.get(sessionId);
        // If cache map is null, generate the new map and save the current timestamp as first timestamp
        if (cache == null) {
            cache = genNewSessionCache();
            cpuDataCacheMap.put(sessionId, cache);
            cpuFirstTsMap.put(sessionId, timestamp);
        }
        synchronized (cpuDataCacheMap.get(sessionId)) {
            // Save relative time
            int time = (int) (timestamp - cpuFirstTsMap.get(sessionId));
            cache.addCaCheData(time, dataModels);
            // Here we need to sort the map, otherwise the key(timestamp) of the map will be out of order
            cpuDataCacheMap.put(sessionId, cache);
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addThreadDataModel");
        }
        LRUCache<List<ChartDataModel>> cache = threadDataCacheMap.get(sessionId);
        // If cache map is null, generate the new map and save the current timestamp as first timestamp
        if (cache == null) {
            cache = genNewSessionCache();
            threadDataCacheMap.put(sessionId, cache);
            threadFirstTsMap.put(sessionId, timestamp);
        }
        synchronized (threadDataCacheMap.get(sessionId)) {
            // Save relative time
            int time = (int) (timestamp - threadFirstTsMap.get(sessionId));
            cache.addCaCheData(time, dataModels);
            threadDataCacheMap.put(sessionId, cache);
        }
    }

    /**
     * Add Cpu data to cache map
     *
     * @param sessionId Session id
     * @param timestamp Timestamp of data
     * @param dataModels Data model list
     */
    public void addDeadThreadDataModel(long sessionId, long timestamp, List<ChartDataModel> dataModels) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addThreadDataModel");
        }
        LRUCache<List<ChartDataModel>> cache = threadDeadCacheMap.get(sessionId);
        // If cache map is null, generate the new map and save the current timestamp as first timestamp
        if (cache == null) {
            cache = genNewSessionCache();
            threadDeadCacheMap.put(sessionId, cache);
        }
        synchronized (threadDeadCacheMap.get(sessionId)) {
            // Save relative time
            int time = (int) (timestamp - threadFirstTsMap.get(sessionId));
            cache.addCaCheData(time, dataModels);
            threadDeadCacheMap.put(sessionId, cache);
        }
    }

    /**
     * Generate new session cache map
     *
     * @return LRUCache <List<ChartDataModel>>
     */
    private LRUCache<List<ChartDataModel>> genNewSessionCache() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("genNewSessionCache");
        }
        return new LRUCache<>();
    }

    /**
     * Get cpu data
     *
     * @param sessionId Session id
     * @param startTime start time
     * @param endTime end time
     * @return LinkedHashMap <Integer, List<ChartDataModel>> getCpuData
     */
    public LinkedHashMap<Integer, List<ChartDataModel>> getCpuData(long sessionId, int startTime, int endTime) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getCpuData");
        }
        LinkedHashMap<Integer, List<ChartDataModel>> result = new LinkedHashMap<>();
        LRUCache<List<ChartDataModel>> cache = cpuDataCacheMap.get(sessionId);
        if (cache == null) {
            return result;
        }
        synchronized (cpuDataCacheMap.get(sessionId)) {
            result = cache.getCaCheData(startTime, endTime);
        }
        return result;
    }

    /**
     * Get thread data
     *
     * @param sessionId Session id
     * @param startTime start time
     * @param endTime end time
     * @return LinkedHashMap <Integer, List<ChartDataModel>> getCpuData
     */
    public LinkedHashMap<Integer, List<ChartDataModel>> getThreadData(long sessionId, int startTime, int endTime) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getThreadData");
        }
        LinkedHashMap<Integer, List<ChartDataModel>> result = new LinkedHashMap<>();
        LRUCache<List<ChartDataModel>> cache = threadDataCacheMap.get(sessionId);
        if (cache == null) {
            return result;
        }
        synchronized (threadDataCacheMap.get(sessionId)) {
            result = cache.getCaCheData(startTime, endTime);
        }
        return result;
    }


    /**
     * Get thread data
     *
     * @param sessionId Session id
     * @param startTime start time
     * @param endTime end time
     * @return LinkedHashMap <Integer, List<ChartDataModel>> getCpuData
     */
    public LinkedHashMap<Integer, List<ChartDataModel>> getDeadThreadData(long sessionId, int startTime, int endTime) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getThreadData");
        }
        LinkedHashMap<Integer, List<ChartDataModel>> result = new LinkedHashMap<>();
        LRUCache<List<ChartDataModel>> cache = threadDeadCacheMap.get(sessionId);
        if (cache == null) {
            return result;
        }
        synchronized (threadDeadCacheMap.get(sessionId)) {
            result = cache.getCaCheData(startTime, endTime);
        }
        return result;
    }

    /**
     * Clear cache by session id when the session was deleted
     *
     * @param sessionId Session id
     */
    public void clearCacheBySession(long sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("clearCacheBySession");
        }
        cpuDataCacheMap.remove(sessionId);
        cpuFirstTsMap.remove(sessionId);
        threadDataCacheMap.remove(sessionId);
        threadFirstTsMap.remove(sessionId);
    }
}
