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
import ohos.devtools.views.distributed.util.DistributedDB;
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
import java.awt.event.KeyEvent;

class DistributedPanelTest {
    private FrameFixture frame;
    private DistributedPanel panel;
    private JFrame jFrame;
    private Robot robot;

    @BeforeEach
    void setUp() {
        DistributedDB.setDbName(Config.TRACE_DISTRIBUTED_A, Config.TRACE_DISTRIBUTED_B);
        DistributedDB.load(true);
        jFrame = new JFrame();
        try {
            robot = new Robot();
            IdeGlassPane ideGlassPane = new IdeGlassPaneImpl(jFrame.getRootPane());
            jFrame.getRootPane().setGlassPane((JPanel) ideGlassPane);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        panel = new DistributedPanel();
        jFrame.add(panel);
        frame = new FrameFixture(jFrame);
        frame.show(new Dimension(1024, 600));
        frame.moveTo(new Point(0, 0));
    }

    @AfterEach
    void tearDown() {
        frame.cleanUp();
    }

    @Test
    void load() {
        panel.load(new DistributedParams.Builder()
            .setPkgNameA("com.maps")
            .setPkgNameB("com.distributed.ims")
            .setDeviceNameA("distributed Mate40")
            .setDeviceNameB("distributed P30")
            .setProcessIdA(27521)
            .setProcessIdB(1155)
            .setPathA(Config.TRACE_DISTRIBUTED_A)
            .setPathB(Config.TRACE_DISTRIBUTED_B)
            .setOffsetA(186485719530000L)
            .setOffsetB(186590530788000L)
            .build());
        panel.updateUI();
        delay(10000);
        select(280, 90, 285, 90); // 测试选择时间区间
        keyClick(KeyEvent.VK_W); // 测试 wsad 缩放
        keyClick(KeyEvent.VK_S);
        keyClick(KeyEvent.VK_A);
        keyClick(KeyEvent.VK_D);
        mouseClick(23, 234); // 展开thread
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
