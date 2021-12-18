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
import ohos.devtools.datasources.transport.grpc.service.ProcessPluginConfig;
import ohos.devtools.datasources.transport.grpc.service.ProcessPluginResult;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.utils.common.util.CommonUtil;
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ohos.devtools.datasources.transport.grpc.HiProfilerClient.getSha256;

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
    private String commserverName = InProcessServerBuilder.generateName();
    private final MutableHandlerRegistry commRegistry = new MutableHandlerRegistry();
    private final GrpcCleanupRule commGrpcCleanup = new GrpcCleanupRule();

    /**
     * functional testing init
     *
     * @tc.name: setUp
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_setUp_0001
     * @tc.desc: setUp
     * @tc.type: functional testing
     * @tc.require: SR-005
     * @throws IOException IOException
     */
    @Before
    public void setUp() throws IOException {
        IP = "";
        firstPort = 5001;
        secondPort = 5002;
        thirdPort = 5003;
        commserverName = InProcessServerBuilder.generateName();
        commGrpcCleanup.register(
            InProcessServerBuilder.forName(commserverName).fallbackHandlerRegistry(commRegistry)
                .directExecutor().build()
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
     * @throws IOException IOException
     */
    @Test
    public void getCapabilitiesTest01() throws IOException {
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
        String serverName = InProcessServerBuilder.generateName();
        final MutableHandlerRegistry registryer = new MutableHandlerRegistry();
        final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).fallbackHandlerRegistry(registryer).directExecutor().build()
                .start());
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        registryer.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 55835, channel);
        ProfilerServiceTypes.GetCapabilitiesResponse res =
            HiProfilerClient.getInstance().getCapabilities(IP, 55835);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 10001, channel);
        ProfilerServiceTypes.GetCapabilitiesResponse res = HiProfilerClient.getInstance()
            .getCapabilities(IP, 10001);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 11004, channel);
        ProfilerServiceTypes.GetCapabilitiesResponse res = HiProfilerClient.getInstance().getCapabilities(IP, 11004);
        List<ProfilerServiceTypes.ProfilerPluginCapability> caps = res.getCapabilitiesList();
        Assert.assertEquals(caps.size(), 2);
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
    public void processListCreateSessionTest01() {
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
        HiProfilerClient.getInstance().getProfilerClient(IP, 10002, channel);
        int res = HiProfilerClient.getInstance()
            .processListCreateSession(10002, "/data/local/tmp/libmemdata.z.so",
                212, true, DeviceType.FULL_HOS_DEVICE);
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
    public void processListCreateSessionTest02() {
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
        HiProfilerClient.getInstance().getProfilerClient(IP, 10002, channel);
        int res = HiProfilerClient.getInstance()
            .processListCreateSession(10002, "/data/local/tmp/libmemdata.z.so",
                212, false, DeviceType.FULL_HOS_DEVICE);
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
    public void processListCreateSessionTest03() {
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
        HiProfilerClient.getInstance().getProfilerClient(IP, 10003, channel);
        int res = HiProfilerClient.getInstance()
            .processListCreateSession(10003, "/data/local/tmp/libmemdata.z.so",
                212, true, DeviceType.FULL_HOS_DEVICE);

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
    public void processListCreateSessionTest04() {
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
        HiProfilerClient.getInstance().getProfilerClient(IP, 0, channel);
        int res = HiProfilerClient.getInstance()
            .processListCreateSession(0, "/data/local/tmp/libmemdata.z.so",
                212, false, DeviceType.FULL_HOS_DEVICE);
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
    public void processListCreateSessionTest05() {
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
        HiProfilerClient.getInstance().getProfilerClient(IP, -1, channel);
        int res = HiProfilerClient.getInstance()
            .processListCreateSession(-1, "/data/local/tmp/libmemdata.z.so",
                212, true, DeviceType.FULL_HOS_DEVICE);
        Assert.assertEquals(res, -1);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
        HiProfilerClient.getInstance().getProfilerClient(IP, 10002, channel);
        ProcessPluginConfig.ProcessConfig plug =
            ProcessPluginConfig.ProcessConfig.newBuilder().setReportProcessTree(true).build();
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfig = ProfilerServiceHelper
            .profilerSessionConfig(true, null, 10,
                ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE, 5000);
        String sha256 = getSha256("/data/local/tmp/libprocessplugin.z.so");
        CommonTypes.ProfilerPluginConfig plugConfig = ProfilerServiceHelper
            .profilerPluginConfig("/data/local/tmp/libprocessplugin.z.so",
                sha256, 2, plug.toByteString());
        List<CommonTypes.ProfilerPluginConfig> plugs = new ArrayList();
        plugs.add(plugConfig);
        ProfilerServiceTypes.CreateSessionRequest sessionRequest =
            ProfilerServiceHelper.createSessionRequest(CommonUtil.getRequestId(), sessionConfig, plugs);
        ProfilerServiceTypes.CreateSessionResponse createSessionResponse =
            HiProfilerClient.getInstance().requestCreateSession(IP, 10002, sessionRequest);
        int sessionId = createSessionResponse.getSessionId();
        Assert.assertEquals(sessionId, 0);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
        HiProfilerClient.getInstance().getProfilerClient(IP, 10002, channel);
        ProfilerServiceTypes.CreateSessionResponse createSessionResponse =
            HiProfilerClient.getInstance().requestCreateSession(IP, 10002, null);
        int sessionId = createSessionResponse.getSessionId();
        Assert.assertEquals(sessionId, 0);
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
                    ProfilerServiceTypes.CreateSessionResponse.newBuilder().setSessionId(0).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
        HiProfilerClient.getInstance().getProfilerClient(IP, 10002, channel);
        ProcessPluginConfig.ProcessConfig plug =
            ProcessPluginConfig.ProcessConfig.newBuilder().setReportProcessTree(true).build();
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfig = ProfilerServiceHelper
            .profilerSessionConfig(true, null, 10,
                ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE, 5000);
        String sha256 = getSha256("/data/local/tmp/libprocessplugin.z.so");
        CommonTypes.ProfilerPluginConfig plugConfig = ProfilerServiceHelper
            .profilerPluginConfig("/data/local/tmp/libprocessplugin.z.so",
                sha256, 2, plug.toByteString());
        List<CommonTypes.ProfilerPluginConfig> plugs = new ArrayList();
        plugs.add(plugConfig);
        ProfilerServiceTypes.CreateSessionRequest sessionRequest =
            ProfilerServiceHelper.createSessionRequest(CommonUtil.getRequestId(), sessionConfig, plugs);
        ProfilerServiceTypes.CreateSessionResponse createSessionResponse =
            HiProfilerClient.getInstance().requestCreateSession(IP, 0, sessionRequest);
        int sessionId = createSessionResponse.getSessionId();
        Assert.assertEquals(sessionId, -1);
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
                    ProfilerServiceTypes.CreateSessionResponse.newBuilder().setSessionId(-1).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
        HiProfilerClient.getInstance().getProfilerClient(IP, 0, channel);
        ProfilerServiceTypes.CreateSessionResponse createSessionResponse =
            HiProfilerClient.getInstance().requestCreateSession(IP, -1, null);
        int sessionId = createSessionResponse.getSessionId();
        Assert.assertEquals(sessionId, -1);
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
                    ProfilerServiceTypes.CreateSessionResponse.newBuilder().setSessionId(0).setStatus(-1).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
        HiProfilerClient.getInstance().getProfilerClient(IP, 65536, channel);
        ProcessPluginConfig.ProcessConfig plug =
            ProcessPluginConfig.ProcessConfig.newBuilder().setReportProcessTree(true).build();
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfig = ProfilerServiceHelper
            .profilerSessionConfig(true, null, 10,
                ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE, 5000);
        String sha256 = getSha256("/data/local/tmp/libprocessplugin.z.so");
        CommonTypes.ProfilerPluginConfig plugConfig = ProfilerServiceHelper
            .profilerPluginConfig("/data/local/tmp/libprocessplugin.z.so", sha256,
                2, plug.toByteString());
        List<CommonTypes.ProfilerPluginConfig> plugs = new ArrayList();
        plugs.add(plugConfig);
        ProfilerServiceTypes.CreateSessionRequest sessionRequest =
            ProfilerServiceHelper.createSessionRequest(CommonUtil.getRequestId(), sessionConfig, plugs);
        ProfilerServiceTypes.CreateSessionResponse createSessionResponse =
            HiProfilerClient.getInstance().requestCreateSession(IP, 65536, sessionRequest);
        int sessionId = createSessionResponse.getSessionId();
        Assert.assertEquals(sessionId, -1);
    }

    /**
     * functional testing requestStartSession normal based on status is 0
     *
     * @tc.name: requestStartSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestStartSession_0001
     * @tc.desc: requestStartSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-001
     * @throws IOException IOException
     */
    @Test
    public void requestStartSessionTest01() throws IOException {
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
        final GrpcCleanupRule grpcClean = new GrpcCleanupRule();
        final MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();
        String server = InProcessServerBuilder.generateName();
        ManagedChannel channel =
            grpcClean.register(InProcessChannelBuilder.forName(server).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
        grpcClean.register(
            InProcessServerBuilder.forName(server).fallbackHandlerRegistry(serviceRegistry).directExecutor().build()
                .start());
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 10009, channel);
        boolean res = HiProfilerClient.getInstance().requestStartSession(IP, 10009, 1);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, -1, channel);
        boolean res = HiProfilerClient.getInstance().requestStartSession(IP, -1, 2);
        Assert.assertFalse(res);
    }

    /**
     * functional testing requestStopSession normal based on status is 0
     *
     * @tc.name: requestStopSession
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_requestStopSession_0001
     * @tc.desc: requestStopSession
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-002
     * @throws IOException IOException
     */
    @Test
    public void requestStopSessionTest01() throws IOException {
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
        final GrpcCleanupRule stopgrpcCleanup = new GrpcCleanupRule();
        final MutableHandlerRegistry stopServiceRegistry = new MutableHandlerRegistry();
        String stopServerName = InProcessServerBuilder.generateName();
        stopgrpcCleanup.register(
            InProcessServerBuilder.forName(stopServerName).fallbackHandlerRegistry(stopServiceRegistry)
                .directExecutor().build()
                .start());
        ManagedChannel channel =
            stopgrpcCleanup.register(InProcessChannelBuilder.forName(stopServerName).directExecutor().build());
        stopServiceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 10112, channel);
        boolean res = HiProfilerClient.getInstance().requestStopSession(IP, 10112, 111, false);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, -1, channel);

        boolean res = HiProfilerClient.getInstance().requestStopSession(IP, -1, 111, true);
        Assert.assertFalse(res);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 11121, channel);
        boolean res = HiProfilerClient.getInstance().requestDestroySession(IP, 11121, 222);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, -1, channel);
        boolean res = HiProfilerClient.getInstance().requestDestroySession(IP, -1, 222);
        Assert.assertFalse(res);
    }

    /**
     * functional testing fetchProcessData normal Single get 3
     *
     * @tc.name: fetchProcessData
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_fetchProcessData_0001
     * @tc.desc: fetchProcessData
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-005
     * @throws IOException IOException
     */
    @Test
    public void fetchProcessDataTest01() throws IOException {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void fetchData(ProfilerServiceTypes.FetchDataRequest request,
                StreamObserver<ProfilerServiceTypes.FetchDataResponse> responseObserver) {
                ProcessPluginResult.ProcessInfo processOne =
                    ProcessPluginResult.ProcessInfo.newBuilder().setName("com.eg.and.AlipayGphone:push").setPid(31141)
                        .build();
                ProcessPluginResult.ProcessInfo processTwo =
                    ProcessPluginResult.ProcessInfo.newBuilder().setName("com.eg.and.AlipayGphone:push").setPid(31142)
                        .build();
                ProcessPluginResult.ProcessInfo processThree =
                    ProcessPluginResult.ProcessInfo.newBuilder().setName("com.eg.and.AlipayGphone:push").setPid(31143)
                        .build();
                ProcessPluginResult.ProcessData data =
                    ProcessPluginResult.ProcessData.newBuilder().addProcessesinfo(0, processOne)
                        .addProcessesinfo(1, processTwo).addProcessesinfo(2, processThree).build();
                CommonTypes.ProfilerPluginData pluginData =
                    CommonTypes.ProfilerPluginData.newBuilder().setName("process-plugin").setStatus(0)
                        .setData(data.toByteString()).build();
                ProfilerServiceTypes.FetchDataResponse fetchDataResponse =
                    ProfilerServiceTypes.FetchDataResponse.newBuilder().setResponseId(123456789).setStatus(0)
                        .setHasMore(false).addPluginData(pluginData).build();
                responseObserver.onNext(fetchDataResponse);
                responseObserver.onCompleted();
            }
        };
        GrpcCleanupRule cleanupGrpc = new GrpcCleanupRule();
        MutableHandlerRegistry registry = new MutableHandlerRegistry();
        String name = InProcessServerBuilder.generateName();
        cleanupGrpc.register(
            InProcessServerBuilder.forName(name).fallbackHandlerRegistry(registry).directExecutor().build().start());
        ManagedChannel channel = cleanupGrpc.register(InProcessChannelBuilder.forName(name).directExecutor().build());
        registry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 65511, channel);
        List<ProcessInfo> res = HiProfilerClient.getInstance().fetchProcessData(IP, 65511, 11111);
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
                    ProcessPluginResult.ProcessInfo processOne =
                        ProcessPluginResult.ProcessInfo.newBuilder().setName("com.eg.and.AlipayGphone:push")
                            .setPid(31141).build();
                    ProcessPluginResult.ProcessInfo processTwo =
                        ProcessPluginResult.ProcessInfo.newBuilder().setName("com.eg.and.AlipayGphone:push")
                            .setPid(31142).build();
                    ProcessPluginResult.ProcessInfo processThree =
                        ProcessPluginResult.ProcessInfo.newBuilder().setName("com.eg.and.AlipayGphone:push")
                            .setPid(31143).build();
                    ProcessPluginResult.ProcessData data =
                        ProcessPluginResult.ProcessData.newBuilder().addProcessesinfo(0, processOne)
                            .addProcessesinfo(1, processTwo).addProcessesinfo(2, processThree).build();
                    CommonTypes.ProfilerPluginData pluginData =
                        CommonTypes.ProfilerPluginData.newBuilder().setName("process-plugin").setStatus(0)
                            .setData(data.toByteString()).build();
                    ProfilerServiceTypes.FetchDataResponse fetchDataResponse =
                        ProfilerServiceTypes.FetchDataResponse.newBuilder().setResponseId(123456789).setStatus(0)
                            .setHasMore(false).addPluginData(pluginData).build();
                    responseObserver.onNext(fetchDataResponse);
                    responseObserver.onCompleted();
                } else {
                    ProcessPluginResult.ProcessInfo processOne =
                        ProcessPluginResult.ProcessInfo.newBuilder().setName("com.eg.and.AlipayGphone:push")
                            .setPid(31141).build();
                    ProcessPluginResult.ProcessInfo processTwo =
                        ProcessPluginResult.ProcessInfo.newBuilder().setName("com.eg.and.AlipayGphone:push")
                            .setPid(31142).build();
                    ProcessPluginResult.ProcessData data =
                        ProcessPluginResult.ProcessData.newBuilder().addProcessesinfo(0, processOne)
                            .addProcessesinfo(1, processTwo).build();
                    CommonTypes.ProfilerPluginData pluginData =
                        CommonTypes.ProfilerPluginData.newBuilder().setName("process-plugin").setStatus(0)
                            .setData(data.toByteString()).build();
                    ProfilerServiceTypes.FetchDataResponse fetchDataResponse =
                        ProfilerServiceTypes.FetchDataResponse.newBuilder().setResponseId(222).setStatus(0)
                            .setHasMore(false).addPluginData(pluginData).build();
                    responseObserver.onNext(fetchDataResponse);
                    responseObserver.onCompleted();
                }
            }
        };
        getFetchProcessData(getFeatureImpl);
    }

    private void getFetchProcessData(MockProfilerServiceImplBase getFeatureImpl) {
        ManagedChannel channel =
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 10008, channel);
        List<ProcessInfo> res = HiProfilerClient.getInstance().fetchProcessData(IP, 10008, 1);
        Assert.assertEquals(res.size(), 3);
        List<ProcessInfo> ress = HiProfilerClient.getInstance().fetchProcessData(IP, 10008, 22);
        Assert.assertEquals(ress.size(), 2);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
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
                    ProfilerServiceTypes.FetchDataResponse
                        .newBuilder().setResponseId(1).setStatus(-1).setHasMore(false)
                        .build();
                responseObserver.onNext(fetchDataResponse);
                responseObserver.onCompleted();
            }
        };
        ManagedChannel channel =
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
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
            commGrpcCleanup.register(InProcessChannelBuilder.forName(commserverName).directExecutor().build());
        commRegistry.addService(getFeatureImpl);
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

    /**
     * functional testing keepSessionTest
     *
     * @tc.name: fetchProcessData
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_keepSession_0001
     * @tc.desc: keepSessionTest
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-005
     * @throws IOException IOException
     */
    @Test
    public void keepSessionTest01() throws IOException {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void keepSession(ProfilerServiceTypes.KeepSessionRequest request,
                StreamObserver<ProfilerServiceTypes.KeepSessionResponse> responseObserver) {
                ProfilerServiceTypes.KeepSessionResponse reply =
                    ProfilerServiceTypes.KeepSessionResponse.newBuilder().setStatus(0).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        String keepServerName = InProcessServerBuilder.generateName();
        final MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();
        final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(keepServerName).directExecutor().build());
        grpcCleanup.register(
            InProcessServerBuilder.forName(keepServerName)
                .fallbackHandlerRegistry(serviceRegistry).directExecutor().build()
                .start());
        serviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, thirdPort, channel);
        ProfilerServiceTypes.KeepSessionResponse res =
            HiProfilerClient.getInstance().keepSession(IP, 0, 1);
        Assert.assertNull(res);
    }

    /**
     * functional testing keepSessionTest
     *
     * @tc.name: fetchProcessData
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_keepSession_0002
     * @tc.desc: keepSessionTest
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-005
     * @throws IOException IOException
     */
    @Test
    public void keepSessionTest02() throws IOException {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void keepSession(ProfilerServiceTypes.KeepSessionRequest request,
                StreamObserver<ProfilerServiceTypes.KeepSessionResponse> responseObserver) {
                ProfilerServiceTypes.KeepSessionResponse reply =
                    ProfilerServiceTypes.KeepSessionResponse.newBuilder().setStatus(1).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        String serverName = InProcessServerBuilder.generateName();
        final MutableHandlerRegistry keepServiceRegistry = new MutableHandlerRegistry();
        final GrpcCleanupRule keepGrpcCleanup = new GrpcCleanupRule();
        ManagedChannel channel =
            keepGrpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        keepGrpcCleanup.register(
            InProcessServerBuilder.forName(serverName)
                .fallbackHandlerRegistry(keepServiceRegistry).directExecutor().build()
                .start());
        keepServiceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 65514, channel);
        ProfilerServiceTypes.KeepSessionResponse res =
            HiProfilerClient.getInstance().keepSession(IP, 65514, 1);
        int status = res.getStatus();
        Assert.assertEquals(status, 1);
    }

    /**
     * functional testing keepSessionTest
     *
     * @tc.name: fetchProcessData
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_keepSession_0003
     * @tc.desc: keepSessionTest
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-005
     * @throws IOException IOException
     */
    @Test
    public void keepSessionTest03() throws IOException {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void keepSession(ProfilerServiceTypes.KeepSessionRequest request,
                StreamObserver<ProfilerServiceTypes.KeepSessionResponse> responseObserver) {
                ProfilerServiceTypes.KeepSessionResponse reply =
                    ProfilerServiceTypes.KeepSessionResponse.newBuilder().setStatus(0).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        String keepServerName = InProcessServerBuilder.generateName();
        final MutableHandlerRegistry keepServiceRegistry = new MutableHandlerRegistry();
        final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
        keepServiceRegistry.addService(getFeatureImpl);
        grpcCleanup.register(
            InProcessServerBuilder.forName(keepServerName)
                .fallbackHandlerRegistry(keepServiceRegistry).directExecutor().build()
                .start());
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(keepServerName).directExecutor().build());
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 65536, channel);
        ProfilerServiceTypes.KeepSessionResponse res =
            HiProfilerClient.getInstance().keepSession(IP, 65536, 6565);
        Assert.assertEquals(res, null);
    }

    /**
     * functional testing keepSessionTest
     *
     * @tc.name: fetchProcessData
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_keepSession_0004
     * @tc.desc: keepSessionTest
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-005
     * @throws IOException IOException
     */
    @Test
    public void keepSessionTest04() throws IOException {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void keepSession(ProfilerServiceTypes.KeepSessionRequest request,
                StreamObserver<ProfilerServiceTypes.KeepSessionResponse> responseObserver) {
                ProfilerServiceTypes.KeepSessionResponse reply =
                    ProfilerServiceTypes.KeepSessionResponse.newBuilder().setStatus(-1).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        String keepServerName = InProcessServerBuilder.generateName();
        final MutableHandlerRegistry keppserviceRegistry = new MutableHandlerRegistry();
        final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
        ManagedChannel channel =
            grpcCleanup.register(InProcessChannelBuilder.forName(keepServerName).directExecutor().build());
        grpcCleanup.register(
            InProcessServerBuilder.forName(keepServerName)
                .fallbackHandlerRegistry(keppserviceRegistry).directExecutor().build()
                .start());
        keppserviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, -1, channel);
        ProfilerServiceTypes.KeepSessionResponse res = HiProfilerClient.getInstance().keepSession(IP, -1, 2);
        Assert.assertEquals(res, null);
    }

    /**
     * functional testing keepSessionTest
     *
     * @tc.name: fetchProcessData
     * @tc.number: OHOS_JAVA_grpc_HiProfilerClient_keepSession_0005
     * @tc.desc: keepSessionTest
     * @tc.type: functional testing
     * @tc.require: SR-005-AR-005
     * @throws IOException IOException
     */
    @Test
    public void keepSessionTest05() throws IOException {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void keepSession(ProfilerServiceTypes.KeepSessionRequest request,
                StreamObserver<ProfilerServiceTypes.KeepSessionResponse> responseObserver) {
                ProfilerServiceTypes.KeepSessionResponse reply =
                    ProfilerServiceTypes.KeepSessionResponse.getDefaultInstance();
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException exception) {
                    Assert.assertFalse(false);
                }
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
        };
        String keepserverName = InProcessServerBuilder.generateName();
        final MutableHandlerRegistry keepserviceRegistry = new MutableHandlerRegistry();
        final GrpcCleanupRule keepGrpcCleanup = new GrpcCleanupRule();
        ManagedChannel channel =
            keepGrpcCleanup.register(InProcessChannelBuilder.forName(keepserverName).directExecutor().build());
        keepGrpcCleanup.register(
            InProcessServerBuilder.forName(keepserverName)
                .fallbackHandlerRegistry(keepserviceRegistry).directExecutor().build()
                .start());
        keepserviceRegistry.addService(getFeatureImpl);
        ProfilerClient client = HiProfilerClient.getInstance().getProfilerClient(IP, 622, channel);
        ProfilerServiceTypes.KeepSessionResponse res =
            HiProfilerClient.getInstance().keepSession(IP, 622, 222);
        int status = res.getStatus();
        Assert.assertEquals(status, 0);
    }
}
