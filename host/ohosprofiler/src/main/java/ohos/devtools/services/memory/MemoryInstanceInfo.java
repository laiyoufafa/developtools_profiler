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
 * 具体实例信息
 *
 * @version 1.0
 * @date 2021/03/29 21:22
 **/
public class MemoryInstanceInfo implements Serializable {
    private static final long serialVersionUID = -2702142952950557386L;
    /**
     * 当前实例对象Id
     */
    private Integer id;
    /**
     * 端侧获取的instanceId
     */
    private Integer instanceId;
    /**
     * 当前实例Instance对应的类Id
     */
    private Integer cId;
    /**
     * 实例名称
     */
    private String instance;
    /**
     * 当前Instance的创建时间
     */
    private Long allocTime;
    /**
     * 当前Instance的销毁时间
     */
    private Long deallocTime;
    /**
     * 当前Instance的入库时间
     */
    private Long createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
    }

    public Integer getcId() {
        return cId;
    }

    public void setcId(Integer cId) {
        this.cId = cId;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public Long getAllocTime() {
        return allocTime;
    }

    public void setAllocTime(Long allocTime) {
        this.allocTime = allocTime;
    }

    public Long getDeallocTime() {
        return deallocTime;
    }

    public void setDeallocTime(Long deallocTime) {
        this.deallocTime = deallocTime;
    }

    public Long getCreateTime() {
        return createTime;
    }

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
