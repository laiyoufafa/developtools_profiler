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

import ohos.devtools.views.trace.bean.FunctionBean;
import ohos.devtools.views.trace.bean.ThreadData;
import ohos.devtools.views.trace.component.AnalystPanel;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JComponent;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * Method call data row
 *
 * @since 2021/04/22 12:25
 */
public class FunctionDataFragment extends AbstractDataFragment<FunctionBean> implements FunctionBean.IEventListener {
    /**
     * graph event callback
     */
    public static FunctionBean currentSelectedFunctionData;

    /**
     * Thread object
     */
    public ThreadData thread;

    /**
     * structure
     *
     * @param contentPanel contentPanel
     * @param functionBeans functionBeans
     */
    public FunctionDataFragment(JComponent contentPanel, ArrayList<FunctionBean> functionBeans) {
        super(contentPanel, true, false);
        this.setRoot(contentPanel);
        this.data = functionBeans;
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
        String name = thread.getThreadName() + " " + thread.getTid();
        Rectangle2D bounds = graphics.getFontMetrics().getStringBounds(name, graphics);
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
                graphics.drawString(substring.substring((int) wordNum), Utils.getX(getDescRect()) + 10,
                    (int) (Utils.getY(getDescRect()) + bounds.getHeight() * 3 + 2));
            }
        }
        if (data != null) {
            data.stream().filter(it -> it.getStartTime() + it.getDuration() > startNS && it.getStartTime() < endNS)
                .forEach(bean -> {
                    int x1;
                    int x2;
                    if (bean.getStartTime() < startNS) {
                        x1 = getX(startNS);
                    } else {
                        x1 = getX(bean.getStartTime());
                    }
                    if (bean.getStartTime() + bean.getDuration() > endNS) {
                        x2 = getX(endNS);
                    } else {
                        x2 = getX(bean.getStartTime() + bean.getDuration());
                    }
                    bean.setRect(x1 + Utils.getX(getDataRect()), Utils.getY(getDataRect()) + 10 + 20 * bean.getDepth(),
                        x2 - x1 <= 0 ? 1 : x2 - x1, 20);
                    bean.root = getRoot();
                    bean.setEventListener(this);
                    bean.draw(graphics);
                });
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
        if (data != null) {
            data.stream()
                .filter(bean -> bean.getStartTime() + bean.getDuration() > startNS && bean.getStartTime() < endNS)
                .filter(bean -> bean.edgeInspect(event)).findFirst().ifPresent(bean -> {
                bean.onClick(event);
            });
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

    /**
     * Mouse entered event
     *
     * @param event event
     */
    @Override
    public void mouseEntered(MouseEvent event) {
    }

    /**
     * Mouse moved event
     *
     * @param evt event
     */
    @Override
    public void mouseMoved(MouseEvent evt) {
        MouseEvent event = getRealMouseEvent(evt);
        super.mouseMoved(event);
        clearFocus(event);
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
     * @param event event
     * @param data data
     */
    @Override
    public void click(MouseEvent event, FunctionBean data) {
        clearSelected();
        data.setSelected(true);
        data.repaint();
        currentSelectedFunctionData = data;
        if (AnalystPanel.iFunctionDataClick != null) {
            AnalystPanel.iFunctionDataClick.click(data);
        }
    }

    /**
     * Loss of focus event
     *
     * @param event event
     * @param data data
     */
    @Override
    public void blur(MouseEvent event, FunctionBean data) {
    }

    /**
     * Get focus event
     *
     * @param event event
     * @param data data
     */
    @Override
    public void focus(MouseEvent event, FunctionBean data) {
    }

    /**
     * Mouse move event
     *
     * @param event event
     * @param data data
     */
    @Override
    public void mouseMove(MouseEvent event, FunctionBean data) {
    }

    // function After loading together, there is no need to redraw drawFrame according to the time interval
    @Override
    public void drawFrame() {
    }
}
