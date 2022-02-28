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

package ohos.devtools.services.memory.memoryservice;

import ohos.devtools.datasources.databases.datatable.enties.ProcessMemInfo;
import ohos.devtools.services.memory.memorydao.MemoryDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Memory business processing class
 *
 * @since 2021/5/19 16:39
 */
public class MemoryService {
    private static final Logger LOGGER = LogManager.getLogger(MemoryService.class);

    private static MemoryService instance;

    private MemoryService() {
    }

    /**
     * MemoryService
     *
     * @return MemoryService
     */
    public static MemoryService getInstance() {
        if (instance == null) {
            synchronized (MemoryService.class) {
                if (instance == null) {
                    instance = new MemoryService();
                }
            }
        }
        return instance;
    }

    /**
     * get All Data
     *
     * @param sessionId sessionId
     * @return List　<ProcessMemInfo>
     */
    public List<ProcessMemInfo> getAllData(long sessionId) {
        return MemoryDao.getInstance().getAllData(sessionId);
    }

    /**
     * deleteSessionData
     *
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean deleteSessionData(long sessionId) {
        return MemoryDao.getInstance().deleteSessionData(sessionId);
    }
}
