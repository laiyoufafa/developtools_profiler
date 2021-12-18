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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ProfilerLogManager
 */
public class ProfilerLogManager {
    private static final Logger LOGGER = LogManager.getLogger(ProfilerLogManager.class);
    private static Level nowLogLevel = Level.OFF;

    /**
     * 单例进程对象
     */
    private static ProfilerLogManager singleton = null;

    /**
     * getSingleton
     *
     * @return ProfilerLogManager
     */
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

    /**
     * updateLogLevel
     *
     * @param logLevel loglevel
     * @return boolean
     */
    public static boolean updateLogLevel(Level logLevel) {
        if (logLevel == null) {
            return false;
        }
        nowLogLevel = logLevel;
        return true;
    }

    /**
     * isInfoEnabled
     *
     * @return boolean
     */
    public static boolean isInfoEnabled() {
        return nowLogLevel.intLevel() >= Level.INFO.intLevel();
    }

    /**
     * isErrorEnabled
     *
     * @return boolean
     */
    public static boolean isErrorEnabled() {
        return nowLogLevel.intLevel() >= Level.ERROR.intLevel();
    }

    /**
     * isDebugEnabled
     *
     * @return boolean
     */
    public static boolean isDebugEnabled() {
        return nowLogLevel.intLevel() >= Level.DEBUG.intLevel();
    }

    /**
     * isWarnEnabled
     *
     * @return boolean
     */
    public static boolean isWarnEnabled() {
        return nowLogLevel.intLevel() >= Level.WARN.intLevel();
    }

    /**
     * getNowLogLevel
     *
     * @return Level
     */
    public static Level getNowLogLevel() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getNowLogLevel");
        }
        return nowLogLevel;
    }
}
