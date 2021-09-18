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
import ohos.devtools.datasources.transport.grpc.ProfilerClient;
import ohos.devtools.datasources.transport.grpc.ProfilerServiceHelper;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.utils.common.util.CommonUtil;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.plugin.entity.PluginConf;
import ohos.devtools.datasources.utils.plugin.service.PlugManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import static ohos.devtools.datasources.utils.common.Constant.MEMORY_PLUG;

/**
 * DataPoller utilities class
 */
public class DataPoller extends Thread {
    private static final Logger LOGGER = LogManager.getLogger(DataPoller.class);
    private long localSessionId;
    private int sessionId;
    private ProfilerClient client;

    private boolean stopFlag = false;
    private boolean startRefresh = false;

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private Map<String, Queue> queueMap = new HashMap<>();
    private List<AbsDataConsumer> consumers = new ArrayList<>();

    /**
     * Data Poller
     *
     * @param localSessionId local SessionId
     * @param sessionId session Id
     * @param client client
     */
    public DataPoller(Long localSessionId, int sessionId, ProfilerClient client) {
        this.localSessionId = localSessionId;
        this.sessionId = sessionId;
        this.client = client;
        init();
    }

    private void init() {
        List<PluginConf> items = PlugManager.getInstance().getProfilerPlugConfig(localSessionId);
        for (PluginConf conf : items) {
            Class<? extends AbsDataConsumer> consumerClass = conf.getConsumerClass();
            if (Objects.isNull(consumerClass)) {
                continue;
            }
            AbsDataConsumer absDataConsumer = null;
            try {
                absDataConsumer = consumerClass.getConstructor().newInstance();
                LinkedBlockingQueue linkedBlockingQueue = new LinkedBlockingQueue();
                queueMap.put(conf.getPluginDataName(), linkedBlockingQueue);
                absDataConsumer.init(linkedBlockingQueue, sessionId, localSessionId);
                executorService.execute(absDataConsumer);
                consumers.add(absDataConsumer);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException exception) {
                LOGGER.error("start Poll init has Exception {}", exception.getMessage());
            }
        }
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
            while ((!stopFlag) && response.hasNext()) {
                ProfilerServiceTypes.FetchDataResponse fetchDataResponse = response.next();
                List<CommonTypes.ProfilerPluginData> lists = fetchDataResponse.getPluginDataList();
                if (lists.isEmpty()) {
                    continue;
                }
                lists.parallelStream().forEach(pluginData -> {
                    handleData(pluginData);
                });
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
        String name = pluginData.getName();
        if (name.equals(MEMORY_PLUG)) {
            LOGGER.debug("get Memory Date, time is {}", DateTimeUtil.getNowTimeLong());
        }
        Queue queue = queueMap.get(name);
        if (Objects.nonNull(queue)) {
            queue.offer(pluginData);
        }
        if (!startRefresh) {
            long timeStamp = (pluginData.getTvSec() * 1000000000L + pluginData.getTvNsec()) / 1000000;
            SessionManager.getInstance().stopLoadingView(localSessionId, timeStamp);
            startRefresh = true;
        }
    }

    private void dataPollerEnd() {
        consumers.forEach(absDataConsumer -> absDataConsumer.shutDown());
        executorService.shutdown();
    }

    /**
     * shutDown
     */
    public void shutDown() {
        consumers.forEach(absDataConsumer -> absDataConsumer.shutDown());
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
