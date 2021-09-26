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
import io.grpc.StatusRuntimeException;
import ohos.devtools.datasources.transport.grpc.service.BytracePluginConfigOuterClass;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.HiperfCallPluginConfigOuterClass;
import ohos.devtools.datasources.transport.grpc.service.MemoryPluginConfig;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.transport.grpc.service.TracePluginConfigOuterClass;
import ohos.devtools.datasources.utils.common.GrpcException;
import ohos.devtools.datasources.utils.common.util.BeanUtil;
import ohos.devtools.datasources.utils.common.util.CommonUtil;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.datasources.utils.process.service.ProcessManager;
import ohos.devtools.datasources.utils.session.entity.SessionInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.views.common.LayoutConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ohos.devtools.datasources.transport.grpc.HiProfilerClient.getSTDSha256;
import static ohos.devtools.datasources.transport.grpc.HiProfilerClient.getSha256;
import static ohos.devtools.datasources.utils.device.entity.DeviceType.LEAN_HOS_DEVICE;
import static ohos.devtools.views.common.Constant.IS_SUPPORT_NEW_HDC;

/**
 * get trace data
 */
public class SystemTraceHelper {
    private static final Logger LOGGER = LogManager.getLogger(ProcessManager.class);
    private static final String SYSTEM_PROBES_KIND_IN_MEMORY = "inMemoryValue";
    private static final String SYSTEM_PROBES_KIND_MAX_DURATION = "maxDuration";
    private static final String SYSTEM_PROBES_KIND_FTRACE_EVENT = "ftrace_events";
    private static final String SYSTEM_PROBES_KIND_ATRACE_APPS = "atrace_apps";
    private static final int SECOND_TO_MS = 1000;
    private static final int MEMORY_MB_TO_KB = 1024;

    /**
     * 单例进程对象
     */
    private static SystemTraceHelper singleton;

    /**
     * 获取实例
     *
     * @return TraceManager
     */
    public static SystemTraceHelper getSingleton() {
        if (singleton == null) {
            synchronized (ProcessManager.class) {
                if (singleton == null) {
                    singleton = new SystemTraceHelper();
                }
            }
        }
        return singleton;
    }

    /**
     * createSession startSession
     *
     * @param deviceIPPortInfoParam deviceIPPortInfoParam
     * @param sessionConfigParam sessionConfigParam
     * @param configByteParam configByteParam
     * @param pluginNameParam pluginNameParam
     * @return String
     */
    public String createAndStartSession(DeviceIPPortInfo deviceIPPortInfoParam,
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfigParam, byte[] configByteParam, String pluginNameParam) {
        String sha256;
        if (IS_SUPPORT_NEW_HDC && deviceIPPortInfoParam.getDeviceType() == LEAN_HOS_DEVICE) {
            sha256 = getSTDSha256(pluginNameParam);
        } else {
            sha256 = getSha256(pluginNameParam);
        }
        CommonTypes.ProfilerPluginConfig plugConfig =
            CommonTypes.ProfilerPluginConfig.newBuilder().setPluginSha256(sha256)
                .setName(pluginNameParam)
                .setConfigData(ByteString.copyFrom(configByteParam))
                .build();
        ProfilerServiceTypes.CreateSessionRequest request =
            ProfilerServiceTypes.CreateSessionRequest.newBuilder()
                .setRequestId(CommonUtil.getRequestId())
                .setSessionConfig(sessionConfigParam)
                .addPluginConfigs(plugConfig)
                .build();
        HiProfilerClient hiprofiler = HiProfilerClient.getInstance();
        ProfilerClient client =
            hiprofiler.getProfilerClient(deviceIPPortInfoParam.getIp(), deviceIPPortInfoParam.getForwardPort());
        ProfilerServiceTypes.CreateSessionResponse createSessionResponse = client.createSession(request);
        ProfilerServiceTypes.StartSessionRequest requestStartSession = ProfilerServiceHelper
            .startSessionRequest(CommonUtil.getRequestId(), createSessionResponse.getSessionId(), new ArrayList<>());
        // 调用哪些进程（采集数据）
        client.startSession(requestStartSession);
        return String.valueOf(createSessionResponse.getSessionId());
    }

    /**
     * stop Session and destroy Session Request
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param sessionIdParam sessionIdParam
     */
    public void stopAndDestroySession(DeviceIPPortInfo deviceIPPortInfo, String sessionIdParam) {
        // 停止session
        int sessionId = Integer.valueOf(sessionIdParam);
        HiProfilerClient hiprofiler = HiProfilerClient.getInstance();
        ProfilerClient client =
            hiprofiler.getProfilerClient(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
        ProfilerServiceTypes.StopSessionRequest stopSession =
            ProfilerServiceHelper.stopSessionRequest(CommonUtil.getRequestId(), sessionId);
        HiProfilerClient.getInstance()
            .requestStopSession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId, true);
        // 销毁session
        ProfilerServiceTypes.DestroySessionRequest req =
            ProfilerServiceHelper.destroySessionRequest(CommonUtil.getRequestId(), sessionId);
        try {
            client.destroySession(req);
        } catch (StatusRuntimeException exception) {
            LOGGER.info("destroy session Exception: {}", exception.getMessage());
        }
    }

    /**
     * stop Session and destroy Session Request
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param sessionIdParam sessionIdParam
     */
    public void stopSession(DeviceIPPortInfo deviceIPPortInfo, String sessionIdParam) {
        // 停止session
        int sessionId = Integer.valueOf(sessionIdParam);
        HiProfilerClient hiprofiler = HiProfilerClient.getInstance();
        ProfilerClient client =
            hiprofiler.getProfilerClient(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
        ProfilerServiceTypes.StopSessionRequest stopSession =
            ProfilerServiceHelper.stopSessionRequest(CommonUtil.getRequestId(), sessionId);
        HiProfilerClient.getInstance()
            .requestStopSession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId, true);
    }

    /**
     * destroySessionRequest
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param sessionIdParam sessionIdParam
     */
    public void cancelActionDestroySession(DeviceIPPortInfo deviceIPPortInfo, String sessionIdParam) {
        int sessionId = Integer.valueOf(sessionIdParam);
        HiProfilerClient hiprofiler = HiProfilerClient.getInstance();
        ProfilerClient client =
            hiprofiler.getProfilerClient(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
        // 销毁session
        ProfilerServiceTypes.DestroySessionRequest req =
            ProfilerServiceHelper.destroySessionRequest(CommonUtil.getRequestId(), sessionId);
        try {
            client.destroySession(req);
        } catch (StatusRuntimeException exception) {
            LOGGER.info("destroy session Exception: {}", exception.getMessage());
        }
    }

    /**
     * request start session
     *
     * @param deviceIPPortInfo device IP Port Info
     * @param userCheckBoxForPerfettoStr userCheckBoxForPerfettoStr
     * @param maxDurationParam maxDurationParam
     * @param inMemoryValue inMemoryValue
     * @param isRoot isRoot
     * @return String
     * @throws GrpcException GrpcException
     */
    public String createSessionByTraceRequest(DeviceIPPortInfo deviceIPPortInfo, String userCheckBoxForPerfettoStr,
        int maxDurationParam, int inMemoryValue, boolean isRoot) throws GrpcException {
        BytracePluginConfigOuterClass.BytracePluginConfig.Builder build =
            BytracePluginConfigOuterClass.BytracePluginConfig.newBuilder();
        build.setBuffeSize(inMemoryValue * MEMORY_MB_TO_KB);
        build.setClock("boot");
        if (userCheckBoxForPerfettoStr != null && userCheckBoxForPerfettoStr.length() > 0) {
            Arrays.stream(userCheckBoxForPerfettoStr.split(";")).filter(param -> param.trim().length() > 0)
                .forEach(param -> build.addCategories(param));
        } else {
            // catch All
            build.addCategories("");
        }
        build.setIsRoot(isRoot);
        build.setTime(maxDurationParam);
        build.setOutfileName("/data/local/tmp/hiprofiler_data.bytrace");
        BytracePluginConfigOuterClass.BytracePluginConfig config = build.build();
        byte[] configByte = BeanUtil.serializeByCodedOutPutStream(config);
        ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig bf =
            ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.newBuilder().setPages(LayoutConstants.NUMBER_THREAD)
                .setPolicy(ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE).build();
        int keepAliveTime = maxDurationParam + 1;
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfig =
            ProfilerServiceTypes.ProfilerSessionConfig.newBuilder()
                .setSessionMode(ProfilerServiceTypes.ProfilerSessionConfig.Mode.ONLINE).addBuffers(bf)
                .setKeepAliveTime(keepAliveTime).build();
        // 获取插件名称
        ProfilerServiceTypes.GetCapabilitiesResponse response =
            HiProfilerClient.getInstance().getCapabilities(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
        List<ProfilerServiceTypes.ProfilerPluginCapability> capabilitiesList = response.getCapabilitiesList();
        ProfilerServiceTypes.ProfilerPluginCapability profilerPluginCapability =
            capabilitiesList.stream().filter(item -> item.getName().contains("libbytrace")).findFirst().get();
        String pluginName = profilerPluginCapability.getName();
        return this.createAndStartSession(deviceIPPortInfo, sessionConfig, configByte, pluginName);
    }

    /**
     * request start session
     *
     * @param deviceIPPortInfo device IP Port Info
     * @param fileSuffixTimestampParam file Suffix Timestamp Param
     * @param processNameParam processName Param
     * @return String
     * @throws GrpcException GrpcException
     */
    public String createSessionByTraceRequestNoParam(DeviceIPPortInfo deviceIPPortInfo, String fileSuffixTimestampParam,
        String processNameParam) throws GrpcException {
        BytracePluginConfigOuterClass.BytracePluginConfig.Builder build =
            BytracePluginConfigOuterClass.BytracePluginConfig.newBuilder();
        String fileStorePath = "/data/local/tmp/hiprofiler_data";
        fileStorePath = fileStorePath.concat(fileSuffixTimestampParam).concat(".bytrace");
        if (IS_SUPPORT_NEW_HDC && deviceIPPortInfo.getDeviceType() == DeviceType.LEAN_HOS_DEVICE) {
            build.addCategories("sched");
            build.addCategories("freq");
            build.addCategories("idle");
            build.addCategories("workq");
            build.setIsRoot(false);
        } else if (deviceIPPortInfo.getDeviceType() == DeviceType.LEAN_HOS_DEVICE) {
            build.addCategories("sched");
            build.addCategories("freq");
            build.addCategories("idle");
            build.addCategories("workq");
            build.setIsRoot(true);
        } else {
            build.setIsRoot(false);
            build.addCategories("gfx");
        }
        build.setTime(0);
        build.setOutfileName(fileStorePath);
        BytracePluginConfigOuterClass.BytracePluginConfig config = build.build();
        byte[] configByte = BeanUtil.serializeByCodedOutPutStream(config);
        ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig bf =
            ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.newBuilder().setPages(LayoutConstants.NUMBER_THREAD)
                .setPolicy(ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE).build();
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfig =
            ProfilerServiceTypes.ProfilerSessionConfig.newBuilder()
                .setSessionMode(ProfilerServiceTypes.ProfilerSessionConfig.Mode.ONLINE).addBuffers(bf)
                .build();
        // 获取插件名称
        ProfilerServiceTypes.GetCapabilitiesResponse response =
            HiProfilerClient.getInstance().getCapabilities(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
        List<ProfilerServiceTypes.ProfilerPluginCapability> capabilitiesList = response.getCapabilitiesList();
        ProfilerServiceTypes.ProfilerPluginCapability profilerPluginCapability =
            capabilitiesList.stream().filter(item -> item.getName().contains("libbytrace")).findFirst().get();
        String pluginName = profilerPluginCapability.getName();
        return this.createAndStartSession(deviceIPPortInfo, sessionConfig, configByte, pluginName);
    }

    /**
     * Request start session
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param eventsList eventsList
     * @param atraceEventsList atraceEventsList
     * @param maxDuration maxDuration
     * @param inMemoryValue inMemoryValue
     * @return String
     * @throws GrpcException GrpcException
     */
    public String createSessionHtraceRequest(DeviceIPPortInfo deviceIPPortInfo, ArrayList<ArrayList<String>> eventsList,
        ArrayList<ArrayList<String>> atraceEventsList, int maxDuration, int inMemoryValue) throws GrpcException {
        TracePluginConfigOuterClass.TracePluginConfig.Builder build =
            TracePluginConfigOuterClass.TracePluginConfig.newBuilder();
        if (eventsList != null && !eventsList.isEmpty()) {
            eventsList.forEach(events -> events.forEach(event -> {
                build.addFtraceEvents(event);
            }));
        }
        if (atraceEventsList != null && !atraceEventsList.isEmpty()) {
            atraceEventsList.forEach(events -> events.forEach(event -> {
                build.addBytraceCategories(event);
            }));
        }
        build.setClock("boot");
        build.setParseKsyms(true);
        build.setBufferSizeKb(inMemoryValue * MEMORY_MB_TO_KB);
        build.setFlushIntervalMs(1000);
        build.setFlushThresholdKb(4096);
        TracePluginConfigOuterClass.TracePluginConfig config = build.build();
        byte[] configByte = BeanUtil.serializeByCodedOutPutStream(config);
        ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig bf =
            ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.newBuilder().setPages(LayoutConstants.NUMBER_THREAD)
                .setPolicy(ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE).build();
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfig =
            ProfilerServiceTypes.ProfilerSessionConfig.newBuilder().setSampleDuration(maxDuration * SECOND_TO_MS)
                .setSessionMode(ProfilerServiceTypes.ProfilerSessionConfig.Mode.OFFLINE).addBuffers(bf)
                .setResultFile("/data/local/tmp/hiprofiler_data.htrace").build();
        MemoryPluginConfig.MemoryConfig.Builder memoryBuilder = MemoryPluginConfig.MemoryConfig.newBuilder();
        memoryBuilder.setReportProcessTree(true);
        memoryBuilder.setReportProcessMemInfo(true);
        MemoryPluginConfig.MemoryConfig memoryConfig = memoryBuilder.build();
        byte[] memoryConfigByte = BeanUtil.serializeByCodedOutPutStream(memoryConfig);
        return this.createAndStartSession(deviceIPPortInfo, sessionConfig, configByte, memoryConfigByte);
    }

    /**
     * createAndStartSession
     *
     * @param deviceIPPortInfoParam deviceIPPortInfoParam
     * @param sessionConfigParam sessionConfigParam
     * @param configByteParam configByteParam
     * @param memoryConfigByteParam memoryConfigByteParam
     * @return String
     */
    public String createAndStartSession(DeviceIPPortInfo deviceIPPortInfoParam,
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfigParam, byte[] configByteParam,
        byte[] memoryConfigByteParam) {
        String pluginName = getPluginName(deviceIPPortInfoParam, "libftrace");
        String sha256;
        if (IS_SUPPORT_NEW_HDC && deviceIPPortInfoParam.getDeviceType() == LEAN_HOS_DEVICE) {
            sha256 = getSTDSha256(pluginName);
        } else {
            sha256 = getSha256(pluginName);
        }
        CommonTypes.ProfilerPluginConfig plugConfig =
            CommonTypes.ProfilerPluginConfig.newBuilder().setPluginSha256(sha256).setName(pluginName)
                .setConfigData(ByteString.copyFrom(configByteParam)).build();
        String pluginMemoryName = getPluginName(deviceIPPortInfoParam, "libmem");
        String memSha256;
        if (IS_SUPPORT_NEW_HDC && deviceIPPortInfoParam.getDeviceType() == LEAN_HOS_DEVICE) {
            memSha256 = getSTDSha256(pluginMemoryName);
        } else {
            memSha256 = getSha256(pluginMemoryName);
        }
        CommonTypes.ProfilerPluginConfig memoryPlugConfig =
            CommonTypes.ProfilerPluginConfig.newBuilder().setPluginSha256(memSha256).setName(pluginMemoryName)
                .setSampleInterval(5000).setConfigData(ByteString.copyFrom(memoryConfigByteParam)).build();
        ProfilerServiceTypes.CreateSessionRequest request =
            ProfilerServiceTypes.CreateSessionRequest.newBuilder().setRequestId(1).setSessionConfig(sessionConfigParam)
                .addPluginConfigs(plugConfig).addPluginConfigs(memoryPlugConfig).build();
        HiProfilerClient hiprofiler = HiProfilerClient.getInstance();
        ProfilerClient client =
            hiprofiler.getProfilerClient(deviceIPPortInfoParam.getIp(), deviceIPPortInfoParam.getForwardPort());
        ProfilerServiceTypes.CreateSessionResponse createSessionResponse = client.createSession(request);
        ProfilerServiceTypes.StartSessionRequest requestStartSession = ProfilerServiceHelper
            .startSessionRequest(CommonUtil.getRequestId(), createSessionResponse.getSessionId(), new ArrayList<>());
        // 调用哪些进程（采集数据）
        client.startSession(requestStartSession);
        return String.valueOf(createSessionResponse.getSessionId());
    }

    /**
     * getPluginName
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param PluginName PluginName
     * @return String
     */
    public String getPluginName(DeviceIPPortInfo deviceIPPortInfo, String PluginName) {
        ProfilerServiceTypes.GetCapabilitiesResponse response =
            HiProfilerClient.getInstance().getCapabilities(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
        List<ProfilerServiceTypes.ProfilerPluginCapability> capabilitiesList = response.getCapabilitiesList();
        if (capabilitiesList.isEmpty()) {
            return "";
        }
        ProfilerServiceTypes.ProfilerPluginCapability profilerPluginCapability =
            capabilitiesList.stream().filter(item -> item.getName().contains(PluginName)).findFirst().get();
        return profilerPluginCapability.getName();
    }
}
