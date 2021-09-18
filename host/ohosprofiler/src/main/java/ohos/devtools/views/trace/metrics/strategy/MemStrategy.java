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
import ohos.devtools.views.trace.metrics.bean.Memory;

import java.util.ArrayList;
import java.util.List;

/**
 * Memory Strategy
 */
public class MemStrategy implements Strategy {
    /**
     * Query Memory Result
     *
     * @param sql sql
     * @return string string
     */
    @Override
    public String getQueryResult(MetricsSql sql) {
        List<Memory> list = new ArrayList<>() {
        };
        if ("trace_mem_top10".equals(sql.getName())) {
            MetricsDb.getInstance().query(MetricsSql.TRACE_MEM, list);
        } else {
            MetricsDb.getInstance().query(sql, list);
        }
        List newList;
        if ("trace_mem_top10".equals(sql.getName()) && list.size() >= 10) {
            newList = list.subList(0, 10);
        } else {
            newList = list;
        }
        return handleMemoryInfo(newList);
    }

    private String handleMemoryInfo(List<Memory> result) {
        StringBuilder builder = new StringBuilder();
        if (result.size() == 10) {
            builder.append("trace_mem_top10 {").append(System.lineSeparator());
        } else {
            builder.append("trace_mem {").append(System.lineSeparator());
        }
        for (Memory item : result) {
            builder.append("  process_metrics {").append(System.lineSeparator());
            builder.append("    process_name: \"").append(item.getProcessName()).append("\"")
                .append(System.lineSeparator());
            builder.append("    overall_counters {").append(System.lineSeparator());
            builder.append("      anon_rss {").append(System.lineSeparator());
            builder.append("        min: ").append(item.getMinNum()).append(System.lineSeparator());
            builder.append("        max: ").append(item.getMaxNum()).append(System.lineSeparator());
            builder.append("        avg: ").append(item.getAvgNum()).append(System.lineSeparator());
            builder.append("      }").append(System.lineSeparator());
            builder.append("    }").append(System.lineSeparator());
            builder.append("  }").append(System.lineSeparator());
        }
        builder.append("}").append(System.lineSeparator());
        return builder.toString();
    }
}
