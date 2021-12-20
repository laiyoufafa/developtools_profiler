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

import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Device forwarding port class
 */
public class DeviceForwardPortTest {
    private DeviceForwardPort deviceForwardPort;
    private DeviceIPPortInfo deviceIPPortInfo;

    /**
     * functional testing init
     *
     * @tc.name: DeviceForwardPort initialization configuration
     * @tc.number: OHOS_JAVA_device_DeviceForwardPort_init_0001
     * @tc.desc: DeviceForwardPort initialization configuration
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Before
    public void initObj() {
        deviceForwardPort = DeviceForwardPort.getInstance();
        deviceIPPortInfo = new DeviceIPPortInfo();
        deviceIPPortInfo.setIp("");
        deviceIPPortInfo.setPort(5001);
        deviceIPPortInfo.setDeviceID("1");
    }

    /**
     * functional testing getInstance
     *
     * @tc.name: getInstance
     * @tc.number: OHOS_JAVA_device_DeviceForwardPort_getInstance_0001
     * @tc.desc: getInstance
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void getInstanceTest01() {
        DeviceForwardPort deviceForwardPorts = DeviceForwardPort.getInstance();
        Assert.assertNotNull(deviceForwardPorts);
    }

    /**
     * functional testing getInstance
     *
     * @tc.name: getInstance
     * @tc.number: OHOS_JAVA_device_DeviceForwardPort_getInstance_0002
     * @tc.desc: getInstance
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void getInstanceTest02() {
        DeviceForwardPort deviceForwardPorts = DeviceForwardPort.getInstance();
        DeviceForwardPort deviceForward = DeviceForwardPort.getInstance();
        Assert.assertEquals(deviceForward, deviceForwardPorts);
    }

    /**
     * functional testing setDeviceIPPortInfo
     *
     * @tc.name: setDeviceIPPortInfo
     * @tc.number: OHOS_JAVA_device_DeviceForwardPort_setDeviceIPPortInfo_0001
     * @tc.desc: setDeviceIPPortInfo
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void setDeviceIPPortInfoTest01() {
        int deviceIPPortInfoNew = deviceForwardPort.forwardDevicePort(deviceIPPortInfo);
        Assert.assertNotNull(deviceIPPortInfoNew);
    }

    /**
     * functional testing setDeviceIPPortInfo
     *
     * @tc.name: setDeviceIPPortInfo
     * @tc.number: OHOS_JAVA_device_DeviceForwardPort_setDeviceIPPortInfo_0002
     * @tc.desc: setDeviceIPPortInfo
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void setDeviceIPPortInfoTest02() {
        int deviceIPPortInfoNew = deviceForwardPort.forwardDevicePort(deviceIPPortInfo);
        Assert.assertNotEquals(5001, deviceIPPortInfoNew);
    }

    /**
     * functional testing configuration
     *
     * @tc.name: DeviceForwardPort initialization configuration
     * @tc.number: OHOS_JAVA_device_DeviceForwardPort_getForwardPort_0001
     * @tc.desc: DeviceForwardPort initialization configuration
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void getForwardPortTest() {
        int anInt = deviceForwardPort.getForwardPort();
        Assert.assertNotNull(anInt);
    }
}
