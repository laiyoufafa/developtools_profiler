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

package ohos.devtools.datasources.utils.device.entity;

import java.io.Serializable;

/**
 * Device IP and port number information
 *
 * @version 1.0
 * @date 2021/2/2 19:02
 **/
public class DeviceIPPortInfo implements Serializable {
    private String deviceID;
    private String deviceName;
    private String ip;
    private int port;
    private String deviceType;
    private int forwardPort;

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getForwardPort() {
        return forwardPort;
    }

    public void setForwardPort(int forwardPort) {
        this.forwardPort = forwardPort;
    }

    @Override
    public String toString() {
        return "DeviceIPPortInfo{" + "deviceID='" + deviceID + '\'' + ", deviceName='" + deviceName + '\'' + ", ip='"
            + ip + '\'' + ", port=" + port + ", deviceType=" + deviceType + ", forwardPort=" + forwardPort + '}';
    }
}
