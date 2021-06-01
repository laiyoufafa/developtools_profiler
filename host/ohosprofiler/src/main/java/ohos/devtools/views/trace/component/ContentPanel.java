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

import com.intellij.ui.components.JBPanel;
import ohos.devtools.views.trace.bean.WakeupBean;
import ohos.devtools.views.trace.fragment.AbstractDataFragment;
import ohos.devtools.views.trace.util.Final;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Rolling container
 *
 * @version 1.0.1
 * @date 2021/04/20 12:24
 */
public final class ContentPanel extends JBPanel {
    /**
     * FragmentList to be rendered
     */
    public List<AbstractDataFragment> fragmentList = new ArrayList<>();

    /**
     * Analysis component
     */
    public AnalystPanel analystPanel;
    private WakeupBean wakeupBean;

    /**
     * Constructor
     *
     * @param analystPanel component
     */
    public ContentPanel(AnalystPanel analystPanel) {
        this.analystPanel = analystPanel;
        this.setOpaque(false);
        setFont(Final.NORMAL_FONT);
    }

    /**
     * Sets the wakeupBean .
     * <p>You can use getWakeupBean() to get the value of wakeupBean</p>
     *
     * @param wakeup wakeup
     */
    public void setWakeupBean(WakeupBean wakeup) {
        this.wakeupBean = wakeup;
    }

    /**
     * Gets the value of wakeupBean .
     *
     * @return the value of ohos.devtools.views.trace.bean.WakeupBean
     */
    public WakeupBean getWakeupBean() {
        return wakeupBean;
    }

    /**
     * refresh bottom tab
     */
    public void refreshTab() {
        if (analystPanel != null) {
            if (TabPanel.getMyHeight() != 0) {
                analystPanel.moveToFront(analystPanel.tab);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (graphics instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) graphics;
            g2.setFont(Final.NORMAL_FONT);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (getParent() instanceof TimeViewPort) {
                TimeViewPort parent = (TimeViewPort) getParent();
                Rectangle viewRect = parent.getViewRect();
                // Render the line in the viewport display area, and the line beyond the range will not be rendered
                fragmentList.stream().filter(fragment -> fragment.visible).filter(fragment -> {
                    if (fragment != null && fragment.getRect() != null && viewRect != null) {
                        return fragment.getRect().y + fragment.getRect().height >= viewRect.y + TimeViewPort.height
                            && fragment.getRect().y <= viewRect.y + viewRect.height - TabPanel.getMyHeight();
                    } else {
                        return false;
                    }
                }).forEach(fragment -> {
                    if (fragment != null) {
                        fragment.draw(g2);
                    }
                });
            }
        }
    }

    /**
     * add data line
     *
     * @param fragment data fragment
     */
    public void addDataFragment(AbstractDataFragment fragment) {
        fragmentList.add(fragment);
    }

    /**
     * add data line
     *
     * @param index    line index
     * @param fragment data fragment
     */
    public void addDataFragment(int index, AbstractDataFragment fragment) {
        fragmentList.add(index, fragment);
    }

    /**
     * refresh content data
     */
    public void refresh() {
        List<AbstractDataFragment> fs =
            fragmentList.stream().filter(fragment -> fragment.visible).collect(Collectors.toList());
        int timeViewHeight = TimeViewPort.height;
        for (int index = 0, len = fs.size(); index < len; index++) {
            AbstractDataFragment dataFragment = fs.get(index);
            timeViewHeight += dataFragment.defaultHeight;
            dataFragment.getRect().height = dataFragment.defaultHeight;
            dataFragment.getDescRect().height = dataFragment.defaultHeight;
            dataFragment.getDataRect().height = dataFragment.defaultHeight;
            dataFragment.getRect().y = timeViewHeight - dataFragment.defaultHeight;
            dataFragment.getDescRect().y = timeViewHeight - dataFragment.defaultHeight;
            dataFragment.getDataRect().y = timeViewHeight - dataFragment.defaultHeight;
            dataFragment.getRect().x = 0;
            dataFragment.getDescRect().x = 0;
            dataFragment.getDataRect().x = 200;
        }
        Dimension dim = new Dimension(0, timeViewHeight);
        this.setPreferredSize(dim);
        this.setSize(dim);
        this.setMaximumSize(dim);
        repaint();
    }

    /**
     * time range change will call this
     *
     * @param startNS range start ns
     * @param endNS   range end ns
     */
    public void rangeChange(long startNS, long endNS) {
        fragmentList.forEach(fragment -> fragment.range(startNS, endNS));
    }
}
