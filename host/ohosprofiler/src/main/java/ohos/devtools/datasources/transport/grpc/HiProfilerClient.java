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
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.MemoryPluginConfig;
import ohos.devtools.datasources.transport.grpc.service.MemoryPluginResult;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.utils.common.util.CommonUtil;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.views.common.LayoutConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static ohos.devtools.datasources.utils.common.Constant.DEVICE_FULL_TYPE;
import static ohos.devtools.datasources.utils.common.Constant.MEMORY_PLUG;
import static ohos.devtools.datasources.utils.common.Constant.MEMORY_PLUGS_NAME;
import static ohos.devtools.views.common.ViewConstants.NUM_2;
import static ohos.devtools.views.common.ViewConstants.NUM_5;

/**
 * Provide device-side grpc interface encapsulation for each module in the application
 *
 * @version 1.0
 * @date 2021/02/01 19:23
 **/
public final class HiProfilerClient {
    private static final Logger LOGGER = LogManager.getLogger(HiProfilerClient.class);

    private static final String IP = InetAddress.getLoopbackAddress().getHostAddress();

    /**
     * Used to store the created Profiler
     */
    private static ConcurrentHashMap<String, ProfilerClient> profilerClientMap =
        new ConcurrentHashMap<>(CommonUtil.collectionSize(0));

    /**
     * Singleton Class Instance
     *
     * @version 1.0
     * @date 2021/02/01 19:23
     **/
    private static class SingletonClassInstance {
        private static final HiProfilerClient INSTANCE = new HiProfilerClient();
    }

    /**
     * Get instance
     *
     * @return HiProfilerClient
     */
    public static HiProfilerClient getInstance() {
        return SingletonClassInstance.INSTANCE;
    }

    private HiProfilerClient() {
    }

    /**
     * Get profilerclient
     *
     * @param ip      ip address
     * @param port    port number
     * @param channel channel
     * @return ProfilerClient
     */
    public ProfilerClient getProfilerClient(String ip, int port, ManagedChannel channel) {
        String mapKey = IP + port;
        if (port <= 0 || port > LayoutConstants.PORT) {
            return null;
        }
        if (Objects.isNull(profilerClientMap.get(mapKey))) {
            ProfilerClient profilerClient = new ProfilerClient(IP, port, channel);
            profilerClientMap.put(mapKey, profilerClient);
            return profilerClient;
        }
        return profilerClientMap.get(mapKey);
    }

    /**
     * get profilerClient.
     *
     * @param ip   ip address
     * @param port port number
     * @return ProfilerClient
     */
    public ProfilerClient getProfilerClient(String ip, int port) {
        if (port <= 0 || port > LayoutConstants.PORT) {
            return null;
        }
        String mapKey = IP + port;
        if (profilerClientMap.get(mapKey) == null) {
            ProfilerClient profilerClient = new ProfilerClient(IP, port);
            profilerClientMap.put(mapKey, profilerClient);
            return profilerClient;
        }
        return profilerClientMap.get(mapKey);
    }

    /**
     * Destroy profilerClient
     *
     * @param ip   ip address
     * @param port port number
     * @return boolean
     */
    public boolean destroyProfiler(String ip, int port) {
        if (port <= 0 || port > LayoutConstants.PORT) {
            return false;
        }
        String mapKey = IP + port;
        if (Objects.isNull(profilerClientMap.get(mapKey))) {
            return true;
        }
        ProfilerClient client = profilerClientMap.get(mapKey);
        client.shutdown();
        return profilerClientMap.remove(mapKey, client);
    }

    /**
     * requestCreateSession
     *
     * @param port              port number
     * @param name              name
     * @param pid               pid
     * @param reportProcessTree report process tree
     * @param deviceType        device Type
     * @return int
     */
    public int requestCreateSession(int port, String name, int pid, boolean reportProcessTree, String deviceType) {
        if (port <= 0 || port > LayoutConstants.PORT) {
            return -1;
        }
        ProfilerClient client = getProfilerClient("", port);
        LOGGER.info("process Session start222", DateTimeUtil.getNowTimeLong());
        if (client.isUsed()) {
            LOGGER.info("process Session is Used", DateTimeUtil.getNowTimeLong());
            return -1;
        }
        client.setUsed(true);
        LOGGER.info("process Session start3333", DateTimeUtil.getNowTimeLong());
        MemoryPluginConfig.MemoryConfig plug;
        int pages = 2;
        if (DEVICE_FULL_TYPE.equals(deviceType)) {
            pages = 10;
            plug = MemoryPlugHelper.createMemRequest(pid, reportProcessTree, false, false, true);
        } else {
            plug = MemoryPlugHelper.createMemRequest(pid, reportProcessTree, false, false, false);
        }
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfig = ProfilerServiceHelper
            .profilerSessionConfig(true, null, pages,
                ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE);
        CommonTypes.ProfilerPluginConfig plugConfig =
            ProfilerServiceHelper.profilerPluginConfig(name, "ABDSSFDFG", 0, plug.toByteString());
        List<CommonTypes.ProfilerPluginConfig> plugs = new ArrayList();
        plugs.add(plugConfig);
        ProfilerServiceTypes.CreateSessionRequest request =
            ProfilerServiceHelper.createSessionRequest(CommonUtil.getRequestId(), sessionConfig, plugs);
        ProfilerServiceTypes.CreateSessionResponse response = null;
        try {
            response = client.createSession(request);
            LOGGER.info("process Session start444 {} ", DateTimeUtil.getNowTimeLong());
        } catch (StatusRuntimeException exception) {
            destroyProfiler("", port);
            return -1;
        }
        client.setUsed(false);
        return response.getSessionId();
    }

    /**
     * Request to start session
     *
     * @param deviceIp  deviceIp
     * @param port      port number
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean requestStartSession(String deviceIp, int port, int sessionId) {
        if (port <= 0 || port > LayoutConstants.PORT) {
            return false;
        }
        return requestStartSession(deviceIp, port, sessionId, 0);
    }

    private boolean requestStartSession(String deviceIp, int port, int sessionId, int retryCount) {
        int retryCounts = retryCount + 1;
        ProfilerServiceTypes.StartSessionRequest requestStartSession =
            ProfilerServiceHelper.startSessionRequest(CommonUtil.getRequestId(), sessionId, new ArrayList<>());
        ProfilerServiceTypes.StartSessionResponse response = null;
        ProfilerClient client = getProfilerClient(deviceIp, port);
        if (client.isUsed()) {
            return false;
        }
        client.setUsed(true);
        try {
            response = client.startSession(requestStartSession);
        } catch (StatusRuntimeException exception) {
            destroyProfiler("", port);
            if (retryCounts > NUM_5) {
                return true;
            }
            return requestStartSession(deviceIp, port, sessionId, retryCounts);
        }
        client.setUsed(false);
        return response.getStatus() == 0 ? true : false;
    }

    /**
     * requestStopSession
     *
     * @param deviceIp  deviceIp
     * @param port      port number
     * @param sessionId sessionId
     * @param isForce   isForce
     * @return boolean
     */
    public boolean requestStopSession(String deviceIp, int port, int sessionId, boolean isForce) {
        if (port <= 0 || port > LayoutConstants.PORT) {
            return false;
        }
        return requestStopSession(deviceIp, port, sessionId, isForce, 0);
    }

    private boolean requestStopSession(String deviceIp, int port, int sessionId, boolean isForce, int retryCount) {
        ProfilerClient client = getProfilerClient(deviceIp, port);
        if (isForce) {
            client.setUsed(false);
        }
        if (client.isUsed()) {
            return false;
        }
        client.setUsed(true);
        ProfilerServiceTypes.StopSessionRequest stopSession =
            ProfilerServiceHelper.stopSessionRequest(CommonUtil.getRequestId(), sessionId);
        ProfilerServiceTypes.StopSessionResponse response = null;
        int retryCounts = retryCount;
        try {
            retryCounts = retryCount + 1;
            Long stopTime = DateTimeUtil.getNowTimeLong();
            LOGGER.info("startStopSession {}", stopTime);
            response = client.stopSession(stopSession);
            LOGGER.info("startStopEndSession {}", DateTimeUtil.getNowTimeLong() - stopTime);
        } catch (StatusRuntimeException exception) {
            LOGGER.info("stopSession has Exception {}", exception.getMessage());
            destroyProfiler(deviceIp, port);
            if (retryCounts > NUM_2) {
                return true;
            }
            return requestStopSession(deviceIp, port, sessionId, false, retryCounts);
        }
        client.setUsed(false);
        return response.getStatus() == 0 ? true : false;
    }

    /**
     * request destory Session
     *
     * @param deviceIp  deviceIp
     * @param port      port number
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean requestDestroySession(String deviceIp, int port, int sessionId) {
        if (port <= 0 || port > LayoutConstants.PORT) {
            return false;
        }
        return requestDestroySession(deviceIp, port, sessionId, 0);
    }

    private boolean requestDestroySession(String deviceIp, int port, int sessionId, int retryCount) {
        int retryCounts = retryCount + 1;
        ProfilerClient client = getProfilerClient(deviceIp, port);
        if (client.isUsed()) {
            return false;
        }
        client.setUsed(true);
        ProfilerServiceTypes.DestroySessionRequest req =
            ProfilerServiceHelper.destroySessionRequest(CommonUtil.getRequestId(), sessionId);
        ProfilerServiceTypes.DestroySessionResponse response = null;
        try {
            response = client.destroySession(req);
        } catch (StatusRuntimeException exception) {
            destroyProfiler(deviceIp, port);
            if (retryCounts > NUM_2) {
                return true;
            }
            return requestDestroySession(deviceIp, port, sessionId, retryCounts);
        }
        client.setUsed(false);
        return response.getStatus() == 0 ? true : false;
    }

    /**
     * Fetch process data
     *
     * @param deviceIp  deviceIp
     * @param port      port number
     * @param sessionId sessionId
     * @return List <ProcessInfo>
     */
    public List<ProcessInfo> fetchProcessData(String deviceIp, int port, int sessionId) {
        ProfilerClient client = getProfilerClient(deviceIp, port);
        if (client.isUsed()) {
            return new ArrayList<>();
        }
        client.setUsed(true);
        List<ProcessInfo> processInfos = new ArrayList<>();
        ProfilerServiceTypes.FetchDataRequest fetchData =
            ProfilerServiceHelper.fetchDataRequest(CommonUtil.getRequestId(), sessionId, null);
        Iterator<ProfilerServiceTypes.FetchDataResponse> res = null;
        try {
            res = client.fetchData(fetchData);
        } catch (StatusRuntimeException exception) {
            destroyProfiler(deviceIp, port);
            LOGGER.info("GrpcException {}", exception.getMessage());
            return new ArrayList<>();
        }
        try {
            if (res.hasNext()) {
                ProfilerServiceTypes.FetchDataResponse fetchDataResponse = res.next();
                int pluginStatus = fetchDataResponse.getStatus();
                if (pluginStatus != 0) {
                    client.setUsed(false);
                    return new ArrayList<>();
                }
                List<CommonTypes.ProfilerPluginData> lists = fetchDataResponse.getPluginDataList();
                processInfos = extractedData(lists);
            }
        } catch (StatusRuntimeException statusRuntimeException) {
            destroyProfiler(deviceIp, port);
            LOGGER.error(" get ProcessInfo failed {}", statusRuntimeException.getMessage());
        }
        client.setUsed(false);
        return processInfos;
    }

    private List<ProcessInfo> extractedData(List<CommonTypes.ProfilerPluginData> lists) {
        List<ProcessInfo> process = new ArrayList<>();

        for (CommonTypes.ProfilerPluginData profilerPluginData : lists) {
            if (MEMORY_PLUGS_NAME.equals(profilerPluginData.getName()) || MEMORY_PLUG
                .equals(profilerPluginData.getName())) {
                if (profilerPluginData.getStatus() != 0) {
                    continue;
                }
                ByteString data = profilerPluginData.getData();
                MemoryPluginResult.MemoryData.Builder builder = MemoryPluginResult.MemoryData.newBuilder();
                MemoryPluginResult.MemoryData memorydata = null;
                try {
                    memorydata = builder.mergeFrom(data).build();
                } catch (InvalidProtocolBufferException exception) {
                    LOGGER.info("mergeFrom failed {}", exception.getMessage());
                }
                List<MemoryPluginResult.ProcessMemoryInfo> processMemoryInfos = memorydata.getProcessesinfoList();
                for (MemoryPluginResult.ProcessMemoryInfo processMemoryInfo : processMemoryInfos) {
                    ProcessInfo processInfo = new ProcessInfo();
                    processInfo.setProcessId(processMemoryInfo.getPid());
                    processInfo.setProcessName(processMemoryInfo.getName());
                    process.add(processInfo);
                }
            }
        }
        return process;
    }

    /**
     * Get capabilities
     *
     * @param deviceIp deviceIp
     * @param port     port number
     * @return ProfilerServiceTypes.GetCapabilitiesResponse
     */
    public ProfilerServiceTypes.GetCapabilitiesResponse getCapabilities(String deviceIp, int port) {
        if (port <= 0 || port > LayoutConstants.PORT) {
            return null;
        }
        return getCapabilities(deviceIp, port, 0);
    }

    /**
     * Get capabilities
     *
     * @param deviceIp   deviceIp
     * @param port       port number
     * @param retryCount retry Count
     * @return ProfilerServiceTypes.GetCapabilitiesResponse
     */
    private ProfilerServiceTypes.GetCapabilitiesResponse getCapabilities(String deviceIp, int port, int retryCount) {
        int counts = retryCount + 1;
        ProfilerServiceTypes.GetCapabilitiesResponse response;
        ProfilerClient client = getProfilerClient(deviceIp, port);
        try {
            response = client.getCapabilities(
                ProfilerServiceTypes.GetCapabilitiesRequest.newBuilder().setRequestId(CommonUtil.getRequestId())
                    .build());
        } catch (StatusRuntimeException exception) {
            LOGGER.info("exception Error {}", exception.getMessage());
            destroyProfiler(deviceIp, port);
            if (counts > NUM_2) {
                return ProfilerServiceTypes.GetCapabilitiesResponse.newBuilder().build();
            }
            return getCapabilities(deviceIp, port, counts);
        }
        return response;
    }
}
