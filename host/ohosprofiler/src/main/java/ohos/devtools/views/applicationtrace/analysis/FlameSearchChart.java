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

import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SideBorder;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.applicationtrace.listener.IAllThreadDataListener;
import ohos.devtools.views.applicationtrace.listener.IOtherThreadDataListener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;

/**
 * FlameSearchChart
 *
 * @since 2021/04/22 12:25
 */
public class FlameSearchChart extends JBPanel {
    private JBScrollPane scrollPane;
    private FlameChart flameChart;
    private JBTextField search = new JBTextField();
    private String currentSearchText = "";

    /**
     * structure
     */
    public FlameSearchChart() {
        this(null, null);
    }

    /**
     * structure with listener
     *
     * @param iAllThreadDataListener all thread listener
     * @param iOtherThreadDataListener other thread listener
     */
    public FlameSearchChart(IAllThreadDataListener iAllThreadDataListener,
        IOtherThreadDataListener iOtherThreadDataListener) {
        flameChart = new FlameChart(iAllThreadDataListener, iOtherThreadDataListener);
        init();
    }

    private void init() {
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                currentSearchText = search.getText();
                flameChart.setCurrentSearchText(currentSearchText);
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                currentSearchText = search.getText();
                if (currentSearchText.isEmpty()) {
                    flameChart.resetAllNode();
                } else {
                    flameChart.setCurrentSearchText(currentSearchText);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
            }
        });
        scrollPane = new JBScrollPane(flameChart, JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
            JBScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(IdeBorderFactory.createBorder(SideBorder.NONE));
        setLayout(new MigLayout("inset 10", "[grow,fill]", "[][grow,fill]"));
        add(search, "wrap");
        add(scrollPane);
    }

    /**
     * fresh current data
     *
     * @param datasource datasource
     * @param dur dur
     */
    public void freshData(List<DefaultMutableTreeNode> datasource, long dur) {
        flameChart.setAllNode(datasource, dur);
    }
}
