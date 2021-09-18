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

import java.util.Objects;

/**
 * The OtherThreadPanel
 *
 * @date 2021/04/22 12:25
 */
public class OtherThreadPanel extends JBPanel {
    private JBTabbedPane otherThreadTab;
    private TopBottomPanel topBottomPanel;
    private TopBottomPanel bottomUpPanel;
    private FlameSearchChart flameSearchChart;

    /**
     * structure
     *
     * @param analysisEnum analysisEnum
     */
    public OtherThreadPanel(AnalysisEnum analysisEnum) {
        setLayout(new MigLayout("inset 0", "[grow,fill]", "[grow,fill]"));
        otherThreadTab = new JBTabbedPane();
        otherThreadTab.setBackground(JBColor.background().darker());
        if (Objects.equals(analysisEnum, AnalysisEnum.APP)) {
            topBottomPanel = new TopBottomPanel(null, AllData::getFuncTreeTopDown);
            bottomUpPanel = new TopBottomPanel(null, AllData::getFuncTreeBottomUp);
            flameSearchChart = new FlameSearchChart(null, AllData::getFuncTreeFlameChart);
        }
        setBorder(JBUI.Borders.empty(5, 8));
        otherThreadTab.addTab("Summary", new OtherThreadSummaryPanel());
        otherThreadTab.addTab("Top Down", topBottomPanel);
        otherThreadTab.addTab("Flame Chart", flameSearchChart);
        otherThreadTab.addTab("Bottom Up", bottomUpPanel);
        add(otherThreadTab);
    }

}
