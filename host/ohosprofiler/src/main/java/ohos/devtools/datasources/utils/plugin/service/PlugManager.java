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

package ohos.devtools.datasources.utils.plugin.service;

import ohos.devtools.datasources.utils.plugin.dao.PlugDao;
import ohos.devtools.datasources.utils.plugin.entity.HiProfilerPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 对插件和其配置的管理
 *
 * @version 1.0
 * @Date 2021/2/2 15:17
 **/
public class PlugManager {
    // 日志
    private static final Logger LOGGER = LogManager.getLogger(PlugManager.class);

    // 单例
    private static volatile PlugManager singleton;

    /**
     * 获取实例
     *
     * @return PlugManager
     */
    public static PlugManager getInstance() {
        if (singleton == null) {
            synchronized (PlugManager.class) {
                if (singleton == null) {
                    singleton = new PlugManager();
                }
            }
        }
        return singleton;
    }

    private PlugManager() {
    }

    /**
     * 自研插件配置信息写表
     *
     * @param plugin plugin
     * @return boolean
     */
    public boolean insertPlugInfo(HiProfilerPlugin plugin) {
        boolean insert = false;
        insert = PlugDao.getInstance().insertPlugInfo(plugin);
        return insert;
    }

    /**
     * 对外提供插件配置信息查询的接口
     *
     * @param deviceId deviceId
     * @return List HiProfilerPlugin
     */
    public List<HiProfilerPlugin> selectPlugConfig(String deviceId) {
        List<HiProfilerPlugin> hiProfilerPlugins = new ArrayList<>();
        hiProfilerPlugins = PlugDao.getInstance().selectPlugConfig(deviceId);
        return hiProfilerPlugins;
    }
}
