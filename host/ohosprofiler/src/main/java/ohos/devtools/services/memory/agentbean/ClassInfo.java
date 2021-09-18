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
 * class object entity
 */
public class ClassInfo implements Serializable {
    private static final long serialVersionUID = -3958115376721507302L;

    /**
     * current object id
     */
    private Integer id;

    /**
     * class id obtained from the end side
     */
    private Integer cId;

    /**
     * ClassName
     */
    private String className;

    /**
     * get Id
     *
     * @return Integer
     */
    public Integer getId() {
        return id;
    }

    /**
     * set Id
     *
     * @param id id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * get cId
     *
     * @return cId
     */
    public Integer getcId() {
        return cId;
    }

    /**
     * set cId
     *
     * @param cId cId
     */
    public void setcId(Integer cId) {
        this.cId = cId;
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

    @Override
    public String toString() {
        return "ClassInfo{" + "id=" + id + ", cId=" + cId + ", className='" + className + '\'' + '}';
    }
}
