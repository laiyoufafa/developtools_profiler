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

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.grpc.util.MutableHandlerRegistry;
import ohos.devtools.datasources.transport.grpc.service.BytracePluginConfigOuterClass;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.utils.common.util.BeanUtil;
import ohos.devtools.datasources.utils.common.util.CommonUtil;
import ohos.devtools.views.common.LayoutConstants;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Profiler Client Test
 */
public class ProfilerClientTest {
    private String ip;
    private int port;
    private String serverName;
    private ManagedChannel channel;
    private final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
    private final MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();

    /**
     * functional testing init
     *
     * @tc.name: setUp
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_setUp_0001
     * @tc.desc: setUp
     * @tc.type: functional testing
     * @tc.require: SR-005
     * @throws IOException throw IOException
     */
    @Before
    public void setUp() throws IOException {
        ip = "";
        port = 5001;
        serverName = InProcessServerBuilder.generateName();
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName).fallbackHandlerRegistry(serviceRegistry).directExecutor().build()
                .start());
        MockProfilerServiceImplBase getFeatureImpl = getMockProfilerServiceImplBase();
        channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());
        serviceRegistry.addService(getFeatureImpl);
    }

    @NotNull
    private MockProfilerServiceImplBase getMockProfilerServiceImplBase() {
        MockProfilerServiceImplBase getFeatureImpl = new MockProfilerServiceImplBase() {
            @Override
            public void getCapabilities(ProfilerServiceTypes.GetCapabilitiesRequest request,
                StreamObserver<ProfilerServiceTypes.GetCapabilitiesResponse> responseObserver) {
                ProfilerServiceTypes.GetCapabilitiesResponse reply = getGetCapabilitiesResponse();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }

            @Override
            public void createSession(ProfilerServiceTypes.CreateSessionRequest request,
                StreamObserver<ProfilerServiceTypes.CreateSessionResponse> responseObserver) {
                ProfilerServiceTypes.CreateSessionResponse reply = getCreateSessionResponse();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }

            @Override
            public void startSession(ProfilerServiceTypes.StartSessionRequest request,
                StreamObserver<ProfilerServiceTypes.StartSessionResponse> responseObserver) {
                ProfilerServiceTypes.StartSessionResponse reply = getStartSessionResponse();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }

            @Override
            public void stopSession(ProfilerServiceTypes.StopSessionRequest request,
                StreamObserver<ProfilerServiceTypes.StopSessionResponse> responseObserver) {
                ProfilerServiceTypes.StopSessionResponse reply =
                    ProfilerServiceTypes.StopSessionResponse.newBuilder().setStatus(0).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }

            @Override
            public void destroySession(ProfilerServiceTypes.DestroySessionRequest request,
                StreamObserver<ProfilerServiceTypes.DestroySessionResponse> responseObserver) {
                ProfilerServiceTypes.DestroySessionResponse reply =
                    ProfilerServiceTypes.DestroySessionResponse.newBuilder().setStatus(0).build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }

            @Override
            public void fetchData(ProfilerServiceTypes.FetchDataRequest request,
                StreamObserver<ProfilerServiceTypes.FetchDataResponse> responseObserver) {
                fetchDataResponseObserver(responseObserver);
            }
        };
        return getFeatureImpl;
    }

    public void fetchDataResponseObserver(StreamObserver<ProfilerServiceTypes.FetchDataResponse> responseObserver) {
        ProfilerServiceTypes.FetchDataResponse fetchDataResponse =
                ProfilerServiceTypes.FetchDataResponse.newBuilder().setResponseId(1).setStatus(-1).setHasMore(false)
                        .build();
        responseObserver.onNext(fetchDataResponse);
        responseObserver.onCompleted();
    }

    private ProfilerServiceTypes.StartSessionResponse getStartSessionResponse() {
        CommonTypes.ProfilerPluginState profilerPluginState = CommonTypes.ProfilerPluginState.newBuilder().build();
        ProfilerServiceTypes.StartSessionResponse reply =
            ProfilerServiceTypes.StartSessionResponse.newBuilder().setStatus(0).addPluginStatus(profilerPluginState)
                .build();
        return reply;
    }

    private ProfilerServiceTypes.CreateSessionResponse getCreateSessionResponse() {
        ProfilerServiceTypes.CreateSessionResponse reply =
            ProfilerServiceTypes.CreateSessionResponse.getDefaultInstance();
        return reply;
    }

    private ProfilerServiceTypes.GetCapabilitiesResponse getGetCapabilitiesResponse() {
        ProfilerServiceTypes.ProfilerPluginCapability pluginCapability = ProfilerServiceTypes.ProfilerPluginCapability
            .newBuilder(ProfilerServiceTypes.ProfilerPluginCapability.newBuilder().setName("test0")
                .setPath("/data/local/tmp/libmemdata.z.so").build()).build();
        ProfilerServiceTypes.GetCapabilitiesResponse reply =
            ProfilerServiceTypes.GetCapabilitiesResponse.newBuilder().addCapabilities(pluginCapability).setStatus(0)
                .build();
        return reply;
    }

    /**
     * functional testing
     *
     * @tc.name: getCapabilitiesTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_getCapabilitiesTest_0001
     * @tc.desc: get Capabilities Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void getCapabilitiesTest01() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        ProfilerServiceTypes.GetCapabilitiesResponse capabilities = profilerClient.getCapabilities(
            ProfilerServiceTypes.GetCapabilitiesRequest.newBuilder().setRequestId(CommonUtil.getRequestId()).build());
        Assert.assertNotNull(capabilities);
    }

    /**
     * functional testing
     *
     * @tc.name: getCapabilitiesTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_getCapabilitiesTest_0002
     * @tc.desc: get Capabilities Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void getCapabilitiesTest02() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        ProfilerServiceTypes.GetCapabilitiesResponse capabilities = profilerClient.getCapabilities(null);
        Assert.assertNotNull(capabilities);
    }

    /**
     * functional testing
     *
     * @tc.name: getCapabilitiesTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_getCapabilitiesTest_0003
     * @tc.desc: get Capabilities Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void getCapabilitiesTest03() {
        ProfilerClient profilerClient = new ProfilerClient(ip, 0, channel);
        ProfilerServiceTypes.GetCapabilitiesResponse capabilities = profilerClient.getCapabilities(
            ProfilerServiceTypes.GetCapabilitiesRequest.newBuilder().setRequestId(CommonUtil.getRequestId()).build());
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: getCapabilitiesTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_getCapabilitiesTest_0004
     * @tc.desc: get Capabilities Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void getCapabilitiesTest04() {
        ProfilerClient profilerClient = new ProfilerClient(null, -1, channel);
        ProfilerServiceTypes.GetCapabilitiesResponse capabilities = profilerClient.getCapabilities(
            ProfilerServiceTypes.GetCapabilitiesRequest.newBuilder().setRequestId(CommonUtil.getRequestId()).build());
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: getCapabilitiesTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_getCapabilitiesTest_0005
     * @tc.desc: get Capabilities Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void getCapabilitiesTest05() {
        ProfilerClient profilerClient = new ProfilerClient(null, port, channel);
        ProfilerServiceTypes.GetCapabilitiesResponse capabilities = profilerClient.getCapabilities(
            ProfilerServiceTypes.GetCapabilitiesRequest.newBuilder().setRequestId(CommonUtil.getRequestId()).build());
        Assert.assertNotNull(capabilities);
    }

    /**
     * functional testing
     *
     * @tc.name: createSessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_createSessionTest_0001
     * @tc.desc: create Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void createSessionTest01() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        BytracePluginConfigOuterClass.BytracePluginConfig.Builder build =
            BytracePluginConfigOuterClass.BytracePluginConfig.newBuilder();
        BytracePluginConfigOuterClass.BytracePluginConfig config = build.build();
        byte[] configByte = BeanUtil.serializeByCodedOutPutStream(config);
        ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig bf =
            ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.newBuilder().setPages(LayoutConstants.NUMBER_THREAD)
                .setPolicy(ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE).build();
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfig =
            ProfilerServiceTypes.ProfilerSessionConfig.newBuilder()
                .setSessionMode(ProfilerServiceTypes.ProfilerSessionConfig.Mode.ONLINE).addBuffers(bf).build();
        CommonTypes.ProfilerPluginConfig plugConfig =
            CommonTypes.ProfilerPluginConfig.newBuilder().setName("pluginName")
                .setConfigData(ByteString.copyFrom(configByte)).build();
        ProfilerServiceTypes.CreateSessionRequest request =
            ProfilerServiceTypes.CreateSessionRequest.newBuilder().setRequestId(1).setSessionConfig(sessionConfig)
                .addPluginConfigs(plugConfig).build();
        ProfilerServiceTypes.CreateSessionResponse session = profilerClient.createSession(request);
        Assert.assertNotNull(session);
    }

    /**
     * functional testing
     *
     * @tc.name: createSessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_createSessionTest_0002
     * @tc.desc: create Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void createSessionTest02() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        ProfilerServiceTypes.CreateSessionResponse session = profilerClient.createSession(null);
        Assert.assertNotNull(session);
    }

    /**
     * functional testing
     *
     * @tc.name: createSessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_createSessionTest_0003
     * @tc.desc: create Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void createSessionTest03() {
        ProfilerClient profilerClient = new ProfilerClient(ip, -1, channel);
        BytracePluginConfigOuterClass.BytracePluginConfig.Builder build =
            BytracePluginConfigOuterClass.BytracePluginConfig.newBuilder();
        BytracePluginConfigOuterClass.BytracePluginConfig config = build.build();
        byte[] configByte = BeanUtil.serializeByCodedOutPutStream(config);
        ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig bf =
            ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.newBuilder().setPages(LayoutConstants.NUMBER_THREAD)
                .setPolicy(ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE).build();
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfig =
            ProfilerServiceTypes.ProfilerSessionConfig.newBuilder()
                .setSessionMode(ProfilerServiceTypes.ProfilerSessionConfig.Mode.ONLINE).addBuffers(bf).build();
        CommonTypes.ProfilerPluginConfig plugConfig =
            CommonTypes.ProfilerPluginConfig.newBuilder().setName("pluginName")
                .setConfigData(ByteString.copyFrom(configByte)).build();
        ProfilerServiceTypes.CreateSessionRequest request =
            ProfilerServiceTypes.CreateSessionRequest.newBuilder().setRequestId(1).setSessionConfig(sessionConfig)
                .addPluginConfigs(plugConfig).build();
        ProfilerServiceTypes.CreateSessionResponse response = profilerClient.createSession(request);
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: createSessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_createSessionTest_0004
     * @tc.desc: create Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void createSessionTest04() {
        ProfilerClient profilerClient = new ProfilerClient(null, port, channel);
        ProfilerServiceTypes.CreateSessionResponse session = profilerClient.createSession(null);
        Assert.assertNotNull(session);
    }

    /**
     * functional testing
     *
     * @tc.name: createSessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_createSessionTest_0005
     * @tc.desc: create Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void createSessionTest05() {
        ProfilerClient profilerClient = new ProfilerClient(ip, 0, channel);
        BytracePluginConfigOuterClass.BytracePluginConfig.Builder build =
            BytracePluginConfigOuterClass.BytracePluginConfig.newBuilder();
        BytracePluginConfigOuterClass.BytracePluginConfig config = build.build();
        byte[] configByte = BeanUtil.serializeByCodedOutPutStream(config);
        ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig bf =
            ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.newBuilder().setPages(LayoutConstants.NUMBER_THREAD)
                .setPolicy(ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE).build();
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfig =
            ProfilerServiceTypes.ProfilerSessionConfig.newBuilder()
                .setSessionMode(ProfilerServiceTypes.ProfilerSessionConfig.Mode.ONLINE).addBuffers(bf).build();
        CommonTypes.ProfilerPluginConfig plugConfig =
            CommonTypes.ProfilerPluginConfig.newBuilder().setName("pluginName")
                .setConfigData(ByteString.copyFrom(configByte)).build();
        ProfilerServiceTypes.CreateSessionRequest request =
            ProfilerServiceTypes.CreateSessionRequest.newBuilder().setRequestId(1).setSessionConfig(sessionConfig)
                .addPluginConfigs(plugConfig).build();
        ProfilerServiceTypes.CreateSessionResponse session = profilerClient.createSession(request);
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: startSessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_startSessionTest_0001
     * @tc.desc: start Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void startSessionTest01() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        ProfilerServiceTypes.StartSessionRequest requestStartSession =
            ProfilerServiceHelper.startSessionRequest(CommonUtil.getRequestId(), 1, new ArrayList<>());
        ProfilerServiceTypes.StartSessionResponse startSessionResponse =
            profilerClient.startSession(requestStartSession);
        Assert.assertNotNull(startSessionResponse);
    }

    /**
     * functional testing
     *
     * @tc.name: startSessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_startSessionTest_0002
     * @tc.desc: start Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void startSessionTest02() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        ProfilerServiceTypes.StartSessionRequest requestStartSession =
            ProfilerServiceHelper.startSessionRequest(CommonUtil.getRequestId(), 1, null);
        ProfilerServiceTypes.StartSessionResponse startSessionResponse =
            profilerClient.startSession(requestStartSession);
        Assert.assertNotNull(startSessionResponse);
    }

    /**
     * functional testing
     *
     * @tc.name: startSessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_startSessionTest_0003
     * @tc.desc: start Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void startSessionTest03() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        ProfilerServiceTypes.StartSessionRequest requestStartSession =
            ProfilerServiceHelper.startSessionRequest(CommonUtil.getRequestId(), -1, new ArrayList<>());
        ProfilerServiceTypes.StartSessionResponse startSessionResponse =
            profilerClient.startSession(requestStartSession);
        Assert.assertNotNull(startSessionResponse);
    }

    /**
     * functional testing
     *
     * @tc.name: startSessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_startSessionTest_0004
     * @tc.desc: start Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void startSessionTest04() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        ProfilerServiceTypes.StartSessionRequest requestStartSession =
            ProfilerServiceHelper.startSessionRequest(-1, 0, new ArrayList<>());
        ProfilerServiceTypes.StartSessionResponse startSessionResponse =
            profilerClient.startSession(requestStartSession);
        Assert.assertNotNull(startSessionResponse);
    }

    /**
     * functional testing
     *
     * @tc.name: startSessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_startSessionTest_0005
     * @tc.desc: start Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void startSessionTest05() {
        ProfilerClient profilerClient = new ProfilerClient(null, 0, channel);
        ProfilerServiceTypes.StartSessionResponse startSessionResponse = profilerClient.startSession(null);
        Assert.assertNotNull(startSessionResponse);
    }

    /**
     * functional testing
     *
     * @tc.name: fetchDataTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_fetchDataTest_0001
     * @tc.desc: fetch Data Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void fetchDataTest01() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        ProfilerServiceTypes.FetchDataRequest fetchDataRequest =
            ProfilerServiceHelper.fetchDataRequest(CommonUtil.getRequestId(), 1, null);
        Iterator<ProfilerServiceTypes.FetchDataResponse> fetchDataResponseIterator =
            profilerClient.fetchData(fetchDataRequest);
        Assert.assertNotNull(fetchDataResponseIterator);
    }

    /**
     * functional testing
     *
     * @tc.name: fetchDataTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_fetchDataTest_0002
     * @tc.desc: fetch Data Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void fetchDataTest02() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        Iterator<ProfilerServiceTypes.FetchDataResponse> fetchDataResponseIterator = profilerClient.fetchData(null);
        Assert.assertNotNull(fetchDataResponseIterator);
    }

    /**
     * functional testing
     *
     * @tc.name: fetchDataTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_fetchDataTest_0003
     * @tc.desc: fetch Data Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void fetchDataTest03() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        ProfilerServiceTypes.FetchDataRequest fetchDataRequest =
            ProfilerServiceHelper.fetchDataRequest(CommonUtil.getRequestId(), -1, null);
        Iterator<ProfilerServiceTypes.FetchDataResponse> fetchDataResponseIterator =
            profilerClient.fetchData(fetchDataRequest);
        Assert.assertNotNull(fetchDataResponseIterator);
    }

    /**
     * functional testing
     *
     * @tc.name: fetchDataTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_fetchDataTest_0004
     * @tc.desc: fetch Data Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void fetchDataTest04() {
        ProfilerClient profilerClient = new ProfilerClient(null, -1, channel);
        ProfilerServiceTypes.FetchDataRequest fetchDataRequest =
            ProfilerServiceHelper.fetchDataRequest(CommonUtil.getRequestId(), 0, null);
        Iterator<ProfilerServiceTypes.FetchDataResponse> fetchDataResponseIterator =
            profilerClient.fetchData(fetchDataRequest);
        Assert.assertNotNull(fetchDataResponseIterator);
    }

    /**
     * functional testing
     *
     * @tc.name: fetchDataTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_fetchDataTest_0005
     * @tc.desc: fetch Data Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void fetchDataTest05() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        ProfilerServiceTypes.FetchDataRequest fetchDataRequest = ProfilerServiceHelper.fetchDataRequest(-1, -1, null);
        Iterator<ProfilerServiceTypes.FetchDataResponse> fetchDataResponseIterator =
            profilerClient.fetchData(fetchDataRequest);
        Assert.assertNotNull(fetchDataResponseIterator);
    }

    /**
     * functional testing
     *
     * @tc.name: stopSessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_stopSessionTest_0001
     * @tc.desc: stop Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void stopSessionTest01() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        ProfilerServiceTypes.StopSessionRequest stopSession =
            ProfilerServiceHelper.stopSessionRequest(CommonUtil.getRequestId(), 1);
        ProfilerServiceTypes.StopSessionResponse stopSessionResponse = profilerClient.stopSession(stopSession);
        Assert.assertNotNull(stopSessionResponse);
    }

    /**
     * functional testing
     *
     * @tc.name: stopSessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_stopSessionTest_0002
     * @tc.desc: stop Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void stopSessionTest02() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        ProfilerServiceTypes.StopSessionResponse stopSessionResponse = profilerClient.stopSession(null);
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: stopSessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_stopSessionTest_0003
     * @tc.desc: stop Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void stopSessionTest03() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        ProfilerServiceTypes.StopSessionRequest stopSession =
            ProfilerServiceHelper.stopSessionRequest(CommonUtil.getRequestId(), -1);
        ProfilerServiceTypes.StopSessionResponse stopSessionResponse = profilerClient.stopSession(stopSession);
        Assert.assertNotNull(stopSessionResponse);
    }

    /**
     * functional testing
     *
     * @tc.name: stopSessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_stopSessionTest_0004
     * @tc.desc: stop Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void stopSessionTest04() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        ProfilerServiceTypes.StopSessionRequest stopSession = ProfilerServiceHelper.stopSessionRequest(-1, 1);
        ProfilerServiceTypes.StopSessionResponse stopSessionResponse = profilerClient.stopSession(stopSession);
        Assert.assertNotNull(stopSessionResponse);
    }

    /**
     * functional testing
     *
     * @tc.name: stopSessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_stopSessionTest_0005
     * @tc.desc: stop Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void stopSessionTest05() {
        ProfilerClient profilerClient = new ProfilerClient(null, -1, channel);
        ProfilerServiceTypes.StopSessionRequest stopSession = ProfilerServiceHelper.stopSessionRequest(-1, -1);
        ProfilerServiceTypes.StopSessionResponse stopSessionResponse = profilerClient.stopSession(stopSession);
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: destroySessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_destroySessionTest_0001
     * @tc.desc: destroy Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void destroySessionTest01() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        ProfilerServiceTypes.DestroySessionRequest req =
            ProfilerServiceHelper.destroySessionRequest(CommonUtil.getRequestId(), 1);
        ProfilerServiceTypes.DestroySessionResponse destroySessionResponse = profilerClient.destroySession(req);
        Assert.assertEquals(destroySessionResponse.getStatus(), 0);
    }

    /**
     * functional testing
     *
     * @tc.name: destroySessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_destroySessionTest_0002
     * @tc.desc: destroy Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void destroySessionTest02() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        ProfilerServiceTypes.DestroySessionResponse destroySessionResponse = profilerClient.destroySession(null);
        Assert.assertNotNull(destroySessionResponse);
    }

    /**
     * functional testing
     *
     * @tc.name: destroySessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_destroySessionTest_0003
     * @tc.desc: destroy Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void destroySessionTest03() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        ProfilerServiceTypes.DestroySessionRequest req =
            ProfilerServiceHelper.destroySessionRequest(CommonUtil.getRequestId(), -1);
        ProfilerServiceTypes.DestroySessionResponse destroySessionResponse = profilerClient.destroySession(req);
        Assert.assertNotNull(destroySessionResponse);
    }

    /**
     * functional testing
     *
     * @tc.name: destroySessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_destroySessionTest_0004
     * @tc.desc: destroy Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void destroySessionTest04() {
        ProfilerClient profilerClient = new ProfilerClient(ip, port, channel);
        ProfilerServiceTypes.DestroySessionRequest req = ProfilerServiceHelper.destroySessionRequest(-1, 1);
        ProfilerServiceTypes.DestroySessionResponse destroySessionResponse = profilerClient.destroySession(req);
        Assert.assertNotNull(destroySessionResponse);
    }

    /**
     * functional testing
     *
     * @tc.name: destroySessionTest01
     * @tc.number: OHOS_JAVA_grpc_ProfilerClient_destroySessionTest_0005
     * @tc.desc: destroy Session Test
     * @tc.type: functional testing
     * @tc.require: SR-005
     */
    @Test
    public void destroySessionTest05() {
        ProfilerClient profilerClient = new ProfilerClient(null, -1, channel);
        ProfilerServiceTypes.DestroySessionRequest req =
            ProfilerServiceHelper.destroySessionRequest(CommonUtil.getRequestId(), -1);
        ProfilerServiceTypes.DestroySessionResponse destroySessionResponse = profilerClient.destroySession(req);
        Assert.assertNotNull(destroySessionResponse);
    }
}
