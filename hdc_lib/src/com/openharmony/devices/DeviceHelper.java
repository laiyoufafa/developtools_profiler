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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeoutException;

import com.openharmony.hdc.Hilog;

/**
 * connect and read write with hdc server ¡¢create at 20210912
 */
final class DeviceHelper {
    private static final String TAG = "HdcHelper";
    private static final String DEFAULT_ENCODING = "ISO-8859-1";
    private static final int WAIT_TIME = 5;
    private static final int DEFAULT_TIMEOUT = 5000; // standard delay, in ms

    /**
     * write byte to channel
     *
     * @param chan SocketChannel
     * @param data byte we send
     * @throws TimeoutException timeout error
     * @throws IOException IO error
     */
    public static void write(SocketChannel chan, byte[] data) throws TimeoutException, IOException {
        write(chan, data, -1, DEFAULT_TIMEOUT);
    }

    private static void write(SocketChannel chan, byte[] data, int length, int timeout)
            throws TimeoutException, IOException {
        ByteBuffer buf = ByteBuffer.wrap(data, 0, length != -1 ? length : data.length);
        int numWaits = 0;

        while (buf.position() != buf.limit()) {
            int count;
            count = chan.write(buf);
            if (count < 0) {
                Hilog.debug("HdcHelper", "write: channel EOF");
            } else if (count == 0) {
                if (timeout != 0 && timeout < numWaits * WAIT_TIME) {
                    Hilog.error(TAG, "write error");
                    throw new TimeoutException();
                }
                // non-blocking spin
                try {
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException error) {
                    Hilog.error(TAG, "write error");
                }
                numWaits++;
            } else {
                numWaits = 0;
            }
        }
    }

    static void read(SocketChannel chan, byte[] data) throws TimeoutException, IOException {
        read(chan, data, -1, DEFAULT_TIMEOUT);
    }

    private static void read(SocketChannel chan, byte[] data, int length, long timeout)
            throws TimeoutException, IOException {
        ByteBuffer buf = ByteBuffer.wrap(data, 0, length != -1 ? length : data.length);
        int numWaits = 0;
        while (buf.position() != buf.limit()) {
            int count = chan.read(buf);
            if (count < 0) {
                throw new IOException("EOF");
            } else if (count == 0) {
                if (timeout != 0 && timeout < numWaits * WAIT_TIME) {
                    throw new TimeoutException();
                }
                try {
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException error) {
                    Hilog.error(TAG, error);
                }
                numWaits++;
            } else {
                numWaits = 0;
            }
        }
    }

    /**
     * read byte from SocketChannel
     *
     * @param socket SocketChannel
     * @param buffer byte we want read
     * @return what's we get byte and format it to string
     * @throws IOException IO error
     */
    public static String readServer(SocketChannel socket, byte[] buffer) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(buffer, 0, buffer.length);
        while (buf.position() != buf.limit()) {
            Hilog.debug(TAG, "read Incoming Device EOF");
            int count;
            count = socket.read(buf);
            if (count < 0) {
                Hilog.debug(TAG, "read Incoming Device EOF");
                break;
            }
        }

        try {
            return new String(buffer, 0, buf.position(), DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException error) {
            Hilog.error(TAG, "read Incoming Device Data error : " + error);
        }
        return "";
    }

    /**
     * read verify code OHOS HDC
     *
     * @param chan readHdcResponse
     * @return server response
     * @throws TimeoutException time error
     * @throws IOException IO error
     */
    public static HdcResponse readHdcResponse(SocketChannel chan)
            throws TimeoutException, IOException {
        HdcResponse resp = new HdcResponse();
        byte[] reply = new byte[48]; // ChannelHandShake is 48
        read(chan, reply);
        if (isOHOS(reply)) {
            resp.okay = true; // we get "OHOS HDC"
        } else {
            resp.okay = false;
        }
        return resp;
    }

    private static boolean isOHOS(byte[] reply) {
        return reply[4] == (byte) 'O' && reply[5] == (byte) 'H' && reply[6] == (byte) 'O' && reply[7] == (byte) 'S'
                && reply[9] == (byte) 'H' && reply[10] == (byte) 'D' && reply[11] == (byte) 'C';
    }

    /**
     * ChannelHandShake
     */
    public static class HdcResponse {
        /**
         * server Hdc Response
         */
        HdcResponse() {
        }

        /**
         * check server OHOS
         */
        public boolean okay; // first 4 bytes in response were "OHOS HDC"?
    }
}
