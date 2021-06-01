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

package ohos.devtools.datasources.utils.session.entity;

import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SessionInfoTest测试类
 *
 * @version 1.0
 * @date 2021/04/09 14:06
 **/
public class SessionInfoTest {
    /**
     * 时间戳
     */
    private long longParam = 2006 - 07 - 11;

    /**
     * sessionId数字3243
     */
    private int sessionId = 3243;

    /**
     * DeviceIPPortInfo类
     */
    private DeviceIPPortInfo deviceIPPortInfo;

    /**
     * functional testing init
     *
     * @tc.name: SessionInfo init
     * @tc.number: OHOS_JAVA_session_SessionInfo_init_0001
     * @tc.desc: SessionInfo init
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Before
    public void initObj() {
        deviceIPPortInfo = new DeviceIPPortInfo();
        deviceIPPortInfo.setIp("");
        deviceIPPortInfo.setPort(5001);
    }

    /**
     * functional testing getInstance
     *
     * @tc.name: SessionInfo getDefaultInstance
     * @tc.number: OHOS_JAVA_session_SessionInfo_getDefaultInstance_0001
     * @tc.desc: SessionInfo getDefaultInstance
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void getDefaultInstanceTest() {
        SessionInfo sessionInfo = SessionInfo.getDefaultInstance();
        Assert.assertNotNull(sessionInfo);
    }

    /**
     * functional testing set sessionName
     *
     * @tc.name: SessionInfo sessionName
     * @tc.number: OHOS_JAVA_session_SessionInfo_sessionName_0001
     * @tc.desc: SessionInfo sessionName
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void sessionNameTest() {
        SessionInfo.Builder builder = SessionInfo.builder().sessionName(Object.class.toString());
        Assert.assertNotNull(builder);
    }

    /**
     * functional testing set sessionId
     *
     * @tc.name: SessionInfo sessionId
     * @tc.number: OHOS_JAVA_session_SessionInfo_sessionId_0001
     * @tc.desc: SessionInfo sessionId
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void sessionIdTest() {
        SessionInfo.Builder builder = SessionInfo.builder().sessionId(sessionId);
        Assert.assertNotNull(builder);
    }

    /**
     * functional testing set startTimestamp
     *
     * @tc.name: SessionInfo startTimestamp
     * @tc.number: OHOS_JAVA_session_SessionInfo_startTimestamp_0001
     * @tc.desc: SessionInfo startTimestamp
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void startTimestampTest() {
        SessionInfo.Builder builder = SessionInfo.builder().startTimestamp(longParam);
        Assert.assertNotNull(builder);
    }

    /**
     * functional testing set endTimestamp
     *
     * @tc.name: SessionInfo endTimestamp
     * @tc.number: OHOS_JAVA_session_SessionInfo_endTimestamp_0001
     * @tc.desc: SessionInfo endTimestamp
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void endTimestampTest() {
        SessionInfo.Builder builder = SessionInfo.builder().endTimestamp(longParam);
        Assert.assertNotNull(builder);
    }

    /**
     * functional testing set streamId
     *
     * @tc.name: SessionInfo streamId
     * @tc.number: OHOS_JAVA_session_SessionInfo_streamId_0001
     * @tc.desc: SessionInfo streamId
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void streamIdTest() {
        SessionInfo.Builder builder = SessionInfo.builder().streamId(longParam);
        Assert.assertNotNull(builder);
    }

    /**
     * functional testing set pid
     *
     * @tc.name: SessionInfo pid
     * @tc.number: OHOS_JAVA_session_SessionInfo_pid_0001
     * @tc.desc: SessionInfo pid
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void pidTest() {
        SessionInfo.Builder builder = SessionInfo.builder().pid(longParam);
        Assert.assertNotNull(builder);
    }

    /**
     * functional testing set deviceIPPortInfo
     *
     * @tc.name: SessionInfo deviceIPPortInfo
     * @tc.number: OHOS_JAVA_session_SessionInfo_deviceIPPortInfo_0001
     * @tc.desc: SessionInfo deviceIPPortInfo
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void deviceIPPortInfoTest() {
        SessionInfo.Builder builder = SessionInfo.builder().deviceIPPortInfo(deviceIPPortInfo);
        Assert.assertNotNull(builder);
    }

    /**
     * functional testing build
     *
     * @tc.name: SessionInfo build
     * @tc.number: OHOS_JAVA_session_SessionInfo_build_0001
     * @tc.desc: SessionInfo build
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void buildTest() {
        SessionInfo sessionInfo = SessionInfo.builder().build();
        Assert.assertNotNull(sessionInfo);
    }

    /**
     * functional testing hashCode
     *
     * @tc.name: SessionInfo hashCode
     * @tc.number: OHOS_JAVA_session_SessionInfo_hashCode_0001
     * @tc.desc: SessionInfo hashCode
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void hashCodeTest() {
        int number = SessionInfo.getDefaultInstance().hashCode();
        Assert.assertNotNull(number);
    }

    /**
     * functional testing equals
     *
     * @tc.name: SessionInfo equals
     * @tc.number: OHOS_JAVA_session_SessionInfo_equals_0001
     * @tc.desc: SessionInfo equals
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void equalsTest() {
        boolean flag = SessionInfo.getDefaultInstance().equals(Object.class);
        Assert.assertFalse(flag);
    }

}
