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

package ohos.devtools.views.distributed.util;

import ohos.devtools.views.trace.DField;
import ohos.devtools.views.trace.Sql;
import ohos.devtools.views.trace.util.DataUtils;
import ohos.devtools.views.trace.util.Final;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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
 * DistributedDBA
 *
 * @since 2021/08/10 16:53
 */
public final class DistributedDB {
    private static boolean isLocal;
    private static volatile DistributedDB db = new DistributedDB();
    private static String dbNameA = "";
    private static String dbNameB = "";

    private LinkedBlockingQueue<Connection> poolA = new LinkedBlockingQueue();
    private LinkedBlockingQueue<Connection> poolB = new LinkedBlockingQueue();

    private DistributedDB() {
    }

    /**
     * Gets the value of dbName .
     *
     * @return the value of java.lang.String
     */
    public static String getDbNameA() {
        return dbNameA;
    }

    /**
     * Gets the value of dbName .
     *
     * @return the value of java.lang.String
     */
    public static String getDbNameB() {
        return dbNameB;
    }

    /**
     * Sets the dbName .You can use getDbName() to get the value of dbName
     *
     * @param dbNameA dbName
     * @param dbNameB dbNameB
     */
    public static void setDbName(final String dbNameA, final String dbNameB) {
        DistributedDB.dbNameA = dbNameA;
        DistributedDB.dbNameB = dbNameB;
    }

    /**
     * Load the database file according to the file location variable
     *
     * @param isLocal isLocal
     */
    public static void load(final boolean isLocal) {
        DistributedDB.isLocal = isLocal;
        try {
            Class.forName("org.sqlite.JDBC");
            for (Connection connection : db.poolA) {
                connection.close();
            }
            for (Connection connection : db.poolB) {
                connection.close();
            }
            db.poolA.clear();
            db.poolB.clear();
            final int maxConnNum = 4;
            for (int size = 0; size < maxConnNum; size++) {
                db.newConnA().ifPresent(connection -> {
                    try {
                        db.poolA.put(connection);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                });
                db.newConnB().ifPresent(connection -> {
                    try {
                        db.poolB.put(connection);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                });
            }
            db.newConnA().ifPresent(DistributedDB::excuteConnection);
            db.newConnB().ifPresent(DistributedDB::excuteConnection);
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
    }

    private static void excuteConnection(Connection connection) {
        try {
            Statement statement = connection.createStatement();
            String views = getSql("DistributedCpuViews");
            String[] split = views.split(";");
            for (String str : split) {
                statement.execute(str);
            }
            statement.close();
            connection.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Get the current current db object
     *
     * @return Db db
     */
    public static DistributedDB getInstance() {
        if (db == null) {
            db = new DistributedDB();
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
        String tmp = Final.IS_RESOURCE_SQL ? "-self/" : "/";
        String path = "sql" + tmp + sqlName + ".sql";
        try (InputStream STREAM = DataUtils.class.getClassLoader().getResourceAsStream(path)) {
            return IOUtils.toString(STREAM, Charset.forName("UTF-8")).replaceAll("/\\*[\\s\\S]*?\\*/", "");
        } catch (UnsupportedEncodingException exception) {
            exception.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return "";
    }

    /**
     * Get database connection
     *
     * @return Connection
     */
    public Connection getConnA() {
        Connection connection = null;
        try {
            connection = poolA.take();
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
        return connection;
    }

    /**
     * Get database connection
     *
     * @return Connection
     */
    public Connection getConnB() {
        Connection connection = null;
        try {
            connection = poolB.take();
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
    public void freeA(final Connection conn) {
        try {
            poolA.put(conn);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Return the connection to the database connection pool after use
     *
     * @param conn conn
     */
    public void freeB(final Connection conn) {
        try {
            poolB.put(conn);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    private Optional<Connection> newConnA() {
        URL path = URLClassLoader.getSystemClassLoader().getResource(dbNameA);
        Connection conn = null;
        try {
            if (isLocal) {
                conn = DriverManager.getConnection("jdbc:sqlite:" + dbNameA);
            } else {
                conn = DriverManager.getConnection("jdbc:sqlite::resource:" + path);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return Optional.ofNullable(conn);
    }

    private Optional<Connection> newConnB() {
        URL path = URLClassLoader.getSystemClassLoader().getResource(dbNameB);
        Connection conn = null;
        try {
            if (isLocal) {
                conn = DriverManager.getConnection("jdbc:sqlite:" + dbNameB);
            } else {
                conn = DriverManager.getConnection("jdbc:sqlite::resource:" + path);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return Optional.ofNullable(conn);
    }

    /**
     * Read the sql directory under resource
     *
     * @param em em
     * @param res res
     * @param args args
     * @param <T> return type
     */
    public <T> void queryA(Sql em, List<T> res, Object... args) {
        String sql = String.format(Locale.ENGLISH, getSql(em.getName()), args);
        queryA(sql, res);
    }

    /**
     * Read the sql directory under resource
     *
     * @param em em
     * @param res res
     * @param args args
     * @param <T> return type
     */
    public <T> void queryB(Sql em, List<T> res, Object... args) {
        String sql = String.format(Locale.ENGLISH, getSql(em.getName()), args);
        queryB(sql, res);
    }

    /**
     * query sql count
     *
     * @param em em
     * @param args args
     * @return int int
     */
    public int queryCountA(Sql em, Object... args) {
        String sql = String.format(Locale.ENGLISH, getSql(em.getName()), args);
        return queryCountA(sql);
    }

    /**
     * query sql count
     *
     * @param em em
     * @param args args
     * @return int int
     */
    public int queryCountB(Sql em, Object... args) {
        String sql = String.format(Locale.ENGLISH, getSql(em.getName()), args);
        return queryCountB(sql);
    }

    /**
     * query count from sql
     *
     * @param sql sql
     * @return int int
     */
    public int queryCountA(String sql) {
        Statement stat = null;
        ResultSet rs = null;
        int count = 0;
        Connection conn = getConnA();
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
            releaseA(rs, stat, conn);
        }
        return count;
    }

    /**
     * query count from sql
     *
     * @param sql sql
     * @return int int
     */
    public int queryCountB(String sql) {
        Statement stat = null;
        ResultSet rs = null;
        int count = 0;
        Connection conn = getConnB();
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
            releaseB(rs, stat, conn);
        }
        return count;
    }

    /**
     * Read the sql directory under resource
     *
     * @param res res
     * @param sql sql
     * @param <T> return type
     */
    public <T> void queryA(String sql, List<T> res) {
        Statement stat = null;
        ResultSet rs = null;
        Connection conn = getConnA();
        if (Objects.isNull(conn)) {
            return;
        }
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
        } catch (ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException
            | InvocationTargetException | NoSuchMethodException exception) {
            exception.printStackTrace();
        } finally {
            releaseA(rs, stat, conn);
        }
    }

    /**
     * Read the sql directory under resource
     *
     * @param res res
     * @param sql sql
     * @param <T> return type
     */
    public <T> void queryB(String sql, List<T> res) {
        Statement stat = null;
        ResultSet rs = null;
        Connection conn = getConnB();
        if (Objects.isNull(conn)) {
            return;
        }
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
        } catch (ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException
            | InvocationTargetException | NoSuchMethodException exception) {
            exception.printStackTrace();
        } finally {
            releaseB(rs, stat, conn);
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
        } else if (declaredField.getType() == String.class) {
            declaredField.set(data, rs.getString(annotation.name()));
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
        return pt.getActualTypeArguments()[0];
    }

    private void releaseA(ResultSet rs, Statement stat, Connection conn) {
        try {
            if (Objects.nonNull(rs)) {
                rs.close();
            }
            if (Objects.nonNull(stat)) {
                stat.close();
            }
            if (Objects.nonNull(conn)) {
                freeA(conn);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private void releaseB(ResultSet rs, Statement stat, Connection conn) {
        try {
            if (Objects.nonNull(rs)) {
                rs.close();
            }
            if (Objects.nonNull(stat)) {
                stat.close();
            }
            if (Objects.nonNull(conn)) {
                freeB(conn);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * update a db
     *
     * @param em em
     * @param args args
     * @return update row count
     */
    public int updateA(Sql em, Object... args) {
        String sql = String.format(Locale.ENGLISH, getSql(em.getName()), args);
        return updateA(sql);
    }

    /**
     * update b db
     *
     * @param em em
     * @param args args
     * @return update row count
     */
    public int updateB(Sql em, Object... args) {
        String sql = String.format(Locale.ENGLISH, getSql(em.getName()), args);
        return updateB(sql);
    }

    private int updateA(String sql) {
        Connection conn = getConnA();
        Statement stat = null;
        try {
            stat = conn.createStatement();
            return stat.executeUpdate(sql);
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            releaseA(null, stat, conn);
        }
        return 0;
    }

    private int updateB(String sql) {
        Connection conn = getConnB();
        Statement stat = null;
        try {
            stat = conn.createStatement();
            return stat.executeUpdate(sql);
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            releaseB(null, stat, conn);
        }
        return 0;
    }
}
