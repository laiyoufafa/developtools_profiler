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
import ohos.devtools.views.trace.metrics.bean.DistributeTerm;

import java.util.ArrayList;
import java.util.List;

/**
 * Distribute Term Strategy
 */
public class DistributeTermStrategy implements Strategy {
    /**
     * Query DistributeTerm Result
     *
     * @param sql sql
     * @return string string
     */
    @Override
    public String getQueryResult(MetricsSql sql) {
        List<DistributeTerm> list = new ArrayList<>() {
        };
        MetricsDb.getInstance().query(sql, list);
        return handleDistributeTermInfo(list);
    }

    private String handleDistributeTermInfo(List<DistributeTerm> result) {
        StringBuilder builder = new StringBuilder();
        builder.append("distributed_term {").append(System.lineSeparator());
        for (DistributeTerm item : result) {
            String[] threadIds = item.getThreadId().split(",");
            String[] threadNames = item.getThreadName().split(",");
            String[] processIds = item.getProcessId().split(",");
            String[] processNames =
                item.getProcessName() == null ? new String[threadIds.length] : item.getProcessName().split(",");
            String[] funNames = item.getFunName().split(",");
            String[] durations = item.getDur().split(",");
            String[] times = item.getTime().split(",");
            String[] flags = item.getFlag().split(",");
            builder.append("\tdistributed_term_item{").append(System.lineSeparator());
            long receiverTime = 0;
            long senderTime = 0;
            for (int index = 0; index < flags.length; index++) {
                String sendOrRecv = "C".equals(flags[index]) ? "\t\tsender {" : "\t\treceiver {";
                builder.append(sendOrRecv).append(System.lineSeparator());
                if (item.getFlag().contains("C,S") || item.getFlag().contains("S,C")) {
                    builder.append("\t\t\tAcross the device:").append(false).append(System.lineSeparator());
                    if ("S".equals(flags[index])) {
                        receiverTime = Long.parseLong(times[index]);
                    }
                    if ("C".equals(flags[index])) {
                        senderTime = Long.parseLong(times[index]);
                    }
                } else {
                    builder.append("\t\t\tAcross the device:").append(true).append(System.lineSeparator());
                }
                appendId(builder, item);
                builder.append("\t\t\tfunction_name:").append(funNames[index]).append(System.lineSeparator());
                builder.append("\t\t\tprocess_info{").append(System.lineSeparator());
                builder.append("\t\t\t\tprocess_id:").append(processIds[index]).append(System.lineSeparator());
                builder.append("\t\t\t\tprocess_name:").append(processNames[index]).append(System.lineSeparator());
                builder.append("\t\t\t}").append(System.lineSeparator());
                builder.append("\t\t\tthread_info{").append(System.lineSeparator());
                builder.append("\t\t\t\tthread_id:").append(threadIds[index]).append(System.lineSeparator());
                builder.append("\t\t\t\tthread_name:").append(threadNames[index]).append(System.lineSeparator());
                builder.append("\t\t\t}").append(System.lineSeparator());
                builder.append("\t\t\tdur:").append(durations[index]).append(System.lineSeparator());
                appendDelay(item, builder, flags, index, receiverTime - senderTime);
                builder.append("\t\t }").append(System.lineSeparator());
            }
            builder.append("\t}").append(System.lineSeparator());
        }
        builder.append("}").append(System.lineSeparator());
        return builder.toString();
    }

    private void appendDelay(DistributeTerm item, StringBuilder builder, String[] flags, int index, long delay) {
        if (item.getFlag().contains("C,S") || item.getFlag().contains("S,C")) {
            if ("S".equals(flags[index])) {
                builder.append("\t\t\tdelay:").append(delay).append(System.lineSeparator());
            }
        } else {
            if ("S".equals(flags[index])) {
                builder.append("\t\t\tdelay:").append("").append(System.lineSeparator());
            }
        }
    }

    private void appendId(StringBuilder builder, DistributeTerm item) {
        builder.append("\t\t\ttrace_name:").append(item.getTraceName()).append(System.lineSeparator());
        builder.append("\t\t\ttrace_id {").append(System.lineSeparator());
        builder.append("\t\t\t\tchainID:").append(item.getChainId()).append(System.lineSeparator());
        builder.append("\t\t\t\tspanID:").append(item.getSpanId()).append(System.lineSeparator());
        builder.append("\t\t\t\tparentSpanID:").append(item.getParentSpanId()).append(System.lineSeparator());
        builder.append("\t\t\t}").append(System.lineSeparator());
    }

}
