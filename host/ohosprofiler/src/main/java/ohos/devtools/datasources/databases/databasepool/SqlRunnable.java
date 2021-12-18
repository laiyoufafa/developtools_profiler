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

package ohos.devtools.datasources.databases.databasepool;

import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * sql carried out
 *
 * @since 2021/10/26
 */
public class SqlRunnable {
    private static final Logger LOGGER = LogManager.getLogger(SqlRunnable.class);

    /**
     * method
     *
     * @param conn Connection
     * @param sql sql
     * @return boolean
     */
    public boolean execute(Connection conn, String sql) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("execute");
        }
        boolean result = false;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            result = stmt.executeUpdate(sql) > 0 ? true : false;
        } catch (SQLException throwAbles) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(throwAbles.getMessage());
            }
        } finally {
            close(stmt, conn);
        }
        return result;
    }

    /**
     * executeBatch
     *
     * @param conn Connection
     * @param ste Prepared Statement
     * @return boolean
     */
    public boolean executeBatch(Connection conn, PreparedStatement ste) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("executeBatch");
        }
        try {
            conn.setAutoCommit(false);
            int[] result = ste.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException throwAbles) {
            try {
                conn.rollback();
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error(exception.getMessage());
                }
                return false;
            }
        } finally {
            close(ste, conn);
        }
        return false;
    }

    /**
     * executeQuery
     *
     * @param stmt Statement
     * @param sql sql
     * @return ResultSet
     */
    public ResultSet executeQuery(Statement stmt, String sql) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("executeQuery");
        }
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(sql);
        } catch (SQLException throwAbles) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(throwAbles.getMessage());
            }
        }
        return rs;
    }

    /**
     * close
     *
     * @param st Statement
     */
    public void close(Statement st) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("close");
        }
        close(st, null, null);
    }

    /**
     * close
     *
     * @param rs Result set
     */
    public void close(ResultSet rs) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("close");
        }
        close(null, rs, null);
    }

    /**
     * close
     *
     * @param con Connection
     */
    public void close(Connection con) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("close");
        }
        close(null, null, con);
    }

    /**
     * close
     *
     * @param st Statement
     * @param con Connection
     */
    public void close(Statement st, Connection con) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("close");
        }
        close(st, null, con);
    }

    /**
     * close
     *
     * @param rs Result set
     * @param con Connection
     */
    public void close(ResultSet rs, Connection con) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("close");
        }
        close(null, rs, con);
    }

    /**
     * close
     *
     * @param st Statement
     * @param rs Result set
     * @param con Connection
     */
    public void close(Statement st, ResultSet rs, Connection con) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("close");
        }
        if (st != null) {
            try {
                st.close();
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error(exception.getMessage());
                }
            }
        }
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error(exception.getMessage());
                }
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error(exception.getMessage());
                }
            }
        }
    }
}
