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
import ohos.devtools.datasources.transport.hdc.HdcWrapper;
import ohos.devtools.datasources.utils.device.entity.DeviceIPPortInfo;
import ohos.devtools.datasources.utils.device.entity.DeviceType;
import ohos.devtools.datasources.utils.device.service.DeviceForwardPort;
import ohos.devtools.datasources.utils.plugin.service.PlugManager;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_CHECK_FPORT;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_GET_DEVICES;
import static ohos.devtools.datasources.transport.hdc.HdcCmdList.HDC_RESET;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_CHECK_FPORT;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_GET_DEVICES;
import static ohos.devtools.datasources.transport.hdc.HdcStdCmdList.HDC_STD_RESET;
import static ohos.devtools.datasources.transport.hdc.HdcWrapper.conversionCommand;
import static ohos.devtools.datasources.utils.device.entity.DeviceType.LEAN_HOS_DEVICE;
import static ohos.devtools.views.common.Constant.IS_SUPPORT_NEW_HDC;

/**
 * KeepSession
 *
 * @since 2021/5/19 16:39
 */
public class KeepSession implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(KeepSession.class);
    private final long localSessionId;
    private final int sessionId;
    private final DeviceIPPortInfo deviceIPPortInfo;
    private int logCount = 1;

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
        new SendSession().start();
    }

    private void handleForwardNotExits(DeviceIPPortInfo deviceIPPortInfo) {
        int forwardPort = deviceIPPortInfo.getForwardPort();
        ArrayList<String> cmdStr;
        if (forwardPort > 0) {
            if (IS_SUPPORT_NEW_HDC && deviceIPPortInfo.getDeviceType() == LEAN_HOS_DEVICE) {
                cmdStr = conversionCommand(HDC_STD_CHECK_FPORT, deviceIPPortInfo.getDeviceID());
            } else {
                cmdStr = conversionCommand(HDC_CHECK_FPORT, deviceIPPortInfo.getDeviceID());
            }
            String forward = HdcWrapper.getInstance().execCmdBy(cmdStr, 1);
            if (!forward.contains(String.valueOf(forwardPort))) {
                DeviceForwardPort.getInstance().forwardDevicePort(deviceIPPortInfo, forwardPort);
            }
        }
    }

    private class SendSession extends Thread {

        /**
         * SendSession
         */
        public SendSession() {
            super.setName("SendSession");
        }

        @Override
        public void run() {
            try {
                ProfilerServiceTypes.KeepSessionResponse keepSessionResponse = HiProfilerClient.getInstance()
                    .keepSession(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort(), sessionId);
                if (ProfilerLogManager.isDebugEnabled()) {
                    LOGGER.debug("KeepSession sessionId  {}, resp is {}", sessionId, keepSessionResponse.toString());
                }
            } catch (StatusRuntimeException exception) {
                LOGGER.error("KeepSession StatusRuntimeException ", exception);
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("KeepSession StatusRuntimeException ", exception);
                }
                if (exception instanceof StatusRuntimeException) {
                    String findCmd;
                    if (PlugManager.getInstance().getSystemOsName().contains("win")) {
                        findCmd = "findstr";
                    } else {
                        findCmd = "grep";
                    }
                    ArrayList<String> cmdStr;
                    if (deviceIPPortInfo.getDeviceType() == DeviceType.FULL_HOS_DEVICE) {
                        cmdStr = conversionCommand(HDC_GET_DEVICES, findCmd, deviceIPPortInfo.getDeviceID());
                    } else {
                        cmdStr = conversionCommand(HDC_STD_GET_DEVICES, findCmd, deviceIPPortInfo.getDeviceID());
                    }
                    LOGGER.info("KeepSession DEADLINE_EXCEEDED  check Device Exits");
                    if (StringUtils.isBlank(HdcWrapper.getInstance().execCmdBy(cmdStr, 2))) {
                        LOGGER.info("KeepSession DEADLINE_EXCEEDED  Device not Exits, to reset");
                        if (deviceIPPortInfo.getDeviceType() == DeviceType.FULL_HOS_DEVICE) {
                            String result = HdcWrapper.getInstance().execCmdBy(HDC_RESET);
                            if (result.contains(deviceIPPortInfo.getDeviceID()) && result.contains("device")) {
                                handleForwardNotExits(deviceIPPortInfo);
                            }
                        } else {
                            HdcWrapper.getInstance().execCmdBy(HDC_STD_RESET);
                            handleForwardNotExits(deviceIPPortInfo);
                        }
                        HiProfilerClient.getInstance()
                            .destroyProfiler(deviceIPPortInfo.getIp(), deviceIPPortInfo.getForwardPort());
                    }
                }
            }
        }
    }
}