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

import ohos.devtools.views.trace.util.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * test AbstractDataFragment class .
 *
 * @version 1.0
 * @date 2021/4/24 18:04
 */
class AbstractDataFragmentTest {
    private AbstractDataFragment abstractDataFragment;

    /**
     * init the setUp .
     */
    @BeforeEach
    void setUp() {
        abstractDataFragment = new AbstractDataFragment(null, false, false) {
            @Override
            public void mouseClicked(MouseEvent event) {
            }

            @Override
            public void mousePressed(MouseEvent event) {
            }

            @Override
            public void mouseExited(MouseEvent event) {
            }

            @Override
            public void mouseEntered(MouseEvent event) {
            }

            @Override
            public void mouseMoved(MouseEvent event) {
            }

            @Override
            public void mouseReleased(MouseEvent event) {
            }

            @Override
            public void keyReleased(KeyEvent event) {
            }
        };
    }

    /**
     * test set the Visible .
     */
    @Test
    void setVisible() {
        boolean currentVis = true;
        abstractDataFragment.setVisible(currentVis);
        assertEquals(abstractDataFragment.visible, currentVis);
    }

    /**
     * test the range function .
     */
    @Test
    void range() {
        long startNs = 1L;
        long endNs = 5L;
        abstractDataFragment.range(startNs, endNs);
        assertEquals(startNs, abstractDataFragment.startNS);
        assertEquals(endNs, abstractDataFragment.endNS);
    }

    /**
     * test the getX function .
     */
    @Test
    void getX() {
        abstractDataFragment.range(100L, 1000000L);
        Rectangle rectangle = new Rectangle();
        rectangle.width = 1000;
        abstractDataFragment.setDataRect(rectangle);
        assertEquals(9, abstractDataFragment.getX(10000L));
    }

    /**
     * test the getXDouble function .
     */
    @Test
    void getXDouble() {
        abstractDataFragment.range(100L, 1000000L);
        Rectangle rectangle = new Rectangle();
        rectangle.width = 1000;
        abstractDataFragment.setDataRect(rectangle);
        assertEquals(9, abstractDataFragment.getXDouble(10000L));
    }

    /**
     * test set the clearSelected .
     */
    @Test
    void clearSelected() {
        abstractDataFragment.clearSelected();
        assertNotEquals(abstractDataFragment, null);
    }

    /**
     * test set the setRect .
     */
    @Test
    void setRect() {
        Rectangle rectangle = new Rectangle();
        rectangle.width = 1000;
        rectangle.height = 100;
        Utils.setX(rectangle, 10);
        Utils.setY(rectangle, 10);
        abstractDataFragment.setRect(rectangle);
        abstractDataFragment.setRect(100, 100, 100, 100);
        assertEquals(100, Utils.getX(abstractDataFragment.getRect()));
        assertEquals(100, Utils.getY(abstractDataFragment.getRect()));
        assertEquals(100, abstractDataFragment.getRect().height);
        assertEquals(100, abstractDataFragment.getRect().width);
    }
}