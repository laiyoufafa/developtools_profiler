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

package ohos.devtools.datasources.utils.device.service;

import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.datasources.utils.common.Constant;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.device.dao.DeviceDao;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceStatus;
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.datasources.utils.plugin.entity.PluginConf;
import ohos.devtools.datasources.utils.plugin.service.PlugManager;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_CHECK_SERVER;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_CLEAR_CMD;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_GET_PLUGIN_MD5S;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_GET_TYPE;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_LIST_TARGETS_STR;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_PUSH_CMD;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_PUSH_FILE_SHELL;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_PUSH_OHOS_SHELL;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_ROOT_CLEAR_CMD;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_RUN_OHOS;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_START_PROFILERD;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_CHECK_SERVER;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_GET_PLUGIN_MD5S;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_LIST_TARGETS_STR;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_PUSH_CMD;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_PUSH_FILE_SHELL;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_PUSH_OHOS_SHELL;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_ROOT_CLEAR_CMD;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_RUN_OHOS;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_START_PROFILER;
import static ohos.devtools.datasources.transport.hdc.HdcWrapper.conversionCommand;
import static ohos.devtools.datasources.utils.common.Constant.DEVICE_STAT_FAIL;
import static ohos.devtools.datasources.utils.common.Constant.DEVTOOLS_PLUGINS_V7_PATH;
import static ohos.devtools.datasources.utils.common.Constant.DEVTOOLS_PLUGINS_V8_PATH;
import static ohos.devtools.datasources.utils.common.Constant.PLUGIN_NOT_FOUND;
import static ohos.devtools.datasources.utils.common.Constant.PLUGIN_RESULT_OK;
import static ohos.devtools.datasources.utils.common.Constant.UNZIP_SHELL_PLUGINS_PATH;
import static ohos.devtools.datasources.utils.common.Constant.UPDATE_PLUGIN;
import static ohos.devtools.datasources.utils.device.entity.DeviceType.FULL_HOS_DEVICE;
import static ohos.devtools.datasources.utils.device.entity.DeviceType.LEAN_HOS_DEVICE;
import static ohos.devtools.views.common.Constant.IS_SUPPORT_NEW_HDC;

/**
 * DevicesManager
 */
public class MultiDeviceManager {
    private static final Logger LOGGER = LogManager.getLogger(MultiDeviceManager.class);
    private static final int MAX_RETRY_COUNT = 3;
    private static final String PUSH_DEVICES = "StarPUsh";
    private static boolean logFindDevice = true;
    private final DeviceDao deviceDao = new DeviceDao();

    private static class SingletonClassInstance {
        private static final MultiDeviceManager INSTANCE = new MultiDeviceManager();
    }

    /**
     * getInstance
     *
     * @return MultiDeviceManager
     */
    public static MultiDeviceManager getInstance() {
        return MultiDeviceManager.SingletonClassInstance.INSTANCE;
    }

    private MultiDeviceManager() {
    }

    /**
     * Start managing devices
     */
    public void start() {
        Optional<ScheduledExecutorService> scheduledExecutorService =
            QuartzManager.getInstance().checkService(PUSH_DEVICES);
        if (scheduledExecutorService.isPresent()) {
            boolean shutdown = scheduledExecutorService.get().isShutdown();
            if (shutdown) {
                QuartzManager.getInstance().deleteExecutor(PUSH_DEVICES);
                startDevicePoller();
            }
        } else {
            startDevicePoller();
        }
    }

    private void startDevicePoller() {
        QuartzManager.getInstance().addExecutor(PUSH_DEVICES, this::devicePool);
        QuartzManager.getInstance().startExecutor(PUSH_DEVICES, QuartzManager.DELAY, QuartzManager.PERIOD);
    }

    /**
     * stop managing devices
     */
    public void shutDown() {
        QuartzManager.getInstance().endExecutor(PUSH_DEVICES);
    }

    /**
     * main Methods Of Equipment Management Logic
     */
    private void devicePool() {
        List<DeviceIPPortInfo> connectDevices = getConnectDevices();
        List<DeviceIPPortInfo> deviceIPPortInfoList = deviceDao.selectOfflineDevice(connectDevices);
        deviceIPPortInfoList.forEach(this::handleOfflineDevices);
        for (DeviceIPPortInfo deviceIPPortInfo : connectDevices) {
            Optional<DeviceIPPortInfo> hasDeviceIPPort = deviceDao.getDeviceIPPortInfo(deviceIPPortInfo.getDeviceID());
            boolean checkUpdate = false;
            if (hasDeviceIPPort.isPresent()) {
                deviceIPPortInfo = hasDeviceIPPort.get();
            } else {
                deviceDao.insertDeviceIPPortInfo(deviceIPPortInfo);
                checkUpdate = true;
            }
            if (IS_SUPPORT_NEW_HDC && deviceIPPortInfo.getDeviceType() == LEAN_HOS_DEVICE) {
                checkUpdate = false;
            }
            String serviceCapability = isServiceCapability(deviceIPPortInfo, checkUpdate);
            switch (serviceCapability) {
                case PLUGIN_RESULT_OK:
                    break;
                case UPDATE_PLUGIN:
                case PLUGIN_NOT_FOUND:
                    pushPluginAndRun(deviceIPPortInfo);
                    break;
                case DEVICE_STAT_FAIL:
                    handleRestartDevice(deviceIPPortInfo);
                    break;
                default:
                    LOGGER.error("An unknown situation has occurred");
                    break;
            }
        }
    }

    /**
     * handle RestartDevice
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     */
    private void handleRestartDevice(DeviceIPPortInfo deviceIPPortInfo) {
        if (deviceIPPortInfo.getRetryNum() >= MAX_RETRY_COUNT) {
            return;
        }
        String deviceId = deviceIPPortInfo.getDeviceID();
        ArrayList<String> cmdStr;
        if (IS_SUPPORT_NEW_HDC && deviceIPPortInfo.getDeviceType() == LEAN_HOS_DEVICE) {
            cmdStr = conversionCommand(HDC_STD_START_PROFILER, deviceId);
        } else {
            cmdStr = conversionCommand(HDC_START_PROFILERD, deviceId);
        }
        HdcWrapper.getInstance().execCmdBy(cmdStr);
        String serviceCapability = isServiceCapability(deviceIPPortInfo, false);
        if (PLUGIN_RESULT_OK.equals(serviceCapability)) {
            deviceDao.updateDeviceIPPortInfo(DeviceStatus.OK.getStatus(), 0, deviceIPPortInfo.getDeviceID());
        } else {
            deviceDao.updateDeviceIPPortInfo(DeviceStatus.FAILED.getStatus(), deviceIPPortInfo.getRetryNum() + 1,
                deviceIPPortInfo.getDeviceID());
        }
    }

    /**
     * handleOfflineDevices
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     */
    private void handleOfflineDevices(DeviceIPPortInfo deviceIPPortInfo) {
        LOGGER.info("handle offline Device {}", deviceIPPortInfo.getDeviceID());
        deviceDao.deleteOfflineDeviceIPPort(deviceIPPortInfo);
        SessionManager.getInstance().deleteSessionByOffLineDevice(deviceIPPortInfo);
    }

    /**
     * pushPluginAndRun
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     */
    private void pushPluginAndRun(DeviceIPPortInfo deviceIPPortInfo) {
        if (pushHiProfilerTools(deviceIPPortInfo)) {
            boolean pushShellResult = pushDevToolsShell(deviceIPPortInfo);
            if (pushShellResult) {
                pushDevTools(deviceIPPortInfo);
            }
            String cap = isServiceCapability(deviceIPPortInfo, false);
            if (PLUGIN_RESULT_OK.equals(cap)) {
                deviceDao.updateDeviceIPPortInfo(DeviceStatus.OK.getStatus(), 0, deviceIPPortInfo.getDeviceID());
                logFindDevice(deviceIPPortInfo, false);
            }
        } else {
            LOGGER.debug("Device: {} push hiprofiler_cli failed", deviceIPPortInfo.getDeviceID());
        }
    }

    /**
     * push Hi profiler Tools
     *
     * @param info info
     * @return boolean
     */
    public boolean pushHiProfilerTools(DeviceIPPortInfo info) {
        ArrayList<String> cmdStr;
        if (IS_SUPPORT_NEW_HDC && info.getDeviceType() == LEAN_HOS_DEVICE) {
            String devToolsPath = SessionManager.getInstance().getPluginPath() + DEVTOOLS_PLUGINS_V7_PATH;
            HdcWrapper.getInstance().getHdcStringResult(conversionCommand(HDC_STD_ROOT_CLEAR_CMD, info.getDeviceID()));
            cmdStr = conversionCommand(HDC_STD_PUSH_CMD, info.getDeviceID(), devToolsPath);
            String result = HdcWrapper.getInstance().getHdcStringResult(cmdStr);
            return result.contains("FileTransfer finish");
        } else {
            String devToolsPath = SessionManager.getInstance().getPluginPath() + DEVTOOLS_PLUGINS_V8_PATH;
            if (info.getDeviceType() == LEAN_HOS_DEVICE) {
                HdcWrapper.getInstance().getHdcStringResult(conversionCommand(HDC_ROOT_CLEAR_CMD, info.getDeviceID()));
            } else {
                HdcWrapper.getInstance().getHdcStringResult(conversionCommand(HDC_CLEAR_CMD, info.getDeviceID()));
            }
            cmdStr = conversionCommand(HDC_PUSH_CMD, info.getDeviceID(), devToolsPath);
            String result = HdcWrapper.getInstance().getHdcStringResult(cmdStr);
            return result.contains(Constant.DEVICE_SATA_STAT_PUSHED);
        }
    }

    /**
     * push Dev Tools
     *
     * @param info info
     */
    public void pushDevTools(DeviceIPPortInfo info) {
        List<PluginConf> pluginConfig = PlugManager.getInstance().getPluginConfig(info.getDeviceType(), null);
        String plugFiles = pluginConfig.stream().map(pluginConf -> {
            String pluginFileName = pluginConf.getPluginFileName();
            return pluginFileName.substring(pluginFileName.lastIndexOf("/") + 1);
        }).collect(Collectors.joining(","));
        ArrayList<String> cmdStr;
        if (IS_SUPPORT_NEW_HDC && info.getDeviceType() == DeviceType.LEAN_HOS_DEVICE) {
            cmdStr = conversionCommand(HDC_STD_RUN_OHOS, info.getDeviceID(), plugFiles);
        } else {
            cmdStr = conversionCommand(HDC_RUN_OHOS, info.getDeviceID(), plugFiles);
        }
        HdcWrapper.getInstance().execCmdBy(cmdStr);
    }

    /**
     * push trace
     *
     * @param info info
     */
    public void pushTrace(DeviceIPPortInfo info) {
        String pluginPath = SessionManager.getInstance().getPluginPath() + "fbs_dev_1.trace";
        String pluginPath2 = SessionManager.getInstance().getPluginPath() + "fbs_dev_2.trace";
        ArrayList<String> cmdStr;
        ArrayList<String> cmdStr2;
        if (IS_SUPPORT_NEW_HDC && info.getDeviceType() == LEAN_HOS_DEVICE) {
            cmdStr = conversionCommand(HDC_STD_PUSH_FILE_SHELL, info.getDeviceID(), pluginPath,
                "/data/local/tmp/fbs_dev_1.trace");
            cmdStr2 = conversionCommand(HDC_STD_PUSH_FILE_SHELL, info.getDeviceID(), pluginPath2,
                "/data/local/tmp/fbs_dev_2.trace");
        } else {
            cmdStr = conversionCommand(HDC_PUSH_FILE_SHELL, info.getDeviceID(), pluginPath,
                "/data/local/tmp/fbs_dev_1.trace");
            cmdStr2 = conversionCommand(HDC_PUSH_FILE_SHELL, info.getDeviceID(), pluginPath2,
                "/data/local/tmp/fbs_dev_2.trace");
        }
        HdcWrapper.getInstance().execCmdBy(cmdStr);
        HdcWrapper.getInstance().execCmdBy(cmdStr2);
    }

    /**
     * push DevTools Shell
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @return boolean
     */
    public boolean pushDevToolsShell(DeviceIPPortInfo deviceIPPortInfo) {
        String pluginPath = SessionManager.getInstance().getPluginPath() + UNZIP_SHELL_PLUGINS_PATH;
        ArrayList<String> cmdStr;
        String deviceID = deviceIPPortInfo.getDeviceID();
        if (IS_SUPPORT_NEW_HDC && deviceIPPortInfo.getDeviceType() == LEAN_HOS_DEVICE) {
            cmdStr = conversionCommand(HDC_STD_PUSH_OHOS_SHELL, deviceID, pluginPath);
            String result = HdcWrapper.getInstance().getHdcStringResult(cmdStr);
            return result.contains("FileTransfer finish");
        } else {
            cmdStr = conversionCommand(HDC_PUSH_OHOS_SHELL, deviceID, pluginPath);
            String result = HdcWrapper.getInstance().getHdcStringResult(cmdStr);
            return result.contains(Constant.DEVICE_SATA_STAT_PUSHED);
        }
    }

    /**
     * get Connect Devices
     *
     * @return List <DeviceIPPortInfo>
     */
    private List<DeviceIPPortInfo> getConnectDevices() {
        List<DeviceIPPortInfo> deviceIPPortInfoList = new ArrayList<>();
        ArrayList<ArrayList<String>> devices = HdcWrapper.getInstance().getListResult(HDC_LIST_TARGETS_STR);
        for (List<String> deviceInfo : devices) {
            if (!deviceInfo.contains(Constant.DEVICE_STAT_OFFLINE)) {
                String deviceId = deviceInfo.get(0);
                ArrayList<String> getProtoCmd = conversionCommand(HDC_GET_TYPE, deviceId);
                String result = HdcWrapper.getInstance().getHdcStringResult(getProtoCmd);
                DeviceIPPortInfo info;
                if (result.contains(FULL_HOS_DEVICE.getCpuAbi())) {
                    info = buildDeviceInfo(deviceInfo, FULL_HOS_DEVICE);
                } else {
                    info = buildDeviceInfo(deviceInfo, LEAN_HOS_DEVICE);
                }
                deviceIPPortInfoList.add(info);
                logFindDevice(info, true);
            }
        }
        if (IS_SUPPORT_NEW_HDC) {
            ArrayList<ArrayList<String>> deviceList =
                HdcWrapper.getInstance().getListHdcStdResult(HDC_STD_LIST_TARGETS_STR);
            for (List<String> deviceInfo : deviceList) {
                if (deviceInfo.contains("Connected")) {
                    DeviceIPPortInfo info = buildHdcStdDeviceInfo(deviceInfo);
                    deviceIPPortInfoList.add(info);
                }
            }
        }
        return deviceIPPortInfoList;
    }

    /**
     * run shell to check whether the service is available
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param checkUpdate checkUpdate
     * @return String
     */
    private String isServiceCapability(DeviceIPPortInfo deviceIPPortInfo, boolean checkUpdate) {
        String serialNumber = deviceIPPortInfo.getDeviceID();
        ArrayList<String> cmdStr;
        if (IS_SUPPORT_NEW_HDC && deviceIPPortInfo.getDeviceType() == LEAN_HOS_DEVICE) {
            cmdStr = conversionCommand(HDC_STD_CHECK_SERVER, serialNumber);
        } else {
            cmdStr = conversionCommand(HDC_CHECK_SERVER, serialNumber);
        }
        ArrayList<ArrayList<String>> listCliResult = HdcWrapper.getInstance().getCliResult(cmdStr);
        if (listCliResult.isEmpty()) {
            return PLUGIN_NOT_FOUND;
        }
        ArrayList<String> list = listCliResult.get(0);
        if (list.contains(PLUGIN_RESULT_OK)) {
            if (deviceIPPortInfo.getForwardPort() <= 0) {
                if (checkUpdate) {
                    boolean updateVersion = updateVersion(deviceIPPortInfo);
                    if (updateVersion) {
                        return UPDATE_PLUGIN;
                    }
                }
                String ip;
                int port;
                if (IS_SUPPORT_NEW_HDC && deviceIPPortInfo.getDeviceType() == LEAN_HOS_DEVICE) {
                    ip = "127.0.0.1";
                    port = 50051;
                } else {
                    int first = 1;
                    int second = 2;
                    ip = listCliResult.get(first).get(first);
                    port = Integer.parseInt(listCliResult.get(second).get(first));
                }
                int forwardDevicePort = DeviceForwardPort.getInstance().forwardDevicePort(deviceIPPortInfo);
                deviceDao.updateDeviceInfo(ip, port, forwardDevicePort, deviceIPPortInfo.getDeviceID());
                deviceDao.updateDeviceIPPortInfo(DeviceStatus.OK.getStatus(), 0, deviceIPPortInfo.getDeviceID());
            }
            return PLUGIN_RESULT_OK;
        } else if (list.contains(DEVICE_STAT_FAIL)) {
            return DEVICE_STAT_FAIL;
        } else {
            return PLUGIN_NOT_FOUND;
        }
    }

    /**
     * updateVersion
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @return boolean
     */
    private boolean updateVersion(DeviceIPPortInfo deviceIPPortInfo) {
        String devToolsPath = SessionManager.getInstance().getPluginPath() + DEVTOOLS_PLUGINS_V8_PATH;
        File devtoolsPath = new File(devToolsPath);
        Map<String, String> cmdResultMap;
        if (IS_SUPPORT_NEW_HDC && deviceIPPortInfo.getDeviceType() == LEAN_HOS_DEVICE) {
            cmdResultMap = HdcWrapper.getInstance()
                .getCmdResultMap(conversionCommand(HDC_STD_GET_PLUGIN_MD5S, deviceIPPortInfo.getDeviceID()));
        } else {
            cmdResultMap = HdcWrapper.getInstance()
                .getCmdResultMap(conversionCommand(HDC_GET_PLUGIN_MD5S, deviceIPPortInfo.getDeviceID()));
        }
        Map<String, String> resultMap = new HashMap<>();
        File[] pluginList = devtoolsPath.listFiles();
        for (File plugin : pluginList) {
            try {
                String pluginMd5 = DigestUtils.md5Hex(new FileInputStream(plugin));
                resultMap.put(plugin.getName(), pluginMd5);
            } catch (IOException ioException) {
                LOGGER.info("get plugin MD5 sum Failed {}", ioException.getMessage());
                return true;
            }
        }
        return !compareWithMap(cmdResultMap, resultMap);
    }

    /**
     * Does parentMap contain childMap
     *
     * @param parentMap parentMap
     * @param childMap childMap
     * @return boolean
     */
    private boolean compareWithMap(Map<String, String> parentMap, Map<String, String> childMap) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : parentMap.entrySet()) {
            builder.append(entry.getKey()).append("_").append(entry.getValue());
        }
        int count = 0;
        for (Map.Entry<String, String> entry : childMap.entrySet()) {
            String map1KeyVal = entry.getKey() + "_" + entry.getValue();
            boolean contains = builder.toString().contains(map1KeyVal);
            if (contains) {
                count++;
            }
        }
        return childMap.size() == count;
    }

    /**
     * buildDeviceInfo
     *
     * @param deviceInfo deviceInfo
     * @param deviceType deviceType
     * @return DeviceIPPortInfo
     */
    private DeviceIPPortInfo buildDeviceInfo(List<String> deviceInfo, DeviceType deviceType) {
        DeviceIPPortInfo info = new DeviceIPPortInfo();
        info.setDeviceID(deviceInfo.get(0));
        info.setDeviceType(deviceType);
        String deviceName = "";
        for (String str : deviceInfo) {
            deviceName = buildDeviceName(deviceName, str);
        }
        info.setDeviceName(deviceName);
        info.setDeviceStatus(DeviceStatus.INIT.getStatus());
        info.setRetryNum(0);
        return info;
    }

    /**
     * buildDeviceInfo
     *
     * @param deviceInfo deviceInfo
     * @return DeviceIPPortInfo
     */
    private DeviceIPPortInfo buildHdcStdDeviceInfo(List<String> deviceInfo) {
        DeviceIPPortInfo info = new DeviceIPPortInfo();
        String deviceId = deviceInfo.get(0);
        info.setDeviceID(deviceId);
        info.setDeviceType(LEAN_HOS_DEVICE);
        info.setDeviceName(deviceId);
        info.setDeviceStatus(DeviceStatus.INIT.getStatus());
        info.setRetryNum(0);
        return info;
    }

    /**
     * buildDeviceName
     *
     * @param deviceName deviceName
     * @param str str
     * @return String
     */
    private String buildDeviceName(String deviceName, String str) {
        String devName = deviceName;
        if (str.contains("product:")) {
            String[] split = str.split(":");
            devName = devName + "-" + split[1];
        }
        if (str.contains("model:")) {
            String[] split = str.split(":");
            devName = split[1] + devName;
        }
        if (str.contains("transport_id:")) {
            String[] split = str.split(":");
            devName = devName + split[1];
        }
        return devName;
    }

    /**
     * getAllDeviceIPPortInfos
     *
     * @return List <DeviceIPPortInfo><DeviceIPPortInfo>
     */
    public List<DeviceIPPortInfo> getOnlineDeviceInfoList() {
        return deviceDao.getOnlineDeviceInfoList();
    }

    /**
     * getHiLogDeviceInfoList
     *
     * @return List <DeviceIPPortInfo>
     */
    public List<DeviceIPPortInfo> getHiLogDeviceInfoList() {
        return getConnectDevices();
    }

    private void logFindDevice(DeviceIPPortInfo deviceIPPortInfo, boolean find) {
        if (logFindDevice) {
            if (find) {
                LOGGER
                    .debug("find device {}, time is {}", deviceIPPortInfo.getDeviceID(), DateTimeUtil.getNowTimeLong());
            } else {
                LOGGER.debug("Device is OK {}, Time is {}", deviceIPPortInfo.getDeviceID(),
                    DateTimeUtil.getNowTimeLong());
                logFindDevice = false;
            }
        }
    }
}
