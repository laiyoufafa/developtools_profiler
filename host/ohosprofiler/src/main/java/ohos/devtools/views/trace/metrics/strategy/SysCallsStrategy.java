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
import ohos.devtools.views.trace.metrics.bean.SysCalls;

import java.util.ArrayList;
import java.util.List;

/**
 * SysCalls Strategy
 */
public class SysCallsStrategy implements Strategy {
    /**
     * Query SysCalls Result
     *
     * @param sql sql
     * @return string string
     */
    @Override
    public String getQueryResult(MetricsSql sql) {
        List<SysCalls> list = new ArrayList<>() {
        };
        MetricsDb.getInstance().query(sql, list);
        return handleSysCallsInfo(list);
    }

    private String handleSysCallsInfo(List<SysCalls> result) {
        StringBuilder builder = new StringBuilder();
        builder.append("sys_calls{").append(System.lineSeparator());
        for (SysCalls item : result) {
            builder.append("\tfunction{").append(System.lineSeparator());
            builder.append("\t\tfunction_name:").append(item.getFunName()).append(System.lineSeparator());
            builder.append("\t\tdurMax:").append(item.getMaxDur()).append(System.lineSeparator());
            builder.append("\t\tdurMin:").append(item.getMinDur()).append(System.lineSeparator());
            builder.append("\t\tdurAvg:").append(item.getAvgDur()).append(System.lineSeparator());
            builder.append("\t}").append(System.lineSeparator());
        }
        builder.append("}").append(System.lineSeparator());
        return builder.toString();
    }
}
