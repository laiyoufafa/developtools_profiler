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

package ohos.devtools.services.hiperf;

import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Database operation class
 *
 * @since 2021/8/25
 */
public final class PerfDAO {
    private static final Logger LOGGER = LogManager.getLogger(PerfDAO.class);
    private static volatile PerfDAO db = new PerfDAO();

    private String dbName;
    private LinkedBlockingQueue<Connection> pool = new LinkedBlockingQueue();

    private PerfDAO() {
    }

    /**
     * Get the current current db object
     *
     * @return Db
     */
    public static PerfDAO getInstance() {
        if (db == null) {
            db = new PerfDAO();
        }
        return db;
    }

    /**
     * Get database connection
     *
     * @return Connection
     */
    public Connection getConn() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getConn");
        }
        Connection connection = null;
        try {
            connection = pool.take();
        } catch (InterruptedException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("getConn InterruptedException error: ", exception);
            }
        }
        return connection;
    }

    /**
     * Return the connection to the database connection pool after use
     *
     * @param conn conn
     */
    public void free(final Connection conn) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("free");
        }
        try {
            pool.put(conn);
        } catch (InterruptedException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(exception.getMessage());
            }
        }
    }

    private Optional<Connection> newConn() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("newConn");
        }
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
        } catch (SQLException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(exception.getMessage());
            }
        }
        return Optional.ofNullable(conn);
    }

    /**
     * create table
     *
     * @param timeStamp timeStamp
     */
    public void createTable(String timeStamp) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createTable");
        }
        if (timeStamp != null) {
            dbName = SessionManager.getInstance().tempPath() + "perf%s.db";
            dbName = String.format(Locale.ENGLISH, dbName, timeStamp);
        } else {
            dbName = SessionManager.getInstance().tempPath() + "perf.db";
        }
        final int maxConnNum = 10;
        try {
            Class.forName("org.sqlite.JDBC");
            for (Connection connection : db.pool) {
                connection.close();
            }
            db.pool.clear();
            for (int size = 0; size < maxConnNum; size++) {
                db.newConn().ifPresent(connection -> {
                    try {
                        db.pool.put(connection);
                    } catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                });
            }
            executeStatement();
        } catch (SQLException | ClassNotFoundException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(exception.getMessage());
            }
        }
    }

    /**
     * execute Statement
     */
    private static void executeStatement() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("executeStatement");
        }
        db.newConn().ifPresent(connection -> {
            try {
                Statement statement = connection.createStatement();
                statement.execute("drop table if exists 'perf_sample'");
                statement.execute("drop table if exists 'perf_callchain'");
                statement.execute("drop table if exists 'perf_files'");
                statement.execute("drop table if exists 'perf_thread'");
                statement.execute(
                    "create table perf_sample("
                        + "id integer primary key autoincrement,"
                        + "sample_id integer,"
                        + "timestamp integer,"
                        + "thread_id integer,"
                        + "event_count integer,"
                        + "event_type_id integer)");
                statement.execute(
                    "create table perf_callchain("
                        + "id integer primary key autoincrement,"
                        + "sample_id integer,"
                        + "callchain_id integer,"
                        + "vaddr_in_file integer,"
                        + "file_id integer,"
                        + "symbol_id integer)");
                statement.execute(
                    "create table perf_files("
                        + "id integer primary key autoincrement,"
                        + "file_id integer,"
                        + "symbol text,"
                        + "path text)");
                statement.execute(
                    "create table perf_thread("
                        + "id integer primary key autoincrement,"
                        + "thread_id integer,"
                        + "process_id integer,"
                        + "thread_name text)");
                statement.close();
                connection.close();
            } catch (SQLException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error(exception.getMessage());
                }
            }
        });
    }
}
