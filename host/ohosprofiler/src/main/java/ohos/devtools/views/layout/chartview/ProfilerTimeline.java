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

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import ohos.devtools.views.charts.utils.ChartUtils;
import ohos.devtools.views.layout.chartview.utils.OperationUtils;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.math.BigDecimal;

import static ohos.devtools.views.common.ColorConstants.TIMELINE_SCALE;
import static ohos.devtools.views.layout.chartview.utils.ChartViewConstants.TIMELINE_FONT_SIZE;
import static ohos.devtools.views.layout.chartview.utils.ChartViewConstants.TIMELINE_MARK_COUNTS;

/**
 * User-defined timeline component of Profiler
 *
 * @since 2021/11/22
 */
public class ProfilerTimeline extends JBPanel {
    /**
     * Offset in point
     */
    private static final int OFFSET_POINT = 5;

    /**
     * draw line top border
     */
    private static final int TOP_BORDER = 6;

    /**
     * Offset in seconds
     */
    private static final int OFFSET_SECONDS = 10;

    /**
     * Unit value of minutes converted to seconds
     */
    private static final int SECONDS_UNIT = 60;

    /**
     * Conversion rate for time conversion
     */
    private static final int CONVERSION_RATE = 1000;

    /**
     * The maximum time that can be displayed on the timeline
     */
    private int maxDisplayTime;

    /**
     * Minimum interval between timescales
     */
    private int minMarkInterval;

    /**
     * Right coordinates of timeline
     */
    private int right;

    /**
     * Top coordinates of timeline
     */
    private int top;

    /**
     * The start time of the timeline when drawing
     */
    private int startTime;

    /**
     * The end time of the timeline when drawing
     */
    private int endTime;

    /**
     * Coordinate axis X0 point when drawing timeline
     *
     * @see "It is the coordinate axis X0 point used in daily drawing, not the coordinate axis origin of Swing"
     */
    private int x0;

    /**
     * Coordinate axis Y0 point when drawing timeline
     *
     * @see "It is the coordinate axis Y0 point used in daily drawing, not the coordinate axis origin of Swing"
     */
    private int y0;

    /**
     * The start time offset when the scale is drawn when the timeline is full of panels
     */
    private int offsetTime = 0;

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
     * Constructor
     *
     * @param width Width of timeline
     * @param height Height of timeline
     */
    public ProfilerTimeline(int width, int height) {
        this.setPreferredSize(new Dimension(width, height));
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
    }

    /**
     * Initialization of points and scale information
     */
    private void initPoints() {
        // Determine the origin of the drawn axis
        x0 = this.getX();
        right = x0 + this.getWidth();
        top = this.getY();
        y0 = top + this.getHeight() - 1;
        if (right == 0 || maxDisplayTime == 0) {
            return;
        }
        // Calculate how many pixels a time unit takes
        pixelPerTime = OperationUtils.divide(right, maxDisplayTime);
        // If the current time is greater than the maximum time, the offset is calculated
        if (endTime > maxDisplayTime && minMarkInterval != 0) {
            startCoordinate = x0;
            // Determine if there is offset time
            if (endTime % minMarkInterval == 0) {
                offsetTime = 0;
            } else {
                // If the remainder of current time and minMarkInterval is not 0,
                // the offset is calculated: minimum interval - current time% minimum interval
                offsetTime = minMarkInterval - endTime % minMarkInterval;
            }
        } else {
            // If the current time is less than the maximum time,
            // the timeline needs to be drawn from the middle with an offset of 0
            offsetTime = 0;
            startCoordinate = x0 + right - OperationUtils.multiply(pixelPerTime, endTime);
        }
    }

    /**
     * Draw the coordinate axis of the timeline
     *
     * @param graphics Graphics
     */
    private void drawAxis(Graphics graphics) {
        graphics.setColor(TIMELINE_SCALE);
        // Draw the vertical line to the left of the timeline
        graphics.drawLine(x0, top, x0, y0);
        // Draw a horizontal line at the bottom of the timeline
        graphics.drawLine(x0, y0, right, y0);
        // The time line is drawn from offset time (the scale is drawn in fact)
        for (int drawTime = offsetTime; drawTime <= maxDisplayTime; drawTime += minMarkInterval) {
            int pointX = startCoordinate + ChartUtils.multiply(pixelPerTime, drawTime);
            // Calculate the actual time to show
            int showTime = startTime + drawTime;
            int result = (showTime / minMarkInterval) % TIMELINE_MARK_COUNTS;
            // Draw coordinate axis numbers and large scale every minMarkInterval
            graphics.setColor(TIMELINE_SCALE);
            if (result == 0) {
                // Draw a long scale
                graphics.drawLine(pointX, y0, pointX, top);
                graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, TIMELINE_FONT_SIZE));
                // Time after conversion
                String str = millisecondToTime(showTime);
                graphics.setColor(JBColor.foreground());
                graphics.drawString(str, pointX + OFFSET_POINT, y0 - OFFSET_SECONDS);
            } else {
                graphics.drawLine(pointX, top, pointX, top + TOP_BORDER);
            }
        }
    }

    /**
     * Draw the coordinate number of the timeline
     *
     * @param time time
     * @return String
     */
    private String millisecondToTime(int time) {
        String timeStr;
        int hour;
        int minute;
        int second;
        int millisecond;
        if (time <= 0) {
            return "0";
        } else {
            second = time / CONVERSION_RATE;
            minute = second / SECONDS_UNIT;
            millisecond = time % CONVERSION_RATE;
            if (second < SECONDS_UNIT) {
                timeStr = secondFormat(second) + "." + millisecondFormat(millisecond);
            } else if (minute < SECONDS_UNIT) {
                second = second % SECONDS_UNIT;
                timeStr = secondFormat(minute) + ":" + secondFormat(second) + "." + millisecondFormat(millisecond);
            } else {
                hour = minute / SECONDS_UNIT;
                minute = minute % SECONDS_UNIT;
                int num3600 = SECONDS_UNIT * SECONDS_UNIT;
                second = second - hour * num3600 - minute * SECONDS_UNIT;
                timeStr = secondFormat(hour) + ":" + secondFormat(minute) + ":" + secondFormat(second) + "."
                    + millisecondFormat(millisecond);
            }
        }
        return timeStr;
    }

    /**
     * Format conversion of hour, minute and second
     *
     * @param secondTime secondTime
     * @return String
     */
    private String secondFormat(int secondTime) {
        String formatTime;
        if (secondTime == 0) {
            formatTime = "00";
        } else if (secondTime > 0 && secondTime < OFFSET_SECONDS) {
            formatTime = Integer.toString(secondTime);
        } else {
            formatTime = "" + secondTime;
        }
        return formatTime;
    }

    /**
     * Millisecond format conversion
     *
     * @param millisecondTime millisecondTime
     * @return String
     */
    private String millisecondFormat(int millisecondTime) {
        String formatTime;
        if (millisecondTime == 0) {
            formatTime = "000";
        } else if (millisecondTime > 0 && millisecondTime < OFFSET_SECONDS) {
            formatTime = Integer.toString(millisecondTime);
        } else if (millisecondTime >= OFFSET_SECONDS && millisecondTime < OFFSET_SECONDS * OFFSET_SECONDS) {
            formatTime = Integer.toString(millisecondTime);
        } else {
            formatTime = "" + millisecondTime;
        }
        return formatTime;
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
}
