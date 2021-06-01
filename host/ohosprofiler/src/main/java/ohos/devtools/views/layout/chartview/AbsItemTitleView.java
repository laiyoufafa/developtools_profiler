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

import ohos.devtools.views.charts.model.ChartDataRange;
import ohos.devtools.views.common.ColorConstants;
import ohos.devtools.views.common.LayoutConstants;
import ohos.devtools.views.common.OperationUtils;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.math.BigDecimal;

import static ohos.devtools.views.common.ColorConstants.ITEM_PANEL;
import static ohos.devtools.views.common.ViewConstants.LABEL_DEFAULT_HEIGHT;
import static ohos.devtools.views.common.ViewConstants.LABEL_DEFAULT_WIDTH;

/**
 * Chart标题栏的抽象父类
 *
 * @since 2021/4/22 15:42
 */
public abstract class AbsItemTitleView extends JPanel implements MouseListener, MouseMotionListener {
    /**
     * 最底层面板
     */
    protected final ProfilerChartsView bottomPanel;

    /**
     * 标题名称
     */
    private final String name;

    /**
     * 构造函数
     *
     * @param bottomPanel 最底层面板
     * @param name        指标项名称
     */
    public AbsItemTitleView(ProfilerChartsView bottomPanel, String name) {
        this.bottomPanel = bottomPanel;
        this.name = name;
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        // 标题颜色
        this.setBackground(ITEM_PANEL);
        // 这里宽度随意给一个值，渲染时Swing会自动把宽度铺满容器
        this.setPreferredSize(new Dimension(LABEL_DEFAULT_WIDTH, LABEL_DEFAULT_HEIGHT));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        drawMouseRuler(graphics);
        drawSelectedRuler(graphics);
    }

    /**
     * 绘制跟随鼠标的标尺
     *
     * @param graphics Graphics
     */
    private void drawMouseRuler(Graphics graphics) {
        int mouseX;
        Point mousePoint = getMousePosition();
        if (mousePoint == null) {
            // 没有鼠标位置时，绘制标尺的X坐标为0，或鼠标当前进入的组件为空，则不需要绘制
            if (this.bottomPanel.getRulerXCoordinate() == 0 || this.bottomPanel.getCurrentEntered() == null) {
                return;
            }

            // 绘制标尺的X坐标，以Chart和Timeline起点为0点
            mouseX = this.bottomPanel.getRulerXCoordinate();
        } else {
            mouseX = (int) mousePoint.getX();
            // 绘制标尺的X坐标，以Chart和Timeline起点为0点
            this.bottomPanel.setRulerXCoordinate(mouseX);
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
        float[] dash = {LayoutConstants.FLOAT_VALUE, 0f, LayoutConstants.FLOAT_VALUE};
        // 定义虚线条特征
        BasicStroke bs =
            new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, dash, LayoutConstants.FLOAT_VALUE);
        graphicD.setColor(ColorConstants.RULER);
        graphicD.setStroke(bs);
        graphicD.drawLine(mouseX, this.getY(), mouseX, this.getHeight());
        graphicD.setStroke(defaultStroke);
        // 把当前组件标记为已绘制，刷新其他组件的标尺
        this.bottomPanel.compRulerDrawn(this);
        this.bottomPanel.refreshCompRuler();
    }

    /**
     * 绘制框选时的标尺
     *
     * @param graphics Graphics
     */
    private void drawSelectedRuler(Graphics graphics) {
        ChartDataRange selectedRange = this.bottomPanel.getObserver().getStandard().getSelectedRange();
        if (selectedRange != null) {
            graphics.setColor(ColorConstants.RULER);
            int maxDisplay = this.bottomPanel.getObserver().getStandard().getMaxDisplayMillis();
            BigDecimal pixelPerTime = OperationUtils.divide(this.getWidth(), maxDisplay);
            int startCoordinate = calcStartCoordinate(pixelPerTime);

            int startTime = this.bottomPanel.getObserver().getStandard().getDisplayRange().getStartTime();
            int offsetStartTime = selectedRange.getStartTime() - startTime;
            int selectStartX = startCoordinate + OperationUtils.multiply(pixelPerTime, offsetStartTime);
            // 绘制框选起始点的竖线
            graphics.drawLine(selectStartX, this.getY(), selectStartX, this.getHeight());

            int offsetEndTime = selectedRange.getEndTime() - startTime;
            int selectEndX = startCoordinate + OperationUtils.multiply(pixelPerTime, offsetEndTime);
            // 绘制框选结束点的竖线
            graphics.drawLine(selectEndX, this.getY(), selectEndX, this.getHeight());
        }
    }

    /**
     * 出起始坐标startCoordinate，这一块和Timeline的一样
     *
     * @param pixelPerTime 比例
     * @return int
     */
    private int calcStartCoordinate(BigDecimal pixelPerTime) {
        int maxDisplay = this.bottomPanel.getObserver().getStandard().getMaxDisplayMillis();
        int displayEndTime = this.bottomPanel.getObserver().getStandard().getDisplayRange().getEndTime();
        int startCoordinate;
        if (displayEndTime > maxDisplay) {
            startCoordinate = 0;
        } else {
            startCoordinate = this.getWidth() - OperationUtils.multiply(pixelPerTime, displayEndTime);
        }
        return startCoordinate;
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
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
        // 当前组件需要绘制标尺，鼠标退出，则更新底层父级panel的currentEntered为null
        this.bottomPanel.setCurrentEntered(null);

        // 这里需要重绘一下当前界面，否则会残留有之前的ruler
        this.bottomPanel.resetRulerDrawStatus();
        this.bottomPanel.refreshCompRuler();
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        // 鼠标移动，则所有组件的标尺需要重新绘制
        this.bottomPanel.resetRulerDrawStatus();
        this.repaint();
        this.revalidate();
    }

    public String getName() {
        return name;
    }

}
