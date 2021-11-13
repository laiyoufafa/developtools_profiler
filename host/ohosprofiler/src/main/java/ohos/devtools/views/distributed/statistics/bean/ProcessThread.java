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

package ohos.devtools.views.distributed.statistics.bean;

import ohos.devtools.views.trace.DField;
import org.apache.commons.lang.StringUtils;

/**
 * Process entity class
 *
 * @since 2021/8/19 16:39
 */
public class ProcessThread {
    @DField(name = "pid")
    private Integer pid;

    @DField(name = "process_name")
    private String processName;

    @DField(name = "thread_name")
    private String threadName;

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

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public String toString() {
        return "ProcessThread{" + "pid=" + pid + ", processName='" + (StringUtils.isEmpty(processName) ? "null"
                : processName) + '\'' + ", threadName='" + threadName + '\'' + '}';
    }
}
