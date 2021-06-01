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

/**
 * Device Info
 *
 * @version 1.0
 * @date 2021/03/4 10:55
 **/
public class DeviceInfo {
    private String deviceID;
    private String deviceName;
    private String ramInfo;
    private String romInfo;

    /**
     * set Device ID
     *
     * @param deviceID deviceID
     */
    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    /**
     * set Device Name
     *
     * @param deviceName deviceName
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * set RamInfo
     *
     * @param ramInfo ramInfo
     */
    public void setRamInfo(String ramInfo) {
        this.ramInfo = ramInfo;
    }

    /**
     * set RomInfo
     *
     * @param romInfo romInfo
     */
    public void setRomInfo(String romInfo) {
        this.romInfo = romInfo;
    }

    /**
     * get DeviceID
     *
     * @return String
     */
    public String getDeviceID() {
        return deviceID;
    }

    /**
     * get DeviceName
     *
     * @return String
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * String
     *
     * @return getRamInfo
     */
    public String getRamInfo() {
        return ramInfo;
    }

    /**
     * String
     *
     * @return getRomInfo
     */
    public String getRomInfo() {
        return romInfo;
    }

    @Override
    public String toString() {
        return "DeviceInfo{" + "deviceID='" + deviceID + '\'' + ", deviceName='" + deviceName + '\'' + ", RAMInfo='"
            + ramInfo + '\'' + ", ROMInfo='" + romInfo + '\'' + '}';
    }
}
