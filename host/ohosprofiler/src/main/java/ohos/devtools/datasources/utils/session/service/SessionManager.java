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

package ohos.devtools.datasources.utils.session.service;

import com.alibaba.fastjson.JSONObject;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import io.grpc.StatusRuntimeException;
import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.databases.datatable.MemoryTable;
import ohos.devtools.datasources.databases.datatable.enties.ProcessMemInfo;
import ohos.devtools.datasources.transport.grpc.HiProfilerClient;
import ohos.devtools.datasources.transport.grpc.MemoryPlugHelper;
import ohos.devtools.datasources.transport.grpc.ProfilerClient;
import ohos.devtools.datasources.transport.grpc.ProfilerServiceHelper;
import ohos.devtools.datasources.transport.grpc.service.AgentPluginConfig;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.MemoryPluginConfig;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.datasources.utils.common.Constant;
import ohos.devtools.datasources.utils.common.util.CommonUtil;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.common.util.PrintUtil;
import ohos.devtools.datasources.utils.datahandler.datapoller.DataPoller;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceProcessInfo;
import ohos.devtools.datasources.utils.device.entity.TraceFileInfo;
import ohos.devtools.datasources.utils.monitorconfig.service.MonitorConfigManager;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.session.entity.SessionInfo;
import ohos.devtools.services.memory.ClassInfo;
import ohos.devtools.services.memory.ClassInfoDao;
import ohos.devtools.services.memory.ClassInfoManager;
import ohos.devtools.services.memory.MemoryHeapDao;
import ohos.devtools.services.memory.MemoryHeapInfo;
import ohos.devtools.services.memory.MemoryHeapManager;
import ohos.devtools.services.memory.MemoryInstanceDao;
import ohos.devtools.services.memory.MemoryInstanceDetailsDao;
import ohos.devtools.services.memory.MemoryInstanceDetailsInfo;
import ohos.devtools.services.memory.MemoryInstanceDetailsManager;
import ohos.devtools.services.memory.MemoryInstanceInfo;
import ohos.devtools.services.memory.MemoryInstanceManager;
import ohos.devtools.services.memory.MemoryService;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JProgressBar;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Locale.ENGLISH;
import static ohos.devtools.datasources.transport.hdc.HdcCommandEnum.HDC_START_JAVAHEAP;
import static ohos.devtools.datasources.utils.common.Constant.DEVICE_FULL_TYPE;
import static ohos.devtools.datasources.utils.common.Constant.JVMTI_AGENT_PLUG;
import static ohos.devtools.datasources.utils.common.Constant.MEMORY_PLUG;
import static ohos.devtools.datasources.utils.common.Constant.MEMORY_PLUGS;
import static ohos.devtools.views.common.LayoutConstants.TWO_THOUSAND;

/**
 * @Description session Management core class
 * @Date 2021/2/7 13:30
 **/
public class SessionManager {
    /**
     * Global log
     */
    private static final Logger LOGGER = LogManager.getLogger(SessionManager.class);

    /**
     * Singleton session.
     */
    private static final SessionManager SINGLETON = new SessionManager();

    /**
     * getInstance
     *
     * @return SessionManager
     */
    public static SessionManager getInstance() {
        return SessionManager.SINGLETON;
    }

    /**
     * developMode
     */
    private boolean developMode = false;

    /**
     * Analyzed Sessions
     */
    private HashMap<Long, SessionInfo> profilingSessions;

    private HashMap<Long, DataPoller> dataPollerHashMap = new HashMap<>();

    private MemoryTable memoTable;

    private ClassInfoDao classInfoDao;

    private MemoryHeapDao memoryHeapDao;

    private MemoryInstanceDao memoryInstanceDao;

    private MemoryInstanceDetailsDao memoryInstanceDetailsDao;

    private List<CommonTypes.ProfilerPluginConfig> plugs;

    private ProfilerServiceTypes.ProfilerSessionConfig.Builder sessionConfigBuilder;

    private SessionManager() {
        profilingSessions = new HashMap<>();
    }

    /**
     * Clear session Id directly, use with caution
     *
     * @param localSessionId localSessionId
     */
    public void deleteLocalSession(Long localSessionId) {
        if (profilingSessions != null) {
            ProfilerChartsView profilerChartsView = ProfilerChartsView.sessionMap.get(localSessionId);
            if (profilerChartsView != null) {
                profilerChartsView.getObserver().stopRefresh(true);
            }
            profilingSessions.remove(localSessionId);
        }
    }

    /**
     * Create Session based on device information, process information, and specific scenarios
     *
     * @param device     device
     * @param process    process
     * @param model      model
     * @param configJson configJson
     * @return boolean
     * @date 2021/2/7 16:22
     */
    public Long createSession(DeviceIPPortInfo device, ProcessInfo process, int model, JSONObject configJson) {
        if (device == null || process == null || configJson == null || device.getForwardPort() == 0) {
            return -1L;
        }
        int sessionId = 0;
        // Real-time scene
        Long localSessionID = Constant.ABNORMAL;
        if (model == Constant.REALTIME_SCENE) {
            localSessionID = handleAgentConfig(configJson, device, process);
            if (localSessionID == Constant.ABNORMAL) {
                return Constant.ABNORMAL;
            }
            ProfilerServiceTypes.CreateSessionRequest request = ProfilerServiceHelper
                .createSessionRequest(CommonUtil.getRequestId(), sessionConfigBuilder.build(), plugs);
            ProfilerClient createSessionClient =
                HiProfilerClient.getInstance().getProfilerClient(device.getIp(), device.getForwardPort());
            if (createSessionClient.isUsed()) {
                LOGGER.error("create Session failed");
                return Constant.ABNORMAL;
            }
            ProfilerServiceTypes.CreateSessionResponse respon = null;
            try {
                createSessionClient.setUsed(true);
                respon = createSessionClient.createSession(request);
            } catch (StatusRuntimeException statusRuntimeException) {
                if ("UNAVAILABLE".equals(statusRuntimeException.getStatus().getCode())) {
                    HiProfilerClient.getInstance().destroyProfiler(device.getIp(), device.getForwardPort());
                }
                LOGGER.error("status RuntimeException getStatus:{}", statusRuntimeException.getStatus());
                return Constant.ABNORMAL;
            }
            sessionId = respon.getSessionId();
            createSessionClient.setUsed(false);
        }
        int pid = process.getProcessId();
        String deviceId = device.getDeviceID();
        String sessionName = CommonUtil.generateSessionName(deviceId, pid);
        SessionInfo session =
            SessionInfo.builder().sessionId(sessionId).sessionName(sessionName).deviceIPPortInfo(device).build();
        // 建立LocalSessionId和端侧session的关系
        profilingSessions.put(localSessionID, session);
        if (sessionId != 0) {
            PrintUtil.print(LOGGER, "Task with Session created successfully.", 0);
            return localSessionID;
        } else {
            LOGGER.error("Failed to create task with Session!");
            return Constant.ABNORMAL;
        }
    }

    private long handleAgentConfig(JSONObject configJson, DeviceIPPortInfo device, ProcessInfo process) {
        long localSessionID = CommonUtil.getLocalSessionId();
        String agentPlug = "jvmtiagent_" + process.getProcessName();
        boolean startJavaHeap = isStartJavaHeap(device, agentPlug);
        String proc = process.getProcessName();
        MonitorConfigManager.getInstance().analyzeCharTarget(localSessionID, configJson);
        if (StringUtils.isNotBlank(proc) && (!startJavaHeap)) {
            String hdcCommand = String.format(ENGLISH, HDC_START_JAVAHEAP.getHdcCommand(), device.getDeviceID(), proc);
            String res = HdcWrapper.getInstance().getHdcStringResult(hdcCommand);
            if (res.contains("javaHeapSuccess")) {
                startJavaHeap = true;
            }
        }
        LOGGER.info("Start agent status is {} ", startJavaHeap);
        ProfilerServiceTypes.GetCapabilitiesResponse capabilitiesRes =
            HiProfilerClient.getInstance().getCapabilities(device.getIp(), device.getForwardPort());
        List<ProfilerServiceTypes.ProfilerPluginCapability> capability = capabilitiesRes.getCapabilitiesList();
        sessionConfigBuilder = ProfilerServiceTypes.ProfilerSessionConfig.newBuilder()
            .setSessionMode(ProfilerServiceTypes.ProfilerSessionConfig.Mode.ONLINE);
        if (capability == null || capability.size() == 0) {
            localSessionID = Constant.ABNORMAL;
        }
        List<ProfilerServiceTypes.ProfilerPluginCapability> memPlug = getLibmemdataplugin(capability, MEMORY_PLUGS);
        List<ProfilerServiceTypes.ProfilerPluginCapability> list = getLibmemdataplugin(capability, agentPlug);
        plugs = new ArrayList<>();
        MemoryPluginConfig.MemoryConfig plug = getConfig(device, process);
        if (!memPlug.isEmpty()) {
            ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Builder memoryBuffer = getBuilder();
            CommonTypes.ProfilerPluginConfig plugConfig =
                ProfilerServiceHelper.profilerPluginConfig(memPlug.get(0).getName(), "", 40, plug.toByteString());
            sessionConfigBuilder.addBuffers(memoryBuffer);
            plugs.add(plugConfig);
        }
        if (startJavaHeap && (!list.isEmpty())) {
            AgentPluginConfig.AgentConfig agent =
                AgentPluginConfig.AgentConfig.newBuilder().setPid(process.getProcessId()).build();
            CommonTypes.ProfilerPluginConfig jvmTiAgent =
                ProfilerServiceHelper.profilerPluginConfig(list.get(0).getName(), "", 100, agent.toByteString());
            ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Builder javaHeapBuffer =
                ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.newBuilder().setPages(TWO_THOUSAND)
                    .setPolicy(ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE);
            sessionConfigBuilder.addBuffers(javaHeapBuffer);
            plugs.add(jvmTiAgent);
        }
        if (plugs.isEmpty()) {
            localSessionID = Constant.ABNORMAL;
        }
        return localSessionID;
    }

    private List<ProfilerServiceTypes.ProfilerPluginCapability> getLibmemdataplugin(
        List<ProfilerServiceTypes.ProfilerPluginCapability> capabilities, String libmemdataplugin) {
        return capabilities.stream()
            .filter(profilerPluginCapability -> profilerPluginCapability.getName().contains(libmemdataplugin))
            .collect(Collectors.toList());
    }

    private boolean isStartJavaHeap(DeviceIPPortInfo device, String agentPlugName) {
        boolean startJavaHeap = false;
        ProfilerServiceTypes.GetCapabilitiesResponse response =
            HiProfilerClient.getInstance().getCapabilities(device.getIp(), device.getForwardPort());
        List<ProfilerServiceTypes.ProfilerPluginCapability> capabilitiesResponse = response.getCapabilitiesList();
        List<ProfilerServiceTypes.ProfilerPluginCapability> agentStatus =
            getLibmemdataplugin(capabilitiesResponse, agentPlugName);
        if (!agentStatus.isEmpty()) {
            startJavaHeap = true;
        }
        return startJavaHeap;
    }

    private ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Builder getBuilder() {
        return ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.newBuilder().setPages(LayoutConstants.TEN)
            .setPolicy(ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE);
    }

    private MemoryPluginConfig.MemoryConfig getConfig(DeviceIPPortInfo device, ProcessInfo process) {
        MemoryPluginConfig.MemoryConfig plug;
        if (DEVICE_FULL_TYPE.equals(device.getDeviceType())) {
            plug = MemoryPlugHelper.createMemRequest(process.getProcessId(), false, true, true, true);
        } else {
            plug = MemoryPlugHelper.createMemRequest(process.getProcessId(), false, true, true, false);
        }
        return plug;
    }

    /**
     * Establish a session with the end side and start the session.
     *
     * @param localSessionId Local Session Id
     * @param restartFlag    Whether to start again
     * @return boolean
     * @date 2021/2/4 17:37
     */
    public boolean startSession(Long localSessionId, boolean restartFlag) {
        if (localSessionId == null) {
            return false;
        }
        SessionInfo session = profilingSessions.get(localSessionId);
        if (session == null) {
            return true;
        }
        if (restartFlag) {
            // Click start, delete the previous data first
            MemoryService.getInstance().deleteSessionData(localSessionId);
            deleteAllAgentData(localSessionId, false);
        }
        int sessionId = session.getSessionId();
        DeviceIPPortInfo device = session.getDeviceIPPortInfo();
        return HiProfilerClient.getInstance().requestStartSession(device.getIp(), device.getForwardPort(), sessionId);
    }

    /**
     * Turn on polling to get data
     *
     * @param localSessionId localSessionId
     * @return boolean Turn on polling
     * @date 2021/2/22 14:57
     */
    public boolean fetchData(Long localSessionId) {
        try {
            if (localSessionId == null || localSessionId <= 0) {
                return false;
            }
            SessionInfo session = profilingSessions.get(localSessionId);
            if (session == null) {
                return true;
            }
            DeviceIPPortInfo device = session.getDeviceIPPortInfo();
            ProfilerClient client =
                HiProfilerClient.getInstance().getProfilerClient(device.getIp(), device.getForwardPort());
            if (client.isUsed()) {
                return false;
            }
            client.setUsed(true);
            HashMap<String, AbstractDataStore> map = new HashMap();
            map.put(MEMORY_PLUG, new MemoryTable());
            map.put(JVMTI_AGENT_PLUG, new ClassInfoDao());
            map.put("jvmtiagentDetails", new MemoryInstanceDetailsDao());
            map.put("jvmtiagentInstance", new MemoryInstanceDao());
            map.put("jvmtiagentMemoryHeap", new MemoryHeapDao());
            LOGGER.info("start new DataPoller {}", DateTimeUtil.getNowTimeLong());
            int sessionId = session.getSessionId();
            DataPoller dataPoller = new DataPoller(localSessionId, sessionId, client, map);
            dataPoller.start();
            dataPollerHashMap.put(localSessionId, dataPoller);
            return true;
        } catch (Exception exception) {
            LOGGER.error(exception.getMessage());
            return false;
        }
    }

    /**
     * isRefsh
     *
     * @param localSessionId localSessionId
     * @return boolean
     */
    public SessionInfo isRefsh(Long localSessionId) {
        return profilingSessions.get(localSessionId);
    }

    /**
     * View stop Loading
     *
     * @param localSession   local Session
     * @param firstTimeStamp first Time Stamp
     */
    public void stopLoadingView(Long localSession, long firstTimeStamp) {
        SessionInfo sessionInfo = profilingSessions.get(localSession);
        if (sessionInfo != null) {
            sessionInfo.setStartTimestamp(firstTimeStamp);
            sessionInfo.setStartRefsh(true);
            profilingSessions.put(localSession, sessionInfo);
        }
    }

    /**
     * stop Session
     *
     * @param localSessionId localSessionId
     * @return boolean Stop success indicator
     * @date 2021/2/20 16:20
     */
    public boolean endSession(Long localSessionId) {
        if (localSessionId == null || localSessionId <= 0) {
            return false;
        }
        SessionInfo session = profilingSessions.get(localSessionId);
        if (session == null) {
            return true;
        }
        session.setStartRefsh(false);
        int sessionId = session.getSessionId();
        DeviceIPPortInfo device = session.getDeviceIPPortInfo();
        boolean stopSessionRes =
            HiProfilerClient.getInstance().requestStopSession(device.getIp(), device.getForwardPort(), sessionId, true);
        if (stopSessionRes) {
            DataPoller dataPoller = dataPollerHashMap.get(localSessionId);
            if (dataPoller != null) {
                dataPoller.shutDown();
            }

            PrintUtil.print(LOGGER, "Task with Session stopped successfully.", 0);
        } else {
            LOGGER.error("Failed to stop task with Session!");
        }
        return stopSessionRes;
    }

    /**
     * Delete the Session session interface
     *
     * @param localSessionId localSessionId
     * @return boolean Is the deletion successful
     */
    public boolean deleteSession(Long localSessionId) {
        try {
            if (localSessionId == null || localSessionId <= 0) {
                return false;
            }
            SessionInfo session = profilingSessions.get(localSessionId);
            if (session == null) {
                return false;
            }
            // Delete session information in local memory
            profilingSessions.remove(localSessionId);
            // Delete the data information related to the session of the database
            MemoryService.getInstance().deleteSessionData(localSessionId);
            deleteAllAgentData(localSessionId, true);
            if (session.isOfflineMode()) {
                return true;
            }
            int sessionId = session.getSessionId();
            DeviceIPPortInfo device = session.getDeviceIPPortInfo();
            boolean stopSessionRes = HiProfilerClient.getInstance()
                .requestStopSession(device.getIp(), device.getForwardPort(), sessionId, true);
            // Delete collection item
            if (stopSessionRes) {
                boolean destroySessionRes = false;
                try {
                    destroySessionRes = HiProfilerClient.getInstance()
                        .requestDestroySession(device.getIp(), device.getForwardPort(), sessionId);
                    if (destroySessionRes) {
                        DataPoller dataPooler = dataPollerHashMap.get(localSessionId);
                        if (dataPooler != null) {
                            dataPooler.shutDown();
                        }
                        HiProfilerClient.getInstance().destroyProfiler(device.getIp(), device.getForwardPort());
                    }
                } catch (StatusRuntimeException exception) {
                    LOGGER.error(exception.getMessage());
                }
                PrintUtil.print(LOGGER, "Task with Session deleted successfully.", 0);
                return destroySessionRes;
            } else {
                LOGGER.error("Failed to delete task with Session ");
                return false;
            }
        } finally {
            if (localSessionId != null && localSessionId > 0) {
                MemoryService.getInstance().deleteSessionData(localSessionId);
                deleteAllAgentData(localSessionId, true);
            }
        }
    }

    private void deleteAllAgentData(Long localSessionId, boolean deleteClassInfo) {
        if (memoTable == null) {
            memoTable = new MemoryTable();
        }
        if (memoryHeapDao == null) {
            memoryHeapDao = new MemoryHeapDao();
        }
        if (memoryInstanceDao == null) {
            memoryInstanceDao = new MemoryInstanceDao();
        }
        if (classInfoDao == null) {
            classInfoDao = new ClassInfoDao();
        }
        if (memoryInstanceDetailsDao == null) {
            memoryInstanceDetailsDao = new MemoryInstanceDetailsDao();
        }
        if (deleteClassInfo) {
            classInfoDao.deleteSessionData(localSessionId);
        }
        memoryHeapDao.deleteSessionData(localSessionId);
        memoryInstanceDao.deleteSessionData(localSessionId);
        memoryInstanceDetailsDao.deleteSessionData(localSessionId);
    }

    /**
     * Used to notify the end side to close all session connections after the IDE is closed.
     */
    public void stopAllSession() {
        if (profilingSessions.isEmpty()) {
            return;
        }
        profilingSessions.values().forEach(sessionInfo -> {
            HiProfilerClient hiprofiler = HiProfilerClient.getInstance();
            DeviceIPPortInfo device = sessionInfo.getDeviceIPPortInfo();
            if (device != null) {
                hiprofiler
                    .requestStopSession(device.getIp(), device.getForwardPort(), sessionInfo.getSessionId(), true);
                hiprofiler.requestDestroySession(device.getIp(), device.getForwardPort(), sessionInfo.getSessionId());
                hiprofiler.destroyProfiler(device.getIp(), device.getForwardPort());
            }
        });
    }

    /**
     * Save the collected data to a file.
     *
     * @param sessionId         sessionId
     * @param deviceProcessInfo deviceProcessInfo
     * @param pathname          pathname
     * @return boolean
     */
    public boolean saveSessionDataToFile(long sessionId, DeviceProcessInfo deviceProcessInfo, String pathname) {
        if (sessionId <= 0 || deviceProcessInfo == null || StringUtils.isEmpty(pathname)) {
            return false;
        }
        List<ProcessMemInfo> memInfoList = MemoryService.getInstance().getAllData(sessionId);
        List<ClassInfo> classInfos = new ClassInfoManager().getAllClassInfoData(sessionId);
        List<MemoryHeapInfo> memoryHeapInfos = new MemoryHeapManager().getAllMemoryHeapInfos(sessionId);
        List<MemoryInstanceDetailsInfo> detailsInfos = new MemoryInstanceDetailsManager().getAllMemoryInstanceDetails();
        ArrayList<MemoryInstanceInfo> memoryInstanceInfos = new MemoryInstanceManager().getAllMemoryInstanceInfos();
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            File file = new File(pathname);
            fileOutputStream = new FileOutputStream(file);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            // Start importing the number of meminfo in an object record file
            TraceFileInfo startObj = new TraceFileInfo();
            int recordNum = memInfoList.size() + classInfos.size() + memoryHeapInfos.size() + detailsInfos.size()
                + memoryInstanceInfos.size();
            startObj.setRecordNum(recordNum);
            startObj.setCreateTime(new Date().getTime());
            // Set the trace file version, the subsequent file save content format changes and is not compatible with
            // the previous file, you need to modify the version number, and you need to modify the version number
            // in the local Session Data From File method.
            startObj.setVersion("V1.0");
            objectOutputStream.writeObject(startObj);

            for (int index = 0; index < memInfoList.size(); index++) {
                ProcessMemInfo memObject = memInfoList.get(index);
                objectOutputStream.writeObject(memObject);
                if (index == 0) {
                    deviceProcessInfo.setStartTime(memObject.getTimeStamp());
                }
                if (index == (memInfoList.size() - 1)) {
                    deviceProcessInfo.setEndTime(memObject.getTimeStamp());
                }
            }
            writeCollectionData(objectOutputStream, classInfos, memoryHeapInfos, detailsInfos, memoryInstanceInfos);
            objectOutputStream.writeObject(deviceProcessInfo);
            PrintUtil.print(LOGGER, "Task with Session ID {} Save To File successfully.", 0);
        } catch (IOException exception) {
            return false;
        } finally {
            closeIoStream(null, null, fileOutputStream, objectOutputStream);
        }
        return true;
    }

    private void writeCollectionData(ObjectOutputStream objectOutputStream, List<ClassInfo> classInfos,
        List<MemoryHeapInfo> memoryHeapInfos, List<MemoryInstanceDetailsInfo> detailsInfos,
        ArrayList<MemoryInstanceInfo> memoryInstanceInfos) throws IOException {
        for (ClassInfo classInfo : classInfos) {
            objectOutputStream.writeObject(classInfo);
        }
        for (MemoryHeapInfo memoryHeapInfo : memoryHeapInfos) {
            objectOutputStream.writeObject(memoryHeapInfo);
        }
        for (MemoryInstanceDetailsInfo instanceDetailsInfo : detailsInfos) {
            objectOutputStream.writeObject(instanceDetailsInfo);
        }
        for (int index = 0; index < memoryInstanceInfos.size(); index++) {
            MemoryInstanceInfo instanceInfo = memoryInstanceInfos.get(index);
            objectOutputStream.writeObject(instanceInfo);
        }
    }

    /**
     * local Session Data From File
     *
     * @param jProgressBar jProgressBar
     * @param file         file
     * @return Optional<DeviceProcessInfo>
     */
    public Optional<DeviceProcessInfo> localSessionDataFromFile(JProgressBar jProgressBar, File file) {
        if (jProgressBar == null || file == null) {
            return Optional.ofNullable(null);
        }
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        DeviceProcessInfo deviceProcessInfo = null;
        try {
            fileInputStream = new FileInputStream(file);
            objectInputStream = new ObjectInputStream(fileInputStream);
            Object firstObj = objectInputStream.readObject();
            TraceFileInfo traceFileInfo = null;
            if (firstObj instanceof TraceFileInfo) {
                traceFileInfo = (TraceFileInfo) firstObj;
                if (!"V1.0".equals(traceFileInfo.getVersion())) {
                    // The trace file is not the latest version
                    return Optional.empty();
                }
            } else {
                // The trace file is not the latest version
                return Optional.empty();
            }
            deviceProcessInfo = loadFileInDataBase(jProgressBar, traceFileInfo, objectInputStream);
        } catch (IOException | ClassNotFoundException exception) {
            if (exception.getMessage().indexOf("invalid stream header") >= 0) {
                if (file.getName().indexOf(".bin") >= 0) {
                    return Optional.empty();
                }
                return Optional.empty();
            }
            LOGGER.error("load Data Error {}", exception.getMessage());
            return Optional.empty();
        } finally {
            closeIoStream(fileInputStream, objectInputStream, null, null);
        }
        long localSessionId = deviceProcessInfo.getLocalSessionId();
        SessionInfo session = SessionInfo.builder().sessionName(String.valueOf(localSessionId)).build();
        session.setOfflineMode(true);
        profilingSessions.put(localSessionId, session);
        jProgressBar.setValue(LayoutConstants.HUNDRED);
        return Optional.of(deviceProcessInfo);
    }

    private DeviceProcessInfo loadFileInDataBase(JProgressBar jProgressBar, TraceFileInfo traceFileInfo,
        ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        List<ProcessMemInfo> processMemInfoList = new ArrayList();
        List<ClassInfo> classInfos = new ArrayList<>();
        List<MemoryHeapInfo> memoryHeapInfos = new ArrayList<>();
        List<MemoryInstanceInfo> instanceInfos = new ArrayList<>();
        List<MemoryInstanceDetailsInfo> detailsInfos = new ArrayList<>();
        long objNum = traceFileInfo.getRecordNum() + 1;
        long currentNum = 0;
        while (true) {
            Object object = objectInputStream.readObject();
            if (object instanceof DeviceProcessInfo) {
                // Finally, if there is still data in the datalist, import the database
                if ((!processMemInfoList.isEmpty()) || (!classInfos.isEmpty()) || (!memoryHeapInfos.isEmpty())
                    || (!instanceInfos.isEmpty()) || (!detailsInfos.isEmpty())) {
                    extracted(processMemInfoList, classInfos, memoryHeapInfos, instanceInfos, detailsInfos);
                    currentNum = currentNum + processMemInfoList.size();
                    int progress = (int) (currentNum * LayoutConstants.HUNDRED / objNum);
                    jProgressBar.setValue(progress);
                }
                DeviceProcessInfo deviceProcessInfo = (DeviceProcessInfo) object;
                return deviceProcessInfo;
            } else if (object instanceof ClassInfo) {
                ClassInfo classInfo = (ClassInfo) object;
                classInfos.add(classInfo);
                insertDataToDataBase(classInfos, false);
            } else if (object instanceof MemoryHeapInfo) {
                MemoryHeapInfo memoryHeapInfo = (MemoryHeapInfo) object;
                memoryHeapInfos.add(memoryHeapInfo);
                insertDataToDataBase(memoryHeapInfos, false);
            } else if (object instanceof MemoryInstanceDetailsInfo) {
                MemoryInstanceDetailsInfo detailsInfo = (MemoryInstanceDetailsInfo) object;
                detailsInfos.add(detailsInfo);
                insertDataToDataBase(detailsInfos, false);
            } else if (object instanceof MemoryInstanceInfo) {
                MemoryInstanceInfo memoryInstanceInfo = (MemoryInstanceInfo) object;
                instanceInfos.add(memoryInstanceInfo);
                insertDataToDataBase(instanceInfos, false);
            } else if (object instanceof ProcessMemInfo) {
                ProcessMemInfo processMem = (ProcessMemInfo) object;
                processMemInfoList.add(processMem);
                insertDataToDataBase(processMemInfoList, false);
            } else {
                continue;
            }
            currentNum = currentNum + 1;
            loadPercentage(jProgressBar, objNum, currentNum);
        }
    }

    private void loadPercentage(JProgressBar jProgressBar, long objNum, long currentNum) {
        int progress = (int) (currentNum * LayoutConstants.HUNDRED / objNum);
        double result = progress % 25;
        if (result == 0) {
            jProgressBar.setValue(progress);
        }
    }

    private void extracted(List<ProcessMemInfo> processMemInfoList, List<ClassInfo> classInfos,
        List<MemoryHeapInfo> memoryHeapInfos, List<MemoryInstanceInfo> instanceInfos,
        List<MemoryInstanceDetailsInfo> detailsInfos) {
        insertDataToDataBase(processMemInfoList, true);
        insertDataToDataBase(classInfos, true);
        insertDataToDataBase(memoryHeapInfos, true);
        insertDataToDataBase(instanceInfos, true);
        insertDataToDataBase(detailsInfos, true);
    }

    private void closeIoStream(FileInputStream fileInputStream, ObjectInputStream objectInputStream,
        FileOutputStream fileOutputStream, ObjectOutputStream objectOutputStream) {
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException exception) {
                LOGGER.error("exception:{}", exception.getMessage());
            }
        }
        if (objectInputStream != null) {
            try {
                objectInputStream.close();
            } catch (IOException exception) {
                LOGGER.error("exception:{}", exception.getMessage());
            }
        }
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
            } catch (IOException exception) {
                LOGGER.error("exception:{}", exception.getMessage());
            }
        }
        if (objectOutputStream != null) {
            try {
                objectOutputStream.close();
            } catch (IOException exception) {
                LOGGER.error("exception:{}", exception.getMessage());
            }
        }
    }

    private void insertDataToDataBase(List dataList, boolean endData) {
        if (dataList.size() < LayoutConstants.THREE_HUNDRED) {
            if (!endData) {
                return;
            }
        }
        if (dataList.size() == 0) {
            return;
        }
        if (memoTable == null) {
            memoTable = new MemoryTable();
        }
        if (memoryHeapDao == null) {
            memoryHeapDao = new MemoryHeapDao();
        }
        if (memoryInstanceDao == null) {
            memoryInstanceDao = new MemoryInstanceDao();
        }
        if (classInfoDao == null) {
            classInfoDao = new ClassInfoDao();
        }
        if (memoryInstanceDetailsDao == null) {
            memoryInstanceDetailsDao = new MemoryInstanceDetailsDao();
        }
        Object object = dataList.get(0);
        if (object instanceof ClassInfo) {
            classInfoDao.insertClassInfos(dataList);
        } else if (object instanceof MemoryHeapInfo) {
            memoryHeapDao.insertMemoryHeapInfos(dataList);
        } else if (object instanceof MemoryInstanceDetailsInfo) {
            memoryInstanceDetailsDao.insertMemoryInstanceDetailsInfo(dataList);
        } else if (object instanceof MemoryInstanceInfo) {
            memoryInstanceDao.insertMemoryInstanceInfos(dataList);
        } else if (object instanceof ProcessMemInfo) {
            memoTable.insertProcessMemInfo(dataList);
        } else {
            return;
        }
        dataList.clear();
    }

    /**
     * delete Session By OffLine Divece
     *
     * @param device device
     */
    public void deleteSessionByOffLineDivece(DeviceIPPortInfo device) {
        if (profilingSessions.isEmpty() || device == null) {
            return;
        }
        Set<Long> sessionIds = profilingSessions.keySet();
        for (Long session : sessionIds) {
            SessionInfo sessionInfo = profilingSessions.get(session);
            HiProfilerClient hiprofiler = HiProfilerClient.getInstance();
            DeviceIPPortInfo deviceSource = sessionInfo.getDeviceIPPortInfo();
            if (device.getDeviceID().equals(deviceSource.getDeviceID())) {
                // 停止chart刷新
                ProfilerChartsView.sessionMap.get(session).getObserver().stopRefresh(true);
                profilingSessions.remove(session);
            }
        }
    }

    /**
     * get Plugin Path
     *
     * @return String plugin Path
     */
    public String getPluginPath() {
        String pluginPath = "";
        if (developMode) {
            pluginPath = "C:\\ohos\\";
        } else {
            PluginId plugin = PluginManager.getPluginByClassName(this.getClass().getName());
            if (plugin != null) {
                File path = PluginManager.getPlugin(plugin).getPath();
                try {
                    pluginPath = path.getCanonicalPath() + "\\ohos\\";
                } catch (IOException ioException) {
                    LOGGER.error("ioException {}", ioException.getMessage());
                }
            }
        }
        LOGGER.debug("path is {}", pluginPath);
        return pluginPath;
    }

    public void setDevelopMode(boolean developMode) {
        this.developMode = developMode;
    }
}
