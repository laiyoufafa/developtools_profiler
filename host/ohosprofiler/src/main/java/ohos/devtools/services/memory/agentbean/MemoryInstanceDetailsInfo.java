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
import java.util.Objects;

/**
 * call stack information
 *
 * @since 2021/5/19 16:39
 */
public class MemoryInstanceDetailsInfo implements Serializable {
    private static final long serialVersionUID = -7886031529563053311L;

    /**
     * 当前对象Id
     */
    private Integer id;

    /**
     * 调用栈对应的instanceId
     */
    private Integer instanceId;

    /**
     * 端侧获取的frameId
     */
    private Integer frameId;

    /**
     * 端侧获取的类名
     */
    private String className;

    /**
     * 端侧获取的方法名
     */
    private String methodName;

    /**
     * 端侧获取的属性名
     */
    private String fieldName;

    /**
     * lineNumber
     */
    private Integer lineNumber;

    /**
     * getId
     *
     * @return Integer Integer
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
     * getInstanceId
     *
     * @return Integer instanceId
     */
    public Integer getInstanceId() {
        return instanceId;
    }

    /**
     * setInstanceId
     *
     * @param instanceId instanceId
     */
    public void setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * getFrameId
     *
     * @return Integer frameId
     */
    public Integer getFrameId() {
        return frameId;
    }

    /**
     * setFrameId
     *
     * @param frameId frameId
     */
    public void setFrameId(Integer frameId) {
        this.frameId = frameId;
    }

    /**
     * getClassName
     *
     * @return String className
     */
    public String getClassName() {
        return className;
    }

    /**
     * setClassName
     *
     * @param className className
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * getMethodName
     *
     * @return String methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * setMethodName
     *
     * @param methodName methodName
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * getFieldName
     *
     * @return String fieldName
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * setFieldName
     *
     * @param fieldName fieldName
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * getLineNumber
     *
     * @return Integer lineNumber
     */
    public Integer getLineNumber() {
        return lineNumber;
    }

    /**
     * setLineNumber
     *
     * @param lineNumber lineNumber
     */
    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
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
        return "MemoryInstanceDetailsInfo{"
            + "id=" + id
            + ", instanceId=" + instanceId
            + ", frameId=" + frameId
            + ", className='" + className + '\''
            + ", methodName='" + methodName + '\''
            + ", fieldName='" + fieldName + '\''
            + ", lineNumber=" + lineNumber
            + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, instanceId, frameId, className, methodName, fieldName, lineNumber);
    }
}
