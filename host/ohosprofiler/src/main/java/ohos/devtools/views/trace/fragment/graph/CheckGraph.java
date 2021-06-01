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
 * Data row selection CheckBox
 *
 * @version 1.0
 * @date 2021/04/22 12:25
 **/
public class CheckGraph extends AbstractGraph {
    private Boolean isChecked;
    private Image checkNo = ImageUtils.getInstance().getCheckNo();
    private Image checkYes = ImageUtils.getInstance().getCheckYes();

    /**
     * Gets the value of isChecked .
     *
     * @return the value of java.lang.Boolean
     */
    public Boolean getChecked() {
        return isChecked;
    }

    /**
     * Sets the isChecked .
     * <p>You can use getChecked() to get the value of isChecked</p>
     *
     * @param isChecked isChecked
     */
    public void setChecked(final Boolean isChecked) {
        this.isChecked = isChecked;
    }

    /**
     * structure
     *
     * @param fragment fragment
     * @param root     root
     */
    public CheckGraph(final AbstractDataFragment fragment, final JComponent root) {
        this.fragment = fragment;
        this.root = root;
    }

    /**
     * Drawing method
     *
     * @param graphics graphics
     */
    @Override
    public void draw(final Graphics2D graphics) {
        if (isChecked != null) {
            final int size = 12;
            final int xOffset = 18;
            final int yOffset = 6;
            rect.width = size;
            rect.height = size;
            rect.x = fragment.getDescRect().width - xOffset;
            rect.y = fragment.getRect().y + fragment.getRect().height / 2 - yOffset;
            if (isChecked) {
                graphics.drawImage(checkYes, rect.x, rect.y, rect.width, rect.height, null);
            } else {
                graphics.drawImage(checkNo, rect.x, rect.y, rect.width, rect.height, null);
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
