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

package com.openharmony.devices;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import com.openharmony.devices.DeviceHelper.HdcResponse;
import com.openharmony.hdc.HarmonyDebugConnector;
import com.openharmony.hdc.Hilog;
import com.openharmony.utils.FormatUtil;

/**
 * Scan device ¡¢create at 20210912
 */
public final class DeviceMonitor {
    private static final String TAG = "DeviceMonitor";
    private HarmonyDebugConnector mHdc;
    private final ArrayList<Devices> mDevices = new ArrayList<Devices>();

    private boolean mIsStart;
    private boolean mIsTestMode;
    private SocketChannel mHdcConnection;
    private ExecutorService mCachedThreadPool = Executors.newCachedThreadPool();

    /**
     * init DeviceMonitor
     *
     * @param hdc Instantiated HarmonyDebugConnector
     * @param isTestMode true is test mode
     */
    public DeviceMonitor(HarmonyDebugConnector hdc, boolean isTestMode) {
        if (hdc == null) {
            Hilog.e(TAG, "hdc is null");
        }
        if (isTestMode) {
            mIsTestMode = true;
            mIsStart = false;
        } else {
            mIsStart = false;
            mHdc = hdc;
            Hilog.d(TAG, "init DeviceMonitor");
        }
    }

    /**
     * start device monitoring
     */
    public void start() {
        if (mIsTestMode) {
            mIsStart = true;
            return;
        }
        mIsStart = true;
        mCachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                deviceMonitorLoop();
            }
        });
    }

    /**
     * stop device monitoring
     */
    public void stop() {
        if (mIsTestMode) {
            mIsStart = false;
            return;
        }
        if (mCachedThreadPool != null) {
            mCachedThreadPool.shutdownNow();
        }
        mIsStart = false;
    }

    /**
     * get current HarmonyDebugConnector
     *
     * @return HarmonyDebugConnector
     */
    public HarmonyDebugConnector getHdc() {
        return mHdc;
    }

    /**
     * get DeviceMonitor status
     *
     * @return whether device scan is running
     */
    public boolean isDeviceMonitorStart() {
        return mIsStart;
    }

    /**
     * get all devices info
     *
     * @return current devices array
     */
    public Devices[] getDevices() {
        synchronized (mDevices) {
            return mDevices.toArray(new Devices[mDevices.size()]);
        }
    }

    private void deviceMonitorLoop() {
        Hilog.d(TAG, "deviceMonitorLoop IsStart is :" + mIsStart);
        mHdcConnection = getHdcSocketChannel(mHdcConnection, mHdc);
        Hilog.d(TAG, "start connect hdc server ");
        // we will get OHOS HDC
        HdcResponse ohos = null;
        ohos = getHeadResponse(mHdcConnection);
        if (ohos != null && ohos.okay) {
            Hilog.d(TAG, "get ohos............");
            sendHeadRequest("");
        } else {
            Hilog.d(TAG, "not get ohos............");
            return;
        }
        while (mIsStart) {
            threadSleep(1); // keep socket connect
            if (mHdcConnection != null) {
                Hilog.d(TAG, "sendDeviceListMonitoringRequest");
                sendDeviceListMonitoringRequest(true);
            } else {
                Hilog.d(TAG, "HdcConnection is null");
            }
            if (mIsStart) {
                getIncomingDeviceData();
            }
        }

        if (mHdcConnection != null) {
            try {
                mHdcConnection.close();
            } catch (IOException error) {
                Hilog.e(TAG, error);
            }
        }
    }

    private SocketChannel getHdcSocketChannel(SocketChannel channel, HarmonyDebugConnector hdc) {
        try {
            channel = SocketChannel.open(hdc.getSocketAddress());
            channel.socket().setTcpNoDelay(true);
        } catch (IOException error) {
            Hilog.e(TAG, "openHdcConnection failed :" + error);
        }
        return channel;
    }

    private void getIncomingDeviceData() {
        byte[] bufferSize = new byte[4];
        try {
            String size = DeviceHelper.readServer(mHdcConnection, bufferSize);
            Hilog.d(TAG, size);
            String result = DeviceHelper.readServer(mHdcConnection,
                    new byte[FormatUtil.asciiStringToInt(size.getBytes())]);
            updateDeviceList(result);
        } catch (IOException error) {
            Hilog.e(TAG, error);
        }
    }

    private void updateDeviceList(String result) {
        if (result.indexOf("Empty") > 0) {
            Hilog.d(TAG, "no devices");
            return;
        }

        ArrayList<Devices> list = new ArrayList<Devices>();
        String[] allDeviceInfo = result.split("\n");
        for (int deviceNo = 0; deviceNo < allDeviceInfo.length; deviceNo++) {
            String[] arry = allDeviceInfo[deviceNo].split("\\s+"); // delete space
            DeviceState deviceState = new DeviceState(arry[0], false);

            if (arry.length == 4) {
                deviceState.setConnection(arry[1]);
                deviceState.setStatus(arry[2]);
                deviceState.setAddress(arry[3]);
            }
            if (arry.length == 3) {
                deviceState.setConnection(arry[1]);
                deviceState.setStatus(arry[2]);
            }
            Devices devices = new Devices(arry[0], deviceState, false);
            devices.setClient(mHdc);
            list.add(devices);
        }
        updateDevice(list);
    }

    private void updateDevice(ArrayList<Devices> list) {
        synchronized (HarmonyDebugConnector.getLock()) {
            synchronized (mDevices) {
                for (int currentDevice = 0; currentDevice < mDevices.size();) {
                    Devices device = mDevices.get(currentDevice);
                    int count = list.size();
                    boolean foundMatch = false;
                    for (int dd = 0; dd < count; dd++) {
                        Devices newDevice = list.get(dd);
                        if (newDevice.getSerialNumber().equals(device.getSerialNumber())) {
                            foundMatch = true;
                            // update devices status
                            if (device.getState().getAddress().equals(newDevice.getState().getAddress())
                                    && device.getState().getStatus().equals(newDevice.getState().getStatus())
                                    && device.getState().getConnection().equals(newDevice.getState().getConnection())) {
                                Hilog.d(TAG, "device not change.........................................");
                            } else {
                                Hilog.d(TAG, "device change.........................................");
                                mHdc.deviceChanged(newDevice);
                                device.setState(newDevice.getState());
                                updateDeviceProp(newDevice);
                            }
                            // remove the new device from the list since it's been used
                            list.remove(dd);
                            break;
                        }
                    }
                    if (!foundMatch) {
                        mHdc.deviceDisconnected(device);
                    } else {
                        // process the next one
                        currentDevice++;
                    }
                }
                for (Devices newDevice : list) {
                    // add them to the list and tell hdc devices
                    mDevices.add(newDevice);
                    mHdc.deviceConnected(newDevice);
                    updateDeviceProp(newDevice);
                }
            }
        }

    }

    private void threadSleep(int second) {
        try {
            Thread.sleep(1000 * second);
        } catch (InterruptedException error) {
            Hilog.e(TAG, error);
        }
    }

    private void sendHeadRequest(String connectKey) {
        try {
            DeviceHelper.write(mHdcConnection, ChannelHandShake.getHeadData(connectKey));
        } catch (TimeoutException | IOException error) {
            Hilog.e(TAG, error);
        }
    }

    private void sendDeviceListMonitoringRequest(boolean longConnect) {
        String command = longConnect ? "HLONGlist targets -v" : "list targets -v";
        try {
            DeviceHelper.write(mHdcConnection, ChannelHandShake.getCommandByte(command));
        } catch (TimeoutException | IOException error) {
            Hilog.e(TAG, error);
        }
    }

    private HdcResponse getHeadResponse(SocketChannel channel) {
        HdcResponse resp = null;
        try {
            resp = DeviceHelper.readHdcResponse(channel);
        } catch (TimeoutException | IOException error) {
            Hilog.e(TAG, error);
        }
        return resp;
    }

    private void updateDeviceProp(Devices device) {
        for (int deviceProp = 0; deviceProp < DevicePreferences.DEFAULT_PROP.length; deviceProp++) {
            String temp = DevicePreferences.DEFAULT_PROP[deviceProp];
            switch (temp) {
                case "ro.boot.selinux":
                    device.setSelinux(getDeviceProp(device, temp));
                    break;
                case "ro.build.date":
                    device.setBuildDate(getDeviceProp(device, temp));
                    break;
                case "ro.build.fingerprint":
                    device.setFingerprint(getDeviceProp(device, temp));
                    break;
                case "ro.build.version.sdk":
                    device.setSdkVersion(getDeviceProp(device, temp));
                    break;
                case "ro.vndk.version":
                    device.setVndkVersion(getDeviceProp(device, temp));
                    break;
                case "ro.build.type":
                    device.setBuildType(getDeviceProp(device, temp));
                    break;
                case "ro.debuggable":
                    device.setDebuggable(getDeviceProp(device, temp));
                    break;
                case "ro.secure":
                    device.setSecure(getDeviceProp(device, temp));
                    break;
                case "ro.build.version.security_patch":
                    device.setSecurityPatch(getDeviceProp(device, temp));
                    break;
                case "ro.product.manufacturer":
                    device.setManufacturer(getDeviceProp(device, temp));
                    break;
                case "ro.product.brand":
                    device.setProductBrand(getDeviceProp(device, temp));
                    break;
                case "ro.product.board":
                    device.setProductBoard(getDeviceProp(device, temp));
                    break;
                case "ro.product.model":
                    device.setProductModel(getDeviceProp(device, temp));
                    break;
                case "ro.product.device":
                    device.setProductDevice(getDeviceProp(device, temp));
                    break;
                default: Hilog.e(TAG, "this prop not in device");
            }
        }
    }

    private String getDeviceProp(Devices device, String prop) {
        return  device.getClient().getProp(prop, DevicePreferences.DEFAULT_EMPTY);
    }
}
