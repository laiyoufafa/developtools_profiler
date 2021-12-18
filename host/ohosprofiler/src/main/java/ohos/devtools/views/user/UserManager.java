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

package ohos.devtools.views.user;

import model.AbstractSdk;

import java.util.Optional;

/**
 * Sdk Manager
 *
 * @since 2021/11/22
 */
public class UserManager {
    private static volatile UserManager userManager;

    private UserManager() {
    }

    /**
     * Get an instance
     *
     * @return UserDataDao
     */
    public static UserManager getInstance() {
        if (userManager == null) {
            synchronized (UserManager.class) {
                if (userManager == null) {
                    userManager = new UserManager();
                }
            }
        }
        return userManager;
    }

    /**
     * getSdkImpl
     *
     * @return Optional<AbstractSdk>
     */
    public Optional<AbstractSdk> getSdkImpl() {
        return Optional.ofNullable(null);
    }
}
