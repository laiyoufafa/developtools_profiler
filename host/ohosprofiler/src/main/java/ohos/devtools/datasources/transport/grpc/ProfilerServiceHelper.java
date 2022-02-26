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
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Create the request object of the profiler Client
 *
 * @since 2021/5/19 16:39
 */
public final class ProfilerServiceHelper {
    private static final Logger LOGGER = LogManager.getLogger(ProfilerServiceHelper.class);

    private ProfilerServiceHelper() {
    }

    /**
     * Create a Create Session Request object for grpc request.
     *
     * @param requestId requestId
     * @param sessionConfig Session config
     * @param pluginConfigs Plugin configs
     * @return ProfilerServiceTypes.CreateSessionRequest
     */
    public static ProfilerServiceTypes.CreateSessionRequest createSessionRequest(int requestId,
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfig,
        List<CommonTypes.ProfilerPluginConfig> pluginConfigs) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createSessionRequest");
        }
        ProfilerServiceTypes.CreateSessionRequest.Builder createBuilder =
            ProfilerServiceTypes.CreateSessionRequest.newBuilder();
        createBuilder.setRequestId(requestId);
        if (sessionConfig != null) {
            createBuilder.setSessionConfig(sessionConfig);
        }
        if (pluginConfigs != null) {
            for (CommonTypes.ProfilerPluginConfig profilerPluginConfig : pluginConfigs) {
                createBuilder.addPluginConfigs(profilerPluginConfig);
            }
        }
        return createBuilder.build();
    }

    /**
     * Create a start Session Request object for grpc request.
     *
     * @param requestId requestId
     * @param sessionId sessionId
     * @param pluginConfigs Plugin configs
     * @return ProfilerServiceTypes.StartSessionRequest
     */
    public static ProfilerServiceTypes.StartSessionRequest startSessionRequest(int requestId, int sessionId,
        List<CommonTypes.ProfilerPluginConfig> pluginConfigs) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("startSessionRequest");
        }
        ProfilerServiceTypes.StartSessionRequest.Builder startBuilder =
            ProfilerServiceTypes.StartSessionRequest.newBuilder();
        startBuilder.setRequestId(requestId);
        startBuilder.setSessionId(sessionId);
        if (pluginConfigs != null) {
            pluginConfigs.forEach(profilerPluginConfig -> {
                startBuilder.addUpdateConfigs(profilerPluginConfig);
            });
        }
        return startBuilder.build();
    }

    /**
     * Create a fetch Data Request object for grpc request
     *
     * @param requestId requestId
     * @param sessionId sessionId
     * @param addtionData addtionData not used temporarily, you can pass null
     * @return ProfilerServiceTypes.FetchDataRequest
     */
    public static ProfilerServiceTypes.FetchDataRequest fetchDataRequest(int requestId, int sessionId,
        ByteString addtionData) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("fetchDataRequest");
        }
        ProfilerServiceTypes.FetchDataRequest.Builder builder = ProfilerServiceTypes.FetchDataRequest.newBuilder();
        builder.setRequestId(requestId);
        builder.setSessionId(sessionId);
        if (addtionData != null) {
            builder.setAddtionData(addtionData);
        }
        return builder.build();
    }

    /**
     * Create a stop Session Request object for grpc request.
     *
     * @param requestId requestId
     * @param sessionId sessionId
     * @return ProfilerServiceTypes.StopSessionRequest
     */
    public static ProfilerServiceTypes.StopSessionRequest stopSessionRequest(int requestId, int sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("stopSessionRequest");
        }
        ProfilerServiceTypes.StopSessionRequest.Builder builder = ProfilerServiceTypes.StopSessionRequest.newBuilder();
        builder.setRequestId(requestId);
        builder.setSessionId(sessionId);
        return builder.build();
    }

    /**
     * Create a destroy Session Request object for grpc request
     *
     * @param requestId requestId
     * @param sessionId sessionId
     * @return ProfilerServiceTypes.DestroySessionRequest
     */
    public static ProfilerServiceTypes.DestroySessionRequest destroySessionRequest(int requestId, int sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("destroySessionRequest");
        }
        ProfilerServiceTypes.DestroySessionRequest.Builder builder =
            ProfilerServiceTypes.DestroySessionRequest.newBuilder();
        builder.setRequestId(requestId);
        builder.setSessionId(sessionId);
        return builder.build();
    }

    /**
     * Construct Session Config object
     *
     * @param online online Whether it is online mode, true online false offline
     * @param resultFile resultFile
     * @param pages pages
     * @param value value
     * @param keepTime keepTime
     * @return ProfilerServiceTypes.ProfilerSessionConfig
     */
    public static ProfilerServiceTypes.ProfilerSessionConfig profilerSessionConfig(boolean online, String resultFile,
        int pages, ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy value, int keepTime) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("profilerSessionConfig");
        }
        ProfilerServiceTypes.ProfilerSessionConfig.Builder builder =
            ProfilerServiceTypes.ProfilerSessionConfig.newBuilder();
        if (online) {
            builder.setSessionMode(ProfilerServiceTypes.ProfilerSessionConfig.Mode.ONLINE);
        } else {
            builder.setSessionMode(ProfilerServiceTypes.ProfilerSessionConfig.Mode.OFFLINE);
            builder.setResultFile(resultFile);
        }
        builder.setKeepAliveTime(keepTime);
        ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Builder build =
            ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.newBuilder();
        if (value != null && value.getNumber() > 0) {
            build.setPolicy(value);
        }
        if (pages > 0) {
            build.setPages(pages);
        }
        return builder.addBuffers(build.build()).build();
    }

    /**
     * Construct profiler Plugin Config object
     *
     * @param name name
     * @param pluginSha256 pluginSha256
     * @param sampleInterval sampleInterval
     * @param confData confData Objects serialized by each plug-in
     * @return CommonTypes.ProfilerPluginConfig
     */
    public static CommonTypes.ProfilerPluginConfig profilerPluginConfig(String name, String pluginSha256,
        int sampleInterval, ByteString confData) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("profilerPluginConfig");
        }
        CommonTypes.ProfilerPluginConfig.Builder builder = CommonTypes.ProfilerPluginConfig.newBuilder();
        if (StringUtils.isNotBlank(name)) {
            builder.setName(name);
        }
        if (StringUtils.isNotBlank(pluginSha256)) {
            builder.setPluginSha256(pluginSha256);
        }
        if (sampleInterval > 0) {
            builder.setSampleInterval(sampleInterval);
        }
        if (confData != null) {
            builder.setConfigData(confData);
        }
        return builder.build();
    }
}
