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

package ohos.devtools.views.charts;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import ohos.devtools.views.charts.model.ChartDataModel;
import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartStandard;
import ohos.devtools.views.charts.model.ChartType;
import ohos.devtools.views.charts.tooltip.LegendTooltip;
import ohos.devtools.views.charts.utils.ChartUtils;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static java.awt.AlphaComposite.SRC_OVER;
import static java.awt.BasicStroke.CAP_BUTT;
import static java.awt.BasicStroke.JOIN_ROUND;
import static ohos.devtools.views.charts.utils.ChartConstants.CHART_HEADER_HEIGHT;
import static ohos.devtools.views.charts.utils.ChartConstants.DEFAULT_CHART_COLOR;
import static ohos.devtools.views.charts.utils.ChartConstants.DEFAULT_SELECT;
import static ohos.devtools.views.charts.utils.ChartConstants.SCALE_LINE_LEN;
import static ohos.devtools.views.charts.utils.ChartConstants.TRANSLUCENT_VALUE;
import static ohos.devtools.views.charts.utils.ChartConstants.Y_AXIS_STR_OFFSET_X;
import static ohos.devtools.views.charts.utils.ChartConstants.Y_AXIS_STR_OFFSET_Y;
import static ohos.devtools.views.charts.utils.ChartUtils.divide;
import static ohos.devtools.views.charts.utils.ChartUtils.divideInt;
import static ohos.devtools.views.charts.utils.ChartUtils.multiply;
import static ohos.devtools.views.common.ColorConstants.TIMELINE_SCALE;
import static ohos.devtools.views.common.LayoutConstants.FLOAT_VALUE;
import static ohos.devtools.views.common.LayoutConstants.INITIAL_VALUE;
import static ohos.devtools.views.charts.utils.ChartConstants.CHART_MAX_Y;
import static ohos.devtools.views.charts.utils.ChartConstants.CHART_SECTION_NUM_Y;

/**
 * Abstract parent  class of all charts
 */
public abstract class ProfilerChart extends JBPanel implements MouseListener, MouseMotionListener {
    private static final int NUM_5 = 5;

    /**
     * The start time of the x-axis when drawing
     */
    protected int startTime;

    /**
     * The end time of the x-axis when drawing
     */
    protected int endTime;

    /**
     * Enable/disable select function
     *
     * @see "true: chart fold, false: chart expand"
     * @see "chart expand: show ruler and tooltip"
     */
    protected boolean fold;

    /**
     * Enable/disable select function
     */
    protected boolean enableSelect;

    /**
     * Whether the mouse enters chart
     *
     * @see "Here is the entry into the paint chart, not the chart component"
     */
    protected boolean enterChart;

    /**
     * Chart name, used as key in selected map
     */
    protected final String chartName;

    /**
     * Right of chart
     */
    protected int right = 0;

    /**
     * Coordinate axis X0 point when drawing chart
     *
     * @see "It is the coordinate axis X0 point used in daily drawing, not the coordinate axis origin of Swing"
     */
    protected int x0 = 0;

    /**
     * Coordinate axis Y0 point when drawing chart
     *
     * @see "It is the coordinate axis Y0 point used in daily drawing, not the coordinate axis origin of Swing"
     */
    protected int y0 = 0;

    /**
     * The x-axis is the coordinate of the starting plot
     *
     * @see "The dynamic timeline and chart appear from right to left"
     */
    protected int startXCoordinate = 0;

    /**
     * The top of this panel
     */
    protected int panelTop = 0;

    /**
     * The maximum value that can be displayed on the x-axis
     */
    protected int maxDisplayX = 1;

    /**
     * Minimum scale interval on X-axis scale line
     */
    protected int minMarkIntervalX = 1;

    /**
     * Y-axis label
     */
    protected String axisLabelY = "";

    /**
     * Y-axis maximum unit
     */
    protected int maxUnitY = CHART_MAX_Y;

    /**
     * Number of y-axis scale segments
     */
    protected int sectionNumY = CHART_SECTION_NUM_Y;

    /**
     * Top of chart
     */
    protected int top = CHART_HEADER_HEIGHT;

    /**
     * The anchor point of dragging box selection, that is, the fixed point
     */
    protected int dragAnchorPoint = INITIAL_VALUE;

    /**
     * The starting point of dragging and dropping box selection
     */
    protected int dragStartPoint = INITIAL_VALUE;

    /**
     * The ending point of dragging and dropping box selection
     */
    protected int dragEndPoint = INITIAL_VALUE;

    /**
     * Bottom parent panel
     */
    protected final ProfilerChartsView bottomPanel;

    /**
     * YAxisLable
     */
    protected ArrayList<String> yAxisList = new ArrayList<>();

    /**
     * Data map
     *
     * @see "Key: time, Value: The values of chart at this point in time>"
     */
    protected volatile LinkedHashMap<Integer, List<ChartDataModel>> dataMap;

    /**
     * Legends
     */
    protected JBPanel legends;

    /**
     * Tooltip
     */
    protected LegendTooltip tooltip;

    /**
     * Enable/disable drag select
     */
    protected boolean enableDragSelect = false;

    /**
     * Chart type
     */
    protected ChartType chartType;

    /**
     * Number of pixels per X-axis unit
     */
    protected BigDecimal pixelPerX;

    /**
     * Number of pixels per Y-axis unit
     */
    protected BigDecimal pixelPerY;

    /**
     * Update when mouse moved
     *
     * @see "Use function getMousePosition() will be null sometime."
     */
    private Point mousePoint;

    /**
     * Whether the chart can be dragged or not
     */
    private boolean canDragged = false;

    /**
     * Whether the chart is being dragged or not
     */
    private boolean dragging = false;


    /**
     * Constructor
     *
     * @param bottomPanel ProfilerChartsView
     * @param name chart name
     */
    ProfilerChart(ProfilerChartsView bottomPanel, String name) {
        this.bottomPanel = bottomPanel;
        this.chartName = name;
        // 设置可透明显示
        this.setOpaque(false);
        this.setLayout(new BorderLayout());
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.tooltip = new LegendTooltip();
        // 添加图例组件的布局
        legends = new JBPanel(new FlowLayout(FlowLayout.RIGHT));
        legends.setOpaque(false);
        initLegends();
        this.add(legends, BorderLayout.NORTH);
    }

    /**
     * Constructor
     *
     * @param bottomPanel ProfilerChartsView
     * @param name chart name
     * @param yAxisList yAxisList
     */
    ProfilerChart(ProfilerChartsView bottomPanel, String name, ArrayList yAxisList) {
        this.bottomPanel = bottomPanel;
        this.chartName = name;
        this.yAxisList = yAxisList;
        // Set transparent display
        this.setOpaque(false);
        this.setLayout(new BorderLayout());
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.tooltip = new LegendTooltip();
        // 添加图例组件的布局
        legends = new JBPanel(new FlowLayout(FlowLayout.RIGHT));
        legends.setOpaque(false);
        initLegends();
        this.add(legends, BorderLayout.NORTH);
    }

    /**
     * Init legends
     */
    protected abstract void initLegends();

    /**
     * Build legends of chart
     *
     * @param lastModels Data on the far right side of the panel
     * @see "The legend shows the y value corresponding to the rightmost X axis, not the mouse hover position"
     */
    protected abstract void buildLegends(List<ChartDataModel> lastModels);

    /**
     * Paint chart
     *
     * @param graphics Graphics
     */
    protected abstract void paintChart(Graphics graphics);

    /**
     * Build tooltip content
     *
     * @param showKey Key to show
     * @param actualKey The actual value of the key in the data map
     * @param newChart Is it a new chart
     */
    protected abstract void buildTooltip(int showKey, int actualKey, boolean newChart);

    /**
     * User defined events
     *
     * @param event MouseEvent
     */
    protected abstract void leftMouseClickEvent(MouseEvent event);

    /**
     * User defined events
     *
     * @param event MouseEvent
     */
    protected abstract void rightMouseClickEvent(MouseEvent event);

    /**
     * User defined events
     *
     * @param event MouseEvent
     */
    protected abstract void mouseDraggedEvent(MouseEvent event);

    /**
     * User defined events
     *
     * @param event MouseEvent
     */
    protected abstract void mouseReleaseEvent(MouseEvent event);

    /**
     * Refresh chart
     *
     * @param startTime The start time of the x-axis when drawing
     * @param endTime The end time of the x-axis when drawing
     * @param dataMap Map of chart data
     */
    public void refreshChart(int startTime, int endTime, LinkedHashMap<Integer, List<ChartDataModel>> dataMap) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.dataMap = dataMap;
        refreshLegends();
        this.repaint();
        this.revalidate();
    }

    /**
     * Refresh legends
     */
    protected void refreshLegends() {
        // Find the closest value in the array of time
        int lastTime = getLastTime();
        List<ChartDataModel> models = dataMap.get(lastTime);
        if (models != null && !models.isEmpty()) {
            buildLegends(models);
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.setColor(JBColor.GRAY);
        initPoint();
        // Save the maximum value of current y-axis
        int crtMaxY = this.maxUnitY;
        paintChart(graphics);
        // If maxUnitY was update in the process of paint chart, it needs to be redrawn at this time. Otherwise,
        // the current drawing is still based on the old value, which will cause the maximum value to exceed the panel
        if (crtMaxY != this.maxUnitY) {
            repaint();
            return;
        }
        drawYAxis(graphics);
        if (enableSelect) {
            paintSelectedArea(graphics);
            drawSelectedRuler(graphics);
        }
        // chart expand: show ruler
        if (!fold) {
            drawMouseRuler(graphics);
        }
        // chart expand: show tooltip
        if (enterChart && !fold) {
            showTooltip(false);
        }
    }

    /**
     * Initialization of points and scale information
     */
    protected void initPoint() {
        // Calculate the top, bottom, left and right margins of the drawing area in the panel
        int left = 0;
        right = left + this.getWidth();
        int bottom = this.getHeight();
        x0 = left;
        y0 = bottom;
        // How many pixels does one unit of x-axis occupy
        pixelPerX = divide(right - left, maxDisplayX);
        // How many pixels does one unit of y-axis occupy
        pixelPerY = divide(top - y0, maxUnitY);
        // If the time exceeds maxDisplayX and continues to move forward, the drawing should start from x0
        if (endTime < maxDisplayX) {
            startXCoordinate = right - multiply(pixelPerX, endTime);
        } else {
            startXCoordinate = x0;
        }
    }

    /**
     * Draw Y-axis
     *
     * @param graphics Graphics
     */
    protected void drawYAxis(Graphics graphics) {
        // Calculate the length of each scale segment and the pixel increment when drawing circularly
        int interval = divideInt(maxUnitY, sectionNumY);
        int index = 0;
        for (int value = interval; value <= maxUnitY; value += interval) {
            int y = y0 + multiply(pixelPerY, value);
            // Draw Y-axis scale
            graphics.setColor(TIMELINE_SCALE);
            graphics.drawLine(x0, y, x0 + SCALE_LINE_LEN, y);
            // Draw the string of Y-axis scale
            String str = getYaxisLabelStr(value);
            graphics.setColor(JBColor.foreground());
            graphics.drawString(str, x0 + Y_AXIS_STR_OFFSET_X, y + Y_AXIS_STR_OFFSET_Y);
        }
    }

    /**
     * Gets the string in Y-axis units
     *
     * @param value int
     * @return String
     */
    protected String getYaxisLabelStr(int value) {
        return value == maxUnitY ? value + axisLabelY : value + "";
    }

    /**
     * Draw the box selection area
     *
     * @param graphics Graphics
     * @see "In fact, two translucent rectangles are drawn here, which are covered on the chart. With the mouse drag,
     * the size of the rectangle is changed to realize the box selection"
     */
    protected void paintSelectedArea(Graphics graphics) {
        if (this.bottomPanel.getPublisher().getStandard().getSelectedRange(chartName) == null) {
            return;
        }
        ChartDataRange selectedRange = this.bottomPanel.getPublisher().getStandard().getSelectedRange(chartName);
        int selectedStartTime = selectedRange.getStartTime();
        int selectedEndTime = selectedRange.getEndTime();
        int selectedStartX = multiply(pixelPerX, selectedStartTime - this.startTime) + startXCoordinate;
        int selectedEndX = multiply(pixelPerX, selectedEndTime - this.startTime) + startXCoordinate;
        int height = this.bottomPanel.getHeight();
        // Here, the brush is transparent, and the covered rectangle is drawn
        Graphics2D g2d = castGraphics2D(graphics);
        if (g2d != null) {
            g2d.setComposite(AlphaComposite.getInstance(SRC_OVER, TRANSLUCENT_VALUE));
        }
        graphics.setColor(ColorConstants.CHART_BG);
        graphics.fillRect(0, 0, selectedStartX, height);
        graphics.fillRect(selectedEndX, 0, right - selectedEndX, height);
    }

    /**
     * Draw a ruler that follows the mouse
     *
     * @param graphics Graphics
     */
    protected void drawMouseRuler(Graphics graphics) {
        if (mousePoint == null) {
            return;
        }

        Graphics2D g2d = castGraphics2D(graphics);
        if (g2d == null) {
            return;
        }
        // Define dashed bar features
        float[] dash = {FLOAT_VALUE, 0f, FLOAT_VALUE};
        BasicStroke bs = new BasicStroke(1, CAP_BUTT, JOIN_ROUND, 1.0f, dash, FLOAT_VALUE);
        // Save original line features
        Stroke stroke = g2d.getStroke();
        BasicStroke defaultStroke = castBasicStroke(stroke);
        g2d.setColor(ColorConstants.RULER);
        g2d.setStroke(bs);
        int mouseX = (int) mousePoint.getX();
        g2d.drawLine(mouseX, panelTop, mouseX, this.getHeight());
        // After drawing, the default format should be restored, otherwise the graphics drawn later are dotted lines
        g2d.setStroke(defaultStroke);
    }

    /**
     * castGraphics2D
     *
     * @param graphics graphics
     * @return Graphics2D
     */
    protected Graphics2D castGraphics2D(Graphics graphics) {
        Graphics2D graph = null;
        if (graphics instanceof Graphics2D) {
            graph = (Graphics2D) graphics;
        }
        return graph;
    }

    /**
     * castBasicStroke
     *
     * @param stroke stroke
     * @return BasicStroke
     */
    protected BasicStroke castBasicStroke(Stroke stroke) {
        BasicStroke basicStroke = null;
        if (stroke instanceof BasicStroke) {
            basicStroke = (BasicStroke) stroke;
        }
        return basicStroke;
    }

    /**
     * Draw a ruler when the chart is being selected
     *
     * @param graphics Graphics
     */
    protected void drawSelectedRuler(Graphics graphics) {
        ChartDataRange selectedRange = this.bottomPanel.getPublisher().getStandard().getSelectedRange(chartName);
        if (selectedRange != null) {
            graphics.setColor(ColorConstants.RULER);
            int startX = startXCoordinate + multiply(pixelPerX, selectedRange.getStartTime() - startTime);
            graphics.drawLine(startX, panelTop, startX, this.getHeight());
            if (!enableDragSelect) {
                drawInvertedTriangle(startX, graphics);
            }
            int endX = startXCoordinate + multiply(pixelPerX, selectedRange.getEndTime() - startTime);
            graphics.drawLine(endX, panelTop, endX, this.getHeight());
            if (!enableDragSelect) {
                drawInvertedTriangle(endX, graphics);
            }
        }
    }

    /**
     * Draw an inverted triangle
     *
     * @param bottomVertexX Vertex below inverted triangle
     * @param graphics Graphics
     */
    private void drawInvertedTriangle(int bottomVertexX, Graphics graphics) {
        Polygon polygon = new Polygon();
        polygon.addPoint(bottomVertexX, panelTop + NUM_5);
        polygon.addPoint(bottomVertexX - NUM_5, panelTop);
        polygon.addPoint(bottomVertexX + NUM_5, panelTop);
        polygon.addPoint(bottomVertexX, panelTop + NUM_5);
        graphics.setColor(JBColor.foreground());
        graphics.fillPolygon(polygon);
    }

    /**
     * Check the position of the mouse to determine whether it is necessary to display the tool tip
     *
     * @param event MouseEvent
     */
    protected void checkMouseForTooltip(MouseEvent event) {
        // If the X coordinate of the mouse is less than the X starting coordinate of chart, the tooltip is not required
        if (event.getX() < startXCoordinate) {
            enterChart = false;
            tooltip.hideTip();
            return;
        }
        if (!enterChart) {
            enterChart = true;
        }
        if (!fold) {
            // Tooltip position needs to be refreshed when mouse move
            tooltip.followWithMouse(event);
            showTooltip(true);
        }
    }

    /**
     * Show tooltip
     *
     * @param newChart Is it a new chart
     */
    protected void showTooltip(boolean newChart) {
        if (dragging && !enableDragSelect) {
            tooltip.hideTip();
            return;
        }
        // If the X coordinate of the mouse is less than the X starting coordinate of chart, the tooltip is not required
        if (mousePoint == null || mousePoint.getX() < startXCoordinate) {
            tooltip.hideTip();
            return;
        }
        if (dataMap == null || dataMap.size() == 0) {
            tooltip.hideTip();
            return;
        }
        int[] timeArray = dataMap.keySet().stream().mapToInt(Integer::valueOf).toArray();
        if (timeArray.length == 0) {
            return;
        }
        // The time to display
        // the time corresponding to the mouse = (mouse X coordinate - x start coordinate of drawing chart) / the
        // number of pixels corresponding to 1 unit of X axis + start time
        int showKey = divide(mousePoint.getX() - startXCoordinate, pixelPerX) + startTime;
        // The display time is not necessarily in the dataMap time array, and the closest time needs to be found,
        // and then the value is obtained through this time
        int actualKey = timeArray[ChartUtils.searchClosestIndex(timeArray, showKey)];
        buildTooltip(showKey, actualKey, newChart);
    }

    /**
     * Gets the chart color of the current data
     *
     * @param index chart data index
     * @param models Data list
     * @return Color
     */
    protected Color getCurrentLineColor(int index, List<ChartDataModel> models) {
        Color color = DEFAULT_CHART_COLOR;
        if (models == null || models.size() == 0) {
            tooltip.hideTip();
            return color;
        }
        for (ChartDataModel model : models) {
            if (model.getIndex() == index && model.getColor() != null) {
                color = model.getColor();
            }
        }
        return color;
    }

    /**
     * Get the index of the next chart data
     *
     * @param current Index of current chart data
     * @param models All chart's data models
     * @return Next chart's index
     */
    protected int getNextLineIndex(int current, List<ChartDataModel> models) {
        int next = INITIAL_VALUE;
        if (models == null || models.isEmpty()) {
            return next;
        }
        int size = models.size();
        for (int index = 0; index < size; index++) {
            ChartDataModel model = models.get(index);
            int newIndex = index + 1;
            if (model.getIndex() == current && newIndex < size) {
                next = models.get(index + 1).getIndex();
                break;
            }
        }
        return next;
    }

    /**
     * Find the sum of the values of all elements in the collection after the index is specified
     *
     * @param models Data list
     * @param index Specify index
     * @return int
     */
    public int getListSum(List<ChartDataModel> models, int index) {
        int sum = 0;
        if (index == INITIAL_VALUE || models == null) {
            return sum;
        }
        for (ChartDataModel model : models) {
            if (model.getIndex() < index) {
                continue;
            }
            sum += model.getValue();
        }
        return sum;
    }

    /**
     * Find the value of ChartDataModel in the list by index
     *
     * @param models Data list
     * @param index Specify index
     * @return int
     */
    protected int getModelValueByIndex(List<ChartDataModel> models, int index) {
        int value = 0;
        if (index == INITIAL_VALUE || models == null) {
            return value;
        }
        for (ChartDataModel model : models) {
            if (model.getIndex() == index) {
                value = model.getValue();
                break;
            }
        }
        return value;
    }

    /**
     * Get the last time on the current chart, because End time in standard does not necessarily have data
     *
     * @return int
     */
    protected int getLastTime() {
        // Gets the set of values for End time time
        int[] timeArray = dataMap.keySet().stream().mapToInt(Integer::valueOf).toArray();
        if (timeArray.length == 0) {
            return INITIAL_VALUE;
        }

        // In the array of time, find the closest value
        ChartDataRange range = bottomPanel.getPublisher().getStandard().getDisplayRange();
        return timeArray[ChartUtils.searchClosestIndex(timeArray, range.getEndTime())];
    }

    /**
     * Initialize Y-axis maximum units
     */
    public void initMaxUnitY() {
        this.maxUnitY = CHART_MAX_Y;
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        if (!enableSelect) {
            return;
        }
        int button = event.getButton();
        // If the left click point is less than the starting point, it will not be updated
        if (button == MouseEvent.BUTTON1 && event.getX() < startXCoordinate) {
            return;
        }
        if (button == MouseEvent.BUTTON1) {
            // Left click trigger box to select scene
            this.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
            mouseLeftClick(event);
        } else {
            // Right click to cancel the selection and clear the selection range
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            mouseRightClick(event);
        }
    }

    private void mouseLeftClick(MouseEvent event) {
        int mouseX = event.getX();
        // By default, 10px is selected in the back box of the mouse position.
        // If it exceeds the far right, 10px is selected from the starting point to the left
        int mouseXNumber = mouseX + DEFAULT_SELECT;
        if (mouseXNumber < right) {
            dragStartPoint = mouseX;
            dragEndPoint = mouseX + DEFAULT_SELECT;
        } else {
            dragStartPoint = right - DEFAULT_SELECT;
            dragEndPoint = right;
        }
        int selectStart = getTimeByMouseX(dragStartPoint);
        int selectEnd = getTimeByMouseX(dragEndPoint);
        this.bottomPanel.getPublisher().getStandard().updateSelectedStart(chartName, selectStart);
        this.bottomPanel.getPublisher().getStandard().updateSelectedEnd(chartName, selectEnd);
        // Pause refreshing data
        this.bottomPanel.getPublisher().pauseRefresh();
        // Refresh the interface manually once, otherwise the interface will not darken
        ChartDataRange range = bottomPanel.getPublisher().getStandard().getDisplayRange();
        bottomPanel.getPublisher().notifyRefresh(range.getStartTime(), range.getEndTime());
        // Call the method to be override
        leftMouseClickEvent(event);
    }

    private void mouseRightClick(MouseEvent event) {
        dragStartPoint = INITIAL_VALUE;
        dragEndPoint = INITIAL_VALUE;
        this.bottomPanel.getPublisher().getStandard().clearSelectedRange(chartName);
        // Manually refresh the interface once
        ChartDataRange range = bottomPanel.getPublisher().getStandard().getDisplayRange();
        bottomPanel.getPublisher().notifyRefresh(range.getStartTime(), range.getEndTime());
        // Call the method to be override
        rightMouseClickEvent(event);
    }

    @Override
    public void mousePressed(MouseEvent event) {
        if (!enableSelect) {
            return;
        }
        // If the pressed point is less than the starting point, it will not be updated
        if (event.getX() < startXCoordinate) {
            return;
        }
        ChartDataRange selectedRange = this.bottomPanel.getPublisher().getStandard().getSelectedRange(chartName);
        // selectedRange is null, which means that there is no click and the drag starts directly.
        // This scene is not considered for the moment
        if (selectedRange == null) {
            return;
        }
        int mouseX = event.getX();
        // After dragging the scroll bar, the value of dragStartPoint will change,
        // so we get point by time from select range
        dragStartPoint = getPointXByTime(selectedRange.getStartTime());
        dragEndPoint = getPointXByTime(selectedRange.getEndTime());
        if (Math.abs(mouseX - dragStartPoint) > NUM_5 && Math.abs(mouseX - dragEndPoint) > NUM_5) {
            canDragged = false;
            return;
        }
        // Determine the anchor point when dragging
        boolean isCloseToStart = Math.abs(mouseX - dragStartPoint) < NUM_5;
        if (isCloseToStart) {
            dragAnchorPoint = dragEndPoint;
            canDragged = true;
        }
        boolean isCloseToEnd = Math.abs(mouseX - dragEndPoint) < NUM_5;
        if (isCloseToEnd) {
            dragAnchorPoint = dragStartPoint;
            canDragged = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        if (!enableSelect) {
            return;
        }
        // Release event not processed with non left key
        if (event.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        /*
         * The trigger sequence of drag events is: press > drag > release
         * The trigger sequence of click events is: press > release > click
         */
        if (!dragging) {
            return;
        }
        // Call the method to be override
        mouseReleaseEvent(event);
        dragging = false;
    }

    @Override
    public void mouseEntered(MouseEvent event) {
        mousePoint = event.getPoint();
        // If the X coordinate of the mouse is less than the X starting coordinate of chart, the tooltip is not required
        if (event.getX() < startXCoordinate) {
            enterChart = false;
            tooltip.hideTip();
            return;
        }

        enterChart = true;
        if (!fold) {
            // Tooltip position needs to be refreshed when mouse move
            tooltip.followWithMouse(event);
            showTooltip(true);
        }
    }

    @Override
    public void mouseExited(MouseEvent event) {
        mousePoint = null;
        enterChart = false;
        tooltip.hideTip();
        this.repaint();
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        if (!enableSelect || !canDragged) {
            // Call repaint, mainly to update the ruler following the mouse movement
            this.repaint();
            return;
        }
        dragging = true;
        ChartDataRange selectedRange = this.bottomPanel.getPublisher().getStandard().getSelectedRange(chartName);
        if (selectedRange == null) {
            // Call repaint, mainly to update the ruler following the mouse movement
            this.repaint();
            return;
        }
        int mouseX = event.getX();
        if (mouseX > dragAnchorPoint) {
            // If the mouse position is larger than the anchor position, it means that you drag it on the right side of
            // the anchor and update end to the mouse point
            dragRightRuler(mouseX);
        } else {
            // If the mouse position is smaller than the anchor position, it means that it is dragging on the left side
            // of the anchor, then update end to the anchor and start to the mouse point
            dragLeftRuler(mouseX);
        }
        checkCursorStyle(mouseX);
        // Call the method to be override
        mouseDraggedEvent(event);
        this.repaint();
    }

    private void dragLeftRuler(int mouseX) {
        dragStartPoint = mouseX;
        ChartStandard standard = this.bottomPanel.getPublisher().getStandard();
        if (dragAnchorPoint != INITIAL_VALUE) {
            dragEndPoint = dragAnchorPoint;
            standard.updateSelectedEnd(chartName, getTimeByMouseX(dragAnchorPoint));
        }
        standard.updateSelectedStart(chartName, getTimeByMouseX(mouseX));
    }

    private void dragRightRuler(int mouseX) {
        dragEndPoint = mouseX;
        ChartStandard standard = this.bottomPanel.getPublisher().getStandard();
        if (dragAnchorPoint != INITIAL_VALUE) {
            dragStartPoint = dragAnchorPoint;
            standard.updateSelectedStart(chartName, getTimeByMouseX(dragAnchorPoint));
        }
        standard.updateSelectedEnd(chartName, getTimeByMouseX(mouseX));
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        mousePoint = event.getPoint();
        checkCursorStyle(event.getX());
        checkMouseForTooltip(event);
        this.repaint();
    }

    /**
     * Through the X coordinate of the mouse, calculate the corresponding time
     *
     * @param mouseX X coordinate of mouse
     * @return Corresponding X-axis time
     * @see "Note that the time corresponding to the time x axis calculated here is not necessarily in the keyset of
     * the datamap drawing chart. It may be between two values. When using it, you need to find the closest value to
     * the keyset of the datamap"
     */
    private int getTimeByMouseX(int mouseX) {
        // Time corresponding to mouse = (mouse X coordinate - x start coordinate of drawing chart) / number
        // of pixels corresponding to 1 unit of X axis + start time
        return ChartUtils.divide(mouseX - startXCoordinate, pixelPerX) + startTime;
    }

    /**
     * Calculate the corresponding X-axis coordinate through time
     *
     * @param time Current time
     * @return Corresponding X-axis coordinate value
     */
    private int getPointXByTime(int time) {
        // Corresponding coordinates on the mouse = number of pixels corresponding to 1 unit of X axis *
        // (current time - x start time of drawing chart) + X start coordinates of drawing chart
        return ChartUtils.multiply(pixelPerX, time - startTime) + startXCoordinate;
    }

    /**
     * Update the mouse style according to the coordinates of the mouse
     *
     * @param crtMouseX X-axis coordinates of mouse
     */
    private void checkCursorStyle(int crtMouseX) {
        ChartDataRange selectedRange = this.bottomPanel.getPublisher().getStandard().getSelectedRange(chartName);
        if (selectedRange == null) {
            return;
        }

        int selectStart = startXCoordinate + ChartUtils.multiply(pixelPerX, selectedRange.getStartTime() - startTime);
        int selectEnd = startXCoordinate + ChartUtils.multiply(pixelPerX, selectedRange.getEndTime() - startTime);
        // When the mouse is close to the ruler, the mouse will be reset
        if (Math.abs(selectStart - crtMouseX) < NUM_5) {
            this.setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
        } else if (Math.abs(selectEnd - crtMouseX) < NUM_5) {
            this.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
        } else {
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public LegendTooltip getTooltip() {
        return tooltip;
    }

    public void setMaxDisplayX(int maxDisplayX) {
        this.maxDisplayX = maxDisplayX;
    }

    public void setMinMarkIntervalX(int minMarkIntervalX) {
        this.minMarkIntervalX = minMarkIntervalX;
    }

    public String getAxisLabelY() {
        return axisLabelY;
    }

    public void setAxisLabelY(String axisLabelY) {
        this.axisLabelY = axisLabelY;
    }

    public void setMaxUnitY(int maxUnitY) {
        this.maxUnitY = maxUnitY;
    }

    public int getMaxUnitY() {
        return maxUnitY;
    }

    public void setSectionNumY(int sectionNumY) {
        this.sectionNumY = sectionNumY;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public ProfilerChartsView getBottomPanel() {
        return bottomPanel;
    }

    public void setEnableSelect(boolean enableSelect) {
        this.enableSelect = enableSelect;
    }

    public void setFold(boolean fold) {
        this.fold = fold;
    }

    public int getMinMarkIntervalX() {
        return minMarkIntervalX;
    }

    public void setEnableDragSelect(boolean enableDragSelect) {
        this.enableDragSelect = enableDragSelect;
    }
}
