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

package ohos.devtools.views.distributed.component;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.applicationtrace.util.TimeUtils;
import ohos.devtools.views.distributed.DistributedDataPane;
import ohos.devtools.views.distributed.bean.DistributedFuncBean;
import ohos.devtools.views.distributed.util.DistributedCache;
import ohos.devtools.views.trace.AbstractRow;
import ohos.devtools.views.trace.EventDispatcher;
import ohos.devtools.views.trace.ExpandPanel;
import ohos.devtools.views.trace.Tip;
import ohos.devtools.views.trace.TraceSimpleRow;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
public class DistributedTracePanel extends JBPanel {
    /**
     * currentSelectThreadIds
     */
    public static final List<Integer> CURRENT_SELECT_THREAD_IDS = new ArrayList<>();

    /**
     * root TracePanel
     */
    public static DistributedTracePanel root;

    /**
     * current start time
     */
    public static long startNS;

    /**
     * current end time
     */
    public static long endNS;

    /**
     * DURATION 10_000_000_000L
     */
    private static long DURATION = 0L;

    /**
     * range start time
     */
    private static Long rangeStartNS;

    /**
     * range end time
     */
    private static Long rangeEndNS;

    private boolean isDragRelease = false;
    private DistributedTimeShaft timeShaft;
    private JBScrollPane scrollPane;
    private JBPanel contentPanel;
    private List<Component> componentList;
    private Point startPoint;
    private Point endPoint;
    private List<Component> allComponent;
    private boolean isInit = false;
    private JBLabel totalLabel = new JBLabel();
    private JBLabel rangeStartLabel = new JBLabel();

    /**
     * structure function
     */
    public DistributedTracePanel() {
        timeShaft = new DistributedTimeShaft((startNS, endNS, scale) -> {
            EventDispatcher.dispatcherRange(startNS, endNS, scale);
            Arrays.stream(contentPanel.getComponents()).filter(DeviceExpandPanel.class::isInstance)
                .map(it -> ((DeviceExpandPanel) it)).forEach(it -> it.refresh(startNS, endNS));
            rangeStartLabel.setText(ohos.devtools.views.trace.util.TimeUtils.getSecondFromNSecond(startNS));
        }, keyEvent -> timeShaftComplete(), mouseEvent -> timeShaftComplete());
        contentPanel = new JBPanel();
        contentPanel.setLayout(new MigLayout("inset 0,wrap 1", "0[grow,fill]0", "0[]0"));
        contentPanel.setFocusable(true);
        contentPanel.setBorder(null);
        setLayout(new MigLayout("inset 0", "0[115!]0[grow,fill]0", "0[]0"));
        scrollPane = new JBScrollPane(contentPanel);
        scrollPane.setBorder(null);
        totalLabel.setText("Total: " + TimeUtils.getDistributedTotalTime(DistributedTracePanel.getDURATION()));
        add(totalLabel, "span 1 1,align center");
        add(new DistributedRuler(DistributedTracePanel.getDURATION()), "pushx,growx, h 20!,wrap");
        add(rangeStartLabel, "span 1 1,align right,gapright 5");
        add(timeShaft, "pushx,growx,h 30!,wrap");
        add(scrollPane, "span 2,push,grow");
        setBorder(null);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(event -> Arrays.stream(contentPanel.getComponents())
            .filter(it -> scrollPane.getViewport().getViewRect().intersects(it.getBounds()))
            .filter(DeviceExpandPanel.class::isInstance).map(it -> ((DeviceExpandPanel) it))
            .forEach(it -> it.refresh(startNS, endNS)));
        root = this;
        contentPanel.addMouseListener(new ContentMouseAdapter());
        contentPanel.addMouseMotionListener(new ContentMouseMotionAdapter());
        contentPanel.addComponentListener(new ContentComponentAdapter());
        // Add monitor to change the selected func color block according to funcId
        addMonitorChange();
    }

    private void addMonitorChange() {
        EventDispatcher.addFuncSelectChange(funcId -> {
            if (Objects.nonNull(DistributedFuncBean.currentSelectedFunc)) {
                DistributedFuncBean.currentSelectedFunc.setSelected(false);
            }
            allComponent.stream().filter(TraceFuncRow.class::isInstance)
                .map(it -> ((TraceFuncRow<DistributedFuncBean>) it)).forEach(row -> {
                if (row.getData() != null) {
                    row.getData().stream().filter(it -> it.getId() == funcId).forEach(it -> {
                        it.setSelected(true);
                        // Rectangle is the bounds after row expansion
                        DistributedFuncBean.currentSelectedFunc = it;
                        int offsetHeight = 0;
                        for (Component component : scrollPane.getRootPane().getLayeredPane().getComponents()) {
                            if (component.getClass() == DistributedDataPane.class) {
                                offsetHeight = component.getBounds().height;
                            }
                        }
                        int finalOffsetHeight = offsetHeight;
                        if (row.isCollapsed()) {
                            row.setCollapsed(false, rectangle1 -> {
                                Rectangle rct =
                                    SwingUtilities.convertRectangle(row.content, it.getRect(), contentPanel);
                                scrollPane.getVerticalScrollBar().setValue(0);
                                scrollPane.getViewport().scrollRectToVisible(new Rectangle(0,
                                    Utils.getY(rct) + finalOffsetHeight + it.getRect().y + it.getRect().height,
                                    row.getBounds().width, rct.height));
                                long rangeX1 = it.getStartTs() - it.getDur() * 2;
                                if (rangeX1 < 0) {
                                    rangeX1 = 0;
                                }
                                long rangeX2 = it.getEndTs() + it.getDur() * 2;
                                timeShaft.setRange(rangeX1, rangeX2);
                                timeShaft.notifyRangeChange(rangeX1, rangeX2);
                            });
                        } else {
                            rowUnCollapsed(row, it, finalOffsetHeight);
                        }
                    });
                }
            });
        });
    }

    private void rowUnCollapsed(TraceFuncRow<DistributedFuncBean> row, DistributedFuncBean it,
        int finalOffsetHeight) {
        Rectangle rct = SwingUtilities.convertRectangle(row.content, it.getRect(), contentPanel);
        scrollPane.getVerticalScrollBar().setValue(0);
        scrollPane.getViewport().scrollRectToVisible(
            new Rectangle(0, Utils.getY(rct) + finalOffsetHeight + it.getRect().y + it.getRect().height,
                row.getBounds().width, rct.height));
        long rangeX1 = it.getStartTs() - it.getDur() * 2;
        if (rangeX1 < 0) {
            rangeX1 = 0;
        }
        long rangeX2 = it.getEndTs() + it.getDur() * 2;
        timeShaft.setRange(rangeX1, rangeX2);
        timeShaft.notifyRangeChange(rangeX1, rangeX2);
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
            allComponent = Arrays.stream(contentPanel.getComponents()).filter(DeviceExpandPanel.class::isInstance)
                .map(it -> ((DeviceExpandPanel) it)).flatMap(it -> Arrays.stream(it.getContent().getComponents()))
                .filter(ExpandPanel.class::isInstance).map(it -> ((ExpandPanel) it))
                .flatMap(it -> Arrays.stream(it.getContent().getComponents())).collect(Collectors.toList());
        }
        boolean flag = allComponent.stream().anyMatch(it -> {
            if (it instanceof AbstractRow) {
                AbstractRow row = (AbstractRow) it;
                Rectangle rectangle = SwingUtilities.convertRectangle(row, row.getContentBounds(), contentPanel);
                return rectangle.contains(event.getPoint());
            }
            return false;
        });
        if (flag) {
            allComponent.forEach(component -> {
                if (component instanceof AbstractRow) {
                    AbstractRow row = (AbstractRow) component;
                    Optional<ExpandPanel> exp = getExpandPanel(row);
                    Optional<DeviceExpandPanel> dxp = getDevicePanel(row);
                    if (exp.isPresent() && dxp.isPresent() && !exp.get().isCollapsed() && !dxp.get().isCollapsed()) {
                        Rectangle rectangle =
                            SwingUtilities.convertRectangle(row, row.getContentBounds(), contentPanel);
                        if (rectangle.contains(event.getPoint())) {
                            Point point = SwingUtilities.convertPoint(contentPanel, event.getPoint(), row.content);
                            row.mouseMoveHandler(point);
                        }
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

    private void notifySelectRangeChange() {
        if (Objects.isNull(DistributedTracePanel.rangeStartNS) && Objects.isNull(DistributedTracePanel.rangeEndNS)) {
            EventDispatcher.dispatcherThreadRange(DistributedTracePanel.startNS, DistributedTracePanel.endNS,
                CURRENT_SELECT_THREAD_IDS);
        } else {
            long st =
                DistributedTracePanel.rangeStartNS < DistributedTracePanel.startNS ? DistributedTracePanel.startNS
                    : DistributedTracePanel.rangeStartNS;
            long et = DistributedTracePanel.rangeEndNS > DistributedTracePanel.endNS ? DistributedTracePanel.endNS
                : DistributedTracePanel.rangeEndNS;
            EventDispatcher.dispatcherThreadRange(st, et, CURRENT_SELECT_THREAD_IDS);
        }
    }

    public DistributedTimeShaft getTimeShaft() {
        return timeShaft;
    }

    private Optional<DeviceExpandPanel> getDevicePanel(Container component) {
        if (component == null) {
            return Optional.empty();
        }
        if (component instanceof DeviceExpandPanel) {
            DeviceExpandPanel deviceExpandPanel = (DeviceExpandPanel) component;
            return Optional.ofNullable(deviceExpandPanel);
        } else {
            return getDevicePanel(component.getParent());
        }
    }

    private Optional<ExpandPanel> getExpandPanel(Container component) {
        if (component == null) {
            return Optional.empty();
        }
        if (component instanceof ExpandPanel) {
            ExpandPanel expandPanel = (ExpandPanel) component;
            return Optional.ofNullable(expandPanel);
        } else {
            return getExpandPanel(component.getParent());
        }
    }

    private class ContentMouseAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent event) {
            super.mousePressed(event);
            if (Objects.isNull(componentList)) {
                componentList =
                    Arrays.stream(contentPanel.getComponents()).filter(it -> it instanceof DeviceExpandPanel)
                        .map(it -> ((DeviceExpandPanel) it))
                        .flatMap(it -> Arrays.stream(it.getContent().getComponents()))
                        .filter(it -> it instanceof ExpandPanel).map(it -> ((ExpandPanel) it))
                        .flatMap(it -> Arrays.stream(it.getContent().getComponents())).collect(Collectors.toList());
            }
            if (componentList.size() > 0) {
                startPoint = event.getPoint();
                componentList.stream().filter(TraceFuncRow.class::isInstance).map(it -> ((TraceFuncRow<?>) it))
                    .forEach(thread -> {
                        Rectangle rect =
                            SwingUtilities.convertRectangle(thread.getParent(), thread.getBounds(), contentPanel);
                        if (rect.contains(startPoint) && Utils.getX(startPoint) < Utils
                            .getX(thread.getContentBounds())) {
                            Optional<DeviceExpandPanel> devicePanel = getDevicePanel(thread);
                            if (devicePanel.isPresent()) {
                                DistributedCache.setCurrentDBFlag(devicePanel.get().getDbFlag());
                            }
                        }
                    });
            }
        }

        @Override
        public void mouseClicked(MouseEvent event) {
            super.mouseClicked(event);
            DistributedTracePanel.rangeStartNS = null;
            DistributedTracePanel.rangeEndNS = null;
            AtomicBoolean flag = new AtomicBoolean(false);
            CURRENT_SELECT_THREAD_IDS.clear();
            componentList.stream().filter(TraceFuncRow.class::isInstance).map(it -> ((TraceFuncRow<?>) it))
                .forEach(thread -> {
                    Optional<ExpandPanel> exp = getExpandPanel(thread);
                    Optional<DeviceExpandPanel> dxp = getDevicePanel(thread);
                    if (exp.isPresent() && dxp.isPresent() && !exp.get().isCollapsed() && !dxp.get().isCollapsed()) {
                        // Create a rectangle
                        Rectangle rect =
                            SwingUtilities.convertRectangle(thread.getParent(), thread.getBounds(), contentPanel);

                        // If the click coordinates are in the thread name display area, select the thread
                        if (rect.contains(startPoint) && Utils.getX(startPoint) < Utils
                            .getX(thread.getContentBounds())) {
                            if (!CURRENT_SELECT_THREAD_IDS.contains(thread.getTid())) {
                                CURRENT_SELECT_THREAD_IDS.add(thread.getTid());
                            }
                            thread.setSelect(true, null, null);
                        } else {
                            // If the clicked coordinates are on the color block,
                            // it means that the color block is selected instead of the selected row
                            Point point =
                                SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), thread.content);
                            if ((Objects.nonNull(thread.getData()) && thread.getData().stream()
                                .anyMatch(it -> it.getRect().contains(point)))) {
                                thread.getData().stream().filter(it -> it.getRect().contains(point))
                                    .forEach(it -> it.onClick(event));
                                flag.set(true);
                            }
                            CURRENT_SELECT_THREAD_IDS.remove(thread.getTid());
                            thread.setSelect(false, null, null);
                        }
                    }
                });

            // The flag value is true to indicate that the click is on the method color block
            if (!flag.get()) {
                notifySelectRangeChange();
            }
        }

        @Override
        public void mouseExited(MouseEvent event) {
            super.mouseExited(event);
            Tip.getInstance().hidden();
        }

        @Override
        public void mouseReleased(MouseEvent event) {
            super.mouseReleased(event);
            if (isDragRelease) {
                notifySelectRangeChange();
                isDragRelease = false;
            }
        }
    }

    private class ContentMouseMotionAdapter extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent event) {
            super.mouseDragged(event);
            isDragRelease = true;
            endPoint = event.getPoint();
            int xPoint = Math.min(Utils.getX(startPoint), Utils.getX(endPoint));
            int yPoint = Math.min(Utils.getY(startPoint), Utils.getY(endPoint));
            int width = Math.abs(Utils.getX(startPoint) - Utils.getX(endPoint)) == 0 ? 1
                : Math.abs(Utils.getX(startPoint) - Utils.getX(endPoint));
            int height = Math.abs(Utils.getY(startPoint) - Utils.getY(endPoint));
            Rectangle range = new Rectangle(xPoint, yPoint, width, height);
            CURRENT_SELECT_THREAD_IDS.clear();
            componentList.stream().filter(TraceFuncRow.class::isInstance).map(it -> ((TraceFuncRow<?>) it))
                .forEach(thread -> {
                    Optional<ExpandPanel> exp = getExpandPanel(thread);
                    Optional<DeviceExpandPanel> dxp = getDevicePanel(thread);
                    if (exp.isPresent() && dxp.isPresent() && !exp.get().isCollapsed() && !dxp.get().isCollapsed()) {
                        Rectangle rect =
                            SwingUtilities.convertRectangle(thread.getParent(), thread.getBounds(), contentPanel);
                        if (range.intersects(rect)) {
                            if (!CURRENT_SELECT_THREAD_IDS.contains(thread.getTid())) {
                                CURRENT_SELECT_THREAD_IDS.add(thread.getTid());
                            }
                            thread.setSelect(true, xPoint - Utils.getX(thread.getContentBounds()),
                                xPoint + width - Utils.getX(thread.getContentBounds()));
                        } else {
                            if (CURRENT_SELECT_THREAD_IDS.contains(thread.getTid())) {
                                CURRENT_SELECT_THREAD_IDS.remove(thread.getTid());
                            }
                            thread.setSelect(false, null, null);
                        }
                    }
                });
            Tip.getInstance().hidden();
        }

        @Override
        public void mouseMoved(MouseEvent event) {
            super.mouseMoved(event);
            tip(event);
        }
    }

    private class ContentComponentAdapter extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent event) {
            super.componentResized(event);
            if (!isInit) {
                isInit = true;
                Arrays.stream(contentPanel.getComponents()).filter(DeviceExpandPanel.class::isInstance)
                    .map(it -> ((DeviceExpandPanel) it)).forEach(it -> it.refresh(startNS, endNS));
            }
        }
    }

    public static long getDURATION() {
        return DURATION;
    }

    public static void setDURATION(long DURATION) {
        DistributedTracePanel.DURATION = DURATION;
    }

    public static Long getRangeStartNS() {
        return rangeStartNS;
    }

    public static void setRangeStartNS(Long rangeStartNS) {
        DistributedTracePanel.rangeStartNS = rangeStartNS;
    }

    public static Long getRangeEndNS() {
        return rangeEndNS;
    }

    public static void setRangeEndNS(Long rangeEndNS) {
        DistributedTracePanel.rangeEndNS = rangeEndNS;
    }
}
