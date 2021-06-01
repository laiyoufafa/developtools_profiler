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

package ohos.devtools.services.memory;

import ohos.devtools.views.common.LayoutConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static ohos.devtools.views.common.ViewConstants.INITIAL_VALUE;

/**
 * 多设备+多模式缓存区域
 *
 * @version 1.0
 * @date 2021/2/2 19:02
 **/
public final class ChartDataCache<T> {
    private static final Logger LOGGER = LogManager.getLogger(ChartDataCache.class);

    /**
     * 总内存的数据量，chart面板可以绘制20s时长额度数据，则缓存保存30s的数据
     */
    private static final int MINT = 30;

    /**
     * 单例实体
     */
    private static volatile ChartDataCache instance;

    /**
     * 存放各种类型缓存的map
     */
    private final Map<String, LinkedHashMap<Long, T>> dataCacheMap = new ConcurrentHashMap<>();
    /**
     * 单个chart的缓存
     */
    private LinkedHashMap<Long, T> cacheBox = null;

    /**
     * 0参私有化
     */
    private ChartDataCache() {
    }

    /**
     * 实体方法
     *
     * @return ChartDataCache对象
     */
    public static ChartDataCache getInstance() {
        if (instance == null) {
            synchronized (ChartDataCache.class) {
                if (instance == null) {
                    instance = new ChartDataCache();
                }
            }
        }
        return instance;
    }

    /**
     * 构造方法
     *
     * @param cacheName 缓存名称（设备名+模块）
     * @param blockSize chart面板1s平均可获得的数据数量
     */
    public void initCache(String cacheName, int blockSize) {
        int cacheMaxSize = MINT * blockSize;
        cacheBox = new LinkedHashMap<>(cacheMaxSize, LayoutConstants.LOAD_FACTOR, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, T> eldest) {
                if (cacheBox.size() > cacheMaxSize) {
                    cacheBox.remove(eldest.getKey());
                    return true;
                } else {
                    return false;
                }
            }
        };
        dataCacheMap.put(cacheName, cacheBox);
    }

    public Map<String, LinkedHashMap<Long, T>> getDataCacheMap() {
        return dataCacheMap;
    }

    /**
     * 数据块添加到cache缓存中
     *
     * @param cacheName  缓存名称
     * @param cacheBlock 待从数据库获取的数据
     */
    public void addCacheBlock(String cacheName, LinkedHashMap<Long, T> cacheBlock) {
        synchronized (dataCacheMap.get(cacheName)) {
            LinkedHashMap<Long, T> cacheBoxAdd = dataCacheMap.get(cacheName);
            for (Map.Entry<Long, T> entry : cacheBlock.entrySet()) {
                cacheBoxAdd.put(entry.getKey(), entry.getValue());
            }

            // 这里需要给Map中的集合排序，否则会出现Map的key时间戳乱序现象
            LinkedHashMap<Long, T> sorted =
                dataCacheMap.get(cacheName).entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> {
                        throw new AssertionError();
                    }, LinkedHashMap::new));
            dataCacheMap.put(cacheName, sorted);
        }
    }

    /**
     * chart调用的接口，获取cache内存中的数据
     *
     * @param cacheName      缓存名称
     * @param startTime      开始时间
     * @param endTime        结束时间
     * @param firstTimestamp 本次Chart首次创建并启动刷新时的时间戳
     * @return LinkedHashMap
     */
    public LinkedHashMap<Long, T> getDataCache(String cacheName, int startTime, int endTime, long firstTimestamp) {
        LinkedHashMap<Long, T> result = new LinkedHashMap<>();
        long startTs = startTime + firstTimestamp;
        long endTs = endTime + firstTimestamp;
        // 当startTime > 0时（Chart铺满界面时），需要取第一个点的前一个点用于Chart绘制，填充空白，解决边界闪烁
        if (startTime > 0) {
            long beforeStart = getTargetTime(cacheName, startTs, true);
            if (beforeStart != 0) {
                result.put(beforeStart, dataCacheMap.get(cacheName).get(beforeStart));
            }
        }
        synchronized (dataCacheMap.get(cacheName)) {
            Set<Map.Entry<Long, T>> entrySet = dataCacheMap.get(cacheName).entrySet();
            for (Map.Entry<Long, T> entry : entrySet) {
                long key = entry.getKey();
                if (key >= startTs && key <= endTs) {
                    result.put(key, entry.getValue());
                }
            }

            // 取最后一个点的后一个点用于Chart绘制，填充空白，解决边界闪烁
            long afterEnd = getTargetTime(cacheName, endTs, false);
            if (afterEnd != 0) {
                result.put(afterEnd, dataCacheMap.get(cacheName).get(afterEnd));
            }
        }
        return result;
    }

    /**
     * 清理数据缓存
     *
     * @param cacheName cacheName
     */
    public void clearDataCache(String cacheName) {
        LinkedHashMap<Long, T> cacheBoxClear = dataCacheMap.get(cacheName);
        cacheBoxClear.clear();
    }

    /**
     * 在dataMap中找到给定时间的前一个时间或者后一个时间
     *
     * @param cacheName String
     * @param time      给定时间
     * @param isBefore  true：前一个时间，false：后一个时间
     * @return 结果
     */
    private long getTargetTime(String cacheName, long time, boolean isBefore) {
        synchronized (dataCacheMap.get(cacheName)) {
            LinkedHashMap<Long, T> cacheBoxGet = dataCacheMap.get(cacheName);
            if (cacheBoxGet == null || cacheBoxGet.size() == 0) {
                return 0;
            }

            Set<Long> keySet = cacheBoxGet.keySet();
            Long[] timeArray = keySet.toArray(new Long[0]);
            // 先判断下是不是大于最大值或者小于最小值，是的话直接返回
            if (time == timeArray[0] || time == timeArray[timeArray.length - 1]) {
                return 0;
            }

            return timeArray[searchIndex(timeArray, time, isBefore)];
        }
    }

    /**
     * 在有序数组中找到目标值的前一个或后一个值的index
     *
     * @param arr   有序数组
     * @param value 目标值
     * @param flag  true：取前一个值，false：取后一个值
     * @return 目标值的前一个或后一个值的index
     */
    private static int searchIndex(Long[] arr, long value, boolean flag) {
        // 开始位置
        int low = 0;
        // 结束位置
        int high = arr.length - 1;
        // 先判断下是不是大于最大值或者小于最小值，是的话直接返回
        if (value <= arr[low]) {
            return low;
        }
        if (value >= arr[high]) {
            return high;
        }

        int halfValue = 2;
        int index = INITIAL_VALUE;
        while (low <= high) {
            int middle = (low + high) / halfValue;
            // 如果值正好相等，则直接返回查询到的索引
            if (value == arr[middle]) {
                index = flag ? middle - 1 : middle + 1;
                break;
            }

            // 大于当前index的值，小于下一个index的值，根据flag取前一个或后一个
            if (value > arr[middle] && value < arr[middle + 1]) {
                // 返回查询到的索引
                index = flag ? middle : middle + 1;
                break;
            }

            if (value > arr[middle]) {
                low = middle + 1;
            }

            if (value < arr[middle]) {
                high = middle - 1;
            }
        }
        return index;
    }

}
