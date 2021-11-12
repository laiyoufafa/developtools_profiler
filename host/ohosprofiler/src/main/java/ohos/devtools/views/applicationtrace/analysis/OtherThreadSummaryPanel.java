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
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.applicationtrace.DataPanel;
import ohos.devtools.views.applicationtrace.util.TimeUtils;
import ohos.devtools.views.trace.EventPanel;
import ohos.devtools.views.trace.ExpandPanel;
import ohos.devtools.views.trace.TracePanel;

import java.util.List;

/**
 * The OtherThreadSummaryPanel
 *
 * @since 2021/04/22 12:25
 */
public class OtherThreadSummaryPanel extends EventPanel {
    private EventTable topFuncTable;
    private ExpandPanel topFuncPanel;
    private JBLabel timeRange = new JBLabel();
    private JBLabel duration = new JBLabel();
    private JBLabel thread = new JBLabel();
    private JBLabel idLable = new JBLabel("ID");
    private JBLabel id = new JBLabel();

    /**
     * structure
     */
    public OtherThreadSummaryPanel() {
        setLayout(new MigLayout("inset 0"));
        setBackground(JBColor.background().brighter());
        setData();
    }

    private void setData() {
        removeAll();
        setBorder(JBUI.Borders.empty(15, 15));
        add(new JBLabel("Time Range"));
        add(timeRange, "growx,pushx,wrap");
        add(new JBLabel("Duration"));
        add(duration, "growx,pushx,wrap");
        add(new JBLabel("Data Type"));
        thread.setText("Thread");
        add(thread, "growx,pushx,wrap");
        add(idLable);
        add(id, "growx,pushx,wrap");
        if (DataPanel.analysisEnum.equals(AnalysisEnum.APP)) {
            StateTable stateTable = new StateTable();
            ExpandPanel stateTablePanel = new ExpandPanel("States");
            stateTablePanel.setBackground(JBColor.background().brighter());
            stateTablePanel.getContent().add(stateTable, "pushx,growx");
            add(stateTablePanel, "span 2,growx,growy,wrap");
        }
        topFuncTable = new EventTable(title -> {
            if (topFuncPanel != null) {
                topFuncPanel.setVisible(true);
                if (title == null) {
                    remove(topFuncPanel);
                } else {
                    topFuncPanel.setTitle(title);
                    add(topFuncPanel, "span 2,growx,growy,wrap");
                }
            }
        });
        topFuncPanel = new ExpandPanel("");
        topFuncPanel.setBackground(JBColor.background().brighter());
        topFuncPanel.getContent().add(topFuncTable, "pushx,growx");
        add(topFuncPanel, "span 2,growx,growy,wrap");
    }

    @Override
    public void change(long startNS, long endNS, List<Integer> threadIds) {
        if (threadIds.size() == 1) {
            id.setVisible(true);
            idLable.setVisible(true);
            id.setText(threadIds.get(0).toString());
            topFuncTable.getData(startNS, endNS, threadIds);
            topFuncPanel.setVisible(true);
        } else {
            id.setVisible(false);
            idLable.setVisible(false);
            topFuncPanel.setVisible(false);
        }
        timeRange.setText(TimeUtils.getTimeFormatString(startNS) + "~" + TimeUtils.getTimeFormatString(endNS));
        duration.setText(TimeUtils.getTimeWithUnit(endNS - startNS));
    }

    @Override
    public void change(long startNS, long endNS, long scale) {
        if (TracePanel.rangeStartNS == null && TracePanel.rangeEndNS == null) {
            change(startNS, endNS, TracePanel.currentSelectThreadIds);
        }
    }

}
