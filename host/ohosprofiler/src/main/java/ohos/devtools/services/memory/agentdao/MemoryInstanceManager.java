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

import ohos.devtools.services.memory.agentbean.MemoryInstanceInfo;

import java.util.ArrayList;

/**
 * Memory Instance Manager
 */
public class MemoryInstanceManager {
    /**
     * Find the corresponding instance information in the database according
     * to the class ID and provide it to the UI query interface
     *
     * @param cId 父Id
     * @param startTime startTime
     * @param endTime endTime
     * @return ArrayList <MemoryInstanceInfo>
     */
    public ArrayList<MemoryInstanceInfo> getMemoryInstanceInfos(Integer cId, Long startTime, Long endTime) {
        MemoryInstanceDao memoryInstanceDao = MemoryInstanceDao.getInstance();
        return memoryInstanceDao.getMemoryInstanceInfos(cId, startTime, endTime);
    }

    /**
     * Check the instance information according to the class
     *
     * @return ArrayList <MemoryInstanceInfo>
     */
    public ArrayList<MemoryUpdateInfo> getAllMemoryInstanceInfos() {
        MemoryInstanceDao memoryInstanceDao = MemoryInstanceDao.getInstance();
        return memoryInstanceDao.getAllMemoryInstanceInfos();
    }
}
