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
import ohos.devtools.views.trace.util.Final;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
import java.util.Map;

/**
 * test LeftFragment class .
 *
 * @version 1.0
 * @date 2021/4/24 17:54
 **/
class LeftFragmentTest {
    /**
     * test function the draw .
     */
    @Test
    void draw() {
        LeftFragment fragment = new LeftFragment(new JPanel());
        BufferedImage image = UIUtil.createImage(new JPanel(), 1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        fragment.draw(graphics2D);
        Assertions.assertNotNull(fragment);
    }

    /**
     * test function the getRect .
     */
    @Test
    void getRect() {
        LeftFragment fragment = new LeftFragment(new JPanel());
        fragment.setRect(new Rectangle(0, 0, 100, 100));
        Assertions.assertEquals(100, fragment.getRect().width);
    }

    /**
     * test function the setRect .
     */
    @Test
    void setRect() {
        LeftFragment fragment = new LeftFragment(new JPanel());
        fragment.setRect(new Rectangle(0, 0, 100, 100));
        Assertions.assertEquals(100, fragment.getRect().width);
    }

    /**
     * test function the getDescRect .
     */
    @Test
    void getDescRect() {
        LeftFragment fragment = new LeftFragment(new JPanel());
        fragment.setDescRect(new Rectangle(0, 0, 100, 100));
        Assertions.assertEquals(100, fragment.getDescRect().width);
    }

    /**
     * test function the setDescRect .
     */
    @Test
    void setDescRect() {
        LeftFragment fragment = new LeftFragment(new JPanel());
        fragment.setDescRect(new Rectangle(0, 0, 100, 100));
        Assertions.assertEquals(100, fragment.getDescRect().width);
    }

    /**
     * test function the getDataRect .
     */
    @Test
    void getDataRect() {
        LeftFragment fragment = new LeftFragment(new JPanel());
        fragment.setDataRect(new Rectangle(0, 0, 100, 100));
        Assertions.assertEquals(100, fragment.getDataRect().width);
    }

    /**
     * test function the setDataRect .
     */
    @Test
    void setDataRect() {
        LeftFragment fragment = new LeftFragment(new JPanel());
        fragment.setDataRect(new Rectangle(0, 0, 100, 100));
        Assertions.assertEquals(100, fragment.getDataRect().width);
    }

    /**
     * test function the getLineColor .
     */
    @Test
    void getLineColor() {
        LeftFragment fragment = new LeftFragment(new JPanel());
        fragment.getLineColor();
        Assertions.assertEquals(200, fragment.getRect().width);
    }

    /**
     * test function the getTextColor .
     */
    @Test
    void getTextColor() {
        LeftFragment fragment = new LeftFragment(new JPanel());
        fragment.getTextColor();
        Assertions.assertEquals(200, fragment.getRect().width);
    }

    /**
     * test function the getStartTimeS .
     */
    @Test
    void getStartTimeS() {
        LeftFragment fragment = new LeftFragment(new JPanel());
        fragment.setStartTimeS("StartTimeS");
        Assertions.assertEquals("StartTimeS", fragment.getStartTimeS());
    }

    /**
     * test function the setStartTimeS .
     */
    @Test
    void setStartTimeS() {
        LeftFragment fragment = new LeftFragment(new JPanel());
        fragment.setStartTimeS("StartTimeS");
        Assertions.assertEquals("StartTimeS", fragment.getStartTimeS());
    }

    /**
     * test function the setStartNS .
     */
    @Test
    void setStartNS() {
        LeftFragment fragment = new LeftFragment(new JPanel());
        fragment.setStartNS(100L);
        Assertions.assertEquals(100L, fragment.getStartNS());
    }

    /**
     * test function the getStartNS .
     */
    @Test
    void getStartNS() {
        LeftFragment fragment = new LeftFragment(new JPanel());
        fragment.setStartNS(100L);
        Assertions.assertEquals(100L, fragment.getStartNS());
    }

    /**
     * test function the getExtendHeight .
     */
    @Test
    void getExtendHeight() {
        LeftFragment fragment = new LeftFragment(new JPanel());
        fragment.setExtendHeight(100);
        Assertions.assertEquals(100, fragment.getExtendHeight());
    }

    /**
     * test function the setExtendHeight .
     */
    @Test
    void setExtendHeight() {
        LeftFragment fragment = new LeftFragment(new JPanel());
        fragment.setExtendHeight(100);
        Assertions.assertEquals(100, fragment.getExtendHeight());
    }

    /**
     * test function the getLogString .
     */
    @Test
    void getLogString() {
        LeftFragment fragment = new LeftFragment(new JPanel());
        fragment.setLogString("logString");
        Assertions.assertEquals("logString", fragment.getLogString());
    }

    /**
     * test function the setLogString .
     */
    @Test
    void setLogString() {
        LeftFragment fragment = new LeftFragment(new JPanel());
        fragment.setLogString("logString");
        Assertions.assertEquals("logString", fragment.getLogString());
    }

}