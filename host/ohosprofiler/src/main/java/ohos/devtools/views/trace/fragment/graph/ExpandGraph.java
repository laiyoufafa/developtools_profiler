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
import ohos.devtools.views.trace.util.Utils;

import javax.swing.JComponent;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;

/**
 * dataPanel open
 *
 * @since 2021/04/22 12:25
 */
public class ExpandGraph extends AbstractGraph {
    private boolean isExpand;
    private Image image = ImageUtils.getInstance().getArrowDown();
    private IClickListener clickListener;

    /**
     * class construct
     *
     * @param fragment fragment
     * @param root root
     */
    public ExpandGraph(final AbstractDataFragment fragment, final JComponent root) {
        this.fragment = fragment;
        this.root = root;
    }

    /**
     * Gets the value of image .
     *
     * @return the value of java.awt.Image
     */
    public Image getImage() {
        return image;
    }

    /**
     * Sets the image .
     * <p>You can use getImage() to get the value of image</p>
     *
     * @param image image
     */
    public void setImage(final Image image) {
        this.image = image;
    }

    /**
     * Gets the value of isExpand .
     *
     * @return the value of boolean
     */
    public boolean isExpand() {
        return isExpand;
    }

    /**
     * Sets the isExpand .
     * <p>You can use getExpand() to get the value of isExpand</p>
     *
     * @param expand expand
     */
    public void setExpand(final boolean expand) {
        this.isExpand = expand;
    }

    /**
     * draw by graphics
     *
     * @param graphics graphics
     */
    @Override
    public void draw(final Graphics2D graphics) {
        if (isExpand) {
            image = ImageUtils.getInstance().getArrowUpFocus();
        } else {
            if (flagFocus) {
                image = ImageUtils.getInstance().getArrowDownFocus();
            } else {
                image = ImageUtils.getInstance().getArrowDown();
            }
        }
        graphics.drawImage(image, Utils.getX(rect), Utils.getY(rect), 12, 12, null);
    }

    /**
     * when view onFocus
     *
     * @param event event
     */
    @Override
    public void onFocus(final MouseEvent event) {
        if (isExpand) {
            image = ImageUtils.getInstance().getArrowUpFocus();
        } else {
            image = ImageUtils.getInstance().getArrowDownFocus();
        }
        repaint();
    }

    /**
     * when view onBlur
     *
     * @param event event
     */
    @Override
    public void onBlur(final MouseEvent event) {
        if (isExpand) {
            image = ImageUtils.getInstance().getArrowUp();
        } else {
            image = ImageUtils.getInstance().getArrowDown();
        }
        repaint();
    }

    /**
     * when view onClick
     *
     * @param event event
     */
    @Override
    public void onClick(final MouseEvent event) {
        if (clickListener != null) {
            clickListener.click(event);
        }
    }

    /**
     * when view onMouseMove
     *
     * @param event event
     */
    @Override
    public void onMouseMove(final MouseEvent event) {
    }

    /**
     * setOnClickListener
     *
     * @param listener listener
     */
    public void setOnClickListener(final IClickListener listener) {
        clickListener = listener;
    }

    /**
     * click listener class
     */
    public interface IClickListener {
        /**
         * click event
         *
         * @param event event
         */
        void click(MouseEvent event);
    }
}
