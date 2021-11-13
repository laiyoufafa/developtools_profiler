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

package ohos.devtools.views.trace.util;

import ohos.devtools.views.trace.Sql;
import ohos.devtools.views.trace.bean.AsyncEvent;
import ohos.devtools.views.trace.bean.Clock;
import ohos.devtools.views.trace.bean.Process;
import ohos.devtools.views.trace.bean.ProcessMem;
import ohos.devtools.views.trace.bean.ThreadData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static ohos.devtools.Config.TRACE_SYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * test Db class .
 *
 * @since 2021/4/24 17:52
 */
class DbTest {
    private static String dbPath = TRACE_SYS;

    /**
     * test function the load .
     */
    @Test
    void load() {
        Db.load(false);
        assertEquals(true, Db.getInstance() != null);
    }

    @Test
    void loadTrue() throws NoSuchFieldException, IllegalAccessException {
        Db.load(false);
        Db instance = Db.getInstance();
        final Field field = instance.getClass().getDeclaredField("isLocal");
        field.setAccessible(true);
        assertFalse((boolean) field.get(instance));
    }

    @Test
    void loadTrueNoPath() throws NoSuchFieldException, IllegalAccessException {
        Db.load(true);
        Db instance = Db.getInstance();
        final Field field = instance.getClass().getDeclaredField("isLocal");
        field.setAccessible(true);
        assertTrue((boolean) field.get(instance));
    }

    @Test
    void loadTrueEmptyPath() throws NoSuchFieldException, IllegalAccessException {
        Db.setDbName("");
        Db.load(true);
        Db instance = Db.getInstance();
        final Field field = instance.getClass().getDeclaredField("isLocal");
        field.setAccessible(true);
        assertTrue((boolean) field.get(instance));
    }

    @Test
    void loadFalse() throws NoSuchFieldException, IllegalAccessException {
        Db.setDbName("");
        Db.load(false);
        Db instance = Db.getInstance();
        final Field field = instance.getClass().getDeclaredField("isLocal");
        field.setAccessible(true);
        assertFalse((boolean) field.get(instance));
    }

    /**
     * test function the getSql .
     */
    @Test
    void getSql() {
        assertEquals(true, Db.getSql("QueryTotalTime") != null);
    }

    /**
     * test function the getSql .
     */
    @Test
    void getSqlTotal() {
        String sql = Db.getSql(Sql.DISTRIBUTED_QUERY_TOTAL_TIME.getName());
        assertNotNull(sql);
    }

    @Test
    void getSqlTid() {
        String sql = Db.getSql(Sql.DISTRIBUTED_GET_FUN_DATA_BY_TID.getName());
        assertNotNull(sql);
    }

    @Test
    void getSqlThread() {
        String sql = Db.getSql(Sql.DISTRIBUTED_QUERY_THREADS_BY_PID.getName());
        assertNotNull(sql);
    }

    @Test
    void getSqlCpu() {
        String sql = Db.getSql(Sql.DISTRIBUTED_TRACE_CPU.getName());
        assertNotNull(sql);
    }

    @Test
    void getConn() {
        Db.setDbName(dbPath);
        Db.load(true);
        Db instance = Db.getInstance();
        assertNotNull(instance.getConn());
    }

    @Test
    void getConnNoPath() {
        Db.load(true);
        Db instance = Db.getInstance();
        assertNotNull(instance.getConn());
    }

    @Test
    void getConnAEmptyPath() {
        Db.setDbName(dbPath);
        Db.load(true);
        Db instance = Db.getInstance();
        assertNotNull(instance.getConn());
    }

    @Test
    void getConnEmptyPath() {
        Db.setDbName(dbPath);
        Db.load(true);
        Db instance = Db.getInstance();
        assertNotNull(instance.getConn());
    }

    @Test
    void getConnWrongPath() {
        Db.setDbName("");
        Db.load(true);
        Db instance = Db.getInstance();
        assertNotNull(instance.getConn());
    }

    /**
     * test function the getDbName .
     */
    @Test
    void getDbName() {
        assertEquals("trace.db", Db.getDbName());
    }

    /**
     * test function the setDbName .
     */
    @Test
    void setDbName() {
        Db.setDbName("trace.db");
        assertEquals("trace.db", Db.getDbName());
    }

    /**
     * test function the free .
     */
    @Test
    void free() {
        Db.setDbName(dbPath);
        Db.load(true);
        Db instance = Db.getInstance();
        Connection connA = instance.getConn();
        instance.free(connA);
        assertNotNull(connA);
    }

    @Test
    void freeCreate() {
        Db.setDbName(dbPath);
        Db.load(true);
        Db instance = Db.getInstance();
        Connection conn = createNewConnection(dbPath);
        instance.free(conn);
        assertNotNull(conn);
    }

    @Test
    void freeNew() {
        Db.setDbName(dbPath);
        Db.load(true);
        Db instance = Db.getInstance();
        Connection connB = createNewConnection(dbPath);
        if (connB != null) {
            instance.free(connB);
            assertNotNull(connB);
        }
    }

    @Test
    void freePutTake() {
        Db.setDbName(dbPath);
        Db.load(true);
        Db instance = Db.getInstance();
        Connection connA = instance.getConn();
        instance.free(connA);
        Connection connAagain = instance.getConn();
        instance.free(connAagain);
        assertNotNull(connAagain);
    }

    @Test
    void freeNewPutTake() {
        Db.setDbName(dbPath);
        Db.load(true);
        Db instance = Db.getInstance();
        Connection connA = createNewConnection(dbPath);
        if (connA != null) {
            instance.free(connA);
            assertNotNull(connA);
            Connection connAagain = instance.getConn();
            instance.free(connAagain);
            assertNotNull(connAagain);
        }
    }

    @Test
    void queryProcess() {
        Db.setDbName(dbPath);
        List<Process> processes = new ArrayList<>() {
        };
        Db.load(true);
        Db instance = Db.getInstance();
        instance.query(Sql.SYS_QUERY_PROCESS, processes);
        assertNotNull(processes);
    }

    @Test
    void queryProcessMem() {
        List<ProcessMem> processMem = new ArrayList<>() {
        };
        Db.setDbName(dbPath);
        Db.load(true);
        Db instance = Db.getInstance();
        instance.query(Sql.SYS_GET_PROCESS_MEM, processMem);
        assertNotNull(processMem);
    }

    @Test
    void queryClocks() {
        Db.setDbName(dbPath);
        ArrayList<Clock> clocks = new ArrayList<>() {
        };
        Db.load(true);
        Db instance = Db.getInstance();
        instance.query(Sql.SYS_QUERY_CLOCK_LIST, clocks);
        assertNotNull(clocks);
    }

    @Test
    void queryProcessThreads() {
        Db.setDbName(dbPath);
        List<ThreadData> processThreads = new ArrayList<>() {
        };
        Db.load(true);
        Db instance = Db.getInstance();
        instance.query(Sql.SYS_QUERY_PROCESS_THREADS, processThreads);
        assertNotNull(processThreads);
    }

    @Test
    void queryAsyncEvents() {
        Db.setDbName(dbPath);
        List<AsyncEvent> asyncEvents = new ArrayList<>() {
        };
        Db.load(true);
        Db instance = Db.getInstance();
        instance.query(Sql.SYS_GET_ASYNC_EVENTS, asyncEvents);
        assertNotNull(asyncEvents);
    }

    @Test
    void queryCount() {
        Db.setDbName(dbPath);
        Db.load(true);
        int index = Db.getInstance().queryCount(Sql.SYS_QUERY_CPU_DATA_COUNT, 1, 0, 0);
        assertEquals(index, 0);
    }

    @Test
    void queryCountNegative() {
        Db.setDbName(dbPath);
        Db.load(true);
        int index = Db.getInstance().queryCount(Sql.SYS_QUERY_CPU_DATA_COUNT, 1, -1, -1);
        assertEquals(index, 0);
    }

    @Test
    void queryCountMax() {
        Db.setDbName(dbPath);
        Db.load(true);
        int index = Db.getInstance().queryCount(Sql.SYS_QUERY_CPU_DATA_COUNT, 1, Integer.MAX_VALUE, -1);
        assertEquals(index, 0);
    }

    @Test
    void queryCountMin() {
        Db.setDbName(dbPath);
        Db.load(true);
        int index = Db.getInstance().queryCount(Sql.SYS_QUERY_CPU_DATA_COUNT, 1, Integer.MIN_VALUE, -1);
        assertEquals(index, 0);
    }

    @Test
    void queryCountNormal() {
        Db.setDbName(dbPath);
        Db.load(true);
        int index = Db.getInstance().queryCount(Sql.SYS_QUERY_CPU_DATA_COUNT, 1, 0, 1000);
        assertTrue(index >= 0);
    }

    @Test
    void querySqlCount() {
        Db.setDbName(dbPath);
        Db.load(true);
        String sql = String.format(Locale.ENGLISH, Db.getSql(Sql.SYS_QUERY_CPU_DATA_COUNT.getName()), 1, 0, 0);
        int index = Db.getInstance().queryCount(sql);
        assertEquals(index, 0);
    }

    @Test
    void querySqlCountNegative() {
        Db.setDbName(dbPath);
        Db.load(true);
        String sql = String.format(Locale.ENGLISH, Db.getSql(Sql.SYS_QUERY_CPU_DATA_COUNT.getName()), 1, -1, -1);
        int index = Db.getInstance().queryCount(sql);
        assertEquals(index, 0);
    }

    @Test
    void querySqlCountMax() {
        Db.setDbName(dbPath);
        Db.load(true);
        String sql =
            String.format(Locale.ENGLISH, Db.getSql(Sql.SYS_QUERY_CPU_DATA_COUNT.getName()), 1, Integer.MAX_VALUE, -1);
        int index = Db.getInstance().queryCount(sql);
        assertEquals(index, 0);
    }

    @Test
    void querySqlCountMin() {
        Db.setDbName(dbPath);
        Db.load(true);
        String sql =
            String.format(Locale.ENGLISH, Db.getSql(Sql.SYS_QUERY_CPU_DATA_COUNT.getName()), 1, Integer.MIN_VALUE, -1);
        int index = Db.getInstance().queryCount(sql);
        assertEquals(index, 0);
    }

    @Test
    void querySqlCountNormal() {
        Db.setDbName(dbPath);
        Db.load(true);
        String sql = String.format(Locale.ENGLISH, Db.getSql(Sql.SYS_QUERY_CPU_DATA_COUNT.getName()), 1, 0, 1000);
        int index = Db.getInstance().queryCount(Sql.SYS_QUERY_CPU_DATA_COUNT, 1, 0, 1000);
        assertTrue(index >= 0);
    }

    /**
     * test function the getInstance .
     */
    @Test
    void getInstance() {
        assertEquals(true, Db.getInstance() != null);
    }

    /**
     * init the db setup .
     */
    @BeforeEach
    void setUp() {
        Db.getInstance();
        Db.setDbName("trace.db");
        Db.load(false);
    }

    private Connection createNewConnection(String path) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + path);
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return conn;
    }
}