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
 * Equipment process
 *
 * @version 1.0
 * @date 2021/2/2 19:02
 **/
public class DeviceProcessInfo implements Serializable {
    private static final long serialVersionUID = -3815785606619485252L;
    private String deviceName;
    private String processName;
    private long localSessionId;
    private String deviceType;
    private long startTime;
    private long endTime;

    /**
     * get DeviceName
     *
     * @return String
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * set DeviceName
     *
     * @param deviceName deviceName
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * get ProcessName
     *
     * @return String
     */
    public String getProcessName() {
        return processName;
    }

    /**
     * set ProcessName
     *
     * @param processName processName
     */
    public void setProcessName(String processName) {
        this.processName = processName;
    }

    /**
     * get Device Type
     *
     * @return String
     */
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * set Device Type
     *
     * @param deviceType deviceType
     */
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    /**
     * get LocalSession Id
     *
     * @return long
     */
    public long getLocalSessionId() {
        return localSessionId;
    }

    /**
     * set LocalSession Id
     *
     * @param localSessionId localSessionId
     */
    public void setLocalSessionId(long localSessionId) {
        this.localSessionId = localSessionId;
    }

    /**
     * get Start Time
     *
     * @return long
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * set Start Time
     *
     * @param startTime startTime
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * long
     *
     * @return getEndTime
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * set End Time
     *
     * @param endTime endTime
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
