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

package ohos.devtools.services.memory;

import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.utils.common.util.CloseResourceUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ohos.devtools.datasources.utils.common.Constant.JVMTI_AGENT_PLUG;

/**
 * 处理端侧获取的类数据
 *
 * @version 1.0
 * @date 2021/04/03 11:03
 **/
public class ClassInfoDao extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(ClassInfoDao.class);
    private static final int SQL_INDEX_TWO = 2;
    private static final int SQL_INDEX_THREE = 3;
    private static volatile ClassInfoDao singleton;

    /**
     * ClassInfoDao
     *
     * @return ClassInfoDao
     */
    public static ClassInfoDao getInstance() {
        if (singleton == null) {
            synchronized (ClassInfoDao.class) {
                if (singleton == null) {
                    singleton = new ClassInfoDao();
                }
            }
        }
        return singleton;
    }

    public ClassInfoDao() {
        createClassInfo();
    }

    /**
     * 获取数据库连接
     *
     * @param tableName 表名
     * @return Connection
     * @date 2021/4/3 11:44
     */
    private Connection getConnection(String tableName) {
        Optional<Connection> optionalConnection = getConnectByTable(tableName);
        Connection conn = null;
        if (optionalConnection.isPresent()) {
            conn = optionalConnection.get();
        }
        return conn;
    }

    /**
     * createClassInfo
     *
     * @return boolean
     */
    public boolean createClassInfo() {
        boolean createResult = false;
        String dbName = JVMTI_AGENT_PLUG;
        String classInfoTable = "ClassInfo";
        String sql = "CREATE TABLE ClassInfo " + "(   id    Integer primary key autoincrement not null, "
            + "    cId        int not null, " + "  className  varchar  not null " + ");";
        createResult = createTable(dbName, classInfoTable, sql);
        return createResult;
    }

    /**
     * insertClassInfo
     *
     * @param classInfo classInfo
     */
    public void insertClassInfo(ClassInfo classInfo) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection("ClassInfo");
            String sql = "insert into ClassInfo (id,cId,className) values (null,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, classInfo.getcId());
            ps.setString(Option.SQL_INDEX_TWO, classInfo.getClassName());
            ps.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
    }

    /**
     * insertClassInfos
     *
     * @param classInfos classInfos
     * @return boolean
     */
    public boolean insertClassInfos(List<ClassInfo> classInfos) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection("ClassInfo");
            conn.setAutoCommit(false);
            String sql = "insert into ClassInfo (id,cId,className) values (null,?,?)";
            ps = conn.prepareStatement(sql);
            for (ClassInfo classInfo : classInfos) {
                ps.setInt(1, classInfo.getcId());
                ps.setString(SQL_INDEX_TWO, classInfo.getClassName());
                ps.addBatch();
            }
            int[] results = ps.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException throwables) {
            LOGGER.error("SQLException error: " + throwables.getMessage());
            return false;
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
    }

    /**
     * 获取所有的ClassInfo数据.
     *
     * @param sessionId sessionId
     * @return List<ClassInfo>
     */
    public List<ClassInfo> getAllClassInfoData(Long sessionId) {
        Connection conn = getConnection("ClassInfo");
        PreparedStatement ps = null;
        ArrayList<ClassInfo> classInfos = new ArrayList<>();
        try {
            String sql = "select id,cId,className from ClassInfo";
            ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            ClassInfo classInfo = null;
            while (rs.next()) {
                classInfo = new ClassInfo();
                Integer id = rs.getInt("id");
                Integer cId = rs.getInt("cId");
                String className = rs.getString("className");
                classInfo.setId(id);
                classInfo.setcId(cId);
                classInfo.setClassName(className);
                classInfos.add(classInfo);
            }
            return classInfos;
        } catch (SQLException throwables) {
            LOGGER.info("memoryHeapInfo Exception {}", throwables.getMessage());
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
        return classInfos;
    }

    /**
     * 获取所有的ClassInfo数据.
     *
     * @param className className
     * @return int cid
     */
    public int getClassIdByClassName(String className) {
        Connection conn = getConnection("ClassInfo");
        PreparedStatement ps = null;
        int cId = 0;
        try {
            String sql = "select cId from ClassInfo where className = '" + className + "'";
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            if (rs.next()) {
                cId = rs.getInt("cId");
            }
            return cId;
        } catch (SQLException throwables) {
            LOGGER.info("memoryHeapInfo Exception {}", throwables.getMessage());
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
        return cId;
    }

    /**
     * 删除一次场景下的堆信息数据
     *
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean deleteSessionData(long sessionId) {
        StringBuffer deleteSql = new StringBuffer("DELETE FROM ClassInfo");
        Connection connection = DataBaseApi.getInstance().getConnectByTable("ClassInfo").get();
        return execute(connection, deleteSql.toString());
    }
}
