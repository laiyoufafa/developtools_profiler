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

package ohos.devtools.datasources.utils.plugin.entity;

import java.util.Arrays;

/**
 * 自研插件的实体类
 *
 * @version 1.0
 * @date 2021/2/2 19:03
 **/
public final class HiProfilerPlugin {
    // 设备id
    private String deviceId;

    // 插件的基本属性
    private int plugId;

    // 插件的名称
    private String name;

    // 插件的路径
    private String path;

    // 插件的状态
    private int status;

    // 插件的版本
    private String version;

    // 插件的签名
    private String plugSha256;

    // 插件的采集频率
    private int sampleInterval;

    // 插件的采集项配置
    private byte[] configData;

    /**
     * 构建方法
     *
     * @return Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public int getPlugId() {
        return plugId;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public int getStatus() {
        return status;
    }

    public String getPlugSha256() {
        return plugSha256;
    }

    public int getSampleInterval() {
        return sampleInterval;
    }

    public byte[] getConfigData() {
        if (configData != null) {
            return Arrays.copyOf(configData, configData.length);
        } else {
            return new byte[0];
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private HiProfilerPlugin(Builder builder) {
        deviceId = builder.deviceId;
        plugId = builder.plugId;
        name = builder.name;
        path = builder.path;
        status = builder.status;
        version = builder.version;
        plugSha256 = builder.plugSha256;
        sampleInterval = builder.sampleInterval;
        configData = builder.configData;
    }

    @Override
    public String toString() {
        return "HiProfilerPlugin{" + "deviceId='" + deviceId + '\'' + ", plugId=" + plugId + ", name='" + name + '\''
            + ", path='" + path + '\'' + ", status=" + status + ", version='" + version + '\'' + ", plugSha256='"
            + plugSha256 + '\'' + ", sampleInterval=" + sampleInterval + ", configData=" + Arrays.toString(configData)
            + '}';
    }

    /**
     * @ClassName: Builder
     * @Description: HiProfilerPlugin自研插件的build类
     * @Date: 2021/2/2 19:11
     */
    public static final class Builder {
        // 设备id
        private String deviceId;

        // 插件的id
        private int plugId;

        // 插件的名称
        private String name;

        // 插件的路径
        private String path;

        // 插件的状态
        private int status;

        // 插件的版本
        private String version;

        // 插件的签名
        private String plugSha256;

        // 插件的采集频率
        private int sampleInterval;

        // 插件的采集项配置
        private byte[] configData;

        private Builder() {
        }

        /**
         * deviceId
         *
         * @param id id
         * @return Builder
         */
        public Builder deviceId(String id) {
            this.deviceId = id;
            return this;
        }

        /**
         * id方法
         *
         * @param pluginId pluginId
         * @return Builder
         */
        public Builder id(int pluginId) {
            this.plugId = pluginId;
            return this;
        }

        /**
         * name
         *
         * @param plugName plugName
         * @return Builder
         */
        public Builder name(String plugName) {
            this.name = plugName;
            return this;
        }

        /**
         * 路径
         *
         * @param path path
         * @return Builder
         */
        public Builder path(String path) {
            this.path = path;
            return this;
        }

        /**
         * version
         *
         * @param plugVersion plugVersion
         * @return Builder
         */
        public Builder version(String plugVersion) {
            this.version = plugVersion;
            return this;
        }

        /**
         * 插件的状态
         *
         * @param plugStatus plugStatus
         * @return Builder
         */
        public Builder status(int plugStatus) {
            this.status = plugStatus;
            return this;
        }

        /**
         * plugSha256
         *
         * @param pluginSha256 pluginSha256
         * @return Builder
         */
        public Builder plugSha256(String pluginSha256) {
            this.plugSha256 = pluginSha256;
            return this;
        }

        /**
         * sampleInterval
         *
         * @param pluginSampleInterval pluginSampleInterval
         * @return Builder
         */
        public Builder sampleInterval(int pluginSampleInterval) {
            this.sampleInterval = pluginSampleInterval;
            return this;
        }

        /**
         * 配置数据
         *
         * @param data data
         * @return Builder
         */
        public Builder configData(byte[] data) {
            if (data != null) {
                this.configData = Arrays.copyOf(data, data.length);
            } else {
                this.configData = new byte[0];
            }
            return this;
        }

        /**
         * build
         *
         * @return HiProfilerPlugin
         */
        public HiProfilerPlugin build() {
            return new HiProfilerPlugin(this);
        }
    }
}
