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

package ohos.devtools.views.distributed;

import com.intellij.ui.AncestorListenerAdapter;
import com.intellij.ui.components.JBPanel;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.distributed.bean.DistributedFuncBean;
import ohos.devtools.views.distributed.bean.DistributedParams;
import ohos.devtools.views.distributed.bean.DistributedThreadBean;
import ohos.devtools.views.distributed.component.DeviceExpandPanel;
import ohos.devtools.views.distributed.component.DistributedTracePanel;
import ohos.devtools.views.distributed.component.TraceFuncRow;
import ohos.devtools.views.distributed.util.DistributedCache;
import ohos.devtools.views.distributed.util.DistributedDB;
import ohos.devtools.views.trace.EventDispatcher;
import ohos.devtools.views.trace.ExpandPanel;
import ohos.devtools.views.trace.Sql;
import ohos.devtools.views.trace.Tip;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * DistributedPanel
 *
 * @since 2021/7/6 15:36
 */
public class DistributedPanel extends JBPanel {
    /**
     * JLayeredPane layeredPane
     */
    private JLayeredPane layeredPane;
    private DistributedTracePanel tracePanel;
    private Tip tip = Tip.getInstance();
    private DistributedParams params;
    private DistributedDataPane tab;
    private JBPanel spacerPanel = new JBPanel();
    private int displayHeight = 300;

    /**
     * DistributedPanel
     *
     */
    public DistributedPanel() {
        setLayout(new MigLayout("insets 0"));
    }

    /**
     * load by paramstest
     *
     * @param params params
     */
    public void load(DistributedParams params) {
        this.params = params;
        Utils.resetPool();
        recycleData();
        DistributedDB.setDbName(params.getPathA(), params.getPathB());
        DistributedDB.load(true);
        initUI();
        EventDispatcher.addThreadRangeListener((startNS, endNS, threadIds) -> {
            if (threadIds.isEmpty()) {
                hideTab();
            } else {
                Optional.ofNullable(tab).ifPresent(it -> {
                    Rectangle rectangle =
                        SwingUtilities.convertRectangle(tracePanel, tracePanel.getBounds(), layeredPane);
                    displayTab(
                        new Rectangle(Utils.getX(rectangle), Utils.getY(rectangle) + displayHeight, rectangle.width,
                            rectangle.height - displayHeight));
                });
            }
        });
        EventDispatcher.addClickListener(it -> {
            if (it instanceof DistributedFuncBean) {
                DistributedFuncBean func = (DistributedFuncBean) it;
                Optional.ofNullable(tab).ifPresent(item -> {
                    Rectangle rectangle =
                        SwingUtilities.convertRectangle(tracePanel, tracePanel.getBounds(), layeredPane);
                    displayTab(
                        new Rectangle(Utils.getX(rectangle), Utils.getY(rectangle) + displayHeight, rectangle.width,
                            rectangle.height - displayHeight));
                });
            }
        });
    }

    private void recycleData() {
        removeAll();
        EventDispatcher.clearData();
        DistributedTracePanel.CURRENT_SELECT_THREAD_IDS.clear();
        DistributedFuncBean.currentSelectedFunc = null;
        DistributedCache.recycleData();
    }

    private void initUI() {
        DistributedCache.setDistribuetedParams(params);
        CompletableFuture.runAsync(() -> {
            DistributedDB.getInstance().queryA(Sql.DISTRIBUTED_QUERY_TOTAL_TIME, DistributedCache.DUR_A);
            DistributedDB.getInstance().queryB(Sql.DISTRIBUTED_QUERY_TOTAL_TIME, DistributedCache.DUR_B);
            if (DistributedCache.getDistribuetedParams().getOffsetA() != null) {
                DistributedDB.getInstance().updateA(Sql.DISTRIBUTED_SET_TRACE_RANGE_START_TIME,
                    DistributedCache.getDistribuetedParams().getOffsetA());
            }
            if (DistributedCache.getDistribuetedParams().getOffsetB() != null) {
                DistributedDB.getInstance().updateB(Sql.DISTRIBUTED_SET_TRACE_RANGE_START_TIME,
                    DistributedCache.getDistribuetedParams().getOffsetB());
            }
            if (!DistributedCache.DUR_A.isEmpty() && !DistributedCache.DUR_B.isEmpty()) {
                Long totalA = DistributedCache.DUR_A.get(0).getTotal();
                Long totalB = DistributedCache.DUR_B.get(0).getTotal();
                DistributedTracePanel.setDURATION(Math.max(totalA, totalB));
                DistributedTracePanel.startNS = 0;
                DistributedTracePanel.endNS = Math.max(totalA, totalB);
            }
            SwingUtilities.invokeLater(this::tracePanelInit);
        }, Utils.getPool()).whenComplete((unused, throwable) -> {
            if (Objects.nonNull(throwable)) {
                throwable.printStackTrace();
            }
        });
    }

    private void tracePanelInit() {
        setLayout(new MigLayout("insets 0"));
        tracePanel = new DistributedTracePanel();
        tab = new DistributedDataPane(it -> {
            hideTab();
        }, rectangle -> {
            setSpacerPanelHeight(rectangle.height);
        });
        insertDeviceA();
        insertDeviceB();
        tracePanel.getContentPanel().add(spacerPanel, "pushx,growx,h 200!");
        tracePanel.addAncestorListener(new AncestorListenerAdapter() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                super.ancestorAdded(event);
                layeredPane = DistributedPanel.this.getRootPane().getLayeredPane();
                layeredPane.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent componentEvent) {
                        super.componentResized(componentEvent);
                        if (tab != null) {
                            Rectangle rectangle =
                                SwingUtilities.convertRectangle(tracePanel, tracePanel.getBounds(), layeredPane);
                            displayHeight = layeredPane.getHeight() / 2;
                            tab.setBounds(new Rectangle(Utils.getX(rectangle), Utils.getY(rectangle) + displayHeight,
                                rectangle.width, rectangle.height - displayHeight));
                        }
                    }
                });
                SwingUtilities.invokeLater(() -> {
                    layeredPane.add(tab);
                    hideTab();
                    tip.setJLayeredPane(layeredPane);
                });
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                super.ancestorRemoved(event);
                hideTab();
                tip.hidden();
            }
        });
        add(tracePanel, "push,grow");
    }

    private void insertDeviceA() {
        DistributedDB.getInstance()
            .queryA(Sql.DISTRIBUTED_QUERY_THREADS_BY_PID, DistributedCache.THREADS_A, params.getProcessIdA());
        Map<Integer, String> threadNamesA = DistributedCache.THREADS_A.stream()
            .collect(Collectors.toMap(th -> th.getTid(), th -> th.getName() == null ? "" : th.getName()));
        DistributedCache.setThreadNamesA(threadNamesA);
        DeviceExpandPanel root =
            new DeviceExpandPanel(params.getPkgNameA() + " (" + params.getDeviceNameA() + ")", "A");
        ExpandPanel panel =
            new ExpandPanel("Process " + params.getProcessIdA() + " (" + DistributedCache.THREADS_A.size() + ")", 110);
        for (DistributedThreadBean thread : DistributedCache.THREADS_A) {
            TraceFuncRow<DistributedFuncBean> row = new TraceFuncRow<>(thread.getName(), thread.getTid());
            row.setRender((g2, data2) -> {
                if (data2 != null) {
                    data2.stream().filter(item -> row.contains(item)).forEach(func -> {
                        func.setRect(row.getRectByNode(func, 1, row.getFuncHeight()));
                        func.draw(g2);
                    });
                }
            });
            row.setSupplier(() -> {
                List<DistributedFuncBean> funcs = new ArrayList<>() {
                };
                DistributedDB.getInstance().queryA(Sql.DISTRIBUTED_GET_FUN_DATA_BY_TID, funcs, thread.getTid());
                funcs.forEach((item) -> {
                    item.setCurrentType(DistributedFuncBean.BeanDataType.TYPE_A);
                    DistributedCache.ID_FUNC_BEAN_MAP_A.put(item.getId(), item);
                });
                DistributedCache.FUNC_MAP_A.put(thread.getTid(), funcs);
                int maxDept = funcs.stream().mapToInt(DistributedFuncBean::getDepth).max().orElse(0) + 1;
                int maxHeight = maxDept * row.getFuncHeight() + row.getFuncHeight();
                if (maxHeight < 30) {
                    maxHeight = 30;
                }
                row.setMaxDept(maxDept);
                if (panel.getContent().getLayout() instanceof MigLayout) {
                    MigLayout migLayout = (MigLayout) panel.getContent().getLayout();
                    migLayout.setComponentConstraints(row, "growx,pushx,h " + maxHeight + "!");
                }
                row.updateUI();
                return funcs;
            });
            panel.addTraceRow(row);
        }
        root.addRow(panel, "gapleft 5,pushx,growx");
        tracePanel.getContentPanel().add(root, "pushx,growx");
    }

    private void insertDeviceB() {
        DistributedDB.getInstance()
            .queryB(Sql.DISTRIBUTED_QUERY_THREADS_BY_PID, DistributedCache.THREADS_B, params.getProcessIdB());
        Map<Integer, String> threadNamesB = DistributedCache.THREADS_B.stream()
            .collect(Collectors.toMap(th -> th.getTid(), th -> th.getName() == null ? "" : th.getName()));
        DistributedCache.setThreadNamesB(threadNamesB);
        DeviceExpandPanel root =
            new DeviceExpandPanel(params.getPkgNameB() + " (" + params.getDeviceNameB() + ")", "B");
        ExpandPanel panel =
            new ExpandPanel("Process " + params.getProcessIdB() + " (" + DistributedCache.THREADS_B.size() + ")", 110);
        for (DistributedThreadBean thread : DistributedCache.THREADS_B) {
            TraceFuncRow<DistributedFuncBean> row = new TraceFuncRow<>(thread.getName(), thread.getTid());
            row.setRender((g2, data2) -> {
                if (data2 != null) {
                    data2.stream().filter(item -> row.contains(item)).forEach(func -> {
                        func.setRect(row.getRectByNode(func, 1, row.getFuncHeight()));
                        func.draw(g2);
                    });
                }
            });
            row.setSupplier(() -> {
                List<DistributedFuncBean> funcs = new ArrayList<>() {
                };
                DistributedDB.getInstance().queryB(Sql.DISTRIBUTED_GET_FUN_DATA_BY_TID, funcs, thread.getTid());
                funcs.forEach((item) -> {
                    item.setCurrentType(DistributedFuncBean.BeanDataType.TYPE_B);
                    DistributedCache.FUNC_BEAN_MAP.put(item.getId(), item);
                });
                DistributedCache.FUNC_MAP_B.put(thread.getTid(), funcs);
                int maxDept = funcs.stream().mapToInt(DistributedFuncBean::getDepth).max().orElse(0) + 1;
                int maxHeight = maxDept * row.getFuncHeight() + row.getFuncHeight();
                if (maxHeight < 30) {
                    maxHeight = 30;
                }
                row.setMaxDept(maxDept);
                if (panel.getContent().getLayout() instanceof MigLayout) {
                    MigLayout migLayout = (MigLayout) panel.getContent().getLayout();
                    migLayout.setComponentConstraints(row, "growx,pushx,h " + maxHeight + "!");
                }
                row.updateUI();
                return funcs;
            });
            panel.addTraceRow(row);
        }
        root.addRow(panel, "gapleft 5,pushx,growx");
        tracePanel.getContentPanel().add(root, "pushx,growx");
    }

    private void hideTab() {
        if (tab != null && layeredPane != null) {
            if (DistributedFuncBean.currentSelectedFunc != null) {
                DistributedFuncBean.currentSelectedFunc.setSelected(false);
                DistributedFuncBean.currentSelectedFunc = null;
            }
            tab.setVisible(false);
            layeredPane.setLayer(tab, JLayeredPane.UNDEFINED_CONDITION);
            setSpacerPanelHeight(0);
            tracePanel.getTimeShaft().requestFocusInWindow();
        }
    }

    private void displayTab(Rectangle rectangle) {
        if (Objects.nonNull(tab) && Objects.nonNull(layeredPane)) {
            tab.setVisible(true);
            layeredPane.setLayer(tab, JLayeredPane.DRAG_LAYER);
            if (tab.getBounds().isEmpty()) {
                tab.setBounds(rectangle);
            }
            setSpacerPanelHeight(tab.getBounds().height);
        }
    }

    private void setSpacerPanelHeight(int height) {
        MigLayout layout = (MigLayout) tracePanel.getContentPanel().getLayout();
        layout.setComponentConstraints(spacerPanel, "growx,pushx,h " + (height - tab.getBarHeight()) + "!");
        spacerPanel.updateUI();
    }
}
