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

import com.intellij.util.ui.UIUtil;
import ohos.devtools.views.trace.bean.CpuData;
import ohos.devtools.views.trace.util.Final;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * test CpuDataFragment class .
 *
 * @version 1.0
 * @date 2021/4/24 17:57
 **/
class CpuDataFragmentTest {
    private CpuDataFragment cpuDataFragment;
    private JPanel jPanel;
    private JFrame testFrame;

    /**
     * test function the draw .
     */
    @Test
    void draw() {
        BufferedImage image = UIUtil.createImage(new JPanel(), 1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        cpuDataFragment.draw(graphics2D);
        Assertions.assertNotNull(cpuDataFragment.favoriteGraph);
    }

    /**
     * test function the mouseClicked .
     */
    @Test
    void mouseClicked() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        cpuDataFragment.mouseClicked(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment.favoriteGraph);
    }

    /**
     * test function the mousePressed .
     */
    @Test
    void mousePressed() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        cpuDataFragment.mousePressed(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment.favoriteGraph);
    }

    /**
     * test function the mouseExited .
     */
    @Test
    void mouseExited() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        cpuDataFragment.mouseExited(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment.favoriteGraph);
    }

    /**
     * test function the mouseEntered .
     */
    @Test
    void mouseEntered() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        cpuDataFragment.mouseEntered(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment.favoriteGraph);
    }

    /**
     * test function the mouseMoved .
     */
    @Test
    void mouseMoved() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        cpuDataFragment.mouseMoved(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment.favoriteGraph);
    }

    /**
     * test function the mouseReleased .
     */
    @Test
    void mouseReleased() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        cpuDataFragment.mouseReleased(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment.favoriteGraph);
    }

    /**
     * test function the click .
     */
    @Test
    void click() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        cpuDataFragment.click(mouseEvent, new CpuData());
        Assertions.assertNotNull(cpuDataFragment.favoriteGraph);
    }

    /**
     * test function the blur .
     */
    @Test
    void blur() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        cpuDataFragment.blur(mouseEvent, new CpuData());
        Assertions.assertNotNull(cpuDataFragment.favoriteGraph);
    }

    /**
     * test function the focus .
     */
    @Test
    void focus() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        cpuDataFragment.focus(mouseEvent, new CpuData());
        Assertions.assertNotNull(cpuDataFragment.favoriteGraph);
    }

    /**
     * init the setUp .
     */
    @BeforeEach
    void setUp() {
        if (this.testFrame == null) {
            this.testFrame = new JFrame();
        }
        List<CpuData> list = new ArrayList<>();
        jPanel = new JPanel();
        cpuDataFragment = new CpuDataFragment(jPanel, 1, list);
    }

    /**
     * on the tearDown .
     */
    @AfterEach
    void tearDown() {
        if (this.testFrame != null) {
            this.testFrame.dispose();
            this.testFrame = null;
        }
    }

}