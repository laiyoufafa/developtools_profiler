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

import ohos.devtools.views.trace.metrics.MetricsSql;

import java.util.HashMap;
import java.util.Map;

/**
 * MetadataStrategy
 */
public class MetricsFactory {
    private static final MetricsFactory METRICS_FACTORY = new MetricsFactory();
    private final Map<MetricsSql, Strategy> strategyMap = new HashMap<>();

    private MetricsFactory() {
        strategyMap.put(MetricsSql.TRACE_METADATA, new MetadataStrategy());
        strategyMap.put(MetricsSql.TRACE_CPU, new CpuStrategy());
        strategyMap.put(MetricsSql.TRACE_CPU_TOP10, new CpuStrategy());
        strategyMap.put(MetricsSql.TRACE_TASK_NAMES, new TraceTaskStrategy());
        strategyMap.put(MetricsSql.TRACE_STATS, new TraceStatsStrategy());
        strategyMap.put(MetricsSql.TRACE_MEM, new MemStrategy());
        strategyMap.put(MetricsSql.TRACE_MEM_TOP10, new MemStrategy());
        strategyMap.put(MetricsSql.TRACE_MEM_UNAGG, new MemAggStrategy());
        strategyMap.put(MetricsSql.DISTRIBUTED_TERM, new DistributeTermStrategy());
        strategyMap.put(MetricsSql.SYS_CALLS_TOP10, new SysCallsTopStrategy());
        strategyMap.put(MetricsSql.SYS_CALLS, new SysCallsStrategy());
    }

    /**
     * Get singleton
     *
     * @return MetricsFactory MetricsFactory
     */
    public static MetricsFactory getInstance() {
        return METRICS_FACTORY;
    }

    /**
     * Get Strategy
     *
     * @param sql sql
     * @return Strategy Strategy
     */
    public Strategy getStrategy(MetricsSql sql) {
        return strategyMap.get(sql);
    }
}
