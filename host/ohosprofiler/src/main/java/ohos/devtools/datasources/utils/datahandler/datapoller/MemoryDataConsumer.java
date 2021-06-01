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

import com.google.protobuf.InvalidProtocolBufferException;
import ohos.devtools.datasources.databases.datatable.MemoryTable;
import ohos.devtools.datasources.databases.datatable.enties.ProcessMemInfo;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.MemoryPluginResult;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * @Description MemoryDataConsumer
 * @Date 2021/4/9 13:56
 **/
public class MemoryDataConsumer extends Thread {
    private static final Logger DATA = LogManager.getLogger("Data");
    private static final Logger LOGGER = LogManager.getLogger(MemoryDataConsumer.class);

    /**
     * Interval for saving data to the database, in ms.
     */
    private static final long SAVE_FREQ = 1000;
    private List<ProcessMemInfo> processMemInfoList = new ArrayList<>();
    private Queue<CommonTypes.ProfilerPluginData> queue;
    private MemoryTable memoryTable;
    private Integer sessionId;
    private Long localSessionId;
    private int logIndex = 0;
    private boolean stopFlag = false;
    private boolean isInsert = false;

    /**
     * Time reference variable for saving data to the in-memory database at the interval specified by SAVE_FREQ.
     */
    private long flagTime = DateTimeUtil.getNowTimeLong();

    /**
     * MemoryDataConsumer
     *
     * @param queue          Indicates the memory queue.
     * @param memoryTable    Indicates the memory table.
     * @param sessionId      Indicates the session ID.
     * @param localSessionId Indicates the local session ID.
     */
    public MemoryDataConsumer(Queue queue, MemoryTable memoryTable, Integer sessionId, Long localSessionId) {
        this.queue = queue;
        this.memoryTable = memoryTable;
        this.sessionId = sessionId;
        this.localSessionId = localSessionId;
    }

    /**
     * Run MemoryDataConsumer.
     */
    @Override
    public void run() {
        while (true) {
            try {
                if (logIndex == Integer.MAX_VALUE) {
                    logIndex = 0;
                }
                if (stopFlag) {
                    return;
                }
                long now = DateTimeUtil.getNowTimeLong();
                if (now - flagTime > SAVE_FREQ) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException exception) {
                        LOGGER.info("InterruptedException");
                    }
                }
                if (queue.size() > 0) {
                    handleMemoryData(queue.poll());
                }
                insertMemoryData();
            } catch (Exception exception) {
                LOGGER.info(exception);
            }
        }
    }

    /**
     * shutDown
     */
    public void shutDown() {
        stopFlag = true;
    }

    private void handleMemoryData(CommonTypes.ProfilerPluginData memoryData) {
        MemoryPluginResult.MemoryData.Builder builder = MemoryPluginResult.MemoryData.newBuilder();
        MemoryPluginResult.MemoryData memorydata = null;
        try {
            memorydata = builder.mergeFrom(memoryData.getData()).build();
        } catch (InvalidProtocolBufferException exe) {
            return;
        }
        List<MemoryPluginResult.ProcessMemoryInfo> processMemoryInfoList = memorydata.getProcessesinfoList();
        if (stopFlag) {
            return;
        }
        processMemoryInfoList.forEach(processMemoryInfo -> {
            MemoryPluginResult.AppSummary app = processMemoryInfo.getMemsummary();
            ProcessMemInfo procMemInfo = new ProcessMemInfo();
            procMemInfo.setData(app);
            procMemInfo.setSession(localSessionId);
            procMemInfo.setSessionId(sessionId);
            long timeStamp = (memoryData.getTvSec() * 1000000000L + memoryData.getTvNsec()) / 1000000;
            procMemInfo.setTimeStamp(timeStamp);
            LOGGER.debug("TimeStamp {}, AppSummary {}", timeStamp, app);
            processMemInfoList.add(procMemInfo);
            isInsert = false;
            insertMemoryData();
        });
    }

    private void insertMemoryData() {
        if (!isInsert) {
            long now = DateTimeUtil.getNowTimeLong();
            if (now - flagTime > SAVE_FREQ) {
                memoryTable.insertProcessMemInfo(processMemInfoList);
                processMemInfoList.clear();
                // Update flagTime.
                flagTime = now;
            }
            isInsert = true;
        }
    }
}
