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

import java.io.UnsupportedEncodingException;

/**
 * hdc server format ¡¢create at 20210912
 */
class ChannelHandShake {
    private static final String DEFAULT_ENCODING = "ISO-8859-1";

    /**
     * Respond to server-side message,if connectKey is "",just get list target command
     * 4(length) + 12(command) + 32(device id)
     *
     * @param connectKey device ID
     * @return Send check byte stream
     * @throws UnsupportedEncodingException UnsupportedEncodingException when we get head data
     */
    public static byte[] getHeadData(String connectKey) throws UnsupportedEncodingException {
        String head = "OHOS HDC";
        byte[] headBuff = new byte[48];
        String nread = "" + (char) (headBuff.length - 4);
        System.arraycopy(nread.getBytes(DEFAULT_ENCODING), 0, headBuff, 3, 1);
        System.arraycopy(head.getBytes(DEFAULT_ENCODING), 0, headBuff, 4, head.length());
        System.arraycopy(connectKey.getBytes(DEFAULT_ENCODING), 0, headBuff, 16, connectKey.length());
        return headBuff;
    }

    /**
     * send command,we must send it after verify
     *
     * @param command we want to send
     * @return Send command byte stream
     * @throws UnsupportedEncodingException UnsupportedEncodingException when we get command data
     */
    public static byte[] getCommandByte(String command) throws UnsupportedEncodingException {
        byte[] buff = new byte[command.length() + 4 + 1];
        String nread = "" + (char) (command.length() + 1);
        System.arraycopy(nread.getBytes(DEFAULT_ENCODING), 0, buff, 3, 1);
        System.arraycopy(command.getBytes(DEFAULT_ENCODING), 0, buff, 4, command.length());
        return buff;
    }
}
