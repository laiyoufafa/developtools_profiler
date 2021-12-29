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

package ohos.devtools.views.distributed;

import com.intellij.openapi.wm.IdeGlassPane;
import com.intellij.openapi.wm.impl.IdeGlassPaneImpl;
import ohos.devtools.Config;
import ohos.devtools.views.distributed.bean.DistributedParams;
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

class DistributedDataStatisticsPaneTest {
    private FrameFixture frame;
    private DistributedDataStatisticsPane panel;
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
        DistributedPanel distributedPanel = new DistributedPanel();
        distributedPanel.load(new DistributedParams.Builder()
            .setPkgNameA("com.test.maps")
            .setPkgNameB("com.distributed.ims")
            .setDeviceNameA("distributed device 1")
            .setDeviceNameB("distributed device 2")
            .setProcessIdA(27521)
            .setProcessIdB(1155)
            .setPathA(Config.TRACE_DISTRIBUTED_A)
            .setPathB(Config.TRACE_DISTRIBUTED_B)
            .setOffsetA(186485719530000L)
            .setOffsetB(186590530788000L)
            .build());
        distributedPanel.updateUI();
        panel = new DistributedDataStatisticsPane();
        jFrame.add(panel);
        frame = new FrameFixture(jFrame);
        frame.show(new Dimension(1024, 600));
        frame.moveTo(new Point(0, 0));
        //        inspect();
    }

    @AfterEach
    void tearDown() {
        frame.cleanUp();
    }

    @Test
    void load() {
        delay();
        mouseClick(390, 68);

        mouseClick(312, 64);
        mouseClick(290, 87);
        mouseClick(390, 68);

        mouseClick(312, 64);
        mouseClick(290, 106);
        mouseClick(390, 68);

        mouseClick(312, 64);
        mouseClick(290, 123);
        mouseClick(390, 68);

        mouseClick(312, 64);
        mouseClick(290, 140);
        mouseClick(390, 68);

        mouseClick(312, 64);
        mouseClick(290, 158);
        mouseClick(390, 68);
        //        inspect();
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

    private void inspect() {
        while (true) {
            Point location = MouseInfo.getPointerInfo().getLocation();
            robot.delay(1000);
        }
    }
}