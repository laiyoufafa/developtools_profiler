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

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import io.grpc.StatusRuntimeException;
import ohos.devtools.datasources.databases.datatable.MemoryTable;
import ohos.devtools.datasources.databases.datatable.enties.ProcessCpuData;
import ohos.devtools.datasources.databases.datatable.enties.ProcessMemInfo;
import ohos.devtools.datasources.transport.grpc.HiProfilerClient;
import ohos.devtools.datasources.transport.grpc.ProfilerClient;
import ohos.devtools.datasources.transport.grpc.ProfilerServiceHelper;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.datasources.utils.common.Constant;
import ohos.devtools.datasources.utils.common.util.CommonUtil;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.common.util.PrintUtil;
import ohos.devtools.datasources.utils.common.util.Validate;
import ohos.devtools.datasources.utils.datahandler.datapoller.DataPoller;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceProcessInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.datasources.utils.device.entity.TraceFileInfo;
import ohos.devtools.datasources.utils.plugin.entity.HiProfilerPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.PluginBufferConfig;
import ohos.devtools.datasources.utils.plugin.entity.PluginConf;
import ohos.devtools.datasources.utils.plugin.entity.PluginMode;
import ohos.devtools.datasources.utils.plugin.service.PlugManager;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.datasources.utils.session.KeepSession;
import ohos.devtools.datasources.utils.session.entity.SessionInfo;
import ohos.devtools.services.cpu.CpuDao;
import ohos.devtools.services.cpu.CpuValidate;
import ohos.devtools.services.memory.MemoryValidate;
import ohos.devtools.services.memory.agentbean.ClassInfo;
import ohos.devtools.services.memory.agentbean.MemoryHeapInfo;
import ohos.devtools.services.memory.agentbean.MemoryInstanceDetailsInfo;
import ohos.devtools.services.memory.agentdao.ClassInfoDao;
import ohos.devtools.services.memory.agentdao.ClassInfoManager;
import ohos.devtools.services.memory.agentdao.MemoryHeapDao;
import ohos.devtools.services.memory.agentdao.MemoryHeapManager;
import ohos.devtools.services.memory.agentdao.MemoryInstanceDao;
import ohos.devtools.services.memory.agentdao.MemoryInstanceDetailsDao;
import ohos.devtools.services.memory.agentdao.MemoryInstanceDetailsManager;
import ohos.devtools.services.memory.agentdao.MemoryInstanceManager;
import ohos.devtools.services.memory.agentdao.MemoryUpdateInfo;
import ohos.devtools.services.memory.memoryservice.MemoryService;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.dialog.ExportFileChooserDialog;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.swing.JProgressBar;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_CHMOD_PROC;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_CHMOD_PROC;
import static ohos.devtools.datasources.transport.hdc.HdcWrapper.conversionCommand;
import static ohos.devtools.datasources.utils.common.Constant.DEVTOOLS_PLUGINS_V8_PATH;
import static ohos.devtools.datasources.utils.device.entity.DeviceType.LEAN_HOS_DEVICE;
import static ohos.devtools.datasources.utils.plugin.entity.PluginBufferConfig.Policy.RECYCLE;
import static ohos.devtools.views.common.Constant.IS_DEVELOP_MODE;
import static ohos.devtools.views.common.Constant.IS_SUPPORT_NEW_HDC;

/**
 * session Management core class
 */
public class SessionManager {
    /**
     * Global log
     */
    private static final Logger LOGGER = LogManager.getLogger(SessionManager.class);
    private static final int KEEP_SESSION_TIME = 3000;
    private static final int KEEP_SESSION_REQUEST_TIME = 2500;
    private static final String STD_DEVELOPTOOLS = "stddeveloptools";

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

    private Project project;

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
                profilerChartsView.getPublisher().stopRefresh(true);
            }
            SessionInfo sessionInfo = profilingSessions.get(localSessionId);
            if (Objects.nonNull(sessionInfo)) {
                String keepSessionName =
                    getKeepSessionName(sessionInfo.getDeviceIPPortInfo(), sessionInfo.getSessionId());
                QuartzManager.getInstance().deleteExecutor(keepSessionName);
            }
            profilingSessions.remove(localSessionId);
        }
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Create Session based on device information, process information, and specific scenarios
     *
     * @param device device
     * @param process process
     * @return Long sessionId
     */
    public Long createSession(DeviceIPPortInfo device, ProcessInfo process) {
        if (device == null || process == null || device.getForwardPort() == 0) {
            return Constant.ABNORMAL;
        }
        ProfilerServiceTypes.ProfilerSessionConfig.Builder sessionConfigBuilder = getSessionConfigBuilder();
        List<ProfilerServiceTypes.ProfilerPluginCapability> capability = getProfilerPluginCapabilities(device);
        if (capability == null || capability.size() == 0) {
            return Constant.ABNORMAL;
        }
        long localSessionID = CommonUtil.getLocalSessionId();
        List<CommonTypes.ProfilerPluginConfig> plugs = new ArrayList();
        List<PluginConf> configs = PlugManager.getInstance().getPluginConfig(device.getDeviceType(), PluginMode.ONLINE);
        for (PluginConf conf : configs) {
            if (conf.isAlwaysAdd()) {
                PlugManager.getInstance().addPluginStartSuccess(localSessionID, conf);
                continue;
            }
            if (conf.isOperationStart()) {
                continue;
            }
            if (conf.isSpecialStart()) {
                boolean startResult = handleSpecialStartPlug(conf, device, process, plugs, sessionConfigBuilder);
                if (startResult) {
                    PlugManager.getInstance().addPluginStartSuccess(localSessionID, conf);
                }
            } else {
                Optional<ProfilerServiceTypes.ProfilerPluginCapability> plug =
                    getLibPlugin(capability, conf.getPluginFileName());
                if (plug.isPresent()) {
                    ProfilerServiceTypes.ProfilerPluginCapability profilerPluginCapability = plug.get();
                    sessionConfigBuilder.addBuffers(getBufferConfig(conf));
                    HiProfilerPluginConfig hiPluginConfig =
                        conf.getICreatePluginConfig().createPluginConfig(device, process);
                    CommonTypes.ProfilerPluginConfig pluginConfig =
                        getProfilerPluginConfig(conf, profilerPluginCapability, hiPluginConfig, device);
                    plugs.add(pluginConfig);
                    PlugManager.getInstance().addPluginStartSuccess(localSessionID, conf);
                }
            }
        }
        ProfilerServiceTypes.CreateSessionResponse res = createSessionResponse(device, sessionConfigBuilder, plugs);
        if (res.getSessionId() > 0) {
            startKeepLiveSession(device, res.getSessionId(), localSessionID);
            profilingSessions.put(localSessionID, createSessionInfo(device, process, res.getSessionId()));
            PrintUtil.print(LOGGER, "Task with Session created successfully.", 0);
            return localSessionID;
        } else {
            LOGGER.error("Failed to create task with Session!");
            return Constant.ABNORMAL;
        }
    }

    private boolean handleSpecialStartPlug(PluginConf conf, DeviceIPPortInfo device,
        ProcessInfo process, List<CommonTypes.ProfilerPluginConfig> plugs,
        ProfilerServiceTypes.ProfilerSessionConfig.Builder sessionConfigBuilder) {
        boolean startResult = conf.getSpecialStartPlugMethod().specialStartPlugMethod(device, process);
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            LOGGER.error("sleep");
        }
        List<ProfilerServiceTypes.ProfilerPluginCapability> caps = getProfilerPluginCapabilities(device);
        Optional<ProfilerServiceTypes.ProfilerPluginCapability> plug =
            getLibPlugin(caps, conf.getGetPluginName().getPluginName(device, process));
        LOGGER.info("plug : {}", plug);
        if (startResult && plug.isPresent()) {
            ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig bufferConfig = getBufferConfig(conf);
            LOGGER.info("BufferConfig {}", bufferConfig);
            sessionConfigBuilder.addBuffers(bufferConfig);
            HiProfilerPluginConfig pluginConfig =
                conf.getICreatePluginConfig().createPluginConfig(device, process);
            CommonTypes.ProfilerPluginConfig profilerPluginConfig =
                getAgentProfilerPluginConfig(conf, plug.get(), pluginConfig);
            LOGGER.info("profilerPluginConfig : {}", profilerPluginConfig);
            plugs.add(profilerPluginConfig);
            return true;
        }
        return false;
    }

    private CommonTypes.ProfilerPluginConfig getProfilerPluginConfig(PluginConf conf,
        ProfilerServiceTypes.ProfilerPluginCapability plug, HiProfilerPluginConfig pluginConfig,
        DeviceIPPortInfo device) {
        LOGGER.info("pluginConfig is{}", pluginConfig);
        String pluginFileName = conf.getPluginFileName();
        String fileName = pluginFileName.substring(pluginFileName.lastIndexOf("/") + 1);
        StringBuilder stringBuilder = new StringBuilder(SessionManager.getInstance().getPluginPath());
        if (IS_SUPPORT_NEW_HDC && device.getDeviceType() == LEAN_HOS_DEVICE) {
            stringBuilder.append(STD_DEVELOPTOOLS).append(File.separator).append(fileName);
        } else {
            stringBuilder.append(DEVTOOLS_PLUGINS_V8_PATH).append(File.separator).append(fileName);
        }
        String filePath = stringBuilder.toString();
        File pluginFile = new File(filePath);
        try {
            String fileSha256 = DigestUtils.sha256Hex(new FileInputStream(pluginFile));
            LOGGER.error("plugin sha256Hex  {}", fileSha256);
            return ProfilerServiceHelper
                .profilerPluginConfig(plug.getName(), fileSha256, pluginConfig.getSampleInterval(),
                    pluginConfig.getConfData());
        } catch (IOException ioException) {
            LOGGER.error("plugin sha256Hex fail {}", fileName);
            return CommonTypes.ProfilerPluginConfig.getDefaultInstance();
        }
    }

    private CommonTypes.ProfilerPluginConfig getAgentProfilerPluginConfig(PluginConf conf,
        ProfilerServiceTypes.ProfilerPluginCapability plug, HiProfilerPluginConfig pluginConfig) {
        LOGGER.info("pluginConfig is{}", pluginConfig);
        String pluginFileName = conf.getPluginFileName();
        String fileName = pluginFileName.substring(pluginFileName.lastIndexOf("/") + 1);
        StringBuilder stringBuilder = new StringBuilder(SessionManager.getInstance().getPluginPath());
        stringBuilder.append(DEVTOOLS_PLUGINS_V8_PATH).append(File.separator).append(fileName).toString();
        String filePath = stringBuilder.toString();
        File pluginFile = new File(filePath);
        try {
            String fileSha256 = DigestUtils.sha256Hex(new FileInputStream(pluginFile));
            LOGGER.error("plugin sha256Hex  {}", fileSha256);
            return ProfilerServiceHelper
                .profilerPluginConfig(plug.getName(), "", pluginConfig.getSampleInterval(), pluginConfig.getConfData());
        } catch (IOException ioException) {
            LOGGER.error("plugin sha256Hex fail {}", fileName);
            return CommonTypes.ProfilerPluginConfig.getDefaultInstance();
        }
    }

    private ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig getBufferConfig(PluginConf conf) {
        PluginBufferConfig pluginBufferConfig = conf.getPluginBufferConfig();
        ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Builder builder =
            ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.newBuilder();
        if (pluginBufferConfig.getPolicy() == RECYCLE) {
            builder.setPolicy(ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE);
        } else {
            builder.setPolicy(ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.FLATTEN);
        }
        ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig bufferConfig =
            builder.setPages(pluginBufferConfig.getPages()).build();
        return bufferConfig;
    }

    private ProfilerServiceTypes.CreateSessionResponse createSessionResponse(DeviceIPPortInfo device,
        ProfilerServiceTypes.ProfilerSessionConfig.Builder sessionConfigBuilder,
        List<CommonTypes.ProfilerPluginConfig> plugs) {
        ProfilerServiceTypes.CreateSessionRequest request =
            ProfilerServiceHelper.createSessionRequest(CommonUtil.getRequestId(), sessionConfigBuilder.build(), plugs);
        ProfilerClient createSessionClient =
            HiProfilerClient.getInstance().getProfilerClient(device.getIp(), device.getForwardPort());
        ProfilerServiceTypes.CreateSessionResponse response = null;
        try {
            response = createSessionClient.createSession(request);
        } catch (StatusRuntimeException statusRuntimeException) {
            LOGGER.error("status RuntimeException getStatus:{}", statusRuntimeException.getStatus());
            return ProfilerServiceTypes.CreateSessionResponse.newBuilder().setSessionId(-1).build();
        }
        return response;
    }

    private List<ProfilerServiceTypes.ProfilerPluginCapability> getProfilerPluginCapabilities(DeviceIPPortInfo device) {
        ProfilerServiceTypes.GetCapabilitiesResponse capabilitiesRes =
            HiProfilerClient.getInstance().getCapabilities(device.getIp(), device.getForwardPort());
        return capabilitiesRes.getCapabilitiesList();
    }

    private ProfilerServiceTypes.ProfilerSessionConfig.Builder getSessionConfigBuilder() {
        ProfilerServiceTypes.ProfilerSessionConfig.Builder sessionConfigBuilder =
            ProfilerServiceTypes.ProfilerSessionConfig.newBuilder().setKeepAliveTime(KEEP_SESSION_TIME)
                .setSessionMode(ProfilerServiceTypes.ProfilerSessionConfig.Mode.ONLINE);
        return sessionConfigBuilder;
    }

    private void startKeepLiveSession(DeviceIPPortInfo deviceIPPortInfo, int sessionId, long localSessionId) {
        String keepSessionName = getKeepSessionName(deviceIPPortInfo, sessionId);
        QuartzManager.getInstance()
            .addExecutor(keepSessionName, new KeepSession(localSessionId, sessionId, deviceIPPortInfo));
        QuartzManager.getInstance().startExecutor(keepSessionName, 0, KEEP_SESSION_REQUEST_TIME);
    }

    /**
     * getKeepSessionName
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param sessionId sessionId
     * @return String
     */
    public String getKeepSessionName(DeviceIPPortInfo deviceIPPortInfo, int sessionId) {
        if (Objects.nonNull(deviceIPPortInfo)) {
            return "KEEP" + deviceIPPortInfo.getDeviceName() + sessionId;
        } else {
            return "";
        }
    }

    private SessionInfo createSessionInfo(DeviceIPPortInfo device, ProcessInfo process, int sessionId) {
        String deviceId = device.getDeviceID();
        String sessionName = CommonUtil.generateSessionName(deviceId, process.getProcessId());
        SessionInfo session =
            SessionInfo.builder().sessionId(sessionId).sessionName(sessionName).pid(process.getProcessId())
                .processName(process.getProcessName()).deviceIPPortInfo(device).build();
        return session;
    }

    private Optional<ProfilerServiceTypes.ProfilerPluginCapability> getLibPlugin(
        List<ProfilerServiceTypes.ProfilerPluginCapability> capabilities, String libDataPlugin) {
        Optional<ProfilerServiceTypes.ProfilerPluginCapability> ability = capabilities.stream()
            .filter(profilerPluginCapability -> profilerPluginCapability.getName().contains(libDataPlugin)).findFirst();
        return ability;
    }

    /**
     * Establish a session with the end side and start the session.
     *
     * @param localSessionId Local Session Id
     * @param restartFlag Whether to start again
     * @return boolean
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
        LOGGER.info("startSession sessionId {}", sessionId);
        DeviceIPPortInfo device = session.getDeviceIPPortInfo();
        return HiProfilerClient.getInstance().requestStartSession(device.getIp(), device.getForwardPort(), sessionId);
    }

    /**
     * Turn on polling to get data
     *
     * @param localSessionId localSessionId
     * @return boolean Turn on polling
     */
    public boolean fetchData(Long localSessionId) {
        if (localSessionId == null || localSessionId <= 0) {
            return false;
        }
        // Set permissions on the process that gets CPU data
        DeviceIPPortInfo deviceInfo = SessionManager.getInstance().getDeviceInfoBySessionId(localSessionId);
        ArrayList<String> cmdStr;
        if (IS_SUPPORT_NEW_HDC && deviceInfo.getDeviceType() == LEAN_HOS_DEVICE) {
            cmdStr = conversionCommand(HDC_STD_CHMOD_PROC, deviceInfo.getDeviceID());
            HdcWrapper.getInstance().execCmdBy(cmdStr, 10);
        }
        if ((!IS_SUPPORT_NEW_HDC) && deviceInfo.getDeviceType() == LEAN_HOS_DEVICE) {
            cmdStr = conversionCommand(HDC_CHMOD_PROC, deviceInfo.getDeviceID());
            HdcWrapper.getInstance().execCmdBy(cmdStr, 10);
        }
        try {
            if (localSessionId <= 0) {
                return false;
            }
            SessionInfo session = profilingSessions.get(localSessionId);
            if (session == null) {
                return true;
            }
            DeviceIPPortInfo device = session.getDeviceIPPortInfo();
            ProfilerClient client =
                HiProfilerClient.getInstance().getProfilerClient(device.getIp(), device.getForwardPort());
            LOGGER.info("start new DataPoller {}", DateTimeUtil.getNowTimeLong());
            int sessionId = session.getSessionId();
            DataPoller dataPoller = new DataPoller(localSessionId, sessionId, client);
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
    public SessionInfo getSessionInfo(Long localSessionId) {
        return profilingSessions.get(localSessionId);
    }

    /**
     * getDeviceInfoBySessionId
     *
     * @param localSessionId localSessionId
     * @return DeviceIPPortInfo
     */
    public DeviceIPPortInfo getDeviceInfoBySessionId(long localSessionId) {
        return profilingSessions.get(localSessionId).getDeviceIPPortInfo();
    }

    /**
     * View stop Loading
     *
     * @param localSession local Session
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
        LOGGER.info("endSession sessionId {}", sessionId);
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
            String keepSessionName = getKeepSessionName(session.getDeviceIPPortInfo(), session.getSessionId());
            QuartzManager.getInstance().deleteExecutor(keepSessionName);
            // Delete session information in local memory
            profilingSessions.remove(localSessionId);
            // Delete the data information related to the session of the database
            MemoryService.getInstance().deleteSessionData(localSessionId);
            deleteAllAgentData(localSessionId, true);
            if (session.isOfflineMode()) {
                return true;
            }
            int sessionId = session.getSessionId();
            LOGGER.info("deleteSession sessionId {}", sessionId);
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
                        DataPoller dataPoller = dataPollerHashMap.get(localSessionId);
                        if (dataPoller != null) {
                            dataPoller.shutDown();
                        }
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
            doDeleteSessionData(localSessionId);
        }
    }

    /**
     * doDeleteSessionData
     *
     * @param localSessionId localSessionId
     */
    private void doDeleteSessionData(Long localSessionId) {
        if (localSessionId != null && localSessionId > 0) {
            boolean traceFile = ProfilerChartsView.sessionMap.get(localSessionId).getPublisher().isTraceFile();
            if (!traceFile) {
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
            String keepSessionName = getKeepSessionName(sessionInfo.getDeviceIPPortInfo(), sessionInfo.getSessionId());
            QuartzManager.getInstance().deleteExecutor(keepSessionName);
            DeviceIPPortInfo device = sessionInfo.getDeviceIPPortInfo();
            if (device != null) {
                HiProfilerClient hiProfiler = HiProfilerClient.getInstance();
                hiProfiler
                    .requestStopSession(device.getIp(), device.getForwardPort(), sessionInfo.getSessionId(), true);
                hiProfiler.requestDestroySession(device.getIp(), device.getForwardPort(), sessionInfo.getSessionId());
            }
        });
    }

    /**
     * Save the collected data to a file.
     *
     * @param sessionId sessionId
     * @param deviceProcessInfo deviceProcessInfo
     * @param pathname pathname
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
        ArrayList<MemoryUpdateInfo> memoryInstanceInfos = new MemoryInstanceManager().getAllMemoryInstanceInfos();
        List<ProcessCpuData> cpuInfoList = CpuDao.getInstance().getAllData(sessionId);
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            File file = new File(pathname);
            fileOutputStream = new FileOutputStream(file);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            // Start importing the number of meminfo in an object record file
            TraceFileInfo startObj = new TraceFileInfo();
            int recordNum = memInfoList.size() + classInfos.size() + memoryHeapInfos.size() + detailsInfos.size()
                + memoryInstanceInfos.size() + cpuInfoList.size();
            startObj.setRecordNum(recordNum);
            startObj.setCreateTime(new Date().getTime());
            // Set the trace file version, the subsequent file save content format changes and is not compatible with
            // the previous file, you need to modify the version number, and you need to modify the version number
            // in the local Session Data From File method.
            startObj.setVersion("V1.0");
            objectOutputStream.writeObject(startObj);
            for (int index = 0; index < memInfoList.size(); index++) {
                setDeviceProcessInfo(deviceProcessInfo, memInfoList, objectOutputStream, index);
            }
            for (ProcessCpuData processCpuData : cpuInfoList) {
                objectOutputStream.writeObject(processCpuData);
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

    /**
     * setDeviceProcessInfo
     *
     * @param deviceProcessInfo deviceProcessInfo
     * @param memInfoList memInfoList
     * @param objectOutputStream objectOutputStream
     * @param index index
     * @throws IOException
     */
    private void setDeviceProcessInfo(DeviceProcessInfo deviceProcessInfo, List<ProcessMemInfo> memInfoList,
        ObjectOutputStream objectOutputStream, int index) throws IOException {
        ProcessMemInfo memObject = memInfoList.get(index);
        objectOutputStream.writeObject(memObject);
        if (index == 0) {
            deviceProcessInfo.setStartTime(memObject.getTimeStamp());
        }
        if (index == (memInfoList.size() - 1)) {
            deviceProcessInfo.setEndTime(memObject.getTimeStamp());
        }
    }

    private void writeCollectionData(ObjectOutputStream objectOutputStream, List<ClassInfo> classInfos,
        List<MemoryHeapInfo> memoryHeapInfos, List<MemoryInstanceDetailsInfo> detailsInfos,
        ArrayList<MemoryUpdateInfo> memoryInstanceInfos) throws IOException {
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
            MemoryUpdateInfo instanceInfo = memoryInstanceInfos.get(index);
            objectOutputStream.writeObject(instanceInfo);
        }
    }

    /**
     * local Session Data From File
     *
     * @param jProgressBar jProgressBar
     * @param file file
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
        long objNum = traceFileInfo.getRecordNum() + 1;
        long currentNum = 0;
        Validate[] validates = {new CpuValidate(), new MemoryValidate()};
        while (true) {
            Object object = objectInputStream.readObject();
            for (Validate item : validates) {
                if (item.validate(object)) {
                    currentNum = currentNum + 1;
                    loadPercentage(jProgressBar, objNum, currentNum);
                    item.addToList(object);
                    break;
                }
            }
            if (object instanceof DeviceProcessInfo) {
                // Finally, if there is still data in the datalist, import the database
                int processMemInfoNum = 0;
                for (Validate item : validates) {
                    if (item instanceof MemoryValidate) {
                        processMemInfoNum = ((MemoryValidate) item).getMenInfoSize();
                    }
                    item.batchInsertToDb();
                }
                currentNum = currentNum + processMemInfoNum;
                int progress = (int) (currentNum * LayoutConstants.HUNDRED / objNum);
                jProgressBar.setValue(progress);
                DeviceProcessInfo deviceProcessInfo = (DeviceProcessInfo) object;
                return deviceProcessInfo;
            } else {
                continue;
            }
        }
    }

    private void loadPercentage(JProgressBar jProgressBar, long objNum, long currentNum) {
        int progress = (int) (currentNum * LayoutConstants.HUNDRED / objNum);
        double result = progress % 25;
        if (result == 0) {
            jProgressBar.setValue(progress);
        }
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

    /**
     * delete Session By OffLine Device
     *
     * @param device device
     */
    public void deleteSessionByOffLineDevice(DeviceIPPortInfo device) {
        if (profilingSessions.isEmpty() || device == null) {
            return;
        }
        Set<Long> sessionIds = profilingSessions.keySet();
        for (Long session : sessionIds) {
            SessionInfo sessionInfo = profilingSessions.get(session);
            if (sessionInfo != null) {
                DeviceIPPortInfo deviceSource = sessionInfo.getDeviceIPPortInfo();
                if (device.getDeviceID().equals(deviceSource.getDeviceID())) {
                    String keepSessionName =
                        getKeepSessionName(sessionInfo.getDeviceIPPortInfo(), sessionInfo.getSessionId());
                    QuartzManager.getInstance().deleteExecutor(keepSessionName);
                    // 停止chart刷新
                    ProfilerChartsView.sessionMap.get(session).getPublisher().stopRefresh(true);
                    profilingSessions.remove(session);
                }
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
        if (IS_DEVELOP_MODE || developMode) {
            pluginPath = "C:\\ohos\\";
        } else {
            PluginId plugin = PluginManager.getPluginByClassName(this.getClass().getName());
            if (plugin != null) {
                File path = PluginManager.getPlugin(plugin).getPath();
                try {
                    pluginPath = path.getCanonicalPath() + File.separator + "ohos" + File.separator;
                } catch (IOException ioException) {
                    LOGGER.error("ioException ", ioException);
                }
            }
        }
        return pluginPath;
    }

    /**
     * get temp Path
     *
     * @return String temp Path
     */
    public String tempPath() {
        String pluginPath = "";
        if (IS_DEVELOP_MODE || developMode) {
            pluginPath = "C:\\ohos\\";
        } else {
            pluginPath = PathManager.getTempPath() + File.separator + "ohos" + File.separator;
        }
        return pluginPath;
    }

    /**
     * getHdcPath
     *
     * @return String
     */
    public String getHdcPath() {
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac")) {
            return getPluginPath() + "macHdc" + File.separator + "hdc";
        } else {
            return getPluginPath() + "winHdc" + File.separator + "hdc";
        }
    }

    /**
     * getHdcStdPath
     *
     * @return String
     */
    public String getHdcStdPath() {
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac")) {
            return getPluginPath() + "macHdc" + File.separator + "hdc_std";
        } else {
            return getPluginPath() + "winHdc" + File.separator + "hdc_std.exe";
        }
    }

    /**
     * get pid
     *
     * @param localSessionId localSessionId
     * @return long
     */
    public long getPid(Long localSessionId) {
        return profilingSessions.get(localSessionId).getPid();
    }

    /**
     * get ProcessName
     *
     * @param localSessionId localSessionId
     * @return long
     */
    public String getProcessName(Long localSessionId) {
        SessionInfo sessionInfo = profilingSessions.get(localSessionId);
        if (sessionInfo != null) {
            return sessionInfo.getProcessName();
        }
        return "";
    }

    public void setDevelopMode(boolean developMode) {
        this.developMode = developMode;
    }

    /**
     * export File
     *
     * @param exportFileName exportFileName
     * @param fileChooserDialog fileChooserDialog
     * @return boolean
     */
    public boolean exportDumpOrHookFile(String exportFileName, ExportFileChooserDialog fileChooserDialog) {
        // not get from device
        int line;
        boolean result = true;
        FileOutputStream fileOut = null;
        BufferedOutputStream dataOut = null;
        File file = new File(SessionManager.getInstance().tempPath() + File.separator + exportFileName);
        try {
            // Excuting an order
            fileOut = new FileOutputStream(
                fileChooserDialog.getExportFilePath() + File.separator + fileChooserDialog.getExportFileName()
                    + "." + fileChooserDialog.getFileType());
            dataOut = new BufferedOutputStream(fileOut);
            try (InputStream inputStream = new FileInputStream(file);
                BufferedInputStream brInputStream = new BufferedInputStream(inputStream);) {
                while ((line = brInputStream.read()) != -1) {
                    dataOut.write(line);
                }
            } catch (IOException exception) {
                LOGGER.error("exception {}", exception.getMessage());
                result = false;
            }
        } catch (IOException exception) {
            LOGGER.error("exception {}", exception.getMessage());
            result = false;
        } finally {
            try {
                dataOut.flush();
                if (fileOut != null) {
                    fileOut.close();
                }
            } catch (IOException exception) {
                LOGGER.error("exception {}", exception.getMessage());
                result = false;
            }
        }
        return result;
    }

    /**
     * getDeviceType By sessionId
     *
     * @param sessionId sessionId
     * @return DeviceType
     */
    public DeviceType getDeviceType(long sessionId) {
        SessionInfo sessionInfo = profilingSessions.get(sessionId);
        if (sessionInfo == null || sessionInfo.getDeviceIPPortInfo() == null) {
            return LEAN_HOS_DEVICE;
        }
        return sessionInfo.getDeviceIPPortInfo().getDeviceType();
    }
}
