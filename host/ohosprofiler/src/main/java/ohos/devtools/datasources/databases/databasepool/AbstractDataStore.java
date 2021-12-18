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

package ohos.devtools.datasources.databases.databasepool;

import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.utils.common.util.BeanUtil;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ohos.devtools.datasources.databases.databasepool.DataTableHelper.getDeleteCondition;
import static ohos.devtools.datasources.databases.databasepool.DataTableHelper.mapToString;
import static ohos.devtools.datasources.databases.databasepool.DataTableHelper.sqlPlaceholder;

/**
 * Provides common data table operations
 *
 * @since 2021/10/26
 */
public abstract class AbstractDataStore<T> extends SqlRunnable {
    private static final Logger LOGGER = LogManager.getLogger(AbstractDataStore.class);
    private static final String TYPE = "type";
    private static final String STRING = "String";

    /**
     * Data insertion
     *
     * @param dataObject dataObject
     * @param <T> <T>
     * @return boolean
     */
    @SuppressWarnings("checkstyle:JavadocMethod")
    public <T> boolean insert(T dataObject) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insert");
        }
        if (dataObject instanceof List) {
            return insertInDateBaseBatch((List) dataObject);
        }
        return insertInDataBase(dataObject);
    }

    /**
     * Get database connection
     *
     * @param tableName tableName
     * @return Optional<Connection>
     */
    protected Optional<Connection> getConnectByTable(String tableName) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getConnectByTable");
        }
        return DataBaseApi.getInstance().getConnectByTable(tableName);
    }

    /**
     * Get database connection
     *
     * @param dbName dbName
     * @return Optional<Connection>
     */
    protected Optional<Connection> getConnectBydbName(String dbName) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getConnectBydbName");
        }
        return DataBaseApi.getInstance().getConnectBydbname(dbName);
    }

    /**
     * Insert data in bulk
     *
     * @param dataObject dataObject
     * @return boolean
     */
    private boolean insertInDateBaseBatch(List<T> dataObject) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertInDateBaseBatch");
        }
        Object obj = dataObject.get(0);
        String tableName = BeanUtil.getObjectName(obj);
        Connection connection = DataBaseApi.getInstance().getConnectByTable(tableName).get();
        StringBuffer insertSql = new StringBuffer("INSERT OR IGNORE INTO ");
        List<String> attrs = BeanUtil.getObjectAttributeNames(obj);
        String value = attrs.stream().collect(Collectors.joining(","));
        insertSql.append(tableName).append("(").append(value).append(") VALUES (").append(sqlPlaceholder(attrs.size()))
            .append(")");
        try {
            PreparedStatement psmt = connection.prepareStatement(insertSql.toString());
            for (Object data : dataObject) {
                applyParams(psmt, data);
                psmt.addBatch();
            }
            return executeBatch(connection, psmt);
        } catch (SQLException throwAbles) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(throwAbles.getMessage());
            }
        }
        return false;
    }

    private <T> void applyParams(PreparedStatement statement, T data) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("applyParams");
        }
        List<Map> list = BeanUtil.getFieldsInfo(data);
        try {
            for (int index = 0; index < list.size(); index++) {
                Map objMap = list.get(index);
                Object objValue = objMap.get("value");
                if (String.valueOf(objMap.get(TYPE)).contains(STRING)) {
                    if (objValue instanceof String) {
                        statement.setString(index + 1, (String) objValue);
                    }
                } else if (String.valueOf(objMap.get(TYPE)).contains("Integer")) {
                    if (objValue instanceof Integer) {
                        statement.setLong(index + 1, (int) objValue);
                    }
                } else if (String.valueOf(objMap.get(TYPE)).contains("Long")) {
                    if (objValue instanceof Long) {
                        statement.setLong(index + 1, (long) objValue);
                    }
                } else if (String.valueOf(objMap.get(TYPE)).contains("byte")) {
                    if (objValue instanceof byte[]) {
                        statement.setBytes(index + 1, (byte[]) objValue);
                    }
                } else if (String.valueOf(objMap.get(TYPE)).contains("Boolean")) {
                    if (objValue instanceof Boolean) {
                        statement.setBoolean(index + 1, (boolean) objValue);
                    }
                } else {
                    continue;
                }
            }
        } catch (SQLException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(exception.getMessage());
            }
        }
    }

    private <T> boolean insertInDataBase(T dataObject) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertInDataBase");
        }
        String tableName = BeanUtil.getObjectName(dataObject);
        StringBuffer insertSql = new StringBuffer("INSERT OR IGNORE INTO ");
        List<Map<String, Object>> objectInfos = BeanUtil.getFields(dataObject);
        StringBuffer attrs = new StringBuffer();
        StringBuffer values = new StringBuffer();
        objectInfos.forEach(objectMap -> {
            attrs.append(objectMap.get("name")).append(",");
            if (String.valueOf(objectMap.get(TYPE)).contains(STRING)) {
                values.append("'").append(objectMap.get("value")).append("',");
            } else {
                values.append(objectMap.get("value")).append(",");
            }
        });
        insertSql.append(tableName).append("(").append(attrs.deleteCharAt(attrs.length() - 1).toString())
            .append(") VALUES (").append(values.deleteCharAt(values.length() - 1).toString()).append(")");
        Connection connection = DataBaseApi.getInstance().getConnectByTable(tableName).get();
        return execute(connection, insertSql.toString());
    }

    /**
     * Delete database
     *
     * @param dataObject dataObject
     * @return boolean
     */
    public boolean delete(T dataObject) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("delete");
        }
        return deleteInDataBase(dataObject);
    }

    private <T> boolean deleteInDataBase(T dataObject) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deleteInDataBase");
        }
        String tableName = BeanUtil.getObjectName(dataObject);
        StringBuffer deleteSql = new StringBuffer("DELETE FROM ");
        Map<String, Object> beanMap = BeanUtil.getFiledsInfos(dataObject);
        deleteSql.append(tableName).append("WHERE ").append(getDeleteCondition(beanMap)).append("1 = 1");
        Connection connection = DataBaseApi.getInstance().getConnectByTable(tableName).get();
        return execute(connection, deleteSql.toString());
    }

    /**
     * update
     *
     * @param clazz clazz
     * @param condition condition
     * @param setValue setValue
     * @param <T> <T>
     * @return boolean
     */
    public <T> boolean update(Class<T> clazz, Map<String, Object> condition, Map<String, Object> setValue) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("update");
        }
        StringBuffer updateSql = new StringBuffer("UPDATE ");
        String tableName = clazz.getSimpleName();
        updateSql.append(tableName).append("SET").append(mapToString(setValue)).append("WHERE ").append(condition);
        Connection connection = DataBaseApi.getInstance().getConnectByTable(tableName).get();
        return execute(connection, updateSql.toString());
    }

    /**
     * Inquire
     *
     * @param clazz clazz
     * @param value value
     * @param condition condition
     * @param <T> T
     * @return List <T>
     */
    public <T> List<T> select(Class<T> clazz, Map<String, Object> value, Map<String, Object> condition) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("select");
        }
        StringBuffer selectSql = new StringBuffer("SELECT ");
        if (value == null || value.isEmpty()) {
            selectSql.append("*");
        }
        String tableName = clazz.getSimpleName();
        if (condition == null || condition.isEmpty()) {
            selectSql.append(" FROM ").append(tableName);
        } else {
            selectSql.append(" FROM ").append(tableName).append(" WHERE ").append(getDeleteCondition(condition));
        }
        Connection connection = DataBaseApi.getInstance().getConnectByTable(tableName).get();
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            rs = executeQuery(stmt, selectSql.toString());
            return new DataBaseRsHelp<T>().util(clazz.newInstance(), rs);
        } catch (SQLException | IllegalAccessException | InstantiationException | NoSuchFieldException exe) {
            LOGGER.error("select Exception: ", exe);
        } finally {
            close(rs, connection);
        }
        return new ArrayList<>();
    }

    /**
     * Create a table in the specified database
     *
     * @param dbName db Name
     * @param tableName table Name
     * @param params params
     * @return boolean
     */
    public boolean createTable(String dbName, String tableName, List<String> params) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createTable");
        }
        DataBaseApi dataSource = DataBaseApi.getInstance();
        return dataSource.createTable(dbName, tableName, params);
    }

    /**
     * Create a table in the specified database
     *
     * @param dbName db Name
     * @param tableName table Name
     * @param sql sql
     * @return boolean
     */
    public boolean createTable(String dbName, String tableName, String sql) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createTable");
        }
        DataBaseApi dataSource = DataBaseApi.getInstance();
        return dataSource.createTable(dbName, tableName, sql);
    }

    /**
     * Create an index on the specified data table
     *
     * @param tableName table Name
     * @param indexName index Name
     * @param params params
     * @return boolean
     */
    public boolean createIndex(String tableName, String indexName, List<String> params) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createIndex");
        }
        DataBaseApi dataSource = DataBaseApi.getInstance();
        return dataSource.createIndex(tableName, indexName, params);
    }
}
