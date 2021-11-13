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

package ohos.devtools.views.applicationtrace;

import com.intellij.openapi.wm.IdeGlassPane;
import com.intellij.openapi.wm.impl.IdeGlassPaneImpl;
import ohos.devtools.Config;
import org.fest.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;

/**
 * App Trace Panel Test
 */
class AppTracePanelTest {
    private FrameFixture frame;
    private AppTracePanel panel;
    private JFrame jFrame;
    private Robot robot;

    @BeforeEach
    void setUp() {
        jFrame = new JFrame();
        try {
            robot = new Robot();
            robot.setAutoDelay(2000);
            IdeGlassPane ideGlassPane = new IdeGlassPaneImpl(jFrame.getRootPane());
            jFrame.getRootPane().setGlassPane((JPanel) ideGlassPane);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        panel = new AppTracePanel();
        jFrame.add(panel);
        frame = new FrameFixture(jFrame);
        frame.show(new Dimension(1920, 1080));
        frame.moveTo(new Point(0, 0));
    }

    @AfterEach
    void tearDown() {
        frame.close();
        frame.cleanUp();
    }

    @Test
    void load() {
        panel.load(Config.TRACE_APP, Config.TRACE_CPU, 8593, true);
        panel.updateUI();

        delay(10000);
        mouseClick(317, 373);
        select(494, 367, 584, 467);
        // select(419, 369, 500, 469);
        // mouseClick(1290, 96);
        // mouseClick(315, 535);
        // mouseClick(1290, 96);
        // mouseClick(52, 437);
        // mouseClick(1386, 97);
        delay();
    }

    private void mouseClick(int level, int vertical) {
        robot.delay(2000);
        robot.mouseMove(level, vertical);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    private void keyClick(int keyEvent) {
        robot.delay(2000);
        robot.keyPress(keyEvent);
        robot.keyRelease(keyEvent);
    }

    private void select(int x1, int y1, int x2, int y2) {
        robot.delay(2000);
        robot.mouseMove(x1, y1);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseMove(x2, y2);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    private void wheel(int wheelAmt) {
        robot.delay(1000);
        robot.mouseWheel(wheelAmt);
    }

    private void delay() {
        robot.delay(2000);
    }

    private void delay(int time) {
        robot.delay(time);
    }

    private void inspect() {
        while (true) {
            Point location = MouseInfo.getPointerInfo().getLocation();
            robot.delay(1000);
        }
    }
}