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
import com.intellij.ui.JBColor;
import ohos.devtools.datasources.databases.datatable.CpuTable;
import ohos.devtools.datasources.databases.datatable.enties.ProcessCpuData;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.CpuPluginResult;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.services.cpu.CpuDataCache;
import ohos.devtools.views.charts.model.ChartDataModel;
import ohos.devtools.views.layout.chartview.MonitorItemDetail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * CpuDataConsumer
 *
 * @since 2021/11/22
 */
public class CpuDataConsumer extends AbsDataConsumer {
    private static final Logger LOGGER = LogManager.getLogger(CpuDataConsumer.class);
    private static final long SAVE_FREQ = 1000L;
    private static CpuPluginResult.CpuData prevData = null;
    private static List<ChartDataModel> lastThread = null;

    private List<ProcessCpuData> processCpuDataList = new ArrayList<>();
    private Map<String, ChartDataModel> threadMap = new HashMap<>();
    private Map<Long, ChartDataModel> deadThreadInfo = new HashMap<>();
    private DeviceIPPortInfo deviceIPPortInfo;
    private Queue<CommonTypes.ProfilerPluginData> queue;
    private CpuTable cpuTable;
    private Integer sessionId;
    private Long localSessionId;
    private int logIndex = 0;
    private boolean stopFlag = false;
    private boolean isInsert = false;

    /**
     * Time reference variable for saving data to the cpu database at the interval specified by SAVE_FREQ.
     */
    private long flagTime = DateTimeUtil.getNowTimeLong();

    /**
     * CpuDataConsumer
     */
    public CpuDataConsumer() {
        super();
    }

    /**
     * Run CpuDataConsumer.
     */
    @Override
    public void run() {
        while (!stopFlag) {
            CommonTypes.ProfilerPluginData poll = queue.poll();
            if (poll != null) {
                handleCpuData(poll);
            } else {
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
            }
            insertCpuData();
        }
    }

    @Override
    public void init(Queue queue, int sessionId, long localSessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("init");
        }
        this.queue = queue;
        this.cpuTable = new CpuTable();
        this.sessionId = sessionId;
        this.localSessionId = localSessionId;
        this.deviceIPPortInfo = SessionManager.getInstance().getDeviceInfoBySessionId(localSessionId);
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

    private void handleCpuData(CommonTypes.ProfilerPluginData cpuDataParam) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("handleCpuData");
        }
        CpuPluginResult.CpuData.Builder builder = CpuPluginResult.CpuData.newBuilder();
        CpuPluginResult.CpuData cpudata = null;
        try {
            cpudata = builder.mergeFrom(cpuDataParam.getData()).build();
        } catch (InvalidProtocolBufferException exe) {
            return;
        }
        if (stopFlag) {
            return;
        }
        ProcessCpuData procCpuData = new ProcessCpuData();
        procCpuData.setData(cpudata);
        procCpuData.setSession(localSessionId);
        procCpuData.setSessionId(sessionId);
        long timeStamp = (cpuDataParam.getTvSec() * 1000000000L + cpuDataParam.getTvNsec()) / 1000000;
        procCpuData.setTimeStamp(timeStamp);
        processCpuDataList.add(procCpuData);
        addDataToCache(procCpuData);
        isInsert = false;
        insertCpuData();
    }

    /**
     * addDataToCache
     *
     * @param procCpuData ProcessCpuData
     */
    private void addDataToCache(ProcessCpuData procCpuData) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addDataToCache");
        }
        List<ChartDataModel> cpuDataModels = getProcessData(procCpuData.getData());
        CpuDataCache.getInstance().addCpuDataModel(localSessionId, procCpuData.getTimeStamp(), cpuDataModels);
        List<ChartDataModel> threadModels = getThreadStatus(procCpuData.getData());
        CpuDataCache.getInstance().addThreadDataModel(localSessionId, procCpuData.getTimeStamp(), threadModels);
        if (lastThread == null) {
            lastThread = threadModels;
        } else {
            List<ChartDataModel> collect = new ArrayList<>();
            lastThread.stream().forEach(
                chartDataModel -> {
                    String key = chartDataModel.getName() + chartDataModel.getIndex();
                    ChartDataModel lastChartDataModel = threadMap.get(key);
                    if (lastChartDataModel == null) {
                        deadThreadInfo.put(procCpuData.getTimeStamp(), chartDataModel);
                        collect.add(chartDataModel);
                    }
                }
            );
            if (!collect.isEmpty()) {
                CpuDataCache.getInstance().addDeadThreadDataModel(localSessionId, procCpuData.getTimeStamp(), collect);
            }
        }
        lastThread = threadModels;
    }

    /**
     * getProcessData
     *
     * @param cpuData cpuData
     * @return List <ChartDataModel>
     */
    public static List<ChartDataModel> getProcessData(CpuPluginResult.CpuData cpuData) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getProcessData");
        }
        ChartDataModel appModel = buildChartDataModel(MonitorItemDetail.CPU_APP);
        long dataTimestamp = TimeUnit.NANOSECONDS.toMicros(cpuData.getCpuUsageInfo().getTimestamp().getTvSec());
        long elapsedTime = (cpuData.getCpuUsageInfo().getSystemBootTimeMs() - cpuData.getCpuUsageInfo()
            .getPrevSystemBootTimeMs());  // system_boot_time_ms
        double appValue = 100.0 * (cpuData.getCpuUsageInfo().getProcessCpuTimeMs() - cpuData.getCpuUsageInfo()
            .getPrevProcessCpuTimeMs()) / elapsedTime;  // process_cpu_time_ms
        double systemValue = 100.0 * (cpuData.getCpuUsageInfo().getSystemCpuTimeMs() - cpuData.getCpuUsageInfo()
            .getPrevSystemCpuTimeMs()) / elapsedTime;  // system_cpu_time_ms
        if (ProfilerLogManager.isWarnEnabled()) {
            if (cpuData.getCpuUsageInfo().getProcessCpuTimeMs()
                    != cpuData.getCpuUsageInfo().getPrevProcessCpuTimeMs()) {
                LOGGER.debug("---------------------------------------------------------------------------");
                LOGGER.warn("getSystemBootTimeMs:{}", cpuData.getCpuUsageInfo().getSystemBootTimeMs());
                LOGGER.warn("getPrevSystemBootTimeMs:{}", cpuData.getCpuUsageInfo().getPrevSystemBootTimeMs());
                LOGGER.warn("getProcessCpuTimeMs:{}", cpuData.getCpuUsageInfo().getProcessCpuTimeMs());
                LOGGER.warn("getPrevProcessCpuTimeMs:{}", cpuData.getCpuUsageInfo().getPrevProcessCpuTimeMs());
                LOGGER.warn("getSystemCpuTimeMs:{}", cpuData.getCpuUsageInfo().getSystemCpuTimeMs());
                LOGGER.warn("getPrevSystemCpuTimeMs:{}", cpuData.getCpuUsageInfo().getPrevSystemCpuTimeMs());
            }
        }
        systemValue = Math.max(0, Math.min(systemValue, 100.0));
        appValue = Math.max(0, Math.min(appValue, systemValue));
        appModel.setDoubleValue(appValue);
        appModel.setValue((int) Math.ceil(appValue));
        ChartDataModel systemModel = buildChartDataModel(MonitorItemDetail.CPU_SYSTEM);
        systemModel.setValue((int) Math.ceil(systemValue));
        systemModel.setDoubleValue(systemValue);
        prevData = cpuData;
        List<ChartDataModel> list = new ArrayList<>();
        list.add(systemModel);
        list.add(appModel);
        return list;
    }

    /**
     * getThreadStatus
     *
     * @param cpuData cpuData
     * @return List <ChartDataModel>
     */
    public List<ChartDataModel> getThreadStatus(CpuPluginResult.CpuData cpuData) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getThreadStatus");
        }
        List<ChartDataModel> list = new ArrayList<>();
        threadMap.clear();
        // add the thread info data
        long elapsedTime =
            (cpuData.getCpuUsageInfo().getSystemBootTimeMs() - cpuData.getCpuUsageInfo().getPrevSystemBootTimeMs());
        cpuData.getThreadInfoList().forEach(threadInfo -> {
            double threadValue = 0d;
            if (elapsedTime != 0) {
                threadValue =
                    100.0 * (threadInfo.getThreadCpuTimeMs() - threadInfo.getPrevThreadCpuTimeMs()) / elapsedTime;
            }
            BigDecimal bigDecimal = new BigDecimal(threadValue);
            ChartDataModel threadInfoModel = new ChartDataModel();
            threadInfoModel.setIndex(threadInfo.getTid());
            threadInfoModel.setColor(JBColor.GREEN);
            threadInfoModel.setName(threadInfo.getThreadName());
            threadInfoModel.setValue(threadInfo.getThreadStateValue());
            threadInfoModel.setDoubleValue(bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
            String key = threadInfo.getThreadName() + threadInfo.getTid();
            threadMap.put(key, threadInfoModel);
            list.add(threadInfoModel);
        });
        return list;
    }

    /**
     * buildChartDataModel
     *
     * @param monitorItemDetail MonitorItemDetail
     * @return ChartDataModel
     */
    private static ChartDataModel buildChartDataModel(MonitorItemDetail monitorItemDetail) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("buildChartDataModel");
        }
        ChartDataModel cpuData = new ChartDataModel();
        cpuData.setIndex(monitorItemDetail.getIndex());
        cpuData.setColor(monitorItemDetail.getColor());
        cpuData.setName(monitorItemDetail.getName());
        return cpuData;
    }

    private void insertCpuData() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertCpuData");
        }
        if (!isInsert) {
            long now = DateTimeUtil.getNowTimeLong();
            if (now - flagTime > SAVE_FREQ) {
                cpuTable.insertProcessCpuInfo(processCpuDataList);
                processCpuDataList.clear();
                cpuTable.insertDeadThreadInfo(deadThreadInfo, localSessionId);
                deadThreadInfo.clear();
                // Update flagTime.
                flagTime = now;
            }
            isInsert = true;
        }
    }
}
