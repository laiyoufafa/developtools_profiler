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

package ohos.devtools.datasources.transport.grpc;

import ohos.devtools.datasources.transport.grpc.service.DiskioPluginConfig;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Disk Io Helper
 *
 * @since: 2021/9/20
 */
public class DiskIoHelper {
    private static final Logger LOGGER = LogManager.getLogger(DiskIoHelper.class);

    private DiskIoHelper() {
    }

    /**
     * The configuration object when requesting single-process DiskIo data needs to be
     * converted into binary and passed into createSessionRequest or startSessionRequest
     *
     * @param pid pid
     * @return DiskIoPluginConfig.DiskIoConfig
     */
    public static DiskioPluginConfig.DiskioConfig createDiskIORequest(int pid) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createDiskIORequest");
        }
        DiskioPluginConfig.DiskioConfig.Builder builder = DiskioPluginConfig.DiskioConfig.newBuilder();
        if (pid > 0) {
            builder.setUnspeciFied(pid);
        }
        return builder.build();
    }

    /**
     * The configuration object when requesting multi-process DiskIo data needs to be converted
     * into binary and passed into createSessionRequest or startSessionRequest
     *
     * @param pidList pidList
     * @return DiskIoPluginConfig.DiskIoConfig
     */
    public static DiskioPluginConfig.DiskioConfig createDiskIORequest(List<Integer> pidList) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createDiskIORequest");
        }
        DiskioPluginConfig.DiskioConfig.Builder builder = DiskioPluginConfig.DiskioConfig.newBuilder();
        return builder.build();
    }
}
