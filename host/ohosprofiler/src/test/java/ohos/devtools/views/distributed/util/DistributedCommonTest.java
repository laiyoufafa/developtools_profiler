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

package ohos.devtools.views.distributed.util;

import com.intellij.util.ui.UIUtil;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DistributedCommonTest {

    @Test
    void getStringRect() {
        BufferedImage image = UIUtil.createImage(new JPanel(), 1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        assertNotNull(DistributedCommon.getStringRect(graphics2D, "str"));
    }

    @Test
    void x2ns() {
        assertEquals(0, DistributedCommon.x2ns(0, new Rectangle()));
    }

    @Test
    void testX2ns() {
        assertEquals(0, DistributedCommon.x2ns(0, new Rectangle(), 0));
    }

    @Test
    void nsToXByDur() {
        assertNotNull(DistributedCommon.nsToXByDur(0, new Rectangle(), 0));
    }

    @Test
    void setAlpha() {
        BufferedImage image = UIUtil.createImage(new JPanel(), 1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        DistributedCommon.setAlpha(graphics2D, 0F);
        assertNotNull(graphics2D);
    }

    @Test
    void drawStringCenter() {
        BufferedImage image = UIUtil.createImage(new JPanel(), 1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        DistributedCommon.drawStringCenter(graphics2D, "str", new Rectangle(0, 0));
        assertNotNull(graphics2D);
    }

    @Test
    void drawStringVHCenter() {
        BufferedImage image = UIUtil.createImage(new JPanel(), 1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        DistributedCommon.drawStringVHCenter(graphics2D, "str", new Rectangle(0, 0));
        assertNotNull(graphics2D);
    }

    @Test
    void drawStringMiddleHeight() {
        BufferedImage image = UIUtil.createImage(new JPanel(), 1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        DistributedCommon.drawStringMiddleHeight(graphics2D, "str", new Rectangle(0, 0));
        assertNotNull(graphics2D);
    }
}