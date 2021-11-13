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

package ohos.devtools.views.perftrace.bean;

import ohos.devtools.views.perftrace.PerfData;
import ohos.devtools.views.trace.DField;

import java.util.Objects;

/**
 * PrefSample from db file
 *
 * @since 2021/5/12 16:34
 */
public class PrefSample {
    /**
     * KERNEL path name
     */
    public static final String KERNEL = "[kernel.kallsyms]";
    private static final String DATA_APP_DIR = "/data/app";

    @DField(name = "id")
    private long id;
    @DField(name = "callchain_id")
    private long callChainId;
    @DField(name = "sample_id")
    private long sampleId;
    @DField(name = "thread_id")
    private long threadId;
    @DField(name = "timestamp")
    private long ts;
    @DField(name = "file_id")
    private long fileId;
    @DField(name = "symbol_id")
    private int symbolId;
    @DField(name = "vaddr_in_file")
    private long vaddrInFile;
    private String name;
    private boolean isUserWrite = false;

    /**
     * get the isUserWrite
     *
     * @return boolean isUserWrite
     */
    public boolean isUserWrite() {
        return isUserWrite;
    }

    /**
     * set the isUserWrite
     *
     * @param userWrite isUserWrite
     */
    public void setUserWrite(boolean userWrite) {
        isUserWrite = userWrite;
    }

    /**
     * set the name
     *
     * @return String name
     */
    public String getName() {
        return name;
    }

    /**
     * set the name by the file
     *
     * @param file file
     * @return PrefSample
     */
    public PrefSample setName(PrefFile file) {
        if (Objects.isNull(file)) {
            name = KERNEL + "+0x" + Long.toHexString(vaddrInFile);
        } else {
            if (file.getPath().startsWith(DATA_APP_DIR + PerfData.THREAD_NAMES.get((int) threadId))) {
                isUserWrite = true;
            }
            if (symbolId == -1) {
                name = file.getFileName() + "+0x" + Long.toHexString(vaddrInFile);
            } else {
                name = file.getSymbol();
            }
        }
        return this;
    }

    /**
     * get the sample isSameStack form other sample
     *
     * @param sample sample
     * @return isSameStack
     */
    public boolean isSameStack(PrefSample sample) {
        if (!(sample.getFileId() == this.getFileId() && sample.getSymbolId() == this.getSymbolId())) {
            return false;
        }
        if (this.getSymbolId() == -1) {
            return this.getVaddrInFile() == sample.getVaddrInFile();
        }
        return true;
    }

    /**
     * get the callChainId
     *
     * @return long callChainId
     */
    public long getCallChainId() {
        return callChainId;
    }

    /**
     * set the callChainId
     *
     * @param callChainId callChainId
     */
    public void setCallChainId(long callChainId) {
        this.callChainId = callChainId;
    }

    /**
     * get the sampleId
     *
     * @return long sampleId
     */
    public long getSampleId() {
        return sampleId;
    }

    /**
     * set the sampleId
     *
     * @param sampleId sampleId
     */
    public void setSampleId(long sampleId) {
        this.sampleId = sampleId;
    }

    /**
     * get the sampleId
     *
     * @return long threadId
     */
    public long getThreadId() {
        return threadId;
    }

    /**
     * set the sampleId
     *
     * @param threadId threadId
     */
    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    /**
     * get the ts
     *
     * @return long ts
     */
    public long getTs() {
        return ts;
    }

    /**
     * set the ts
     *
     * @param ts ts
     */
    public void setTs(long ts) {
        this.ts = ts;
    }

    /**
     * get the fileId
     *
     * @return long fileId
     */
    public long getFileId() {
        return fileId;
    }

    /**
     * set the fileId
     *
     * @param fileId fileId
     */
    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    /**
     * get the symbolId
     *
     * @return long symbolId
     */
    public int getSymbolId() {
        return symbolId;
    }

    /**
     * set the symbolId
     *
     * @param symbolId symbolId
     */
    public void setSymbolId(int symbolId) {
        this.symbolId = symbolId;
    }

    /**
     * get the vaddrInFile
     *
     * @return long vaddrInFile
     */
    public long getVaddrInFile() {
        return vaddrInFile;
    }

    /**
     * set the vaddrInFile
     *
     * @param vaddrInFile vaddrInFile
     */
    public void setVaddrInFile(long vaddrInFile) {
        this.vaddrInFile = vaddrInFile;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
