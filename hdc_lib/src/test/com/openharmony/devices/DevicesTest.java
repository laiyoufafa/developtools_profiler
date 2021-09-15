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

import com.openharmony.devices.DeviceState;
import com.openharmony.devices.Devices;

/**
 * device UT test¡¢create at 20210914
 */
public class DevicesTest {
    private Devices mDeviceTest;
    private DeviceState mDeviceStateTest;

    /**
     * init test env.
     *
     * @throws Exception device state exception
     */
    @Before
    public void setUp() throws Exception {
        mDeviceStateTest = new DeviceState("aaaa", true);
        mDeviceTest = new Devices(mDeviceStateTest.getSerialNumber(), mDeviceStateTest, true);
    }

    /**
     * test init device id
     */
    @Test
    public void testDevices01() {
        assertEquals("aaaa", mDeviceTest.getSerialNumber());
    }

    /**
     * test init device id with error para
     */
    @Test
    public void testDevices02() {
        Devices device = new Devices(null, mDeviceStateTest, false);
        assertEquals("TestModeDeviceID", device.getSerialNumber());
    }

    /**
     * test init device id with error para
     */
    @Test
    public void testDevices03() {
        Devices device = new Devices("", mDeviceStateTest, false);
        assertEquals("TestModeDeviceID", device.getSerialNumber());
    }

    /**
     * test init device id with error para
     */
    @Test
    public void testDevices04() {
        Devices device = new Devices("  ", mDeviceStateTest, false);
        assertEquals("TestModeDeviceID", device.getSerialNumber());
    }

    /**
     * test init device id with correct para
     */
    @Test
    public void testDevices05() {
        Devices device = new Devices("aaaaa", mDeviceStateTest, true);
        assertEquals("aaaaa", device.getSerialNumber());
    }

    /**
     * Test get current client
     */
    @Test
    public void testGetClient() {
        assertNotNull(mDeviceTest.getClient());
    }

    /**
     * tets get device id
     */
    @Test
    public void testGetSerialNumber() {
        assertNotNull(mDeviceTest.getSerialNumber());
        assertEquals(mDeviceStateTest.getSerialNumber(), mDeviceTest.getSerialNumber());
    }

    /**
     * test get device status
     */
    @Test
    public void testIsOnline() {
        assertNotNull(mDeviceTest.isOnline());
        assertTrue(mDeviceTest.isOnline());
    }

    /**
     * test get device base status
     */
    @Test
    public void testGetState() {
        assertNotNull(mDeviceTest.getState());
        assertEquals(mDeviceStateTest, mDeviceTest.getState());
    }

    /**
     * tets get selinux status
     */
    @Test
    public void testGetSelinux() {
        assertNotNull(mDeviceTest.getSelinux());
        assertEquals("Permission", mDeviceTest.getSelinux());
    }

    /**
     * test get build date
     */
    @Test
    public void testGetBuildDate() {
        assertNotNull(mDeviceTest.getBuildDate());
        assertEquals("19700101", mDeviceTest.getBuildDate());
    }

    /**
     * tets get fingerprint
     */
    @Test
    public void testGetFingerprint() {
        assertNotNull(mDeviceTest.getFingerprint());
        assertEquals("v300:10/QP1A.190711.020/ohosbuild05291519:userdebug/test-keys", mDeviceTest.getFingerprint());
    }

    /**
     * tets get Sdk Version
     */
    @Test
    public void testGetSdkVersion() {
        assertNotNull(mDeviceTest.getSdkVersion());
        assertEquals("28", mDeviceTest.getSdkVersion());
    }

    /**
     * tets get Build Type
     */
    @Test
    public void testGetBuildType() {
        assertNotNull(mDeviceTest.getBuildType());
        assertEquals("userdebug", mDeviceTest.getBuildType());
    }

    /**
     * tets get Debuggable status
     */
    @Test
    public void testGetDebuggable() {
        assertNotNull(mDeviceTest.getDebuggable());
        assertEquals("true", mDeviceTest.getDebuggable());
    }

    /**
     * tets get Vndk Version
     */
    @Test
    public void testGetVndkVersion() {
        assertNotNull(mDeviceTest.getVndkVersion());
        assertEquals("28", mDeviceTest.getVndkVersion());
    }

    /**
     * tets get Secure status
     */
    @Test
    public void testGetSecure() {
        assertNotNull(mDeviceTest.getSecure());
        assertEquals("1", mDeviceTest.getSecure());
    }

    /**
     * tets get Security Patch
     */
    @Test
    public void testGetSecurityPatch() {
        assertNotNull(mDeviceTest.getSecurityPatch());
        assertEquals("19700101", mDeviceTest.getSecurityPatch());
    }

    /**
     * tets get device Name
     */
    @Test
    public void testGetProductName() {
        assertNotNull(mDeviceTest.getName());
        assertEquals("OHOS", mDeviceTest.getName());
    }

    /**
     * tets get Product Brand
     */
    @Test
    public void testGetProductBrand() {
        assertNotNull(mDeviceTest.getProductBrand());
        assertEquals("OHOS", mDeviceTest.getProductBrand());
    }

    /**
     * tets get Product Board
     */
    @Test
    public void testGetProductBoard() {
        assertNotNull(mDeviceTest.getProductBoard());
        assertEquals("OHOS", mDeviceTest.getProductBoard());
    }

    /**
     * tets get Product Model
     */
    @Test
    public void testGetProductModel() {
        assertNotNull(mDeviceTest.getProductModel());
        assertEquals("OHOS", mDeviceTest.getProductModel());
    }

    /**
     * tets get Product Device
     */
    @Test
    public void testGetProductDevice() {
        assertNotNull(mDeviceTest.getProductDevice());
        assertEquals("OHOS", mDeviceTest.getProductDevice());
    }
}
