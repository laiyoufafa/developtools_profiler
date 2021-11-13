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

package ohos.devtools.datasources.utils.session;

import io.grpc.StatusRuntimeException;
import ohos.devtools.datasources.transport.grpc.HiProfilerClient;
import ohos.devtools.datasources.transport.grpc.service.ProfilerServiceTypes;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.datasources.utils.quartzmanager.QuartzManager;
import ohos.devtools.datasources.utils.session.service.SessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * KeepSession
 */
public class KeepSession implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(KeepSession.class);
    private final long localSessionId;
    private final int sessionId;
    private final DeviceIPPortInfo deviceIPPortInfo;

    /**
     * KeepSession
     *
     * @param localSessionId localSessionId
     * @param sessionId sessionId
     * @param deviceIPPortInfo deviceIPPortInfo
     */
    public KeepSession(long localSessionId, int sessionId, DeviceIPPortInfo deviceIPPortInfo) {
        this.localSessionId = localSessionId;
        this.sessionId = sessionId;
        this.deviceIPPortInfo = deviceIPPortInfo;
    }

    @Override
    public void run() {
        try {
            ProfilerServiceTypes.KeepSessionResponse keepSessionResponse = HiProfilerClient.getInstance()
                .keepSession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId);
        } catch (StatusRuntimeException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("KeepSession StatusRuntimeException ", exception);
            }
            String keepSessionName = SessionManager.getInstance().getKeepSessionName(deviceIPPortInfo, sessionId);
            QuartzManager.getInstance().deleteExecutor(keepSessionName);
        }
    }
}
