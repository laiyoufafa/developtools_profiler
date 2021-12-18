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

package ohos.devtools.samplecode.datasources.utils.datahandler.datapoller;

import com.google.protobuf.InvalidProtocolBufferException;
import com.intellij.ui.JBColor;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.SamplePluginResult;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.datahandler.datapoller.AbsDataConsumer;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.samplecode.datasources.databases.datatable.SampleCodeTable;
import ohos.devtools.samplecode.datasources.databases.datatable.enties.SampleCodeInfo;
import ohos.devtools.samplecode.services.samplecode.SampleCodeDataCache;
import ohos.devtools.views.charts.model.ChartDataModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Consumer class of data
 *
 * @since 2021/11/24
 */
public class SampleCodeConsumer extends AbsDataConsumer {
    private static final Logger LOGGER = LogManager.getLogger(SampleCodeConsumer.class);

    /**
     * Interval for saving data to the database in ms.
     */
    private static final long SAVE_FREQ = 1000L;

    private final List<SampleCodeInfo> sampleCodeInfoList = new ArrayList<>();
    private Queue<CommonTypes.ProfilerPluginData> queue;
    private SampleCodeTable sampleCodeTable;
    private Integer sessionId;
    private Long localSessionId;

    /**
     * stop status
     */
    private boolean stopFlag = false;

    /**
     * Insert status
     */
    private boolean isInsert = false;

    /**
     * Whether to calculate the rate
     */
    private boolean canCalculationRate = false;

    /**
     * Time reference variable for saving data to the in-memory database at the interval specified by SAVE_FREQ.
     */
    private long flagTime = DateTimeUtil.getNowTimeLong();

    /**
     * SampleCode Consumer
     */
    public SampleCodeConsumer() {
        super();
    }

    @Override
    public void run() {
        while (!stopFlag) {
            long now = DateTimeUtil.getNowTimeLong();
            if (now - flagTime > SAVE_FREQ) {
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException exception) {
                    LOGGER.info("SampleCodeConsumer InterruptedException");
                }
            }
            if (queue.size() > 0) {
                CommonTypes.ProfilerPluginData poll = queue.poll();
                if (poll == null) {
                    return;
                }
                handleData(poll);
            }
            insertData();
        }
    }

    @Override
    public void init(Queue queue, int sessionId, long localSessionId) {
        this.queue = queue;
        this.sampleCodeTable = new SampleCodeTable();
        this.sessionId = sessionId;
        this.localSessionId = localSessionId;
    }

    /**
     * shutDown
     */
    public void shutDown() {
        stopFlag = true;
    }

    /**
     * handleData
     *
     * @param sampleData sampleData
     */
    private void handleData(CommonTypes.ProfilerPluginData sampleData) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("handleData");
        }
        SamplePluginResult.SampleData.Builder builder = SamplePluginResult.SampleData.newBuilder();
        SamplePluginResult.SampleData sample = null;
        try {
            sample = builder.mergeFrom(sampleData.getData()).build();
        } catch (InvalidProtocolBufferException exe) {
            return;
        }
        SampleCodeInfo sampleCodeInfo = new SampleCodeInfo();
        sampleCodeInfo.setSession(localSessionId);
        sampleCodeInfo.setSessionId(sessionId);
        sampleCodeInfo.setIntData(sample.getIntData());
        sampleCodeInfo.setDoubleData(sample.getDoubleData());
        long timeStamp = (sampleData.getTvSec() * 1000000000L + sampleData.getTvNsec()) / 1000000;
        sampleCodeInfo.setTimeStamp(timeStamp);
        sampleCodeInfoList.add(sampleCodeInfo);
        addDataToCache(sampleCodeInfo);
        isInsert = false;
        insertData();
    }

    /**
     * Process and add info to cache
     *
     * @param sampleCodeInfo sampleCodeInfo
     */
    private void addDataToCache(SampleCodeInfo sampleCodeInfo) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addDataToCache");
        }
        List<ChartDataModel> dataModels = sampleSummary(sampleCodeInfo);
        SampleCodeDataCache.getInstance().addDataModel(localSessionId, sampleCodeInfo.getTimeStamp(), dataModels);
    }

    /**
     * SysDiskIo into chart needed
     *
     * @param sampleCodeInfo sampleCodeInfo
     * @return List <ChartDataModel>
     */
    public static List<ChartDataModel> sampleSummary(SampleCodeInfo sampleCodeInfo) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("sampleSummary");
        }
        List<ChartDataModel> list = new ArrayList<>();

        ChartDataModel intValue = buildChartDataModel(MonitorItemDetail.INT_VALUE);
        ChartDataModel doubleValue = buildChartDataModel(MonitorItemDetail.DOUBLE_VALUE);

        intValue.setValue(sampleCodeInfo.getIntData());
        list.add(intValue);
        doubleValue.setValue((int) sampleCodeInfo.getDoubleData());
        list.add(doubleValue);

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

    /**
     * insertDiskIoData
     */
    private void insertData() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertData");
        }
        if (!isInsert) {
            long now = DateTimeUtil.getNowTimeLong();
            if (now - flagTime > SAVE_FREQ) {
                sampleCodeTable.insertData(sampleCodeInfoList);
                sampleCodeInfoList.clear();
                // Update flagTime.
                flagTime = now;
            }
            isInsert = true;
        }
    }

    private enum MonitorItemDetail {
        /**
         * Read
         */
        INT_VALUE(0, "int", JBColor.RED),

        /**
         * write
         */
        DOUBLE_VALUE(1, "double", JBColor.yellow);

        private final int index;
        private final String name;
        private final Color color;

        MonitorItemDetail(int index, String name, Color color) {
            this.index = index;
            this.name = name;
            this.color = color;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public Color getColor() {
            return color;
        }
    }
}
