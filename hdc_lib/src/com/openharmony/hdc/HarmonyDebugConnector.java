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
    private static final String DEFAULT_HDC_HOST = "127.0.0.1"; // Where to find the HDC PORT.
    private static final int DEFAULT_HDC_PORT = 8710; // const string DEFAULT_SERVER_ADDR = "127.0.0.1:8710";

    private static String mHdcLocation; // full path of hdc, D:\hos_ide\hdc.exe
    private static InetAddress mHostAddr;
    private static InetSocketAddress mSocketAddr;

    private static boolean mInitialized;
    private static boolean mIsHdcServerStarted;
    private static boolean mIsDeviceMonitorRun;

    private static HarmonyDebugConnector mThis;
    private static HdcCommand mHdcCommand;
    private static Object mLock = new Object();

    private static ArrayList<IDeviceChangeListener> mDeviceListeners = new ArrayList<IDeviceChangeListener>();
    private static ArrayList<IFileClientListener> mFileListeners = new ArrayList<IFileClientListener>();
    private static ArrayList<IHilogClientListener> mHilogListeners = new ArrayList<IHilogClientListener>();
    private static ArrayList<IShellClientListener> mShellListeners = new ArrayList<IShellClientListener>();
    private static ArrayList<IConnectorChangeListener> mConnectorListeners =  new ArrayList<IConnectorChangeListener>();

    private DeviceMonitor mDeviceMonitor;

    private HarmonyDebugConnector() {
        Hilog.d(TAG, "init HDC lib");
    }

    private HarmonyDebugConnector(String binLocation) throws InvalidParameterException {
        Hilog.d(TAG, "init HDC lib with " + binLocation);
        if (binLocation == null || binLocation.isEmpty()) {
            throw new InvalidParameterException("HarmonyDebugConnect value error");
        }
        mHdcLocation = binLocation;
        mHdcCommand = new HdcCommand(mHdcLocation);
    }

    /**
     * init HarmonyDebugConnector socket address
     *
     * @throws IllegalStateException HarmonyDebugConnect has already been init
     */
    public void initIfNeeded() {
        if (mInitialized) {
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
        return mIsHdcServerStarted;
    }

    private static boolean isHdcBin() {
        if (mHdcCommand != null) {
            return mHdcCommand.isCorrectVersion(mHdcLocation);
        } else {
            return false;
        }
    }

    private void init() {
        initHdcSocketAddr();
        mInitialized = true;

        if (mThis != null) {
            mThis.startHdcServer();
        }
        waitForHdcService();
    }

    private boolean waitForHdcService() {
        int timeOut = 5;
        while (!mIsHdcServerStarted) {
            try {
                stopHdcServer();
                Thread.sleep(1000);
                timeOut--;
                startHdcServer();
                if (timeOut < 0) {
                    Hilog.e(TAG, "wait for hdc server start time out");
                    break;
                }
            } catch (InterruptedException error) {
                Hilog.d(TAG, "wait for hdc server start" + error.getMessage());
                break;
            }
        }
        return mIsHdcServerStarted;
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
        listeners = mFileListeners.toArray(new IFileClientListener[mFileListeners.size()]);
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
        listeners = mFileListeners.toArray(new IFileClientListener[mFileListeners.size()]);
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
        listeners = mHilogListeners.toArray(new IHilogClientListener[mHilogListeners.size()]);
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
        listeners = mShellListeners.toArray(new IShellClientListener[mShellListeners.size()]);
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
        synchronized (mLock) {
            listeners = mDeviceListeners.toArray(new IDeviceChangeListener[mDeviceListeners.size()]);
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
        synchronized (mLock) {
            listenersCopy = mDeviceListeners.toArray(new IDeviceChangeListener[mDeviceListeners.size()]);
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
        synchronized (mLock) {
            listenersCopy = mDeviceListeners.toArray(new IDeviceChangeListener[mDeviceListeners.size()]);
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
        synchronized (mLock) {
            if (!mDeviceListeners.contains(listener)) {
                mDeviceListeners.add(listener);
            } else {
                Hilog.e(TAG, "already add " + listener);
            }
        }
    }

    /**
     * Cancel DeviceChangeListener
     *
     * @param listener IDeviceChangeListener
     */
    public static void removeDeviceChangeListener(IDeviceChangeListener listener) {
        synchronized (mLock) {
            if (mDeviceListeners.contains(listener)) {
                mDeviceListeners.remove(listener);
            } else {
                Hilog.e(TAG, listener + " is not contain");
            }
        }
    }

    /**
     * get current DeviceChangeListener
     *
     * @return DeviceListeners list
     */
    public static ArrayList<IDeviceChangeListener> getDeviceChangeListener() {
        return mDeviceListeners;
    }

    /**
     * add FileClientListener
     *
     * @param listener IFileClientListener
     */
    public static void addFileSendRecvListener(IFileClientListener listener) {
        synchronized (mLock) {
            if (!mFileListeners.contains(listener)) {
                mFileListeners.add(listener);
            } else {
                Hilog.e(TAG, "already add " + listener);
            }
        }
    }

    /**
     * Cancel IFileClientListener
     *
     * @param listener IFileClientListener
     */
    public static void removeFileSendRecvListener(IFileClientListener listener) {
        synchronized (mLock) {
            if (mFileListeners.contains(listener)) {
                mFileListeners.remove(listener);
            } else {
                Hilog.e(TAG, listener + " is not contain");
            }
        }
    }

    /**
     * get current FileClientListener
     *
     * @return FileListeners list
     */
    public static ArrayList<IFileClientListener> getFileClientListener() {
        return mFileListeners;
    }

    /**
     * add IFileClientListener
     *
     * @param listener IFileClientListener
     */
    public static void addHilogRecvListener(IHilogClientListener listener) {
        synchronized (mLock) {
            if (!mHilogListeners.contains(listener)) {
                mHilogListeners.add(listener);
            } else {
                Hilog.e(TAG, "already add " + listener);
            }
        }
    }

    /**
     * Cancel IHilogClientListener
     *
     * @param listener IHilogClientListener
     */
    public static void removeHilogRecvListener(IHilogClientListener listener) {
        synchronized (mLock) {
            if (mHilogListeners.contains(listener)) {
                mHilogListeners.remove(listener);
            } else {
                Hilog.e(TAG, listener + " is not contain");
            }
        }
    }

    /**
     * get current HilogClientListener
     *
     * @return HilogListeners list
     */
    public static ArrayList<IHilogClientListener> getHilogClientListener() {
        return mHilogListeners;
    }

    /**
     * add IShellClientListener
     *
     * @param listener IShellClientListener
     */
    public static void addShellRecvListener(IShellClientListener listener) {
        synchronized (mLock) {
            if (!mShellListeners.contains(listener)) {
                mShellListeners.add(listener);
            } else {
                Hilog.e(TAG, "already add " + listener);
            }
        }
    }

    /**
     * Cancel IShellClientListener
     *
     * @param listener IShellClientListener
     */
    public static void removeShellRecvListener(IShellClientListener listener) {
        synchronized (mLock) {
            if (mShellListeners.contains(listener)) {
                mShellListeners.remove(listener);
            } else {
                Hilog.e(TAG, listener + " is not contain");
            }
        }
    }

    /**
     * get current ShellClientListener
     *
     * @return ShellListeners list
     */
    public static ArrayList<IShellClientListener> getShellClientListener() {
        return mShellListeners;
    }

    /**
     * add IConnectorChangeListener
     *
     * @param listener IConnectorChangeListener
     */
    public static void addConnectorChangeListener(IConnectorChangeListener listener) {
        synchronized (mLock) {
            if (!mConnectorListeners.contains(listener)) {
                mConnectorListeners.add(listener);
            } else {
                Hilog.e(TAG, "already add " + listener);
            }
        }
    }

    /**
     * Cancel IConnectorChangeListener
     *
     * @param listener IConnectorChangeListener
     */
    public static void removeConnectorChangeListener(IConnectorChangeListener listener) {
        synchronized (mLock) {
            if (mConnectorListeners.contains(listener)) {
                mConnectorListeners.remove(listener);
            } else {
                Hilog.e(TAG, listener + " is not contain");
            }
        }
    }

    /**
     * get current ConnetorListener
     *
     * @return ConnetorListener list
     */
    public static ArrayList<IConnectorChangeListener> getConnetorListener() {
        return mConnectorListeners;
    }

    /**
     * create hdc connect
     *
     *  @param binLocation hdc bin
     *  @param forceStart  restart server
     *  @return Instantiated HarmonyDebugConnector
     */
    public static HarmonyDebugConnector createConnect(String binLocation, boolean forceStart) {
        synchronized (mLock) {
            Hilog.d(TAG, "start HarmonyDebugConnector createConnect");
            if (binLocation == null) {
                Hilog.d(TAG, "HarmonyDebugConnector createConnect");
                return mThis;
            }
            if (mThis != null) {
                if (mThis.mHdcLocation != null && mThis.mHdcLocation.equals(binLocation) && !forceStart) {
                    return mThis;
                }
            }

            try {
                mThis = new HarmonyDebugConnector(binLocation);
                if (isHdcBin()) {
                    Hilog.d(TAG, "Hdc Version right");
                } else {
                    mThis = null;
                    Hilog.e(TAG, "Not Hdc Bin or Hdc version older");
                }
            } catch (InvalidParameterException error) {
                Hilog.e(TAG, error);
                mThis = null;
            }

            if (mThis != null) {
                IConnectorChangeListener[] listenersCopy = mConnectorListeners.toArray(
                        new IConnectorChangeListener[mConnectorListeners.size()]);

                for (IConnectorChangeListener listener : listenersCopy) {
                        listener.connectorChanged(mThis);
                }
            }
            return mThis;
        }
    }

    /**
     * destroy connect
     */
    public void destroyConnect() {
        synchronized (mLock) {
            stopDevicesMonitor();
            stopHdcServer();
            mInitialized = false;
            mIsHdcServerStarted = false;
            mDeviceListeners.clear();
            mShellListeners.clear();
            mFileListeners.clear();
            mHilogListeners.clear();
            if (mThis != null) {
                IConnectorChangeListener[] listenersCopy = mConnectorListeners.toArray(
                        new IConnectorChangeListener[mConnectorListeners.size()]);

                for (IConnectorChangeListener listener : listenersCopy) {
                        listener.connectorChanged(mThis);
                }
            }
            mThis = null;
        }
    }

    private static void initHdcSocketAddr() {
        try {
            mHostAddr = InetAddress.getByName(DEFAULT_HDC_HOST);
            mSocketAddr = new InetSocketAddress(mHostAddr, DEFAULT_HDC_PORT);
        } catch (UnknownHostException error) {
            Hilog.d(TAG, "Socket error :" + error);
        }
    }

    /**
     * get default socket address (local:8710)
     *
     * @return InetSocketAddress
     */
    public InetSocketAddress getSocketAddress() {
        startHdcServer(); // if we want to connect,we must keep server running
        return mSocketAddr;
    }

    /**
     * get hdc connector by default port and bin
     *
     * @return HarmonyDebugConnector
     */
    public HarmonyDebugConnector getHdcConnector() {
        return mThis;
    }

    @Override
    public String toString() {
        return "HDC Bin Path is : " + mThis.mHdcLocation + "HDC default port is : " + DEFAULT_HDC_PORT;
    }

    /**
     * start Devices Monitor
     * if isDeviceMonitorRun is true,it will return false
     *
     * @return whether start Devices Monitor successfully
     */
    public boolean startDevicesMonitor() {
        if (!mIsDeviceMonitorRun) {
            mDeviceMonitor = new DeviceMonitor(this, false);
            mDeviceMonitor.start();
            mIsDeviceMonitorRun = true;
            return true;
        } else {
            Hilog.e(TAG, "Device Monitor already run");
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
        if (!mIsDeviceMonitorRun) {
            Hilog.e(TAG, "Device Monitor already stop");
            return false;
        }
        // kill the monitoring services
        if (mDeviceMonitor != null) {
            mDeviceMonitor.stop();
        }
        mIsDeviceMonitorRun = false;
        return true;
    }

    /**
     * Devices Monitor status
     *
     * @return Devices Monitor is running
     */
    public boolean isDeviceMonitorRun() {
        return mIsDeviceMonitorRun;
    }

    /**
     * get Device Monitor
     *
     * @return mDeviceMonitor
     */
    public DeviceMonitor getDeviceMonitor() {
        return mDeviceMonitor;
    }
    // Devices Monitor end

    /**
     * get current devices array
     *
     * @return device array with device base info
     */
    public Devices[] getDevices() {
        synchronized (mLock) {
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
        return mLock;
    }

    private void startHdcServer() {
        if (mHdcCommand.executeHdcCommand("start")) {
            mIsHdcServerStarted = true;
        }
    }

    private void stopHdcServer() {
        if (mHdcCommand.executeHdcCommand("kill")) {
            mIsHdcServerStarted = false;
        }
    }
}