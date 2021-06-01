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

package ohos.devtools.views.trace.component;

import ohos.devtools.views.trace.bean.CpuData;
import ohos.devtools.views.trace.bean.CpuFreqData;
import ohos.devtools.views.trace.bean.FlagBean;
import ohos.devtools.views.trace.bean.FunctionBean;
import ohos.devtools.views.trace.bean.ProcessMem;
import ohos.devtools.views.trace.bean.ThreadData;
import ohos.devtools.views.trace.util.Db;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * test AnalystPanel class
 *
 * @version 1.0
 * @date 2021/4/24 18:05
 **/
class AnalystPanelTest {
    /**
     * test init setUp .
     */
    @BeforeEach
    void setUp() {
        Db.getInstance();
        Db.setDbName("trace.db");
        Db.load(false);
    }

    /**
     * test add the CpuList .
     */
    @Test
    void addCpuList() {
        AnalystPanel analystPanel = new AnalystPanel();
        ArrayList<List<CpuData>> lists = new ArrayList<>();
        analystPanel.addCpuList(lists);
        assertEquals(lists, AnalystPanel.cpuList);
    }

    /**
     * test add the ThreadsList .
     */
    @Test
    void addThreadsList() {
        AnalystPanel analystPanel = new AnalystPanel();
        List<ThreadData> lists = new ArrayList<>();
        List<ProcessMem> processMem = new ArrayList<>();
        analystPanel.addThreadsList(lists, processMem);
        assertEquals(lists, AnalystPanel.threadsList);
    }

    /**
     * test add the CpuFreqList .
     */
    @Test
    void addCpuFreqList() {
        AnalystPanel analystPanel = new AnalystPanel();
        ArrayList<List<CpuFreqData>> lists = new ArrayList<>();
        Map<String, Object> cpuMaxFreq = new HashMap<>();
        analystPanel.addCpuFreqList(lists, cpuMaxFreq);
        assertEquals(lists, AnalystPanel.cpuFreqList);
    }

    /**
     * test add the ThreadsList .
     */
    @Test
    void clickFunctionData() {
        FunctionBean functionBean = new FunctionBean();
        functionBean.setSelected(false);
        functionBean.setCategory("cate");
        functionBean.setDepth(1);
        functionBean.setFunName("functionBean");
        functionBean.setTid(1);
        functionBean.setDepth(1);
        functionBean.setTrackId(1);
        AnalystPanel analystPanel = new AnalystPanel();
        analystPanel.clickFunctionData(functionBean);
    }

    /**
     * test function clickThreadData .
     */
    @Test
    void clickThreadData() {
        AnalystPanel analystPanel = new AnalystPanel();
        analystPanel.clickThreadData(new ThreadData());
        assertEquals(10_000_000_000L, AnalystPanel.DURATION);
    }

    /**
     * test function clickCpuData .
     */
    @Test
    void clickCpuData() {
        AnalystPanel analystPanel = new AnalystPanel();
        analystPanel.clickCpuData(new CpuData());
        assertEquals(10_000_000_000L, AnalystPanel.DURATION);
    }

    /**
     * test function clickTimeFlag .
     */
    @Test
    void clickTimeFlag() {
        AnalystPanel analystPanel = new AnalystPanel();
        analystPanel.clickTimeFlag(new FlagBean());
        assertEquals(10_000_000_000L, AnalystPanel.DURATION);
    }
}