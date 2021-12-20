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

import ohos.devtools.datasources.utils.common.util.FileUtils;
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.datasources.utils.plugin.IPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.AnalysisType;
import ohos.devtools.datasources.utils.plugin.entity.PluginConf;
import ohos.devtools.datasources.utils.plugin.entity.PluginMode;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.views.layout.chartview.ProfilerMonitorItem;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PlugManager
 *
 * @since 2021/5/20
 */
public class PlugManager {
    private static final Logger LOGGER = LogManager.getLogger(PlugManager.class);
    private static volatile PlugManager singleton;

    private final MultiValueMap profilerConfigMap = new MultiValueMap();
    private final List<PluginConf> confLists = new ArrayList<>();
    private String systemOsName = "";

    private PlugManager() {
    }

    /**
     * getInstance
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

    /**
     * get Plugin Config
     *
     * @param deviceType deviceType
     * @param pluginMode pluginMode
     * @param analysisType analysisType
     * @return List <PluginConf>
     */
    public List<PluginConf> getPluginConfig(DeviceType deviceType, PluginMode pluginMode, AnalysisType analysisType) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getPluginConfig");
        }
        List<PluginConf> collect;
        if (Objects.nonNull(analysisType)) {
            collect = confLists.stream().filter(pluginConf -> pluginConf.getAnalysisTypes().contains(analysisType))
                .collect(Collectors.toList());
        } else {
            collect = confLists;
        }
        if (Objects.isNull(pluginMode)) {
            if (Objects.isNull(deviceType)) {
                return collect;
            }
            return collect.stream().filter(hiProfilerPluginConf -> {
                List<DeviceType> supportDeviceTypes = hiProfilerPluginConf.getSupportDeviceTypes();
                if (supportDeviceTypes.isEmpty()) {
                    return hiProfilerPluginConf.isEnable();
                } else {
                    return supportDeviceTypes.contains(deviceType) && hiProfilerPluginConf.isEnable();
                }
            }).collect(Collectors.toList());
        }
        return collect.stream().filter(hiProfilerPluginConf -> {
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("loadingPlugs");
        }
        if (pluginConfigs != null && pluginConfigs.size() > 0) {
            for (Class<? extends IPluginConfig> pluginConfigPackage : pluginConfigs) {
                IPluginConfig config;
                try {
                    if (pluginConfigPackage != null) {
                        config = pluginConfigPackage.getConstructor().newInstance();
                        config.registerPlugin();
                    }
                } catch (InstantiationException | IllegalAccessException
                    | InvocationTargetException | NoSuchMethodException exception) {
                    if (ProfilerLogManager.isErrorEnabled()) {
                        LOGGER.error("loadingPlugs exception {}", exception.getMessage());
                    }
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("loadingPlug");
        }
        IPluginConfig config;
        try {
            if (pluginConfig != null) {
                config = pluginConfig.getConstructor().newInstance();
                config.registerPlugin();
            }
        } catch (InstantiationException | IllegalAccessException
            | InvocationTargetException | NoSuchMethodException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("loadingPlug exception {}", exception.getMessage());
            }
        }
    }

    /**
     * registerPlugin
     *
     * @param pluginConf pluginConf
     */
    public void registerPlugin(PluginConf pluginConf) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("registerPlugin");
        }
        if (confLists.contains(pluginConf)) {
            return;
        }
        confLists.add(pluginConf);
    }

    /**
     * Add a plug-in that started successfully
     *
     * @param sessionId sessionId
     * @param pluginConf hiProfilerPluginConf
     */
    public void addPluginStartSuccess(long sessionId, PluginConf pluginConf) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addPluginStartSuccess");
        }
        boolean isRegister = profilerConfigMap.containsValue(sessionId, pluginConf);
        if (isRegister) {
            return;
        }
        profilerConfigMap.put(sessionId, pluginConf);
    }

    /**
     * getProfilerMonitorItemMap
     *
     * @param sessionId sessionId
     * @return List <PluginConf>
     */
    public List<PluginConf> getProfilerPlugConfig(long sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getProfilerPlugConfig");
        }
        Collection<PluginConf> collection = profilerConfigMap.getCollection(sessionId);
        if (Objects.nonNull(collection)) {
            return new ArrayList<>(collection);
        }
        return new ArrayList<>();
    }

    /**
     * getProfilerMonitorItemMap
     *
     * @param sessionId sessionId
     * @return List <ProfilerMonitorItem>
     */
    public List<ProfilerMonitorItem> getProfilerMonitorItemList(long sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getProfilerMonitorItemList");
        }
        Collection<PluginConf> collection = profilerConfigMap.getCollection(sessionId);
        if (Objects.nonNull(collection)) {
            List<ProfilerMonitorItem> itemList = collection.stream().filter(
                hiProfilerPluginConf -> hiProfilerPluginConf.isChartPlugin() && Objects
                    .nonNull(hiProfilerPluginConf.getMonitorItem())).map(PluginConf::getMonitorItem)
                .collect(Collectors.toList());
            return itemList.stream().sorted(Comparator.comparingInt(ProfilerMonitorItem::getIndex))
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * getPluginConfigByName
     *
     * @param configFileName configFileName
     * @return Optional<PluginConf
     */
    public Optional<PluginConf> getPluginConfigByName(String configFileName) {
        if (StringUtils.isNotBlank(configFileName)) {
            List<PluginConf> collect =
                confLists.stream().filter(pluginConf -> pluginConf.getPluginFileName().equals(configFileName))
                    .collect(Collectors.toList());
            if (!collect.isEmpty()) {
                return Optional.ofNullable(collect.get(0));
            }
        }
        return Optional.empty();
    }

    /**
     * unzip StdDevelopTools
     *
     * @return boolean
     */
    public boolean unzipStdDevelopTools() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("unzipStdDevelopTools");
        }
        String pluginPath = SessionManager.getInstance().getPluginPath() + "stddeveloptools.tar";
        File stdFile = new File(pluginPath);
        if (stdFile.exists()) {
            return FileUtils.unzipTarFile(stdFile).size() > 0;
        }
        return false;
    }

    /**
     * getSystemOsName
     *
     * @return String
     */
    public String getSystemOsName() {
        if (StringUtils.isBlank(systemOsName)) {
            systemOsName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        }
        return systemOsName;
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
