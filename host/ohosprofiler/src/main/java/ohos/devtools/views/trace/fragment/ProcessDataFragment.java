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

import ohos.devtools.views.trace.bean.CpuFreqData;
import ohos.devtools.views.trace.bean.Process;
import ohos.devtools.views.trace.bean.ProcessData;
import ohos.devtools.views.trace.component.AnalystPanel;
import ohos.devtools.views.trace.component.ContentPanel;
import ohos.devtools.views.trace.fragment.graph.ExpandGraph;
import ohos.devtools.views.trace.util.ColorUtils;
import ohos.devtools.views.trace.util.Db;
import ohos.devtools.views.trace.util.ImageUtils;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * process data
 *
 * @version 1.0
 * @date 2021/04/22 12:25
 **/
public class ProcessDataFragment extends AbstractDataFragment<CpuFreqData> implements ExpandGraph.IClickListener {
    private final ExpandGraph expandGraph;
    private final Process process;
    private int defaultHeight;
    private boolean isLoading;
    private int x1;
    private int x2;
    private Rectangle2D bounds;

    /**
     * constructor
     *
     * @param root    root
     * @param process process
     */
    public ProcessDataFragment(JComponent root, Process process) {
        this.process = process;
        this.setRoot(root);
        expandGraph = new ExpandGraph(this, root);
        expandGraph.setOnClickListener(this);
    }

    /**
     * draw method
     *
     * @param graphics graphics
     */
    @Override
    public void draw(Graphics2D graphics) {
        super.draw(graphics);
        if (AnalystPanel.cpuNum == 0) {
            return;
        }
        drawDefaultState(graphics);

        // left data info
        String name;
        if (process.getName() == null || process.getName().isEmpty()) {
            process.setName("Process");
        }
        name = process.getName() + " " + process.getPid();
        bounds = graphics.getFontMetrics().getStringBounds(name, graphics);
        double wordWidth = bounds.getWidth() / name.length(); // Width per character
        double wordNum = (getDescRect().width - 40) / wordWidth; // How many characters can be displayed on each line
        if (bounds.getWidth() < getDescRect().width - 40) { // Direct line display
            graphics.drawString(name, getDescRect().x + 30, (int) (getDescRect().y + bounds.getHeight() + 10));
        } else {
            String substring = name.substring((int) wordNum);
            if (substring.length() < wordNum) {
                graphics.drawString(name.substring(0, (int) wordNum), getDescRect().x + 30,
                    (int) (getDescRect().y + bounds.getHeight() + 8));
                graphics
                    .drawString(substring, getDescRect().x + 30, (int) (getDescRect().y + bounds.getHeight() * 2 + 8));
            } else {
                graphics.drawString(name.substring(0, (int) wordNum), getDescRect().x + 30,
                    (int) (getDescRect().y + bounds.getHeight() + 2));
                graphics.drawString(substring.substring(0, (int) wordNum), getDescRect().x + 30,
                    (int) (getDescRect().y + bounds.getHeight() * 2 + 2));
                graphics.drawString(substring.substring((int) wordNum), getDescRect().x + 30,
                    (int) (getDescRect().y + bounds.getHeight() * 3 + 2));
            }
        }
        expandGraph.setRect(getDescRect().x + 8, getRect().y + getRect().height / 2 - 6, 12, 12);
        expandGraph.draw(graphics);
    }

    private void drawDefaultState(Graphics graphics) {
        if (!expandGraph.isExpand()) {
            int height = (getRect().height) / AnalystPanel.cpuNum;
            if (processData != null) {
                for (int index = 0; index < processData.size(); index++) {
                    ProcessData data = this.processData.get(index);
                    if (data.getStartTime() < startNS) {
                        x1 = getX(startNS);
                    } else {
                        x1 = getX(data.getStartTime());
                    }
                    if (data.getStartTime() + data.getDuration() > endNS) {
                        x2 = getX(endNS);
                    } else {
                        x2 = getX(data.getStartTime() + data.getDuration());
                    }
                    graphics.setColor(ColorUtils.MD_PALETTE[process.getPid() % ColorUtils.MD_PALETTE.length]);
                    graphics.fillRect(x1 + getDataRect().x, getDataRect().y + height * data.getCpu() + 2,
                        x2 - x1 <= 0 ? 1 : x2 - x1, height - 4);
                }
            } else {
                if (process.getPid() != 0) {
                    graphics.setColor(getRoot().getForeground());
                    graphics.drawString("Loading...", getDataRect().x, getDataRect().y + 12);
                    loadData();
                }
            }
            graphics.setColor(getRoot().getForeground());
        } else {
            graphics.setColor(getRoot().getForeground());
            graphics.fillRect(getRect().x, getRect().y, getRect().width, getRect().height);
            graphics.setColor(getRoot().getBackground());
        }
    }

    /**
     * click handler
     *
     * @param event event
     */
    @Override
    public void mouseClicked(MouseEvent event) {
        if (expandGraph.edgeInspect(event)) {
            expandGraph.onClick(event);
        }
    }

    /**
     * mouse pressed handler
     *
     * @param event event
     */
    @Override
    public void mousePressed(MouseEvent event) {
    }

    /**
     * mouse exited handler
     *
     * @param event event
     */
    @Override
    public void mouseExited(MouseEvent event) {
    }

    /**
     * mouse entered handler
     *
     * @param event event
     */
    @Override
    public void mouseEntered(MouseEvent event) {
    }

    /**
     * mouse moved handler
     *
     * @param event event
     */
    @Override
    public void mouseMoved(MouseEvent event) {
        clearFocus(event);
    }

    /**
     * mouse released handler
     *
     * @param event event
     */
    @Override
    public void mouseReleased(MouseEvent event) {
    }

    /**
     * click handler
     *
     * @param event event
     */
    @Override
    public void click(MouseEvent event) {
        expandGraph.setExpand(!expandGraph.isExpand());
        if (this.getRoot() instanceof ContentPanel) {
            ContentPanel contentPanel = (ContentPanel) this.getRoot();
            if (expandGraph.isExpand()) {
                expandGraph.setImage(ImageUtils.getInstance().getArrowUpFocus());
                for (AbstractDataFragment dataFragment : contentPanel.fragmentList) {
                    if (dataFragment.parentUuid.equals(uuid)) {
                        dataFragment.visible = true;
                    }
                }
            } else {
                expandGraph.setImage(ImageUtils.getInstance().getArrowDownFocus());
                for (AbstractDataFragment dataFragment : contentPanel.fragmentList) {
                    if (dataFragment.parentUuid.equals(uuid)) {
                        dataFragment.visible = false;
                    }
                }
            }
            contentPanel.refresh();
        }
    }

    private List<ProcessData> processData;

    private void loadData() {
        if (!isLoading) {
            isLoading = true;
            ForkJoinPool.commonPool().submit(() -> {
                processData = Db.getInstance().queryProcessData(process.getPid());
                SwingUtilities.invokeLater(() -> {
                    isLoading = false;
                    repaint();
                });
            });
        }
    }
}
