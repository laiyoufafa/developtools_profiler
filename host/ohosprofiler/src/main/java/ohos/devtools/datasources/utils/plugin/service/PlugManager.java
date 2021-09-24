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

package ohos.devtools.datasources.utils.plugin.service;

import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.datasources.utils.plugin.IPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.PluginConf;
import ohos.devtools.datasources.utils.plugin.entity.PluginMode;
import ohos.devtools.views.layout.chartview.ProfilerMonitorItem;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * PlugManager
 */
public class PlugManager {
    private static final Logger LOGGER = LogManager.getLogger(PlugManager.class);
    private static volatile PlugManager singleton;
    private MultiValueMap profilerConfigMap = new MultiValueMap();
    private List<PluginConf> confLists = new ArrayList<>();

    /**
     * get Instance
     *
     * @return PlugManager
     */
    public static PlugManager getInstance() {
        if (singleton == null) {
            synchronized (PlugManager.class) {
                if (singleton == null) {
                    singleton = new PlugManager();
                }
            }
        }
        return singleton;
    }

    private PlugManager() {
    }

    /**
     * get Plugin Config
     *
     * @param deviceType deviceType
     * @param pluginMode pluginMode
     * @return List <PluginConf>
     */
    public List<PluginConf> getPluginConfig(DeviceType deviceType, PluginMode pluginMode) {
        if (Objects.isNull(pluginMode)) {
            return confLists.stream().filter(hiProfilerPluginConf -> {
                List<DeviceType> supportDeviceTypes = hiProfilerPluginConf.getSupportDeviceTypes();
                if (supportDeviceTypes.isEmpty()) {
                    return hiProfilerPluginConf.isEnable();
                } else {
                    return supportDeviceTypes.contains(deviceType) && hiProfilerPluginConf.isEnable();
                }
            }).collect(Collectors.toList());
        }
        return confLists.stream().filter(hiProfilerPluginConf -> {
            List<DeviceType> supportDeviceTypes = hiProfilerPluginConf.getSupportDeviceTypes();
            if (supportDeviceTypes.isEmpty()) {
                return hiProfilerPluginConf.isEnable() && hiProfilerPluginConf.getPluginMode() == pluginMode;
            } else {
                return supportDeviceTypes.contains(deviceType) && hiProfilerPluginConf.isEnable()
                    && hiProfilerPluginConf.getPluginMode() == pluginMode;
            }
        }).collect(Collectors.toList());
    }

    /**
     * loadingPlug
     *
     * @param pluginConfigs pluginConfigs
     */
    public void loadingPlugs(List<Class<? extends IPluginConfig>> pluginConfigs) {
        if (pluginConfigs != null && pluginConfigs.size() > 0) {
            for (Class<? extends IPluginConfig> pluginConfigPackage : pluginConfigs) {
                IPluginConfig config;
                try {
                    if (pluginConfigPackage != null) {
                        config = pluginConfigPackage.getConstructor().newInstance();
                        config.registerPlugin();
                    }
                } catch (InstantiationException |
                    IllegalAccessException |
                    InvocationTargetException |
                    NoSuchMethodException exception) {
                    LOGGER.error("registerPlugin exception {}", exception.getMessage());
                    continue;
                }
            }
        }
    }

    /**
     * loadingPlug
     *
     * @param pluginConfig pluginConfig
     */
    public void loadingPlug(Class<? extends IPluginConfig> pluginConfig) {
        IPluginConfig config;
        try {
            if (pluginConfig != null) {
                config = pluginConfig.getConstructor().newInstance();
                config.registerPlugin();
            }
        } catch (InstantiationException |
            IllegalAccessException |
            InvocationTargetException |
            NoSuchMethodException exception) {
            LOGGER.error("registerPlugin exception {}", exception.getMessage());
        }
    }

    /**
     * registerPlugin
     *
     * @param pluginConf pluginConf
     */
    public void registerPlugin(PluginConf pluginConf) {
        confLists.add(pluginConf);
    }

    /**
     * Add a plug-in that started successfully
     *
     * @param sessionId sessionId
     * @param pluginConf hiProfilerPluginConf
     */
    public void addPluginStartSuccess(long sessionId, PluginConf pluginConf) {
        profilerConfigMap.put(sessionId, pluginConf);
    }

    /**
     * getProfilerMonitorItemMap
     *
     * @param sessionId sessionId
     * @return List <PluginConf>
     */
    public List<PluginConf> getProfilerPlugConfig(long sessionId) {
        Collection<PluginConf> collection = profilerConfigMap.getCollection(sessionId);
        if (Objects.nonNull(collection)) {
            return collection.stream().collect(Collectors.toList());
        }
        return new ArrayList();
    }

    /**
     * getProfilerMonitorItemMap
     *
     * @param sessionId sessionId
     * @return List <ProfilerMonitorItem>
     */
    public List<ProfilerMonitorItem> getProfilerMonitorItemList(long sessionId) {
        Collection<PluginConf> collection = profilerConfigMap.getCollection(sessionId);
        if (Objects.nonNull(collection)) {
            List<ProfilerMonitorItem> itemList = collection.stream().filter(
                hiProfilerPluginConf -> hiProfilerPluginConf.isChartPlugin() && Objects
                    .nonNull(hiProfilerPluginConf.getMonitorItem()))
                .map(hiProfilerPluginConf -> hiProfilerPluginConf.getMonitorItem()).collect(Collectors.toList());
            if (Objects.nonNull(itemList)) {
                return itemList.stream().sorted(Comparator.comparingInt(ProfilerMonitorItem::getIndex))
                    .collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    /**
     * clear Profiler Monitor ItemMap
     */
    public void clearProfilerMonitorItemMap() {
        profilerConfigMap.clear();
    }

    /**
     * clear PluginConf List
     */
    public void clearPluginConfList() {
        confLists.clear();
    }
}
