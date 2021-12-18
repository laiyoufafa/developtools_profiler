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

import ohos.devtools.services.memory.nativebean.NativeFrame;

import java.util.ArrayList;
import java.util.Objects;

/**
 * InstanceObject
 *
 * @since 2021/8/21
 */
public class NativeInstanceObject {
    private String addr;
    private long size;
    private long instanceCount;
    private boolean isAdd;
    private String fileName;
    private String functionName;
    private ArrayList<NativeFrame> nativeFrames = new ArrayList<>();
    private boolean isDeAllocated = false;

    public int getInstanceCount() {
        return (int) instanceCount;
    }

    public ArrayList<NativeFrame> getNativeFrames() {
        return nativeFrames;
    }

    public String getFunctionName() {
        return functionName;
    }

    public long getAllowSize() {
        return size;
    }

    public boolean isDeAllocated() {
        return isDeAllocated;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    /**
     * add Native Frames
     *
     * @param nativeFrame nativeFrame
     */
    public void addNativeFrames(NativeFrame nativeFrame) {
        nativeFrames.add(nativeFrame);
    }

    public void setInstanceCount(long instanceCount) {
        this.instanceCount = instanceCount;
    }

    public void setDeAllocated(boolean deAllocated) {
        isDeAllocated = deAllocated;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isAdd() {
        return isAdd;
    }

    public void setAdd(boolean add) {
        isAdd = add;
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
        return "NativeInstanceObject{"
            + "addr='" + addr + '\''
            + ", size=" + size
            + ", instanceCount=" + instanceCount
            + ", isAdd=" + isAdd
            + ", fileName='" + fileName + '\''
            + ", functionName='" + functionName + '\''
            + ", nativeFrames=" + nativeFrames
            + ", isDeAllocated=" + isDeAllocated
            + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(addr, size, instanceCount, isAdd, fileName, functionName, nativeFrames, isDeAllocated);
    }
}
