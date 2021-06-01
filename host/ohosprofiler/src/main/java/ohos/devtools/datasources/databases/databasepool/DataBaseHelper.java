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
 * @version 1.0
 * @date 2021/1/25 9:45
 **/
public class DataBaseHelper {
    private DataBaseHelper() {
    }

    private static final Logger LOGGER = LogManager.getLogger(DataBaseHelper.class);
    private static final String SQL_START_FLAG = "##";

    /**
     * Check whether the specified database exists.
     *
     * @param dataBaseUrl dataBaseUrl
     * @return Returns true if the database exists; returns false otherwise.
     */
    public static boolean checkDataBaseExists(String dataBaseUrl) {
        String dbPath = getFilePath(dataBaseUrl);
        File dbFile = new File(dbPath);
        return dbFile.exists();
    }

    /**
     * Enter a specific path
     *
     * @param sqlPath sqlPath
     * @return List<String>
     * @throws IOException IOException
     */
    public static List<String> loadSqlFileToList(String sqlPath) throws IOException {
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
        String filePath = url.replace("jdbc:sqlite:", "");
        return filePath;
    }

    /**
     * getUrlByDataBaseName
     *
     * @param dbName dbName
     * @return String String
     */
    public static String getUrlByDataBaseName(String dbName) {
        String dbPath = SessionManager.getInstance().getPluginPath();
        if (StringUtils.isBlank(dbName)) {
            return "jdbc:sqlite:" + dbPath + "defaultDB";
        }
        return "jdbc:sqlite:" + dbPath + dbName;
    }

    /**
     * createDataBase
     *
     * @return DataBase
     */
    public static DataBase createDataBase() {
        Properties pop = new Properties();
        try {
            pop.load(DataBaseHelper.class.getClassLoader().getResourceAsStream("db.properties"));
            return DataBase.builder().driver(pop.getProperty("driver"))
                .initialSize(Integer.parseInt(pop.getProperty("initialSize")))
                .maxActive(Integer.parseInt(pop.getProperty("maxActive")))
                .minIdle(Integer.parseInt(pop.getProperty("minIdle"))).filters(pop.getProperty("filters"))
                .maxWait(Integer.parseInt(pop.getProperty("maxWait")))
                .timeBetweenEvictionRunsMillis(Integer.parseInt(pop.getProperty("timeBetweenEvictionRunsMillis")))
                .minEvictableIdleTimeMillis(Integer.parseInt(pop.getProperty("minEvictableIdleTimeMillis")))
                .validationQuery(pop.getProperty("validationQuery"))
                .testWhileIdle(Boolean.parseBoolean(pop.getProperty("testWhileIdle")))
                .testOnBorrow(Boolean.parseBoolean(pop.getProperty("testOnBorrow")))
                .testOnReturn(Boolean.parseBoolean(pop.getProperty("testOnReturn"))).build();
        } catch (IOException exception) {
            LOGGER.error("createDataBase" + exception.getMessage());
        }
        return DataBase.builder().build();
    }

    /**
     * Create default database
     *
     * @return DataBase
     */
    public static DataBase createDefaultDataBase() {
        Properties pop = new Properties();
        try {
            pop.load(DataBaseHelper.class.getClassLoader().getResourceAsStream("db.properties"));
            return DataBase.builder().driver(pop.getProperty("driver"))
                .initialSize(Integer.parseInt(pop.getProperty("initialSize")))
                .maxActive(Integer.parseInt(pop.getProperty("maxActive")))
                .minIdle(Integer.parseInt(pop.getProperty("minIdle"))).filters(pop.getProperty("filters"))
                .maxWait(Integer.parseInt(pop.getProperty("maxWait")))
                .timeBetweenEvictionRunsMillis(Integer.parseInt(pop.getProperty("timeBetweenEvictionRunsMillis")))
                .minEvictableIdleTimeMillis(Integer.parseInt(pop.getProperty("minEvictableIdleTimeMillis")))
                .validationQuery(pop.getProperty("validationQuery"))
                .testWhileIdle(Boolean.parseBoolean(pop.getProperty("testWhileIdle")))
                .testOnBorrow(Boolean.parseBoolean(pop.getProperty("testOnBorrow")))
                .testOnReturn(Boolean.parseBoolean(pop.getProperty("testOnReturn"))).build();
        } catch (IOException exception) {
            LOGGER.error("createDefaultDataBase" + exception.getMessage());
        }
        return DataBase.builder().build();
    }
}
