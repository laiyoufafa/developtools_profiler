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

package ohos.devtools.views.distributed.util;

import ohos.devtools.Config;
import ohos.devtools.views.distributed.statistics.bean.Cpu;
import ohos.devtools.views.distributed.statistics.bean.MemAgg;
import ohos.devtools.views.distributed.statistics.bean.Memory;
import ohos.devtools.views.distributed.statistics.bean.Metadata;
import ohos.devtools.views.distributed.statistics.bean.Stats;
import ohos.devtools.views.trace.Sql;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DistributedDBTest {
    private static String dbAPath = Config.TRACE_DISTRIBUTED_A;

    private static String dbBPath = Config.TRACE_DISTRIBUTED_B;

    private long startTime = 0L;

    @Test
    void load() throws NoSuchFieldException, IllegalAccessException {
        DistributedDB.setDbName(dbAPath, dbBPath);
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        final Field field = instance.getClass().getDeclaredField("isLocal");
        field.setAccessible(true);
        assertTrue((boolean) field.get(instance));
    }

    @Test
    void loadTrue() throws NoSuchFieldException, IllegalAccessException {
        DistributedDB.setDbName(dbAPath, dbBPath);
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        final Field field = instance.getClass().getDeclaredField("isLocal");
        field.setAccessible(true);
        assertTrue((boolean) field.get(instance));
    }

    @Test
    void loadTrueNoPath() throws NoSuchFieldException, IllegalAccessException {
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        final Field field = instance.getClass().getDeclaredField("isLocal");
        field.setAccessible(true);
        assertTrue((boolean) field.get(instance));
    }

    @Test
    void loadTrueEmptyPath() throws NoSuchFieldException, IllegalAccessException {
        DistributedDB.setDbName("", dbBPath);
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        final Field field = instance.getClass().getDeclaredField("isLocal");
        field.setAccessible(true);
        assertTrue((boolean) field.get(instance));
    }

    @Test
    void loadFalse() throws NoSuchFieldException, IllegalAccessException {
        DistributedDB.setDbName("", dbBPath);
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        final Field field = instance.getClass().getDeclaredField("isLocal");
        field.setAccessible(true);
        assertTrue((boolean) field.get(instance));
    }

    @Test
    void getSqlTotal() {
        String sql = DistributedDB.getSql(Sql.DISTRIBUTED_QUERY_TOTAL_TIME.getName());

        assertNotNull(sql);
    }

    @Test
    void getSqlTid() {
        String sql = DistributedDB.getSql(Sql.DISTRIBUTED_GET_FUN_DATA_BY_TID.getName());
        assertNotNull(sql);
    }

    @Test
    void getSqlThread() {
        String sql = DistributedDB.getSql(Sql.DISTRIBUTED_QUERY_THREADS_BY_PID.getName());
        assertNotNull(sql);
    }

    @Test
    void getSqlCpu() {
        String sql = DistributedDB.getSql(Sql.DISTRIBUTED_TRACE_CPU.getName());
        assertNotNull(sql);
    }

    @Test
    void getSql() {
        String sql = DistributedDB.getSql(Sql.DISTRIBUTED_TRACE_MEM.getName());
        assertNotNull(sql);
    }

    @Test
    void getConnA() {
        DistributedDB.setDbName(dbAPath, dbBPath);
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        assertNotNull(instance.getConnA());
    }

    @Test
    void getConnANoPath() {
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        assertNotNull(instance.getConnA());
    }

    @Test
    void getConnAEmptyPath() {
        DistributedDB.setDbName("", dbBPath);
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        assertNotNull(instance.getConnA());
    }

    @Test
    void getConnABEmptyPath() {
        DistributedDB.setDbName("", "");
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        assertNotNull(instance.getConnA());
    }

    @Test
    void getConnAWrongPath() {
        DistributedDB.setDbName(dbBPath, dbAPath);
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        assertNotNull(instance.getConnA());
    }

    @Test
    void getConnB() {
        DistributedDB.setDbName(dbAPath, dbBPath);
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        assertNotNull(instance.getConnB());
    }

    @Test
    void getConnBNoPath() {
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        assertNotNull(instance.getConnB());
    }

    @Test
    void getConnBEmptyPath() {
        DistributedDB.setDbName("", dbBPath);
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        assertNotNull(instance.getConnB());
    }

    @Test
    void getConnBAEmptyPath() {
        DistributedDB.setDbName("", "");
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        assertNotNull(instance.getConnB());
    }

    @Test
    void getConnBWrongPath() {
        DistributedDB.setDbName(dbBPath, dbAPath);
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        assertNotNull(instance.getConnB());
    }

    @Test
    void freeA() {
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        Connection connA = instance.getConnA();
        instance.freeA(connA);
        assertNotNull(connA);
    }

    @Test
    void freeAFromB() {
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        Connection connB = instance.getConnB();
        instance.freeA(connB);
        assertNotNull(connB);
    }

    @Test
    void freeANew() {
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        Connection connB = createNewConnection(dbAPath);
        if (connB != null) {
            instance.freeA(connB);
            assertNotNull(connB);
        }
    }

    @Test
    void freeAPutTake() {
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        Connection connA = instance.getConnA();
        instance.freeA(connA);
        Connection connAagain = instance.getConnA();
        instance.freeA(connAagain);
        assertNotNull(connAagain);
    }

    @Test
    void freeANewPutTake() {
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        Connection connA = createNewConnection(dbAPath);
        if (connA != null) {
            instance.freeA(connA);
            assertNotNull(connA);
            Connection connAagain = instance.getConnA();
            instance.freeA(connAagain);
            assertNotNull(connAagain);
        }
    }

    @Test
    void freeB() {
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        Connection connB = instance.getConnB();
        instance.freeA(connB);
        assertNotNull(connB);
    }

    @Test
    void freeBFromA() {
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        Connection connA = instance.getConnA();
        instance.freeB(connA);
        assertNotNull(connA);
    }

    @Test
    void freeBNew() {
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        Connection connB = createNewConnection(dbBPath);
        if (connB != null) {
            instance.freeB(connB);
            assertNotNull(connB);
        }
    }

    @Test
    void freeBPutTake() {
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        Connection connB = instance.getConnB();
        instance.freeA(connB);
        assertNotNull(connB);
        Connection connBagain = instance.getConnB();
        instance.freeA(connBagain);
        assertNotNull(connBagain);
    }

    @Test
    void freeBNewPutTake() {
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        Connection connB = createNewConnection(dbBPath);
        if (connB != null) {
            instance.freeA(connB);
            assertNotNull(connB);
            Connection connBagain = instance.getConnB();
            instance.freeA(connBagain);
            assertNotNull(connBagain);
        }
    }

    @Test
    void queryA() {
        List<Cpu> cpuList = new ArrayList<>() {
        };
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        instance.queryA(Sql.DISTRIBUTED_TRACE_CPU, cpuList);
        assertNotNull(cpuList);
    }

    @Test
    void queryAMemAggList() {
        List<MemAgg> list = new ArrayList<>() {
        };
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        instance.queryA(Sql.DISTRIBUTED_TRACE_MEM_UNAGG, list);
        assertNotNull(list);
    }

    @Test
    void queryAStatsList() {
        List<Stats> list = new ArrayList<>() {
        };
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        instance.queryA(Sql.DISTRIBUTED_TRACE_STATS, list);
        assertNotNull(list);
    }

    @Test
    void queryAMemoryList() {
        DistributedDB.setDbName(dbAPath, dbBPath);
        List<Memory> list = new ArrayList<>() {
        };
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        instance.queryA(Sql.DISTRIBUTED_TRACE_MEM, list);
        assertNotNull(list);
    }

    @Test
    void queryAMetadataList() {
        DistributedDB.setDbName(dbAPath, dbBPath);
        List<Metadata> list = new ArrayList<>() {
        };
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        instance.queryA(Sql.DISTRIBUTED_TRACE_METADATA, list);
        assertNotNull(list);
    }

    @Test
    void queryB() {
        List<Cpu> cpuList = new ArrayList<>() {
        };
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        instance.queryB(Sql.DISTRIBUTED_TRACE_CPU, cpuList);
        assertNotNull(cpuList);
    }

    @Test
    void queryBMemAggList() {
        List<MemAgg> list = new ArrayList<>() {
        };
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        instance.queryB(Sql.DISTRIBUTED_TRACE_MEM_UNAGG, list);
        assertNotNull(list);
    }

    @Test
    void queryBStatsList() {
        List<Stats> list = new ArrayList<>() {
        };
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        instance.queryB(Sql.DISTRIBUTED_TRACE_STATS, list);
        assertNotNull(list);
    }

    @Test
    void queryBMemoryList() {
        List<Memory> list = new ArrayList<>() {
        };
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        instance.queryB(Sql.DISTRIBUTED_TRACE_MEM, list);
        assertNotNull(list);
    }

    @Test
    void queryBMetadataList() {
        List<Metadata> list = new ArrayList<>() {
        };
        DistributedDB.load(true);
        DistributedDB instance = DistributedDB.getInstance();
        instance.queryB(Sql.DISTRIBUTED_TRACE_METADATA, list);
        assertNotNull(list);
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

    @AfterAll
    static void end() {
        DistributedDB.setDbName(dbAPath, dbBPath);
        DistributedDB.load(true);
    }
}