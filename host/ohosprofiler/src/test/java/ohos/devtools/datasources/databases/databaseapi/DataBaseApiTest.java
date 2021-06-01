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

package ohos.devtools.datasources.databases.databaseapi;

import com.alibaba.druid.pool.DruidDataSource;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @version 1.0
 * @date 2021/03/22 16:07
 **/
public class DataBaseApiTest {
    private List<String> processMemInfo;
    private List<String> processMemInfoIndex;

    /**
     * functional testing init
     *
     * @tc.name: setUp
     * @tc.number: OHOS_JAVA_database_DataBaseApi_setUp_0001
     * @tc.desc: setUp
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Before
    public void setUp() {
        processMemInfo = new ArrayList() {
            {
                add("id INTEGER primary key autoincrement not null");
                add("sessionId INTEGER NOT NULL");
                add("session INTEGER NOT NULL");
                add("timeStamp Long NOT NULL");
                add("Data BLOB NOT NULL");
            }
        };
        processMemInfoIndex = new ArrayList() {
            {
                add("id");
                add("sessionId");
                add("timeStamp");
            }
        };
        SessionManager.getInstance().setDevelopMode(true);
    }

    /**
     * functional testing init
     *
     * @tc.name: init DataSourceManager
     * @tc.number: OHOS_JAVA_database_DataBaseApi_initDataSourceManager_0001
     * @tc.desc: init DataSourceManager
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void initDataSourceManager() {
        try {
            DataBaseApi.getInstance().initDataSourceManager();
            Assert.assertTrue(true);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing init
     *
     * @tc.name: init DataSourceManager
     * @tc.number: OHOS_JAVA_database_DataBaseApi_initDataSourceManager_0002
     * @tc.desc: init DataSourceManager
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void initDataSourceManager02() {
        try {
            DataBaseApi.getInstance().initDataSourceManager();
            DataBaseApi.getInstance().initDataSourceManager();
            Assert.assertTrue(true);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing get Connect
     *
     * @tc.name: get DefaultDataBase Connect
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getDefaultDataBaseConnect_0001
     * @tc.desc: get DefaultDataBase Connect
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getDefaultDataBaseConnect01() {
        Optional<Connection> res = null;
        try {
            DataBaseApi.getInstance().initDataSourceManager();
            res = DataBaseApi.getInstance().getDefaultDataBaseConnect();
            Assert.assertTrue(res.isPresent());
        } catch (Exception exception) {
            Assert.assertTrue(res.isPresent());
        }
    }

    /**
     * functional testing get Connect
     *
     * @tc.name: get DefaultDataBase Connect
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getDefaultDataBaseConnect_0002
     * @tc.desc: get DefaultDataBase Connect
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getDefaultDataBaseConnect02() {
        Optional<Connection> res = null;
        try {
            DataBaseApi.getInstance().initDataSourceManager();
            res = DataBaseApi.getInstance().getDefaultDataBaseConnect();
            Assert.assertTrue(res.isPresent());
            Connection conn = res.get();
            Assert.assertNotNull(conn);
        } catch (Exception exception) {
            Assert.assertTrue(res.isPresent());
        }
    }

    /**
     * functional testing get Connect
     *
     * @tc.name: get DefaultDataBase Connect
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getDefaultDataBaseConnect_0003
     * @tc.desc: get DefaultDataBase Connect
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getDefaultDataBaseConnect03() {
        Optional<Connection> res = null;
        try {
            res = DataBaseApi.getInstance().getDefaultDataBaseConnect();
            Assert.assertTrue(res.isPresent());
        } catch (Exception exception) {
            Assert.assertTrue(res.isPresent());
        }
    }

    /**
     * functional testing get Connect
     *
     * @tc.name: get DefaultDataBase Connect
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getDefaultDataBaseConnect_0004
     * @tc.desc: get DefaultDataBase Connect
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getDefaultDataBaseConnect04() {
        Optional<Connection> res = null;
        try {
            res = DataBaseApi.getInstance().getDefaultDataBaseConnect();
            Assert.assertTrue(res.isPresent());
            Connection cc = res.get();
            Assert.assertNotNull(cc);
        } catch (Exception exception) {
            Assert.assertTrue(res.isPresent());
        }
    }

    /**
     * functional testing get Connect
     *
     * @tc.name: get Connect By Table
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getConnectByTable_0001
     * @tc.desc: get Connect By Table
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getConnectByTableTest01() {
        Optional<Connection> res = null;
        try {
            DataBaseApi.getInstance().initDataSourceManager();
            res = DataBaseApi.getInstance().getConnectByTable("DeviceInfo");
            Assert.assertTrue(res.isPresent());
        } catch (Exception exception) {
            Assert.assertTrue(res.isPresent());
        }
    }

    /**
     * functional testing get Connect
     *
     * @tc.name: get Connect By Table
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getConnectByTable_0002
     * @tc.desc: get Connect By Table
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getConnectByTableTest02() {
        Optional<Connection> res = null;
        try {
            DataBaseApi.getInstance().initDataSourceManager();
            res = DataBaseApi.getInstance().getConnectByTable("");
            Assert.assertFalse(res.isPresent());
        } catch (Exception exception) {
            Assert.assertTrue(res.isPresent());
        }
    }

    /**
     * functional testing get Connect
     *
     * @tc.name: get Connect By Table
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getConnectByTable_0003
     * @tc.desc: get Connect By Table
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getConnectByTableTest03() {
        Optional<Connection> res = null;
        try {
            DataBaseApi.getInstance().initDataSourceManager();
            res = DataBaseApi.getInstance().getConnectByTable(null);
            Assert.assertFalse(res.isPresent());
        } catch (Exception exception) {
            Assert.assertTrue(res.isPresent());
        }
    }

    /**
     * functional testing get Connect
     *
     * @tc.name: get Connect By Table
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getConnectByTable_0004
     * @tc.desc: get Connect By Table
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getConnectByTableTest04() {
        Optional<Connection> res = null;
        try {
            res = DataBaseApi.getInstance().getConnectByTable("DeviceInfo");
            Assert.assertTrue(res.isPresent());
        } catch (Exception exception) {
            Assert.assertTrue(res.isPresent());
        }
    }

    /**
     * functional testing get Connect
     *
     * @tc.name: get Connect By Table
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getConnectByTable_0005
     * @tc.desc: get Connect By Table
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getConnectByTableTest05() {
        Optional<Connection> res = null;
        try {
            res = DataBaseApi.getInstance().getConnectByTable("");
            Assert.assertFalse(res.isPresent());
        } catch (Exception exception) {
            Assert.assertTrue(res.isPresent());
        }
    }

    /**
     * functional testing get Connect
     *
     * @tc.name: get Connect By dbname
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getConnectBydbname_0001
     * @tc.desc: get Connect By dbname
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getConnectBydbnameTest01() {
        Optional<Connection> res = DataBaseApi.getInstance().getConnectBydbname("defaultDB");
        Assert.assertTrue(res.isPresent());
    }

    /**
     * functional testing get Connect
     *
     * @tc.name: get Connect By dbname
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getConnectBydbname_0002
     * @tc.desc: get Connect By dbname
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getConnectBydbnameTest02() {
        Optional<Connection> res = null;
        try {
            res = DataBaseApi.getInstance().getConnectBydbname("defaultDB");
            Assert.assertTrue(res.isPresent());
        } catch (Exception exception) {
            Assert.assertTrue(res.isPresent());
        }
    }

    /**
     * functional testing get Connect
     *
     * @tc.name: get Connect By dbname
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getConnectBydbname_0003
     * @tc.desc: get Connect By dbname
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getConnectBydbnameTest03() {
        Optional<Connection> res = null;
        try {
            res = DataBaseApi.getInstance().getConnectBydbname("test");
            Assert.assertTrue(res.isPresent());
        } catch (Exception exception) {
            Assert.assertTrue(res.isPresent());
        }
    }

    /**
     * functional testing get Connect
     *
     * @tc.name: get Connect By dbname
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getConnectBydbname_0004
     * @tc.desc: get Connect By dbname
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getConnectBydbnameTest04() {
        Optional<Connection> res = null;
        try {
            res = DataBaseApi.getInstance().getConnectBydbname("test");
            Assert.assertTrue(res.isPresent());
            res = DataBaseApi.getInstance().getConnectBydbname("test");
            Assert.assertTrue(res.isPresent());
        } catch (Exception exception) {
            Assert.assertTrue(res.isPresent());
        }
    }

    /**
     * functional testing get Connect
     *
     * @tc.name: get Connect By dbname
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getConnectBydbname_0005
     * @tc.desc: get Connect By dbname
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getConnectBydbnameTest05() {
        Optional<Connection> res = null;
        try {
            res = DataBaseApi.getInstance().getConnectBydbname("defaultDB");
            Assert.assertTrue(res.isPresent());
            res = DataBaseApi.getInstance().getConnectBydbname("test");
            Assert.assertTrue(res.isPresent());
        } catch (Exception exception) {
            Assert.assertTrue(res.isPresent());
        }
    }

    /**
     * functional testing get DataBaseName
     *
     * @tc.name: get Pool By DataBaseName
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getPoolByDataBaseName_0001
     * @tc.desc: get Pool By DataBaseName
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getPoolByDataBaseNameTest01() {
        DataSource res = null;
        try {
            res = DataBaseApi.getInstance().getPoolByDataBaseName("");
            Assert.assertNotNull(res);
        } catch (Exception exception) {
            Assert.assertNotNull(res);
        }
    }

    /**
     * functional testing get DataBaseName
     *
     * @tc.name: get Pool By DataBaseName
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getPoolByDataBaseName_0002
     * @tc.desc: get Pool By DataBaseName
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getPoolByDataBaseNameTest02() {
        DataSource res = null;
        try {
            res = DataBaseApi.getInstance().getPoolByDataBaseName(null);
            Assert.assertNotNull(res);
        } catch (Exception exception) {
            Assert.assertNotNull(res);
        }
    }

    /**
     * functional testing get DataBaseName
     *
     * @tc.name: get Pool By DataBaseName
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getPoolByDataBaseName_0003
     * @tc.desc: get Pool By DataBaseName
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getPoolByDataBaseNameTest03() {
        DataSource res = null;
        try {
            res = DataBaseApi.getInstance().getPoolByDataBaseName("test");
            Assert.assertNotNull(res);
        } catch (Exception exception) {
            Assert.assertNotNull(res);
        }
    }

    /**
     * functional testing get DataBaseName
     *
     * @tc.name: get Pool By DataBaseName
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getPoolByDataBaseName_0004
     * @tc.desc: get Pool By DataBaseName
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getPoolByDataBaseNameTest04() {
        DataSource res = null;
        try {
            res = DataBaseApi.getInstance().getPoolByDataBaseName("test");
            Assert.assertNotNull(res);
            res = DataBaseApi.getInstance().getPoolByDataBaseName("test");
            Assert.assertNotNull(res);
        } catch (Exception exception) {
            Assert.assertNotNull(res);
        }
    }

    /**
     * functional testing get DataBaseName
     *
     * @tc.name: get Pool By DataBaseName
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getPoolByDataBaseName_0005
     * @tc.desc: get Pool By DataBaseName
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getPoolByDataBaseNameTest05() {
        DataSource res = null;
        try {
            res = DataBaseApi.getInstance().getPoolByDataBaseName(null);
            Assert.assertNotNull(res);
            res = DataBaseApi.getInstance().getPoolByDataBaseName("test");
            Assert.assertNotNull(res);
        } catch (Exception exception) {
            Assert.assertNotNull(res);
        }
    }

    /**
     * functional testing register table
     *
     * @tc.name: register table
     * @tc.number: OHOS_JAVA_database_DataBaseApi_registerTable_0001
     * @tc.desc: register table
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void registerTableTest01() {
        try {
            DataBaseApi.getInstance().registerTable("test", "ttest");
            Assert.assertTrue(true);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing register table
     *
     * @tc.name: register table
     * @tc.number: OHOS_JAVA_database_DataBaseApi_registerTable_0002
     * @tc.desc: register table
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void registerTableTest02() {
        try {
            DataBaseApi.getInstance().registerTable("", "ttest");
            Assert.assertTrue(true);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing register table
     *
     * @tc.name: register table
     * @tc.number: OHOS_JAVA_database_DataBaseApi_registerTable_0003
     * @tc.desc: register table
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void registerTableTest03() {
        try {
            DataBaseApi.getInstance().registerTable("test", "");
            Assert.assertTrue(true);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing register table
     *
     * @tc.name: register table
     * @tc.number: OHOS_JAVA_database_DataBaseApi_registerTable_0004
     * @tc.desc: register table
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void registerTableTest04() {
        try {
            DataBaseApi.getInstance().registerTable(null, null);
            Assert.assertTrue(true);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing register table
     *
     * @tc.name: register table
     * @tc.number: OHOS_JAVA_database_DataBaseApi_registerTable_0005
     * @tc.desc: register table
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void registerTableTest05() {
        try {
            DataBaseApi.getInstance().registerTable("test", "ttest");
            Assert.assertTrue(true);
            DataBaseApi.getInstance().registerTable("test", "ttest");
            Assert.assertTrue(true);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing register DataSource
     *
     * @tc.name: register DataSource
     * @tc.number: OHOS_JAVA_database_DataBaseApi_registerDataSource_0001
     * @tc.desc: register DataSource
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void registerDataSourceTest01() {
        try {
            DataBaseApi.getInstance().registerDataSource(null, null);
            Assert.assertTrue(true);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing register DataSource
     *
     * @tc.name: register DataSource
     * @tc.number: OHOS_JAVA_database_DataBaseApi_registerDataSource_0002
     * @tc.desc: register DataSource
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void registerDataSourceTest02() {
        try {
            DataBaseApi.getInstance().registerDataSource("dataBase", null);
            Assert.assertTrue(true);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing register DataSource
     *
     * @tc.name: register DataSource
     * @tc.number: OHOS_JAVA_database_DataBaseApi_registerDataSource_0003
     * @tc.desc: register DataSource
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void registerDataSourceTest03() {
        try {
            DataBaseApi.getInstance().registerDataSource("dataBase", new DruidDataSource());
            Assert.assertTrue(true);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing register DataSource
     *
     * @tc.name: register DataSource
     * @tc.number: OHOS_JAVA_database_DataBaseApi_registerDataSource_0004
     * @tc.desc: register DataSource
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void registerDataSourceTest04() {
        try {
            DataBaseApi.getInstance().registerDataSource(null, new DruidDataSource());
            Assert.assertTrue(true);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing register DataSource
     *
     * @tc.name: register DataSource
     * @tc.number: OHOS_JAVA_database_DataBaseApi_registerDataSource_0005
     * @tc.desc: register DataSource
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void registerDataSourceTest05() {
        try {
            DataBaseApi.getInstance().registerDataSource("dataBase", new DruidDataSource());
            Assert.assertTrue(true);
            DataBaseApi.getInstance().registerDataSource(null, new DruidDataSource());
            Assert.assertTrue(true);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing Database creation
     *
     * @tc.name: Database creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createDataBase_0001
     * @tc.desc: Database creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createDataBaseTest01() {
        try {
            boolean res = DataBaseApi.getInstance().createDataBase(null);
            Assert.assertFalse(res);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing Database creation
     *
     * @tc.name: Database creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createDataBase_0002
     * @tc.desc: Database creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createDataBaseTest02() {
        try {
            boolean res = DataBaseApi.getInstance().createDataBase("testaa");
            Assert.assertTrue(res);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing Database creation
     *
     * @tc.name: Database creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createDataBase_0003
     * @tc.desc: Database creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createDataBaseTest03() {
        try {
            boolean res = DataBaseApi.getInstance().createDataBase("testaa");
            Assert.assertTrue(res);
            boolean res0 = DataBaseApi.getInstance().createDataBase("testaa");
            Assert.assertTrue(res0);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing Database creation
     *
     * @tc.name: Database creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createDataBase_0005
     * @tc.desc: Database creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createDataBaseTest05() {
        try {
            boolean res = DataBaseApi.getInstance().createDataBase(null);
            Assert.assertFalse(res);
            boolean res0 = DataBaseApi.getInstance().createDataBase("testaa");
            Assert.assertTrue(res0);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing table creation
     *
     * @tc.name: Database table creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createTable_0001
     * @tc.desc: Database table creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createTableTest01() {
        try {
            boolean res =
                DataBaseApi.getInstance().createTable(DataBaseApi.DEFAULT_DATABASE_DBNAME, "testTable", processMemInfo);
            Assert.assertTrue(res);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing table creation
     *
     * @tc.name: Database table creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createTable_0002
     * @tc.desc: Database table creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createTableTest02() {
        try {
            boolean res =
                DataBaseApi.getInstance().createTable(DataBaseApi.DEFAULT_DATABASE_DBNAME, null, processMemInfo);
            Assert.assertFalse(res);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing table creation
     *
     * @tc.name: Database table creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createTable_0003
     * @tc.desc: Database table creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createTableTest03() {
        try {
            boolean res = DataBaseApi.getInstance().createTable("", null, processMemInfo);
            Assert.assertFalse(res);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing table creation
     *
     * @tc.name: Database table creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createTable_0004
     * @tc.desc: Database table creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createTableTest04() {
        try {
            boolean res = DataBaseApi.getInstance().createTable(null, null, processMemInfo);
            Assert.assertFalse(res);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing table creation
     *
     * @tc.name: Database table creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createTable_0005
     * @tc.desc: Database table creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createTableTest05() {
        try {
            boolean res = DataBaseApi.getInstance().createTable(null, null, processMemInfo);
            Assert.assertFalse(res);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing index creation
     *
     * @tc.name: Database index creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createIndex_0001
     * @tc.desc: Database index creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createIndexTest01() {
        try {
            boolean res =
                DataBaseApi.getInstance().createTable(DataBaseApi.DEFAULT_DATABASE_DBNAME, "testTable", processMemInfo);
            Assert.assertTrue(res);
            boolean result = DataBaseApi.getInstance().createIndex("testTable", "testIndex", processMemInfoIndex);
            Assert.assertTrue(result);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing index creation
     *
     * @tc.name: Database index creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createIndex_0002
     * @tc.desc: Database index creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createIndexTest02() {
        try {
            boolean res =
                DataBaseApi.getInstance().createTable(DataBaseApi.DEFAULT_DATABASE_DBNAME, "testTable", processMemInfo);
            Assert.assertTrue(res);
            boolean result = DataBaseApi.getInstance().createIndex("testTable", null, processMemInfoIndex);
            Assert.assertFalse(result);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing index creation
     *
     * @tc.name: Database index creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createIndex_0003
     * @tc.desc: Database index creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createIndexTest03() {
        try {
            boolean res =
                DataBaseApi.getInstance().createTable(DataBaseApi.DEFAULT_DATABASE_DBNAME, "testTable", processMemInfo);
            Assert.assertTrue(res);
            boolean result = DataBaseApi.getInstance().createIndex(null, "aaa", processMemInfoIndex);
            Assert.assertFalse(result);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing index creation
     *
     * @tc.name: Database index creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createIndex_0004
     * @tc.desc: Database index creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createIndexTest04() {
        try {
            boolean res =
                DataBaseApi.getInstance().createTable(DataBaseApi.DEFAULT_DATABASE_DBNAME, "testTable", processMemInfo);
            Assert.assertTrue(res);

            boolean result = DataBaseApi.getInstance().createIndex(null, null, processMemInfoIndex);
            Assert.assertFalse(result);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing index creation
     *
     * @tc.name: Database index creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createIndex_0005
     * @tc.desc: Database index creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createIndexTest05() {
        try {
            boolean res =
                DataBaseApi.getInstance().createTable(DataBaseApi.DEFAULT_DATABASE_DBNAME, "testTable", processMemInfo);
            Assert.assertTrue(res);
            boolean result = DataBaseApi.getInstance().createIndex(null, null, null);
            Assert.assertFalse(result);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }
}
