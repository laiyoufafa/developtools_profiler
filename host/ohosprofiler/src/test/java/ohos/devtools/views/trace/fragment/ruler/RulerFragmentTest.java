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

package ohos.devtools.views.trace.fragment.ruler;

import com.intellij.util.ui.UIUtil;
import org.junit.Test;

import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * test RulerFragment class .
 *
 * @date 2021/4/24 17:55
 */
class RulerFragmentTest {
    /**
     * test function the draw .
     */
    @Test
    void draw() {
        RulerFragment fragment = new RulerFragment(new JPanel(), (startNS, endNS) -> {
        });
        BufferedImage image = UIUtil.createImage(new JPanel(), 1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        fragment.draw(graphics2D);
        assertNotNull(fragment);
    }

    /**
     * test function the mouseMoved .
     */
    @Test
    void mouseMoved() {
        RulerFragment fragment = new RulerFragment(new JPanel(), (startNS, endNS) -> {
        });
        JPanel jPanel = new JPanel();
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        fragment.mouseMoved(mouseEvent);
        assertEquals(0, fragment.getRect().width);
    }

}