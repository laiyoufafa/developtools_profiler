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

import com.intellij.ui.components.JBPanel;
import ohos.devtools.views.layout.chartview.memory.MemoryItemView;

import javax.swing.SpringLayout;
import javax.swing.SpringLayout.Constraints;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.Spring.constant;
import static javax.swing.Spring.minus;
import static javax.swing.SpringLayout.EAST;
import static javax.swing.SpringLayout.SOUTH;

/**
 * Save the custom layout panel of each indicator item View
 */
public class ItemsView extends JBPanel {
    private static final int ITEM_MIN_EXPAND_HEIGHT = 400;
    private static final int ITEM_MIN_HEIGHT = 175;
    private final SpringLayout spring = new SpringLayout();
    private final ProfilerChartsView bottomPanel;
    private final List<MonitorItemView> items;
    private int showHeight;
    private int itemFoldHeight;

    /**
     * Constructor
     *
     * @param bottomPanel ProfilerChartsView
     */
    public ItemsView(ProfilerChartsView bottomPanel) {
        this.setLayout(spring);
        this.bottomPanel = bottomPanel;
        this.items = new ArrayList<>();
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                adjustLayout(event.getComponent().getWidth());
            }
        });
    }

    void updateShowHeight(int showHeight) {
        this.showHeight = showHeight;
        adjustLayout(this.getWidth());
    }

    /**
     * update need ShowTable View
     */
    void updateShowTableView() {
        for (MonitorItemView item : items) {
            if (item instanceof MemoryItemView) {
                MemoryItemView memoryItemView = (MemoryItemView) item;
                JBPanel heapViewPanel = memoryItemView.getHeapViewPanel();
                if (heapViewPanel != null) {
                    memoryItemView.remove(heapViewPanel);
                }
            }
        }
    }

    /**
     * Add a monitor item view
     *
     * @param item Profiler monitor item enum
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */
    void addMonitorItemView(ProfilerMonitorItem item)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        MonitorItemView itemView = item.getClazz().getConstructor().newInstance();
        itemView.init(bottomPanel, this, item);
        this.add(itemView);
        items.add(itemView);
        // Add item constraints
        if (items.size() == 1) {
            itemView.setPreferredSize(new Dimension(this.getWidth(), this.getHeight()));
            spring.putConstraint(EAST, itemView, minus(constant(0)), EAST, this);
            Constraints currentCons = spring.getConstraints(itemView);
            currentCons.setX(constant(0));
            currentCons.setY(constant(0));
        } else {
            Constraints previousCons = spring.getConstraints(items.get(items.size() - 1));
            Constraints currentCons = spring.getConstraints(itemView);
            currentCons.setX(constant(0));
            // When there is more than one item, the current item is placed in the SOUTH of the previous one
            currentCons.setY(previousCons.getConstraint(SOUTH));
            currentCons.setConstraint(EAST, previousCons.getConstraint(EAST));
        }
        adjustLayout(this.getWidth());
    }

    /**
     * Adjust the layout of the current view，including constraints and size
     *
     * @param width width of this view
     */
    private void adjustLayout(int width) {
        if (items.size() == 0) {
            return;
        }

        if (items.size() == 1) {
            items.get(0).setPreferredSize(new Dimension(width, this.getHeight()));
            return;
        }

        // Calculate and save the single item height when all items are folded
        int heightSize = items.size() * ITEM_MIN_HEIGHT;
        boolean flag = heightSize > showHeight;
        itemFoldHeight = flag ? ITEM_MIN_HEIGHT : showHeight / items.size();

        if (isItemExpend()) {
            // If an item is expanded, there is no need to change the height
            items.forEach(item -> {
                int oldHeight = Double.valueOf(item.getPreferredSize().getHeight()).intValue();
                item.setPreferredSize(new Dimension(width, oldHeight));
            });
        } else {
            items.forEach(item -> item.setPreferredSize(new Dimension(width, itemFoldHeight)));
        }

        // When there is only one item, there is no need to adjust the constraint
        if (items.size() > 1) {
            adjustConstraints();
        }
        // Adjust the height of current view
        adjustTotalSize();
    }

    private boolean isItemExpend() {
        boolean expend = false;
        for (MonitorItemView item : items) {
            if (!item.isFold()) {
                expend = true;
                break;
            }
        }
        return expend;
    }

    private void adjustConstraints() {
        int size = items.size();
        for (int index = 1; index < size; index++) {
            Constraints previousCons = spring.getConstraints(items.get(index - 1));
            Constraints currentCons = spring.getConstraints(items.get(index));
            currentCons.setX(constant(0));
            // the current item is placed in the SOUTH of the previous one
            currentCons.setY(previousCons.getConstraint(SOUTH));
            currentCons.setConstraint(EAST, previousCons.getConstraint(EAST));
        }
    }

    private void adjustTotalSize() {
        int totalHeight = calcTotalHeight();
        this.setPreferredSize(new Dimension(this.getWidth(), totalHeight));
    }

    private int calcTotalHeight() {
        int total = 0;
        for (MonitorItemView item : items) {
            total += Double.valueOf(item.getPreferredSize().getHeight()).intValue();
        }
        return total;
    }

    /**
     * Fold or expend the item
     *
     * @param fold ture: fold, false: expand
     * @param item Monitor item view
     */
    public void itemFoldOrExpend(boolean fold, MonitorItemView item) {
        if (items.size() == 1) {
            return;
        }

        int newHeight;
        if (fold) {
            newHeight = itemFoldHeight;
        } else {
            newHeight = ITEM_MIN_EXPAND_HEIGHT;
        }
        item.setPreferredSize(new Dimension(this.getWidth(), newHeight));
        adjustLayout(this.getWidth());
    }

    /**
     * Click on chart of chart, expand item
     *
     * @param item MonitorItemView
     */
    public void itemChartClick(MonitorItemView item) {
        if (items.size() > 1) {
            item.setPreferredSize(new Dimension(this.getWidth(), showHeight));
            adjustLayout(this.getWidth());
        }
    }

    public List<MonitorItemView> getItems() {
        return items;
    }
}
