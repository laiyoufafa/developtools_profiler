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

package ohos.devtools.views.distributed.statistics;

import ohos.devtools.views.distributed.statistics.bean.ProcessThread;
import ohos.devtools.views.distributed.util.DistributedDB;
import ohos.devtools.views.trace.Sql;
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
     * @param device device
     * @return String String
     */
    @Override
    public String getQueryResult(String device) {
        List<ProcessThread> processThreads = new ArrayList<>() {
        };
        if (device.equalsIgnoreCase("A")) {
            DistributedDB.getInstance().queryA(Sql.DISTRIBUTED_TRACE_TASK_NAMES, processThreads);
        } else {
            DistributedDB.getInstance().queryB(Sql.DISTRIBUTED_TRACE_TASK_NAMES, processThreads);
        }
        StringBuilder builder = new StringBuilder();
        builder.append("trace_task_names: {").append(System.lineSeparator());
        for (ProcessThread item : processThreads) {
            builder.append("  process: {").append(System.lineSeparator());
            builder.append("    pid: ").append(item.getPid()).append(System.lineSeparator());
            builder.append("    process_name: ")
                .append(StringUtils.isEmpty(item.getProcessName()) ? null : item.getProcessName())
                .append(System.lineSeparator());
            String[] threadNames = item.getThreadName().split(SEPARATOR);
            for (String str : threadNames) {
                builder.append("    thread_name: ").append("\"").append(str).append("\"")
                    .append(System.lineSeparator());
            }
            builder.append("  }").append(System.lineSeparator());
        }
        builder.append("}");
        return builder.toString();
    }
}
