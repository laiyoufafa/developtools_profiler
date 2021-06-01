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

import com.intellij.openapi.ui.ComboBox;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.chart.ProfilerTimeline;
import ohos.devtools.views.layout.swing.LevelTablePanel;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static ohos.devtools.views.common.ColorConstants.ITEM_PANEL;
import static ohos.devtools.views.common.ViewConstants.LABEL_DEFAULT_HEIGHT;
import static ohos.devtools.views.common.ViewConstants.LABEL_DEFAULT_WIDTH;

/**
 * @Description 二级界面标题面板
 * @Date 2021/2/2 19:02
 **/
public class MemoryStageTitleView extends AbsItemTitleView {
    private final MemoryStageView memoryStageView;

    /**
     * @param bottomPanel     最底层面板
     * @param name            二级页面的标题名称
     * @param memoryStageView 二级页面
     */
    public MemoryStageTitleView(ProfilerChartsView bottomPanel, String name, MemoryStageView memoryStageView) {
        super(bottomPanel, name);
        this.memoryStageView = memoryStageView;
        initBackTitle();
        initCheckBox(name);
    }

    /**
     * 初始化标题
     */
    private void initBackTitle() {
        JButton jButtonRun = new JButton(
            new ImageIcon(MemoryStageTitleView.class.getClassLoader().getResource("images/backtrack.png")));
        jButtonRun.setPreferredSize(new Dimension(LABEL_DEFAULT_HEIGHT, LABEL_DEFAULT_HEIGHT));
        jButtonRun.setBorder(null);
        jButtonRun.setBackground(ITEM_PANEL);
        jButtonRun.setOpaque(false);
        this.add(jButtonRun);
        jButtonRun.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                bottomPanel.getObserver().detach(memoryStageView.getChartObserver());
                bottomPanel.removeStageView(memoryStageView);
                bottomPanel.resumeMonitorItemView();
                bottomPanel.getTaskScenePanelChart().getjButtonBottom().setIcon(new ImageIcon(
                    MemoryStageTitleView.class.getClassLoader().getResource("images/button_bottom_bar_grey.png")));
                bottomPanel.setFlagDown(false);
                bottomPanel.getTimeline().removeTablePanel();
                hideLeftTable();
            }
        });
    }

    private void initCheckBox(String name) {
        ComboBox<String> jComboBox = new ComboBox<>();
        jComboBox.setBorder(null);
        jComboBox.addItem(name);
        jComboBox.setBackground(ITEM_PANEL);
        jComboBox.setBounds(LayoutConstants.CHOOSE_HEIGHT, LayoutConstants.JLABEL_SIZE, LABEL_DEFAULT_WIDTH,
            LABEL_DEFAULT_HEIGHT);
        this.add(jComboBox);
    }

    /**
     * 隐藏右边列表
     */
    private void hideLeftTable() {
        ProfilerTimeline timeline = bottomPanel.getTimeline();
        if (timeline != null) {
            LevelTablePanel levelTablePanel = timeline.getLevelTablePanel();
            if (levelTablePanel != null) {
                JPanel suspensionTable = levelTablePanel.getSuspensionTable();
                if (suspensionTable != null) {
                    suspensionTable.setVisible(false);
                }
            }
        }
    }
}
