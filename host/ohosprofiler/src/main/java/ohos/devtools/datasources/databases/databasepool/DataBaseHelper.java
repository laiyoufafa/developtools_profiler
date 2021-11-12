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

import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static ohos.devtools.views.common.LayoutConstants.NEGATIVE_ONE;
import static ohos.devtools.views.common.LayoutConstants.THOUSAND_TWENTY_FOUR;

/**
 * Used for database creation in the project, default table creation
 *
 * @since 2021/10/22 16:30
 */
public class DataBaseHelper {
    private static final Logger LOGGER = LogManager.getLogger(DataBaseHelper.class);
    private static final String SQL_START_FLAG = "##";
    private static final String DB_DRIVER = "driver";
    private static final String DB_INIT_SIZE = "initialSize";
    private static final String DB_MAX_ACTIVE = "maxActive";
    private static final String DB_MIN_IDLE = "minIdle";
    private static final String DB_FILTERS = "filters";
    private static final String DB_MAX_WAIT = "maxWait";
    private static final String DB_RUN_MILLIS = "timeBetweenEvictionRunsMillis";
    private static final String DB_IDLE_MILLIS = "minEvictableIdleTimeMillis";
    private static final String DB_QUERY = "validationQuery";
    private static final String DB_TEST_IDLE = "testWhileIdle";
    private static final String DB_TEST_BORROW = "testOnBorrow";
    private static final String DB_TEST_RETURN = "testOnReturn";
    private static final String JDBC_SQLITE = "jdbc:sqlite:";

    private DataBaseHelper() {
    }

    /**
     * Check whether the specified database exists.
     *
     * @param dataBaseUrl dataBaseUrl
     * @return Returns true if the database exists; returns false otherwise.
     */
    public static boolean checkDataBaseExists(String dataBaseUrl) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("checkDataBaseExists");
        }
        String dbPath = getFilePath(dataBaseUrl);
        File dbFile = new File(dbPath);
        return dbFile.exists();
    }

    /**
     * Enter a specific path
     *
     * @param sqlPath sqlPath
     * @return List <String>
     * @throws IOException IOException
     */
    public static List<String> loadSqlFileToList(String sqlPath) throws IOException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("loadSqlFileToList");
        }
        File file = new File(sqlPath);
        if (!file.exists() || file.isDirectory()) {
            return new ArrayList<>();
        }
        InputStream sqlFileIn = new FileInputStream(sqlPath);
        StringBuffer sqlSb = new StringBuffer();
        byte[] buff = new byte[THOUSAND_TWENTY_FOUR];
        int byteRead = 0;
        while (true) {
            byteRead = sqlFileIn.read(buff);
            if (byteRead == NEGATIVE_ONE) {
                break;
            }
            sqlSb.append(new String(buff, 0, byteRead, Charset.defaultCharset()));
        }
        sqlFileIn.close();
        String[] sqlArr = sqlSb.toString().split("(\\r\\n)|(\\n)");
        List<String> sqlList = new ArrayList();
        List<String> sqlLists = Arrays.asList(sqlArr);
        StringBuilder createTableSql = new StringBuilder();
        for (String sql : sqlLists) {
            if (StringUtils.isNotBlank(sql) && sql.startsWith(SQL_START_FLAG)) {
                continue;
            }
            if ((!sql.contains("*"))) {
                createTableSql.append(sql);
                if (sql.endsWith(");")) {
                    sqlList.add(createTableSql.toString());
                    createTableSql = new StringBuilder();
                }
            }
        }
        return sqlList;
    }

    /**
     * Get database file path through JDBC URL
     *
     * @param url url
     * @return String
     */
    public static String getFilePath(String url) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getFilePath");
        }
        return url.replace("jdbc:sqlite:", "");
    }

    /**
     * getUrlByDataBaseName
     *
     * @param dbName dbName
     * @return String String
     */
    public static String getUrlByDataBaseName(String dbName) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getUrlByDataBaseName");
        }
        String dbPath = SessionManager.getInstance().tempPath();
        if (StringUtils.isBlank(dbName)) {
            return JDBC_SQLITE + dbPath + "defaultDB";
        }
        return JDBC_SQLITE + dbPath + dbName;
    }

    /**
     * createDataBase
     *
     * @return DataBase
     */
    public static DataBase createDataBase() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createDataBase");
        }
        Properties pop = new Properties();
        try {
            pop.load(DataBaseHelper.class.getClassLoader().getResourceAsStream("db.properties"));
            return DataBase.builder().driver(pop.getProperty(DB_DRIVER))
                .initialSize(Integer.parseInt(pop.getProperty(DB_INIT_SIZE)))
                .maxActive(Integer.parseInt(pop.getProperty(DB_MAX_ACTIVE)))
                .minIdle(Integer.parseInt(pop.getProperty(DB_MIN_IDLE))).filters(pop.getProperty(DB_FILTERS))
                .maxWait(Integer.parseInt(pop.getProperty(DB_MAX_WAIT)))
                .timeBetweenEvictionRunsMillis(Integer.parseInt(pop.getProperty(DB_RUN_MILLIS)))
                .minEvictableIdleTimeMillis(Integer.parseInt(pop.getProperty(DB_IDLE_MILLIS)))
                .validationQuery(pop.getProperty(DB_QUERY))
                .testWhileIdle(Boolean.parseBoolean(pop.getProperty(DB_TEST_IDLE)))
                .testOnBorrow(Boolean.parseBoolean(pop.getProperty(DB_TEST_BORROW)))
                .testOnReturn(Boolean.parseBoolean(pop.getProperty(DB_TEST_RETURN))).build();
        } catch (IOException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("createDataBase ", exception);
            }
        }
        return DataBase.builder().build();
    }

    /**
     * Create default database
     *
     * @return DataBase
     */
    public static DataBase createDefaultDataBase() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createDefaultDataBase");
        }
        Properties pop = new Properties();
        try {
            pop.load(DataBaseHelper.class.getClassLoader().getResourceAsStream("db.properties"));
            return DataBase.builder().driver(pop.getProperty(DB_DRIVER))
                .initialSize(Integer.parseInt(pop.getProperty(DB_INIT_SIZE)))
                .maxActive(Integer.parseInt(pop.getProperty(DB_MAX_ACTIVE)))
                .minIdle(Integer.parseInt(pop.getProperty(DB_MIN_IDLE))).filters(pop.getProperty(DB_FILTERS))
                .maxWait(Integer.parseInt(pop.getProperty(DB_MAX_WAIT)))
                .timeBetweenEvictionRunsMillis(Integer.parseInt(pop.getProperty(DB_RUN_MILLIS)))
                .minEvictableIdleTimeMillis(Integer.parseInt(pop.getProperty(DB_IDLE_MILLIS)))
                .validationQuery(pop.getProperty(DB_QUERY))
                .testWhileIdle(Boolean.parseBoolean(pop.getProperty(DB_TEST_IDLE)))
                .testOnBorrow(Boolean.parseBoolean(pop.getProperty(DB_TEST_BORROW)))
                .testOnReturn(Boolean.parseBoolean(pop.getProperty(DB_TEST_RETURN))).build();
        } catch (IOException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("createDefaultDataBase ", exception);
            }
        }
        return DataBase.builder().build();
    }
}
