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

package ohos.devtools.views.user;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import model.AbstractSdk;
import model.bean.ChartDataModel;
import model.bean.ChartEnum;
import model.bean.Legend;
import ohos.devtools.datasources.transport.grpc.service.DiskioPluginConfig;
import ohos.devtools.datasources.transport.grpc.service.DiskioPluginResult;
import ohos.devtools.views.charts.utils.ChartUtils;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * User Impl
 *
 * @since 2021/11/22
 */
public class UserImpl extends AbstractSdk {
    private static boolean canCalculationRate = false;

    @Override
    protected void init() {
        LinkedList<Legend> list = new LinkedList<>();
        Legend readLegend = new Legend();
        readLegend.setLegendColor(new Color(0xFFDD9A16, true));
        readLegend.setLegendName("SDK_read:");
        list.add(readLegend);
        Legend writeLegend = new Legend();
        writeLegend.setLegendColor(new Color(0xFF04A9AC, true));
        writeLegend.setLegendName("SDK_write:");
        list.add(writeLegend);
        initLegend(list);
        setPluginFileName("/data/local/tmp/libdiskiodataplugin.z.so");
        setPluginDataName("diskio-plugin");
        setChartType(ChartEnum.BROKEN_LINE);
        setUnit("Kb/s");
        setTitleName("SDK");
        setLegends(list);
    }

    @Override
    public ByteString getPluginByteString(int pid) {
        DiskioPluginConfig.DiskioConfig.Builder builder = DiskioPluginConfig.DiskioConfig.newBuilder();
        if (pid > 0) {
            builder.setUnspeciFied(pid);
        }
        DiskioPluginConfig.DiskioConfig plug = builder.build();
        return plug.toByteString();
    }

    @Override
    public List<ChartDataModel> sampleData(ByteString data) {
        DiskioPluginResult.DiskioData.Builder builder = DiskioPluginResult.DiskioData.newBuilder();
        DiskioPluginResult.DiskioData sdkData = null;
        try {
            sdkData = builder.mergeFrom(data).build();
        } catch (InvalidProtocolBufferException exe) {
            return new ArrayList<>();
        }
        BigDecimal readValue;
        BigDecimal writeValue;
        if (canCalculationRate) {
            readValue =
                setCalculationRate(sdkData.getRdSectorsKb(), sdkData.getPrevRdSectorsKb(), sdkData.getTimestamp(),
                    sdkData.getPrevTimestamp());
            writeValue =
                setCalculationRate(sdkData.getWrSectorsKb(), sdkData.getPrevWrSectorsKb(), sdkData.getTimestamp(),
                    sdkData.getPrevTimestamp());

        } else {
            readValue = new BigDecimal(0);
            writeValue = new BigDecimal(0);
            canCalculationRate = true;
        }
        List<ChartDataModel> list = new ArrayList<>();
        list.add(getChartDataModel(readValue.intValue(), 0));
        list.add(getChartDataModel(writeValue.intValue(), 1));
        return list;
    }

    private BigDecimal setCalculationRate(long currentKb, long LastTimeKb,
        DiskioPluginResult.CollectTimeStamp diskSampleTime, DiskioPluginResult.CollectTimeStamp diskPrevSampleTime) {
        BigDecimal pow = new BigDecimal(10).pow(9);
        BigDecimal curSampleTime = new BigDecimal(diskSampleTime.getTvSec())
            .add(ChartUtils.divide(new BigDecimal(diskSampleTime.getTvNsec()), pow));
        BigDecimal prevSampleSec = new BigDecimal(diskPrevSampleTime.getTvSec());
        BigDecimal prevSampleTime =
            prevSampleSec.add(ChartUtils.divide(new BigDecimal(diskPrevSampleTime.getTvNsec()), pow));
        BigDecimal ultimateTime = curSampleTime.subtract(prevSampleTime);
        return ChartUtils.divide(new BigDecimal(currentKb - LastTimeKb), ultimateTime);
    }
}
