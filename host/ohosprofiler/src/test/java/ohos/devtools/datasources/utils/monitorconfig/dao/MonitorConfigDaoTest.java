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

package ohos.devtools.datasources.utils.monitorconfig.dao;

import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DAO layer test class of monitoring item configuration data
 *
 * @since 2021/11/22
 */
public class MonitorConfigDaoTest {
    private MonitorConfigDao monitorConfigDao;

    /**
     * init
     */
    @Before
    public void init() {
        DataBaseApi apo = DataBaseApi.getInstance();
        apo.initDataSourceManager();
    }

    /**
     * functional testing getInstance
     *
     * @tc.name: MonitorConfigDao initialization configuration
     * @tc.number: OHOS_JAVA_monitor_MonitorConfigDao_getInstance_0001
     * @tc.desc: MonitorConfigDao getInstance
     * @tc.type: functional testing
     * @tc.require: AR000FK61R
     */
    @Test
    public void getInstanceTest01() {
        monitorConfigDao = MonitorConfigDao.getInstance();
        Assert.assertNotNull(monitorConfigDao);
    }

    /**
     * functional testing getInstance
     *
     * @tc.name: MonitorConfigDao initialization configuration
     * @tc.number: OHOS_JAVA_monitor_MonitorConfigDao_getInstance_0002
     * @tc.desc: MonitorConfigDao getInstance
     * @tc.type: functional testing
     * @tc.require: AR000FK61R
     */
    @Test
    public void getInstanceTest02() {
        monitorConfigDao = MonitorConfigDao.getInstance();
        MonitorConfigDao configDao = MonitorConfigDao.getInstance();
        Assert.assertEquals(monitorConfigDao, configDao);
    }
}
