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

package ohos.devtools.views.trace.fragment;

import ohos.devtools.views.trace.Sql;
import ohos.devtools.views.trace.bean.Process;
import ohos.devtools.views.trace.bean.ProcessData;
import ohos.devtools.views.trace.component.AnalystPanel;
import ohos.devtools.views.trace.component.ContentPanel;
import ohos.devtools.views.trace.fragment.graph.ExpandGraph;
import ohos.devtools.views.trace.util.ColorUtils;
import ohos.devtools.views.trace.util.Db;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.ImageUtils;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * process data
 *
 * @since 2021/04/22 12:25
 */
public class ProcessDataFragment extends AbstractDataFragment<ProcessData> implements ExpandGraph.IClickListener {
    /**
     * current focus Process Data .
     */
    public static ProcessData focusProcessData = null;
    private final Process process;
    private final float alpha60 = .6f;
    private final float alpha100 = 1.0f;
    private ExpandGraph expandGraph;
    private boolean isLoading;
    private int x1;
    private int x2;
    private Rectangle2D bounds;
    private Color processColor;
    private ProcessData tipProcessData = null;
    private int tipX; // X position of the message

    /**
     * constructor
     *
     * @param root root
     * @param process process
     */
    public ProcessDataFragment(JComponent root, Process process) {
        super(root, false, false);
        this.process = process;
        processColor = ColorUtils.colorForTid(process.getPid());
        this.setRoot(root);
        expandGraph = new ExpandGraph(this, root);
        expandGraph.setOnClickListener(this);
    }

    /**
     * Gets the value of expandGraph .
     *
     * @return the value of ohos.devtools.views.trace.fragment.graph.ExpandGraph
     */
    public ExpandGraph getExpandGraph() {
        return expandGraph;
    }

    /**
     * Gets the value of process .
     *
     * @return the value of ohos.devtools.views.trace.bean.Process
     */
    public Process getProcess() {
        return process;
    }

    /**
     * draw method
     *
     * @param graphics graphics
     */
    @Override
    public void draw(Graphics2D graphics) {
        super.draw(graphics);
        drawDefaultState(graphics);

        // left data info
        String name;
        if (process.getName() == null || process.getName().isEmpty()) {
            process.setName("Process");
        }
        name = process.getName() + " " + process.getPid();
        bounds = graphics.getFontMetrics().getStringBounds(name, graphics);
        double wordWidth = bounds.getWidth() / name.length(); // Width per character
        double wordNum = (getDescRect().width - 40) / wordWidth; // How many characters can be displayed on each line
        if (bounds.getWidth() < getDescRect().width - 40) { // Direct line display
            graphics.drawString(name, Utils.getX(getDescRect()) + 30,
                (int) (Utils.getY(getDescRect()) + bounds.getHeight() + 10));
        } else {
            String substring = name.substring((int) wordNum);
            if (substring.length() < wordNum) {
                graphics.drawString(name.substring(0, (int) wordNum), Utils.getX(getDescRect()) + 30,
                    (int) (Utils.getY(getDescRect()) + bounds.getHeight() + 8));
                graphics.drawString(substring, Utils.getX(getDescRect()) + 30,
                    (int) (Utils.getY(getDescRect()) + bounds.getHeight() * 2 + 8));
            } else {
                graphics.drawString(name.substring(0, (int) wordNum), Utils.getX(getDescRect()) + 30,
                    (int) (Utils.getY(getDescRect()) + bounds.getHeight() + 2));
                graphics.drawString(substring.substring(0, (int) wordNum), Utils.getX(getDescRect()) + 30,
                    (int) (Utils.getY(getDescRect()) + bounds.getHeight() * 2 + 2));
                graphics.drawString(substring.substring((int) wordNum), Utils.getX(getDescRect()) + 30,
                    (int) (Utils.getY(getDescRect()) + bounds.getHeight() * 3 + 2));
            }
        }
        expandGraph.setRect(Utils.getX(getDescRect()) + 8, Utils.getY(getRect()) + getRect().height / 2 - 6, 12, 12);
        expandGraph.draw(graphics);
    }

    private void drawDefaultState(Graphics graphics) {
        if (!expandGraph.isExpand()) {
            int height = (getRect().height) / (AnalystPanel.getCpuNum() == 0 ? 1 : AnalystPanel.getCpuNum());
            if (data != null) {
                if (graphics instanceof Graphics2D) {
                    data.stream()
                        .filter(pd -> pd.getStartTime() + pd.getDuration() > startNS && pd.getStartTime() < endNS)
                        .forEach(pd -> drawProcessData(pd, height, (Graphics2D) graphics));
                    drawTips((Graphics2D) graphics);
                }
            } else {
                if (process.getPid() != 0) {
                    graphics.setColor(getRoot().getForeground());
                    graphics.drawString("Loading...", Utils.getX(getDataRect()), Utils.getY(getDataRect()) + 12);
                    loadData();
                }
            }
            graphics.setColor(getRoot().getForeground());
        } else {
            graphics.setColor(getRoot().getForeground());
            graphics.fillRect(Utils.getX(getRect()), Utils.getY(getRect()), getRect().width, getRect().height);
            graphics.setColor(getRoot().getBackground());
        }
    }

    private void drawProcessData(ProcessData pd, int height, Graphics2D graphics) {
        if (pd.getStartTime() < startNS) {
            x1 = getX(startNS);
        } else {
            x1 = getX(pd.getStartTime());
        }
        if (pd.getStartTime() + pd.getDuration() > endNS) {
            x2 = getX(endNS);
        } else {
            x2 = getX(pd.getStartTime() + pd.getDuration());
        }
        graphics.setComposite(java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha100));
        if (CpuDataFragment.focusCpuData != null || focusProcessData != null) {
            if (CpuDataFragment.focusCpuData != null) {
                if (CpuDataFragment.focusCpuData.getProcessId() != pd.getPid()) {
                    graphics.setColor(Color.GRAY);
                } else if (CpuDataFragment.focusCpuData.getTid() != pd.getTid()) {
                    graphics.setComposite(java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha60));
                    graphics.setColor(processColor);
                } else {
                    graphics.setColor(processColor);
                }
            } else {
                if (focusProcessData.getPid() != pd.getPid()) {
                    graphics.setColor(Color.GRAY);
                } else if (focusProcessData.getTid() != pd.getTid()) {
                    graphics.setComposite(java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha60));
                    graphics.setColor(processColor);
                } else {
                    graphics.setColor(processColor);
                }
            }
        } else {
            graphics.setColor(processColor);
        }
        pd.setRect(x1 + Utils.getX(getDataRect()), Utils.getY(getDataRect()) + height * pd.getCpu() + 2,
            x2 - x1 <= 0 ? 1 : x2 - x1, height - 4);
        graphics.fillRect(x1 + Utils.getX(getDataRect()), Utils.getY(getDataRect()) + height * pd.getCpu() + 2,
            x2 - x1 <= 0 ? 1 : x2 - x1, height - 4);
    }

    private void drawTips(Graphics2D graphics) {
        if (tipProcessData != null) {
            graphics.setComposite(java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha100));
            graphics.setFont(Final.NORMAL_FONT);
            if (tipProcessData.getProcess() == null || tipProcessData.getProcess().isEmpty()) {
                tipProcessData.setProcess(tipProcessData.getThread());
            }
            String processName = "P:" + tipProcessData.getProcess() + " [" + tipProcessData.getPid() + "]";
            String threadName = "T:" + tipProcessData.getThread() + " [" + tipProcessData.getTid() + "]";
            Rectangle2D processBounds =
                graphics.getFontMetrics(Final.NORMAL_FONT).getStringBounds(processName, graphics);
            Rectangle2D threadBounds = graphics.getFontMetrics(Final.NORMAL_FONT).getStringBounds(threadName, graphics);
            int tipWidth = (int) (Math.max(processBounds.getWidth(), threadBounds.getWidth()) + 20);
            graphics.setColor(getRoot().getForeground());
            graphics.fillRect(tipX, Utils.getY(getRect()), tipWidth, getRect().height);
            graphics.setColor(getRoot().getBackground());
            graphics.drawString(processName, tipX + 10, Utils.getY(getRect()) + 12);
            graphics.drawString(threadName, tipX + 10, Utils.getY(getRect()) + 24);
        }
    }

    /**
     * click handler
     *
     * @param event event
     */
    @Override
    public void mouseClicked(MouseEvent event) {
        super.mouseClicked(event);
        if (expandGraph.edgeInspect(event)) {
            expandGraph.onClick(event);
        }
    }

    /**
     * mouse pressed handler
     *
     * @param event event
     */
    @Override
    public void mousePressed(MouseEvent event) {
    }

    /**
     * mouse exited handler
     *
     * @param event event
     */
    @Override
    public void mouseExited(MouseEvent event) {
    }

    /**
     * mouse entered handler
     *
     * @param event event
     */
    @Override
    public void mouseEntered(MouseEvent event) {
    }

    /**
     * mouse moved handler
     *
     * @param ent event
     */
    @Override
    public void mouseMoved(MouseEvent ent) {
        MouseEvent event = getRealMouseEvent(ent);
        super.mouseMoved(event);
        clearFocus(event);
        if (!expandGraph.isExpand()) {
            tipProcessData = null;
            if (edgeInspect(event) && data != null) {
                focusProcessData = null;
                data.stream().filter(pd -> pd.getStartTime() + pd.getDuration() > startNS && pd.getStartTime() < endNS)
                    .forEach(pd -> {
                        if (pd.edgeInspect(event)) {
                            tipX = event.getX();
                            focusProcessData = pd;
                            tipProcessData = pd;
                            repaint();
                        }
                    });
            }
        }
    }

    /**
     * mouse released handler
     *
     * @param event event
     */
    @Override
    public void mouseReleased(MouseEvent event) {
    }

    /**
     * key released event
     *
     * @param event event
     */
    @Override
    public void keyReleased(KeyEvent event) {
    }

    /**
     * click handler
     *
     * @param event event
     */
    @Override
    public void click(MouseEvent event) {
        expandGraph.setExpand(!expandGraph.isExpand());
        if (this.getRoot() instanceof ContentPanel) {
            ContentPanel contentPanel = (ContentPanel) this.getRoot();
            if (expandGraph.isExpand()) {
                expandGraph.setImage(ImageUtils.getInstance().getArrowUpFocus());
                for (AbstractDataFragment dataFragment : contentPanel.fragmentList) {
                    if (dataFragment.parentUuid.equals(uuid)) {
                        dataFragment.visible = true;
                    }
                }
            } else {
                expandGraph.setImage(ImageUtils.getInstance().getArrowDownFocus());
                for (AbstractDataFragment dataFragment : contentPanel.fragmentList) {
                    if (dataFragment.parentUuid.equals(uuid)) {
                        dataFragment.visible = false;
                    }
                }
            }
            contentPanel.refresh();
        }
    }

    /**
     * expandThreads
     */
    public void expandThreads() {
        expandGraph.setExpand(true);
        if (this.getRoot() instanceof ContentPanel) {
            ContentPanel contentPanel = (ContentPanel) this.getRoot();
            if (expandGraph.isExpand()) {
                expandGraph.setImage(ImageUtils.getInstance().getArrowUpFocus());
                for (AbstractDataFragment dataFragment : contentPanel.fragmentList) {
                    if (dataFragment.parentUuid.equals(uuid)) {
                        dataFragment.visible = true;
                    }
                }
            } else {
                expandGraph.setImage(ImageUtils.getInstance().getArrowDownFocus());
                for (AbstractDataFragment dataFragment : contentPanel.fragmentList) {
                    if (dataFragment.parentUuid.equals(uuid)) {
                        dataFragment.visible = false;
                    }
                }
            }
            contentPanel.refresh();
        }
    }

    private void loadData() {
        if (!isLoading) {
            isLoading = true;
            CompletableFuture.runAsync(() -> {
                List<ProcessData> list = new ArrayList<>() {
                };
                int count = Db.getInstance()
                    .queryCount(st -> addStatement(st), Sql.SYS_QUERY_PROCESS_DATA_COUNT, process.getPid(), startNS,
                        endNS);
                if (count > Final.CAPACITY) {
                    Db.getInstance()
                        .query(st -> addStatement(st), Sql.SYS_QUERY_PROCESS_DATA_LIMIT, list, process.getPid(),
                            startNS, endNS, Final.CAPACITY);
                } else {
                    Db.getInstance()
                        .query(st -> addStatement(st), Sql.SYS_QUERY_PROCESS_DATA, list, process.getPid(), startNS,
                            endNS);
                }

                data = list;
                SwingUtilities.invokeLater(() -> {
                    isLoading = false;
                    repaint();
                });
            }, Utils.getPool()).whenComplete((unused, throwable) -> {
                if (Objects.nonNull(throwable)) {
                    throwable.printStackTrace();
                }
            });
        }
    }
}
