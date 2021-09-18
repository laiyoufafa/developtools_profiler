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
 * @date: 2021/4/24 17:52
 */
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