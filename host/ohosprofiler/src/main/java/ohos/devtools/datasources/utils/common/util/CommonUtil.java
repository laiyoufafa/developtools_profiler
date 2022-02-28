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

package ohos.devtools.datasources.utils.common.util;

import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.common.LayoutConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Extract saves for individual function tool methods.
 *
 * @since 2021/5/19 16:39
 */
public class CommonUtil {
    private static final Logger LOGGER = LogManager.getLogger(CommonUtil.class);

    private static volatile Integer requestId = 1;

    private CommonUtil() {
    }

    /**
     * Sets the intial collection size.
     *
     * @param size size
     * @return Returns the initial collection size.
     */
    public static int collectionSize(int size) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("collectionSize");
        }
        return (size <= 0) ? LayoutConstants.SIXTEEN : (int) (size / LayoutConstants.LOAD_FACTOR + 1.0F);
    }

    /**
     * Gets the request ID.
     *
     * @return Returns the initial collection size.
     */
    public static int getRequestId() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getRequestId");
        }
        if (requestId == Integer.MAX_VALUE) {
            requestId = 1;
        }
        return requestId++;
    }

    /**
     * Generates a session name.
     *
     * @param deviceName Indicates the device name.
     * @param pid Indicates the process ID.
     * @return String
     */
    public static String generateSessionName(String deviceName, int pid) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("generateSessionName");
        }
        return deviceName + pid;
    }

    /**
     * Gets the ID of a local session.
     *
     * @return Long
     */
    public static Long getLocalSessionId() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getLocalSessionId");
        }
        return System.currentTimeMillis();
    }
}
