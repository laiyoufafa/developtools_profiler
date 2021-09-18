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

package ohos.devtools.views.charts.model;

import java.util.HashMap;
import java.util.Map;

import static ohos.devtools.views.charts.utils.ChartConstants.DEFAULT_MAX_MILLIS;
import static ohos.devtools.views.charts.utils.ChartConstants.DEFAULT_TIME_UNIT;

/**
 * Chart绘制标准，用于统一Timeline和Chart
 *
 * @since 2021/1/26 15:10
 */
public class ChartStandard {
    /**
     * sessionId
     */
    private final long sessionId;

    /**
     * 用户框选时间范围的集合
     *
     * @see "Key: 区别各个Chart的key, value: 框选的时间范围"
     */
    private final Map<String, ChartDataRange> selectedRangeMap = new HashMap<>();

    /**
     * 窗体上可以展示的最大毫秒数，单位为毫秒
     */
    private int maxDisplayMillis;

    /**
     * Timeline和Chart刻度线上的最小刻度间隔，单位为毫秒
     */
    private int minMarkInterval;

    /**
     * 本次Chart首次创建并启动刷新时的时间戳
     */
    private long firstTimestamp;

    /**
     * 本次Chart最后一个数据的时间戳
     */
    private long lastTimestamp;

    /**
     * Chart展示的时间范围
     */
    private ChartDataRange displayRange;

    /**
     * 构造函数
     *
     * @param sessionId sessionId
     */
    public ChartStandard(long sessionId) {
        this.sessionId = sessionId;
        maxDisplayMillis = DEFAULT_MAX_MILLIS;
        minMarkInterval = DEFAULT_TIME_UNIT;
    }

    /**
     * 修改Chart展示的时间范围
     *
     * @param start 开始时间
     * @param end   结束时间
     */
    public void updateDisplayTimeRange(int start, int end) {
        if (displayRange == null) {
            displayRange = new ChartDataRange();
        }
        displayRange.setStartTime(start);
        displayRange.setEndTime(end);
    }

    /**
     * 根据Key获取对应的框选时间范围
     *
     * @param key 区别各个Chart的key
     * @return ChartDataRange
     */
    public ChartDataRange getSelectedRange(String key) {
        return selectedRangeMap.get(key);
    }

    /**
     * 更新用户框选的起始时间和坐标点
     *
     * @param key       区别各个Chart的key
     * @param startTime 用户新框选的时间范围
     */
    public void updateSelectedStart(String key, int startTime) {
        ChartDataRange range = selectedRangeMap.get(key);
        if (range == null) {
            range = new ChartDataRange();
            selectedRangeMap.put(key, range);
        }
        range.setStartTime(startTime);
    }

    /**
     * 更新用户框选的起始时间和坐标点
     *
     * @param key     区别各个Chart的key
     * @param endTime 用户新框选的时间范围
     */
    public void updateSelectedEnd(String key, int endTime) {
        ChartDataRange range = selectedRangeMap.get(key);
        if (range == null) {
            range = new ChartDataRange();
            selectedRangeMap.put(key, range);
        }
        range.setEndTime(endTime);
    }

    /**
     * 清空所有Chart的框选时间范围
     */
    public void clearAllSelectedRanges() {
        selectedRangeMap.clear();
    }

    /**
     * 清空指定Chart的框选时间范围
     *
     * @param key 区别各个Chart的key
     */
    public void clearSelectedRange(String key) {
        selectedRangeMap.remove(key);
    }

    public long getSessionId() {
        return sessionId;
    }

    public int getMaxDisplayMillis() {
        return maxDisplayMillis;
    }

    public void setMaxDisplayMillis(int maxDisplayMillis) {
        this.maxDisplayMillis = maxDisplayMillis;
    }

    public int getMinMarkInterval() {
        return minMarkInterval;
    }

    public void setMinMarkInterval(int minMarkInterval) {
        this.minMarkInterval = minMarkInterval;
    }

    public long getFirstTimestamp() {
        return firstTimestamp;
    }

    public void setFirstTimestamp(long firstTimestamp) {
        this.firstTimestamp = firstTimestamp;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public ChartDataRange getDisplayRange() {
        return displayRange;
    }
}
