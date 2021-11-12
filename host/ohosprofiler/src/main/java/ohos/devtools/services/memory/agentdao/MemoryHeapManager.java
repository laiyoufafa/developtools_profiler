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

package ohos.devtools.services.memory.agentdao;

import ohos.devtools.services.memory.agentbean.AgentHeapBean;
import ohos.devtools.services.memory.agentbean.MemoryHeapInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Memory Heap Manager
 */
public class MemoryHeapManager {
    private final MemoryHeapDao memoryHeapDao = MemoryHeapDao.getInstance();

    /**
     * Get the data in the selected time period and provide it to the heap information query interface of the UI
     *
     * @param sessionId sessionId
     * @param startTime startTime
     * @param endTime   endTime
     * @return ArrayList <MemoryHeapInfo>
     */
    public List<AgentHeapBean> getMemoryHeapInfos(Long sessionId, Long startTime, Long endTime) {
        return memoryHeapDao.getMemoryHeapInfos(sessionId, startTime, endTime);
    }

    /**
     * get AllMemoryHeapInfos
     *
     * @param sessionId sessionId
     * @return ArrayList <MemoryHeapInfo>
     */
    public ArrayList<MemoryHeapInfo> getAllMemoryHeapInfos(Long sessionId) {
        return memoryHeapDao.getAllMemoryHeapInfos(sessionId);
    }
}
