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

import com.alibaba.druid.pool.DruidDataSource;
import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.databases.databasepool.DataBase;
import ohos.devtools.datasources.databases.databasepool.DataBaseHelper;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static ohos.devtools.datasources.databases.databaseapi.DataBaseApi.DEFAULT_DATABASE_DBNAME;
import static ohos.devtools.datasources.databases.databasepool.DataBaseHelper.getFilePath;
import static ohos.devtools.datasources.databases.databasepool.DataBaseHelper.getUrlByDataBaseName;
import static ohos.devtools.datasources.databases.databasepool.DataBaseHelper.loadSqlFileToList;
import static ohos.devtools.datasources.databases.databasepool.DataTableHelper.getTableNameBySql;

/**
 * @Description Database creation class
 * @Date 2021/2/2 11:28
 **/
public class DataBaseManager {
    private static final Logger LOGGER = LogManager.getLogger(DataBaseManager.class);

    private static Set<String> dbLists = new HashSet<>();

    private static class SingletonClassInstance {
        private static final DataBaseManager INSTANCE = new DataBaseManager();
    }

    /**
     * getInstance
     *
     * @return DataBaseApi
     */
    public static DataBaseManager getInstance() {
        return DataBaseManager.SingletonClassInstance.INSTANCE;
    }

    private DataBaseManager() {
    }

    /**
     * Initialize the main database of the project
     *
     * @param dataBase DataBase
     * @return boolean
     */
    public boolean initDefaultDataBase(DataBase dataBase) {
        return createDataBase(dataBase);
    }

    /**
     * Create a data table in the main database of the database,
     * and the table creation statement is stored in the path configured by default Sql
     *
     * @param dataBase DataBase
     * @return boolean
     */
    public boolean initDefaultSql(DataBase dataBase) {
        Properties properties = new Properties();
        Statement statement = null;
        Connection connection = null;
        try {
            properties.load(DataBaseManager.class.getClassLoader().getResourceAsStream("db.properties"));
            String initPath = properties.getProperty("defaultSql");
            if (StringUtils.isNotBlank(initPath)) {
                String sqlConfigPath = SessionManager.getInstance().getPluginPath() + initPath;
                List<String> sqlList = loadSqlFileToList(sqlConfigPath);
                DataSource dataSource = createDruidConnectionPool(dataBase);
                DataBaseApi.getInstance().registerDataSource(DEFAULT_DATABASE_DBNAME, dataSource);
                connection = dataSource.getConnection();
                statement = connection.createStatement();
                for (String sql : sqlList) {
                    String tableName = getTableNameBySql(sql);
                    String dbName = DataBaseApi.getInstance().checkTableRegister(tableName);
                    if (DEFAULT_DATABASE_DBNAME.equals(dbName)) {
                        continue;
                    }
                    statement.execute(sql);
                    DataBaseApi.getInstance().registerTable(tableName, DEFAULT_DATABASE_DBNAME);
                }
                return true;
            }
        } catch (IOException | SQLException exception) {
            LOGGER.error("create Table Exception {}", exception.getMessage());
            return false;
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException exception) {
                    LOGGER.error(exception.getMessage());
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException exception) {
                    LOGGER.error(exception.getMessage());
                }
            }
        }
        return false;
    }

    /**
     * Create database
     *
     * @param dbName dbName
     * @return boolean
     */
    public boolean createDataBase(String dbName) {
        DataBase dataBase = DataBaseHelper.createDataBase();
        dataBase.setUrl(getUrlByDataBaseName(dbName));
        return createDataBase(dataBase);
    }

    /**
     * Create database
     *
     * @param dataBase dataBase
     * @return boolean
     */
    private boolean createDataBase(DataBase dataBase) {
        if (dataBase.getUrl() == null || StringUtils.isBlank(dataBase.getUrl())) {
            return false;
        }
        String dbPath = getFilePath(dataBase.getUrl());
        if (dbLists.contains(dataBase.getUrl())) {
            return true;
        }
        File dbFile = new File(dbPath);
        if (dbFile.exists()) {
            boolean deletResult = dbFile.delete();
            if (!deletResult) {
                LOGGER.error("delete Error");
            }
        } else {
            LOGGER.error("File Error");
        }
        File parent = dbFile.getParentFile();
        if (parent == null) {
            parent.mkdirs();
        }
        try {
            Class.forName("org.sqlite.JDBC");
            DriverManager.getConnection(dataBase.getUrl());
            dbLists.add(dataBase.getUrl());
            return true;
        } catch (SQLException | ClassNotFoundException throwAbles) {
            LOGGER.error("create DataBase failed {}", throwAbles.getMessage());
            return false;
        }
    }

    /**
     * Create a database connection pool
     *
     * @param dataBase dataBase
     * @return DataSource
     */
    public DataSource createDruidConnectionPool(DataBase dataBase) {
        boolean base = createDataBase(dataBase);
        if (!base) {
            return null;
        }
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(dataBase.getUrl());
        dataSource.setInitialSize(dataBase.getInitialSize());
        dataSource.setMaxActive(dataBase.getMaxActive());
        dataSource.setMinIdle(dataBase.getMinIdle());
        try {
            dataSource.setFilters(dataBase.getFilters());
            dataSource.setMaxWait(dataBase.getMaxWait());
            dataSource.setTimeBetweenEvictionRunsMillis(dataBase.getTimeBetweenEvictionRunsMillis());
            dataSource.setValidationQuery(dataBase.getValidationQuery());
            dataSource.setTestWhileIdle(dataBase.isTestWhileIdle());
            dataSource.setTestOnBorrow(dataBase.isTestOnBorrow());
            dataSource.setTestOnReturn(dataBase.isTestOnReturn());
        } catch (SQLException throwAbles) {
            LOGGER.error("create ConnectPoll {}", throwAbles.getMessage());
        }
        return dataSource;
    }
}
