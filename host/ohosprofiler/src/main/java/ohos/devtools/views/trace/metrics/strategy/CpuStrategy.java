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
import ohos.devtools.views.trace.metrics.bean.Cpu;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * cpu Strategy
 */
public class CpuStrategy implements Strategy {
    /**
     * Separator
     */
    private static final String SEPARATOR = ",";

    /**
     * get Query cpu Result
     *
     * @param sql sql
     * @return string string
     */
    @Override
    public String getQueryResult(MetricsSql sql) {
        List<Cpu> cpuList = new ArrayList<>() {
        };
        MetricsDb.getInstance().query(sql, cpuList);
        return handleCpuInfo(cpuList);
    }

    private String handleCpuInfo(List<Cpu> result) {
        StringBuilder builder = new StringBuilder();
        if (result.size() == 10) {
            builder.append("trace_cpu_top10 {").append(System.lineSeparator());
        } else {
            builder.append("trace_cpu {").append(System.lineSeparator());
        }
        for (Cpu item : result) {
            // filter the data with duration 0, which will result in AVG_ Frequency calculation error null
            if (item.getAvgFrequency() == null) {
                continue;
            }
            builder.append("  process_info {").append(System.lineSeparator());
            builder.append("    name: ")
                .append(StringUtils.isEmpty(item.getProcessName()) ? null : item.getProcessName())
                .append(System.lineSeparator());
            builder.append("    threads {").append(System.lineSeparator());
            builder.append("      name: ").append(item.getThreadName()).append(System.lineSeparator());
            String[] avgFrequency = item.getAvgFrequency().split(SEPARATOR);
            String[] cpuIds = item.getCpu().split(SEPARATOR);
            String[] durations = item.getDuration().split(SEPARATOR);
            String[] minFreq = item.getMinFreq().split(SEPARATOR);
            String[] maxFreq = item.getMaxFreq().split(SEPARATOR);
            for (int index = 0; index < cpuIds.length; index++) {
                builder.append("      cpu {").append(System.lineSeparator());
                builder.append("        id: ").append(cpuIds[index]).append(System.lineSeparator());
                builder.append("        min_freq_khz: ").append(minFreq[index]).append(System.lineSeparator());
                builder.append("        max_freq_khz: ").append(maxFreq[index]).append(System.lineSeparator());
                builder.append("        avg_freq_khz: ").append(avgFrequency[index]).append(System.lineSeparator());
                builder.append("        duration_ns: ").append(durations[index]).append(System.lineSeparator());
                builder.append("      }").append(System.lineSeparator());
            }
            builder.append("    }").append(System.lineSeparator());
            builder.append("   }").append(System.lineSeparator());
        }
        builder.append("}").append(System.lineSeparator());
        return builder.toString();
    }
}
