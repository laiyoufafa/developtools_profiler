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

package com.openharmony.client;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeoutException;

import com.openharmony.client.ClientHelper.HdcResponse;
import com.openharmony.hdc.HarmonyDebugConnector;
import com.openharmony.hdc.Hilog;
import com.openharmony.utils.FormatUtil;

/**
 * send Remote Command to server ��create at 20210912
 */
class RemoteCommand {
    private static String TAG = "RemoteCommand";
    private HarmonyDebugConnector mHdc;
    private SocketChannel mHdcConnection;
    private String mDevicesId;

    /**
     * init client send Remote command by this methond
     *
     * @param Hdc with hdc bin
     * @param connectKey device id ,if it's empty,just local command can be send
     */
    protected RemoteCommand(HarmonyDebugConnector hdc, String connectKey) {
        mHdc = hdc;
        mDevicesId = connectKey;
    }

    /**
     * client send Remote command by this methond
     *
     * @param command send to device
     * @return HDC Server response
     */
    protected String sendRemoteCommand(String command) {
        if (command.isEmpty()) {
            Hilog.error(TAG, "empty command");
            return Command.EMPTY_RESP;
        }
        return sendCommand(command);
    }

    private String sendCommand(String command) {
        boolean isNeedCallBack = false;
        if (command.startsWith(Command.BACK_FLAG)) {
            isNeedCallBack = true;
        }
        HdcResponse ohos = null;
        setHdcSocketChannel(mHdc);
        // we will get OHOS HDC
        ohos = getHeadResponse(mHdcConnection);
        if (ohos != null && ohos.okay) {
            Hilog.debug(TAG, "get ohos............");
            sendHeadRequest(mDevicesId);
        } else {
            Hilog.debug(TAG, "not get ohos............");
            return Command.VERIFY_ERROR_RESP;
        }
        // we will get OHOS HDC end
        if (mHdcConnection != null) {
            if (isNeedCallBack) {
                sendRequest(command.substring(Command.BACK_FLAG.length()));
            } else {
                sendRequest(command);
            }
        } else {
            Hilog.debug(TAG, "HdcConnection is null");
        }
        if (isNeedCallBack) {
            getIncomingDataAndSend(command.substring(Command.BACK_FLAG.length()));
            return Command.CALLBACK_RESP;
        } else {
            return getIncomingData();
        }
    }

    private void setHdcSocketChannel(HarmonyDebugConnector hdc) {
        try {
            mHdcConnection = SocketChannel.open(hdc.getSocketAddress());
            mHdcConnection.socket().setTcpNoDelay(true);
        } catch (IOException error) {
            Hilog.error(TAG, "openHdcConnection failed :" + error);
        }
    }

    private void sendRequest(String command) {
        try {
            ClientHelper.write(mHdcConnection, ChannelHandShake.getCommandByte(command));
        } catch (TimeoutException | IOException error) {
            Hilog.error(TAG, error);
        }
    }

    private HdcResponse getHeadResponse(SocketChannel channel) {
        HdcResponse resp = null;
        try {
            resp = ClientHelper.readHdcResponse(channel);
        } catch (TimeoutException | IOException error) {
            Hilog.error(TAG, error);
        }
        return resp;
    }

    private void sendHeadRequest(String connectKey) {
        try {
            ClientHelper.write(mHdcConnection, ChannelHandShake.getHeadData(connectKey));
        } catch (TimeoutException | IOException error) {
            Hilog.error(TAG, error);
        }
    }

    private String getIncomingData() {
        String result = "";
        String encoding = "ISO-8859-1";
        do {
            byte[] bufferSize = new byte[4];
            try {
                String size = ClientHelper.readServer(mHdcConnection, bufferSize);
                String temp = ClientHelper.readServer(mHdcConnection,
                        new byte[FormatUtil.asciiStringToInt(size.getBytes(encoding))]);
                result += temp;
                if (temp.isEmpty()) {
                    mHdcConnection.close();
                    break;
                }
            } catch (IOException error) {
                Hilog.error(TAG, error);
            }
        } while (true);
        return result.trim();
    }

    private void getIncomingDataAndSend(String command) {
        do {
            byte[] bufferSize = new byte[4];
            String encoding = "ISO-8859-1";
            try {
                String size = ClientHelper.readServer(mHdcConnection, bufferSize);
                String temp = ClientHelper.readServer(mHdcConnection,
                        new byte[FormatUtil.asciiStringToInt(size.getBytes(encoding))]);
                if (temp.isEmpty()) {
                    mHdcConnection.close();
                    break;
                }

                if (command.startsWith("shell")) {
                    mHdc.getShellResult(temp);
                } else if (command.startsWith("hilog")) {
                    mHdc.getHilogResult(temp);
                } else if (command.startsWith("file send")) {
                    mHdc.sendFileResult(temp);
                } else if (command.startsWith("file recv")) {
                    mHdc.recvFileResult(temp);
                } else {
                    Hilog.debug(TAG, "other command" + command);
                }
            } catch (IOException error) {
                Hilog.error(TAG, error);
            }
        } while (true);
    }
}