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
import ohos.devtools.views.charts.model.ChartLegendColorRect;
import ohos.devtools.views.charts.tooltip.LegendTooltip;
import ohos.devtools.views.charts.tooltip.TooltipItem;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.MonitorItemDetail;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;

import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static ohos.devtools.views.charts.model.ChartType.LINE;
import static ohos.devtools.views.charts.utils.ChartConstants.DEFAULT_LINE_WIDTH;
import static ohos.devtools.views.charts.utils.ChartConstants.NUM_2;
import static ohos.devtools.views.charts.utils.ChartConstants.NUM_3;
import static ohos.devtools.views.charts.utils.ChartUtils.divideInt;
import static ohos.devtools.views.charts.utils.ChartUtils.multiply;
import static ohos.devtools.views.common.MonitorItemDetail.NETWORK_CONN;
import static ohos.devtools.views.common.MonitorItemDetail.NETWORK_RCV;
import static ohos.devtools.views.common.MonitorItemDetail.NETWORK_SENT;

/**
 * @Description 折线图
 * @Date 2021/4/20 16:37
 **/
public class LineChart extends ProfilerChart {
    private final ChartLegendColorRect receivedColor = new ChartLegendColorRect();

    private final JLabel receivedLabel = new JLabel();

    private final ChartLegendColorRect sentColor = new ChartLegendColorRect();

    private final JLabel sentLabel = new JLabel();

    private final ChartLegendColorRect connectionsColor = new ChartLegendColorRect();

    private final JLabel connectionsLabel = new JLabel();

    /**
     * 构造函数
     *
     * @param bottomPanel 最底层父级面板
     */
    public LineChart(ProfilerChartsView bottomPanel) {
        super(bottomPanel);
        chartType = LINE;
        initLegends();
    }

    /**
     * 初始化图例
     */
    private void initLegends() {
        receivedColor.setOpaque(false);
        receivedLabel.setOpaque(false);
        sentColor.setOpaque(false);
        sentLabel.setOpaque(false);
        connectionsColor.setOpaque(false);
        connectionsLabel.setOpaque(false);
    }

    /**
     * 构建图例
     *
     * @param lastModels 面板上最右侧的数据
     * @see "图例展示的是面板上最右侧X轴对应的Y值，而非鼠标悬停处"
     */
    @Override
    protected void buildLegends(List<ChartDataModel> lastModels) {
        if (legends.getComponents().length == 0) {
            addLegendComponents();
        }
        Map<MonitorItemDetail, List<JComponent>> allItemLegendMap = initItemLegends();
        lastModels.forEach(model -> parseModelToLegend(model, allItemLegendMap));
        // Map中只剩下没有选择的监控项，需要隐藏
        allItemLegendMap.forEach((item, components) -> components.forEach(component -> component.setVisible(false)));
    }

    /**
     * 添加Legend组件
     */
    private void addLegendComponents() {
        legends.add(receivedColor);
        legends.add(receivedLabel);
        legends.add(sentColor);
        legends.add(sentLabel);
        legends.add(connectionsColor);
        legends.add(connectionsLabel);
    }

    /**
     * 初始化一个全量的Network图例的Map
     *
     * @return Map<监控项, 对应的图例组件>
     */
    private Map<MonitorItemDetail, List<JComponent>> initItemLegends() {
        Map<MonitorItemDetail, List<JComponent>> map = new HashMap<>();
        map.put(NETWORK_RCV, Arrays.asList(receivedColor, receivedLabel));
        map.put(NETWORK_SENT, Arrays.asList(sentColor, sentLabel));
        map.put(NETWORK_CONN, Arrays.asList(connectionsColor, connectionsLabel));
        return map;
    }

    /**
     * 处理数据，转化为图例
     *
     * @param model            数据
     * @param allItemLegendMap 内存图例的Map
     */
    private void parseModelToLegend(ChartDataModel model, Map<MonitorItemDetail, List<JComponent>> allItemLegendMap) {
        MonitorItemDetail item = MonitorItemDetail.getItemByName(model.getName());
        switch (item) {
            case NETWORK_RCV:
                refreshColorText(receivedColor, receivedLabel, model);
                // 如果model保存的为当前监控项，则显示其组件
                allItemLegendMap.get(NETWORK_RCV).forEach(component -> component.setVisible(true));
                // 组件设置为显示后，从Map中移除，循环完成后，Map中就只剩下没有选择的监控项，需要隐藏
                allItemLegendMap.remove(NETWORK_RCV);
                break;
            case NETWORK_SENT:
                refreshColorText(sentColor, sentLabel, model);
                allItemLegendMap.get(NETWORK_SENT).forEach(component -> component.setVisible(true));
                allItemLegendMap.remove(NETWORK_SENT);
                break;
            case NETWORK_CONN:
                refreshColorText(connectionsColor, connectionsLabel, model);
                allItemLegendMap.get(NETWORK_CONN).forEach(component -> component.setVisible(true));
                allItemLegendMap.remove(NETWORK_CONN);
                break;
            default:
                break;
        }
    }

    /**
     * 更新图例的颜色和文本
     *
     * @param colorRect 图例色块
     * @param label     图例文本标签
     * @param model     数据
     */
    private void refreshColorText(ChartLegendColorRect colorRect, JLabel label, ChartDataModel model) {
        // 文本标签
        String showValue = String.valueOf(model.getValue());
        String text = String.format(Locale.ENGLISH, "%s:%s%s", model.getName(), showValue, axisLabelY);
        colorRect.setColor(model.getColor());
        colorRect.setOpaque(false);
        legends.add(colorRect);
        if (!label.getText().equals(text)) {
            label.setText(text);
        }
        // 重要！设置透明，否则标尺会被盖住
        label.setOpaque(false);
        legends.add(label);
    }

    /**
     * 绘制图表
     *
     * @param graphics Graphics
     */
    @Override
    protected void paintChart(Graphics graphics) {
        if (dataMap == null || dataMap.size() == 0) {
            return;
        }
        Graphics graphic = graphics.create();
        Graphics2D graphicD = null;
        if (graphic instanceof Graphics2D) {
            graphicD = (Graphics2D) graphic;
        }
        // 获取原始线条特征
        Stroke stroke = graphicD.getStroke();
        BasicStroke defaultStroke = null;
        if (stroke instanceof BasicStroke) {
            defaultStroke = (BasicStroke) stroke;
        }
        // 定义虚线条特征
        BasicStroke bs = new BasicStroke(DEFAULT_LINE_WIDTH);
        graphicD.setColor(ColorConstants.RULER);
        graphicD.setStroke(bs);
        // 循环绘制多条折线
        List<ChartDataModel> lines = dataMap.entrySet().iterator().next().getValue();
        lines.forEach((line) -> paintLine(line.getIndex(), graphics));
        // 绘制完成后，要把默认格式还原
        graphicD.setStroke(defaultStroke);
    }

    /**
     * 绘制折线
     *
     * @param index    折线的index
     * @param graphics Graphics
     */
    private void paintLine(int index, Graphics graphics) {
        int[] timeArray = dataMap.keySet().stream().mapToInt(Integer::valueOf).toArray();
        int length = timeArray.length;
        int[] pointX = new int[length];
        int[] pointY = new int[length];
        for (int i = 0; i < length; i++) {
            int time = timeArray[i];
            pointX[i] = startXCoordinate + multiply(pixelPerX, time - startTime);
            int value = dataMap.get(time).get(index).getValue(); // 更新Y轴最大值
            if (value > maxUnitY) {
                // 更新y最大值的占用比例
                maxUnitY = divideInt(value * NUM_3, NUM_2);
            }
            int y = y0 + multiply(pixelPerY, value);
            pointY[i] = y;
        }
        // 每条线的颜色是一样的，所以这里只需要获取一次，不需要在下面的Map循环中每次都获取
        graphics.setColor(getCurrentLineColor(index, dataMap.get(timeArray[0])));
        graphics.drawPolyline(pointX, pointY, length);
    }

    /**
     * 构造悬浮提示框的内容
     *
     * @param showKey    要展示的Key
     * @param actualKey  实际在数据集合中取值的Key
     * @param isNewChart 是否为新Chart
     */
    @Override
    protected void buildTooltip(int showKey, int actualKey, boolean isNewChart) {
        List<TooltipItem> tooltipItems = buildTooltipItems(actualKey);
        LegendTooltip.getInstance().showTip(this, showKey + "", "", tooltipItems, isNewChart);
    }

    /**
     * 构造Tooltip
     *
     * @param time 当前时间
     * @return List
     */
    private List<TooltipItem> buildTooltipItems(int time) {
        List<TooltipItem> tooltipItems = new ArrayList<>();
        if (dataMap == null || dataMap.size() == 0 || dataMap.get(time) == null) {
            return tooltipItems;
        }
        for (ChartDataModel model : dataMap.get(time)) {
            String showValue = String.valueOf(model.getValue());
            String text = String.format(Locale.ENGLISH, "%s:%s%s", model.getName(), showValue, axisLabelY);
            TooltipItem tooltipItem = new TooltipItem(model.getColor(), text);
            tooltipItems.add(tooltipItem);
        }
        return tooltipItems;
    }
}
