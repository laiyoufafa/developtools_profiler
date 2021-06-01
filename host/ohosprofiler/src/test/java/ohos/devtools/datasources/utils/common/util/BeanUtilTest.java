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

package ohos.devtools.datasources.utils.common.util;

import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * @Description BeanUtilTest
 * @Date 2021/4/8 13:15
 **/
public class BeanUtilTest {
    /**
     * DeviceIPPortInfo
     */
    private DeviceIPPortInfo deviceIPPortInfo;

    /**
     * serializes
     */
    private byte[] serializes;

    /**
     * functional testing init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_common_BeanUtil_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Before
    public void init() {
        deviceIPPortInfo = new DeviceIPPortInfo();
        deviceIPPortInfo.setPort(1);
        deviceIPPortInfo.setIp("");
        deviceIPPortInfo.setDeviceName("");
        deviceIPPortInfo.setDeviceID("");
        deviceIPPortInfo.setDeviceType("");
        serializes = BeanUtil.serialize(deviceIPPortInfo);
    }

    /**
     * functional testing serialize
     *
     * @tc.name: serialize
     * @tc.number: OHOS_JAVA_common_BeanUtil_serialize_0001
     * @tc.desc: serialize
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testserialize() {
        byte[] serialize = BeanUtil.serialize(deviceIPPortInfo);
        Assert.assertNotNull(serialize);
    }

    /**
     * functional testing deserialize
     *
     * @tc.name: deserialize
     * @tc.number: OHOS_JAVA_common_BeanUtil_deserialize_0001
     * @tc.desc: deserialize
     * @tc.type: functional testing
     * @tc.require: AR000FK61M
     */
    @Test
    public void testdeserialize() {
        Object obj = new BeanUtil().deserialize(serializes);
        Assert.assertNotNull(obj);
    }

    /**
     * functional testing getFiledsInfo
     *
     * @tc.name: getFiledsInfo
     * @tc.number: OHOS_JAVA_common_BeanUtil_getFiledsInfo_0001
     * @tc.desc: getFiledsInfo
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgetFiledsInfo() {
        List<Map> fieldsInfo = BeanUtil.getFieldsInfo(deviceIPPortInfo);
        Assert.assertNotNull(fieldsInfo);
    }

    /**
     * functional testing getFileds
     *
     * @tc.name: getFileds
     * @tc.number: OHOS_JAVA_common_BeanUtil_getFileds_0001
     * @tc.desc: getFileds
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgetFiled() {
        List<Map<String, Object>> fields = BeanUtil.getFields(deviceIPPortInfo);
        Assert.assertNotNull(fields);
    }

    /**
     * functional testing getFiledsInfo
     *
     * @tc.name: getFiledsInfo
     * @tc.number: OHOS_JAVA_common_BeanUtil_getFiledsInfo_0001
     * @tc.desc: getFiledsInfo
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgetFiledsInfos() {
        Map<String, Object> fieldsInfoInfos = BeanUtil.getFiledsInfos(deviceIPPortInfo);
        Assert.assertNotNull(fieldsInfoInfos);
    }

    /**
     * functional testing getObjectAttributeNames
     *
     * @tc.name: getObjectAttributeNames
     * @tc.number: OHOS_JAVA_common_BeanUtil_getObjectAttributeNames_0001
     * @tc.desc: getObjectAttributeNames
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgetObjectAttributeNames() {
        List<String> objectAttributeNames = BeanUtil.getObjectAttributeNames(deviceIPPortInfo);
        Assert.assertNotNull(objectAttributeNames);
    }

    /**
     * functional testing getObjectValue
     *
     * @tc.name: getObjectValue
     * @tc.number: OHOS_JAVA_common_BeanUtil_getObjectValue_0001
     * @tc.desc: getObjectValue
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgetObjectValue() {
        List<String> objectValue = BeanUtil.getObjectValue(deviceIPPortInfo);
        Assert.assertNotNull(objectValue);
    }

    /**
     * functional testing getObjectName
     *
     * @tc.name: getObjectName
     * @tc.number: OHOS_JAVA_common_BeanUtil_getObjectName_0001
     * @tc.desc: getObjectName
     * @tc.type: functional testing
     * @tc.require: AR000FK61N
     */
    @Test
    public void testgetObjectName() {
        String objectName = BeanUtil.getObjectName(deviceIPPortInfo);
        Assert.assertNotNull(objectName);
    }

}
