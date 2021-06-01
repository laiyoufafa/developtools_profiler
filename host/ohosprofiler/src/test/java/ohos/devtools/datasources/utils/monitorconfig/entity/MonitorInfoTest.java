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

package ohos.devtools.datasources.utils.monitorconfig.entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * MonitorInfoTest
 *
 * @version 1.0
 * @date 2021/04/14 10:52
 **/
public class MonitorInfoTest {
    private Long sessionId = 0L;

    /**
     * functional testing set localSessionId
     *
     * @tc.name: MonitorInfo localSessionId
     * @tc.number: OHOS_JAVA_monitor_MonitorInfo_localSessionId_0001
     * @tc.desc: MonitorInfo localSessionId
     * @tc.type: functional testing
     * @tc.require: AR000FK61R
     */
    @Test
    public void localSessionIdTest() {
        MonitorInfo.Builder builder = MonitorInfo.builder().localSessionId(sessionId);
        Assert.assertNotNull(builder);
    }

    /**
     * functional testing set monitorType
     *
     * @tc.name: MonitorInfo monitorType
     * @tc.number: OHOS_JAVA_monitor_MonitorInfo_monitorType_0001
     * @tc.desc: MonitorInfo monitorType
     * @tc.type: functional testing
     * @tc.require: AR000FK61R
     */
    @Test
    public void monitorTypeTest() {
        MonitorInfo.Builder builder = MonitorInfo.builder().monitorType("");
        Assert.assertNotNull(builder);
    }

    /**
     * functional testing set parameter
     *
     * @tc.name: MonitorInfo parameter
     * @tc.number: OHOS_JAVA_monitor_MonitorInfo_parameter_0001
     * @tc.desc: MonitorInfo parameter
     * @tc.type: functional testing
     * @tc.require: AR000FK61R
     */
    @Test
    public void parameterTest() {
        MonitorInfo.Builder builder = MonitorInfo.builder().parameter("");
        Assert.assertNotNull(builder);
    }

    /**
     * functional testing set value
     *
     * @tc.name: MonitorInfo value
     * @tc.number: OHOS_JAVA_monitor_MonitorInfo_value_0001
     * @tc.desc: MonitorInfo value
     * @tc.type: functional testing
     * @tc.require: AR000FK61R
     */
    @Test
    public void valueTest() {
        MonitorInfo.Builder builder = MonitorInfo.builder().value("");
        Assert.assertNotNull(builder);
    }

    /**
     * functional testing set build
     *
     * @tc.name: MonitorInfo build
     * @tc.number: OHOS_JAVA_monitor_MonitorInfo_build_0001
     * @tc.desc: MonitorInfo build
     * @tc.type: functional testing
     * @tc.require: AR000FK61R
     */
    @Test
    public void buildTest() {
        MonitorInfo monitorInfo = MonitorInfo.builder().build();
        Assert.assertNotNull(monitorInfo);
    }

    /**
     * functional testing equals
     *
     * @tc.name: MonitorInfo equals
     * @tc.number: OHOS_JAVA_monitor_MonitorInfo_equals_0001
     * @tc.desc: MonitorInfo equals
     * @tc.type: functional testing
     * @tc.require: AR000FK61R
     */
    @Test
    public void equalsTest() {
        boolean flag = MonitorInfo.builder().build().equals(Object.class);
        Assert.assertFalse(flag);
    }

    /**
     * functional testing toString
     *
     * @tc.name: MonitorInfo toString
     * @tc.number: OHOS_JAVA_monitor_MonitorInfo_toString_0001
     * @tc.desc: MonitorInfo toString
     * @tc.type: functional testing
     * @tc.require: AR000FK61R
     */
    @Test
    public void toStringTest() {
        String str = MonitorInfo.builder().build().toString();
        Assert.assertNotNull(str);
    }

    /**
     * functional testing hashCode
     *
     * @tc.name: MonitorInfo hashCode
     * @tc.number: OHOS_JAVA_monitor_MonitorInfo_hashCode_0001
     * @tc.desc: MonitorInfo hashCode
     * @tc.type: functional testing
     * @tc.require: AR000FK61R
     */
    @Test
    public void hashCodeTest() {
        int number = MonitorInfo.builder().build().hashCode();
        Assert.assertNotNull(number);
    }

}
