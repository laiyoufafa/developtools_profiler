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

package ohos.devtools.services.distribute;

import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;

import java.util.Objects;

/**
 * DistributeDevice
 *
 * @since: 2021/9/20
 */
public class DistributeDevice {
    private DeviceIPPortInfo deviceIPPortInfo;
    private String processName;
    private String sessionId;

    /**
     * DistributeDevice
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param processName processName
     */
    public DistributeDevice(DeviceIPPortInfo deviceIPPortInfo, String processName) {
        this.deviceIPPortInfo = deviceIPPortInfo;
        this.processName = processName;
    }

    /**
     * getDeviceIPPortInfo
     *
     * @return DeviceIPPortInfo
     */
    public DeviceIPPortInfo getDeviceIPPortInfo() {
        return deviceIPPortInfo;
    }

    /**
     * getProcessName
     *
     * @return String
     */
    public String getProcessName() {
        return processName;
    }

    /**
     * getSessionId
     *
     * @return String
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * setSessionId
     *
     * @param sessionId sessionId
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        DistributeDevice that = null;
        boolean flag = false;
        if (object instanceof DistributeDevice) {
            that = (DistributeDevice) object;
            flag =
                Objects.equals(deviceIPPortInfo, that.deviceIPPortInfo) && Objects.equals(processName, that.processName)
                    && Objects.equals(sessionId, that.sessionId);
        }
        return flag;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceIPPortInfo, processName, sessionId);
    }

    @Override
    public String toString() {
        return "DistributeDevice{"
            + "deviceIPPortInfo="
            + deviceIPPortInfo
            + ", processName='"
            + processName
            + '\''
            + ", sessionId='"
            + sessionId
            + '\''
            + '}';
    }
}
