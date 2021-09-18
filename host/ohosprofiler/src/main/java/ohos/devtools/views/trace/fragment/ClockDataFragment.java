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
import ohos.devtools.views.trace.bean.Clock;
import ohos.devtools.views.trace.bean.ClockData;
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
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * cpu data
 *
 * @date 2021/04/22 12:25
 */
public class ClockDataFragment extends AbstractDataFragment<ClockData> implements ClockData.IEventListener {
    /**
     * The node that currently has focus
     */
    public static ClockData focusCpuData;

    /**
     * Currently selected cpu graphics node
     */
    public static ClockData currentSelectedCpuData;

    private final Clock clock;

    /**
     * cpu data collection
     */
    private double x1;

    private double x2;

    private Rectangle2D bounds;

    private ClockData showTipCpuData; // Prompt window

    private int tipX; // X position of the message

    private int tipWidth; // Prompt message width

    private boolean isLoading;

    private Long min;
    private Long max;

    /**
     * structure
     *
     * @param root  root
     * @param clock clock
     */
    public ClockDataFragment(javax.swing.JComponent root, Clock clock) {
        super(root, true, false);
        this.clock = clock;
        this.setRoot(root);
        this.data = data;
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
        bounds = graphics.getFontMetrics().getStringBounds(clock.getName(), graphics);
        graphics.drawString(clock.getName(), (int) (getDescRect().getX() + 10),
            (int) (getDescRect().getY() + (getDescRect().getHeight()) / 2 + bounds.getHeight() / 3));
        if (Objects.isNull(data) || data.isEmpty()) {
            graphics.setColor(getRoot().getForeground());
            graphics.drawString("Loading...", Utils.getX(getDataRect()), Utils.getY(getDataRect()) + 12);
            loadData();
        } else {
            data.stream()
                .filter(it -> it.getStartTime() + it.getDuration() > startNS && it.getStartTime() < endNS)
                .forEach(it -> {
                    if (it.getStartTime() < startNS) {
                        x1 = 0;
                    } else {
                        x1 = getXDouble(it.getStartTime());
                    }
                    if (it.getStartTime() + it.getDuration() > endNS) {
                        x2 = getDataRect().width;
                    } else {
                        x2 = getXDouble(it.getStartTime() + it.getDuration());
                    }
                    int index = data.indexOf(it);
                    if (index > 0) {
                        it.setDelta(it.getValue() - data.get(index - 1).getValue());
                    } else {
                        it.setDelta(0L);
                    }
                    it.setRoot(getRoot());
                    double getV = x2 - x1 <= 0 ? 1 : x2 - x1;
                    it.setRect(x1 + getDataRect().getX(), getDataRect().getY() + 5, getV,
                        getDataRect().getHeight() - 10);
                    it.setEventListener(ClockDataFragment.this);
                    it.setMinValue(min);
                    it.setMaxValue(max);
                    it.draw(graphics);
                });
        }
        drawTips(graphics);
    }

    private void drawTips(Graphics2D graphics) {
        if (showTipCpuData != null) {
            graphics.setFont(Final.NORMAL_FONT);
            String process = "value:" + showTipCpuData.getValue();
            String thread = "";
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
                cpuData -> cpuData.getStartTime() + cpuData.getDuration() > startNS && cpuData.getStartTime() < endNS)
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
        clearFocus(event);
        if (showTipCpuData != null) {
            showTipCpuData.select(false);
        }
        showTipCpuData = null;
        if (Objects.nonNull(data) && edgeInspect(event)) {
            data.stream().filter(
                    it -> it.getStartTime() + it.getDuration() > startNS && it.getStartTime() < endNS)
                .forEach(it -> {
                    it.onMouseMove(event);
                    if (it.edgeInspect(event)) {
                        if (!it.flagFocus) {
                            it.flagFocus = true;
                            it.onFocus(event);
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
     * Click event
     *
     * @param evt  event
     * @param data data
     */
    @Override
    public void click(MouseEvent evt, ClockData data) {
        MouseEvent event = getRealMouseEvent(evt);
        clearSelected();
        if (showTipCpuData != null) {
            showTipCpuData.select(true);
            showTipCpuData.repaint();
            currentSelectedCpuData = ClockDataFragment.focusCpuData;
            if (AnalystPanel.iClockDataClick != null) {
                AnalystPanel.iClockDataClick.click(showTipCpuData);
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
    public void blur(MouseEvent event, ClockData data) {
        if (showTipCpuData != null) {
            showTipCpuData.select(false);
        }
        showTipCpuData = null;
        ClockDataFragment.focusCpuData = null;
        getRoot().repaint();
    }

    /**
     * Get focus event
     *
     * @param event event
     * @param data  data
     */
    @Override
    public void focus(MouseEvent event, ClockData data) {
        showTipCpuData = data;
        showTipCpuData.select(true);
        ClockDataFragment.focusCpuData = data;
        getRoot().repaint();
    }

    /**
     * Mouse movement event
     *
     * @param event event
     * @param data  data
     */
    @Override
    public void mouseMove(MouseEvent event, ClockData data) {
        showTipCpuData = data;
        showTipCpuData.select(true);
        ClockDataFragment.focusCpuData = data;
        tipX = event.getX();
        getRoot().repaint();
    }

    private void loadData() {
        if (!isLoading) {
            isLoading = true;
            CompletableFuture.runAsync(() -> {
                ArrayList<ClockData> clockData = new ArrayList<>() {
                };
                if (clock.getName().endsWith(" Frequency")) {
                    Db.getInstance().query(Sql.SYS_QUERY_CLOCK_FREQUENCY, clockData, clock.getSrcname());
                    for (int idx = 0, len = clockData.size(); idx < len; idx++) {
                        ClockData it = clockData.get(idx);
                        if (idx == len - 1) {
                            it.setDuration(AnalystPanel.DURATION - it.getStartTime());
                        } else {
                            it.setDuration(clockData.get(idx + 1).getStartTime() - it.getStartTime());
                        }
                    }
                } else if (clock.getName().endsWith(" State")) {
                    Db.getInstance().query(Sql.SYS_QUERY_CLOCK_STATE, clockData, clock.getSrcname());
                } else {
                    if (clock.getName().endsWith("ScreenState")) {
                        Db.getInstance().query(Sql.SYS_QUERY_SCREEN_STATE, clockData);
                        for (int idx = 0, len = clockData.size(); idx < len; idx++) {
                            ClockData it = clockData.get(idx);
                            if (idx == len - 1) {
                                it.setDuration(AnalystPanel.DURATION - it.getStartTime());
                            } else {
                                it.setDuration(clockData.get(idx + 1).getStartTime() - it.getStartTime());
                            }
                        }
                    }
                }
                min = clockData.stream().mapToLong(ClockData::getValue).min().orElseThrow(NoSuchElementException::new);
                max = clockData.stream().mapToLong(ClockData::getValue).max().orElseThrow(NoSuchElementException::new);
                data = clockData;
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
