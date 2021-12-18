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

package ohos.devtools.datasources.databases.datatable.enties;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * NetWorkInfo
 *
 * @since 2021/10/26
 */
public class NetWorkInfo implements Serializable {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 8995251835580792857L;

    private long localSessionId;
    private int sessionId;
    private long timeStamp;
    private BigDecimal sendSpeed;
    private BigDecimal receiveSpeed;
    private int energyNetworkData;

    public long getLocalSessionId() {
        return localSessionId;
    }

    public void setLocalSessionId(long localSessionId) {
        this.localSessionId = localSessionId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public BigDecimal getSendSpeed() {
        return sendSpeed;
    }

    public void setSendSpeed(BigDecimal sendSpeed) {
        this.sendSpeed = sendSpeed;
    }

    public BigDecimal getReceiveSpeed() {
        return receiveSpeed;
    }

    public void setReceiveSpeed(BigDecimal receiveSpeed) {
        this.receiveSpeed = receiveSpeed;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getEnergyNetworkData() {
        return energyNetworkData;
    }

    public void setEnergyNetworkData(int energyNetworkData) {
        this.energyNetworkData = energyNetworkData;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localSessionId, sessionId, timeStamp, sendSpeed, receiveSpeed, energyNetworkData);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "NetWorkInfo{"
            + "localSessionId="
            + localSessionId
            + ", sessionId=" + sessionId
            + ", timeStamp=" + timeStamp
            + ", sendSpeed=" + sendSpeed
            + ", receiveSpeed=" + receiveSpeed
            + ", energyNetworkData=" + energyNetworkData
            + '}';
    }
}
