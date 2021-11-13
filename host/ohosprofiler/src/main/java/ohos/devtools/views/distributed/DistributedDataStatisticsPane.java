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

package ohos.devtools.views.distributed;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import net.miginfocom.swing.MigLayout;
import ohos.devtools.views.distributed.statistics.CpuStrategy;
import ohos.devtools.views.distributed.statistics.MemAggStrategy;
import ohos.devtools.views.distributed.statistics.MemStrategy;
import ohos.devtools.views.distributed.statistics.MetadataStrategy;
import ohos.devtools.views.distributed.statistics.Strategy;
import ohos.devtools.views.distributed.statistics.TraceStatsStrategy;
import ohos.devtools.views.distributed.statistics.TraceTaskStrategy;
import ohos.devtools.views.distributed.util.DistributedCache;

import javax.swing.JButton;
import javax.swing.JComboBox;
import java.awt.Color;
import java.awt.Dimension;

/**
 * DistributedDataStatisticsPane
 *
 * @since 2021/8/26 15:10
 */
public class DistributedDataStatisticsPane extends JBPanel {
    private StartButton startBt = new StartButton();
    private JComboBox<TypeItem> deviceSelect = new JComboBox() {
        {
            addItem(DistributedCache.getDistribuetedParams().getDeviceNameA());
            addItem(DistributedCache.getDistribuetedParams().getDeviceNameB());
        }
    };
    private JComboBox<TypeItem> typeSelect = new JComboBox() {
        {
            addItem(new TypeItem("trace_cpu", new CpuStrategy()));
            addItem(new TypeItem("trace_mem", new MemStrategy()));
            addItem(new TypeItem("trace_mem_unagg", new MemAggStrategy()));
            addItem(new TypeItem("trace_task_names", new TraceTaskStrategy()));
            addItem(new TypeItem("trace_stats", new TraceStatsStrategy()));
            addItem(new TypeItem("trace_metadata", new MetadataStrategy()));
        }
    };
    private JBScrollPane scrollPane = new JBScrollPane();
    private JBTextArea resultTextArea = new JBTextArea();

    /**
     * DistributedDataStatisticsPane
     */
    public DistributedDataStatisticsPane() {
        setLayout(new MigLayout("insets 0"));
        deviceSelect.setSelectedIndex(0);
        typeSelect.setSelectedIndex(0);
        resultTextArea.setEditable(false);
        scrollPane.setViewportView(resultTextArea);
        scrollPane.setBackground(JBColor.background().darker());
        add(new JBLabel("Analysis："), "split 4, h 30!");
        add(deviceSelect, "h 30!");
        add(typeSelect, "h 30!");
        add(startBt, "h 30!, wrap");
        add(scrollPane, "push,grow");
    }

    class StartButton extends JButton {
        /**
         * StartButton
         */
        public StartButton() {
            setBorderPainted(false);
            setContentAreaFilled(false);
            setBackground(new Color(0, 0, 0, 0));
            setMaximumSize(new Dimension(AllIcons.Process.ProgressResume.getIconWidth(),
                AllIcons.Process.ProgressResume.getIconHeight()));
            setIcon(AllIcons.Process.ProgressResume);
            setFocusable(true);
            addActionListener(event -> {
                if (typeSelect.getSelectedItem() instanceof TypeItem) {
                    TypeItem item = (TypeItem) typeSelect.getSelectedItem();
                    if (deviceSelect.getSelectedIndex() == 0) {
                        resultTextArea.setText(item.getValue().getQueryResult("A"));
                    } else {
                        resultTextArea.setText(item.getValue().getQueryResult("B"));
                    }
                }
            });
        }
    }

    class TypeItem {
        private String name;
        private Strategy value;

        public TypeItem(String name, Strategy value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Strategy getValue() {
            return value;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
