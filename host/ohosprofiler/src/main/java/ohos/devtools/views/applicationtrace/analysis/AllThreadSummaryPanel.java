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
import ohos.devtools.views.applicationtrace.util.TimeUtils;
import ohos.devtools.views.trace.EventPanel;

import java.util.List;

/**
 * app data all thread summary panel
 *
 * @since 2021/5/20 18:00
 */
public class AllThreadSummaryPanel extends EventPanel {
    private JBLabel timeRange = new JBLabel();
    private JBLabel duration = new JBLabel();

    /**
     * AllThreadSummaryPanel structure function
     */
    public AllThreadSummaryPanel() {
        setLayout(new MigLayout("inset 0"));
        setData();
    }

    private void setData() {
        removeAll();
        setBorder(JBUI.Borders.empty(15, 15));
        add(new JBLabel("Time Range"));
        add(timeRange, "growx,pushx,wrap");
        add(new JBLabel("Duration"));
        add(duration, "growx,pushx");
        setBackground(JBColor.background().brighter());
    }

    @Override
    public void change(long startNS, long endNS, long scale) {
        timeRange.setText(TimeUtils.getTimeFormatString(startNS) + "~" + TimeUtils.getTimeFormatString(endNS));
        duration.setText(TimeUtils.getTimeWithUnit(endNS - startNS));
    }

    @Override
    public void change(long startNS, long endNS, List<Integer> threadIds) {
    }
}
