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

import ohos.devtools.views.common.ProfilerMonitorItem;

import javax.swing.JPanel;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import static ohos.devtools.views.common.ColorConstants.CHART_BG;
import static ohos.devtools.views.common.ViewConstants.CHART_DEFAULT_HEIGHT;
import static ohos.devtools.views.common.ViewConstants.CHART_INIT_WIDTH;

/**
 * Save the custom layout panel of each indicator item View
 *
 * @since 2021/2/25 17:26
 */
public class ItemViewsPanel extends JPanel {
    /**
     * Bottom panel
     */
    private final ProfilerChartsView bottomPanel;

    /**
     * Save the collection of each indicator item View
     */
    private final List<AbsItemView> items;

    /**
     * Constructor
     *
     * @param bottomPanel Bottom panel
     */
    public ItemViewsPanel(ProfilerChartsView bottomPanel) {
        super(true);
        this.bottomPanel = bottomPanel;
        this.items = new ArrayList<>();
        // Do not use the Swing preset layout, use a custom absolute layout, one observation item is paved.
        // See the addMonitorItemView() method for the layout
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent exception) {
                resizeItemsByComponent(exception);
            }
        });
        this.setBackground(CHART_BG);
    }

    /**
     * Add a monitored metric item view
     *
     * @param item Index item enumeration class
     */
    void addMonitorItemView(ProfilerMonitorItem item) {
        // Create a Chart panel for each row
        int offsetY = this.items.size() * CHART_DEFAULT_HEIGHT;
        AbsItemView itemView;
        if (item != ProfilerMonitorItem.MEMORY) {
            return;
        }

        itemView = new MemoryItemView(bottomPanel, this, offsetY);
        // The current Panel is an absolute layout, you must specify the position, and then add to the current Panel
        itemView.setBounds(0, offsetY, CHART_INIT_WIDTH, CHART_DEFAULT_HEIGHT);
        // Add to layout
        this.add(itemView);
        // Add to collection for zoom
        this.items.add(itemView);
        // Set the layout. When there is an observation item,
        // it should cover the middle area. Do not use the Swing preset layout, use a custom absolute layout
        if (this.items.size() == 1) {
            this.setLayout(new GridLayout());
        }
        if (this.items.size() > 1) {
            this.setLayout(null);
        }
        this.repaint();
        this.revalidate();
    }

    /**
     * Triggered by a drag event: resize all indicator items
     */
    void resizeItemsByDrag() {
        int refreshOffsetY = 0;
        for (AbsItemView item : items) {
            // Save offset
            item.setCoordinateY(refreshOffsetY);
            item.setBounds(0, refreshOffsetY, item.getWidth(), item.getHeight());
            refreshOffsetY += item.getHeight();
        }

        this.repaint();
        this.revalidate();
    }

    /**
     * Triggered by the current component size change: re-adjust the size of all indicator items
     *
     * @param componentEvent Component Event
     */
    void resizeItemsByComponent(ComponentEvent componentEvent) {
        for (AbsItemView item : items) {
            item.setSize(componentEvent.getComponent().getWidth(), item.getHeight());
        }
        this.repaint();
        this.revalidate();
    }
}
