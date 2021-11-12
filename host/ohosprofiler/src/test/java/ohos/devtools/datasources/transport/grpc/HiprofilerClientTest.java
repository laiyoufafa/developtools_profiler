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
import ohos.devtools.datasources.transport.grpc.service.MemoryPluginResult;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * test hiprofiler module
 *
 * @since 2021/2/1 9:31
 */
public class HiprofilerClientTest {
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
     * get Instance
     *
     * @tc.name: getInstanceTest01
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_getInstanceTest_0001
     * @tc.desc: get Instance
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void getInstanceTest() {
        HiProfilerClient instance = HiProfilerClient.getInstance();
        Assert.assertNotNull(instance);
    }

    /**
     * functional testing getProfilerClient normal get Single
     *
     * @tc.name: getProfilerClient
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_getProfilerClient_0001
     * @tc.desc: getProfilerClient
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void getProfilerClientTest01() {
        ProfilerClient profilerClient = HiProfilerClient.getInstance().getProfilerClient(IP, firstPort);
        Assert.assertNotNull(profilerClient);
    }

    /**
     * functional testing getProfilerClient normal get instance diffrent port is not equals
     *
     * @tc.name: getProfilerClient
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_getProfilerClient_0002
     * @tc.desc: getProfilerClient
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void getProfilerClientTest02() {
        ProfilerClient profilerClient = HiProfilerClient.getInstance().getProfilerClient(IP, firstPort);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, secondPort);
        Assert.assertNotEquals(profilerClient, client);
    }

    /**
     * functional testing getProfilerClient normal get instance same port is equals
     *
     * @tc.name: getProfilerClient
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_getProfilerClient_0003
     * @tc.desc: getProfilerClient
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void getProfilerClientTest03() {
        ProfilerClient profilerClient = HiProfilerClient.getInstance().getProfilerClient(IP, secondPort);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, secondPort);
        Assert.assertEquals(profilerClient, client);
    }

    /**
     * functional testing getProfilerClient abnormal port is -1
     *
     * @tc.name: getProfilerClient
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_getProfilerClient_0004
     * @tc.desc: getProfilerClient
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void getProfilerClientTest04() {
        ProfilerClient profilerClient = HiProfilerClient.getInstance().getProfilerClient(IP, -1);
        Assert.assertNull(profilerClient);
    }

    /**
     * functional testing getProfilerClient abnormal port is 0
     *
     * @tc.name: getProfilerClient
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_getProfilerClient_0005
     * @tc.desc: getProfilerClient
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void getProfilerClientTest05() {
        ProfilerClient profilerClient = HiProfilerClient.getInstance().getProfilerClient(IP, 0);
        Assert.assertNull(profilerClient);
    }

    /**
     * get ProfilerClient
     *
     * @tc.name: getProfilerClient
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_getProfilerClientMultiParam_0001
     * @tc.desc: get ProfilerClient
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void getProfilerClientMultiParam01() {
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 65536, channel);
        Assert.assertNull(client);
    }

    /**
     * get ProfilerClient
     *
     * @tc.name: getProfilerClient
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_getProfilerClientMultiParam_0002
     * @tc.desc: get ProfilerClient
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void getProfilerClientMultiParam02() {
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, firstPort, channel);
        ProfilerClient getClient = HiProfilerClient.getInstance().getProfilerClient(IP, secondPort, channel);
        Assert.assertNotEquals(client, getClient);
    }

    /**
     * get ProfilerClient
     *
     * @tc.name: getProfilerClient
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_getProfilerClientMultiParam_0003
     * @tc.desc: get ProfilerClient
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void getProfilerClientMultiParam03() {
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, firstPort, channel);
        ProfilerClient getClient = HiProfilerClient.getInstance().getProfilerClient(IP, firstPort, channel);
        Assert.assertEquals(client, getClient);
    }

    /**
     * get ProfilerClient
     *
     * @tc.name: getProfilerClient
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_getProfilerClientMultiParam_0004
     * @tc.desc: get ProfilerClient
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void getProfilerClientMultiParam04() {
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 0, channel);
        Assert.assertNull(client);
    }

    /**
     * get ProfilerClient
     *
     * @tc.name: getProfilerClient
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_getProfilerClientMultiParam_0005
     * @tc.desc: get ProfilerClient
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void getProfilerClientMultiParam05() {
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, -1, channel);
        Assert.assertNull(client);
    }

    /**
     * functional testing destroyProfiler normal
     *
     * @tc.name: destroyProfiler
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_destroyProfiler_0001
     * @tc.desc: destroyProfiler
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-004
     */
    @Test
    public void destroyProfilerTest01() {
        boolean res = HiProfilerClient.getInstance().destroyProfiler(IP, secondPort);
        Assert.assertTrue(res);
    }

    /**
     * functional testing destroyProfiler normal different port
     *
     * @tc.name: destroyProfiler
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_destroyProfiler_0002
     * @tc.desc: destroyProfiler
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-004
     */
    @Test
    public void destroyProfilerTest02() {
        boolean res = HiProfilerClient.getInstance().destroyProfiler(null, firstPort);
        Assert.assertTrue(res);
    }

    /**
     * functional testing destroyProfiler abnormal port is 0
     *
     * @tc.name: destroyProfiler
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_destroyProfiler_0003
     * @tc.desc: destroyProfiler
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-004
     */
    @Test
    public void destroyProfilerTest03() {
        boolean res = HiProfilerClient.getInstance().destroyProfiler(IP, 0);
        Assert.assertFalse(res);
    }

    /**
     * functional testing destroyProfiler abnormal port is 65536
     *
     * @tc.name: destroyProfiler
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_destroyProfiler_0004
     * @tc.desc: destroyProfiler
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-004
     */
    @Test
    public void destroyProfilerTest04() {
        boolean res = HiProfilerClient.getInstance().destroyProfiler(IP, 65536);
        Assert.assertFalse(res);
    }

    /**
     * functional testing destroyProfiler abnormal port is -1
     *
     * @tc.name: destroyProfiler
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_destroyProfiler_0005
     * @tc.desc: destroyProfiler
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-004
     */
    @Test
    public void destroyProfilerTest05() {
        boolean res = HiProfilerClient.getInstance().destroyProfiler(IP, -1);
        Assert.assertFalse(res);
    }

    /**
     * functional testing getCapabilities normal based on port and Status
     *
     * @tc.name: getCapabilities
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_getCapabilities_0001
     * @tc.desc: getCapabilities
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-002
     */
    @Test
    public void getCapabilitiesTest01() {
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
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, thirdPort, channel);
        ProfilerServiceTypes.GetCapabilitiesResponse res =
            HiProfilerClient.getInstance().getCapabilities(IP, thirdPort);
        List<ProfilerServiceTypes.ProfilerPluginCapability> caps = res.getCapabilitiesList();
        caps.forEach(profilerPluginCapability -> {
            Assert.assertEquals(profilerPluginCapability.getName(), "test0");
        });
        Assert.assertEquals(caps.size(), 1);
    }

    /**
     * functional testing getCapabilities abnormal based on port is 0
     *
     * @tc.name: getCapabilities
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_getCapabilities_0002
     * @tc.desc: getCapabilities
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-002
     */
    @Test
    public void getCapabilitiesTest02() {
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
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 0, channel);
        ProfilerServiceTypes.GetCapabilitiesResponse res = HiProfilerClient.getInstance().getCapabilities(IP, 0);
        Assert.assertNull(res);
    }

    /**
     * functional testing getCapabilities abnormal based on port and Status is -1
     *
     * @tc.name: getCapabilities
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_getCapabilities_0003
     * @tc.desc: getCapabilities
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-002
     */
    @Test
    public void getCapabilitiesTest03() {
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
                        .setStatus(-1).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 10001, channel);
        ProfilerServiceTypes.GetCapabilitiesResponse res = HiProfilerClient.getInstance().getCapabilities(IP, 10001);
        Assert.assertEquals(res.getStatus(), -1);
    }

    /**
     * functional testing getCapabilities abnormal based on port is -1
     *
     * @tc.name: getCapabilities
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_getCapabilities_0004
     * @tc.desc: getCapabilities
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-002
     */
    @Test
    public void getCapabilitiesTest04() {
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
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, -1, channel);
        ProfilerServiceTypes.GetCapabilitiesResponse res = HiProfilerClient.getInstance().getCapabilities(IP, -1);
        Assert.assertNull(res);
    }

    /**
     * functional testing getCapabilities 2 normal based on port and Status
     *
     * @tc.name: getCapabilities
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_getCapabilities_0005
     * @tc.desc: getCapabilities
     * @tc.type: functional testing
     * @tc.require: SR-004-AR-002
     */
    @Test
    public void getCapabilitiesTest05() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void getCapabilities(ProfilerServiceTypes.GetCapabilitiesRequest request,
                StreamObserver<ProfilerServiceTypes.GetCapabilitiesResponse> responseObserver) {
                ProfilerServiceTypes.ProfilerPluginCapability pluginCapability =
                    ProfilerServiceTypes.ProfilerPluginCapability.newBuilder(
                        ProfilerServiceTypes.ProfilerPluginCapability.newBuilder()
                            .setName("/data/local/tmp/libbytraceplugin.z.so")
                            .setPath("/data/local/tmp/libbytraceplugin.z.so").build()).build();

                ProfilerServiceTypes.ProfilerPluginCapability pluginCapabilityPtrace =
                    ProfilerServiceTypes.ProfilerPluginCapability.newBuilder(
                        ProfilerServiceTypes.ProfilerPluginCapability.newBuilder()
                            .setName("/data/local/tmp/libptrace_plugin.z.so")
                            .setPath("/data/local/tmp/libptrace_plugin.z.so").build()).build();

                ProfilerServiceTypes.GetCapabilitiesResponse reply =
                    ProfilerServiceTypes.GetCapabilitiesResponse.newBuilder().addCapabilities(pluginCapability)
                        .addCapabilities(pluginCapabilityPtrace).setStatus(0).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 11004, channel);
        ProfilerServiceTypes.GetCapabilitiesResponse res = HiProfilerClient.getInstance().getCapabilities(IP, 11004);
        List<ProfilerServiceTypes.ProfilerPluginCapability> caps = res.getCapabilitiesList();
        Assert.assertEquals(caps.size(), 2);
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
     * functional testing requestCreateSession Normal based on reportprocesstree is true
     *
     * @tc.name: requestCreateSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestCreateSession_0001
     * @tc.desc: requestCreateSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-001
     */
    @Test
    public void requestCreateSessionTest01() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void createSession(ProfilerServiceTypes.CreateSessionRequest request,
                StreamObserver<ProfilerServiceTypes.CreateSessionResponse> responseObserver) {
                ProfilerServiceTypes.CreateSessionResponse reply =
                    ProfilerServiceTypes.CreateSessionResponse.getDefaultInstance();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        HiProfilerClient.getInstance().getProfilerClient(IP, 10002, channel);
        int res = HiProfilerClient.getInstance()
            .processListCreateSession(10002, "/data/local/tmp/libmemdata.z.so", 212, true, DeviceType.FULL_HOS_DEVICE);
        Assert.assertEquals(res, 0);
    }

    /**
     * functional testing requestCreateSession Normal based on reportprocesstree is false
     *
     * @tc.name: requestCreateSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestCreateSession_0002
     * @tc.desc: requestCreateSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-001
     */
    @Test
    public void requestCreateSessionTest02() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void createSession(ProfilerServiceTypes.CreateSessionRequest request,
                StreamObserver<ProfilerServiceTypes.CreateSessionResponse> responseObserver) {
                ProfilerServiceTypes.CreateSessionResponse reply =
                    ProfilerServiceTypes.CreateSessionResponse.getDefaultInstance();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        HiProfilerClient.getInstance().getProfilerClient(IP, 10002, channel);
        int res = HiProfilerClient.getInstance()
            .processListCreateSession(10002, "/data/local/tmp/libmemdata.z.so", 212, false, DeviceType.FULL_HOS_DEVICE);
        Assert.assertEquals(res, 0);
    }

    /**
     * functional testing requestCreateSession abNormal based on SessionId is -1 and  reportprocesstree is true
     *
     * @tc.name: requestCreateSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestCreateSession_0003
     * @tc.desc: requestCreateSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-001
     */
    @Test
    public void requestCreateSessionTest03() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void createSession(ProfilerServiceTypes.CreateSessionRequest request,
                StreamObserver<ProfilerServiceTypes.CreateSessionResponse> responseObserver) {
                ProfilerServiceTypes.CreateSessionResponse reply =
                    ProfilerServiceTypes.CreateSessionResponse.newBuilder().setSessionId(-1).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        HiProfilerClient.getInstance().getProfilerClient(IP, 10003, channel);
        int res = HiProfilerClient.getInstance()
            .processListCreateSession(10003, "/data/local/tmp/libmemdata.z.so", 212, true, DeviceType.FULL_HOS_DEVICE);

        Assert.assertEquals(res, -1);
    }

    /**
     * functional testing requestCreateSession abNormal based on port is 0
     *
     * @tc.name: requestCreateSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestCreateSession_0004
     * @tc.desc: requestCreateSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-001
     */
    @Test
    public void requestCreateSessionTest04() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void createSession(ProfilerServiceTypes.CreateSessionRequest request,
                StreamObserver<ProfilerServiceTypes.CreateSessionResponse> responseObserver) {
                ProfilerServiceTypes.CreateSessionResponse reply =
                    ProfilerServiceTypes.CreateSessionResponse.newBuilder().setSessionId(1).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        HiProfilerClient.getInstance().getProfilerClient(IP, 0, channel);
        int res = HiProfilerClient.getInstance()
            .processListCreateSession(0, "/data/local/tmp/libmemdata.z.so", 212, false, DeviceType.FULL_HOS_DEVICE);
        Assert.assertEquals(res, -1);
    }

    /**
     * functional testing requestCreateSession abNormal based on port is -1
     *
     * @tc.name: requestCreateSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestCreateSession_0005
     * @tc.desc: requestCreateSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-001
     */
    @Test
    public void requestCreateSessionTest05() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void createSession(ProfilerServiceTypes.CreateSessionRequest request,
                StreamObserver<ProfilerServiceTypes.CreateSessionResponse> responseObserver) {
                ProfilerServiceTypes.CreateSessionResponse reply =
                    ProfilerServiceTypes.CreateSessionResponse.newBuilder().setSessionId(-1).setStatus(-1).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        HiProfilerClient.getInstance().getProfilerClient(IP, -1, channel);
        int res = HiProfilerClient.getInstance()
            .processListCreateSession(-1, "/data/local/tmp/libmemdata.z.so", 212, true, DeviceType.FULL_HOS_DEVICE);
        Assert.assertEquals(res, -1);
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
        HiProfilerClient.getInstance().getProfilerClient(IP, 10004, channel);
        int res = HiProfilerClient.getInstance()
            .processListCreateSession(10004, "/data/local/tmp/libmemdata.z.so", 212, true, DeviceType.FULL_HOS_DEVICE);
        Assert.assertEquals(res, -1);
    }

    /**
     * functional testing requestStartSession normal based on status is 0
     *
     * @tc.name: requestStartSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestStartSession_0001
     * @tc.desc: requestStartSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-001
     */
    @Test
    public void requestStartSessionTest01() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
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
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 10003, channel);
        boolean res = HiProfilerClient.getInstance().requestStartSession(IP, 10003, 1);
        Assert.assertTrue(res);
    }

    /**
     * functional testing requestStartSession abnormal based on status is -1
     *
     * @tc.name: requestStartSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestStartSession_0002
     * @tc.desc: requestStartSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-001
     */
    @Test
    public void requestStartSessionTest02() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void startSession(ProfilerServiceTypes.StartSessionRequest request,
                StreamObserver<ProfilerServiceTypes.StartSessionResponse> responseObserver) {
                CommonTypes.ProfilerPluginState profilerPluginState =
                    CommonTypes.ProfilerPluginState.newBuilder().build();
                ProfilerServiceTypes.StartSessionResponse reply =
                    ProfilerServiceTypes.StartSessionResponse.newBuilder().setStatus(-1)
                        .addPluginStatus(profilerPluginState).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 11103, channel);
        boolean res = HiProfilerClient.getInstance().requestStartSession(IP, 11103, 1);
        Assert.assertFalse(res);
    }

    /**
     * functional testing requestStartSession normal based on status is 0 and port different
     *
     * @tc.name: requestStartSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestStartSession_0003
     * @tc.desc: requestStartSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-001
     */
    @Test
    public void requestStartSessionTest03() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
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
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 65536, channel);
        boolean res = HiProfilerClient.getInstance().requestStartSession(IP, 65536, 2);
        Assert.assertFalse(res);
    }

    /**
     * functional testing requestStartSession abnormal based on port is 0
     *
     * @tc.name: requestStartSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestStartSession_0004
     * @tc.desc: requestStartSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-001
     */
    @Test
    public void requestStartSessionTest04() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
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
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 0, channel);
        boolean res = HiProfilerClient.getInstance().requestStartSession(IP, 0, 2);
        Assert.assertFalse(res);
    }

    /**
     * functional testing requestStartSession abnormal based on port is -1
     *
     * @tc.name: requestStartSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestStartSession_0005
     * @tc.desc: requestStartSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-001
     */
    @Test
    public void requestStartSessionTest05() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
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
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, -1, channel);
        boolean res = HiProfilerClient.getInstance().requestStartSession(IP, -1, 2);
        Assert.assertFalse(res);
    }

    /**
     * functional testing requestStartSession abnormal based on port is -1
     *
     * @tc.name: requestStartSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestStartSession_0006
     * @tc.desc: requestStartSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-001
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
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 10003, channel);
        boolean res = HiProfilerClient.getInstance().requestStartSession(IP, 10003, 1);
        Assert.assertTrue(res);
    }

    /**
     * functional testing requestStopSession normal based on status is 0
     *
     * @tc.name: requestStopSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestStopSession_0001
     * @tc.desc: requestStopSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-002
     */
    @Test
    public void requestStopSessionTest01() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void stopSession(ProfilerServiceTypes.StopSessionRequest request,
                StreamObserver<ProfilerServiceTypes.StopSessionResponse> responseObserver) {
                ProfilerServiceTypes.StopSessionResponse reply =
                    ProfilerServiceTypes.StopSessionResponse.newBuilder().setStatus(0).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 10004, channel);

        boolean res = HiProfilerClient.getInstance().requestStopSession(IP, 10004, 111, false);
        Assert.assertTrue(res);
    }

    /**
     * functional testing requestStopSession normal based on status is -1
     *
     * @tc.name: requestStopSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestStopSession_0002
     * @tc.desc: requestStopSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-002
     */
    @Test
    public void requestStopSessionTest02() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void stopSession(ProfilerServiceTypes.StopSessionRequest request,
                StreamObserver<ProfilerServiceTypes.StopSessionResponse> responseObserver) {
                ProfilerServiceTypes.StopSessionResponse reply =
                    ProfilerServiceTypes.StopSessionResponse.newBuilder().setStatus(-1).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 10104, channel);

        boolean res = HiProfilerClient.getInstance().requestStopSession(IP, 10104, 111, false);
        Assert.assertFalse(res);
    }

    /**
     * functional testing requestStopSession normal based on port
     *
     * @tc.name: requestStopSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestStopSession_0003
     * @tc.desc: requestStopSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-002
     */
    @Test
    public void requestStopSessionTest03() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void stopSession(ProfilerServiceTypes.StopSessionRequest request,
                StreamObserver<ProfilerServiceTypes.StopSessionResponse> responseObserver) {
                ProfilerServiceTypes.StopSessionResponse reply =
                    ProfilerServiceTypes.StopSessionResponse.newBuilder().setStatus(0).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 65536, channel);

        boolean res = HiProfilerClient.getInstance().requestStopSession(IP, 65536, 111, false);
        Assert.assertFalse(res);
    }

    /**
     * functional testing requestStopSession abnormal based on port is 0
     *
     * @tc.name: requestStopSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestStopSession_0004
     * @tc.desc: requestStopSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-002
     */
    @Test
    public void requestStopSessionTest04() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void stopSession(ProfilerServiceTypes.StopSessionRequest request,
                StreamObserver<ProfilerServiceTypes.StopSessionResponse> responseObserver) {
                ProfilerServiceTypes.StopSessionResponse reply =
                    ProfilerServiceTypes.StopSessionResponse.newBuilder().setStatus(0).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 0, channel);

        boolean res = HiProfilerClient.getInstance().requestStopSession(IP, 0, 111, false);
        Assert.assertFalse(res);
    }

    /**
     * functional testing requestStopSession abnormal based on port is -1
     *
     * @tc.name: requestStopSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestStopSession_0005
     * @tc.desc: requestStopSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-002
     */
    @Test
    public void requestStopSessionTest05() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void stopSession(ProfilerServiceTypes.StopSessionRequest request,
                StreamObserver<ProfilerServiceTypes.StopSessionResponse> responseObserver) {
                ProfilerServiceTypes.StopSessionResponse reply =
                    ProfilerServiceTypes.StopSessionResponse.newBuilder().setStatus(0).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, -1, channel);

        boolean res = HiProfilerClient.getInstance().requestStopSession(IP, -1, 111, true);
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
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 10004, channel);
        boolean res = HiProfilerClient.getInstance().requestStopSession(IP, 10004, 111, false);
        Assert.assertTrue(res);
    }

    /**
     * functional testing requestdestorySession normal based on status is 0
     *
     * @tc.name: requestdestorySession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestdestorySession_0001
     * @tc.desc: requestdestorySession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-004
     */
    @Test
    public void requestDestroySessionTest01() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void destroySession(ProfilerServiceTypes.DestroySessionRequest request,
                StreamObserver<ProfilerServiceTypes.DestroySessionResponse> responseObserver) {
                ProfilerServiceTypes.DestroySessionResponse reply =
                    ProfilerServiceTypes.DestroySessionResponse.newBuilder().setStatus(0).build();
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
     * functional testing requestdestorySession abnormal based on status is -1
     *
     * @tc.name: requestdestorySession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestdestorySession_0002
     * @tc.desc: requestdestorySession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-004
     */
    @Test
    public void requestDestroySessionTest02() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void destroySession(ProfilerServiceTypes.DestroySessionRequest request,
                StreamObserver<ProfilerServiceTypes.DestroySessionResponse> responseObserver) {
                ProfilerServiceTypes.DestroySessionResponse reply =
                    ProfilerServiceTypes.DestroySessionResponse.newBuilder().setStatus(-1).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 10006, channel);
        boolean res = HiProfilerClient.getInstance().requestDestroySession(IP, 10006, 222);
        Assert.assertFalse(res);
    }

    /**
     * functional testing requestdestorySession normal based on status is 0 and diffrent port
     *
     * @tc.name: requestdestorySession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestdestorySession_0003
     * @tc.desc: requestdestorySession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-004
     */
    @Test
    public void requestDestroySessionTest03() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void destroySession(ProfilerServiceTypes.DestroySessionRequest request,
                StreamObserver<ProfilerServiceTypes.DestroySessionResponse> responseObserver) {
                ProfilerServiceTypes.DestroySessionResponse reply =
                    ProfilerServiceTypes.DestroySessionResponse.newBuilder().setStatus(0).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 65536, channel);
        boolean res = HiProfilerClient.getInstance().requestDestroySession(IP, 65536, 222);
        Assert.assertFalse(res);
    }

    /**
     * functional testing requestdestorySession abnormal based on port is 0
     *
     * @tc.name: requestdestorySession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestdestorySession_0004
     * @tc.desc: requestdestorySession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-004
     */
    @Test
    public void requestDestroySessionTest04() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void destroySession(ProfilerServiceTypes.DestroySessionRequest request,
                StreamObserver<ProfilerServiceTypes.DestroySessionResponse> responseObserver) {
                ProfilerServiceTypes.DestroySessionResponse reply =
                    ProfilerServiceTypes.DestroySessionResponse.newBuilder().setStatus(0).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 0, channel);
        boolean res = HiProfilerClient.getInstance().requestDestroySession(IP, 0, 222);
        Assert.assertFalse(res);
    }

    /**
     * functional testing requestdestorySession abnormal based on port is -1
     *
     * @tc.name: requestdestorySession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestdestorySession_0005
     * @tc.desc: requestdestorySession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-004
     */
    @Test
    public void requestDestroySessionTest05() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void destroySession(ProfilerServiceTypes.DestroySessionRequest request,
                StreamObserver<ProfilerServiceTypes.DestroySessionResponse> responseObserver) {
                ProfilerServiceTypes.DestroySessionResponse reply =
                    ProfilerServiceTypes.DestroySessionResponse.newBuilder().setStatus(0).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, -1, channel);
        boolean res = HiProfilerClient.getInstance().requestDestroySession(IP, -1, 222);
        Assert.assertFalse(res);
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
     * functional testing fetchProcessData normal Single get 3
     *
     * @tc.name: fetchProcessData
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_fetchProcessData_0001
     * @tc.desc: fetchProcessData
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-005
     */
    @Test
    public void fetchProcessDataTest01() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void fetchData(ProfilerServiceTypes.FetchDataRequest request,
                StreamObserver<ProfilerServiceTypes.FetchDataResponse> responseObserver) {
                MemoryPluginResult.AppSummary sss =
                    MemoryPluginResult.AppSummary.newBuilder().setJavaHeap(getIntData()).setNativeHeap(getIntData())
                        .setCode(getIntData()).setStack(getIntData()).setGraphics(getIntData())
                        .setPrivateOther(getIntData()).setSystem(0).build();
                MemoryPluginResult.ProcessMemoryInfo processesInfoOne =
                    MemoryPluginResult.ProcessMemoryInfo.newBuilder().setPid(31141)
                        .setName("com.eg.and.AlipayGphone:push").setRssShmemKb(1).setMemsummary(sss).build();
                MemoryPluginResult.ProcessMemoryInfo processesInfoTwo =
                    MemoryPluginResult.ProcessMemoryInfo.newBuilder().setPid(31142).setName("com.eg.and.AlipayGphone")
                        .setRssShmemKb(2222222).build();
                MemoryPluginResult.ProcessMemoryInfo processesInfoThree =
                    MemoryPluginResult.ProcessMemoryInfo.newBuilder().setPid(31144)
                        .setName("com.hisunflytone.and:pushservice").setRssShmemKb(3333333).build();
                MemoryPluginResult.MemoryData aaa =
                    MemoryPluginResult.MemoryData.newBuilder().addProcessesinfo(processesInfoOne)
                        .addProcessesinfo(processesInfoTwo).addProcessesinfo(processesInfoThree).build();
                CommonTypes.ProfilerPluginData data =
                    CommonTypes.ProfilerPluginData.newBuilder().setName("memory-plugin").setStatus(0)
                        .setData(aaa.toByteString()).build();
                ProfilerServiceTypes.FetchDataResponse fetchDataResponse =
                    ProfilerServiceTypes.FetchDataResponse.newBuilder().setResponseId(123456789).setStatus(0)
                        .setHasMore(false).addPluginData(data).build();
                responseObserver.onNext(fetchDataResponse);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 10007, channel);
        List<ProcessInfo> res = HiProfilerClient.getInstance().fetchProcessData(IP, 10007, 11111);
        Assert.assertEquals(res.size(), 3);
    }

    /**
     * functional testing fetchProcessData normal Repeated get 3 and 2
     *
     * @tc.name: fetchProcessData
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_fetchProcessData_0002
     * @tc.desc: fetchProcessData
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-005
     */
    @Test
    public void fetchProcessDataTest02() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void fetchData(ProfilerServiceTypes.FetchDataRequest request,
                StreamObserver<ProfilerServiceTypes.FetchDataResponse> responseObserver) {
                int sessionId = request.getSessionId();
                if (sessionId == 1) {
                    MemoryPluginResult.AppSummary sss =
                        MemoryPluginResult.AppSummary.newBuilder().setJavaHeap(getIntData()).setNativeHeap(getIntData())
                            .setCode(getIntData()).setStack(getIntData()).setGraphics(getIntData())
                            .setPrivateOther(getIntData()).setSystem(0).build();
                    MemoryPluginResult.ProcessMemoryInfo processesInfoZero =
                        MemoryPluginResult.ProcessMemoryInfo.newBuilder().setPid(31141)
                            .setName("com.eg.and.AlipayGphone:push").setRssShmemKb(1).setMemsummary(sss).build();
                    MemoryPluginResult.ProcessMemoryInfo processesInfoOne =
                        MemoryPluginResult.ProcessMemoryInfo.newBuilder().setPid(31142)
                            .setName("com.eg.and.AlipayGphone").setRssShmemKb(2222222).build();
                    MemoryPluginResult.ProcessMemoryInfo processesInfoTwo =
                        MemoryPluginResult.ProcessMemoryInfo.newBuilder().setPid(31144)
                            .setName("com.hisunflytone.and:pushservice").setRssShmemKb(3333333).build();
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
                } else {
                    ProfilerServiceTypes.FetchDataResponse fetchDataResponse = getFetchDataResponse();
                    responseObserver.onNext(fetchDataResponse);
                    responseObserver.onCompleted();
                }
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 10008, channel);
        List<ProcessInfo> res = HiProfilerClient.getInstance().fetchProcessData(IP, 10008, 1);
        Assert.assertEquals(res.size(), 3);
        List<ProcessInfo> ress = HiProfilerClient.getInstance().fetchProcessData(IP, 10008, 22);
        Assert.assertEquals(ress.size(), 2);
    }

    private ProfilerServiceTypes.FetchDataResponse getFetchDataResponse() {
        MemoryPluginResult.AppSummary appSummary =
            MemoryPluginResult.AppSummary.newBuilder().setJavaHeap(getIntData()).setNativeHeap(getIntData())
                .setCode(getIntData()).setStack(getIntData()).setGraphics(getIntData())
                .setPrivateOther(getIntData()).setSystem(0).build();
        MemoryPluginResult.ProcessMemoryInfo processesInfoZero =
            MemoryPluginResult.ProcessMemoryInfo.newBuilder().setPid(31141)
                .setName("com.eg.and.AlipayGphone:push").setRssShmemKb(1).setMemsummary(appSummary).build();
        MemoryPluginResult.ProcessMemoryInfo processesInfoOne =
            MemoryPluginResult.ProcessMemoryInfo.newBuilder().setPid(31143)
                .setName("com.eg.and.AlipayGphone").setRssShmemKb(1111).build();
        MemoryPluginResult.MemoryData aaa =
            MemoryPluginResult.MemoryData.newBuilder().addProcessesinfo(processesInfoZero)
                .addProcessesinfo(processesInfoOne).build();
        CommonTypes.ProfilerPluginData data =
            CommonTypes.ProfilerPluginData.newBuilder().setName("memory-plugin").setStatus(0)
                .setData(aaa.toByteString()).build();
        return ProfilerServiceTypes.FetchDataResponse.newBuilder().setResponseId(222).setStatus(0)
            .setHasMore(false).addPluginData(data).build();
    }

    /**
     * functional testing fetchProcessData normal get no response data base on status is 0
     *
     * @tc.name: fetchProcessData
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_fetchProcessData_0003
     * @tc.desc: fetchProcessData
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-005
     */
    @Test
    public void fetchProcessDataTest03() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void fetchData(ProfilerServiceTypes.FetchDataRequest request,
                StreamObserver<ProfilerServiceTypes.FetchDataResponse> responseObserver) {
                ProfilerServiceTypes.FetchDataResponse fetchDataResponse =
                    ProfilerServiceTypes.FetchDataResponse.newBuilder().setResponseId(123456789).setStatus(0)
                        .setHasMore(false).build();
                responseObserver.onNext(fetchDataResponse);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 10009, channel);
        List<ProcessInfo> res = HiProfilerClient.getInstance().fetchProcessData(IP, 10009, 2222);
        Assert.assertEquals(res.size(), 0);
    }

    /**
     * functional testing fetchProcessData normal get no response data base on status is 0
     *
     * @tc.name: fetchProcessData
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_fetchProcessData_0004
     * @tc.desc: fetchProcessData
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-005
     */
    @Test
    public void fetchProcessDataTest04() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void fetchData(ProfilerServiceTypes.FetchDataRequest request,
                StreamObserver<ProfilerServiceTypes.FetchDataResponse> responseObserver) {
                ProfilerServiceTypes.FetchDataResponse fetchDataResponse =
                    ProfilerServiceTypes.FetchDataResponse.newBuilder().setResponseId(1).setStatus(-1).setHasMore(false)
                        .build();
                responseObserver.onNext(fetchDataResponse);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 11009, channel);
        List<ProcessInfo> res = HiProfilerClient.getInstance().fetchProcessData(IP, 11009, 2222);
        Assert.assertEquals(res.size(), 0);
    }

    /**
     * functional testing fetchProcessData
     *
     * @tc.name: fetchProcessData
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_fetchProcessData_0005
     * @tc.desc: fetchProcessData
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-005
     */
    @Test
    public void fetchProcessDataTest05() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void fetchData(ProfilerServiceTypes.FetchDataRequest request,
                StreamObserver<ProfilerServiceTypes.FetchDataResponse> responseObserver) {
                MemoryPluginResult.AppSummary sss =
                    MemoryPluginResult.AppSummary.newBuilder().setJavaHeap(getIntData()).setNativeHeap(getIntData())
                        .setCode(getIntData()).setStack(getIntData()).setGraphics(getIntData())
                        .setPrivateOther(getIntData()).setSystem(0).build();
                MemoryPluginResult.ProcessMemoryInfo processesInfoZero =
                    MemoryPluginResult.ProcessMemoryInfo.newBuilder().setPid(3114)
                        .setName("com.eg.and.AlipayGphone:push").setRssShmemKb(1).setMemsummary(sss).build();
                MemoryPluginResult.ProcessMemoryInfo processesInfoOne =
                    MemoryPluginResult.ProcessMemoryInfo.newBuilder().setPid(3114).setName("com.eg.and.AlipayGphone")
                        .setRssShmemKb(2222).build();
                MemoryPluginResult.MemoryData aaa =
                    MemoryPluginResult.MemoryData.newBuilder().addProcessesinfo(processesInfoZero)
                        .addProcessesinfo(processesInfoOne).build();
                CommonTypes.ProfilerPluginData data =
                    CommonTypes.ProfilerPluginData.newBuilder().setName("memory-plugin").setStatus(0)
                        .setData(aaa.toByteString()).build();
                ProfilerServiceTypes.FetchDataResponse fetchDataResponse =
                    ProfilerServiceTypes.FetchDataResponse.newBuilder().setResponseId(12345).setStatus(-1)
                        .setHasMore(false).addPluginData(data).build();
                responseObserver.onNext(fetchDataResponse);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 11007, channel);
        List<ProcessInfo> res = HiProfilerClient.getInstance().fetchProcessData(IP, 11007, 11111);
        Assert.assertEquals(res.size(), 0);
    }

    /**
     * get Int Data
     *
     * @return int
     */
    private int getIntData() {
        requestId++;
        if (requestId == Integer.MAX_VALUE) {
            requestId = 0;
        }
        return requestId;
    }
}
