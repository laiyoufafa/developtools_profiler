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

package ohos.devtools.services.memory;

import ohos.devtools.datasources.databases.datatable.enties.ProcessMemInfo;
import ohos.devtools.datasources.transport.grpc.service.MemoryPluginResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Description Memory业务处理类
 * @Date 2021/2/7 13:47
 **/
public class MemoryService {
    private static final Logger LOGGER = LogManager.getLogger(MemoryService.class);

    private static MemoryService instance;

    /**
     * MemoryService
     *
     * @return MemoryService
     */
    public static MemoryService getInstance() {
        if (instance == null) {
            synchronized (MemoryService.class) {
                if (instance == null) {
                    instance = new MemoryService();
                }
            }
        }
        return instance;
    }

    private LinkedHashMap<Long, MemoryPluginResult.AppSummary> memordata;

    private MemoryService() {
    }

    /**
     * 添加数据
     *
     * @param sessionId      sessionId
     * @param min            min
     * @param max            max
     * @param firstTimestamp 本次Chart首次创建并启动刷新时的时间戳
     */
    public void addData(long sessionId, int min, int max, long firstTimestamp) {
        memordata = MemoryDao.getInstance().getData(sessionId, min, max, firstTimestamp, false);
        ChartDataCache.getInstance().addCacheBlock(String.valueOf(sessionId), memordata);
    }

    /**
     * 获取数据
     *
     * @param sessionId      sessionId
     * @param min            min
     * @param max            max
     * @param firstTimestamp 本次Chart首次创建并启动刷新时的时间戳
     * @return LinkedHashMap<Long, MemoryPluginResult.AppSummary>
     */
    public LinkedHashMap<Long, MemoryPluginResult.AppSummary> getData(long sessionId, int min, int max,
        long firstTimestamp) {
        return MemoryDao.getInstance().getData(sessionId, min, max, firstTimestamp, false);
    }

    /**
     * 获取所有数据
     *
     * @param sessionId sessionId
     * @return List<ProcessMemInfo>
     */
    public List<ProcessMemInfo> getAllData(long sessionId) {
        List<ProcessMemInfo> listData = MemoryDao.getInstance().getAllData(sessionId);
        return listData;
    }

    /**
     * deleteSessionData
     *
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean deleteSessionData(long sessionId) {
        return MemoryDao.getInstance().deleteSessionData(sessionId);
    }
}
