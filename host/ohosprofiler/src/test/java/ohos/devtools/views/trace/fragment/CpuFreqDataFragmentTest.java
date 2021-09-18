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
import ohos.devtools.views.trace.bean.CpuData;
import ohos.devtools.views.trace.bean.CpuFreqData;
import ohos.devtools.views.trace.bean.CpuFreqMax;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JPanel;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * test CpuFreqDataFragment class .
 *
 * @date: 2021/4/24 17:57
 */
class CpuFreqDataFragmentTest {
    private CpuFreqDataFragment cpuFreqDataFragment;
    private JPanel jPanel;

    /**
     * init the setUp .
     */
    @BeforeEach
    void setUp() {
        List<CpuFreqData> list = new ArrayList<>();
        CpuFreqMax cpuFreqMax = new CpuFreqMax();
        cpuFreqMax.setName("100");
        cpuFreqMax.setValue(2.5);
        jPanel = new JPanel();
        cpuFreqDataFragment = new CpuFreqDataFragment(jPanel, "name", cpuFreqMax, list);
    }

    /**
     * test function the draw .
     */
    @Test
    void draw() {
        BufferedImage image = UIUtil.createImage(new JPanel(), 1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = image.createGraphics();
        cpuFreqDataFragment.draw(graphics2D);
        Assertions.assertNotNull(cpuFreqDataFragment.favoriteGraph);
    }

    /**
     * test function the mouseClicked .
     */
    @Test
    void mouseClicked() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        cpuFreqDataFragment.mouseClicked(mouseEvent);
        Assertions.assertNotNull(cpuFreqDataFragment.favoriteGraph);
    }

    /**
     * test function the mouseMoved .
     */
    @Test
    void mouseMoved() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        cpuFreqDataFragment.mouseMoved(mouseEvent);
        Assertions.assertNotNull(cpuFreqDataFragment.favoriteGraph);
    }

    /**
     * test function the click .
     */
    @Test
    void click() {
        MouseEvent mouseEvent = new MouseEvent(jPanel, 1, 1, 1, 1, 1, 1, true, 1);
        cpuFreqDataFragment.click(mouseEvent, new CpuData());
        Assertions.assertNotNull(cpuFreqDataFragment.favoriteGraph);
    }
}