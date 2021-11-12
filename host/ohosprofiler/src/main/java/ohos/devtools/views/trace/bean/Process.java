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
 * Process entity class
 *
 * @since 2021/04/22 12:25
 */
public class Process {
    @DField(name = "pid")
    private Integer pid;

    @DField(name = "processName")
    private String name;

    /**
     * Gets the value of pid .
     *
     * @return the value of int
     */
    public Integer getPid() {
        return pid;
    }

    /**
     * Sets the pid .
     * <p>You can use getPid() to get the value of pid</p>
     *
     * @param id id
     */
    public void setPid(final Integer id) {
        this.pid = id;
    }

    /**
     * Gets the value of name .
     *
     * @return the value of java.lang.String
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name .
     * <p>You can use getName() to get the value of name</p>
     *
     * @param name name
     */
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return pid + "-" + name;
    }
}
