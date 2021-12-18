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

package ohos.devtools.views.trace;

import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.Consumer;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.perftrace.bean.PrefFunc;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * TracePanel
 *
 * @since 2021/5/13 13:06
 */
public class TracePanel extends JBPanel {
    /**
     * DURATION
     */
    public static long DURATION = 10_000_000_000L;

    /**
     * currentSelectThreadIds
     */
    public static List<Integer> currentSelectThreadIds = new ArrayList<>();

    /**
     * time shaft start time
     */
    public static long START_TS;

    /**
     * time shaft end time
     */
    public static long END_TS;

    /**
     * root TracePanel
     */
    public static TracePanel root;

    /**
     * current start time
     */
    public static long startNS;

    /**
     * current end time
     */
    public static long endNS;

    /**
     * range start time
     */
    public static Long rangeStartNS;

    /**
     * range end time
     */
    public static Long rangeEndNS;
    private TimeShaft timeShaft;
    private Ruler ruler;
    private JBScrollPane scrollPane;
    private JBPanel contentPanel;
    private List<Component> componentList;
    private Point startPoint;
    private Point endPoint;
    private List<Component> allComponent;

    /**
     * structure function
     */
    public TracePanel() {
        this(true);
    }

    /**
     * TracePanel constructor
     *
     * @param showTimeShaft showTimeShaft
     */
    public TracePanel(boolean showTimeShaft) {
        timeShaft = new TimeShaft((startNS, endNS, scale) -> {
            EventDispatcher.dispatcherRange(startNS, endNS, scale);
            Component[] components = contentPanel.getComponents();
            for (Component component : components) {
                if (component instanceof ExpandPanel) {
                    ((ExpandPanel) component).refresh(startNS, endNS);
                }
            }
        }, keyEvent -> timeShaftComplete(), mouseEvent -> timeShaftComplete());
        contentPanel = new JBPanel();
        contentPanel.setLayout(new MigLayout("inset 0,wrap 1", "[grow,fill]", "0[]0"));
        contentPanel.setFocusable(true);
        contentPanel.setBorder(null);
        setLayout(new MigLayout("inset 0", "", "0[]0"));
        scrollPane = new JBScrollPane(contentPanel);
        scrollPane.setBorder(null);
        ruler = new Ruler(TracePanel.DURATION);
        if (showTimeShaft) {
            add(ruler, "wrap,pushx,growx,h 20!");
            add(timeShaft, "wrap,pushx,growx,h 50!");
        }
        add(scrollPane, "push,grow");
        setBorder(null);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(event -> {
            Component[] components = contentPanel.getComponents();
            for (Component component : components) {
                if (scrollPane.getViewport().getViewRect().intersects(component.getBounds())) {
                    if (component instanceof ExpandPanel) {
                        ((ExpandPanel) component).refresh(startNS, endNS);
                    }
                }
            }
        });
        root = this;
        contentPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                super.mousePressed(event);
                mousePressedThreadRow(event);
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                super.mouseClicked(event);
                PrefFunc.setSelectedPrefFunc(null);
                mouseClickThreadRow(event);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                super.mouseExited(event);
                Tip.getInstance().hidden();
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                super.mouseReleased(event);
            }
        });
        contentPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent event) {
                super.mouseDragged(event);
                PrefFunc.setSelectedPrefFunc(null);
                mouseDraggedThreadRow(event);
            }

            @Override
            public void mouseMoved(MouseEvent event) {
                super.mouseMoved(event);
                tip(event);
            }
        });
    }

    /**
     * range end time
     *
     * @return get current contentPanel
     */
    public JBPanel getContentPanel() {
        return contentPanel;
    }

    private void timeShaftComplete() {
        Arrays.stream(contentPanel.getComponents()).filter(it -> it instanceof ExpandPanel)
            .map(it -> ((ExpandPanel) it)).filter(it -> !it.isCollapsed()).forEach(
            it -> Arrays.stream(it.getContent().getComponents()).filter(row -> row instanceof TraceSimpleRow)
                .map(row -> ((TraceSimpleRow) row))
                .filter(row -> row.getRowName().toLowerCase(Locale.ENGLISH).startsWith("cpu"))
                .forEach(row -> row.reload()));
    }

    private void tip(MouseEvent event) {
        if (Objects.isNull(allComponent)) {
            allComponent = Arrays.stream(contentPanel.getComponents()).filter(ExpandPanel.class::isInstance)
                .flatMap(component -> Arrays.stream(((ExpandPanel) component).getContent().getComponents()))
                .collect(Collectors.toList());
        }
        boolean flag = allComponent.stream().anyMatch(it -> {
            if (it instanceof AbstractRow) {
                AbstractRow row = (AbstractRow) it;
                Rectangle rectangle = SwingUtilities.convertRectangle(row, row.getContentBounds(), contentPanel);
                if (rectangle.contains(event.getPoint())) {
                    return true;
                }
            }
            return false;
        });
        if (flag) {
            allComponent.forEach(component -> {
                if (component instanceof AbstractRow) {
                    AbstractRow row = (AbstractRow) component;
                    Rectangle rectangle = SwingUtilities.convertRectangle(row, row.getContentBounds(), contentPanel);
                    if (rectangle.contains(event.getPoint())) {
                        Point point = SwingUtilities.convertPoint(contentPanel, event.getPoint(), row.content);
                        row.mouseMoveHandler(point);
                    }
                }
            });
        } else {
            Tip.getInstance().hidden();
        }
    }

    /**
     * structure function
     *
     * @param startNS startNS
     * @param endNS endNS
     */
    public void setRange(long startNS, long endNS) {
        Optional.ofNullable(timeShaft).ifPresent(tf -> tf.setRange(startNS, endNS));
    }

    private void mouseDraggedThreadRow(MouseEvent event) {
        endPoint = SwingUtilities.convertPoint(contentPanel, event.getPoint(), componentList.get(0).getParent());
        int xPoint = Math.min(Utils.getX(startPoint), Utils.getX(endPoint));
        int yPoint = Math.min(Utils.getY(startPoint), Utils.getY(endPoint));
        int width = Math.abs(Utils.getX(startPoint) - Utils.getX(endPoint)) == 0 ? 1
            : Math.abs(Utils.getX(startPoint) - Utils.getX(endPoint));
        int height = Math.abs(Utils.getY(startPoint) - Utils.getY(endPoint));
        Rectangle range = new Rectangle(xPoint, yPoint, width, height);

        for (Component component : componentList) {
            if (component instanceof TraceThreadRow) {
                TraceThreadRow cp = (TraceThreadRow) component;
                if (range.intersects(component.getBounds())) {
                    if (!currentSelectThreadIds.contains(cp.getTid())) {
                        currentSelectThreadIds.add(cp.getTid());
                    }
                    cp.setSelect(true, xPoint - Utils.getX(cp.getContentBounds()),
                        xPoint + width - Utils.getX(cp.getContentBounds()));
                } else {
                    if (currentSelectThreadIds.contains(cp.getTid())) {
                        currentSelectThreadIds.remove(cp.getTid());
                    }
                    cp.setSelect(false, null, null);
                }
            }
        }
        notifySelectRangeChange();
        Tip.getInstance().hidden();
    }

    private void mouseClickThreadRow(MouseEvent event) {
        TracePanel.rangeStartNS = null;
        TracePanel.rangeEndNS = null;
        AtomicBoolean flag = new AtomicBoolean(false);
        componentList.forEach(component -> {
            if (component instanceof TraceThreadRow) {
                TraceThreadRow<?, ?> thread = (TraceThreadRow<?, ?>) component;
                if (thread.getBounds().contains(startPoint) && Utils.getX(startPoint) < Utils
                    .getX(thread.getContentBounds())) {
                    if (!currentSelectThreadIds.contains(thread.getTid())) {
                        currentSelectThreadIds.add(thread.getTid());
                    }
                    thread.setSelect(true, null, null);
                } else {
                    Point point = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), thread.content);
                    if ((thread.getData() != null && thread.getData().stream()
                        .anyMatch(it -> it.getRect().contains(point))) || (thread.getData2() != null && thread
                        .getData2().stream().anyMatch(it -> it.getRect().contains(point)))) {
                        if (Objects.nonNull(thread.getData())) {
                            thread.getData().stream().filter(it -> it.getRect().contains(point)).findFirst()
                                .ifPresent(it -> it.onClick(event));
                        }
                        if (Objects.nonNull(thread.getData2())) {
                            thread.getData2().stream().filter(it -> it.getRect().contains(point)).findFirst()
                                .ifPresent(it -> it.onClick(event));
                        }
                        flag.set(true);
                    }
                    currentSelectThreadIds.remove(thread.getTid());
                    thread.setSelect(false, null, null);
                }
            }
        });
        if (!flag.get()) {
            notifySelectRangeChange();
        }
    }

    private void notifySelectRangeChange() {
        if (Objects.isNull(TracePanel.rangeStartNS) && Objects.isNull(TracePanel.rangeEndNS)) {
            EventDispatcher.dispatcherThreadRange(TracePanel.startNS, TracePanel.endNS, currentSelectThreadIds);
        } else {
            long st = TracePanel.rangeStartNS < TracePanel.startNS ? TracePanel.startNS : TracePanel.rangeStartNS;
            long et = TracePanel.rangeEndNS > TracePanel.endNS ? TracePanel.endNS : TracePanel.rangeEndNS;
            EventDispatcher.dispatcherThreadRange(st, et, currentSelectThreadIds);
        }
    }

    private void mousePressedThreadRow(MouseEvent event) {
        if (Objects.isNull(componentList)) {
            componentList = Arrays.stream(contentPanel.getComponents()).filter(component -> {
                if (component instanceof ExpandPanel) {
                    ExpandPanel ep = (ExpandPanel) component;
                    if (!ep.getTitle().startsWith("Display") && !ep.getTitle().startsWith("CPU")) {
                        return true;
                    }
                }
                return false;
            }).flatMap(component -> Arrays.stream(((ExpandPanel) component).getContent().getComponents()))
                .collect(Collectors.toList());
        }
        if (componentList.size() > 0) {
            startPoint = SwingUtilities.convertPoint(contentPanel, event.getPoint(), componentList.get(0).getParent());
        }
    }

    /**
     * paint the TimeShaft
     *
     * @param consumer consumer
     */
    public void paintTimeShaft(Consumer<Graphics2D> consumer) {
        timeShaft.setTimeShaftConsumer(consumer);
        timeShaft.repaint();
    }

    /**
     * get the TimeShaft
     *
     * @return timeShaft timeShaft
     */
    public TimeShaft getTimeShaft() {
        return timeShaft;
    }

    /**
     * get the TimeRuler
     *
     * @return ruler timeRuler
     */
    public Ruler getRuler() {
        return ruler;
    }
}
