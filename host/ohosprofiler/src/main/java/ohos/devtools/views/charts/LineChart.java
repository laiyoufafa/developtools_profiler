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

import ohos.devtools.views.charts.model.ChartDataModel;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.util.List;

import static ohos.devtools.views.charts.model.ChartType.LINE;
import static ohos.devtools.views.charts.utils.ChartConstants.DEFAULT_LINE_WIDTH;
import static ohos.devtools.views.charts.utils.ChartUtils.divideInt;
import static ohos.devtools.views.charts.utils.ChartUtils.multiply;

/**
 * Line chart
 *
 * @since 2021/5/19 16:39
 */
public class LineChart extends ProfilerChart {
    private static final int NUM_2 = 2;
    private static final int NUM_3 = 3;

    private boolean isZoom = true;
    private int lineWidth = -1;
    private String chartName;

    /**
     * Constructor
     *
     * @param bottomPanel ProfilerChartsView
     * @param name chart name
     */
    public LineChart(ProfilerChartsView bottomPanel, String name) {
        super(bottomPanel, name);
        this.chartName = name;
        chartType = LINE;
    }

    /**
     * Constructor
     *
     * @param bottomPanel bottomPanel
     * @param name name
     * @param isZoom isZoom
     * @param lineWidth lineWidth
     */
    public LineChart(ProfilerChartsView bottomPanel, String name, boolean isZoom, int lineWidth) {
        super(bottomPanel, name);
        chartType = LINE;
        this.chartName = name;
        this.isZoom = isZoom;
        this.lineWidth = lineWidth;
    }

    /**
     * Init legends
     */
    @Override
    protected void initLegends() {
    }

    /**
     * Build legends of chart
     *
     * @param lastModels Data on the far right side of the panel
     * @see "The legend shows the y value corresponding to the rightmost X axis, not the mouse hover position"
     */
    @Override
    protected void buildLegends(List<ChartDataModel> lastModels) {
    }

    /**
     * Paint chart
     *
     * @param graphics Graphics
     */
    @Override
    protected void paintChart(Graphics graphics) {
        if (dataMap == null || dataMap.size() == 0) {
            return;
        }
        Graphics2D graphicD = castGraphics2D(graphics);
        if (graphicD == null) {
            return;
        }
        graphicD.setColor(ColorConstants.RULER);
        graphicD.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Define dashed bar features
        if (lineWidth == -1) {
            lineWidth = DEFAULT_LINE_WIDTH;
        }
        BasicStroke bs = new BasicStroke(lineWidth);
        // Save original line features
        Stroke stroke = graphicD.getStroke();
        graphicD.setStroke(bs);
        // 循环绘制多条折线
        List<ChartDataModel> lines = dataMap.entrySet().iterator().next().getValue();
        if (lines != null && lines.size() > 0) {
            lines.forEach((line) -> paintLine(line.getIndex(), graphics));
        }
        // After drawing, the default format should be restored, otherwise the graphics drawn later are dotted lines
        BasicStroke defaultStroke = castBasicStroke(stroke);
        graphicD.setStroke(defaultStroke);
    }

    /**
     * Paint poly line
     *
     * @param index index of line chart
     * @param graphics Graphics
     */
    private void paintLine(int index, Graphics graphics) {
        int[] timeArray = dataMap.keySet().stream().mapToInt(Integer::valueOf).toArray();
        int length = timeArray.length;
        int[] pointX = new int[length];
        int[] pointY = new int[length];
        for (int timeIndex = 0; timeIndex < length; timeIndex++) {
            int time = timeArray[timeIndex];
            pointX[timeIndex] = startXCoordinate + multiply(pixelPerX, time - startTime);
            List<ChartDataModel> chartDataModels = dataMap.get(time);
            if (chartDataModels == null) {
                return;
            }
            ChartDataModel chartDataModel = chartDataModels.get(index);
            if (chartDataModel == null) {
                return;
            }
            int value = chartDataModel.getValue();
            // Update Y-axis maximum
            if (value > maxUnitY) {
                if (isZoom) {
                    maxUnitY = divideInt(value * NUM_3, NUM_2);
                } else {
                    maxUnitY = value;
                }
            }
            int pointResultY = yZero + multiply(pixelPerY, value);
            pointY[timeIndex] = pointResultY;
        }
        graphics.setColor(getCurrentLineColor(index, dataMap.get(timeArray[0])));
        graphics.drawPolyline(pointX, pointY, length);
    }

    /**
     * Build tooltip content
     *
     * @param showKey Key to show
     * @param actualKey The actual value of the key in the data map
     * @param newChart Is it a new chart
     */
    @Override
    protected void buildTooltip(int showKey, int actualKey, boolean newChart) {
    }

    /**
     * User defined events
     *
     * @param event MouseEvent
     */
    @Override
    protected void leftMouseClickEvent(MouseEvent event) {
    }

    /**
     * User defined events
     *
     * @param event MouseEvent
     */
    @Override
    protected void rightMouseClickEvent(MouseEvent event) {
    }

    /**
     * User defined events
     *
     * @param event MouseEvent
     */
    @Override
    protected void mouseDraggedEvent(MouseEvent event) {
    }

    /**
     * User defined events
     *
     * @param event MouseEvent
     */
    @Override
    protected void mouseReleaseEvent(MouseEvent event) {
    }

    /**
     * getChartName
     *
     * @return String
     */
    public String getChartName() {
        return chartName;
    }
}
