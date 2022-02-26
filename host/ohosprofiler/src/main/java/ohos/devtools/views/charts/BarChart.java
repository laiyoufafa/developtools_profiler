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
import ohos.devtools.views.charts.model.ChartStandard;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static ohos.devtools.views.charts.model.ChartType.BAR;
import static ohos.devtools.views.charts.utils.ChartUtils.divideInt;
import static ohos.devtools.views.charts.utils.ChartUtils.multiply;

/**
 * Histogram, bar chart
 *
 * @since 2021/11/22
 */
public class BarChart extends ProfilerChart {
    private static final int REDUCTION_MULTIPLE = 2;
    private static final int EXPANSION_MULTIPLE = 3;

    private int default_width;

    /**
     * Constructor
     *
     * @param bottomPanel bottomPanel
     * @param name name
     * @param YAxisList YAxisList
     */
    public BarChart(ProfilerChartsView bottomPanel, String name, ArrayList YAxisList) {
        super(bottomPanel, name, YAxisList);
        chartType = BAR;
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
     * @param gra Graphics
     */
    @Override
    protected void paintChart(Graphics gra) {
        synchronized (gra) {
            ArrayList<BarDataModel> resultList = new ArrayList<>();
            ChartStandard cStandard = this.bottomPanel.getPublisher().getStandard();
            int maxDisplayTime = cStandard.getMaxDisplayMillis();
            int minMarkInterval = cStandard.getMinMarkInterval();
            // Calculate the length of each interval
            default_width = 5;
            if (dataMap == null || dataMap.size() == 0) {
                return;
            }
            for (int key : dataMap.keySet()) {
                int rectLeftX = startXCoordinate + multiply(pixelPerX, key - startTime - minMarkInterval / 2) + 2;
                int valueY;
                int sum = 0;
                // Folding state, summation
                if (super.fold) {
                    valueY = getListSum(dataMap.get(key));
                    if (valueY > 0) {
                        if (valueY > maxUnitY) {
                            maxUnitY = divideInt(valueY * EXPANSION_MULTIPLE, REDUCTION_MULTIPLE);
                        }
                        int pointY = yZero + multiply(pixelPerY, valueY);
                        resultList.add(new BarDataModel(rectLeftX, pointY, yZero - pointY, ColorConstants.CPU));
                    }
                } else {
                    if (dataMap.get(key) != null && dataMap.get(key).size() > 0) {
                        for (ChartDataModel chartDataModel : dataMap.get(key)) {
                            if (chartDataModel.getValue() > 0) {
                                sum += chartDataModel.getValue();
                                valueY = chartDataModel.getValue();
                                if (sum > maxUnitY) {
                                    maxUnitY = divideInt(sum * EXPANSION_MULTIPLE, REDUCTION_MULTIPLE);
                                }
                                int pointY = yZero + multiply(pixelPerY, valueY);
                                int sumPointY = yZero + multiply(pixelPerY, sum);
                                resultList.add(
                                    new BarDataModel(rectLeftX, sumPointY, yZero - pointY, chartDataModel.getColor()));
                            }
                        }
                    }
                }
            }
            paintAssistLine(resultList, gra);
        }
    }

    /**
     * getListSum
     *
     * @param models models
     * @return int
     */
    public int getListSum(List<ChartDataModel> models) {
        int sum = 0;
        if (models != null && models.size() > 0) {
            for (ChartDataModel chartDataModel : models) {
                sum += chartDataModel.getValue();
            }
        }
        return sum;
    }

    /**
     * Paint bar chart
     *
     * @param resultList The data list
     * @param gra Graphics
     */
    private void paintAssistLine(ArrayList<BarDataModel> resultList, Graphics gra) {
        if (!resultList.isEmpty()) {
            for (BarDataModel bdm : resultList) {
                gra.setColor(bdm.getColor());
                gra.fillRect(bdm.getPointX(), bdm.getPointY(), default_width - 4, bdm.getHeight());
            }
        }
    }

    /**
     * buildTooltipItem
     *
     * @param time time
     * @param dataMap dataMap
     * @param index index
     * @return ChartDataModel
     */
    private ChartDataModel buildTooltipItem(int time, LinkedHashMap<Integer, List<ChartDataModel>> dataMap, int index) {
        ChartDataModel chartDataModel = new ChartDataModel();
        if (dataMap == null || dataMap.size() == 0 || dataMap.get(time) == null) {
            return chartDataModel;
        }
        for (ChartDataModel model : dataMap.get(time)) {
            if (index == model.getIndex()) {
                chartDataModel = model;
            }
        }
        return chartDataModel;
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

    private class BarDataModel {
        private int pointX;
        private int pointY;
        private int width;
        private int height;
        private Color color;

        public BarDataModel(int pointX, int pointY, int height, Color color) {
            this.pointX = pointX;
            this.pointY = pointY;
            this.height = height;
            this.color = color;
        }

        public int getPointX() {
            return pointX;
        }

        public void setPointX(int pointX) {
            this.pointX = pointX;
        }

        public int getPointY() {
            return pointY;
        }

        public void setPointY(int pointY) {
            this.pointY = pointY;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }
    }
}
