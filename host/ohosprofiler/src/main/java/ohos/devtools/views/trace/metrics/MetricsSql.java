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

package ohos.devtools.views.trace.metrics;

/**
 * sql text file path
 *
 * @since 2021/9/16
 */
public enum MetricsSql {
    DISTRIBUTED_TERM("distributed_term"),
    TRACE_CPU("trace_cpu"),
    TRACE_CPU_TOP10("trace_cpu_top10"),
    TRACE_MEM("trace_mem"),
    TRACE_MEM_TOP10("trace_mem_top10"),
    TRACE_MEM_UNAGG("trace_mem_unagg"),
    TRACE_TASK_NAMES("trace_task_names"),
    TRACE_STATS("trace_stats"),
    TRACE_METADATA("trace_metadata"),
    SYS_CALLS("sys_calls"),
    SYS_CALLS_TOP10("sys_calls_top10");

    /**
     * get name
     *
     * @return name name
     */
    public String getName() {
        return name;
    }

    private final String name;

    MetricsSql(String name) {
        this.name = name;
    }
}
