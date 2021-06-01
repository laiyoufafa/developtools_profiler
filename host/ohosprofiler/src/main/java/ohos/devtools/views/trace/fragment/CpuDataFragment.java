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

import ohos.devtools.views.trace.bean.CpuData;
import ohos.devtools.views.trace.component.AnalystPanel;
import ohos.devtools.views.trace.component.ContentPanel;
import ohos.devtools.views.trace.fragment.graph.CheckGraph;
import ohos.devtools.views.trace.fragment.graph.FavoriteGraph;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.TimeUtils;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Optional;

/**
 * cpu data
 *
 * @version 1.0
 * @date 2021/04/22 12:25
 **/
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
     * cpu data collection
     */
    public List<CpuData> data;

    /**
     * Favorite button
     */
    public FavoriteGraph favoriteGraph;

    /**
     * Select button
     */
    public CheckGraph checkGraph;

    private double x1;

    private double x2;

    private Rectangle2D bounds;

    private CpuData showTipCpuData; // Prompt window

    private int tipX; // X position of the message

    private int tipWidth; // Prompt message width

    private int index;

    private final BasicStroke boldStoke = new BasicStroke(2);
    private final BasicStroke normalStoke = new BasicStroke(1);

    /**
     * structure
     *
     * @param root  root
     * @param index index
     * @param data  data
     */
    public CpuDataFragment(javax.swing.JComponent root, int index, List<CpuData> data) {
        this.index = index;
        this.setRoot(root);
        this.data = data;
        favoriteGraph = new FavoriteGraph(this, root);
        checkGraph = new CheckGraph(this, root);
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
        favoriteGraph.setRightGraph(isSelected != null ? checkGraph : null);
        checkGraph.setChecked(isSelected);
        checkGraph.draw(graphics);
        favoriteGraph.draw(graphics);
        data.stream().filter(
            cpuData -> cpuData.getStartTime() + cpuData.getDuration() > startNS && cpuData.getStartTime() < endNS)
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
                    .setRect(x1 + getDataRect().getX(), getDataRect().getY() + 5, getV, getDataRect().getHeight() - 10);
                cpuGraph.setEventListener(CpuDataFragment.this);
                cpuGraph.draw(graphics);
            });
        drawTips(graphics);
        drawWakeup(graphics);
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
            graphics.fillRect(tipX, showTipCpuData.rect.y, tipWidth, showTipCpuData.rect.height);
            graphics.setColor(getRoot().getBackground());
            graphics.drawString(process, tipX + 10, showTipCpuData.rect.y + 12);
            graphics.drawString(thread, tipX + 10, showTipCpuData.rect.y + 24);
        }
    }

    private void drawWakeup(Graphics2D graphics) {
        if (getRoot() instanceof ContentPanel) {
            ContentPanel contentPanel = (ContentPanel) getRoot();
            Optional.ofNullable(contentPanel.getWakeupBean()).ifPresent(wakeup -> {
                int wakeupX = getX(wakeup.getWakeupTime());
                graphics.setColor(Color.BLACK);
                graphics.setStroke(boldStoke);
                Rectangle visibleRect = contentPanel.getVisibleRect();
                graphics.drawLine(wakeupX + getDataRect().x, visibleRect.y, wakeupX + getDataRect().x,
                    visibleRect.y + visibleRect.height);
                if (wakeup.getWakeupCpu() == index) {
                    final int[] xs =
                        {getDataRect().x + wakeupX, getDataRect().x + wakeupX + 6, getDataRect().x + wakeupX,
                            getDataRect().x + wakeupX - 6};
                    final int[] ys = {getRect().y + getRect().height / 2 - 10, getRect().y + getRect().height / 2,
                        getRect().y + getRect().height / 2 + 10, getRect().y + getRect().height / 2};
                    graphics.fillPolygon(xs, ys, xs.length);
                    if (currentSelectedCpuData != null) {
                        Rectangle rectangle = new Rectangle(wakeupX + getDataRect().x,
                            currentSelectedCpuData.rect.y + currentSelectedCpuData.rect.height / 2,
                            currentSelectedCpuData.rect.x - wakeupX - getDataRect().x, 30);
                        graphics.drawLine(getDataRect().x + wakeupX,
                            currentSelectedCpuData.rect.y + currentSelectedCpuData.rect.height - 2,
                            currentSelectedCpuData.rect.x,
                            currentSelectedCpuData.rect.y + currentSelectedCpuData.rect.height - 2);
                        if (rectangle.width > 10) {
                            drawArrow(graphics, getDataRect().x + wakeupX,
                                currentSelectedCpuData.rect.y + currentSelectedCpuData.rect.height - 2, -1);
                            drawArrow(graphics, currentSelectedCpuData.rect.x,
                                currentSelectedCpuData.rect.y + currentSelectedCpuData.rect.height - 2, 1);
                        }
                        long offsetTime = currentSelectedCpuData.getStartTime() - wakeup.getWakeupTime();
                        String timeString = TimeUtils.getTimeString(offsetTime);
                        rectangle.y -= 5;
                        drawString(graphics, rectangle, timeString, Placement.CENTER);
                    }
                }
                graphics.setStroke(normalStoke);
            });
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
     * Mouse click event
     *
     * @param event event
     */
    @Override
    public void mouseClicked(MouseEvent event) {
        if (favoriteGraph.edgeInspect(event)) {
            favoriteGraph.onClick(event);
        }
        if (checkGraph.edgeInspect(event)) {
            checkGraph.onClick(event);
        }
        data.stream().filter(
            cpuData -> cpuData.getStartTime() + cpuData.getDuration() > startNS && cpuData.getStartTime() < endNS)
            .filter(cpuData -> cpuData.edgeInspect(event)).findFirst().ifPresent(cpuData -> {
            cpuData.onClick(event);
        });
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
     * @param event event
     */
    @Override
    public void mouseMoved(MouseEvent event) {
        favoriteGraph.display(edgeInspectRect(getDescRect(), event));
        if (favoriteGraph.edgeInspect(event)) {
            if (!favoriteGraph.flagFocus) {
                favoriteGraph.flagFocus = true;
                favoriteGraph.onFocus(event);
            }
        } else {
            if (favoriteGraph.flagFocus) {
                favoriteGraph.flagFocus = false;
                favoriteGraph.onBlur(event);
            }
        }
        if (edgeInspect(event)) {
            data.stream().filter(
                cpuData -> cpuData.getStartTime() + cpuData.getDuration() > startNS && cpuData.getStartTime() < endNS)
                .forEach(cpuData -> {
                    cpuData.onMouseMove(event);
                    if (cpuData.edgeInspect(event)) {
                        if (!cpuData.flagFocus) {
                            cpuData.flagFocus = true;
                            cpuData.onFocus(event);
                        }
                    }
                });
        } else {
            showTipCpuData = null;
        }
        JComponent component = getRoot();
        if (component instanceof ContentPanel) {
            ContentPanel root = ((ContentPanel) component);
            root.refreshTab();
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
     * Click event
     *
     * @param event event
     * @param data  data
     */
    @Override
    public void click(MouseEvent event, CpuData data) {
        if (showTipCpuData != null) {
            clearSelected();
            showTipCpuData.select(true);
            showTipCpuData.repaint();
            currentSelectedCpuData = data;
            if (AnalystPanel.iCpuDataClick != null) {
                AnalystPanel.iCpuDataClick.click(showTipCpuData);
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
}
