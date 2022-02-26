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

package ohos.devtools.datasources.utils.plugin.entity;

import ohos.devtools.datasources.utils.datahandler.datapoller.AbsDataConsumer;
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.datasources.utils.plugin.ICreatePluginConfig;
import ohos.devtools.datasources.utils.plugin.IGetPluginName;
import ohos.devtools.datasources.utils.plugin.ISpecialStartPlugMethod;
import ohos.devtools.views.layout.chartview.ProfilerMonitorItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * PluginConf
 *
 * @since 2021/5/19 16:39
 */
public final class PluginConf {
    private String pluginFileName;
    private String pluginDataName;
    private IGetPluginName IGetPluginName;
    private boolean isEnable = true;
    private PluginMode pluginMode;
    private Class<? extends AbsDataConsumer> consumerClass;
    private boolean isChartPlugin;
    private ProfilerMonitorItem monitorItem;
    private PluginBufferConfig pluginBufferConfig;
    private ICreatePluginConfig ICreatePluginConfig;
    private boolean isSpecialStart;
    private ISpecialStartPlugMethod ISpecialStartPlugMethod;
    private boolean isAlwaysAdd;
    private boolean operationStart = false;
    private List<AnalysisType> analysisTypes = new ArrayList<>();
    private List<DeviceType> supportDeviceTypes = new ArrayList<>();

    /**
     * PluginConf
     *
     * @param pluginFileName pluginFileName
     * @param pluginDataName pluginDataName
     * @param consumerClass consumerClass
     * @param isChartPlugin isChartPlugin
     * @param monitorItem monitorItem
     */
    public PluginConf(String pluginFileName, String pluginDataName, Class<? extends AbsDataConsumer> consumerClass,
        boolean isChartPlugin, ProfilerMonitorItem monitorItem) {
        this.pluginFileName = pluginFileName;
        this.pluginDataName = pluginDataName;
        this.consumerClass = consumerClass;
        this.isChartPlugin = isChartPlugin;
        this.monitorItem = monitorItem;
    }

    /**
     * getPluginMode
     *
     * @return PluginMode
     */
    public PluginMode getPluginMode() {
        return pluginMode;
    }

    /**
     * setPluginMode
     *
     * @param pluginMode pluginMode
     */
    public void setPluginMode(PluginMode pluginMode) {
        this.pluginMode = pluginMode;
    }

    /**
     * getMonitorItem
     *
     * @return ProfilerMonitorItem
     */
    public ProfilerMonitorItem getMonitorItem() {
        return monitorItem;
    }

    /**
     * setMonitorItem
     *
     * @param monitorItem monitorItem
     */
    public void setMonitorItem(ProfilerMonitorItem monitorItem) {
        this.monitorItem = monitorItem;
    }

    /**
     * isChartPlugin
     *
     * @return boolean
     */
    public boolean isChartPlugin() {
        return isChartPlugin;
    }

    /**
     * setChartPlugin
     *
     * @param chartPlugin chartPlugin
     */
    public void setChartPlugin(boolean chartPlugin) {
        isChartPlugin = chartPlugin;
    }

    /**
     * getPluginBufferConfig
     *
     * @return PluginBufferConfig
     */
    public PluginBufferConfig getPluginBufferConfig() {
        if (pluginBufferConfig == null) {
            return new PluginBufferConfig(10, PluginBufferConfig.Policy.RECYCLE);
        }
        return pluginBufferConfig;
    }

    /**
     * setPluginBufferConfig
     *
     * @param pluginBufferConfig pluginBufferConfig
     */
    public void setPluginBufferConfig(PluginBufferConfig pluginBufferConfig) {
        this.pluginBufferConfig = pluginBufferConfig;
    }

    /**
     * getICreatePluginConfig
     *
     * @return ICreatePluginConfig
     */
    public ICreatePluginConfig getICreatePluginConfig() {
        return ICreatePluginConfig;
    }

    /**
     * setICreatePluginConfig
     *
     * @param ICreatePluginConfig ICreatePluginConfig
     */
    public void setICreatePluginConfig(ICreatePluginConfig ICreatePluginConfig) {
        this.ICreatePluginConfig = ICreatePluginConfig;
    }

    /**
     * getPluginFileName
     *
     * @return String
     */
    public String getPluginFileName() {
        return pluginFileName;
    }

    /**
     * setPluginFileName
     *
     * @param pluginFileName pluginFileName
     */
    public void setPluginFileName(String pluginFileName) {
        this.pluginFileName = pluginFileName;
    }

    /**
     * getPluginDataName
     *
     * @return String
     */
    public String getPluginDataName() {
        return pluginDataName;
    }

    /**
     * isSpecialStart
     *
     * @return boolean
     */
    public boolean isSpecialStart() {
        return isSpecialStart;
    }

    /**
     * setSpecialStart
     *
     * @param specialStart specialStart
     */
    public void setSpecialStart(boolean specialStart) {
        isSpecialStart = specialStart;
    }

    /**
     * getSpecialStartPlugMethod
     *
     * @return ISpecialStartPlugMethod
     */
    public ISpecialStartPlugMethod getSpecialStartPlugMethod() {
        return ISpecialStartPlugMethod;
    }

    /**
     * setSpecialStartPlugMethod
     *
     * @param ISpecialStartPlugMethod ISpecialStartPlugMethod
     */
    public void setSpecialStartPlugMethod(ISpecialStartPlugMethod ISpecialStartPlugMethod) {
        this.ISpecialStartPlugMethod = ISpecialStartPlugMethod;
    }

    /**
     * setPluginDataName
     *
     * @param pluginDataName pluginDataName
     */
    public void setPluginDataName(String pluginDataName) {
        this.pluginDataName = pluginDataName;
    }

    /**
     * isEnable
     *
     * @return boolean
     */
    public boolean isEnable() {
        return isEnable;
    }

    /**
     * setEnable
     *
     * @param enable enable
     */
    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    /**
     * getConsumerClass
     *
     * @return Class<? extends AbsDataConsumer>
     */
    public Class<? extends AbsDataConsumer> getConsumerClass() {
        return consumerClass;
    }

    /**
     * setConsumerClass
     *
     * @param consumerClass consumerClass
     */
    public void setConsumerClass(Class<? extends AbsDataConsumer> consumerClass) {
        this.consumerClass = consumerClass;
    }

    /**
     * getSupportDeviceTypes
     *
     * @return List<DeviceType>
     */
    public List<DeviceType> getSupportDeviceTypes() {
        return supportDeviceTypes;
    }

    /**
     * addSupportDeviceTypes
     *
     * @param supportDeviceTypes supportDeviceTypes
     */
    public void addSupportDeviceTypes(DeviceType supportDeviceTypes) {
        this.supportDeviceTypes.add(supportDeviceTypes);
    }

    /**
     * setGetPluginName
     *
     * @param IGetPluginName IGetPluginName
     */
    public void setGetPluginName(IGetPluginName IGetPluginName) {
        this.IGetPluginName = IGetPluginName;
    }

    /**
     * getGetPluginName
     *
     * @return IGetPluginName
     */
    public IGetPluginName getGetPluginName() {
        return IGetPluginName;
    }

    /**
     * isAlwaysAdd
     *
     * @return boolean
     */
    public boolean isAlwaysAdd() {
        return isAlwaysAdd;
    }

    /**
     * setAlwaysAdd
     *
     * @param alwaysAdd alwaysAdd
     */
    public void setAlwaysAdd(boolean alwaysAdd) {
        isAlwaysAdd = alwaysAdd;
    }

    /**
     * isOperationStart
     *
     * @return boolean
     */
    public boolean isOperationStart() {
        return operationStart;
    }

    /**
     * setOperationStart
     *
     * @param operationStart operationStart
     */
    public void setOperationStart(boolean operationStart) {
        this.operationStart = operationStart;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PluginConf that = (PluginConf) obj;
        return pluginFileName.equals(that.pluginFileName);
    }

    /**
     * get AnalysisTypes
     *
     * @return List<AnalysisType>
     */
    public List<AnalysisType> getAnalysisTypes() {
        return analysisTypes;
    }

    /**
     * add AnalysisTypes
     *
     * @param analysisType analysisType
     */
    public void addAnalysisTypes(AnalysisType analysisType) {
        this.analysisTypes.add(analysisType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginFileName);
    }

    @Override
    public String toString() {
        return "PluginConf{"
            + "pluginFileName='" + pluginFileName + '\''
            + ", pluginDataName='" + pluginDataName + '\''
            + ", IGetPluginName=" + IGetPluginName
            + ", isEnable=" + isEnable
            + ", pluginMode=" + pluginMode
            + ", consumerClass=" + consumerClass
            + ", isChartPlugin=" + isChartPlugin
            + ", monitorItem=" + monitorItem
            + ", pluginBufferConfig=" + pluginBufferConfig
            + ", ICreatePluginConfig=" + ICreatePluginConfig
            + ", isSpecialStart=" + isSpecialStart
            + ", ISpecialStartPlugMethod=" + ISpecialStartPlugMethod
            + ", isAlwaysAdd=" + isAlwaysAdd
            + ", operationStart=" + operationStart
            + ", supportDeviceTypes=" + supportDeviceTypes
            + '}';
    }
}