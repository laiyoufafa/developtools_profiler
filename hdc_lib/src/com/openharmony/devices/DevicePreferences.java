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

package com.openharmony.devices;

/**
 * DevicePreferences which device use it ¡¢create at 20210912
 */
final class DevicePreferences {
    /**
     * DEFAULT_EMPTY
     */
    public static final String DEFAULT_EMPTY = "NoValue"; // no values

    /**
     * DEFAULT_PROP device will get
     */
    public static final String[] DEFAULT_PROP = {
            "ro.boot.selinux", "ro.build.date", "ro.build.fingerprint",
            "ro.build.version.sdk", "ro.vndk.version", "ro.build.type", "ro.debuggable", "ro.secure",
            "ro.build.version.security_patch", "ro.product.manufacturer", "ro.product.brand", "ro.product.board",
            "ro.product.model", "ro.product.device",
    };
}