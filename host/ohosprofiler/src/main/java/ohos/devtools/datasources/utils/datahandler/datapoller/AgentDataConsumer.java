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

package ohos.devtools.datasources.utils.datahandler.datapoller;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import ohos.devtools.datasources.databases.datatable.enties.EnergyData;
import ohos.devtools.datasources.databases.datatable.enties.EnergyLocationInfo;
import ohos.devtools.datasources.transport.grpc.service.AgentPluginEnergyData;
import ohos.devtools.datasources.transport.grpc.service.AgentPluginJavaHeap;
import ohos.devtools.datasources.transport.grpc.service.AgentPluginResult;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import ohos.devtools.services.memory.agentbean.ClassInfo;
import ohos.devtools.services.memory.agentbean.MemoryHeapInfo;
import ohos.devtools.services.memory.agentbean.MemoryInstanceDetailsInfo;
import ohos.devtools.services.memory.agentdao.ClassInfoDao;
import ohos.devtools.services.memory.agentdao.MemoryHeapDao;
import ohos.devtools.services.memory.agentdao.MemoryInstanceDao;
import ohos.devtools.services.memory.agentdao.MemoryInstanceDetailsDao;
import ohos.devtools.services.memory.agentdao.MemoryUpdateInfo;
import ohos.devtools.views.charts.model.ChartDataModel;
import ohos.devtools.views.layout.chartview.MonitorItemDetail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * AgentDataConsumer
 */
public class AgentDataConsumer extends AbsDataConsumer {
    private static final Logger LOGGER = LogManager.getLogger(AgentDataConsumer.class);
    private static final String RUNNING_LOCK_REQUIRED_TYPE = "Acquired";
    private static final String RUNNING_LOCK_END_TYPE = "Release";
    private static final String ONE_SHOT_REQUIRED_TYPE = "Set";
    private static final String ONE_SHOT_EMD_TYPE = "Cancelled";
    private static final String LOCATION_REQUIRED_TYPE = "Request";
    private static final String LOCATION_REQUIRED_DEC = "N/A";
    private static final String LOCATION_REQUIRED_TYPE_SUFFIX = ":Request";
    private static final String LOCATION_END_TYPE = "Move";
    private static final String WORK_REQUIRED_TYPE = "Start";
    private static final String WORK_END_TYPE = "Stop";
    private static final String HAS_WORK_UPDATE = "true";
    private static final String NO_WORK_UPDATE = "false";
    private static final String WORK_NETWORK_TYPE = "NetworkType: ";
    private static final String WORK_CHARGING_TYPE = "Charging: ";
    private static final String WORK_STORAGE_TYPE = "Storage: ";
    private static final String WORK_DEEP_IDLE_TYPE = "Deep Idle: ";
    private static final String WORK_BATTERY_TYPE = "Battery: ";
    private static final String WORK_PERSISTED_TYPE = "Persisted";
    private static final String WORK_REPEAT_COUNTER_TYPE = "RepeatCounter: ";
    private static final String WORK_REPEAT_CYCLE_TYPE = "RepeatCycleTime: ";
    private static final String WORK_DELAY_TYPE = "Delay";
    private static final String SPECIAL_SYMBOL_COLON = ":";
    private static final String SPECIAL_SYMBOL_POINT = ".";
    private static final int END_STATUS = 6;
    private static final int BACKGROUND_STATUS = 4;
    private static final int MAX_SIZE = 2000;
    private static final int NS_CONVERT_MS = 1000000;

    private int energyUsage;
    private int energyRegisterUsage;
    private Boolean isStart;
    private long eventId;
    private String systemEvent;
    private String description;
    private String priority;
    private long minInterval;
    private long festInterval;
    private long triggertimeNs;
    private String startType;
    private String endType;
    private String callStack;
    private String processName;
    private long agentStartTimeStamp;
    private boolean agentFirstStartTimeStamp = true;
    private Queue<CommonTypes.ProfilerPluginData> queue;
    private long localSessionId;
    private ClassInfoDao classInfoDao;
    private MemoryInstanceDetailsDao memoryInstanceDetailsDao;
    private MemoryInstanceDao memoryInstanceDao;
    private MemoryHeapDao memoryHeapDao;
    private boolean stopFlag = false;
    private List<ClassInfo> classInfoList = new ArrayList<>();
    private List<MemoryInstanceDetailsInfo> memoryInstanceDetailsInfos = new ArrayList<>();
    private List<MemoryHeapInfo> memoryHeapInfos = new ArrayList<>();
    private List<MemoryUpdateInfo> memoryUpdateInfos = new ArrayList<>();
    private List<MemoryUpdateInfo> memoryUpdates = new ArrayList<>();

    /**
     * MemoryHeapDataConsumer
     */
    public AgentDataConsumer() {
    }

    @Override
    public void init(Queue queue, int sessionId, long localSessionId) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("init");
        }
        this.queue = queue;
        this.processName =
            SessionManager.getInstance().getSessionInfo(localSessionId).getProcessInfo().getProcessName();
        this.localSessionId = localSessionId;
        this.classInfoDao = new ClassInfoDao();
        this.memoryInstanceDetailsDao = new MemoryInstanceDetailsDao();
        this.memoryInstanceDao = new MemoryInstanceDao();
        this.memoryHeapDao = new MemoryHeapDao();
    }

    /**
     * run
     */
    @Override
    public void run() {
        while (!stopFlag) {
            CommonTypes.ProfilerPluginData dataObject = queue.poll();
            if (dataObject != null) {
                handleMemoryHeapHandle(dataObject);
            } else {
                insertDataOrUpdate(true);
                try {
                    TimeUnit.MILLISECONDS.sleep(20);
                } catch (InterruptedException exception) {
                    if (ProfilerLogManager.isErrorEnabled()) {
                        LOGGER.error(exception.getMessage());
                    }
                }
            }
        }
    }

    private void insertDataOrUpdate(boolean isInsert) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("insertDataOrUpdate");
        }
        if (isNeedInsert() || isInsert) {
            boolean insertRes = classInfoDao.insertClassInfos(classInfoList);
            if (insertRes) {
                classInfoList.clear();
            }
            boolean memHeapInfoRes = memoryHeapDao.insertMemoryHeapInfos(memoryHeapInfos);
            if (memHeapInfoRes) {
                memoryHeapInfos.clear();
            }
            boolean instanceRes = memoryInstanceDetailsDao.insertMemoryInstanceDetailsInfo(memoryInstanceDetailsInfos);
            if (instanceRes) {
                memoryInstanceDetailsInfos.clear();
            }
            boolean insertSuccess = memoryInstanceDao.insertMemoryInstanceInfos(memoryUpdateInfos);
            if (insertSuccess) {
                memoryUpdateInfos.clear();
            }
        }
    }

    private boolean isNeedInsert() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("isNeedInsert");
        }
        return classInfoList.size() >= MAX_SIZE || memoryHeapInfos.size() >= MAX_SIZE
            || memoryInstanceDetailsInfos.size() >= MAX_SIZE || memoryUpdates.size() >= MAX_SIZE;
    }

    /**
     * shutDown
     */
    public void shutDown() {
        stopFlag = true;
    }

    private void handleMemoryHeapHandle(CommonTypes.ProfilerPluginData memoryData) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("handleMemoryHeapHandle");
        }
        if (agentFirstStartTimeStamp) {
            agentStartTimeStamp = DateTimeUtil.getNowTimeLong();
            agentFirstStartTimeStamp = false;
        }
        ByteString data = memoryData.getData();
        AgentPluginResult.AgentData.Builder agentDataBuilder = AgentPluginResult.AgentData.newBuilder();
        AgentPluginResult.AgentData agentData = null;
        try {
            agentData = agentDataBuilder.mergeFrom(data).build();
        } catch (InvalidProtocolBufferException invalidProtocolBufferException) {
            LOGGER.error("mergeFrom Data failed ", invalidProtocolBufferException);
            return;
        }
        if (agentData.hasJavaheapData()) {
            AgentPluginJavaHeap.BatchAgentMemoryEvent javaHeapData = agentData.getJavaheapData();
            handleJavaHeapData(javaHeapData);
        }
    }

    /**
     * energyLocationRegister
     *
     * @param timestamp timestamp
     * @param event event
     * @param priority priority
     * @param energyUsage energyUsage
     */
    public void energyLocationRegister(long timestamp, long event, String priority, int energyUsage) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("energyLocationRegister");
        }
        EnergyLocationInfo energyLocationInfo = new EnergyLocationInfo();
        energyLocationInfo.setSessionId(localSessionId);
        energyLocationInfo.setEventId(event);
        energyLocationInfo.setTimestamp(timestamp);
        energyLocationInfo.setEnergyUsage(energyUsage);
        energyLocationInfo.setPriority(priority);
        ArrayList<EnergyLocationInfo> energyLocationInfos = new ArrayList<>();
        energyLocationInfos.add(energyLocationInfo);
    }

    /**
     * handleWorkData
     *
     * @param workInfo workInfo
     * @param energyData energyData
     */
    public void handleEnergyWorkData(AgentPluginEnergyData.WorkInfo workInfo, EnergyData energyData) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("handleEnergyWorkData");
        }
        if (workInfo.getIsRequestNetwork()) {
            energyData.setWorkNetworkType(WORK_NETWORK_TYPE.concat(workInfo.getNetworkType().name()));
        }
        if (workInfo.getIsRequestCharging()) {
            energyData.setWorkCharging(WORK_CHARGING_TYPE.concat(workInfo.getChargeType().name()));
        }
        if (workInfo.getIsRequestStorage()) {
            energyData.setWorkStorage(WORK_STORAGE_TYPE.concat(workInfo.getStorageType().name()));
        }
        if (workInfo.getIsRequestDeepIdle()) {
            energyData.setWorkDeepIdle(WORK_DEEP_IDLE_TYPE.concat(String.valueOf(workInfo.getWaitTime())));
        }
        if (workInfo.getIsRequestBattery()) {
            energyData.setWorkBattery(WORK_BATTERY_TYPE.concat(workInfo.getBatteryLevel().name()).concat(" ")
                .concat(String.valueOf(workInfo.getBatteryStatus())));
        }
        if (workInfo.getIsRequestPersisted()) {
            energyData.setWorkPersisted(WORK_PERSISTED_TYPE);
        }
        if (workInfo.getIsRequestRepeat()) {
            energyData
                .setWorkRepeatCounter(WORK_REPEAT_COUNTER_TYPE.concat(String.valueOf(workInfo.getRepeatCounter())));
            energyData
                .setWorkRepeatCycleTime(WORK_REPEAT_CYCLE_TYPE.concat(String.valueOf(workInfo.getRepeatCycleTime())));
            if (workInfo.getIsRequestDelay()) {
                energyData.setWorkDelay(WORK_DELAY_TYPE);
            }
        }
    }

    /**
     * buildEnergyLocationChartDataModel
     *
     * @param energyLocationUsage energyLocationUsage
     * @return ArrayList <ChartDataModel>
     */
    public static ArrayList<ChartDataModel> buildEnergyLocationChartDataModel(int energyLocationUsage) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("buildEnergyLocationChartDataModel");
        }
        MonitorItemDetail monitorItemDetail = MonitorItemDetail.ENERGY_LOCATION;
        ChartDataModel energyLocationModel = new ChartDataModel();
        energyLocationModel.setIndex(monitorItemDetail.getIndex());
        energyLocationModel.setColor(monitorItemDetail.getColor());
        energyLocationModel.setName(monitorItemDetail.getName());
        energyLocationModel.setValue(energyLocationUsage);
        ArrayList<ChartDataModel> result = new ArrayList<>();
        result.add(energyLocationModel);
        return result;
    }

    private void handleJavaHeapData(AgentPluginJavaHeap.BatchAgentMemoryEvent javaHeapData) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("handleJavaHeapData");
        }
        List<AgentPluginJavaHeap.AgentMemoryEvent> eventsList = javaHeapData.getEventsList();
        for (AgentPluginJavaHeap.AgentMemoryEvent agentMemoryEvent : eventsList) {
            long agentTime = getAgentTime(agentMemoryEvent.getTvSec(), agentMemoryEvent.getTvNsec());
            if (agentMemoryEvent.hasClassData()) {
                AgentPluginJavaHeap.ClassInfo classData = agentMemoryEvent.getClassData();
                int clazzId = classData.getClassId();
                String clzName = classData.getClassName();
                if (clazzId > 0) {
                    addClassInfoList(clazzId, clzName);
                }
            }
            if (agentMemoryEvent.hasAllocData()) {
                AgentPluginJavaHeap.AllocationInfo allocData = agentMemoryEvent.getAllocData();
                int instanceId = allocData.getObjectId();
                int classId = allocData.getClassId();
                List<AgentPluginJavaHeap.AllocationInfo.StackFrameInfo> frameInfoList = allocData.getFrameInfoList();
                if (instanceId > 0) {
                    callStackInfo(instanceId, frameInfoList);
                    MemoryHeapInfo memoryHeapInfo = getMemoryHeapInfo(allocData, instanceId, classId);
                    long objSize = allocData.getObjectSize();
                    int arrayLength = allocData.getArrayLength();
                    memoryHeapInfo.setTotalCount(1);
                    if (arrayLength <= 0) {
                        memoryHeapInfo.setShallowSize(objSize);
                    } else {
                        memoryHeapInfo.setShallowSize(arrayLength * objSize);
                    }
                    memoryHeapInfo.setCreateTime(agentTime);
                    memoryHeapInfos.add(memoryHeapInfo);
                }
            }
            if (agentMemoryEvent.hasFreeData()) {
                AgentPluginJavaHeap.DeallocationInfo freeData = agentMemoryEvent.getFreeData();
                int objectId = freeData.getObjectId();
                if (objectId != 0) {
                    MemoryUpdateInfo memoryUpdateInfo = new MemoryUpdateInfo(agentTime, objectId);
                    memoryUpdateInfos.add(memoryUpdateInfo);
                    insertDataOrUpdate(false);
                }
            }
            insertDataOrUpdate(false);
        }
    }

    private MemoryHeapInfo getMemoryHeapInfo(AgentPluginJavaHeap.AllocationInfo allocData, int instanceId,
        int classId) {
        MemoryHeapInfo memoryHeapInfo = new MemoryHeapInfo();
        memoryHeapInfo.setAllocations(1);
        memoryHeapInfo.setcId(classId);
        memoryHeapInfo.setInstanceId(instanceId);
        memoryHeapInfo.setSessionId(localSessionId);
        memoryHeapInfo.setHeapId(allocData.getHeapId());
        memoryHeapInfo.setDeallocations(0);
        return memoryHeapInfo;
    }

    /**
     * addClassInfoList
     *
     * @param clazzId clazzId
     * @param clzName clzName
     */
    private void addClassInfoList(int clazzId, String clzName) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addClassInfoList");
        }
        ClassInfo classInfo = new ClassInfo();
        classInfo.setcId(clazzId);
        classInfo.setClassName(clzName);
        classInfoList.add(classInfo);
    }

    private long getAgentTime(long tvSec, long tvnsec) {
        return (tvSec * 1000000000L + tvnsec) / 1000000;
    }

    private void callStackInfo(int instanceId, List<AgentPluginJavaHeap.AllocationInfo.StackFrameInfo> stackFrame) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("callStackInfo");
        }
        for (AgentPluginJavaHeap.AllocationInfo.StackFrameInfo stackFrameInfo : stackFrame) {
            MemoryInstanceDetailsInfo memoryInstanceDetailsInfo = new MemoryInstanceDetailsInfo();
            int frameId = stackFrameInfo.getFrameId();
            String className = stackFrameInfo.getClassName();
            String methodName = stackFrameInfo.getMethodName();
            String fileName = stackFrameInfo.getFileName();
            int lineNumber = stackFrameInfo.getLineNumber();
            memoryInstanceDetailsInfo.setClassName(className);
            memoryInstanceDetailsInfo.setFrameId(frameId);
            memoryInstanceDetailsInfo.setMethodName(methodName);
            memoryInstanceDetailsInfo.setLineNumber(lineNumber);
            memoryInstanceDetailsInfo.setInstanceId(instanceId);
            memoryInstanceDetailsInfo.setFieldName(fileName);
            memoryInstanceDetailsInfos.add(memoryInstanceDetailsInfo);
        }
    }
}
