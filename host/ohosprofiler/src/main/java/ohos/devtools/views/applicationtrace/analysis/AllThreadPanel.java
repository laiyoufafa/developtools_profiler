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

package ohos.devtools.views.applicationtrace.analysis;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.applicationtrace.AllData;
import ohos.devtools.views.perftrace.PerfData;

import java.util.Objects;

/**
 * app data all thread show panel
 *
 * @since 2021/5/20 18:00
 */
public class AllThreadPanel extends JBPanel {
    private JBTabbedPane allThreadTab;
    private AllThreadSummaryPanel allThreadSummaryPanel = new AllThreadSummaryPanel();
    private TopBottomPanel topDownPanel;
    private TopBottomPanel bottomUpPanel;
    private FlameSearchChart flameSearchChart;

    /**
     * AllThreadPanel structure function
     *
     * @param analysisEnum analysisEnum
     */
    public AllThreadPanel(AnalysisEnum analysisEnum) {
        setLayout(new MigLayout("insets 0 0 10 0", "[grow,fill]", "[grow,fill]"));
        allThreadTab = new JBTabbedPane();
        allThreadTab.setBackground(JBColor.background().darker());
        setBorder(JBUI.Borders.empty(5, 8));
        if (Objects.equals(analysisEnum, AnalysisEnum.APP)) {
            topDownPanel =
                new TopBottomPanel((startNS, endNS, scale) -> AllData.getFuncTreeTopDown(startNS, endNS), null);
            bottomUpPanel =
                new TopBottomPanel((startNS, endNS, scale) -> AllData.getFuncTreeBottomUp(startNS, endNS), null);
            flameSearchChart =
                new FlameSearchChart((startNS, endNS, scale) -> AllData.getFuncTreeFlameChart(startNS, endNS), null);
        } else {
            topDownPanel =
                new TopBottomPanel((startNS, endNS, scale) -> PerfData.getFuncTreeTopDown(startNS, endNS), null);
            bottomUpPanel =
                new TopBottomPanel((startNS, endNS, scale) -> PerfData.getFuncTreeBottomUp(startNS, endNS), null);
            flameSearchChart =
                new FlameSearchChart((startNS, endNS, scale) -> PerfData.getFuncTreeFlameChart(startNS, endNS), null);
        }
        allThreadTab.addTab("Summary", allThreadSummaryPanel);
        allThreadTab.addTab("Top Down", topDownPanel);
        allThreadTab.addTab("Flame Chart", flameSearchChart);
        allThreadTab.addTab("Bottom Up", bottomUpPanel);
        add(allThreadTab);
    }
}
