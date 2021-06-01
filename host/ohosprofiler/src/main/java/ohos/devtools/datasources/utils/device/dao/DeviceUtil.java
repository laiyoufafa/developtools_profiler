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
import ohos.devtools.datasources.databases.databasepool.SqlRunnable;
import ohos.devtools.datasources.utils.common.util.PrintUtil;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Device-related execution sql class
 *
 * @version 1.0
 * @date 2021/2/2 19:02
 **/
public class DeviceUtil extends AbstractDataStore {
    private static final Logger LOGGER = LogManager.getLogger(DeviceUtil.class);
    private SqlRunnable sqlRunnable = new SqlRunnable();

    /**
     * Add device information
     *
     * @param info info
     */
    public void insertDeviceInfo(DeviceInfo info) {
        boolean bool = insert(info);
        if (bool) {
            PrintUtil.print(LOGGER, "Successfully added data to device table DeviceInfo", 1);
        } else {
            PrintUtil.print(LOGGER, "Failed added data to device table DeviceInfo", 1);
        }
    }

    /**
     * insertDeviceIPPortInfo
     *
     * @param info info
     */
    public void insertDeviceIPPortInfo(DeviceIPPortInfo info) {
        boolean bool = insert(info);
        if (bool) {
            PrintUtil.print(LOGGER, "Successfully added data to device table DeviceIPPortInfo", 1);
        } else {
            PrintUtil.print(LOGGER, "Failed added data to device table DeviceIPPortInfo", 1);
        }
    }

    /**
     * deleteExceptDeviceIPPort
     *
     * @param list list
     */
    public void deleteExceptDeviceIPPort(ArrayList<DeviceIPPortInfo> list) {
        Connection conn = null;
        String str = "";
        Optional<Connection> deviceIPPort = getConnectByTable("DeviceIPPortInfo");
        if (deviceIPPort.isPresent()) {
            conn = deviceIPPort.get();
        }
        for (DeviceIPPortInfo info : list) {
            str = str + " '" + info.getDeviceID() + "',";
        }
        str = str.substring(0, str.length() - 1);
        String delSql = "delete from DeviceIPPortInfo where deviceID not in (" + str + ");";
        LOGGER.debug("deleteExceptDeviceIPPort = {}", delSql);
        execute(conn, delSql);
    }

    /**
     * Delete all device IP and port number information
     */
    public void deleteAllDeviceIPPortInfo() {
        Connection conn = null;
        Optional<Connection> deviceIPPort = getConnectByTable("DeviceIPPortInfo");
        if (deviceIPPort.isPresent()) {
            conn = deviceIPPort.get();
        }
        String sql = "delete from DeviceIPPortInfo";
        execute(conn, sql);
    }

    /**
     * hasDevice
     *
     * @param serialNumber serialNumber
     * @return boolean
     */
    public boolean hasDevice(String serialNumber) {
        Connection conn = null;
        Optional<Connection> deviceIPPort = getConnectByTable("DeviceInfo");
        if (deviceIPPort.isPresent()) {
            conn = deviceIPPort.get();
        }
        boolean flag = false;
        try {
            Statement pstmt = conn.createStatement();
            String sql = "select count(1) hasDevice from DeviceInfo where deviceID = " + "'" + serialNumber + "';";
            LOGGER.debug("hasDevice = {}", sql);
            ResultSet resultSet = pstmt.executeQuery(sql);
            String hasDevice = "";
            while (resultSet.next()) {
                hasDevice = resultSet.getString("hasDevice");
            }
            if (!"".equals(hasDevice) && Integer.parseInt(hasDevice) > 0) {
                flag = true;
            }
            sqlRunnable.close(pstmt, resultSet, conn);
        } catch (SQLException exception) {
            LOGGER.error("SQLException error: {}", exception.getMessage());
        }

        return flag;
    }

    /**
     * Is there a device IP and port number
     *
     * @param serialNumber serialNumber
     * @return boolean
     */
    public boolean hasDeviceIPPort(String serialNumber) {
        Connection conn = null;
        Optional<Connection> deviceIPPort = getConnectByTable("DeviceIPPortInfo");
        if (deviceIPPort.isPresent()) {
            conn = deviceIPPort.get();
        }
        Statement pstmt = null;
        ResultSet resultSet = null;
        boolean flag = false;
        try {
            pstmt = conn.createStatement();
            String sql =
                "select count(1) hasDevice from DeviceIPPortInfo where deviceID = " + "'" + serialNumber + "';";
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
            LOGGER.error("sqlException error: {}", sqlException.getMessage());
        } finally {
            sqlRunnable.close(pstmt, resultSet, conn);
        }
        return flag;
    }

    /**
     * Get all device information
     *
     * @return List<DeviceInfo>
     */
    public List<DeviceInfo> getAllDeviceInfo() {
        List<DeviceInfo> list = select(DeviceInfo.class, null, null);
        return list;
    }

    /**
     * get All Device IP Port Info
     *
     * @return getAllDeviceIPPortInfo
     */
    public List<DeviceIPPortInfo> getAllDeviceIPPortInfos() {
        List<DeviceIPPortInfo> list = select(DeviceIPPortInfo.class, null, null);
        return list;
    }

    /**
     * getDeviceIPPortInfo
     *
     * @param deviceID deviceID
     * @return DeviceIPPortInfo
     */
    public DeviceIPPortInfo getDeviceIPPortInfo(String deviceID) {
        Map<String, Object> hashMap = new HashMap<>();
        hashMap.put("deviceID", deviceID);
        List<DeviceIPPortInfo> list = select(DeviceIPPortInfo.class, null, hashMap);
        return list.get(0);
    }
}
