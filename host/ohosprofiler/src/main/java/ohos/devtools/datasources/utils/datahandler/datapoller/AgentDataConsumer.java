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
import ohos.devtools.datasources.transport.grpc.service.AgentPluginJavaHeap;
import ohos.devtools.datasources.transport.grpc.service.AgentPluginResult;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.services.memory.agentbean.ClassInfo;
import ohos.devtools.services.memory.agentbean.MemoryHeapInfo;
import ohos.devtools.services.memory.agentbean.MemoryInstanceDetailsInfo;
import ohos.devtools.services.memory.agentdao.ClassInfoDao;
import ohos.devtools.services.memory.agentdao.MemoryHeapDao;
import ohos.devtools.services.memory.agentdao.MemoryInstanceDao;
import ohos.devtools.services.memory.agentdao.MemoryInstanceDetailsDao;
import ohos.devtools.services.memory.agentdao.MemoryUpdateInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Memory Heap DataConsumer
 */
public class AgentDataConsumer extends AbsDataConsumer {
    private static final Logger LOGGER = LogManager.getLogger(AgentDataConsumer.class);
    private static final int MAX_SIZE = 2000;
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
                    LOGGER.info("InterruptedException");
                }
            }
        }
    }

    private void insertDataOrUpdate(boolean isInsert) {
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
            LOGGER.error("mergeFrom Data failed {}", invalidProtocolBufferException.getMessage());
            return;
        }
        if (agentData.hasJavaheapData()) {
            AgentPluginJavaHeap.BatchAgentMemoryEvent javaHeapData = agentData.getJavaheapData();
            handleJavaHeapData(javaHeapData, memoryData);
        }
    }

    private void handleJavaHeapData(AgentPluginJavaHeap.BatchAgentMemoryEvent javaHeapData,
        CommonTypes.ProfilerPluginData memoryData) {
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
                    MemoryHeapInfo memoryHeapInfo = new MemoryHeapInfo();
                    memoryHeapInfo.setAllocations(1);
                    callStackInfo(instanceId, frameInfoList);
                    memoryHeapInfo.setcId(classId);
                    memoryHeapInfo.setInstanceId(instanceId);
                    memoryHeapInfo.setSessionId(localSessionId);
                    memoryHeapInfo.setHeapId(allocData.getHeapId());
                    memoryHeapInfo.setDeallocations(0);
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

    /**
     * addClassInfoList
     *
     * @param clazzId clazzId
     * @param clzName clzName
     */
    private void addClassInfoList(int clazzId, String clzName) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setcId(clazzId);
        classInfo.setClassName(clzName);
        classInfoList.add(classInfo);
    }

    private long getAgentTime(long tvSec, long tvnsec) {
        return (tvSec * 1000000000L + tvnsec) / 1000000;
    }

    private void callStackInfo(int instanceId, List<AgentPluginJavaHeap.AllocationInfo.StackFrameInfo> stackFrame) {
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
