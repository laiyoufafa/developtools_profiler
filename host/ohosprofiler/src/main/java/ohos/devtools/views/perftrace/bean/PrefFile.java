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

import ohos.devtools.views.trace.DField;

import java.util.Objects;

/**
 * PrefFile
 *
 * @since 2021/5/12 16:34
 */
public class PrefFile {
    @DField(name = "file_id")
    private long fileId;
    @DField(name = "symbol")
    private String symbol;
    @DField(name = "path")
    private String path;

    private String fileName = "";

    /**
     * get the fileName
     *
     * @return String fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * get the fileId
     *
     * @return String fileId
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
     * get the symbol
     *
     * @return symbol symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * set the symbol
     *
     * @param symbol symbol
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * get the symbol
     *
     * @return path path
     */
    public String getPath() {
        return path;
    }

    /**
     * use path trans to last .so file name
     *
     * @param path path of file
     */
    public void setPath(String path) {
        this.path = path;
        if (Objects.nonNull(path) && !path.isEmpty()) {
            int lastSplitIndex = path.lastIndexOf("/");
            if (lastSplitIndex > 0) {
                this.fileName = path.substring(lastSplitIndex + 1);
                return;
            }
        }
        this.fileName = path;
    }
}
