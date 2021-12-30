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

import ohos.devtools.datasources.transport.grpc.service.MemoryPluginConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Construct the configuration information help class of the memory plugin.
 */
public final class MemoryPlugHelper {
    private static final Logger LOGGER = LogManager.getLogger(MemoryPlugHelper.class);

    private MemoryPlugHelper() {
    }

    /**
     * Grpc request when requesting process information.
     *
     * @return MemoryPluginConfig.MemoryConfig
     */
    public static MemoryPluginConfig.MemoryConfig createProcessRequest() {
        MemoryPluginConfig.MemoryConfig.Builder builder = MemoryPluginConfig.MemoryConfig.newBuilder();
        builder.setReportProcessTree(true);
        return builder.build();
    }

    /**
     * The configuration object when requesting single-process memory data needs to be
     * converted into binary and passed into createSessionRequest or startSessionRequest
     *
     * @param pid pid
     * @param reportProcessTree reportProcessTree
     * @param reportProcessMemInfo reportProcessMemInfo
     * @param reportAppMemInfo reportAppMemInfo
     * @param reportAppMemByService reportAppMemByService
     * @return MemoryPluginConfig.MemoryConfig
     */
    public static MemoryPluginConfig.MemoryConfig createMemRequest(int pid, boolean reportProcessTree,
        boolean reportProcessMemInfo, boolean reportAppMemInfo, boolean reportAppMemByService) {
        MemoryPluginConfig.MemoryConfig.Builder builder = MemoryPluginConfig.MemoryConfig.newBuilder();
        if (pid > 0) {
            builder.addPid(pid);
        }
        builder.setReportProcessTree(reportProcessTree);
        builder.setReportProcessMemInfo(reportProcessMemInfo);
        builder.setReportAppMemInfo(reportAppMemInfo);
        builder.setReportAppMemByMemoryService(reportAppMemByService);
        return builder.build();
    }

    /**
     * The configuration object when requesting multi-process memory data needs to be converted
     * into binary and passed into createSessionRequest or startSessionRequest
     *
     * @param pids pids
     * @return MemoryPluginConfig.MemoryConfig
     */
    public static MemoryPluginConfig.MemoryConfig createMemRequest(List<Integer> pids) {
        MemoryPluginConfig.MemoryConfig.Builder builder = MemoryPluginConfig.MemoryConfig.newBuilder();
        builder.addAllPid(pids);
        return builder.build();
    }

}
