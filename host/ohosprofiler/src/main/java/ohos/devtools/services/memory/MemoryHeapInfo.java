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

package ohos.devtools.services.memory;

import java.io.Serializable;

/**
 * 堆信息
 *
 * @version 1.0
 * @date 2021/03/29 21:20
 **/
public class MemoryHeapInfo implements Serializable {
    private static final long serialVersionUID = -4742624779850639424L;
    /**
     * 当前对象Id
     */
    private Integer id;
    /**
     * 端侧获取的classId
     */
    private Integer cId;
    /**
     * heapId: app、zygote、image、JNI
     */
    private Integer heapId;
    /**
     * 当前会话Id
     */
    private Long sessionId;
    private String arrangeStyle;
    private String className;
    /**
     * 有调用栈信息的创建的实例个数
     */
    private Integer allocations;
    /**
     * 销毁的实例个数
     */
    private Integer deallocations;
    /**
     * 堆内存中所有的实例的个数(对应端的array_length)
     */
    private Integer totalCount;
    /**
     * 堆内存中所有的实例的总大小(对应端的array_length*object_size)
     */
    private Long shallowSize;
    /**
     * createTime
     */
    private Long createTime;
    /**
     * 端侧获取的instanceId
     */
    private Integer instanceId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getcId() {
        return cId;
    }

    public void setcId(Integer cId) {
        this.cId = cId;
    }

    public Integer getHeapId() {
        return heapId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public void setHeapId(Integer heapId) {
        this.heapId = heapId;
    }

    public String getArrangeStyle() {
        return arrangeStyle;
    }

    public void setArrangeStyle(String arrangeStyle) {
        this.arrangeStyle = arrangeStyle;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getAllocations() {
        return allocations;
    }

    public void setAllocations(Integer allocations) {
        this.allocations = allocations;
    }

    public Integer getDeallocations() {
        return deallocations;
    }

    public void setDeallocations(Integer deallocations) {
        this.deallocations = deallocations;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Long getShallowSize() {
        return shallowSize;
    }

    public void setShallowSize(Long shallowSize) {
        this.shallowSize = shallowSize;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Integer getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public String toString() {
        return "MemoryHeapInfo{" + "id=" + id + ", cId=" + cId + ", heapId=" + heapId + ", sessionId=" + sessionId
            + ", arrangeStyle='" + arrangeStyle + '\'' + ", className='" + className + '\'' + ", allocations="
            + allocations + ", deallocations=" + deallocations + ", totalCount=" + totalCount + ", shallowSize="
            + shallowSize + ", createTime=" + createTime + '}';
    }
}
