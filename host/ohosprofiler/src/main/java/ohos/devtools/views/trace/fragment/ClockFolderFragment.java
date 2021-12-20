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

import ohos.devtools.views.trace.bean.Clock;
import ohos.devtools.views.trace.bean.ClockData;
import ohos.devtools.views.trace.bean.ProcessData;
import ohos.devtools.views.trace.component.ContentPanel;
import ohos.devtools.views.trace.fragment.graph.ExpandGraph;
import ohos.devtools.views.trace.util.ImageUtils;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

/**
 * process data
 *
 * @since 2021/04/22 12:25
 */
public class ClockFolderFragment extends AbstractDataFragment<ClockData> implements ExpandGraph.IClickListener {
    /**
     * current focus Process Data .
     */
    private final Clock process;
    private final float alpha60 = .6f;
    private final float alpha100 = 1.0f;
    private ExpandGraph expandGraph;
    private boolean isLoading;
    private int x1;
    private int x2;
    private Rectangle2D bounds;
    private ProcessData tipProcessData = null;
    private int tipX; // X position of the message

    /**
     * constructor
     *
     * @param root root
     * @param process process
     */
    public ClockFolderFragment(JComponent root, Clock process) {
        super(root, false, false);
        this.process = process;
        this.setRoot(root);
        expandGraph = new ExpandGraph(this, root);
        expandGraph.setOnClickListener(this);
    }

    /**
     * Gets the value of expandGraph .
     *
     * @return the value of ohos.devtools.views.trace.fragment.graph.ExpandGraph
     */
    public ExpandGraph getExpandGraph() {
        return expandGraph;
    }

    /**
     * Gets the value of process .
     *
     * @return the value of ohos.devtools.views.trace.bean.Process
     */
    public Clock getProcess() {
        return process;
    }

    /**
     * draw method
     *
     * @param graphics graphics
     */
    @Override
    public void draw(Graphics2D graphics) {
        super.draw(graphics);
        drawDefaultState(graphics);

        // left data info
        String name;
        if (process.getName() == null || process.getName().isEmpty()) {
            process.setName("Clocks");
        }
        name = process.getName();
        bounds = graphics.getFontMetrics().getStringBounds(name, graphics);
        double wordWidth = bounds.getWidth() / name.length(); // Width per character
        double wordNum = (getDescRect().width - 40) / wordWidth; // How many characters can be displayed on each line
        if (bounds.getWidth() < getDescRect().width - 40) { // Direct line display
            graphics.drawString(name, Utils.getX(getDescRect()) + 30,
                (int) (Utils.getY(getDescRect()) + bounds.getHeight() + 10));
        } else {
            String substring = name.substring((int) wordNum);
            if (substring.length() < wordNum) {
                graphics.drawString(name.substring(0, (int) wordNum), Utils.getX(getDescRect()) + 30,
                    (int) (Utils.getY(getDescRect()) + bounds.getHeight() + 8));
                graphics.drawString(substring, Utils.getX(getDescRect()) + 30,
                    (int) (Utils.getY(getDescRect()) + bounds.getHeight() * 2 + 8));
            } else {
                graphics.drawString(name.substring(0, (int) wordNum), Utils.getX(getDescRect()) + 30,
                    (int) (Utils.getY(getDescRect()) + bounds.getHeight() + 2));
                graphics.drawString(substring.substring(0, (int) wordNum), Utils.getX(getDescRect()) + 30,
                    (int) (Utils.getY(getDescRect()) + bounds.getHeight() * 2 + 2));
                graphics.drawString(substring.substring((int) wordNum), Utils.getX(getDescRect()) + 30,
                    (int) (Utils.getY(getDescRect()) + bounds.getHeight() * 3 + 2));
            }
        }
        expandGraph.setRect(Utils.getX(getDescRect()) + 8, Utils.getY(getRect()) + getRect().height / 2 - 6, 12, 12);
        expandGraph.draw(graphics);
    }

    private void drawDefaultState(Graphics graphics) {
        if (!expandGraph.isExpand()) {
            graphics.setColor(getRoot().getForeground());
        } else {
            graphics.setColor(getRoot().getForeground());
            graphics.fillRect(Utils.getX(getRect()), Utils.getY(getRect()), getRect().width, getRect().height);
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
        super.mouseClicked(event);
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
     * @param ent event
     */
    @Override
    public void mouseMoved(MouseEvent ent) {
        MouseEvent event = getRealMouseEvent(ent);
        super.mouseMoved(event);
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
     * key released event
     *
     * @param event event
     */
    @Override
    public void keyReleased(KeyEvent event) {
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

    /**
     * expandThreads
     */
    public void expandThreads() {
        expandGraph.setExpand(true);
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
}
