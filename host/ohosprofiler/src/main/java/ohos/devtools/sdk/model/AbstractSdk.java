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

package ohos.devtools.sdk.model;

import com.google.protobuf.ByteString;
import model.bean.ChartDataModel;
import model.bean.ChartEnum;
import model.bean.Legend;

import java.util.List;

/**
 * AbstractSdk
 *
 * @since 2021/11/20
 */
public abstract class AbstractSdk {
    private List<Legend> legends;
    private String pluginFileName;
    private String pluginDataName;
    private ChartEnum chartType;
    private String unit;
    private String titleName;

    /**
     * AbstractSdk constructor
     */
    public AbstractSdk() {
        init();
    }

    /**
     * init
     */
    protected abstract void init();

    /**
     * getPluginByteString
     *
     * @param pid pid
     * @return ByteString
     */
    public abstract ByteString getPluginByteString(int pid);

    /**
     * sampleData
     *
     * @param data data
     * @return List<ChartDataModel>
     */
    public abstract List<ChartDataModel> sampleData(ByteString data);

    /**
     * getChartDataModel
     *
     * @param value value
     * @param index index
     * @return ChartDataModel
     */
    public ChartDataModel getChartDataModel(int value, int index) {
        Legend legend = this.getLegends().get(index);
        ChartDataModel item = new ChartDataModel();
        item.setIndex(legend.getIndex());
        item.setColor(legend.getLegendColor());
        item.setName(legend.getLegendName());
        item.setValue(value);
        return item;
    }

    /**
     * getChartDataModel
     *
     * @param value value
     * @param index index
     * @return ChartDataModel
     */
    public ChartDataModel getChartDataModel(double value, int index) {
        Legend legend = this.getLegends().get(index);
        ChartDataModel item = new ChartDataModel();
        item.setIndex(legend.getIndex());
        item.setColor(legend.getLegendColor());
        item.setName(legend.getLegendName());
        item.setDoubleValue(value);
        return item;
    }

    /**
     * initLegend
     *
     * @param legends legends
     */
    public void initLegend(List<Legend> legends) {
        for (int index = 0; index < legends.size(); index++) {
            legends.get(index).setIndex(index);
            if (legends.get(index).getLegendHeight() == 0) {
                legends.get(index).setLegendHeight(5);
            }
            if (legends.get(index).getLegendWidth() == 0) {
                legends.get(index).setLegendWidth(12);
            }
        }
    }

    public List<Legend> getLegends() {
        return legends;
    }

    public void setLegends(List<Legend> legends) {
        this.legends = legends;
    }

    public String getPluginFileName() {
        return pluginFileName;
    }

    public void setPluginFileName(String pluginFileName) {
        this.pluginFileName = pluginFileName;
    }

    public String getPluginDataName() {
        return pluginDataName;
    }

    public void setPluginDataName(String pluginDataName) {
        this.pluginDataName = pluginDataName;
    }

    public ChartEnum getChartType() {
        return chartType;
    }

    public void setChartType(ChartEnum chartType) {
        this.chartType = chartType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getTitleName() {
        return titleName;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }
}
