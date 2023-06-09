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

package ohos.devtools.services.memory.agentdao;

import java.io.Serializable;
import java.util.Objects;

/**
 * MemoryUpdateInfo
 *
 * @since 2021/9/20
 */
public class MemoryUpdateInfo implements Serializable {
    private static final long serialVersionUID = 1679168310864454754L;

    private long updateTime;
    private long instanceId;

    /**
     * MemoryUpdateInfo
     *
     * @param updateTime updateTime
     * @param instanceId instanceId
     */
    public MemoryUpdateInfo(long updateTime, long instanceId) {
        this.updateTime = updateTime;
        this.instanceId = instanceId;
    }

    /**
     * get UpdateTime
     *
     * @return updateTime
     */
    public long getUpdateTime() {
        return updateTime;
    }

    /**
     * set UpdateTime
     *
     * @param updateTime updateTime
     */
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * get InstanceId
     *
     * @return instanceId
     */
    public long getInstanceId() {
        return instanceId;
    }

    /**
     * set InstanceId
     *
     * @param instanceId instanceId
     */
    public void setInstanceId(long instanceId) {
        this.instanceId = instanceId;
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
        return "MemoryUpdateInfo{"
            + "updateTime=" + updateTime
            + ", instanceId=" + instanceId
            + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(updateTime, instanceId);
    }
}
