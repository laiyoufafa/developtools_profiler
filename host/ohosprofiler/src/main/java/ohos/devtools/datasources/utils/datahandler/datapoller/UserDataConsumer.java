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

import ohos.devtools.datasources.databases.datatable.UserDataTable;
import ohos.devtools.datasources.databases.datatable.enties.UserData;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.services.userdata.UserDataCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Consumer class of SdkData, Perform Sdk data construction and table insertion
 *
 * @since 2021/11/22
 */
public class UserDataConsumer extends AbsDataConsumer {
    private static final Logger LOGGER = LogManager.getLogger(UserDataConsumer.class);
    private static final long SAVE_FREQ = 1000L;

    private List<UserData> sysSdkInfoList = new ArrayList<>();
    private Queue<CommonTypes.ProfilerPluginData> queue;
    private UserDataTable userDataTable;
    private Integer sessionId;
    private Long localSessionId;
    private boolean stopFlag = false;
    private boolean isInsert = false;
    private long flagTime = DateTimeUtil.getNowTimeLong();

    /**
     * Sdk Data Consumer
     */
    public UserDataConsumer() {
        super();
    }

    @Override
    public void run() {
        while (!stopFlag) {
            CommonTypes.ProfilerPluginData poll = queue.poll();
            if (Objects.nonNull(poll)) {
                handleSdkData(poll);
            } else {
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
            }
            insertSdkData();
        }
    }

    @Override
    public void init(Queue queue, int sessionId, long localSessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("init");
        }
        this.queue = queue;
        this.userDataTable = new UserDataTable();
        this.sessionId = sessionId;
        this.localSessionId = localSessionId;
    }

    /**
     * shutDown
     */
    public void shutDown() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("shutDown");
        }
        stopFlag = true;
    }

    /**
     * handleDiskIoData
     *
     * @param sdkSourceData sdkIoData
     */
    private void handleSdkData(CommonTypes.ProfilerPluginData sdkSourceData) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("handleSdkData");
        }
        UserData sdkData = new UserData();
        sdkData.setBytes(sdkSourceData.toByteArray());
        sdkData.setSession(localSessionId);
        sdkData.setSessionId(sessionId);
        long timeStamp = (sdkSourceData.getTvSec() * 1000000000L + sdkSourceData.getTvNsec()) / 1000000;
        sdkData.setTimeStamp(timeStamp);
        sysSdkInfoList.add(sdkData);
        addDataToCache(sdkData, timeStamp);
        isInsert = false;
        insertSdkData();
    }

    private void addDataToCache(UserData sdkData, long timeStamp) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addDataToCache");
        }
        UserDataCache.getInstance().addDataModel(localSessionId, timeStamp, sdkData);
    }

    /**
     * insertDiskIoData
     */
    private void insertSdkData() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertSdkData");
        }
        if (!isInsert) {
            long now = DateTimeUtil.getNowTimeLong();
            if (now - flagTime > SAVE_FREQ) {
                userDataTable.insertSdkDataInfo(sysSdkInfoList);
                sysSdkInfoList.clear();
                // Update flagTime.
                flagTime = now;
            }
            isInsert = true;
        }
    }

}
