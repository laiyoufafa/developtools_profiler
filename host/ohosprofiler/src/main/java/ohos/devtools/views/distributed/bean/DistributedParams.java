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

package ohos.devtools.views.distributed.bean;

/**
 * DistributedParams
 *
 * @since 2021/8/1 14:58
 */
public class DistributedParams {
    private String pkgNameA;
    private String pkgNameB;
    private Integer processIdA;
    private Integer processIdB;
    private String deviceNameA;
    private String deviceNameB;
    private String pathA;
    private String pathB;
    private Long offsetA;
    private Long offsetB;

    /**
     * DistributedParams const
     *
     * @param builder builder
     */
    public DistributedParams(Builder builder) {
        this.pkgNameA = builder.pkgNameA;
        this.pkgNameB = builder.pkgNameB;
        this.processIdA = builder.processIdA;
        this.processIdB = builder.processIdB;
        this.deviceNameA = builder.deviceNameA;
        this.deviceNameB = builder.deviceNameB;
        this.pathA = builder.pathA;
        this.pathB = builder.pathB;
        this.offsetA = builder.offsetA;
        this.offsetB = builder.offsetB;
    }

    /**
     * getPkgNameA
     *
     * @return pkgNameA pkgNameA
     */
    public String getPkgNameA() {
        return pkgNameA;
    }

    /**
     * getPkgNameB
     *
     * @return pkgNameB pkgNameB
     */
    public String getPkgNameB() {
        return pkgNameB;
    }

    /**
     * getProcessIdA
     *
     * @return processIdA processIdA
     */
    public Integer getProcessIdA() {
        return processIdA;
    }

    /**
     * getProcessIdB
     *
     * @return processIdB processIdB
     */
    public Integer getProcessIdB() {
        return processIdB;
    }

    /**
     * getDeviceNameA
     *
     * @return deviceNameA deviceNameA
     */
    public String getDeviceNameA() {
        return deviceNameA;
    }

    /**
     * getDeviceNameB
     *
     * @return deviceNameB deviceNameB
     */
    public String getDeviceNameB() {
        return deviceNameB;
    }

    /**
     * getPathA
     *
     * @return pathA pathA
     */
    public String getPathA() {
        return pathA;
    }

    /**
     * getPathB
     *
     * @return pathB pathB
     */
    public String getPathB() {
        return pathB;
    }

    /**
     * getOffsetA
     *
     * @return offsetA offsetA
     */
    public Long getOffsetA() {
        return offsetA;
    }

    /**
     * getOffsetB
     *
     * @return offsetB offsetB
     */
    public Long getOffsetB() {
        return offsetB;
    }

    /**
     * class Builder builder
     */
    public static class Builder {
        private String pkgNameA;
        private String pkgNameB;
        private String deviceNameA;
        private String deviceNameB;
        private Integer processIdA;
        private Integer processIdB;
        private String pathA;
        private String pathB;
        private Long offsetA;
        private Long offsetB;

        /**
         * setPkgNameA
         *
         * @param pkgNameA pkgNameA
         * @return Builder builder
         */
        public Builder setPkgNameA(String pkgNameA) {
            this.pkgNameA = pkgNameA;
            return this;
        }

        /**
         * setPkgNameB
         *
         * @param pkgNameB pkgNameB
         * @return Builder builder
         */
        public Builder setPkgNameB(String pkgNameB) {
            this.pkgNameB = pkgNameB;
            return this;
        }

        /**
         * setDeviceNameA
         *
         * @param deviceNameA deviceNameA
         * @return Builder builder
         */
        public Builder setDeviceNameA(String deviceNameA) {
            this.deviceNameA = deviceNameA;
            return this;
        }

        /**
         * setDeviceNameB
         *
         * @param deviceNameB deviceNameB
         * @return Builder builder
         */
        public Builder setDeviceNameB(String deviceNameB) {
            this.deviceNameB = deviceNameB;
            return this;
        }

        /**
         * setProcessIdA
         *
         * @param processIdA processIdA
         * @return Builder builder
         */
        public Builder setProcessIdA(Integer processIdA) {
            this.processIdA = processIdA;
            return this;
        }

        /**
         * setProcessIdB
         *
         * @param processIdB processIdB
         * @return Builder builder
         */
        public Builder setProcessIdB(Integer processIdB) {
            this.processIdB = processIdB;
            return this;
        }

        /**
         * setPathA
         *
         * @param pathA pathA
         * @return Builder builder
         */
        public Builder setPathA(String pathA) {
            this.pathA = pathA;
            return this;
        }

        /**
         * setPathB
         *
         * @param pathB pathB
         * @return Builder builder
         */
        public Builder setPathB(String pathB) {
            this.pathB = pathB;
            return this;
        }

        /**
         * setOffsetA
         *
         * @param offsetA offsetA
         * @return Builder builder
         */
        public Builder setOffsetA(Long offsetA) {
            this.offsetA = offsetA;
            return this;
        }

        /**
         * setOffsetB
         *
         * @param offsetB offsetB
         * @return Builder builder
         */
        public Builder setOffsetB(Long offsetB) {
            this.offsetB = offsetB;
            return this;
        }

        /**
         * build current DistributedParams
         *
         * @return DistributedParams params
         */
        public DistributedParams build() {
            return new DistributedParams(this);
        }
    }
}
