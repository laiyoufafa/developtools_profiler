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

package ohos.devtools.views.common.chart.treetable;

import java.util.ArrayList;
import java.util.Objects;

/**
 * DataNode
 *
 * @version 1.0
 * @date 2021/03/15 10:07
 **/
public class DataNode {
    private Integer id;
    private Integer cId;
    private Integer heapId;
    private Long sessionId;
    private String className;
    private Integer allocations;
    private Integer deallocations;
    private Integer totalCount;
    private Long shallowSize;
    ArrayList<DataNode> children;

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

    public void setHeapId(Integer heapId) {
        this.heapId = heapId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
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

    public ArrayList<DataNode> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<DataNode> children) {
        this.children = children;
    }

    /**
     * addChildren
     *
     * @param children children
     */
    public void addChildren(DataNode children) {
        if (this.children == null) {
            this.children = new ArrayList<DataNode>();
        }
        this.children.add(children);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        DataNode dataNode = null;
        if (object instanceof DataNode) {
            dataNode = (DataNode) object;
        }
        return Objects.equals(id, dataNode.id) && Objects.equals(cId, dataNode.cId) && Objects
            .equals(heapId, dataNode.heapId) && Objects.equals(sessionId, dataNode.sessionId) && Objects
            .equals(className, dataNode.className) && Objects.equals(allocations, dataNode.allocations) && Objects
            .equals(deallocations, dataNode.deallocations) && Objects.equals(totalCount, dataNode.totalCount) && Objects
            .equals(shallowSize, dataNode.shallowSize) && Objects.equals(children, dataNode.children);
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(id, cId, heapId, sessionId, className, allocations, deallocations, totalCount, shallowSize, children);
    }

    /**
     * toStr
     *
     * @return String
     */
    public String toStr() {
        return className + '，' + allocations + '，' + deallocations + '，' + totalCount + '，' + shallowSize;
    }

    @Override
    public String toString() {
        return className;
    }
}
