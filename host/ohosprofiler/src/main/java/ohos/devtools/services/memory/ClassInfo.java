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
 * 类对象实体
 *
 * @version 1.0
 * @date 2021/04/03 10:58
 **/
public class ClassInfo implements Serializable {
    private static final long serialVersionUID = -3958115376721507302L;
    /**
     * 当前对象Id
     */
    private Integer id;
    /**
     * 端侧获取的classId
     */
    private Integer cId;
    /**
     * 类名
     */
    private String className;

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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return "ClassInfo{" + "id=" + id + ", cId=" + cId + ", className='" + className + '\'' + '}';
    }
}
