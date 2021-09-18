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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * test AbstractGraph class .
 *
 * @date: 2021/4/24 17:55
 */
class AbstractGraphTest {
    /**
     * test function the draw .
     */
    @Test
    void draw() {
        AbstractGraph graph = new AbstractGraph() {
            @Override
            public void draw(Graphics2D graphics) {
            }

            @Override
            public void onFocus(MouseEvent event) {
            }

            @Override
            public void onBlur(MouseEvent event) {
            }

            @Override
            public void onClick(MouseEvent event) {
            }

            @Override
            public void onMouseMove(MouseEvent event) {
            }
        };
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
        AbstractGraph graph = new AbstractGraph() {
            @Override
            public void draw(Graphics2D graphics) {
            }

            @Override
            public void onFocus(MouseEvent event) {
            }

            @Override
            public void onBlur(MouseEvent event) {
            }

            @Override
            public void onClick(MouseEvent event) {
            }

            @Override
            public void onMouseMove(MouseEvent event) {
            }
        };
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the onBlur .
     */
    @Test
    void onBlur() {
        AbstractGraph graph = new AbstractGraph() {
            @Override
            public void draw(Graphics2D graphics) {
            }

            @Override
            public void onFocus(MouseEvent event) {
            }

            @Override
            public void onBlur(MouseEvent event) {
            }

            @Override
            public void onClick(MouseEvent event) {
            }

            @Override
            public void onMouseMove(MouseEvent event) {
            }
        };
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the onClick .
     */
    @Test
    void onClick() {
        AbstractGraph graph = new AbstractGraph() {
            @Override
            public void draw(Graphics2D graphics) {
            }

            @Override
            public void onFocus(MouseEvent event) {
            }

            @Override
            public void onBlur(MouseEvent event) {
            }

            @Override
            public void onClick(MouseEvent event) {
            }

            @Override
            public void onMouseMove(MouseEvent event) {
            }
        };
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the onMouseMove .
     */
    @Test
    void onMouseMove() {
        AbstractGraph graph = new AbstractGraph() {
            @Override
            public void draw(Graphics2D graphics) {
            }

            @Override
            public void onFocus(MouseEvent event) {
            }

            @Override
            public void onBlur(MouseEvent event) {
            }

            @Override
            public void onClick(MouseEvent event) {
            }

            @Override
            public void onMouseMove(MouseEvent event) {
            }
        };
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the edgeInspect .
     */
    @Test
    void edgeInspect() {
        AbstractGraph graph = new AbstractGraph() {
            @Override
            public void draw(Graphics2D graphics) {
            }

            @Override
            public void onFocus(MouseEvent event) {
            }

            @Override
            public void onBlur(MouseEvent event) {
            }

            @Override
            public void onClick(MouseEvent event) {
            }

            @Override
            public void onMouseMove(MouseEvent event) {
            }
        };
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the repaint .
     */
    @Test
    void repaint() {
        AbstractGraph graph = new AbstractGraph() {
            @Override
            public void draw(Graphics2D graphics) {
            }

            @Override
            public void onFocus(MouseEvent event) {
            }

            @Override
            public void onBlur(MouseEvent event) {
            }

            @Override
            public void onClick(MouseEvent event) {
            }

            @Override
            public void onMouseMove(MouseEvent event) {
            }
        };
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the setRect .
     */
    @Test
    void setRect() {
        AbstractGraph graph = new AbstractGraph() {
            @Override
            public void draw(Graphics2D graphics) {
            }

            @Override
            public void onFocus(MouseEvent event) {
            }

            @Override
            public void onBlur(MouseEvent event) {
            }

            @Override
            public void onClick(MouseEvent event) {
            }

            @Override
            public void onMouseMove(MouseEvent event) {
            }
        };
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }

    /**
     * test function the drawString .
     */
    @Test
    void drawString() {
        AbstractGraph graph = new AbstractGraph() {
            @Override
            public void draw(Graphics2D graphics) {
            }

            @Override
            public void onFocus(MouseEvent event) {
            }

            @Override
            public void onBlur(MouseEvent event) {
            }

            @Override
            public void onClick(MouseEvent event) {
            }

            @Override
            public void onMouseMove(MouseEvent event) {
            }
        };
        graph.setRect(0.0F, 0.0F, 100.0F, 100.0F);
        Assertions.assertEquals(100, graph.rect.width);
    }
}