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

package ohos.devtools.datasources.utils.device.dao;

import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.utils.common.util.CloseResourceUtil;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.commons.lang.StringUtils;
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

import static ohos.devtools.datasources.utils.device.entity.DeviceType.FULL_HOS_DEVICE;
import static ohos.devtools.datasources.utils.device.entity.DeviceType.LEAN_HOS_DEVICE;

/**
 * Device-related execution sql class
 */
public class DeviceDao extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(DeviceDao.class);
    private static final String DEVICE_TABLE = "DeviceIPPortInfo";

    /**
     * insertDeviceIPPortInfo
     *
     * @param info info
     */
    public void insertDeviceIPPortInfo(DeviceIPPortInfo info) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertDeviceIPPortInfo");
        }
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            Optional<Connection> optionalConnection = getConnectByTable(DEVICE_TABLE);
            if (optionalConnection.isPresent()) {
                conn = optionalConnection.get();
                String sql = "insert into "
                        + "DeviceIPPortInfo ("
                        + "deviceID,"
                        + "deviceName,"
                        + "ip,"
                        + "deviceType,"
                        + "connectType,"
                        + "deviceStatus,"
                        + "retryNum,"
                        + "port,"
                        + "forwardPort) "
                        + "values "
                        + "(?,?,?,?,?,?,?,?,?)";
                ps = conn.prepareStatement(sql);
                ps.setString(1, info.getDeviceID());
                ps.setString(2, info.getDeviceName());
                ps.setString(3, info.getIp());
                ps.setString(4, info.getDeviceType().getCpuAbi());
                ps.setString(5, info.getConnectType());
                ps.setInt(6, info.getDeviceStatus());
                ps.setInt(7, info.getRetryNum());
                ps.setInt(8, info.getPort());
                ps.setInt(9, info.getForwardPort());
                ps.executeUpdate();
            }
        } catch (SQLException sqlException) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("sql Exception ", sqlException.getMessage());
            }
        } finally {
            CloseResourceUtil.closeResource(LOGGER, conn, ps, null);
        }
    }

    /**
     * deleteExceptDeviceIPPort
     *
     * @param list List<DeviceIPPortInfo>
     * @return List <DeviceIPPortInfo>
     */
    public List<DeviceIPPortInfo> selectOfflineDevice(List<DeviceIPPortInfo> list) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("selectOfflineDevice");
        }
        StringBuilder str = new StringBuilder();
        for (DeviceIPPortInfo info : list) {
            str.append(" '").append(info.getDeviceID()).append("',");
        }
        String selectSql;
        if (StringUtils.isNotBlank(str.toString())) {
            str = new StringBuilder(str.substring(0, str.length() - 1));
            selectSql = "select "
                    + "*  "
                    + "from "
                    + "DeviceIPPortInfo "
                    + "where "
                    + "deviceID "
                    + "not in "
                    + "(" + str + ");";
        } else {
            selectSql = "select "
                    + "*  "
                    + "from "
                    + "DeviceIPPortInfo";
        }
        PreparedStatement statement = null;
        Connection connection = null;
        ResultSet rs = null;
        List<DeviceIPPortInfo> deviceIPPortInfoList = new ArrayList<>();
        try {
            Optional<Connection> optionalConnection = getConnectByTable(DEVICE_TABLE);
            if (optionalConnection.isPresent()) {
                connection = optionalConnection.get();
                statement = connection.prepareStatement(selectSql);
                rs = statement.executeQuery();
                while (rs.next()) {
                    addDeviceIPPortInfoList(deviceIPPortInfoList, rs);
                }
            }
        } catch (SQLException sqlException) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("sql Exception ", sqlException);
            }
        } finally {
            close(statement, rs, connection);
        }
        return deviceIPPortInfoList;
    }

    /**
     * addDeviceIPPortInfoList
     *
     * @param deviceIPPortInfoList List<DeviceIPPortInfo>
     * @param rs rs
     * @throws SQLException SQLException
     */
    private void addDeviceIPPortInfoList(List<DeviceIPPortInfo> deviceIPPortInfoList, ResultSet rs)
        throws SQLException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addDeviceIPPortInfoList");
        }
        DeviceIPPortInfo deviceInfo = new DeviceIPPortInfo();
        String deviceID = rs.getString("deviceID");
        String deviceName = rs.getString("deviceName");
        String ip = rs.getString("ip");
        String deviceType = rs.getString("deviceType");
        deviceInfo.setDeviceID(deviceID);
        deviceInfo.setDeviceName(deviceName);
        deviceInfo.setIp(ip);
        if (deviceType.equals(FULL_HOS_DEVICE.getCpuAbi())) {
            deviceInfo.setDeviceType(FULL_HOS_DEVICE);
        } else {
            deviceInfo.setDeviceType(LEAN_HOS_DEVICE);
        }
        String connectType = rs.getString("connectType");
        deviceInfo.setConnectType(connectType);
        int deviceStatus = rs.getInt("deviceStatus");
        deviceInfo.setDeviceStatus(deviceStatus);
        int retryNum = rs.getInt("retryNum");
        deviceInfo.setRetryNum(retryNum);
        int port = rs.getInt("port");
        deviceInfo.setPort(port);
        int forwardPort = rs.getInt("forwardPort");
        deviceInfo.setOfflineCount(rs.getInt("offlineCount"));
        deviceInfo.setForwardPort(forwardPort);
        deviceIPPortInfoList.add(deviceInfo);
    }

    /**
     * deleteExceptDeviceIPPort
     *
     * @param deviceIPPortInfo List<DeviceIPPortInfo>
     */
    public void deleteOfflineDeviceIPPort(DeviceIPPortInfo deviceIPPortInfo) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deleteOfflineDeviceIPPort");
        }
        Optional<Connection> connection = getConnectByTable(DEVICE_TABLE);
        if (connection.isPresent()) {
            Connection conn = connection.get();
            String delSql = "delete from "
                    + "DeviceIPPortInfo "
                    + "where "
                    + "deviceID = "
                    + "'" + deviceIPPortInfo.getDeviceID() + "'";
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("deleteExceptDeviceIPPort = {}", delSql);
            }
            execute(conn, delSql);
        }
    }

    /**
     * updateDeviceIPPortInfo
     *
     * @param offlineCount offlineCount
     * @param deviceId deviceId
     * @return boolean boolean
     */
    public boolean updateDeviceIPPortInfo(int offlineCount, String deviceId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("updateDeviceIPPortInfo");
        }
        StringBuilder sql = new StringBuilder("update DeviceIPPortInfo set ");
        if (offlineCount >= 0) {
            sql.append("offlineCount = ").append(offlineCount);
        }
        sql.append(" where deviceID = '").append(deviceId).append("'");
        Statement statement = null;
        Connection conn = null;
        try {
            Optional<Connection> optionalConnection = getConnectByTable(DEVICE_TABLE);
            if (optionalConnection.isPresent()) {
                conn = optionalConnection.get();
                statement = conn.createStatement();
                int executeUpdate = statement.executeUpdate(sql.toString());
                return executeUpdate > 0;
            }
        } catch (SQLException sqlException) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("sql Exception ", sqlException.getMessage());
            }
            return false;
        } finally {
            close(statement, conn);
        }
        return false;
    }

    /**
     * deleteExceptDeviceIPPort
     *
     * @param list list
     */
    public void deleteExceptDeviceIPPort(List<DeviceIPPortInfo> list) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deleteExceptDeviceIPPort");
        }
        StringBuilder str = new StringBuilder();
        Optional<Connection> deviceIPPort = getConnectByTable(DEVICE_TABLE);
        if (deviceIPPort.isPresent()) {
            Connection conn = deviceIPPort.get();
            for (DeviceIPPortInfo info : list) {
                str.append(" '").append(info.getDeviceID()).append("',");
            }
            str = new StringBuilder(str.substring(0, str.length() - 1));
            String delSql = "delete from "
                    + "DeviceIPPortInfo "
                    + "where "
                    + "deviceID "
                    + "not in "
                    + "(" + str + ");";
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("deleteExceptDeviceIPPort = {}", delSql);
            }
            execute(conn, delSql);
        }
    }

    /**
     * updateDeviceIPPortInfo
     *
     * @param deviceStatus deviceStatus
     * @param retryCount retryCount
     * @param deviceId deviceId
     * @return boolean
     */
    public boolean updateDeviceIPPortInfo(int deviceStatus, int retryCount, String deviceId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("updateDeviceIPPortInfo");
        }
        StringBuilder sql = new StringBuilder("update DeviceIPPortInfo set ");
        if (deviceStatus >= 0) {
            sql.append("deviceStatus = ").append(deviceStatus).append(",");
        }
        if (retryCount > -1) {
            sql.append("retryNum = ").append(retryCount);
        }
        sql.append(" where deviceID = '").append(deviceId).append("'");
        Statement statement = null;
        Connection conn = null;
        try {
            Optional<Connection> optionalConnection = getConnectByTable(DEVICE_TABLE);
            if (optionalConnection.isPresent()) {
                conn = optionalConnection.get();
                statement = conn.createStatement();
                int executeUpdate = statement.executeUpdate(sql.toString());
                return executeUpdate > 0;
            }
        } catch (SQLException sqlException) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("sql Exception ", sqlException.getMessage());
            }
            return false;
        } finally {
            close(statement, conn);
        }
        return false;
    }

    /**
     * updateDeviceInfo
     *
     * @param ip ip
     * @param port port
     * @param forwardPort forwardPort
     * @param deviceId deviceId
     * @return boolean
     */
    public boolean updateDeviceInfo(String ip, int port, int forwardPort, String deviceId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("updateDeviceInfo");
        }
        StringBuilder sql =
            new StringBuilder("update DeviceIPPortInfo set ip = '").append(ip).append("',").append(" port = ")
                .append(port).append(",").append("forwardPort = ").append(forwardPort).append(" where deviceID = '")
                .append(deviceId).append("'");
        Statement statement = null;
        Connection conn = null;
        try {
            Optional<Connection> optionalConnection = getConnectByTable(DEVICE_TABLE);
            if (optionalConnection.isPresent()) {
                conn = optionalConnection.get();
                statement = conn.createStatement();
                int executeUpdate = statement.executeUpdate(sql.toString());
                return executeUpdate > 0;
            }
        } catch (SQLException sqlException) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("update DeviceInfo failed ", sqlException.getMessage());
            }
            return false;
        } finally {
            close(statement, conn);
        }
        return false;
    }

    /**
     * Delete all device IP and port number information
     */
    public void deleteAllDeviceIPPortInfo() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deleteAllDeviceIPPortInfo");
        }
        Optional<Connection> deviceIPPort = getConnectByTable(DEVICE_TABLE);
        if (deviceIPPort.isPresent()) {
            Connection conn = deviceIPPort.get();
            String sql = "delete from DeviceIPPortInfo";
            execute(conn, sql);
        }
    }

    /**
     * Is there a device IP and port number
     *
     * @param serialNumber serialNumber
     * @return boolean
     */
    public boolean hasDeviceIPPort(String serialNumber) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("hasDeviceIPPort");
        }
        Optional<Connection> deviceIPPort = getConnectByTable(DEVICE_TABLE);
        boolean flag = false;
        if (deviceIPPort.isPresent()) {
            Connection conn = deviceIPPort.get();
            Statement pstmt = null;
            ResultSet resultSet = null;
            try {
                pstmt = conn.createStatement();
                String sql = "select "
                        + "count(1) "
                        + "as "
                        + "hasDevice "
                        + "from "
                        + "DeviceIPPortInfo "
                        + "where "
                        + "deviceID = "
                        + "" + "'" + serialNumber + "';";
                LOGGER.debug("hasDevice = {}", sql);
                resultSet = pstmt.executeQuery(sql);
                String hasDevice = "";
                while (resultSet.next()) {
                    hasDevice = resultSet.getString("hasDevice");
                }
                if (!"".equals(hasDevice) && Integer.parseInt(hasDevice) > 0) {
                    flag = true;
                }
            } catch (SQLException sqlException) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("sqlException error ", sqlException.getMessage());
                }
            } finally {
                close(pstmt, resultSet, conn);
            }
        }
        return flag;
    }

    /**
     * get All Device IP Port Info
     *
     * @return List <DeviceIPPortInfo>
     */
    public List<DeviceIPPortInfo> getAllDeviceIPPortInfos() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getAllDeviceIPPortInfos");
        }
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<DeviceIPPortInfo> deviceIPPortInfos = new ArrayList<>();
        try {
            Optional<Connection> optionalConnection = getConnectByTable(DEVICE_TABLE);
            if (optionalConnection.isPresent()) {
                conn = optionalConnection.get();
                String sql = "select * from DeviceIPPortInfo";
                ps = conn.prepareStatement(sql);
                rs = ps.executeQuery();
                DeviceIPPortInfo deviceIPPortInfo;
                while (rs.next()) {
                    deviceIPPortInfo = new DeviceIPPortInfo();
                    String deviceID = rs.getString("deviceID");
                    String deviceName = rs.getString("deviceName");
                    String ip = rs.getString("ip");
                    String deviceType = rs.getString("deviceType");
                    deviceIPPortInfo.setDeviceID(deviceID);
                    deviceIPPortInfo.setDeviceName(deviceName);
                    deviceIPPortInfo.setIp(ip);
                    if (deviceType.equals(FULL_HOS_DEVICE.getCpuAbi())) {
                        deviceIPPortInfo.setDeviceType(FULL_HOS_DEVICE);
                    } else {
                        deviceIPPortInfo.setDeviceType(LEAN_HOS_DEVICE);
                    }
                    String connectType = rs.getString("connectType");
                    deviceIPPortInfo.setConnectType(connectType);
                    int port = rs.getInt("port");
                    deviceIPPortInfo.setPort(port);
                    int forwardPort = rs.getInt("forwardPort");
                    deviceIPPortInfo.setForwardPort(forwardPort);
                    deviceIPPortInfos.add(deviceIPPortInfo);
                }
                return deviceIPPortInfos;
            }

        } catch (SQLException sqlException) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("sqlException error ", sqlException.getMessage());
            }
        } finally {
            close(ps, rs, conn);
        }
        return deviceIPPortInfos;
    }

    /**
     * get All Device IP Port Info
     *
     * @return List <DeviceIPPortInfo>
     */
    public List<DeviceIPPortInfo> getOnlineDeviceInfoList() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getOnlineDeviceInfoList");
        }
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<DeviceIPPortInfo> deviceIPPortInfos = new ArrayList<>();
        try {
            Optional<Connection> optionalConnection = getConnectByTable(DEVICE_TABLE);
            if (optionalConnection.isPresent()) {
                conn = optionalConnection.get();
                String sql = "select "
                        + "* "
                        + "from "
                        + "DeviceIPPortInfo "
                        + "where "
                        + "deviceStatus = 1";
                ps = conn.prepareStatement(sql);
                rs = ps.executeQuery();
                DeviceIPPortInfo deviceIPPortInfo;
                while (rs.next()) {
                    deviceIPPortInfo = new DeviceIPPortInfo();
                    String deviceType = rs.getString("deviceType");
                    deviceIPPortInfo.setDeviceID(rs.getString("deviceID"));
                    deviceIPPortInfo.setDeviceName(rs.getString("deviceName"));
                    deviceIPPortInfo.setIp(rs.getString("ip"));
                    if (deviceType.equals(FULL_HOS_DEVICE.getCpuAbi())) {
                        deviceIPPortInfo.setDeviceType(FULL_HOS_DEVICE);
                    } else {
                        deviceIPPortInfo.setDeviceType(LEAN_HOS_DEVICE);
                    }
                    String connectType = rs.getString("connectType");
                    deviceIPPortInfo.setConnectType(connectType);
                    int port = rs.getInt("port");
                    deviceIPPortInfo.setPort(port);
                    deviceIPPortInfo.setForwardPort(rs.getInt("forwardPort"));
                    deviceIPPortInfos.add(deviceIPPortInfo);
                }
                return deviceIPPortInfos;
            }
        } catch (SQLException sqlException) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("sqlException error ", sqlException.getMessage());
            }
        } finally {
            close(ps, rs, conn);
        }
        return deviceIPPortInfos;
    }

    /**
     * getDeviceIPPortInfo
     *
     * @param deviceID deviceID
     * @return Optional <DeviceIPPortInfo>
     */
    public Optional<DeviceIPPortInfo> getDeviceIPPortInfo(String deviceID) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getDeviceIPPortInfo");
        }
        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            Optional<Connection> optionalConnection = getConnectByTable(DEVICE_TABLE);
            if (optionalConnection.isPresent()) {
                conn = optionalConnection.get();
                String sql = "select "
                        + "* "
                        + "from "
                        + "DeviceIPPortInfo "
                        + "where "
                        + "deviceID = "
                        + "'" + deviceID + "'";
                statement = conn.createStatement();
                rs = statement.executeQuery(sql);
                DeviceIPPortInfo deviceIPPortInfo = null;
                while (rs.next()) {
                    deviceIPPortInfo = new DeviceIPPortInfo();
                    String deviceType = rs.getString("deviceType");
                    deviceIPPortInfo.setDeviceID(rs.getString("deviceID"));
                    deviceIPPortInfo.setDeviceName(rs.getString("deviceName"));
                    deviceIPPortInfo.setIp(rs.getString("ip"));
                    if (deviceType.equals(FULL_HOS_DEVICE.getCpuAbi())) {
                        deviceIPPortInfo.setDeviceType(FULL_HOS_DEVICE);
                    } else {
                        deviceIPPortInfo.setDeviceType(LEAN_HOS_DEVICE);
                    }
                    deviceIPPortInfo.setConnectType(rs.getString("connectType"));
                    deviceIPPortInfo.setPort(rs.getInt("port"));
                    deviceIPPortInfo.setForwardPort(rs.getInt("forwardPort"));
                    deviceIPPortInfo.setRetryNum(rs.getInt("retryNum"));
                }
                return Optional.ofNullable(deviceIPPortInfo);
            }
        } catch (SQLException sqlException) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("sqlException error ", sqlException);
            }
        } finally {
            close(statement, rs, conn);
        }
        return Optional.empty();
    }
}
