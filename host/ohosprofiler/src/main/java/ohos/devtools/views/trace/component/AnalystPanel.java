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

import com.intellij.ui.components.JBLayeredPane;
import com.intellij.ui.components.JBScrollPane;
import ohos.devtools.views.trace.bean.CpuData;
import ohos.devtools.views.trace.bean.CpuFreqData;
import ohos.devtools.views.trace.bean.FlagBean;
import ohos.devtools.views.trace.bean.FunctionBean;
import ohos.devtools.views.trace.bean.Process;
import ohos.devtools.views.trace.bean.ProcessMem;
import ohos.devtools.views.trace.bean.ThreadData;
import ohos.devtools.views.trace.bean.WakeupBean;
import ohos.devtools.views.trace.fragment.AbstractDataFragment;
import ohos.devtools.views.trace.fragment.CpuDataFragment;
import ohos.devtools.views.trace.fragment.CpuFreqDataFragment;
import ohos.devtools.views.trace.fragment.MemDataFragment;
import ohos.devtools.views.trace.fragment.ProcessDataFragment;
import ohos.devtools.views.trace.fragment.ThreadDataFragment;
import ohos.devtools.views.trace.listener.IFlagListener;
import ohos.devtools.views.trace.util.Db;
import ohos.devtools.views.trace.util.TimeUtils;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
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
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_D;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_SHIFT;
import static java.awt.event.KeyEvent.VK_W;

/**
 * Analysis component
 *
 * @version 1.0.1
 * @date 2021/04/20 12:24
 */
public final class AnalystPanel extends JBLayeredPane
    implements MouseWheelListener, KeyListener, MouseListener, MouseMotionListener {
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
     * bottom tab
     */
    public TabPanel tab;

    private final javax.swing.JScrollPane scrollPane = new JBScrollPane();
    private ContentPanel contentPanel;
    private TimeViewPort viewport;
    private final int defaultFragmentHeight = 40;
    private double wheelSize;
    private double rangeNs;
    private double lefPercent;
    private double rightPercent;
    private long startNS;
    private final double defaultScale = 0.1;
    private boolean isUserInteraction;

    /**
     * Constructor
     */
    public AnalystPanel() {
        viewport = new TimeViewPort(height -> viewport.setBorder(null), (sn, en) -> {
            // When the time axis range changes,
            // the contentPanel is notified that all data is refreshed according to the time axis
            contentPanel.rangeChange(sn, en);
        });
        setBorder(null);
        contentPanel = new ContentPanel(this);
        contentPanel.setBorder(null);
        viewport.setView(contentPanel);
        scrollPane.setViewport(viewport);
        scrollPane.setBorder(null);
        viewport.addChangeListener(event -> {
            if (TabPanel.getMyHeight() != 0) {
                moveToFront(tab);
            }
        });
        tab = new TabPanel();
        this.add(scrollPane);
        this.add(tab);
        tab.setFocusable(true);
        // tab Set invisible first
        this.setLayer(scrollPane, DEFAULT_LAYER);
        this.setPosition(scrollPane, 0);
        this.setLayer(tab, FRAME_CONTENT_LAYER);
        this.setPosition(tab, 1);
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent event) {
                super.componentResized(event);
                Rectangle rootBounds = AnalystPanel.this.getBounds();
                scrollPane.setBounds(rootBounds);
                tab.setBounds(rootBounds.x, rootBounds.height - tab.getMHeight(), rootBounds.width, tab.getMHeight());
                viewport.setRootHeight(getHeight());
                contentPanel.repaint();
            }
        });
        contentPanel.setFocusable(true);
        contentPanel.addMouseMotionListener(this);
        contentPanel.addMouseListener(this);
        contentPanel.addKeyListener(this);
        iCpuDataClick = cpu -> clickCpuData(cpu);
        iThreadDataClick = thread -> clickThreadData(thread);
        iFunctionDataClick = fun -> clickFunctionData(fun);
        iFlagClick = flag -> clickTimeFlag(flag);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(final AdjustmentEvent e) {
                tab.hide();
            }
        });
    }

    /**
     * add cpu data list
     *
     * @param list source
     */
    public void addCpuList(final List<List<CpuData>> list) {
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
    public void addCpuFreqList(final List<List<CpuFreqData>> list, final Map<String, Object> cpuMaxFreq) {
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
     * @param list       thread list
     * @param processMem process list
     */
    public void addThreadsList(final List<ThreadData> list, final List<ProcessMem> processMem) {
        threadsList = list;
        List<Process> processes = Db.getInstance().queryProcess();
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
        list.stream().filter(data -> data.getProcessName() == null || data.getProcessName().isEmpty())
            .forEach(threadData -> {
                Process process = new Process();
                process.setPid(threadData.getTid());
                process.setName(threadData.getThreadName());
                ProcessDataFragment processDataFragment = new ProcessDataFragment(contentPanel, process);
                contentPanel.addDataFragment(processDataFragment);
                if (!process.getName().startsWith("swapper") && process.getPid() != 0) {
                    ThreadDataFragment fgr = new ThreadDataFragment(contentPanel, threadData);
                    fgr.defaultHeight = defaultFragmentHeight;
                    fgr.parentUuid = processDataFragment.uuid;
                    fgr.visible = false;
                    contentPanel.addDataFragment(fgr);
                }
            });
    }

    /**
     * load database
     *
     * @param name    db name
     * @param isLocal is local db
     */
    public void load(final String name, final boolean isLocal) {
        Db.setDbName(name);
        Db.load(isLocal);
        ForkJoinPool.commonPool().submit(() -> {
            DURATION = Db.getInstance().queryTotalTime();

            // Add cpu time slice information
            int cpuMax = Db.getInstance().queryCpuMax();
            List<List<CpuData>> list = new ArrayList<>();
            for (int index = 0; index <= cpuMax; index++) {
                List<CpuData> cpuData = Db.getInstance().queryCpuData(index);
                list.add(cpuData);
            }

            // Add cpu frequency information
            List<List<CpuFreqData>> freqList =
                Db.getInstance().queryCpuFreq().stream().map(Db.getInstance()::queryCpuFreqData)
                    .collect(Collectors.toList());

            // Add the memory information of the process
            List<ProcessMem> processMem = Db.getInstance().getProcessMem();

            // Add thread information
            List<ThreadData> processThreads = Db.getInstance().queryProcessThreads();
            Map<String, Object> cpuMaxFreq = Db.getInstance().queryCpuMaxFreq();
            javax.swing.SwingUtilities.invokeLater(() -> {
                viewport.rulerFragment.setRange(0, AnalystPanel.DURATION, 0);
                tab.hide();
                addCpuList(list);
                addCpuFreqList(freqList, cpuMaxFreq);
                addThreadsList(processThreads, processMem); // The memory information of the process and

                // The thread information of the process is displayed together
                contentPanel.refresh();
            });
        });
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
     * The bottom tab is displayed when the method event is clicked.
     *
     * @param bean function
     */
    public void clickFunctionData(final FunctionBean bean) {
        setLayer(tab, JLayeredPane.DRAG_LAYER);
        ArrayList<ScrollSlicePanel.SliceData> dataSource = new ArrayList<>();
        dataSource.add(ScrollSlicePanel.createSliceData("Name", bean.getFunName(), false));
        dataSource.add(ScrollSlicePanel.createSliceData("Category", bean.getCategory(), false));
        dataSource.add(
            ScrollSlicePanel.createSliceData("StartTime", TimeUtils.getTimeString(bean.getStartTime()) + "", false));
        dataSource
            .add(ScrollSlicePanel.createSliceData("Duration", TimeUtils.getTimeString(bean.getDuration()) + "", false));
        SwingUtilities.invokeLater(() -> {
            tab.recovery();
            tab.removeAll();
            ScrollSlicePanel ssp = new ScrollSlicePanel();
            ssp.setData("Slice Details", dataSource, null);
            tab.add("Current Selection", ssp);
        });
    }

    /**
     * When you click the CPU event, the bottom tab is displayed.
     *
     * @param threadData thread
     */
    public void clickThreadData(final ThreadData threadData) {
        setLayer(tab, javax.swing.JLayeredPane.DRAG_LAYER);
        ArrayList<ScrollSlicePanel.SliceData> dataSource = new ArrayList<>();
        dataSource.add(ScrollSlicePanel
            .createSliceData("StartTime", TimeUtils.getTimeString(threadData.getStartTime()) + "", false));
        dataSource.add(ScrollSlicePanel
            .createSliceData("Duration", TimeUtils.getTimeString(threadData.getDuration()) + "", false));
        String state = Utils.getEndState(threadData.getState());
        if ("Running".equals(Utils.getEndState(threadData.getState()))) {
            state = state + " on CPU " + threadData.getCpu();
        }
        dataSource.add(ScrollSlicePanel.createSliceData("State", state, false));
        String processName = threadData.getProcessName();
        if (processName == null || processName.isEmpty()) {
            processName = threadData.getThreadName();
        }
        dataSource
            .add(ScrollSlicePanel.createSliceData("Process", processName + " [" + threadData.getPid() + "]", false));
        javax.swing.SwingUtilities.invokeLater(() -> {
            tab.recovery();
            tab.removeAll();
            ScrollSlicePanel ssp = new ScrollSlicePanel();
            ssp.setData("Thread State", dataSource, null);
            tab.add("Current Selection", ssp);
        });
    }

    /**
     * The bottom tab is displayed when you click the CPU event.
     *
     * @param cpu cpu
     */
    public void clickCpuData(final CpuData cpu) {
        setLayer(tab, javax.swing.JLayeredPane.DRAG_LAYER);
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
        ForkJoinPool.commonPool().submit(() -> {
            Optional<WakeupBean> wb = Db.getInstance().queryWakeupThread(cpu);
            SwingUtilities.invokeLater(() -> {
                contentPanel.setWakeupBean(wb.orElse(null));
                tab.recovery();
                tab.removeAll();
                ScrollSlicePanel ssp = new ScrollSlicePanel();
                ssp.setData("Slice Details", dataSource, wb.orElse(null));
                tab.add("Current Selection", ssp);
                repaint();
            });
        });
    }

    /**
     * Evoking the red flag corresponds to the tabPanel at the bottom.
     *
     * @param flagBean flag
     */
    public void clickTimeFlag(final FlagBean flagBean) {
        setLayer(tab, javax.swing.JLayeredPane.DRAG_LAYER);
        tab.recovery();
        tab.removeAll();
        ScrollFlagPanel flagPanel = new ScrollFlagPanel(flagBean);
        flagPanel.setFlagListener(new IFlagListener() {
            @Override
            public void flagRemove(final FlagBean flag) {
                flag.remove();
                viewport.rulerFragment.repaint();
                tab.recovery();
                tab.removeAll();
                tab.hide();
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
        tab.add("Current Selection", flagPanel);
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

    @Override
    public void keyReleased(final KeyEvent event) {
        switch (event.getExtendedKeyCode()) {
            case VK_SHIFT:
            case VK_CONTROL:
                if (isUserInteraction) {
                    isUserInteraction = false;
                    contentPanel.removeMouseWheelListener(this);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
        contentPanel.fragmentList.forEach(fragment -> fragment.mouseClicked(event));
    }

    @Override
    public void mousePressed(final MouseEvent event) {
        viewport.mousePressed(event);
        contentPanel.fragmentList.forEach(fragment -> fragment.mouseReleased(event));
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
        contentPanel.fragmentList.forEach(fragment -> fragment.mouseReleased(event));
    }

    @Override
    public void mouseEntered(final MouseEvent event) {
        contentPanel.fragmentList.forEach(fragment -> fragment.mouseEntered(event));
    }

    @Override
    public void mouseExited(final MouseEvent event) {
        contentPanel.fragmentList.forEach(fragment -> fragment.mouseExited(event));
    }

    @Override
    public void mouseDragged(final MouseEvent event) {
        viewport.mouseDragged(event);
    }

    @Override
    public void mouseMoved(final MouseEvent event) {
        viewport.mouseMoved(event);
        if (event.getY() < tab.getBounds().y) {
            contentPanel.requestFocusInWindow();
        }
        contentPanel.fragmentList.forEach(fragment -> fragment.mouseMoved(event));
    }

    @Override
    public void mouseWheelMoved(final MouseWheelEvent event) {
        tab.hide();
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
            lefPercent = (event.getX() - viewport.rulerFragment.getRect().x) * 1.0 / rulerFragmentWidth;
            if (scaleMin(event)) {
                scale();
            }
        }
    }

    private boolean scaleMin(MouseWheelEvent event) {
        if (event.getPreciseWheelRotation() < 0) { // Zoom out
            long rightNS = viewport.rulerFragment.getRightNS();
            long leftNS = viewport.rulerFragment.getLeftNS();
            long centerNS = viewport.rulerFragment.getCenterNS();
            final int minRul = 1000;
            if (rightNS - leftNS <= minRul) {
                rightNS = leftNS + minRul;
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
        repaint();
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
        repaint();
    }

}
