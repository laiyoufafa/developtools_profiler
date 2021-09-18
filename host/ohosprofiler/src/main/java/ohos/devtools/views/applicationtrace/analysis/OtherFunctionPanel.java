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
import ohos.devtools.views.applicationtrace.bean.Func;
import ohos.devtools.views.trace.EventDispatcher;

/**
 * The OtherFunctionPanel
 *
 * @date 2021/04/22 12:25
 */
public class OtherFunctionPanel extends JBPanel {
    private JBTabbedPane otherFunctionTab;
    private TopBottomPanel topBottomPanel;
    private TopBottomPanel bottomUpPanel;
    private FlameSearchChart flameSearchChart;

    /**
     * structure
     */
    public OtherFunctionPanel() {
        setLayout(new MigLayout("inset 0", "[grow,fill]", "[grow,fill]"));
        otherFunctionTab = new JBTabbedPane();
        otherFunctionTab.setBackground(JBColor.background().darker());
        setBorder(JBUI.Borders.empty(5, 8));
        topBottomPanel = new TopBottomPanel();
        bottomUpPanel = new TopBottomPanel();
        flameSearchChart = new FlameSearchChart();
        otherFunctionTab.addTab("Summary", new OtherFunctionSummaryPanel());
        otherFunctionTab.addTab("Top Down", topBottomPanel);
        otherFunctionTab.addTab("Flame Chart", flameSearchChart);
        otherFunctionTab.addTab("Bottom Up", bottomUpPanel);
        add(otherFunctionTab);
        EventDispatcher.addClickListener(node -> {
            if (node instanceof Func) {
                Func func = (Func) node;
                topBottomPanel.freshTreeData(AllData.getFuncTreeByFuncTopDown(func));
                bottomUpPanel.freshTreeData(AllData.getFuncTreeByFuncBottomUp(func));
                flameSearchChart.freshData(AllData.getFuncTreeFlameChart(func), func.getDur());
            }
        });
    }
}
