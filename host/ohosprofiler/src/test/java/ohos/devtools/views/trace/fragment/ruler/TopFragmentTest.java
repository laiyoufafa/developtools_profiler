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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * test TopFragment class .
 *
 * @date 2021/4/24 17:55
 */
class TopFragmentTest {
    /**
     * test function the draw .
     */
    @Test
    void draw() {
        TopFragment fragment = new TopFragment(new JPanel());
        BufferedImage image = UIUtil.createImage(new JPanel(), 1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        fragment.draw(graphics2D);
        Assertions.assertNotNull(fragment);
    }
}