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

package ohos.devtools.views.charts.tooltip;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import ohos.devtools.views.charts.model.ChartDataModel;
import ohos.devtools.views.charts.utils.ChartUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Customize the tooltip that can follow the mouse as a legend
 */
public final class LegendTooltip extends JComponent {
    private static final Logger LOGGER = LogManager.getLogger(LegendTooltip.class);

    private static final int MIN_SIZE = 2;

    private static final int NUM_2 = 2;

    private static final int NUM_3 = 4;

    /**
     * 常量1，距离鼠标常量，防止鼠标遮挡
     */
    private static final Point CONST_POINT = new Point(12, 5);

    /**
     * 常量2，防止离鼠标太近，触发mouse exit事件
     */
    private static final Point CONST_POINT2 = new Point(30, 75);

    /**
     * Grid布局的默认行数
     */
    private static final int DEFAULT_ROWS = 2;

    /**
     * Grid布局的行高
     */
    private static final int ROW_HEIGHT = 36;

    /**
     * Tooltip默认宽度
     */
    private static final int DEFAULT_WIDTH = 170;

    /**
     * thread unspecified
     */
    private static final int THREAD_UNSPECIFIED = 0;

    /**
     * thread running
     */
    private static final int THREAD_RUNNING = 1;

    /**
     * thread sleeping
     */
    private static final int THREAD_SLEEPING = 2;

    /**
     * thread stopped
     */
    private static final int THREAD_STOPPED = 3;

    /**
     * thread waiting
     */
    private static final int THREAD_WAITING = 4;

    /**
     * 当前Tooltip的主面板
     */
    private JPanel mainPanel;

    /**
     * 当前Tooltip的父组件的根面板
     */
    private JRootPane parentRootPane;

    /**
     * 窗口的遮罩层，不能随便修改，只作父对象应用
     */
    private JLayeredPane mask;

    /**
     * 当前Tooltip中Grid布局的行数
     */
    private int rows = DEFAULT_ROWS;

    /**
     * Constructor
     */
    public LegendTooltip() {
        initTip();
    }

    /**
     * 初始化Tooltip
     */
    private void initTip() {
        this.setLayout(new FlowLayout());
        // false为控件透明，true为不透明
        this.setOpaque(false);
        this.setVisible(false);
        mainPanel = new JPanel(new GridLayout(rows, 1));
        mainPanel.setBackground(JBColor.background().darker());
    }

    /**
     * 隐藏组件
     */
    public void hideTip() {
        this.setVisible(false);
    }

    /**
     * 为某个组件设置tip
     *
     * @param parent 显示tooltip的对象
     * @param timeline 显示的时间Tip
     * @param totalValue Total值
     * @param tooltipItems 要显示的图例
     * @param isCharting boolean
     * @param axisYUnit axisYUnit
     */
    public void showTip(JComponent parent, String timeline, String totalValue, List<TooltipItem> tooltipItems,
        boolean isCharting, String axisYUnit) {
        if (parent != null && parent.getRootPane() != null) {
            this.rows = tooltipItems.size() + NUM_2;
            // 重新组建Tooltip
            if (isCharting) {
                rebuild(parent);
                resize();
                return;
            }

            // 动态添加和绘制图例
            addLegends(timeline, totalValue, tooltipItems, axisYUnit);
            this.validate();
            this.setVisible(true);
        }
    }

    /**
     * 重新组建Tooltip
     *
     * @param parent 显示tooltip的对象
     */
    private void rebuild(JComponent parent) {
        parentRootPane = parent.getRootPane();
        JLayeredPane layerPane = parentRootPane.getLayeredPane();

        // 先从旧面板中移除tip
        if (mask != null && mask != layerPane) {
            mask.remove(this);
        }
        mask = layerPane;

        // 防止还有没有移除监听的组件
        layerPane.remove(this);

        // 由于每次要重绘mainPanel，所以也要先移除mainPanel
        this.remove(mainPanel);

        // 放置tip在遮罩窗口顶层
        layerPane.add(this, JLayeredPane.POPUP_LAYER);
        // 窗口遮罩层添加监听

        // 根据传入的TooltipItem集合大小，重新创建mainPanel
        mainPanel = new JPanel(new GridLayout(rows, 1));
        mainPanel.setBorder(new LineBorder(Color.BLACK));
        mainPanel.setBackground(JBColor.background().darker());
        this.add(mainPanel);
    }

    /**
     * Tooltip中添加图例
     *
     * @param timeline 时间
     * @param totalValue Total值
     * @param tooltipItems 图例集合
     * @param axisYUnit axisYUnit
     */
    private void addLegends(String timeline, String totalValue, List<TooltipItem> tooltipItems, String axisYUnit) {
        mainPanel.removeAll();
        // 添加时间
        JBLabel timeLabel = new JBLabel();
        timeLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        long ms = 0;
        String pattern = "^\\d{0,20}$";
        boolean isMatch = Pattern.matches(pattern, timeline);
        if (isMatch) {
            ms = Long.parseLong(timeline);
        } else {
            LOGGER.error("Time format error:{}", timeline);
        }
        timeLabel.setText(ChartUtils.formatTime(ms));
        timeLabel.setOpaque(false);
        mainPanel.add(timeLabel);
        if (StringUtils.isNotBlank(totalValue)) {
            // 添加悬浮框的total值
            JBLabel totalLabel = new JBLabel();
            totalLabel.setBorder(new EmptyBorder(0, 5, 5, 5));
            totalLabel.setOpaque(false);
            totalLabel.setText("Total:" + totalValue + axisYUnit);
            mainPanel.add(totalLabel);
        }
        // 添加图例
        for (TooltipItem tooltipItem : tooltipItems) {
            JPanel single = new JPanel(new FlowLayout(FlowLayout.LEFT));
            single.setOpaque(false);

            Color color = tooltipItem.getColor();
            if (color != null) {
                single.add(new TooltipColorRect(tooltipItem.getColor()));
            }
            JBLabel nameLabel = new JBLabel();
            nameLabel.setOpaque(false);
            nameLabel.setText(tooltipItem.getText());
            single.add(nameLabel);
            mainPanel.add(single);
        }
    }

    /**
     * 坐标转换，标签跟随鼠标移动
     *
     * @param mouseEvent MouseEvent
     */
    public void followWithMouse(MouseEvent mouseEvent) {
        if (mask == null) {
            return;
        }

        if (this.getWidth() < MIN_SIZE || this.getHeight() < MIN_SIZE) {
            this.setVisible(false);
            return;
        }

        this.setVisible(true);
        Point screenPoint = mouseEvent.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(screenPoint, mask);

        int newLocationX = (int) (screenPoint.getX() + CONST_POINT.getX());
        int newLocationY = (int) (screenPoint.getY() + CONST_POINT.getY());

        Dimension tipSize = mainPanel.getPreferredSize();
        if (newLocationX + tipSize.width > parentRootPane.getWidth()) {
            newLocationX = (int) (screenPoint.getX() - tipSize.width - CONST_POINT2.getX());
        }
        if (newLocationY + tipSize.height > parentRootPane.getHeight()) {
            newLocationY = (int) (screenPoint.getY() - tipSize.height - CONST_POINT2.getY());
        }

        this.setLocation(newLocationX, newLocationY);
    }

    /**
     * 重新调整大小
     */
    private void resize() {
        this.setSize(DEFAULT_WIDTH, this.rows * ROW_HEIGHT);
    }

    /**
     * showThreadStatusTip
     *
     * @param parent parent
     * @param timeline timeline
     * @param chartDataModel chartDataModel
     * @param isCharting isCharting
     */
    public void showThreadStatusTip(JComponent parent, String timeline, ChartDataModel chartDataModel,
        boolean isCharting) {
        if (parent != null && parent.getRootPane() != null) {
            if (isCharting) {
                this.rows = NUM_3;
                rebuild(parent);
                resize();
                return;
            }
            addThreadStatusLegends(timeline, chartDataModel.getName(), chartDataModel.getValue(),
                chartDataModel.getDoubleValue());
            this.validate();
            this.setVisible(true);
        }
    }

    /**
     * addThreadStatusLegends
     *
     * @param timeline timeline
     * @param threadName threadName
     * @param threadStatus threadStatus
     * @param threadUsage threadUsage
     */
    private void addThreadStatusLegends(String timeline, String threadName, int threadStatus, double threadUsage) {
        mainPanel.removeAll();
        // 添加时间
        JBLabel timeLabel = new JBLabel();
        timeLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        long ms = 0L;
        String pattern = "^\\d{0,20}$";
        boolean isMatch = Pattern.matches(pattern, timeline);
        if (isMatch) {
            ms = Long.parseLong(timeline);
        } else {
            LOGGER.error("Time format error:{}", timeline);
        }
        timeLabel.setText(ChartUtils.formatTime(ms));
        timeLabel.setOpaque(false);
        mainPanel.add(timeLabel);

        JBLabel nameLabel = new JBLabel();
        nameLabel.setBorder(new EmptyBorder(0, 5, 5, 5));
        nameLabel.setOpaque(false);
        nameLabel.setText("Thread:" + threadName);
        mainPanel.add(nameLabel);

        JBLabel statusLabel = new JBLabel();
        statusLabel.setBorder(new EmptyBorder(0, 5, 5, 5));
        statusLabel.setOpaque(false);
        switch (threadStatus) {
            case THREAD_RUNNING:
                statusLabel.setText("RUNNING");
                break;
            case THREAD_SLEEPING:
                statusLabel.setText("SLEEPING");
                break;
            case THREAD_STOPPED:
                statusLabel.setText("STOPPED");
                break;
            case THREAD_WAITING:
                statusLabel.setText("WAITING");
                break;
            default:
                statusLabel.setText("UNSPECIFIED");
        }
        mainPanel.add(statusLabel);
        JBLabel usageLabel = new JBLabel();
        usageLabel.setBorder(new EmptyBorder(0, 5, 5, 5));
        usageLabel.setOpaque(false);
        usageLabel.setText("OccRate:" + threadUsage);
        mainPanel.add(usageLabel);
    }

    /**
     * showGpuTip
     *
     * @param parent parent
     * @param timeline timeline
     * @param tooltipItems tooltipItems
     * @param isCharting isCharting
     */
    public void showGpuTip(JComponent parent, String timeline, List<TooltipItem> tooltipItems, boolean isCharting) {
        if (parent != null && parent.getRootPane() != null) {
            this.rows = tooltipItems.size() + 1;
            if (isCharting) {
                rebuild(parent);
                this.setSize(300, this.rows * ROW_HEIGHT);
                return;
            }

            addGpuLegends(timeline, tooltipItems);
            this.validate();
            this.setVisible(true);
        }
    }

    private void addGpuLegends(String timeline, List<TooltipItem> tooltipItems) {
        mainPanel.removeAll();
        JBLabel timeLabel = new JBLabel();
        timeLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        long ms = 0;
        String pattern = "^\\d{0,20}$";
        boolean isMatch = Pattern.matches(pattern, timeline);
        if (isMatch) {
            ms = Long.parseLong(timeline);
        } else {
            LOGGER.error("Time format error:{}", timeline);
        }
        timeLabel.setText(ChartUtils.formatTime(ms));
        timeLabel.setOpaque(false);
        mainPanel.add(timeLabel);
        for (TooltipItem tooltipItem : tooltipItems) {
            JPanel single = new JPanel(new FlowLayout(FlowLayout.LEFT));
            single.setOpaque(false);
            JBLabel nameLabel = new JBLabel();
            nameLabel.setOpaque(false);
            nameLabel.setText(tooltipItem.getName() + " : " + tooltipItem.getText());
            single.add(nameLabel);
            mainPanel.add(single);
        }
    }
}
