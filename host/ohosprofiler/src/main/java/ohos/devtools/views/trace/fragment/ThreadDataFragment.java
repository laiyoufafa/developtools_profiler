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
import ohos.devtools.views.trace.bean.FunctionBean;
import ohos.devtools.views.trace.bean.ThreadData;
import ohos.devtools.views.trace.component.AnalystPanel;
import ohos.devtools.views.trace.component.ContentPanel;
import ohos.devtools.views.trace.util.Db;
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

/**
 * Thread data row
 *
 * @version 1.0
 * @date 2021/04/22 12:25
 */
public class ThreadDataFragment extends AbstractDataFragment<ThreadData> implements ThreadData.IEventListener {
    /**
     * graph event callback
     */
    public static ThreadData currentSelectedThreadData;

    /**
     * Thread object
     */
    public ThreadData thread;

    /**
     * delayClickStartTime
     */
    public Long delayClickStartTime;

    private int x1;
    private int x2;
    private Rectangle2D bounds;
    private boolean isLoading;

    /**
     * structure
     *
     * @param root root
     * @param thread thread
     */
    public ThreadDataFragment(JComponent root, ThreadData thread) {
        super(root, true, false);
        this.thread = thread;
        this.setRoot(root);
    }

    /**
     * getData
     *
     * @return List<ThreadData>
     */
    public List<ThreadData> getData() {
        return this.data;
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
        bounds = graphics.getFontMetrics().getStringBounds(name, graphics);
        double wordWidth = bounds.getWidth() / name.length(); // Width per character
        double wordNum = (getDescRect().getWidth() - 40) / wordWidth;
        if (bounds.getWidth() < getDescRect().getWidth() - 40) { // Direct line display
            graphics.drawString(name, (int) (getDescRect().getX() + 10),
                (int) (getDescRect().getY() + bounds.getHeight() + 10));
        } else {
            String substring = name.substring((int) wordNum);
            if (substring.length() < wordNum) {
                graphics.drawString(name.substring(0, (int) wordNum), (int) (getDescRect().getX() + 10),
                    (int) (getDescRect().getY() + bounds.getHeight() + 8));
                graphics.drawString(substring, Utils.getX(getDescRect()) + 10,
                    (int) (getDescRect().getY() + bounds.getHeight() * 2 + 8));
            } else {
                graphics.drawString(name.substring(0, (int) wordNum), (int) (getDescRect().getX() + 10),
                    (int) (getDescRect().getY() + bounds.getHeight() + 2));
                graphics.drawString(substring.substring(0, (int) wordNum), (int) (getDescRect().getX() + 10),
                    (int) (getDescRect().getY() + bounds.getHeight() * 2 + 2));
                graphics.drawString(substring.substring((int) wordNum), (int) (getDescRect().getX() + 10),
                    (int) (getDescRect().getY() + bounds.getHeight() * 3 + 2));
            }
        }
        drawData(graphics);
    }

    private void drawData(Graphics2D graphics) {
        if (data != null) {
            data.stream().filter(threadData -> threadData.getStartTime() + threadData.getDuration() > startNS
                && threadData.getStartTime() < endNS).forEach(threadData -> {
                if (threadData.getStartTime() < startNS) {
                    x1 = getX(startNS);
                } else {
                    x1 = getX(threadData.getStartTime());
                }
                if (threadData.getStartTime() + threadData.getDuration() > endNS) {
                    x2 = getX(endNS);
                } else {
                    x2 = getX(threadData.getStartTime() + threadData.getDuration());
                }
                threadData.setRect(x1 + Utils.getX(getDataRect()), Utils.getY(getDataRect()) + 5,
                    x2 - x1 <= 0 ? 1 : x2 - x1,
                    getDataRect().height - 10);
                threadData.root = getRoot();
                threadData.setEventListener(this);
                threadData.draw(graphics);
            });
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
            data.stream().filter(threadData -> threadData.getStartTime() + threadData.getDuration() > startNS
                && threadData.getStartTime() < endNS).filter(threadData -> threadData.edgeInspect(event)).findFirst()
                .ifPresent(threadData -> {
                    threadData.setProcessName(thread.getProcessName());
                    threadData.setThreadName(thread.getThreadName());
                    threadData.onClick(event);
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
        if (edgeInspect(event)) {
            if (data != null) {
                data.stream().filter(threadData -> threadData.getStartTime() + threadData.getDuration() > startNS
                        && threadData.getStartTime() < endNS).filter(threadData -> threadData.edgeInspect(event))
                    .findFirst().ifPresent(filter -> {
                        filter.onMouseMove(event);
                        if (filter.edgeInspect(event)) {
                            if (!filter.flagFocus) {
                                filter.flagFocus = true;
                                filter.onFocus(event);
                            }
                        } else {
                            if (filter.flagFocus) {
                                filter.flagFocus = false;
                                filter.onBlur(event);
                            }
                        }
                    });
            }
        }
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

    private void loadData() {
        if (!isLoading) {
            isLoading = true;
            CompletableFuture.runAsync(() -> {
                List<ThreadData> list = new ArrayList<>() {
                };
                Db.getInstance().query(Sql.SYS_QUERY_THREAD_DATA, list, thread.getTid());
                data = list;
                ArrayList<FunctionBean> functionBeans = new ArrayList<>() {
                };
                Db.getInstance().query(Sql.SYS_GET_FUN_DATA_BY_TID, functionBeans, thread.getTid());
                SwingUtilities.invokeLater(() -> {
                    isLoading = false;
                    if (!functionBeans.isEmpty()) {
                        int maxHeight =
                            (functionBeans.stream().mapToInt(bean -> bean.getDepth()).max().getAsInt() + 1) * 20;
                        FunctionDataFragment functionDataFragment =
                            new FunctionDataFragment(this.getRoot(), functionBeans);
                        functionDataFragment.parentUuid = this.parentUuid;
                        functionDataFragment.thread = this.thread;
                        functionDataFragment.defaultHeight = maxHeight + 20;
                        functionDataFragment.visible = true;
                        if (this.getRoot() instanceof ContentPanel) {
                            ContentPanel contentPanel = (ContentPanel) this.getRoot();
                            int index = contentPanel.fragmentList.indexOf(this) + 1;
                            contentPanel.addDataFragment(index, functionDataFragment);
                            contentPanel.refresh();
                        }
                    } else {
                        repaint();
                    }
                    if (delayClickStartTime != null) {
                        data.stream().filter(it -> it.getStartTime() == delayClickStartTime).findFirst()
                            .ifPresent(it -> {
                                click(null, it);
                                delayClickStartTime = null;
                            });
                    }
                });
            }, Utils.getPool()).whenComplete((unused, throwable) -> {
                if (Objects.nonNull(throwable)) {
                    throwable.printStackTrace();
                }
            });
        }
    }

    /**
     * click event
     *
     * @param event event
     * @param data data
     */
    @Override
    public void click(MouseEvent event, ThreadData data) {
        clearSelected();
        data.select(true);
        data.repaint();
        currentSelectedThreadData = data;
        if (AnalystPanel.iThreadDataClick != null) {
            AnalystPanel.iThreadDataClick.click(data);
        }
    }

    /**
     * Mouse blur event
     *
     * @param event event
     * @param data data
     */
    @Override
    public void blur(MouseEvent event, ThreadData data) {
    }

    /**
     * Mouse focus event
     *
     * @param event event
     * @param data data
     */
    @Override
    public void focus(MouseEvent event, ThreadData data) {
    }

    /**
     * Mouse move event
     *
     * @param event event
     * @param data data
     */
    @Override
    public void mouseMove(MouseEvent event, ThreadData data) {
        getRoot().repaint();
    }
}
