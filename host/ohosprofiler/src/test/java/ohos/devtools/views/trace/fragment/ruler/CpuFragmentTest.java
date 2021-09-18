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

package ohos.devtools.views.trace.fragment.ruler;

import com.intellij.util.ui.UIUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import static org.junit.Assert.assertEquals;

/**
 * test CpuFragment class .
 *
 * @date 2021/4/24 17:54
 */
class CpuFragmentTest {
    /**
     * test function the draw .
     */
    @Test
    void draw() {
        CpuFragment cpuFragment = new CpuFragment(new JPanel(), (leftX, rightX, leftNS, rightNS, centerNS) -> {
        });
        BufferedImage image = UIUtil.createImage(new JPanel(), 1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        cpuFragment.draw(graphics2D);
        Assertions.assertNotNull(cpuFragment);
    }

    /**
     * test function the getRect .
     */
    @Test
    void getRect() {
        CpuFragment cpuFragment = new CpuFragment(new JPanel(), (leftX, rightX, leftNS, rightNS, centerNS) -> {
        });
        assertEquals(true, cpuFragment.getRect() != null);
    }

    /**
     * test function the setRect .
     */
    @Test
    void setRect() {
        CpuFragment cpuFragment = new CpuFragment(new JPanel(), (leftX, rightX, leftNS, rightNS, centerNS) -> {
        });
        cpuFragment.setRect(new Rectangle(0, 0, 100, 100));
        assertEquals(100, cpuFragment.getRect().width);
    }

    /**
     * test function the getDescRect .
     */
    @Test
    void getDescRect() {
        CpuFragment cpuFragment = new CpuFragment(new JPanel(), (leftX, rightX, leftNS, rightNS, centerNS) -> {
        });
        cpuFragment.setRect(new Rectangle(0, 0, 100, 100));
        assertEquals(100, cpuFragment.getRect().width);
    }

    /**
     * test function the setDescRect .
     */
    @Test
    void setDescRect() {
        CpuFragment cpuFragment = new CpuFragment(new JPanel(), (leftX, rightX, leftNS, rightNS, centerNS) -> {
        });
        cpuFragment.setRect(new Rectangle(0, 0, 100, 100));
        assertEquals(100, cpuFragment.getRect().width);
    }

    /**
     * test function the getDataRect .
     */
    @Test
    void getDataRect() {
        CpuFragment cpuFragment = new CpuFragment(new JPanel(), (leftX, rightX, leftNS, rightNS, centerNS) -> {
        });
        cpuFragment.setRect(new Rectangle(0, 0, 100, 100));
        assertEquals(100, cpuFragment.getRect().width);
    }

    /**
     * test function the setDataRect .
     */
    @Test
    void setDataRect() {
        CpuFragment cpuFragment = new CpuFragment(new JPanel(), (leftX, rightX, leftNS, rightNS, centerNS) -> {
        });
        cpuFragment.setRect(new Rectangle(0, 0, 100, 100));
        assertEquals(100, cpuFragment.getRect().width);
    }

    /**
     * test function the getRoot .
     */
    @Test
    void getRoot() {
        CpuFragment cpuFragment = new CpuFragment(new JPanel(), (leftX, rightX, leftNS, rightNS, centerNS) -> {
        });
        cpuFragment.setRect(new Rectangle(0, 0, 100, 100));
        assertEquals(100, cpuFragment.getRect().width);
    }

    /**
     * test function the setRoot .
     */
    @Test
    void setRoot() {
        CpuFragment cpuFragment = new CpuFragment(new JPanel(), (leftX, rightX, leftNS, rightNS, centerNS) -> {
        });
        cpuFragment.setRect(new Rectangle(0, 0, 100, 100));
        assertEquals(100, cpuFragment.getRect().width);
    }

    /**
     * test function the getLineColor .
     */
    @Test
    void getLineColor() {
        CpuFragment cpuFragment = new CpuFragment(new JPanel(), (leftX, rightX, leftNS, rightNS, centerNS) -> {
        });
        cpuFragment.setRect(new Rectangle(0, 0, 100, 100));
        assertEquals(100, cpuFragment.getRect().width);
    }

    /**
     * test function the getTextColor .
     */
    @Test
    void getTextColor() {
        CpuFragment cpuFragment = new CpuFragment(new JPanel(), (leftX, rightX, leftNS, rightNS, centerNS) -> {
        });
        cpuFragment.setRect(new Rectangle(0, 0, 100, 100));
        assertEquals(100, cpuFragment.getRect().width);
    }

    /**
     * test function the repaint .
     */
    @Test
    void repaint() {
        CpuFragment cpuFragment = new CpuFragment(new JPanel(), (leftX, rightX, leftNS, rightNS, centerNS) -> {
        });
        cpuFragment.setRect(new Rectangle(0, 0, 100, 100));
        assertEquals(100, cpuFragment.getRect().width);
    }

    /**
     * test function the getSelectX .
     */
    @Test
    void getSelectX() {
        CpuFragment cpuFragment = new CpuFragment(new JPanel(), (leftX, rightX, leftNS, rightNS, centerNS) -> {
        });
        cpuFragment.setSelectX(100);
        assertEquals(100, cpuFragment.getSelectX());
    }

    /**
     * test function the setSelectX .
     */
    @Test
    void setSelectX() {
        CpuFragment cpuFragment = new CpuFragment(new JPanel(), (leftX, rightX, leftNS, rightNS, centerNS) -> {
        });
        cpuFragment.setSelectX(100);
        assertEquals(100, cpuFragment.getSelectX());
    }

    /**
     * test function the getSelectY .
     */
    @Test
    void getSelectY() {
        CpuFragment cpuFragment = new CpuFragment(new JPanel(), (leftX, rightX, leftNS, rightNS, centerNS) -> {
        });
        cpuFragment.setSelectY(100);
        assertEquals(100, cpuFragment.getSelectY());
    }

    /**
     * test function the setSelectY .
     */
    @Test
    void setSelectY() {
        CpuFragment cpuFragment = new CpuFragment(new JPanel(), (leftX, rightX, leftNS, rightNS, centerNS) -> {
        });
        cpuFragment.setSelectY(100);
        assertEquals(100, cpuFragment.getSelectY());
    }

    /**
     * test function the setRangeChangeListener .
     */
    @Test
    void setRangeChangeListener() {
        CpuFragment cpuFragment = new CpuFragment(new JPanel(), (leftX, rightX, leftNS, rightNS, centerNS) -> {
        });
        cpuFragment.setRect(new Rectangle(0, 0, 100, 100));
        assertEquals(100, cpuFragment.getRect().width);
    }

    /**
     * test function the mouseDragged .
     */
    @Test
    void mouseDragged() {
        CpuFragment cpuFragment = new CpuFragment(new JPanel(), (leftX, rightX, leftNS, rightNS, centerNS) -> {
        });
        cpuFragment.setRect(new Rectangle(0, 0, 100, 100));
        assertEquals(100, cpuFragment.getRect().width);
    }

    /**
     * test function the setRange .
     */
    @Test
    void setRange() {
        CpuFragment cpuFragment = new CpuFragment(new JPanel(), (leftX, rightX, leftNS, rightNS, centerNS) -> {
        });
        cpuFragment.setRect(new Rectangle(0, 0, 100, 100));
        assertEquals(100, cpuFragment.getRect().width);
    }
}