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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ohos.devtools.datasources.databases.databasemanager.DataBaseManager;
import ohos.devtools.datasources.databases.databasepool.DataBase;
import ohos.devtools.datasources.databases.databasepool.DataBaseHelper;
import ohos.devtools.datasources.utils.common.util.CloseResourceUtil;
import ohos.devtools.datasources.utils.common.util.CommonUtil;

import static ohos.devtools.datasources.databases.databasepool.DataBaseHelper.getUrlByDataBaseName;

/**
 * Provides database related operations
 */
public class DataBaseApi {
    private static final Logger LOGGER = LogManager.getLogger(DataBaseApi.class);

    /**
     * System main library name
     */
    public static final String DEFAULT_DATABASE_DBNAME = "defaultDB";

    /**
     * CREATE TABLE sql
     */
    private static final String CREATE_TABLE = "CREATE TABLE if not exists %s";

    /**
     * CREATE TABLE INDEX sql
     */
    private static final String CREATE_TABLE_INDEX = "CREATE INDEX IF NOT EXISTS ";

    /**
     * Correspondence between storage database name and database connection pool
     */
    private static Map<String, DataSource> dataSourcePooleMap = new ConcurrentHashMap(CommonUtil.collectionSize(0));

    /**
     * Correspondence between storage database name and database connection pool
     */
    private static List<String> tableIndex = new ArrayList<>(CommonUtil.collectionSize(0));

    /**
     * Correspondence between storage table name and database name
     */
    private static Map<String, String> groupTables = new ConcurrentHashMap(CommonUtil.collectionSize(0));

    private static class SingletonClassInstance {
        private static final DataBaseApi INSTANCE = new DataBaseApi();
    }

    /**
     * getInstance
     *
     * @return DataBaseApi
     */
    public static DataBaseApi getInstance() {
        return DataBaseApi.SingletonClassInstance.INSTANCE;
    }

    private DataBaseApi() {
    }

    /**
     * Initialize the data source manager, which is called when the system is started, and only called once.
     *
     * @return boolean
     */
    public boolean initDataSourceManager() {
        return createDataBase(null, true);
    }

    /**
     * Create a database. If it is a system main library, create a data table in the main library.
     *
     * @param dbName data storage name
     * @param appStart Whether the application just started
     * @return boolean
     */
    private boolean createDataBase(String dbName, boolean appStart) {
        if (appStart) {
            if (StringUtils.isBlank(dbName) || DEFAULT_DATABASE_DBNAME.equals(dbName)) {
                DataBaseManager dataBaseManager = DataBaseManager.getInstance();
                DataBase dataBase = DataBaseHelper.createDefaultDataBase();
                dataBase.setUrl(getUrlByDataBaseName(DEFAULT_DATABASE_DBNAME));
                boolean result = dataBaseManager.initDefaultDataBase(dataBase);
                if (result) {
                    return dataBaseManager.initDefaultSql(dataBase);
                }
            } else {
                if (StringUtils.isBlank(dbName) || appStart) {
                    return false;
                }
                if (dataSourcePooleMap.get(dbName) != null) {
                    return true;
                }
                return DataBaseManager.getInstance().createDataBase(dbName);
            }
        } else {
            if (StringUtils.isBlank(dbName)) {
                return false;
            }
            if (dataSourcePooleMap.get(dbName) != null) {
                return true;
            }
            return DataBaseManager.getInstance().createDataBase(dbName);
        }
        return false;
    }

    /**
     * Get the database connection of the main library of the system
     *
     * @return Optional <Connection>
     */
    public Optional<Connection> getDefaultDataBaseConnect() {
        try {
            return Optional.of(getPoolByDataBaseName(DEFAULT_DATABASE_DBNAME).getConnection());
        } catch (SQLException sqlException) {
            LOGGER.info("getConnectByTable Exception ", sqlException);
            return Optional.empty();
        }
    }

    /**
     * Get database connection based on table name
     *
     * @param tableName tableName
     * @return Optional <Connection>
     */
    public Optional<Connection> getConnectByTable(String tableName) {
        if (StringUtils.isBlank(tableName)) {
            return Optional.empty();
        }
        String dbName = groupTables.get(tableName.toUpperCase(Locale.ENGLISH));
        if (StringUtils.isNotBlank(dbName)) {
            try {
                Connection conn = getPoolByDataBaseName(dbName).getConnection();
                return Optional.of(conn);
            } catch (SQLException throwAbles) {
                LOGGER.info("getConnectByTable Exception ", throwAbles);
            }
        }
        return Optional.empty();
    }

    /**
     * Get database connection based on database name
     *
     * @param dataBaseName dataBaseName
     * @return Optional <Connection>
     */
    public Optional<Connection> getConnectBydbname(String dataBaseName) {
        Connection connection;
        try {
            connection = getPoolByDataBaseName(dataBaseName).getConnection();
            return Optional.of(connection);
        } catch (SQLException throwAbles) {
            LOGGER.info("getConnectByTable Exception ", throwAbles);
        }
        return Optional.empty();
    }

    /**
     * Get the database connection pool object according to the database name
     *
     * @param dataBaseName dataBaseName
     * @return DataSource
     */
    public DataSource getPoolByDataBaseName(String dataBaseName) {
        DataSource dataSource;
        if (StringUtils.isBlank(dataBaseName)) {
            dataSource = dataSourcePooleMap.get(DEFAULT_DATABASE_DBNAME);
            if (dataSource == null) {
                DataBase dataBase = DataBaseHelper.createDataBase();
                dataBase.setUrl(getUrlByDataBaseName(dataBaseName));
                DataSource dataPool = DataBaseManager.getInstance().createDruidConnectionPool(dataBase);
                registerDataSource(DEFAULT_DATABASE_DBNAME, dataPool);
                return dataSourcePooleMap.get(DEFAULT_DATABASE_DBNAME);
            }
            return dataSourcePooleMap.get(DEFAULT_DATABASE_DBNAME);
        } else {
            dataSource = dataSourcePooleMap.get(dataBaseName);
            if (dataSource == null) {
                DataBase dataBase = DataBaseHelper.createDataBase();
                dataBase.setUrl(getUrlByDataBaseName(dataBaseName));
                DataSource dataPool = DataBaseManager.getInstance().createDruidConnectionPool(dataBase);
                registerDataSource(dataBaseName, dataPool);
                return dataSourcePooleMap.get(dataBaseName);
            }
            return dataSourcePooleMap.get(dataBaseName);
        }
    }

    /**
     * Correspondence between registered data table and database
     *
     * @param tableName tableName
     * @param dbName dbName
     */
    public void registerTable(String tableName, String dbName) {
        if (StringUtils.isNotBlank(tableName) && StringUtils.isNotBlank(dbName)) {
            groupTables.put(tableName.toUpperCase(Locale.ENGLISH), dbName);
        }
    }

    /**
     * Correspondence between registered data table and database
     *
     * @param tableName tableName
     * @return String
     */
    public String checkTableRegister(String tableName) {
        if (StringUtils.isNotBlank(tableName)) {
            return groupTables.get(tableName.toUpperCase(Locale.ENGLISH));
        }
        return "";
    }

    /**
     * Correspondence between registered data table and database
     *
     * @param tableName tableName
     * @return String
     */
    public boolean checkIndexRegister(String tableName) {
        if (StringUtils.isNotBlank(tableName)) {
            return tableIndex.contains(tableName);
        }
        return false;
    }

    /**
     * Correspondence between registered database name and database connection pool
     *
     * @param dataBaseName dataBaseName
     * @param dataSource dataSource
     */
    public void registerDataSource(String dataBaseName, DataSource dataSource) {
        if (StringUtils.isNotBlank(dataBaseName) && dataSource != null) {
            dataSourcePooleMap.put(dataBaseName, dataSource);
        }
    }

    /**
     * Index of the registry.
     *
     * @param tableName tableName
     */
    public void registerCreateIndex(String tableName) {
        if (StringUtils.isNotBlank(tableName)) {
            tableIndex.add(tableName);
        }
    }

    /**
     * Create a database, which is used to dynamically create a database during system operation
     *
     * @param dbName dbName
     * @return boolean
     */
    public boolean createDataBase(String dbName) {
        return createDataBase(dbName, false);
    }

    /**
     * Create table
     *
     * @param dbName dbName
     * @param tableName tableName
     * @param params params
     * @return boolean
     */
    public boolean createTable(String dbName, String tableName, List<String> params) {
        if (StringUtils.isBlank(tableName) || StringUtils.isBlank(dbName)) {
            return false;
        }
        if (params == null && params.isEmpty()) {
            return false;
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(String.format(Locale.ENGLISH, CREATE_TABLE, tableName));
        Optional<Connection> optionalConnection = getConnectBydbname(dbName);
        if (optionalConnection.isPresent()) {
            Connection conn = optionalConnection.get();
            String value = params.stream().collect(Collectors.joining(","));
            Statement statement = null;
            try {
                String db = DataBaseApi.getInstance().checkTableRegister(tableName);
                if (tableName.equals(db)) {
                    return true;
                }
                statement = conn.createStatement();
                statement.execute(String.format(Locale.ENGLISH, "%s ( %s )", stringBuffer, value));
                registerTable(tableName, dbName);
                return true;
            } catch (SQLException throwAbles) {
                LOGGER.info("SQLException Error : ", throwAbles);
                return false;
            } finally {
                CloseResourceUtil.closeResource(LOGGER, conn, null, statement);
            }
        }
        return false;
    }

    /**
     * createTable
     *
     * @param dbName dbName
     * @param tableName tableName
     * @param sql sql
     * @return boolean
     */
    public boolean createTable(String dbName, String tableName, String sql) {
        if (StringUtils.isBlank(tableName) || StringUtils.isBlank(dbName) || StringUtils.isBlank(sql)) {
            return false;
        }
        Optional<Connection> connect = getConnectBydbname(dbName);
        if (connect.isPresent()) {
            Connection conn = connect.get();
            Statement statm = null;
            try {
                String db = DataBaseApi.getInstance().checkTableRegister(tableName);
                if (dbName.equals(db)) {
                    return true;
                }
                statm = conn.createStatement();
                statm.execute(sql);
                registerTable(tableName, dbName);
                return true;
            } catch (SQLException throwAbles) {
                LOGGER.info("create Table Error ", throwAbles);
            } finally {
                CloseResourceUtil.closeResource(LOGGER, conn, null, statm);
            }
        }
        return false;
    }

    /**
     * Create table index
     *
     * @param tableName tableName
     * @param indexName indexName
     * @param columnName columnName
     * @return boolean
     */
    public boolean createIndex(String tableName, String indexName, List<String> columnName) {
        if (StringUtils.isBlank(tableName) || StringUtils.isBlank(indexName)) {
            return false;
        }
        if (columnName == null || columnName.isEmpty()) {
            return false;
        }
        if (checkIndexRegister(tableName)) {
            return true;
        }
        StringBuffer stringBuffer = new StringBuffer(CREATE_TABLE_INDEX).append(indexName).append(" ON ");
        stringBuffer.append(tableName).append("(");
        String value = columnName.stream().collect(Collectors.joining(","));
        stringBuffer.append(value).append(")");
        Optional<Connection> optionalConnection = getConnectByTable(tableName);
        if (optionalConnection.isPresent()) {
            Connection conn = optionalConnection.get();
            Statement statement = null;
            try {
                statement = conn.createStatement();
                statement.execute(stringBuffer.toString());
                registerCreateIndex(tableName);
                return true;
            } catch (SQLException throwAbles) {
                LOGGER.info("SQLException Error : ", throwAbles);
                return false;
            } finally {
                CloseResourceUtil.closeResource(LOGGER, conn, null, statement);
            }
        }
        return false;
    }
}
