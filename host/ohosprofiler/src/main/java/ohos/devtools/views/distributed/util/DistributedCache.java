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

package ohos.devtools.views.distributed.util;

import ohos.devtools.views.applicationtrace.bean.Duration;
import ohos.devtools.views.distributed.bean.DistributedFuncBean;
import ohos.devtools.views.distributed.bean.DistributedParams;
import ohos.devtools.views.distributed.bean.DistributedThreadBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DistributedCache
 *
 * @since 2021/08/07 13:41
 */
public class DistributedCache {
    /**
     * durA duration from A database
     */
    public static final List<Duration> DUR_A = new ArrayList<>() {
    };

    /**
     * threadsA thread list from A database
     */
    public static final List<DistributedThreadBean> THREADS_A = new ArrayList<>() {
    };

    /**
     * threadMapA thread map from A database
     */
    public static final Map<Integer, List<DistributedThreadBean>> THREAD_MAP_A = new HashMap<>();

    /**
     * funcMapA function map from A database
     */
    public static final Map<Integer, List<DistributedFuncBean>> FUNC_MAP_A = new HashMap<>();

    /**
     * idFuncBeanMapA function map with name is id and value is function from A database
     */
    public static final Map<Integer, DistributedFuncBean> ID_FUNC_BEAN_MAP_A = new HashMap<>();

    /**
     * durB duration from B database
     */
    public static final List<Duration> DUR_B = new ArrayList<>() {
    };

    /**
     * threadsB thread list from B database
     */
    public static final List<DistributedThreadBean> THREADS_B = new ArrayList<>() {
    };

    /**
     * threadMapB thread map from A database
     */
    public static final Map<Integer, List<DistributedThreadBean>> THREAD_MAP_B = new HashMap<>();

    /**
     * funcMapB function map from B database
     */
    public static final Map<Integer, List<DistributedFuncBean>> FUNC_MAP_B = new HashMap<>();

    /**
     * idFuncBeanMapB function map with name is id and value is function from B database
     */
    public static final Map<Integer, DistributedFuncBean> ID_FUNC_BEAN_MAP_B = new HashMap<>();

    /**
     * DistributedParams distribuetedParams map
     */
    private static DistributedParams distribuetedParams;

    /**
     * threadNamesA thread name list from A database
     */
    private static Map<Integer, String> threadNamesA = new HashMap<>();

    /**
     * threadNamesB thread name list from B database
     */
    private static Map<Integer, String> threadNamesB = new HashMap<>();

    /**
     * totalMedianTimes
     */
    private static Double totalMedianTimes = 2.0D;

    /**
     * delayMedianTimes
     */
    private static Double delayMedianTimes = 1.0D;

    /**
     * currentDBFlag current db
     */
    private static String currentDBFlag = "A";

    /**
     * recycleData recycler all data
     */
    public static void recycleData() {
        DUR_A.clear();
        DUR_B.clear();
        THREADS_A.clear();
        THREADS_B.clear();
        threadNamesA.clear();
        threadNamesB.clear();
        THREAD_MAP_A.entrySet().stream().forEach(it -> it.getValue().clear());
        FUNC_MAP_A.entrySet().stream().forEach(it -> it.getValue().clear());
        THREAD_MAP_B.entrySet().stream().forEach(it -> it.getValue().clear());
        FUNC_MAP_B.entrySet().stream().forEach(it -> it.getValue().clear());
        ID_FUNC_BEAN_MAP_A.clear();
        ID_FUNC_BEAN_MAP_B.clear();
        THREAD_MAP_A.clear();
        THREAD_MAP_B.clear();
        FUNC_MAP_A.clear();
        FUNC_MAP_B.clear();
    }

    public static void setThreadNamesA(Map<Integer, String> threadNamesA) {
        DistributedCache.threadNamesA = threadNamesA;
    }

    public static Map<Integer, String> getThreadNamesB() {
        return threadNamesB;
    }

    public static void setThreadNamesB(Map<Integer, String> threadNamesB) {
        DistributedCache.threadNamesB = threadNamesB;
    }

    public static DistributedParams getDistribuetedParams() {
        return distribuetedParams;
    }

    public static void setDistribuetedParams(DistributedParams distribuetedParams) {
        DistributedCache.distribuetedParams = distribuetedParams;
    }

    public static Double getTotalMedianTimes() {
        return totalMedianTimes;
    }

    public static void setTotalMedianTimes(Double totalMedianTimes) {
        DistributedCache.totalMedianTimes = totalMedianTimes;
    }

    public static Double getDelayMedianTimes() {
        return delayMedianTimes;
    }

    public static void setDelayMedianTimes(Double delayMedianTimes) {
        DistributedCache.delayMedianTimes = delayMedianTimes;
    }

    public static String getCurrentDBFlag() {
        return currentDBFlag;
    }

    public static void setCurrentDBFlag(String currentDBFlag) {
        DistributedCache.currentDBFlag = currentDBFlag;
    }
}
