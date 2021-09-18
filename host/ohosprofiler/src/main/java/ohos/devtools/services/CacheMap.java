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

package ohos.devtools.services;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Map with its own recycling mechanism
 *
 * @param <K> k
 * @param <V> v
 */
public class CacheMap<K,V> extends LinkedHashMap<K,V> {
    /**
     * Cache max size
     *
     * @see "Cache about 40 seconds of data"
     */
    private static final int CACHE_MAX_SIZE = 1000;

    /**
     * Map default load factor
     */
    private static final float LOAD_FACTOR = 0.75F;

    /**
     * CacheMap constructor
     */
    public CacheMap() {
        this(CACHE_MAX_SIZE, LOAD_FACTOR, true);
    }

    /**
     * CacheMap constructor
     *
     * @param initialCapacity initialCapacity
     * @param loadFactor loadFactor
     * @param accessOrder accessOrder
     */
    public CacheMap(int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > CACHE_MAX_SIZE;
    }
}
