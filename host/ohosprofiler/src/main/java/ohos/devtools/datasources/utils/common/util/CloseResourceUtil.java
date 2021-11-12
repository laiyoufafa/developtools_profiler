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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Close Resource Util
 */
public class CloseResourceUtil {
    private static final Logger LOGGER = LogManager.getLogger(CloseResourceUtil.class);

    /**
     * Close Resource
     *
     * @param logger Logger
     * @param conn Connection
     * @param ps PreparedStatement
     * @param statement Statement
     */
    public static void closeResource(Logger logger, Connection conn, PreparedStatement ps, Statement statement) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("closeResource");
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("SQLException error: " + exception.getMessage());
                }
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("SQLException error: " + exception.getMessage());
                }
            }
        }

        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("SQLException error: " + exception.getMessage());
                }
            }
        }
    }
}
