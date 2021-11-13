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
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import ohos.devtools.datasources.transport.grpc.service.IProfilerServiceGrpc;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.common.LayoutConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * 提供设备侧grpc接口封装
 */
public class ProfilerClient {
    private static final Logger LOGGER = LogManager.getLogger(ProfilerClient.class);

    private ManagedChannel channel;

    private String host;

    private int port;

    private IProfilerServiceGrpc.IProfilerServiceBlockingStub profilerBlockInClient;

    /**
     * ProfilerClient
     *
     * @param host localhost
     * @param port port number
     */
    public ProfilerClient(String host, int port) {
        this(host, port, null);
    }

    /**
     * ProfilerClient
     *
     * @param host localhost
     * @param port port number
     * @param channel channel
     */
    public ProfilerClient(String host, int port, ManagedChannel channel) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("ProfilerClient");
        }
        this.host = host;
        this.port = port;
        if (channel == null || channel.isShutdown() || channel.isTerminated()) {
            this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        } else {
            this.channel = channel;
        }
        profilerBlockInClient = IProfilerServiceGrpc.newBlockingStub(this.channel);
    }

    /**
     * get profiler client
     *
     * @return IProfilerServiceGrpc.IProfilerServiceBlockingStub
     */
    public IProfilerServiceGrpc.IProfilerServiceBlockingStub getProfilerClient() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getProfilerClient");
        }
        return profilerBlockInClient;
    }

    /**
     * 获取支持的插件列表
     *
     * @param getCapabilitiesRequest getCapabilitiesRequest
     * @return ProfilerServiceTypes.GetCapabilitiesResponse
     * @throws StatusRuntimeException StatusRuntimeException
     */
    public ProfilerServiceTypes.GetCapabilitiesResponse getCapabilities(
        ProfilerServiceTypes.GetCapabilitiesRequest getCapabilitiesRequest) throws StatusRuntimeException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getProfilerClient");
        }
        ProfilerServiceTypes.GetCapabilitiesResponse res =
            profilerBlockInClient.withDeadlineAfter(LayoutConstants.FIVE, TimeUnit.SECONDS)
                .getCapabilities(getCapabilitiesRequest);
        return res;
    }

    /**
     * createSession
     *
     * @param createSessionRequest createSessionRequest
     * @return ProfilerServiceTypes.CreateSessionResponse
     * @throws StatusRuntimeException GrpcException
     */
    public ProfilerServiceTypes.CreateSessionResponse createSession(
        ProfilerServiceTypes.CreateSessionRequest createSessionRequest) throws StatusRuntimeException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createSession");
        }
        ProfilerServiceTypes.CreateSessionResponse response =
            profilerBlockInClient.withDeadlineAfter(LayoutConstants.FIVE, TimeUnit.SECONDS)
                .createSession(createSessionRequest);
        return response;
    }

    /**
     * startSession
     *
     * @param startSessionRequest startSessionRequest
     * @return ProfilerServiceTypes.StartSessionResponse
     * @throws StatusRuntimeException StatusRuntimeException
     */
    public ProfilerServiceTypes.StartSessionResponse startSession(
        ProfilerServiceTypes.StartSessionRequest startSessionRequest) throws StatusRuntimeException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("startSession");
        }
        ProfilerServiceTypes.StartSessionResponse response =
            profilerBlockInClient.withDeadlineAfter(LayoutConstants.THREE, TimeUnit.SECONDS)
                .startSession(startSessionRequest);
        return response;
    }

    /**
     * 抓取数据
     *
     * @param fetchDataRequest fetchDataRequest
     * @return Iterator<ProfilerServiceTypes.FetchDataResponse>
     * @throws StatusRuntimeException StatusRuntimeException
     */
    public Iterator<ProfilerServiceTypes.FetchDataResponse> fetchData(
        ProfilerServiceTypes.FetchDataRequest fetchDataRequest) throws StatusRuntimeException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("fetchData");
        }
        Iterator<ProfilerServiceTypes.FetchDataResponse> response =
            profilerBlockInClient.withMaxInboundMessageSize(Integer.MAX_VALUE)
                .withMaxOutboundMessageSize(Integer.MAX_VALUE).fetchData(fetchDataRequest);
        return response;
    }

    /**
     * stop Session
     *
     * @param stopSessionRequest stopSessionRequest
     * @return ProfilerServiceTypes.StopSessionResponse
     * @throws StatusRuntimeException StatusRuntimeException
     */
    public ProfilerServiceTypes.StopSessionResponse stopSession(
        ProfilerServiceTypes.StopSessionRequest stopSessionRequest) throws StatusRuntimeException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("stopSession");
        }
        ProfilerServiceTypes.StopSessionResponse response =
            profilerBlockInClient.withDeadlineAfter(3, TimeUnit.SECONDS).stopSession(stopSessionRequest);
        return response;
    }

    /**
     * destroy Session
     *
     * @param destroyRequest destroyRequest
     * @return ProfilerServiceTypes.DestroySessionResponse
     * @throws StatusRuntimeException StatusRuntimeException
     */
    public ProfilerServiceTypes.DestroySessionResponse destroySession(
        ProfilerServiceTypes.DestroySessionRequest destroyRequest) throws StatusRuntimeException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("destroySession");
        }
        ProfilerServiceTypes.DestroySessionResponse res =
            profilerBlockInClient.withDeadlineAfter(1, TimeUnit.SECONDS).destroySession(destroyRequest);
        return res;
    }

    /**
     * keepSession
     *
     * @param keepSessionRequest keepSessionRequest
     * @return ProfilerServiceTypes.KeepSessionResponse
     * @throws StatusRuntimeException
     */
    public ProfilerServiceTypes.KeepSessionResponse keepSession(
        ProfilerServiceTypes.KeepSessionRequest keepSessionRequest) throws StatusRuntimeException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("keepSession");
        }
        ProfilerServiceTypes.KeepSessionResponse res =
            profilerBlockInClient.keepSession(keepSessionRequest);
        return res;
    }

    /**
     * Closing method
     */
    public void shutdown() {
        try {
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("shutdown");
            }
            channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(exception.getMessage());
            }
        }
    }

    /**
     * get ManagedChannel
     *
     * @return ManagedChannel
     */
    public ManagedChannel getChannel() {
        return channel;
    }
}
