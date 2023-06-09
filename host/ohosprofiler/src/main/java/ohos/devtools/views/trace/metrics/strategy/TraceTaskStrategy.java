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

package ohos.devtools.views.trace.metrics.strategy;

import ohos.devtools.views.trace.metrics.MetricsDb;
import ohos.devtools.views.trace.metrics.MetricsSql;
import ohos.devtools.views.trace.metrics.bean.ProcessThread;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * trace task Strategy
 *
 * @since 2021/5/19 16:39
 */
public class TraceTaskStrategy implements Strategy {
    /**
     * Separator
     */
    private static final String SEPARATOR = ",";

    /**
     * get Query trace task Result
     *
     * @param sql sql
     * @return String String
     */
    @Override
    public String getQueryResult(MetricsSql sql) {
        List<ProcessThread> processThreads = new ArrayList<>() {
        };
        MetricsDb.getInstance().query(sql, processThreads);
        StringBuilder builder = new StringBuilder();
        builder.append("trace_task_names: {").append(System.lineSeparator());
        for (ProcessThread item : processThreads) {
            builder.append("  process: {").append(System.lineSeparator());
            builder.append("    pid: ").append(item.getPid()).append(System.lineSeparator());
            builder.append("    process_name: ")
                .append(StringUtils.isEmpty(item.getProcessName()) ? null : item.getProcessName())
                .append(System.lineSeparator());
            if (StringUtils.isEmpty(item.getThreadName())) {
                builder.append("    thread_name: ").append((String) null).append(System.lineSeparator());
            } else {
                String[] threadNames = item.getThreadName().split(SEPARATOR);
                for (String str : threadNames) {
                    builder.append("    thread_name: ").append("\"").append(str).append("\"")
                        .append(System.lineSeparator());
                }
            }
            builder.append("  }").append(System.lineSeparator());
        }
        builder.append("}");
        return builder.toString();
    }
}
