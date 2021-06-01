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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.grpc.StatusRuntimeException;

import com.google.protobuf.ByteString;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ohos.devtools.datasources.transport.grpc.HiProfilerClient;
import ohos.devtools.datasources.transport.grpc.ProfilerClient;
import ohos.devtools.datasources.transport.grpc.ProfilerServiceHelper;
import ohos.devtools.datasources.transport.grpc.service.BytracePluginConfigOuterClass;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.transport.grpc.service.PtracePluginConfigOuterClass;
import ohos.devtools.datasources.transport.grpc.service.TracePluginConfigOuterClass;
import ohos.devtools.datasources.utils.common.GrpcException;
import ohos.devtools.datasources.utils.common.util.BeanUtil;
import ohos.devtools.datasources.utils.common.util.CommonUtil;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.process.service.ProcessManager;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.SystemTunningProbesCheckbox;

/**
 * get trace data
 *
 * @version 1.0
 * @date 2021/04/12 17:07
 **/
public class TraceManager {
    private static final Logger LOGGER = LogManager.getLogger(ProcessManager.class);

    /**
     * 单例进程对象
     */
    private static TraceManager singleton;

    /**
     * 获取实例
     *
     * @return TraceManager
     */
    public static TraceManager getSingleton() {
        if (singleton == null) {
            synchronized (ProcessManager.class) {
                if (singleton == null) {
                    singleton = new TraceManager();
                }
            }
        }
        return singleton;
    }

    /**
     * 请求通过ptrace获取数据
     *
     * @param deviceIPPortInfo              deviceIPPortInfo
     * @param getUserCheckBoxForPerfettoStr getUserCheckBoxForPerfettoStr
     * @param isReturn                      isReturn
     * @param maxDurationParam              maxDurationParam
     * @return String
     * @throws GrpcException GrpcException
     */
    public String createSessionRequestPerfetto(DeviceIPPortInfo deviceIPPortInfo, String getUserCheckBoxForPerfettoStr,
        int maxDurationParam, Boolean isReturn) throws GrpcException {
        HiProfilerClient hiprofiler = HiProfilerClient.getInstance();
        ProfilerClient client =
            hiprofiler.getProfilerClient(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
        // 获取插件名称
        ProfilerServiceTypes.GetCapabilitiesResponse response =
            HiProfilerClient.getInstance().getCapabilities(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
        List<ProfilerServiceTypes.ProfilerPluginCapability> capabilitiesList = response.getCapabilitiesList();
        ProfilerServiceTypes.ProfilerPluginCapability profilerPluginCapability =
            capabilitiesList.stream().filter(item -> item.getName().contains("libptrace")).findFirst().get();
        String pluginName = profilerPluginCapability.getName();
        PtracePluginConfigOuterClass.PtracePluginConfig.Builder build =
            PtracePluginConfigOuterClass.PtracePluginConfig.newBuilder();
        build.setConfigText(getUserCheckBoxForPerfettoStr);
        build.setResultPath("/data/local/tmp/hiprofiler_data.ptrace");
        PtracePluginConfigOuterClass.PtracePluginConfig config = build.build();
        ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig bf =
            ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.newBuilder().setPages(LayoutConstants.NUMBER_THREAD)
                .setPolicy(ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE).build();
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfig =
            ProfilerServiceTypes.ProfilerSessionConfig.newBuilder()
                .setSessionMode(ProfilerServiceTypes.ProfilerSessionConfig.Mode.OFFLINE).addBuffers(bf)
                .setResultFile("/data/local/tmp/hiprofiler_data.ptrace").build();
        CommonTypes.ProfilerPluginConfig plugConfig =
            CommonTypes.ProfilerPluginConfig.newBuilder().setName(pluginName).setPluginSha256("111")
                .setSampleInterval(1).setConfigData(config.toByteString()).build();
        ProfilerServiceTypes.CreateSessionRequest request =
            ProfilerServiceTypes.CreateSessionRequest.newBuilder().setRequestId(1).setSessionConfig(sessionConfig)
                .addPluginConfigs(plugConfig).build();
        if (isReturn) {
            return getUserCheckBoxForPerfettoStr;
        } else {
            // 通知端侧要用那些插件（分配资源）
            ProfilerServiceTypes.CreateSessionResponse response1 = client.createSession(request);
            ProfilerServiceTypes.StartSessionRequest requestStartSession = ProfilerServiceHelper
                .startSessionRequest(CommonUtil.getRequestId(), response1.getSessionId(), new ArrayList<>());
            // 调用哪些进程（采集数据）
            ProfilerServiceTypes.StartSessionResponse startSessionResponse = client.startSession(requestStartSession);
            return String.valueOf(response1.getSessionId());
        }
    }

    /**
     * stopSession、destroySessionRequest
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param sessionIdParam   sessionIdParam
     */
    public void stopAndDestroySession(DeviceIPPortInfo deviceIPPortInfo, String sessionIdParam) {
        // 停止和销毁session
        int sessionId = Integer.valueOf(sessionIdParam);
        HiProfilerClient hiprofiler = HiProfilerClient.getInstance();
        ProfilerClient client =
            hiprofiler.getProfilerClient(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
        ProfilerServiceTypes.StopSessionRequest stopSession =
            ProfilerServiceHelper.stopSessionRequest(CommonUtil.getRequestId(), sessionId);
        HiProfilerClient.getInstance()
            .requestStopSession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId, true);
        ProfilerServiceTypes.DestroySessionRequest req =
            ProfilerServiceHelper.destroySessionRequest(CommonUtil.getRequestId(), sessionId);
        try {
            client.destroySession(req);
        } catch (StatusRuntimeException exception) {
            HiProfilerClient.getInstance()
                .requestDestroySession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getPort(), sessionId);
        }
        client.setUsed(false);
    }

    /**
     * request start session
     *
     * @param deviceIPPortInfo device IP Port Info
     * @param getUserCheckBoxForPerfettoStr getUserCheckBoxForPerfettoStr
     * @param maxDurationParam maxDurationParam
     * @param inMemoryValue inMemoryValue
     * @param isReturn isReturn
     * @return String
     * @throws GrpcException GrpcException
     */
    public String createSessionByTraceRequest(DeviceIPPortInfo deviceIPPortInfo, String getUserCheckBoxForPerfettoStr,
        int maxDurationParam, int inMemoryValue, Boolean isReturn) throws GrpcException {
        BytracePluginConfigOuterClass.BytracePluginConfig.Builder build =
            BytracePluginConfigOuterClass.BytracePluginConfig.newBuilder();
        build.setBuffeSize(inMemoryValue * SystemTunningProbesCheckbox.MEMORY_MB_TO_KB);
        build.setClock("boot");
        if (getUserCheckBoxForPerfettoStr != null && getUserCheckBoxForPerfettoStr.length() > 0) {
            Arrays.stream(getUserCheckBoxForPerfettoStr.split(";")).filter(param -> param.trim().length() > 0)
                .forEach(param -> build.addCategories(param));
        } else {
            // catch All
            build.addCategories("");
        }
        build.setTime(maxDurationParam);
        build.setOutfileName("/data/local/tmp/hiprofiler_data.bytrace");
        BytracePluginConfigOuterClass.BytracePluginConfig config = build.build();
        byte[] ccByte = BeanUtil.serializeByCodedOutPutStream(config);
        ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig bf =
            ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.newBuilder().setPages(LayoutConstants.NUMBER_THREAD)
                .setPolicy(ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE).build();
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfig =
            ProfilerServiceTypes.ProfilerSessionConfig.newBuilder()
                .setSessionMode(ProfilerServiceTypes.ProfilerSessionConfig.Mode.OFFLINE).addBuffers(bf)
                .setResultFile("/data/local/tmp/hiprofiler_data.bytrace").build();
        // 获取插件名称
        ProfilerServiceTypes.GetCapabilitiesResponse response =
            HiProfilerClient.getInstance().getCapabilities(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
        List<ProfilerServiceTypes.ProfilerPluginCapability> capabilitiesList = response.getCapabilitiesList();
        ProfilerServiceTypes.ProfilerPluginCapability profilerPluginCapability =
            capabilitiesList.stream().filter(item -> item.getName().contains("libbytrace")).findFirst().get();
        String pluginName = profilerPluginCapability.getName();
        CommonTypes.ProfilerPluginConfig plugConfig =
            CommonTypes.ProfilerPluginConfig.newBuilder().setName(pluginName).setPluginSha256("111")
                .setSampleInterval(1).setConfigData(ByteString.copyFrom(ccByte)).build();
        ProfilerServiceTypes.CreateSessionRequest request =
            ProfilerServiceTypes.CreateSessionRequest.newBuilder().setRequestId(1).setSessionConfig(sessionConfig)
                .addPluginConfigs(plugConfig).build();
        HiProfilerClient hiprofiler = HiProfilerClient.getInstance();
        ProfilerClient client =
            hiprofiler.getProfilerClient(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
        ProfilerServiceTypes.CreateSessionResponse createSessionResponse = client.createSession(request);
        ProfilerServiceTypes.StartSessionRequest requestStartSession = ProfilerServiceHelper
            .startSessionRequest(CommonUtil.getRequestId(), createSessionResponse.getSessionId(), new ArrayList<>());
        // 调用哪些进程（采集数据）
        client.startSession(requestStartSession);
        return String.valueOf(createSessionResponse.getSessionId());
    }

    /**
     * 请求启动session
     *
     * @param deviceIPPortInfo   deviceIPPortInfo
     * @param param              param
     * @param paramRecordSetting paramRecordSetting
     * @param isReturn           isReturn
     * @return String
     * @throws GrpcException GrpcException
     */
    public String createSessionRequest(DeviceIPPortInfo deviceIPPortInfo, HashMap<String, ArrayList<String>> param,
        HashMap<String, Integer> paramRecordSetting, Boolean isReturn) throws GrpcException {
        int sampleDuration = 0;
        int resultMaxSize = 0;
        HiProfilerClient hiprofiler = HiProfilerClient.getInstance();
        ProfilerClient client =
            hiprofiler.getProfilerClient(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
        // 获取插件名称
        ProfilerServiceTypes.GetCapabilitiesResponse response =
            HiProfilerClient.getInstance().getCapabilities(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
        List<ProfilerServiceTypes.ProfilerPluginCapability> capabilitiesList = response.getCapabilitiesList();
        if (capabilitiesList.size() == 0) {
            return "";
        }
        ProfilerServiceTypes.ProfilerPluginCapability profilerPluginCapability =
            capabilitiesList.stream().filter(item -> item.getName().contains("libtrace")).findFirst().get();
        String pluginName = null;
        TracePluginConfigOuterClass.TracePluginConfig.Builder build =
            TracePluginConfigOuterClass.TracePluginConfig.newBuilder();
        pluginName = profilerPluginCapability.getName();
        String configDataStr = "";
        if (param != null && !param.isEmpty()) {
            for (String key : param.keySet()) {
                configDataStr = getConfigDataStr(param, build, configDataStr, key);
            }
        }
        if (paramRecordSetting != null && paramRecordSetting.size() > 0) {
            for (String key : paramRecordSetting.keySet()) {
                switch (key) {
                    case SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_KIND_IN_MEMORY:
                        build
                            .setBufferSizeKb(paramRecordSetting.get(key) * SystemTunningProbesCheckbox.MEMORY_MB_TO_KB);
                        // 将MB 转换为KB
                        resultMaxSize = paramRecordSetting.get(key) * SystemTunningProbesCheckbox.MEMORY_MB_TO_KB;
                        break;
                    case SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_KIND_MAX_DURATION:
                        build.setTracePeriodMs(paramRecordSetting.get(key) * SystemTunningProbesCheckbox.SECOND_TO_MS);
                        // 将MB 转换为KB
                        sampleDuration = paramRecordSetting.get(key) * SystemTunningProbesCheckbox.SECOND_TO_MS;
                        break;
                    default:
                }
            }
        }
        build.setClock("local");
        TracePluginConfigOuterClass.TracePluginConfig config = build.build();
        byte[] ccByte = BeanUtil.serializeByCodedOutPutStream(config);
        ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig bf =
            ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.newBuilder().setPages(LayoutConstants.NUMBER_THREAD)
                .setPolicy(ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE).build();
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfig =
            ProfilerServiceTypes.ProfilerSessionConfig.newBuilder().setSampleDuration(sampleDuration)
                .setResultMaxSize(resultMaxSize).setSessionMode(ProfilerServiceTypes.ProfilerSessionConfig.Mode.OFFLINE)
                .addBuffers(bf).setResultFile("/data/local/tmp/hiprofiler_data.htrace").build();
        CommonTypes.ProfilerPluginConfig plugConfig =
            CommonTypes.ProfilerPluginConfig.newBuilder().setName(pluginName).setPluginSha256("111")
                .setSampleInterval(1).setConfigData(ByteString.copyFrom(ccByte)).build();
        ProfilerServiceTypes.CreateSessionRequest request =
            ProfilerServiceTypes.CreateSessionRequest.newBuilder().setRequestId(1).setSessionConfig(sessionConfig)
                .addPluginConfigs(plugConfig).build();
        if (isReturn) {
            String[] strCommand = request.toString().split("config_data");
            return strCommand[0].concat("config_data:").concat("\"").concat(configDataStr).concat("\"")
                .concat(System.lineSeparator() + "}");
        } else {
            // 通知端侧要用那些插件（分配资源）
            ProfilerServiceTypes.CreateSessionResponse response1 = client.createSession(request);
            ProfilerServiceTypes.StartSessionRequest requestStartSession = ProfilerServiceHelper
                .startSessionRequest(CommonUtil.getRequestId(), response1.getSessionId(), new ArrayList<>());
            // 调用哪些进程（采集数据）
            ProfilerServiceTypes.StartSessionResponse startSessionResponse = client.startSession(requestStartSession);
            return "";
        }
    }

    private String getConfigDataStr(HashMap<String, ArrayList<String>> param,
        TracePluginConfigOuterClass.TracePluginConfig.Builder build, String configDataStr, String key) {
        String configStr = configDataStr;
        switch (key) {
            case SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_KIND_FTRACE_EVENT:
                for (int index = 0; index < param.get(key).size(); index++) {
                    build.addFtraceEvents(param.get(key).get(index));
                    configStr =
                        configStr.concat(System.lineSeparator()).concat(param.get(key).get(index));
                }
                break;
            case SystemTunningProbesCheckbox.SYSTEM_TUNNING_PROBES_KIND_ATRACE_APPS:
                for (int index = 0; index < param.get(key).size(); index++) {
                    build.addBytraceApps(param.get(key).get(index));
                    configStr.concat(param.get(key).get(index));
                }
                break;
            default:
        }
        return configStr;
    }
}
