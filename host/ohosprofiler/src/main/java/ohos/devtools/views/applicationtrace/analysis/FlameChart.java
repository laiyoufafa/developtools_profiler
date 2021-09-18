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

package ohos.devtools.views.applicationtrace.analysis;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import ohos.devtools.views.applicationtrace.bean.TreeTableBean;
import ohos.devtools.views.applicationtrace.listener.IAllThreadDataListener;
import ohos.devtools.views.applicationtrace.listener.IOtherThreadDataListener;
import ohos.devtools.views.trace.Common;
import ohos.devtools.views.trace.EventPanel;
import ohos.devtools.views.trace.Tip;
import ohos.devtools.views.trace.TracePanel;
import ohos.devtools.views.trace.util.Utils;
import org.apache.commons.lang3.Range;

import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_D;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_W;

/**
 * FlameChart
 *
 * @version 1.0
 * @date: 2021/5/24 11:57
 */
public class FlameChart extends EventPanel implements KeyListener, MouseListener, MouseMotionListener {
    private static final int ROW_HEIGHT = 16;

    private List<DefaultMutableTreeNode> data;
    private long duration;
    private int xPoint = 0;
    private int visibleWidth;
    private IAllThreadDataListener iAllThreadDataListener;
    private IOtherThreadDataListener iOtherThreadDataListener;
    private List<TreeTableBean> dataBean = new ArrayList<>();
    private TreeTableBean activeBean;
    private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
    private String currentSearchText = "";
    private double percent = 0;

    /**
     * constructor
     */
    public FlameChart() {
        this(null, null);
    }

    /**
     * constructor with listener
     *
     * @param iAllThreadDataListener   all thread data listener
     * @param iOtherThreadDataListener other thread data listener
     */
    public FlameChart(IAllThreadDataListener iAllThreadDataListener,
        IOtherThreadDataListener iOtherThreadDataListener) {
        this.iOtherThreadDataListener = iOtherThreadDataListener;
        this.iAllThreadDataListener = iAllThreadDataListener;
        initChart();
    }

    private void initChart() {
        setOpaque(true);
        setBackground(JBColor.background().brighter());
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    public void change(long startNS, long endNS, long scale) {
        if (iAllThreadDataListener != null) {
            freshData(startNS, endNS, null, scale);
        }
        if (iOtherThreadDataListener != null && TracePanel.rangeStartNS == null && TracePanel.rangeEndNS == null) {
            change(startNS, endNS, TracePanel.currentSelectThreadIds);
        }
    }

    @Override
    public void change(long startNS, long endNS, List<Integer> threadIds) {
        if (iOtherThreadDataListener != null) {
            freshData(startNS, endNS, threadIds, 0);
        }
    }

    /**
     * set the serach input text
     *
     * @param currentSearchText currentSearchText
     */
    public void setCurrentSearchText(String currentSearchText) {
        this.currentSearchText = currentSearchText;
        TopBottomPanel.getNodeContainSearch(rootNode, currentSearchText);
        int maxDepth = data.stream().mapToInt(it -> it.getDepth()).max().orElse(0);
        setPreferredSize(new Dimension(0, (maxDepth + 3) * ROW_HEIGHT));
        scrollToBottom();
    }

    private void freshData(long startNS, long endNS, List<Integer> threadIds, long scale) {
        duration = endNS - startNS;
        if (threadIds == null) {
            data = iAllThreadDataListener.getAllThreadData(startNS, endNS, scale);
        } else {
            data = iOtherThreadDataListener.getOtherThreadData(startNS, endNS, threadIds);
        }
        setAllNode(data);
    }

    /**
     * set all node in flame chart
     *
     * @param datasource datasource
     * @param dur        dur
     */
    public void setAllNode(List<DefaultMutableTreeNode> datasource, long dur) {
        duration = dur;
        setAllNode(datasource);
    }

    /**
     * set all node in flame chart
     *
     * @param datasource datasource
     */
    public void setAllNode(List<DefaultMutableTreeNode> datasource) {
        data = datasource;
        rootNode.removeAllChildren();
        datasource.forEach(item -> {
            rootNode.add(item);
        });
        TopBottomPanel.getNodeContainSearch(rootNode, currentSearchText);
        int maxDepth = datasource.stream().mapToInt(it -> it.getDepth()).max().orElse(0);
        setPreferredSize(new Dimension(0, (maxDepth + 3) * ROW_HEIGHT));
        SwingUtilities.invokeLater(this::scrollToBottom);
    }

    /**
     * reset all node in flame chart
     */
    public void resetAllNode() {
        TopBottomPanel.resetAllNode(rootNode);
        int maxDepth = data.stream().mapToInt(it -> it.getDepth()).max().orElse(0);
        setPreferredSize(new Dimension(0, (maxDepth + 3) * ROW_HEIGHT));
        scrollToBottom();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (graphics instanceof Graphics2D) {
            dataBean.clear();
            Graphics2D g2 = (Graphics2D) graphics;
            if (data != null && data.size() > 0) {
                int threadWidth = getWidth() / data.size();
                for (int index = 0; index < data.size(); index++) {
                    DefaultMutableTreeNode node = data.get(index);
                    if (node.getUserObject() instanceof TreeTableBean) {
                        TreeTableBean bean = (TreeTableBean) node.getUserObject();
                        bean.setRect(
                            new Rectangle(threadWidth * index, getHeight() - ROW_HEIGHT * 2, threadWidth, ROW_HEIGHT));
                        bean.draw(g2);
                        dataBean.add(bean);
                        if (node.getChildCount() > 0) {
                            duration = bean.getChildrenNS();
                            paintChild(g2, node, threadWidth * index, getHeight() - ROW_HEIGHT * 2, threadWidth);
                        }
                    }
                }
            }
        }
    }

    private void paintChild(Graphics2D g2, DefaultMutableTreeNode node, int xPoint, int yPoint, int tWidth) {
        int childCount = node.getChildCount();
        int left = xPoint;
        int tw = (int) (tWidth * 1.0);
        ArrayList<Integer> lines = new ArrayList<>();
        for (int index = 0; index < childCount; index++) {
            if (node.getChildAt(index) instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode nd = (DefaultMutableTreeNode) node.getChildAt(index);
                if (nd.getUserObject() instanceof TreeTableBean) {
                    TreeTableBean bean = (TreeTableBean) nd.getUserObject();
                    int funcWidth = getFuncWidth(bean.getChildrenNS(), tw);
                    lines.add(funcWidth);
                    bean.setRect(new Rectangle(left, yPoint - ROW_HEIGHT, funcWidth, ROW_HEIGHT));
                    bean.draw(g2);
                    dataBean.add(bean);
                    left += funcWidth;
                    if (nd.getChildCount() > 0) {
                        paintChild(g2, nd, Utils.getX(bean.getRect()), yPoint - ROW_HEIGHT, tw);
                    }
                }
            }
        }
        if (lines.stream().allMatch(line -> Objects.equals(line, 0))) {
            Common.setAlpha(g2, getAlpha(lines));
            g2.fillRect(xPoint, yPoint - ROW_HEIGHT, 1, ROW_HEIGHT);
        }
    }

    private float getAlpha(List list) {
        int size = list.size();
        float alpha;
        if (Range.between(1, 2).contains(size)) {
            alpha = 0.9f;
        } else if (Range.between(3, 5).contains(size)) {
            alpha = 0.8f;
        } else if (Range.between(6, 8).contains(size)) {
            alpha = 0.7f;
        } else if (Range.between(9, 11).contains(size)) {
            alpha = 0.6f;
        } else {
            alpha = 0.5f;
        }
        return alpha;
    }

    private int getFuncWidth(long dur, int tw) {
        long dura = dur;
        if (dur > duration) {
            dura = duration;
        }
        double wid = dura * tw * 1.0 / duration;
        return (int) wid;
    }

    @Override
    public void keyTyped(KeyEvent event) {
    }

    @Override
    public void keyPressed(KeyEvent event) {
        switch (event.getExtendedKeyCode()) {
            case VK_A:
                translation(-1);
                break;
            case VK_D:
                translation(1);
                break;
            case VK_W:
                scale(1);
                break;
            case VK_S:
                scale(-1);
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
    }

    @Override
    public void mouseClicked(MouseEvent event) {
    }

    @Override
    public void mousePressed(MouseEvent event) {
    }

    @Override
    public void mouseReleased(MouseEvent event) {
    }

    @Override
    public void mouseEntered(MouseEvent event) {
        requestFocusInWindow();
    }

    @Override
    public void mouseExited(MouseEvent event) {
        Tip.getInstance().hidden();
    }

    private void translation(int index) {
        if (getParent().getParent() instanceof JBScrollPane) {
            JBScrollPane parent = (JBScrollPane) getParent().getParent();
            int width = parent.getViewport().getVisibleRect().width;
            int offset = width / 10;
            JScrollBar jsBar = parent.getHorizontalScrollBar();
            if (index > 0) {
                xPoint += offset;
                if (xPoint >= jsBar.getMaximum() - width) {
                    xPoint = jsBar.getMaximum() - width;
                }
            } else {
                xPoint -= offset;
                if (xPoint <= jsBar.getMinimum()) {
                    xPoint = jsBar.getMinimum();
                }
            }
            jsBar.setValue(xPoint);
            percent = jsBar.getValue() * 1.0 / (jsBar.getMaximum());
        }
    }

    private void scale(int index) {
        if (getParent().getParent() instanceof JBScrollPane) {
            JBScrollPane parent = (JBScrollPane) getParent().getParent();
            visibleWidth = parent.getViewport().getVisibleRect().width;
            JScrollBar jsBar = parent.getHorizontalScrollBar();
            int fixWidth = getWidth();
            if (index > 0) {
                fixWidth += getWidth() * 0.2;
            } else {
                fixWidth -= getWidth() * 0.2;
            }
            if (fixWidth < visibleWidth) {
                fixWidth = visibleWidth;
            }
            xPoint = jsBar.getValue();
            setPreferredSize(new Dimension(fixWidth, 0));
            jsBar.setValue((int) (jsBar.getMaximum() * percent));
            revalidate();
            repaint();
        }
    }

    private void scrollToBottom() {
        if (getParent().getParent() instanceof JBScrollPane) {
            JBScrollPane parent = (JBScrollPane) getParent().getParent();
            JScrollBar jsBar = parent.getVerticalScrollBar();
            jsBar.setValue(jsBar.getMaximum());
            revalidate();
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent event) {
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        if (Objects.nonNull(activeBean)) {
            activeBean.moveOut(event.getPoint(), this);
            Tip.getInstance().hidden();
        }
        dataBean.stream().filter(it -> it.getRect().contains(event.getPoint())).forEach(it -> {
            it.moveIn(event.getPoint(), this);
            List<String> stringList = it.getStringList("");
            Tip.getInstance().display(this, event.getPoint(), stringList);
            activeBean = it;
        });
    }
}
