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

package ohos.devtools.views.trace.fragment.graph;

import ohos.devtools.views.trace.fragment.AbstractDataFragment;
import ohos.devtools.views.trace.util.ImageUtils;

import javax.swing.JComponent;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;

/**
 * Data row collection button
 *
 * @version 1.0
 * @date 2021/04/22 12:25
 **/
public class FavoriteGraph extends AbstractGraph {
    private boolean isFavorite;
    private boolean isDisplay;
    private Image img = ImageUtils.getInstance().getStar();
    private Image imgFill = ImageUtils.getInstance().getStarFill();
    private AbstractGraph rightGraph;

    /**
     * Gets the value of rightGraph .
     *
     * @return the value of ohos.devtools.views.trace.fragment.graph.Graph
     */
    public AbstractGraph getRightGraph() {
        return rightGraph;
    }

    /**
     * Sets the rightGraph .
     * <p>You can use getRightGraph() to get the value of rightGraph</p>
     *
     * @param rightGraph rightGraph
     */
    public void setRightGraph(final AbstractGraph rightGraph) {
        this.rightGraph = rightGraph;
    }

    /**
     * structure
     *
     * @param fragment fragment fragment
     * @param root root root
     */
    public FavoriteGraph(final AbstractDataFragment fragment, final JComponent root) {
        this.fragment = fragment;
        this.root = root;
    }

    /**
     * Determine whether to display
     *
     * @return boolean boolean
     */
    public boolean isDisplay() {
        return isDisplay;
    }

    /**
     * Show favorites
     *
     * @param show show
     */
    public void display(final boolean show) {
        isDisplay = show;
        if (root != null) {
            root.repaint(rect);
        }
    }

    /**
     * Determine whether to save
     *
     * @return boolean boolean
     */
    public boolean isFavorite() {
        return isFavorite;
    }

    /**
     * favorite
     *
     * @param favorite favorite
     */
    public void favorite(final boolean favorite) {
        isFavorite = favorite;
    }

    /**
     * Drawing method
     *
     * @param graphics graphics
     */
    @Override
    public void draw(final Graphics2D graphics) {
        final int size = 12;
        final int xOffset = 18;
        final int yOffset = 6;
        final int padding = 10;
        rect.width = size;
        rect.height = size;
        rect.y = fragment.getRect().y + fragment.getRect().height / 2 - yOffset;
        if (rightGraph == null) {
            rect.x = fragment.getDescRect().width - xOffset;
        } else {
            rect.x = fragment.getDescRect().width - xOffset - size - padding;
        }
        if (isFavorite) {
            graphics.drawImage(ImageUtils.getInstance().getStarFill(), rect.x, rect.y, rect.width, rect.height, null);
        } else {
            if (isDisplay) {
                graphics.drawImage(ImageUtils.getInstance().getStar(), rect.x, rect.y, rect.width, rect.height, null);
            }
        }
    }

    /**
     * Get focus event
     *
     * @param event event
     */
    @Override
    public void onFocus(final MouseEvent event) {
    }

    /**
     * Loss of focus event
     *
     * @param event event
     */
    @Override
    public void onBlur(final MouseEvent event) {
    }

    /**
     * Click event
     *
     * @param event event
     */
    @Override
    public void onClick(final MouseEvent event) {
    }

    /**
     * Mouse movement event
     *
     * @param event event
     */
    @Override
    public void onMouseMove(final MouseEvent event) {
    }
}
