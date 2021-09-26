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
import ohos.devtools.services.memory.agentdao.ClassInfoDao;
import ohos.devtools.services.memory.agentdao.MemoryHeapDao;
import ohos.devtools.services.memory.agentdao.MemoryInstanceDao;
import ohos.devtools.services.memory.agentdao.MemoryInstanceDetailsDao;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static ohos.devtools.datasources.utils.common.Constant.JVMTI_AGENT_PLUG;
import static ohos.devtools.datasources.utils.common.Constant.MEMORY_PLUG;

/**
 * Memory Data Consumer Test
 */
public class MemoryDataConsumerTest {
    private long localSessionId;
    private int sessionId;
    private Queue queue;
    private MemoryTable memoryTable;

    /**
     * functional testing init
     *
     * @tc.name: Memory initialization configuration
     * @tc.number: OHOS_JAVA_datahandler_MemoryDataConsumer_init_0001
     * @tc.desc: Memory initialization configuration
     * @tc.type: functional testing
     * @tc.require: AR000FK61R
     */
    @Before
    public void init() {
        this.localSessionId = 3274894L;
        this.sessionId = 3247;
        queue = new LinkedBlockingQueue();
        HashMap<String, AbstractDataStore> tableService = new HashMap<>();
        tableService.put(MEMORY_PLUG, new MemoryTable());
        tableService.put(JVMTI_AGENT_PLUG, new ClassInfoDao());
        tableService.put("jvmtiagentDetails", new MemoryInstanceDetailsDao());
        tableService.put("jvmtiagentInstance", new MemoryInstanceDao());
        tableService.put("jvmtiagentMemoryHeap", new MemoryHeapDao());
        if (tableService.get(MEMORY_PLUG) instanceof MemoryTable) {
            memoryTable = (MemoryTable) tableService.get(MEMORY_PLUG);
        }
    }

    /**
     * functional testing MemoryDataConsumer
     *
     * @tc.name: MemoryDataConsumer
     * @tc.number: OHOS_JAVA_datahandler_MemoryDataConsumer_constructor_0001
     * @tc.desc: MemoryDataConsumer
     * @tc.type: functional testing
     * @tc.require: SR000FK61J
     */
    @Test
    public void handleMemoryDataTest() {
        MemoryDataConsumer consumer = new MemoryDataConsumer();
        consumer.init(queue, sessionId, localSessionId);
        ExecutorService executorService = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
        executorService.execute(consumer);
    }
}
