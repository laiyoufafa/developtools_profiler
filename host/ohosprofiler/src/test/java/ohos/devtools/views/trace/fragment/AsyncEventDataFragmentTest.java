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

import ohos.devtools.views.trace.bean.AsyncEvent;
import ohos.devtools.views.trace.component.AnalystPanel;
import ohos.devtools.views.trace.component.ContentPanel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

class AsyncEventDataFragmentTest {
    AnalystPanel analystPanel = new AnalystPanel();
    ContentPanel contentPanel = new ContentPanel(analystPanel);
    AsyncEvent asyncEvent = new AsyncEvent();
    List<AsyncEvent> list = new ArrayList<>();
    AsyncEventDataFragment fragment = new AsyncEventDataFragment(contentPanel, asyncEvent, list);

    @Test
    void mouseClicked() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, 1, 1, 1, true, 1);
        fragment.mouseClicked(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseClickedMax() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, 2, true, 1);
        fragment.mouseClicked(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseClickedMin() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, Integer.MIN_VALUE, Integer.MIN_VALUE, 2, true, 1);
        fragment.mouseClicked(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseClickedzero() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, 0, 0, 2, true, 1);
        fragment.mouseClicked(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseClickedNegative() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, -2, -2, 2, true, 1);
        fragment.mouseClicked(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseReleased() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, 1, 1, 1, true, 1);
        fragment.mouseReleased(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseReleasedMax() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, 2, true, 1);
        fragment.mouseReleased(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseReleasedMin() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, Integer.MIN_VALUE, Integer.MIN_VALUE, 2, true, 1);
        fragment.mouseReleased(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseReleasedzero() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, 0, 0, 2, true, 1);
        fragment.mouseReleased(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseReleasedNegative() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, -2, -2, 2, true, 1);
        fragment.mouseReleased(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseEntered() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, 1, 1, 1, true, 1);
        fragment.mouseEntered(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseEnteredMax() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, 2, true, 1);
        fragment.mouseEntered(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseEnteredMin() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, Integer.MIN_VALUE, Integer.MIN_VALUE, 2, true, 1);
        fragment.mouseEntered(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseEnteredzero() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, 0, 0, 2, true, 1);
        fragment.mouseEntered(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseEnteredNegative() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, -2, -2, 2, true, 1);
        fragment.mouseEntered(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseExited() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, 1, 1, 1, true, 1);
        fragment.mouseExited(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseExitedMax() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, 2, true, 1);
        fragment.mouseExited(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseExitedMin() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, Integer.MIN_VALUE, Integer.MIN_VALUE, 2, true, 1);
        fragment.mouseExited(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseExitedzero() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, 0, 0, 2, true, 1);
        fragment.mouseExited(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseExitedNegative() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, -2, -2, 2, true, 1);
        fragment.mouseExited(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseMoved() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, 1, 1, 1, true, 1);
        fragment.mouseMoved(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseMovedMax() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, 2, true, 1);
        fragment.mouseMoved(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseMovedMin() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, Integer.MIN_VALUE, Integer.MIN_VALUE, 2, true, 1);
        fragment.mouseMoved(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseMovedzero() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, 0, 0, 2, true, 1);
        fragment.mouseMoved(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void mouseMovedNegative() {
        MouseEvent mouseEvent = new MouseEvent(contentPanel, 1, 1, 1, -2, -2, 2, true, 1);
        fragment.mouseMoved(mouseEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void keyReleased() {
        KeyEvent keyEvent = new KeyEvent(contentPanel, 1, 1L, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        fragment.keyReleased(keyEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void keyReleasedMax() {
        KeyEvent keyEvent = new KeyEvent(contentPanel, 1, Integer.MAX_VALUE, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        fragment.keyReleased(keyEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void keyReleasedMin() {
        KeyEvent keyEvent = new KeyEvent(contentPanel, 1, Integer.MIN_VALUE, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        fragment.keyReleased(keyEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void keyReleasedZero() {
        KeyEvent keyEvent = new KeyEvent(contentPanel, 1, 0, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        fragment.keyReleased(keyEvent);
        Assertions.assertNotNull(fragment);
    }

    @Test
    void keyReleasedNegative() {
        KeyEvent keyEvent = new KeyEvent(contentPanel, 1, -1, 1, KeyEvent.KEY_PRESSED, 'A', 1);
        fragment.keyReleased(keyEvent);
        Assertions.assertNotNull(fragment);
    }

}