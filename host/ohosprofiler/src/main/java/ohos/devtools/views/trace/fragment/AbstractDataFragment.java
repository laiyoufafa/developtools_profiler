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

import ohos.devtools.views.trace.component.AnalystPanel;
import ohos.devtools.views.trace.fragment.graph.AbstractGraph;
import ohos.devtools.views.trace.fragment.graph.CheckGraph;
import ohos.devtools.views.trace.fragment.graph.FavoriteGraph;
import ohos.devtools.views.trace.fragment.ruler.AbstractFragment;
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.nonNull;

/**
 * Draw data rows
 *
 * @param <T> Plot data type
 * @since 2021/04/22 12:25
 */
public abstract class AbstractDataFragment<T extends AbstractGraph> extends AbstractFragment {
    /**
     * uuid
     */
    public String uuid = UUID.randomUUID().toString();

    /**
     * is alive
     */
    public boolean isAlive;

    /**
     * Parent node uuid
     */
    public String parentUuid = UUID.randomUUID().toString();

    /**
     * The default height can be modified. After hiding, the height of rect descRect dataRect is 0,
     * no rendering, and the display restores the height according to defaultHeight
     */
    public int defaultHeight = 40;

    /**
     * Small font
     */
    public Font smallFont = new Font("宋体", Font.ITALIC, 10);

    /**
     * ndicates whether the data row is selected.
     * null does not display the selected state. true/false displays the sufficient selection box
     */
    public Boolean isSelected = false;

    /**
     * Whether to show
     */
    public boolean visible = true;

    /**
     * Favorite button
     */
    public FavoriteGraph favoriteGraph;

    /**
     * Select button
     */
    public CheckGraph checkGraph;

    /**
     * Start event
     */
    public long startNS;

    /**
     * End event
     */
    public long endNS;

    /**
     * data list
     */
    protected List<T> data;

    private IDataFragment dataFragmentListener;
    private final boolean hasFavorite;
    private final boolean hasCheck;

    /**
     * The construction method
     *
     * @param component component
     * @param hasFavorite hasFavorite
     * @param hasCheck hasCheck
     */
    public AbstractDataFragment(JComponent component, boolean hasFavorite, boolean hasCheck) {
        this.hasFavorite = hasFavorite;
        this.hasCheck = hasCheck;
        favoriteGraph = new FavoriteGraph(this, component, event -> {
            if (nonNull(dataFragmentListener)) {
                dataFragmentListener.collect(this);
            }
        });
        checkGraph = new CheckGraph(this, component);
    }

    /**
     * Set to show or hide
     *
     * @param visible visible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Time range interval change
     *
     * @param startNS Starting time
     * @param endNS End Time
     */
    public void range(long startNS, long endNS) {
        this.startNS = startNS;
        this.endNS = endNS;
    }

    /**
     * Data click event
     *
     * @param event event
     */
    public void mouseClicked(MouseEvent event) {
        if (favoriteGraph.isFavorite()) {
            MouseEvent me = SwingUtilities.convertMouseEvent(getRoot(), event, getRoot().getParent());
            if (favoriteGraph.edgeInspect(me)) {
                favoriteGraph.onClick(me);
            }
            if (checkGraph.edgeInspect(me)) {
                checkGraph.onClick(me);
            }
        } else {
            if (favoriteGraph.edgeInspect(event)) {
                favoriteGraph.onClick(event);
            }
            if (checkGraph.edgeInspect(event)) {
                checkGraph.onClick(event);
            }
        }
    }

    /**
     * Mouse click event
     *
     * @param event event
     */
    public abstract void mousePressed(MouseEvent event);

    /**
     * Mouse exited event
     *
     * @param event event
     */
    public abstract void mouseExited(MouseEvent event);

    /**
     * Mouse entered event
     *
     * @param event event
     */
    public abstract void mouseEntered(MouseEvent event);

    /**
     * Mouse move event
     *
     * @param event event
     */
    public void mouseMoved(MouseEvent event) {
        favoriteGraph.display(edgeInspectRect(getDescRect(), event));
        if (hasFavorite && !visible) {
            if (favoriteGraph.edgeInspect(event)) {
                if (!favoriteGraph.flagFocus) {
                    favoriteGraph.flagFocus = true;
                    favoriteGraph.onFocus(event);
                }
            } else {
                if (favoriteGraph.flagFocus) {
                    favoriteGraph.flagFocus = false;
                    favoriteGraph.onBlur(event);
                }
            }
        }
    }

    /**
     * Mouse release event
     *
     * @param event event
     */
    public abstract void mouseReleased(MouseEvent event);

    /**
     * Drawing method
     *
     * @param graphics graphics
     */
    @Override
    public void draw(Graphics2D graphics) {
        if (endNS == 0) {
            endNS = AnalystPanel.DURATION;
        }
        getRect().width = getRoot().getWidth();
        getDescRect().width = 200;
        getDataRect().width = getRoot().getWidth() - 200;
        graphics.setColor(getRoot().getForeground());
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
        graphics.drawLine(Utils.getX(getRect()), Utils.getY(getRect()) + getRect().height, getRoot().getWidth(),
            Utils.getY(getRect()) + getRect().height);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        if (hasCheck) {
            checkGraph.setChecked(isSelected);
            checkGraph.draw(graphics);
        }
        if (hasFavorite) {
            if (hasCheck) {
                favoriteGraph.setRightGraph(isSelected != null ? checkGraph : null);
            }
            favoriteGraph.draw(graphics);
        }
    }

    /**
     * Calculate the x coordinate based on time
     *
     * @param ns time
     * @return int x coordinate
     */
    public int getX(long ns) {
        if (endNS == 0) {
            endNS = AnalystPanel.DURATION;
        }
        int xSize = (int) ((ns - startNS) * getDataRect().width / (endNS - startNS));
        if (xSize < 0) {
            xSize = 0;
        }
        if (xSize > getDataRect().width) {
            xSize = getDataRect().width;
        }
        return xSize;
    }

    /**
     * Calculate the x coordinate based on time
     *
     * @param ns time
     * @return double Returns the x coordinate
     */
    public double getXDouble(long ns) {
        if (endNS == 0) {
            endNS = AnalystPanel.DURATION;
        }
        double xSize = (ns - startNS) * getDataRect().width / (endNS - startNS);
        if (xSize < 0) {
            xSize = 0;
        }
        if (xSize > getDataRect().width) {
            xSize = getDataRect().width;
        }
        return xSize;
    }

    /**
     * Clear focus
     *
     * @param event Mouse event
     */
    public void clearFocus(MouseEvent event) {
        if (edgeInspect(event)) {
            CpuDataFragment.focusCpuData = null;
        }
    }

    /**
     * The control component is drawn once
     */
    public void drawFrame() {
        if (data != null) {
            data.clear();
            data = null;
            repaint();
        }
    }

    /**
     * Clear selection element
     */
    public void clearSelected() {
        if (nonNull(CpuDataFragment.currentSelectedCpuData)) {
            CpuDataFragment.currentSelectedCpuData.select(false);
            CpuDataFragment.currentSelectedCpuData.repaint();
        }
        if (nonNull(ThreadDataFragment.currentSelectedThreadData)) {
            ThreadDataFragment.currentSelectedThreadData.select(false);
            ThreadDataFragment.currentSelectedThreadData.repaint();
        }
        if (nonNull(FunctionDataFragment.currentSelectedFunctionData)) {
            FunctionDataFragment.currentSelectedFunctionData.setSelected(false);
            FunctionDataFragment.currentSelectedFunctionData.repaint();
        }
    }

    /**
     * Set rect object
     *
     * @param xSize x coordinate
     * @param ySize y coordinate
     * @param width width
     * @param height height
     */
    public void setRect(int xSize, int ySize, int width, int height) {
        getRect().setLocation(xSize, ySize);
        getRect().width = width;
        getRect().height = height;
    }

    /**
     * Gets the value of dataFragmentListener .
     *
     * @return the value of ohos.devtools.views.trace.fragment.AbstractDataFragment.IDataFragment
     */
    public IDataFragment getDataFragmentListener() {
        return dataFragmentListener;
    }

    /**
     * Sets the dataFragmentListener .
     * <p>You can use getDataFragmentListener() to get the value of dataFragmentListener</p>
     *
     * @param listener listener
     */
    public void setDataFragmentListener(IDataFragment listener) {
        this.dataFragmentListener = listener;
    }

    /**
     * recycle the data
     */
    public void recycle() {
        if (data != null) {
            data.clear();
        }
    }

    /**
     * get the real mouse event
     *
     * @param evt MouseEvent
     * @return MouseEvent
     */
    public MouseEvent getRealMouseEvent(MouseEvent evt) {
        MouseEvent event;
        if (favoriteGraph.isFavorite()) {
            event = SwingUtilities.convertMouseEvent(getRoot(), evt, getRoot().getParent());
        } else {
            event = evt;
        }
        return event;
    }

    /**
     * key released
     *
     * @param event event
     */
    public abstract void keyReleased(KeyEvent event);

    /**
     * get the click mouse event is empty
     *
     * @param event MouseEvent
     * @return return the click point is in function or thread
     */
    public boolean isEmptyClick(MouseEvent event) {
        if (Objects.nonNull(data)) {
            return data.stream().allMatch(it -> !it.rect.contains(event.getPoint()));
        }
        return true;
    }

    /**
     * IDataFragment
     *
     * @since 2021/04/22 12:25
     */
    public interface IDataFragment {
        /**
         * collect Callback
         *
         * @param fgr data
         */
        void collect(AbstractDataFragment fgr);

        /**
         * check Callback
         *
         * @param fgr data
         */
        void check(AbstractDataFragment fgr);
    }
}
