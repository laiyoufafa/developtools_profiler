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

package ohos.devtools.datasources.utils.trace.service;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.grpc.util.MutableHandlerRegistry;
import ohos.devtools.datasources.transport.grpc.HiProfilerClient;
import ohos.devtools.datasources.transport.grpc.MockProfilerServiceImplBase;
import ohos.devtools.datasources.transport.grpc.ProfilerClient;
import ohos.devtools.datasources.transport.grpc.SystemTraceHelper;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.MemoryPluginResult;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.utils.common.GrpcException;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * get Trace Data Test Class
 */
public class SystemTraceHelperTest {
    private static volatile Integer requestId = 1;

    /**
     * grpcCleanup
     */
    public GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
    private DeviceIPPortInfo deviceIPPortInfo;
    private ManagedChannel channel;
    private String serverName;
    private MockProfilerServiceImplBase getFeatureImpl;
    private MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();

    /**
     * TraceManager init
     *
     * @tc.name: TraceManager init
     * @tc.number: OHOS_JAVA_trace_TraceManager_init_0001
     * @tc.desc: TraceManager init
     * @tc.type: functional testing
     * @tc.require: SR-032
     * @throws IOException IOException
     */
    @Before
    public void initObj() throws IOException {
        SessionManager.getInstance().setDevelopMode(true);
        setDeviceInfo();
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
                ProfilerServiceTypes.GetCapabilitiesResponse reply = getGetCapabilitiesResponse();
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
                beginSession(responseObserver);
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
                SystemTraceHelperTest.this.dataFetch(responseObserver);
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
                SystemTraceHelperTest.this.destroy(responseObserver);
            }
        };
        register();
    }

    private void dataFetch(StreamObserver<ProfilerServiceTypes.FetchDataResponse> responseObserver) {
        ProfilerServiceTypes.FetchDataResponse fetchDataResponse = getFetchDataResponse();
        responseObserver.onNext(fetchDataResponse);
        responseObserver.onCompleted();
    }

    private void destroy(StreamObserver<ProfilerServiceTypes.DestroySessionResponse> responseObserver) {
        ProfilerServiceTypes.DestroySessionResponse reply =
            ProfilerServiceTypes.DestroySessionResponse.newBuilder().setStatus(0).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    private void register() throws IOException {
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).fallbackHandlerRegistry(serviceRegistry).directExecutor().build()
                .start());
        channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
    }

    private void setDeviceInfo() {
        deviceIPPortInfo = new DeviceIPPortInfo();
        deviceIPPortInfo.setIp("");
        deviceIPPortInfo.setPort(5001);
        deviceIPPortInfo.setForwardPort(5001);
        serverName = InProcessServerBuilder.generateName();
    }

    private void beginSession(StreamObserver<ProfilerServiceTypes.StartSessionResponse> responseObserver) {
        CommonTypes.ProfilerPluginState profilerPluginState = CommonTypes.ProfilerPluginState.newBuilder().build();
        ProfilerServiceTypes.StartSessionResponse reply =
            ProfilerServiceTypes.StartSessionResponse.newBuilder().setStatus(0).addPluginStatus(profilerPluginState)
                .build();
        responseObserver.onNext(reply);
    }

    private ProfilerServiceTypes.GetCapabilitiesResponse getGetCapabilitiesResponse() {
        ProfilerServiceTypes.ProfilerPluginCapability pluginCapability = ProfilerServiceTypes.ProfilerPluginCapability
            .newBuilder(ProfilerServiceTypes.ProfilerPluginCapability.newBuilder()
                .setName("/data/local/tmp/libbytraceplugin.z.so").setPath("/data/local/tmp/libbytraceplugin.z.so")
                .build()).build();

        ProfilerServiceTypes.ProfilerPluginCapability pluginCapabilityPtrace =
            ProfilerServiceTypes.ProfilerPluginCapability.newBuilder(
                ProfilerServiceTypes.ProfilerPluginCapability.newBuilder()
                    .setName("/data/local/tmp/libptrace_plugin.z.so").setPath("/data/local/tmp/libptrace_plugin.z.so")
                    .build()).build();

        ProfilerServiceTypes.GetCapabilitiesResponse reply =
            ProfilerServiceTypes.GetCapabilitiesResponse.newBuilder().addCapabilities(pluginCapability)
                .addCapabilities(pluginCapabilityPtrace).setStatus(0).build();
        return reply;
    }

    private ProfilerServiceTypes.FetchDataResponse getFetchDataResponse() {
        MemoryPluginResult.AppSummary sss =
            MemoryPluginResult.AppSummary.newBuilder().setJavaHeap(getIntData()).setNativeHeap(getIntData())
                .setCode(getIntData()).setStack(getIntData()).setGraphics(getIntData()).setPrivateOther(getIntData())
                .setSystem(0).build();
        MemoryPluginResult.ProcessMemoryInfo processesInfo0 =
            MemoryPluginResult.ProcessMemoryInfo.newBuilder().setPid(31141).setName("rcu_gp").setRssShmemKb(1)
                .setMemsummary(sss).build();
        MemoryPluginResult.ProcessMemoryInfo processesInfo1 =
            MemoryPluginResult.ProcessMemoryInfo.newBuilder().setPid(31142).setName("rcu_bh").setRssShmemKb(2222222)
                .build();
        MemoryPluginResult.ProcessMemoryInfo processesInfo2 =
            MemoryPluginResult.ProcessMemoryInfo.newBuilder().setPid(31144).setName("netns").setRssShmemKb(3333333)
                .build();
        MemoryPluginResult.MemoryData aaa =
            MemoryPluginResult.MemoryData.newBuilder().addProcessesinfo(processesInfo0).addProcessesinfo(processesInfo1)
                .addProcessesinfo(processesInfo2).build();
        CommonTypes.ProfilerPluginData data =
            CommonTypes.ProfilerPluginData.newBuilder().setName("memory-plugin").setStatus(0)
                .setData(aaa.toByteString()).build();
        ProfilerServiceTypes.FetchDataResponse fetchDataResponse =
            ProfilerServiceTypes.FetchDataResponse.newBuilder().setResponseId(123456789).setStatus(0).setHasMore(false)
                .addPluginData(data).build();
        return fetchDataResponse;
    }

    /**
     * TraceManager get Singleton
     *
     * @tc.name: TraceManager getSingleton
     * @tc.number: OHOS_JAVA_trace_TraceManager_getSingleton_0001
     * @tc.desc: TraceManager getSingleton
     * @tc.type: functional testing
     * @tc.require: SR-032
     */
    @Test
    public void getSingletonTest() {
        SystemTraceHelper systemTraceHelper = SystemTraceHelper.getSingleton();
        Assert.assertNotNull(systemTraceHelper);
    }

    private int getIntData() {
        requestId++;
        if (requestId == Integer.MAX_VALUE) {
            requestId = 0;
        }
        return requestId;
    }

    /**
     * TraceManager create Session By Trace Request
     *
     * @tc.name: TraceManager createSessionByTraceRequest
     * @tc.number: OHOS_JAVA_trace_TraceManager_getSingleton_0001
     * @tc.desc: TraceManager createSessionByTraceRequest
     * @tc.type: functional testing
     * @tc.require: SR-032
     * @throws GrpcException GrpcException
     */
    @Test
    public void createSessionByTraceRequestTest() throws GrpcException {
        SystemTraceHelper systemTraceHelper = SystemTraceHelper.getSingleton();
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient("", 5001, channel);
        String sessionId = systemTraceHelper
            .createSessionByTraceRequest(deviceIPPortInfo, "idle", 5,
                10, "/data/local/tmp/hiprofiler_data.bytrace",true);
        Assert.assertNotNull(sessionId);
    }

    /**
     * TraceManager stop And Destroy Session
     *
     * @tc.name: TraceManager stopAndDestroySession
     * @tc.number: OHOS_JAVA_trace_TraceManager_getSingleton_0001
     * @tc.desc: TraceManager stopAndDestroySession
     * @tc.type: functional testing
     * @tc.require: SR-032
     * @throws GrpcException GrpcException
     */
    @Test
    public void stopAndDestroySessionTest() throws GrpcException {
        SystemTraceHelper systemTraceHelper = SystemTraceHelper.getSingleton();
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient("", 5001, channel);
        String sessionId = systemTraceHelper
            .createSessionByTraceRequest(deviceIPPortInfo, "idle", 5,
                10, "/data/local/tmp/hiprofiler_data.bytrace", true);
        systemTraceHelper.stopAndDestroySession(deviceIPPortInfo, sessionId);
    }
}
