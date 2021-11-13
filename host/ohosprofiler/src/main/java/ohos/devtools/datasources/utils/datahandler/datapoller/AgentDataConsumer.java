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
import ohos.devtools.datasources.databases.datatable.AbilityTable;
import ohos.devtools.datasources.databases.datatable.EnergyTable;
import ohos.devtools.datasources.databases.datatable.NetWorkInfoTable;
import ohos.devtools.datasources.transport.grpc.service.AgentPluginAppData;
import ohos.devtools.datasources.transport.grpc.service.AgentPluginJavaHeap;
import ohos.devtools.datasources.transport.grpc.service.AgentPluginResult;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.services.ability.AbilityActivityInfo;
import ohos.devtools.services.ability.AbilityDataCache;
import ohos.devtools.services.ability.AbilityEventInfo;
import ohos.devtools.services.ability.EventType;
import ohos.devtools.services.memory.agentbean.ClassInfo;
import ohos.devtools.services.memory.agentbean.MemoryHeapInfo;
import ohos.devtools.services.memory.agentbean.MemoryInstanceDetailsInfo;
import ohos.devtools.services.memory.agentdao.ClassInfoDao;
import ohos.devtools.services.memory.agentdao.MemoryHeapDao;
import ohos.devtools.services.memory.agentdao.MemoryInstanceDao;
import ohos.devtools.services.memory.agentdao.MemoryInstanceDetailsDao;
import ohos.devtools.services.memory.agentdao.MemoryUpdateInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private int energyUsage;
    private int energyRegisterUsage;
    private Boolean isStart;
    private long eventId;
    private String systemEvent;
    private String description;
    private String provider;
    private String priority;
    private long minInterval;
    private long festInterval;
    private long triggertimeNs;
    private String startType;
    private String endType;
    private String callStack;
    private long agentStartTimeStamp;
    private boolean agentFirstStartTimeStamp = true;
    private Queue<CommonTypes.ProfilerPluginData> queue;
    private long localSessionId;
    private ClassInfoDao classInfoDao;
    private MemoryInstanceDetailsDao memoryInstanceDetailsDao;
    private MemoryInstanceDao memoryInstanceDao;
    private MemoryHeapDao memoryHeapDao;
    private boolean stopFlag = false;
    private AbilityDataCache abilityDataCache;
    private String beforeAbilityName;
    private int beforeLifeCycleId = -1;
    private String eventType;
    private EnergyTable energyTable = new EnergyTable();
    private AbilityTable abilityTable = new AbilityTable();
    private List<ClassInfo> classInfoList = new ArrayList<>();
    private List<MemoryInstanceDetailsInfo> memoryInstanceDetailsInfos = new ArrayList<>();
    private List<MemoryHeapInfo> memoryHeapInfos = new ArrayList<>();
    private List<MemoryUpdateInfo> memoryUpdateInfos = new ArrayList<>();
    private List<MemoryUpdateInfo> memoryUpdates = new ArrayList<>();
    private NetWorkInfoTable netWorkInfoTable = new NetWorkInfoTable();

    /**
     * activityStartTime
     */
    private long activityStartTime = -1;

    /**
     * activityEndTime
     */
    private long activityEndTime = -1;

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
        addAbilityLastData();
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
        if (agentData.hasAppData()) {
            AgentPluginAppData.BatchAgentAbilityEvent appData = agentData.getAppData();
            handleAbilityData(appData);
        }
    }

    /**
     * handle Ability data
     *
     * @param appData appData
     */
    private void handleAbilityData(AgentPluginAppData.BatchAgentAbilityEvent appData) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("handleAbility");
        }
        List<AgentPluginAppData.AgentAbilityEvent> eventsList = appData.getEventsList();
        for (AgentPluginAppData.AgentAbilityEvent abilityEvent : eventsList) {
            // real stamp time
            long timestamp = (abilityEvent.getTvSec() * 1000000000L + abilityEvent.getTvNsec()) / 1000000;
            if (abilityEvent.hasAbilityState()) {
                // AbilityStateInfo
                AgentPluginAppData.AbilityStateInfo abilityState = abilityEvent.getAbilityState();
                String abilityName = abilityState.getAbilityName();
                if (StringUtils.isNotBlank(abilityName)) {
                    createAbilityStateData(timestamp, abilityState);
                }
            }
            if (abilityEvent.hasKeyEvent()) {
                // KeyEvent
                AgentPluginAppData.KeyEvent eventKey = abilityEvent.getKeyEvent();
                int keyType = eventKey.getKeyType();
                createAbilityEventData(timestamp, EventType.KEY_EVENT, true, keyType, eventKey.getIsDown());
            }
            if (abilityEvent.hasMouseEvent()) {
                // MouseEvent
                int keyType = abilityEvent.getMouseEvent().getActionType();
                createAbilityEventData(timestamp, EventType.MOUSE_EVENT, false, keyType, false);
            }
            if (abilityEvent.hasRotationEvent()) {
                // RotationEvent
                int keyType = (int) abilityEvent.getRotationEvent().getValue();
                createAbilityEventData(timestamp, EventType.ROTATION_EVENT, false, keyType, false);
            }
            if (abilityEvent.hasTouchEvent()) {
                // TouchEvent
                int keyType = abilityEvent.getTouchEvent().getTouchType();
                if (keyType == 1 || keyType == 2) {
                    createAbilityEventData(timestamp, EventType.TOUCH_EVENT, false, keyType, false);
                }
            }
        }
    }

    /**
     * create AbilityEvent Data
     *
     * @param timestamp timestamp
     * @param eventType event Type
     * @param useKey is able use Key
     * @param keyType key Type
     * @param isDown is able Down
     */
    private void createAbilityEventData(long timestamp, EventType eventType, boolean useKey, int keyType,
        boolean isDown) {
        if (activityStartTime != -1 && timestamp >= activityStartTime) {
            if (timestamp <= activityEndTime || activityEndTime == -1) {
                AbilityEventInfo abilityEventInfo = new AbilityEventInfo();
                abilityEventInfo.setSessionId(localSessionId);
                abilityEventInfo.setTimeStamp(timestamp);
                abilityEventInfo.setEventType(eventType);
                abilityEventInfo.setKeyType(keyType);
                if (useKey) {
                    abilityEventInfo.setDown(isDown);
                }
                // add DataModel to abilityDataCache
                abilityDataCache.addEventDataModel(localSessionId, abilityEventInfo);
                // insertAppEventInfo
                boolean insertResult = abilityTable.insertAppEventInfo(abilityEventInfo);
                if (!insertResult) {
                    LOGGER.debug("insert ability event failed time {}", timestamp);
                }
            }
        }
    }

    /**
     * create Ability State Data
     *
     * @param timestamp timestamp
     * @param abilityStateInfo AgentPluginAppData.AbilityStateInfo
     */
    private void createAbilityStateData(long timestamp, AgentPluginAppData.AbilityStateInfo abilityStateInfo) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("executeUpdateAbilityData");
        }
        if (abilityDataCache == null) {
            abilityDataCache = AbilityDataCache.getInstance();
        }
        int status = abilityStateInfo.getStateValue();
        String abilityStateName = abilityStateInfo.getAbilityName();
        int lifeCycleId = abilityStateInfo.getLifeCycleId();
        if (beforeAbilityName == null && beforeLifeCycleId == -1) {
            updateAbilityData(timestamp, abilityStateInfo);
        } else {
            if (beforeLifeCycleId == lifeCycleId && Objects.equals(beforeAbilityName, abilityStateName)) {
                if (status == END_STATUS || status == BACKGROUND_STATUS) {
                    updateAbilityData(timestamp, abilityStateInfo);
                    beforeAbilityName = null;
                    beforeLifeCycleId = -1;
                }
            } else {
                updateAbilityData(timestamp, abilityStateInfo);
            }
        }
    }

    /**
     * update AbilityData
     *
     * @param time time
     * @param abilityStateInfo AgentPluginAppData.AbilityStateInfo
     */
    private void updateAbilityData(long time, AgentPluginAppData.AbilityStateInfo abilityStateInfo) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("updateAbilityData");
        }
        if (abilityStateInfo.getStateValue() == END_STATUS) {
            activityEndTime = time;
            activityStartTime = -1;
        } else {
            activityStartTime = time;
            activityEndTime = -1;
        }
        // Create an Ability Slice Data object
        AbilityActivityInfo abilityActivityInfo = new AbilityActivityInfo();
        abilityActivityInfo.setAbilityStateName(abilityStateInfo.getAbilityName());
        abilityActivityInfo.setSessionId(localSessionId);
        abilityActivityInfo.setLifeCycleId(abilityStateInfo.getLifeCycleId());
        abilityActivityInfo.setTimeStamp(time);
        abilityActivityInfo.setAbilityState(abilityStateInfo.getStateValue());
        // add DataModel to abilityDataCache
        abilityDataCache.addActivityDataModel(localSessionId, abilityActivityInfo);
        // insert AppActivity Info
        boolean insertResult = abilityTable.insertAppActivityInfo(abilityActivityInfo);
        if (!insertResult) {
            if (ProfilerLogManager.isInfoEnabled()) {
                LOGGER.info("insert ability activity failed {}", abilityStateInfo.getLifeCycleId());
            }

        }
        beforeAbilityName = abilityStateInfo.getAbilityName();
        beforeLifeCycleId = abilityStateInfo.getLifeCycleId();
    }

    /**
     * Increase the last fake data of ability
     */
    private void addAbilityLastData() {
        if (abilityDataCache == null) {
            abilityDataCache = AbilityDataCache.getInstance();
        }
        // Create an Ability Slice Data object
        AbilityActivityInfo abilityActivityInfo = new AbilityActivityInfo();
        abilityActivityInfo.setAbilityStateName(beforeAbilityName);
        abilityActivityInfo.setSessionId(localSessionId);
        abilityActivityInfo.setLifeCycleId(beforeLifeCycleId);
        abilityActivityInfo.setTimeStamp(-1);
        abilityActivityInfo.setAbilityState(END_STATUS);
        // add DataModel to abilityDataCache
        abilityDataCache.addActivityDataModel(localSessionId, abilityActivityInfo);
        // insert AppActivity Info
        boolean insertResult = abilityTable.insertAppActivityInfo(abilityActivityInfo);
        if (!insertResult) {
            LOGGER.debug("insert ability activity failed {}", beforeLifeCycleId);
        }
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

    @NotNull
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
