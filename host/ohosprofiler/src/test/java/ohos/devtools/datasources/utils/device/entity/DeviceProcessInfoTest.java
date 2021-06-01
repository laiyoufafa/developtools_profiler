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

package ohos.devtools.datasources.utils.device.entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * @Description DeviceProcessInfoTest
 * @Date 2021/4/3 19:50
 **/
public class DeviceProcessInfoTest {
    /**
     * functional testing init
     *
     * @tc.name: DeviceProcessInfo initialization configuration
     * @tc.number: OHOS_JAVA_device_DeviceProcessInfo_init_0001
     * @tc.desc: DeviceProcessInfo initialization configuration
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-001
     */
    @Test
    public void getTraceFileInfo() {
        DeviceProcessInfo deviceProcessInfo = new DeviceProcessInfo();
        deviceProcessInfo.setDeviceName("test");
        deviceProcessInfo.setDeviceType("test");
        deviceProcessInfo.setProcessName("test");
        deviceProcessInfo.setEndTime(23472L);
        deviceProcessInfo.setLocalSessionId(24367L);
        deviceProcessInfo.setStartTime(24379L);
        deviceProcessInfo.getDeviceName();
        deviceProcessInfo.getDeviceType();
        deviceProcessInfo.getProcessName();
        deviceProcessInfo.getEndTime();
        deviceProcessInfo.getLocalSessionId();
        deviceProcessInfo.getStartTime();
        Assert.assertNotNull(deviceProcessInfo);
    }
}