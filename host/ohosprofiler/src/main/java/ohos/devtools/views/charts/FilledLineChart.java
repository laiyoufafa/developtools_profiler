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
import ohos.devtools.views.layout.chartview.ProfilerChartsView;

import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;

import static ohos.devtools.views.charts.model.ChartType.FILLED_LINE;
import static ohos.devtools.views.charts.utils.ChartUtils.divideInt;
import static ohos.devtools.views.charts.utils.ChartUtils.multiply;
import static ohos.devtools.views.common.LayoutConstants.INITIAL_VALUE;

/**
 * Filled line chart
 */
public class FilledLineChart extends ProfilerChart {
    private static final int NUM_2 = 2;
    private static final int NUM_3 = 3;

    /**
     * Do line charts need to be stacked
     */
    private final boolean stacked;

    /**
     * Constructor
     *
     * @param bottomPanel ProfilerChartsView
     * @param name chart name
     * @param stacked Do line charts need to be stacked
     */
    public FilledLineChart(ProfilerChartsView bottomPanel, String name, boolean stacked) {
        super(bottomPanel, name);
        this.stacked = stacked;
        chartType = FILLED_LINE;
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
        List<ChartDataModel> lines = dataMap.entrySet().iterator().next().getValue();
        // Sort from small to large according to [index]
        lines.sort(Comparator.comparingInt(ChartDataModel::getIndex));
        lines.forEach((line) -> paintFilledLine(line.getIndex(), graphics));
    }

    /**
     * Draw filled poly line
     *
     * @param index index of line chart
     * @param graphics Graphics
     * @see "Stacked method: the y value of the current broken line point is the sum of all the Y values below
     * the current broken line. After the Y point is added, the Y point of the line below it should be added from
     * right to left to form a closed figure."
     * @see "If line charts do not need to be stacked, The model should go from [small to large] according to the index,
     * and the value should go from [large to small]. Otherwise, smaller values will be masked."
     */
    private void paintFilledLine(int index, Graphics graphics) {
        Polygon polygon = new Polygon();
        int[] timeArray = dataMap.keySet().stream().mapToInt(Integer::valueOf).toArray();
        graphics.setColor(getCurrentLineColor(index, dataMap.get(timeArray[0])));
        /*
         * Stacked scheme: add the point of the current index line from left to back, and then add the point of
         * the next index line from right to left
         */
        // Adds the point of the current polyline from left to right
        for (int time : timeArray) {
            int pointX = startXCoordinate + multiply(pixelPerX, time - startTime);
            int valueY;
            if (stacked) {
                valueY = getListSum(dataMap.get(time), index);
            } else {
                valueY = getModelValueByIndex(dataMap.get(time), index);
            }
            // If the current value exceeds the maximum, the maximum update value is 1.5 times the current value
            if (valueY > maxUnitY) {
                maxUnitY = divideInt(valueY * NUM_3, NUM_2);
            }
            int pointY = y0 + multiply(pixelPerY, valueY);
            polygon.addPoint(pointX, pointY);
        }
        // Draw the line below
        paintAssistLine(index, polygon, timeArray);
        // Use the brush to fill the polygon to form a filled poly line
        graphics.fillPolygon(polygon);
    }

    private void paintAssistLine(int index, Polygon polygon, int[] timeArray) {
        // If nextLine does not exist, it indicates that index is the last line. You can directly add Y0 points
        // at the beginning and end of the line. You don't need to cycle through all the points
        int nextLineIndex = getNextLineIndex(index, dataMap.get(timeArray[0]));
        if (nextLineIndex == INITIAL_VALUE || !stacked) {
            int endX = startXCoordinate + multiply(pixelPerX, timeArray[timeArray.length - 1] - startTime);
            int startX = startXCoordinate + multiply(pixelPerX, timeArray[0] - startTime);
            polygon.addPoint(endX, y0);
            polygon.addPoint(startX, y0);
        } else {
            // Add the point of the next index line from right to left
            for (int time = timeArray.length - 1; time >= 0; time--) {
                // Calculate the X and Y points of the data on the line chart
                int pointX = startXCoordinate + multiply(pixelPerX, timeArray[time] - startTime);
                int sum = getListSum(dataMap.get(timeArray[time]), nextLineIndex);
                // If the current value exceeds the maximum, the maximum update value is 1.5 times the current value
                if (sum > maxUnitY) {
                    maxUnitY = divideInt(sum * NUM_3, NUM_2);
                }
                int pointY = y0 + multiply(pixelPerY, sum);
                polygon.addPoint(pointX, pointY);
            }
        }
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
}
