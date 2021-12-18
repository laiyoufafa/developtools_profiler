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

package ohos.devtools.services.userdata;

import ohos.devtools.views.user.UserManager;

import java.util.Objects;

/**
 * UserData Business processing
 *
 * @since 2021/9/10
 */
public class UserDataService {
    private static UserDataService instance;

    private UserDataService() {
    }

    /**
     * DiskIoService
     *
     * @return MemoryService
     */
    public static UserDataService getInstance() {
        if (instance == null) {
            synchronized (UserDataService.class) {
                if (instance == null) {
                    instance = new UserDataService();
                }
            }
        }
        return instance;
    }

    /**
     * deleteSessionData
     *
     * @param sessionId sessionId
     * @return boolean
     */
    public boolean deleteSessionData(long sessionId) {
        if (Objects.nonNull(UserManager.getInstance().getSdkImpl())) {
            return UserDataDao.getInstance().deleteSessionData(sessionId);
        }
        return true;
    }
}
