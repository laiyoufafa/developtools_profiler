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
import ohos.devtools.views.trace.bean.CpuData;
import ohos.devtools.views.trace.component.AnalystPanel;
import ohos.devtools.views.trace.component.ContentPanel;
import ohos.devtools.views.trace.util.Db;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * cpu data
 *
 * @date 2021/04/22 12:25
 */
public class CpuDataFragment extends AbstractDataFragment<CpuData> implements CpuData.IEventListener {
    /**
     * The node that currently has focus
     */
    public static CpuData focusCpuData;

    /**
     * Currently selected cpu graphics node
     */
    public static CpuData currentSelectedCpuData;

    /**
     * If this value is not empty, select the node whose startTime is equal to this value after the data is loaded
     */
    public Long delayClickStartTime;

    /**
     * cpu data collection
     */
    private double x1;

    private double x2;

    private Rectangle2D bounds;

    private CpuData showTipCpuData; // Prompt window

    private int tipX; // X position of the message

    private int tipWidth; // Prompt message width

    private int index;
    private boolean isLoading;

    /**
     * structure
     *
     * @param root  root
     * @param index index
     * @param data  data
     */
    public CpuDataFragment(javax.swing.JComponent root, int index, List<CpuData> data) {
        super(root, true, false);
        this.index = index;
        this.setRoot(root);
        this.data = data;
    }

    /**
     * Gets the value of index .
     *
     * @return the value of int
     */
    public int getIndex() {
        return index;
    }

    /**
     * get cpu data list
     *
     * @return cpu data
     */
    public List<CpuData> getData() {
        return this.data;
    }

    /**
     * Drawing method
     *
     * @param graphics graphics
     */
    @Override
    public void draw(Graphics2D graphics) {
        super.draw(graphics);

        // Supplement the information on the left
        graphics.setColor(getRoot().getForeground());
        bounds = graphics.getFontMetrics().getStringBounds("Cpu " + index, graphics);
        graphics.drawString("Cpu " + index, (int) (getDescRect().getX() + 10),
            (int) (getDescRect().getY() + (getDescRect().getHeight()) / 2 + bounds.getHeight() / 3));
        if (Objects.isNull(data) || data.isEmpty()) {
            graphics.setColor(getRoot().getForeground());
            graphics.drawString("Loading...", Utils.getX(getDataRect()), Utils.getY(getDataRect()) + 12);
            loadData();
        } else {
            data.stream().filter(
                    cpuData -> cpuData.getStartTime() + cpuData.getDuration() > startNS
                            && cpuData.getStartTime() < endNS)
                .forEach(cpuGraph -> {
                    if (cpuGraph.getStartTime() < startNS) {
                        x1 = 0;
                    } else {
                        x1 = getXDouble(cpuGraph.getStartTime());
                    }
                    if (cpuGraph.getStartTime() + cpuGraph.getDuration() > endNS) {
                        x2 = getDataRect().width;
                    } else {
                        x2 = getXDouble(cpuGraph.getStartTime() + cpuGraph.getDuration());
                    }
                    cpuGraph.setRoot(getRoot());
                    double getV = x2 - x1 <= 0 ? 1 : x2 - x1;
                    cpuGraph
                        .setRect(x1 + getDataRect().getX(), getDataRect().getY() + 5, getV,
                            getDataRect().getHeight() - 10);
                    cpuGraph.setEventListener(CpuDataFragment.this);
                    cpuGraph.draw(graphics);
                });
        }
        drawTips(graphics);
    }

    private void drawTips(Graphics2D graphics) {
        if (showTipCpuData != null) {
            graphics.setFont(Final.NORMAL_FONT);
            if (showTipCpuData.getProcessName() == null || showTipCpuData.getProcessName().isEmpty()) {
                showTipCpuData.setProcessName(showTipCpuData.getName());
            }
            String process = "P:" + showTipCpuData.getProcessName() + " [" + showTipCpuData.getProcessId() + "]";
            String thread = "T:" + showTipCpuData.getName() + " [" + showTipCpuData.getTid() + "]";
            Rectangle2D processBounds = graphics.getFontMetrics(Final.NORMAL_FONT).getStringBounds(process, graphics);
            Rectangle2D threadBounds = graphics.getFontMetrics(Final.NORMAL_FONT).getStringBounds(thread, graphics);
            tipWidth = (int) (Math.max(processBounds.getWidth(), threadBounds.getWidth()) + 20);
            graphics.setColor(getRoot().getForeground());
            graphics.fillRect(tipX, Utils.getY(showTipCpuData.rect), tipWidth, showTipCpuData.rect.height);
            graphics.setColor(getRoot().getBackground());
            graphics.drawString(process, tipX + 10, Utils.getY(showTipCpuData.rect) + 12);
            graphics.drawString(thread, tipX + 10, Utils.getY(showTipCpuData.rect) + 24);
        }
    }

    /**
     * Mouse click event
     *
     * @param event event
     */
    @Override
    public void mouseClicked(MouseEvent event) {
        super.mouseClicked(event);
        ContentPanel.clickFragment = this;
        data.stream().filter(
                cpuData -> cpuData.getStartTime() + cpuData.getDuration() > startNS
                        && cpuData.getStartTime() < endNS)
            .filter(cpuData -> cpuData.edgeInspect(event)).findFirst().ifPresent(cpuData -> cpuData.onClick(event));
    }

    /**
     * Mouse pressed event
     *
     * @param event event
     */
    @Override
    public void mousePressed(MouseEvent event) {
    }

    /**
     * Mouse exited event
     *
     * @param event event
     */
    @Override
    public void mouseExited(MouseEvent event) {
    }

    /**
     * Mouse entered event
     *
     * @param event event
     */
    @Override
    public void mouseEntered(MouseEvent event) {
    }

    /**
     * Mouse move event
     *
     * @param evt evt
     */
    @Override
    public void mouseMoved(MouseEvent evt) {
        MouseEvent event = getRealMouseEvent(evt);
        super.mouseMoved(event);
        showTipCpuData = null;
        if (edgeInspect(event)) {
            data.stream().filter(
                    cpuData -> cpuData.getStartTime() +
                        cpuData.getDuration() > startNS && cpuData.getStartTime() < endNS)
                .forEach(cpuData -> {
                    cpuData.onMouseMove(event);
                    if (cpuData.edgeInspect(event)) {
                        if (!cpuData.flagFocus) {
                            cpuData.flagFocus = true;
                            cpuData.onFocus(event);
                        }
                    }
                });
        }
    }

    /**
     * Mouse released event
     *
     * @param event event
     */
    @Override
    public void mouseReleased(MouseEvent event) {
        data.clear();
        repaint();
    }

    /**
     * key released event
     *
     * @param event event
     */
    @Override
    public void keyReleased(KeyEvent event) {
        data.clear();
        repaint();
    }

    /**
     * Click event
     *
     * @param evt  event
     * @param data data
     */
    @Override
    public void click(MouseEvent evt, CpuData data) {
        MouseEvent event = getRealMouseEvent(evt);
        clearSelected();
        if (showTipCpuData != null) {
            showTipCpuData.select(true);
            showTipCpuData.repaint();
            currentSelectedCpuData = CpuDataFragment.focusCpuData;
            if (AnalystPanel.iCpuDataClick != null) {
                AnalystPanel.iCpuDataClick.click(showTipCpuData);
            }
        } else {
            currentSelectedCpuData = data;
            data.select(true);
            data.repaint();
            if (AnalystPanel.iCpuDataClick != null) {
                AnalystPanel.iCpuDataClick.click(data);
            }
        }
    }

    /**
     * Loss of focus event
     *
     * @param event event
     * @param data  data
     */
    @Override
    public void blur(MouseEvent event, CpuData data) {
        showTipCpuData = null;
        CpuDataFragment.focusCpuData = null;
        getRoot().repaint();
    }

    /**
     * Get focus event
     *
     * @param event event
     * @param data  data
     */
    @Override
    public void focus(MouseEvent event, CpuData data) {
        showTipCpuData = data;
        CpuDataFragment.focusCpuData = data;
        getRoot().repaint();
    }

    /**
     * Mouse movement event
     *
     * @param event event
     * @param data  data
     */
    @Override
    public void mouseMove(MouseEvent event, CpuData data) {
        showTipCpuData = data;
        CpuDataFragment.focusCpuData = data;
        tipX = event.getX();
        getRoot().repaint();
    }

    private void loadData() {
        if (!isLoading) {
            isLoading = true;
            CompletableFuture.runAsync(() -> {
                List<CpuData> cpuData = new ArrayList<>() {
                };
                int count = Db.getInstance().queryCount(Sql.SYS_QUERY_CPU_DATA_COUNT, index, startNS, endNS);
                if (count > Final.CAPACITY) {
                    Db.getInstance()
                        .query(Sql.SYS_QUERY_CPU_DATA_LIMIT, cpuData, index, startNS, endNS, Final.CAPACITY);
                } else {
                    Db.getInstance().query(Sql.SYS_QUERY_CPU_DATA, cpuData, index, startNS, endNS);
                }
                data = cpuData;
                SwingUtilities.invokeLater(() -> {
                    isLoading = false;
                    repaint();
                    if (delayClickStartTime != null) {
                        data.stream().filter(it -> it.getStartTime() == delayClickStartTime).findFirst()
                            .ifPresent(it -> click(null, it));
                        delayClickStartTime = null;
                    }
                });
            }, Utils.getPool()).whenComplete((unused, throwable) -> {
                if (Objects.nonNull(throwable)) {
                    throwable.printStackTrace();
                }
            });
        }
    }
}
