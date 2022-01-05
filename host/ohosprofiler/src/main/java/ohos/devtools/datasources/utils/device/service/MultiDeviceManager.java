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
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_CHECK_FPORT;
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
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_CHECK_FPORT;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_CHECK_SERVER;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_GET_PLUGIN_MD5S;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_LIST_TARGETS_STR;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_PUSH_FILE_SHELL;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_PUSH_OHOS_SHELL;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_REMOVE_SHELL;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_RUN_OHOS;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_START_PROFILER;
import static ohos.devtools.datasources.transport.hdc.HdcWrapper.conversionCommand;
import static ohos.devtools.datasources.utils.common.Constant.DEVICE_STAT_FAIL;
import static ohos.devtools.datasources.utils.common.Constant.DEVTOOLS_PLUGINS_FULL_PATH;
import static ohos.devtools.datasources.utils.common.Constant.PLUGIN_NOT_FOUND;
import static ohos.devtools.datasources.utils.common.Constant.PLUGIN_RESULT_OK;
import static ohos.devtools.datasources.utils.common.Constant.TIME_OUT;
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

    private static final MultiDeviceManager INSTANCE = new MultiDeviceManager();

    private static boolean logFindDevice = false;

    private final DeviceDao deviceDao = new DeviceDao();

    /**
     * getInstance
     *
     * @return MultiDeviceManager
     */
    public static MultiDeviceManager getInstance() {
        return MultiDeviceManager.INSTANCE;
    }

    private MultiDeviceManager() {
        super();
    }

    /**
     * Start managing devices
     */
    public void start() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("start");
        }
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("startDevicePoller");
        }
        QuartzManager.getInstance().addExecutor(PUSH_DEVICES, new Runnable() {
            @Override
            public void run() {
                devicePool();
            }
        });
        QuartzManager.getInstance().startExecutor(PUSH_DEVICES, QuartzManager.DELAY, QuartzManager.PERIOD);
    }

    /**
     * stop managing devices
     */
    public void shutDown() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("shutDown");
        }
        QuartzManager.getInstance().deleteExecutor(PUSH_DEVICES);
    }

    /**
     * main Methods Of Equipment Management Logic
     */
    private void devicePool() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("devicePool");
        }
        List<DeviceIPPortInfo> connectDevices = getConnectDevices();
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("connectDevices {}", connectDevices);
        }
        List<DeviceIPPortInfo> deviceIPPortInfoList = deviceDao.selectOfflineDevice(connectDevices);
        deviceIPPortInfoList.forEach(this::handleOfflineDevices);
        for (DeviceIPPortInfo deviceIPPortInfo : connectDevices) {
            Optional<DeviceIPPortInfo> hasDeviceIPPort = deviceDao.getDeviceIPPortInfo(deviceIPPortInfo.getDeviceID());
            boolean checkUpdate = false;
            if (hasDeviceIPPort.isPresent()) {
                deviceIPPortInfo = hasDeviceIPPort.get();
                handleForwardNotExits(deviceIPPortInfo);
            } else {
                deviceDao.insertDeviceIPPortInfo(deviceIPPortInfo);
                if (IS_SUPPORT_NEW_HDC && deviceIPPortInfo.getDeviceType() == LEAN_HOS_DEVICE) {
                    ArrayList<String> cmdStr = conversionCommand(HDC_STD_REMOVE_SHELL, deviceIPPortInfo.getDeviceID());
                    HdcWrapper.getInstance().execCmd(cmdStr);
                }
                checkUpdate = true;
            }
            if (IS_SUPPORT_NEW_HDC && deviceIPPortInfo.getDeviceType() == LEAN_HOS_DEVICE) {
                checkUpdate = false;
            }
            String serviceCapability = isServiceCapability(deviceIPPortInfo, checkUpdate, 0);
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
                    if (ProfilerLogManager.isErrorEnabled()) {
                        LOGGER.error("An unknown situation has occurred");
                    }
                    break;
            }
        }
    }

    private void handleForwardNotExits(DeviceIPPortInfo deviceIPPortInfo) {
        int forwardPort = deviceIPPortInfo.getForwardPort();
        ArrayList<String> cmdStr;
        if (forwardPort > 0) {
            if (IS_SUPPORT_NEW_HDC && deviceIPPortInfo.getDeviceType() == LEAN_HOS_DEVICE) {
                cmdStr = conversionCommand(HDC_STD_CHECK_FPORT, deviceIPPortInfo.getDeviceID());
            } else {
                cmdStr = conversionCommand(HDC_CHECK_FPORT, deviceIPPortInfo.getDeviceID());
            }
            String forward = HdcWrapper.getInstance().execCmdBy(cmdStr, 5);
            if (!forward.contains(String.valueOf(forwardPort))) {
                DeviceForwardPort.getInstance().forwardDevicePort(deviceIPPortInfo, forwardPort);
            }
        }
    }

    /**
     * handle RestartDevice
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     */
    private void handleRestartDevice(DeviceIPPortInfo deviceIPPortInfo) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("handleRestartDevice");
        }
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
        HdcWrapper.getInstance().getListResult(cmdStr);
        String serviceCapability = isServiceCapability(deviceIPPortInfo, false, 0);
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("handleOfflineDevices {}", deviceIPPortInfo.getDeviceID());
        }
        if (deviceIPPortInfo.getOfflineCount() >= 3) {
            deviceDao.deleteOfflineDeviceIPPort(deviceIPPortInfo);
            SessionManager.getInstance().deleteSessionByOffLineDevice(deviceIPPortInfo);
        } else {
            deviceDao.updateDeviceIPPortInfo(deviceIPPortInfo.getOfflineCount() + 1, deviceIPPortInfo.getDeviceID());
        }
    }

    /**
     * pushPluginAndRun
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     */
    private void pushPluginAndRun(DeviceIPPortInfo deviceIPPortInfo) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("pushPluginAndRun");
        }
        if (pushDevToolsShell(deviceIPPortInfo)) {
            boolean pushShellResult = pushHiProfilerTools(deviceIPPortInfo);
            if (pushShellResult) {
                pushHiPerfFIle(deviceIPPortInfo);
                pushDevTools(deviceIPPortInfo);
            }
            String cap = isServiceCapability(deviceIPPortInfo, false, 0);
            if (PLUGIN_RESULT_OK.equals(cap)) {
                deviceDao.updateDeviceIPPortInfo(DeviceStatus.OK.getStatus(), 0, deviceIPPortInfo.getDeviceID());
                logFindDevice(deviceIPPortInfo, false);
            }
        } else {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("Device: {} push hiprofiler_cli failed", deviceIPPortInfo.getDeviceID());
            }
        }
    }

    /**
     * pushHiprofilerTools
     *
     * @param info info
     * @return boolean
     */
    public boolean pushHiProfilerTools(DeviceIPPortInfo info) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("pushHiProfilerTools");
        }
        ArrayList<String> cmdStr;
        if (IS_SUPPORT_NEW_HDC && info.getDeviceType() == LEAN_HOS_DEVICE) {
            return true;
        } else {
            String devToolsPath = SessionManager.getInstance().getPluginPath() + DEVTOOLS_PLUGINS_FULL_PATH;
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
     * pushHiPerfFIle
     *
     * @param info info
     */
    public void pushHiPerfFIle(DeviceIPPortInfo info) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("pushHiperfFIle");
        }
        if (IS_SUPPORT_NEW_HDC && info.getDeviceType() == DeviceType.LEAN_HOS_DEVICE) {
            String hiPerfPath = SessionManager.getInstance().getPluginPath() + "hiperf" + File.separator + "hiperf";
            ArrayList<String> cmdStr =
                conversionCommand(HDC_STD_PUSH_FILE_SHELL, info.getDeviceID(), hiPerfPath, "/data/local/tmp/");
            HdcWrapper.getInstance().execCmdBy(cmdStr);
        }
    }

    /**
     * pushDevTools
     *
     * @param info info
     */
    public void pushDevTools(DeviceIPPortInfo info) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("pushDevTools");
        }
        List<PluginConf> pluginConfig = PlugManager.getInstance().getPluginConfig(info.getDeviceType(), null, null);
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
     * pushtrace
     *
     * @param info info
     */
    public void pushtrace(DeviceIPPortInfo info) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("pushtrace");
        }
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("pushDevToolsShell");
        }
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getConnectDevices");
        }
        List<DeviceIPPortInfo> deviceIPPortInfoList = getHdcDevices(0);
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deviceIPPortInfoList {} ", deviceIPPortInfoList);
        }
        if (IS_SUPPORT_NEW_HDC) {
            List<DeviceIPPortInfo> stdDeviceIPPortInfoList = getHdcStdDevices(0);
            deviceIPPortInfoList.addAll(stdDeviceIPPortInfoList);
        }
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deviceIPPortInfoList {} ", deviceIPPortInfoList);
        }
        return deviceIPPortInfoList;
    }

    private List<DeviceIPPortInfo> getHdcStdDevices(int tryCount) {
        List<DeviceIPPortInfo> deviceIPPortInfoList = new ArrayList<>();
        ArrayList<ArrayList<String>> deviceList =
            HdcWrapper.getInstance().getListHdcStdResult(HDC_STD_LIST_TARGETS_STR);
        for (List<String> deviceInfo : deviceList) {
            if (deviceInfo.contains("TimeOut")) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException interruptedException) {
                    interruptedException.getMessage();
                }
                if (tryCount >= 3) {
                    return deviceIPPortInfoList;
                }
                return getHdcStdDevices(tryCount + 1);
            }
            if (deviceInfo.contains("Connected")) {
                deviceIPPortInfoList.add(buildHdcStdDeviceInfo(deviceInfo));
            }
        }
        return deviceIPPortInfoList;
    }

    private List<DeviceIPPortInfo> getHdcDevices(int tryCount) {
        List<DeviceIPPortInfo> deviceIPPortInfoList = new ArrayList<>();
        ArrayList<ArrayList<String>> devices = HdcWrapper.getInstance().getListResult(HDC_LIST_TARGETS_STR);
        for (List<String> deviceInfo : devices) {
            if (deviceInfo.contains(TIME_OUT)) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException interruptedException) {
                    interruptedException.getMessage();
                }
                if (tryCount >= 3) {
                    return deviceIPPortInfoList;
                }
                return getHdcDevices(tryCount + 1);
            }
            if (deviceInfo.contains("device")) {
                ArrayList<String> getProtoCmd = conversionCommand(HDC_GET_TYPE, deviceInfo.get(0));
                String result = HdcWrapper.getInstance().getHdcStringResult(getProtoCmd, 5);
                if (TIME_OUT.equals(result)) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.getMessage();
                    }
                    result = HdcWrapper.getInstance().getHdcStringResult(getProtoCmd, 5);
                }
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
        return deviceIPPortInfoList;
    }

    /**
     * run shell to check whether the service is available
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param checkUpdate checkUpdate
     * @param tryCount tryCount
     * @return String
     */
    private String isServiceCapability(DeviceIPPortInfo deviceIPPortInfo, boolean checkUpdate, int tryCount) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("isServiceCapability");
        }
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
        } else if (list.contains(TIME_OUT)) {
            if (tryCount >= 3) {
                return TIME_OUT;
            }
            return isServiceCapability(deviceIPPortInfo, checkUpdate, tryCount + 1);
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("updateVersion");
        }
        String devToolsPath = SessionManager.getInstance().getPluginPath() + DEVTOOLS_PLUGINS_FULL_PATH;
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
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("get plugin MD5 sum Failed {}", ioException.getMessage());
                }
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("compareWithMap");
        }
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("buildDeviceInfo");
        }
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
        info.setConnectType("USB");
        return info;
    }

    /**
     * buildDeviceInfo
     *
     * @param deviceInfo deviceInfo
     * @return DeviceIPPortInfo
     */
    private DeviceIPPortInfo buildHdcStdDeviceInfo(List<String> deviceInfo) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("buildHdcStdDeviceInfo");
        }
        DeviceIPPortInfo info = new DeviceIPPortInfo();
        if (deviceInfo.contains("USB")) {
            info.setConnectType("USB");
        } else {
            info.setConnectType("WLAN");
        }
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
     * @return List <DeviceIPPortInfo>
     */
    public List<DeviceIPPortInfo> getOnlineDeviceInfoList() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getOnlineDeviceInfoList");
        }
        return deviceDao.getOnlineDeviceInfoList();
    }

    /**
     * getHiLogDeviceInfoList
     *
     * @return List <DeviceIPPortInfo>
     */
    public List<DeviceIPPortInfo> getDeviceInfoList() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getHiLogDeviceInfoList");
        }
        return getConnectDevices();
    }

    private void logFindDevice(DeviceIPPortInfo deviceIPPortInfo, boolean find) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("logFindDevice");
        }
        if (logFindDevice) {
            if (find) {
                if (ProfilerLogManager.isInfoEnabled()) {
                    LOGGER.info("find device {}, time is {}", deviceIPPortInfo.getDeviceID(),
                        DateTimeUtil.getNowTimeLong());
                }
            } else {
                if (ProfilerLogManager.isInfoEnabled()) {
                    LOGGER.info("Device is OK {}, Time is {}", deviceIPPortInfo.getDeviceID(),
                        DateTimeUtil.getNowTimeLong());
                }
                logFindDevice = false;
            }
        }
    }
}
