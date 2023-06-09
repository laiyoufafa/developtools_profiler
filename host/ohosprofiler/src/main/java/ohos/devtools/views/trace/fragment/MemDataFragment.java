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
import ohos.devtools.views.trace.bean.ProcessMem;
import ohos.devtools.views.trace.bean.ProcessMemData;
import ohos.devtools.views.trace.component.AnalystPanel;
import ohos.devtools.views.trace.component.ContentPanel;
import ohos.devtools.views.trace.util.Db;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Memory data line
 *
 * @since 2021/04/22 12:25
 */
public class MemDataFragment extends AbstractDataFragment<ProcessMemData> implements ProcessMemData.IEventListener {
    /**
     * graph event callback
     */
    private static ProcessMemData currentSelectedMemData;

    /**
     * The node that currently has focus
     */
    private static ProcessMemData currentFocusMemData;

    /**
     * Process memory
     */
    public ProcessMem mem;

    private ProcessMemData showTipData; // Prompt window
    private int tipX; // X position of the message
    private int tipWidth; // Prompt message width
    private boolean isLoading;
    private Rectangle2D bounds;
    private int max;

    /**
     * structure
     *
     * @param root root
     * @param mem mem
     */
    public MemDataFragment(JComponent root, ProcessMem mem) {
        super(root, true, false);
        this.mem = mem;
        this.setRoot(root);
    }

    /**
     * Drawing method
     *
     * @param graphics graphics
     */
    @Override
    public void draw(Graphics2D graphics) {
        super.draw(graphics);
        graphics.setFont(Final.NORMAL_FONT);
        graphics.setColor(getRoot().getForeground());
        String name = mem.getTrackName();
        bounds = graphics.getFontMetrics().getStringBounds(name, graphics);
        double wordWidth = bounds.getWidth() / name.length(); // Width per character
        double wordNum = (getDescRect().width - 40) / wordWidth; // How many characters can be displayed on each line
        if (bounds.getWidth() < getDescRect().width - 40) { // Direct line display
            graphics.drawString(name, Utils.getX(getDescRect()) + 10,
                (int) (Utils.getY(getDescRect()) + bounds.getHeight() + 10));
        } else {
            String substring = name.substring((int) wordNum);
            if (substring.length() < wordNum) {
                graphics.drawString(name.substring(0, (int) wordNum), Utils.getX(getDescRect()) + 10,
                    (int) (Utils.getY(getDescRect()) + bounds.getHeight() + 8));
                graphics.drawString(substring, Utils.getX(getDescRect()) + 10,
                    (int) (Utils.getY(getDescRect()) + bounds.getHeight() * 2 + 8));
            } else {
                graphics.drawString(name.substring(0, (int) wordNum), Utils.getX(getDescRect()) + 10,
                    (int) (Utils.getY(getDescRect()) + bounds.getHeight() + 2));
                graphics.drawString(substring.substring(0, (int) wordNum), Utils.getX(getDescRect()) + 10,
                    (int) (Utils.getY(getDescRect()) + bounds.getHeight() * 2 + 2));
            }
        }
        drawData(graphics);
    }

    private void drawData(Graphics2D graphics) {
        if (data != null) {
            List<ProcessMemData> collect = data.stream().filter(
                memData -> memData.getStartTime() + memData.getDuration() > startNS && memData.getStartTime() < endNS)
                .collect(Collectors.toList());
            int x1;
            int x2;
            for (int index = 0, len = collect.size(); index < len; index++) {
                ProcessMemData memData = collect.get(index);
                if (index == len - 1) {
                    memData.setDuration(AnalystPanel.getDURATION());
                } else {
                    memData.setDuration(collect.get(index + 1).getStartTime() - memData.getStartTime());
                }
                if (memData.getStartTime() < startNS) {
                    x1 = getX(startNS);
                } else {
                    x1 = getX(memData.getStartTime());
                }
                if (memData.getStartTime() + memData.getDuration() > endNS) {
                    x2 = getX(endNS);
                } else {
                    x2 = getX(memData.getStartTime() + memData.getDuration());
                }
                if (index > 0) {
                    memData.setDelta((long) (memData.getValue() - data.get(index - 1).getValue()));
                } else {
                    memData.setDelta(0L);
                }
                memData.root = getRoot();
                memData.setRect(x1 + Utils.getX(getDataRect()), Utils.getY(getDataRect()), x2 - x1 <= 0 ? 1 : x2 - x1,
                    getDataRect().height);
                memData.setEventListener(MemDataFragment.this);
                memData.setMaxValue(max);
                memData.draw(graphics);
            }
        } else {
            graphics.setColor(getRoot().getForeground());
            graphics.drawString("Loading...", Utils.getX(getDataRect()), Utils.getY(getDataRect()) + 12);
            loadData();
        }
        drawTips(graphics);
    }

    private void drawTips(Graphics2D graphics) {
        if (showTipData != null) {
            graphics.setFont(Final.NORMAL_FONT);
            String process = "value:" + showTipData.getValue();
            String thread = "";
            Rectangle2D processBounds = graphics.getFontMetrics(Final.NORMAL_FONT).getStringBounds(process, graphics);
            Rectangle2D threadBounds = graphics.getFontMetrics(Final.NORMAL_FONT).getStringBounds(thread, graphics);
            tipWidth = (int) (Math.max(processBounds.getWidth(), threadBounds.getWidth()) + 20);
            graphics.setColor(getRoot().getForeground());
            graphics.fillRect(tipX, Utils.getY(showTipData.rect), tipWidth, showTipData.rect.height);
            graphics.setColor(getRoot().getBackground());
            graphics.drawString(process, tipX + 10, Utils.getY(showTipData.rect) + 12);
            graphics.drawString(thread, tipX + 10, Utils.getY(showTipData.rect) + 24);
        }
    }

    /**
     * Mouse clicked event
     *
     * @param event event
     */
    @Override
    public void mouseClicked(MouseEvent event) {
        super.mouseClicked(event);
        ContentPanel.clickFragment = this;
        if (data != null) {
            data.stream().filter(
                memData -> memData.getStartTime() + memData.getDuration() > startNS && memData.getStartTime() < endNS)
                .filter(memData -> memData.edgeInspect(event)).findFirst().ifPresent(memData -> memData.onClick(event));
        }
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

    @Override
    public void mouseEntered(MouseEvent event) {
    }

    @Override
    public void mouseMoved(MouseEvent evt) {
        MouseEvent event = getRealMouseEvent(evt);
        super.mouseMoved(event);
        clearFocus(event);
        if (showTipData != null) {
            showTipData.select(false);
        }
        showTipData = null;
        if (Objects.nonNull(data) && edgeInspect(event)) {
            data.stream().filter(it -> it.getStartTime() + it.getDuration() > startNS && it.getStartTime() < endNS)
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

    private void loadData() {
        if (!isLoading) {
            isLoading = true;
            CompletableFuture.runAsync(() -> {
                List<ProcessMemData> list = new ArrayList<>() {
                };
                Db.getInstance().query(st -> addStatement(st), Sql.SYS_GET_PROCESS_MEM_DATA, list, mem.getTrackId());
                data = list;
                SwingUtilities.invokeLater(() -> {
                    data.stream().mapToInt(memData -> memData.getValue()).max().ifPresent(maxData -> {
                        max = maxData;
                    });
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

    /**
     * Mouse click event
     *
     * @param event event
     * @param data data
     */
    @Override
    public void click(MouseEvent event, ProcessMemData data) {
        clearSelected();
        if (showTipData != null) {
            showTipData.select(true);
            showTipData.repaint();
            currentSelectedMemData = MemDataFragment.currentFocusMemData;
            if (AnalystPanel.getiMemDataClick() != null) {
                AnalystPanel.getiMemDataClick().click(showTipData);
            }
        }
    }

    /**
     * Mouse blur event
     *
     * @param event event
     * @param data data
     */
    @Override
    public void blur(MouseEvent event, ProcessMemData data) {
        if (showTipData != null) {
            showTipData.select(false);
        }
        showTipData = null;
        MemDataFragment.currentFocusMemData = null;
        getRoot().repaint();
    }

    /**
     * Mouse focus event
     *
     * @param event event
     * @param data data
     */
    @Override
    public void focus(MouseEvent event, ProcessMemData data) {
        showTipData = data;
        showTipData.select(true);
        MemDataFragment.currentFocusMemData = data;
        getRoot().repaint();
    }

    /**
     * Mouse move event
     *
     * @param event event
     * @param data data
     */
    @Override
    public void mouseMove(MouseEvent event, ProcessMemData data) {
        showTipData = data;
        showTipData.select(true);
        MemDataFragment.currentFocusMemData = data;
        tipX = event.getX();
        getRoot().repaint();
    }
}
