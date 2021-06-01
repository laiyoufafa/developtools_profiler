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

package ohos.devtools.datasources.utils.process.service.entity;

import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 进程实体测试类
 *
 * @version 1.0
 * @date 2021/04/09 15:14
 **/
public class ProcessInfoTest {
    /**
     * ProcessInfo类
     */
    private ProcessInfo processInfo;

    /**
     * functional testing init
     *
     * @tc.name: ProcessInfo init
     * @tc.number: OHOS_JAVA_process_ProcessInfo_init_0001
     * @tc.desc: ProcessInfo init
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Before
    public void initObj() {
        processInfo = new ProcessInfo();
    }

    /**
     * functional testing getDeviceId
     *
     * @tc.name: ProcessInfo getDeviceId
     * @tc.number: OHOS_JAVA_process_ProcessInfo_getDeviceId_0001
     * @tc.desc: ProcessInfo getDeviceId
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void getDeviceIdTest() {
        try {
            if (new ProcessInfo().getDeviceId() != null) {
                Assert.assertTrue(true);
            }
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing getProcessId
     *
     * @tc.name: ProcessInfo getProcessId
     * @tc.number: OHOS_JAVA_process_ProcessInfo_getProcessId_0001
     * @tc.desc: ProcessInfo getProcessId
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void getProcessIdTest() {
        try {
            if (new ProcessInfo().getProcessId() != null) {
                Assert.assertTrue(true);
            }
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing getProcessName
     *
     * @tc.name: ProcessInfo getProcessName
     * @tc.number: OHOS_JAVA_process_ProcessInfo_getProcessName_0001
     * @tc.desc: ProcessInfo getProcessName
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void getProcessNameTest() {
        try {
            if (new ProcessInfo().getProcessName() != null) {
                Assert.assertTrue(true);
            }
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing getState
     *
     * @tc.name: ProcessInfo getState
     * @tc.number: OHOS_JAVA_process_ProcessInfo_getState_0001
     * @tc.desc: ProcessInfo getState
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void getStateTest() {
        try {
            if (new ProcessInfo().getState() != null) {
                Assert.assertTrue(true);
            }
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing getStartTime
     *
     * @tc.name: ProcessInfo getStartTime
     * @tc.number: OHOS_JAVA_process_ProcessInfo_getStartTime_0001
     * @tc.desc: ProcessInfo getStartTime
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void getStartTimeTest() {
        try {
            if (new ProcessInfo().getStartTime() != null) {
                Assert.assertTrue(true);
            }
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing getArch
     *
     * @tc.name: ProcessInfo getArch
     * @tc.number: OHOS_JAVA_process_ProcessInfo_getArch_0001
     * @tc.desc: ProcessInfo getArch
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void getArchTest() {
        try {
            if (new ProcessInfo().getArch() != null) {
                Assert.assertTrue(true);
            }
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing getAgentStatus
     *
     * @tc.name: ProcessInfo getAgentStatus
     * @tc.number: OHOS_JAVA_process_ProcessInfo_getAgentStatus_0001
     * @tc.desc: ProcessInfo getAgentStatus
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void getAgentStatusTest() {
        try {
            if (new ProcessInfo().getAgentStatus() != null) {
                Assert.assertTrue(true);
            }
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * functional testing toString
     *
     * @tc.name: ProcessInfo toString
     * @tc.number: OHOS_JAVA_process_ProcessInfo_toString_0001
     * @tc.desc: ProcessInfo toString
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void toStringTest() {
        try {
            if (new ProcessInfo().toString() != null) {
                Assert.assertTrue(true);
            }
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }
}
