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

import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import ohos.devtools.datasources.utils.plugin.service.PlugManager;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PerformanceIndexPopupMenu
 */
public class PerformanceIndexPopupMenu {
    private static final int OTHER_ITEM = 2;
    private static final int HORIZONTAL_GAP = 15;
    private static final int VERTICAL_GAP = 13;
    private static final int ITEM_WIDTH = 140;
    private static final int ITEM_HEIGHT = 20;
    private static final int MEMORY_ITEM_PANEL_WIDTH = 160;
    private static final int MARGIN_RIGHT = 160;
    private static final int MEMORY_ITEM_HEIGHT = 35;
    private static final int POPUP_OFFSET = 15;
    private static final String ITEM = "item";
    private JBLabel titleLabel = new JBLabel("DataSource");
    private JBCheckBox checkBoxSelectAll = new JBCheckBox("Select all");
    private final LinkedList<String> ITEMS;
    private final JBPopupMenu popupMenu;
    private final ArrayList<JBCheckBox> allCheckBoxes;
    private final List<MonitorItemView> cacheItemList;
    private final Map<String, LinkedList<String>> map;
    private final ProfilerChartsView profilerView;
    private final List<ProfilerMonitorItem> profilerMonitorItemMap;

    /**
     * PerformanceIndexPopupMenu constructor
     *
     * @param profilerView profilerView
     */
    public PerformanceIndexPopupMenu(ProfilerChartsView profilerView, long sessionId) {
        this.profilerView = profilerView;
        ITEMS = new LinkedList<>();
        popupMenu = new JBPopupMenu();
        allCheckBoxes = new ArrayList<>();
        cacheItemList = new ArrayList<>();
        map = new HashMap<>();
        profilerMonitorItemMap = PlugManager.getInstance().getProfilerMonitorItemList(sessionId);
        addCheckBoxes();
        addCheckItemsToPanel();
        initItemPreferredSize();
        initDefaultCheck();
        initListener();
    }

    private void initDefaultCheck() {
        for (JBCheckBox box : allCheckBoxes) {
            box.setSelected(true);
        }
    }

    private void addCheckBoxes() {
        for (ProfilerMonitorItem item : profilerMonitorItemMap) {
            ITEMS.add(item.getName());
        }
        map.put(ITEM, ITEMS);
        for (String str : ITEMS) {
            JBCheckBox checkBox = new JBCheckBox(str);
            allCheckBoxes.add(checkBox);
        }
    }

    private void addCheckItemsToPanel() {
        JBPanel panel = new JBPanel(new FlowLayout(FlowLayout.LEADING, HORIZONTAL_GAP, VERTICAL_GAP));
        panel.add(titleLabel);
        panel.add(checkBoxSelectAll);
        for (JBCheckBox box : allCheckBoxes) {
            panel.add(box);
        }
        panel.setPreferredSize(
            new Dimension(MEMORY_ITEM_PANEL_WIDTH, (allCheckBoxes.size() + OTHER_ITEM) * MEMORY_ITEM_HEIGHT));
        popupMenu.add(panel);
    }

    private void initItemPreferredSize() {
        titleLabel.setPreferredSize(new Dimension(ITEM_WIDTH, ITEM_HEIGHT));
        checkBoxSelectAll.setPreferredSize(new Dimension(ITEM_WIDTH, ITEM_HEIGHT));
        for (JBCheckBox box : allCheckBoxes) {
            box.setPreferredSize(new Dimension(ITEM_WIDTH, ITEM_HEIGHT));
        }
    }

    private void initListener() {
        checkBoxSelectAll.addItemListener(event -> {
            if (checkBoxSelectAll.isSelected()) {
                for (JBCheckBox box : allCheckBoxes) {
                    box.setSelected(true);
                }
            } else {
                for (JBCheckBox box : allCheckBoxes) {
                    box.setSelected(false);
                }
            }
        });
        ItemListener itemListener = new ItemListener() {
            JCheckBox checkBox;

            /**
             * itemStateChanged
             *
             * @param event event
             */
            public void itemStateChanged(ItemEvent event) {
                Object sourceObject = event.getSource();
                if (sourceObject instanceof JCheckBox) {
                    checkBox = (JCheckBox) sourceObject;
                    String itemStr = checkBox.getText();
                    filterItemList();
                    if (checkBox.isSelected()) {
                        addItemView(itemStr);
                    } else {
                        reduceItemView(itemStr);
                    }
                    profilerView.getItemsView().revalidate();
                    profilerView.getItemsView().repaint();
                }
            }
        };
        for (JBCheckBox box : allCheckBoxes) {
            box.addItemListener(itemListener);
        }
    }

    private void filterItemList() {
        List<JBCheckBox> selectedList =
            allCheckBoxes.stream().filter(AbstractButton::isSelected).collect(Collectors.toList());
        LinkedList<String> selectedItem = new LinkedList<>();
        for (JBCheckBox jc : selectedList) {
            selectedItem.add(jc.getText());
        }
        map.put(ITEM, selectedItem);
        List<MonitorItemView> items = profilerView.getItemsView().getItems();
        if (cacheItemList.size() == 0) {
            cacheItemList.addAll(items);
        }
    }

    private void addItemView(String selectedStr) {
        ItemsView itemsView = profilerView.getItemsView();
        List<MonitorItemView> items = profilerView.getItemsView().getItems();
        Class<? extends MonitorItemView> selectClass = null;
        int selectIndex = 0;
        // Get the class information according to the selected string
        for (ProfilerMonitorItem item : profilerMonitorItemMap) {
            if (selectedStr.equals(item.getName())) {
                selectClass = item.getClazz();
                selectIndex = item.getIndex();
            }
        }
        // Add the selected chart item to the chart again
        for (MonitorItemView item : cacheItemList) {
            if (selectClass.isInstance(item)) {
                if (items.size() == 0) {
                    items.add(item);
                    itemsView.itemFoldOrExpend(false, item);
                }
                // Get the index of the item displayed in the interface through enumeration
                addItemViewByIndex(itemsView, profilerMonitorItemMap, item, selectIndex);
            }
        }
        if (items.size() == 1) {
            items.get(0).setPreferredSize(new Dimension(items.get(0).getWidth(), itemsView.getHeight() - ITEM_HEIGHT));
        }
    }

    private void addItemViewByIndex(ItemsView itemsView, List<ProfilerMonitorItem> profilerMonitorItemMap,
        MonitorItemView item, int selectIndex) {
        List<MonitorItemView> items = profilerView.getItemsView().getItems();
        for (int index = 0; index < items.size(); index++) {
            for (ProfilerMonitorItem monitorItem : profilerMonitorItemMap) {
                if (monitorItem.getClazz().isInstance(items.get(index))) {
                    // The selected one is smaller than the first one and is added to the 0 position
                    if (selectIndex < monitorItem.getIndex() && index == 0) {
                        items.add(0, item);
                        itemsView.itemFoldOrExpend(false, item);
                        return;
                    }
                    // The selected one is larger than the last one, so add it directly
                    if (selectIndex > monitorItem.getIndex() && index == items.size() - 1) {
                        items.add(item);
                        itemsView.itemFoldOrExpend(false, item);
                        return;
                    }
                    // add Intermediate item
                    if (selectIndex < monitorItem.getIndex()) {
                        items.add(index, item);
                        itemsView.itemFoldOrExpend(false, item);
                        return;
                    }
                }
            }
        }
    }

    private void reduceItemView(String notSelectStr) {
        List<MonitorItemView> items = profilerView.getItemsView().getItems();
        Class<? extends MonitorItemView> notSelectClass = null;
        // Get the class information according to the unselected string
        for (ProfilerMonitorItem item : profilerMonitorItemMap) {
            if (notSelectStr.equals(item.getName())) {
                notSelectClass = item.getClazz();
            }
        }
        // Delete the item view of chart according to the class information
        for (MonitorItemView item : items) {
            if (notSelectClass.isInstance(item)) {
                hideItem(items, item);
                break;
            }
        }
        // resize
        ItemsView itemsView = profilerView.getItemsView();
        for (MonitorItemView item : items) {
            itemsView.itemFoldOrExpend(true, item);
            break;
        }
        if (items.size() == 1) {
            items.get(0).setPreferredSize(new Dimension(items.get(0).getWidth(), itemsView.getHeight() - ITEM_HEIGHT));
        }
    }

    private void hideItem(List<MonitorItemView> items, MonitorItemView item) {
        item.setPreferredSize(new Dimension(item.getWidth(), 0));
        items.remove(item);
    }

    /**
     * show ItemMenu
     *
     * @param event event
     */
    public void showItemMenu(MouseEvent event) {
        popupMenu.show(event.getComponent(), event.getX() - MARGIN_RIGHT, event.getY() + POPUP_OFFSET);
    }
}
