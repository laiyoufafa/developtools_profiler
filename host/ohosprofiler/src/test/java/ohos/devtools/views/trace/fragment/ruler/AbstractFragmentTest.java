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

import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * test AbstractFragment class .
 *
 * @since 2021/4/24 17:53
 */
class AbstractFragmentTest {
    /**
     * test function the getRect .
     */
    @Test
    void getRect() {
        AbstractFragment fragment = new AbstractFragment() {
            @Override
            public void draw(Graphics2D graphics) {
            }
        };
        Rectangle rect = fragment.getRect();
        assertEquals(true, rect != null);
    }

    /**
     * test function the setRect .
     */
    @Test
    void setRect() {
        AbstractFragment fragment = new AbstractFragment() {
            @Override
            public void draw(Graphics2D graphics) {
            }
        };
        fragment.setRect(new Rectangle(0, 0, 100, 100));
        assertEquals(100, fragment.getRect().width);
    }

    /**
     * test function the getDescRect .
     */
    @Test
    void getDescRect() {
        AbstractFragment fragment = new AbstractFragment() {
            @Override
            public void draw(Graphics2D graphics) {
            }
        };
        fragment.setDescRect(new Rectangle(0, 0, 100, 100));
        assertEquals(100, fragment.getDescRect().width);
    }

    /**
     * test function the setDescRect .
     */
    @Test
    void setDescRect() {
        AbstractFragment fragment = new AbstractFragment() {
            @Override
            public void draw(Graphics2D graphics) {
            }
        };
        fragment.setDescRect(new Rectangle(0, 0, 100, 100));
        assertEquals(100, fragment.getDescRect().width);
    }

    /**
     * test function the getDataRect .
     */
    @Test
    void getDataRect() {
        AbstractFragment fragment = new AbstractFragment() {
            @Override
            public void draw(Graphics2D graphics) {
            }
        };
        fragment.setDataRect(new Rectangle(0, 0, 100, 100));
        assertEquals(100, fragment.getDataRect().width);
    }

    /**
     * test function the setDataRect .
     */
    @Test
    void setDataRect() {
        AbstractFragment fragment = new AbstractFragment() {
            @Override
            public void draw(Graphics2D graphics) {
            }
        };
        fragment.setDataRect(new Rectangle(0, 0, 100, 100));
        assertEquals(100, fragment.getDataRect().width);
    }

    /**
     * test function the getRoot .
     */
    @Test
    void getRoot() {
        AbstractFragment fragment = new AbstractFragment() {
            @Override
            public void draw(Graphics2D graphics) {
            }
        };
        JPanel jPanel = new JPanel();
        fragment.setRoot(jPanel);
        assertEquals(jPanel, fragment.getRoot());
    }

    /**
     * test function the setRoot .
     */
    @Test
    void setRoot() {
        AbstractFragment fragment = new AbstractFragment() {
            @Override
            public void draw(Graphics2D graphics) {
            }
        };
        JPanel jPanel = new JPanel();
        fragment.setRoot(jPanel);
        assertEquals(jPanel, fragment.getRoot());
    }

    /**
     * test function the getLineColor .
     */
    @Test
    void getLineColor() {
        AbstractFragment fragment = new AbstractFragment() {
            @Override
            public void draw(Graphics2D graphics) {
            }
        };
        assertNotNull(fragment.getLineColor());
    }

    /**
     * test function the getTextColor .
     */
    @Test
    void getTextColor() {
        AbstractFragment fragment = new AbstractFragment() {
            @Override
            public void draw(Graphics2D graphics) {
            }
        };
        assertNotNull(fragment.getTextColor());
    }

    /**
     * test function the repaint .
     */
    @Test
    void repaint() {
        AbstractFragment fragment = new AbstractFragment() {
            @Override
            public void draw(Graphics2D graphics) {
            }
        };
        fragment.repaint();
        assertNotNull(fragment);
    }

}