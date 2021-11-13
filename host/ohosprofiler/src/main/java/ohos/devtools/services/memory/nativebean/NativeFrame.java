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

package ohos.devtools.services.memory.nativebean;

import java.util.Objects;

/**
 * NativeFrame
 *
 * @since 2021/9/20
 */
public class NativeFrame {
    private String pc;
    private String fileName;
    private String functionName;

    public NativeFrame(String pc, String fileName, String functionName) {
        this.pc = pc;
        this.fileName = fileName;
        this.functionName = functionName;
    }

    public String getPc() {
        return pc;
    }

    public void setPc(String pc) {
        this.pc = pc;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "NativeFrame{"
            + "pc='" + pc + '\''
            + ", fileName='" + fileName + '\''
            + ", functionName='" + functionName + '\''
            + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(pc, fileName, functionName);
    }
}
