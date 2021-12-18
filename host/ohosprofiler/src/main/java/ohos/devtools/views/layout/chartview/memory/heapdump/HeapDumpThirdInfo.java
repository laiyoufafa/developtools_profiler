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

package ohos.devtools.views.layout.chartview.memory.heapdump;

import java.util.Objects;

/**
 * HeapDumpThirdInfo
 *
 * @since 2021/11/22
 */
public class HeapDumpThirdInfo {
    /**
     * native Size
     */
    protected long nativeSize;

    /**
     * shallow Size
     */
    protected long shallowSize;

    /**
     * retained Size
     */
    protected long retainedSize;

    /**
     * instanceId
     */
    protected long instanceId;

    /**
     * isObj
     */
    protected boolean isObj;

    /**
     * depth
     */
    protected Integer depth;

    public long getNativeSize() {
        return nativeSize;
    }

    public void setNativeSize(long nativeSize) {
        this.nativeSize = nativeSize;
    }

    public long getShallowSize() {
        return shallowSize;
    }

    public void setShallowSize(long shallowSize) {
        this.shallowSize = shallowSize;
    }

    public long getRetainedSize() {
        return retainedSize;
    }

    public void setRetainedSize(long retainedSize) {
        this.retainedSize = retainedSize;
    }

    public long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
    }

    public boolean isObj() {
        return isObj;
    }

    public void setObj(boolean obj) {
        isObj = obj;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public Integer getDepth() {
        return depth;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nativeSize, shallowSize, retainedSize, instanceId, isObj, depth);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "HeapDumpThirdInfo{"
            + "nativeSize="
            + nativeSize
            + ", shallowSize="
            + shallowSize
            + ", retainedSize="
            + retainedSize
            + ", instanceId="
            + instanceId
            + ", isObj="
            + isObj
            + ", depth="
            + depth
            + '}';
    }
}
