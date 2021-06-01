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

package ohos.devtools.datasources.utils.plugin.dao;

import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.utils.plugin.entity.HiProfilerPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 插件dao层(与sqlite交互)
 *
 * @version 1.0
 * @date 2021/2/2 15:10
 **/
public class PlugDao extends AbstractDataStore {
    // 日志
    private static final Logger LOGGER = LogManager.getLogger(PlugDao.class);

    private static volatile PlugDao singleton;

    /**
     * getInstance
     *
     * @return PlugDao
     */
    public static PlugDao getInstance() {
        if (singleton == null) {
            synchronized (PlugDao.class) {
                if (singleton == null) {
                    singleton = new PlugDao();
                }
            }
        }
        return singleton;
    }

    private PlugDao() {
    }

    /**
     * 插入自研插件Info信息
     *
     * @param hiProfilerPlugin hiProfilerPlugin
     * @return boolean
     */
    public boolean insertPlugInfo(HiProfilerPlugin hiProfilerPlugin) {
        boolean insert = false;
        insert = insert(hiProfilerPlugin);

        return insert;
    }

    /**
     * 保存插件Info信息
     *
     * @param deviceId deviceId
     * @return List<HiProfilerPlugin>
     */
    public List<HiProfilerPlugin> selectPlugConfig(String deviceId) {
        return new ArrayList<>();
    }

}
