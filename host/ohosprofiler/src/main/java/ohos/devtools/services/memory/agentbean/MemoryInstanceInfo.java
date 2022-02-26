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

package ohos.devtools.services.memory.agentbean;

import java.io.Serializable;

/**
 * specific instance information
 *
 * @since 2021/5/19 16:39
 */
public class MemoryInstanceInfo implements Serializable {
    private static final long serialVersionUID = -2702142952950557386L;

    /**
     * id Of The Current Instance Object
     */
    private Integer id;

    /**
     * instanceId Obtained FromT he EndSide
     */
    private Integer instanceId;

    /**
     * Class Id corresponding to the current instance Instance
     */
    private Integer cId;

    /**
     * instance Name
     */
    private String instance;

    /**
     * creation time of the current instance
     */
    private Long allocTime;

    /**
     * Destruction Time Of The CurrentInstance
     */
    private Long deallocTime;

    /**
     * Storage Time Of The CurrentInstance
     */
    private Long createTime;

    /**
     * get Id
     *
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * setId
     *
     * @param id id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * getInstance Id
     *
     * @return instanceId
     */
    public Integer getInstanceId() {
        return instanceId;
    }

    /**
     * set InstanceId
     *
     * @param instanceId instanceId
     */
    public void setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * get class Id
     *
     * @return Integer
     */
    public Integer getcId() {
        return cId;
    }

    /**
     * set class Id
     *
     * @param cId cId
     */
    public void setcId(Integer cId) {
        this.cId = cId;
    }

    /**
     * get Instance
     *
     * @return instance
     */
    public String getInstance() {
        return instance;
    }

    /**
     * setInstance
     *
     * @param instance instance
     */
    public void setInstance(String instance) {
        this.instance = instance;
    }

    /**
     * getAllocTime
     *
     * @return allocTime
     */
    public Long getAllocTime() {
        return allocTime;
    }

    /**
     * setAllocTime
     *
     * @param allocTime allocTime
     */
    public void setAllocTime(Long allocTime) {
        this.allocTime = allocTime;
    }

    /**
     * getDeallocTime
     *
     * @return deallocTime
     */
    public Long getDeallocTime() {
        return deallocTime;
    }

    /**
     * setDeallocTime
     *
     * @param deallocTime deallocTime
     */
    public void setDeallocTime(Long deallocTime) {
        this.deallocTime = deallocTime;
    }

    /**
     * getCreateTime
     *
     * @return createTime
     */
    public Long getCreateTime() {
        return createTime;
    }

    /**
     * setCreateTime
     *
     * @param createTime createTime
     */
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "MemoryInstanceInfo{" + "id=" + id + ", instanceId=" + instanceId + ", cId=" + cId + ", instance='"
            + instance + '\'' + ", allocTime=" + allocTime + ", deallocTime=" + deallocTime + ", createTime="
            + createTime + '}';
    }
}
