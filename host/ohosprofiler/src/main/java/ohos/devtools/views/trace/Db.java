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

package ohos.devtools.views.trace;

import ohos.devtools.views.trace.util.DataUtils;
import ohos.devtools.views.trace.util.Final;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Database operation class
 *
 * @date 2021/04/22 12:25
 */
public final class Db {
    private static boolean isLocal;
    private static volatile Db db = new Db();
    private static String dbName = "trace.db";
    private final String[] units = new String[] {"", "K", "M", "G", "T", "E"};
    private LinkedBlockingQueue<Connection> pool = new LinkedBlockingQueue<>();

    private Db() {
    }

    /**
     * Gets the value of dbName .
     *
     * @return String
     */
    public static String getDbName() {
        return dbName;
    }

    /**
     * Sets the dbName.You can use getDbName() to get the value of dbName</p>
     *
     * @param dbName dbName
     */
    public static void setDbName(final String dbName) {
        Db.dbName = dbName;
    }

    /**
     * Load the database file according to the file location variable
     *
     * @param isLocal isLocal
     */
    public static void load(final boolean isLocal) {
        Db.isLocal = isLocal;
        final int maxConnNum = 10;
        try {
            Class.forName("org.sqlite.JDBC");
            for (Connection connection : db.pool) {
                connection.close();
            }
            db.pool.clear();
            for (int size = 0; size < maxConnNum; size++) {
                db.newConn().ifPresent(connection -> {
                    try {
                        db.pool.put(connection);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                });
            }
            db.newConn().ifPresent(connection -> {
                try {
                    Statement statement = connection.createStatement();
                    String views = getSql("Views");
                    for (String view : views.split(";")) {
                        statement.execute(view + ";");
                    }
                    statement.close();
                    connection.close();
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            });
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Get the current current db object
     *
     * @return Db
     */
    public static Db getInstance() {
        if (db == null) {
            db = new Db();
        }
        return db;
    }

    /**
     * Read the sql directory under resource
     *
     * @param sqlName sqlName
     * @return String sql
     */
    public static String getSql(String sqlName) {
        String tmp = Final.OHOS ? "-self/" : "/";
        String path = "sql-app" + tmp + sqlName + ".txt";
        try (InputStream STREAM = DataUtils.class.getClassLoader().getResourceAsStream(path)) {
            return IOUtils.toString(STREAM, Charset.forName("UTF-8"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return "";
    }

    /**
     * Get database connection
     *
     * @return Connection
     */
    public Connection getConn() {
        Connection connection = null;
        try {
            connection = pool.take();
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
        return connection;
    }

    /**
     * Return the connection to the database connection pool after use
     *
     * @param conn conn
     */
    public void free(final Connection conn) {
        try {
            pool.put(conn);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    private Optional<Connection> newConn() {
        URL path = URLClassLoader.getSystemClassLoader().getResource(dbName);
        Connection conn = null;
        try {
            if (isLocal) {
                conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            } else {
                conn = DriverManager.getConnection("jdbc:sqlite::resource:" + path);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return Optional.ofNullable(conn);
    }

    /**
     * query sql count
     *
     * @param em em
     * @param args args
     * @return count int
     */
    public int queryCount(Sql em, Object... args) {
        String sql = String.format(Locale.ENGLISH, getSql(em.getName()), args);
        return queryCount(sql);
    }

    /**
     * query count from sql
     *
     * @param sql sql
     * @return count int
     */
    public int queryCount(String sql) {
        Statement stat = null;
        ResultSet rs = null;
        int count = 0;
        Connection conn = getConn();
        if (Objects.isNull(conn)) {
            return 0;
        }
        try {
            stat = conn.createStatement();
            String tSql = sql.trim();
            if (sql.trim().endsWith(";")) {
                tSql = sql.trim().substring(0, sql.trim().length() - 1);
            }
            rs = stat.executeQuery("select count(1) as count from (" + tSql + ");");
            while (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            release(rs, stat, conn);
        }
        return count;
    }

    /**
     * Read the sql directory under resource
     *
     * @param em em
     * @param res res
     * @param args args
     * @param <T> return type
     */
    public <T> void query(Sql em, List<T> res, Object... args) {
        String sql = String.format(Locale.ENGLISH, getSql(em.getName()), args);
        query(sql, res);
    }

    /**
     * Read the sql directory under resource
     *
     * @param <T> return type
     * @param res res
     * @param sql sql
     */
    public <T> void query(String sql, List<T> res) {
        Statement stat = null;
        ResultSet rs = null;
        Connection conn = getConn();
        Type argument = getType(res);
        try {
            Class<T> aClass = (Class<T>) Class.forName(argument.getTypeName());
            stat = conn.createStatement();
            rs = stat.executeQuery(sql);
            ArrayList<String> columnList = new ArrayList<>();
            ResultSetMetaData rsMeta = rs.getMetaData();
            int columnCount = rsMeta.getColumnCount();
            for (int index = 1; index <= columnCount; index++) {
                columnList.add(rsMeta.getColumnName(index));
            }
            while (rs.next()) {
                T data = aClass.getConstructor().newInstance();
                for (Field declaredField : aClass.getDeclaredFields()) {
                    declaredField.setAccessible(true);
                    DField annotation = declaredField.getAnnotation(DField.class);
                    if (Objects.nonNull(annotation) && columnList.contains(annotation.name())) {
                        setData(declaredField, data, rs, annotation);
                    }
                }
                res.add(data);
            }
        } catch (ClassNotFoundException | SQLException | InstantiationException
            | IllegalAccessException | InvocationTargetException
            | NoSuchMethodException exception) {
            exception.printStackTrace();
        } finally {
            release(rs, stat, conn);
        }
    }

    private <T> void setData(Field declaredField, T data, ResultSet rs, DField annotation)
        throws SQLException, IllegalAccessException {
        if (declaredField.getType() == Long.class || declaredField.getType() == long.class) {
            declaredField.set(data, rs.getLong(annotation.name()));
        } else if (declaredField.getType() == Integer.class || declaredField.getType() == int.class) {
            declaredField.set(data, rs.getInt(annotation.name()));
        } else if (declaredField.getType() == Double.class || declaredField.getType() == double.class) {
            declaredField.set(data, rs.getDouble(annotation.name()));
        } else if (declaredField.getType() == Float.class || declaredField.getType() == float.class) {
            declaredField.set(data, rs.getFloat(annotation.name()));
        } else if (declaredField.getType() == Boolean.class || declaredField.getType() == boolean.class) {
            declaredField.set(data, rs.getBoolean(annotation.name()));
        } else if (declaredField.getType() == Blob.class) {
            declaredField.set(data, rs.getBlob(annotation.name()));
        } else {
            declaredField.set(data, rs.getObject(annotation.name()));
        }
    }

    private <T> Type getType(List<T> res) {
        Type clazz = res.getClass().getGenericSuperclass();
        ParameterizedType pt = null;
        if (clazz instanceof ParameterizedType) {
            pt = (ParameterizedType) clazz;
        }
        if (pt == null) {
            return clazz;
        }
        Type argument = pt.getActualTypeArguments()[0];
        return argument;
    }

    private void release(ResultSet rs, Statement stat, Connection conn) {
        try {
            if (Objects.nonNull(rs)) {
                rs.close();
            }
            if (Objects.nonNull(stat)) {
                stat.close();
            }
            if (Objects.nonNull(conn)) {
                free(conn);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
