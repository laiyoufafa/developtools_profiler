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

import ohos.devtools.views.distributed.statistics.bean.Stats;
import ohos.devtools.views.distributed.util.DistributedDB;
import ohos.devtools.views.trace.Sql;

import java.util.ArrayList;
import java.util.List;

/**
 * Trace Stats Strategy
 *
 * @since 2021/5/19 16:39
 */
public class TraceStatsStrategy implements Strategy {
    /**
     * get Query stats Result
     *
     * @param device device
     * @return String String
     */
    @Override
    public String getQueryResult(String device) {
        List<Stats> stats = new ArrayList<>() {
        };
        if (device.equalsIgnoreCase("A")) {
            DistributedDB.getInstance().queryA(Sql.DISTRIBUTED_TRACE_STATS, stats);
        } else {
            DistributedDB.getInstance().queryB(Sql.DISTRIBUTED_TRACE_STATS, stats);
        }
        StringBuilder builder = new StringBuilder();
        builder.append("trace_stats: {").append(System.lineSeparator());
        for (Stats item : stats) {
            builder.append("  stat:{").append(System.lineSeparator());
            builder.append("    name: ").append(item.getName()).append("_").append(item.getType())
                .append(System.lineSeparator());
            builder.append("    count: ").append(item.getCount()).append(System.lineSeparator());
            builder.append("    source: ").append(item.getSource()).append(System.lineSeparator());
            builder.append("    severity: ").append(item.getSeverity()).append(System.lineSeparator());
            builder.append("  }").append(System.lineSeparator());
        }
        builder.append("}");
        return builder.toString();
    }
}
