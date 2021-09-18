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

package ohos.devtools.datasources.utils.session.service;

import com.alibaba.fastjson.JSONObject;
import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.grpc.util.MutableHandlerRegistry;
import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.transport.grpc.HiProfilerClient;
import ohos.devtools.datasources.transport.grpc.MockProfilerServiceImplBase;
import ohos.devtools.datasources.transport.grpc.ProfilerClient;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.MemoryPluginResult;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceProcessInfo;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.services.memory.agentbean.MemoryHeapInfo;
import ohos.devtools.services.memory.agentdao.MemoryHeapManager;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.TaskScenePanelChart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JProgressBar;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Session Manager Test
 */
public class SessionManagerTest {
    private static volatile Integer requestId = 1;
    private final MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();
    private final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
    private SessionManager session;
    private DeviceIPPortInfo device;
    private ProcessInfo process;
    private JSONObject jsonObject;
    private DeviceProcessInfo deviceProcessInfo;
    private String serverName;
    private MemoryHeapInfo memoryHeapInfo;
    private MemoryHeapManager memoryHeapManager;
    private MockProfilerServiceImplBase getFeatureImpl;
    private ManagedChannel channel;

    /**
     * functional testing init
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_session_SessionManager_init
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Before
    public void init() {
        session = SessionManager.getInstance();
        session.setDevelopMode(true);
        DataBaseApi apo = DataBaseApi.getInstance();
        apo.initDataSourceManager();
        jsonObject = new JSONObject();
        JSONObject memoryObject = new JSONObject();
        memoryObject.put("Select All", true);
        memoryObject.put("Java", true);
        memoryObject.put("Native", true);
        memoryObject.put("Graphics", true);
        memoryObject.put("Stack", true);
        memoryObject.put("Code", true);
        memoryObject.put("Others", true);
        jsonObject.put("Memory", memoryObject);
        device = new DeviceIPPortInfo();
        device.setIp("");
        device.setPort(3333);
        device.setForwardPort(3333);
        device.setDeviceID("1");
        process = new ProcessInfo();
        process.setProcessId(1);
        process.setProcessName("process");
        deviceProcessInfo = new DeviceProcessInfo();
        serverName = InProcessServerBuilder.generateName();
        memoryHeapManager = new MemoryHeapManager();
        memoryHeapInfo = new MemoryHeapInfo();
        memoryHeapInfo.setcId(1);
        memoryHeapInfo.setHeapId(1);
        memoryHeapInfo.setSessionId(1L);
        memoryHeapInfo.setAllocations(10);
        memoryHeapInfo.setDeallocations(0);
        memoryHeapInfo.setTotalCount(79);
        memoryHeapInfo.setShallowSize(348L);
        memoryHeapInfo.setCreateTime(20210406L);
        try {
            grpcCleanup.register(
                InProcessServerBuilder.forName(serverName).fallbackHandlerRegistry(serviceRegistry).directExecutor()
                    .build().start());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        createServer();
        channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService((BindableService) getFeatureImpl);
    }

    private void createServer () {
        getFeatureImpl = new MockProfilerServiceImplBase() {
            /**
             * init getCapabilities
             *
             * @param request request
             * @param responseObserver responseObserver
             */
            @Override
            public void getCapabilities(ProfilerServiceTypes.GetCapabilitiesRequest request,
                StreamObserver<ProfilerServiceTypes.GetCapabilitiesResponse> responseObserver) {
                ProfilerServiceTypes.ProfilerPluginCapability pluginCapability =
                    ProfilerServiceTypes.ProfilerPluginCapability.newBuilder(
                        ProfilerServiceTypes.ProfilerPluginCapability.newBuilder()
                            .setName("/data/local/tmp/libmemdataplugin.z.so")
                            .setPath("/data/local/tmp/libmemdataplugin.z.so").build()).build();
                ProfilerServiceTypes.GetCapabilitiesResponse reply =
                    ProfilerServiceTypes.GetCapabilitiesResponse.newBuilder().addCapabilities(pluginCapability)
                        .setStatus(0).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }

            /**
             * init createSession
             *
             * @param request request
             * @param responseObserver responseObserver
             */
            @Override
            public void createSession(ProfilerServiceTypes.CreateSessionRequest request,
                StreamObserver<ProfilerServiceTypes.CreateSessionResponse> responseObserver) {
                ProfilerServiceTypes.CreateSessionResponse reply =
                    ProfilerServiceTypes.CreateSessionResponse.newBuilder().setSessionId(1).setStatus(0).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }

            /**
             * init startSession
             *
             * @param request request
             * @param responseObserver responseObserver
             */
            @Override
            public void startSession(ProfilerServiceTypes.StartSessionRequest request,
                StreamObserver<ProfilerServiceTypes.StartSessionResponse> responseObserver) {
                CommonTypes.ProfilerPluginState profilerPluginState =
                    CommonTypes.ProfilerPluginState.newBuilder().build();
                ProfilerServiceTypes.StartSessionResponse reply =
                    ProfilerServiceTypes.StartSessionResponse.newBuilder().setStatus(0)
                        .addPluginStatus(profilerPluginState).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }

            /**
             * init fetchData
             *
             * @param request request
             * @param responseObserver responseObserver
             */
            @Override
            public void fetchData(ProfilerServiceTypes.FetchDataRequest request,
                StreamObserver<ProfilerServiceTypes.FetchDataResponse> responseObserver) {
                MemoryPluginResult.AppSummary sss =
                    MemoryPluginResult.AppSummary.newBuilder().setJavaHeap(getIntData()).setNativeHeap(getIntData())
                        .setCode(getIntData()).setStack(getIntData()).setGraphics(getIntData())
                        .setPrivateOther(getIntData()).setSystem(0).build();
                MemoryPluginResult.ProcessMemoryInfo processesInfoZero =
                    MemoryPluginResult.ProcessMemoryInfo.newBuilder().setPid(31141).setName("rcu_gp").setRssShmemKb(1)
                        .setMemsummary(sss).build();
                MemoryPluginResult.ProcessMemoryInfo processesInfoOne =
                    MemoryPluginResult.ProcessMemoryInfo.newBuilder().setPid(31142).setName("rcu_bh")
                        .setRssShmemKb(2222222).build();
                MemoryPluginResult.ProcessMemoryInfo processesInfoTwo =
                    MemoryPluginResult.ProcessMemoryInfo.newBuilder().setPid(31144).setName("netns")
                        .setRssShmemKb(3333333).build();
                MemoryPluginResult.MemoryData aaa =
                    MemoryPluginResult.MemoryData.newBuilder().addProcessesinfo(processesInfoZero)
                        .addProcessesinfo(processesInfoOne).addProcessesinfo(processesInfoTwo).build();
                CommonTypes.ProfilerPluginData data =
                    CommonTypes.ProfilerPluginData.newBuilder().setName("memory-plugin").setStatus(0)
                        .setData(aaa.toByteString()).build();
                ProfilerServiceTypes.FetchDataResponse fetchDataResponse =
                    ProfilerServiceTypes.FetchDataResponse.newBuilder().setResponseId(123456789).setStatus(0)
                        .setHasMore(false).addPluginData(data).build();
                responseObserver.onNext(fetchDataResponse);
                responseObserver.onCompleted();
            }

            /**
             * init stopSession
             *
             * @param request request
             * @param responseObserver responseObserver
             */
            @Override
            public void stopSession(ProfilerServiceTypes.StopSessionRequest request,
                StreamObserver<ProfilerServiceTypes.StopSessionResponse> responseObserver) {
                ProfilerServiceTypes.StopSessionResponse reply =
                    ProfilerServiceTypes.StopSessionResponse.newBuilder().build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }

            /**
             * init destroySession
             *
             * @param request request
             * @param responseObserver responseObserver
             */
            @Override
            public void destroySession(ProfilerServiceTypes.DestroySessionRequest request,
                StreamObserver<ProfilerServiceTypes.DestroySessionResponse> responseObserver) {
                ProfilerServiceTypes.DestroySessionResponse reply =
                    ProfilerServiceTypes.DestroySessionResponse.newBuilder().setStatus(0).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }

        };
    }
    /**
     * functional testing Instance
     *
     * @tc.name: get Instance
     * @tc.number: OHOS_JAVA_session_SessionManager_getInstance_0001
     * @tc.desc: get Instance
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void getInstance() {
        SessionManager sessionInstance = SessionManager.getInstance();
        Assert.assertNotNull(sessionInstance);
    }

    /**
     * functional testing create Session
     *
     * @tc.name: create Session
     * @tc.number: OHOS_JAVA_session_SessionManager_createSession_0001
     * @tc.desc: create Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void testCreateSessionRealTime01() {
        long num = session.createSession(null, process);
        Assert.assertNotNull(num);
    }

    /**
     * functional testing create Session
     *
     * @tc.name: create Session
     * @tc.number: OHOS_JAVA_session_SessionManager_createSession_0002
     * @tc.desc: create Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void testCreateSessionRealTime02() {
        long num = session.createSession(device, null);
        Assert.assertEquals(-1L, num);
    }

    /**
     * functional testing create Session
     *
     * @tc.name: create Session
     * @tc.number: OHOS_JAVA_session_SessionManager_createSession_0003
     * @tc.desc: create Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void testCreateSessionRealTime03() {
        long num = session.createSession(device, process);
        Assert.assertNotNull(num);
    }

    /**
     * functional testing create Session
     *
     * @tc.name: create Session
     * @tc.number: OHOS_JAVA_session_SessionManager_createSession_0004
     * @tc.desc: create Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void testCreateSession04() {
        long num = session.createSession(null, null);
        Assert.assertEquals(-1L, num);
    }

    /**
     * functional testing create Session
     *
     * @tc.name: create Session
     * @tc.number: OHOS_JAVA_session_SessionManager_createSession_0005
     * @tc.desc: create Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void testCreateSession05() {
        long num = session.createSession(null, process);
        Assert.assertEquals(num, -1L);
    }

    /**
     * functional testing start Session
     *
     * @tc.name: start Session
     * @tc.number: OHOS_JAVA_session_SessionManager_startSession_0001
     * @tc.desc: start Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void testStart01() {
        boolean flag = session.startSession(null, false);
        Assert.assertFalse(flag);
    }

    /**
     * functional testing start Session
     *
     * @tc.name: start Session
     * @tc.number: OHOS_JAVA_session_SessionManager_startSession_0002
     * @tc.desc: start Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void testStart02() {
        boolean flag = session.startSession(null, true);
        Assert.assertFalse(flag);
    }

    /**
     * functional testing start Session
     *
     * @tc.name: start Session
     * @tc.number: OHOS_JAVA_session_SessionManager_startSession_0003
     * @tc.desc: start Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void testStart03() {
        boolean flag = session.startSession(2L, false);
        Assert.assertTrue(flag);
    }

    /**
     * functional testing start Session
     *
     * @tc.name: start Session
     * @tc.number: OHOS_JAVA_session_SessionManager_startSession_0004
     * @tc.desc: start Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void testStart04() {
        boolean flag = session.startSession(2L, true);
        Assert.assertTrue(flag);
    }

    /**
     * functional testing start Session
     *
     * @tc.name: start Session
     * @tc.number: OHOS_JAVA_session_SessionManager_startSession_0005
     * @tc.desc: start Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5S7
     */
    @Test
    public void testStart05() {
        long num = session.createSession(device, process);
        boolean flag = session.startSession(num, false);
        Assert.assertTrue(flag);
    }

    /**
     * functional testing fetchData
     *
     * @tc.name: fetchData
     * @tc.number: OHOS_JAVA_session_SessionManager_fetchData_0001
     * @tc.desc: fetchData
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testFetchData01() {
        boolean flag = session.fetchData(null);
        Assert.assertFalse(flag);
    }

    /**
     * functional testing fetchData
     *
     * @tc.name: fetchData
     * @tc.number: OHOS_JAVA_session_SessionManager_fetchData_0002
     * @tc.desc: fetchData
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testFetchData02() {
        long num = session.createSession(null, process);
        session.startSession(num, false);
        boolean flag = session.fetchData(num);
        Assert.assertFalse(flag);
    }

    /**
     * functional testing fetchData
     *
     * @tc.name: fetchData
     * @tc.number: OHOS_JAVA_session_SessionManager_fetchData_0003
     * @tc.desc: fetchData
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testFetchData03() {
        boolean flag = session.fetchData(0L);
        Assert.assertFalse(flag);
    }

    /**
     * functional testing fetchData
     *
     * @tc.name: fetchData
     * @tc.number: OHOS_JAVA_session_SessionManager_fetchData_0004
     * @tc.desc: fetchData
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testFetchData04() {
        boolean flag = session.fetchData(-1L);
        Assert.assertFalse(flag);
    }

    /**
     * functional testing fetchData
     *
     * @tc.name: fetchData
     * @tc.number: OHOS_JAVA_session_SessionManager_fetchData_0005
     * @tc.desc: fetchData
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testFetchData05() {
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient("", 3333, channel);
        long num = session.createSession(device, process);
        session.startSession(num, false);
        boolean flag = session.fetchData(num);
        Assert.assertTrue(flag);
    }

    /**
     * functional testing end Session
     *
     * @tc.name: end Session
     * @tc.number: OHOS_JAVA_session_SessionManager_endSession_0001
     * @tc.desc: end Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5S8
     */
    @Test
    public void testEndSession01() {
        boolean flag = session.endSession(null);
        Assert.assertFalse(flag);
    }

    /**
     * functional testing end Session
     *
     * @tc.name: end Session
     * @tc.number: OHOS_JAVA_session_SessionManager_endSession_0002
     * @tc.desc: end Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5S8
     */
    @Test
    public void testEndSession02() {
        boolean flag = session.endSession(1L);
        Assert.assertTrue(flag);
    }

    /**
     * functional testing end Session
     *
     * @tc.name: end Session
     * @tc.number: OHOS_JAVA_session_SessionManager_endSession_0003
     * @tc.desc: end Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5S8
     */
    @Test
    public void testEndSession03() {
        boolean flag = session.endSession(0L);
        Assert.assertFalse(flag);
    }

    /**
     * functional testing end Session
     *
     * @tc.name: end Session
     * @tc.number: OHOS_JAVA_session_SessionManager_endSession_0004
     * @tc.desc: end Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5S8
     */
    @Test
    public void testEndSession04() {
        boolean flag = session.endSession(-1L);
        Assert.assertFalse(flag);
    }

    /**
     * functional testing end Session
     *
     * @tc.name: end Session
     * @tc.number: OHOS_JAVA_session_SessionManager_endSession_0005
     * @tc.desc: end Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5S8
     */
    @Test
    public void testEndSession05() {
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient("", 3333, channel);
        long num = session.createSession(device, process);
        boolean flag = session.endSession(num);
        Assert.assertTrue(flag);
    }

    /**
     * functional testing delete Session
     *
     * @tc.name: delete Session
     * @tc.number: OHOS_JAVA_session_SessionManager_deleteSession_0001
     * @tc.desc: delete Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5SA
     */
    @Test
    public void testDeleteSession01() {
        boolean flag = session.deleteSession(null);
        Assert.assertFalse(flag);
    }

    /**
     * functional testing delete Session
     *
     * @tc.name: delete Session
     * @tc.number: OHOS_JAVA_session_SessionManager_deleteSession_0002
     * @tc.desc: delete Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5SA
     */
    @Test
    public void testDeleteSession02() {
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient("", 3333, channel);
        long num = session.createSession(device, process);
        ProfilerChartsView view = new ProfilerChartsView(num, true, new TaskScenePanelChart());
        boolean flag = session.deleteSession(num);
        Assert.assertTrue(flag);
    }

    /**
     * functional testing delete Session
     *
     * @tc.name: delete Session
     * @tc.number: OHOS_JAVA_session_SessionManager_deleteSession_0003
     * @tc.desc: delete Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5SA
     */
    @Test
    public void testDeleteSession03() {
        boolean flag = session.deleteSession(0L);
        Assert.assertFalse(flag);
    }

    /**
     * functional testing delete Session
     *
     * @tc.name: delete Session
     * @tc.number: OHOS_JAVA_session_SessionManager_deleteSession_0004
     * @tc.desc: delete Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5SA
     */
    @Test
    public void testDeleteSession04() {
        boolean flag = session.deleteSession(-1L);
        Assert.assertFalse(flag);
    }

    /**
     * functional testing delete Session
     *
     * @tc.name: delete Session
     * @tc.number: OHOS_JAVA_session_SessionManager_deleteSession_0005
     * @tc.desc: delete Session
     * @tc.type: functional testing
     * @tc.require: AR000FK5SA
     */
    @Test
    public void testDeleteSession05() {
        long num = session.createSession(device, process);
        ProfilerChartsView view = new ProfilerChartsView(num, true, new TaskScenePanelChart());
        boolean flag = session.deleteSession(num);
        Assert.assertFalse(flag);
    }

    /**
     * functional testing saveSessionDataToFile
     *
     * @tc.name: saveSessionDataToFile
     * @tc.number: OHOS_JAVA_session_SessionManager_saveSessionDataToFile_0001
     * @tc.desc: save SessionData To File
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testSaveSessionDataToFile01() {
        boolean flag = session.saveSessionDataToFile(0L, deviceProcessInfo, "");
        Assert.assertFalse(flag);
    }

    /**
     * functional testing saveSessionDataToFile
     *
     * @tc.name: saveSessionDataToFile
     * @tc.number: OHOS_JAVA_session_SessionManager_saveSessionDataToFile_0002
     * @tc.desc: save SessionData To File
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testSaveSessionDataToFile02() {
        boolean flag = session.saveSessionDataToFile(-1L, deviceProcessInfo, "");
        Assert.assertFalse(flag);
    }

    /**
     * functional testing saveSessionDataToFile
     *
     * @tc.name: saveSessionDataToFile
     * @tc.number: OHOS_JAVA_session_SessionManager_saveSessionDataToFile_0003
     * @tc.desc: save SessionData To File
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testSaveSessionDataToFile03() {
        boolean flag = session.saveSessionDataToFile(1L, deviceProcessInfo, "");
        Assert.assertFalse(flag);
    }

    /**
     * functional testing saveSessionDataToFile
     *
     * @tc.name: saveSessionDataToFile
     * @tc.number: OHOS_JAVA_session_SessionManager_saveSessionDataToFile_0004
     * @tc.desc: save SessionData To File
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testSaveSessionDataToFile04() {
        boolean flag = session.saveSessionDataToFile(1L, null, "");
        Assert.assertFalse(flag);
    }

    /**
     * functional testing saveSessionDataToFile
     *
     * @tc.name: saveSessionDataToFile
     * @tc.number: OHOS_JAVA_session_SessionManager_saveSessionDataToFile_0005
     * @tc.desc: save SessionData To File
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testSaveSessionDataToFile05() {
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient("", 3333, channel);
        long num = session.createSession(device, process);
        session.fetchData(num);
        boolean flag = session.saveSessionDataToFile(num, deviceProcessInfo, "/");
        Assert.assertFalse(flag);
    }

    /**
     * functional testing saveSessionDataToFile
     *
     * @tc.name: saveSessionDataToFile
     * @tc.number: OHOS_JAVA_session_SessionManager_saveSessionDataToFile_0006
     * @tc.desc: save SessionData To File
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testSaveSessionDataToFile06() {
        boolean flag = session.saveSessionDataToFile(1L, deviceProcessInfo, null);
        Assert.assertFalse(flag);
    }

    /**
     * functional testing localSessionDataFromFile
     *
     * @tc.name: localSessionDataFromFile
     * @tc.number: OHOS_JAVA_session_SessionManager_localSessionDataFromFile_0001
     * @tc.desc: local SessionData From File
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testLocalSessionDataFromFile01() {
        JProgressBar jProgressBar = new JProgressBar();
        Optional<DeviceProcessInfo> optional = session.localSessionDataFromFile(jProgressBar, null);
        Assert.assertFalse(optional.isPresent());
    }

    /**
     * functional testing localSessionDataFromFile
     *
     * @tc.name: localSessionDataFromFile
     * @tc.number: OHOS_JAVA_session_SessionManager_localSessionDataFromFile_0002
     * @tc.desc: local SessionData From File
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testLocalSessionDataFromFile02() {
        File file = new File("");
        Optional<DeviceProcessInfo> optional = session.localSessionDataFromFile(null, file);
        Assert.assertFalse(optional.isPresent());
    }

    /**
     * functional testing localSessionDataFromFile
     *
     * @tc.name: localSessionDataFromFile
     * @tc.number: OHOS_JAVA_session_SessionManager_localSessionDataFromFile_0003
     * @tc.desc: local SessionData From File
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testLocalSessionDataFromFile03() {
        Optional<DeviceProcessInfo> optional = session.localSessionDataFromFile(null, null);
        Assert.assertFalse(optional.isPresent());
    }

    /**
     * functional testing localSessionDataFromFile
     *
     * @tc.name: localSessionDataFromFile
     * @tc.number: OHOS_JAVA_session_SessionManager_localSessionDataFromFile_0004
     * @tc.desc: local SessionData From File
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testLocalSessionDataFromFile05() {
        JProgressBar jProgressBar = new JProgressBar();
        File rootPath = new File(this.getClass().getResource("/Demo.trace").getFile().toString());
        Optional<DeviceProcessInfo> deviceProcessInfoNew = session.localSessionDataFromFile(jProgressBar, rootPath);
        Assert.assertNotNull(deviceProcessInfoNew);
    }

    /**
     * functional testing deleteSessionByOffLineDivece
     *
     * @tc.name: deleteSessionByOffLineDivece
     * @tc.number: OHOS_JAVA_session_SessionManager_deleteSessionByOffLineDivece_0001
     * @tc.desc: delete Session By OffLineDivece
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testDeleteSessionByOffLineDevice01() {
        session.deleteSessionByOffLineDevice(device);
        Assert.assertTrue(true);
    }

    /**
     * functional testing deleteSessionByOffLineDivece
     *
     * @tc.name: deleteSessionByOffLineDivece
     * @tc.number: OHOS_JAVA_session_SessionManager_deleteSessionByOffLineDivece_0002
     * @tc.desc: delete Session By OffLineDivece
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testDeleteSessionByOffLineDevice02() {
        session.deleteSessionByOffLineDevice(null);
        Assert.assertTrue(true);
    }

    /**
     * functional testing deleteLocalSession
     *
     * @tc.name: deleteLocalSession
     * @tc.number: OHOS_JAVA_session_SessionManager_deleteLocalSession_0001
     * @tc.desc: delete LocalSession
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testDeleteLocalSession() {
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient("", 3333, channel);
        long num = session.createSession(device, process);
        session.deleteLocalSession(num);
        Assert.assertTrue(true);
    }

    /**
     * functional testing stop AllSession
     *
     * @tc.name: stop AllSession
     * @tc.number: OHOS_JAVA_session_SessionManager_stopAllSession_0001
     * @tc.desc: stop AllSession
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testStopAllSession() {
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient("", 3333, channel);
        long num = session.createSession(device, process);
        session.startSession(num, false);
        session.stopAllSession();
        Assert.assertTrue(true);
    }

    /**
     * functional testing getPluginPath
     *
     * @tc.name: getPluginPath
     * @tc.number: OHOS_JAVA_session_SessionManager_getPluginPath_0001
     * @tc.desc: getPluginPath
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testGetPluginPath() {
        String pluginPath = session.getPluginPath();
        Assert.assertNotNull(pluginPath);
    }

    /**
     * functional testing getPluginPath
     *
     * @tc.name: getPluginPath
     * @tc.number: OHOS_JAVA_session_SessionManager_getPluginPath_0002
     * @tc.desc: getPluginPath
     * @tc.type: functional testing
     * @tc.require: SR000FK5S6
     */
    @Test
    public void testGetPluginPathFalse() {
        session.setDevelopMode(false);
        String pluginPath = session.getPluginPath();
        Assert.assertNotNull(pluginPath);
    }

    private int getIntData() {
        requestId++;
        if (requestId == Integer.MAX_VALUE) {
            requestId = 0;
        }
        return requestId;
    }
}
