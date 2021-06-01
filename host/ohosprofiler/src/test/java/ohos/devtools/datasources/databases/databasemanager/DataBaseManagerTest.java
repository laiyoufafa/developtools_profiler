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
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import javax.sql.DataSource;
import static ohos.devtools.datasources.databases.databaseapi.DataBaseApi.DEFAULT_DATABASE_DBNAME;
import static ohos.devtools.datasources.databases.databasepool.DataBaseHelper.getUrlByDataBaseName;

/**
 * @version 1.0
 * @date 2021/03/26 11:06
 **/
public class DataBaseManagerTest {
    /**
     * functional testing init
     *
     * @tc.name: setUp
     * @tc.number: OHOS_JAVA_database_DataBaseManager_setUp_0001
     * @tc.desc: setUp
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Before
    public void setUp() {
        SessionManager.getInstance().setDevelopMode(true);
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
        try {
            DataBase dataBase = DataBaseHelper.createDefaultDataBase();
            dataBase.setUrl(getUrlByDataBaseName(DEFAULT_DATABASE_DBNAME));
            boolean res = DataBaseManager.getInstance().initDefaultDataBase(dataBase);
            Assert.assertTrue(res);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
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
        try {
            DataBase dataBase = DataBaseHelper.createDefaultDataBase();
            dataBase.setUrl(getUrlByDataBaseName(DEFAULT_DATABASE_DBNAME));
            boolean res = DataBaseManager.getInstance().initDefaultDataBase(dataBase);
            Assert.assertTrue(res);
            boolean res01 = DataBaseManager.getInstance().initDefaultDataBase(dataBase);
            Assert.assertTrue(res01);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
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
        try {
            DataBase dataBase = DataBaseHelper.createDefaultDataBase();
            dataBase.setUrl(getUrlByDataBaseName(DEFAULT_DATABASE_DBNAME));
            boolean result = DataBaseManager.getInstance().initDefaultSql(dataBase);
            Assert.assertTrue(result);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
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
        try {
            DataBase dataBase = DataBaseHelper.createDefaultDataBase();
            dataBase.setUrl(getUrlByDataBaseName(DEFAULT_DATABASE_DBNAME));
            boolean res = DataBaseManager.getInstance().initDefaultDataBase(dataBase);
            Assert.assertTrue(res);
            boolean result = DataBaseManager.getInstance().initDefaultSql(dataBase);
            Assert.assertTrue(result);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
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
        try {
            boolean res = DataBaseManager.getInstance().createDataBase("test01");
            Assert.assertTrue(res);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
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
        try {
            boolean res = DataBaseManager.getInstance().createDataBase(null);
            Assert.assertTrue(res);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
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
        try {
            boolean res = DataBaseManager.getInstance().createDataBase("");
            Assert.assertTrue(res);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
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
        DataSource result = null;
        try {
            boolean res = DataBaseManager.getInstance().createDataBase("test01");
            Assert.assertTrue(res);
            DataBase dataBase = DataBaseHelper.createDataBase();
            dataBase.setUrl(getUrlByDataBaseName("test01"));
            result = DataBaseManager.getInstance().createDruidConnectionPool(dataBase);
            Assert.assertNotNull(result);
        } catch (Exception exception) {
            Assert.assertNotNull(result);
        }
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
        DataSource result = null;
        try {
            DataBase dataBase = DataBaseHelper.createDataBase();
            dataBase.setUrl(getUrlByDataBaseName("test02"));
            result = DataBaseManager.getInstance().createDruidConnectionPool(dataBase);
            Assert.assertNotNull(result);
        } catch (Exception exception) {
            Assert.assertNotNull(result);
        }
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
        DataSource result = null;
        try {
            result = DataBaseManager.getInstance().createDruidConnectionPool(null);
            Assert.assertNull(result);
        } catch (Exception exception) {
            Assert.assertNull(result);
        }
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
        DataSource result = null;
        try {
            boolean res = DataBaseManager.getInstance().createDataBase("test01");
            Assert.assertTrue(res);
            DataBase dataBase = DataBaseHelper.createDataBase();
            dataBase.setUrl(getUrlByDataBaseName("test01"));
            result = DataBaseManager.getInstance().createDruidConnectionPool(dataBase);
            DataBase dataBas2 = DataBaseHelper.createDataBase();
            dataBas2.setUrl(getUrlByDataBaseName("test01"));
            result = DataBaseManager.getInstance().createDruidConnectionPool(dataBase);
            Assert.assertNotNull(result);
        } catch (Exception exception) {
            Assert.assertNotNull(result);
        }
    }
}
