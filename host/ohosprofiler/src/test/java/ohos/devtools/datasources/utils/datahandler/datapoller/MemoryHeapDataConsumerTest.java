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

import ohos.devtools.datasources.databases.databasepool.AbstractDataStore;
import ohos.devtools.datasources.databases.datatable.MemoryTable;
import ohos.devtools.datasources.utils.common.util.DateTimeUtil;
import ohos.devtools.services.memory.ClassInfoDao;
import ohos.devtools.services.memory.MemoryHeapDao;
import ohos.devtools.services.memory.MemoryInstanceDao;
import ohos.devtools.services.memory.MemoryInstanceDetailsDao;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static ohos.devtools.datasources.utils.common.Constant.JVMTI_AGENT_PLUG;
import static ohos.devtools.datasources.utils.common.Constant.MEMORY_PLUG;

/**
 * @Description MemoryHeapDataConsumerTest
 * @Date 2021/4/23 15:02
 **/
public class MemoryHeapDataConsumerTest {
    private long localSessionId;
    private Queue queue;
    private ClassInfoDao classInfoDao;
    private MemoryInstanceDetailsDao memoryInstanceDetailsDao;
    private MemoryInstanceDao memoryInstanceDao;
    private MemoryHeapDao memoryHeapDao;
    private Long countTime;

    /**
     * functional testing init
     *
     * @tc.name: MemoryHeapData initialization configuration
     * @tc.number: OHOS_JAVA_datahandler_MemoryHeapHandle_init_0001
     * @tc.desc: MemoryHeapData initialization configuration
     * @tc.type: functional testing
     * @tc.require: AR000FK61R
     */
    @Before
    public void init() {
        this.localSessionId = 3274894L;
        queue = new LinkedBlockingQueue();
        HashMap<String, AbstractDataStore> tableService = new HashMap<>();
        tableService.put(MEMORY_PLUG, new MemoryTable());
        tableService.put(JVMTI_AGENT_PLUG, new ClassInfoDao());
        tableService.put("jvmtiagentDetails", new MemoryInstanceDetailsDao());
        tableService.put("jvmtiagentInstance", new MemoryInstanceDao());
        tableService.put("jvmtiagentMemoryHeap", new MemoryHeapDao());
        if (tableService.get(JVMTI_AGENT_PLUG) instanceof ClassInfoDao) {
            classInfoDao = (ClassInfoDao) tableService.get(JVMTI_AGENT_PLUG);
        }
        if (tableService.get("jvmtiagentDetails") instanceof MemoryInstanceDetailsDao) {
            memoryInstanceDetailsDao = (MemoryInstanceDetailsDao) tableService.get("jvmtiagentDetails");
        }
        if (tableService.get("jvmtiagentInstance") instanceof MemoryInstanceDao) {
            memoryInstanceDao = (MemoryInstanceDao) tableService.get("jvmtiagentInstance");
        }

        if (tableService.get("jvmtiagentMemoryHeap") instanceof MemoryHeapDao) {
            memoryHeapDao = (MemoryHeapDao) tableService.get("jvmtiagentMemoryHeap");
        }
        countTime = DateTimeUtil.getNowTimeLong();
    }

    /**
     * functional testing MemoryHeapHandle
     *
     * @tc.name: MemoryHeapHandle
     * @tc.number: OHOS_JAVA_datahandler_MemoryHeapHandle_constructor_0001
     * @tc.desc: MemoryHeapHandle
     * @tc.type: functional testing
     * @tc.require: SR000FK61J
     */
    @Test
    public void handleMemoryHeapHandleTest() {
        MemoryHeapDataConsumer memoryHeapDataConsumer =
            new MemoryHeapDataConsumer(queue, localSessionId, classInfoDao, memoryInstanceDetailsDao, memoryInstanceDao,
                memoryHeapDao);
        memoryHeapDataConsumer.start();
    }
}
