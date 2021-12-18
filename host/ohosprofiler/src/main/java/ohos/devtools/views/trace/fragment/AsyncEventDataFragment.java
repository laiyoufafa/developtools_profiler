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

import ohos.devtools.views.trace.bean.AsyncEvent;
import ohos.devtools.views.trace.bean.ThreadData;
import ohos.devtools.views.trace.component.AnalystPanel;
import ohos.devtools.views.trace.util.Final;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JComponent;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Memory data line
 *
 * @since 2021/04/22 12:25
 */
public class AsyncEventDataFragment extends AbstractDataFragment<AsyncEvent> implements AsyncEvent.IEventListener {
    /**
     * graph event callback
     */
    public static ThreadData currentSelectedThreadData;

    /**
     * Process memory
     */
    public AsyncEvent obj;
    private boolean isLoading;
    private Rectangle2D bounds;
    private int max;

    /**
     * structure
     *
     * @param root root
     * @param obj mem
     * @param asyncEvents asyncEvents
     */
    public AsyncEventDataFragment(JComponent root, AsyncEvent obj, List<AsyncEvent> asyncEvents) {
        super(root, true, false);
        this.obj = obj;
        this.setRoot(root);
        this.data = asyncEvents;
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
        String name = obj.getName();
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
            List<AsyncEvent> collect = data.stream().filter(
                memData -> memData.getStartTime() + memData.getDuration() > startNS && memData.getStartTime() < endNS)
                .collect(Collectors.toList());
            int x1;
            int x2;
            for (int index = 0, len = collect.size(); index < len; index++) {
                AsyncEvent asyncEvent = collect.get(index);
                if (asyncEvent.getStartTime() < startNS) {
                    x1 = getX(startNS);
                } else {
                    x1 = getX(asyncEvent.getStartTime());
                }
                if (asyncEvent.getStartTime() + asyncEvent.getDuration() > endNS) {
                    x2 = getX(endNS);
                } else {
                    x2 = getX(asyncEvent.getStartTime() + asyncEvent.getDuration());
                }
                asyncEvent.root = getRoot();
                asyncEvent.setEventListener(this);
                asyncEvent.setRect(x1 + Utils.getX(getDataRect()),
                    Utils.getY(getDataRect()) + asyncEvent.getDepth() * 20 + 10, x2 - x1 <= 0 ? 1 : x2 - x1, 20);
                asyncEvent.draw(graphics);
            }
        } else {
            graphics.setColor(getRoot().getForeground());
            graphics.drawString("Loading...", Utils.getX(getDataRect()), Utils.getY(getDataRect()) + 12);
            loadData();
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
     * key released event
     *
     * @param event event
     */
    @Override
    public void keyReleased(KeyEvent event) {
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
    }

    @Override
    public void mouseReleased(MouseEvent event) {
    }

    private void loadData() {
    }

    @Override
    public void click(MouseEvent event, AsyncEvent data) {
        if (AnalystPanel.getiAsyncFunctionDataClick() != null) {
            AnalystPanel.getiAsyncFunctionDataClick().click(data);
        }
    }

    @Override
    public void blur(MouseEvent event, AsyncEvent data) {

    }

    @Override
    public void focus(MouseEvent event, AsyncEvent data) {

    }

    @Override
    public void mouseMove(MouseEvent event, AsyncEvent data) {

    }
}
