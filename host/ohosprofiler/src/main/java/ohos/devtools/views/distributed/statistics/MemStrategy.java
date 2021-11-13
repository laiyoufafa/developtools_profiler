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

import ohos.devtools.views.distributed.statistics.bean.Memory;
import ohos.devtools.views.distributed.util.DistributedDB;
import ohos.devtools.views.trace.Sql;

import java.util.ArrayList;
import java.util.List;

/**
 * MMemory Strategy
 *
 * @since 2021/5/19 16:39
 */
public class MemStrategy implements Strategy {
    /**
     * Query Memory Result
     *
     * @param device device
     * @return string string
     */
    @Override
    public String getQueryResult(String device) {
        List<Memory> list = new ArrayList<>() {
        };
        if (device.equalsIgnoreCase("A")) {
            DistributedDB.getInstance().queryA(Sql.DISTRIBUTED_TRACE_MEM, list);
        } else {
            DistributedDB.getInstance().queryB(Sql.DISTRIBUTED_TRACE_MEM, list);
        }
        return handleMemoryInfo(list);
    }

    private String handleMemoryInfo(List<Memory> result) {
        StringBuilder builder = new StringBuilder();
        builder.append("trace_mem {").append(System.lineSeparator());
        for (Memory item : result) {
            builder.append("  process_metrics {").append(System.lineSeparator());
            builder.append("    process_name: \"").append(item.getProcessName()).append("\"")
                .append(System.lineSeparator());
            builder.append("    overall_counters {").append(System.lineSeparator());
            builder.append("      anon_rss {").append(System.lineSeparator());
            builder.append("        min: ").append(item.getMinNum()).append(System.lineSeparator());
            builder.append("        max: ").append(item.getMaxNum()).append(System.lineSeparator());
            if (item.getMaxNum() == item.getMaxNum()) {
                builder.append("        avg: ").append(item.getMinNum()).append(System.lineSeparator());
            } else {
                builder.append("        avg: ").append((item.getMinNum() + item.getMaxNum()) / 2)
                    .append(System.lineSeparator());
            }
            builder.append("      }").append(System.lineSeparator());
            builder.append("    }").append(System.lineSeparator());
            builder.append("  }").append(System.lineSeparator());
        }
        builder.append("}").append(System.lineSeparator());
        return builder.toString();
    }
}
