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

import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.commons.collections.map.LRUMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * LRUCache
 *
 * @param <V> v
 * @since: 2021/8/25
 */
public class LRUCache<V> {
    private static final Logger LOGGER = LogManager.getLogger(LRUCache.class);

    private final LinkedList<Integer> linkedList = new LinkedList<>();
    private final LRUMap lruMap = new LRUMap(1000);

    /**
     * addCaCheData
     *
     * @param key key
     * @param value value
     */
    public void addCaCheData(Integer key, V value) {
        this.add(key);
        lruMap.put(key, value);
    }

    private void add(int element) {
        boolean inserted = false;
        for (int index = 0; index < linkedList.size(); index++) {
            if (element < linkedList.get(index)) {
                linkedList.add(index, element);
                inserted = true;
                break;
            }
        }
        if (!inserted) {
            linkedList.add(element);
        }
    }

    /**
     * getCaCheData
     *
     * @param start start
     * @param end end
     * @return LinkedHashMap<Integer, V>
     */
    public LinkedHashMap<Integer, V> getCaCheData(int start, int end) {
        boolean firstEntry = true;
        boolean endEntry = true;
        int startIndex;
        LinkedHashMap<Integer, V> linkedHashMap = new LinkedHashMap<>();
        for (int i = 0; i < linkedList.size(); i++) {
            int integer = linkedList.get(i);
            if (integer >= start && integer <= end) {
                if (firstEntry) {
                    startIndex = i - 1;
                    if (startIndex >= 0) {
                        Integer timeBeforeKey = linkedList.get(startIndex);
                        linkedHashMap.put(timeBeforeKey, (V) lruMap.get(timeBeforeKey));
                    }
                    firstEntry = false;
                }
                linkedHashMap.put(integer, (V) lruMap.get(integer));
            } else if (integer > end) {
                if (endEntry) {
                    linkedHashMap.put(integer, (V) lruMap.get(integer));
                    break;
                }
            } else {
                if (ProfilerLogManager.isInfoEnabled()) {
                    LOGGER.info("linkedHashMap put error: {}", integer);
                }
            }
        }
        long removeStartTime = (end - start) >= 10000 ? start : (end - 10000) ;
        Iterator<Integer> iterator = linkedList.iterator();
        while (true) {
            if (!iterator.hasNext()) {
                break;
            }
            Integer next = iterator.next();
            if (next < (removeStartTime - 1000)) {
                iterator.remove();
            } else {
                break;
            }
        }
        return linkedHashMap;
    }

    /**
     * size
     *
     * @return int
     */
    public int size() {
        return linkedList.size();
    }

    /**
     * isEmpty
     *
     * @return boolean
     */
    public boolean isEmpty() {
        return linkedList.isEmpty();
    }

    /**
     * get Map data
     *
     * @param key key
     * @return V
     */
    public V get(int key) {
        return (V) lruMap.get(key);
    }
}
