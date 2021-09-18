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
 * Memory InstanceDetailsInfo
 */
public class MemoryInstanceDetailsInfo implements Serializable {
    private static final long serialVersionUID = -7886031529563053311L;

    /**
     * Id
     */
    private Integer id;

    /**
     * instanceId
     */
    private Integer instanceId;

    /**
     * frameId
     */
    private Integer frameId;

    /**
     * className
     */
    private String className;

    /**
     * methodName
     */
    private String methodName;

    /**
     * fieldName
     */
    private String fieldName;

    /**
     * lineNumber
     */
    private Integer lineNumber;

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

    public Integer getFrameId() {
        return frameId;
    }

    public void setFrameId(Integer frameId) {
        this.frameId = frameId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return "MemoryInstanceDetailsInfo{" + "frameId=" + frameId + ", className='" + className + '\''
            + ", methodName='" + methodName + '\'' + ", fieldName='" + fieldName + '\'' + ", lineNumber=" + lineNumber
            + '}';
    }
}
