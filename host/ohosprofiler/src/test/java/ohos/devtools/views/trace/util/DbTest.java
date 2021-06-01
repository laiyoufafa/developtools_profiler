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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * test Db class .
 *
 * @version 1.0
 * @date: 2021/4/24 17:52
 **/
class DbTest {
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
     * test function the getConn .
     */
    @Test
    void getConn() {
        Connection conn = Db.getInstance().getConn();
        assertEquals(true, conn != null);
    }

    /**
     * test function the free .
     */
    @Test
    void free() {
        Connection conn = Db.getInstance().getConn();
        Db.getInstance().free(conn);
        assertEquals(true, conn != null);
    }

    /**
     * test function the load .
     */
    @Test
    void load() {
        Db.load(false);
        assertEquals(true, Db.getInstance() != null);
    }

    /**
     * test function the getInstance .
     */
    @Test
    void getInstance() {
        assertEquals(true, Db.getInstance() != null);
    }

    /**
     * test function the getSql .
     */
    @Test
    void getSql() {
        assertEquals(true, Db.getSql("QueryTotalTime") != null);
    }

    /**
     * test function the queryTotalTime .
     */
    @Test
    void queryTotalTime() {
        long queryTotalTime = Db.getInstance().queryTotalTime();
        assertEquals(true, queryTotalTime > 0);
    }

    /**
     * test function the queryCpu .
     */
    @Test
    void queryCpu() {
        assertEquals(true, Db.getInstance().queryCpu() != null);
    }

    /**
     * test function the queryCpuFreq .
     */
    @Test
    void queryCpuFreq() {
        assertEquals(true, Db.getInstance().queryCpuFreq() != null);
    }

    /**
     * test function the queryCpuMax .
     */
    @Test
    void queryCpuMax() {
        assertEquals(true, Db.getInstance().queryCpuMax() >= 0);
    }

    /**
     * test function the queryCpuData .
     */
    @Test
    void queryCpuData() {
        assertEquals(true, Db.getInstance().queryCpuData(0) != null);
    }

    /**
     * test function the queryCpuMaxFreq .
     */
    @Test
    void queryCpuMaxFreq() {
        assertEquals(true, Db.getInstance().queryCpuMaxFreq() != null);
    }

    /**
     * test function the queryCpuFreqData .
     */
    @Test
    void queryCpuFreqData() {
        assertEquals(true, Db.getInstance().queryCpuFreqData(0) != null);
    }

    /**
     * test function the queryProcess .
     */
    @Test
    void queryProcess() {
        assertEquals(true, Db.getInstance().queryProcess() != null);
    }

    /**
     * test function the queryProcessThreads .
     */
    @Test
    void queryProcessThreads() {
        assertEquals(true, Db.getInstance().queryProcessThreads() != null);
    }

    /**
     * test function the queryProcessData .
     */
    @Test
    void queryProcessData() {
        assertEquals(true, Db.getInstance().queryProcessData(0) != null);
    }

    /**
     * test function the queryThreadData .
     */
    @Test
    void queryThreadData() {
        assertEquals(true, Db.getInstance().queryThreadData(0) != null);
    }

    /**
     * test function the queryWakeupThread .
     */
    @Test
    void queryWakeupThread() {
        assertEquals(null, Db.getInstance().queryWakeupThread(null));
    }

    /**
     * test function the getCpuUtilizationRate .
     */
    @Test
    void getCpuUtilizationRate() {
        assertEquals(true, Db.getInstance().getCpuUtilizationRate() != null);
    }

    /**
     * test function the getProcessMem .
     */
    @Test
    void getProcessMem() {
        assertEquals(true, Db.getInstance().getProcessMem() != null);
    }

    /**
     * test function the getProcessMemData .
     */
    @Test
    void getProcessMemData() {
        assertEquals(true, Db.getInstance().getProcessMemData(0) != null);
    }

    /**
     * test function the getFunDataByTid .
     */
    @Test
    void getFunDataByTid() {
        assertEquals(true, Db.getInstance().getFunDataByTid(0) != null);
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

    /**
     * set down .
     */
    @AfterEach
    void tearDown() {
    }
}