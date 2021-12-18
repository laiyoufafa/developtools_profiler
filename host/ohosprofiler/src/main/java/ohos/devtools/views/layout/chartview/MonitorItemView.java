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

import com.intellij.ui.components.JBPanel;
import ohos.devtools.views.charts.ProfilerChart;

/**
 * Abstract parent class profiler monitor item view
 *
 * @since 2021/11/22
 */
public abstract class MonitorItemView extends JBPanel implements MonitorItemInterface {
    /**
     * bottomPanel
     */
    protected ProfilerChartsView bottomPanel;

    /**
     * parent
     */
    protected ItemsView parent;

    /**
     * chart
     */
    protected ProfilerChart chart;

    /**
     * chart
     */
    protected ProfilerChart expandShowChart;

    /**
     * fold
     */
    protected boolean fold = true;

    /**
     * showTable
     */
    protected boolean showTable = false;

    public ProfilerChartsView getBottomPanel() {
        return bottomPanel;
    }

    public ProfilerChart getChart() {
        return chart;
    }

    public boolean isFold() {
        return fold;
    }

    public boolean isShowTable() {
        return showTable;
    }
}
