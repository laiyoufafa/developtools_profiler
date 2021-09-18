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
import ohos.devtools.views.trace.bean.ThreadData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * test ThreadDataFragment class .
 *
 * @date 2021/4/24 17:58
 */
class ThreadDataFragmentTest {
    private ThreadDataFragment threadDataFragment;
    private JPanel jPanel;

    /**
     * init the threadDataFragment .
     */
    @BeforeEach
    void setUp() {
        ThreadData process = new ThreadData();
        jPanel = new JPanel();
        threadDataFragment = new ThreadDataFragment(jPanel, process);
    }

    /**
     * test function the draw .
     */
    @Test
    void draw() {
        BufferedImage image = UIUtil.createImage(new JPanel(), 1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        threadDataFragment.draw(graphics2D);
        Assertions.assertNotNull(threadDataFragment);
    }

    /**
     * test function the mouseClicked .
     */
    @Test
    void mouseClicked() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        threadDataFragment.mouseClicked(mouseEvent);
        Assertions.assertNotNull(threadDataFragment);
    }

    /**
     * test function the mouseMoved .
     */
    @Test
    void mouseMoved() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        threadDataFragment.mouseMoved(mouseEvent);
        Assertions.assertNotNull(threadDataFragment);
    }

    /**
     * test function the click .
     */
    @Test
    void click() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        threadDataFragment.click(mouseEvent, new ThreadData());
        Assertions.assertNotNull(threadDataFragment);
    }
}