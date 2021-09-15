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
 * send Local Command to server ¡¢create at 20210912
 */
public class LocalCommand {
    private static String TAG = "LocalCommand";

    private HarmonyDebugConnector mHdc;
    private SocketChannel mHdcConnection;

    /**
     * init client send Local command by this methond
     *
     * @param Hdc with hdc bin
     * @param connectKey device id ,if it's empty,just local command can be send
     */
    protected LocalCommand(HarmonyDebugConnector hdc) {
        mHdc = hdc;
    }

    /**
     * client send Local command by this methond
     *
     * @param command send to device
     * @return HDC Server response
     */
    protected String sendLocalCommand(String command) {
        if (command.isEmpty()) {
            Hilog.e(TAG, "empty command");
            return Command.EMPTY_RESP;
        }
        return sendCommand(command);
    }

    private String sendCommand(String command) {
        HdcResponse ohos = null;
        mHdcConnection = getHdcSocketChannel(mHdcConnection, mHdc);
        // Hilog.e(TAG, "start connect hdc server : " + command);
        // we will get OHOS HDC
        ohos = getHeadResponse(mHdcConnection);
        if (ohos != null && ohos.okay) {
            Hilog.d(TAG, "get ohos............");
            sendHeadRequest("");
        } else {
            Hilog.d(TAG, "not get ohos............");
            return Command.VERIFY_ERROR_RESP;
        }
        // we will get OHOS HDC end
        if (mHdcConnection != null) {
            Hilog.d(TAG, "sendDeviceListMonitoringRequest");
            sendRequest(command);
        } else {
            Hilog.d(TAG, "HdcConnection is null");
        }
        return getIncomingData();
    }

    private void sendRequest(String command) {
        try {
            ClientHelper.write(mHdcConnection, ChannelHandShake.getCommandByte(command));
        } catch (TimeoutException | IOException error) {
            Hilog.e(TAG, error);
        }
    }

    private SocketChannel getHdcSocketChannel(SocketChannel channel, HarmonyDebugConnector hdc) {
        try {
            channel = SocketChannel.open(hdc.getSocketAddress());
            channel.socket().setTcpNoDelay(true);
        } catch (IOException error) {
            Hilog.e(TAG, "openHdcConnection failed :" + error);
        }
        return channel;
    }

    private HdcResponse getHeadResponse(SocketChannel channel) {
        HdcResponse resp = null;
        try {
            resp = ClientHelper.readHdcResponse(channel);
        } catch (TimeoutException | IOException error) {
            Hilog.e(TAG, error);
        }
        return resp;
    }

    private void sendHeadRequest(String connectKey) {
        try {
            ClientHelper.write(mHdcConnection, ChannelHandShake.getHeadData(connectKey));
        } catch (TimeoutException | IOException error) {
            Hilog.e(TAG, error);
        }
    }

    private String getIncomingData() {
        String result = "";
        do {
            byte[] bufferSize = new byte[4];
            try {
                String size = ClientHelper.readServer(mHdcConnection, bufferSize);
                String temp = ClientHelper.readServer(mHdcConnection,
                        new byte[FormatUtil.asciiStringToInt(size.getBytes())]);
                result += temp;
                if (temp.isEmpty()) {
                    mHdcConnection.close();
                    break;
                }
            } catch (IOException error) {
                Hilog.e(TAG, error);
            }
        } while (true);
        return result.trim();
    }
}
