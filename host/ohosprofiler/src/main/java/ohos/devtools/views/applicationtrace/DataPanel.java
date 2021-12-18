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

package ohos.devtools.views.applicationtrace;

import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SideBorder;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBInsets;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.applicationtrace.analysis.AllThreadPanel;
import ohos.devtools.views.applicationtrace.analysis.AnalysisEnum;
import ohos.devtools.views.applicationtrace.analysis.OtherFunctionPanel;
import ohos.devtools.views.applicationtrace.analysis.OtherThreadPanel;
import ohos.devtools.views.applicationtrace.bean.Func;
import ohos.devtools.views.perftrace.PerfData;
import ohos.devtools.views.perftrace.bean.PrefFunc;
import ohos.devtools.views.trace.EventDispatcher;
import ohos.devtools.views.trace.EventPanel;
import ohos.devtools.views.trace.TracePanel;

import java.util.ArrayList;
import java.util.List;

/**
 * DataPanel
 *
 * @since 2021/5/13 13:22
 */
public class DataPanel extends EventPanel {
    /**
     * enum analysis
     */
    public static AnalysisEnum analysisEnum;

    private JBTabbedPane analysisTab;
    private List<Integer> currentTids = new ArrayList<>();
    private OtherFunctionPanel otherFunctionPanel;
    private OtherThreadPanel otherThreadPanel;

    /**
     * construction
     *
     * @param analysisEnum analysisEnum
     */
    public DataPanel(AnalysisEnum analysisEnum) {
        setLayout(new MigLayout("inset 0", "[grow,fill]", "[grow,fill]"));
        analysisTab = new JBTabbedPane();
        analysisTab.setBorder(IdeBorderFactory.createBorder(SideBorder.NONE));
        analysisTab.setTabComponentInsets(JBInsets.create(0, 0));
        this.analysisEnum = analysisEnum;
        initComp();
    }

    private void initComp() {
        removeAll();
        analysisTab.removeAll();
        analysisTab.addTab("Analysis", new JBPanel<>());
        analysisTab.addTab("All Threads", new AllThreadPanel(analysisEnum));
        otherThreadPanel = new OtherThreadPanel(analysisEnum);
        analysisTab.setEnabledAt(0, false);
        analysisTab.setSelectedIndex(1);
        otherFunctionPanel = new OtherFunctionPanel();
        add(analysisTab, "growx,pushx");
        EventDispatcher.addClickListener(node -> {
            String tabName = "";
            if (node instanceof Func) {
                tabName = ((Func) node).getFuncName();
            } else {
                if (node instanceof PrefFunc) {
                    tabName = ((PrefFunc) node).getFuncName();
                }
            }
            if (tabName != null && tabName.length() > 20) {
                tabName = tabName.substring(0, 20) + "...";
            }
            if (analysisTab.getTabCount() == 3) {
                analysisTab.remove(analysisTab.getTabCount() - 1);
            }
            analysisTab.addTab(tabName, otherFunctionPanel);
            analysisTab.setSelectedIndex(analysisTab.getTabCount() - 1);
        });
    }

    @Override
    public void change(long startNS, long endNS, List<Integer> threadIds) {
        if (threadIds.size() == 0) {
            if (analysisTab.getTabCount() == 3) {
                analysisTab.remove(analysisTab.getTabCount() - 1);
            }
        } else {
            if (analysisTab.getTabCount() == 3) {
                if (analysisTab.getComponentAt(analysisTab.getTabCount() - 1) instanceof OtherFunctionPanel) {
                    analysisTab.remove(analysisTab.getTabCount() - 1);
                    analysisTab.addTab(createTabName(threadIds), otherThreadPanel);
                } else {
                    if (!threadIds.equals(currentTids)) {
                        analysisTab.setTitleAt(analysisTab.getTabCount() - 1, createTabName(threadIds));
                    }
                }
            } else {
                analysisTab.addTab(createTabName(threadIds), otherThreadPanel);
            }
        }
        analysisTab.setSelectedIndex(analysisTab.getTabCount() - 1);
    }

    @Override
    public void change(long startNS, long endNS, long scale) {
        if (analysisTab.getTabCount() != 3) {
            change(startNS, endNS, TracePanel.currentSelectThreadIds);
        }
    }

    private String createTabName(List<Integer> ids) {
        if (ids.size() == 1 && ids.get(0) != null) {
            String name = null;
            if (analysisEnum.equals(AnalysisEnum.APP)) {
                if (AllData.threadNames != null) {
                    name = AllData.threadNames.get(ids.get(0));
                }
            } else {
                if (PerfData.getThreadNames() != null) {
                    name = PerfData.getThreadNames().get(ids.get(0));
                }
            }
            if (name != null && name.length() > 20) {
                name = name.substring(0, 20) + "...";
            }
            return name == null ? "" : name;
        } else {
            return ids.size() + " Threads";
        }
    }
}
