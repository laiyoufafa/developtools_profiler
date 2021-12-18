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
import ohos.devtools.datasources.transport.grpc.HiProfilerClient;
import ohos.devtools.datasources.transport.grpc.MockProfilerServiceImplBase;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.ProcessPluginResult;
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
    private DeviceIPPortInfo deviceInfo = new DeviceIPPortInfo();
    private String deviceId;
    private String serverName;
    private String IP;
    private int firstPort;
    private MockProfilerServiceImplBaseCustom getFeatureImpl;
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
        deviceInfo = new DeviceIPPortInfo();
        deviceInfo.setIp("");
        deviceInfo.setDeviceID("1");
        deviceInfo.setPort(5001);
        deviceInfo.setForwardPort(5001);
        deviceInfo.setDeviceName("");
        deviceInfo.setDeviceType(DeviceType.FULL_HOS_DEVICE);
        deviceId = "1";
        processInfo = new ProcessInfo();
        processInfo.setDeviceId("1");
        processInfo.setProcessId(1);
        processInfo.setProcessName("com.go.maps");
        processName = "goo";
        IP = "";
        firstPort = 5001;
        serverName = InProcessServerBuilder.generateName();
        getFeatureImpl = new MockProfilerServiceImplBaseCustom();
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).fallbackHandlerRegistry(serviceRegistry).directExecutor().build()
                .start());
        channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
    }

    class MockProfilerServiceImplBaseCustom extends MockProfilerServiceImplBase {
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
                    ProfilerServiceTypes.ProfilerPluginCapability
                        .newBuilder().setName("/data/local/tmp/libprocessplugin.z.so")
                        .setPath("/data/local/tmp/libprocessplugin.z.so").build()).build();
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
            ProcessPluginResult.ProcessInfo processInfo0 = ProcessPluginResult.ProcessInfo.newBuilder()
                .setPid(1).setName("init").build();
            ProcessPluginResult.ProcessInfo processInfo1 = ProcessPluginResult.ProcessInfo.newBuilder()
                .setPid(2).setName("rcu_gp").build();
            ProcessPluginResult.ProcessInfo processInfo2 = ProcessPluginResult.ProcessInfo.newBuilder()
                .setPid(3).setName("rcu_bh").build();
            ProcessPluginResult.ProcessData sss =
                ProcessPluginResult.ProcessData.newBuilder().addProcessesinfo(processInfo0)
                    .addProcessesinfo(processInfo1).addProcessesinfo(processInfo2).build();
            CommonTypes.ProfilerPluginData data =
                CommonTypes.ProfilerPluginData.newBuilder().setName("process-plugin").setStatus(0)
                    .setData(sss.toByteString()).build();
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

        @Override
        public void keepSession(ProfilerServiceTypes.KeepSessionRequest request,
            StreamObserver<ProfilerServiceTypes.KeepSessionResponse> responseObserver) {
            ProfilerServiceTypes.KeepSessionResponse reply =
                ProfilerServiceTypes.KeepSessionResponse.getDefaultInstance();
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
        List<ProcessInfo> processList = ProcessManager.getInstance().getProcessList(deviceInfo);
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
        deviceInfo.setIp("10");
        deviceInfo.setPort(Integer.MAX_VALUE);
        new DeviceDao().insertDeviceIPPortInfo(deviceInfo);
        List<ProcessInfo> processList1 = ProcessManager.getInstance().getProcessList(deviceInfo);
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
        deviceInfo.setIp("10");
        deviceInfo.setForwardPort(-1);
        new DeviceDao().insertDeviceIPPortInfo(deviceInfo);
        List<ProcessInfo> processList = ProcessManager.getInstance().getProcessList(deviceInfo);
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
        DeviceIPPortInfo deviceIPPortInfo = new DeviceIPPortInfo();
        deviceIPPortInfo.setPort(5001);
        deviceIPPortInfo.setForwardPort(8765);
        deviceIPPortInfo.setDeviceName("");
        deviceIPPortInfo.setDeviceType(DeviceType.FULL_HOS_DEVICE);
        deviceIPPortInfo.setIp("10.40.111.1111");
        deviceIPPortInfo.setDeviceID("ANA_ANA_001");
        new DeviceDao().insertDeviceIPPortInfo(deviceIPPortInfo);
        HiProfilerClient.getInstance().getProfilerClient("10.40.111.1111", 8765, channel);
        List<ProcessInfo> processList = ProcessManager.getInstance().getProcessList(deviceIPPortInfo);
        int size = processList.size();
        Assert.assertEquals(size, 3);
    }

    private int getIntData() {
        requestId++;
        if (requestId == Integer.MAX_VALUE) {
            requestId = 0;
        }
        return requestId;
    }
}
