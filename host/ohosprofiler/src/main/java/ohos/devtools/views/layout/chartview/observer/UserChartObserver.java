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

package ohos.devtools.views.layout.chartview.observer;

import com.google.protobuf.InvalidProtocolBufferException;
import model.AbstractSdk;
import ohos.devtools.datasources.databases.datatable.enties.UserData;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.services.userdata.UserDataCache;
import ohos.devtools.services.userdata.UserDataDao;
import ohos.devtools.views.charts.ProfilerChart;
import ohos.devtools.views.charts.model.ChartDataModel;
import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartStandard;
import ohos.devtools.views.layout.chartview.event.IChartEventObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sdk Chart Observer
 *
 * @since 2021/11/22
 */
public class UserChartObserver implements IChartEventObserver {
    private static final Logger LOGGER = LogManager.getLogger(UserChartObserver.class);

    private final ProfilerChart chart;
    private final long sessionId;
    private boolean chartFold;
    private AbstractSdk sdk;

    /**
     * DiskIoChartObserver.
     *
     * @param chart chart
     * @param sessionId sessionId
     * @param chartFold chartFold
     * @param sdk SdkImpl
     */
    public UserChartObserver(ProfilerChart chart, long sessionId, boolean chartFold, AbstractSdk sdk) {
        this.chart = chart;
        this.sessionId = sessionId;
        this.chartFold = chartFold;
        this.sdk = sdk;
    }

    /**
     * Refresh chart drawing standard
     *
     * @param startTime Start time of chart
     * @param endTime End time of chart
     * @param maxDisplayMillis Maximum display time on view
     * @param minMarkInterval The minimum scale interval
     */
    @Override
    public void refreshStandard(int startTime, int endTime, int maxDisplayMillis, int minMarkInterval) {
        chart.setMaxDisplayX(maxDisplayMillis);
        chart.setMinMarkIntervalX(minMarkInterval);
        chart.setStartTime(startTime);
        chart.setEndTime(endTime);
        chart.repaint();
        chart.revalidate();
    }

    @Override
    public void refreshView(ChartDataRange range, long firstTimestamp, boolean useCache) {
        LinkedHashMap<Integer, List<ChartDataModel>> queryResult = new LinkedHashMap<>();
        int start = range.getStartTime();
        int end = range.getEndTime();
        boolean chartStop = chart.getBottomPanel().isPause() || chart.getBottomPanel().isStop();
        LinkedHashMap<Integer, UserData> data;
        if (chartStop || !useCache) {
            data = UserDataDao.getInstance().getData(sessionId, start, end, firstTimestamp, true);
        } else {
            data = UserDataCache.getInstance().getData(sessionId, start, end);
        }
        Set<Map.Entry<Integer, UserData>> entries = data.entrySet();
        entries.forEach(integerSdkDataEntry -> {
            byte[] bytes = integerSdkDataEntry.getValue().getBytes();
            CommonTypes.ProfilerPluginData.Builder builders = CommonTypes.ProfilerPluginData.newBuilder();
            try {
                CommonTypes.ProfilerPluginData plugData = builders.mergeFrom(bytes).build();
                List<model.bean.ChartDataModel> sdkChartDataModel = sdk.sampleData(plugData.getData());
                List<ChartDataModel> chartDataModels = sdkChartDataModel.stream().map(item -> {
                    ChartDataModel model = new ChartDataModel();
                    model.setColor(item.getColor());
                    model.setIndex(item.getIndex());
                    model.setName(item.getName());
                    model.setValue(item.getValue());
                    return model;
                }).collect(Collectors.toList());
                queryResult.put(integerSdkDataEntry.getKey(), chartDataModels);
            } catch (InvalidProtocolBufferException exe) {
                exe.printStackTrace();
            }
        });
        chart.refreshChart(range.getStartTime(), end, queryResult);
    }

    /**
     * Update fold status and refresh current chart
     *
     * @param chartFold true: Chart fold/expand
     */
    public void setChartFold(boolean chartFold) {
        this.chartFold = chartFold;
        refreshManually();
    }

    /**
     * Refresh current chart manually
     */
    public void refreshManually() {
        ChartStandard standard = chart.getBottomPanel().getPublisher().getStandard();
        ChartDataRange range = standard.getDisplayRange();
        long firstTs = standard.getFirstTimestamp();
        refreshView(range, firstTs, !chart.getBottomPanel().getPublisher().isTraceFile());
    }
}
