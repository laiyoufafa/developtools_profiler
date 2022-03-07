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

package ohos.devtools.datasources.utils.monitorconfig.service;

import com.alibaba.fastjson.JSONObject;
import ohos.devtools.datasources.utils.monitorconfig.dao.MonitorConfigDao;
import ohos.devtools.datasources.utils.monitorconfig.entity.MonitorInfo;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MonitorConfigManager
 *
 * @since 2021/5/19 16:39
 */
public class MonitorConfigManager {
    private static final Logger LOGGER = LogManager.getLogger(MonitorConfigManager.class);

    /**
     * dataMap
     */
    public static final ConcurrentHashMap<Long, Map<String, LinkedList<String>>> DATA_MAP = new ConcurrentHashMap<>();

    private static volatile MonitorConfigManager singleton;

    private MonitorConfigManager() {
    }

    /**
     * getInstance
     *
     * @return MonitorConfigManager
     */
    public static MonitorConfigManager getInstance() {
        if (singleton == null) {
            synchronized (MonitorConfigManager.class) {
                if (singleton == null) {
                    singleton = new MonitorConfigManager();
                }
            }
        }
        return singleton;
    }

    /**
     * analyzeCharTarget
     *
     * @param localSessionId localSessionId
     * @param jsonMonitor    jsonMonitor
     * @return Map<String, LinkedList < String>>
     */
    public Map<String, LinkedList<String>> analyzeCharTarget(long localSessionId, JSONObject jsonMonitor) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("analyzeCharTarget");
        }
        // 写表的实体对象传递
        MonitorInfo monitorInfo = null;
        LinkedList<MonitorInfo> monitorInfos = new LinkedList<>();

        // 传递给界面的具体指标项配置(True)，通过Dao获取则丢弃这个集合对象
        LinkedList<String> monitor = null;
        Map<String, LinkedList<String>> monitors = new HashMap<>();

        // 迭代传递的JSON对象解析，并写表
        Iterator<Map.Entry<String, Object>> rtn = jsonMonitor.entrySet().iterator();
        while (rtn.hasNext()) {
            Map.Entry<String, Object> unit = rtn.next();
            monitor = new LinkedList<>();

            JSONObject jsonObject = jsonMonitor.getJSONObject(unit.getKey());
            Iterator<Map.Entry<String, Object>> iterator = jsonObject.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, Object> next = iterator.next();
                monitorInfo = MonitorInfo.builder().localSessionId(localSessionId).monitorType(unit.getKey())
                    .parameter(next.getKey()).value(next.getValue().toString()).build();

                if ("true".equals(next.getValue().toString())) {
                    monitor.add(next.getKey());
                }
                monitorInfos.add(monitorInfo);
            }

            if (monitor.size() > 0) {
                // 传递给二级界面的数据monitors
                monitors.put(unit.getKey(), monitor);
            }
        }

        DATA_MAP.put(localSessionId, monitors);
        // 解析后的数据先写表
        MonitorConfigDao.getInstance().insertMonitorInfos(monitorInfos);
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("analyze Chart Target success");
        }
        // 返回界面所有配置(true)的指标项
        return monitors;
    }
}
