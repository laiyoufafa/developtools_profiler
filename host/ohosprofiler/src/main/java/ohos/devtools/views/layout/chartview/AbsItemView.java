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

import ohos.devtools.views.charts.ProfilerChart;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static ohos.devtools.views.common.ViewConstants.NUM_3;

/**
 * The abstract parent class of the view panel of each indicator analysis item
 *
 * @version 1.0
 * @date 2021/2/25 17:22
 */
public abstract class AbsItemView extends JPanel {
    /**
     * Index item name
     */
    protected String itemName;

    /**
     * Chart
     */
    protected ProfilerChart chart;

    /**
     * Bottom panel
     */
    protected ProfilerChartsView bottomPanel;

    /**
     * The parent container of the current view
     *
     * @see "For custom zoom"
     */
    protected ItemViewsPanel parent;

    /**
     * The Y-axis coordinate of the current panel in the parent panel, that is, the Y-axis offset
     */
    private int coordinateY;

    /**
     * Constructor
     *
     * @param bottomPanel Bottom panel
     * @param parent      The parent container of the current view
     * @param coordinateY The Y-axis coordinate of the current panel in the parent panel, that is, the Y-axis offset
     */
    public AbsItemView(ProfilerChartsView bottomPanel, ItemViewsPanel parent, int coordinateY) {
        super(true);
        this.bottomPanel = bottomPanel;
        this.parent = parent;
        this.coordinateY = coordinateY;
        this.setLayout(new BorderLayout());
        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                dragEvent(mouseEvent);
            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                moveEvent(mouseEvent);
            }
        });
    }

    /**
     * Initialize the title panel
     *
     * @param itemName Panel name
     */
    protected void initTitlePanel(String itemName) {
        this.itemName = itemName;
        ItemTitleView itemTitleView = new ItemTitleView(this.bottomPanel, itemName);
        this.add(itemTitleView, BorderLayout.NORTH);
        // 标题面板需要绘制标尺，把他加入集合
        this.bottomPanel.addRulerComp(itemTitleView);
    }

    /**
     * Custom mouse drag events to achieve vertical zoom
     *
     * @param mouseEvent MouseEvent
     */
    private void dragEvent(MouseEvent mouseEvent) {
        // 鼠标在当前组件中的Y轴坐标，即为缩放时当前组件的高度
        int newHeight = mouseEvent.getY();
        // 目前只允许在Chart下方进行拖拽缩放，也就是mouseMoved中，鼠标变为resize之后
        if (this.getCursor().getType() == Cursor.S_RESIZE_CURSOR) {
            this.setBounds(0, coordinateY, mouseEvent.getComponent().getWidth(), newHeight);
            // 刷新父Panel，重新调整各个指标项面板的大小
            parent.resizeItemsByDrag();
        }
    }

    /**
     * Custom mouse movement events, change the mouse style
     *
     * @param mouseEvent MouseEvent
     */
    private void moveEvent(MouseEvent mouseEvent) {
        int mouseY = mouseEvent.getY();
        // 只监听鼠标靠近Panel底部时将样式修改为resize，顶部不允许resize
        if (Math.abs(this.getHeight() - mouseY) < NUM_3) {
            this.setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
        } else {
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        this.repaint();
        this.revalidate();
    }

    public ProfilerChart getChart() {
        return chart;
    }

    /**
     * Setter
     *
     * @param coordinateY int
     */
    public void setCoordinateY(int coordinateY) {
        this.coordinateY = coordinateY;
    }
}
