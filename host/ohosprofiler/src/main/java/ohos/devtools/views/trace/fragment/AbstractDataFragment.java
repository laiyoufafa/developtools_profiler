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
import ohos.devtools.views.trace.fragment.ruler.AbstractFragment;

import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.UUID;

/**
 * Draw data rows
 *
 * @param <T> Plot data type
 * @version 1.0
 * @date 2021/04/22 12:25
 **/
public abstract class AbstractDataFragment<T> extends AbstractFragment {
    /**
     * uuid
     */
    public String uuid = UUID.randomUUID().toString();

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
     * Start event
     */
    public long startNS;

    /**
     * End event
     */
    public long endNS;

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
     * @param endNS   End Time
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
    public abstract void mouseClicked(MouseEvent event);

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
    public abstract void mouseMoved(MouseEvent event);

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
        graphics.drawLine(getRect().x, getRect().y + getRect().height, getRoot().getWidth(),
            getRect().y + getRect().height);
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
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
     * Clear selection element
     */
    public void clearSelected() {
        if (CpuDataFragment.currentSelectedCpuData != null) {
            CpuDataFragment.currentSelectedCpuData.select(false);
            CpuDataFragment.currentSelectedCpuData.repaint();
        }
        if (ThreadDataFragment.currentSelectedThreadData != null) {
            ThreadDataFragment.currentSelectedThreadData.select(false);
            ThreadDataFragment.currentSelectedThreadData.repaint();
        }
        if (FunctionDataFragment.currentSelectedFunctionData != null) {
            FunctionDataFragment.currentSelectedFunctionData.setSelected(false);
            FunctionDataFragment.currentSelectedFunctionData.repaint();
        }
    }

    /**
     * Set rect object
     *
     * @param xSize  x coordinate
     * @param ySize  y coordinate
     * @param width  width
     * @param height height
     */
    public void setRect(int xSize, int ySize, int width, int height) {
        getRect().x = xSize;
        getRect().y = ySize;
        getRect().width = width;
        getRect().height = height;
    }
}
