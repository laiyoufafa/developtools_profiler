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

package ohos.devtools.services.userdata;

import ohos.devtools.datasources.databases.datatable.enties.UserData;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.services.LRUCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UserDataCache
 *
 * @since 2021/11/22
 */
public class UserDataCache {
    private static final Logger LOGGER = LogManager.getLogger(UserDataCache.class);
    private static final float LOAD_FACTOR = 0.75F;
    private static final int CACHE_MAX_SIZE = 1500;
    private static final UserDataCache INSTANCE = new UserDataCache();

    private final Map<Long, LRUCache<UserData>> dataCacheMap = new ConcurrentHashMap<>();
    private final Map<Long, Long> firstTsMap = new HashMap<>();

    private UserDataCache() {
    }

    /**
     * Instance getter
     *
     * @return DiskIoDataCache
     */
    public static UserDataCache getInstance() {
        return UserDataCache.INSTANCE;
    }

    /**
     * Add user data to cache map
     *
     * @param sessionId Session id
     * @param timestamp Timestamp of data
     * @param sdkData sdkData
     */
    public void addDataModel(long sessionId, long timestamp, UserData sdkData) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addDataModel");
        }
        LRUCache<UserData> cache = dataCacheMap.get(sessionId);
        // If cache map is null, generate the new map and save the current timestamp as first timestamp
        if (cache == null) {
            cache = genNewSessionCache();
            dataCacheMap.put(sessionId, cache);
            firstTsMap.put(sessionId, timestamp);
        }
        synchronized (dataCacheMap.get(sessionId)) {
            // Save relative time
            int time = (int) (timestamp - firstTsMap.get(sessionId));
            cache.addCaCheData(time, sdkData);
            dataCacheMap.put(sessionId, cache);
        }
    }

    /**
     * Generate new session cache map
     *
     * @return Map
     */
    private LRUCache<UserData> genNewSessionCache() {
        return new LRUCache<>();
    }

    /**
     * Get user data
     *
     * @param sessionId Session id
     * @param startTime start time
     * @param endTime end time
     * @return Data map
     */
    public LinkedHashMap<Integer, UserData> getData(long sessionId, int startTime, int endTime) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getData");
        }
        LinkedHashMap<Integer, UserData> result = new LinkedHashMap<>();
        LRUCache<UserData> cache = dataCacheMap.get(sessionId);
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
