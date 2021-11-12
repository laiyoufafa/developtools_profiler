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

import com.intellij.ui.AncestorListenerAdapter;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SideBorder;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBInsets;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.trace.Sql;
import ohos.devtools.views.trace.bean.AsyncEvent;
import ohos.devtools.views.trace.bean.Clock;
import ohos.devtools.views.trace.bean.ClockData;
import ohos.devtools.views.trace.bean.Cpu;
import ohos.devtools.views.trace.bean.CpuData;
import ohos.devtools.views.trace.bean.CpuFreqData;
import ohos.devtools.views.trace.bean.CpuFreqMax;
import ohos.devtools.views.trace.bean.CpuMax;
import ohos.devtools.views.trace.bean.Duration;
import ohos.devtools.views.trace.bean.FlagBean;
import ohos.devtools.views.trace.bean.FunctionBean;
import ohos.devtools.views.trace.bean.Process;
import ohos.devtools.views.trace.bean.ProcessMem;
import ohos.devtools.views.trace.bean.ThreadData;
import ohos.devtools.views.trace.bean.WakeupBean;
import ohos.devtools.views.trace.bean.WakeupTime;
import ohos.devtools.views.trace.fragment.AbstractDataFragment;
import ohos.devtools.views.trace.fragment.AsyncEventDataFragment;
import ohos.devtools.views.trace.fragment.ClockDataFragment;
import ohos.devtools.views.trace.fragment.CpuDataFragment;
import ohos.devtools.views.trace.fragment.CpuFreqDataFragment;
import ohos.devtools.views.trace.fragment.FunctionDataFragment;
import ohos.devtools.views.trace.fragment.MemDataFragment;
import ohos.devtools.views.trace.fragment.ProcessDataFragment;
import ohos.devtools.views.trace.fragment.ThreadDataFragment;
import ohos.devtools.views.trace.listener.IFlagListener;
import ohos.devtools.views.trace.util.Db;
import ohos.devtools.views.trace.util.TimeUtils;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_D;
import static java.awt.event.KeyEvent.VK_F;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_SHIFT;
import static java.awt.event.KeyEvent.VK_W;

/**
 * Analysis component
 *
 * @since 2021/04/20 12:24
 */
public final class AnalystPanel extends JBPanel
    implements MouseWheelListener, KeyListener, MouseListener, MouseMotionListener {
    /**
     * rect clicked
     */
    public static boolean clicked = false;

    /**
     * cpu click listener
     */
    public static ICpuDataClick iCpuDataClick;

    /**
     * thread click listener
     */
    public static IThreadDataClick iThreadDataClick;

    /**
     * function click listener
     */
    public static IFunctionDataClick iFunctionDataClick;

    /**
     * clock data click listener
     */
    public static IClockDataClick iClockDataClick;

    /**
     * flag click listener
     */
    public static IFlagClick iFlagClick;

    /**
     * cpu data list
     */
    public static List<List<CpuData>> cpuList;

    /**
     * cpu freg data list
     */
    public static List<List<CpuFreqData>> cpuFreqList;

    /**
     * thread data list
     */
    public static List<ThreadData> threadsList;

    /**
     * duration
     */
    public static long DURATION = 10_000_000_000L;

    /**
     * cpu count
     */
    public static int cpuNum;

    /**
     * layered pane
     */
    public static JLayeredPane layeredPane;

    /**
     * function click listener
     */
    private static IAsyncFunctionDataClick iAsyncFunctionDataClick;

    /**
     * bottom tab
     */
    public TabPanel tab;
    private final JBScrollPane scrollPane = new JBScrollPane();
    private final int defaultFragmentHeight = 40;
    private final double defaultScale = 0.1;
    private ContentPanel contentPanel;
    private TimeViewPort viewport;
    private double wheelSize;
    private double rangeNs;
    private double lefPercent;
    private double rightPercent;
    private long startNS;
    private boolean isUserInteraction;
    private Cursor wCursor = new Cursor(Cursor.W_RESIZE_CURSOR);
    private Cursor eCursor = new Cursor(Cursor.E_RESIZE_CURSOR);
    private Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
    private Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    private TabLogTable tabLogTable = new TabLogTable();

    /**
     * Constructor
     */
    public AnalystPanel() {
        setLayout(new MigLayout("insets 0"));
        viewport = new TimeViewPort(height -> viewport.setBorder(null), (sn, en) -> {
            /**
             * When the time axis range changes,
             * the contentPanel is notified that all data is refreshed according to the time axis
             */
            contentPanel.rangeChange(sn, en);
            tabLogTable.rangeChange(sn, en);
        });
        setBorder(null);
        contentPanel = new ContentPanel(this);
        contentPanel.setBorder(null);
        viewport.setView(contentPanel);
        scrollPane.setViewport(viewport);
        scrollPane.setBorder(null);
        tab = new TabPanel();
        tab.setFocusable(true);
        tab.setVisible(false);
        tab.setBorder(IdeBorderFactory.createBorder(SideBorder.NONE));
        tab.setTabComponentInsets(JBInsets.create(0, 0));
        addAncestorListener(new AncestorListenerAdapter() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                super.ancestorAdded(event);
                layeredPane = AnalystPanel.this.getRootPane().getLayeredPane();
                layeredPane.add(tab);
                layeredPane.setLayer(tab, JLayeredPane.UNDEFINED_CONDITION);
                layeredPane.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent event) {
                        super.componentResized(event);
                        tab.setBounds(0, 0, 0, 0);
                    }
                });
                contentPanel.requestFocusInWindow();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                super.ancestorRemoved(event);
                if (Objects.nonNull(tab)) {
                    tab.hidden();
                }
            }
        });
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                super.componentResized(event);
                viewport.setRootHeight(getHeight());
                contentPanel.repaint();
            }
        });
        add(scrollPane, "push,grow");
        contentPanel.setFocusable(true);
        contentPanel.addMouseMotionListener(this);
        contentPanel.addMouseListener(this);
        contentPanel.addKeyListener(this);
        iCpuDataClick = cpu -> clickCpuData(cpu);
        iThreadDataClick = thread -> clickThreadData(thread);
        iFunctionDataClick = fun -> clickFunctionData(fun);
        iClockDataClick = clock -> clickClockData(clock);
        iFlagClick = flag -> clickTimeFlag(flag);
        iAsyncFunctionDataClick = data -> clickAsyncFunctionData(data);
    }

    static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return obj -> seen.putIfAbsent(keyExtractor.apply(obj), Boolean.TRUE) == null;
    }

    /**
     * add cpu data list
     *
     * @param list source
     */
    public void addCpuList(final List<List<CpuData>> list) {
        if (list == null) {
            return;
        }
        cpuList = list;
        cpuNum = list.size();
        for (int index = 0; index < list.size(); index++) {
            List<CpuData> dataList = list.get(index);
            contentPanel.addDataFragment(new CpuDataFragment(contentPanel, index, dataList));
        }
    }

    /**
     * add cpu freg data list
     *
     * @param list       source
     * @param cpuMaxFreq cpu size
     */
    public void addCpuFreqList(final List<List<CpuFreqData>> list, final CpuFreqMax cpuMaxFreq) {
        cpuFreqList = list;
        for (int index = 0; index < list.size(); index++) {
            List<CpuFreqData> dataList = list.get(index);

            // Fill in the duration field in FreqData and calculate based on the start time of the next node
            for (int idx = 0, len = dataList.size(); idx < len; idx++) {
                CpuFreqData cpuGraph = dataList.get(idx);
                if (idx == len - 1) {
                    cpuGraph.setDuration(AnalystPanel.DURATION - cpuGraph.getStartTime());
                } else {
                    cpuGraph.setDuration(dataList.get(idx + 1).getStartTime() - cpuGraph.getStartTime());
                }
            }
            contentPanel.addDataFragment(
                new CpuFreqDataFragment(contentPanel, "Cpu " + index + " Frequency", cpuMaxFreq, dataList));
        }
    }

    /**
     * add thread data list
     *
     * @param list        thread list
     * @param processMem  process list
     * @param asyncEvents asyncEvents
     */
    public void addThreadsList(final List<ThreadData> list, final List<ProcessMem> processMem,
        List<AsyncEvent> asyncEvents) {
        /*
         * If the list has no data (obtained by memory sorting),
         * then directly query the process and thread tables to find the data
         */
        if (list.isEmpty()) {
            Db.getInstance().query(Sql.SYS_QUERY_PROCESS_THREADS_NORDER, list);
        }
        threadsList = list;
        List<Process> processes = new ArrayList<>() {
        };
        Db.getInstance().query(Sql.SYS_QUERY_PROCESS, processes);
        if (processes.isEmpty()) {
            Db.getInstance().query(Sql.SYS_QUERY_PROCESS_NORDER, processes);
        }
        for (Process process : processes) {
            if (process.getPid() == 0) {
                continue;
            }
            ProcessDataFragment processDataFragment = new ProcessDataFragment(contentPanel, process);
            contentPanel.addDataFragment(processDataFragment);
            processMem.stream().filter(mem -> mem.getPid() == process.getPid()).forEach(mem -> {
                MemDataFragment fgr = new MemDataFragment(contentPanel, mem);
                fgr.defaultHeight = defaultFragmentHeight;
                fgr.parentUuid = processDataFragment.uuid;
                fgr.visible = false;
                contentPanel.addDataFragment(fgr);
            });
            asyncEvents.stream().filter(it -> it.getPid().equals(process.getPid()))
                .filter(distinctByKey(it -> it.getName())).forEach(it -> {
                    List<AsyncEvent> collect = asyncEvents.stream()
                        .filter(wt -> wt.getPid().equals(it.getPid()) && wt.getName().equals(it.getName()))
                        .collect(Collectors.toList());
                    AsyncEventDataFragment fgr = new AsyncEventDataFragment(contentPanel, it, collect);
                    int maxHeight = (collect.stream().mapToInt(bean -> bean.getDepth()).max().getAsInt() + 1) * 20;
                    fgr.defaultHeight = maxHeight + 20;
                    fgr.parentUuid = processDataFragment.uuid;
                    fgr.visible = false;
                    contentPanel.addDataFragment(fgr);
                });
            List<ThreadData> collect = list.stream().filter(
                threadData -> threadData.getPid() == process.getPid() && threadData.getTid() != 0
                    && threadData.getThreadName() != null).collect(Collectors.toList());
            for (ThreadData data : collect) {
                ThreadDataFragment fgr = new ThreadDataFragment(contentPanel, data);
                fgr.defaultHeight = defaultFragmentHeight;
                fgr.parentUuid = processDataFragment.uuid;
                fgr.visible = false;
                contentPanel.addDataFragment(fgr);
            }
        }
        // If you use the memory method to find the data, you need to execute the following code
        if (!list.isEmpty()) {
            addThreadsList2(list);
        }
    }

    private void addThreadsList2(List<ThreadData> list) {
        list.stream().filter(data -> data.getProcessName() == null || data.getProcessName().isEmpty())
            .forEach(threadData -> {
                Process process = new Process();
                process.setPid(threadData.getTid());
                process.setName(threadData.getThreadName());
                ProcessDataFragment processDataFragment = new ProcessDataFragment(contentPanel, process);
                contentPanel.addDataFragment(processDataFragment);
                if (process.getName() == null) {
                    process.setName("Process");
                }
                if (!process.getName().startsWith("swapper") && process.getPid() != 0) {
                    ThreadDataFragment fgr = new ThreadDataFragment(contentPanel, threadData);
                    fgr.defaultHeight = defaultFragmentHeight;
                    fgr.parentUuid = processDataFragment.uuid;
                    fgr.visible = false;
                    contentPanel.addDataFragment(fgr);
                }
            });
    }

    private void recycle() {
        Utils.resetPool();
        if (cpuList != null) {
            cpuList.forEach(List::clear);
            cpuList.clear();
        }
        if (cpuFreqList != null) {
            cpuFreqList.forEach(List::clear);
            cpuFreqList.clear();
        }
        if (threadsList != null) {
            threadsList.clear();
        }
    }

    /**
     * load database
     *
     * @param name    db name
     * @param isLocal is local db
     */
    public void load(final String name, final boolean isLocal) {
        recycle();
        Db.setDbName(name);
        Db.load(isLocal);
        tab.hidden();
        CompletableFuture.runAsync(() -> {
            loadDur();
            List<List<CpuData>> list = loadCpuData();
            List<List<CpuFreqData>> freqList = loadCpuFreqData();
            // Add the memory information of the process
            List<ProcessMem> processMem = new ArrayList<>() {
            };
            Db.getInstance().query(Sql.SYS_GET_PROCESS_MEM, processMem);
            List<AsyncEvent> asyncEvents = new ArrayList<>() {
            };
            Db.getInstance().query(Sql.SYS_GET_ASYNC_EVENTS, asyncEvents);
            // Add thread information
            List<ThreadData> processThreads = new ArrayList<>() {
            };
            Db.getInstance().query(Sql.SYS_QUERY_PROCESS_THREADS, processThreads);
            List<CpuFreqMax> cpuFreqMaxList = new ArrayList<>() {
            };
            Db.getInstance().query(Sql.SYS_QUERY_CPU_MAX_FREQ, cpuFreqMaxList);
            SwingUtilities.invokeLater(() -> {
                viewport.recycle();
                viewport.rulerFragment.setRange(0, AnalystPanel.DURATION, 0);
                viewport.rulerFragment.recycle();
                viewport.cpuFragment.reloadData();
                contentPanel.recycle();
                addCpuList(list);
                if (cpuFreqMaxList.size() > 0) {
                    addCpuFreqList(freqList, cpuFreqMaxList.get(0).math());
                }
                ArrayList<Clock> clocks = new ArrayList<>() {
                };
                Db.getInstance().query(Sql.SYS_QUERY_CLOCK_LIST, clocks);
                addClock(clocks);
                addThreadsList(processThreads, processMem, asyncEvents); // The memory information of the process and
                // The thread information of the process is displayed together
                // load logs
                Rectangle b3 =
                    SwingUtilities.convertRectangle(scrollPane, scrollPane.getBounds(), layeredPane);
                tab.removeAll();
                tab.display(b3);
                tab.add("Logs", tabLogTable);
                tab.hideInBottom();
                tabLogTable.query(true);
                tabLogTable.requestFocusInWindow();
                contentPanel.refresh();
            });
        }, Utils.getPool()).whenComplete((unused, throwable) -> {
            if (Objects.nonNull(throwable)) {
                throwable.printStackTrace();
            }
        });
    }

    private void addClock(ArrayList<Clock> clocks) {
        if (!clocks.isEmpty()) {
            Clock screenState = new Clock();
            screenState.setName("ScreenState");
            screenState.setSrcname("ScreenState");
            contentPanel.addDataFragment(new ClockDataFragment(contentPanel, screenState));
            clocks.forEach(it -> {
                contentPanel.addDataFragment(new ClockDataFragment(contentPanel, it));
            });
        }
    }

    private void loadDur() {
        List<Duration> dur = new ArrayList<>() {
        };
        Db.getInstance().query(Sql.SYS_QUERY_TOTAL_TIME, dur);
        if (dur != null && dur.size() > 0) {
            DURATION = dur.get(0).getTotal();
        }
    }

    private List<List<CpuData>> loadCpuData() {
        List<CpuMax> cpuMaxes = new ArrayList<>() {
        };
        Db.getInstance().query(Sql.SYS_QUERY_CPU_MAX, cpuMaxes);
        if (cpuMaxes.isEmpty()) {
            return new ArrayList<>();
        }
        int cpuMax = cpuMaxes.get(0).getCpu();
        List<List<CpuData>> list = new ArrayList<>();
        for (int index = 0; index <= cpuMax; index++) {
            List<CpuData> cpuData = new ArrayList<>() {
            };
            list.add(cpuData);
        }
        return list;
    }

    private List<List<CpuFreqData>> loadCpuFreqData() {
        List<Cpu> cpus = new ArrayList<>() {
        };
        Db.getInstance().query(Sql.SYS_QUERY_CPU_FREQ, cpus);
        List<List<CpuFreqData>> freqList = cpus.stream().map(it -> {
            List<CpuFreqData> cpuFreqData = new ArrayList<>() {
            };
            Db.getInstance().query(Sql.SYS_QUERY_CPU_FREQ_DATA, cpuFreqData, it.getCpu());
            return cpuFreqData;
        }).collect(Collectors.toList());
        return freqList;
    }

    /**
     * The bottom tab is displayed when the select event is clicked.
     *
     * @param cpus      select cpu list（）
     * @param threadIds select thread id list
     * @param trackIds  select mem track id list
     * @param ns        select start time ns and end time ns
     * @param funTids   funTids
     */
    public void boxSelection(final List<Integer> cpus, final List<Integer> threadIds, final List<Integer> trackIds,
        final List<Integer> funTids, final LeftRightNS ns) {
        if (cpus.isEmpty() && threadIds.isEmpty() && trackIds.isEmpty() && funTids.isEmpty()) {
            tab.hidden();
            return;
        }
        SwingUtilities.invokeLater(() -> {
            Rectangle b3 =
                SwingUtilities.convertRectangle(scrollPane, scrollPane.getBounds(), layeredPane);
            tab.display(b3);
            tab.removeAll();
            if (cpus != null && !cpus.isEmpty()) {
                TabCpuByThread cpuThread = new TabCpuByThread();
                TabCpuByProcess cpuProcess = new TabCpuByProcess();
                cpuThread.loadTabData(cpus, ns.getLeftNs(), ns.getRightNs());
                cpuProcess.loadTabData(cpus, ns.getLeftNs(), ns.getRightNs());
                tab.add("CPU by thread", cpuThread);
                tab.add("CPU by process", cpuProcess);
            }
            if (threadIds != null && !threadIds.isEmpty()) {
                TabThreadStates threadStatesTab = new TabThreadStates();
                threadStatesTab.loadTabData(threadIds, ns.getLeftNs(), ns.getRightNs());
                tab.add("Thread States", threadStatesTab);
            }
            if (funTids != null && !funTids.isEmpty()) {
                TabSlices slicesTab = new TabSlices();
                slicesTab.loadTabData(funTids, ns.getLeftNs(), ns.getRightNs());
                tab.add("Slices", slicesTab);
            }
            if (trackIds != null && !trackIds.isEmpty()) {
                TabCounter counterTab = new TabCounter();
                counterTab.loadTabData(trackIds, ns.getLeftNs(), ns.getRightNs());
                tab.add("Counters", counterTab);
            }
            tab.add("Logs", tabLogTable);
            tabLogTable.query(true);
        });
    }

    /**
     * The bottom tab is displayed when the method event is clicked.
     *
     * @param bean function
     */
    public void clickFunctionData(final FunctionBean bean) {
        cancelRangeSelect();
        ArrayList<ScrollSlicePanel.SliceData> dataSource = new ArrayList<>();
        dataSource.add(ScrollSlicePanel.createSliceData("Name", bean.getFunName(), false));
        dataSource.add(ScrollSlicePanel.createSliceData("Category", bean.getCategory(), false));
        dataSource.add(ScrollSlicePanel.createSliceData("StartTime",
            TimeUtils.getTimeString(bean.getStartTime()) + "", false));
        dataSource.add(ScrollSlicePanel.createSliceData("Duration",
            TimeUtils.getTimeString(bean.getDuration()) + "", false));
        SwingUtilities.invokeLater(() -> {
            Rectangle b3 =
                SwingUtilities.convertRectangle(scrollPane, scrollPane.getBounds(), layeredPane);
            tab.display(b3);
            tab.removeAll();
            ScrollSlicePanel ssp = new ScrollSlicePanel();
            ssp.setData("Slice Details", dataSource, null);
            tab.add("Current Selection", ssp);
            tab.add("Logs", tabLogTable);
            tabLogTable.query(true);
        });
    }

    /**
     * The bottom tab is displayed when the method event is clicked.
     *
     * @param bean function
     */
    public void clickAsyncFunctionData(final AsyncEvent bean) {
        cancelRangeSelect();
        ArrayList<ScrollSlicePanel.SliceData> dataSource = new ArrayList<>();
        dataSource.add(ScrollSlicePanel.createSliceData("Name", bean.getName(), false));
        dataSource.add(ScrollSlicePanel.createSliceData("Cookie", bean.getCookie().toString(), false));
        dataSource.add(ScrollSlicePanel.createSliceData("StartTime",
            TimeUtils.getTimeString(bean.getStartTime()) + "", false));
        dataSource.add(ScrollSlicePanel.createSliceData("Duration",
            TimeUtils.getTimeString(bean.getDuration()) + "", false));
        SwingUtilities.invokeLater(() -> {
            Rectangle b3 =
                SwingUtilities.convertRectangle(scrollPane, scrollPane.getBounds(), layeredPane);
            tab.display(b3);
            tab.removeAll();
            ScrollSlicePanel ssp = new ScrollSlicePanel();
            ssp.setData("Slice Details", dataSource, null);
            tab.add("Current Selection", ssp);
            tab.add("Logs", tabLogTable);
            tabLogTable.query(true);
        });
    }

    /**
     * The bottom tab is displayed when the clock data event is clicked.
     *
     * @param clock clock data
     */
    public void clickClockData(final ClockData clock) {
        cancelRangeSelect();
        ArrayList<ScrollSlicePanel.SliceData> dataSource = new ArrayList<>();
        dataSource.add(ScrollSlicePanel.createSliceData("Start time",
            TimeUtils.getTimeString(clock.getStartTime()), false));
        dataSource.add(ScrollSlicePanel.createSliceData("Value",
            String.valueOf(clock.getValue()), false));
        dataSource.add(ScrollSlicePanel.createSliceData("Delta",
            String.valueOf(clock.getDelta()), false));
        dataSource.add(ScrollSlicePanel.createSliceData("Duration",
            TimeUtils.getTimeString(clock.getDuration()) + "", false));
        SwingUtilities.invokeLater(() -> {
            Rectangle b3 =
                SwingUtilities.convertRectangle(scrollPane, scrollPane.getBounds(), layeredPane);
            tab.display(b3);
            tab.removeAll();
            ScrollSlicePanel ssp = new ScrollSlicePanel();
            ssp.setData("Counter Details", dataSource, null);
            tab.add("Current Selection", ssp);
            tab.add("Logs", tabLogTable);
            tabLogTable.query(true);
        });
    }

    /**
     * When you click the CPU event, the bottom tab is displayed.
     *
     * @param threadData thread
     */
    public void clickThreadData(final ThreadData threadData) {
        cancelRangeSelect();
        ArrayList<ScrollSlicePanel.SliceData> dataSource = new ArrayList<>();
        dataSource.add(ScrollSlicePanel
            .createSliceData("StartTime", TimeUtils.getTimeString(threadData.getStartTime()) + "", false));
        dataSource.add(ScrollSlicePanel
            .createSliceData("Duration", TimeUtils.getTimeString(threadData.getDuration()) + "", false));
        String state = Utils.getEndState(threadData.getState());
        if ("Running".equals(Utils.getEndState(threadData.getState()))) {
            state = state + " on CPU " + threadData.getCpu();
        }
        dataSource.add(ScrollSlicePanel.createSliceData("State", state, true));
        String processName = threadData.getProcessName();
        if (processName == null || processName.isEmpty()) {
            processName = threadData.getThreadName();
        }
        dataSource
            .add(ScrollSlicePanel.createSliceData("Process", processName + " [" + threadData.getPid() + "]", false));
        SwingUtilities.invokeLater(() -> {
            Rectangle b3 =
                SwingUtilities.convertRectangle(scrollPane, scrollPane.getBounds(), layeredPane);
            tab.setRootRect(b3);
            tab.display(b3);
            tab.removeAll();
            ScrollSlicePanel ssp = new ScrollSlicePanel();
            ssp.setData("Thread State", dataSource, null);
            ssp.setScrollSliceLinkListener(bean -> {
                contentPanel.scrollToCpu(threadData.getCpu(), threadData.getStartTime());
            });
            tab.add("Current Selection", ssp);
            tab.add("Logs", tabLogTable);
            tabLogTable.query(true);
        });
    }

    /**
     * The bottom tab is displayed when you click the CPU event.
     *
     * @param cpu cpu
     */
    public void clickCpuData(final CpuData cpu) {
        cancelRangeSelect();
        ArrayList<ScrollSlicePanel.SliceData> dataSource = new ArrayList<>();
        String process = cpu.getProcessName();
        int processId = cpu.getProcessId();
        if (cpu.getProcessName() == null || cpu.getProcessName().isEmpty()) {
            process = cpu.getName();
            processId = cpu.getTid();
        }
        dataSource.add(ScrollSlicePanel.createSliceData("Process", process + " [" + processId + "]", false));
        dataSource.add(ScrollSlicePanel.createSliceData("Thread", cpu.getName() + " [" + cpu.getTid() + "]", true));
        dataSource.add(ScrollSlicePanel.createSliceData("CmdLine", cpu.getProcessCmdLine() + "", false));
        dataSource.add(
            ScrollSlicePanel.createSliceData("StartTime", TimeUtils.getTimeString(cpu.getStartTime()) + "", false));
        dataSource
            .add(ScrollSlicePanel.createSliceData("Duration", TimeUtils.getTimeString(cpu.getDuration()) + "", false));
        dataSource.add(ScrollSlicePanel.createSliceData("Prio", cpu.getPriority() + "", false));
        dataSource.add(ScrollSlicePanel.createSliceData("End State", Utils.getEndState(cpu.getEndState()), false));
        // wakeup description
        CompletableFuture.runAsync(() -> {
            Optional<WakeupBean> wb = queryWakeUpThread(cpu);
            SwingUtilities.invokeLater(() -> {
                contentPanel.setWakeupBean(wb.orElse(null));
                Rectangle b3 =
                    SwingUtilities.convertRectangle(scrollPane, scrollPane.getBounds(), layeredPane);
                tab.display(b3);
                tab.removeAll();
                ScrollSlicePanel ssp = new ScrollSlicePanel();
                ssp.setData("Slice Details", dataSource, wb.orElse(null));
                ssp.setScrollSliceLinkListener(bean -> {
                    contentPanel.scrollToThread(cpu.getProcessId(), cpu.getTid(), cpu.getStartTime(), tab.getHeight());
                });
                tab.add("Current Selection", ssp);
                tab.add("Logs", tabLogTable);
                tabLogTable.query(true);
                repaint();
            });
        }, Utils.getPool()).whenComplete((unused, throwable) -> {
            if (Objects.nonNull(throwable)) {
                throwable.printStackTrace();
            }
        });
    }

    private Optional<WakeupBean> queryWakeUpThread(final CpuData cpuData) {
        WakeupBean wb = null;
        List<WakeupTime> result = new ArrayList<>() {
        };
        Db.getInstance()
            .query(Sql.SYS_GET_WAKEUP_TIME, result, cpuData.getId(), cpuData.getStartTime(), cpuData.getId(),
                cpuData.getStartTime());
        if (result != null && result.size() > 0) {
            WakeupTime wakeupTime = result.get(0);
            if (wakeupTime.getWakeTs() < wakeupTime.getPreRow()) {
                return Optional.ofNullable(wb);
            }
            List<WakeupBean> beans = new ArrayList<>() {
            };
            Db.getInstance().query(Sql.SYS_GET_WAKEUP_THREAD, beans, wakeupTime.getWakeTs(), wakeupTime.getWakeTs(),
                wakeupTime.getWakeTs());
            if (beans != null && beans.size() > 0) {
                wb = beans.get(0);
                wb.setWakeupTime(wakeupTime.getWakeTs() - wakeupTime.getStartTs());
                wb.setSchedulingLatency(cpuData.getStartTime() - wb.getWakeupTime());
                if (wb.getWakeupProcess() == null) {
                    wb.setWakeupProcess(wb.getWakeupThread());
                }
                if (wb.getWakeupPid() == null) {
                    wb.setWakeupPid(wb.getWakeupTid());
                }
                wb.setSchedulingDesc(Db.getSql("QueryWakeUpThread_Desc"));
            }
        }
        return Optional.ofNullable(wb);
    }

    /**
     * Evoking the red flag corresponds to the tabPanel at the bottom.
     *
     * @param flagBean flag
     */
    public void clickTimeFlag(final FlagBean flagBean) {
        cancelRangeSelect();
        clicked = true;
        layeredPane.setLayer(tab, javax.swing.JLayeredPane.DRAG_LAYER);
        ScrollFlagPanel flagPanel = new ScrollFlagPanel(flagBean);
        flagPanel.setFlagListener(new IFlagListener() {
            @Override
            public void flagRemove(final FlagBean flag) {
                flag.remove();
                viewport.rulerFragment.repaint();
                tab.removeAll();
                tab.hidden();
            }

            @Override
            public void flagChange(final FlagBean flag) {
                if (flag.getName() != null && !flag.getName().isEmpty()) {
                    flagBean.setName(flag.getName());
                }
                flagBean.setColor(flag.getColor());
                viewport.rulerFragment.repaint();
            }
        });
        SwingUtilities.invokeLater(() -> {
            Rectangle b3 =
                SwingUtilities.convertRectangle(scrollPane, scrollPane.getBounds(), layeredPane);
            tab.display(b3);
            tab.removeAll();
            tab.add("Current Selection", flagPanel);
            repaint();
        });
    }

    @Override
    public void keyTyped(final KeyEvent event) {
    }

    @Override
    public void keyPressed(final KeyEvent event) {
        switch (event.getExtendedKeyCode()) {
            case VK_A:
                wheelSize = viewport.rulerFragment.getScale() * -0.2;
                translation();
                break;
            case VK_D:
                wheelSize = viewport.rulerFragment.getScale() * 0.2;
                translation();
                break;
            case VK_W:
                wheelSize = viewport.rulerFragment.getScale() * -0.2;
                lefPercent = 0.5;
                scale();
                break;
            case VK_S:
                wheelSize = viewport.rulerFragment.getScale() * 0.2;
                lefPercent = 0.5;
                scale();
                break;
            case VK_F:
                keyPressedVKF(event);
                break;
            case VK_SHIFT:
            case VK_CONTROL:
                if (!isUserInteraction) {
                    isUserInteraction = true;
                    contentPanel.addMouseWheelListener(this);
                }
                break;
            default:
                break;
        }
    }

    private void keyPressedVKF(KeyEvent event) {
        if (event.isControlDown() || event.isMetaDown()) {
            for (AbstractDataFragment frg : viewport.favoriteFragments) {
                viewport.cancel(frg);
            }
            String keyword = Optional.ofNullable(JOptionPane
                    .showInputDialog(null, "Search for pid uid name",
                        "Search", JOptionPane.PLAIN_MESSAGE, null, null,
                        ""))
                .map(it -> it.toString().trim()).orElse("");
            if (keyword == null || keyword.isEmpty()) {
                keyPressedVKFEmptyKeyword();
            } else {
                contentPanel.fragmentList.stream().forEach(it -> {
                    if (it instanceof ThreadDataFragment) {
                        ThreadDataFragment fr1 = (ThreadDataFragment) it;
                        if (String.valueOf(fr1.thread.getTid()).equals(keyword)) {
                            it.setVisible(true);
                        } else if (fr1.thread.getThreadName() != null
                            && fr1.thread.getThreadName().indexOf(keyword) != -1) {
                            it.setVisible(true);
                        } else {
                            it.setVisible(false);
                        }
                    }
                    if (it instanceof ProcessDataFragment) {
                        ProcessDataFragment it1 = (ProcessDataFragment) it;
                        if (it1.getProcess().getName() != null && it1.getProcess().getName().indexOf(keyword) != -1) {
                            it1.getExpandGraph().setExpand(true);
                            it.setVisible(true);
                        } else if (it1.getProcess().getPid().toString().equals(keyword)) {
                            it1.getExpandGraph().setExpand(true);
                            it.setVisible(true);
                        } else {
                            it.setVisible(false);
                        }
                    }
                });
            }
            contentPanel.refresh();
        }
    }

    private void keyPressedVKFEmptyKeyword() {
        contentPanel.fragmentList.stream().forEach(it -> {
            if (it instanceof ThreadDataFragment || it instanceof MemDataFragment
                || it instanceof AsyncEventDataFragment || it instanceof FunctionDataFragment) {
                it.setVisible(false);
            } else {
                if (it instanceof ProcessDataFragment) {
                    ProcessDataFragment it1 = (ProcessDataFragment) it;
                    it1.getExpandGraph().setExpand(false);
                }
                it.setVisible(true);
            }
        });
    }

    @Override
    public void keyReleased(final KeyEvent event) {
        switch (event.getExtendedKeyCode()) {
            case VK_SHIFT:
            case VK_CONTROL:
                if (isUserInteraction) {
                    isUserInteraction = false;
                    contentPanel.removeMouseWheelListener(this);
                    contentPanel.fragmentList.forEach(it -> it.keyReleased(event));
                    contentPanel.fragmentList.forEach(it -> it.drawFrame());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
        clicked = false;
        viewport.mouseClicked(event);
        // Select the area, judge to click on the tab in the area to restore the display
        Rectangle rectangle = new Rectangle(contentPanel.x1, contentPanel.y1, contentPanel.x2 - contentPanel.x1,
            contentPanel.y2 - contentPanel.y1);
        if (rectangle.contains(event.getPoint())) {
            tab.display();
        } else {
            if (SwingUtilities.convertMouseEvent(contentPanel, event, AnalystPanel.this).getY() > TimeViewPort.height) {
                contentPanel.fragmentList.stream().filter(it -> it.getRect().contains(event.getPoint()))
                    .forEach(fragment -> fragment.mouseClicked(event));
            }
            if (!clicked) {
                tab.hidden();
                contentPanel.fragmentList.stream().findFirst().ifPresent(it -> {
                    it.clearSelected();
                });
                contentPanel.clearWakeupAndBoxSelect();
            }
        }
    }

    @Override
    public void mousePressed(final MouseEvent event) {
        viewport.mousePressed(event);
        if (SwingUtilities.convertMouseEvent(contentPanel, event, AnalystPanel.this).getY() > TimeViewPort.height) {
            Rectangle rect = new Rectangle(0, 0, viewport.getWidth(), TimeViewPort.height);
            if (!rect.contains(event.getPoint()) && Utils.getX(event.getPoint()) > 200) {
                if (getCursor().getType() == Cursor.DEFAULT_CURSOR) {
                    contentPanel.startPoint = event.getPoint();
                }
                contentPanel.drawRangeSelect = true;
                contentPanel.fragmentList.forEach(fragment -> fragment.mousePressed(event));
            }
        }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
        if (SwingUtilities.convertMouseEvent(contentPanel, event, AnalystPanel.this).getY() > TimeViewPort.height) {
            contentPanel.drawRangeSelect = false;
            contentPanel.fragmentList.stream().filter(it -> !(it instanceof CpuDataFragment))
                .forEach(fragment -> fragment.mouseReleased(event));
        } else {
            contentPanel.fragmentList.forEach(it -> it.mouseReleased(event));
            if (isUserInteraction) {
                isUserInteraction = false;
                contentPanel.fragmentList.forEach(it -> it.drawFrame());
            }
        }
        if (Objects.nonNull(contentPanel.startPoint) && Objects.nonNull(contentPanel.endPoint)) {
            if (contentPanel.startPoint.getX() > contentPanel.endPoint.getX()) {
                Point tmp = contentPanel.startPoint;
                contentPanel.startPoint = contentPanel.endPoint;
                contentPanel.endPoint = tmp;
            }
        }
        contentPanel.repaint();
    }

    @Override
    public void mouseEntered(final MouseEvent event) {
        if (SwingUtilities.convertMouseEvent(contentPanel, event, AnalystPanel.this).getY() > TimeViewPort.height) {
            contentPanel.fragmentList.forEach(fragment -> fragment.mouseEntered(event));
        }
    }

    @Override
    public void mouseExited(final MouseEvent event) {
        if (SwingUtilities.convertMouseEvent(contentPanel, event, AnalystPanel.this).getY() > TimeViewPort.height) {
            contentPanel.fragmentList.forEach(fragment -> fragment.mouseExited(event));
        }
    }

    @Override
    public void mouseDragged(final MouseEvent event) {
        viewport.mouseDragged(event);
        isUserInteraction = true;
        /**
         * If the drag range is in the area below the time axis,
         * the range selection function appears,and the tab box pops up
         */
        if (SwingUtilities.convertMouseEvent(contentPanel, event, AnalystPanel.this).getY() > TimeViewPort.height) {
            Rectangle rect = new Rectangle(0, 0, viewport.getWidth(), TimeViewPort.height);
            if (!rect.contains(event.getPoint()) && Utils.getX(event.getPoint()) > 200) {
                long startNSTmp = Math.min(viewport.rulerFragment.getLeftNS(), viewport.rulerFragment.getRightNS());
                long endNSTmp = Math.max(viewport.rulerFragment.getLeftNS(), viewport.rulerFragment.getRightNS());
                long dur = endNSTmp - startNSTmp;
                if (getCursor().getType() == Cursor.W_RESIZE_CURSOR) {
                    contentPanel.startPoint = event.getPoint();
                    contentPanel.x1 = Math.min(Utils.getX(contentPanel.startPoint), Utils.getX(contentPanel.endPoint));
                    contentPanel.x2 = Math.max(Utils.getX(contentPanel.startPoint), Utils.getX(contentPanel.endPoint));
                    contentPanel.rangeStartNS =
                        (contentPanel.x1 - 200) * dur / (viewport.rulerFragment.getRect().width) + startNSTmp;
                    contentPanel.rangeEndNS =
                        (contentPanel.x2 - 200) * dur / (viewport.rulerFragment.getRect().width) + startNSTmp;
                } else if (getCursor().getType() == Cursor.E_RESIZE_CURSOR) {
                    contentPanel.endPoint = event.getPoint();
                    contentPanel.x1 = Math.min(Utils.getX(contentPanel.startPoint), Utils.getX(contentPanel.endPoint));
                    contentPanel.x2 = Math.max(Utils.getX(contentPanel.startPoint), Utils.getX(contentPanel.endPoint));
                    contentPanel.rangeStartNS =
                        (contentPanel.x1 - 200) * dur / (viewport.rulerFragment.getRect().width) + startNSTmp;
                    contentPanel.rangeEndNS =
                        (contentPanel.x2 - 200) * dur / (viewport.rulerFragment.getRect().width) + startNSTmp;
                } else {
                    contentPanel.endPoint = event.getPoint();
                    if (Objects.nonNull(contentPanel.startPoint)) {
                        contentPanel.x1 =
                            Math.min(Utils.getX(contentPanel.startPoint), Utils.getX(contentPanel.endPoint));
                        contentPanel.y1 =
                            Math.min(Utils.getY(contentPanel.startPoint), Utils.getY(contentPanel.endPoint));
                        contentPanel.x2 =
                            Math.max(Utils.getX(contentPanel.startPoint), Utils.getX(contentPanel.endPoint));
                        contentPanel.y2 =
                            Math.max(Utils.getY(contentPanel.startPoint), Utils.getY(contentPanel.endPoint));
                        contentPanel.rangeStartNS =
                            (contentPanel.x1 - 200) * dur / (viewport.rulerFragment.getRect().width) + startNSTmp;
                        contentPanel.rangeEndNS =
                            (contentPanel.x2 - 200) * dur / (viewport.rulerFragment.getRect().width) + startNSTmp;
                    }
                }
                contentPanel.drawRangeSelectData = true;
                contentPanel.repaint();
            }
        }
    }

    @Override
    public void mouseMoved(final MouseEvent event) {
        viewport.mouseMoved(event);
        if (SwingUtilities.convertMouseEvent(contentPanel, event, AnalystPanel.this).getY() > TimeViewPort.height) {
            if (event.getY() < Utils.getY(tab.getBounds())) {
                contentPanel.requestFocusInWindow();
            }
            contentPanel.fragmentList.forEach(fragment -> fragment.mouseMoved(event));
        }
        Rectangle rect1 = new Rectangle(contentPanel.x1 - 2, contentPanel.y1, 4, contentPanel.y2 - contentPanel.y1);
        Rectangle rect2 = new Rectangle(contentPanel.x2 - 2, contentPanel.y1, 4, contentPanel.y2 - contentPanel.y1);
        if (rect1.contains(event.getPoint())) {
            setCursor(wCursor);
        } else if (rect2.contains(event.getPoint())) {
            setCursor(eCursor);
        } else {
            setCursor(defaultCursor);
        }
    }

    @Override
    public void mouseWheelMoved(final MouseWheelEvent event) {
        if (event.isShiftDown() && !event.isControlDown()) { // Pan
            long scale = viewport.rulerFragment.getScale();
            if (Math.abs(event.getPreciseWheelRotation()) >= 1) {
                wheelSize = scale * event.getPreciseWheelRotation() * defaultScale;
            } else {
                wheelSize = scale * event.getPreciseWheelRotation();
            }
            translation();
        }
        if (event.isControlDown() && !event.isShiftDown()) { // Zoom
            if (Math.abs(event.getPreciseWheelRotation()) >= 1) {
                wheelSize = viewport.rulerFragment.getScale() * event.getPreciseWheelRotation() * defaultScale;
            } else {
                wheelSize = viewport.rulerFragment.getScale() * event.getPreciseWheelRotation();
            }
            int rulerFragmentWidth = viewport.rulerFragment.getRect().width;
            lefPercent = (event.getX() - Utils.getX(viewport.rulerFragment.getRect())) * 1.0 / rulerFragmentWidth;
            if (scaleMin(event)) {
                scale();
            }
        }
    }

    private boolean scaleMin(MouseWheelEvent event) {
        if (event.getPreciseWheelRotation() < 0) { // Zoom out
            long rightNS = viewport.rulerFragment.getRightNS();
            long leftNS = viewport.rulerFragment.getLeftNS();
            long centerNS;
            final int minrul = 1000;
            if (rightNS - leftNS <= minrul) {
                rightNS = leftNS + minrul;
                centerNS = leftNS;
                viewport.rulerFragment.setRange(leftNS, rightNS, centerNS);
                viewport.cpuFragment.setRange(leftNS, rightNS);
                for (AbstractDataFragment fragment : viewport.favoriteFragments) {
                    fragment.range(leftNS, rightNS);
                }
                repaint();
                return false;
            }
        }
        return true;
    }

    private void scale() {
        if (lefPercent < 0) {
            lefPercent = 0;
        }
        if (lefPercent > 1) {
            lefPercent = 1;
        }
        rightPercent = 1 - lefPercent;
        if (lefPercent > 0) {
            double leftNs = viewport.rulerFragment.getLeftNS() - this.wheelSize * lefPercent;
            viewport.rulerFragment.setLeftNS((long) leftNs);
        }
        if (rightPercent > 0) {
            double rightNs = viewport.rulerFragment.getRightNS() + this.wheelSize * rightPercent;
            viewport.rulerFragment.setRightNS((long) rightNs);
        }
        if (viewport.rulerFragment.getLeftNS() <= 0) {
            viewport.rulerFragment.setLeftNS(0);
        }
        if (viewport.rulerFragment.getRightNS() >= DURATION) {
            viewport.rulerFragment.setRightNS(DURATION);
        }
        viewport.rulerFragment.setCenterNS(viewport.rulerFragment.getLeftNS());
        viewport.rulerFragment.setRange(viewport.rulerFragment.getLeftNS(), viewport.rulerFragment.getRightNS(),
            viewport.rulerFragment.getCenterNS());
        viewport.cpuFragment.setRange(viewport.rulerFragment.getLeftNS(), viewport.rulerFragment.getRightNS());
        if (lefPercent > 0) {
            startNS = viewport.leftFragment.getStartNS();
            startNS -= wheelSize * lefPercent;
            if (startNS > 0) {
                viewport.leftFragment.setStartTime(startNS);
            } else {
                viewport.leftFragment.setStartTime(0);
            }
        }
        for (AbstractDataFragment fragment : viewport.favoriteFragments) {
            fragment.range(viewport.rulerFragment.getLeftNS(), viewport.rulerFragment.getRightNS());
        }
        resetRangePoint();
        contentPanel.repaint();
    }

    private void translation() {
        long leftNS = viewport.rulerFragment.getLeftNS();
        long rightNS = viewport.rulerFragment.getRightNS();
        long centerNS;

        if (leftNS + wheelSize <= 0) {
            rangeNs = rightNS - leftNS;
            leftNS = 0;
            rightNS = (long) rangeNs;
            centerNS = leftNS;
            viewport.rulerFragment.setRange(leftNS, rightNS, centerNS);
            viewport.cpuFragment.setRange(leftNS, rightNS);
            viewport.leftFragment.setStartTime(0);
        } else if (rightNS + wheelSize >= DURATION) {
            rangeNs = rightNS - leftNS;
            rightNS = DURATION;
            leftNS = (long) (DURATION - rangeNs);
            centerNS = leftNS;
            viewport.rulerFragment.setRange(leftNS, rightNS, centerNS);
            viewport.cpuFragment.setRange(leftNS, rightNS);
            viewport.leftFragment.setStartTime(leftNS);
        } else {
            leftNS += wheelSize;
            rightNS += wheelSize;
            centerNS = leftNS;
            viewport.rulerFragment.setRange(leftNS, rightNS, centerNS);
            viewport.cpuFragment.setRange(leftNS, rightNS);
            startNS = viewport.leftFragment.getStartNS();
            startNS += wheelSize;
            viewport.leftFragment.setStartTime(startNS);
        }

        // Slide the icons that need to be viewed in the timeShaft collection
        for (AbstractDataFragment fragment : viewport.favoriteFragments) {
            fragment.range(leftNS, rightNS);
        }
        resetRangePoint();
        contentPanel.repaint();
    }

    private void resetRangePoint() {
        contentPanel.fragmentList.stream().findFirst().ifPresent(it -> {
            if (Objects.nonNull(contentPanel.startPoint)) {
                Utils.setX(contentPanel.startPoint, it.getX(contentPanel.rangeStartNS) + 200);
                contentPanel.x1 = Utils.getX(contentPanel.startPoint);
            }
            if (Objects.nonNull(contentPanel.endPoint)) {
                Utils.setX(contentPanel.endPoint, it.getX(contentPanel.rangeEndNS) + 200);
                contentPanel.x2 = Utils.getX(contentPanel.endPoint);
            }
        });
    }

    private void cancelRangeSelect() {
        contentPanel.startPoint = null;
        contentPanel.endPoint = null;
    }

    public static IAsyncFunctionDataClick getiAsyncFunctionDataClick() {
        return iAsyncFunctionDataClick;
    }

    public static void setiAsyncFunctionDataClick(IAsyncFunctionDataClick iAsyncFunctionDataClick) {
        AnalystPanel.iAsyncFunctionDataClick = iAsyncFunctionDataClick;
    }

    /**
     * cpu data click callback
     */
    public interface ICpuDataClick {
        /**
         * cpu data click callback
         *
         * @param cpu cpu
         */
        void click(CpuData cpu);
    }

    /**
     * thread data click callback
     */
    public interface IThreadDataClick {
        /**
         * thread data click callback
         *
         * @param data thread
         */
        void click(ThreadData data);
    }

    /**
     * function data click callback
     */
    public interface IFunctionDataClick {
        /**
         * function data click callback
         *
         * @param data function
         */
        void click(FunctionBean data);
    }

    /**
     * async function data click callback
     */
    public interface IAsyncFunctionDataClick {
        /**
         * function data click callback
         *
         * @param data function
         */
        void click(AsyncEvent data);
    }

    /**
     * function data click callback
     */
    public interface IClockDataClick {
        /**
         * function data click callback
         *
         * @param data function
         */
        void click(ClockData data);
    }

    /**
     * flag click callback
     */
    public interface IFlagClick {
        /**
         * flag click callback
         *
         * @param data flag
         */
        void click(FlagBean data);
    }

    /**
     * wrap left ns and right ns
     */
    public static class LeftRightNS {
        private long leftNs;
        private long rightNs;

        /**
         * Gets the value of leftNs .
         *
         * @return the value of long
         */
        public long getLeftNs() {
            return leftNs;
        }

        /**
         * Sets the leftNs .You can use getLeftNs() to get the value of leftNs
         *
         * @param leftNs leftNs
         */
        public void setLeftNs(long leftNs) {
            this.leftNs = leftNs;
        }

        /**
         * Gets the value of rightNs .
         *
         * @return the value of long
         */
        public long getRightNs() {
            return rightNs;
        }

        /**
         * Sets the rightNs .You can use getRightNs() to get the value of rightNs
         *
         * @param rightNs rightNs
         */
        public void setRightNs(long rightNs) {
            this.rightNs = rightNs;
        }
    }
}
