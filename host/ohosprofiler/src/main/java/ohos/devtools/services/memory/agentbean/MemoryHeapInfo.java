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
 * Memory Heap Info
 */
public class MemoryHeapInfo implements Serializable {
    private static final long serialVersionUID = -4742624779850639424L;

    /**
     * heapId
     */
    private Integer id;

    /**
     * class Id
     */
    private Integer cId;

    /**
     * heapId: app、zygote、image、JNI
     */
    private Integer heapId;

    /**
     * this session Id
     */
    private Long sessionId;

    /**
     * class Name
     */
    private String className;

    /**
     * Number of instances created with call stack information
     */
    private Integer allocations;

    /**
     * number Of Instances Destroyed
     */
    private Integer deallocations;

    /**
     * The number of all instances in the heap memory (array length of the corresponding end)
     */
    private Integer totalCount;

    /**
     * The total size of all instances in the heap memory (array lengthobject size at the corresponding end)
     */
    private Long shallowSize;
    /**
     * createTime
     */
    private Long createTime;

    /**
     * instance Id Obtained From The EndSide
     */
    private Integer instanceId;

    /**
     * updateTime
     */
    private long updateTime;

    /**
     * get instance Id
     *
     * @return Integer id
     */
    public Integer getId() {
        return id;
    }

    /**
     * set instance Id
     *
     * @param id id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * get class Id
     *
     * @return Integer cId
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
     * get Heap Id
     *
     * @return Integer heapId
     */
    public Integer getHeapId() {
        return heapId;
    }

    /**
     * get Session Id
     *
     * @return Long sessionId
     */
    public Long getSessionId() {
        return sessionId;
    }

    /**
     * set SessionId
     *
     * @param sessionId sessionId
     */
    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * set HeapId
     *
     * @param heapId heapId
     */
    public void setHeapId(Integer heapId) {
        this.heapId = heapId;
    }

    /**
     * get ClassName
     *
     * @return String
     */
    public String getClassName() {
        return className;
    }

    /**
     * set ClassName
     *
     * @param className className
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * get Allocations
     *
     * @return allocations
     */
    public Integer getAllocations() {
        return allocations;
    }

    /**
     * set Allocations
     *
     * @param allocations allocations
     */
    public void setAllocations(Integer allocations) {
        this.allocations = allocations;
    }

    /**
     * get Deallocations
     *
     * @return deallocations
     */
    public Integer getDeallocations() {
        return deallocations;
    }

    /**
     * set Deallocations
     *
     * @param deallocations deallocations
     */
    public void setDeallocations(Integer deallocations) {
        this.deallocations = deallocations;
    }

    /**
     * get TotalCount
     *
     * @return totalCount
     */
    public Integer getTotalCount() {
        return totalCount;
    }

    /**
     * set TotalCount
     *
     * @param totalCount totalCount
     */
    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    /**
     * get ShallowSize
     *
     * @return shallowSize
     */
    public Long getShallowSize() {
        return shallowSize;
    }

    /**
     * set ShallowSize
     *
     * @param shallowSize shallowSize
     */
    public void setShallowSize(Long shallowSize) {
        this.shallowSize = shallowSize;
    }

    /**
     * get CreateTime
     *
     * @return createTime
     */
    public Long getCreateTime() {
        return createTime;
    }

    /**
     * set CreateTime
     *
     * @param createTime createTime
     */
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    /**
     * get InstanceId
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
}
