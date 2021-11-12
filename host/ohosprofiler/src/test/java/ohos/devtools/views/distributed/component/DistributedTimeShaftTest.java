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

package ohos.devtools.views.distributed.component;

import com.intellij.util.ui.UIUtil;
import ohos.devtools.views.trace.ITimeRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

class DistributedTimeShaftTest {
    DistributedTimeShaft timeShaft = new DistributedTimeShaft(new ITimeRange() {
        @Override
        public void change(long startNS, long endNS, long scale) {}
    }, keyEvent -> {
        keyEvent.getKeyCode();
    }, event -> {
        event.getX();
    });

    @Test
    void setRange() {
        timeShaft.setRange(0L, 0L);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void setRangeOutSize() {
        timeShaft.setRange(-1L, 1L);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void setRangeAllOutSize() {
        timeShaft.setRange(-1L, -1L);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void setRangeMax() {
        timeShaft.setRange(Long.MAX_VALUE, -1L);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void setRangeMin() {
        timeShaft.setRange(Long.MIN_VALUE, -1L);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mousePressed() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, 1, 1, 1, true, 1);
        timeShaft.mousePressed(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mousePressedMax() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, 2, true, 1);
        timeShaft.mousePressed(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mousePressedMin() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, Integer.MIN_VALUE, Integer.MIN_VALUE, 2, true, 1);
        timeShaft.mousePressed(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mousePressedzero() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, 0, 0, 2, true, 1);
        timeShaft.mousePressed(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mousePressedNegative() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, -2, -2, 2, true, 1);
        timeShaft.mousePressed(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseClicked() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, 1, 1, 1, true, 1);
        timeShaft.mouseClicked(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseClickedMax() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, 2, true, 1);
        timeShaft.mouseClicked(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseClickedMin() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, Integer.MIN_VALUE, Integer.MIN_VALUE, 2, true, 1);
        timeShaft.mouseClicked(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseClickedzero() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, 0, 0, 2, true, 1);
        timeShaft.mouseClicked(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseClickedNegative() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, -2, -2, 2, true, 1);
        timeShaft.mouseClicked(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseReleased() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, 1, 1, 1, true, 1);
        timeShaft.mouseReleased(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseReleasedMax() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, 2, true, 1);
        timeShaft.mouseReleased(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseReleasedMin() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, Integer.MIN_VALUE, Integer.MIN_VALUE, 2, true, 1);
        timeShaft.mouseReleased(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseReleasedzero() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, 0, 0, 2, true, 1);
        timeShaft.mouseReleased(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseReleasedNegative() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, -2, -2, 2, true, 1);
        timeShaft.mouseReleased(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseEntered() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, 1, 1, 1, true, 1);
        timeShaft.mouseEntered(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseEnteredMax() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, 2, true, 1);
        timeShaft.mouseEntered(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseEnteredMin() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, Integer.MIN_VALUE, Integer.MIN_VALUE, 2, true, 1);
        timeShaft.mouseEntered(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseEnteredzero() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, 0, 0, 2, true, 1);
        timeShaft.mouseEntered(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseEnteredNegative() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, -2, -2, 2, true, 1);
        timeShaft.mouseEntered(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseExited() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, 1, 1, 1, true, 1);
        timeShaft.mouseExited(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseExitedMax() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, 2, true, 1);
        timeShaft.mouseExited(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseExitedMin() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, Integer.MIN_VALUE, Integer.MIN_VALUE, 2, true, 1);
        timeShaft.mouseExited(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseExitedzero() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, 0, 0, 2, true, 1);
        timeShaft.mouseExited(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseExitedNegative() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, -2, -2, 2, true, 1);
        timeShaft.mouseExited(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseDragged() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, 1, 1, 1, true, 1);
        timeShaft.mouseDragged(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseDraggedMax() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, 2, true, 1);
        timeShaft.mouseDragged(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseDraggedMin() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, Integer.MIN_VALUE, Integer.MIN_VALUE, 2, true, 1);
        timeShaft.mouseDragged(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseDraggedzero() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, 0, 0, 2, true, 1);
        timeShaft.mouseDragged(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseDraggedNegative() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, -2, -2, 2, true, 1);
        timeShaft.mouseDragged(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseMoved() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, 1, 1, 1, true, 1);
        timeShaft.mouseMoved(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseMovedMax() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, 2, true, 1);
        timeShaft.mouseMoved(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseMovedMin() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, Integer.MIN_VALUE, Integer.MIN_VALUE, 2, true, 1);
        timeShaft.mouseMoved(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseMovedzero() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, 0, 0, 2, true, 1);
        timeShaft.mouseMoved(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void mouseMovedNegative() {
        MouseEvent mouseEvent = new MouseEvent(timeShaft, 1, 1, 1, -2, -2, 2, true, 1);
        timeShaft.mouseMoved(mouseEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void keyPress() {
        KeyEvent keyEvent = new KeyEvent(timeShaft, 1, 1L, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        timeShaft.keyPressed(keyEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void keyPressMax() {
        KeyEvent keyEvent = new KeyEvent(timeShaft, 1, Integer.MAX_VALUE, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        timeShaft.keyPressed(keyEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void keyPressMin() {
        KeyEvent keyEvent = new KeyEvent(timeShaft, 1, Integer.MIN_VALUE, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        timeShaft.keyPressed(keyEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void keyPressZero() {
        KeyEvent keyEvent = new KeyEvent(timeShaft, 1, 0, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        timeShaft.keyPressed(keyEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void keyPressNegative() {
        KeyEvent keyEvent = new KeyEvent(timeShaft, 1, -1, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        timeShaft.keyPressed(keyEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void keyReleased() {
        KeyEvent keyEvent = new KeyEvent(timeShaft, 1, 1L, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        timeShaft.keyReleased(keyEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void keyReleasedMax() {
        KeyEvent keyEvent = new KeyEvent(timeShaft, 1, Integer.MAX_VALUE, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        timeShaft.keyReleased(keyEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void keyReleasedMin() {
        KeyEvent keyEvent = new KeyEvent(timeShaft, 1, Integer.MIN_VALUE, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        timeShaft.keyReleased(keyEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void keyReleasedZero() {
        KeyEvent keyEvent = new KeyEvent(timeShaft, 1, 0, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        timeShaft.keyReleased(keyEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void keyReleasedNegative() {
        KeyEvent keyEvent = new KeyEvent(timeShaft, 1, -1, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        timeShaft.keyReleased(keyEvent);
        Assertions.assertNotNull(timeShaft);
    }

    @Test
    void paintComponent() {
        BufferedImage image = UIUtil.createImage(new JPanel(), 1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        timeShaft.paintComponent(graphics2D);
        Assertions.assertNotNull(timeShaft);
    }
}