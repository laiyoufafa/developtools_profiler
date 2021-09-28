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

package ohos.devtools.datasources.utils.quartzmanager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * timed task management
 */
public class QuartzManager {
    /**
     * DELAY
     */
    public static final int DELAY = 0;

    /**
     * PERIOD
     */
    public static final int PERIOD = 3000;
    private static final Logger LOGGER = LogManager.getLogger(QuartzManager.class);
    private static final long DEFAULT_KEEPALIVE_MILLIS = 10L;
    private static volatile QuartzManager instance;

    /**
     * getInstance
     *
     * @return QuartzManager
     */
    public static QuartzManager getInstance() {
        if (instance == null) {
            synchronized (QuartzManager.class) {
                if (instance == null) {
                    instance = new QuartzManager();
                }
            }
        }
        return instance;
    }

    private Map<String, Runnable> runnableHashMap = new ConcurrentHashMap<String, Runnable>();

    private Map<String, ScheduledExecutorService> executorHashMap =
        new ConcurrentHashMap<String, ScheduledExecutorService>();

    /**
     * execution
     *
     * @param runName runName
     * @param runnable runnable
     */
    public void addExecutor(String runName, Runnable runnable) {
        LOGGER.debug("add scheduleWithFixedDelay{}", runName);
        ScheduledExecutorService scheduled = new ScheduledThreadPoolExecutor(1);
        executorHashMap.put(runName, scheduled);
        runnableHashMap.put(runName, runnable);
    }

    /**
     * begin Execution
     *
     * @param runName runName
     * @param delay delay
     * @param period period
     */
    public void startExecutor(String runName, long delay, long period) {
        ScheduledExecutorService scheduled = executorHashMap.get(runName);
        Runnable runnable = runnableHashMap.get(runName);
        if (delay > 0) {
            LOGGER.debug("scheduleWithFixedDelay start {}", delay);
            scheduled.scheduleWithFixedDelay(runnable, delay, period, TimeUnit.MILLISECONDS);
        } else {
            LOGGER.debug("scheduleAtFixedRate start {}", delay);
            scheduled.scheduleAtFixedRate(runnable, 0, period, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * deleteExecutor
     *
     * @param runName runName
     */
    public void deleteExecutor(String runName) {
        ScheduledExecutorService scheduledExecutorService = executorHashMap.get(runName);
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
            if (executorHashMap != null && executorHashMap.size() != 0) {
                executorHashMap.remove(runName);
            }
            if (runnableHashMap != null && runnableHashMap.size() != 0) {
                runnableHashMap.remove(runName);
            }
        }
    }

    /**
     * endExecutor
     *
     * @param runName runName
     */
    public void endExecutor(String runName) {
        ScheduledExecutorService scheduledExecutorService = executorHashMap.get(runName);
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
    }

    /**
     * checkService
     *
     * @param runName runName
     * @return ScheduledExecutorService
     */
    public Optional<ScheduledExecutorService> checkService(String runName) {
        ScheduledExecutorService scheduledExecutorService = executorHashMap.get(runName);
        return Optional.ofNullable(scheduledExecutorService);
    }
}
