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

package ohos.devtools.views.layout.chartview.memory;

import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import ohos.devtools.datasources.utils.monitorconfig.service.MonitorConfigManager;
import ohos.devtools.views.common.UtConstant;
import ohos.devtools.views.layout.chartview.MonitorItemDetail;
import ohos.devtools.views.layout.chartview.observer.MemoryChartObserver;

import javax.swing.AbstractButton;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MemoryPopupMenu
 */
class MemoryItemPopupMenu {
    /**
     * Number of memory monitoring items
     */
    private static final int CHECK_BOX_AMOUNT = 6;
    private static final int H_GAP = 15;
    private static final int V_GAP = 13;
    private static final int ITEM_WIDTH = 140;
    private static final int ITEM_HEIGHT = 20;
    private static final int MEMORY_ITEM_PANEL_WIDTH = 160;
    private static final int MEMORY_ITEM_PANEL_HEIGHT = 275;
    private static final int POPUP_OFFSET = 15;
    private static final String MEMORY_NAME = "Memory";
    private final long sessionId;
    private final MemoryChartObserver chartObserver;
    private final JBPopupMenu popupMenu = new JBPopupMenu();
    private final JBLabel memoryLabel = new JBLabel(MEMORY_NAME);
    private final JBCheckBox checkBoxSelectAll = new JBCheckBox("Select all");
    private final JBCheckBox checkBoxMemoryJava = new JBCheckBox(MonitorItemDetail.MEM_JAVA.getName());
    private final JBCheckBox checkBoxGpuMemoryNative = new JBCheckBox(MonitorItemDetail.MEM_NATIVE.getName());
    private final JBCheckBox checkBoxGraphics = new JBCheckBox(MonitorItemDetail.MEM_GRAPHICS.getName());
    private final JBCheckBox checkBoxStack = new JBCheckBox(MonitorItemDetail.MEM_STACK.getName());
    private final JBCheckBox checkBoxCode = new JBCheckBox(MonitorItemDetail.MEM_CODE.getName());
    private final JBCheckBox checkBoxOthers = new JBCheckBox(MonitorItemDetail.MEM_OTHERS.getName());
    private final ArrayList<JBCheckBox> checkBoxes = new ArrayList<>();
    private Map<String, LinkedList<String>> configMap;

    /**
     * MemoryPopupMenu constructor
     *
     * @param sessionId sessionId
     * @param chartObserver MemoryChartObserver
     */
    MemoryItemPopupMenu(long sessionId, MemoryChartObserver chartObserver) {
        this.sessionId = sessionId;
        this.chartObserver = chartObserver;
        initCheckBoxes();
        initCheckItems();
        initSelectedItems();
        initListener();
    }

    private void initCheckBoxes() {
        checkBoxSelectAll.setSelected(true);
        checkBoxSelectAll.setEnabled(false);
        checkBoxes.add(checkBoxMemoryJava);
        checkBoxes.add(checkBoxGpuMemoryNative);
        checkBoxGpuMemoryNative.setName(UtConstant.UT_MEMORY_ITEM_POPUP_NATIVE);
        checkBoxes.add(checkBoxGraphics);
        checkBoxStack.setName(UtConstant.UT_MEMORY_ITEM_POPUP_STACK);
        checkBoxes.add(checkBoxStack);
        checkBoxes.add(checkBoxCode);
        checkBoxes.add(checkBoxOthers);
    }

    private void initCheckItems() {
        JBPanel checkItemPanel = new JBPanel(new FlowLayout(FlowLayout.LEADING, H_GAP, V_GAP));
        checkItemPanel.add(memoryLabel);
        memoryLabel.setPreferredSize(new Dimension(ITEM_WIDTH, ITEM_HEIGHT));
        checkBoxSelectAll.setPreferredSize(new Dimension(ITEM_WIDTH, ITEM_HEIGHT));
        checkItemPanel.add(checkBoxSelectAll);
        for (JBCheckBox checkBox : checkBoxes) {
            checkBox.setPreferredSize(new Dimension(ITEM_WIDTH, ITEM_HEIGHT));
            checkItemPanel.add(checkBox);
        }
        checkItemPanel.setPreferredSize(new Dimension(MEMORY_ITEM_PANEL_WIDTH, MEMORY_ITEM_PANEL_HEIGHT));
        popupMenu.add(checkItemPanel);
    }

    private void initSelectedItems() {
        configMap = MonitorConfigManager.dataMap.get(sessionId);
        if (configMap == null || configMap.get(MEMORY_NAME) == null) {
            configMap = initFullItems();
            MonitorConfigManager.dataMap.put(sessionId, configMap);
        }

        for (String str : configMap.get(MEMORY_NAME)) {
            for (JBCheckBox jCheckBox : checkBoxes) {
                if (jCheckBox.getText().equals(str)) {
                    jCheckBox.setSelected(true);
                }
            }
        }
    }

    private Map<String, LinkedList<String>> initFullItems() {
        LinkedList<String> items = new LinkedList<>();
        items.add(MonitorItemDetail.MEM_JAVA.getName());
        items.add(MonitorItemDetail.MEM_NATIVE.getName());
        items.add(MonitorItemDetail.MEM_GRAPHICS.getName());
        items.add(MonitorItemDetail.MEM_STACK.getName());
        items.add(MonitorItemDetail.MEM_CODE.getName());
        items.add(MonitorItemDetail.MEM_OTHERS.getName());
        Map<String, LinkedList<String>> memoryMap = new HashMap<>();
        memoryMap.put(MEMORY_NAME, items);
        return memoryMap;
    }

    private void initListener() {
        checkBoxSelectAll.addItemListener(event -> {
            if (checkBoxSelectAll.isSelected()) {
                for (JBCheckBox checkBox : checkBoxes) {
                    checkBox.setSelected(true);
                }
            }
        });
        memoryAddItemListener(checkBoxMemoryJava);
        memoryAddItemListener(checkBoxGpuMemoryNative);
        memoryAddItemListener(checkBoxGraphics);
        memoryAddItemListener(checkBoxStack);
        memoryAddItemListener(checkBoxCode);
        memoryAddItemListener(checkBoxOthers);
    }

    private void memoryAddItemListener(JBCheckBox checkBox) {
        checkBox.addItemListener(event -> filterItemList());
    }

    private void filterItemList() {
        List<JBCheckBox> selectedList =
            checkBoxes.stream().filter(AbstractButton::isSelected).collect(Collectors.toList());
        // Increase the judgment of the selected number of Check Boxes
        if (selectedList.size() < CHECK_BOX_AMOUNT) {
            // Select All uncheck the state and set it to non-clickable state
            checkBoxSelectAll.setSelected(false);
            checkBoxSelectAll.setEnabled(true);
        } else {
            // Select All Check and set to clickable state
            checkBoxSelectAll.setSelected(true);
            checkBoxSelectAll.setEnabled(false);
        }
        LinkedList<String> memoryFlushed = new LinkedList<>();
        for (JBCheckBox checkBox : selectedList) {
            // When the checked quantity is 1, the last indicator item cannot be clicked
            if (selectedList.size() == 1) {
                checkBox.setEnabled(false);
            } else {
                checkBox.setEnabled(true);
            }
            memoryFlushed.add(checkBox.getText());
        }
        configMap.remove(MEMORY_NAME);
        configMap.put(MEMORY_NAME, memoryFlushed);
        MonitorConfigManager.dataMap.put(sessionId, configMap);
        // Refresh chart manually
        chartObserver.refreshManually();
    }

    /**
     * show memory items
     *
     * @param detailCfgBtn detailCfgBtn
     * @param event event
     */
    void showMemoryItems(JBLabel detailCfgBtn, MouseEvent event) {
        popupMenu.show(detailCfgBtn, event.getX(), event.getY() + POPUP_OFFSET);
    }
}
