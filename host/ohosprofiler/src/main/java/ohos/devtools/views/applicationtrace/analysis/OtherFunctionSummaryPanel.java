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

package ohos.devtools.views.applicationtrace.analysis;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.applicationtrace.AllData;
import ohos.devtools.views.applicationtrace.DataPanel;
import ohos.devtools.views.applicationtrace.bean.AppFunc;
import ohos.devtools.views.applicationtrace.bean.EventBean;
import ohos.devtools.views.applicationtrace.bean.Func;
import ohos.devtools.views.applicationtrace.util.MathUtils;
import ohos.devtools.views.applicationtrace.util.TimeUtils;
import ohos.devtools.views.perftrace.PerfData;
import ohos.devtools.views.perftrace.bean.PrefFunc;
import ohos.devtools.views.trace.EventDispatcher;
import ohos.devtools.views.trace.EventPanel;
import ohos.devtools.views.trace.ExpandPanel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The OtherFunctionSummaryPanel
 *
 * @since 2021/04/22 12:25
 */
public class OtherFunctionSummaryPanel extends EventPanel {
    private JBLabel timeRange = new JBLabel();
    private JBLabel dataType = new JBLabel();
    private EventTable selectEventTable = new EventTable(null);
    private EventTable funcTable = new EventTable(null);
    private ExpandPanel topFuncPanel;
    private JBPanel numberStatistics = new JBPanel();
    private JBLabel count = new JBLabel("");
    private JBLabel average = new JBLabel("");
    private JBLabel max = new JBLabel("");
    private JBLabel min = new JBLabel("");
    private JBLabel stdDev = new JBLabel("");
    private Object currentNode;

    /**
     * structure
     */
    public OtherFunctionSummaryPanel() {
        setLayout(new MigLayout("inset 0"));
        setBackground(JBColor.background().brighter());
        EventDispatcher.addClickListener(node -> {
            if (node != currentNode) {
                currentNode = node;
                if (node instanceof AppFunc) {
                    setPageData((AppFunc) node);
                }
            }
        });
        setData();
    }

    private void setData() {
        removeAll();
        setBorder(JBUI.Borders.empty(15, 15));
        add(new JBLabel("Time Range"));
        add(timeRange, "growx,pushx,wrap");
        add(new JBLabel("Data Type"));
        dataType.setText("Stack Frame");
        add(dataType, "growx,pushx,wrap");
        add(new JBLabel(), "growx,pushx,wrap");
        JBLabel selectEvent = new JBLabel("Selected event");
        add(selectEvent, "growx,pushx,wrap");
        add(selectEventTable, "span 2,growx,pushx,wrap");
        topFuncPanel = new ExpandPanel("");
        setStatistics();
        topFuncPanel.getContent().setBackground(JBColor.background().brighter());
        topFuncPanel.getContent().add(numberStatistics, "w 50%!,wrap");
        JBLabel jbLabel = new JBLabel("Longest running occurrences (select row to navigate)");
        jbLabel.setBorder(JBUI.Borders.empty(10, 0, 10, 0));
        topFuncPanel.getContent().add(jbLabel, "pushx,growx,wrap");
        topFuncPanel.getContent().add(funcTable, "growx,pushx,wrap");
        add(topFuncPanel, "span 2,growx,pushx,wrap");
    }

    private void setStatistics() {
        numberStatistics.setBackground(JBColor.background().brighter());
        numberStatistics.setBorder(JBUI.Borders.empty(0, 15, 0, 15));
        numberStatistics
            .setLayout(new MigLayout("inset 0", "[grow,12.5%] [grow,12.5%] [grow,12.5%] [grow,12.5%]", "[] []"));
        numberStatistics.add(count);
        numberStatistics.add(average);
        numberStatistics.add(max);
        numberStatistics.add(min);
        numberStatistics.add(stdDev, "wrap");
        numberStatistics.add(new JBLabel("Count"));
        numberStatistics.add(new JBLabel("Average"));
        numberStatistics.add(new JBLabel("Max"));
        numberStatistics.add(new JBLabel("Min"));
        numberStatistics.add(new JBLabel("Std Dev"), "wrap");
    }

    /**
     * setPageData
     *
     * @param obj obj
     */
    public void setPageData(AppFunc obj) {
        if (DataPanel.analysisEnum.equals(AnalysisEnum.APP)) {
            if (obj instanceof Func) {
                Func appFunc = (Func) obj;
                getAppTraceData(appFunc);
                timeRange.setText(TimeUtils.getTimeFormatString(appFunc.getStartTs()) + "~" + TimeUtils
                    .getTimeFormatString(appFunc.getEndTs()));
                dataType.setText("Trace Event");
                selectEventTable.dataSource.clear();
                selectEventTable.dataSource.add(initAppBean(appFunc));
                selectEventTable.freshData();
            }
        } else {
            if (obj instanceof PrefFunc) {
                PrefFunc prefFunc = (PrefFunc) obj;
                getPerfData(prefFunc);
                timeRange.setText(TimeUtils.getTimeFormatString(prefFunc.getStartTs()) + " - " + TimeUtils
                    .getTimeFormatString(prefFunc.getEndTs()));
                dataType.setText("Stack Frame");
                selectEventTable.dataSource.clear();
                selectEventTable.dataSource.add(initPerfBean(prefFunc));
                selectEventTable.freshData();
            }
        }
    }

    private void getAppTraceData(Func func) {
        List<Func> collect = AllData.funcMap.get(func.getTid()).stream()
            .filter(filter -> filter.getFuncName().equals(func.getFuncName()))
            .sorted(Comparator.comparingLong(Func::getDur).reversed()).collect(Collectors.toList());
        List<Long> longs = collect.stream().map(Func::getDur).collect(Collectors.toList());
        setStatisticsData(longs);
        List<EventBean> dataSource = new ArrayList<>();
        collect.stream().limit(10).forEach(item -> {
            dataSource.add(initAppBean(item));
        });
        funcTable.dataSource = dataSource;
        funcTable.freshData();
    }

    private void getPerfData(PrefFunc prefFunc) {
        List<PrefFunc> collect = PerfData.getFuncMap().get(Long.valueOf(prefFunc.getTid()).intValue()).stream()
            .filter(filter -> filter.getDepth() != -1 && filter.getFuncName().equals(prefFunc.getFuncName()))
            .sorted(Comparator.comparingLong(PrefFunc::getDur).reversed()).collect(Collectors.toList());
        List<Long> longs = collect.stream().map(PrefFunc::getDur).collect(Collectors.toList());
        setStatisticsData(longs);
        List<EventBean> dataSource = new ArrayList<>();
        collect.stream().limit(10).forEach(item -> {
            dataSource.add(initPerfBean(item));
        });
        funcTable.dataSource = dataSource;
        funcTable.freshData();
    }

    private EventBean initAppBean(Func func) {
        EventBean eventBean = new EventBean();
        eventBean.setName(func.getFuncName());
        eventBean.setStartTime(func.getStartTs());
        eventBean.setWallDuration(func.getDur());
        eventBean.setSelfTime(func.getDur() - AllData.funcMap.get(func.getTid()).stream()
            .filter(filter -> filter.getDepth() != -1 && filter.getParentId().equals(func.getId()))
            .mapToLong(Func::getDur).sum());
        eventBean.setCpuDuration(func.getRunning());
        eventBean.setCpuSelfTime(func.getRunning() - AllData.funcMap.get(func.getTid()).stream()
            .filter(filter -> filter.getDepth() != -1 && filter.getParentId().equals(func.getId()))
            .mapToLong(Func::getRunning).sum());
        return eventBean;
    }

    private EventBean initPerfBean(PrefFunc prefFunc) {
        EventBean eventBean = new EventBean();
        eventBean.setName(prefFunc.getFuncName());
        eventBean.setStartTime(prefFunc.getStartTs());
        eventBean.setWallDuration(prefFunc.getDur());
        long selfTime = prefFunc.getDur() - prefFunc.getChildrenNodes().stream().mapToLong(PrefFunc::getDur).sum();
        eventBean.setSelfTime(selfTime);
        eventBean.setCpuDuration(prefFunc.getDur());
        eventBean.setCpuSelfTime(selfTime);
        return eventBean;
    }

    private void setStatisticsData(List<Long> longs) {
        topFuncPanel.setTitle("All Occurrences(" + longs.size() + ")");
        String countTxt = Integer.toString(longs.size());
        double averageNum = MathUtils.average(longs.stream().map(Long::doubleValue).collect(Collectors.toList()));
        String averageTxt = TimeUtils.getTimeWithUnit(Double.valueOf(averageNum).longValue());
        count.setText(countTxt);
        average.setText(averageTxt);
        String maxTxt = TimeUtils.getTimeWithUnit(longs.stream().max(Long::compareTo).orElse(0L));
        max.setText(maxTxt);
        String minTxt = TimeUtils.getTimeWithUnit(longs.stream().min(Long::compareTo).orElse(0L));
        min.setText(minTxt);
        double stdDevNum =
            MathUtils.standardDiviation(longs.stream().map(Long::doubleValue).collect(Collectors.toList()));
        String stdDevTxt = TimeUtils.getTimeWithUnit(Double.valueOf(stdDevNum).longValue());
        stdDev.setText(stdDevTxt);
    }

    @Override
    public void change(long startNS, long endNS, List<Integer> threadIds) {
    }

    @Override
    public void change(long startNS, long endNS, long scale) {
    }
}
