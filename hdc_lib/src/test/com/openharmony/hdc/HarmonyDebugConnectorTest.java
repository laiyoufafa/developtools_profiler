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

package test.com.openharmony.hdc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.openharmony.devices.DeviceMonitor;
import com.openharmony.devices.Devices;
import com.openharmony.hdc.HarmonyDebugConnector;
import com.openharmony.hdc.HarmonyDebugConnector.IDeviceChangeListener;
import com.openharmony.hdc.HarmonyDebugConnector.IFileClientListener;
import com.openharmony.hdc.HarmonyDebugConnector.IHilogClientListener;
import com.openharmony.hdc.HarmonyDebugConnector.IShellClientListener;

/**
 * HarmonyDebugConnector UT Test¡¢create at 20210914
 */
public class HarmonyDebugConnectorTest {
    private String mHdcPath;
    private HarmonyDebugConnector mHdc;

    /**
     * set Test env
     *
     * @throws Exception hdc init exception
     */
    @Before
    public void setUp() throws Exception {
        mHdcPath = "C:\\hdc_std.exe";
    }

    /**
     * destory hdc init env
     *
     * @throws Exception hdc init exception
     */
    @After
    public void tearDown() throws Exception {
        mHdcPath = null;
        mHdc = null;
    }

    /**
     * test hdc init with false
     */
    @Test(timeout = 2000)
    public void testCreateConnect01() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, false);
        assertNotNull(mHdc);
        mHdc.destroyConnect();
    }

    /**
     * test hdc init with true
     */
    @Test(timeout = 2000)
    public void testCreateConnect02() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        assertNotNull(mHdc);
        mHdc.destroyConnect();
    }

    /**
     * test hdc init with empty and false
     */
    @Test(timeout = 2000)
    public void testCreateConnect03() {
        mHdc = HarmonyDebugConnector.createConnect("", false);
        assertNull(mHdc);
    }

    /**
     * test hdc init with empty and true
     */
    @Test(timeout = 2000)
    public void testCreateConnect04() {
        mHdc = HarmonyDebugConnector.createConnect("", true);
        assertNull(mHdc);
    }

    /**
     * test hdc init with null and false
     */
    @Test(timeout = 2000)
    public void testCreateConnect05() {
        mHdc = HarmonyDebugConnector.createConnect(null, false);
        assertNull(mHdc);
    }

    /**
     * test hdc init with null and true
     */
    @Test(timeout = 2000)
    public void testCreateConnect06() {
        mHdc = HarmonyDebugConnector.createConnect(null, true);
        assertNull(mHdc);
    }

    /**
     * test DeviceMonitor status after Destroy Connect
     */
    @Test(timeout = 2000)
    public void testDestroyConnect01() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.destroyConnect();
        assertFalse(mHdc.isDeviceMonitorRun());
    }

    /**
     * test HdcServer status after Destroy Connect
     */
    @Test(timeout = 2000)
    public void testDestroyConnect02() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.destroyConnect();
        assertFalse(mHdc.isHdcServerRun());
    }

    /**
     * test Hdc Connector status after Destroy Connect
     */
    @Test(timeout = 2000)
    public void testDestroyConnect03() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.destroyConnect();
        assertNull(mHdc.getHdcConnector());
    }

    /**
     * test hdc init
     */
    @Test(timeout = 5000)
    public void testInitIfNeeded01() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        try {
            mHdc.initIfNeeded();
        } catch (IllegalStateException error) {
            assertSame(error.getMessage(), "HarmonyDebugConnect has already been init");
        }
        assertTrue(mHdc.isHdcServerRun());
        mHdc.destroyConnect();
    }

    /**
     * test hdc server status after destroy
     */
    @Test(timeout = 5000)
    public void testInitIfNeeded02() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        try {
            mHdc.initIfNeeded();
        } catch (IllegalStateException error) {
            assertSame(error.getMessage(), "HarmonyDebugConnect has already been init");
        }
        mHdc.destroyConnect();
        assertFalse(mHdc.isHdcServerRun());
    }

    /**
     * test hdc init twice status
     */
    @Test(timeout = 5000)
    public void testInitIfNeeded03() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        try {
            mHdc.initIfNeeded();
        } catch (IllegalStateException error) {
            assertSame(error.getMessage(), "HarmonyDebugConnect has already been init");
        }
        assertTrue(mHdc.isHdcServerRun());
        try {
            mHdc.initIfNeeded();
        } catch (IllegalStateException error) {
            assertSame(error.getMessage(), "HarmonyDebugConnect has already been init");
        }
        mHdc.destroyConnect();
    }

    /**
     * test hdc hdc server and connection status
     */
    @Test(timeout = 5000)
    public void testGetHdcConnection01() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        try {
            Thread.sleep(500);
        } catch (InterruptedException error) {
            error.printStackTrace();
        }
        assertTrue(mHdc.isHdcServerRun());
        assertNotNull(mHdc.getSocketAddress());
        mHdc.destroyConnect();
    }

    /**
     * test get hdc connect socket
     */
    @Test(timeout = 5000)
    public void testGetHdcConnection02() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        assertTrue(mHdc.isHdcServerRun());
        assertNotNull(mHdc.getSocketAddress());
        mHdc.destroyConnect();
    }

    /**
     * test hdc start device monitor
     */
    @Test(timeout = 5000)
    public void testStartDevicesMonitor01() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        assertTrue(mHdc.startDevicesMonitor());
        assertTrue(mHdc.isDeviceMonitorRun());
        mHdc.destroyConnect();
    }

    /**
     * test hdc start device monitor twice
     */
    @Test(timeout = 5000)
    public void testStartDevicesMonitor02() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        assertTrue(mHdc.startDevicesMonitor());
        assertFalse(mHdc.startDevicesMonitor());
        mHdc.destroyConnect();
    }

    /**
     * test hdc stop device monitor
     */
    @Test(timeout = 5000)
    public void testStopDevicesMonitor01() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        assertTrue(mHdc.startDevicesMonitor());
        assertTrue(mHdc.stopDevicesMonitor());
        assertFalse(mHdc.isDeviceMonitorRun());
        mHdc.destroyConnect();
    }

    /**
     * test hdc stop device monitor twice
     */
    @Test(timeout = 5000)
    public void testStopDevicesMonitor02() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        assertTrue(mHdc.startDevicesMonitor());
        assertTrue(mHdc.stopDevicesMonitor());
        assertFalse(mHdc.stopDevicesMonitor());
        assertFalse(mHdc.isDeviceMonitorRun());
        mHdc.destroyConnect();
    }

    /**
     * test get same hdc device monitor
     */
    @Test(timeout = 5000)
    public void testGetDeviceMonitor01() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        assertTrue(mHdc.startDevicesMonitor());
        DeviceMonitor deviceMonitor1 = mHdc.getDeviceMonitor();
        assertFalse(mHdc.startDevicesMonitor());
        DeviceMonitor deviceMonitor2 = mHdc.getDeviceMonitor();
        assertEquals(deviceMonitor1, deviceMonitor2);
        mHdc.destroyConnect();
    }

    /**
     * test get different hdc device monitor
     */
    @Test(timeout = 5000)
    public void testGetDeviceMonitor02() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        assertTrue(mHdc.startDevicesMonitor());
        DeviceMonitor deviceMonitor1 = mHdc.getDeviceMonitor();
        assertTrue(mHdc.stopDevicesMonitor());
        assertTrue(mHdc.startDevicesMonitor());
        DeviceMonitor deviceMonitor2 = mHdc.getDeviceMonitor();
        assertNotEquals(deviceMonitor1, deviceMonitor2);
        mHdc.destroyConnect();
    }

    /**
     * test get scan device
     */
    @Test(timeout = 5000)
    public void testGetDevices() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        assertTrue(mHdc.startDevicesMonitor());
        assertTrue(mHdc.getDevices().length >= 0);
        mHdc.destroyConnect();
    }

    /**
     * test add device change listener
     */
    @Test(timeout = 5000)
    public void testAddDeviceChangeListener01() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        DeviceChange deviceChangeListener = new DeviceChange();
        mHdc.addDeviceChangeListener(deviceChangeListener);
        mHdc.addDeviceChangeListener(deviceChangeListener);
        assertEquals(1, mHdc.getDeviceChangeListener().size());
        mHdc.destroyConnect();
    }

    /**
     * test add two device change listener
     */
    @Test(timeout = 5000)
    public void testAddDeviceChangeListener02() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        DeviceChange deviceChangeListener = new DeviceChange();
        DeviceChange deviceChangeListener1 = new DeviceChange();
        mHdc.addDeviceChangeListener(deviceChangeListener);
        mHdc.addDeviceChangeListener(deviceChangeListener1);
        assertEquals(2, mHdc.getDeviceChangeListener().size());
        mHdc.destroyConnect();
    }

    /**
     * test add zero change listener
     */
    @Test(timeout = 5000)
    public void testAddDeviceChangeListener03() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        assertEquals(0, mHdc.getDeviceChangeListener().size());
        mHdc.destroyConnect();
    }

    /**
     * test add device change listener clear after hdc destroy
     */
    @Test(timeout = 5000)
    public void testAddDeviceChangeListener04() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        DeviceChange deviceChangeListener = new DeviceChange();
        DeviceChange deviceChangeListener1 = new DeviceChange();
        mHdc.addDeviceChangeListener(deviceChangeListener);
        mHdc.addDeviceChangeListener(deviceChangeListener1);
        assertEquals(2, mHdc.getDeviceChangeListener().size());
        mHdc.destroyConnect();
        assertEquals(0, mHdc.getDeviceChangeListener().size());
    }

    /**
     * test remove device change listener
     */
    @Test(timeout = 5000)
    public void testRemoveDeviceChangeListener01() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        DeviceChange deviceChangeListener = new DeviceChange();
        mHdc.addDeviceChangeListener(deviceChangeListener);
        assertEquals(1, mHdc.getDeviceChangeListener().size());
        mHdc.removeDeviceChangeListener(deviceChangeListener);
        assertEquals(0, mHdc.getDeviceChangeListener().size());
        mHdc.destroyConnect();
    }

    /**
     * test remove two device change listener
     */
    @Test(timeout = 5000)
    public void testRemoveDeviceChangeListener02() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();

        DeviceChange deviceChangeListener = new DeviceChange();
        DeviceChange deviceChangeListener1 = new DeviceChange();

        mHdc.addDeviceChangeListener(deviceChangeListener);
        mHdc.addDeviceChangeListener(deviceChangeListener1);
        assertEquals(2, mHdc.getDeviceChangeListener().size());

        mHdc.removeDeviceChangeListener(deviceChangeListener);
        assertEquals(1, mHdc.getDeviceChangeListener().size());
        mHdc.removeDeviceChangeListener(deviceChangeListener1);
        assertEquals(0, mHdc.getDeviceChangeListener().size());
        mHdc.destroyConnect();
    }

    /**
     * test remove different device change listener
     */
    @Test(timeout = 5000)
    public void testRemoveDeviceChangeListener03() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();

        DeviceChange deviceChangeListener = new DeviceChange();
        DeviceChange deviceChangeListener1 = new DeviceChange();
        DeviceChange deviceChangeListener2 = new DeviceChange();

        mHdc.addDeviceChangeListener(deviceChangeListener);
        mHdc.addDeviceChangeListener(deviceChangeListener1);
        mHdc.addDeviceChangeListener(deviceChangeListener2);
        assertEquals(3, mHdc.getDeviceChangeListener().size());

        mHdc.removeDeviceChangeListener(deviceChangeListener1);
        assertEquals(2, mHdc.getDeviceChangeListener().size());

        assertTrue(mHdc.getDeviceChangeListener().contains(deviceChangeListener));
        assertTrue(mHdc.getDeviceChangeListener().contains(deviceChangeListener2));
        mHdc.destroyConnect();
    }

    /**
     * test remove device change listener one by one
     */
    @Test(timeout = 5000)
    public void testRemoveDeviceChangeListener04() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();

        DeviceChange deviceChangeListener = new DeviceChange();
        DeviceChange deviceChangeListener1 = new DeviceChange();

        mHdc.addDeviceChangeListener(deviceChangeListener);
        assertEquals(1, mHdc.getDeviceChangeListener().size());

        mHdc.removeDeviceChangeListener(deviceChangeListener1);
        assertEquals(1, mHdc.getDeviceChangeListener().size());

        DeviceChange deviceChangeListener2 = new DeviceChange();

        assertTrue(mHdc.getDeviceChangeListener().contains(deviceChangeListener));
        assertFalse(mHdc.getDeviceChangeListener().contains(deviceChangeListener1));
        assertFalse(mHdc.getDeviceChangeListener().contains(deviceChangeListener2));
        mHdc.destroyConnect();
    }

    /**
     * test add File Send Recv listener
     */
    @Test(timeout = 5000)
    public void testAddFileSendRecvListener01() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        FileStatus fileStatusListener = new FileStatus();
        mHdc.addFileSendRecvListener(fileStatusListener);
        mHdc.addFileSendRecvListener(fileStatusListener);
        assertEquals(1, mHdc.getFileClientListener().size());
        mHdc.destroyConnect();
    }

    /**
     * test add two File Send Recv listener
     */
    @Test(timeout = 5000)
    public void testAddFileSendRecvListener02() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        FileStatus fileStatusListener = new FileStatus();
        FileStatus fileStatusListener1 = new FileStatus();
        mHdc.addFileSendRecvListener(fileStatusListener);
        mHdc.addFileSendRecvListener(fileStatusListener1);
        assertEquals(2, mHdc.getFileClientListener().size());
        mHdc.destroyConnect();
    }

    /**
     * test add zero File Send Recv listener
     */
    @Test(timeout = 5000)
    public void testAddFileSendRecvListener03() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        assertEquals(0, mHdc.getFileClientListener().size());
        mHdc.destroyConnect();
    }

    /**
     * test add File Send Recv listener clear after hdc destroy
     */
    @Test(timeout = 5000)
    public void testAddFileSendRecvListener04() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        FileStatus fileStatusListener = new FileStatus();
        FileStatus fileStatusListener1 = new FileStatus();
        mHdc.addFileSendRecvListener(fileStatusListener);
        mHdc.addFileSendRecvListener(fileStatusListener1);
        assertEquals(2, mHdc.getFileClientListener().size());
        mHdc.destroyConnect();
        assertEquals(0, mHdc.getFileClientListener().size());
    }

    /**
     * test remove File Send Recv listener
     */
    @Test(timeout = 5000)
    public void testRemoveFileSendRecvListener01() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        FileStatus fileStatusListener = new FileStatus();
        mHdc.addFileSendRecvListener(fileStatusListener);
        assertEquals(1, mHdc.getFileClientListener().size());
        mHdc.removeFileSendRecvListener(fileStatusListener);
        assertEquals(0, mHdc.getFileClientListener().size());
        mHdc.destroyConnect();
    }

    /**
     * test remove two File Send Recv listener
     */
    @Test(timeout = 5000)
    public void testRemoveFileSendRecvListener02() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();

        FileStatus fileStatusListener = new FileStatus();
        FileStatus fileStatusListener1 = new FileStatus();

        mHdc.addFileSendRecvListener(fileStatusListener);
        mHdc.addFileSendRecvListener(fileStatusListener1);
        assertEquals(2, mHdc.getFileClientListener().size());

        mHdc.removeFileSendRecvListener(fileStatusListener);
        assertEquals(1, mHdc.getFileClientListener().size());
        mHdc.removeFileSendRecvListener(fileStatusListener1);
        assertEquals(0, mHdc.getFileClientListener().size());

        mHdc.destroyConnect();
    }

    /**
     * test remove different File Send Recv listener
     */
    @Test(timeout = 5000)
    public void testRemoveFileSendRecvListener03() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();

        FileStatus fileStatusListener = new FileStatus();
        FileStatus fileStatusListener1 = new FileStatus();
        FileStatus fileStatusListener2 = new FileStatus();

        mHdc.addFileSendRecvListener(fileStatusListener);
        mHdc.addFileSendRecvListener(fileStatusListener1);
        mHdc.addFileSendRecvListener(fileStatusListener2);
        assertEquals(3, mHdc.getFileClientListener().size());

        mHdc.removeFileSendRecvListener(fileStatusListener1);
        assertEquals(2, mHdc.getFileClientListener().size());

        assertTrue(mHdc.getFileClientListener().contains(fileStatusListener));
        assertTrue(mHdc.getFileClientListener().contains(fileStatusListener2));
        mHdc.destroyConnect();
    }

    /**
     * test remove File Send Recv one by one
     */
    @Test(timeout = 5000)
    public void testRemoveFileSendRecvListener04() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();

        FileStatus fileStatusListener1 = new FileStatus();
        FileStatus fileStatusListener = new FileStatus();
        mHdc.addFileSendRecvListener(fileStatusListener);
        assertEquals(1, mHdc.getFileClientListener().size());

        mHdc.removeFileSendRecvListener(fileStatusListener1);
        assertEquals(1, mHdc.getFileClientListener().size());

        FileStatus fileStatusListener2 = new FileStatus();

        assertTrue(mHdc.getFileClientListener().contains(fileStatusListener));
        assertFalse(mHdc.getFileClientListener().contains(fileStatusListener1));
        assertFalse(mHdc.getFileClientListener().contains(fileStatusListener2));
        mHdc.destroyConnect();
    }

    /**
     * test add Hilog Recv listener
     */
    @Test(timeout = 5000)
    public void testAddHilogRecvListener01() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        HilogStatus hilogStatusListener = new HilogStatus();
        mHdc.addHilogRecvListener(hilogStatusListener);
        mHdc.addHilogRecvListener(hilogStatusListener);
        assertEquals(1, mHdc.getHilogClientListener().size());
        mHdc.destroyConnect();
    }

    /**
     * test add two Hilog Recv listener
     */
    @Test(timeout = 5000)
    public void testAddHilogRecvListener02() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        HilogStatus hilogStatusListener = new HilogStatus();
        HilogStatus hilogStatusListener1 = new HilogStatus();
        mHdc.addHilogRecvListener(hilogStatusListener);
        mHdc.addHilogRecvListener(hilogStatusListener1);
        assertEquals(2, mHdc.getHilogClientListener().size());
        mHdc.destroyConnect();
    }

    /**
     * test add zero Hilog Recv listener
     */
    @Test(timeout = 5000)
    public void testAddHilogRecvListener03() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        assertEquals(0, mHdc.getHilogClientListener().size());
        mHdc.destroyConnect();
    }

    /**
     * test add Hilog Recv listener clear after hdc destroy
     */
    @Test(timeout = 5000)
    public void testAddHilogRecvListener04() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        HilogStatus hilogStatusListener = new HilogStatus();
        HilogStatus hilogStatusListener1 = new HilogStatus();
        mHdc.addHilogRecvListener(hilogStatusListener);
        mHdc.addHilogRecvListener(hilogStatusListener1);
        assertEquals(2, mHdc.getHilogClientListener().size());
        mHdc.destroyConnect();
        assertEquals(0, mHdc.getHilogClientListener().size());
    }

    /**
     * test remove Hilog Recv listener
     */
    @Test(timeout = 5000)
    public void testRemoveHilogRecvListener01() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        HilogStatus hilogStatusListener = new HilogStatus();
        mHdc.addHilogRecvListener(hilogStatusListener);
        assertEquals(1, mHdc.getHilogClientListener().size());
        mHdc.removeHilogRecvListener(hilogStatusListener);
        assertEquals(0, mHdc.getHilogClientListener().size());
        mHdc.destroyConnect();
    }

    /**
     * test remove two Hilog Recv listener
     */
    @Test(timeout = 5000)
    public void testRemoveHilogRecvListener02() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();

        HilogStatus hilogStatusListener = new HilogStatus();
        HilogStatus hilogStatusListener1 = new HilogStatus();

        mHdc.addHilogRecvListener(hilogStatusListener);
        mHdc.addHilogRecvListener(hilogStatusListener1);
        assertEquals(2, mHdc.getHilogClientListener().size());

        mHdc.removeHilogRecvListener(hilogStatusListener);
        assertEquals(1, mHdc.getHilogClientListener().size());
        mHdc.removeHilogRecvListener(hilogStatusListener1);
        assertEquals(0, mHdc.getHilogClientListener().size());
        mHdc.destroyConnect();
    }

    /**
     * test remove different Hilog Recv listener
     */
    @Test(timeout = 5000)
    public void testRemoveHilogRecvListener03() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();

        HilogStatus hilogStatusListener = new HilogStatus();
        HilogStatus hilogStatusListener1 = new HilogStatus();
        HilogStatus hilogStatusListener2 = new HilogStatus();

        mHdc.addHilogRecvListener(hilogStatusListener);
        mHdc.addHilogRecvListener(hilogStatusListener1);
        mHdc.addHilogRecvListener(hilogStatusListener2);
        assertEquals(3, mHdc.getHilogClientListener().size());

        mHdc.removeHilogRecvListener(hilogStatusListener1);
        assertEquals(2, mHdc.getHilogClientListener().size());

        assertTrue(mHdc.getHilogClientListener().contains(hilogStatusListener));
        assertTrue(mHdc.getHilogClientListener().contains(hilogStatusListener2));
        mHdc.destroyConnect();
    }

    /**
     * test remove Hilog Recv one by one
     */
    @Test(timeout = 5000)
    public void testRemoveHilogRecvListener04() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();

        HilogStatus hilogStatusListener = new HilogStatus();
        HilogStatus hilogStatusListener1 = new HilogStatus();

        mHdc.addHilogRecvListener(hilogStatusListener);
        assertEquals(1, mHdc.getHilogClientListener().size());

        mHdc.removeHilogRecvListener(hilogStatusListener1);
        assertEquals(1, mHdc.getHilogClientListener().size());
        HilogStatus hilogStatusListener2 = new HilogStatus();
        assertTrue(mHdc.getHilogClientListener().contains(hilogStatusListener));
        assertFalse(mHdc.getHilogClientListener().contains(hilogStatusListener1));
        assertFalse(mHdc.getHilogClientListener().contains(hilogStatusListener2));
        mHdc.destroyConnect();
    }

    /**
     * test add Shell Recv listener
     */
    @Test(timeout = 5000)
    public void testAddShellRecvListener01() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        ShellStatus shellStatusListener = new ShellStatus();
        mHdc.addShellRecvListener(shellStatusListener);
        mHdc.addShellRecvListener(shellStatusListener);
        assertEquals(1, mHdc.getShellClientListener().size());
        mHdc.destroyConnect();
    }

    /**
     * test add two Shell Recv listener
     */
    @Test(timeout = 5000)
    public void testAddShellRecvListener02() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        ShellStatus shellStatusListener = new ShellStatus();
        ShellStatus shellStatusListener1 = new ShellStatus();
        mHdc.addShellRecvListener(shellStatusListener);
        mHdc.addShellRecvListener(shellStatusListener1);
        assertEquals(2, mHdc.getShellClientListener().size());
        mHdc.destroyConnect();
    }

    /**
     * test add zero Shell Recv listener
     */
    @Test(timeout = 5000)
    public void testAddShellRecvListener03() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        assertEquals(0, mHdc.getShellClientListener().size());
        mHdc.destroyConnect();
    }

    /**
     * test add Shell Recv listener clear after hdc destroy
     */
    @Test(timeout = 5000)
    public void testAddShellRecvListener04() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        ShellStatus shellStatusListener = new ShellStatus();
        ShellStatus shellStatusListener1 = new ShellStatus();
        mHdc.addShellRecvListener(shellStatusListener);
        mHdc.addShellRecvListener(shellStatusListener1);
        assertEquals(2, mHdc.getShellClientListener().size());
        mHdc.destroyConnect();
        assertEquals(0, mHdc.getShellClientListener().size());
    }

    /**
     * test remove Shell Recv listener
     */
    @Test(timeout = 5000)
    public void testRemoveShellRecvListener01() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();
        ShellStatus shellStatusListener = new ShellStatus();
        mHdc.addShellRecvListener(shellStatusListener);
        assertEquals(1, mHdc.getShellClientListener().size());
        mHdc.removeShellRecvListener(shellStatusListener);
        assertEquals(0, mHdc.getShellClientListener().size());
        mHdc.destroyConnect();
    }

    /**
     * test remove two Shell Recv listener
     */
    @Test(timeout = 5000)
    public void testRemoveShellRecvListener02() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();

        ShellStatus shellStatusListener = new ShellStatus();
        ShellStatus shellStatusListener1 = new ShellStatus();

        mHdc.addShellRecvListener(shellStatusListener);
        mHdc.addShellRecvListener(shellStatusListener1);
        assertEquals(2, mHdc.getShellClientListener().size());

        mHdc.removeShellRecvListener(shellStatusListener);
        assertEquals(1, mHdc.getShellClientListener().size());
        mHdc.removeShellRecvListener(shellStatusListener1);
        assertEquals(0, mHdc.getShellClientListener().size());
        mHdc.destroyConnect();
    }

    /**
     * test remove different Shell Recv listener
     */
    @Test(timeout = 5000)
    public void testRemoveShellRecvListener03() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();

        ShellStatus shellStatusListener = new ShellStatus();
        ShellStatus shellStatusListener1 = new ShellStatus();
        ShellStatus shellStatusListener2 = new ShellStatus();

        mHdc.addShellRecvListener(shellStatusListener);
        mHdc.addShellRecvListener(shellStatusListener1);
        mHdc.addShellRecvListener(shellStatusListener2);
        assertEquals(3, mHdc.getShellClientListener().size());

        mHdc.removeShellRecvListener(shellStatusListener1);
        assertEquals(2, mHdc.getShellClientListener().size());

        assertTrue(mHdc.getShellClientListener().contains(shellStatusListener));
        assertTrue(mHdc.getShellClientListener().contains(shellStatusListener2));
        mHdc.destroyConnect();
    }

    /**
     * test remove Shell Recv one by one
     */
    @Test(timeout = 5000)
    public void testRemoveShellRecvListener04() {
        mHdc = HarmonyDebugConnector.createConnect(mHdcPath, true);
        mHdc.initIfNeeded();

        ShellStatus shellStatusListener = new ShellStatus();
        ShellStatus shellStatusListener1 = new ShellStatus();

        mHdc.addShellRecvListener(shellStatusListener);
        assertEquals(1, mHdc.getShellClientListener().size());

        mHdc.removeShellRecvListener(shellStatusListener1);
        assertEquals(1, mHdc.getShellClientListener().size());
        ShellStatus shellStatusListener2 = new ShellStatus();
        assertTrue(mHdc.getShellClientListener().contains(shellStatusListener));
        assertFalse(mHdc.getShellClientListener().contains(shellStatusListener1));
        assertFalse(mHdc.getShellClientListener().contains(shellStatusListener2));

        mHdc.destroyConnect();
    }

    private class DeviceChange implements IDeviceChangeListener {
        @Override
        public void deviceConnected(Devices device) {
        }

        @Override
        public void deviceDisconnected(Devices device) {
        }

        @Override
        public void deviceChanged(Devices device) {
        }
    }

    private class FileStatus implements IFileClientListener {
        @Override
        public void sendFileResult(String resp) {
        }

        @Override
        public void recvFileResult(String resp) {
        }
    }

    private class ShellStatus implements IShellClientListener {
        @Override
        public void shellRecv(String resp) {
        }
    }

    private class HilogStatus implements IHilogClientListener {
        @Override
        public void hilogRecv(String resp) {
        }
    }
}
