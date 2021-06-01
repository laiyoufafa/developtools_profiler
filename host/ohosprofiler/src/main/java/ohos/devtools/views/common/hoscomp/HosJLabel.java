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

package ohos.devtools.views.common.hoscomp;

import ohos.devtools.datasources.utils.session.entity.SessionInfo;

import javax.swing.JLabel;

/**
 * @Description HosJLabel
 * @Date 2021/3/11 13:12
 **/
public class HosJLabel extends JLabel {
    private SessionInfo sessionInfo;

    private long sessionId;

    private String deviceName;

    private String processName;

    private String message;

    private Long firstStamp;

    private boolean deviceType = false;
    private long startTime;
    private long endTime;

    public boolean getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(boolean deviceType) {
        this.deviceType = deviceType;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public HosJLabel(String text) {
        super(text);
    }

    public HosJLabel() {
        super("", null, LEADING);
    }

    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    public void setSessionInfo(SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public Long getFirstStamp() {
        return firstStamp;
    }

    public void setFirstStamp(Long firstStamp) {
        this.firstStamp = firstStamp;
    }
}
