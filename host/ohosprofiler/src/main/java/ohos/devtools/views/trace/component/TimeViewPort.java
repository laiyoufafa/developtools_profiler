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

package ohos.devtools.views.trace.component;

import com.intellij.ui.components.JBViewport;
import ohos.devtools.views.trace.fragment.AbstractDataFragment;
import ohos.devtools.views.trace.fragment.CpuDataFragment;
import ohos.devtools.views.trace.fragment.ruler.CpuFragment;
import ohos.devtools.views.trace.fragment.ruler.LeftFragment;
import ohos.devtools.views.trace.fragment.ruler.RulerFragment;
import ohos.devtools.views.trace.fragment.ruler.TopFragment;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Timeline component
 *
 * @date 2021/04/20 12:24
 */
public final class TimeViewPort extends JBViewport {
    /**
     * Initial height of the time axis
     */
    public static int height = 140;

    /**
     * Fragment above
     */
    public TopFragment topFragment;

    /**
     * Fragment on the left
     */
    public LeftFragment leftFragment;

    /**
     * Cpu fragment
     */
    public CpuFragment cpuFragment;

    /**
     * Rule fragment
     */
    public RulerFragment rulerFragment;

    /**
     * Favorite fragment list
     */
    public List<AbstractDataFragment> favoriteFragments = new CopyOnWriteArrayList<>();

    private final IRangeChangeListener rangeChangeListener;
    private final IHeightChangeListener heightChangeListener;

    /**
     * Constructor。
     *
     * @param heightChangeListener Altitude change monitoring
     * @param rangeChangeListener Altitude change monitoring
     */
    public TimeViewPort(IHeightChangeListener heightChangeListener, IRangeChangeListener rangeChangeListener) {
        this.rangeChangeListener = rangeChangeListener;
        this.heightChangeListener = heightChangeListener;
        this.topFragment = new TopFragment(this);
        this.leftFragment = new LeftFragment(this);
        this.cpuFragment = new CpuFragment(this, (leftX, rightX, leftNS, rightNS, centerNS) -> {
            leftFragment.setStartTime(leftNS);
            rulerFragment.setRange(leftNS, rightNS, centerNS);
        });
        this.rulerFragment = new RulerFragment(this, (startNS, endNS) -> {
            if (rangeChangeListener != null) {
                rangeChangeListener.change(startNS, endNS);
            }
        });
        this.setOpaque(true);
        setFont(Final.NORMAL_FONT);
    }

    /**
     * mouse clicked handler
     *
     * @param event event
     */
    public void mouseClicked(MouseEvent event) {
        for (AbstractDataFragment favoriteFragment : favoriteFragments) {
            favoriteFragment.mouseClicked(event);
        }
        rulerFragment.mouseClicked(event);
    }

    /**
     * set root height
     *
     * @param rootHeight root height
     */
    public void setRootHeight(int rootHeight) {
        rulerFragment.setExtendHeight(rootHeight - height);
        leftFragment.setExtendHeight(rootHeight - height);
    }

    /**
     * collect fragment
     *
     * @param dataFragment fragment
     */
    public void favorite(AbstractDataFragment dataFragment) {
        dataFragment.setVisible(false);
        dataFragment.favoriteGraph.favorite(true);
        Utils.setY(dataFragment.getRect(), height);
        height += dataFragment.getRect().height;
        favoriteFragments.add(dataFragment);
        if (heightChangeListener != null) {
            heightChangeListener.change(height);
            repaint();
        }
    }

    /**
     * cancel collect fragment
     *
     * @param dataFragment fragment
     */
    public void cancel(AbstractDataFragment dataFragment) {
        if (favoriteFragments.contains(dataFragment)) {
            dataFragment.setVisible(true);
            dataFragment.favoriteGraph.favorite(false);
            height -= dataFragment.getRect().height;
            favoriteFragments.remove(dataFragment);
            if (heightChangeListener != null) {
                heightChangeListener.change(height);
                repaint();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
    }

    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);
        if (graphics instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) graphics;
            graphics.setColor(getBackground());
            graphics.fillRect(0, 0, getWidth(), height);
            leftFragment.draw(g2);
            topFragment.draw(g2);
            cpuFragment.draw(g2);
            rulerFragment.draw(g2);
            for (int index = favoriteFragments.size() - 1; index >= 0; index--) {
                AbstractDataFragment frg = favoriteFragments.get(index);
                if (index == favoriteFragments.size() - 1) {
                    Utils.setY(frg.getRect(), height - frg.getRect().height);
                    Utils.setY(frg.getDescRect(), height - frg.getRect().height);
                    Utils.setY(frg.getDataRect(), height - frg.getRect().height);
                } else {
                    Utils.setY(frg.getRect(),
                        Utils.getY(favoriteFragments.get(index + 1).getRect()) - frg.getRect().height);
                    Utils.setY(frg.getDescRect(),
                        Utils.getY(favoriteFragments.get(index + 1).getRect()) - frg.getRect().height);
                    Utils.setY(frg.getDataRect(),
                        Utils.getY(favoriteFragments.get(index + 1).getRect()) - frg.getRect().height);
                }
                frg.draw(g2);
            }
        }
    }

    /**
     * custom mouse event pressed
     *
     * @param event mouse event
     */
    public void mousePressed(final MouseEvent event) {
        Object eventSource = event.getSource();
        if (eventSource instanceof JComponent) {
            JComponent jComponent = (JComponent) eventSource;
            int scrollY = event.getY() + jComponent
                .getY(); // subtract the scroll height of the parent node. After scrolling down, y is a negative number.
            cpuFragment.setSelectX(
                event.getX() < Utils.getX(cpuFragment.getRect()) ? Utils.getX(cpuFragment.getRect()) : event.getX());
            cpuFragment.setSelectY(scrollY);
            rulerFragment.setSelectX(
                event.getX() < Utils.getX(rulerFragment.getRect()) ? Utils.getX(rulerFragment.getRect()) :
                    event.getX());
            rulerFragment.setSelectY(scrollY);
        }
    }

    /**
     * custom mouse event dragged
     *
     * @param event mouse event
     */
    public void mouseDragged(final MouseEvent event) {
        cpuFragment.mouseDragged(event);
    }

    /**
     * custom mouse event moved
     *
     * @param event mouse event
     */
    public void mouseMoved(final MouseEvent event) {
        Object eventSource = event.getSource();
        if (eventSource instanceof JComponent) {
            JComponent jComponent = (JComponent) eventSource;
            int scrollY = event.getY() + jComponent
                .getY(); // Subtract the scroll height of the parent node. After scrolling down, y is a negative number.
            cpuFragment.setSelectX(
                event.getX() < Utils.getX(cpuFragment.getRect()) ? Utils.getX(cpuFragment.getRect()) : event.getX());
            cpuFragment.setSelectY(scrollY);
            rulerFragment.setSelectX(
                event.getX() < Utils.getX(rulerFragment.getRect()) ? Utils.getX(rulerFragment.getRect()) :
                    event.getX());
            rulerFragment.setSelectY(scrollY);
            CpuDataFragment.focusCpuData = null;
            rulerFragment.mouseMoved(event);
            favoriteFragments.forEach(fragment -> fragment.mouseMoved(event));
        }
    }

    /**
     * recycle all fragments
     */
    public void recycle() {
        if (favoriteFragments != null) {
            favoriteFragments.forEach(AbstractDataFragment::recycle);
            favoriteFragments.clear();
        }
    }

    /**
     * Timeline interval change listener
     */
    @FunctionalInterface
    public interface IRangeChangeListener {
        /**
         * change callback
         *
         * @param startNS start time
         * @param endNS end time
         */
        void change(long startNS, long endNS);
    }

    /**
     * Height change listener
     */
    @FunctionalInterface
    public interface IHeightChangeListener {
        /**
         * change callback
         *
         * @param height height change
         */
        void change(int height);
    }
}
