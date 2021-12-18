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

package ohos.devtools.datasources.transport.grpc;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.grpc.util.MutableHandlerRegistry;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Hiprofiler Client Over Time
 *
 * @since 2021/11/22
 **/
public class HiprofilerClientOverTime {
    private static volatile int requestId = 1;

    private String IP;
    private int firstPort;
    private int secondPort;
    private int thirdPort;
    private String serverName;
    private final MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();
    private final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    /**
     * functional testing init
     *
     * @tc.name: setUp
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_setUp_0001
     * @tc.desc: setUp
     * @tc.type: functional testing
     * @tc.require: SR-005
     * @throws IOException throw IOException
     */
    @Before
    public void setUp() throws IOException {
        IP = "";
        firstPort = 5001;
        secondPort = 5002;
        thirdPort = 5003;
        serverName = InProcessServerBuilder.generateName();
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).fallbackHandlerRegistry(serviceRegistry).directExecutor().build()
                .start());
    }

    /**
     * get Capabilities Overtime Test
     *
     * @tc.name: getCapabilitiesOvertimeTest06
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_getCapabilitiesOvertimeTest_0006
     * @tc.desc: get Capabilities Overtime Test
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-005
     */
    @Test
    public void getCapabilitiesOvertimeTest06() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
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
                try {
                    TimeUnit.SECONDS.sleep(6);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, thirdPort, channel);
        ProfilerServiceTypes.GetCapabilitiesResponse res = HiProfilerClient.getInstance().getCapabilities(IP, 5004);
        List<ProfilerServiceTypes.ProfilerPluginCapability> caps = res.getCapabilitiesList();
        Assert.assertEquals(caps.size(), 0);
    }

    /**
     * functional testing requestCreateSession abNormal based on port is -1
     *
     * @tc.name: requestCreateSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestCreateSession_0006
     * @tc.desc: requestCreateSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-001
     */
    @Test
    public void requestCreateSessionOvertimeTest06() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void createSession(ProfilerServiceTypes.CreateSessionRequest request,
                StreamObserver<ProfilerServiceTypes.CreateSessionResponse> responseObserver) {
                ProfilerServiceTypes.CreateSessionResponse reply =
                    ProfilerServiceTypes.CreateSessionResponse.getDefaultInstance();
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        HiProfilerClient.getInstance().getProfilerClient(IP, 10006, channel);
        ProfilerServiceTypes.CreateSessionResponse createSessionResponse =
            HiProfilerClient.getInstance().requestCreateSession(IP, 10006, null);
        int sessionId = createSessionResponse.getSessionId();
        Assert.assertEquals(sessionId, -1);
    }

    /**
     * functional testing requestdestory
     *
     * @tc.name: requestDestroySessionOvertimeTest06
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestDestroySessionOvertimeTest_0006
     * @tc.desc: request Destroy Session Overtime Test
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-004
     */
    @Test
    public void requestDestroySessionOvertimeTest06() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void destroySession(ProfilerServiceTypes.DestroySessionRequest request,
                StreamObserver<ProfilerServiceTypes.DestroySessionResponse> responseObserver) {
                ProfilerServiceTypes.DestroySessionResponse reply =
                    ProfilerServiceTypes.DestroySessionResponse.newBuilder().setStatus(0).build();
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 10005, channel);
        boolean res = HiProfilerClient.getInstance().requestDestroySession(IP, 10005, 222);
        Assert.assertTrue(res);
    }

    /**
     * functional testing requestStartSession abnormal based on port is -1
     *
     * @tc.name: requestStopSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestStartSession_0006
     * @tc.desc: requestStartSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-002
     */
    @Test
    public void requestStartSessionOvertimeTest06() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void startSession(ProfilerServiceTypes.StartSessionRequest request,
                StreamObserver<ProfilerServiceTypes.StartSessionResponse> responseObserver) {
                CommonTypes.ProfilerPluginState profilerPluginState =
                    CommonTypes.ProfilerPluginState.newBuilder().build();
                ProfilerServiceTypes.StartSessionResponse reply =
                    ProfilerServiceTypes.StartSessionResponse.newBuilder().setStatus(0)
                        .addPluginStatus(profilerPluginState).build();
                try {
                    TimeUnit.SECONDS.sleep(4);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 65529, channel);
        boolean res = HiProfilerClient.getInstance().requestStartSession(IP, 65529, 1);
        Assert.assertFalse(res);
    }

    /**
     * functional testing requestStopSession abnormal based on port is -1
     *
     * @tc.name: requestStopSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestStopSession_0006
     * @tc.desc: requestStopSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-002
     */
    @Test
    public void requestStopSessionOvertimeTest06() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void stopSession(ProfilerServiceTypes.StopSessionRequest request,
                StreamObserver<ProfilerServiceTypes.StopSessionResponse> responseObserver) {
                ProfilerServiceTypes.StopSessionResponse reply =
                    ProfilerServiceTypes.StopSessionResponse.newBuilder().setStatus(0).build();
                try {
                    TimeUnit.SECONDS.sleep(4);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 65530, channel);
        boolean res = HiProfilerClient.getInstance().requestStopSession(IP, 65530, 111, false);
        Assert.assertFalse(res);
    }

}
