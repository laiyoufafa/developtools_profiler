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
import com.intellij.ui.components.JBPanel;
import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import ohos.devtools.services.ability.AbilityActivityInfo;
import ohos.devtools.services.ability.AbilityDataCache;
import ohos.devtools.services.ability.AbilityEventInfo;
import ohos.devtools.services.ability.EventType;
import ohos.devtools.views.charts.utils.ChartUtils;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.layout.chartview.utils.OperationUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static ohos.devtools.views.charts.utils.ChartUtils.multiply;
import static ohos.devtools.views.layout.chartview.utils.ChartViewConstants.TIMELINE_WIDTH;

/**
 * AppAbility component of Profiler
 *
 * @since 2021/10/25
 */
public class ProfilerAppAbility extends JBPanel {
    private static final Logger LOGGER = LogManager.getLogger(ProfilerAppAbility.class);

    /**
     * Left spacing of AbilityLifeChart
     */
    private static final int LEFT_SPACING = 5;

    /**
     * ability drawn height and left compensation amount
     */
    private static final int ABILITY_HEIGHT = 6;

    /**
     * font size
     */
    private static final int FONT_SIZE = 12;

    /**
     * draw point
     */
    private static final int DRAW_POINT = 14;

    /**
     * draw character of point Y
     */
    private static final int DRAW_CHARACTER_POINTY = 40;

    /**
     * Event compensation value on the right side of ability to solve the jitter on the right
     */
    private static final int COMPENSATION_VALUE = 50;

    /**
     * profiler ability height
     */
    private static final int PROFILER_ABILITY_HEIGHT = 80;

    /**
     * Critical time of interval
     */
    private static final int CRITICAL_TIME = 100;

    /**
     * ability stop status message
     */
    private static final String STOP_STATUS = " - stopped - saved";

    /**
     * ability background status
     */
    private static final int BACKGROUND_STATUS = 4;

    /**
     * ability end status
     */
    private static final int END_STATUS = 6;

    /**
     * rotation size
     */
    private static final int ROTATION_SIZE = 16;

    /**
     * rotation image
     */
    private static final String ROTATION_IMAGE = "/images/rotation.png";

    /**
     * sliceInfo List
     */
    private List<AbilityCardInfo> sliceInfoList;

    /**
     * eventInfoList
     */
    private List<AbilityEventInfo> eventInfoList;

    /**
     * abilityActivityInfoList
     */
    private List<AbilityActivityInfo> abilityActivityList;

    /**
     * The maximum time that can be displayed on the timeline
     */
    private int maxDisplayTime;

    /**
     * Minimum interval between timescales
     */
    private int minMarkInterval;

    /**
     * The start time of the timeline when drawing
     */
    private int startTime;

    /**
     * The end time of the timeline when drawing
     */
    private int endTime;

    /**
     * The x-axis is the coordinate of the starting plot
     *
     * @see "The dynamic timeline and chart appear from right to left"
     */
    private int startCoordinate;

    /**
     * Number of pixels per X-axis time unit
     */
    private BigDecimal pixelPerTime;

    /**
     * Tooltip of Ability
     */
    private final AbilityTooltip tooltip;

    /**
     * Update when mouse moved
     *
     * @see "Use function getMousePosition() will be null sometime."
     */
    private Point mousePoint;

    /**
     * Whether the mouse enters chart
     *
     * @see "Here is the entry into the paint chart, not the chart component"
     */
    private boolean enterChart;

    /**
     * ability Height
     */
    private int abilityHeight;

    /**
     * The runtime is equivalent to endTime, and the pause is distinguished from endTime
     */
    private long lastTimestamp;

    /**
     * Last Timestamp after the last refresh
     */
    private long beforeLastTimestamp = -1;

    /**
     * rotation image
     */
    private Image rotationImage = null;

    /**
     * first Time Stamp
     */
    private long firstTimeStamp;

    /**
     * Constructor
     */
    public ProfilerAppAbility() {
        this.sliceInfoList = Lists.newArrayList();
        this.eventInfoList = Lists.newArrayList();
        this.abilityActivityList = Lists.newArrayList();
        this.tooltip = new AbilityTooltip(this);
        this.setPreferredSize(new Dimension(TIMELINE_WIDTH, PROFILER_ABILITY_HEIGHT));
        this.setBackground(JBColor.background().brighter());
        this.initMouseListener();
        this.add(tooltip);
        try {
            rotationImage = ImageIO.read(getClass().getResourceAsStream(ROTATION_IMAGE));
        } catch (IOException exception) {
            LOGGER.error("abilityEventInfo ImageIO Failed to read picture", exception);
        }
    }

    /**
     * refresh ability activity data
     *
     * @param startTime The start time of the x-axis when drawing
     * @param endTime The end time of the x-axis when drawing
     * @param firstTime firstTime
     */
    public void refreshAbilityTime(int startTime, int endTime, long firstTime) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("refreshAbilityTime");
        }
        this.startTime = startTime;
        this.endTime = endTime;
        this.firstTimeStamp = firstTime;
    }

    /**
     * refresh ability activity data
     *
     * @param abilityActivityInfoList abilityActivityInfoList
     */
    public void refreshActivityAbility(List<AbilityActivityInfo> abilityActivityInfoList) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("refreshActivityAbility");
        }
        this.abilityActivityList = abilityActivityInfoList;
    }

    /**
     * refresh ability event data
     *
     * @param eventInfoList eventInfoList
     */
    public void refreshEventAbility(List<AbilityEventInfo> eventInfoList) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("refreshEventAbility");
        }
        this.eventInfoList = eventInfoList;
    }

    /**
     * refresh ability tooltip data
     *
     * @param sliceInfoList sliceInfoList
     */
    public void refreshActivityToolTip(List<AbilityCardInfo> sliceInfoList) {
        this.sliceInfoList = sliceInfoList;
    }

    /**
     * refresh Activity EndStatus
     *
     * @param status status
     */
    public void refreshActivityEndStatus(boolean status) {
        if (status) {
            this.sliceInfoList = Lists.newArrayList();
            this.eventInfoList = Lists.newArrayList();
            this.abilityActivityList = Lists.newArrayList();
        } else {
            beforeLastTimestamp = lastTimestamp;
            if (sliceInfoList.size() >= 1) {
                sliceInfoList.get(sliceInfoList.size() - 1).setEndTime(endTime);
                sliceInfoList.get(sliceInfoList.size() - 1).setAbilityCardStatus(AbilityCardStatus.INITIAL);
            }
        }
    }

    /**
     * paintComponent
     *
     * @param graphics graphics
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        initPoints();
        drawAxis(graphics);
        // chart expand: show tooltip
        if (enterChart) {
            showTooltip();
        }
    }

    /**
     * Initialization of points and scale information
     */
    private void initPoints() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initPoints");
        }
        // Coordinate axis X0 point when drawing timeline
        int x0 = this.getX();
        // Right coordinates of timeline
        int right = x0 + this.getWidth();
        if (right == 0 || maxDisplayTime == 0) {
            return;
        }
        // Calculate how many pixels a time unit takes
        pixelPerTime = OperationUtils.divide(right, maxDisplayTime);
        // If the current time is greater than the maximum time, the offset is calculated
        if (endTime > maxDisplayTime && minMarkInterval != 0) {
            startCoordinate = x0;
        } else {
            // If the current time is less than the maximum time,
            // the timeline needs to be drawn from the middle with an offset of 0
            startCoordinate = x0 + right - OperationUtils.multiply(pixelPerTime, endTime);
        }
    }

    /**
     * Draw the coordinate axis of the timeline
     *
     * @param graphics Graphics
     */
    private void drawAxis(Graphics graphics) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("drawAxis");
        }
        if (graphics instanceof Graphics2D) {
            Graphics2D graphicsDraw = (Graphics2D) graphics;
            graphicsDraw.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphicsDraw.setFont(new Font("PingFang SC", Font.PLAIN, FONT_SIZE));
            // Draw discontinuous multiple state diagrams
            if (Objects.deepEquals(sliceInfoList.size(), 0) && Objects.deepEquals(eventInfoList.size(), 0)) {
                return;
            }
            abilityHeight = ChartUtils.divideInt(ChartUtils.multiply(new BigDecimal(this.getHeight()), 2), 3);
            if (eventInfoList.size() > 0) {
                drawAbilityEvent(graphicsDraw);
            }
            if (abilityActivityList.size() > 0) {
                drawAbilityActivity(graphicsDraw);
            }
        }
    }

    /**
     * Draw Ability Activity Cycle
     *
     * @param graphics graphics
     */
    private void drawAbilityActivity(Graphics2D graphics) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("drawAbilityActivity");
        }
        for (int index = 0; index < abilityActivityList.size(); index++) {
            AbilityActivityInfo beforeActivityInfo = abilityActivityList.get(index);
            int beforeLifeCycleId = beforeActivityInfo.getLifeCycleId();
            long beforeTimeStamp = beforeActivityInfo.getTimeStamp() - firstTimeStamp;
            // long beforeTimeStamp = beforeActivityInfo.getTimeStamp();
            String beforeStateName = beforeActivityInfo.getAbilityStateName();
            // Calculate the current width of draw String characters
            FontMetrics fontMetrics = graphics.getFontMetrics(graphics.getFont());
            int drawEndLength = fontMetrics.stringWidth(beforeStateName + STOP_STATUS);
            if (abilityActivityList.size() > (index + 1)) {
                AbilityActivityInfo afterActivityInfo = abilityActivityList.get(index + 1);
                int afterLifeCycleId = afterActivityInfo.getLifeCycleId();
                long afterTimeStamp = afterActivityInfo.getTimeStamp() - firstTimeStamp;
                if (beforeLifeCycleId == afterLifeCycleId && beforeActivityInfo.isStartStatus() && !afterActivityInfo
                    .isStartStatus()) {
                    drawActivityView(graphics, beforeTimeStamp, beforeStateName, drawEndLength, afterTimeStamp);
                }
            } else {
                if (beforeActivityInfo.isStartStatus()) {
                    if (beforeTimeStamp <= endTime) {
                        int pointX = startCoordinate + multiply(pixelPerTime, (int) beforeTimeStamp - startTime);
                        int width = multiply(pixelPerTime, (int) (endTime - beforeTimeStamp));
                        Color abilityStatusColor;
                        String abilityStatusName = "";
                        if (beforeLastTimestamp == lastTimestamp) {
                            abilityStatusColor = ColorConstants.ABILITY_INITIAL_COLOR;
                            abilityStatusName = beforeStateName + STOP_STATUS;
                        } else {
                            abilityStatusColor = ColorConstants.ABILITY_ACTIVE_COLOR;
                            abilityStatusName = beforeStateName;
                        }
                        graphics.setPaint(abilityStatusColor);
                        // y、height constant, x、width Dynamically change with time axis offset
                        graphics.fillRect(pointX, abilityHeight, width, ABILITY_HEIGHT);
                        graphics.setPaint(JBColor.black);
                        int leftPoint = pointX + LEFT_SPACING;
                        if (startCoordinate == 0 && beforeTimeStamp <= startTime) {
                            leftPoint = LEFT_SPACING;
                        }
                        graphics.drawString(abilityStatusName, leftPoint, DRAW_CHARACTER_POINTY);
                    }
                }
            }
        }
    }

    /**
     * draw ability activity view
     *
     * @param graphics graphics
     * @param beforeTimeStamp beforeTimeStamp
     * @param beforeStateName beforeStateName
     * @param drawEndLength drawEndLength
     * @param afterTimeStamp afterTimeStamp
     */
    private void drawActivityView(Graphics2D graphics, long beforeTimeStamp, String beforeStateName, int drawEndLength,
        long afterTimeStamp) {
        int pointX = startCoordinate + multiply(pixelPerTime, (int) beforeTimeStamp - startTime);
        int width = multiply(pixelPerTime, (int) (afterTimeStamp - beforeTimeStamp));
        int leftWidth = multiply(pixelPerTime, (int) (afterTimeStamp - startTime));
        graphics.setPaint(ColorConstants.ABILITY_INITIAL_COLOR);
        // y、height constant, x、width Dynamically change with time axis offset
        graphics.fillRect(pointX, abilityHeight, width, ABILITY_HEIGHT);
        graphics.setPaint(JBColor.black);
        int leftPoint = pointX + LEFT_SPACING;
        if (startCoordinate == 0 && beforeTimeStamp <= startTime && leftWidth >= drawEndLength) {
            leftPoint = LEFT_SPACING;
        }
        graphics.drawString(beforeStateName + STOP_STATUS, leftPoint, DRAW_CHARACTER_POINTY);
    }

    /**
     * Draw Ability Event
     *
     * @param graphics graphics
     */
    private void drawAbilityEvent(Graphics2D graphics) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("drawAbilityEvent");
        }
        for (int index = 0; index < eventInfoList.size(); index++) {
            if (Objects.deepEquals(null, eventInfoList.get(index))) {
                return;
            }
            AbilityEventInfo abilityEventBefore = eventInfoList.get(index);
            EventType eventType = abilityEventBefore.getEventType();
            long beforeTimeStamp = abilityEventBefore.getTimeStamp();
            int beforeKeyType = abilityEventBefore.getKeyType();
            int pointX = startCoordinate + multiply(pixelPerTime, (int) (beforeTimeStamp - firstTimeStamp) - startTime);
            if (eventType.equals(EventType.KEY_EVENT)) {
                // Subject to the moment of pressing when clicked
                if (beforeKeyType == 2 && !abilityEventBefore.isDown()) {
                    // key event
                    graphics.setColor(ColorConstants.ABILITY_BACK_COLOR);
                    int[] horizontalPoint = {pointX, pointX + DRAW_POINT, pointX + DRAW_POINT};
                    int[] verticalPoint = {DRAW_POINT + (DRAW_POINT / 2), DRAW_POINT, DRAW_POINT + DRAW_POINT};
                    // When drawing back Image, only point X changes
                    graphics.drawPolygon(horizontalPoint, verticalPoint, 3);
                }
            } else if (eventType.equals(EventType.ROTATION_EVENT)) {
                // rotation event
                if (rotationImage != null) {
                    // When drawing rotation Image, only point X changes
                    graphics.drawImage(rotationImage, pointX, DRAW_POINT, ROTATION_SIZE, ROTATION_SIZE, null);
                }
            } else if (eventType.equals(EventType.MOUSE_EVENT)) {
                // mouse event
                if (ProfilerLogManager.isInfoEnabled()) {
                    LOGGER.info("draw Ability mouse event");
                }
            } else {
                if (beforeKeyType == 1) {
                    if (eventInfoList.size() > (index + 1)) {
                        // touch event
                        AbilityEventInfo abilityEventAfter = eventInfoList.get(index + 1);
                        drawFinishedTouch(graphics, beforeTimeStamp, pointX, abilityEventAfter);
                    } else {
                        drawUnFinishedTouch(graphics, beforeTimeStamp, pointX);
                    }
                }
            }
        }
    }

    /**
     * draw UnFinished Touch
     *
     * @param graphics graphics
     * @param beforeTimeStamp beforeTimeStamp
     * @param pointX pointX
     */
    private void drawUnFinishedTouch(Graphics2D graphics, long beforeTimeStamp, int pointX) {
        if (endTime - beforeTimeStamp > CRITICAL_TIME) {
            int width = multiply(pixelPerTime, (int) (endTime - beforeTimeStamp - firstTimeStamp));
            // If it is less than 100 ms, draw a circle with only point X change
            graphics.setColor(ColorConstants.ABILITY_HOME_COLOR);
            // When drawing back Oval, only point X changes
            graphics.fillOval(pointX, DRAW_POINT, DRAW_POINT, DRAW_POINT);
            graphics.setColor(ColorConstants.ABILITY_HOME_OTHER_COLOR);
            // Draw rectangle (width pointX)
            graphics.fillRect(pointX, DRAW_POINT, width, DRAW_POINT);
            // If it is greater than 100 ms, draw a circle of point X change and add an ellipse (pointX)
            graphics.fillOval(pointX + width, DRAW_POINT, DRAW_POINT, DRAW_POINT);
        } else {
            // If it is less than 100 ms, draw a circle with only point X change
            graphics.setColor(ColorConstants.ABILITY_HOME_COLOR);
            // When drawing back Oval, only point X changes
            graphics.fillOval(pointX, DRAW_POINT, DRAW_POINT, DRAW_POINT);
        }
    }

    /**
     * draw Finished Touch
     *
     * @param graphics graphics
     * @param beforeTimeStamp beforeTimeStamp
     * @param pointX pointX
     * @param abilityEventAfter abilityEventAfter
     */
    private void drawFinishedTouch(Graphics2D graphics, long beforeTimeStamp,
        int pointX, AbilityEventInfo abilityEventAfter) {
        long afterTimeStamp = abilityEventAfter.getTimeStamp();
        int afterKeyType = abilityEventAfter.getKeyType();
        if (afterKeyType == 2 && afterTimeStamp - beforeTimeStamp >= CRITICAL_TIME) {
            int width = multiply(pixelPerTime, (int) (afterTimeStamp - beforeTimeStamp));
            // If it is less than 100 ms, draw a circle with only point X change
            graphics.setColor(ColorConstants.ABILITY_HOME_COLOR);
            // When drawing back Oval, only point X changes
            graphics.fillOval(pointX, DRAW_POINT, DRAW_POINT, DRAW_POINT);
            graphics.setColor(ColorConstants.ABILITY_HOME_OTHER_COLOR);
            // Draw rectangle (width pointX)
            graphics.fillRect(pointX + (DRAW_POINT / 2), DRAW_POINT, width, DRAW_POINT);
            // If it is greater than 100 ms, draw a circle of point X change and add an ellipse (pointX)
            graphics.fillOval(pointX + width, DRAW_POINT, DRAW_POINT, DRAW_POINT);
        } else {
            // If it is less than 100 ms, draw a circle with only point X change
            graphics.setColor(ColorConstants.ABILITY_HOME_COLOR);
            // When drawing back Oval, only point X changes
            graphics.fillOval(pointX, DRAW_POINT, DRAW_POINT, DRAW_POINT);
        }
    }

    /**
     * refresh ability Last Data
     *
     * @param sessionId sessionId
     */
    public void refreshLastData(long sessionId) {
        abilityActivityList =
            AbilityDataCache.getInstance().getActivityData(sessionId, startTime, endTime, firstTimeStamp);
    }

    /**
     * Show tooltip
     */
    private void showTooltip() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("showTooltip");
        }
        // If the X coordinate of the mouse is less than the X starting coordinate of chart, the tooltip is not required
        if (mousePoint == null || mousePoint.getX() < startCoordinate) {
            tooltip.hideTip();
            return;
        }
        // The current time to display
        int currentTime = ChartUtils.divide(mousePoint.getX() - startCoordinate, pixelPerTime) + startTime;
        // test

        int maxY = abilityHeight + ABILITY_HEIGHT;
        // Whether the mouse moves on the tooltip with data
        boolean isDataTooltip = mousePoint.getY() >= abilityHeight && mousePoint.getY() <= maxY;
        tooltip.showTip(this, currentTime, sliceInfoList, isDataTooltip);
    }

    /**
     * init Mouse Listener
     */
    private void initMouseListener() {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("initMouseListener");
        }
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                mousePoint = event.getPoint();
                // If the X coordinate of the mouse is less than the X starting coordinate of chart
                if (event.getX() < startCoordinate) {
                    enterChart = false;
                    tooltip.hideTip();
                    return;
                }
                enterChart = true;
                // Tooltip position needs to be refreshed when mouse move
                tooltip.followWithMouse(event);
                showTooltip();
                checkMouseForTooltip(event);
                ProfilerAppAbility.this.repaint();
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                mousePoint = null;
                enterChart = false;
                tooltip.hideTip();
                ProfilerAppAbility.this.repaint();
            }
        });
        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                mousePoint = event.getPoint();
                checkMouseForTooltip(event);
                ProfilerAppAbility.this.repaint();
            }
        });
    }

    /**
     * Check the position of the mouse to determine whether it is necessary to display the tool tip
     *
     * @param event MouseEvent
     */
    private void checkMouseForTooltip(MouseEvent event) {
        if (ProfilerLogManager.isInfoEnabled()) {
            LOGGER.info("checkMouseForTooltip");
        }
        // If the X coordinate of the mouse is less than the X starting coordinate of chart, the tooltip is not required
        if (event.getX() < startCoordinate) {
            enterChart = false;
            tooltip.hideTip();
            return;
        }
        if (!enterChart) {
            enterChart = true;
        }
        // Tooltip position needs to be refreshed when mouse move
        tooltip.followWithMouse(event);
        showTooltip();
    }

    /**
     * setMaxDisplayTime
     *
     * @param maxDisplayTime maxDisplayTime
     */
    public void setMaxDisplayTime(int maxDisplayTime) {
        this.maxDisplayTime = maxDisplayTime;
    }

    /**
     * setMinMarkInterval
     *
     * @param minMarkInterval minMarkInterval
     */
    public void setMinMarkInterval(int minMarkInterval) {
        this.minMarkInterval = minMarkInterval;
    }

    /**
     * getStartTime
     *
     * @return int
     */
    public int getStartTime() {
        return startTime;
    }

    /**
     * setStartTime
     *
     * @param startTime startTime
     */
    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    /**
     * getEndTime
     *
     * @return int
     */
    public int getEndTime() {
        return endTime;
    }

    /**
     * setEndTime
     *
     * @param endTime endTime
     */
    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public List<AbilityActivityInfo> getAbilityActivityList() {
        return abilityActivityList;
    }

    public void setAbilityActivityList(List<AbilityActivityInfo> abilityActivityList) {
        this.abilityActivityList = abilityActivityList;
    }

    public List<AbilityEventInfo> getEventInfoList() {
        return eventInfoList;
    }

    public void setEventInfoList(List<AbilityEventInfo> eventInfoList) {
        this.eventInfoList = eventInfoList;
    }
}
