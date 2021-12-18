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


package ohos.devtools.views.trace.bean;

import ohos.devtools.views.trace.DField;

/**
 * BinderArg
 *
 * @since 2021/6/29 10:55
 */
public class BinderArg {
    @DField(name = "argset")
    private Integer argset;
    @DField(name = "keyName")
    private String keyName;
    @DField(name = "id")
    private Integer id;
    @DField(name = "desc")
    private String desc;
    @DField(name = "strValue")
    private String strValue;

    /**
     * Get argset
     *
     * @return argset
     */
    public Integer getArgset() {
        return argset;
    }

    /**
     * set argset
     *
     * @param argset argset
     */
    public void setArgset(Integer argset) {
        this.argset = argset;
    }

    /**
     * Get keyName
     *
     * @return keyName
     */
    public String getKeyName() {
        return keyName;
    }

    /**
     * set keyName
     *
     * @param keyName keyName
     */
    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    /**
     * Get id
     *
     * @return id
     */
    public Integer getId() {
        return id;
    }

    /**
     * set id
     *
     * @param id id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Get desc
     *
     * @return desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * set desc
     *
     * @param desc desc
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * Get strValue
     *
     * @return strValue
     */
    public String getStrValue() {
        return strValue;
    }

    /**
     * set strValue
     *
     * @param strValue strValue
     */
    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }
}
