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

package ohos.devtools.datasources.utils.profilerlog;

import ohos.devtools.datasources.utils.process.service.ProcessManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

/**
 * ProfilerLogManager
 */
public class ProfilerLogManager {
    private static final Logger LOGGER = LogManager.getLogger(ProcessManager.class);

    /**
     * 单例进程对象
     */
    private static ProfilerLogManager singleton = null;

    public static ProfilerLogManager getSingleton() {
        if (singleton == null) {
            synchronized (ProfilerLogManager.class) {
                if (singleton == null) {
                    singleton = new ProfilerLogManager();
                }
            }
        }
        return singleton;
    }

    private Level nowLogLevel = Level.ERROR;

    /**
     * 修改日志等级
     *
     * @param logLevel loglevel
     * @return boolean
     */
    public boolean updateLogLevel(Level logLevel) {
        if (logLevel == null) {
            return false;
        }
        org.apache.logging.log4j.spi.LoggerContext context = LogManager.getContext(false);
        LoggerContext loggerContext = null;
        if (context instanceof LoggerContext) {
            loggerContext = (LoggerContext) context;
        } else {
            return false;
        }
        Configuration config = loggerContext.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(logLevel);
        loggerContext.updateLoggers();
        nowLogLevel = logLevel;
        return true;
    }

    /**
     * getNowLogLevel
     *
     * @return Level
     */
    public Level getNowLogLevel() {
        return nowLogLevel;
    }
}
