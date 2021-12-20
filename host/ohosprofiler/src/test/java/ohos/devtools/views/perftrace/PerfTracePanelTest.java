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

package ohos.devtools.views.perftrace;

import com.intellij.openapi.wm.IdeGlassPane;
import com.intellij.openapi.wm.impl.IdeGlassPaneImpl;
import ohos.devtools.Config;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;

/**
 * Perf Trace Panel Test
 */
class PerfTracePanelTest {
    private String dbPath = Config.TRACE_PREF;
    private String cpuDbPath = Config.TRACE_CPU;
    private FrameFixture frame;
    private PerfTracePanel panel;
    private Robot robot;
    private JFrame jFrame;

    @BeforeEach
    void setUp() {
        jFrame = new JFrame();
        try {
            robot = new Robot();
            IdeGlassPane ideGlassPane = new IdeGlassPaneImpl(jFrame.getRootPane());
            jFrame.getRootPane().setGlassPane((JPanel) ideGlassPane);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        panel = new PerfTracePanel();
        jFrame.add(panel);
        frame = new FrameFixture(jFrame);
        frame.show(new Dimension(1920, 1080));
        frame.moveTo(new Point(0, 0));
        SwingUtilities.invokeLater(() -> {
            panel.load(dbPath, cpuDbPath, true);
            panel.updateUI();

        });
    }

    @AfterEach
    void tearDown() {
        frame.cleanUp();
    }

    @Test
    void load() {
        delay(20000);
        mouseClick(51, 166);
        mouseClick(1297, 92);
        select(326, 164, 489, 280);
        mouseClick(234, 418);
        mouseClick(1297, 92);
        mouseClick(263, 89);
        mouseClick(221, 93);
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