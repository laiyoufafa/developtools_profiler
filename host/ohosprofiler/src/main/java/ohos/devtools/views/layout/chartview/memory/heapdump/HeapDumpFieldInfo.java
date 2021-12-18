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

/**
 * MemoryHeapDumpFieldInfo
 *
 * @since 2021/11/22
 */
public class HeapDumpFieldInfo extends HeapDumpThirdInfo {
    private Integer id;
    private String fieldName;
    private int fieldType;
    private String fieldValue;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * getInstanceId
     *
     * @return long
     */
    public long getInstanceId() {
        return instanceId;
    }

    /**
     * setInstanceId
     *
     * @param instanceId instanceId
     */
    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public int getFieldType() {
        return fieldType;
    }

    public void setFieldType(Integer fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    /**
     * FieldTypeValue
     */
    public enum FieldTypeValue {
        OBJECT(1), CLASS(1), ARRAY(2), STRING(3), DEFAULT(4);

        private Integer fieldType;

        FieldTypeValue(Integer fieldType) {
            this.fieldType = fieldType;
        }

        public Integer getFieldType() {
            return fieldType;
        }
    }

    @Override
    public String toString() {
        if (fieldName.contains("click to see next")) {
            return fieldName;
        }
        return fieldName + " = " + fieldValue;
    }
}
