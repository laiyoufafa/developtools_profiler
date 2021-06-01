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

import ohos.devtools.datasources.transport.hdc.HdcCommandEnum;
import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.datasources.utils.common.Constant;
import ohos.devtools.datasources.utils.device.dao.DeviceUtil;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static ohos.devtools.datasources.transport.hdc.HdcCommandEnum.HDC_CHECK_SERVER;
import static ohos.devtools.datasources.transport.hdc.HdcCommandEnum.HDC_GET_TYPE;
import static ohos.devtools.datasources.utils.common.Constant.DEVICE_FULL_TYPE;
import static ohos.devtools.datasources.utils.common.Constant.DEVICE_LEAN_TYPE;
import static ohos.devtools.datasources.utils.common.Constant.DEVTOOLS_PLUGINS_V7_PATH;
import static ohos.devtools.datasources.utils.common.Constant.DEVTOOLS_PLUGINS_V8_PATH;
import static ohos.devtools.datasources.utils.common.Constant.UNZIP_SHELL_PLUGINS_PATH;

/**
 * Profiler的监控项
 *
 * @since 2021/3/4 10:55
 */
public final class MultiDeviceManager implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(MultiDeviceManager.class);

    private static volatile MultiDeviceManager instance;

    private final HdcWrapper hdcHelper = HdcWrapper.getInstance();

    private final DeviceUtil sqlUtil = new DeviceUtil();

    private ArrayList<DeviceIPPortInfo> serialNumberList = new ArrayList<>();

    private ArrayList<ArrayList<String>> listCliResult;

    private final ThreadPoolExecutor executor =
        new ThreadPoolExecutor(1, 10, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    /**
     * getInstance
     *
     * @return MultiDeviceManager
     */
    public static MultiDeviceManager getInstance() {
        if (instance == null) {
            synchronized (MultiDeviceManager.class) {
                if (instance == null) {
                    instance = new MultiDeviceManager();
                }
            }
        }
        return instance;
    }

    private MultiDeviceManager() {
    }

    /**
     * run
     */
    @Override
    public synchronized void run() {
        init();
    }

    private void init() {
        ArrayList<ArrayList<String>> devices = hdcDevices();
        if (devices != null && devices.size() != 0) {
            LOGGER.debug("There are currently {} devices connected to the system", devices.size());
            initSerialNumberList(devices);
            // 根据serialNumber做相关业务
            doService();
        } else {
            LOGGER.debug("No device is currently connected to the system {}", devices);
            if (serialNumberList != null && serialNumberList.size() != 0) {
                for (DeviceIPPortInfo offinleDevice : serialNumberList) {
                    SessionManager.getInstance().deleteSessionByOffLineDivece(offinleDevice);
                }
                serialNumberList.clear();
            }
            sqlUtil.deleteAllDeviceIPPortInfo();
        }
    }

    private String isServiceCapability(DeviceIPPortInfo deviceIPPortInfo) {
        String serialNumber = deviceIPPortInfo.getDeviceID();
        String cmdStr = String.format(Locale.ENGLISH, HDC_CHECK_SERVER.getHdcCommand(), serialNumber);
        LOGGER.debug("hiprofilerCliGetport cmdStr = {}", cmdStr);
        listCliResult = hdcHelper.getCliResult(cmdStr);
        LOGGER.debug("hiprofilerCliGetport getCliResult = {}", listCliResult);
        if (listCliResult.isEmpty()) {
            return Constant.DEVICE_STAT_NOT_FOUND;
        }
        ArrayList<String> list = listCliResult.get(0);
        if (list.contains(Constant.HIPRO_FILER_RESULT_OK)) {
            return Constant.HIPRO_FILER_RESULT_OK;
        } else if (list.contains(Constant.DEVICE_STAT_FAIL)) {
            return Constant.DEVICE_STAT_FAIL;
        } else {
            return Constant.DEVICE_STAT_NOT_FOUND;
        }
    }

    /**
     * 获取设备IP和端口号
     *
     * @param serialNumber serialNumber
     * @return ArrayList<ArrayList < String>>
     */
    public ArrayList<ArrayList<String>> getDeviceIPPort(String serialNumber) {
        String cmdStr =
            HdcCommandEnum.HDC_STR.getHdcCommand() + " " + " -t " + serialNumber + " " + HdcCommandEnum.HDC_SHELL_STR
                .getHdcCommand() + " " + " \"chmod +x /data/local/tmp/hiprofiler_cmd &&" + " " + Constant.DEST_PATH
                + "/" + Constant.HIPRO_FILER_CMDNAME;

        LOGGER.debug("hiprofilerCliGetport cmdStr = {}", cmdStr);
        ArrayList<ArrayList<String>> cliResult = hdcHelper.getCliResult(cmdStr);
        LOGGER.debug("hiprofilerCliGetport getCliResult = {}", cliResult);
        return cliResult;
    }

    private ArrayList<ArrayList<String>> hdcDevices() {
        String hdcStr = HdcCommandEnum.HDC_LIST_TARGETS_STR.getHdcCommand();
        LOGGER.debug("init hdcStr = {}", hdcStr);
        ArrayList<ArrayList<String>> device = hdcHelper.getListResult(hdcStr);
        LOGGER.debug("init devices = {}", device);
        return device;
    }

    private void initSerialNumberList(ArrayList<ArrayList<String>> devices) {
        ArrayList<DeviceIPPortInfo> serialNumbers = new ArrayList<>();
        for (ArrayList<String> deviceInfo : devices) {
            if (deviceInfo.contains(Constant.DEVICE_STAT_ONLINE)) {
                DeviceIPPortInfo info = new DeviceIPPortInfo();
                String deviceId = deviceInfo.get(0);
                info.setDeviceID(deviceInfo.get(0));
                String getProtocmd = String.format(Locale.ENGLISH, HDC_GET_TYPE.getHdcCommand(), deviceId);
                String result = hdcHelper.getHdcStringResult(getProtocmd);
                if (result.contains(DEVICE_FULL_TYPE)) {
                    info.setDeviceType(DEVICE_FULL_TYPE);
                } else {
                    info.setDeviceType(DEVICE_LEAN_TYPE);
                }
                String deviceName = "";
                for (String str : deviceInfo) {
                    deviceName = getString(deviceName, str);
                }
                info.setDeviceName(deviceName);
                serialNumbers.add(info);
            }
        }
        ArrayList<DeviceIPPortInfo> offlineDevice = new ArrayList<>();
        for (DeviceIPPortInfo preInfo : serialNumberList) {
            String deviceID = preInfo.getDeviceID();
            boolean flag = false;
            for (DeviceIPPortInfo info : serialNumbers) {
                if (deviceID.equals(info.getDeviceID())) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                offlineDevice.add(preInfo);
            }
        }
        if (offlineDevice != null && offlineDevice.size() != 0) {
            LOGGER.debug("offlineDeviceList = {}", offlineDevice);
            for (DeviceIPPortInfo device : offlineDevice) {
                SessionManager.getInstance().deleteSessionByOffLineDivece(device);
            }
        }
        serialNumberList.clear();
        serialNumberList = serialNumbers;
        if (serialNumberList != null && serialNumberList.size() != 0) {
            sqlUtil.deleteExceptDeviceIPPort(serialNumberList);
        } else {
            sqlUtil.deleteAllDeviceIPPortInfo();
        }
    }

    private String getString(String deviceName, String str) {
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

    private void doService() {
        for (DeviceIPPortInfo info : serialNumberList) {
            String serialNumber = info.getDeviceID();
            String capability = isServiceCapability(info);
            if (Constant.HIPRO_FILER_RESULT_OK.equals(capability)) {
                // ok 表示具备服务能力
                if (!sqlUtil.hasDeviceIPPort(serialNumber)) {
                    insertDeviceIPPort(info);
                } else {
                    LOGGER.debug("The data already exists in the data table: {}", serialNumber);
                }
            } else if (Constant.DEVICE_STAT_NOT_FOUND.equals(capability)) {
                // not found 表示还没有端侧的程序
                if (pushHiprofilerTools(info)) {
                    // 推送成功后,调用脚本解压，并且拉起程序
                    boolean pushShellResult = pushDevToolsShell(serialNumber);
                    if (pushShellResult) {
                        uzipDevTools(info);
                    }
                    String cap = isServiceCapability(info);
                    if (Constant.HIPRO_FILER_RESULT_OK.equals(cap)) {
                        if (!sqlUtil.hasDeviceIPPort(serialNumber)) {
                            insertDeviceIPPort(info);
                        } else {
                            LOGGER.debug("The data already exists in the data table: {}", serialNumber);
                        }
                    }
                } else {
                    // 推送失败
                    LOGGER.debug("Device: {} push hiprofiler_cli failed", serialNumber);
                }
            } else {
                String cmdStr = "";
                if (DEVICE_FULL_TYPE.equals(info.getDeviceType())) {
                    cmdStr = String
                        .format(Locale.ENGLISH, HdcCommandEnum.HDC_START_PROFILERD.getHdcCommand(), info.getDeviceID());
                } else if (DEVICE_LEAN_TYPE.equals(info.getDeviceType())) {
                    cmdStr = String.format(Locale.ENGLISH, HdcCommandEnum.HDC_STARTV7_PROFILERD.getHdcCommand(),
                        info.getDeviceID());
                } else {
                    continue;
                }
                LOGGER.debug("cmdStr = {}", cmdStr);
                String result = hdcHelper.execCmdBy(cmdStr);
                LOGGER.debug("getStringResult = {}", result);
                doService();
            }
        }
    }

    private void insertDeviceIPPort(DeviceIPPortInfo info) {
        int first = 1;
        int second = 2;
        String ip = listCliResult.get(first).get(first);
        int port = Integer.parseInt(listCliResult.get(second).get(first));
        // IP
        info.setIp(ip);
        // Port
        info.setPort(port);
        DeviceForwardPort ins = DeviceForwardPort.getInstance();
        DeviceIPPortInfo portInfo = ins.setDeviceIPPortInfo(info);
        sqlUtil.insertDeviceIPPortInfo(portInfo);
    }

    /**
     * pushHiprofilerTools
     *
     * @param info info
     * @return boolean
     */
    public boolean pushHiprofilerTools(DeviceIPPortInfo info) {
        String cmdStr = "";
        String devToolsPath = SessionManager.getInstance().getPluginPath();
        if (DEVICE_FULL_TYPE.equals(info.getDeviceType())) {
            devToolsPath = devToolsPath + DEVTOOLS_PLUGINS_V8_PATH;
            cmdStr = String
                .format(Locale.ENGLISH, HdcCommandEnum.HDC_PUSH_CMD.getHdcCommand(), info.getDeviceID(), devToolsPath);
        } else if (DEVICE_LEAN_TYPE.equals(info.getDeviceType())) {
            devToolsPath = devToolsPath + DEVTOOLS_PLUGINS_V7_PATH;
            cmdStr = String
                .format(Locale.ENGLISH, HdcCommandEnum.HDC_PUSH_CMD.getHdcCommand(), info.getDeviceID(), devToolsPath);
        } else {
            LOGGER.error("DeviceType error {}", info.getDeviceType());
        }
        LOGGER.debug("pushHiprofilerCli cmdStr = {}", cmdStr);
        String result = hdcHelper.getHdcStringResult(cmdStr);
        LOGGER.debug("pushHiprofilerCli getStringResult = {}", result);
        return result.contains(Constant.DEVICE_SATA_STAT_PUSHED);
    }

    /**
     * pushDevToolsShell
     *
     * @param serialNumber serialNumber
     * @return boolean
     */
    public boolean pushDevToolsShell(String serialNumber) {
        String uzipPath = SessionManager.getInstance().getPluginPath() + UNZIP_SHELL_PLUGINS_PATH;
        String cmdStr =
            String.format(Locale.ENGLISH, HdcCommandEnum.HDC_PUSH_OHOS_SHELL.getHdcCommand(), serialNumber, uzipPath);
        LOGGER.debug("cmdStr = {}", cmdStr);
        String result = hdcHelper.getHdcStringResult(cmdStr);
        LOGGER.debug("getStringResult = {}", result);
        return result.contains(Constant.DEVICE_SATA_STAT_PUSHED);
    }

    /**
     * uzipDevTools
     *
     * @param info info
     */
    public void uzipDevTools(DeviceIPPortInfo info) {
        String cmdStr = "";
        if (DEVICE_FULL_TYPE.equals(info.getDeviceType())) {
            cmdStr = String.format(Locale.ENGLISH, HdcCommandEnum.HDC_RUN_OHOS.getHdcCommand(), info.getDeviceID());
        } else if (DEVICE_LEAN_TYPE.equals(info.getDeviceType())) {
            cmdStr = String.format(Locale.ENGLISH, HdcCommandEnum.HDC_RUN_V7_OHOS.getHdcCommand(), info.getDeviceID());
        } else {
            LOGGER.error("DeviceType error {}", info.getDeviceType());
        }
        LOGGER.debug("cmdStr = {}", cmdStr);
        String result = hdcHelper.execCmdBy(cmdStr);
        LOGGER.debug("getStringResult = {}", result);
    }

    /**
     * 获取设备信息
     *
     * @return List<DeviceInfo>
     */
    public List<DeviceInfo> getDevicesInfo() {
        return sqlUtil.getAllDeviceInfo();
    }

    /**
     * getAllDeviceIPPortInfos
     *
     * @return List<DeviceIPPortInfo>
     */
    public List<DeviceIPPortInfo> getAllDeviceIPPortInfos() {
        return sqlUtil.getAllDeviceIPPortInfos();
    }

}
