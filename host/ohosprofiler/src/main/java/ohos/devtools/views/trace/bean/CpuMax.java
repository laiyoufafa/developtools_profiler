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
 * CpuMax
 *
 * @since 2021/5/27 12:22
 */
public class CpuMax {
    @DField(name = "cpu")
    private Integer cpu;

    /**
     * Gets the value of cpu .
     *
     * @return the value of java.lang.Integer
     */
    public Integer getCpu() {
        return cpu;
    }

    /**
     * Sets the cpu .You can use getCpu() to get the value of cpu
     *
     * @param cpu cpu
     */
    public void setCpu(Integer cpu) {
        this.cpu = cpu;
    }
}
