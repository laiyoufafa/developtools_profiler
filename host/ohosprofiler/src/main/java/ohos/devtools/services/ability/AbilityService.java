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

package ohos.devtools.services.ability;

import java.util.List;

/**
 * Ability Service
 *
 * @since: 2021/9/20
 */
public class AbilityService {
    private static AbilityService instance;

    private AbilityService() {
    }

    /**
     * DiskIoService instance
     *
     * @return MemoryService
     */
    public static AbilityService getInstance() {
        if (instance == null) {
            synchronized (AbilityService.class) {
                if (instance == null) {
                    instance = new AbilityService();
                }
            }
        }
        return instance;
    }

    /**
     * Get all data
     *
     * @param sessionId sessionId
     * @return List <AbilityCardData>
     */
    public List<AbilityActivityInfo> getAllData(long sessionId) {
        return AbilityDao.getInstance().getAllData(sessionId);
    }

    /**
     * deleteSessionData
     *
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean deleteSessionData(long sessionId) {
        return AbilityDao.getInstance().deleteAbilityData(sessionId);
    }
}
