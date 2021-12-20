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

package ohos.devtools.services.hiperf;

import ohos.devtools.datasources.utils.monitorconfig.entity.ConfigInfo;
import ohos.devtools.datasources.utils.monitorconfig.entity.PerfConfig;
import ohos.devtools.datasources.utils.session.entity.SessionInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * HiPerf Command Test
 *
 * @since 2021/2/1 9:31
 */
public class HiPerfCommandTest {
    private SessionInfo sessionInfo;
    private HiPerfCommand hiPerfCommand;

    /**
     * functional testing init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_perf_HiPerfCommandTest_init
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Before
    public void init() {
        sessionInfo = SessionInfo.builder().sessionId(1).sessionName("Test").pid(2).processName("processName").build();
    }

    /**
     * hiPerf Command Test
     *
     * @tc.name: hiPerfCommandTest01
     * @tc.number: OHOS_JAVA_perf_HiPerfCommandTest_hiPerfCommandTest01
     * @tc.desc: hiPerf Command Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void hiPerfCommandTest01() {
        String fileStorePath = this.getClass().getResource("/Demo.hprof").toString();
        hiPerfCommand = new HiPerfCommand(1, false, "1", fileStorePath);
        Assert.assertNotNull(hiPerfCommand);
    }

    /**
     * hiPerf Command Test
     *
     * @tc.name: hiPerfCommandTest02
     * @tc.number: OHOS_JAVA_perf_HiPerfCommandTest_hiPerfCommandTest02
     * @tc.desc: hiPerf Command Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void hiPerfCommandTest02() {
        String fileStorePath = this.getClass().getResource("/Demo.hprof").toString();
        hiPerfCommand = new HiPerfCommand(0, false, "1", fileStorePath);
        Assert.assertNotNull(hiPerfCommand);
    }

    /**
     * hiPerf Command Test
     *
     * @tc.name: hiPerfCommandTest03
     * @tc.number: OHOS_JAVA_perf_HiPerfCommandTest_hiPerfCommandTest03
     * @tc.desc: hiPerf Command Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void hiPerfCommandTest03() {
        String fileStorePath = this.getClass().getResource("/Demo.hprof").toString();
        hiPerfCommand = new HiPerfCommand(0, false, null, fileStorePath);
        Assert.assertNotNull(hiPerfCommand);
    }

    /**
     * hiPerf Command Test
     *
     * @tc.name: hiPerfCommandTest04
     * @tc.number: OHOS_JAVA_perf_HiPerfCommandTest_hiPerfCommandTest04
     * @tc.desc: hiPerf Command Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void hiPerfCommandTest04() {
        hiPerfCommand = new HiPerfCommand(0, false, "1", null);
        Assert.assertNotNull(hiPerfCommand);
    }

    /**
     * hiPerf Command Test
     *
     * @tc.name: hiPerfCommandTest05
     * @tc.number: OHOS_JAVA_perf_HiPerfCommandTest_hiPerfCommandTest05
     * @tc.desc: hiPerf Command Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void hiPerfCommandTest05() {
        hiPerfCommand = new HiPerfCommand(0, false, null, null);
        Assert.assertNotNull(hiPerfCommand);
    }

    /**
     * execute Report Test
     *
     * @tc.name: executeRecordTest01
     * @tc.number: OHOS_JAVA_perf_HiPerfCommandTest_executeReportTest01
     * @tc.desc: execute Report Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void executeRecordTest01() {
        SessionManager.getInstance().getProfilingSessions().put(1L, sessionInfo);
        String fileStorePath = this.getClass().getResource("/PerfDemo.trace").toString();
        hiPerfCommand = new HiPerfCommand(1, false, "1", fileStorePath);
        PerfConfig perfConfig = ConfigInfo.getInstance().getPerfConfig(true);
        hiPerfCommand.executeRecord(perfConfig);
        Assert.assertTrue(true);
    }

    /**
     * execute Report Test
     *
     * @tc.name: executeRecordTest02
     * @tc.number: OHOS_JAVA_perf_HiPerfCommandTest_executeReportTest02
     * @tc.desc: execute Report Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void executeRecordTest02() {
        SessionManager.getInstance().getProfilingSessions().put(1L, sessionInfo);
        hiPerfCommand = new HiPerfCommand(1, false, "1", null);
        PerfConfig perfConfig = ConfigInfo.getInstance().getPerfConfig(false);
        hiPerfCommand.executeRecord(perfConfig);
        Assert.assertTrue(true);
    }

    /**
     * execute Report Test
     *
     * @tc.name: executeRecordTest03
     * @tc.number: OHOS_JAVA_perf_HiPerfCommandTest_executeReportTest03
     * @tc.desc: execute Report Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void executeRecordTest03() {
        SessionManager.getInstance().getProfilingSessions().put(1L, sessionInfo);
        String fileStorePath = this.getClass().getResource("/PerfDemo.trace").toString();
        hiPerfCommand = new HiPerfCommand(1, true, "1", fileStorePath);
        PerfConfig perfConfig = ConfigInfo.getInstance().getPerfConfig(false);
        hiPerfCommand.executeRecord(perfConfig);
        Assert.assertTrue(true);
    }

    /**
     * execute Report Test
     *
     * @tc.name: executeRecordTest04
     * @tc.number: OHOS_JAVA_perf_HiPerfCommandTest_executeReportTest04
     * @tc.desc: execute Report Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void executeRecordTest04() {
        SessionManager.getInstance().getProfilingSessions().put(1L, sessionInfo);
        String fileStorePath = this.getClass().getResource("/PerfDemo.trace").toString();
        hiPerfCommand = new HiPerfCommand(1, false, "2", fileStorePath);
        PerfConfig perfConfig = ConfigInfo.getInstance().getPerfConfig(false);
        hiPerfCommand.executeRecord(perfConfig);
        Assert.assertTrue(true);
    }

    /**
     * execute Report Test
     *
     * @tc.name: executeRecordTest05
     * @tc.number: OHOS_JAVA_perf_HiPerfCommandTest_executeReportTest05
     * @tc.desc: execute Report Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void executeRecordTest05() {
        SessionManager.getInstance().getProfilingSessions().put(1L, sessionInfo);
        String fileStorePath = this.getClass().getResource("/Demo.hprof").toString();
        hiPerfCommand = new HiPerfCommand(1, false, "2", fileStorePath);
        PerfConfig perfConfig = ConfigInfo.getInstance().getPerfConfig(false);
        hiPerfCommand.executeRecord(perfConfig);
        Assert.assertTrue(true);
    }

    /**
     * stop Record Test
     *
     * @tc.name: stopRecordTest
     * @tc.number: OHOS_JAVA_perf_HiPerfCommandTest_stopRecordTest
     * @tc.desc: stop Record Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void stopRecordTest() {
        String fileStorePath = this.getClass().getResource("/Demo.hprof").toString();
        hiPerfCommand = new HiPerfCommand(1, false, "2", fileStorePath);
        hiPerfCommand.stopRecord();
    }

    /**
     * execute Report Test
     *
     * @tc.name: executeReportTest
     * @tc.number: OHOS_JAVA_perf_HiPerfCommandTest_executeReportTest
     * @tc.desc: execute Report Test
     * @tc.type: functional testing
     * @tc.require: SR-010
     */
    @Test
    public void executeReportTest() {
        String fileStorePath = this.getClass().getResource("/Demo.hprof").toString();
        hiPerfCommand = new HiPerfCommand(1, false, "2", fileStorePath);
        hiPerfCommand.executeReport();
    }
}