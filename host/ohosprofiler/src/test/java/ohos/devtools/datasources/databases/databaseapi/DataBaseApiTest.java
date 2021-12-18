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
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Base Api Test
 *
 * @since 2021/2/1 9:31
 */
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
    }

    /**
     * get Instance Test
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getInstanceTest_0001
     * @tc.desc: get Instance Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getInstanceTest01() {
        DataBaseApi dataBaseApi = DataBaseApi.getInstance();
        Assert.assertNotNull(dataBaseApi);
    }

    /**
     * get Instance Test
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_database_DataBaseApi_getInstanceTest_0002
     * @tc.desc: get Instance Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getInstanceTest02() {
        DataBaseApi dataBaseApi1 = DataBaseApi.getInstance();
        DataBaseApi dataBaseApi2 = DataBaseApi.getInstance();
        Assert.assertEquals(dataBaseApi1, dataBaseApi2);
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
        boolean flag = DataBaseApi.getInstance().initDataSourceManager();
        Assert.assertTrue(flag);
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
        boolean flag1 = DataBaseApi.getInstance().initDataSourceManager();
        boolean flag2 = DataBaseApi.getInstance().initDataSourceManager();
        Assert.assertEquals(flag1, flag2);
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
        Optional<Connection> res = DataBaseApi.getInstance().getDefaultDataBaseConnect();
        Assert.assertTrue(res.isPresent());
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
        Optional<Connection> res1 = DataBaseApi.getInstance().getDefaultDataBaseConnect();
        Optional<Connection> res2 = DataBaseApi.getInstance().getDefaultDataBaseConnect();
        Assert.assertNotEquals(res1, res2);
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
        boolean flag =
            DataBaseApi.getInstance().createTable(DataBaseApi.DEFAULT_DATABASE_DBNAME, "DeviceInfo", processMemInfo);
        Optional<Connection> res = DataBaseApi.getInstance().getConnectByTable("DeviceInfo");
        Assert.assertTrue(res.isPresent());
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
        Optional<Connection> res = DataBaseApi.getInstance().getConnectByTable("");
        Assert.assertFalse(res.isPresent());
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
        Optional<Connection> res = DataBaseApi.getInstance().getConnectByTable(null);
        Assert.assertFalse(res.isPresent());
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
        boolean flag1 =
            DataBaseApi.getInstance().createTable(DataBaseApi.DEFAULT_DATABASE_DBNAME, "DeviceInfo1", processMemInfo);
        Optional<Connection> res1 = DataBaseApi.getInstance().getConnectByTable("DeviceInfo1");
        boolean flag2 =
            DataBaseApi.getInstance().createTable(DataBaseApi.DEFAULT_DATABASE_DBNAME, "DeviceInfo2", processMemInfo);
        Optional<Connection> res2 = DataBaseApi.getInstance().getConnectByTable("DeviceInfo2");
        Assert.assertNotEquals(res1, res2);
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
        boolean flag1 =
            DataBaseApi.getInstance().createTable(DataBaseApi.DEFAULT_DATABASE_DBNAME, "DeviceInfo", processMemInfo);
        Optional<Connection> res1 = DataBaseApi.getInstance().getConnectByTable("DeviceInfo");
        Optional<Connection> res2 = DataBaseApi.getInstance().getConnectByTable("DeviceInfo");
        Assert.assertNotEquals(res1, res2);
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
        Optional<Connection> res = DataBaseApi.getInstance().getConnectBydbname(null);
        Assert.assertTrue(res.isPresent());
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
        Optional<Connection> res = DataBaseApi.getInstance().getConnectBydbname("");
        Assert.assertTrue(res.isPresent());
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
        Optional<Connection> res1 = DataBaseApi.getInstance().getConnectBydbname("test");
        Assert.assertTrue(res1.isPresent());
        Optional<Connection> res2 = DataBaseApi.getInstance().getConnectBydbname("test");
        Assert.assertTrue(res2.isPresent());
        Assert.assertNotEquals(res1, res2);
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
        Optional<Connection> res1 = DataBaseApi.getInstance().getConnectBydbname("defaultDB");
        Optional<Connection> res2 = DataBaseApi.getInstance().getConnectBydbname("test");
        Assert.assertNotEquals(res1, res2);
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
        DataSource res = DataBaseApi.getInstance().getPoolByDataBaseName("");
        Assert.assertNotNull(res);
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
        DataSource res = DataBaseApi.getInstance().getPoolByDataBaseName(null);
        Assert.assertNotNull(res);
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
        DataSource res = DataBaseApi.getInstance().getPoolByDataBaseName("test");
        Assert.assertNotNull(res);
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
        DataSource res1 = DataBaseApi.getInstance().getPoolByDataBaseName(null);
        DataSource res2 = DataBaseApi.getInstance().getPoolByDataBaseName("");
        Assert.assertEquals(res1, res2);
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
        DataSource res1 = DataBaseApi.getInstance().getPoolByDataBaseName("test1");
        DataSource res2 = DataBaseApi.getInstance().getPoolByDataBaseName("test");
        Assert.assertNotEquals(res1, res2);
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
        DataBaseApi.getInstance().registerTable("test", "ttest");
        String test = DataBaseApi.getInstance().checkTableRegister("test");
        Assert.assertEquals(test, "ttest");
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
        boolean ttest = DataBaseApi.getInstance().registerTable("", "ttest");
        Assert.assertFalse(ttest);
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
        DataBaseApi.getInstance().registerTable("test", "");
        Assert.assertTrue(true);
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
        boolean res = DataBaseApi.getInstance().registerTable(null, null);
        Assert.assertFalse(res);
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
        boolean res = DataBaseApi.getInstance().registerTable("", "");
        Assert.assertFalse(res);
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
        boolean rst = DataBaseApi.getInstance().registerDataSource(null, null);
        Assert.assertFalse(rst);
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
        boolean dataBase = DataBaseApi.getInstance().registerDataSource("dataBase", null);
        Assert.assertFalse(dataBase);
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
        boolean dataBase = DataBaseApi.getInstance().registerDataSource("dataBase", new DruidDataSource());
        Assert.assertTrue(dataBase);
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
        boolean rse = DataBaseApi.getInstance().registerDataSource(null, new DruidDataSource());
        Assert.assertFalse(rse);
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
        boolean res = DataBaseApi.getInstance().registerDataSource("", null);
        Assert.assertFalse(res);
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
        boolean res = DataBaseApi.getInstance().createDataBase(null);
        Assert.assertFalse(res);
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
        boolean res = DataBaseApi.getInstance().createDataBase("testaa");
        Assert.assertTrue(res);
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
        boolean res = DataBaseApi.getInstance().createDataBase("test01");
        boolean res0 = DataBaseApi.getInstance().createDataBase("");
        Assert.assertNotEquals(res, res0);
    }

    /**
     * functional testing Database creation
     *
     * @tc.name: Database creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createDataBase_0004
     * @tc.desc: Database creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createDataBaseTest04() {
        boolean res = DataBaseApi.getInstance().createDataBase("");
        Assert.assertFalse(res);
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
        boolean res = DataBaseApi.getInstance().createDataBase(null);
        boolean res0 = DataBaseApi.getInstance().createDataBase("");
        Assert.assertEquals(res, res0);
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
        boolean res =
            DataBaseApi.getInstance().createTable(DataBaseApi.DEFAULT_DATABASE_DBNAME, "testTable", processMemInfo);
        Assert.assertTrue(res);
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
        boolean res = DataBaseApi.getInstance().createTable(DataBaseApi.DEFAULT_DATABASE_DBNAME, null, processMemInfo);
        Assert.assertFalse(res);
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
        boolean res = DataBaseApi.getInstance().createTable("", null, processMemInfo);
        Assert.assertFalse(res);
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
        boolean res = DataBaseApi.getInstance().createTable(null, null, processMemInfo);
        Assert.assertFalse(res);
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
        boolean res = DataBaseApi.getInstance().createTable("", "", processMemInfo);
        Assert.assertFalse(res);
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
        DataBaseApi.getInstance()
            .createTable(DataBaseApi.DEFAULT_DATABASE_DBNAME, "testTable222222", processMemInfo);
        boolean res =
            DataBaseApi.getInstance().createIndex("testTable222222", "testTable2", processMemInfoIndex);
        Assert.assertTrue(res);
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
        boolean result = DataBaseApi.getInstance().createIndex("testTable", null, processMemInfoIndex);
        Assert.assertFalse(result);
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
        boolean result = DataBaseApi.getInstance().createIndex(null, "", processMemInfoIndex);
        Assert.assertFalse(result);
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
        boolean result = DataBaseApi.getInstance().createIndex(null, null, processMemInfoIndex);
        Assert.assertFalse(result);
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
        boolean result = DataBaseApi.getInstance().createIndex(null, null, null);
        Assert.assertFalse(result);
    }

    /**
     * functional testing table check
     *
     * @tc.name: Check Table Register
     * @tc.number: OHOS_JAVA_database_DataBaseApi_checkTableRegister_0001
     * @tc.desc: Check Table Register
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void checkTableRegisterTest01() {
        String res = DataBaseApi.getInstance().checkTableRegister("");
        Assert.assertFalse(StringUtils.isNotBlank(res));
    }

    /**
     * functional testing table check
     *
     * @tc.name: Check Table Register
     * @tc.number: OHOS_JAVA_database_DataBaseApi_checkTableRegister_0002
     * @tc.desc: Check Table Register
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void checkTableRegisterTest02() {
        String res = DataBaseApi.getInstance().checkTableRegister(null);
        Assert.assertFalse(StringUtils.isNotBlank(res));
    }

    /**
     * functional testing table check
     *
     * @tc.name: Check Table Register
     * @tc.number: OHOS_JAVA_database_DataBaseApi_checkTableRegister_0003
     * @tc.desc: Check Table Register
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void checkTableRegisterTest03() {
        DataBaseApi.getInstance().registerCreateIndex("testTable");
        String res = DataBaseApi.getInstance().checkTableRegister("testTable");
        Assert.assertNotNull(res);
    }

    /**
     * functional testing table check
     *
     * @tc.name: Check Table Register
     * @tc.number: OHOS_JAVA_database_DataBaseApi_checkTableRegister_0004
     * @tc.desc: Check Table Register
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void checkTableRegisterTest04() {
        String res1 = DataBaseApi.getInstance().checkTableRegister("testTable");
        String res2 = DataBaseApi.getInstance().checkTableRegister("");
        Assert.assertNotEquals(res1, res2);
    }

    /**
     * functional testing table check
     *
     * @tc.name: Check Table Register
     * @tc.number: OHOS_JAVA_database_DataBaseApi_checkTableRegister_0005
     * @tc.desc: Check Table Register
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void checkTableRegisterTest05() {
        String res1 = DataBaseApi.getInstance().checkTableRegister(null);
        String res2 = DataBaseApi.getInstance().checkTableRegister("");
        Assert.assertEquals(res1, res2);
    }

    /**
     * functional testing index check
     *
     * @tc.name: Check Index Register
     * @tc.number: OHOS_JAVA_database_DataBaseApi_checkIndexRegister_0001
     * @tc.desc: Check Index Register
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void checkIndexRegisterTest01() {
        boolean res = DataBaseApi.getInstance().checkIndexRegister("");
        Assert.assertFalse(res);
    }

    /**
     * functional testing index check
     *
     * @tc.name: Check Index Register
     * @tc.number: OHOS_JAVA_database_DataBaseApi_checkIndexRegister_0002
     * @tc.desc: Check Index Register
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void checkIndexRegisterTest02() {
        boolean res = DataBaseApi.getInstance().checkIndexRegister(null);
        Assert.assertFalse(res);
    }

    /**
     * functional testing index check
     *
     * @tc.name: Check Index Register
     * @tc.number: OHOS_JAVA_database_DataBaseApi_checkIndexRegister_0003
     * @tc.desc: Check Index Register
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void checkIndexRegisterTest03() {
        DataBaseApi.getInstance().registerCreateIndex("testTable");
        boolean res = DataBaseApi.getInstance().checkIndexRegister("testTable");
        Assert.assertTrue(res);
    }

    /**
     * functional testing index check
     *
     * @tc.name: Check Index Register
     * @tc.number: OHOS_JAVA_database_DataBaseApi_checkIndexRegister_0004
     * @tc.desc: Check Index Register
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void checkIndexRegisterTest04() {
        boolean res1 = DataBaseApi.getInstance().checkIndexRegister("tableName1");
        boolean res2 = DataBaseApi.getInstance().checkIndexRegister("");
        Assert.assertEquals(res1, res2);
    }

    /**
     * functional testing index check
     *
     * @tc.name: Check Index Register
     * @tc.number: OHOS_JAVA_database_DataBaseApi_checkIndexRegister_0005
     * @tc.desc: Check Index Register
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void checkIndexRegisterTest05() {
        boolean res1 = DataBaseApi.getInstance().checkIndexRegister(null);
        boolean res2 = DataBaseApi.getInstance().checkIndexRegister("");
        Assert.assertEquals(res1, res2);
    }

    /**
     * functional testing index check
     *
     * @tc.name: Register Create Index
     * @tc.number: OHOS_JAVA_database_DataBaseApi_registerCreateIndex_0001
     * @tc.desc: Register Create Index
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void registerCreateIndexTest01() {
        boolean res = DataBaseApi.getInstance().registerCreateIndex("");
        Assert.assertFalse(res);
    }

    /**
     * functional testing index check
     *
     * @tc.name: Register Create Index
     * @tc.number: OHOS_JAVA_database_DataBaseApi_registerCreateIndex_0002
     * @tc.desc: Register Create Index
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void registerCreateIndexTest02() {
        boolean res = DataBaseApi.getInstance().registerCreateIndex(null);
        Assert.assertFalse(res);
    }

    /**
     * functional testing index check
     *
     * @tc.name: Register Create Index
     * @tc.number: OHOS_JAVA_database_DataBaseApi_registerCreateIndex_0003
     * @tc.desc: Register Create Index
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void registerCreateIndexTest03() {
        boolean testTable = DataBaseApi.getInstance().registerCreateIndex("testTable");
        Assert.assertTrue(testTable);
    }

    /**
     * functional testing table creation
     *
     * @tc.name: Database table creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createTableSql_0001
     * @tc.desc: Database table creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createTableSqlTest01() {
        boolean res = DataBaseApi.getInstance().createTable(DataBaseApi.DEFAULT_DATABASE_DBNAME, "MemoryInstanceInfo",
            "CREATE TABLE MemoryInstanceInfo (instanceId int(100) not null, deallocTime int(100));");
        Assert.assertTrue(res);
    }

    /**
     * functional testing table creation
     *
     * @tc.name: Database table creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createTableSql_0002
     * @tc.desc: Database table creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createTableSqlTest02() {
        boolean res = DataBaseApi.getInstance().createTable(DataBaseApi.DEFAULT_DATABASE_DBNAME, null, "");
        Assert.assertFalse(res);
    }

    /**
     * functional testing table creation
     *
     * @tc.name: Database table creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createTableSql_0003
     * @tc.desc: Database table creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createTableSqlTest03() {
        boolean res = DataBaseApi.getInstance().createTable(DataBaseApi.DEFAULT_DATABASE_DBNAME, "test", "");
        Assert.assertFalse(res);
    }

    /**
     * functional testing table creation
     *
     * @tc.name: Database table creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createTableSql_0004
     * @tc.desc: Database table creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createTableSqlTest04() {
        boolean res = DataBaseApi.getInstance().createTable(null, null, "");
        Assert.assertFalse(res);
    }

    /**
     * functional testing table creation
     *
     * @tc.name: Database table creation
     * @tc.number: OHOS_JAVA_database_DataBaseApi_createTableSql_0005
     * @tc.desc: Database table creation
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createTableSqlTest05() {
        boolean res = DataBaseApi.getInstance().createTable(null, null,
            "CREATE TABLE MemoryInstanceInfos (instanceId int(100) not null, deallocTime int(100))");
        Assert.assertFalse(res);
    }
}
