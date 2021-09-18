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

package ohos.devtools.datasources.databases.databasepool;

/**
 * database objects
 */
public class DataBase {
    private String driver;
    private String url;
    private Integer initialSize;
    private Integer maxActive;
    private Integer minIdle;
    private Integer maxWait;
    private String filters;
    private Integer timeBetweenEvictionRunsMillis;
    private Integer minEvictableIdleTimeMillis;
    private String validationQuery;
    private boolean testWhileIdle;
    private boolean testOnBorrow;
    private boolean testOnReturn;

    private DataBase(Builder builder) {
        driver = builder.driver;
        url = builder.url;
        initialSize = builder.initialSize;
        maxActive = builder.maxActive;
        minIdle = builder.minIdle;
        maxWait = builder.maxWait;
        filters = builder.filters;
        timeBetweenEvictionRunsMillis = builder.timeBetweenEvictionRunsMillis;
        minEvictableIdleTimeMillis = builder.minEvictableIdleTimeMillis;
        validationQuery = builder.validationQuery;
        testWhileIdle = builder.testWhileIdle;
        testOnBorrow = builder.testOnBorrow;
        testOnReturn = builder.testOnReturn;
    }

    /**
     * set Url
     *
     * @param url url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * builder
     *
     * @return Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * get Driver
     *
     * @return String
     */
    public String getDriver() {
        return driver;
    }

    /**
     * get Url
     *
     * @return String
     */
    public String getUrl() {
        return url;
    }

    /**
     * get Initial Size
     *
     * @return Integer
     */
    public Integer getInitialSize() {
        return initialSize;
    }

    /**
     * get Max Active
     *
     * @return Integer
     */
    public Integer getMaxActive() {
        return maxActive;
    }

    /**
     * getMinIdle
     *
     * @return Integer
     */
    public Integer getMinIdle() {
        return minIdle;
    }

    /**
     * getMaxWait
     *
     * @return Integer
     */
    public Integer getMaxWait() {
        return maxWait;
    }

    /**
     * getFilters
     *
     * @return String
     */
    public String getFilters() {
        return filters;
    }

    /**
     * getTimeBetweenEvictionRunsMillis
     *
     * @return Integer
     */
    public Integer getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    /**
     * getMinEvictableIdleTimeMillis
     *
     * @return Integer
     */
    public Integer getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    /**
     * getValidationQuery
     *
     * @return String
     */
    public String getValidationQuery() {
        return validationQuery;
    }

    /**
     * isTestWhileIdle
     *
     * @return boolean
     */
    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    /**
     * isTestOnBorrow
     *
     * @return boolean
     */
    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    /**
     * isTestOnReturn
     *
     * @return boolean
     */
    public boolean isTestOnReturn() {
        return testOnReturn;
    }

    /**
     * Builder
     */
    public static class Builder {
        private String driver;
        private String url;
        private Integer initialSize;
        private Integer maxActive;
        private Integer minIdle;
        private String filters;
        private Integer timeBetweenEvictionRunsMillis;
        private Integer minEvictableIdleTimeMillis;
        private String validationQuery;
        private boolean testWhileIdle;
        private boolean testOnBorrow;
        private boolean testOnReturn;
        private Integer maxWait;

        private Builder() {
        }

        /**
         * drive
         *
         * @param driver driver
         * @return Builder
         */
        public Builder driver(String driver) {
            this.driver = driver;
            return this;
        }

        /**
         * URL path
         *
         * @param url url
         * @return Builder
         */
        public Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * initialSize
         *
         * @param initialSize initialSize
         * @return Builder
         */
        public Builder initialSize(Integer initialSize) {
            this.initialSize = initialSize;
            return this;
        }

        /**
         * Biggest activity
         *
         * @param maxActive maxActive
         * @return Builder
         */
        public Builder maxActive(Integer maxActive) {
            this.maxActive = maxActive;
            return this;
        }

        /**
         * minIdle
         *
         * @param minIdle minIdle
         * @return Builder
         */
        public Builder minIdle(Integer minIdle) {
            this.minIdle = minIdle;
            return this;
        }

        /**
         * maxWait
         *
         * @param maxWait maxWait
         * @return Builder
         */
        public Builder maxWait(Integer maxWait) {
            this.maxWait = maxWait;
            return this;
        }

        /**
         * filters
         *
         * @param filters filters
         * @return Builder
         */
        public Builder filters(String filters) {
            this.filters = filters;
            return this;
        }

        /**
         * timeBetweenEvictionRunsMillis
         *
         * @param timeBetweenEvictionRunsMillis timeBetweenEvictionRunsMillis
         * @return Builder
         */
        public Builder timeBetweenEvictionRunsMillis(Integer timeBetweenEvictionRunsMillis) {
            this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
            return this;
        }

        /**
         * time
         *
         * @param minEvictableIdleTimeMillis minEvictableIdleTimeMillis
         * @return Builder
         */
        public Builder minEvictableIdleTimeMillis(Integer minEvictableIdleTimeMillis) {
            this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
            return this;
        }

        /**
         * validationQuery
         *
         * @param validationQuery validationQuery
         * @return Builder
         */
        public Builder validationQuery(String validationQuery) {
            this.validationQuery = validationQuery;
            return this;
        }

        /**
         * testWhileIdle
         *
         * @param testWhileIdle testWhileIdle
         * @return Builder
         */
        public Builder testWhileIdle(boolean testWhileIdle) {
            this.testWhileIdle = testWhileIdle;
            return this;
        }

        /**
         * test
         *
         * @param testOnBorrow testOnBorrow
         * @return Builder
         */
        public Builder testOnBorrow(boolean testOnBorrow) {
            this.testOnBorrow = testOnBorrow;
            return this;
        }

        /**
         * testOnReturn
         *
         * @param testOnReturn testOnReturn
         * @return Builder
         */
        public Builder testOnReturn(boolean testOnReturn) {
            this.testOnReturn = testOnReturn;
            return this;
        }

        /**
         * build
         *
         * @return DataBase
         */
        public DataBase build() {
            return new DataBase(this);
        }
    }
}
