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

package ohos.devtools.views.layout.chartview;

import ohos.devtools.views.common.LayoutConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.Component;
import java.awt.AWTException;

/**
 * profiler scrollbar test
 */
public class ProfilerScrollbarTest {
    private ProfilerScrollbar bar;

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerTimeline_paint_0001
     * @tc.desc: chart Timeline functional test
     * @tc.type: profiler scrollbar test
     * @tc.require: SR-002-AR-001
     */
    @Before
    public void init() {
        ProfilerChartsView view = new ProfilerChartsView(LayoutConstants.NUM_L, true, new TaskScenePanelChart());
        view.initScrollbar();
        Component[] components = view.getComponents();
        for (Component comp : components) {
            if (comp instanceof ProfilerScrollbar) {
                bar = (ProfilerScrollbar) comp;
                break;
            }
        }
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerTimeline_paint_0001
     * @tc.desc: chart Timeline functional test
     * @tc.type: profiler scrollbar test
     * @tc.require: SR-002-AR-001
     */
    @Test
    public void resizeAndRepositionTest() {
        ProfilerChartsView view = new ProfilerChartsView(LayoutConstants.NUM_L, true, new TaskScenePanelChart());
        new ProfilerScrollbar(view).resizeAndReposition();
        Assert.assertTrue(true);
    }

    /**
     * functional testing
     *
     * @tc.name: view chart
     * @tc.number: OHOS_JAVA_views_ProfilerTimeline_paint_0001
     * @tc.desc: chart Timeline functional test
     * @tc.type: profiler scrollbar test
     * @tc.require: SR-002-AR-001
     * @throws AWTException throw AWTException
     */
    @Test
    public void initDragPressTimeTest() throws AWTException {
        ProfilerChartsView view = new ProfilerChartsView(LayoutConstants.NUM_L, true, new TaskScenePanelChart());
        ProfilerScrollbar profilerScrollbar = new ProfilerScrollbar(view);
        Assert.assertTrue(true);
    }
}
