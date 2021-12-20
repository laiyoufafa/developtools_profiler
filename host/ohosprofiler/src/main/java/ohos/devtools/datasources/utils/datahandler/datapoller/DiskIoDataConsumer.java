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
import ohos.devtools.datasources.databases.datatable.DiskIoTable;
import ohos.devtools.datasources.databases.datatable.enties.DiskIOData;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.DiskioPluginResult;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.services.diskio.DiskIoDataCache;
import ohos.devtools.views.charts.model.ChartDataModel;
import ohos.devtools.views.charts.utils.ChartUtils;
import ohos.devtools.views.layout.chartview.MonitorItemDetail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import static ohos.devtools.views.layout.chartview.MonitorItemDetail.DISK_IO_READ;
import static ohos.devtools.views.layout.chartview.MonitorItemDetail.DISK_IO_WRITE;

/**
 * Consumer class of DiskIoData, Perform disk IO data construction and table insertion
 *
 * @since 2021/9/20
 */
public class DiskIoDataConsumer extends AbsDataConsumer {
    private static final Logger LOGGER = LogManager.getLogger(DiskIoDataConsumer.class);
    private static final long SAVE_FREQ = 1000L;
    private static final int RATE_DENOMINATOR = 10;
    private static final int RATE_MOLECULAR = 9;

    private List<DiskIOData> sysDiskIoInfoList = new ArrayList<>();
    private Queue<CommonTypes.ProfilerPluginData> queue;
    private DiskIoTable diskIoTable;
    private Integer sessionId;
    private Long localSessionId;
    private boolean stopFlag = false;
    private boolean isInsert = false;
    private boolean canCalculationRate = false;
    private long flagTime = DateTimeUtil.getNowTimeLong();

    /**
     * DiskIoDataConsumer
     */
    public DiskIoDataConsumer() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("DiskIoDataConsumer init");
        }
    }

    @Override
    public void run() {
        while (!stopFlag) {
            CommonTypes.ProfilerPluginData poll = queue.poll();
            if (poll != null) {
                handleDiskIoData(poll);
            } else {
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
            }
            insertDiskIoData();
        }
    }

    @Override
    public void init(Queue queue, int sessionId, long localSessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("init");
        }
        this.queue = queue;
        this.diskIoTable = new DiskIoTable();
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
     * handleDiskIoData
     *
     * @param diskIoData diskIoData
     */
    private void handleDiskIoData(CommonTypes.ProfilerPluginData diskIoData) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("handleDiskIoData");
        }
        DiskioPluginResult.DiskioData.Builder builder = DiskioPluginResult.DiskioData.newBuilder();
        DiskioPluginResult.DiskioData diskData = null;
        try {
            diskData = builder.mergeFrom(diskIoData.getData()).build();
        } catch (InvalidProtocolBufferException exception) {
            return;
        }
        if (stopFlag) {
            return;
        }
        BigDecimal readValue;
        BigDecimal writeValue;
        if (canCalculationRate) {
            readValue =
                setCalculationRate(diskData.getRdSectorsKb(), diskData.getPrevRdSectorsKb(), diskData.getTimestamp(),
                    diskData.getPrevTimestamp());
            writeValue =
                setCalculationRate(diskData.getWrSectorsKb(), diskData.getPrevWrSectorsKb(), diskData.getTimestamp(),
                    diskData.getPrevTimestamp());

        } else {
            readValue = new BigDecimal(0);
            writeValue = new BigDecimal(0);
            canCalculationRate = true;
        }
        DiskIOData sysDiskIoInfo = new DiskIOData();
        sysDiskIoInfo.setReadSectorsKb(readValue);
        sysDiskIoInfo.setWriteSectorsKb(writeValue);
        sysDiskIoInfo.setSession(localSessionId);
        sysDiskIoInfo.setSessionId(sessionId);
        long timeStamp = (diskIoData.getTvSec() * 1000000000L + diskIoData.getTvNsec()) / 1000000;
        sysDiskIoInfo.setTimeStamp(timeStamp);
        sysDiskIoInfoList.add(sysDiskIoInfo);
        addDataToCache(sysDiskIoInfo);
        isInsert = false;
        insertDiskIoData();
    }

    /**
     * Process and add diskIo info to cache
     *
     * @param sysDiskIoInfo sysDiskIoInfo
     */
    private void addDataToCache(DiskIOData sysDiskIoInfo) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addDataToCache");
        }
        List<ChartDataModel> dataModels = sysDiskIoSummary(sysDiskIoInfo);
        DiskIoDataCache.getInstance().addDataModel(localSessionId, sysDiskIoInfo.getTimeStamp(), dataModels);
    }

    /**
     * SysDiskIo into chart needed
     *
     * @param diskIoInfo diskIoInfo
     * @return List <ChartDataModel>
     */
    public static List<ChartDataModel> sysDiskIoSummary(DiskIOData diskIoInfo) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("sysDiskIoSummary");
        }
        List<ChartDataModel> list = new ArrayList<>();

        ChartDataModel readDiskIo = buildChartDataModel(DISK_IO_READ);
        ChartDataModel writeDiskIo = buildChartDataModel(DISK_IO_WRITE);

        readDiskIo.setValue(diskIoInfo.getReadSectorsKb().intValue());
        list.add(readDiskIo);
        writeDiskIo.setValue(diskIoInfo.getWriteSectorsKb().intValue());
        list.add(writeDiskIo);

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
    private void insertDiskIoData() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertDiskIoData");
        }
        if (!isInsert) {
            long now = DateTimeUtil.getNowTimeLong();
            if (now - flagTime > SAVE_FREQ) {
                diskIoTable.insertSysDiskIoInfo(sysDiskIoInfoList);
                sysDiskIoInfoList.clear();
                // Update flagTime.
                flagTime = now;
            }
            isInsert = true;
        }
    }

    /**
     * set Calculation Rate
     *
     * @param currentKb currentKb
     * @param LastTimeKb LastTimeKb
     * @param diskSampleTime diskSampleTime
     * @param diskPrevSampleTime diskPrevSampleTime
     * @return double
     */
    private static BigDecimal setCalculationRate(long currentKb, long LastTimeKb,
        DiskioPluginResult.CollectTimeStamp diskSampleTime, DiskioPluginResult.CollectTimeStamp diskPrevSampleTime) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("setCalculationRate");
        }
        BigDecimal pow = new BigDecimal(RATE_DENOMINATOR).pow(RATE_MOLECULAR);
        BigDecimal curSampleTime = new BigDecimal(diskSampleTime.getTvSec())
            .add(ChartUtils.divide(new BigDecimal(diskSampleTime.getTvNsec()), pow));
        BigDecimal prevSampleSec = new BigDecimal(diskPrevSampleTime.getTvSec());
        BigDecimal prevSampleTime =
            prevSampleSec.add(ChartUtils.divide(new BigDecimal(diskPrevSampleTime.getTvNsec()), pow));
        BigDecimal ultimateTime = curSampleTime.subtract(prevSampleTime);
        return ChartUtils.divide(new BigDecimal(currentKb - LastTimeKb), ultimateTime);
    }
}
