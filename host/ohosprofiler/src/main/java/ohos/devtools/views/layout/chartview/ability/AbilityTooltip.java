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

package ohos.devtools.views.layout.chartview.ability;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.views.charts.utils.ChartUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static ohos.devtools.views.layout.chartview.ability.AbilityCardStatus.ACTIVE;

/**
 * AbilityTooltip
 *
 * @since : 2021/10/25
 */
public class AbilityTooltip extends JComponent {
    private static final Logger LOGGER = LogManager.getLogger(AbilityTooltip.class);

    /**
     * The minimum width and height of the initial size of the tooltip
     */
    private static final int MIN_SIZE = 2;

    /**
     * Tooltip border and margin size
     */
    private static final int BORDER_MARGIN_SIZE = 5;

    /**
     * The distance to the mouse is constant to prevent the mouse from being blocked
     */
    private static final Point CONST_POINT = new Point(12, 5);

    /**
     * Prevent the mouse from being too close to trigger the mouse exit event
     */
    private static final Point CONST_POINT2 = new Point(30, 75);

    /**
     * Default number of rows
     */
    private static final int DEFAULT_ROWS = 2;

    /**
     * Number of rows after layout size adjustment
     */
    private static final int RESIZE_ROWS = 3;

    /**
     * Row height
     */
    private static final int ROW_HEIGHT = 30;

    /**
     * Line width
     */
    private static final int ROW_WIDTH = 100;

    /**
     * Tooltip default width
     */
    private static final int DEFAULT_WIDTH = 400;

    /**
     * ProfilerAppAbility
     */
    private final ProfilerAppAbility profilerAppAbility;

    /**
     * The number of rows of the grid layout in the current Tooltip
     */
    private int rows = DEFAULT_ROWS;

    /**
     * The main panel of the current Tooltip
     */
    private JPanel mainPanel;

    /**
     * The root panel of the parent component of the current Tooltip
     */
    private JRootPane parentRootPane;

    /**
     * The mask layer of the window cannot be modified casually, only applied as the parent object
     */
    private JLayeredPane mask;

    /**
     * AbilityTooltip Constructor
     * @param profilerAppAbility profilerAppAbility
     */
    public AbilityTooltip(ProfilerAppAbility profilerAppAbility) {
        this.profilerAppAbility = profilerAppAbility;
        this.setLayout(new FlowLayout());
        this.setOpaque(false);
        this.setVisible(false);
        mainPanel = new JPanel(new GridLayout(rows, 1));
        mainPanel.setBackground(JBColor.background().darker());
    }

    /**
     * Hidden components
     */
    public void hideTip() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("hideTip");
        }
        this.setVisible(false);
    }

    /**
     * Set tip for component
     *
     * @param parent Display tooltip object
     * @param currentTime currentTime
     * @param sliceInfoList sliceInfoList
     * @param isDataTooltip isDataTooltip
     */
    public void showTip(JComponent parent, int currentTime, List<AbilityCardInfo> sliceInfoList,
        Boolean isDataTooltip) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("showTip");
        }
        if (parent != null && parent.getRootPane() != null) {
            this.rows = DEFAULT_ROWS;
            // Rebuild Tooltip
            rebuild(parent);
            // Dynamically add and draw a legend
            addLegends(currentTime, sliceInfoList, isDataTooltip);
            resizeToolTip();
            this.validate();
            this.setVisible(true);
        }
    }

    /**
     * Rebuild Tooltip
     *
     * @param parent Display tooltip object
     */
    private void rebuild(JComponent parent) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("rebuild");
        }
        parentRootPane = parent.getRootPane();
        JLayeredPane layerPane = parentRootPane.getLayeredPane();
        // First remove the tip from the old panel
        if (mask != null && mask != layerPane) {
            mask.remove(this);
        }
        mask = layerPane;
        // Prevent whether there are still components that have been removed from the monitor
        layerPane.remove(this);
        // Since the main Panel needs to be redrawn each time, the main Panel must be removed first
        this.remove(mainPanel);
        // Place the tip on top of the mask window
        layerPane.add(this, JLayeredPane.POPUP_LAYER);
        // Recreate the main Panel according to the size of the Tooltip Item collection passed in
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(this.rows * ROW_WIDTH, this.rows * ROW_HEIGHT));
        mainPanel.setBackground(JBColor.background().darker());
        this.add(mainPanel);
    }

    /**
     * Add legend in Tooltip
     *
     * @param currentTime currentTime
     * @param sliceInfoList sliceInfo List
     * @param isDataTooltip isDataTooltip
     */
    private void addLegends(int currentTime, List<AbilityCardInfo> sliceInfoList, Boolean isDataTooltip) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("addLegends");
        }
        String currentTimeText = currentTime + "";
        mainPanel.removeAll();
        // add current time
        JBLabel timeLabel = new JBLabel();
        timeLabel.setBorder(JBUI.Borders.empty(BORDER_MARGIN_SIZE));
        long ms = transformTime(currentTimeText);
        timeLabel.setText(ChartUtils.formatTime(ms));
        timeLabel.setOpaque(false);
        mainPanel.add(timeLabel, BorderLayout.NORTH);
        if (sliceInfoList == null || sliceInfoList.size() == 0) {
            return;
        }
        // the ability name of the floating box
        JBLabel abilityName = new JBLabel();
        setLabelStyle(abilityName);
        // the ability time of the floating box
        JBLabel abilityTime = new JBLabel();
        setLabelStyle(abilityTime);
        for (AbilityCardInfo abilityCardInfo : sliceInfoList) {
            long abilityEndTime = abilityCardInfo.getEndTime();
            if (Objects.deepEquals(ACTIVE, abilityCardInfo.getAbilityCardStatus()) || (
                currentTime >= abilityCardInfo.getStartTime() && abilityEndTime <= 0)) {
                abilityEndTime = profilerAppAbility.getLastTimestamp();
            }
            if (isDataTooltip && currentTime >= abilityCardInfo.getStartTime() && currentTime <= abilityEndTime) {
                String abilityNameText =
                    abilityCardInfo.getApplicationName() + abilityCardInfo.getAbilityCardStatus().getStatus();
                abilityName.setText(abilityNameText);
                mainPanel.add(abilityName, BorderLayout.CENTER);
                String startTimeText = ChartUtils.formatTime(transformTime(abilityCardInfo.getStartTime() + ""));
                String endTimeText = ChartUtils.formatTime(transformTime(abilityEndTime + ""));
                abilityTime.setText(startTimeText + " - " + endTimeText);
                mainPanel.add(abilityTime, BorderLayout.SOUTH);
                rows = RESIZE_ROWS;
            }
        }
    }

    /**
     * set Label Style
     *
     * @param label label
     */
    private void setLabelStyle(JBLabel label) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("setLabelStyle");
        }
        label.setBorder(JBUI.Borders.empty(0, BORDER_MARGIN_SIZE, BORDER_MARGIN_SIZE, BORDER_MARGIN_SIZE));
        label.setOpaque(false);
    }

    /**
     * transform Time
     *
     * @param currentTimeText currentTimeText
     * @return long
     */
    private long transformTime(String currentTimeText) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("transformTime");
        }
        long ms = 0L;
        String pattern = "^\\d{0,20}$";
        boolean isMatch = Pattern.matches(pattern, currentTimeText);
        if (isMatch) {
            ms = Long.parseLong(currentTimeText);
        } else {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("Time format error:{}", currentTimeText);
            }
        }
        return ms;
    }

    /**
     * Coordinate conversion, the label moves with the mouse
     *
     * @param mouseEvent MouseEvent
     */
    public void followWithMouse(MouseEvent mouseEvent) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("followWithMouse");
        }
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
     * resize ToolTip
     */
    private void resizeToolTip() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("resizeToolTip");
        }
        this.setSize(DEFAULT_WIDTH, this.rows * ROW_HEIGHT);
        mainPanel.setPreferredSize(new Dimension(this.rows * ROW_WIDTH, this.rows * ROW_HEIGHT));
    }
}
