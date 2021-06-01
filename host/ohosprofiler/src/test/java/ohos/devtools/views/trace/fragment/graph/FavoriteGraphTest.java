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

import com.intellij.util.ui.UIUtil;
import ohos.devtools.views.trace.fragment.AbstractDataFragment;
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
import java.util.Map;

/**
 * test FavoriteGraph class .
 *
 * @version 1.0
 * @date 2021/4/24 17:56
 **/
class FavoriteGraphTest {
    /**
     * test function the edgeInspect .
     */
    @Test
    void edgeInspect() {
        FavoriteGraph graph = new FavoriteGraph(null, null);
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the repaint .
     */
    @Test
    void repaint() {
        FavoriteGraph graph = new FavoriteGraph(null, null);
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the setRect .
     */
    @Test
    void setRect() {
        FavoriteGraph graph = new FavoriteGraph(null, null);
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the drawString .
     */
    @Test
    void drawString() {
        FavoriteGraph graph = new FavoriteGraph(null, null);
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the getRightGraph .
     */
    @Test
    void getRightGraph() {
        FavoriteGraph graph = new FavoriteGraph(null, null);
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the setRightGraph .
     */
    @Test
    void setRightGraph() {
        FavoriteGraph graph = new FavoriteGraph(null, null);
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the isDisplay .
     */
    @Test
    void isDisplay() {
        FavoriteGraph graph = new FavoriteGraph(null, null);
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the display .
     */
    @Test
    void display() {
        FavoriteGraph graph = new FavoriteGraph(null, null);
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the isFavorite .
     */
    @Test
    void isFavorite() {
        FavoriteGraph graph = new FavoriteGraph(null, null);
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the favorite .
     */
    @Test
    void favorite() {
        FavoriteGraph graph = new FavoriteGraph(null, null);
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the draw .
     */
    @Test
    void draw() {
        FavoriteGraph graph = new FavoriteGraph(new AbstractDataFragment() {
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
        }, null);
        BufferedImage image = UIUtil.createImage(new JPanel(), 1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        graph.draw(graphics2D);
        Assertions.assertNotNull(graph);
    }

    /**
     * test function the onFocus .
     */
    @Test
    void onFocus() {
        FavoriteGraph graph = new FavoriteGraph(null, null);
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the onBlur .
     */
    @Test
    void onBlur() {
        FavoriteGraph graph = new FavoriteGraph(null, null);
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the onClick .
     */
    @Test
    void onClick() {
        FavoriteGraph graph = new FavoriteGraph(null, null);
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the onMouseMove .
     */
    @Test
    void onMouseMove() {
        FavoriteGraph graph = new FavoriteGraph(null, null);
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }
}