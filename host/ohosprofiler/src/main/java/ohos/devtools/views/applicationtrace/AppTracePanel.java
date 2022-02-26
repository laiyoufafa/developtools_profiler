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

package ohos.devtools.views.applicationtrace;

import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBPanel;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.datasources.transport.grpc.service.CpuPluginResult;
import ohos.devtools.views.applicationtrace.analysis.AnalysisEnum;
import ohos.devtools.views.applicationtrace.bean.Cpu;
import ohos.devtools.views.applicationtrace.bean.CpuFreq;
import ohos.devtools.views.applicationtrace.bean.CpuFreqMax;
import ohos.devtools.views.applicationtrace.bean.CpuMax;
import ohos.devtools.views.applicationtrace.bean.CpuScale;
import ohos.devtools.views.applicationtrace.bean.DisplayFunc;
import ohos.devtools.views.applicationtrace.bean.Duration;
import ohos.devtools.views.applicationtrace.bean.Frame;
import ohos.devtools.views.applicationtrace.bean.Func;
import ohos.devtools.views.applicationtrace.bean.Thread;
import ohos.devtools.views.applicationtrace.bean.VsyncAppBean;
import ohos.devtools.views.applicationtrace.util.TimeUtils;
import ohos.devtools.views.perftrace.PerfData;
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
import ohos.devtools.views.trace.bean.Process;
import ohos.devtools.views.trace.component.AnalystPanel;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * AppTracePanel
 *
 * @since 2021/5/12 16:34
 */
public class AppTracePanel extends JBPanel {
    private TracePanel tracePanel;
    private DataPanel dataPanel;
    private Integer processId;
    private Tip tip = Tip.getInstance();
    private String cpuName;

    /**
     * struct function
     */
    public AppTracePanel() {
        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                tip.hidden();
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent event) {
                super.componentHidden(event);
                tip.hidden();
            }
        });
    }

    /**
     * load the data from db file
     *
     * @param name name
     * @param cpuName cpuName
     * @param isLocal isLocal
     */
    public void load(final String name, final String cpuName, final boolean isLocal) {
        load(name, cpuName, null, isLocal);
    }

    /**
     * load the data from db file
     *
     * @param name name
     * @param cpuName cpuName
     * @param pId pId
     * @param isLocal isLocal
     */
    public void load(final String name, final String cpuName, final Integer pId, final boolean isLocal) {
        Utils.resetPool();
        recycleData();
        this.cpuName = cpuName;
        Db.setDbName(name);
        Db.load(isLocal);
        if (Objects.nonNull(cpuName) && !cpuName.isEmpty()) {
            CpuDb.setDbName(cpuName);
            CpuDb.load(isLocal);
        }
        if (Objects.nonNull(pId)) {
            loadProcess(pId);
        } else {
            loadProcess(null);
        }
    }

    private void recycleData() {
        removeAll();
        AllData.clearData();
        PerfData.clearData();
        EventDispatcher.clearData();
        TracePanel.currentSelectThreadIds.clear();
    }

    private void loadProcess(Integer pId) {
        CompletableFuture.runAsync(() -> {
            List<Process> processes = new ArrayList<>() {
            };
            Db.getInstance().query(Sql.QUERY_PROCESS, processes);
            AllData.processes = processes;
            SwingUtilities.invokeLater(() -> {
                if (Objects.nonNull(pId)) {
                    loadProcessWithPid(pId);
                } else {
                    loadProcessWithoutPid(processes);
                }
            });
        }, Utils.getPool()).whenComplete((unused, throwable) -> {
            if (Objects.nonNull(throwable)) {
                throwable.printStackTrace();
            }
        });
    }

    private void loadProcessWithPid(int pId) {
        processId = pId;
        tracePanel = new TracePanel();
        dataPanel = new DataPanel(AnalysisEnum.APP);
        setLayout(new MigLayout("insets 0"));
        JBSplitter splitter = new JBSplitter();
        splitter.setFirstComponent(tracePanel);
        splitter.setSecondComponent(dataPanel);
        add(splitter, "push,grow");
        List<Duration> dur = new ArrayList<>() {
        };
        Db.getInstance().query(Sql.QUERY_TOTAL_TIME, dur);
        if (dur.size() > 0) {
            TracePanel.DURATION = dur.get(0).getTotal();
            TracePanel.START_TS = dur.get(0).getStartTs();
            TracePanel.END_TS = dur.get(0).getEndTs();
            tracePanel.getRuler().refreshTimeRuler(TracePanel.DURATION);
        }
        List<CpuMax> cpuMaxList = new ArrayList<>() {
        };
        Db.getInstance().query(Sql.QUERY_CPU_MAX, cpuMaxList);
        if (cpuMaxList.size() > 0) {
            cpuMaxList.get(0).getCpu();
        }
        if (Objects.nonNull(cpuName) && !cpuName.isEmpty()) {
            insertCpuScale();
        }
        int cpuMax = 0;
        insertDisplay();
        insertCpu(cpuMax);
        insertProcess();
        tip.setJLayeredPane(AppTracePanel.this.getRootPane().getLayeredPane());
    }

    private void loadProcessWithoutPid(List<Process> processes) {
        Object processObj = JOptionPane
            .showInputDialog(JOptionPane.getRootFrame(), "select process", "process", JOptionPane.PLAIN_MESSAGE, null,
                processes.toArray(new Object[0]), processes.size() > 0 ? processes.get(0).toString() : "");
        if (processObj instanceof Process) {
            Process process = (Process) processObj;
            processId = process.getPid();
            tracePanel = new TracePanel();
            dataPanel = new DataPanel(AnalysisEnum.APP);
            setLayout(new MigLayout("insets 0"));
            JBSplitter splitter = new JBSplitter();
            splitter.setFirstComponent(tracePanel);
            splitter.setSecondComponent(dataPanel);
            add(splitter, "push,grow");
            if (Objects.nonNull(process)) {
                List<Duration> dur = new ArrayList<>() {
                };
                Db.getInstance().query(Sql.QUERY_TOTAL_TIME, dur);
                TracePanel.DURATION = dur.get(0).getTotal();
                TracePanel.START_TS = dur.get(0).getStartTs();
                TracePanel.END_TS = dur.get(0).getEndTs();
                tracePanel.getRuler().refreshTimeRuler(TracePanel.DURATION);
                List<CpuMax> cpuMaxList = new ArrayList<>() {
                };
                Db.getInstance().query(Sql.QUERY_CPU_MAX, cpuMaxList);
                int cpuMax = cpuMaxList.get(0).getCpu();
                if (Objects.nonNull(cpuName) && !cpuName.isEmpty()) {
                    insertCpuScale();
                }
                insertDisplay();
                insertCpu(cpuMax);
                insertProcess();
                tip.setJLayeredPane(AppTracePanel.this.getRootPane().getLayeredPane());
            }
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
            } catch (IOException exception) {
                exception.printStackTrace();
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

    private double getCpuScale(CpuPluginResult.CpuUsageInfo cpuUsageInfo) {
        return (cpuUsageInfo.getProcessCpuTimeMs() - cpuUsageInfo.getPrevProcessCpuTimeMs()) * 1.0 / (
            cpuUsageInfo.getSystemBootTimeMs() - cpuUsageInfo.getPrevSystemBootTimeMs());
    }

    private void insertDisplay() {
        ExpandPanel panel = new ExpandPanel("Display");
        TraceSimpleRow<Frame> frames = new TraceSimpleRow("Frames");
        panel.addTraceRow(frames);
        insertFrame(frames);

        TraceSimpleRow<DisplayFunc> surfaceflinger = new TraceSimpleRow("Surfaceflinger");
        panel.addTraceRow(surfaceflinger);
        insertSurface(surfaceflinger);

        TraceSimpleRow<VsyncAppBean> vsync = new TraceSimpleRow("VSYNC");
        panel.addTraceRow(vsync);
        insertVsyc(vsync);

        tracePanel.getContentPanel().add(panel, "pushx,growx");
    }

    private void insertFrame(TraceSimpleRow<Frame> frames) {
        frames.setSupplier(() -> {
            List<Thread> renderThreadList = new ArrayList<>() {
            };
            List<Thread> frameThreadList = new ArrayList<>() {
            };
            int pid = processId;
            List<Func> renderFuncList = new ArrayList<>() {
            };
            Db.getInstance().query(Sql.GET_THREAD_FUNC_BY_NAME, renderFuncList, pid);
            if (renderFuncList.size() > 0) {
                Db.getInstance().query(Sql.QUERY_THREAD_DATA, renderThreadList, renderFuncList.get(0).getTid());
            }
            Db.getInstance().query(Sql.QUERY_THREAD_DATA, frameThreadList, pid);
            ArrayList<Func> frameFuncList = new ArrayList<>() {
            };
            Db.getInstance().query(Sql.GET_FUN_DATA_BY_TID, frameFuncList, pid);
            setFuncIdel(renderFuncList, renderThreadList);
            setFuncIdel(frameFuncList, frameThreadList);
            List<Frame> frameList = new ArrayList<>();
            frameFuncList.stream().filter(frameFunc -> frameFunc.getFuncName().contains("doFrame")).forEach(func -> {
                Frame frame = new Frame(func);
                frame.setRenderList(renderFuncList.stream().filter(renderFunc -> TimeUtils
                    .isInRange(func.getStartTs(), func.getStartTs() + func.getDur(), renderFunc.getStartTs(),
                        renderFunc.getStartTs() + renderFunc.getDur())).collect(Collectors.toList()));
                frameList.add(frame);
            });
            return frameList;
        });
        frames.setRender((graphics2D, data) -> {
            data.forEach(node -> {
                node.setRect(frames.getRectByNode(node, 20));
                node.draw(graphics2D);
            });
        });
    }

    private void insertSurface(TraceSimpleRow<DisplayFunc> surfaceflinger) {
        surfaceflinger.setRender((graphics2D, data) -> {
            data.forEach(node -> {
                node.setRect(surfaceflinger.getRectByNode(node, 5, 20));
                node.draw(graphics2D);
            });
        });
        surfaceflinger.setSupplier(() -> {
            Process sfProcess = AllData.processes.stream()
                .filter(process -> Objects.equals(process.getName(), "/system/bin/surfaceflinger")).findAny()
                .orElse(null);
            List<Func> flingerThreads = new ArrayList<>() {
            };
            if (sfProcess == null) {
                return new ArrayList<>();
            } else {
                Db.getInstance().query(Sql.GET_FUN_DATA_BY_TID, flingerThreads, sfProcess.getPid());
                flingerThreads =
                    flingerThreads.stream().filter(func -> func.getDepth() == 0).collect(Collectors.toList());
                return flingerThreads.stream().map(DisplayFunc::new).collect(Collectors.toList());
            }
        });
    }

    private void insertVsyc(TraceSimpleRow<VsyncAppBean> vsync) {
        vsync.setRender((g2, data) -> {
            data.stream().filter(data::contains).forEach(node -> {
                node.setRect(vsync.getRectByNode(node));
                node.draw(g2);
            });
        });
        vsync.setSupplier(() -> {
            List<VsyncAppBean> tracks = new ArrayList<>() {
            };
            Db.getInstance().query(Sql.QUERY_VSYNC_APP, tracks);
            for (int idx = 0, len = tracks.size(); idx < len; idx++) {
                VsyncAppBean it = tracks.get(idx);
                if (idx == len - 1) {
                    it.setDuration(AnalystPanel.getDURATION() - it.getStartTime());
                } else {
                    it.setDuration(tracks.get(idx + 1).getStartTime() - it.getStartTime());
                }
            }
            return tracks;
        });
    }

    private void insertCpu(int num) {
        List<CpuFreqMax> cpuFreqMaxList = new ArrayList<>() {
        };
        Db.getInstance().query(Sql.QUERY_CPU_MAX_FREQ, cpuFreqMaxList);
        CpuFreqMax freqMax = cpuFreqMaxList.get(0).math();
        ExpandPanel panel = new ExpandPanel("CPU cores (" + (num + 1) + ")");
        IntStream.rangeClosed(0, num).forEachOrdered(index -> {
            TraceSimpleRow<Cpu> row = new TraceSimpleRow("CPU " + index);
            TraceSimpleRow<CpuFreq> rowFreq = new TraceSimpleRow("CPU " + index + " Frequency");
            insertCpuRow(row, index);
            insertCpuRowFreq(rowFreq, index, row, freqMax);
            panel.addTraceRow(row);
            panel.addTraceRow(rowFreq);
        });
        tracePanel.getContentPanel().add(panel, "growx,pushx");
    }

    private void insertCpuRowFreq(TraceSimpleRow<CpuFreq> rowFreq, int index, TraceSimpleRow<Cpu> row,
        CpuFreqMax freqMax) {
        rowFreq.setSupplier(() -> {
            List<CpuFreq> list = new ArrayList<>() {
            };
            Db.getInstance().query(Sql.QUERY_CPU_FREQ_DATA, list, index);
            for (int idx = 0, len = list.size(); idx < len; idx++) {
                CpuFreq cpuGraph = list.get(idx);
                if (idx == len - 1) {
                    cpuGraph.setDuration(AnalystPanel.getDURATION() - cpuGraph.getStartTime());
                } else {
                    cpuGraph.setDuration(list.get(idx + 1).getStartTime() - cpuGraph.getStartTime());
                }
            }
            return list;
        });
        rowFreq.setRender((g2, data) -> {
            data.stream().filter(item -> row.contains(item)).forEach(node -> {
                node.setRect(row.getRectByNode(node));
                node.setMax(freqMax.getValue());
                node.draw(g2);
            });
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)); // transparency
            Rectangle2D bounds = g2.getFontMetrics().getStringBounds(freqMax.getName(), g2);
            g2.setColor(Color.lightGray);
            g2.drawString(freqMax.getName(), 2, (int) (Utils.getY(row.getContentBounds()) + 2 + bounds.getHeight()));
        });
    }

    private void insertCpuRow(TraceSimpleRow<Cpu> row, int index) {
        row.setSupplier(() -> {
            List<Cpu> cpus = new ArrayList<>() {
            };
            int count =
                Db.getInstance().queryCount(Sql.QUERY_CPU_DATA_COUNT, index, TracePanel.startNS, TracePanel.endNS);
            if (count > Final.CAPACITY) {
                Db.getInstance()
                    .query(Sql.QUERY_CPU_DATA_LIMIT, cpus, index, TracePanel.startNS, TracePanel.endNS, Final.CAPACITY);
            } else {
                Db.getInstance().query(Sql.QUERY_CPU_DATA, cpus, index, TracePanel.startNS, TracePanel.endNS);
            }
            AllData.CPU_MAP.put(index, cpus);
            return cpus;
        });
        row.setRender((g2, data1) -> {
            data1.stream().filter(row::contains).forEach(node -> {
                node.setRect(row.getRectByNode(node, 5));
                node.draw(g2);
            });
        });
    }

    private void insertProcess() {
        List<Thread> threads = new ArrayList<>() {
        };
        Db.getInstance().query(Sql.QUERY_THREADS_BY_PID, threads, processId);
        AllData.threadNames = threads.stream().collect(Collectors.toMap(th -> th.getTid(), th -> th.getThreadName()));
        ExpandPanel panel = new ExpandPanel("Threads (" + threads.size() + ")");
        for (Thread thread : threads) {
            TraceThreadRow<Thread, Func> row = new TraceThreadRow<>(thread.getThreadName(), thread.getTid());
            row.setRender((g2, data1, data2) -> {
                data1.stream().filter(item -> row.contains(item)).forEach(th -> {
                    th.setRect(row.getRectByNode(th, row.getThreadHeight()));
                    th.draw(g2);
                });
                if (data2 != null) {
                    data2.stream().filter(item -> row.contains(item)).forEach(func -> {
                        func.setRect(row.getRectByNode(func, row.getFuncHeight(), row.getFuncHeight()));
                        func.draw(g2);
                    });
                }
            });
            row.setSupplier(() -> {
                List<Thread> threadList = new ArrayList<>() {
                };
                Db.getInstance().query(Sql.QUERY_THREAD_DATA, threadList, thread.getTid());
                AllData.THREAD_MAP.put(thread.getTid(), threadList);
                return threadList;
            });
            handleRowSupplier(panel, thread, row);
            panel.addTraceRow(row);
        }
        tracePanel.getContentPanel().add(panel, "pushx,growx");
    }

    private void handleRowSupplier(ExpandPanel panel, Thread thread, TraceThreadRow<Thread, Func> row) {
        row.setSupplier2(() -> {
            List<Func> funcs = new ArrayList<>() {
            };
            Db.getInstance().query(Sql.GET_FUN_DATA_BY_TID, funcs, thread.getTid());
            if (AllData.THREAD_MAP.containsKey(thread.getTid())) {
                List<Thread> threadList = AllData.THREAD_MAP.get(thread.getTid());
                setFuncIdel(funcs, threadList);
                Func threadFunc = new Func();
                threadFunc.setFuncName(thread.getThreadName());
                threadFunc.setThreadName(thread.getThreadName());
                threadFunc.setTid(thread.getTid());
                threadFunc.setDepth(-1);
                threadFunc.setBloodId(Utils.md5String(thread.getThreadName()));
                threadFunc.setStartTs(0);
                threadFunc.setEndTs(TracePanel.END_TS - TracePanel.START_TS);
                funcs.add(threadFunc);
                List<Func> sortedList = funcs.stream().filter((item) -> item.getDepth() != -1)
                    .sorted(Comparator.comparingInt(Func::getDepth)).collect(Collectors.toList());
                Map<Integer, Func> map = funcs.stream().collect(Collectors.toMap(Func::getId, func -> func));
                sortedList.forEach((func) -> {
                    func.setThreadName(thread.getThreadName());
                    if (func.getDepth() != 0) {
                        func.setParentBloodId(map.get(func.getParentId()).getBloodId());
                    }
                    func.setEndTs(func.getStartTs() + func.getDur());
                    func.createBloodId();
                });
            }
            AllData.FUNC_MAP.put(thread.getTid(), funcs);
            int maxDept = funcs.stream().mapToInt(Func::getDepth).max().orElse(0) + 1;
            int maxHeight = maxDept * row.getFuncHeight() + row.getFuncHeight();
            if (maxHeight < 30) {
                maxHeight = 30;
            }
            row.setMaxDept(maxDept);
            if (panel.getContent().getLayout() instanceof MigLayout) {
                ((MigLayout) panel.getContent().getLayout())
                    .setComponentConstraints(row, "growx,pushx,h " + maxHeight + "!");
            }
            row.updateUI();
            return funcs;
        });
    }

    private void setFuncIdel(List<Func> funcList, List<Thread> threadList) {
        if (funcList.size() > 0 && threadList.size() > 0) {
            funcList.forEach(func -> func.setIdle(threadList.stream().filter(threadValue -> TimeUtils
                .isInRange(func.getStartTs(), func.getEndTs(), threadValue.getStartTime(),
                    threadValue.getStartTime() + threadValue.getDuration()) && !"Running"
                .equals(threadValue.getState())).mapToLong(Thread::getDuration).sum()));
        }
    }
}
