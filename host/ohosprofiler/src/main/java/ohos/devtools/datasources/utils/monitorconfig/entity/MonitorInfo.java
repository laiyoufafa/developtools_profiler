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

package ohos.devtools.datasources.utils.monitorconfig.entity;

import java.util.Objects;

/**
 * MonitorInfo
 */
public final class MonitorInfo {
    private long localSessionId;
    private String monitorType;
    private String parameter;
    private String value;

    /**
     * builder
     *
     * @return Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    private MonitorInfo(Builder builder) {
        localSessionId = builder.localSessionId;
        monitorType = builder.monitorType;
        parameter = builder.parameter;
        value = builder.value;
    }

    public long getLocalSessionId() {
        return localSessionId;
    }

    public void setLocalSessionId(long id) {
        this.localSessionId = id;
    }

    public String getMonitorType() {
        return monitorType;
    }

    public void setMonitorType(String type) {
        this.monitorType = type;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String param) {
        this.parameter = param;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String worth) {
        this.value = worth;
    }

    @Override
    public String toString() {
        return "MonitorInfo{" + "localSessionId=" + localSessionId + ", monitorType='" + monitorType + '\''
            + ", parameter='" + parameter + '\'' + ", value='" + value + '\'' + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        MonitorInfo that = null;
        if (object instanceof MonitorInfo) {
            that = (MonitorInfo) object;
        }
        if (that == null) {
            return true;
        }
        return localSessionId == that.localSessionId && monitorType.equals(that.monitorType) && parameter
            .equals(that.parameter) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localSessionId, monitorType, parameter, value);
    }

    /**
     * Builder
     */
    public static final class Builder {
        private long localSessionId;
        private String monitorType;
        private String parameter;
        private String value;

        private Builder() {
        }

        /**
         * localSessionId
         *
         * @param id id
         * @return Builder
         */
        public Builder localSessionId(long id) {
            this.localSessionId = id;
            return this;
        }

        /**
         * monitorType
         *
         * @param type type
         * @return Builder
         */
        public Builder monitorType(String type) {
            this.monitorType = type;
            return this;
        }

        /**
         * 参数
         *
         * @param param param
         * @return Builder
         */
        public Builder parameter(String param) {
            this.parameter = param;
            return this;
        }

        /**
         * value
         *
         * @param worth worth
         * @return Builder
         */
        public Builder value(String worth) {
            this.value = worth;
            return this;
        }

        /**
         * MonitorInfo
         *
         * @return build
         */
        public MonitorInfo build() {
            return new MonitorInfo(this);
        }
    }

}
