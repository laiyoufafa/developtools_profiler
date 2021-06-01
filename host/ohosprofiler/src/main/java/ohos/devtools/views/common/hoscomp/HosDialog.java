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

package ohos.devtools.views.common.hoscomp;

import ohos.devtools.datasources.utils.monitorconfig.service.MonitorConfigManager;
import ohos.devtools.datasources.utils.session.entity.SessionInfo;
import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;
import ohos.devtools.views.layout.chartview.observer.ProfilerChartsViewObserver;
import ohos.devtools.views.layout.swing.SampleDialogWrapper;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Memory指标项添加复选框
 *
 * @version 1.0
 * @date 2021/03/24 09:33
 **/
public class HosDialog {
    /**
     * sessionInfo信息
     */
    private SessionInfo sessionInfo;
    private JPanel jPanel;
    /**
     * Items项复选框容器
     */
    private JPanel jCheckPanel = new JPanel(null);
    /**
     * 复选框存放集合
     */
    private ArrayList<JCheckBox> jCheckBoxes = new ArrayList<>();

    private ProfilerChartsView profilerView;

    /**
     * Items页面复选框内容
     */
    private JLabel jLabelMemory = new JLabel("Memory");
    private JCheckBox checkBoxSelectAll = new JCheckBox("Select All");
    private JCheckBox checkBoxMemoryJava = new JCheckBox("Java");
    private JCheckBox checkBoxGpuMemoryNative = new JCheckBox("Native");
    private JCheckBox checkBoxGraphics = new JCheckBox("Graphics");
    private JCheckBox checkBoxStack = new JCheckBox("Stack");
    private JCheckBox checkBoxCode = new JCheckBox("Code");
    private JCheckBox checkBoxOthers = new JCheckBox("Others");

    /**
     * HosDialog Constructor
     *
     * @param sessionId session Id
     * @param profilerView profiler View
     */
    public HosDialog(long sessionId, ProfilerChartsView profilerView) {
        addItem();
        this.profilerView = profilerView;
        jCheckBoxes.add(checkBoxSelectAll);
        jCheckBoxes.add(checkBoxMemoryJava);
        jCheckBoxes.add(checkBoxGpuMemoryNative);
        jCheckBoxes.add(checkBoxGraphics);
        jCheckBoxes.add(checkBoxStack);
        jCheckBoxes.add(checkBoxCode);
        jCheckBoxes.add(checkBoxOthers);
        // 设置对话框的宽高
        jCheckPanel
            .setPreferredSize(new Dimension(LayoutConstants.MEMORY_WIDTH_SIZE, LayoutConstants.MEMORY_HEIGHT_SIZE));
        ConcurrentHashMap<Long, Map<String, LinkedList<String>>> configData = MonitorConfigManager.dataMap;
        Map<String, LinkedList<String>> configMap = configData.get(sessionId);

        LinkedList<String> configList = null;
        if (configMap != null) {
            configList = configMap.get("Memory");
        }
        // 让初始选中的Iterm默认选中
        for (String str : configList) {
            for (int index = 0; index < jCheckBoxes.size(); index++) {
                if (jCheckBoxes.get(index).getText().equals(str)) {
                    jCheckBoxes.get(index).setSelected(true);
                    continue;
                }
            }
        }
        // 弹框
        SampleDialogWrapper sampleDialog = new SampleDialogWrapper("Select Add Items", jCheckPanel);
        boolean flag = sampleDialog.showAndGet();
        if (flag) {
            // 获取选中的指标项
            List<JCheckBox> collect = HosDialog.this.jCheckBoxes.stream().filter(jCheckBox -> jCheckBox.isSelected())
                .collect(Collectors.toList());
            if (collect.size() == 0) {
                new SampleDialogWrapper("Reminder Msg", "Please Select Items !").show();
                return;
            }
            // 便利选中项，构建Memory的value
            LinkedList<String> memoryFlushed = new LinkedList<>();
            for (JCheckBox jc : collect) {
                memoryFlushed.add(jc.getText());
            }
            configMap.remove("Memory");
            configMap.put("Memory", memoryFlushed);
            MonitorConfigManager.dataMap.put(sessionId, configMap);
            ProfilerChartsViewObserver bottomPanel = ProfilerChartsView.sessionMap.get(sessionId).getObserver();
            ChartDataRange range = bottomPanel.getStandard().getDisplayRange();
            bottomPanel.notifyRefresh(range.getStartTime(), range.getEndTime());
            profilerView.setAddItemFlag(false);
        } else {
            profilerView.setAddItemFlag(false);
        }
    }

    private void addItem() {
        jLabelMemory.setBounds(LayoutConstants.MEMORY_X, LayoutConstants.MEMORY_Y, LayoutConstants.MEMORY_WIDTH,
            LayoutConstants.MEMORY_HEIGHT);
        checkBoxSelectAll
            .setBounds(LayoutConstants.SELECT_ALL_X, LayoutConstants.SELECT_ALL_Y, LayoutConstants.SELECT_ALL_WIDTH,
                LayoutConstants.SELECT_ALL_HEIGHT);
        checkBoxMemoryJava.setBounds(LayoutConstants.JAVA_X, LayoutConstants.JAVA_Y, LayoutConstants.JAVA_WIDTH,
            LayoutConstants.JAVA_HEIGHT);
        checkBoxGpuMemoryNative
            .setBounds(LayoutConstants.NATIVE_X, LayoutConstants.NATIVE_Y, LayoutConstants.NATIVE_WIDTH,
                LayoutConstants.NATIVE_HEIGHT);
        checkBoxGraphics
            .setBounds(LayoutConstants.GRAPHICS_X, LayoutConstants.GRAPHICS_Y, LayoutConstants.GRAPHICS_WIDTH,
                LayoutConstants.GRAPHICS_HEIGHT);
        checkBoxStack.setBounds(LayoutConstants.STACK_X, LayoutConstants.STACK_Y, LayoutConstants.STACK_WIDTH,
            LayoutConstants.STACK_HEIGHT);
        checkBoxCode.setBounds(LayoutConstants.CODE_X, LayoutConstants.CODE_Y, LayoutConstants.CODE_WIDTH,
            LayoutConstants.CODE_HEIGHT);
        checkBoxOthers.setBounds(LayoutConstants.OTHERS_X, LayoutConstants.OTHERS_Y, LayoutConstants.OTHERS_WIDTH,
            LayoutConstants.OTHERS_HEIGHT);
        jCheckPanel.add(jLabelMemory);
        jCheckPanel.add(checkBoxSelectAll);
        jCheckPanel.add(checkBoxMemoryJava);
        jCheckPanel.add(checkBoxGpuMemoryNative);
        jCheckPanel.add(checkBoxGraphics);
        jCheckPanel.add(checkBoxStack);
        jCheckPanel.add(checkBoxCode);
        jCheckPanel.add(checkBoxOthers);
        checkBoxSelectAll.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent exception) {
                if (HosDialog.this.checkBoxSelectAll.isSelected()) {
                    HosDialog.this.checkBoxMemoryJava.setSelected(true);
                    HosDialog.this.checkBoxGpuMemoryNative.setSelected(true);
                    HosDialog.this.checkBoxGraphics.setSelected(true);
                    HosDialog.this.checkBoxStack.setSelected(true);
                    HosDialog.this.checkBoxCode.setSelected(true);
                    HosDialog.this.checkBoxOthers.setSelected(true);
                } else {
                    HosDialog.this.checkBoxMemoryJava.setSelected(false);
                    HosDialog.this.checkBoxGpuMemoryNative.setSelected(false);
                    HosDialog.this.checkBoxGraphics.setSelected(false);
                    HosDialog.this.checkBoxStack.setSelected(false);
                    HosDialog.this.checkBoxCode.setSelected(false);
                    HosDialog.this.checkBoxOthers.setSelected(false);
                }
            }
        });
    }

    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    public void setSessionInfo(SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    public JPanel getjPanel() {
        return jPanel;
    }

    public void setjPanel(JPanel jPanel) {
        this.jPanel = jPanel;
    }

}
