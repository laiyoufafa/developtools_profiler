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

package com.openharmony.devices;

/**
 * DeviceState for device connect info ¡¢create at 20210912
 */
public class DeviceState {
    private String serialNumber = "";
    private String type = "";
    private String status = "";
    private String address = "";

    /**
     * get DeviceState
     *
     * @param serialNumber device id
     * @param testMode test mode
     */
    public DeviceState(String serialNo , boolean testMode) {
        if (serialNo == null || isUnValid(serialNo)) {
            serialNumber = "TestModeDeviceID";
        }
        if (testMode) {
            setSerialNumber(serialNumber);
            setConnection("USB");
            setStatus("Connected");
            setAddress("localhost");
        } else {
            setSerialNumber(serialNo);
        }
    }

    /**
     * get device id
     *
     * @return serialNumber
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * setSerialNumber
     *
     * @param serialNumber device id
     */
    protected void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * getConnection type
     *
     * @return type
     */
    public String getConnection() {
        return type;
    }

    /**
     * setConnection type
     *
     * @param type connect type
     */
    protected void setConnection(String type) {
        this.type = type;
    }

    /**
     * get device status
     *
     * @return device status
     */
    public String getStatus() {
        return status;
    }

    /**
     * set device status
     *
     * @param status device status
     */
    protected void setStatus(String status) {
        this.status = status;
    }

    /**
     * get device connect address
     *
     * @return adress device connect address
     */
    public String getAddress() {
        return address;
    }

    /**
     * set address
     *
     * @param address device connect address
     */
    protected void setAddress(String address) {
        this.address = address;
    }

    /**
     * state toString
     *
     * @return adress device info
     */
    @Override
    public String toString() {
        return "DeviceState [key=" + serialNumber + ", connect type=" + type + ", status=" + status + ", address="
                + address + "]";
    }

    private static boolean isUnValid(String string) {
        return string.trim().length() == 0;
    }
}
