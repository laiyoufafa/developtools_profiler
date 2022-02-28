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

package ohos.devtools.views.trace.component;

import org.junit.jupiter.api.Test;

import java.awt.event.MouseEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * test TimeViewPort class .
 *
 * @date 2021/4/24 18:04
 */
class TimeViewPortTest {
    /**
     * test set the mousePressed .
     */
    @Test
    void mousePressed() {
        TimeViewPort timeViewPort = new TimeViewPort(height -> {
            String.valueOf(height);
        }, (startNS, endNS) -> {
            long time = startNS + endNS;
        });
        MouseEvent mouseEvent = new MouseEvent(timeViewPort, 1, 1, 1, 1, 1, 1, true, 1);
        timeViewPort.mousePressed(mouseEvent);
        assertEquals(0, timeViewPort.getHeight());
    }

    /**
     * test set the mouseDragged .
     */
    @Test
    void mouseDragged() {
        TimeViewPort timeViewPort = new TimeViewPort(height -> {
            String.valueOf(height);
        }, (startNS, endNS) -> {
            long time = startNS + endNS;
        });
        MouseEvent mouseEvent = new MouseEvent(timeViewPort, 1, 1, 1, 1, 1, 1, true, 1);
        timeViewPort.mouseDragged(mouseEvent);
        assertEquals(0, timeViewPort.getHeight());
    }

    /**
     * test set the mouseMoved .
     */
    @Test
    void mouseMoved() {
        TimeViewPort timeViewPort = new TimeViewPort(height -> {
            String.valueOf(height);
        }, (startNS, endNS) -> {
            long time = startNS + endNS;
        });
        MouseEvent mouseEvent = new MouseEvent(timeViewPort, 1, 1, 1, 1, 1, 1, true, 1);
        timeViewPort.mouseMoved(mouseEvent);
        assertEquals(0, timeViewPort.getHeight());
    }
}