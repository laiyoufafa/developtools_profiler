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

import com.intellij.openapi.wm.IdeGlassPane;
import com.intellij.openapi.wm.impl.IdeGlassPaneImpl;
import ohos.devtools.Config;
import org.assertj.swing.fixture.FrameFixture;
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
 * test AnalystPanel class
 *
 * @since 2021/4/24 18:05
 */
class AnalystPanelAutoTest {
    private FrameFixture frame;
    private SysAnalystPanel distributedPanel;
    private JFrame jFrame;
    private Robot robot;

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
        distributedPanel = new SysAnalystPanel();
        jFrame.add(distributedPanel);
        frame = new FrameFixture(jFrame);
        // Display the frame
        distributedPanel.load(Config.TRACE_SYS, true);
        frame.show(new Dimension(1920, 1080));
        frame.moveTo(new Point(0, 0));

    }

    @AfterEach
    void tearDown() {
        frame.cleanUp();
    }

    @Test
    void load() {
        delay(10000);
        select(600, 110, 610, 110);
        mouseClick(557, 171); // 小旗帜
        mouseClick(560, 172); // 点击小旗帜
        select(557, 212, 600, 212); // 选择cpu区域
        mouseClick(557, 212); // 点击cpu切片
        mouseClick(549, 572); // 点击clock 节点
        wheel(-600);
        mouseClick(13, 371);
        wheel(-200);
        delay();
        mouseClick(620, 272);
        mouseClick(612, 314);
        select(520, 253, 668, 555);
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