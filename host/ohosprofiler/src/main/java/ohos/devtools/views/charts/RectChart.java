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
import ohos.devtools.views.charts.model.ChartDataModel;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static ohos.devtools.views.charts.model.ChartType.RECT;
import static ohos.devtools.views.charts.utils.ChartUtils.multiply;

/**
 * RectChart chart
 */
public class RectChart extends ProfilerChart {
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

    private int threadId;

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    /**
     * Constructor
     *
     * @param bottomPanel ProfilerChartsView
     * @param name chart name
     */
    public RectChart(ProfilerChartsView bottomPanel, String name) {
        super(bottomPanel, name);
        chartType = RECT;
        top = 0;
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
        int[] timeArray = dataMap.keySet().stream().mapToInt(Integer::valueOf).toArray();
        int pointX = 0;
        int lastValue = 0;
        int endX = 0;
        for (int time : timeArray) {
            List<ChartDataModel> chartDataModelList = dataMap.get(time);
            ArrayList<RectDataModel> splitList = new ArrayList<RectDataModel>();
            if (chartDataModelList != null && !chartDataModelList.isEmpty()) {
                for (ChartDataModel chartDataModel : chartDataModelList) {
                    if (chartDataModel.getIndex() == threadId) {
                        int currentValue = chartDataModel.getValue();
                        pointX = startXCoordinate + multiply(pixelPerX, time - startTime);
                        endX = startXCoordinate + multiply(pixelPerX, timeArray[timeArray.length - 1] - startTime);
                        if (currentValue != lastValue) {
                            splitList.add(new RectDataModel(pointX, currentValue));
                            lastValue = currentValue;
                        }
                    }
                }
                for (int index = 0; index < splitList.size(); index++) {
                    switch (splitList.get(index).getThreadStatus()) {
                        case THREAD_RUNNING:
                            graphics.setColor(JBColor.GREEN);
                            break;
                        case THREAD_SLEEPING:
                            graphics.setColor(JBColor.background().darker());
                            break;
                        case THREAD_WAITING:
                            graphics.setColor(JBColor.YELLOW);
                            break;
                        default:
                            graphics.setColor(null);
                    }
                    graphics.fillRect(splitList.get(index).getPointX(), 0, endX, y0);
                }
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

    @Override
    protected void drawYAxis(Graphics graphics) {
    }

    private class RectDataModel {
        private int pointX;
        private int threadStatus;

        /**
         * Constructor
         *
         * @param pointX pointX
         * @param threadStatus threadStatus
         */
        RectDataModel(int pointX, int threadStatus) {
            this.pointX = pointX;
            this.threadStatus = threadStatus;
        }

        public int getPointX() {
            return pointX;
        }

        public void setPointX(int pointX) {
            this.pointX = pointX;
        }

        public int getThreadStatus() {
            return threadStatus;
        }

        public void setThreadStatus(int threadStatus) {
            this.threadStatus = threadStatus;
        }
    }
}
