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

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import ohos.devtools.views.trace.Common;
import ohos.devtools.views.trace.bean.CpuData;
import ohos.devtools.views.trace.bean.WakeupBean;
import ohos.devtools.views.trace.fragment.AbstractDataFragment;
import ohos.devtools.views.trace.fragment.CpuDataFragment;
import ohos.devtools.views.trace.fragment.FunctionDataFragment;
import ohos.devtools.views.trace.fragment.MemDataFragment;
import ohos.devtools.views.trace.fragment.ProcessDataFragment;
import ohos.devtools.views.trace.fragment.ThreadDataFragment;
import ohos.devtools.views.trace.fragment.ruler.AbstractNode;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.TimeUtils;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Rolling container
 *
 * @since 2021/04/20 12:24
 */
public final class ContentPanel extends JBPanel implements AbstractDataFragment.IDataFragment {
    /**
     * clint fragment
     */
    public static AbstractDataFragment clickFragment;

    /**
     * FragmentList to be rendered
     */
    public List<AbstractDataFragment> fragmentList = new ArrayList<>();

    /**
     * start point object
     */
    public Point startPoint;

    /**
     * end point object
     */
    public Point endPoint;

    /**
     * Analysis component
     */
    public AnalystPanel analystPanel;

    /**
     * draw range select flag
     */
    public boolean drawRangeSelect;

    /**
     * draw range select data flag
     */
    public boolean drawRangeSelectData;

    /**
     * range select x1
     */
    public int x1;

    /**
     * range select y1
     */
    public int y1;

    /**
     * range select x2
     */
    public int x2;

    /**
     * range select y2
     */
    public int y2;

    /**
     * range start time
     */
    public long rangeStartNS;

    /**
     * range end time
     */
    public long rangeEndNS;

    private WakeupBean wakeupBean;
    private long startNS;
    private long endNS;
    private final BasicStroke boldStoke = new BasicStroke(2);
    private final BasicStroke normalStoke = new BasicStroke(1);

    /**
     * Constructor
     *
     * @param analystPanel component
     */
    public ContentPanel(AnalystPanel analystPanel) {
        this.analystPanel = analystPanel;
        this.setOpaque(false);
        setFont(Final.NORMAL_FONT);
    }

    /**
     * Gets the value of wakeupBean .
     *
     * @return the value of ohos.devtools.views.trace.bean.WakeupBean
     */
    public WakeupBean getWakeupBean() {
        return wakeupBean;
    }

    /**
     * Sets the wakeupBean .
     * <p>You can use getWakeupBean() to get the value of wakeupBean</p>
     *
     * @param wakeup wakeup
     */
    public void setWakeupBean(WakeupBean wakeup) {
        this.wakeupBean = wakeup;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (graphics instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setFont(Final.NORMAL_FONT);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (getParent() instanceof TimeViewPort) {
                TimeViewPort parent = (TimeViewPort) getParent();
                Rectangle viewRect = parent.getViewRect();
                // Render the line in the viewport display area, and the line beyond the range will not be rendered
                fragmentList.stream().filter(fragment -> fragment.visible).filter(fragment -> {
                    if (fragment != null && fragment.getRect() != null && viewRect != null) {
                        return Utils.getY(fragment.getRect()) + fragment.getRect().height
                            >= Utils.getY(viewRect) + TimeViewPort.height
                            && Utils.getY(fragment.getRect()) <= Utils.getY(viewRect) + viewRect.height;
                    } else {
                        return false;
                    }
                }).forEach(fragment -> {
                    if (fragment != null) {
                        fragment.draw(g2);
                    }
                });
            }
            drawRangeSelect(g2);
            drawWakeup(g2);
        }
    }

    private void drawRangeSelect(Graphics2D g2) {
        if (Objects.nonNull(startPoint) && Objects.nonNull(endPoint)) {
            var ref = new Object() {
                int realY1;
                int realY2;
            };
            fragmentList.stream().filter(it -> it.visible).filter(it -> it.getDataRect().contains(x1, y1))
                .forEach(it -> ref.realY1 = Utils.getY(it.getRect()));
            fragmentList.stream().filter(it -> it.visible).filter(it -> it.getDataRect().contains(x2 - 1, y2 - 1))
                .forEach(it -> ref.realY2 = Utils.getY(it.getRect()) + it.getRect().height);
            int tmpWidth = Math.abs(x2 - x1);
            int tmpHeight = Math.abs(ref.realY2 - ref.realY1);
            Rectangle range = new Rectangle(x1, ref.realY1, tmpWidth, tmpHeight);
            if (drawRangeSelect) {
                g2.setStroke(new BasicStroke(2));
                g2.setColor(JBColor.foreground().brighter());
                g2.drawRect(Utils.getX(range), Utils.getY(range), range.width, range.height);
            } else {
                Common.setAlpha(g2, 0.5F);
                g2.setColor(JBColor.foreground().brighter());
                g2.fillRect(Utils.getX(range), Utils.getY(range), range.width, range.height);
                if (drawRangeSelectData) {
                    drawRangeSelectData = false;
                    List<AbstractDataFragment> rangeFragments =
                        fragmentList.stream().filter(it -> range.intersects(it.getDataRect()))
                            .collect(Collectors.toList());

                    // Draw after dragging
                    rangeFragments.forEach(it -> it.drawFrame());
                    List<AbstractDataFragment> process = new ArrayList<>();
                    rangeFragments.stream().filter(it -> it instanceof ProcessDataFragment)
                        .map(it -> (ProcessDataFragment) it).forEach(it -> {
                            process.addAll(fragmentList.stream().filter(that -> that.parentUuid.equals(it.uuid))
                                .collect(Collectors.toList()));
                        });
                    rangeFragments.addAll(process);
                    List<Integer> cpu = rangeFragments.stream().filter(it -> it instanceof CpuDataFragment)
                        .map(it -> ((CpuDataFragment) it).getIndex()).collect(Collectors.toList());
                    List<Integer> threads = rangeFragments.stream().filter(it -> it instanceof ThreadDataFragment)
                        .map(it -> ((ThreadDataFragment) it).thread.getTid()).collect(Collectors.toList());
                    List<Integer> tracks = rangeFragments.stream().filter(it -> it instanceof MemDataFragment)
                        .map(it -> ((MemDataFragment) it).mem.getTrackId()).collect(Collectors.toList());
                    List<Integer> functions = rangeFragments.stream().filter(it -> it instanceof FunctionDataFragment)
                        .map(it -> ((FunctionDataFragment) it).thread.getTid()).collect(Collectors.toList());
                    AnalystPanel.LeftRightNS ns = new AnalystPanel.LeftRightNS();
                    ns.setLeftNs(rangeStartNS);
                    ns.setRightNs(rangeEndNS);
                    this.analystPanel.boxSelection(cpu, threads, tracks, functions, ns);
                }
            }
            Common.setAlpha(g2, 1.0F);
        }
    }

    /**
     * add data line
     *
     * @param fragment data fragment
     */
    public void addDataFragment(AbstractDataFragment fragment) {
        fragment.setDataFragmentListener(this);
        fragmentList.add(fragment);
    }

    /**
     * add data line
     *
     * @param index line index
     * @param fragment data fragment
     */
    public void addDataFragment(int index, AbstractDataFragment fragment) {
        fragment.setDataFragmentListener(this);
        fragmentList.add(index, fragment);
    }

    /**
     * refresh content data
     */
    public void refresh() {
        List<AbstractDataFragment> fs =
            fragmentList.stream().filter(fragment -> fragment.visible).collect(Collectors.toList());
        int timeViewHeight = TimeViewPort.height;
        for (int index = 0, len = fs.size(); index < len; index++) {
            AbstractDataFragment dataFragment = fs.get(index);
            timeViewHeight += dataFragment.defaultHeight;
            dataFragment.getRect().height = dataFragment.defaultHeight;
            dataFragment.getDescRect().height = dataFragment.defaultHeight;
            dataFragment.getDataRect().height = dataFragment.defaultHeight;
            Utils.setY(dataFragment.getRect(), timeViewHeight - dataFragment.defaultHeight);
            Utils.setY(dataFragment.getDescRect(), timeViewHeight - dataFragment.defaultHeight);
            Utils.setY(dataFragment.getDataRect(), timeViewHeight - dataFragment.defaultHeight);
            Utils.setX(dataFragment.getRect(), 0);
            Utils.setX(dataFragment.getDescRect(), 0);
            Utils.setX(dataFragment.getDataRect(), 200);
        }
        Dimension dim = new Dimension(0, timeViewHeight);
        this.setPreferredSize(dim);
        this.setSize(dim);
        this.setMaximumSize(dim);
        repaint();
    }

    /**
     * recycle content data
     */
    public void recycle() {
        clickFragment = null;
        if (fragmentList != null) {
            fragmentList.forEach(AbstractDataFragment::recycle);
            fragmentList.clear();
        }
    }

    /**
     * time range change will call this
     *
     * @param startNS range start ns
     * @param endNS range end ns
     */
    public void rangeChange(long startNS, long endNS) {
        this.startNS = startNS;
        this.endNS = endNS;
        x1 = (int) getRangeX(rangeStartNS) + 200;
        x2 = (int) getRangeX(rangeEndNS) + 200;
        fragmentList.forEach(fragment -> fragment.range(startNS, endNS));
    }

    private double getRangeX(long range) {
        double xSize = (range - startNS) * (getWidth() - 200) / (endNS - startNS);
        if (xSize < 0) {
            xSize = 0;
        }
        if (xSize > getWidth() - 200) {
            xSize = getWidth() - 200;
        }
        return xSize;
    }

    @Override
    public void collect(AbstractDataFragment fgr) {
        if (getParent() instanceof TimeViewPort) {
            TimeViewPort viewPort = (TimeViewPort) getParent();
            if (fgr.visible) {
                viewPort.favorite(fgr);
            } else {
                viewPort.cancel(fgr);
            }
            refresh();
        }
    }

    @Override
    public void check(AbstractDataFragment fgr) {
    }

    private void drawWakeup(Graphics2D graphics) {
        if (clickFragment == null || CpuDataFragment.currentSelectedCpuData == null || !AnalystPanel.clicked) {
            return;
        }
        Optional.ofNullable(wakeupBean).ifPresent(wakeup -> {
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(Color.BLACK);
            graphics.setStroke(boldStoke);
            Rectangle visibleRect = getVisibleRect();
            Rectangle dataRect = clickFragment.getDataRect();
            int wakeupX = clickFragment.getX(wakeup.getWakeupTime());
            if (wakeup.getWakeupTime() > startNS && wakeup.getWakeupTime() < endNS) {
                /**
                 * Draw the vertical line
                 */
                graphics.drawLine(wakeupX + Utils.getX(dataRect), Utils.getY(visibleRect),
                    wakeupX + Utils.getX(dataRect),
                    Utils.getY(visibleRect) + visibleRect.height);
                /**
                 * Draw a diamond First filter out which cpu fragment the wake-up thread is in,
                 * get the rect of the cpu fragment, and then calculate the coordinates of the drawing diamond
                 */
                for (AbstractDataFragment dataFragment : fragmentList) {
                    if (dataFragment instanceof CpuDataFragment) {
                        if (((CpuDataFragment) dataFragment).getIndex() == wakeup.getWakeupCpu()) {
                            Rectangle wakeCPURect = dataFragment.getRect();
                            final int[] xs = {Utils.getX(dataRect) + wakeupX, Utils.getX(dataRect) + wakeupX + 6,
                                Utils.getX(dataRect) + wakeupX,
                                Utils.getX(dataRect) + wakeupX - 6};
                            final int[] ys =
                                {Utils.getY(wakeCPURect) + wakeCPURect.height / 2 - 10,
                                    Utils.getY(wakeCPURect) + wakeCPURect.height / 2,
                                    Utils.getY(wakeCPURect) + wakeCPURect.height / 2 + 10,
                                    Utils.getY(wakeCPURect) + wakeCPURect.height / 2};
                            graphics.fillPolygon(xs, ys, xs.length);
                            break;
                        }
                    }
                }
            }
            drawArrayAndText(graphics, dataRect, wakeupX, wakeup);
        });
    }

    private void drawArrayAndText(Graphics2D graphics, Rectangle dataRect, int wakeupX, WakeupBean wakeup) {
        /**
         * Draw arrows and text
         */
        CpuData selectCpu = CpuDataFragment.currentSelectedCpuData;
        if (selectCpu != null) {
            Rectangle rectangle =
                new Rectangle(wakeupX + Utils.getX(dataRect), Utils.getY(selectCpu.rect) + selectCpu.rect.height / 2,
                    Utils.getX(selectCpu.rect) - wakeupX - Utils.getX(dataRect), 30);
            if (selectCpu.getStartTime() > startNS && wakeup.getWakeupTime() < endNS) {
                graphics.drawLine(Utils.getX(dataRect) + wakeupX,
                    Utils.getY(selectCpu.rect) + selectCpu.rect.height - 2,
                    Utils.getX(selectCpu.rect),
                    Utils.getY(selectCpu.rect) + selectCpu.rect.height - 2);
            }
            if (rectangle.width > 10) {
                if (wakeup.getWakeupTime() > startNS && wakeup.getWakeupTime() < endNS) {
                    drawArrow(graphics, Utils.getX(dataRect) + wakeupX,
                        Utils.getY(selectCpu.rect) + selectCpu.rect.height - 2,
                        -1);
                }
                if (selectCpu.getStartTime() < endNS && selectCpu.getStartTime() > startNS) {
                    drawArrow(graphics, Utils.getX(selectCpu.rect),
                        Utils.getY(selectCpu.rect) + selectCpu.rect.height - 2, 1);
                }
            }
            if (wakeup.getWakeupTime() < endNS && selectCpu.getStartTime() > startNS) {
                long offsetTime = selectCpu.getStartTime() - wakeup.getWakeupTime();
                String timeString = TimeUtils.getTimeString(offsetTime);
                Utils.setY(rectangle, Utils.getY(rectangle) - 5);
                selectCpu.drawString(graphics, rectangle, timeString, AbstractNode.Placement.CENTER);
            }
        }
        graphics.setStroke(normalStoke);
    }

    /**
     * repaint panel
     */
    public void clearWakeupAndBoxSelect() {
        if (Objects.nonNull(startPoint) || Objects.nonNull(endPoint) || Objects.nonNull(wakeupBean)) {
            drawRangeSelect = false;
            drawRangeSelectData = false;
            startPoint = null;
            endPoint = null;
            repaint();
        }
    }

    private void drawArrow(Graphics2D graphics, int xVal, int yVal, int align) {
        if (align == -1) {
            final int[] xArray = {xVal, xVal + 5, xVal + 5};
            final int[] yArray = {yVal, yVal - 5, yVal + 5};
            graphics.fillPolygon(xArray, yArray, xArray.length);
        }
        if (align == 1) {
            final int[] xArray = {xVal, xVal - 5, xVal - 5};
            final int[] yArray = {yVal, yVal - 5, yVal + 5};
            graphics.fillPolygon(xArray, yArray, xArray.length);
        }
    }

    /**
     * Jump to the cpu line and select the node where startTime starts
     *
     * @param cpu cpu
     * @param startTime startTime
     */
    public void scrollToCpu(int cpu, long startTime) {
        fragmentList.stream().filter(CpuDataFragment.class::isInstance).map(it -> ((CpuDataFragment) it))
            .filter(it -> it.getIndex() == cpu).findFirst().ifPresent(it -> {
                if (getParent() instanceof JViewport) {
                    JViewport parent = (JViewport) getParent();
                    if (it.getData() != null) {
                        it.getData().stream().filter(cpuData -> cpuData.getStartTime() == startTime).findFirst()
                            .ifPresent(cpuData -> it.click(null, cpuData));
                    } else {
                        it.delayClickStartTime = startTime;
                    }
                    JBScrollPane scrollPane = (JBScrollPane) parent.getParent();
                    scrollPane.getVerticalScrollBar().setValue(0);
                }
            });
    }

    /**
     * Jump to the specified location
     *
     * @param processId processId
     * @param tid tid
     * @param startTime startTime
     * @param offsetHeight tab height
     */
    public void scrollToThread(int processId, int tid, long startTime, int offsetHeight) {
        fragmentList.stream().filter(ProcessDataFragment.class::isInstance).map(it -> ((ProcessDataFragment) it))
            .filter(it -> it.getProcess().getPid() == processId).findFirst().ifPresent(it -> {
                if (getParent() instanceof JViewport) {
                    JViewport parent = (JViewport) getParent();
                    it.expandThreads();

                    SwingUtilities.invokeLater(() -> {
                        fragmentList.stream().filter(ThreadDataFragment.class::isInstance)
                            .map(th -> ((ThreadDataFragment) th))
                            .filter(th -> th.thread.getTid() == tid && th.parentUuid.equals(it.uuid)).findFirst()
                            .ifPresent(th -> {
                                // If the thread data has been loaded, data is not empty,
                                // find the node whose start time is startTime, and select it
                                if (th.getData() != null) {
                                    th.getData().stream()
                                        .filter(threadData -> threadData.getStartTime() == startTime)
                                        .findFirst().ifPresent(threadData -> {
                                            th.click(null, threadData);
                                        });
                                } else {
                                    // If the thread data has not been loaded yet,
                                    // save the start time of the node to be selected,
                                    // the load Data() method in the component will process this field,
                                    // select it directly after loading,
                                    // and then set the delay Click Start Time to null
                                    th.delayClickStartTime = startTime;
                                }
                                parent.scrollRectToVisible(
                                    new Rectangle(Utils.getX(th.getRect()), Utils.getY(th.getRect()) + offsetHeight,
                                        th.getRect().width,
                                        th.getRect().height));
                            });
                    });
                }
            });
    }
}
