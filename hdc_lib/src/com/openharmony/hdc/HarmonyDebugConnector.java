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

package com.openharmony.hdc;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import com.openharmony.devices.DeviceMonitor;
import com.openharmony.devices.Devices;

/**
 * HarmonyDebugConnector for connect To hdc server ¡¢create at 20210912
 */
public class HarmonyDebugConnector {
    private static final String TAG = "HarmonyDebugConnector";
    private static final String DEFAULT_HDC_HOST = "127.0.0.1";
    private static final int DEFAULT_HDC_PORT = 8710;

    private static String memHdcLocation;
    private static InetAddress memHostAddr;
    private static InetSocketAddress memSocketAddr;

    private static boolean memInitialized;
    private static boolean memIsHdcServerStarted;
    private static boolean memIsDeviceMonitorRun;

    private static HarmonyDebugConnector memThis;
    private static HdcCommand memHdcCommand;
    private static Object memLock = new Object();

    private static ArrayList<IDeviceChangeListener> memDeviceListeners = new ArrayList<IDeviceChangeListener>();
    private static ArrayList<IFileClientListener> memFileListeners = new ArrayList<IFileClientListener>();
    private static ArrayList<IHilogClientListener> memHilogListeners = new ArrayList<IHilogClientListener>();
    private static ArrayList<IShellClientListener> memShellListeners = new ArrayList<IShellClientListener>();
    private static ArrayList<IConnectorChangeListener> memHdcListeners =  new ArrayList<IConnectorChangeListener>();

    private DeviceMonitor mDeviceMonitor;

    private HarmonyDebugConnector() {
        Hilog.debug(TAG, "init HDC lib");
    }

    private HarmonyDebugConnector(String binLocation) throws InvalidParameterException {
        Hilog.debug(TAG, "init HDC lib with " + binLocation);
        if (binLocation == null || binLocation.isEmpty()) {
            throw new InvalidParameterException("HarmonyDebugConnect value error");
        }
        memHdcLocation = binLocation;
        memHdcCommand = new HdcCommand(memHdcLocation);
    }

    /**
     * init HarmonyDebugConnector socket address
     *
     * @throws IllegalStateException HarmonyDebugConnect has already been init
     */
    public void initIfNeeded() {
        if (memInitialized) {
            throw new IllegalStateException("HarmonyDebugConnect has already been init");
        }
        init();
    }

    /**
     * hdc server is running ?
     *
     * @return whether hdc server running
     */
    public boolean isHdcServerRun() {
        return memIsHdcServerStarted;
    }

    private static boolean isHdcBin() {
        if (memHdcCommand != null) {
            return memHdcCommand.isCorrectVersion(memHdcLocation);
        } else {
            return false;
        }
    }

    private void init() {
        initHdcSocketAddr();
        memInitialized = true;

        if (memThis != null) {
            memThis.startHdcServer();
        }
        waitForHdcService();
    }

    private boolean waitForHdcService() {
        int timeOut = 5;
        while (!memIsHdcServerStarted) {
            try {
                stopHdcServer();
                Thread.sleep(1000);
                timeOut--;
                startHdcServer();
                if (timeOut < 0) {
                    Hilog.error(TAG, "wait for hdc server start time out");
                    break;
                }
            } catch (InterruptedException error) {
                Hilog.debug(TAG, "wait for hdc server start" + error.getMessage());
                break;
            }
        }
        return memIsHdcServerStarted;
    }

    /**
     * IConnectorChangeListener need callback
     */
    public interface IConnectorChangeListener {
        /**
         * HarmonyDebugConnector change listen
         *
         * @param connector HarmonyDebugConnector
         */
        void connectorChanged(HarmonyDebugConnector connector);
    }

    /**
     * IDeviceChangeListener need callback
     */
    public interface IDeviceChangeListener {
        /**
         * deviceConnected
         *
         * @param device device
         */
        void deviceConnected(Devices device);

        /**
         * deviceDisconnected
         *
         * @param device device
         */
        void deviceDisconnected(Devices device);

        /**
         * deviceChanged
         *
         * @param device device
         */
        void deviceChanged(Devices device);
    }

    /**
     * IFileClientListener need callback
     */
    public interface IFileClientListener {
        /**
         * sendFileResult
         *
         * @param resp send result
         */
        void sendFileResult(String resp);

        /**
         * recvFileResult
         *
         * @param resp recv result
         */
        void recvFileResult(String resp);
    }

    /**
     * IHilogClientListener need callback
     */
    public interface IHilogClientListener {
        /**
         * after send hilog command,recv holog at callback
         *
         * @param resp hilog Recv result
         */
        void hilogRecv(String resp);
    }

    /**
     * IShellClientListener need callback
     */
    public interface IShellClientListener {
        /**
         * shell command result
         *
         * @param resp shell command result
         */
        void shellRecv(String resp);
    }

    /**
     * file send and recv status ,it's need implements IFileClientListener
     *
     * @param result command exec result
     */
    public void sendFileResult(String result) {
        IFileClientListener[] listeners = null;
        listeners = memFileListeners.toArray(new IFileClientListener[memFileListeners.size()]);
        // Notify the listeners
        for (IFileClientListener listener : listeners) {
            listener.sendFileResult(result);
        }
    }

    /**
     * file send and recv status ,it's need implements IFileClientListener
     *
     * @param result command exec result
     */
    public void recvFileResult(String result) {
        IFileClientListener[] listeners = null;
        listeners = memFileListeners.toArray(new IFileClientListener[memFileListeners.size()]);
        // Notify the listeners
        for (IFileClientListener listener : listeners) {
            listener.recvFileResult(result);
        }
    }

    /**
     * Hilog ,it's need implements IHilogClientListener
     *
     * @param result hilog result
     */
    public void getHilogResult(String result) {
        IHilogClientListener[] listeners = null;
        listeners = memHilogListeners.toArray(new IHilogClientListener[memHilogListeners.size()]);
        // Notify the listeners
        for (IHilogClientListener listener : listeners) {
            listener.hilogRecv(result);
        }
    }

    /**
     * shell command result ,it's need implements IShellClientListener
     *
     * @param result hilog result
     */
    public void getShellResult(String result) {
        IShellClientListener[] listeners = null;
        listeners = memShellListeners.toArray(new IShellClientListener[memShellListeners.size()]);
        // Notify the listeners
        for (IShellClientListener listener : listeners) {
            listener.shellRecv(result);
        }
    }

    /**
     * device connect ,it's need implements IDeviceChangeListener
     *
     * @param device device info
     */
    public void deviceConnected(Devices device) {
        IDeviceChangeListener[] listeners = null;
        synchronized (memLock) {
            listeners = memDeviceListeners.toArray(new IDeviceChangeListener[memDeviceListeners.size()]);
        }
        // Notify the listeners
        for (IDeviceChangeListener listener : listeners) {
            listener.deviceConnected(device);
        }
    }

    /**
     * device Disconnect ,it's need implements IDeviceChangeListener
     *
     * @param device device info
     */
    public void deviceDisconnected(Devices device) {
        IDeviceChangeListener[] listenersCopy = null;
        synchronized (memLock) {
            listenersCopy = memDeviceListeners.toArray(new IDeviceChangeListener[memDeviceListeners.size()]);
        }
        // Notify the listeners
        for (IDeviceChangeListener listener : listenersCopy) {
            listener.deviceDisconnected(device);
        }
    }

    /**
     * device Changed ,it's need implements IDeviceChangeListener
     *
     * @param device device info
     */
    public void deviceChanged(Devices device) {
        IDeviceChangeListener[] listenersCopy = null;
        synchronized (memLock) {
            listenersCopy = memDeviceListeners.toArray(new IDeviceChangeListener[memDeviceListeners.size()]);
        }
        // Notify the listeners
        for (IDeviceChangeListener listener : listenersCopy) {
            listener.deviceChanged(device);
        }
    }

    /**
     * Add DeviceChangeListener
     *
     * @param listener IDeviceChangeListener
     */
    public static void addDeviceChangeListener(IDeviceChangeListener listener) {
        synchronized (memLock) {
            if (!memDeviceListeners.contains(listener)) {
                memDeviceListeners.add(listener);
            } else {
                Hilog.error(TAG, "already add " + listener);
            }
        }
    }

    /**
     * Cancel DeviceChangeListener
     *
     * @param listener IDeviceChangeListener
     */
    public static void removeDeviceChangeListener(IDeviceChangeListener listener) {
        synchronized (memLock) {
            if (memDeviceListeners.contains(listener)) {
                memDeviceListeners.remove(listener);
            } else {
                Hilog.error(TAG, listener + " is not contain");
            }
        }
    }

    /**
     * get current DeviceChangeListener
     *
     * @return DeviceListeners list
     */
    public ArrayList<IDeviceChangeListener> getDeviceChangeListener() {
        return memDeviceListeners;
    }

    /**
     * add FileClientListener
     *
     * @param listener IFileClientListener
     */
    public static void addFileSendRecvListener(IFileClientListener listener) {
        synchronized (memLock) {
            if (!memFileListeners.contains(listener)) {
                memFileListeners.add(listener);
            } else {
                Hilog.error(TAG, "already add " + listener);
            }
        }
    }

    /**
     * Cancel IFileClientListener
     *
     * @param listener IFileClientListener
     */
    public static void removeFileSendRecvListener(IFileClientListener listener) {
        synchronized (memLock) {
            if (memFileListeners.contains(listener)) {
                memFileListeners.remove(listener);
            } else {
                Hilog.error(TAG, listener + " is not contain");
            }
        }
    }

    /**
     * get current FileClientListener
     *
     * @return FileListeners list
     */
    public ArrayList<IFileClientListener> getFileClientListener() {
        return memFileListeners;
    }

    /**
     * add IFileClientListener
     *
     * @param listener IFileClientListener
     */
    public static void addHilogRecvListener(IHilogClientListener listener) {
        synchronized (memLock) {
            if (!memHilogListeners.contains(listener)) {
                memHilogListeners.add(listener);
            } else {
                Hilog.error(TAG, "already add " + listener);
            }
        }
    }

    /**
     * Cancel IHilogClientListener
     *
     * @param listener IHilogClientListener
     */
    public static void removeHilogRecvListener(IHilogClientListener listener) {
        synchronized (memLock) {
            if (memHilogListeners.contains(listener)) {
                memHilogListeners.remove(listener);
            } else {
                Hilog.error(TAG, listener + " is not contain");
            }
        }
    }

    /**
     * get current HilogClientListener
     *
     * @return HilogListeners list
     */
    public ArrayList<IHilogClientListener> getHilogClientListener() {
        return memHilogListeners;
    }

    /**
     * add IShellClientListener
     *
     * @param listener IShellClientListener
     */
    public static void addShellRecvListener(IShellClientListener listener) {
        synchronized (memLock) {
            if (!memShellListeners.contains(listener)) {
                memShellListeners.add(listener);
            } else {
                Hilog.error(TAG, "already add " + listener);
            }
        }
    }

    /**
     * Cancel IShellClientListener
     *
     * @param listener IShellClientListener
     */
    public static void removeShellRecvListener(IShellClientListener listener) {
        synchronized (memLock) {
            if (memShellListeners.contains(listener)) {
                memShellListeners.remove(listener);
            } else {
                Hilog.error(TAG, listener + " is not contain");
            }
        }
    }

    /**
     * get current ShellClientListener
     *
     * @return ShellListeners list
     */
    public ArrayList<IShellClientListener> getShellClientListener() {
        return memShellListeners;
    }

    /**
     * add IConnectorChangeListener
     *
     * @param listener IConnectorChangeListener
     */
    public static void addConnectorChangeListener(IConnectorChangeListener listener) {
        synchronized (memLock) {
            if (!memHdcListeners.contains(listener)) {
                memHdcListeners.add(listener);
            } else {
                Hilog.error(TAG, "already add " + listener);
            }
        }
    }

    /**
     * Cancel IConnectorChangeListener
     *
     * @param listener IConnectorChangeListener
     */
    public static void removeConnectorChangeListener(IConnectorChangeListener listener) {
        synchronized (memLock) {
            if (memHdcListeners.contains(listener)) {
                memHdcListeners.remove(listener);
            } else {
                Hilog.error(TAG, listener + " is not contain");
            }
        }
    }

    /**
     * get current ConnetorListener
     *
     * @return ConnetorListener list
     */
    public ArrayList<IConnectorChangeListener> getConnetorListener() {
        return memHdcListeners;
    }

    /**
     * create hdc connect
     *
     *  @param binLocation hdc bin
     *  @param forceStart  restart server
     *  @return Instantiated HarmonyDebugConnector
     */
    public static HarmonyDebugConnector createConnect(String binLocation, boolean forceStart) {
        synchronized (memLock) {
            Hilog.debug(TAG, "start HarmonyDebugConnector createConnect");
            if (binLocation == null) {
                Hilog.debug(TAG, "HarmonyDebugConnector createConnect");
                return memThis;
            }
            if (memThis != null) {
                if (memThis.memHdcLocation != null && memThis.memHdcLocation.equals(binLocation) && !forceStart) {
                    return memThis;
                }
            }

            try {
                memThis = new HarmonyDebugConnector(binLocation);
                if (isHdcBin()) {
                    Hilog.debug(TAG, "Hdc Version right");
                } else {
                    memThis = null;
                    Hilog.error(TAG, "Not Hdc Bin or Hdc version older");
                }
            } catch (InvalidParameterException error) {
                Hilog.error(TAG, error);
                memThis = null;
            }

            if (memThis != null) {
                IConnectorChangeListener[] listenersCopy = memHdcListeners.toArray(
                        new IConnectorChangeListener[memHdcListeners.size()]);

                for (IConnectorChangeListener listener : listenersCopy) {
                    listener.connectorChanged(memThis);
                }
            }
            return memThis;
        }
    }

    /**
     * destroy connect
     */
    public void destroyConnect() {
        synchronized (memLock) {
            stopDevicesMonitor();
            stopHdcServer();
            memInitialized = false;
            memIsHdcServerStarted = false;
            memDeviceListeners.clear();
            memShellListeners.clear();
            memFileListeners.clear();
            memHilogListeners.clear();
            if (memThis != null) {
                IConnectorChangeListener[] listenersCopy = memHdcListeners.toArray(
                        new IConnectorChangeListener[memHdcListeners.size()]);

                for (IConnectorChangeListener listener : listenersCopy) {
                    listener.connectorChanged(memThis);
                }
            }
            memThis = null;
        }
    }

    private static void initHdcSocketAddr() {
        try {
            memHostAddr = InetAddress.getByName(DEFAULT_HDC_HOST);
            memSocketAddr = new InetSocketAddress(memHostAddr, DEFAULT_HDC_PORT);
        } catch (UnknownHostException error) {
            Hilog.debug(TAG, "Socket error :" + error);
        }
    }

    /**
     * get default socket address (local:8710)
     *
     * @return InetSocketAddress
     */
    public InetSocketAddress getSocketAddress() {
        startHdcServer(); // if we want to connect,we must keep server running
        return memSocketAddr;
    }

    /**
     * get hdc connector by default port and bin
     *
     * @return HarmonyDebugConnector
     */
    public HarmonyDebugConnector getHdcConnector() {
        return memThis;
    }

    @Override
    public String toString() {
        return "HDC Bin Path is : " + memThis.memHdcLocation + "HDC default port is : " + DEFAULT_HDC_PORT;
    }

    /**
     * start Devices Monitor
     * if isDeviceMonitorRun is true,it will return false
     *
     * @return whether start Devices Monitor successfully
     */
    public boolean startDevicesMonitor() {
        if (!memIsDeviceMonitorRun) {
            mDeviceMonitor = new DeviceMonitor(this, false);
            mDeviceMonitor.start();
            memIsDeviceMonitorRun = true;
            return true;
        } else {
            Hilog.error(TAG, "Device Monitor already run");
            return false;
        }
    }

    /**
     * stop Devices Monitor
     *
     * @return whether stop Devices Monitor successfully
     */
    public boolean stopDevicesMonitor() {
        // if we haven't started we return false;
        if (!memIsDeviceMonitorRun) {
            Hilog.error(TAG, "Device Monitor already stop");
            return false;
        }
        // kill the monitoring services
        if (mDeviceMonitor != null) {
            mDeviceMonitor.stop();
        }
        memIsDeviceMonitorRun = false;
        return true;
    }

    /**
     * Devices Monitor status
     *
     * @return Devices Monitor is running
     */
    public boolean isDeviceMonitorRun() {
        return memIsDeviceMonitorRun;
    }

    /**
     * get Device Monitor
     *
     * @return mDeviceMonitor
     */
    public DeviceMonitor getDeviceMonitor() {
        return mDeviceMonitor;
    }

    /**
     * get current devices array
     *
     * @return device array with device base info
     */
    public Devices[] getDevices() {
        synchronized (memLock) {
            if (mDeviceMonitor != null) {
                return mDeviceMonitor.getDevices();
            }
        }
        return new Devices[0];
    }

    /**
     * getlock
     *
     * @return current lock static
     */
    public static Object getLock() {
        return memLock;
    }

    private void startHdcServer() {
        if (memHdcCommand.executeHdcCommand("start")) {
            memIsHdcServerStarted = true;
        }
    }

    private void stopHdcServer() {
        if (memHdcCommand.executeHdcCommand("kill")) {
            memIsHdcServerStarted = false;
        }
    }
}