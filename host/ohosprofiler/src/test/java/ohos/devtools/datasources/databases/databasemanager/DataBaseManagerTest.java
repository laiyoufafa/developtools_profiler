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

package ohos.devtools.datasources.databases.databasemanager;

import ohos.devtools.datasources.databases.databasepool.DataBase;
import ohos.devtools.datasources.databases.databasepool.DataBaseHelper;
import org.junit.Assert;
import org.junit.Test;
import javax.sql.DataSource;
import static ohos.devtools.datasources.databases.databaseapi.DataBaseApi.DEFAULT_DATABASE_DBNAME;
import static ohos.devtools.datasources.databases.databasepool.DataBaseHelper.getUrlByDataBaseName;

/**
 * Data Base Manager Test
 *
 * @since 2021/2/1 9:31
 */
public class DataBaseManagerTest {
    /**
     * get Instance Test
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_database_DataBaseManager_getInstanceTest_0001
     * @tc.desc: get Instance Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getInstanceTest01() {
        DataBaseManager dataBaseManager = DataBaseManager.getInstance();
        Assert.assertNotNull(dataBaseManager);
    }

    /**
     * get Instance Test
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_database_DataBaseManager_getInstanceTest_0002
     * @tc.desc: get Instance Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getInstanceTest02() {
        DataBaseManager dataBaseManager1 = DataBaseManager.getInstance();
        DataBaseManager dataBaseManager2 = DataBaseManager.getInstance();
        Assert.assertEquals(dataBaseManager1, dataBaseManager2);
    }

    /**
     * functional testing DefaultDataBase
     *
     * @tc.name: init DefaultDataBase
     * @tc.number: OHOS_JAVA_database_DataBaseManager_initDefaultDataBase_0001
     * @tc.desc: init DefaultDataBase
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void initDefaultDataBaseTest01() {
        DataBase dataBase = DataBaseHelper.createDefaultDataBase();
        dataBase.setUrl(getUrlByDataBaseName(DEFAULT_DATABASE_DBNAME));
        boolean res = DataBaseManager.getInstance().initDefaultDataBase(dataBase);
        Assert.assertTrue(res);
    }

    /**
     * functional testing DefaultDataBase
     *
     * @tc.name: init DefaultDataBase
     * @tc.number: OHOS_JAVA_database_DataBaseManager_initDefaultDataBase_0002
     * @tc.desc: init DefaultDataBase
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void initDefaultDataBaseTest02() {
        DataBase dataBase = DataBaseHelper.createDefaultDataBase();
        dataBase.setUrl(getUrlByDataBaseName(null));
        boolean res = DataBaseManager.getInstance().initDefaultDataBase(dataBase);
        Assert.assertTrue(res);
    }

    /**
     * functional testing DefaultDataBase
     *
     * @tc.name: init DefaultDataBase
     * @tc.number: OHOS_JAVA_database_DataBaseManager_initDefaultDataBase_0003
     * @tc.desc: init DefaultDataBase
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void initDefaultDataBaseTest03() {
        DataBase dataBase = DataBaseHelper.createDefaultDataBase();
        boolean res = DataBaseManager.getInstance().initDefaultDataBase(dataBase);
        Assert.assertFalse(res);
    }

    /**
     * functional testing DefaultDataBase
     *
     * @tc.name: init DefaultDataBase
     * @tc.number: OHOS_JAVA_database_DataBaseManager_initDefaultDataBase_0004
     * @tc.desc: init DefaultDataBase
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void initDefaultDataBaseTest04() {
        DataBase dataBase = DataBaseHelper.createDefaultDataBase();
        dataBase.setUrl(getUrlByDataBaseName("defaultDB2"));
        boolean res = DataBaseManager.getInstance().initDefaultDataBase(dataBase);
        Assert.assertTrue(res);
    }

    /**
     * functional testing DefaultDataBase
     *
     * @tc.name: init DefaultDataBase
     * @tc.number: OHOS_JAVA_database_DataBaseManager_initDefaultDataBase_0005
     * @tc.desc: init DefaultDataBase
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void initDefaultDataBaseTest05() {
        boolean res = DataBaseManager.getInstance().initDefaultDataBase(null);
        Assert.assertFalse(res);
    }

    /**
     * functional testing initDefaultSql
     *
     * @tc.name: init DefaultSql
     * @tc.number: OHOS_JAVA_database_DataBaseManager_initDefaultSql_0001
     * @tc.desc: init DefaultSql
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void initDefaultSqlTest01() {
        DataBase dataBase = DataBaseHelper.createDefaultDataBase();
        dataBase.setUrl(getUrlByDataBaseName(DEFAULT_DATABASE_DBNAME));
        boolean result = DataBaseManager.getInstance().initDefaultSql(dataBase);
        Assert.assertTrue(result);
    }

    /**
     * functional testing initDefaultSql
     *
     * @tc.name: init DefaultSql
     * @tc.number: OHOS_JAVA_database_DataBaseManager_initDefaultSql_0002
     * @tc.desc: init DefaultSql
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void initDefaultSqlTest02() {
        DataBase dataBase = DataBaseHelper.createDefaultDataBase();
        dataBase.setUrl(getUrlByDataBaseName(null));
        boolean res = DataBaseManager.getInstance().initDefaultDataBase(dataBase);
        Assert.assertTrue(res);
    }

    /**
     * functional testing initDefaultSql
     *
     * @tc.name: init DefaultSql
     * @tc.number: OHOS_JAVA_database_DataBaseManager_initDefaultSql_0003
     * @tc.desc: init DefaultSql
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void initDefaultSqlTest03() {
        DataBase dataBase = DataBaseHelper.createDefaultDataBase();
        boolean result = DataBaseManager.getInstance().initDefaultSql(dataBase);
        Assert.assertFalse(result);
    }

    /**
     * functional testing initDefaultSql
     *
     * @tc.name: init DefaultSql
     * @tc.number: OHOS_JAVA_database_DataBaseManager_initDefaultSql_0004
     * @tc.desc: init DefaultSql
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void initDefaultSqlTest04() {
        boolean result = DataBaseManager.getInstance().initDefaultSql(null);
        Assert.assertFalse(result);
    }

    /**
     * functional testing initDefaultSql
     *
     * @tc.name: init DefaultSql
     * @tc.number: OHOS_JAVA_database_DataBaseManager_initDefaultSql_0005
     * @tc.desc: init DefaultSql
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void initDefaultSqlTest05() {
        DataBase dataBase = DataBaseHelper.createDefaultDataBase();
        dataBase.setUrl(getUrlByDataBaseName("defaultDB2"));
        boolean result = DataBaseManager.getInstance().initDefaultSql(dataBase);
        Assert.assertTrue(result);
    }

    /**
     * functional testing createDataBase
     *
     * @tc.name: create DataBase
     * @tc.number: OHOS_JAVA_database_DataBaseManager_createDataBase_0001
     * @tc.desc: create DataBase
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createDataBaseTest01() {
        boolean res = DataBaseManager.getInstance().createDataBase("test01");
        Assert.assertTrue(res);
    }

    /**
     * functional testing createDataBase
     *
     * @tc.name: create DataBase
     * @tc.number: OHOS_JAVA_database_DataBaseManager_createDataBase_0002
     * @tc.desc: create DataBase
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createDataBaseTest02() {
        boolean res = DataBaseManager.getInstance().createDataBase(null);
        Assert.assertTrue(res);
    }

    /**
     * functional testing createDataBase
     *
     * @tc.name: create DataBase
     * @tc.number: OHOS_JAVA_database_DataBaseManager_createDataBase_0003
     * @tc.desc: create DataBase
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createDataBaseTest03() {
        boolean res = DataBaseManager.getInstance().createDataBase("");
        Assert.assertTrue(res);
    }

    /**
     * functional testing createDataBase
     *
     * @tc.name: create DataBase
     * @tc.number: OHOS_JAVA_database_DataBaseManager_createDataBase_0004
     * @tc.desc: create DataBase
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createDataBaseTest04() {
        boolean res1 = DataBaseManager.getInstance().createDataBase("test11");
        boolean res2 = DataBaseManager.getInstance().createDataBase("test01");
        Assert.assertEquals(res1, res2);
    }

    /**
     * functional testing createDataBase
     *
     * @tc.name: create DataBase
     * @tc.number: OHOS_JAVA_database_DataBaseManager_createDataBase_0005
     * @tc.desc: create DataBase
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createDataBaseTest05() {
        boolean res1 = DataBaseManager.getInstance().createDataBase("defaultDB");
        Assert.assertTrue(res1);
    }

    /**
     * functional testing createDruidConnectionPool
     *
     * @tc.name: create DruidConnection Pool
     * @tc.number: OHOS_JAVA_database_DataBaseManager_createDruidConnectionPool_0001
     * @tc.desc: create DruidConnection Pool
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createDruidConnectionPoolTest01() {
        DataBase dataBase = DataBaseHelper.createDataBase();
        dataBase.setUrl(getUrlByDataBaseName(DEFAULT_DATABASE_DBNAME));
        DataSource result = DataBaseManager.getInstance().createDruidConnectionPool(dataBase);
        Assert.assertNotNull(result);
    }

    /**
     * functional testing createDruidConnectionPool
     *
     * @tc.name: create DruidConnection Pool
     * @tc.number: OHOS_JAVA_database_DataBaseManager_createDruidConnectionPool_0002
     * @tc.desc: create DruidConnection Pool
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createDruidConnectionPoolTest02() {
        DataBase dataBase = DataBaseHelper.createDataBase();
        dataBase.setUrl(getUrlByDataBaseName(null));
        DataSource result = DataBaseManager.getInstance().createDruidConnectionPool(dataBase);
        Assert.assertNotNull(result);
    }

    /**
     * functional testing createDruidConnectionPool
     *
     * @tc.name: create DruidConnection Pool
     * @tc.number: OHOS_JAVA_database_DataBaseManager_createDruidConnectionPool_0003
     * @tc.desc: create DruidConnection Pool
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createDruidConnectionPoolTest03() {
        DataBase dataBase = DataBaseHelper.createDataBase();
        dataBase.setUrl(getUrlByDataBaseName(""));
        DataSource result = DataBaseManager.getInstance().createDruidConnectionPool(dataBase);
        Assert.assertNotNull(result);
    }

    /**
     * functional testing createDruidConnectionPool
     *
     * @tc.name: create DruidConnection Pool
     * @tc.number: OHOS_JAVA_database_DataBaseManager_createDruidConnectionPool_0004
     * @tc.desc: create DruidConnection Pool
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createDruidConnectionPoolTest04() {
        DataSource result = DataBaseManager.getInstance().createDruidConnectionPool(null);
        Assert.assertNull(result);
    }

    /**
     * functional testing createDruidConnectionPool
     *
     * @tc.name: create DruidConnection Pool
     * @tc.number: OHOS_JAVA_database_DataBaseManager_createDruidConnectionPool_0005
     * @tc.desc: create DruidConnection Pool
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void createDruidConnectionPoolTest05() {
        DataBase dataBase = DataBaseHelper.createDataBase();
        DataSource result = DataBaseManager.getInstance().createDruidConnectionPool(dataBase);
        Assert.assertNull(result);
    }
}
