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
import ohos.devtools.views.common.MonitorItemDetail;
import ohos.devtools.views.charts.tooltip.LegendTooltip;
import ohos.devtools.views.charts.tooltip.TooltipItem;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;

import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.Graphics;
import java.awt.Polygon;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static ohos.devtools.views.charts.model.ChartType.FILLED_LINE;
import static ohos.devtools.views.charts.utils.ChartConstants.INITIAL_VALUE;
import static ohos.devtools.views.charts.utils.ChartConstants.NUM_1024;
import static ohos.devtools.views.charts.utils.ChartConstants.NUM_2;
import static ohos.devtools.views.charts.utils.ChartConstants.NUM_3;
import static ohos.devtools.views.charts.utils.ChartUtils.divide;
import static ohos.devtools.views.charts.utils.ChartUtils.divideInt;
import static ohos.devtools.views.charts.utils.ChartUtils.multiply;
import static ohos.devtools.views.common.MonitorItemDetail.MEM_CODE;
import static ohos.devtools.views.common.MonitorItemDetail.MEM_GRAPHICS;
import static ohos.devtools.views.common.MonitorItemDetail.MEM_JAVA;
import static ohos.devtools.views.common.MonitorItemDetail.MEM_NATIVE;
import static ohos.devtools.views.common.MonitorItemDetail.MEM_OTHERS;
import static ohos.devtools.views.common.MonitorItemDetail.MEM_STACK;

/**
 * 已填充的折线图
 *
 * @since 2021/1/27 16:05
 */
public class FilledLineChart extends ProfilerChart {
    private final JLabel total = new JLabel();

    private final JLabel javaLabel = new JLabel();

    private final JLabel nativeLabel = new JLabel();

    private final JLabel graphicsLabel = new JLabel();

    private final JLabel stackLabel = new JLabel();

    private final JLabel codeLabel = new JLabel();

    private final JLabel othersLabel = new JLabel();

    private final JLabel allocated = new JLabel("-- Allocated:N/A");

    private final ChartLegendColorRect javaColorRect = new ChartLegendColorRect();

    private final ChartLegendColorRect nativeColorRect = new ChartLegendColorRect();

    private final ChartLegendColorRect graphicsColorRect = new ChartLegendColorRect();

    private final ChartLegendColorRect stackColorRect = new ChartLegendColorRect();

    private final ChartLegendColorRect codeColorRect = new ChartLegendColorRect();

    private final ChartLegendColorRect othersColorRect = new ChartLegendColorRect();

    /**
     * 构造函数
     *
     * @param bottomPanel 最底层父级面板
     */
    public FilledLineChart(ProfilerChartsView bottomPanel) {
        super(bottomPanel);
        chartType = FILLED_LINE;
        initLegendComponents();
    }

    /**
     * 初始化图例
     */
    private void initLegendComponents() {
        total.setOpaque(false);
        allocated.setOpaque(false);
        javaColorRect.setOpaque(false);
        javaLabel.setOpaque(false);
        nativeColorRect.setOpaque(false);
        nativeLabel.setOpaque(false);
        graphicsColorRect.setOpaque(false);
        graphicsLabel.setOpaque(false);
        stackColorRect.setOpaque(false);
        stackLabel.setOpaque(false);
        codeColorRect.setOpaque(false);
        codeLabel.setOpaque(false);
        othersColorRect.setOpaque(false);
        othersLabel.setOpaque(false);
    }

    /**
     * 构建图例
     *
     * @param lastModels 面板上最右侧的数据
     * @see "图例展示的是面板上最右侧X轴对应的Y值，而非鼠标悬停处"
     */
    @Override
    protected void buildLegends(List<ChartDataModel> lastModels) {
        if (lastModels.size() == 1 && ("Total").equals(lastModels.get(0).getName())) {
            buildMonitorViewLegend(lastModels);
        } else {
            // 否则为二级界面
            buildStageViewLegend(lastModels);
        }
    }

    /**
     * 构造一级界面的图例
     *
     * @param models 数据集合
     */
    private void buildMonitorViewLegend(List<ChartDataModel> models) {
        if (legends.getComponents().length == 0) {
            legends.add(total);
        }
        BigDecimal totalValue = divide(models.get(0).getValue(), NUM_1024);
        total.setText(totalValue + axisLabelY);
    }

    /**
     * 构造二级界面的图例
     *
     * @param models 数据集合
     */
    private void buildStageViewLegend(List<ChartDataModel> models) {
        if (legends.getComponents().length == 0) {
            addLegendComponents();
        }
        // 文本标签
        BigDecimal totalMB = divide(new BigDecimal(getListSum(models, 0)), new BigDecimal(NUM_1024));
        total.setText(String.format(Locale.ENGLISH, "Total:%s%s", totalMB, axisLabelY));

        Map<MonitorItemDetail, List<JComponent>> allItemLegendMap = initItemLegends();
        models.forEach(model -> parseModelToLegend(model, allItemLegendMap));
        // Map中只剩下没有选择的监控项，需要隐藏
        allItemLegendMap.forEach((item, components) -> components.forEach(component -> component.setVisible(false)));
    }

    /**
     * 添加Legend组件
     */
    private void addLegendComponents() {
        legends.add(total);
        legends.add(javaColorRect);
        legends.add(javaLabel);
        legends.add(nativeColorRect);
        legends.add(nativeLabel);
        legends.add(graphicsColorRect);
        legends.add(graphicsLabel);
        legends.add(stackColorRect);
        legends.add(stackLabel);
        legends.add(codeColorRect);
        legends.add(codeLabel);
        legends.add(othersColorRect);
        legends.add(othersLabel);
        legends.add(allocated);
    }

    /**
     * 初始化一个全量的内存图例的Map
     *
     * @return Map<监控项, 对应的图例组件>
     */
    private Map<MonitorItemDetail, List<JComponent>> initItemLegends() {
        Map<MonitorItemDetail, List<JComponent>> map = new HashMap<>();
        map.put(MEM_JAVA, Arrays.asList(javaColorRect, javaLabel));
        map.put(MEM_NATIVE, Arrays.asList(nativeColorRect, nativeLabel));
        map.put(MEM_GRAPHICS, Arrays.asList(graphicsColorRect, graphicsLabel));
        map.put(MEM_STACK, Arrays.asList(stackColorRect, stackLabel));
        map.put(MEM_CODE, Arrays.asList(codeColorRect, codeLabel));
        map.put(MEM_OTHERS, Arrays.asList(othersColorRect, othersLabel));
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
            case MEM_JAVA:
                refreshColorText(javaColorRect, javaLabel, model);
                // 如果model保存的为当前监控项，则显示其组件
                allItemLegendMap.get(MEM_JAVA).forEach(component -> component.setVisible(true));
                // 组件设置为显示后，从Map中移除，循环完成后，Map中就只剩下没有选择的监控项，需要隐藏
                allItemLegendMap.remove(MEM_JAVA);
                break;
            case MEM_NATIVE:
                refreshColorText(nativeColorRect, nativeLabel, model);
                allItemLegendMap.get(MEM_NATIVE).forEach(component -> component.setVisible(true));
                allItemLegendMap.remove(MEM_NATIVE);
                break;
            case MEM_GRAPHICS:
                refreshColorText(graphicsColorRect, graphicsLabel, model);
                allItemLegendMap.get(MEM_GRAPHICS).forEach(component -> component.setVisible(true));
                allItemLegendMap.remove(MEM_GRAPHICS);
                break;
            case MEM_STACK:
                refreshColorText(stackColorRect, stackLabel, model);
                allItemLegendMap.get(MEM_STACK).forEach(component -> component.setVisible(true));
                allItemLegendMap.remove(MEM_STACK);
                break;
            case MEM_CODE:
                refreshColorText(codeColorRect, codeLabel, model);
                allItemLegendMap.get(MEM_CODE).forEach(component -> component.setVisible(true));
                allItemLegendMap.remove(MEM_CODE);
                break;
            case MEM_OTHERS:
                refreshColorText(othersColorRect, othersLabel, model);
                allItemLegendMap.get(MEM_OTHERS).forEach(component -> component.setVisible(true));
                allItemLegendMap.remove(MEM_OTHERS);
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
        String showValue = divide(model.getValue(), NUM_1024).toString();
        String text = String.format(Locale.ENGLISH, "%s:%s%s", model.getName(), showValue, axisLabelY);
        colorRect.setColor(model.getColor());
        if (!label.getText().equals(text)) {
            label.setText(text);
        }
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
        // 循环绘制多条折线
        List<ChartDataModel> lines = dataMap.entrySet().iterator().next().getValue();
        lines.forEach((line) -> paintFilledLine(line.getIndex(), graphics));
    }

    /**
     * 绘制填充的折线
     *
     * @param index    折线的index
     * @param graphics Graphics
     * @see "堆叠方式：当前折线的点的y值是所有在当前折线之下的y值之和，y点添加完成后，还要从右向左添加他下面一条线的y点，构成闭合图形"
     */
    private void paintFilledLine(int index, Graphics graphics) {
        Polygon polygon = new Polygon();
        int[] timeArray = dataMap.keySet().stream().mapToInt(Integer::valueOf).toArray();
        // 每条线的颜色是一样的，所以这里只需要获取一次，不需要在下面的Map循环中每次都获取
        graphics.setColor(getCurrentLineColor(index, dataMap.get(timeArray[0])));
        // 堆叠方案：从左向后添加当前index折线的点，然后再从右向左添加next index折线的点
        // 从左向后添加当前index折线的点
        for (int time : timeArray) {
            int x = startXCoordinate + multiply(pixelPerX, time - startTime);
            // 折线是堆叠方式，y值应该为当前折线的值加上他下面所有折线的值之和
            int sum = getListSum(dataMap.get(time), index);
            // 更新Y轴最大值
            if (sum > maxUnitY) {
                // 更新y最大值的占用比例
                maxUnitY = divideInt(sum * NUM_3, NUM_2);
            }
            int y = y0 + multiply(pixelPerY, sum);
            polygon.addPoint(x, y);
        }
        // 绘制辅线
        paintAssistLine(index, polygon, timeArray);
        // 使用画笔填充多边形，形成折线
        graphics.fillPolygon(polygon);
    }

    private void paintAssistLine(int index, Polygon polygon, int[] timeArray) {
        // 如果nextLine不存在，表明index是最后一条线，这直接添加首尾的y0点即可，不需要循环计算所有点
        int nextLineIndex = getNextLineIndex(index, dataMap.get(timeArray[0]));
        if (nextLineIndex == INITIAL_VALUE) {
            int endX = startXCoordinate + multiply(pixelPerX, timeArray[timeArray.length - 1] - startTime);
            int startX = startXCoordinate + multiply(pixelPerX, timeArray[0] - startTime);
            polygon.addPoint(endX, y0);
            polygon.addPoint(startX, y0);
        } else {
            // 从右向左添加next index折线的点
            for (int i = timeArray.length - 1; i >= 0; i--) {
                // 计算数据在折线图上的X和Y点
                int x = startXCoordinate + multiply(pixelPerX, timeArray[i] - startTime);
                // 如果是堆叠方式，y值应该为当前折线的值加上他下面所有折线的值之和
                int sum = getListSum(dataMap.get(timeArray[i]), nextLineIndex);
                // 更新Y轴最大值
                if (sum > maxUnitY) {
                    // 更新y最大值的占用比例
                    maxUnitY = divideInt(sum * NUM_3, NUM_2);
                }
                int y = y0 + multiply(pixelPerY, sum);
                polygon.addPoint(x, y);
            }
        }
    }

    /**
     * 获取下一条折线的index
     *
     * @param current    当前折线的index
     * @param lineModels 所有折线的数据模型
     * @return 下一条折线的index
     */
    private int getNextLineIndex(int current, List<ChartDataModel> lineModels) {
        int next = INITIAL_VALUE;
        if (lineModels == null || lineModels.isEmpty()) {
            return next;
        }
        int size = lineModels.size();
        for (int index = 0; index < size; index++) {
            ChartDataModel lineModel = lineModels.get(index);
            int newIndex = index + 1;
            if (lineModel.getIndex() == current && newIndex < size) {
                next = lineModels.get(index + 1).getIndex();
                break;
            }
        }
        return next;
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
        String totalValue = calcTotal(actualKey);
        List<TooltipItem> tooltipItems = buildTooltipItems(actualKey);
        LegendTooltip.getInstance().showTip(this, showKey + "", totalValue, tooltipItems, isNewChart);
    }

    /**
     * 计算某一时间的Total值
     *
     * @param time 时间
     * @return Total值
     */
    private String calcTotal(int time) {
        List<ChartDataModel> models = dataMap.get(time);
        if (models == null || models.size() == 0) {
            return "";
        }
        // 这里拿到是KB，要转为MB
        int value = getListSum(models, 0);
        return divide(value, NUM_1024).toString();
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
            BigDecimal showValue = divide(model.getValue(), NUM_1024);
            String text = String.format(Locale.ENGLISH, "%s:%s%s", model.getName(), showValue, axisLabelY);
            TooltipItem tooltipItem = new TooltipItem(model.getColor(), text);
            tooltipItems.add(tooltipItem);
        }
        return tooltipItems;
    }

    /**
     * 求集合中指定index之后的所有元素的Value的和
     *
     * @param models 数据集合
     * @param index  指定index
     * @return int
     */
    private int getListSum(List<ChartDataModel> models, int index) {
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

}
