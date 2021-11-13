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

package ohos.devtools.datasources.transport.hdc;

import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * IoStreamConsumer
 *
 * @since 2021/9/20
 */
public class IoStreamConsumer extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(IoStreamConsumer.class);
    private InputStream inputStream;
    private ArrayList<String> resultList;
    private volatile boolean isStopped;

    /**
     * IoStreamConsumer
     *
     * @param inputStream inputStream
     */
    public IoStreamConsumer(InputStream inputStream) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("IoStreamConsumer");
        }
        super.setName("IoStreamConsumer");
        this.inputStream = inputStream;
        this.resultList = new ArrayList();
        this.isStopped = false;
    }

    @Override
    public void run() {
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "GBK");
            bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                resultList.add(line);
                if (line.contains("Empty")) {
                    return;
                }
            }
        } catch (IOException ioException) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("IOException ", ioException);
            }
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                    inputStream = null;
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException ioException) {
                if (ProfilerLogManager.isErrorEnabled()) {
                    LOGGER.error("IOException ", ioException);
                }
            }
            this.isStopped = true;
        }
    }

    /**
     * getContent
     *
     * @return ArrayList<String>
     */
    public ArrayList<String> getContent() {
        return this.resultList;
    }
}
