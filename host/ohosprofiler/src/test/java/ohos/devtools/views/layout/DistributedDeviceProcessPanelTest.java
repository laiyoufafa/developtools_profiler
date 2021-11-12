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

package ohos.devtools.views.layout;

import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import org.junit.Assert;
import org.junit.Test;

import java.util.Vector;

/**
 * DistributedDeviceProcessPanelTest
 *
 * @since 2021/2/1 9:31
 */
public class DistributedDeviceProcessPanelTest {
    /**
     * get Instance getDistributedConfigPanel
     *
     * @tc.name: refreshDeviceItemTest
     * @tc.number: OHOS_JAVA_layout_DistributedDeviceProcessPanel_refreshDeviceItemTest_0001
     * @tc.desc: refreshDeviceItemTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void refreshDeviceItemTest01() {
        Vector<DeviceIPPortInfo> deviceIPPortInfos = new Vector<>();
        DeviceIPPortInfo deviceIPPortInfo = new DeviceIPPortInfo();
        deviceIPPortInfo.setPort(1);
        deviceIPPortInfo.setIp("");
        deviceIPPortInfo.setDeviceName("");
        deviceIPPortInfo.setDeviceID("");
        deviceIPPortInfo.setDeviceType(DeviceType.FULL_HOS_DEVICE);
        deviceIPPortInfos.add(deviceIPPortInfo);
        new DistributedDeviceProcessPanel(10).refreshDeviceItem(deviceIPPortInfos);
        Assert.assertTrue(true);
    }

    /**
     * get Instance getVectorTest
     *
     * @tc.name: getVectorTest
     * @tc.number: OHOS_JAVA_layout_DistributedDeviceProcessPanel_getVectorTest_0001
     * @tc.desc: getVectorTest
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void getVectorTest01() {
        Vector<DeviceIPPortInfo> list = new DistributedDeviceProcessPanel(10).getVector();
        int num = list.size();
        Assert.assertEquals(0, num);
    }
}
