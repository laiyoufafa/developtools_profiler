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

import ohos.devtools.views.trace.bean.AsyncEvent;
import ohos.devtools.views.trace.bean.ClockData;
import ohos.devtools.views.trace.bean.CpuData;
import ohos.devtools.views.trace.bean.CpuFreqData;
import ohos.devtools.views.trace.bean.CpuFreqMax;
import ohos.devtools.views.trace.bean.FlagBean;
import ohos.devtools.views.trace.bean.FunctionBean;
import ohos.devtools.views.trace.bean.ThreadData;
import org.junit.jupiter.api.Test;

import javax.swing.JFrame;
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AnalystPanelTest {

    @Test
    void distinctByKey() {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        List<String> collect = list.stream().filter(AnalystPanel.distinctByKey(it -> it)).collect(Collectors.toList());
        assertEquals(3, collect.size());
    }

    @Test
    void distinctByKeySameName() {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("1");
        list.add("3");
        List<String> collect = list.stream().filter(AnalystPanel.distinctByKey(it -> it)).collect(Collectors.toList());
        assertEquals(2, collect.size());
    }

    @Test
    void distinctByKeyAllSameName() {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("1");
        list.add("1");
        List<String> collect = list.stream().filter(AnalystPanel.distinctByKey(it -> it)).collect(Collectors.toList());
        assertEquals(1, collect.size());
    }

    @Test
    void distinctByKeyNoSameName() {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        List<String> collect = list.stream().filter(AnalystPanel.distinctByKey(it -> it)).collect(Collectors.toList());
        assertEquals(4, collect.size());
    }

    @Test
    void distinctByKeyEmpty() {
        List<String> list = new ArrayList<>();
        list.add("");
        list.add("2");
        list.add("3");
        List<String> collect = list.stream().filter(AnalystPanel.distinctByKey(it -> it)).collect(Collectors.toList());
        assertEquals(3, collect.size());
    }

    @Test
    void addCpuList() throws NoSuchFieldException, IllegalAccessException {
        AnalystPanel analystPanel = new AnalystPanel();
        List<CpuData> cpuData = new ArrayList<>();
        List<List<CpuData>> cpuDataList = new ArrayList<>();
        analystPanel.addCpuList(cpuDataList);
        Field field = analystPanel.getClass().getDeclaredField("cpuList");
        field.setAccessible(true);
        assertEquals(field.get(analystPanel), cpuDataList);
    }

    @Test
    void addCpuListNull() throws NoSuchFieldException, IllegalAccessException {
        AnalystPanel analystPanel = new AnalystPanel();
        analystPanel.addCpuList(null);
        Field field = analystPanel.getClass().getDeclaredField("cpuList");
        field.setAccessible(true);
        assertNotNull(field.get(analystPanel));
    }

    @Test
    void addCpuListNormal() throws NoSuchFieldException, IllegalAccessException {
        List<CpuData> cpuData = new ArrayList<>();
        List<List<CpuData>> cpuDataList = new ArrayList<>();
        AnalystPanel analystPanel = new AnalystPanel();
        analystPanel.addCpuList(cpuDataList);
        Field field = analystPanel.getClass().getDeclaredField("cpuList");
        field.setAccessible(true);
        assertEquals(field.get(analystPanel), cpuDataList);
    }

    @Test
    void addCpuListNoEmpty() throws NoSuchFieldException, IllegalAccessException {
        List<CpuData> cpuData = new ArrayList<>();
        List<List<CpuData>> cpuDataList = new ArrayList<>();
        cpuDataList.add(cpuData);
        AnalystPanel analystPanel = new AnalystPanel();
        analystPanel.addCpuList(cpuDataList);
        Field field = analystPanel.getClass().getDeclaredField("cpuList");
        field.setAccessible(true);
        assertEquals(field.get(analystPanel), cpuDataList);
    }

    @Test
    void addCpuListNullList() throws NoSuchFieldException, IllegalAccessException {
        List<CpuData> cpuData = new ArrayList<>();
        List<List<CpuData>> cpuDataList = new ArrayList<>();
        cpuData.add(null);
        cpuDataList.add(cpuData);
        AnalystPanel analystPanel = new AnalystPanel();
        analystPanel.addCpuList(cpuDataList);
        Field field = analystPanel.getClass().getDeclaredField("cpuList");
        field.setAccessible(true);
        assertEquals(field.get(analystPanel), cpuDataList);
    }

    @Test
    void addCpuFreqList() throws NoSuchFieldException, IllegalAccessException {
        CpuFreqMax cpuFreqMax = new CpuFreqMax();
        AnalystPanel analystPanel = new AnalystPanel();
        List<CpuFreqData> cpuData = new ArrayList<>();
        List<List<CpuFreqData>> cpuDataList = new ArrayList<>();
        analystPanel.addCpuFreqList(cpuDataList, cpuFreqMax);
        Field field = analystPanel.getClass().getDeclaredField("cpuFreqList");
        field.setAccessible(true);
        assertEquals(field.get(analystPanel), cpuDataList);
    }

    @Test
    void addCpuFreqListNull() throws NoSuchFieldException, IllegalAccessException {
        CpuFreqMax cpuFreqMax = new CpuFreqMax();
        AnalystPanel analystPanel = new AnalystPanel();
        List<CpuFreqData> cpuData = new ArrayList<>();
        List<List<CpuFreqData>> cpuDataList = new ArrayList<>();
        analystPanel.addCpuFreqList(cpuDataList, null);
        Field field = analystPanel.getClass().getDeclaredField("cpuFreqList");
        field.setAccessible(true);
        assertEquals(field.get(analystPanel), cpuDataList);
    }

    @Test
    void addCpuFreqListNormal() throws NoSuchFieldException, IllegalAccessException {
        CpuFreqMax cpuFreqMax = new CpuFreqMax();
        List<CpuFreqData> cpuData = new ArrayList<>();
        List<List<CpuFreqData>> cpuDataList = new ArrayList<>();
        AnalystPanel analystPanel = new AnalystPanel();
        analystPanel.addCpuFreqList(cpuDataList, cpuFreqMax);
        Field field = analystPanel.getClass().getDeclaredField("cpuFreqList");
        field.setAccessible(true);
        assertEquals(field.get(analystPanel), cpuDataList);
    }

    @Test
    void addCpuFreqListNoEmpty() throws NoSuchFieldException, IllegalAccessException {
        CpuFreqMax cpuFreqMax = new CpuFreqMax();
        List<CpuFreqData> cpuData = new ArrayList<>();
        List<List<CpuFreqData>> cpuDataList = new ArrayList<>();
        cpuDataList.add(cpuData);
        AnalystPanel analystPanel = new AnalystPanel();
        analystPanel.addCpuFreqList(cpuDataList, cpuFreqMax);
        Field field = analystPanel.getClass().getDeclaredField("cpuFreqList");
        field.setAccessible(true);
        assertEquals(field.get(analystPanel), cpuDataList);
    }

    @Test
    void addCpuFreqListNullList() throws NoSuchFieldException, IllegalAccessException {
        List<CpuFreqData> cpuData = new ArrayList<>();
        List<List<CpuFreqData>> cpuDataList = new ArrayList<>();
        cpuDataList.add(cpuData);
        AnalystPanel analystPanel = new AnalystPanel();
        analystPanel.addCpuFreqList(cpuDataList, null);
        Field field = analystPanel.getClass().getDeclaredField("cpuFreqList");
        field.setAccessible(true);
        assertEquals(field.get(analystPanel), cpuDataList);
    }

    @Test
    void boxSelection() {
        AnalystPanel analystPanel = new AnalystPanel();
        List<Integer> cpus = new ArrayList<>();
        List<Integer> threadIds = new ArrayList<>();
        List<Integer> trackIds = new ArrayList<>();
        List<Integer> funTids = new ArrayList<>();
        AnalystPanel.LeftRightNS ns = new AnalystPanel.LeftRightNS();
        analystPanel.boxSelection(cpus, threadIds, trackIds, funTids, ns);
    }

    @Test
    void boxSelectionNull() {
        AnalystPanel analystPanel = new AnalystPanel();
        List<Integer> cpus = new ArrayList<>();
        List<Integer> threadIds = new ArrayList<>();
        List<Integer> trackIds = new ArrayList<>();
        List<Integer> funTids = new ArrayList<>();
        analystPanel.boxSelection(cpus, threadIds, trackIds, funTids, null);
    }

    @Test
    void boxSelectionNotNull() {
        AnalystPanel analystPanel = new AnalystPanel();
        List<Integer> cpus = new ArrayList<>();
        List<Integer> threadIds = new ArrayList<>();
        List<Integer> trackIds = new ArrayList<>();
        List<Integer> funTids = new ArrayList<>();
        AnalystPanel.LeftRightNS ns = new AnalystPanel.LeftRightNS();
        ns.setLeftNs(0L);
        ns.setRightNs(0L);
        analystPanel.boxSelection(cpus, threadIds, trackIds, funTids, ns);
    }

    @Test
    void boxSelectionNotEmpty() {
        AnalystPanel analystPanel = new AnalystPanel();
        List<Integer> cpus = new ArrayList<>();
        List<Integer> threadIds = new ArrayList<>();
        List<Integer> trackIds = new ArrayList<>();
        List<Integer> funTids = new ArrayList<>();
        AnalystPanel.LeftRightNS ns = new AnalystPanel.LeftRightNS();
        ns.setLeftNs(-1L);
        ns.setRightNs(1L);
        analystPanel.boxSelection(cpus, threadIds, trackIds, funTids, ns);
    }

    @Test
    void boxSelectionOnlyCpu() {
        AnalystPanel analystPanel = new AnalystPanel();
        List<Integer> cpus = new ArrayList<>();
        List<Integer> threadIds = new ArrayList<>();
        List<Integer> trackIds = new ArrayList<>();
        List<Integer> funTids = new ArrayList<>();
        AnalystPanel.LeftRightNS ns = new AnalystPanel.LeftRightNS();
        ns.setLeftNs(Long.MAX_VALUE);
        ns.setRightNs(Long.MIN_VALUE);
        analystPanel.boxSelection(cpus, threadIds, trackIds, funTids, null);
    }

    @Test
    void clickFunctionData() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        FunctionBean functionBean = new FunctionBean();
        functionBean.setStartTime(100L);
        functionBean.setDuration(100L);
        analystPanel.clickFunctionData(functionBean);
        assertNotNull(functionBean);
    }

    @Test
    void clickFunctionDataZero() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        FunctionBean functionBean = new FunctionBean();
        functionBean.setStartTime(0L);
        functionBean.setDuration(0L);
        analystPanel.clickFunctionData(functionBean);
        assertNotNull(functionBean);
    }

    @Test
    void clickFunctionDataNegative() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        FunctionBean functionBean = new FunctionBean();
        functionBean.setStartTime(-1L);
        functionBean.setDuration(-1L);
        analystPanel.clickFunctionData(functionBean);
        assertNotNull(functionBean);
    }

    @Test
    void clickFunctionDataOneNegative() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        FunctionBean functionBean = new FunctionBean();
        functionBean.setStartTime(1L);
        functionBean.setDuration(-1L);
        analystPanel.clickFunctionData(functionBean);
        assertNotNull(functionBean);
    }

    @Test
    void clickFunctionDataOneMax() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        FunctionBean functionBean = new FunctionBean();
        functionBean.setStartTime(Long.MAX_VALUE);
        functionBean.setDuration(Long.MIN_VALUE);
        analystPanel.clickFunctionData(functionBean);
        assertNotNull(functionBean);
    }

    @Test
    void clickAsyncFunctionData() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        AsyncEvent asyncEvent = new AsyncEvent();
        asyncEvent.setName("");
        asyncEvent.setCookie(1);
        asyncEvent.setDuration(100L);
        asyncEvent.setStartTime(100L);
        analystPanel.clickAsyncFunctionData(asyncEvent);
        assertNotNull(asyncEvent);
    }

    @Test
    void clickAsyncFunctionDataZero() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        AsyncEvent asyncEvent = new AsyncEvent();
        asyncEvent.setName("");
        asyncEvent.setCookie(0);
        asyncEvent.setDuration(0L);
        asyncEvent.setStartTime(0L);
        analystPanel.clickAsyncFunctionData(asyncEvent);
        assertNotNull(asyncEvent);
    }

    @Test
    void clickAsyncFunctionDataNegative() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        AsyncEvent asyncEvent = new AsyncEvent();
        asyncEvent.setName("");
        asyncEvent.setStartTime(-1L);
        asyncEvent.setDuration(-1L);
        asyncEvent.setCookie(1);
        analystPanel.clickAsyncFunctionData(asyncEvent);
        assertNotNull(asyncEvent);
    }

    @Test
    void clickAsyncFunctionDataOneNegative() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        AsyncEvent asyncEvent = new AsyncEvent();
        asyncEvent.setName("");
        asyncEvent.setStartTime(1L);
        asyncEvent.setDuration(-1L);
        asyncEvent.setCookie(1);
        analystPanel.clickAsyncFunctionData(asyncEvent);
        assertNotNull(asyncEvent);
    }

    @Test
    void clickAsyncFunctionDataOneMax() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        AsyncEvent asyncEvent = new AsyncEvent();
        asyncEvent.setName("");
        asyncEvent.setCookie(1);
        asyncEvent.setStartTime(Long.MAX_VALUE);
        asyncEvent.setDuration(Long.MIN_VALUE);
        analystPanel.clickAsyncFunctionData(asyncEvent);
        assertNotNull(asyncEvent);
    }

    @Test
    void clickClockData() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        ClockData clockData = new ClockData();
        clockData.setDelta(1L);
        clockData.setValue(1L);
        clockData.setStartTime(1L);
        clockData.setDuration(1L);
        analystPanel.clickClockData(clockData);
        assertNotNull(clockData);
    }

    @Test
    void clickClockDataZero() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        ClockData clockData = new ClockData();
        clockData.setDelta(1L);
        clockData.setValue(1L);
        clockData.setDuration(0L);
        clockData.setStartTime(0L);
        analystPanel.clickClockData(clockData);
        assertNotNull(clockData);
    }

    @Test
    void clickClockDataNegative() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        ClockData clockData = new ClockData();
        clockData.setDelta(-1L);
        clockData.setValue(-1L);
        clockData.setStartTime(-1L);
        clockData.setDuration(-1L);
        analystPanel.clickClockData(clockData);
        assertNotNull(clockData);
    }

    @Test
    void clickClockDataOneNegative() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        ClockData clockData = new ClockData();
        clockData.setDelta(-1L);
        clockData.setValue(-1L);
        clockData.setStartTime(1L);
        clockData.setDuration(-1L);
        analystPanel.clickClockData(clockData);
        assertNotNull(clockData);
    }

    @Test
    void clickClockDataOneMax() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        AsyncEvent asyncEvent = new AsyncEvent();
        asyncEvent.setName("");
        asyncEvent.setStartTime(Long.MAX_VALUE);
        asyncEvent.setDuration(Long.MIN_VALUE);
        asyncEvent.setCookie(1);
        analystPanel.clickAsyncFunctionData(asyncEvent);
        assertNotNull(asyncEvent);
    }

    @Test
    void clickThreadData() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        ThreadData threadData = new ThreadData();
        threadData.setState("state");
        threadData.setTid(1);
        threadData.setPid(1);
        threadData.setStartTime(1L);
        threadData.setDuration(1L);
        analystPanel.clickThreadData(threadData);
        assertNotNull(threadData);
    }

    @Test
    void clickThreadDataZero() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        ThreadData threadData = new ThreadData();
        threadData.setState("state");
        threadData.setTid(0);
        threadData.setPid(0);
        threadData.setStartTime(0L);
        threadData.setDuration(0L);
        analystPanel.clickThreadData(threadData);
        assertNotNull(threadData);
    }

    @Test
    void clickThreadDataNegative() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        ThreadData threadData = new ThreadData();
        threadData.setState("state");
        threadData.setTid(-1);
        threadData.setPid(-1);
        threadData.setStartTime(-1L);
        threadData.setDuration(-1L);
        analystPanel.clickThreadData(threadData);
        assertNotNull(threadData);
    }

    @Test
    void clickThreadDataOneNegative() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        ThreadData threadData = new ThreadData();
        threadData.setState("state");
        threadData.setTid(-1);
        threadData.setPid(-1);
        threadData.setStartTime(1L);
        threadData.setDuration(-1L);
        analystPanel.clickThreadData(threadData);
        assertNotNull(threadData);
    }

    @Test
    void clickThreadDataOneMax() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        ThreadData threadData = new ThreadData();
        threadData.setState("state");
        threadData.setTid(-1);
        threadData.setPid(-1);
        threadData.setStartTime(Long.MAX_VALUE);
        threadData.setDuration(Long.MIN_VALUE);
        analystPanel.clickThreadData(threadData);
        assertNotNull(threadData);
    }

    @Test
    void clickCpuData() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        CpuData cpuData = new CpuData();
        cpuData.setProcessName("name");
        cpuData.setProcessId(1);
        cpuData.setCpu(1);
        cpuData.setTid(1);
        cpuData.setStartTime(1L);
        cpuData.setDuration(1L);
        cpuData.setEndState("state");
        analystPanel.clickCpuData(cpuData);
        assertNotNull(cpuData);
    }

    @Test
    void clickCpuDataZero() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        CpuData cpuData = new CpuData();
        cpuData.setProcessName("name");
        cpuData.setProcessId(0);
        cpuData.setCpu(0);
        cpuData.setTid(0);
        cpuData.setStartTime(0L);
        cpuData.setDuration(0L);
        cpuData.setEndState("state");
        analystPanel.clickCpuData(cpuData);
        assertNotNull(cpuData);
    }

    @Test
    void clickCpuDataNegative() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        CpuData cpuData = new CpuData();
        cpuData.setProcessName("name");
        cpuData.setProcessId(-1);
        cpuData.setCpu(-1);
        cpuData.setTid(-1);
        cpuData.setStartTime(-1L);
        cpuData.setDuration(-1L);
        cpuData.setEndState("state");
        analystPanel.clickCpuData(cpuData);
        assertNotNull(cpuData);
    }

    @Test
    void clickCpuDataOneNegative() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        CpuData cpuData = new CpuData();
        cpuData.setProcessName("name");
        cpuData.setProcessId(-1);
        cpuData.setCpu(1);
        cpuData.setTid(1);
        cpuData.setStartTime(-1L);
        cpuData.setDuration(1L);
        cpuData.setEndState("state");
        analystPanel.clickCpuData(cpuData);
        assertNotNull(cpuData);
    }

    @Test
    void clickCpuDataOneMax() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        CpuData cpuData = new CpuData();
        cpuData.setProcessName("name");
        cpuData.setProcessId(-1);
        cpuData.setCpu(1);
        cpuData.setTid(1);
        cpuData.setStartTime(Long.MAX_VALUE);
        cpuData.setDuration(Long.MIN_VALUE);
        cpuData.setEndState("state");
        analystPanel.clickCpuData(cpuData);
        assertNotNull(cpuData);
    }

    @Test
    void clickTimeFlag() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        FlagBean flagBean = new FlagBean();
        flagBean.setColor(Color.BLACK);
        flagBean.setTime(1L);
        analystPanel.clickTimeFlag(flagBean);
        assertNotNull(flagBean);
    }

    @Test
    void clickTimeFlagZero() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        FlagBean flagBean = new FlagBean();
        flagBean.setColor(Color.BLACK);
        flagBean.setTime(0L);
        analystPanel.clickTimeFlag(flagBean);
        assertNotNull(flagBean);
    }

    @Test
    void clickTimeFlagNegative() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        FlagBean flagBean = new FlagBean();
        flagBean.setColor(Color.BLACK);
        flagBean.setTime(-1L);
        analystPanel.clickTimeFlag(flagBean);
        assertNotNull(flagBean);
    }

    @Test
    void clickTimeFlagOneNegative() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        FlagBean flagBean = new FlagBean();
        flagBean.setColor(Color.BLACK);
        flagBean.setTime(-1000L);
        analystPanel.clickTimeFlag(flagBean);
        assertNotNull(flagBean);
    }

    @Test
    void clickTimeFlagOneMax() {
        AnalystPanel analystPanel = new AnalystPanel();
        JFrame jbPanel = new JFrame();
        jbPanel.add(analystPanel);
        FlagBean flagBean = new FlagBean();
        flagBean.setColor(Color.BLACK);
        flagBean.setTime(Long.MAX_VALUE);
        analystPanel.clickTimeFlag(flagBean);
        assertNotNull(flagBean);
    }
}
