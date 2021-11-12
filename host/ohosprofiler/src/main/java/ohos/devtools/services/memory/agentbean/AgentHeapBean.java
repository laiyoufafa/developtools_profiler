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

import java.util.Objects;

/**
 * Agent Heap Bean
 */
public class AgentHeapBean {
    private Integer agentNodeId;
    private Integer agentClazzId;
    private Integer agentHeapId;
    private Long sessionId;
    private String agentClazzName;
    private Integer agentAllocationsCount;
    private Integer agentDeAllocationsCount;
    private Integer agentTotalInstanceCount;
    private Long agentTotalshallowSize;

    public Integer getAgentNodeId() {
        return agentNodeId;
    }

    public void setAgentNodeId(Integer agentNodeId) {
        this.agentNodeId = agentNodeId;
    }

    public Integer getAgentClazzId() {
        return agentClazzId;
    }

    public void setAgentClazzId(Integer agentClazzId) {
        this.agentClazzId = agentClazzId;
    }

    public Integer getAgentHeapId() {
        return agentHeapId;
    }

    public void setAgentHeapId(Integer agentHeapId) {
        this.agentHeapId = agentHeapId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getAgentClazzName() {
        return agentClazzName;
    }

    public void setAgentClazzName(String agentClazzName) {
        this.agentClazzName = agentClazzName;
    }

    public Integer getAgentAllocationsCount() {
        return agentAllocationsCount;
    }

    public void setAgentAllocationsCount(Integer agentAllocationsCount) {
        this.agentAllocationsCount = agentAllocationsCount;
    }

    public Integer getAgentDeAllocationsCount() {
        return agentDeAllocationsCount;
    }

    public void setAgentDeAllocationsCount(Integer agentDeAllocationsCount) {
        this.agentDeAllocationsCount = agentDeAllocationsCount;
    }

    public Integer getAgentTotalInstanceCount() {
        return agentTotalInstanceCount;
    }

    public void setAgentTotalInstanceCount(Integer agentTotalInstanceCount) {
        this.agentTotalInstanceCount = agentTotalInstanceCount;
    }

    public Long getAgentTotalshallowSize() {
        return agentTotalshallowSize;
    }

    public void setAgentTotalshallowSize(Long agentTotalshallowSize) {
        this.agentTotalshallowSize = agentTotalshallowSize;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        AgentHeapBean agentHeapBean = null;
        if (object instanceof AgentHeapBean) {
            agentHeapBean = (AgentHeapBean) object;
        }
        return Objects.equals(agentNodeId, agentHeapBean.agentNodeId) && Objects
            .equals(agentClazzId, agentHeapBean.agentClazzId) && Objects.equals(agentHeapId, agentHeapBean.agentHeapId)
            && Objects.equals(sessionId, agentHeapBean.sessionId) && Objects
            .equals(agentClazzName, agentHeapBean.agentClazzName) && Objects
            .equals(agentAllocationsCount, agentHeapBean.agentAllocationsCount) && Objects
            .equals(agentDeAllocationsCount, agentHeapBean.agentDeAllocationsCount) && Objects
            .equals(agentTotalInstanceCount, agentHeapBean.agentTotalInstanceCount) && Objects
            .equals(agentTotalshallowSize, agentHeapBean.agentTotalshallowSize);
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(agentNodeId, agentClazzId, agentHeapId, sessionId, agentClazzName, agentAllocationsCount,
                agentDeAllocationsCount, agentTotalInstanceCount, agentTotalshallowSize);
    }

    @Override
    public String toString() {
        return agentClazzName;
    }
}
