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

package ohos.devtools.datasources.utils.datahandler.datapoller;

import io.grpc.StatusRuntimeException;
import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.databases.datatable.MemoryTable;
import ohos.devtools.datasources.transport.grpc.ProfilerClient;
import ohos.devtools.datasources.transport.grpc.ProfilerServiceHelper;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.utils.common.util.CommonUtil;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.services.memory.ClassInfoDao;
import ohos.devtools.services.memory.MemoryHeapDao;
import ohos.devtools.services.memory.MemoryInstanceDao;
import ohos.devtools.services.memory.MemoryInstanceDetailsDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static ohos.devtools.datasources.utils.common.Constant.JVMTI_AGENT_PLUG;
import static ohos.devtools.datasources.utils.common.Constant.MEMORY_PLUG;
import static ohos.devtools.datasources.utils.common.Constant.MEMORY_PLUGS_NAME;

/**
 * DataPoller utilities class
 *
 * @version 1.0
 * @date 2021/02/22 15:59
 **/
public class DataPoller extends Thread {
    private static final Logger DATA = LogManager.getLogger("Data");
    private static final Logger LOGGER = LogManager.getLogger(DataPoller.class);
    private long localSessionId;
    private int sessionId;
    private ProfilerClient client;
    private Map<String, AbstractDataStore> tableService;
    private MemoryTable memoryTable;
    private ClassInfoDao classInfoDao;
    private MemoryInstanceDetailsDao memoryInstanceDetailsDao;
    private MemoryInstanceDao memoryInstanceDao;
    private MemoryHeapDao memoryHeapDao;

    private MemoryHeapDataConsumer memoryHeapDataConsumer;
    private MemoryDataConsumer memoryDataConsumer;

    private Queue<CommonTypes.ProfilerPluginData> memoryDataQueue = new LinkedBlockingQueue();
    private Queue<CommonTypes.ProfilerPluginData> memoryHeapDataQueue = new LinkedBlockingQueue();
    private boolean stopFlag = false;
    private boolean startRefresh = false;

    /**
     * Data Poller
     *
     * @param localSessionId local SessionId
     * @param sessionId      session Id
     * @param client         client
     * @param tableService   tableService
     */
    public DataPoller(Long localSessionId, int sessionId, ProfilerClient client,
        Map<String, AbstractDataStore> tableService) {
        this.localSessionId = localSessionId;
        this.sessionId = sessionId;
        this.client = client;
        this.tableService = tableService;
    }

    /**
     * Starts polling.
     */
    private void startPoll() {
        LOGGER.info("start Poller DeviceInfo, {}", DateTimeUtil.getNowTimeLong());
        ProfilerServiceTypes.FetchDataRequest request =
            ProfilerServiceHelper.fetchDataRequest(CommonUtil.getRequestId(), sessionId, null);
        Iterator<ProfilerServiceTypes.FetchDataResponse> response = null;
        try {
            LOGGER.info("start Poller fetchData01, {}", DateTimeUtil.getNowTimeLong());
            response = client.fetchData(request);
            long startTime = DateTimeUtil.getNowTimeLong();
            LOGGER.info("start Poller fetchData02, {}", startTime);
            while (response.hasNext()) {
                if (stopFlag) {
                    return;
                }
                ProfilerServiceTypes.FetchDataResponse fetchDataResponse = response.next();
                List<CommonTypes.ProfilerPluginData> lists = fetchDataResponse.getPluginDataList();
                for (CommonTypes.ProfilerPluginData pluginData : lists) {
                    handleData(pluginData);
                }
            }
        } catch (StatusRuntimeException exception) {
            SessionManager.getInstance().deleteLocalSession(localSessionId);
            LOGGER.error("start Poll has Exception {}", exception.getMessage());
            return;
        } finally {
            dataPollerEnd();
        }
    }

    private void handleData(CommonTypes.ProfilerPluginData pluginData) {
        if (pluginData.getStatus() != 0) {
            return;
        }

        if (MEMORY_PLUGS_NAME.equals(pluginData.getName()) || MEMORY_PLUG.equals(pluginData.getName())) {
            handleMemoryData(pluginData);
        }
        if (JVMTI_AGENT_PLUG.equals(pluginData.getName())) {
            handleAgentData(pluginData);
        }
    }

    private void handleMemoryData(CommonTypes.ProfilerPluginData pluginData) {
        if (tableService.get(MEMORY_PLUG) != null && memoryTable == null) {
            AbstractDataStore abstractDataStore = tableService.get(MEMORY_PLUG);
            if (abstractDataStore instanceof MemoryTable) {
                memoryTable = (MemoryTable) abstractDataStore;
            }
            memoryDataConsumer = new MemoryDataConsumer(memoryDataQueue, memoryTable, sessionId, localSessionId);
            memoryDataConsumer.start();
            offerPluginData(pluginData);
        } else if (tableService.get(MEMORY_PLUG) == null && memoryTable == null) {
            return;
        } else {
            offerPluginData(pluginData);
        }
    }

    private void offerPluginData(CommonTypes.ProfilerPluginData pluginData) {
        if (pluginData != null) {
            memoryDataQueue.offer(pluginData);
            if (!startRefresh) {
                long timeStamp = (pluginData.getTvSec() * 1000000000L + pluginData.getTvNsec()) / 1000000;
                SessionManager.getInstance().stopLoadingView(localSessionId, timeStamp);
                startRefresh = true;
            }
        }
    }

    private void handleAgentData(CommonTypes.ProfilerPluginData pluginData) {
        if (tableService.get(JVMTI_AGENT_PLUG) != null && classInfoDao == null) {
            LOGGER.info("get Dao info");
            if (tableService.get(JVMTI_AGENT_PLUG) instanceof ClassInfoDao) {
                classInfoDao = (ClassInfoDao) tableService.get(JVMTI_AGENT_PLUG);
            }

            if (tableService.get("jvmtiagentDetails") instanceof MemoryInstanceDetailsDao) {
                memoryInstanceDetailsDao = (MemoryInstanceDetailsDao) tableService.get("jvmtiagentDetails");
            }
            if (tableService.get("jvmtiagentInstance") instanceof MemoryInstanceDao) {
                memoryInstanceDao = (MemoryInstanceDao) tableService.get("jvmtiagentInstance");
            }
            if (tableService.get("jvmtiagentMemoryHeap") instanceof MemoryHeapDao) {
                memoryHeapDao = (MemoryHeapDao) tableService.get("jvmtiagentMemoryHeap");
            }
            memoryHeapDataConsumer =
                new MemoryHeapDataConsumer(memoryHeapDataQueue, localSessionId, classInfoDao, memoryInstanceDetailsDao,
                    memoryInstanceDao, memoryHeapDao);
            memoryHeapDataConsumer.start();
            offerHeapInfo(pluginData);
        } else if (tableService.get(JVMTI_AGENT_PLUG) == null && classInfoDao == null) {
            return;
        } else {
            offerHeapInfo(pluginData);
        }
    }

    private void offerHeapInfo(CommonTypes.ProfilerPluginData pluginData) {
        if (pluginData != null) {
            memoryHeapDataQueue.offer(pluginData);
        }
    }

    private void dataPollerEnd() {
        if (memoryDataConsumer != null) {
            memoryDataConsumer.shutDown();
        }
        if (memoryHeapDataConsumer != null) {
            memoryHeapDataConsumer.shutDown();
        }
        client.setUsed(false);
    }

    /**
     * shutDown
     */
    public void shutDown() {
        if (memoryDataConsumer != null) {
            memoryDataConsumer.shutDown();
        }
        if (memoryHeapDataConsumer != null) {
            memoryHeapDataConsumer.shutDown();
        }
        stopFlag = true;
    }

    /**
     * run
     */
    @Override
    public void run() {
        try {
            startPoll();
        } catch (StatusRuntimeException exception) {
            LOGGER.error("exception error{}", exception.getMessage());
        }
    }
}
