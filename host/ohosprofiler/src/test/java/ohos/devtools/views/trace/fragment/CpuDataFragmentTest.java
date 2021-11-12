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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * test CpuDataFragment class .
 *
 * @since 2021/4/24 17:57
 */
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
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseClickedMax() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, 2, true, 1);
        cpuDataFragment.mouseClicked(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseClickedMin() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, Integer.MIN_VALUE, Integer.MIN_VALUE, 2, true, 1);
        cpuDataFragment.mouseClicked(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseClickedzero() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 0, 0, 2, true, 1);
        cpuDataFragment.mouseClicked(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseClickedNegative() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, -2, -2, 2, true, 1);
        cpuDataFragment.mouseClicked(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseReleased() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        cpuDataFragment.mouseReleased(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseReleasedMax() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, 2, true, 1);
        cpuDataFragment.mouseReleased(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseReleasedMin() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, Integer.MIN_VALUE, Integer.MIN_VALUE, 2, true, 1);
        cpuDataFragment.mouseReleased(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseReleasedzero() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 0, 0, 2, true, 1);
        cpuDataFragment.mouseReleased(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseReleasedNegative() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, -2, -2, 2, true, 1);
        cpuDataFragment.mouseReleased(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseEntered() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        cpuDataFragment.mouseEntered(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseEnteredMax() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, 2, true, 1);
        cpuDataFragment.mouseEntered(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseEnteredMin() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, Integer.MIN_VALUE, Integer.MIN_VALUE, 2, true, 1);
        cpuDataFragment.mouseEntered(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseEnteredzero() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 0, 0, 2, true, 1);
        cpuDataFragment.mouseEntered(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseEnteredNegative() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, -2, -2, 2, true, 1);
        cpuDataFragment.mouseEntered(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseExited() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        cpuDataFragment.mouseExited(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseExitedMax() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, 2, true, 1);
        cpuDataFragment.mouseExited(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseExitedMin() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, Integer.MIN_VALUE, Integer.MIN_VALUE, 2, true, 1);
        cpuDataFragment.mouseExited(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseExitedzero() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 0, 0, 2, true, 1);
        cpuDataFragment.mouseExited(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseExitedNegative() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, -2, -2, 2, true, 1);
        cpuDataFragment.mouseExited(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseMoved() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        cpuDataFragment.mouseMoved(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseMovedMax() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, 2, true, 1);
        cpuDataFragment.mouseMoved(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseMovedMin() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, Integer.MIN_VALUE, Integer.MIN_VALUE, 2, true, 1);
        cpuDataFragment.mouseMoved(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseMovedzero() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 0, 0, 2, true, 1);
        cpuDataFragment.mouseMoved(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void mouseMovedNegative() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, -2, -2, 2, true, 1);
        cpuDataFragment.mouseMoved(mouseEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void keyReleased() {
        KeyEvent keyEvent = new KeyEvent(jPanel, 1, 1L, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        cpuDataFragment.keyReleased(keyEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void keyReleasedMax() {
        KeyEvent keyEvent = new KeyEvent(jPanel, 1, Integer.MAX_VALUE, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        cpuDataFragment.keyReleased(keyEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void keyReleasedMin() {
        KeyEvent keyEvent = new KeyEvent(jPanel, 1, Integer.MIN_VALUE, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        cpuDataFragment.keyReleased(keyEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void keyReleasedZero() {
        KeyEvent keyEvent = new KeyEvent(jPanel, 1, 0, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        cpuDataFragment.keyReleased(keyEvent);
        Assertions.assertNotNull(cpuDataFragment);
    }

    @Test
    void keyReleasedNegative() {
        KeyEvent keyEvent = new KeyEvent(jPanel, 1, -1, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        cpuDataFragment.keyReleased(keyEvent);
        Assertions.assertNotNull(cpuDataFragment);
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