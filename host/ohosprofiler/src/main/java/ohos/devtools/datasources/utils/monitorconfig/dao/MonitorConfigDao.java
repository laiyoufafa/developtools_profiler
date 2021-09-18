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

import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.utils.common.util.PrintUtil;
import ohos.devtools.datasources.utils.monitorconfig.entity.MonitorInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * 监控项配置数据的dao层
 */
public class MonitorConfigDao extends AbstractDataStore {
    // 日志
    private static final Logger LOGGER = LogManager.getLogger(MonitorConfigDao.class);
    private static volatile MonitorConfigDao singleton;

    /**
     * Get an instance
     *
     * @return MonitorConfigDao
     */
    public static MonitorConfigDao getInstance() {
        if (singleton == null) {
            synchronized (MonitorConfigDao.class) {
                if (singleton == null) {
                    singleton = new MonitorConfigDao();
                }
            }
        }
        return singleton;
    }

    private MonitorConfigDao() {
    }

    /**
     * 插入界面返回的监控项采集项数据
     *
     * @param monitorInfo monitorInfo
     * @return boolean
     */
    public boolean insertMonitorInfo(MonitorInfo monitorInfo) {
        boolean result = false;
        result = insert(monitorInfo);
        if (result) {
            PrintUtil.print(LOGGER, "local session Data written to the table successfully", 1);
        } else {
            PrintUtil.print(LOGGER, "local session Failed to write data to table", 1);
        }
        return true;
    }

    /**
     * 插入界面返回的监控项采集项数据
     *
     * @param monitorInfo monitorInfo
     * @return boolean
     */
    public boolean insertMonitorInfos(List<MonitorInfo> monitorInfo) {
        return insert(monitorInfo);
    }

}
