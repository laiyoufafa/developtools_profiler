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
 * Provide device-side grpc interface encapsulation
 *
 * @since 2021/5/19 16:39
 */
public class ProfilerClient {
    private static final Logger LOGGER = LogManager.getLogger(ProfilerClient.class);

    /**
     * IProfilerServiceStub
     */
    private IProfilerServiceGrpc.IProfilerServiceStub profilerServiceStub;

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
        profilerServiceStub = IProfilerServiceGrpc.newStub(this.channel);
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
        return profilerBlockInClient.withDeadlineAfter(LayoutConstants.FIVE, TimeUnit.SECONDS)
            .getCapabilities(getCapabilitiesRequest);
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
        return profilerBlockInClient.withDeadlineAfter(LayoutConstants.FIVE, TimeUnit.SECONDS)
            .createSession(createSessionRequest);
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
        return profilerBlockInClient.withDeadlineAfter(LayoutConstants.THREE, TimeUnit.SECONDS)
            .startSession(startSessionRequest);
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
        return profilerBlockInClient.withMaxInboundMessageSize(Integer.MAX_VALUE)
            .withMaxOutboundMessageSize(Integer.MAX_VALUE).fetchData(fetchDataRequest);
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
        return profilerBlockInClient.withDeadlineAfter(3, TimeUnit.SECONDS).stopSession(stopSessionRequest);
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
        return profilerBlockInClient.withDeadlineAfter(1, TimeUnit.SECONDS).destroySession(destroyRequest);
    }

    /**
     * keepSession
     *
     * @param keepSessionRequest keepSessionRequest
     * @return ProfilerServiceTypes.KeepSessionResponse
     * @throws StatusRuntimeException StatusRuntimeException
     */
    public ProfilerServiceTypes.KeepSessionResponse keepSession(
        ProfilerServiceTypes.KeepSessionRequest keepSessionRequest) throws StatusRuntimeException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("keepSession");
        }
        return profilerBlockInClient.withDeadlineAfter(3, TimeUnit.SECONDS).keepSession(keepSessionRequest);
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

    /**
     * getProfilerServiceStub
     *
     * @return IProfilerServiceGrpc.IProfilerServiceStub
     */
    public IProfilerServiceGrpc.IProfilerServiceStub getProfilerServiceStub() {
        return profilerServiceStub;
    }
}
