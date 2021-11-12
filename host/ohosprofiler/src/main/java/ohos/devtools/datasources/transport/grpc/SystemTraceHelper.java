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
import ohos.devtools.datasources.transport.grpc.service.HilogPluginConfig;
import ohos.devtools.datasources.transport.grpc.service.HiperfCallPluginConfigOuterClass;
import ohos.devtools.datasources.transport.grpc.service.MemoryPluginCommon;
import ohos.devtools.datasources.transport.grpc.service.MemoryPluginConfig;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.transport.grpc.service.TracePluginConfigOuterClass;
import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.datasources.utils.common.GrpcException;
import ohos.devtools.datasources.utils.common.util.BeanUtil;
import ohos.devtools.datasources.utils.common.util.CommonUtil;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.datasources.utils.session.KeepSession;
import ohos.devtools.datasources.utils.session.entity.SessionInfo;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.utils.TraceStreamerUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static ohos.devtools.datasources.transport.grpc.HiProfilerClient.getSTDSha256;
import static ohos.devtools.datasources.transport.grpc.HiProfilerClient.getSha256;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_CLEAR_COMMAND_CMD;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_PUSH_FILE_SHELL;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_PUSH_FILE_SHELL;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_ROOT_CLEAR_COMMAND;
import static ohos.devtools.datasources.transport.hdc.HdcWrapper.conversionCommand;
import static ohos.devtools.datasources.utils.device.entity.DeviceType.LEAN_HOS_DEVICE;
import static ohos.devtools.views.common.Constant.IS_SUPPORT_NEW_HDC;

/**
 * get trace data
 */
public class SystemTraceHelper {
    private static final Logger LOGGER = LogManager.getLogger(SystemTraceHelper.class);
    private static final int KEEP_SESSION_REQUEST_TIME = 2500;
    private static final String SYSTEM_PROBES_KIND_IN_MEMORY = "inMemoryValue";
    private static final String SYSTEM_PROBES_KIND_MAX_DURATION = "maxDuration";
    private static final String SYSTEM_PROBES_KIND_FTRACE_EVENT = "ftrace_events";
    private static final String FILE_PATH = "/data/local/tmp";
    private static final int SECOND_TO_MS = 1000;
    private static final int MEMORY_MB_TO_KB = 1024;
    private static ArrayList<String> MEM_INFO = new ArrayList<String>(Arrays.asList(
        "MEMINFO_ACTIVE",
        "MEMINFO_ACTIVE_ANON",
        "MEMINFO_ACTIVE_FILE",
        "MEMINFO_ANON_PAGES",
        "MEMINFO_BUFFERS",
        "MEMINFO_CACHED",
        "MEMINFO_CMA_FREE",
        "MEMINFO_CMA_TOTAL",
        "MEMINFO_COMMIT_LIMIT",
        "MEMINFO_COMMITED_AS",
        "MEMINFO_DIRTY",
        "MEMINFO_INACTIVE",
        "MEMINFO_INACTIVE_ANON",
        "MEMINFO_INACTIVE_FILE",
        "MEMINFO_KERNEL_STACK",
        "MEMINFO_MAPPED",
        "MEMINFO_MEM_AVAILABLE",
        "MEMINFO_MEM_FREE",
        "MEMINFO_MEM_TOTAL",
        "MEMINFO_MLOCKED",
        "MEMINFO_PAGE_TABLES",
        "MEMINFO_SHMEM",
        "MEMINFO_SLAB",
        "MEMINFO_SLAB_RECLAIMABLE",
        "MEMINFO_SLAB_UNRECLAIMABLE",
        "MEMINFO_SWAP_CACHED",
        "MEMINFO_SWAP_FREE",
        "MEMINFO_SWAP_TOTAL",
        "MEMINFO_UNEVICTABLE",
        "MEMINFO_VMALLOC_CHUNK",
        "MEMINFO_VMALLOC_TOTAL",
        "MEMINFO_VMALLOC_USED",
        "MEMINFO_WRITEBACK"));
    private static ArrayList<String> VMEM_INFO = new ArrayList<>(Arrays.asList(
        "VMEMINFO_UNSPECIFIED",
        "VMEMINFO_NR_FREE_PAGES",
        "VMEMINFO_NR_ALLOC_BATCH",
        "VMEMINFO_NR_INACTIVE_ANON",
        "VMEMINFO_NR_ACTIVE_ANON",
        "VMEMINFO_NR_INACTIVE_FILE",
        "VMEMINFO_NR_ACTIVE_FILE",
        "VMEMINFO_NR_UNEVICTABLE",
        "VMEMINFO_NR_MLOCK",
        "VMEMINFO_NR_ANON_PAGES",
        "VMEMINFO_NR_MAPPED",
        "VMEMINFO_NR_FILE_PAGES",
        "VMEMINFO_NR_DIRTY",
        "VMEMINFO_NR_WRITEBACK",
        "VMEMINFO_NR_SLAB_RECLAIMABLE",
        "VMEMINFO_NR_SLAB_UNRECLAIMABLE",
        "VMEMINFO_NR_PAGE_TABLE_PAGES",
        "VMEMINFO_NR_KERNEL_STACK",
        "VMEMINFO_NR_OVERHEAD",
        "VMEMINFO_NR_UNSTABLE",
        "VMEMINFO_NR_BOUNCE",
        "VMEMINFO_NR_VMSCAN_WRITE",
        "VMEMINFO_NR_VMSCAN_IMMEDIATE_RECLAIM",
        "VMEMINFO_NR_WRITEBACK_TEMP",
        "VMEMINFO_NR_ISOLATED_ANON",
        "VMEMINFO_NR_ISOLATED_FILE",
        "VMEMINFO_NR_SHMEM",
        "VMEMINFO_NR_DIRTIED",
        "VMEMINFO_NR_WRITTEN",
        "VMEMINFO_NR_PAGES_SCANNED",
        "VMEMINFO_WORKINGSET_REFAULT",
        "VMEMINFO_WORKINGSET_ACTIVATE",
        "VMEMINFO_WORKINGSET_NODERECLAIM",
        "VMEMINFO_NR_ANON_TRANSPARENT_HUGEPAGES",
        "VMEMINFO_NR_FREE_CMA",
        "VMEMINFO_NR_SWAPCACHE",
        "VMEMINFO_NR_DIRTY_THRESHOLD",
        "VMEMINFO_NR_DIRTY_BACKGROUND_THRESHOLD",
        "VMEMINFO_PGPGIN",
        "VMEMINFO_PGPGOUT",
        "VMEMINFO_PGPGOUTCLEAN",
        "VMEMINFO_PSWPIN",
        "VMEMINFO_PSWPOUT",
        "VMEMINFO_PGALLOC_DMA"));

    private static ArrayList<String> VMEM_INFO_SECOND = new ArrayList<String>(Arrays.asList(
        "VMEMINFO_PGALLOC_NORMAL",
        "VMEMINFO_PGALLOC_MOVABLE",
        "VMEMINFO_PGFREE",
        "VMEMINFO_PGACTIVATE",
        "VMEMINFO_PGDEACTIVATE",
        "VMEMINFO_PGFAULT",
        "VMEMINFO_PGMAJFAULT",
        "VMEMINFO_PGREFILL_DMA",
        "VMEMINFO_PGREFILL_NORMAL",
        "VMEMINFO_PGREFILL_MOVABLE",
        "VMEMINFO_PGSTEAL_KSWAPD_DMA",
        "VMEMINFO_PGSTEAL_KSWAPD_NORMAL",
        "VMEMINFO_PGSTEAL_KSWAPD_MOVABLE",
        "VMEMINFO_PGSTEAL_DIRECT_DMA",
        "VMEMINFO_PGSTEAL_DIRECT_NORMAL",
        "VMEMINFO_PGSTEAL_DIRECT_MOVABLE",
        "VMEMINFO_PGSCAN_KSWAPD_DMA",
        "VMEMINFO_PGSCAN_KSWAPD_NORMAL",
        "VMEMINFO_PGSCAN_KSWAPD_MOVABLE",
        "VMEMINFO_PGSCAN_DIRECT_DMA",
        "VMEMINFO_PGSCAN_DIRECT_NORMAL",
        "VMEMINFO_PGSCAN_DIRECT_MOVABLE",
        "VMEMINFO_PGSCAN_DIRECT_THROTTLE",
        "VMEMINFO_PGINODESTEAL",
        "VMEMINFO_SLABS_SCANNED",
        "VMEMINFO_KSWAPD_INODESTEAL",
        "VMEMINFO_KSWAPD_LOW_WMARK_HIT_QUICKLY",
        "VMEMINFO_KSWAPD_HIGH_WMARK_HIT_QUICKLY",
        "VMEMINFO_PAGEOUTRUN",
        "VMEMINFO_ALLOCSTALL",
        "VMEMINFO_PGROTATED",
        "VMEMINFO_DROP_PAGECACHE",
        "VMEMINFO_DROP_SLAB",
        "VMEMINFO_PGMIGRATE_SUCCESS",
        "VMEMINFO_PGMIGRATE_FAIL",
        "VMEMINFO_COMPACT_MIGRATE_SCANNED",
        "VMEMINFO_COMPACT_FREE_SCANNED",
        "VMEMINFO_COMPACT_ISOLATED",
        "VMEMINFO_COMPACT_STALL",
        "VMEMINFO_COMPACT_FAIL",
        "VMEMINFO_COMPACT_SUCCESS",
        "VMEMINFO_COMPACT_DAEMON_WAKE",
        "VMEMINFO_UNEVICTABLE_PGS_CULLED",
        "VMEMINFO_UNEVICTABLE_PGS_SCANNED",
        "VMEMINFO_UNEVICTABLE_PGS_RESCUED",
        "VMEMINFO_UNEVICTABLE_PGS_MLOCKED",
        "VMEMINFO_UNEVICTABLE_PGS_MUNLOCKED"));

    private static ArrayList<String> VMEM_INFO_THIRD = new ArrayList<String>(Arrays.asList(
        "VMEMINFO_UNEVICTABLE_PGS_CLEARED",
        "VMEMINFO_UNEVICTABLE_PGS_STRANDED",
        "VMEMINFO_NR_ZSPAGES",
        "VMEMINFO_NR_ION_HEAP",
        "VMEMINFO_NR_GPU_HEAP",
        "VMEMINFO_ALLOCSTALL_DMA",
        "VMEMINFO_ALLOCSTALL_MOVABLE",
        "VMEMINFO_ALLOCSTALL_NORMAL",
        "VMEMINFO_COMPACT_DAEMON_FREE_SCANNED",
        "VMEMINFO_COMPACT_DAEMON_MIGRATE_SCANNED",
        "VMEMINFO_NR_FASTRPC",
        "VMEMINFO_NR_INDIRECTLY_RECLAIMABLE",
        "VMEMINFO_NR_ION_HEAP_POOL",
        "VMEMINFO_NR_KERNEL_MISC_RECLAIMABLE",
        "VMEMINFO_NR_SHADOW_CALL_STACK_BYTES",
        "VMEMINFO_NR_SHMEM_HUGEPAGES",
        "VMEMINFO_NR_SHMEM_PMDMAPPED",
        "VMEMINFO_NR_UNRECLAIMABLE_PAGES",
        "VMEMINFO_NR_ZONE_ACTIVE_ANON",
        "VMEMINFO_NR_ZONE_ACTIVE_FILE",
        "VMEMINFO_NR_ZONE_INACTIVE_ANON",
        "VMEMINFO_NR_ZONE_INACTIVE_FILE",
        "VMEMINFO_NR_ZONE_UNEVICTABLE",
        "VMEMINFO_NR_ZONE_WRITE_PENDING",
        "VMEMINFO_OOM_KILL",
        "VMEMINFO_PGLAZYFREE",
        "VMEMINFO_PGLAZYFREED",
        "VMEMINFO_PGREFILL",
        "VMEMINFO_PGSCAN_DIRECT",
        "VMEMINFO_PGSCAN_KSWAPD",
        "VMEMINFO_PGSKIP_DMA",
        "VMEMINFO_PGSKIP_MOVABLE",
        "VMEMINFO_PGSKIP_NORMAL",
        "VMEMINFO_PGSTEAL_DIRECT",
        "VMEMINFO_PGSTEAL_KSWAPD",
        "VMEMINFO_SWAP_RA",
        "VMEMINFO_SWAP_RA_HIT",
        "VMEMINFO_WORKINGSET_RESTORE"
    ));

    /**
     * SystemTraceHelper
     */
    public SystemTraceHelper() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("SystemTraceHelper init");
        }
    }

    /**
     * createSession startSession
     *
     * @param deviceIPPortInfoParam deviceIPPortInfoParam
     * @param sessionConfigParam sessionConfigParam
     * @param configByteParam configByteParam
     * @param pluginNameParam pluginNameParam
     * @param otherConfigByteParam otherConfigByteParam
     * @return String
     */
    public String createAndStartSession(DeviceIPPortInfo deviceIPPortInfoParam,
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfigParam, byte[] configByteParam, String pluginNameParam,
        byte[] otherConfigByteParam) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createAndStartSession");
        }
        long localSessionID = CommonUtil.getLocalSessionId();
        CommonTypes.ProfilerPluginConfig plugConfig =
            plugConfig(deviceIPPortInfoParam, pluginNameParam, configByteParam);
        ProfilerServiceTypes.CreateSessionRequest request = null;
        if (otherConfigByteParam != null) {
            // memory
            String pluginMemoryName = null;
            Optional<String> optionalMem = getPluginName(deviceIPPortInfoParam, "libmem");
            if (optionalMem.isPresent()) {
                pluginMemoryName = optionalMem.get();
            } else {
                return "";
            }
            CommonTypes.ProfilerPluginConfig memoryPlugConfig = plugConfig(deviceIPPortInfoParam,
                pluginMemoryName, otherConfigByteParam);
            // hilog
            String pluginHilogName = null;
            Optional<String> optionalHilog = getPluginName(deviceIPPortInfoParam, "libhilog");
            if (optionalHilog.isPresent()) {
                pluginHilogName = optionalHilog.get();
            } else {
                return "";
            }
            CommonTypes.ProfilerPluginConfig hilogPlugConfig = plugConfig(deviceIPPortInfoParam,
                pluginHilogName, getHilogConfigByte(deviceIPPortInfoParam));
            // hilog
            request = ProfilerServiceTypes.CreateSessionRequest.newBuilder()
                .setRequestId(1)
                .setSessionConfig(sessionConfigParam)
                .addPluginConfigs(plugConfig)
                .addPluginConfigs(memoryPlugConfig)
                .addPluginConfigs(hilogPlugConfig)
                .build();
        } else {
            request = ProfilerServiceTypes.CreateSessionRequest.newBuilder()
                .setRequestId(CommonUtil.getRequestId())
                .setSessionConfig(sessionConfigParam)
                .addPluginConfigs(plugConfig)
                .build();
        }
        return createSessionResponse(deviceIPPortInfoParam, localSessionID, request);
    }

    private String createSessionResponse(DeviceIPPortInfo deviceIPPortInfoParam,
                                         long localSessionID, ProfilerServiceTypes.CreateSessionRequest request) {
        HiProfilerClient hiprofiler = HiProfilerClient.getInstance();
        ProfilerServiceTypes.CreateSessionResponse createSessionResponse =
                hiprofiler.requestCreateSession(deviceIPPortInfoParam.getIp(),
                        deviceIPPortInfoParam.getForwardPort(), request);
        if (createSessionResponse.getSessionId() > 0) {
            startKeepLiveSession(deviceIPPortInfoParam, createSessionResponse.getSessionId(), localSessionID);
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("Htrace Session created successfully.");
            }
        } else {
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("Failed to create Htrace Session");
            }
        }
        // 调用哪些进程（采集数据）
        hiprofiler.requestStartSession(deviceIPPortInfoParam.getIp(), deviceIPPortInfoParam.getForwardPort(),
                createSessionResponse.getSessionId());
        return String.valueOf(createSessionResponse.getSessionId());
    }
    
    /**
     * plugConfig
     *
     * @param deviceIPPortInfoParam deviceIPPortInfoParam
     * @param pluginName pluginName
     * @param configByteParam configByteParam
     * @return CommonTypes.ProfilerPluginConfi
     */
    public CommonTypes.ProfilerPluginConfig plugConfig(DeviceIPPortInfo deviceIPPortInfoParam, String pluginName,
        byte[] configByteParam) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("plugConfig");
        }
        String Sha256;
        if (IS_SUPPORT_NEW_HDC && deviceIPPortInfoParam.getDeviceType() == LEAN_HOS_DEVICE) {
            Sha256 = getSTDSha256(pluginName);
        } else {
            Sha256 = getSha256(pluginName);
        }
        return CommonTypes.ProfilerPluginConfig.newBuilder().setPluginSha256(Sha256)
            .setName(pluginName)
            .setSampleInterval(5000)
            .setConfigData(ByteString.copyFrom(configByteParam))
            .build();
    }

    /**
     * getHilogConfigByte
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @return byte[]
     */
    public byte[] getHilogConfigByte(DeviceIPPortInfo deviceIPPortInfo) {
        HilogPluginConfig.HilogConfig.Builder hilogBuilder = HilogPluginConfig.HilogConfig.newBuilder();
        if (deviceIPPortInfo.getDeviceType() == DeviceType.FULL_HOS_DEVICE) {
            hilogBuilder.setDeviceType(HilogPluginConfig.Type.P40);
        } else {
            hilogBuilder.setDeviceType(HilogPluginConfig.Type.HI3516);
        }
        hilogBuilder.setNeedClear(true);
        HilogPluginConfig.HilogConfig hilogConfig = hilogBuilder.build();
        return BeanUtil.serializeByCodedOutPutStream(hilogConfig);
    }

    /**
     * stopSession、destroySessionRequest
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param sessionIdParam sessionIdParam
     */
    public void stopAndDestroySession(DeviceIPPortInfo deviceIPPortInfo, String sessionIdParam) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("stopAndDestroySession");
        }
        int sessionId = Integer.valueOf(sessionIdParam);
        try {
            HiProfilerClient.getInstance()
                .requestStopSession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId, true);
            HiProfilerClient.getInstance()
                .requestDestroySession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId);
            String keepSessionName = getKeepSessionName(deviceIPPortInfo, Integer.valueOf(sessionIdParam));
            QuartzManager.getInstance().deleteExecutor(keepSessionName);
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("Htrace Session destroy successfully");
            }
        } catch (StatusRuntimeException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.info("destroy session Exception: {}", exception.getMessage());
            }
        }
    }

    /**
     * stopSession、destroySessionRequest
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param sessionIdParam sessionIdParam
     */
    public void stopSession(DeviceIPPortInfo deviceIPPortInfo, String sessionIdParam) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("stopSession");
        }
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("cancelActionDestroySession");
        }
        int sessionId = Integer.valueOf(sessionIdParam);
        HiProfilerClient hiprofiler = HiProfilerClient.getInstance();
        try {
            String keepSessionName = getKeepSessionName(deviceIPPortInfo, Integer.valueOf(sessionIdParam));
            QuartzManager.getInstance().deleteExecutor(keepSessionName);
            hiprofiler.requestDestroySession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId);
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("Htrace Session destroy successfully.");
            }
        } catch (StatusRuntimeException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("destroy session Exception: {}", exception.getMessage());
            }
        }
    }

    /**
     * request start session
     *
     * @param deviceIPPortInfo device IP Port Info
     * @param userCheckBoxForPerfettoStr userCheckBoxForPerfettoStr
     * @param maxDurationParam maxDurationParam
     * @param inMemoryValue inMemoryValue
     * @param outFileName outFileName
     * @param isRoot isRoot
     * @return String
     * @throws GrpcException GrpcException
     */
    public String createSessionByTraceRequest(DeviceIPPortInfo deviceIPPortInfo, String userCheckBoxForPerfettoStr,
        int maxDurationParam, int inMemoryValue, String outFileName, boolean isRoot) throws GrpcException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createSessionByTraceRequest");
        }
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
        build.setOutfileName(outFileName);
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
        return this.createAndStartSession(deviceIPPortInfo, sessionConfig, configByte, pluginName, null);
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
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createSessionByTraceRequestNoParam");
        }
        BytracePluginConfigOuterClass.BytracePluginConfig.Builder build =
            BytracePluginConfigOuterClass.BytracePluginConfig.newBuilder();
        String fileStorePath = "/data/local/tmp/hiprofiler_data";
        fileStorePath = fileStorePath.concat(fileSuffixTimestampParam).concat(".bytrace");
        if (deviceIPPortInfo.getDeviceType() == DeviceType.LEAN_HOS_DEVICE) {
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
        return this.createAndStartSession(deviceIPPortInfo, sessionConfig, configByte, pluginName, null);
    }

    /**
     * request start session
     *
     * @param deviceIPPortInfo device IP Port Info
     * @param fileSuffixTimestampParam file Suffix Timestamp Param
     * @param sessionId sessionId
     * @return String
     * @throws GrpcException GrpcException
     */
    public String createSessionThirdPerfRequest(DeviceIPPortInfo deviceIPPortInfo, String fileSuffixTimestampParam,
        long sessionId) throws GrpcException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createSessionThirdPerfRequest");
        }
        SessionInfo sessionInfo = SessionManager.getInstance().getSessionInfo(sessionId);
        String processName = sessionInfo.getProcessName();
        int pid = sessionInfo.getPid();
        HiperfCallPluginConfigOuterClass.HiperfCallPluginConfig.Builder build =
            HiperfCallPluginConfigOuterClass.HiperfCallPluginConfig.newBuilder();
        String fileStorePath = "/data/local/tmp/thirdPerf_data";
        fileStorePath = fileStorePath.concat(fileSuffixTimestampParam).concat(".trace");
        build.setPid(pid);
        build.setAppName(processName);
        build.setOutfile(fileStorePath);
        if (deviceIPPortInfo.getDeviceType() == DeviceType.LEAN_HOS_DEVICE) {
            build.setIsTrace(true);
            build.setIsRoot(true);
            build.setIsEmulator(false);
        } else {
            build.setIsTrace(false);
            build.setIsRoot(false);
            build.setIsEmulator(true);
        }
        HiperfCallPluginConfigOuterClass.HiperfCallPluginConfig config = build.build();
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
            capabilitiesList.stream().filter(item -> item.getName().contains("libhiperf")).findFirst().get();
        String pluginName = profilerPluginCapability.getName();
        return this.createAndStartSession(deviceIPPortInfo, sessionConfig, configByte, pluginName, null);
    }

    /**
     * 请求启动session
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param eventsList eventsList
     * @param fileSuffixTimestampParam fileSuffixTimestampParam
     * @return String
     * @throws GrpcException GrpcException
     */
    public String createSessionHtraceRequestForCpu(DeviceIPPortInfo deviceIPPortInfo,
        ArrayList<ArrayList<String>> eventsList, String fileSuffixTimestampParam) throws GrpcException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createSessionHtraceRequestForCpu");
        }
        String fileStorePath = "/data/local/tmp/hiprofiler_data";
        fileStorePath = fileStorePath.concat(fileSuffixTimestampParam).concat(".htrace");
        TracePluginConfigOuterClass.TracePluginConfig.Builder build =
            TracePluginConfigOuterClass
                .TracePluginConfig
                .newBuilder();
        if (eventsList != null && !eventsList.isEmpty()) {
            eventsList.forEach(events -> events.forEach(event -> {
                build.addFtraceEvents(event);
            }));
        }
        build.setClock("boot");
        build.setParseKsyms(true);
        build.setFlushIntervalMs(1000);
        build.setFlushThresholdKb(4096);
        TracePluginConfigOuterClass.TracePluginConfig config = build.build();
        byte[] configByte = BeanUtil.serializeByCodedOutPutStream(config);
        ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig bf =
            ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.newBuilder()
                .setPages(LayoutConstants.NUMBER_THREAD)
                .setPolicy(ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE)
                .build();
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfig =
            ProfilerServiceTypes.ProfilerSessionConfig.newBuilder()
                .setSessionMode(ProfilerServiceTypes.ProfilerSessionConfig.Mode.OFFLINE)
                .addBuffers(bf)
                .setResultFile(fileStorePath)
                .build();
        Optional<String> optional = getPluginName(deviceIPPortInfo, "libftrace");
        if (optional.isPresent()) {
            String pluginName = optional.get();
            return this.createAndStartSession(deviceIPPortInfo, sessionConfig, configByte, pluginName, null);
        } else {
            return "";
        }
    }

    /**
     * 请求启动session
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param eventsList eventsList
     * @param traceEventsList traceEventsList
     * @param maxDuration maxDuration
     * @param inMemoryValue inMemoryValue
     * @return String
     * @throws GrpcException GrpcException
     */
    public String createSessionHtraceRequest(DeviceIPPortInfo deviceIPPortInfo, ArrayList<ArrayList<String>> eventsList,
        ArrayList<ArrayList<String>> traceEventsList, int maxDuration, int inMemoryValue) throws GrpcException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createSessionHtraceRequest");
        }
        byte[] configByte = getHtraceConfigByte(eventsList, traceEventsList, inMemoryValue);
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfig = getHtraceConfig(maxDuration);
        byte[] memoryConfigByte = getMemoryConfigByte();
        Optional<String> optional = getPluginName(deviceIPPortInfo, "libftrace");
        if (optional.isPresent()) {
            String pluginName = optional.get();
            return this.createAndStartSession(deviceIPPortInfo, sessionConfig,
                configByte, pluginName, memoryConfigByte);
        } else {
            return "";
        }
    }

    /**
     * 获取htrace的配置信息
     *
     * @param eventsList eventsList
     * @param traceEventsList traceEventsList
     * @param inMemoryValue inMemoryValue
     * @return byte[]
     */
    public byte[] getHtraceConfigByte(ArrayList<ArrayList<String>> eventsList,
        ArrayList<ArrayList<String>> traceEventsList, int inMemoryValue) {
        TracePluginConfigOuterClass.TracePluginConfig.Builder build =
            TracePluginConfigOuterClass
                .TracePluginConfig
                .newBuilder();
        if (eventsList != null && !eventsList.isEmpty()) {
            eventsList.forEach(events -> events.forEach(event -> {
                build.addFtraceEvents(event);
            }));
        }
        if (traceEventsList != null && !traceEventsList.isEmpty()) {
            traceEventsList.forEach(events -> events.forEach(event -> {
                build.addBytraceCategories(event);
            }));
        }
        build.setClock("boot");
        build.setParseKsyms(true);
        build.setBufferSizeKb(inMemoryValue * MEMORY_MB_TO_KB);
        build.setFlushIntervalMs(1000);
        build.setFlushThresholdKb(4096);
        TracePluginConfigOuterClass.TracePluginConfig config = build.build();
        return BeanUtil.serializeByCodedOutPutStream(config);
    }

    /**
     * getHtraceConfig
     *
     * @param maxDuration maxDuration
     * @return ProfilerServiceTypes.ProfilerSessionConfig
     */
    public ProfilerServiceTypes.ProfilerSessionConfig getHtraceConfig(int maxDuration) {
        ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig bf =
            ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.newBuilder()
                .setPages(LayoutConstants.NUMBER_THREAD)
                .setPolicy(ProfilerServiceTypes.ProfilerSessionConfig.BufferConfig.Policy.RECYCLE)
                .build();
        return ProfilerServiceTypes.ProfilerSessionConfig.newBuilder()
            .setSampleDuration(maxDuration * SECOND_TO_MS)
            .setSessionMode(ProfilerServiceTypes.ProfilerSessionConfig.Mode.OFFLINE)
            .addBuffers(bf)
            .setResultFile("/data/local/tmp/hiprofiler_data.htrace")
            .build();
    }

    /**
     * 获取memory的配置信息
     *
     * @return byte[]
     */
    public byte[] getMemoryConfigByte() {
        MemoryPluginConfig.MemoryConfig.Builder memoryBuilder = MemoryPluginConfig.MemoryConfig.newBuilder();
        memoryBuilder.setReportProcessTree(true);
        memoryBuilder.setReportProcessMemInfo(true);
        memoryBuilder.setReportSysmemMemInfo(true);
        memoryBuilder.setReportSysmemVmemInfo(true);
        MEM_INFO.stream().forEach(str ->
            memoryBuilder.addSysMeminfoCounters(MemoryPluginCommon.SysMeminfoType.valueOf(str)));
        VMEM_INFO.stream().forEach(str ->
            memoryBuilder.addSysVmeminfoCounters(MemoryPluginCommon.SysVMeminfoType.valueOf(str)));
        VMEM_INFO_SECOND.stream().forEach(str ->
            memoryBuilder.addSysVmeminfoCounters(MemoryPluginCommon.SysVMeminfoType.valueOf(str)));
        VMEM_INFO_THIRD.stream().forEach(str ->
            memoryBuilder.addSysVmeminfoCounters(MemoryPluginCommon.SysVMeminfoType.valueOf(str)));
        MemoryPluginConfig.MemoryConfig memoryConfig = memoryBuilder.build();
        return BeanUtil.serializeByCodedOutPutStream(memoryConfig);
    }

    /**
     * 请求启动session
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param eventsList eventsList
     * @param traceEventsList traceEventsList
     * @param maxDuration maxDuration
     * @param inMemoryValue inMemoryValue
     * @return String
     * @throws GrpcException GrpcException
     */
    public String getHtraceTraceCommand(DeviceIPPortInfo deviceIPPortInfo, ArrayList<ArrayList<String>> eventsList,
        ArrayList<ArrayList<String>> traceEventsList, int maxDuration, int inMemoryValue) throws GrpcException {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getHtraceTraceCommand");
        }
        byte[] configByte = getHtraceConfigByte(eventsList, traceEventsList, inMemoryValue);
        ProfilerServiceTypes.ProfilerSessionConfig sessionConfig = getHtraceConfig(maxDuration);
        byte[] memoryConfigByte = getMemoryConfigByte();
        String pluginFtraceName = null;
        Optional<String> optionalFtrace = getPluginName(deviceIPPortInfo, "libftrace");
        if (optionalFtrace.isPresent()) {
            pluginFtraceName = optionalFtrace.get();
        } else {
            return "";
        }
        CommonTypes.ProfilerPluginConfig plugConfig =
            CommonTypes.ProfilerPluginConfig.newBuilder()
                .setPluginSha256(getFtraceSha(deviceIPPortInfo, pluginFtraceName))
                .setName(pluginFtraceName)
                .setConfigData(ByteString.copyFrom(configByte))
                .build();
        String pluginMemoryName = null;
        Optional<String> optionalMemory = getPluginName(deviceIPPortInfo, "libmem");
        if (optionalMemory.isPresent()) {
            pluginMemoryName = optionalMemory.get();
        } else {
            return "";
        }
        CommonTypes.ProfilerPluginConfig memoryPlugConfig =
            CommonTypes.ProfilerPluginConfig.newBuilder()
                .setPluginSha256(getMemSha(deviceIPPortInfo, pluginMemoryName))
                .setName(pluginMemoryName)
                .setSampleInterval(5000)
                .setConfigData(ByteString.copyFrom(memoryConfigByte))
                .build();
        ProfilerServiceTypes.CreateSessionRequest request =
            ProfilerServiceTypes.CreateSessionRequest.newBuilder()
                .setRequestId(1)
                .setSessionConfig(sessionConfig)
                .addPluginConfigs(plugConfig)
                .addPluginConfigs(memoryPlugConfig)
                .build();
        return request.toString();
    }

    /**
     * getFtraceSha
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param pluginFtraceName pluginFtraceName
     * @return String
     */
    public String getFtraceSha(DeviceIPPortInfo deviceIPPortInfo, String pluginFtraceName) {
        String sha256;
        if (IS_SUPPORT_NEW_HDC && deviceIPPortInfo.getDeviceType() == LEAN_HOS_DEVICE) {
            sha256 = getSTDSha256(pluginFtraceName);
        } else {
            sha256 = getSha256(pluginFtraceName);
        }
        return sha256;
    }

    /**
     * getMemSha
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param pluginMemoryName pluginMemoryName
     * @return String
     */
    public String getMemSha(DeviceIPPortInfo deviceIPPortInfo, String pluginMemoryName) {
        String memSha256;
        if (IS_SUPPORT_NEW_HDC && deviceIPPortInfo.getDeviceType() == LEAN_HOS_DEVICE) {
            memSha256 = getSTDSha256(pluginMemoryName);
        } else {
            memSha256 = getSha256(pluginMemoryName);
        }
        return memSha256;
    }

    /**
     * push htrace command file to device
     *
     * @param commandStr commandStr
     * @param timeStamp timeStamp
     * @param deviceIPPortInfo deviceIPPortInfo
     * @return String
     */
    public String pushHtraceCommandFile(String commandStr, String timeStamp, DeviceIPPortInfo deviceIPPortInfo) {
        String baseDir = TraceStreamerUtils.getInstance().getCreateFileDir();
        String commandFileName = "command".concat(timeStamp).concat(".txt");
        String commandFileStorePath = baseDir.concat(commandFileName);
        File txt = new File(commandFileStorePath);
        try {
            if (!txt.exists()) {
                txt.createNewFile();
            }
            byte[] bytes = commandStr.getBytes();
            FileOutputStream fos = new FileOutputStream(txt);
            fos.write(bytes, 0, bytes.length);
            fos.close();
        } catch (IOException io) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("pushHtraceCommandFile fail:", io.getMessage());
            }
        }
        ArrayList pushCmd;
        ArrayList clearCommand;
        if (IS_SUPPORT_NEW_HDC && deviceIPPortInfo.getDeviceType() == LEAN_HOS_DEVICE) {
            clearCommand = conversionCommand(HDC_STD_ROOT_CLEAR_COMMAND, deviceIPPortInfo.getDeviceID());
            pushCmd = conversionCommand(HDC_STD_PUSH_FILE_SHELL, deviceIPPortInfo.getDeviceID(),
                commandFileStorePath, FILE_PATH);
        } else {
            clearCommand = conversionCommand(HDC_CLEAR_COMMAND_CMD, deviceIPPortInfo.getDeviceID());
            pushCmd = conversionCommand(HDC_PUSH_FILE_SHELL, deviceIPPortInfo.getDeviceID(),
                commandFileStorePath, FILE_PATH);
        }
        HdcWrapper.getInstance().execCmdBy(clearCommand);
        HdcWrapper.getInstance().execCmdBy(pushCmd);
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("pushHtraceCommandFile success", pushCmd);
        }
        return FILE_PATH.concat("/").concat(commandFileName);
    }

    /**
     * 获取插件名称
     *
     * @param deviceIPPortInfo deviceIPPortInfo
     * @param PluginName PluginName
     * @return Optional
     */
    public Optional getPluginName(DeviceIPPortInfo deviceIPPortInfo, String PluginName) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("getPluginName");
        }
        ProfilerServiceTypes.GetCapabilitiesResponse response =
            HiProfilerClient.getInstance().getCapabilities(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
        List<ProfilerServiceTypes.ProfilerPluginCapability> capabilitiesList = response.getCapabilitiesList();
        if (capabilitiesList.isEmpty()) {
            return Optional.empty();
        }
        if (Optional.ofNullable(capabilitiesList).isPresent()) {
            List<ProfilerServiceTypes.ProfilerPluginCapability> capabilitiesLists =
                capabilitiesList.stream()
                    .filter(item -> item.getName().contains(PluginName)).collect(Collectors.toList());
            if (capabilitiesLists.size() > 0) {
                ProfilerServiceTypes.ProfilerPluginCapability profilerPluginCapability = capabilitiesLists.get(0);
                return Optional.of(profilerPluginCapability.getName());
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
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
}
