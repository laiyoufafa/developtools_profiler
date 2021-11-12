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

package ohos.devtools.services.diskio;

import ohos.devtools.datasources.databases.datatable.enties.DiskIOData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * DiskIo Business processing
 *
 * @since 2021/9/20
 */
public class DiskIoService {
    private static final Logger LOGGER = LogManager.getLogger(DiskIoService.class);
    private static DiskIoService instance;

    private DiskIoService() {
    }

    /**
     * DiskIoService
     *
     * @return MemoryService
     */
    public static DiskIoService getInstance() {
        if (instance == null) {
            synchronized (DiskIoService.class) {
                if (instance == null) {
                    instance = new DiskIoService();
                }
            }
        }
        return instance;
    }

    /**
     * Get all data
     *
     * @param sessionId sessionId
     * @return List <DiskIOData>
     */
    public List<DiskIOData> getAllData(long sessionId) {
        return DiskIoDao.getInstance().getAllData(sessionId);
    }

    /**
     * deleteSessionData
     *
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean deleteSessionData(long sessionId) {
        return DiskIoDao.getInstance().deleteSessionData(sessionId);
    }
}
