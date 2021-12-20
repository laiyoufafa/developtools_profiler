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

package ohos.devtools.views.perftrace;

import com.intellij.ui.AncestorListenerAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBPanel;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.datasources.transport.grpc.service.CpuPluginResult;
import ohos.devtools.views.applicationtrace.AllData;
import ohos.devtools.views.applicationtrace.DataPanel;
import ohos.devtools.views.applicationtrace.analysis.AnalysisEnum;
import ohos.devtools.views.applicationtrace.bean.CpuScale;
import ohos.devtools.views.applicationtrace.bean.Thread;
import ohos.devtools.views.perftrace.bean.PrefFile;
import ohos.devtools.views.perftrace.bean.PrefFunc;
import ohos.devtools.views.perftrace.bean.PrefRange;
import ohos.devtools.views.perftrace.bean.PrefSample;
import ohos.devtools.views.trace.Common;
import ohos.devtools.views.trace.CpuDb;
import ohos.devtools.views.trace.Db;
import ohos.devtools.views.trace.EventDispatcher;
import ohos.devtools.views.trace.ExpandPanel;
import ohos.devtools.views.trace.Sql;
import ohos.devtools.views.trace.Tip;
import ohos.devtools.views.trace.TracePanel;
import ohos.devtools.views.trace.TraceSimpleRow;
import ohos.devtools.views.trace.TraceThreadRow;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.event.AncestorEvent;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.util.stream.Collectors.groupingBy;

/**
 * PerfTracePanel
 *
 * @since 2021/5/12 16:34
 */
public class PerfTracePanel extends JBPanel {
    private TracePanel tracePanel;
    private DataPanel dataPanel;
    private Tip tip = Tip.getInstance();

    /**
     * PerfTracePanel
     */
    public PerfTracePanel() {
        super();
    }

    /**
     * load the data from db file
     *
     * @param name name
     * @param cpuName cpuName
     * @param isLocal isLocal
     */
    public void load(final String name, final String cpuName, final boolean isLocal) {
        recycleData();
        tracePanel = new TracePanel();
        dataPanel = new DataPanel(AnalysisEnum.PREF);
        setLayout(new MigLayout("insets 0"));
        JBSplitter splitter = new JBSplitter();
        splitter.setFirstComponent(tracePanel);
        splitter.setSecondComponent(dataPanel);
        add(splitter, "push,grow");
        this.addAncestorListener(new AncestorListenerAdapter() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                super.ancestorAdded(event);
                tip.setJLayeredPane(PerfTracePanel.this.getRootPane().getLayeredPane());
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                super.ancestorRemoved(event);
            }
        });
        Db.setDbName(name);
        Db.load(isLocal);
        insertAllTime();
        if (Objects.nonNull(cpuName) && !cpuName.isEmpty()) {
            CpuDb.setDbName(cpuName);
            CpuDb.load(isLocal);
            insertCpuScale();
        }
        insertProcess();
    }

    private double getCpuScale(CpuPluginResult.CpuUsageInfo cpuUsageInfo) {
        return (cpuUsageInfo.getProcessCpuTimeMs() - cpuUsageInfo.getPrevProcessCpuTimeMs()) * 1.0 / (
            cpuUsageInfo.getSystemBootTimeMs() - cpuUsageInfo.getPrevSystemBootTimeMs());
    }

    private void recycleData() {
        Utils.resetPool();
        removeAll();
        AllData.clearData();
        PerfData.clearData();
        EventDispatcher.clearData();
        TracePanel.currentSelectThreadIds.clear();
    }

    private void insertCpuScale() {
        List<CpuScale> cpuScales = new ArrayList<>() {
        };
        CpuDb.getInstance().query(Sql.QUERY_CPU_SCALE, cpuScales);
        List<CpuPluginResult.CpuUsageInfo> list = new ArrayList<>();
        cpuScales.forEach(scale -> {
            CpuPluginResult.CpuData.Builder builder = CpuPluginResult.CpuData.newBuilder();
            try {
                CpuPluginResult.CpuData cpuData = builder.mergeFrom(scale.getData()).build();
                CpuPluginResult.CpuUsageInfo cpuUsageInfo = cpuData.getCpuUsageInfo();
                list.add(cpuUsageInfo);
                scale.setScale(getCpuScale(cpuUsageInfo));
                scale.setStartNs(TimeUnit.MILLISECONDS.toNanos(cpuUsageInfo.getPrevProcessCpuTimeMs()));
                scale.setEndNs(TimeUnit.MILLISECONDS.toNanos(cpuUsageInfo.getProcessCpuTimeMs()));

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        tracePanel.getTimeShaft().clearMap();
        tracePanel.paintTimeShaft(g2 -> {
            Rectangle bounds = tracePanel.getTimeShaft().getBounds();
            if (cpuScales == null || cpuScales.isEmpty()) {
                g2.setColor(JBColor.foreground());
                Common.setAlpha(g2, 1f);
                Common.drawStringVHCenter(g2, "No CPU usage data available for this imported trace", bounds);
                return;
            }
            g2.setColor(JBColor.foreground().brighter());
            Common.setAlpha(g2, 0.7f);
            long min = cpuScales.stream().mapToLong(value -> value.getTimeStamp()).min().orElse(0);
            long max = cpuScales.stream().mapToLong(value -> value.getTimeStamp()).max().orElse(0);
            long dur = max - min;
            int height = tracePanel.getTimeShaft().getHeight();
            cpuScales.forEach(it -> {
                long sts = it.getTimeStamp() - min;
                int px = (int) Common.nsToXByDur(sts, bounds, dur);
                int py = (int) Math.round(height * (1.0 - it.getScale()));
                tracePanel.getTimeShaft().putRateMap(px, it.getScale());
                g2.drawLine(px, py, px, height);
            });
            Common.setAlpha(g2, 1.0f);
        });
    }

    private void insertAllTime() {
        List<PrefRange> range = new ArrayList<>() {
        };
        Db.getInstance().query(Sql.QUERY_PREF_TOTAL_TIME, range);
        if (range.size() > 0) {
            PrefRange prefRange = range.get(0);
            PerfData.setPrefRange(prefRange);
            TracePanel.DURATION = prefRange.getEndTime() - prefRange.getStartTime();
            tracePanel.getRuler().refreshTimeRuler(TracePanel.DURATION);
        }
    }

    private void initFileData() {
        List<PrefFile> prefFiles = new ArrayList<>() {
        };
        Db.getInstance().query(Sql.QUERY_PERF_FILES, prefFiles);
        if (prefFiles.size() > 0) {
            prefFiles.forEach(file -> {
                file.setPath(file.getPath());
            });
            PerfData.setPrefFiles(prefFiles.stream().collect(groupingBy(PrefFile::getFileId)));
        }
    }

    private void insertInteraction() {
        TraceSimpleRow user = new TraceSimpleRow("User");
        TraceSimpleRow lifecycle = new TraceSimpleRow("Lifecycle");
        ExpandPanel panel = new ExpandPanel("Interaction");
        panel.addTraceRow(user);
        panel.addTraceRow(lifecycle);
        tracePanel.getContentPanel().add(panel, "pushx,growx");
    }

    private void insertProcess() {
        initFileData();
        List<Thread> threads = new ArrayList<>() {
        };
        Db.getInstance().query(Sql.QUERY_PERF_THREAD, threads);
        ExpandPanel panel = new ExpandPanel("Threads (" + threads.size() + ")");
        for (Thread thread : threads) {
            TraceThreadRow<Thread, PrefFunc> row = new TraceThreadRow<>(thread.getThreadName(), thread.getTid());
            PerfData.getThreadNames().put(thread.getTid(), thread.getThreadName());
            initRow(row, thread, panel);
            panel.addTraceRow(row);
        }
        tracePanel.getContentPanel().add(panel, "pushx,growx");
    }

    private void initRow(TraceThreadRow<Thread, PrefFunc> row, Thread thread, ExpandPanel panel) {
        row.setRender((g2, data1, data2) -> {
            if (data2 != null) {
                data2.stream().filter(item -> row.contains(item)).forEach(func -> {
                    func.setRect(row.getRectByNode(func, row.getFuncHeight(), row.getFuncHeight()));
                    func.draw(g2);
                });
            }
        });
        row.setSupplier2(() -> {
            List<PrefSample> prefSamples = new ArrayList<>() {
            };
            Db.getInstance().query(Sql.QUERY_PERF_FUNC, prefSamples, thread.getTid());
            prefSamples.forEach(sample -> {
                if (PerfData.getPrefFiles().containsKey(sample.getFileId())) {
                    try {
                        if (sample.getSymbolId() == -1) {
                            sample.setName(PerfData.getPrefFiles().get(sample.getFileId()).get(0));
                        } else {
                            sample.setName(PerfData.getPrefFiles().get(sample.getFileId()).get(sample.getSymbolId()));
                        }
                    } catch (IndexOutOfBoundsException exception) {
                        sample.setName(null);
                    }
                } else {
                    sample.setName(null);
                }
                if (PerfData.getPrefRange() != null) {
                    sample.setTs(sample.getTs() - PerfData.getPrefRange().getStartTime());
                }
            });
            List<PrefFunc> funcList = PerfData.formatSampleList(prefSamples);
            PerfData.getFuncMap().put(thread.getTid(), funcList);
            int maxDept = funcList.stream().mapToInt(bean -> bean.getDepth()).max().orElse(0) + 1;
            int maxHeight = maxDept * row.getFuncHeight() + row.getFuncHeight();
            if (maxHeight < 30) {
                maxHeight = 30;
            }
            if (panel.getContent().getLayout() instanceof MigLayout) {
                MigLayout layout = (MigLayout) panel.getContent().getLayout();
                layout.setComponentConstraints(row, "growx,pushx,h " + maxHeight + "!");
            }
            row.setMaxDept(maxDept);
            row.updateUI();
            return funcList;
        });
    }

}
