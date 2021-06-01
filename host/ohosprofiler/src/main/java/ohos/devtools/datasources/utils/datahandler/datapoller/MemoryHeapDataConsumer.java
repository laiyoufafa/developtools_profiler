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

import com.google.protobuf.InvalidProtocolBufferException;
import ohos.devtools.datasources.transport.grpc.service.AgentPluginResult;
import ohos.devtools.datasources.transport.grpc.service.CommonTypes;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.services.memory.ClassInfo;
import ohos.devtools.services.memory.ClassInfoDao;
import ohos.devtools.services.memory.MemoryHeapDao;
import ohos.devtools.services.memory.MemoryHeapInfo;
import ohos.devtools.services.memory.MemoryInstanceDao;
import ohos.devtools.services.memory.MemoryInstanceDetailsDao;
import ohos.devtools.services.memory.MemoryInstanceDetailsInfo;
import ohos.devtools.services.memory.MemoryInstanceInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * @Description MemoryHeapDataConsumer
 * @Date 2021/4/9 13:56
 **/
public class MemoryHeapDataConsumer extends Thread {
    private static final Logger HEAPDATA = LogManager.getLogger("HEAPDATA");
    private static final Logger LOGGER = LogManager.getLogger(MemoryHeapDataConsumer.class);

    /**
     * 每隔多少毫秒保存一次数据入库
     */
    private static final long SAVE_FREQ = 500;

    private Queue<CommonTypes.ProfilerPluginData> queue;
    private Long localSessionId;
    private ClassInfoDao classInfoDao;
    private MemoryInstanceDetailsDao memoryInstanceDetailsDao;
    private MemoryInstanceDao memoryInstanceDao;
    private MemoryHeapDao memoryHeapDao;
    private Integer LogIndex = 0;
    private boolean stopFlag = false;
    private long firstTime = 0L;
    private int firstDataCount = 0;
    private boolean isInsert = false;

    /**
     * Memory入库时的时间参照变量，每隔SAVE_FREQ保存一次数据入库
     */
    private long flagTime = DateTimeUtil.getNowTimeLong();

    private List<ClassInfo> classInfoList = new ArrayList<>();

    private List<MemoryInstanceDetailsInfo> memoryInstanceDetailsInfos = new ArrayList<>();

    private List<MemoryHeapInfo> memoryHeapInfos = new ArrayList<>();

    private List<MemoryInstanceInfo> memoryInstanceInfos = new ArrayList<>();

    /**
     * MemoryHeapDataConsumer
     *
     * @param queue                    queue
     * @param localSessionId           localSessionId
     * @param classInfoDao             classInfoDao
     * @param memoryInstanceDetailsDao memoryInstanceDetailsDao
     * @param memoryInstanceDao        memoryInstanceDao
     * @param memoryHeapDao            memoryHeapDao
     */
    public MemoryHeapDataConsumer(Queue<CommonTypes.ProfilerPluginData> queue, Long localSessionId,
        ClassInfoDao classInfoDao, MemoryInstanceDetailsDao memoryInstanceDetailsDao,
        MemoryInstanceDao memoryInstanceDao, MemoryHeapDao memoryHeapDao) {
        this.queue = queue;
        this.localSessionId = localSessionId;
        this.classInfoDao = classInfoDao;
        this.memoryInstanceDetailsDao = memoryInstanceDetailsDao;
        this.memoryInstanceDao = memoryInstanceDao;
        this.memoryHeapDao = memoryHeapDao;
    }

    /**
     * run
     */
    @Override
    public void run() {
        while (true) {
            try {
                if (LogIndex == Integer.MAX_VALUE) {
                    LogIndex = 0;
                }
                if (stopFlag) {
                    return;
                }
                long now = DateTimeUtil.getNowTimeLong();
                if (now - flagTime > SAVE_FREQ) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(10);
                    } catch (InterruptedException exception) {
                        HEAPDATA.info("InterruptedException");
                    }
                }
                if (queue.size() > 0) {
                    CommonTypes.ProfilerPluginData dataObject = queue.poll();
                    if (dataObject != null) {
                        handleMemoryHeapHandle(dataObject);
                    }
                }
                insertData();
            } catch (Exception exception) {
                LOGGER.error(" exception  {} ", exception.getMessage());
            }
        }
    }

    private void insertData() {
        long now = DateTimeUtil.getNowTimeLong();
        if (now - flagTime > SAVE_FREQ) {
            if (!isInsert) {
                boolean insertRes = classInfoDao.insertClassInfos(classInfoList);
                if (insertRes) {
                    classInfoList.clear();
                }
                boolean instanceRes =
                    memoryInstanceDetailsDao.insertMemoryInstanceDetailsInfo(memoryInstanceDetailsInfos);
                if (instanceRes) {
                    memoryInstanceDetailsInfos.clear();
                }
                boolean memHeapInfoRes = memoryHeapDao.insertMemoryHeapInfos(memoryHeapInfos);
                if (memHeapInfoRes) {
                    memoryHeapInfos.clear();
                }
                boolean memInstanceRes = memoryInstanceDao.insertMemoryInstanceInfos(memoryInstanceInfos);
                if (memInstanceRes) {
                    memoryInstanceInfos.clear();
                }
                isInsert = true;
                flagTime = now;
            }
        }
    }

    /**
     * shutDown
     */
    public void shutDown() {
        stopFlag = true;
    }

    private void handleMemoryHeapHandle(CommonTypes.ProfilerPluginData memoryData) {
        if (firstTime == 0L) {
            // utc 毫秒值
            firstTime = (memoryData.getTvSec() * 1000000000L + memoryData.getTvNsec()) / 1000000;
        }
        AgentPluginResult.BatchAgentMemoryEvent.Builder batchBuilder =
            AgentPluginResult.BatchAgentMemoryEvent.newBuilder();
        AgentPluginResult.BatchAgentMemoryEvent bath = null;
        try {
            bath = batchBuilder.mergeFrom(memoryData.getData()).build();
        } catch (InvalidProtocolBufferException exception) {
            LOGGER.info("mergeFrom failed {}", exception.getMessage());
        }
        List<AgentPluginResult.AgentMemoryEvent> agentMemoryEvents = bath.getEventsList();
        for (AgentPluginResult.AgentMemoryEvent agentMemoryEvent : agentMemoryEvents) {
            if (stopFlag) {
                return;
            }
            // 时间戳
            long timeStamp = agentMemoryEvent.getTimestamp();
            setPluginClassInfo(agentMemoryEvent);
            // memoryHeapInfo信息
            AgentPluginResult.AllocationInfo alloc = agentMemoryEvent.getAllocData();
            int instanceId = alloc.getObjectId();
            int classId = alloc.getClassId();
            String threadName = alloc.getThreadName();
            List<AgentPluginResult.AllocationInfo.StackFrameInfo> stackFram = alloc.getFrameInfoList();
            if (instanceId > 0) {
                MemoryHeapInfo memoryHeapInfo = new MemoryHeapInfo();
                // 根据调用栈信息计算
                if (!stackFram.isEmpty()) {
                    memoryHeapInfo.setAllocations(1);
                } else {
                    memoryHeapInfo.setAllocations(0);
                }
                // 调用栈信息
                callStackInfo(instanceId, stackFram);
                memoryHeapInfo.setcId(classId);
                memoryHeapInfo.setInstanceId(instanceId);
                memoryHeapInfo.setSessionId(localSessionId);
                long createTime = setMemoryHeapInfo(memoryData, timeStamp, alloc, memoryHeapInfo);
                setMemoryInstanceInfo(instanceId, classId, createTime);
            }
            isInsert = false;
            insertData();
            AgentPluginResult.DeallocationInfo deallocationInfo = agentMemoryEvent.getFreeData();
            int objectId = deallocationInfo.getObjectId();
            if (objectId != 0) {
                long dellocTime = timeStamp;
                memoryInstanceDao.updateInstanceInfos(dellocTime, objectId);
                memoryHeapDao.updateMemoryHeapInfo(objectId);
            }
        }
    }

    private void setPluginClassInfo(AgentPluginResult.AgentMemoryEvent agentMemoryEvent) {
        AgentPluginResult.ClassInfo classData = agentMemoryEvent.getClassData();
        int clazzId = classData.getClassId();
        String clzName = classData.getClassName();
        if (clazzId > 0) {
            ClassInfo classInfo = new ClassInfo();
            classInfo.setcId(clazzId);
            classInfo.setClassName(clzName);
            LOGGER.debug("classInfo is {}", classInfo);
            classInfoList.add(classInfo);
        }
    }

    private void setMemoryInstanceInfo(int instanceId, int classId, long createTime) {
        MemoryInstanceInfo memoryInstanceInfo = new MemoryInstanceInfo();
        memoryInstanceInfo.setInstanceId(instanceId);
        memoryInstanceInfo.setCreateTime(createTime);
        memoryInstanceInfo.setAllocTime(createTime);
        memoryInstanceInfo.setcId(classId);
        memoryInstanceInfo.setDeallocTime(0L);
        HEAPDATA.debug("memoryInstanceInfo is {}", memoryInstanceInfo);
        memoryInstanceInfos.add(memoryInstanceInfo);
    }

    private long setMemoryHeapInfo(CommonTypes.ProfilerPluginData memoryData, long timeStamp,
        AgentPluginResult.AllocationInfo alloc, MemoryHeapInfo memoryHeapInfo) {
        int heapId = alloc.getHeapId();
        memoryHeapInfo.setHeapId(heapId);
        memoryHeapInfo.setDeallocations(0);
        long objSize = alloc.getObjectSize();
        int arrayLength = alloc.getArrayLength();
        if (arrayLength < 0) {
            memoryHeapInfo.setTotalCount(1);
            memoryHeapInfo.setShallowSize(objSize);
        } else {
            memoryHeapInfo.setTotalCount(1);
            memoryHeapInfo.setShallowSize(arrayLength * objSize);
        }
        long createTime;
        if (timeStamp == 0L) {
            createTime = firstTime;
        } else {
            createTime = (memoryData.getTvSec() * 1000000000L + memoryData.getTvNsec()) / 1000000;
        }
        memoryHeapInfo.setCreateTime(createTime);
        LOGGER.debug("memoryHeapInfo data is {}", memoryHeapInfo);
        memoryHeapInfos.add(memoryHeapInfo);
        return createTime;
    }

    private void callStackInfo(int instanceId, List<AgentPluginResult.AllocationInfo.StackFrameInfo> stackFram) {
        for (AgentPluginResult.AllocationInfo.StackFrameInfo stackFrameInfo : stackFram) {
            MemoryInstanceDetailsInfo memoryInstanceDetailsInfo = new MemoryInstanceDetailsInfo();
            int frameID = stackFrameInfo.getFrameId();
            String className = stackFrameInfo.getClassName();
            String methodName = stackFrameInfo.getMethodName();
            String fileName = stackFrameInfo.getFileName();
            int lineNumber = stackFrameInfo.getLineNumber();
            memoryInstanceDetailsInfo.setClassName(className);
            memoryInstanceDetailsInfo.setFrameId(frameID);
            memoryInstanceDetailsInfo.setMethodName(methodName);
            memoryInstanceDetailsInfo.setLineNumber(lineNumber);
            memoryInstanceDetailsInfo.setInstanceId(instanceId);
            memoryInstanceDetailsInfo.setFieldName(fileName);
            LOGGER.debug("memoryInstanceDetailsInfo is {}", memoryInstanceDetailsInfo);
            memoryInstanceDetailsInfos.add(memoryInstanceDetailsInfo);
        }
    }
}
