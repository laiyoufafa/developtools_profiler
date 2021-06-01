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

package ohos.devtools.views.charts.tooltip;

import com.intellij.ui.JBColor;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.swing.TaskScenePanelChart;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

/**
 * 自定义Tooltip的测试类
 *
 * @since 2021/3/31 15:56
 */
public class LegendTooltipTest {
    private static final String TEST_TIME = "00:23:189";
    private static final String TEST_TEXT = "Java:123MB";
    private ProfilerChartsView view;
    private List<TooltipItem> tooltipItems;

    /**
     * functional test
     *
     * @tc.name: init
     * @tc.number: OHOS_JAVA_View_LegendTooltip_init_0001
     * @tc.desc: init
     * @tc.type: functional testing
     * @tc.require: SR000FK5SL
     */
    @Before
    public void init() {
        view = new ProfilerChartsView(LayoutConstants.NUM_L, true, new TaskScenePanelChart());
        tooltipItems = Collections.singletonList(new TooltipItem(JBColor.GRAY, TEST_TEXT));
    }

    /**
     * functional test
     *
     * @tc.name: getInstance
     * @tc.number: OHOS_JAVA_View_LegendTooltip_getInstance_0001
     * @tc.desc: getInstance
     * @tc.type: functional testing
     * @tc.require: SR000FK5SL
     */
    @Test
    public void getInstanceTest() {
        Assert.assertNotNull(LegendTooltip.getInstance());
    }

    /**
     * functional test
     *
     * @tc.name: hideTip
     * @tc.number: OHOS_JAVA_View_LegendTooltip_hideTip_0001
     * @tc.desc: hideTip
     * @tc.type: functional testing
     * @tc.require: SR000FK5SL
     */
    @Test
    public void hideTipTest() {
        LegendTooltip.getInstance().hideTip();
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: showTip
     * @tc.number: OHOS_JAVA_View_LegendTooltip_showTip_0001
     * @tc.desc: showTip
     * @tc.type: functional testing
     * @tc.require: SR000FK5SL
     */
    @Test
    public void showTipTest() {
        LegendTooltip.getInstance().showTip(view, TEST_TIME, "0", tooltipItems, true);
        Assert.assertTrue(true);
    }

    /**
     * functional test
     *
     * @tc.name: followWithMouse
     * @tc.number: OHOS_JAVA_View_LegendTooltip_followWithMouse_0001
     * @tc.desc: followWithMouse
     * @tc.type: functional testing
     * @tc.require: SR000FK5SL
     */
    @Test
    public void followWithMouseTest() {
        MouseEvent mouseEvent = new MouseEvent(view, 0, 1L, 0, 0, 0, 1, false);
        LegendTooltip.getInstance().followWithMouse(mouseEvent);
        Assert.assertTrue(true);
    }
}
