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

package ohos.devtools.services.memory.memoryservice;

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
 * Memory data cache
 *
 * @since 2021/5/19 16:39
 */
public class MemoryDataCache {
    private static final Logger LOGGER = LogManager.getLogger(MemoryDataCache.class);
    private static final float LOAD_FACTOR = 0.75F;
    private static final int CACHE_MAX_SIZE = 1500;
    private static MemoryDataCache instance = new MemoryDataCache();

    private final Map<Long, LRUCache<List<ChartDataModel>>> dataCacheMap = new ConcurrentHashMap<>();
    private final Map<Long, Long> firstTsMap = new HashMap<>();

    private MemoryDataCache() {
    }

    /**
     * Instance getter
     *
     * @return MemoryDataCache
     */
    public static MemoryDataCache getInstance() {
        return instance;
    }

    /**
     * Add memory data to cache map
     *
     * @param sessionId Session id
     * @param timestamp Timestamp of data
     * @param dataModels Data model list
     */
    public void addDataModel(long sessionId, long timestamp, List<ChartDataModel> dataModels) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addDataModel");
        }
        LRUCache<List<ChartDataModel>> cache = dataCacheMap.get(sessionId);
        // If cache map is null, generate the new map and save the current timestamp as first timestamp
        if (cache == null) {
            cache = new LRUCache<List<ChartDataModel>>();
            dataCacheMap.put(sessionId, cache);
            firstTsMap.put(sessionId, timestamp);
        }
        synchronized (dataCacheMap.get(sessionId)) {
            // Save relative time
            int time = (int) (timestamp - firstTsMap.get(sessionId));
            cache.addCaCheData(time, dataModels);
            dataCacheMap.put(sessionId, cache);
        }
    }

    /**
     * Get memory data
     *
     * @param sessionId Session id
     * @param startTime start time
     * @param endTime end time
     * @return LinkedHashMap <Integer, List<ChartDataModel>> Data map
     */
    public LinkedHashMap<Integer, List<ChartDataModel>> getData(long sessionId, int startTime, int endTime) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getData");
        }
        LinkedHashMap<Integer, List<ChartDataModel>> result = new LinkedHashMap<>();
        LRUCache<List<ChartDataModel>> cache = dataCacheMap.get(sessionId);
        if (cache == null) {
            return result;
        }
        synchronized (dataCacheMap.get(sessionId)) {
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
        dataCacheMap.remove(sessionId);
        firstTsMap.remove(sessionId);
    }
}
