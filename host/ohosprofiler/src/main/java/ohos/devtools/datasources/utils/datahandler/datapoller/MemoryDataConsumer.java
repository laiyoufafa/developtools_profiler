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
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.services.memory.memoryservice.MemoryDataCache;
import ohos.devtools.views.charts.model.ChartDataModel;
import ohos.devtools.views.layout.chartview.MonitorItemDetail;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static ohos.devtools.views.layout.chartview.MonitorItemDetail.MEM_CODE;
import static ohos.devtools.views.layout.chartview.MonitorItemDetail.MEM_GRAPHICS;
import static ohos.devtools.views.layout.chartview.MonitorItemDetail.MEM_JAVA;
import static ohos.devtools.views.layout.chartview.MonitorItemDetail.MEM_NATIVE;
import static ohos.devtools.views.layout.chartview.MonitorItemDetail.MEM_OTHERS;
import static ohos.devtools.views.layout.chartview.MonitorItemDetail.MEM_STACK;

/**
 * MemoryDataConsumer
 */
public class MemoryDataConsumer extends AbsDataConsumer {
    private static final Logger DATA = LogManager.getLogger("Data");
    private static final Logger LOGGER = LogManager.getLogger(MemoryDataConsumer.class);
    private static final long SAVE_FREQ = 1000L;

    private List<ProcessMemInfo> processMemInfoList = new ArrayList<>();
    private Queue<CommonTypes.ProfilerPluginData> queue;
    private MemoryTable memoryTable;
    private Integer sessionId;
    private Long localSessionId;
    private int logIndex = 0;
    private boolean stopFlag = false;
    private boolean isInsert = false;
    private long flagTime = DateTimeUtil.getNowTimeLong();

    /**
     * MemoryDataConsumer
     */
    public MemoryDataConsumer() {
        super();
    }

    /**
     * Run MemoryDataConsumer.
     */
    @Override
    public void run() {
        while (!stopFlag) {
            CommonTypes.ProfilerPluginData poll = queue.poll();
            if (poll != null) {
                handleMemoryData(poll);
            } else {
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException exception) {
                    LOGGER.info("InterruptedException");
                }
            }
            insertMemoryData();
        }
    }

    @Override
    public void init(Queue queue, int sessionId, long localSessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("init");
        }
        this.queue = queue;
        this.memoryTable = new MemoryTable();
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

    private void handleMemoryData(CommonTypes.ProfilerPluginData memoryData) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("handleMemoryData");
        }
        MemoryPluginResult.MemoryData.Builder builder = MemoryPluginResult.MemoryData.newBuilder();
        MemoryPluginResult.MemoryData memorydata = null;
        try {
            memorydata = builder.mergeFrom(memoryData.getData()).build();
        } catch (InvalidProtocolBufferException exe) {
            return;
        }
        List<MemoryPluginResult.ProcessMemoryInfo> processMemoryInfoList = memorydata.getProcessesinfoList();
        processMemoryInfoList.forEach(processMemoryInfo -> {
            int pid = processMemoryInfo.getPid();
            String name = processMemoryInfo.getName();
            if (pid == -1 && (StringUtils.isEmpty(name) || name == "null")) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("pid is -1, the process is not exits");
                }
                SessionManager.getInstance().deleteSession(localSessionId);
            }
            MemoryPluginResult.AppSummary app = processMemoryInfo.getMemsummary();
            ProcessMemInfo procMemInfo = new ProcessMemInfo();
            procMemInfo.setData(app);
            procMemInfo.setSession(localSessionId);
            procMemInfo.setSessionId(sessionId);
            long timeStamp = getAgentTime(memoryData.getTvSec(), memoryData.getTvNsec());
            procMemInfo.setTimeStamp(timeStamp);
            processMemInfoList.add(procMemInfo);
            addDataToCache(procMemInfo);
            isInsert = false;
            insertMemoryData();
        });
    }

    /**
     * getAgentTime
     *
     * @param tvSec tvSec
     * @param tvnsec tvnsec
     * @return long
     */
    private long getAgentTime(long tvSec, long tvnsec) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getAgentTime");
        }
        return (tvSec * 1000000000L + tvnsec) / 1000000;
    }

    /**
     * Process and add memory info to cache
     *
     * @param procMemInfo ProcessMemInfo
     */
    private void addDataToCache(ProcessMemInfo procMemInfo) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addDataToCache");
        }
        List<ChartDataModel> dataModels = processAppSummary(procMemInfo.getData());
        MemoryDataCache.getInstance().addDataModel(localSessionId, procMemInfo.getTimeStamp(), dataModels);
    }

    /**
     * Process MemoryPluginResult.AppSummary into chart needed
     *
     * @param app MemoryPluginResult.AppSummary
     * @return List <ChartDataModel>
     */
    public static List<ChartDataModel> processAppSummary(MemoryPluginResult.AppSummary app) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("processAppSummary");
        }
        List<ChartDataModel> list = new ArrayList<>();

        ChartDataModel memJava = buildChartDataModel(MEM_JAVA);
        memJava.setValue((int) (app.getJavaHeap()));
        list.add(memJava);

        ChartDataModel memNative = buildChartDataModel(MEM_NATIVE);
        memNative.setValue((int) (app.getNativeHeap()));
        list.add(memNative);

        ChartDataModel memGraphics = buildChartDataModel(MEM_GRAPHICS);
        memGraphics.setValue((int) (app.getGraphics()));
        list.add(memGraphics);

        ChartDataModel memStack = buildChartDataModel(MEM_STACK);
        memStack.setValue((int) (app.getStack()));
        list.add(memStack);

        ChartDataModel memCode = buildChartDataModel(MEM_CODE);
        memCode.setValue((int) (app.getCode()));
        list.add(memCode);

        ChartDataModel memOthers = buildChartDataModel(MEM_OTHERS);
        memOthers.setValue((int) (app.getPrivateOther()));
        list.add(memOthers);
        // Sort by model.index from small to large
        list.sort(Comparator.comparingInt(ChartDataModel::getIndex));
        return list;
    }

    /**
     * Build the data by MonitorItemDetail
     *
     * @param monitorItemDetail MonitorItemDetail
     * @return ChartDataModel
     */
    private static ChartDataModel buildChartDataModel(MonitorItemDetail monitorItemDetail) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("buildChartDataModel");
        }
        ChartDataModel memoryData = new ChartDataModel();
        memoryData.setIndex(monitorItemDetail.getIndex());
        memoryData.setColor(monitorItemDetail.getColor());
        memoryData.setName(monitorItemDetail.getName());
        return memoryData;
    }

    private void insertMemoryData() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertMemoryData");
        }
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
