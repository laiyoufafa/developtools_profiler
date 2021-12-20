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

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.Consumer;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.applicationtrace.bean.Cpu;
import ohos.devtools.views.applicationtrace.bean.CpuFreq;
import ohos.devtools.views.applicationtrace.bean.Frame;
import ohos.devtools.views.applicationtrace.bean.Func;
import ohos.devtools.views.applicationtrace.bean.Thread;
import ohos.devtools.views.applicationtrace.bean.VsyncAppBean;
import ohos.devtools.views.applicationtrace.util.TimeUtils;
import ohos.devtools.views.distributed.bean.DistributedFuncBean;
import ohos.devtools.views.distributed.util.DistributedCommon;
import ohos.devtools.views.perftrace.bean.PrefFunc;

import javax.swing.Icon;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AbstractRow
 *
 * @since 2021/5/20 15:50
 */
public abstract class AbstractRow extends JBPanel {
    /**
     * current row content JBPanel
     */
    public JBPanel content;

    /**
     * current row startNS
     */
    protected long startNS;

    /**
     * current row endNS
     */
    protected long endNS;

    /**
     * current row isLoading
     */
    protected AtomicBoolean isLoading = new AtomicBoolean(false);

    /**
     * current row MigLayout layout
     */
    protected MigLayout layout = new MigLayout("inset 0", "0[110!,left]0[grow,fill]0", "0[grow,fill,center]0");

    /**
     * current row JBLabel nameLabel
     */
    protected JBLabel nameLabel = new JBLabel();

    /**
     * current row MouseEvent Consumer nameLabelClickConsumer
     */
    protected Consumer<MouseEvent> nameLabelClickConsumer;

    /**
     * current row JButton expandBtn
     */
    protected JButton expandBtn = new JButton();

    private Icon myExpandIcon = AllIcons.General.ArrowRight;
    private Icon myCollapseIcon = AllIcons.General.ArrowDown;
    private boolean myIsInitialized;
    private boolean myIsCollapsed;
    private int threadHeight = 14;
    private int funcHeight = 20;
    private int maxDept = 1;
    private final String rowName;
    private final int collapsedNodeHeight = 1;

    /**
     * fresh the Notify data by startNS and endNS
     *
     * @param name name
     * @param hasExpand hasExpand
     * @param defaultExpand defaultExpand
     */
    public AbstractRow(String name, boolean hasExpand, boolean defaultExpand) {
        this.rowName = name;
        setLayout(layout);
        expandBtn.setBorderPainted(false);
        expandBtn.setContentAreaFilled(false);
        expandBtn.setBackground(new Color(0, 0, 0, 0));
        expandBtn.setMaximumSize(new Dimension(myExpandIcon.getIconWidth(), myExpandIcon.getIconHeight()));
        expandBtn.setFocusable(true);
        nameLabel.setText(name);
        if (hasExpand) {
            add(expandBtn, "split 2,w 5!,gapleft 15,gapright 5");
            add(nameLabel, "gapleft 10,w 80!");
        } else {
            add(nameLabel, "gapleft 15,w 90!");
        }
        content = new JBPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                contentPaint(graphics);
            }
        };
        content.setBackground(JBColor.background().darker());
        add(content, "growx,pushx");
        setCollapsed(!defaultExpand);
        expandBtn.addActionListener(event -> setCollapsed(!myIsCollapsed));
    }

    /**
     * isCollapsed
     *
     * @return isCollapsed
     */
    public boolean isCollapsed() {
        return myIsCollapsed;
    }

    /**
     * getStartNS
     *
     * @return long startNS
     */
    public long getStartNS() {
        return startNS;
    }

    /**
     * getEndNS
     *
     * @return long endNS
     */
    public long getEndNS() {
        return endNS;
    }

    /**
     * get Row Name
     *
     * @return String rowName
     */
    public String getRowName() {
        return rowName;
    }

    /**
     * fresh the row data by startNS and endNS
     *
     * @param startNS startNS
     * @param endNS endNS
     */
    public void refresh(long startNS, long endNS) {
        this.startNS = startNS;
        this.endNS = endNS;
        content.repaint();
        refreshNotify();
    }

    /**
     * fresh the Notify data by startNS and endNS
     */
    public void refreshNotify() {
    }

    /**
     * fresh the Notify data by startNS and endNS
     *
     * @return return the Rectangle from the content
     */
    public Rectangle getContentBounds() {
        return content.getBounds();
    }

    /**
     * get the max height
     *
     * @return return the max height
     */
    protected int evalHeight() {
        int maxHeight = maxDept * getFuncHeight() + getFuncHeight();
        if (maxHeight < 30) {
            maxHeight = 30;
        }
        return maxHeight;
    }

    /**
     * Set whether to expand
     *
     * @param isCollapsed isCollapsed
     */
    public void setCollapsed(boolean isCollapsed) {
        setCollapsed(isCollapsed, null);
    }

    /**
     * Set whether to expand
     *
     * @param isCollapsed isCollapsed
     * @param finish null
     */
    public void setCollapsed(boolean isCollapsed, Consumer<Rectangle> finish) {
        int defHeight = getBounds().height;
        if (Objects.nonNull(finish)) {
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent event) {
                    super.componentResized(event);
                    if (defHeight != event.getComponent().getBounds().height) {
                        finish.consume(event.getComponent().getBounds());
                    }
                }
            });
        }
        try {
            if (isCollapsed) {
                setFuncHeight(collapsedNodeHeight);
                setThreadHeight(collapsedNodeHeight);
            } else {
                setFuncHeight(20);
                setThreadHeight(14);
            }
            if (getParent() != null && getParent().getLayout() instanceof MigLayout) {
                MigLayout migLayout = (MigLayout) getParent().getLayout();
                migLayout.setComponentConstraints(this, "growx,pushx,h " + evalHeight() + "!");
            }
            myIsCollapsed = isCollapsed;
            Icon icon = myIsCollapsed ? myExpandIcon : myCollapseIcon;
            if (icon != null) {
                expandBtn.setIcon(icon);
                expandBtn.setBorder(null);
                expandBtn.setBorderPainted(false);
            }
            if (isCollapsed) {
                expandBtn.requestFocusInWindow();
                expandBtn.setSelected(true);
            } else {
                content.requestFocusInWindow();
            }
            revalidate();
            repaint();
        } finally {
            myIsInitialized = true;
        }
    }

    /**
     * get the threadHeight
     *
     * @return return the current threadHeight
     */
    public int getThreadHeight() {
        return threadHeight;
    }

    /**
     * set the threadHeight
     *
     * @param threadHeight threadHeight
     */
    public void setThreadHeight(int threadHeight) {
        this.threadHeight = threadHeight;
    }

    /**
     * get the FuncHeight
     *
     * @return return the current FuncHeight
     */
    public int getFuncHeight() {
        return funcHeight;
    }

    /**
     * set the FuncHeight
     *
     * @param funcHeight funcHeight
     */
    public void setFuncHeight(int funcHeight) {
        this.funcHeight = funcHeight;
    }

    /**
     * get the Name
     *
     * @return return the current nameLabel text
     */
    public String getName() {
        return nameLabel.getText();
    }

    /**
     * contentPaint paint content
     *
     * @param graphics graphics
     */
    public abstract void contentPaint(Graphics graphics);

    /**
     * load data
     */
    public abstract void loadData();

    /**
     * set the Point
     *
     * @param point point
     */
    public void mouseMoveHandler(Point point) {
    }

    /**
     * get the time by x Coordinates
     *
     * @param xCoordinates xCoordinates
     * @return time string
     */
    public String getTimeByX(int xCoordinates) {
        double width = (getEndNS() - getStartNS()) * xCoordinates * 1.0 / getContentBounds().getWidth();
        return TimeUtils.getTimeFormatString((long) width + getStartNS());
    }

    /**
     * get the func is in this row
     *
     * @param func func
     * @return return the row is contains func
     */
    public boolean contains(Func func) {
        return func.getStartTs() + func.getDur() > getStartNS() && func.getStartTs() < getEndNS();
    }

    /**
     * get the threadData is in this row
     *
     * @param threadData threadData
     * @return return the row is contains threadData
     */
    public boolean contains(Thread threadData) {
        return threadData.getStartTime() + threadData.getDuration() > getStartNS()
            && threadData.getStartTime() < getEndNS();
    }

    /**
     * get the cpu is in this row
     *
     * @param cpu cpu
     * @return return the row is contains Cpu
     */
    public boolean contains(Cpu cpu) {
        return cpu.getStartTime() + cpu.getDuration() > getStartNS() && cpu.getStartTime() < getEndNS();
    }

    /**
     * get the CpuFreq is in this row
     *
     * @param item item
     * @return return the row is contains CpuFreq
     */
    public boolean contains(CpuFreq item) {
        return item.getStartTime() + item.getDuration() > getStartNS() && item.getStartTime() < getEndNS();
    }

    /**
     * get the VsyncAppBean is in this row
     *
     * @param item item
     * @return return the row is contains VsyncAppBean
     */
    public boolean contains(VsyncAppBean item) {
        return item.getStartTime() + item.getDuration() > getStartNS() && item.getStartTime() < getEndNS();
    }

    /**
     * get the Frame is in this row
     *
     * @param item item
     * @return return the row is contains Frame
     */
    public boolean contains(Frame item) {
        return item.getStartNs() + item.getDur() > getStartNS() && item.getStartNs() < getEndNS();
    }

    /**
     * get the PrefFunc is in this row
     *
     * @param func func
     * @return return the row is contains PrefFunc
     */
    public boolean contains(PrefFunc func) {
        return func.getEndTs() > getStartNS() && func.getStartTs() < getEndNS();
    }

    /**
     * get the PrefFunc is in this row
     *
     * @param func func
     * @return return the row is contains PrefFunc
     */
    public boolean contains(DistributedFuncBean func) {
        return func.getEndTs() > getStartNS() && func.getStartTs() < getEndNS();
    }

    /**
     * get the Object is in this row
     *
     * @param obj obj
     * @return return the row is contains obj
     */
    public boolean contains(Object obj) {
        if (obj instanceof PrefFunc) {
            return contains((PrefFunc) obj);
        } else if (obj instanceof CpuFreq) {
            return contains((CpuFreq) obj);
        } else if (obj instanceof Cpu) {
            return contains((Cpu) obj);
        } else if (obj instanceof Thread) {
            return contains((Thread) obj);
        } else if (obj instanceof Func) {
            return contains((Func) obj);
        } else if (obj instanceof VsyncAppBean) {
            return contains((VsyncAppBean) obj);
        } else if (obj instanceof Frame) {
            return contains((Frame) obj);
        } else if (obj instanceof DistributedFuncBean) {
            return contains((DistributedFuncBean) obj);
        } else {
            return false;
        }
    }

    /**
     * get the Rectangle by Cpu node
     *
     * @param node node
     * @param padding Rectangle padding
     * @return return Rectangle
     */
    public Rectangle getRectByNode(Cpu node, int padding) {
        double x1;
        double x2;
        if (node.getStartTime() < getStartNS()) {
            x1 = 0;
        } else {
            x1 = Common.ns2x(node.getStartTime(), getContentBounds());
        }
        if (node.getStartTime() + node.getDuration() > getEndNS()) {
            x2 = getContentBounds().getWidth();
        } else {
            x2 = Common.ns2x(node.getStartTime() + node.getDuration(), getContentBounds());
        }
        double getV = x2 - x1 <= 1 ? 1 : x2 - x1;
        Rectangle rectangle = new Rectangle((int) x1, (int) (getContentBounds().getY() + padding), (int) getV,
            (int) (getContentBounds().getHeight() - padding * 2));
        return rectangle;
    }

    /**
     * get the Rectangle by Thread node
     *
     * @param node node
     * @param height Rectangle height
     * @return return Rectangle
     */
    public Rectangle getRectByNode(Thread node, int height) {
        double x1;
        double x2;
        if (node.getStartTime() < getStartNS()) {
            x1 = Common.ns2x(getStartNS(), getContentBounds());
        } else {
            x1 = Common.ns2x(node.getStartTime(), getContentBounds());
        }
        if (node.getStartTime() + node.getDuration() > getEndNS()) {
            x2 = Common.ns2x(getEndNS(), getContentBounds());
        } else {
            x2 = Common.ns2x(node.getStartTime() + node.getDuration(), getContentBounds());
        }
        double getV = x2 - x1 <= 1 ? 1 : x2 - x1;
        Rectangle rectangle = new Rectangle((int) x1, (int) (getContentBounds().getY()), (int) getV, height);
        return rectangle;
    }

    /**
     * get the Rectangle by PrefFunc node
     *
     * @param node node
     * @param height Rectangle height
     * @param paddingTop Rectangle paddingTop
     * @return return Rectangle
     */
    public Rectangle getRectByNode(PrefFunc node, int paddingTop, int height) {
        double x1;
        double x2;
        if (node.getStartTs() < getStartNS()) {
            x1 = Common.ns2x(getStartNS(), getContentBounds());
        } else {
            x1 = Common.ns2x(node.getStartTs(), getContentBounds());
        }
        if (node.getEndTs() > getEndNS()) {
            x2 = Common.ns2x(getEndNS(), getContentBounds());
        } else {
            x2 = Common.ns2x(node.getEndTs(), getContentBounds());
        }
        double getV = x2 - x1 <= 1 ? 1 : x2 - x1;
        Rectangle rectangle =
            new Rectangle((int) x1, (int) (getContentBounds().getY() + node.getDepth() * height + paddingTop),
                (int) getV, height);
        return rectangle;
    }

    /**
     * get the Rectangle by Func node
     *
     * @param node node
     * @param height Rectangle height
     * @param paddingTop Rectangle paddingTop
     * @return return Rectangle
     */
    public Rectangle getRectByNode(Func node, int paddingTop, int height) {
        double x1;
        double x2;
        if (node.getStartTs() < getStartNS()) {
            x1 = Common.ns2x(getStartNS(), getContentBounds());
        } else {
            x1 = Common.ns2x(node.getStartTs(), getContentBounds());
        }
        if (node.getStartTs() + node.getDur() > getEndNS()) {
            x2 = Common.ns2x(getEndNS(), getContentBounds());
        } else {
            x2 = Common.ns2x(node.getStartTs() + node.getDur(), getContentBounds());
        }
        double getV = x2 - x1 <= 1 ? 1 : x2 - x1;
        Rectangle rectangle =
            new Rectangle((int) x1, (int) (getContentBounds().getY() + node.getDepth() * height + paddingTop),
                (int) getV, height);
        return rectangle;
    }

    /**
     * get the Rectangle by FuncBean node
     *
     * @param node node
     * @param height Rectangle height
     * @param paddingTop Rectangle paddingTop
     * @return return Rectangle
     */
    public Rectangle getRectByNode(DistributedFuncBean node, int paddingTop, int height) {
        double x1;
        double x2;
        if (node.getStartTs() < getStartNS()) {
            x1 = DistributedCommon.ns2x(getStartNS(), getContentBounds());
        } else {
            x1 = DistributedCommon.ns2x(node.getStartTs(), getContentBounds());
        }
        if (node.getStartTs() + node.getDur() > getEndNS()) {
            x2 = DistributedCommon.ns2x(getEndNS(), getContentBounds());
        } else {
            x2 = DistributedCommon.ns2x(node.getStartTs() + node.getDur(), getContentBounds());
        }
        double getV = x2 - x1 <= 1 ? 1 : x2 - x1;
        Rectangle rectangle =
            new Rectangle((int) x1, (int) (getContentBounds().getY() + node.getDepth() * height + paddingTop),
                (int) getV, height);
        return rectangle;
    }

    /**
     * get the Rectangle by CpuFreq node
     *
     * @param node node
     * @return return Rectangle
     */
    public Rectangle getRectByNode(CpuFreq node) {
        double x1;
        double x2;
        if (node.getStartTime() < getStartNS()) {
            x1 = Common.ns2x(getStartNS(), getContentBounds());
        } else {
            x1 = Common.ns2x(node.getStartTime(), getContentBounds());
        }
        if (node.getStartTime() + node.getDuration() > getEndNS()) {
            x2 = Common.ns2x(getEndNS(), getContentBounds());
        } else {
            x2 = Common.ns2x(node.getStartTime() + node.getDuration(), getContentBounds());
        }
        double getV = x2 - x1 <= 1 ? 1 : x2 - x1;
        Rectangle rectangle =
            new Rectangle((int) x1, (int) (getContentBounds().getY()), (int) getV, getContentBounds().height);
        return rectangle;
    }

    /**
     * get the Rectangle by VsyncAppBean node
     *
     * @param node node
     * @return return Rectangle
     */
    public Rectangle getRectByNode(VsyncAppBean node) {
        double x1;
        double x2;
        if (node.getStartTime() < getStartNS()) {
            x1 = Common.ns2x(getStartNS(), getContentBounds());
        } else {
            x1 = Common.ns2x(node.getStartTime(), getContentBounds());
        }
        if (node.getStartTime() + node.getDuration() > getEndNS()) {
            x2 = Common.ns2x(getEndNS(), getContentBounds());
        } else {
            x2 = Common.ns2x(node.getStartTime() + node.getDuration(), getContentBounds());
        }
        double getV = x2 - x1 <= 1 ? 1 : x2 - x1;
        Rectangle rectangle =
            new Rectangle((int) x1, (int) (getContentBounds().getY()), (int) getV, getContentBounds().height);
        return rectangle;
    }

    /**
     * get the Rectangle by Frame node
     *
     * @param node node
     * @param height Rectangle height
     * @return return Rectangle
     */
    public Rectangle getRectByNode(Frame node, int height) {
        double x1;
        double x2;
        if (node.getStartNs() < getStartNS()) {
            x1 = Common.ns2x(getStartNS(), getContentBounds());
        } else {
            x1 = Common.ns2x(node.getStartNs(), getContentBounds());
        }
        if (node.getStartNs() + node.getDur() > getEndNS()) {
            x2 = Common.ns2x(getEndNS(), getContentBounds());
        } else {
            x2 = Common.ns2x(node.getStartNs() + node.getDur(), getContentBounds());
        }
        double getV = x2 - x1 <= 1 ? 1 : x2 - x1;
        Rectangle rectangle = new Rectangle((int) x1, (int) (getContentBounds().getY() + 5), (int) getV, height);
        return rectangle;
    }

    /**
     * get the maxDept
     *
     * @return maxDept maxDept
     */
    public int getMaxDept() {
        return this.maxDept;
    }

    /**
     * set the maxDept
     *
     * @param maxDept maxDept
     */
    public void setMaxDept(int maxDept) {
        this.maxDept = maxDept;
    }
}
