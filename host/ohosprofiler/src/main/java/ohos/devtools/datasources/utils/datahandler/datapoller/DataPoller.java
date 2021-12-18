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
import io.grpc.stub.StreamObserver;
import ohos.devtools.datasources.transport.grpc.HiProfilerClient;
import ohos.devtools.datasources.transport.grpc.ProfilerServiceHelper;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.utils.common.util.CommonUtil;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.plugin.entity.PluginConf;
import ohos.devtools.datasources.utils.plugin.service.PlugManager;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static ohos.devtools.datasources.utils.common.Constant.MEMORY_PLUG;

/**
 * DataPoller utilities class
 */
public class DataPoller extends Thread {
    private static final Logger LOGGER = LogManager.getLogger(DataPoller.class);
    private long localSessionId;
    private int sessionId;
    private DeviceIPPortInfo deviceIPPortInfo;
    private boolean stopFlag = false;
    private boolean startRefresh = false;
    private ExecutorService executorService =
        new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    private Map<String, Queue> queueMap = new HashMap<>();
    private List<AbsDataConsumer> consumers = new ArrayList<>();
    private Integer count = 0;

    /**
     * Data Poller
     *
     * @param localSessionId local SessionId
     * @param sessionId session Id
     * @param device device
     */
    public DataPoller(Long localSessionId, int sessionId, DeviceIPPortInfo device) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("DataPoller");
        }
        this.localSessionId = localSessionId;
        this.sessionId = sessionId;
        this.deviceIPPortInfo = device;
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
                if (sessionId == Integer.MAX_VALUE) {
                    if (!startRefresh) {
                        long timeStamp = DateTimeUtil.getNowTimeLong();
                        SessionManager.getInstance().stopLoadingView(localSessionId, timeStamp);
                        startRefresh = true;
                    }
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException exception) {
                LOGGER.error("start Poll init has Exception {}", exception.getMessage());
            }
        }
    }

    private void startPoll() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("startPoll");
        }
        ProfilerServiceTypes.FetchDataRequest request =
            ProfilerServiceHelper.fetchDataRequest(CommonUtil.getRequestId(), sessionId, null);
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("start Poller fetchData01, {}", DateTimeUtil.getNowTimeLong());
        }
        HiProfilerClient.getInstance()
            .getProfilerClient(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort()).getProfilerServiceStub()
            .withMaxInboundMessageSize(Integer.MAX_VALUE).withMaxOutboundMessageSize(Integer.MAX_VALUE)
            .fetchData(request, new StreamObserver<ProfilerServiceTypes.FetchDataResponse>() {
                @Override
                public void onNext(ProfilerServiceTypes.FetchDataResponse fetchDataResponse) {
                    count = 0;
                    List<CommonTypes.ProfilerPluginData> lists = fetchDataResponse.getPluginDataList();
                    buildPluginData(lists);
                }

                @Override
                public void onError(Throwable throwable) {
                    setErrorPoll(throwable);
                }

                @Override
                public void onCompleted() {
                    shutDown();
                }
            });
    }

    private void setErrorPoll(Throwable throwable) {
        LOGGER.error("start Poll has Exception", throwable);
        if (throwable.getMessage().contains("session_id invalid!")) {
            SessionManager.getInstance().deleteLocalSession(localSessionId);
        } else {
            LOGGER.info("restart poller session id {}", sessionId);
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException interruptedException) {
                LOGGER.error("restart poller InterruptedException session id {}", sessionId);
            }
            count++;
            if (count < 3) {
                HiProfilerClient.getInstance()
                    .destroyProfiler(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
                startPoll();
            }
        }
    }

    private void buildPluginData(List<CommonTypes.ProfilerPluginData> lists) {
        lists.parallelStream().forEach(pluginData -> {
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("handleData");
            }
            if (pluginData.getStatus() != 0) {
                return;
            }
            String name = pluginData.getName();
            if (name.equals(MEMORY_PLUG)) {
                if (ProfilerLogManager.isInfoEnabled()) {
                    LOGGER.info("get Memory Date, time is {}", DateTimeUtil.getNowTimeLong());
                }
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
        });
    }

    /**
     * shutDown
     */
    public void shutDown() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("shutDown");
        }
        consumers.forEach(absDataConsumer -> absDataConsumer.shutDown());
        executorService.shutdown();
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
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("exception error{}", exception.getMessage());
            }
        }
    }
}
