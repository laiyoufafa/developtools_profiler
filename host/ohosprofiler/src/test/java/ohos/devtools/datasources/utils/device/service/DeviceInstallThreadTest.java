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
import org.junit.Before;

/**
 * DeviceInstallThreadTest
 *
 * @version 1.0
 * @date 2021/04/09 11:30
 **/
public class DeviceInstallThreadTest {
    /**
     * DeviceIPPortInfo类
     */
    private DeviceIPPortInfo deviceIPPortInfo;

    /**
     * functional testing init
     *
     * @tc.name: DeviceIPPortInfo initialization configuration
     * @tc.number: OHOS_JAVA_device_DeviceIPPortInfo_init_0001
     * @tc.desc: DeviceIPPortInfo initialization configuration
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Before
    public void initObj() {
        deviceIPPortInfo = new DeviceIPPortInfo();
        deviceIPPortInfo.setIp("");
        deviceIPPortInfo.setPort(5001);
    }

}
