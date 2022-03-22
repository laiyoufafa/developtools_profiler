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
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import io.grpc.StatusRuntimeException;
import ohos.devtools.datasources.databases.datatable.MemoryTable;
import ohos.devtools.datasources.databases.datatable.enties.DiskIOData;
import ohos.devtools.datasources.databases.datatable.enties.EnergyLocationInfo;
import ohos.devtools.datasources.databases.datatable.enties.ProcessCpuData;
import ohos.devtools.datasources.databases.datatable.enties.ProcessMemInfo;
import ohos.devtools.datasources.transport.grpc.HiProfilerClient;
import ohos.devtools.datasources.transport.grpc.ProfilerServiceHelper;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.datasources.utils.common.Constant;
import ohos.devtools.datasources.utils.common.util.CommonUtil;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.common.util.Validate;
import ohos.devtools.datasources.utils.datahandler.datapoller.DataPoller;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceProcessInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.datasources.utils.device.entity.TraceFileInfo;
import ohos.devtools.datasources.utils.plugin.entity.AnalysisType;
import ohos.devtools.datasources.utils.plugin.entity.HiProfilerPluginConfig;
import ohos.devtools.datasources.utils.plugin.entity.PluginBufferConfig;
import ohos.devtools.datasources.utils.plugin.entity.PluginConf;
import ohos.devtools.datasources.utils.plugin.entity.PluginMode;
import ohos.devtools.datasources.utils.plugin.service.PlugManager;
import ohos.devtools.datasources.utils.process.entity.ProcessInfo;
import ohos.devtools.datasources.utils.process.service.ProcessManager;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.datasources.utils.session.KeepSession;
import ohos.devtools.datasources.utils.session.entity.SessionInfo;
import ohos.devtools.services.cpu.CpuDao;
import ohos.devtools.services.cpu.CpuService;
import ohos.devtools.services.cpu.CpuValidate;
import ohos.devtools.services.diskio.DiskIoService;
import ohos.devtools.services.diskio.DiskIoValidate;
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
import ohos.devtools.services.userdata.UserDataService;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.dialog.ExportFileChooserDialog;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static ohos.devtools.datasources.transport.hdc.HdcCmdList.CHMOD_TO_OHOS;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_CHMOD_PROC;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_CHMOD_PROC;
import static ohos.devtools.datasources.transport.hdc.HdcWrapper.conversionCommand;
import static ohos.devtools.datasources.utils.common.Constant.DEVTOOLS_PLUGINS_FULL_PATH;
import static ohos.devtools.datasources.utils.device.entity.DeviceType.FULL_HOS_DEVICE;
import static ohos.devtools.datasources.utils.device.entity.DeviceType.LEAN_HOS_DEVICE;
import static ohos.devtools.datasources.utils.plugin.entity.PluginBufferConfig.Policy.RECYCLE;
import static ohos.devtools.views.common.Constant.IS_SUPPORT_NEW_HDC;

/**
 * session Management core class
 *
 * @since 2021/5/19 16:39
 */
public class SessionManager {
    private static final Logger LOGGER = LogManager.getLogger(SessionManager.class);
    private static final int KEEP_SESSION_TIME = 10000;
    private static final int KEEP_SESSION_REQUEST_TIME = 3000;
    private static final String STD_DEVELOPTOOLS = "stddeveloptools";
    private static final SessionManager SINGLETON = new SessionManager();

    private Project project;
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
     * getInstance
     *
     * @return SessionManager
     */
    public static SessionManager getInstance() {
        return SessionManager.SINGLETON;
    }

    /**
     * Clear session Id directly, use with caution
     *
     * @param localSessionId localSessionId
     */
    public void deleteLocalSession(Long localSessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deleteLocalSession");
        }
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
            DataPoller dataPoller = dataPollerHashMap.get(localSessionId);
            if (Objects.nonNull(dataPoller)) {
                dataPoller.shutDown();
            }
            removeLocalSessionData(localSessionId);
        }
    }

    private void removeLocalSessionData(long localSessionId) {
        CpuService.getInstance().deleteSessionData(localSessionId);
        MemoryService.getInstance().deleteSessionData(localSessionId);
        DiskIoService.getInstance().deleteSessionData(localSessionId);
        UserDataService.getInstance().deleteSessionData(localSessionId);
        deleteAllAgentData(localSessionId, true);
        profilingSessions.remove(localSessionId);
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
     * @param analysisType analysisType
     * @return Long sessionId
     */
    public Long createSession(DeviceIPPortInfo device, ProcessInfo process, AnalysisType analysisType) {
        if (device == null || process == null) {
            return Constant.ABNORMAL;
        }
        long localSessionID = CommonUtil.getLocalSessionId();
        if (analysisType == AnalysisType.GPU_CONFIG_TYPE) {
            return handleGpuConfig(device, process, analysisType, localSessionID);
        }
        List<ProfilerServiceTypes.ProfilerPluginCapability> capability = getProfilerPluginCapabilities(device);
        if (capability == null || capability.size() == 0) {
            return Constant.ABNORMAL;
        }
        ProfilerServiceTypes.ProfilerSessionConfig.Builder sessionConfigBuilder =
            getSessionConfigBuilder(device, process);
        List<CommonTypes.ProfilerPluginConfig> plugs = new ArrayList();
        List<PluginConf> configs =
            PlugManager.getInstance().getPluginConfig(device.getDeviceType(), PluginMode.ONLINE, analysisType);
        for (PluginConf conf : configs) {
            if (handlespecailconfig(localSessionID, conf)) {
                continue;
            }
            if (conf.isSpecialStart()) {
                boolean startResult = conf.getSpecialStartPlugMethod().specialStartPlugMethod(device, process);
                try {
                    TimeUnit.MILLISECONDS.sleep(3500);
                } catch (InterruptedException interruptedException) {
                    LOGGER.error("sleep");
                }
                List<ProfilerServiceTypes.ProfilerPluginCapability> caps = getProfilerPluginCapabilities(device);
                Optional<ProfilerServiceTypes.ProfilerPluginCapability> plug =
                    getLibPlugin(caps, conf.getGetPluginName().getPluginName(device, process));
                if (startResult && plug.isPresent()) {
                    sessionConfigBuilder.addBuffers(getBufferConfig(conf));
                    HiProfilerPluginConfig config = conf.getICreatePluginConfig().createPluginConfig(device, process);
                    plugs.add(getAgentPluginConfig(conf, plug.get(), config));
                    PlugManager.getInstance().addPluginStartSuccess(localSessionID, conf);
                }
            } else {
                Optional<ProfilerServiceTypes.ProfilerPluginCapability> plug =
                    getLibPlugin(capability, conf.getPluginFileName());
                if (plug.isPresent()) {
                    sessionConfigBuilder.addBuffers(getBufferConfig(conf));
                    CommonTypes.ProfilerPluginConfig pluginConfig = getProfilerPluginConfig(conf, plug.get(),
                        conf.getICreatePluginConfig().createPluginConfig(device, process), device);
                    plugs.add(pluginConfig);
                    PlugManager.getInstance().addPluginStartSuccess(localSessionID, conf);
                }
            }
        }
        return resultCreateSesion(device, process, localSessionID, sessionConfigBuilder, plugs);
    }

    private void getPluginCapability(DeviceIPPortInfo device, ProcessInfo process, PluginConf conf) {
        Optional<ProfilerServiceTypes.ProfilerPluginCapability> plug;
        do {
            List<ProfilerServiceTypes.ProfilerPluginCapability> caps =
                getProfilerPluginCapabilities(device);
            plug = getLibPlugin(caps, conf.getGetPluginName().getPluginName(device, process));
            try {
                TimeUnit.MILLISECONDS.sleep(1000);
            } catch (InterruptedException interruptedException) {
                LOGGER.error("sleep");
            }
        } while (!plug.isPresent());
    }

    private boolean handlespecailconfig(long localSessionID, PluginConf conf) {
        if (conf.isAlwaysAdd()) {
            PlugManager.getInstance().addPluginStartSuccess(localSessionID, conf);
            return true;
        }
        if (conf.isOperationStart()) {
            return true;
        }
        return false;
    }

    private Long resultCreateSesion(DeviceIPPortInfo device, ProcessInfo process, long localSessionID,
        ProfilerServiceTypes.ProfilerSessionConfig.Builder sessionConfigBuilder,
        List<CommonTypes.ProfilerPluginConfig> plugs) {
        if (!plugs.isEmpty()) {
            ProfilerServiceTypes.CreateSessionResponse res = createSessionResponse(device, sessionConfigBuilder, plugs);
            if (res.getSessionId() > 0) {
                startKeepLiveSession(device, res.getSessionId(), localSessionID);
                profilingSessions.put(localSessionID, createSessionInfo(device, process, res.getSessionId()));
                if (ProfilerLogManager.isInfoEnabled()) {
                    LOGGER.info("Task with Session created successfully");
                }
                return localSessionID;
            }
        }
        if (ProfilerLogManager.isErrorEnabled()) {
            LOGGER.error("Failed to create task with Session!");
        }
        return Constant.ABNORMAL;
    }

    private long handleGpuConfig(DeviceIPPortInfo device, ProcessInfo process, AnalysisType analysisType,
        long localSessionID) {
        List<PluginConf> configs =
            PlugManager.getInstance().getPluginConfig(device.getDeviceType(), PluginMode.ONLINE, analysisType);
        for (PluginConf conf : configs) {
            if (conf.isAlwaysAdd()) {
                PlugManager.getInstance().addPluginStartSuccess(localSessionID, conf);
                continue;
            }
        }
        profilingSessions.put(localSessionID, createSessionInfo(device, process, Integer.MAX_VALUE));
        return localSessionID;
    }

    private CommonTypes.ProfilerPluginConfig getProfilerPluginConfig(PluginConf conf,
        ProfilerServiceTypes.ProfilerPluginCapability plug, HiProfilerPluginConfig pluginConfig,
        DeviceIPPortInfo device) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getProfilerPluginConfig", pluginConfig);
        }
        String pluginFileName = conf.getPluginFileName();
        String fileName = pluginFileName.substring(pluginFileName.lastIndexOf("/") + 1);
        StringBuilder stringBuilder = new StringBuilder();
        if (IS_SUPPORT_NEW_HDC && device.getDeviceType() == LEAN_HOS_DEVICE) {
            stringBuilder.append(SessionManager.getInstance().tempPath()).append(STD_DEVELOPTOOLS)
                .append(File.separator).append(fileName);
        } else {
            stringBuilder.append(SessionManager.getInstance().getPluginPath()).append(DEVTOOLS_PLUGINS_FULL_PATH)
                .append(File.separator).append(fileName);
        }
        return ProfilerServiceHelper
            .profilerPluginConfig(plug.getName(), "", pluginConfig.getSampleInterval(),
                pluginConfig.getConfData());
    }

    private CommonTypes.ProfilerPluginConfig getAgentPluginConfig(PluginConf conf,
        ProfilerServiceTypes.ProfilerPluginCapability plug, HiProfilerPluginConfig pluginConfig) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getAgentProfilerPluginConfig", pluginConfig);
        }
        String pluginFileName = conf.getPluginFileName();
        String fileName = pluginFileName.substring(pluginFileName.lastIndexOf("/") + 1);
        StringBuilder stringBuilder = new StringBuilder(SessionManager.getInstance().getPluginPath());
        stringBuilder.append(DEVTOOLS_PLUGINS_FULL_PATH).append(File.separator).append(fileName).toString();
        return ProfilerServiceHelper
            .profilerPluginConfig(plug.getName(), "", pluginConfig.getSampleInterval(), pluginConfig.getConfData());
    }

    private ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig getBufferConfig(PluginConf conf) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getBufferConfig");
        }
        PluginBufferConfig pluginBufferConfig = conf.getPluginBufferConfig();
        ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Builder builder =
            ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.newBuilder();
        if (pluginBufferConfig.getPolicy() == RECYCLE) {
            builder.setPolicy(ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE);
        } else {
            builder.setPolicy(ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.FLATTEN);
        }
        return builder.setPages(pluginBufferConfig.getPages()).build();
    }

    private ProfilerServiceTypes.CreateSessionResponse createSessionResponse(DeviceIPPortInfo device,
        ProfilerServiceTypes.ProfilerSessionConfig.Builder sessionConfigBuilder,
        List<CommonTypes.ProfilerPluginConfig> plugs) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createSessionResponse");
        }
        ProfilerServiceTypes.CreateSessionRequest request =
            ProfilerServiceHelper.createSessionRequest(CommonUtil.getRequestId(), sessionConfigBuilder.build(), plugs);
        return HiProfilerClient.getInstance().requestCreateSession(device.getIp(), device.getForwardPort(), request);
    }

    private List<ProfilerServiceTypes.ProfilerPluginCapability> getProfilerPluginCapabilities(DeviceIPPortInfo device) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getProfilerPluginCapabilities");
        }
        if (device.getForwardPort() == 0) {
            ArrayList<ProfilerServiceTypes.ProfilerPluginCapability> capabilities =
                new ArrayList<ProfilerServiceTypes.ProfilerPluginCapability>();
            capabilities.add(ProfilerServiceTypes.ProfilerPluginCapability.newBuilder().build());
            return capabilities;
        }
        ProfilerServiceTypes.GetCapabilitiesResponse capabilitiesRes =
            HiProfilerClient.getInstance().getCapabilities(device.getIp(), device.getForwardPort());
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("capability {}", capabilitiesRes.getCapabilitiesList());
        }
        return capabilitiesRes.getCapabilitiesList();
    }

    private ProfilerServiceTypes.ProfilerSessionConfig.Builder getSessionConfigBuilder(DeviceIPPortInfo device,
        ProcessInfo process) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getSessionConfigBuilder");
        }
        if (device.getDeviceType() == FULL_HOS_DEVICE) {
            boolean isDebug = ProcessManager.getInstance().checkIsDebuggerProcess(device, process);
            if (isDebug) {
                return ProfilerServiceTypes.ProfilerSessionConfig.newBuilder().setKeepAliveTime(10000)
                    .setSessionMode(ProfilerServiceTypes.ProfilerSessionConfig.Mode.ONLINE);
            }
        }
        return ProfilerServiceTypes.ProfilerSessionConfig.newBuilder().setKeepAliveTime(KEEP_SESSION_TIME)
            .setSessionMode(ProfilerServiceTypes.ProfilerSessionConfig.Mode.ONLINE);
    }

    private void startKeepLiveSession(DeviceIPPortInfo deviceIPPortInfo, int sessionId, long localSessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("startKeepLiveSession");
        }
        String keepSessionName = getKeepSessionName(deviceIPPortInfo, sessionId);
        QuartzManager.getInstance()
            .addExecutor(keepSessionName, new KeepSession(localSessionId, sessionId, deviceIPPortInfo));
        QuartzManager.getInstance().startExecutor(keepSessionName, 0, KEEP_SESSION_REQUEST_TIME);
    }

    /**
     * createSessionOperationStart
     *
     * @param device device
     * @param process process
     * @param configName configName
     * @return long
     */
    public long createSessionOperationStart(DeviceIPPortInfo device, ProcessInfo process, String configName) {
        Optional<PluginConf> pluginConfigOpt = PlugManager.getInstance().getPluginConfigByName(configName);
        if (pluginConfigOpt.isPresent()) {
            PluginConf pluginConf = pluginConfigOpt.get();
            List<ProfilerServiceTypes.ProfilerPluginCapability> profilerPluginCapabilities =
                getProfilerPluginCapabilities(device);
            Optional<ProfilerServiceTypes.ProfilerPluginCapability> plug =
                getLibPlugin(profilerPluginCapabilities, pluginConf.getPluginDataName());
            if (plug.isPresent()) {
                ProfilerServiceTypes.ProfilerSessionConfig.Builder sessionConfigBuilder =
                    getSessionConfigBuilder(device, process);
                sessionConfigBuilder.addBuffers(getBufferConfig(pluginConf));
                CommonTypes.ProfilerPluginConfig pluginConfig = getProfilerPluginConfig(pluginConf, plug.get(),
                    pluginConf.getICreatePluginConfig().createPluginConfig(device, process), device);
                ArrayList<CommonTypes.ProfilerPluginConfig> profilerPluginConfigs = new ArrayList<>();
                profilerPluginConfigs.add(pluginConfig);
                long localSessionID = CommonUtil.getLocalSessionId();
                return resultCreateSesion(device, process, localSessionID, sessionConfigBuilder, profilerPluginConfigs);
            }
        }
        return Constant.ABNORMAL;
    }

    /**
     * stopAndDestoryOperation
     *
     * @param secondId secondId
     * @return boolean
     */
    public boolean stopAndDestoryOperation(long secondId) {
        if (secondId <= 0) {
            return false;
        }
        SessionInfo session = profilingSessions.get(secondId);
        if (session == null) {
            return false;
        }
        String keepSessionName = getKeepSessionName(session.getDeviceIPPortInfo(), session.getSessionId());
        QuartzManager.getInstance().deleteExecutor(keepSessionName);
        int sessionId = session.getSessionId();
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deleteSession sessionId {}", sessionId);
        }
        DeviceIPPortInfo device = session.getDeviceIPPortInfo();
        if (Objects.isNull(device)) {
            return false;
        }
        boolean stopSessionRes =
            HiProfilerClient.getInstance().requestStopSession(device.getIp(), device.getForwardPort(), sessionId, true);
        if (stopSessionRes) {
            boolean destroySessionRes = false;
            try {
                destroySessionRes = HiProfilerClient.getInstance()
                    .requestDestroySession(device.getIp(), device.getForwardPort(), sessionId);
            } catch (StatusRuntimeException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("StatusRuntimeException", exception);
                }
            } finally {
                profilingSessions.remove(secondId);
            }
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("Session deleted successfully.");
            }
            return destroySessionRes;
        } else {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("Failed to delete Session ");
            }
            return false;
        }
    }

    /**
     * getKeepSessionName
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param sessionId sessionId
     * @return String
     */
    public String getKeepSessionName(DeviceIPPortInfo deviceIPPortInfo, int sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getKeepSessionName");
        }
        if (Objects.nonNull(deviceIPPortInfo)) {
            return "KEEP" + deviceIPPortInfo.getDeviceName() + sessionId;
        } else {
            return "";
        }
    }

    private SessionInfo createSessionInfo(DeviceIPPortInfo device, ProcessInfo process, int sessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createSessionInfo");
        }
        String deviceId = device.getDeviceID();
        String sessionName = CommonUtil.generateSessionName(deviceId, process.getProcessId());
        return SessionInfo.builder().sessionId(sessionId).sessionName(sessionName).pid(process.getProcessId())
            .processName(process.getProcessName()).deviceIPPortInfo(device).processInfo(process).build();
    }

    private Optional<ProfilerServiceTypes.ProfilerPluginCapability> getLibPlugin(
        List<ProfilerServiceTypes.ProfilerPluginCapability> capabilities, String libDataPlugin) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getLibPlugin");
        }
        Optional<ProfilerServiceTypes.ProfilerPluginCapability> ability = capabilities.stream()
            .filter(profilerPluginCapability -> profilerPluginCapability.getName().contains(libDataPlugin)).findFirst();
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("plug : {}", ability);
        }
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("startSession");
        }
        if (localSessionId == null) {
            return false;
        }
        SessionInfo session = profilingSessions.get(localSessionId);
        if (session == null || session.getSessionId() == Integer.MAX_VALUE) {
            return true;
        }
        if (restartFlag) {
            // Click start, delete the previous data first
            CpuService.getInstance().deleteSessionData(localSessionId);
            MemoryService.getInstance().deleteSessionData(localSessionId);
            DiskIoService.getInstance().deleteSessionData(localSessionId);
            UserDataService.getInstance().deleteSessionData(localSessionId);
            deleteAllAgentData(localSessionId, false);
        }
        int sessionId = session.getSessionId();
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("startSession sessionId {}", sessionId);
        }
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("handle fetchData");
        }
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
            if (Objects.isNull(session)) {
                return true;
            }
            DeviceIPPortInfo device = session.getDeviceIPPortInfo();
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("start new DataPoller {}", DateTimeUtil.getNowTimeLong());
            }
            int sessionId = session.getSessionId();
            DataPoller dataPoller = new DataPoller(localSessionId, sessionId, device);
            if (sessionId != Integer.MAX_VALUE && sessionId > 0) {
                dataPoller.start();
            }
            dataPollerHashMap.put(localSessionId, dataPoller);
            return true;
        } catch (Exception exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("fetchData exception", exception);
            }
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getSessionInfo");
        }
        return profilingSessions.get(localSessionId);
    }

    /**
     * getDeviceInfoBySessionId
     *
     * @param localSessionId localSessionId
     * @return DeviceIPPortInfo
     */
    public DeviceIPPortInfo getDeviceInfoBySessionId(long localSessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getDeviceInfoBySessionId");
        }
        return profilingSessions.get(localSessionId).getDeviceIPPortInfo();
    }

    /**
     * View stop Loading
     *
     * @param localSession local Session
     * @param firstTimeStamp first Time Stamp
     */
    public void stopLoadingView(Long localSession, long firstTimeStamp) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("stopLoadingView");
        }
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("endSession");
        }
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
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("Task with Session stopped successfully.");
            }
        } else {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("Failed to stop task with Session!");
            }
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deleteSession");
        }
        try {
            if (localSessionId == null || localSessionId <= 0) {
                return false;
            }
            SessionInfo session = profilingSessions.get(localSessionId);
            if (session == null) {
                return false;
            }
            if (session.getSessionId() == Integer.MAX_VALUE) {
                return true;
            }
            String keepSessionName = getKeepSessionName(session.getDeviceIPPortInfo(), session.getSessionId());
            QuartzManager.getInstance().deleteExecutor(keepSessionName);
            removeLocalSessionData(localSessionId);
            if (session.isOfflineMode()) {
                return true;
            }
            int sessionId = session.getSessionId();
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("deleteSession sessionId {}", sessionId);
            }
            DeviceIPPortInfo device = session.getDeviceIPPortInfo();
            boolean stopSessionRes = HiProfilerClient.getInstance()
                .requestStopSession(device.getIp(), device.getForwardPort(), sessionId, true);
            if (stopSessionRes) {
                boolean destroySessionRes = false;
                try {
                    destroySessionRes = isDestroySessionRes(localSessionId, sessionId, device);
                } catch (StatusRuntimeException exception) {
                    if (ProfilerLogManager.isErrorEnabled()) {
                        LOGGER.error("exception ", exception);
                    }
                }
                if (ProfilerLogManager.isInfoEnabled()) {
                    LOGGER.info("Task with Session deleted successfully.");
                }
                return destroySessionRes;
            } else {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("Failed to delete task with Session ");
                }
                return false;
            }
        } finally {
            doDeleteSessionData(localSessionId);
        }
    }

    private boolean isDestroySessionRes(Long localSessionId, int sessionId, DeviceIPPortInfo device) {
        boolean destroySessionRes =
            HiProfilerClient.getInstance().requestDestroySession(device.getIp(), device.getForwardPort(), sessionId);
        if (destroySessionRes) {
            DataPoller dataPoller = dataPollerHashMap.get(localSessionId);
            if (dataPoller != null) {
                dataPoller.shutDown();
            }
        }
        return destroySessionRes;
    }

    /**
     * doDeleteSessionData
     *
     * @param localSessionId localSessionId
     */
    private void doDeleteSessionData(Long localSessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("doDeleteSessionData");
        }
        if (localSessionId != null && localSessionId > 0) {
            boolean traceFile = ProfilerChartsView.sessionMap.get(localSessionId).getPublisher().isTraceFile();
            if (!traceFile) {
                CpuService.getInstance().deleteSessionData(localSessionId);
                MemoryService.getInstance().deleteSessionData(localSessionId);
                DiskIoService.getInstance().deleteSessionData(localSessionId);
                UserDataService.getInstance().deleteSessionData(localSessionId);
                deleteAllAgentData(localSessionId, true);
            }
        }
    }

    private void deleteAllAgentData(Long localSessionId, boolean deleteClassInfo) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deleteAllAgentData");
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("stopAllSession");
        }
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("saveSessionDataToFile");
        }
        if (sessionId <= 0 || deviceProcessInfo == null || StringUtils.isEmpty(pathname)) {
            return false;
        }
        List<ProcessMemInfo> memInfoList = MemoryService.getInstance().getAllData(sessionId);
        List<ClassInfo> classInfos = new ClassInfoManager().getAllClassInfoData(sessionId);
        List<MemoryHeapInfo> memoryHeapInfos = new MemoryHeapManager().getAllMemoryHeapInfos(sessionId);
        List<MemoryInstanceDetailsInfo> detailsInfos = new MemoryInstanceDetailsManager().getAllMemoryInstanceDetails();
        ArrayList<MemoryUpdateInfo> memoryInstanceInfos = new MemoryInstanceManager().getAllMemoryInstanceInfos();
        List<DiskIOData> sysDiskIoInfoList = DiskIoService.getInstance().getAllData(sessionId);
        List<ProcessCpuData> cpuInfoList = CpuDao.getInstance().getAllData(sessionId);
        List<EnergyLocationInfo> energyList = new LinkedList<>();
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            File file = new File(pathname);
            fileOutputStream = new FileOutputStream(file);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            TraceFileInfo startObj = new TraceFileInfo();
            int recordNum = memInfoList.size() + classInfos.size() + memoryHeapInfos.size() + detailsInfos.size()
                + memoryInstanceInfos.size() + sysDiskIoInfoList.size() + cpuInfoList.size();
            startObj.setRecordNum(recordNum);
            startObj.setCreateTime(new Date().getTime());
            startObj.setVersion("V1.0");
            objectOutputStream.writeObject(startObj);
            for (int index = 0; index < memInfoList.size(); index++) {
                setDeviceProcessInfo(deviceProcessInfo, memInfoList, objectOutputStream, index);
            }
            handleStreamToDataBean(sysDiskIoInfoList, cpuInfoList, energyList, objectOutputStream);
            writeCollectionData(objectOutputStream, classInfos, memoryHeapInfos, detailsInfos, memoryInstanceInfos);
            objectOutputStream.writeObject(deviceProcessInfo);
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("Task with Session ID {} Save To File successfully.");
            }
        } catch (IOException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error(exception.getMessage());
            }
            return false;
        } finally {
            closeIoStream(null, null, fileOutputStream, objectOutputStream);
        }
        return true;
    }

    private void handleStreamToDataBean(List<DiskIOData> sysDiskIoInfoList,
        List<ProcessCpuData> cpuInfoList, List<EnergyLocationInfo> energyList, ObjectOutputStream objectOutputStream)
        throws IOException {
        for (DiskIOData sysDiskIoInfo : sysDiskIoInfoList) {
            objectOutputStream.writeObject(sysDiskIoInfo);
        }
        for (ProcessCpuData processCpuData : cpuInfoList) {
            objectOutputStream.writeObject(processCpuData);
        }
        for (EnergyLocationInfo energyLocationInfo : energyList) {
            objectOutputStream.writeObject(energyLocationInfo);
        }
    }

    /**
     * setDeviceProcessInfo
     *
     * @param deviceProcessInfo deviceProcessInfo
     * @param memInfoList memInfoList
     * @param objectOutputStream objectOutputStream
     * @param index index
     * @throws IOException IOException
     */
    private void setDeviceProcessInfo(DeviceProcessInfo deviceProcessInfo, List<ProcessMemInfo> memInfoList,
        ObjectOutputStream objectOutputStream, int index) throws IOException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("setDeviceProcessInfo");
        }
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("writeCollectionData");
        }
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("localSessionDataFromFile");
        }
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
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("load Data Error {}", exception.getMessage());
            }
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("loadFileInDataBase");
        }
        long objNum = traceFileInfo.getRecordNum() + 1;
        long currentNum = 0L;
        intDbDao();
        Validate[] validates = {new CpuValidate(), new MemoryValidate(), new DiskIoValidate()};
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
                return (DeviceProcessInfo) object;
            } else {
                continue;
            }
        }
    }

    private void intDbDao() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("intDbDao");
        }
    }

    private void loadPercentage(JProgressBar jProgressBar, long objNum, long currentNum) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("loadPercentage");
        }
        int progress = (int) (currentNum * LayoutConstants.HUNDRED / objNum);
        double result = progress % 25;
        if (result == 0) {
            jProgressBar.setValue(progress);
        }
    }

    private void closeIoStream(FileInputStream fileInputStream, ObjectInputStream objectInputStream,
        FileOutputStream fileOutputStream, ObjectOutputStream objectOutputStream) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("closeIoStream");
        }
        if (fileInputStream != null) {
            try {
                fileInputStream.close();
            } catch (IOException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("exception:{}", exception.getMessage());
                }
            }
        }
        if (objectInputStream != null) {
            try {
                objectInputStream.close();
            } catch (IOException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("exception:{}", exception.getMessage());
                }
            }
        }
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
            } catch (IOException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("exception:{}", exception.getMessage());
                }
            }
        }
        if (objectOutputStream != null) {
            try {
                objectOutputStream.close();
            } catch (IOException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("exception:{}", exception.getMessage());
                }
            }
        }
    }

    /**
     * delete Session By OffLine Device
     *
     * @param device device
     */
    public void deleteSessionByOffLineDevice(DeviceIPPortInfo device) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("deleteSessionByOffLineDevice");
        }
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
                    DataPoller dataPoller = dataPollerHashMap.get(session);
                    if (Objects.nonNull(dataPoller)) {
                        dataPoller.shutDown();
                    }
                    removeLocalSessionData(session);
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getPluginPath");
        }
        String pluginPath = "";
        PluginId plugin = PluginManager.getPluginByClassName(this.getClass().getName());
        if (plugin != null) {
            File path = PluginManager.getPlugin(plugin).getPath();
            try {
                pluginPath = path.getCanonicalPath() + File.separator + "ohos" + File.separator;
            } catch (IOException ioException) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("exception:{}", ioException.getMessage());
                }
            }
        } else {
            String sourcePath = SessionManager.class.getResource("").getPath();
            int indexOf = sourcePath.indexOf("/build");
            String substring = sourcePath.substring(1, indexOf);
            pluginPath = substring + "\\src\\main\\resources\\ohosresource\\";
        }
        return pluginPath;
    }

    /**
     * get temp Path
     *
     * @return String temp Path
     */
    public String tempPath() {
        return PathManager.getTempPath() + File.separator + "ohos" + File.separator;
    }

    /**
     * getHdcPath
     *
     * @return String
     */
    public String getHdcPath() {
        String value = "";
        PropertiesComponent instance = null;
        Application application = ApplicationManager.getApplication();
        if (Objects.nonNull(application)) {
            instance = application.getService(PropertiesComponent.class);
        }
        if (Objects.nonNull(instance)) {
            value = instance.getValue("huawei.sdk.location");
        }
        if (StringUtils.isBlank(value)) {
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("sdk path is null");
            }
            return "hdc";
        } else {
            return value + File.separator + "toolchains" + File.separator + "hdc";
        }
    }

    /**
     * getHdcStdPath
     *
     * @return String
     */
    public String getHdcStdPath() {
        String value = "";
        PropertiesComponent instance = null;
        Application application = ApplicationManager.getApplication();
        if (Objects.nonNull(application)) {
            instance = application.getService(PropertiesComponent.class);
        }
        if (Objects.nonNull(instance)) {
            value = instance.getValue("oh.sdk.location");
        }
        if (StringUtils.isBlank(value)) {
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("sdk path is null");
            }
            return "hdc_std";
        } else {
            return value + File.separator + "toolchains" + File.separator + "hdc_std";
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

    /**
     * export File
     *
     * @param exportFileName exportFileName
     * @param fileChooserDialog fileChooserDialog
     * @return boolean
     */
    public boolean exportDumpOrHookFile(String exportFileName, ExportFileChooserDialog fileChooserDialog) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("exportDumpOrHookFile");
        }
        // not get from device
        int line;
        boolean result = true;
        FileOutputStream fileOut = null;
        BufferedOutputStream dataOut = null;
        File file = new File(SessionManager.getInstance().tempPath() + File.separator + exportFileName);
        try {
            // Excuting an order
            fileOut = new FileOutputStream(
                fileChooserDialog.getExportFilePath() + File.separator + fileChooserDialog.getExportFileName() + "."
                    + fileChooserDialog.getFileType());
            dataOut = new BufferedOutputStream(fileOut);
            try (InputStream inputStream = new FileInputStream(file);
                BufferedInputStream brInputStream = new BufferedInputStream(inputStream);) {
                while ((line = brInputStream.read()) != -1) {
                    dataOut.write(line);
                }
            } catch (IOException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("exception {}", exception.getMessage());
                }
                result = false;
            }
        } catch (IOException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("exception {}", exception.getMessage());
            }
            result = false;
        } finally {
            try {
                dataOut.flush();
                if (dataOut != null) {
                    dataOut.close();
                }
                if (fileOut != null) {
                    fileOut.close();
                }
            } catch (IOException exception) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("exception {}", exception.getMessage());
                }
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getDeviceType");
        }
        SessionInfo sessionInfo = profilingSessions.get(sessionId);
        if (sessionInfo == null || sessionInfo.getDeviceIPPortInfo() == null) {
            return LEAN_HOS_DEVICE;
        }
        return sessionInfo.getDeviceIPPortInfo().getDeviceType();
    }

    /**
     * Setting Permissions
     */
    public void settingPermissions() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("settingPermissions");
        }
        if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("mac")) {
            String pluginPath = getPluginPath();
            HdcWrapper.getInstance().execCmdBy(conversionCommand(CHMOD_TO_OHOS, pluginPath));
        }
    }

    public HashMap<Long, SessionInfo> getProfilingSessions() {
        return profilingSessions;
    }

    public void setProfilingSessions(HashMap<Long, SessionInfo> profilingSessions) {
        this.profilingSessions = profilingSessions;
    }
}
