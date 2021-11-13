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

package ohos.devtools.datasources.utils.process.service;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.grpc.util.MutableHandlerRegistry;
import ohos.devtools.datasources.databases.databaseapi.DataBaseApi;
import ohos.devtools.datasources.transport.grpc.MockProfilerServiceImplBase;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.MemoryPluginResult;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.utils.device.dao.DeviceDao;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * process Unit Test
 *
 * @since 2021/2/1 9:31
 */
public class ProcessManagerTest {
    private static volatile Integer requestId = 1;

    private ProcessInfo processInfo;
    private String processName;
    private DeviceIPPortInfo deviceIPPortInfo;
    private String deviceId;
    private String serverName;
    private String IP;
    private int firstPort;
    private MockProfilerServiceImplBase getFeatureImpl;
    private ManagedChannel channel;
    private final MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();
    private final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    /**
     * functional testing init
     *
     * @tc.name: ProcessManager init
     * @tc.number: OHOS_JAVA_process_ProcessManager_init_0001
     * @tc.desc: ProcessManager init
     * @tc.type: functional testing
     * @tc.require: SR-004
     * @throws IOException throw IOException
     */
    @Before
    public void initObj() throws IOException {
        // 应用初始化 Step1 初始化数据中心
        ProfilerLogManager.getSingleton().updateLogLevel(Level.ERROR);
        DataBaseApi apo = DataBaseApi.getInstance();
        apo.initDataSourceManager();
        deviceIPPortInfo = new DeviceIPPortInfo();
        deviceIPPortInfo.setIp("");
        deviceIPPortInfo.setDeviceID("1");
        deviceIPPortInfo.setPort(5001);
        deviceIPPortInfo.setForwardPort(5001);
        deviceIPPortInfo.setDeviceName("");
        deviceIPPortInfo.setDeviceType(DeviceType.FULL_HOS_DEVICE);
        deviceId = "1";
        processInfo = new ProcessInfo();
        processInfo.setDeviceId("1");
        processInfo.setProcessId(1);
        processInfo.setProcessName("com.go.maps");
        processName = "goo";
        IP = "";
        firstPort = 5001;
        serverName = InProcessServerBuilder.generateName();
        getFeatureImpl = new ProcessMock();
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).fallbackHandlerRegistry(serviceRegistry).directExecutor().build()
                .start());
        channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
    }

    private class ProcessMock extends MockProfilerServiceImplBase {
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
                    ProfilerServiceTypes.ProfilerPluginCapability.newBuilder().setName("test0")
                        .setPath("/data/local/tmp/libmemdata.z.so").build()).build();
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
    }

    /**
     * functional testing getInstance
     *
     * @tc.name: ProcessManager getInstance
     * @tc.number: OHOS_JAVA_process_ProcessManager_getInstance_0001
     * @tc.desc: ProcessManager getInstance
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void getProcessManager01() {
        ProcessManager processManager = ProcessManager.getInstance();
        Assert.assertNotNull(processManager);
    }

    /**
     * functional testing getInstance
     *
     * @tc.name: ProcessManager getInstance
     * @tc.number: OHOS_JAVA_process_ProcessManager_getInstance_0002
     * @tc.desc: ProcessManager getInstance
     * @tc.type: functional testing
     * @tc.require: SR-004
     */
    @Test
    public void getProcessManager02() {
        ProcessManager processManagerOne = ProcessManager.getInstance();
        ProcessManager processManagerTwo = ProcessManager.getInstance();
        Assert.assertEquals(processManagerOne, processManagerTwo);
    }

    /**
     * functional testing getProcessList
     *
     * @tc.name: ProcessManager getProcessList
     * @tc.number: OHOS_JAVA_process_ProcessManager_getProcessList_0001
     * @tc.desc: ProcessManager getProcessList
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-003
     */
    @Test
    public void getProcessList() {
        List<ProcessInfo> processList = ProcessManager.getInstance().getProcessList(null);
        int size = processList.size();
        Assert.assertEquals(0, size);
    }

    /**
     * functional testing getProcessList
     *
     * @tc.name: ProcessManager getProcessList
     * @tc.number: OHOS_JAVA_process_ProcessManager_getProcessList_0002
     * @tc.desc: ProcessManager getProcessList
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-003
     */
    @Test
    public void getProcessList1() {
        List<ProcessInfo> processList = ProcessManager.getInstance().getProcessList(deviceIPPortInfo);
        int size = processList.size();
        Assert.assertEquals(0, size);
    }

    /**
     * functional testing getProcessList
     *
     * @tc.name: ProcessManager getProcessList
     * @tc.number: OHOS_JAVA_process_ProcessManager_getProcessList_0002
     * @tc.desc: ProcessManager getProcessList
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-003
     */
    @Test
    public void getProcessList2() {
        deviceIPPortInfo.setIp("10");
        deviceIPPortInfo.setPort(Integer.MAX_VALUE);
        new DeviceDao().insertDeviceIPPortInfo(deviceIPPortInfo);
        List<ProcessInfo> processList1 = ProcessManager.getInstance().getProcessList(deviceIPPortInfo);
        List<ProcessInfo> processList2 = ProcessManager.getInstance().getProcessList(null);
        Assert.assertEquals(processList1, processList2);
    }

    /**
     * functional testing getProcessList
     *
     * @tc.name: ProcessManager getProcessList
     * @tc.number: OHOS_JAVA_process_ProcessManager_getProcessList_0003
     * @tc.desc: ProcessManager getProcessList
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-003
     */
    @Test
    public void getProcessList3() {
        deviceIPPortInfo.setIp("10");
        deviceIPPortInfo.setPort(-1);
        new DeviceDao().insertDeviceIPPortInfo(deviceIPPortInfo);
        List<ProcessInfo> processList = ProcessManager.getInstance().getProcessList(deviceIPPortInfo);
        int size = processList.size();
        Assert.assertEquals(0, size);
    }

    /**
     * functional testing getProcessList
     *
     * @tc.name: ProcessManager getProcessList
     * @tc.number: OHOS_JAVA_process_ProcessManager_getProcessList_0004
     * @tc.desc: ProcessManager getProcessList
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-003
     */
    @Test
    public void getProcessList4() {
        deviceIPPortInfo.setIp("10");
        deviceIPPortInfo.setDeviceID("");
        new DeviceDao().insertDeviceIPPortInfo(deviceIPPortInfo);
        List<ProcessInfo> processList = ProcessManager.getInstance().getProcessList(deviceIPPortInfo);
        int size = processList.size();
        Assert.assertEquals(0, size);
    }

    private int getIntData() {
        requestId++;
        if (requestId == Integer.MAX_VALUE) {
            requestId = 0;
        }
        return requestId;
    }
}
