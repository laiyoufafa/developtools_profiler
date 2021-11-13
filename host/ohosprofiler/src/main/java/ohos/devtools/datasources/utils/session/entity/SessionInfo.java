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

package ohos.devtools.datasources.utils.session.entity;

import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;

/**
 * SessionInfo实体
 */
public class SessionInfo {
    private String sessionName;
    private int sessionId;
    private long startTimestamp;
    private long endTimestamp;
    private long streamId;
    private int pid;
    private String processName;
    private boolean startRefsh;
    private boolean offlineMode;
    private final DeviceIPPortInfo deviceIPPortInfo;
    private ProcessInfo processInfo;

    private SessionInfo(Builder builder) {
        sessionName = builder.sessionName;
        sessionId = builder.sessionId;
        startTimestamp = builder.startTimestamp;
        endTimestamp = builder.endTimestamp;
        streamId = builder.streamId;
        pid = builder.pid;
        processName = builder.processName;
        deviceIPPortInfo = builder.deviceIPPortInfo;
        processInfo = builder.processInfo;
    }

    /**
     * builder
     *
     * @return Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public DeviceIPPortInfo getDeviceIPPortInfo() {
        return deviceIPPortInfo;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp == 0 ? Long.MAX_VALUE : endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public long getStreamId() {
        return streamId;
    }

    public void setStreamId(long streamId) {
        this.streamId = streamId;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public boolean isOfflineMode() {
        return offlineMode;
    }

    public void setOfflineMode(boolean offlineMode) {
        this.offlineMode = offlineMode;
    }

    public boolean isStartRefsh() {
        return startRefsh;
    }

    public void setStartRefsh(boolean startRefsh) {
        this.startRefsh = startRefsh;
    }

    public ProcessInfo getProcessInfo() {
        return processInfo;
    }

    public void setProcessInfo(ProcessInfo processInfo) {
        this.processInfo = processInfo;
    }

    /**
     * Builder
     */
    public static class Builder {
        private String sessionName;
        private int sessionId;
        private long startTimestamp;
        private long endTimestamp;
        private long streamId;
        private int pid;
        private String processName;
        private DeviceIPPortInfo deviceIPPortInfo;
        private ProcessInfo processInfo;

        private Builder() {
        }

        /**
         * sessionName
         *
         * @param sessionName sessionName
         * @return Builder
         */
        public Builder sessionName(String sessionName) {
            this.sessionName = sessionName;
            return this;
        }

        /**
         * sessionId
         *
         * @param sessionId sessionId
         * @return Builder
         */
        public Builder sessionId(int sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        /**
         * startTimestamp
         *
         * @param startTimestamp startTimestamp
         * @return Builder
         */
        public Builder startTimestamp(long startTimestamp) {
            this.startTimestamp = startTimestamp;
            return this;
        }

        /**
         * endTimestamp
         *
         * @param endTimestamp endTimestamp
         * @return Builder
         */
        public Builder endTimestamp(long endTimestamp) {
            this.endTimestamp = endTimestamp;
            return this;
        }

        /**
         * streamId
         *
         * @param streamId streamId
         * @return Builder
         */
        public Builder streamId(long streamId) {
            this.streamId = streamId;
            return this;
        }

        /**
         * 获取pid信息方法
         *
         * @param pid pid
         * @return Builder
         */
        public Builder pid(int pid) {
            this.pid = pid;
            return this;
        }

        /**
         * 获取processName信息方法
         *
         * @param processName processName
         * @return Builder
         */
        public Builder processName(String processName) {
            this.processName = processName;
            return this;
        }

        /**
         * 设备IP和端口号信息
         *
         * @param deviceIPPortInfo deviceIPPortInfo
         * @return Builder
         */
        public Builder deviceIPPortInfo(DeviceIPPortInfo deviceIPPortInfo) {
            this.deviceIPPortInfo = deviceIPPortInfo;
            return this;
        }

        /**
         * Process information
         *
         * @param processInfo processInfo
         * @return Builder
         */
        public Builder processInfo(ProcessInfo processInfo) {
            this.processInfo = processInfo;
            return this;
        }

        /**
         * build方法
         *
         * @return SessionInfo
         */
        public SessionInfo build() {
            return new SessionInfo(this);
        }
    }
}
