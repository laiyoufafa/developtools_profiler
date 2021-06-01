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

import ohos.devtools.services.memory.MemoryService;
import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartStandard;
import ohos.devtools.views.layout.chartview.event.IChartEventObserver;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static ohos.devtools.views.common.LayoutConstants.INDEX_FOUR;
import static ohos.devtools.views.common.LayoutConstants.TEN;
import static ohos.devtools.views.common.ViewConstants.INITIAL_VALUE;

/**
 * 缓存观察类
 *
 * @version 1.0
 * @date 2021/2/2 19:02
 **/
public class CacheObserver implements IChartEventObserver {
    /**
     * 缓存去数据库获取新数据的间隔
     */
    private static final int CACHE_FREQ = 500;

    /**
     * 线程池
     */
    private final ThreadPoolExecutor executor;

    /**
     * Session Id
     */
    private final long sessionId;

    /**
     * 标识时间，缓存每隔CACHE_FREQ去数据库拿一次数据
     */
    private int flagTime = INITIAL_VALUE;

    /**
     * 构造函数
     *
     * @param sessionId Session Id
     */
    public CacheObserver(long sessionId) {
        this.sessionId = sessionId;
        executor = new ThreadPoolExecutor(INDEX_FOUR, TEN, TEN, TimeUnit.SECONDS, new ArrayBlockingQueue<>(INDEX_FOUR),
            new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    /**
     * refreshStandard
     *
     * @param standard       绘图标准
     * @param startTime      开始时间
     * @param endTime        结束时间
     * @param maxDisplayTime 最大显示时间
     */
    @Override
    public void refreshStandard(ChartStandard standard, int startTime, int endTime, int maxDisplayTime) {
    }

    /**
     * refreshView
     *
     * @param range          时间范围
     * @param firstTimestamp 本次Chart首次创建并启动刷新时的时间戳
     * @param isUseCache     是否使用缓存机制
     */
    @Override
    public void refreshView(ChartDataRange range, long firstTimestamp, boolean isUseCache) {
        if (flagTime == INITIAL_VALUE) {
            addCacheData(range.getEndTime(), firstTimestamp);
        }
        int endTime = range.getEndTime();
        if (endTime < CACHE_FREQ) {
            return;
        }
        // endTime < flagTime，说明停止后重新启动了新任务，这里要初始化flagTime
        if (endTime < flagTime) {
            addCacheData(range.getEndTime(), firstTimestamp);
            flagTime = INITIAL_VALUE;
            return;
        }
        if (endTime - flagTime > CACHE_FREQ) {
            // 调用数据库，获取数据
            addCacheData(endTime, firstTimestamp);
            // 刷新标识时间
            flagTime = endTime;
        }
    }

    private void addCacheData(int lastTime, long firstTimestamp) {
        executor.execute(new MemoryCacheThread(lastTime, firstTimestamp));
    }

    private final class MemoryCacheThread implements Runnable {
        private final int endTime;
        private final long firstTimestamp;

        private MemoryCacheThread(int endTime, long firstTimestamp) {
            this.endTime = endTime;
            this.firstTimestamp = firstTimestamp;
        }

        /**
         * run
         */
        @Override
        public void run() {
            // 每CACHE_FREQ去数据库拿一次数据，但是要拿超过CACHE_FREQ的数据
            // 否则会因为Chart已经刷新到x秒，缓存中最后的数据也在x秒，导致Chart右侧抖动
            MemoryService.getInstance().addData(sessionId, endTime, endTime + CACHE_FREQ + CACHE_FREQ, firstTimestamp);
        }
    }
}
