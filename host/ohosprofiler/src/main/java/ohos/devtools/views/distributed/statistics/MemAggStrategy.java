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

import ohos.devtools.views.distributed.statistics.bean.MemAgg;
import ohos.devtools.views.distributed.util.DistributedDB;
import ohos.devtools.views.trace.Sql;

import java.util.ArrayList;
import java.util.List;

/**
 * Memory unagg Strategy
 *
 * @since 2021/5/19 16:39
 */
public class MemAggStrategy implements Strategy {
    /**
     * Query Memory Result
     *
     * @param device device
     * @return string string
     */
    @Override
    public String getQueryResult(String device) {
        List<MemAgg> list = new ArrayList<>() {
        };
        if (device.equalsIgnoreCase("A")) {
            DistributedDB.getInstance().queryA(Sql.DISTRIBUTED_TRACE_MEM_UNAGG, list);
        } else {
            DistributedDB.getInstance().queryB(Sql.DISTRIBUTED_TRACE_MEM_UNAGG, list);
        }
        return handleMemoryInfo(list);
    }

    private String handleMemoryInfo(List<MemAgg> result) {
        StringBuilder builder = new StringBuilder();
        builder.append("trace_mem_unagg: {").append(System.lineSeparator());
        for (MemAgg item : result) {
            builder.append("  process_values: {").append(System.lineSeparator());
            builder.append("    process_name: ").append(item.getProcessName()).append(System.lineSeparator());
            String[] names = item.getName().split(",");
            String[] values = item.getValue().split(",");
            String[] times = item.getTime().split(",");
            int oomScoreValue = 0;
            for (int index = 0; index < names.length; index++) {
                if (names[index].equals("oom_score_adj")) {
                    oomScoreValue = Integer.parseInt(values[index]);
                }
            }
            createStr(builder, oomScoreValue, names, values, times);
            builder.append("    }").append(System.lineSeparator());
        }
        builder.append("}").append(System.lineSeparator());
        return builder.toString();
    }

    private void createStr(StringBuilder builder, int oomScoreValue, String[] names, String[] values, String[] times) {
        long anonTs = 0L;
        int anonValue = 0;
        int swapValue = 0;
        for (int index = 0; index < names.length; index++) {
            if (names[index].equals("mem.rss.anon")) {
                builder.append("      anon_rss: {").append(System.lineSeparator());
                anonTs = Long.parseLong(times[index]);
                anonValue = Integer.parseInt(values[index]);
                builder.append("        ts: ").append(anonTs).append(System.lineSeparator());
                builder.append("        oom_score: ").append(oomScoreValue).append(System.lineSeparator());
                builder.append("        value: ").append(anonValue).append(System.lineSeparator());
                builder.append("      }").append(System.lineSeparator());
            }
            if (names[index].equals("mem.swap")) {
                swapValue = Integer.parseInt(values[index]);
                builder.append("      swap: {").append(System.lineSeparator());
                builder.append("        ts: ").append(times[index]).append(System.lineSeparator());
                builder.append("        oom_score: ").append(oomScoreValue).append(System.lineSeparator());
                builder.append("        value: ").append(swapValue).append(System.lineSeparator());
                builder.append("      }").append(System.lineSeparator());
            }
            if (names[index].equals("mem.rss.file")) {
                builder.append("      file_rss: { ").append(System.lineSeparator());
                builder.append("        ts: ").append(times[index]).append(System.lineSeparator());
                builder.append("        oom_score: ").append(oomScoreValue).append(System.lineSeparator());
                builder.append("        value: ").append(values[index]).append(System.lineSeparator());
                builder.append("      }").append(System.lineSeparator());
            }
            if (names[index].equals("oom_score_adj")) {
                builder.append("      anon_and_swap: {   ").append(System.lineSeparator());
                builder.append("        ts: ").append(anonTs).append(System.lineSeparator());
                builder.append("        oom_score: ").append(oomScoreValue).append(System.lineSeparator());
                builder.append("        value: ").append(anonValue + swapValue).append(System.lineSeparator());
                builder.append("      }").append(System.lineSeparator());
            }
        }
    }
}
