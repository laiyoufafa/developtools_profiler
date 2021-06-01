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
import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.charts.model.ChartType;
import ohos.devtools.views.charts.tooltip.LegendTooltip;
import ohos.devtools.views.charts.utils.ChartUtils;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.layout.chartview.ProfilerChartsView;

import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;

import static java.awt.AlphaComposite.SRC_OVER;
import static java.awt.BasicStroke.CAP_BUTT;
import static java.awt.BasicStroke.JOIN_ROUND;
import static ohos.devtools.views.charts.utils.ChartConstants.CHART_HEADER_HEIGHT;
import static ohos.devtools.views.charts.utils.ChartConstants.DEFAULT_CHART_COLOR;
import static ohos.devtools.views.charts.utils.ChartConstants.INITIAL_VALUE;
import static ohos.devtools.views.charts.utils.ChartConstants.OPAQUE_VALUE;
import static ohos.devtools.views.charts.utils.ChartConstants.SCALE_LINE_LEN;
import static ohos.devtools.views.charts.utils.ChartConstants.TRANSLUCENT_VALUE;
import static ohos.devtools.views.charts.utils.ChartConstants.UNIT;
import static ohos.devtools.views.charts.utils.ChartConstants.Y_AXIS_STR_OFFSET_X;
import static ohos.devtools.views.charts.utils.ChartConstants.Y_AXIS_STR_OFFSET_Y;
import static ohos.devtools.views.charts.utils.ChartUtils.divide;
import static ohos.devtools.views.charts.utils.ChartUtils.divideInt;
import static ohos.devtools.views.charts.utils.ChartUtils.multiply;
import static ohos.devtools.views.common.LayoutConstants.FLOAT_VALUE;
import static ohos.devtools.views.common.ViewConstants.CHART_MAX_Y;
import static ohos.devtools.views.common.ViewConstants.CHART_SECTION_NUM_Y;

/**
 * Chart的抽象父类
 *
 * @since 2021/2/1 9:30
 */
public abstract class ProfilerChart extends JPanel implements MouseListener, MouseMotionListener {
    /**
     * 构建图例
     *
     * @param lastModels 面板上最右侧的数据
     * @see "图例展示的是面板上最右侧X轴对应的Y值，而非鼠标悬停处"
     */
    protected abstract void buildLegends(List<ChartDataModel> lastModels);

    /**
     * 绘制图表
     *
     * @param graphics Graphics
     */
    protected abstract void paintChart(Graphics graphics);

    /**
     * 构造悬浮提示框的内容
     *
     * @param showKey    要展示的Key
     * @param actualKey  实际在数据集合中取值的Key
     * @param isNewChart 是否为新Chart
     */
    protected abstract void buildTooltip(int showKey, int actualKey, boolean isNewChart);

    /**
     * 最底层父级面板
     */
    protected final ProfilerChartsView bottomPanel;

    /**
     * 图表类型
     */
    protected ChartType chartType;

    /**
     * 数据集合
     *
     * @see "Key:时间, Value:所有Chart的值>"
     */
    protected volatile LinkedHashMap<Integer, List<ChartDataModel>> dataMap;

    /**
     * 图例
     */
    protected JPanel legends;

    /**
     * X轴上可以展示的最大数量
     */
    protected int maxDisplayX = 1;

    /**
     * X轴刻度线上的最小刻度间隔
     */
    protected int minMarkIntervalX = 1;

    /**
     * X轴标签
     */
    protected String axisLabelX = "";

    /**
     * Y轴标签
     */
    protected String axisLabelY = "";

    /**
     * Y轴最大单位
     */
    protected int maxUnitY = CHART_MAX_Y;

    /**
     * Y轴坐标刻度分段数量
     */
    protected int sectionNumY = CHART_SECTION_NUM_Y;

    /**
     * 绘图时X轴的开始点
     */
    protected int startTime;

    /**
     * 绘图时X轴的结束点
     */
    protected int endTime;

    /**
     * 当前Chart的顶部
     */
    protected int top = 0;

    /**
     * 当前Chart的右侧
     */
    protected int right = 0;

    /**
     * 绘制Chart时的坐标轴X0点
     *
     * @see "是日常中绘图习惯的坐标轴X0点，非Swing绘图的坐标轴原点"
     */
    protected int x0 = 0;

    /**
     * 绘制Chart时的坐标轴Y0点
     *
     * @see "是日常中绘图习惯的坐标轴Y0点，非Swing绘图的坐标轴原点"
     */
    protected int y0 = 0;

    /**
     * X轴起始绘图的坐标，因为动态Chart从右往左出现
     */
    protected int startXCoordinate = 0;

    /**
     * 每个X轴单位占用的像素数
     */
    protected BigDecimal pixelPerX;

    /**
     * 每个Y轴单位占用的像素数z
     */
    protected BigDecimal pixelPerY;

    /**
     * 鼠标是否进入Chart
     *
     * @see "这里是指进入绘制出来的Chart，而非Chart组件"
     */
    protected boolean isEnterChart;

    /**
     * 构造函数
     *
     * @param bottomPanel 最底层父级面板
     */
    public ProfilerChart(ProfilerChartsView bottomPanel) {
        this.bottomPanel = bottomPanel;
        // 设置可透明显示
        this.setOpaque(false);
        this.setLayout(new BorderLayout());
        // 添加图例组件的布局
        legends = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        legends.setOpaque(false);
        this.add(legends, BorderLayout.NORTH);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    /**
     * 更新Chart的起始、结束时间以及数据集合，实现刷新Chart的功能
     *
     * @param startTime 起始时间
     * @param endTime   结束时间
     * @param dataMap   数据集合
     */
    public void refreshChart(int startTime, int endTime, LinkedHashMap<Integer, List<ChartDataModel>> dataMap) {
        // 保存数据，并重绘界面
        this.startTime = startTime;
        this.endTime = endTime;
        this.dataMap = dataMap;
        refreshLegends();
        this.repaint();
        this.revalidate();
    }

    /**
     * 刷新图例
     */
    protected void refreshLegends() {
        // 在时间的数组中，找到最接近的值
        int lastTime = getLastTime();
        List<ChartDataModel> models = dataMap.get(lastTime);
        if (models != null && !models.isEmpty()) {
            buildLegends(models);
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        // 设置画笔颜色
        graphics.setColor(JBColor.GRAY);
        Graphics2D graph = null;
        if (graphics instanceof Graphics2D) {
            graph = (Graphics2D) graphics;
        }
        // 设置两个目标重叠时的混合类型和透明度
        graph.setComposite(AlphaComposite.getInstance(SRC_OVER, OPAQUE_VALUE));
        // 初始化坐标和比例尺等信息
        initPoint();
        // 绘制Y轴
        drawYAxis(graphics);
        // 记录当前Y轴最大值
        int crtMaxY = this.maxUnitY;
        // 绘制图表
        paintChart(graphics);
        // 如果在画线过程中maxUnitY刷新，这时需要重绘一次，否则当前仍然以旧值绘制，会导致最大值超过面板大小
        if (crtMaxY != this.maxUnitY) {
            repaint();
            return;
        }
        // 绘制框选区域
        paintSelectedArea(graphics);
        // 还原透明度
        graph.setComposite(AlphaComposite.getInstance(SRC_OVER, OPAQUE_VALUE));
        // 绘制跟随鼠标的标尺
        drawMouseRuler(graphics);
        // 绘制框选时的标尺
        drawSelectedRuler(graphics);
        if (isEnterChart) {
            // 展示Tooltip提示框
            showTooltip(false);
        }
    }

    /**
     * 初始化坐标和比例尺等信息
     */
    protected void initPoint() {
        // 计算Panel内绘图区的上下左右边距
        int left = this.getX();
        top = this.getY() + CHART_HEADER_HEIGHT;
        // 整体稍微往下移动一点
        right = left + this.getWidth();
        int bottom = this.getHeight();
        // 确定绘制出的坐标轴的原点
        // 绘制出的坐标轴的X0点
        x0 = left;
        // 绘制出的坐标轴的Y0点
        y0 = bottom;
        // 计算出X轴1个单位占用多少像素
        pixelPerX = divide(right - left, maxDisplayX);
        // 计算出Y轴1个单位占用多少像素
        pixelPerY = divide(top - y0, maxUnitY);
        // 如果时间铺满了面板并继续往前走，绘制图形的时候应该需要从x0开始绘制，不需要像画坐标轴刻度那样存在偏移
        if (endTime < maxDisplayX) {
            startXCoordinate = right - multiply(pixelPerX, endTime);
        } else {
            startXCoordinate = x0;
        }
    }

    /**
     * 绘制Y轴
     *
     * @param graphics Graphics
     */
    protected void drawYAxis(Graphics graphics) {
        // 前2个参数为线段起点左边，后2个参数为线段结束坐标，从上向下。
        graphics.drawLine(x0, this.getY(), x0, y0);
        // 计算每段刻度的长度，同时也是循环绘制时的像素增量
        int interval = divideInt(maxUnitY, sectionNumY);
        for (int i = interval; i <= maxUnitY; i += interval) {
            int y = y0 + multiply(pixelPerY, i);
            // 绘制Y轴刻度，其实就是绘制一条短横线
            graphics.drawLine(x0, y, x0 + SCALE_LINE_LEN, y);
            // 绘制Y轴刻度值
            String str = i == maxUnitY ? divide(i, UNIT) + " " + axisLabelY : divide(i, UNIT) + "";
            graphics.drawString(str, x0 + Y_AXIS_STR_OFFSET_X, y + Y_AXIS_STR_OFFSET_Y);
        }
    }

    /**
     * 绘制框选区域，这里实际是绘制了两块半透明的矩形，盖在Chart上，随着鼠标拖动改变矩形大小实现框选
     *
     * @param graphics Graphics
     */
    protected void paintSelectedArea(Graphics graphics) {
        if (this.bottomPanel.getObserver().getStandard().getSelectedRange() == null) {
            return;
        }
        ChartDataRange selectedRange = this.bottomPanel.getObserver().getStandard().getSelectedRange();
        int endTimeX = selectedRange.getEndTime();
        int startTimeX = selectedRange.getStartTime();
        int startX = multiply(pixelPerX, startTimeX - this.startTime) + startXCoordinate;
        int endX = multiply(pixelPerX, endTimeX - this.startTime) + startXCoordinate;
        int height = this.bottomPanel.getHeight();
        // 这里把画笔透明降低绘制遮盖的矩形
        ((Graphics2D) graphics).setComposite(AlphaComposite.getInstance(SRC_OVER, TRANSLUCENT_VALUE));
        graphics.setColor(ColorConstants.CHART_BG);
        graphics.fillRect(0, 0, startX, height);
        graphics.fillRect(endX, 0, endTime, height);
    }

    /**
     * 绘制跟随鼠标的标尺
     *
     * @param graphics Graphics
     */
    protected void drawMouseRuler(Graphics graphics) {
        int mouseX;
        Point mousePoint = getMousePosition();
        if (mousePoint == null) {
            // 没有鼠标位置时，绘制标尺的X坐标为0，或鼠标当前进入的组件为空，则不需要绘制
            if (this.bottomPanel.getRulerXCoordinate() == 0 || this.bottomPanel.getCurrentEntered() == null) {
                return;
            }
            mouseX = this.bottomPanel.getRulerXCoordinate();
        } else {
            mouseX = (int) mousePoint.getX();
            this.bottomPanel.setRulerXCoordinate(mouseX);
        }
        Graphics2D g2d = null;
        if (graphics instanceof Graphics2D) {
            g2d = (Graphics2D) graphics;
        }
        // 保存原始线条特征
        BasicStroke defaultStroke = null;

        Stroke stroke = g2d.getStroke();
        if (stroke instanceof BasicStroke) {
            defaultStroke = (BasicStroke) stroke;
        }
        float[] dash = {FLOAT_VALUE, 0f, FLOAT_VALUE};
        // 定义虚线条特征
        BasicStroke bs = new BasicStroke(1, CAP_BUTT, JOIN_ROUND, 1.0f, dash, FLOAT_VALUE);
        g2d.setColor(ColorConstants.RULER);
        g2d.setStroke(bs);
        g2d.drawLine(mouseX, this.getY(), mouseX, this.getHeight());
        // 绘制完成后，要把默认格式还原，否则后面绘制的图形都是虚线
        g2d.setStroke(defaultStroke);
        // 把当前组件标记为已绘制，刷新其他组件的标尺
        this.bottomPanel.compRulerDrawn(this);
        this.bottomPanel.refreshCompRuler();
    }

    /**
     * 绘制框选时的标尺
     *
     * @param graphics Graphics
     */
    protected void drawSelectedRuler(Graphics graphics) {
        ChartDataRange selectedRange = this.bottomPanel.getObserver().getStandard().getSelectedRange();
        if (selectedRange != null) {
            graphics.setColor(ColorConstants.RULER);
            int startX = startXCoordinate + multiply(pixelPerX, selectedRange.getStartTime() - startTime);
            graphics.drawLine(startX, this.getY(), startX, this.getHeight());
            int endX = startXCoordinate + multiply(pixelPerX, selectedRange.getEndTime() - startTime);
            graphics.drawLine(endX, this.getY(), endX, this.getHeight());
        }
    }

    /**
     * 检查鼠标位置，判断是否需要显示Tooltip
     *
     * @param mouseEvent MouseEvent
     */
    protected void checkMouseForTooltip(MouseEvent mouseEvent) {
        // 如果鼠标X坐标小于Chart的X起始坐标，则不需要Tooltip
        if (mouseEvent.getX() < startXCoordinate) {
            isEnterChart = false;
            LegendTooltip.getInstance().hideTip();
        } else {
            // 鼠标移动，Tooltip位置需要刷新
            LegendTooltip.getInstance().followWithMouse(mouseEvent);
            if (!isEnterChart) {
                isEnterChart = true;
                showTooltip(true);
            }
        }
    }

    /**
     * 显示悬浮提示框
     *
     * @param isNewChart 是否为新Chart
     */
    protected void showTooltip(boolean isNewChart) {
        Point mousePoint = getMousePosition();
        // 如果鼠标X坐标小于Chart的X起始坐标，则不需要Tooltip
        if (mousePoint == null || mousePoint.getX() < startXCoordinate) {
            LegendTooltip.getInstance().hideTip();
            return;
        }
        if (dataMap == null || dataMap.size() == 0) {
            LegendTooltip.getInstance().hideTip();
            return;
        }
        int[] timeArray = dataMap.keySet().stream().mapToInt(Integer::valueOf).toArray();
        if (timeArray.length == 0) {
            return;
        }
        // 要展示的时间，也就是鼠标上对应的时间 = (鼠标X坐标 - 绘制Chart的X起始坐标) / X轴1个单位对应的像素数 + 起始时间
        int showKey = divide(mousePoint.getX() - startXCoordinate, pixelPerX) + startTime;
        // 展示时间不一定就在dataMap的时间数组中，需要找到最接近的时间，然后通过这个时间拿到value
        int actualKey = timeArray[ChartUtils.searchClosestIndex(timeArray, showKey)];
        buildTooltip(showKey, actualKey, isNewChart);
    }

    /**
     * 获取当前数据的颜色
     *
     * @param index  int
     * @param models 数据集合
     * @return Color
     */
    protected Color getCurrentLineColor(int index, List<ChartDataModel> models) {
        Color color = DEFAULT_CHART_COLOR;
        if (models == null || models.size() == 0) {
            LegendTooltip.getInstance().hideTip();
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
     * 获取当前Chart上的最后一个时间，因为Standard中的endTime，不一定有数据
     *
     * @return int
     */
    protected int getLastTime() {
        // 获取endTime时刻的数值的集合
        int[] timeArray = dataMap.keySet().stream().mapToInt(Integer::valueOf).toArray();
        if (timeArray.length == 0) {
            return INITIAL_VALUE;
        }

        // 在时间的数组中，找到最接近的值
        ChartDataRange range = bottomPanel.getObserver().getStandard().getDisplayRange();
        return timeArray[ChartUtils.searchClosestIndex(timeArray, range.getEndTime())];
    }

    /**
     * 初始化Y轴最大单位
     */
    public void initMaxUnitY() {
        this.maxUnitY = CHART_MAX_Y;
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
        // 当前组件需要绘制标尺，鼠标进入，则更新底层父级panel的currentEntered
        this.bottomPanel.setCurrentEntered(this);
        // 如果鼠标X坐标小于Chart的X起始坐标，则不需要Tooltip
        if (mouseEvent.getX() < startXCoordinate) {
            isEnterChart = false;
            LegendTooltip.getInstance().hideTip();
        } else {
            isEnterChart = true;
            // 鼠标移动，Tooltip位置需要刷新
            LegendTooltip.getInstance().followWithMouse(mouseEvent);
            showTooltip(true);
        }
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        // 当前组件需要绘制标尺，鼠标退出，则更新底层父级panel的currentEntered为null
        this.bottomPanel.setCurrentEntered(null);
        // 这里需要重绘一下当前界面，否则会残留有之前的ruler
        this.bottomPanel.resetRulerDrawStatus();
        this.bottomPanel.refreshCompRuler();
        isEnterChart = false;
        LegendTooltip.getInstance().hideTip();
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        // 这为了防止鼠标从Row底部移上来时，仍然是双箭头resize状态引起用户误解，这里把鼠标置为默认状态
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        // 鼠标移动，则所有组件的标尺需要重新绘制
        this.bottomPanel.resetRulerDrawStatus();
        checkMouseForTooltip(mouseEvent);
        this.repaint();
    }

    public ChartType getChartType() {
        return chartType;
    }

    public void setMaxDisplayX(int maxDisplayX) {
        this.maxDisplayX = maxDisplayX;
    }

    public void setMinMarkIntervalX(int minMarkIntervalX) {
        this.minMarkIntervalX = minMarkIntervalX;
    }

    public void setAxisLabelX(String axisLabelX) {
        this.axisLabelX = axisLabelX;
    }

    public void setAxisLabelY(String axisLabelY) {
        this.axisLabelY = axisLabelY;
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
}
