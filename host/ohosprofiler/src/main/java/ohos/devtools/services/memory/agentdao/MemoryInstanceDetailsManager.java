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

import ohos.devtools.services.memory.agentbean.MemoryInstanceDetailsInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Memory Instance DetailsManager
 */
public class MemoryInstanceDetailsManager {
    private final MemoryInstanceDetailsDao memoryInstanceDetailsDao = MemoryInstanceDetailsDao.getInstance();

    /**
     * Find the call information corresponding to instance in the database according
     * to the instanceid and provide it to the query interface of the UI
     *
     * @param instanceId instanceId
     * @return ArrayList <MemoryInstanceDetailsInfo>
     */
    public ArrayList<MemoryInstanceDetailsInfo> getMemoryInstanceDetailsInfos(Integer instanceId) {
        return memoryInstanceDetailsDao.getMemoryInstanceDetails(instanceId);
    }

    /**
     * Find the call information corresponding to instance in the database according to instanceid
     *
     * @return ArrayList <MemoryInstanceDetailsInfo>
     */
    public List<MemoryInstanceDetailsInfo> getAllMemoryInstanceDetails() {
        return memoryInstanceDetailsDao.getAllMemoryInstanceDetails();
    }
}
