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

package test.com.openharmony.devices;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.openharmony.devices.DeviceMonitor;
import com.openharmony.hdc.HarmonyDebugConnector;

/**
 *  DeviceMonitor UT Test¡¢create at 20210912
 */
public class DeviceMonitorTest {
    private DeviceMonitor mDeviceMonitor;

    /**
     * set up test env
     *
     * @throws Exception device init exception
     */
    @Before
    public void setUp() throws Exception {
        mDeviceMonitor = new DeviceMonitor(HarmonyDebugConnector.createConnect("Test", true), true);
    }

    /**
     * test DeviceMonitor status function
     */
    @Test
    public void testDeviceMonitor01() {
        assertFalse(mDeviceMonitor.isDeviceMonitorStart());
    }

    /**
     * test DeviceMonitor start error status
     */
    @Test
    public void testDeviceMonitor02() {
        DeviceMonitor monitor = new DeviceMonitor(null, false);
        assertFalse(monitor.isDeviceMonitorStart());
    }

    /**
     * test DeviceMonitor start
     */
    @Test
    public void testStart01() {
        mDeviceMonitor.start();
        assertTrue(mDeviceMonitor.isDeviceMonitorStart());
    }

    /**
     * test DeviceMonitor stop
     */
    @Test
    public void testStart02() {
        mDeviceMonitor.stop();
        assertFalse(mDeviceMonitor.isDeviceMonitorStart());
    }

    /**
     * test DeviceMonitor stop twice
     */
    @Test
    public void testStop() {
        mDeviceMonitor.start();
        assertTrue(mDeviceMonitor.isDeviceMonitorStart());
        mDeviceMonitor.stop();
        assertFalse(mDeviceMonitor.isDeviceMonitorStart());
    }

    /**
     * test Get Hdc
     */
    @Test
    public void testGetHdc() {
        assertNull(mDeviceMonitor.getHdc());
    }

    /**
     * test Get Devices
     */
    @Test
    public void testGetDevices() {
        assertTrue(mDeviceMonitor.getDevices().length >= 0);
    }
}
