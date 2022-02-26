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

import ohos.devtools.datasources.transport.grpc.service.CpuPluginConfig;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * cpu plug helper
 *
 * @since 2021/5/19 16:39
 */
public final class CpuPlugHelper {
    private static final Logger LOGGER = LogManager.getLogger(CpuPlugHelper.class);

    private CpuPlugHelper() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("create CpuPlugHelper");
        }
    }

    /**
     * Grpc request when requesting process information.
     *
     * @return MemoryConfig MemoryPluginConfig
     */
    public static CpuPluginConfig.CpuConfig createProcessRequest() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createProcessRequest");
        }
        CpuPluginConfig.CpuConfig.Builder builder = CpuPluginConfig.CpuConfig.newBuilder();
        return builder.build();
    }

    /**
     * The configuration object when requesting single-process memory data needs to be
     * converted into binary and passed into createSessionRequest or startSessionRequest
     *
     * @param pid pid
     * @return MemoryConfig MemoryPluginConfig
     */
    public static CpuPluginConfig.CpuConfig createCpuRequest(int pid) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createCpuRequest");
        }
        CpuPluginConfig.CpuConfig.Builder builder = CpuPluginConfig.CpuConfig.newBuilder();
        if (pid > 0) {
            builder.setPid(pid);
        }
        return builder.build();
    }

    /**
     * The configuration object when requesting multi-process memory data needs to be converted
     * into binary and passed into createSessionRequest or startSessionRequest
     *
     * @param pids pids
     * @return MemoryConfig MemoryPluginConfig
     */
    public static CpuPluginConfig.CpuConfig createCpuRequest(List<Integer> pids) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("createCpuRequest");
        }
        CpuPluginConfig.CpuConfig.Builder builder = CpuPluginConfig.CpuConfig.newBuilder();
        return builder.build();
    }
}
