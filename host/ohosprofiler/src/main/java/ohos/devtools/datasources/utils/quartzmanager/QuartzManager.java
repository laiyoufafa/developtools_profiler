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

import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
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
     * DELAY 0
     */
    public static final int DELAY = 0;

    /**
     * PERIOD 4000
     */
    public static final int PERIOD = 4000;
    private static final Logger LOGGER = LogManager.getLogger(QuartzManager.class);
    private static volatile QuartzManager instance;

    private Map<String, Runnable> runnableHashMap = new ConcurrentHashMap<>();
    private Map<String, ScheduledExecutorService> executorHashMap = new ConcurrentHashMap<>();

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

    /**
     * execution
     *
     * @param runName runName
     * @param runnable runnable
     */
    public void addExecutor(String runName, Runnable runnable) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addExecutor {}", runName);
        }
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("startExecutor {}", runName);
        }
        ScheduledExecutorService scheduled = executorHashMap.get(runName);
        Runnable runnable = runnableHashMap.get(runName);
        if (delay > 0) {
            scheduled.scheduleWithFixedDelay(runnable, delay, period, TimeUnit.MILLISECONDS);
        } else {
            scheduled.scheduleAtFixedRate(runnable, 0, period, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * deleteExecutor
     *
     * @param runName runName
     */
    public void deleteExecutor(String runName) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deleteExecutor {}", runName);
        }
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
     * checkService
     *
     * @param runName runName
     * @return ScheduledExecutorService
     */
    public Optional<ScheduledExecutorService> checkService(String runName) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("checkService");
        }
        ScheduledExecutorService scheduledExecutorService = executorHashMap.get(runName);
        return Optional.ofNullable(scheduledExecutorService);
    }
}
