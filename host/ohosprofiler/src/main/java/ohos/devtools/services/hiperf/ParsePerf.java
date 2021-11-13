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
package ohos.devtools.services.hiperf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

/**
 * ParsePerf Parent Class.
 *
 * @since: 2021/9/20
 */
public abstract class ParsePerf {

    /**
     * insertSample to db
     */
    public abstract void insertSample();

    /**
     * parseFile parse perf.trace file
     *
     * @param file proto buf file
     * @throws IOException read Exception
     */
    public abstract void parseFile(File file) throws IOException;

    /**
     * read File to ByteBuffer
     *
     * @param file trace file
     * @return ByteBuffer
     * @throws IOException read Exception
     */
    protected ByteBuffer byteBufferFromFile(File file) throws IOException {
        try (FileInputStream dataFile = new FileInputStream(file)) {
            MappedByteBuffer buffer = dataFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            return buffer;
        }
    }

    /**
     * verify Head judge file is correct
     *
     * @param buffer ByteBuffer
     * @param head Verify Head
     */
    protected void verifyHead(ByteBuffer buffer, String head) {
        byte[] sign = new byte[head.length()];
        buffer.get(sign);
        if (!(new String(sign, StandardCharsets.UTF_8)).equals(head)) {
            throw new IllegalStateException("perf trace could not be parsed due to magic number mismatch.");
        }
    }
}
