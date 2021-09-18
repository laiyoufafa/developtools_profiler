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

import org.apache.logging.log4j.Logger;

/**
 * logger print
 */
public class PrintUtil {
    /**
     * print logging
     *
     * @param logger logging
     * @param str    logging message
     * @param state  logging state
     */
    public static void print(Logger logger, String str, int state) {
        if (state == 0) {
            if (logger.isInfoEnabled()) {
                logger.info(str);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug(str);
            }
        }
    }
}
