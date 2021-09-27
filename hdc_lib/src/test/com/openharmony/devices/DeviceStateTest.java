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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.openharmony.devices.DeviceState;

/**
 *  Device State UT Test¡¢create at 20210914
 */
public class DeviceStateTest {
    private DeviceState mDeviceStateTest;

    /**
     * setup test env
     *
     * @throws Exception device exception
     */
    @Before
    public void setUp() throws Exception {
        mDeviceStateTest = new DeviceState("aaaa", true);
    }

    /**
     * test DeviceState init
     */
    @Test
    public void testDeviceState01() {
        assertEquals("aaaa", mDeviceStateTest.getSerialNumber());
    }

    /**
     * test DeviceState init with error para
     */
    @Test
    public void testDeviceState02() {
        DeviceState deviceState = new DeviceState(null, false);
        assertEquals("TestModeDeviceID", deviceState.getSerialNumber());
    }

    /**
     * test DeviceState init with error para
     */
    @Test
    public void testDeviceState03() {
        DeviceState deviceState = new DeviceState("", false);
        assertEquals("TestModeDeviceID", deviceState.getSerialNumber());
    }

    /**
     * test DeviceState init with error para
     */
    @Test
    public void testDeviceState04() {
        DeviceState deviceState = new DeviceState("   ", false);
        assertEquals("TestModeDeviceID", deviceState.getSerialNumber());
    }

    /**
     * test DeviceState init with error para
     */
    @Test
    public void testDeviceState05() {
        DeviceState deviceState = new DeviceState("aaaaaa", true);
        assertEquals("aaaaaa", deviceState.getSerialNumber());
    }

    /**
     * test get default device value
     */
    @Test
    public void testGetSerialNumber() {
        assertNotNull(mDeviceStateTest.getSerialNumber());
        assertEquals("aaaa", mDeviceStateTest.getSerialNumber());
    }

    /**
     * test get default device value
     */
    @Test
    public void testGetConnection() {
        assertNotNull(mDeviceStateTest.getConnection());
        assertEquals("USB", mDeviceStateTest.getConnection());
    }

    /**
     * test get default device value
     */
    @Test
    public void testGetStatus() {
        assertNotNull(mDeviceStateTest.getStatus());
        assertEquals("Connected", mDeviceStateTest.getStatus());
    }

    /**
     * test get default device value
     */
    @Test
    public void testGetAddress() {
        assertNotNull(mDeviceStateTest.getAddress());
        assertEquals("localhost", mDeviceStateTest.getAddress());
    }

    /**
     * test get default device value
     */
    @Test
    public void testToString() {
        assertNotNull(mDeviceStateTest.getSerialNumber());
    }
}
